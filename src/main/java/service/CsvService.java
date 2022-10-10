package service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import model.CsvBean;
import model.DownloadElement;
import model.SessionBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Service
public class CsvService {

    private static final String STATE_FILE = "download_state.csv";
    private static final String DIR = "config";

    private static final int MAX_LIST_SIZE = 10;

    private Path stateFilePath;

    private List<CsvBean> beans;

    public CsvService() {
        this.beans = new LinkedList<>();
        this.stateFilePath = Path.of(DIR).resolve(Path.of(STATE_FILE));
    }

    @PostConstruct
    private void initDirectory() {
        Path dir = Path.of(DIR);
        if (!Files.exists(dir)) {
            System.out.println("[ init_csv ] directory not found");
            try {
                System.out.println("[ init_csv ] creating directory: '" + dir + "'");
                Files.createDirectory(dir);
            } catch (IOException e) {
                System.out.println("[ init_csv ] " + e.getMessage());
            }
        }
        stateFilePath = dir.resolve(Path.of(STATE_FILE));
        if (Files.exists(stateFilePath)) {
            System.out.println("[ init_csv ] file exists");
            beans = restoreState(stateFilePath, SessionBean.class);
            beans.sort(Comparator.comparing(o -> ((SessionBean) o).getTimestamp()));
            System.out.println("[ init_csv ] state file loaded [OK]");
        } else {
            System.out.println("[ init_csv ] no state file found");
        }
    }

    private List<CsvBean> restoreState(Path path, Class<? extends CsvBean> clazz) {
        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean<CsvBean> csvToBean = new CsvToBeanBuilder<CsvBean>(reader)
                    .withQuoteChar('\'')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withType(clazz)
                    .build();
            return csvToBean.parse();
        } catch (IOException e) {
            System.out.println("[ restore_state ] " + e.getMessage());
        }
        // return empty list in case of error
        return new ArrayList<>();
    }

    public void saveState(DownloadElement downloadElement) {
        createCsvObject(downloadElement);
//        restoreState(p, SessionBean.class);
//        System.out.println("after restore bean: " + beans);
        saveState(stateFilePath);
    }

    private void saveState(Path path) {
        try (Writer writer = new FileWriter(path.toFile())) {
            System.out.println("[ save_state ] saving...");
            StatefulBeanToCsv<CsvBean> beanToCsv = new StatefulBeanToCsvBuilder<CsvBean>(writer)
                    .withQuotechar('\'')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();
            beanToCsv.write(beans);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            System.out.println("[ save_state ] " + e.getMessage());
        }
    }

    /*
     * Parse DownloadElement to CSV Bean, created bean is added to end of bean list
     * */
    private void createCsvObject(DownloadElement downloadElement) {
        // check if element exists, update existing
        if (beans.size() == MAX_LIST_SIZE) beans.remove(0);
        SessionBean sessionBean = findAndDeleteExistingBean(downloadElement);
        sessionBean.setCaptchaRequestUrl(downloadElement.getCaptchaRequestUrl());
        sessionBean.setResume(downloadElement.isResume());
        sessionBean.setDataOffset(downloadElement.getDataOffset());
        sessionBean.setDataSize(downloadElement.getDataTotalSize());
        sessionBean.setFinalLink(downloadElement.getFinalLink());
        sessionBean.setFileName(downloadElement.getFileName());
        Timestamp timestamp = downloadElement.getTimestamp();
        if (timestamp == null) timestamp = Timestamp.from(Instant.now());
        sessionBean.setTimestamp(timestamp);
        beans.add(sessionBean);
    }

    /*
    *
    * */
    private int findBeanIndexWithPath(DownloadElement downloadElement) {
        String captchaRequestUrl = downloadElement.getCaptchaRequestUrl();
        int i = 0;
        for (CsvBean sb : beans) {
            if (((SessionBean) sb).getCaptchaRequestUrl().equals(captchaRequestUrl)) {
                System.out.println("[ csv_service ] found existing element: " + captchaRequestUrl);
                return i;
            }
            i++;
        }
        return -1;
    }

    /*
     * Search for bean with the same captcha link as download element, if such bean
     * is found it's removed from the list and returned.
     * Otherwise, returns empty bean.
     * */
    private SessionBean findAndDeleteExistingBean(DownloadElement downloadElement) {
        int beanIndexWithPath = findBeanIndexWithPath(downloadElement);
        if (beanIndexWithPath < 0) {
            System.out.println("[ csv_service ] insert new element");
            return new SessionBean();
        }
        return (SessionBean) beans.remove(beanIndexWithPath);
//        String captchaRequestUrl = downloadElement.getCaptchaRequestUrl();
//        int i = 0;
//        for (CsvBean sb : beans) {
//            if (((SessionBean) sb).getCaptchaRequestUrl().equals(captchaRequestUrl)) {
//                System.out.println("[ csv_service ] found existing element: " + captchaRequestUrl);
//                return (SessionBean) beans.remove(i);
//            }
//            i++;
//        }
//        System.out.println("[ csv_service ] insert new element");
//        return new SessionBean();
    }

    public List<DownloadElement> removeFinishedElements() {
        System.out.println("[ csv_clear ] Removing finished elements...");
        beans.removeIf(bean -> !((SessionBean) bean).isResume());
        saveState(stateFilePath);
        return getStateList();
    }

    public List<DownloadElement> removeSelectedElement(int i) {
        beans.remove(i);
        saveState(stateFilePath);
        return getStateList();
    }

    /*
     * Check if state file exist, if so return true, otherwise return false
     * */
    public boolean isStateFile() {
        return Files.exists(stateFilePath);
    }

    /*
     * Load state list from a file
     * */
    public List<DownloadElement> getStateList() {
        List<DownloadElement> list = new ArrayList<>();
        for (CsvBean bean : beans) {
            list.add(parseBeanToDownloadElement((SessionBean) bean));
        }
        return list;
    }

    /*
     * Get the latest element of state list, returns null if list is empty
     * */
    public DownloadElement getLatestDownload() {
        List<DownloadElement> stateList = getStateList();
        if (!stateList.isEmpty()) return stateList.get(stateList.size() - 1);
        return null;
    }

    /*
     * Parse state bean to DownloadElement object
     * */
    DownloadElement parseBeanToDownloadElement(SessionBean csvBean) {
        DownloadElement downloadElement = new DownloadElement();
        downloadElement.setFinalLink(csvBean.getFinalLink());
        downloadElement.setResume(csvBean.isResume());
        downloadElement.setFileName(csvBean.getFileName());
        downloadElement.setDataOffset(csvBean.getDataOffset());
        downloadElement.setDataTotalSize(csvBean.getDataSize());
        downloadElement.setCaptchaRequestUrl(csvBean.getCaptchaRequestUrl());
        downloadElement.setTimestamp(csvBean.getTimestamp());
        return downloadElement;
    }

    public static void main(String[] args) throws IOException {
        Path directory = Path.of("config");
        if (!Files.exists(directory)) Files.createDirectory(directory);
        Path file = Path.of("test.csv");
        Path path = directory.resolve(file);

        DownloadElement downloadElement = new DownloadElement();
        downloadElement.setResume(true);
        downloadElement.setDataOffset(2096810);
        downloadElement.setFileName("dubel Roni Size - Reprazent New Forms 2 .rar");
        downloadElement.setCaptchaRequestUrl("2https://ulozto.net/download-dialog/free/download?fileSlug=lXCHnL50f1pA");
        downloadElement.setFinalLink("https://download.uloz.to/Ps;Hs;up=0;cid=285193758;uip=31.178.230.216;aff=ulozto.net;did=ulozto-net;fide=pyfQcbR;fs=lXCHnL50f1pA;hid=9ZBoYgR;rid=400232830;tm=1662480426;ut=f;rs=0;fet=download_free;He;ch=acab65706281d0a8985df82c762bc188;Pe/file/lXCHnL50f1pA/roni-size-reprazent-new-forms-2-rar?bD&c=285193758&De");

        CsvService csvService = new CsvService();
        csvService.initDirectory();
//        csvService.createCsvObject(downloadElement);
//        csvService.restoreState(path, SessionBean.class);
        csvService.saveState(downloadElement);


    }
}
