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
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    private static final String STATE_FILE = "download_state.csv";
    private static final String DIR = "config";

    private List<CsvBean> beans;

    public CsvService() {
        beans = new ArrayList<>();
    }

    @PostConstruct
    private void initDirectory() {
        Path dir = Path.of(DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void restoreState(Path path, Class<? extends CsvBean> clazz) {
        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean<CsvBean> csvToBean = new CsvToBeanBuilder<CsvBean>(reader)
                    .withQuoteChar('\'')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .withType(clazz)
                    .build();
            beans = csvToBean.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveState(DownloadElement downloadElement) {
        createCsvObject(downloadElement);
        Path p = Path.of(DIR).resolve(Path.of(STATE_FILE));
        saveState(p);
    }

    public void saveState(Path path) {
        try (Writer writer = new FileWriter(path.toFile())) {
            System.out.println("save csv state");
            StatefulBeanToCsv<CsvBean> beanToCsv = new StatefulBeanToCsvBuilder<CsvBean>(writer)
                    .withQuotechar('\'')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();
            beanToCsv.write(beans);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException(e);
        }
    }

    public void createCsvObject(DownloadElement downloadElement) {
        System.out.println("create csv bean");
        SessionBean sessionBean = new SessionBean();
        sessionBean.setCaptchaRequestUrl(downloadElement.getCaptchaRequestUrl());
        sessionBean.setResume(downloadElement.isResume());
        sessionBean.setDataOffset(downloadElement.getDataOffset());
        sessionBean.setFinalLink(downloadElement.getFinalLink());
        sessionBean.setFileName(downloadElement.getFileName());
        beans.add(sessionBean);
    }

    public boolean isStateFile() {
        return Files.exists(Path.of(STATE_FILE));
    }

    public DownloadElement getLatestDownload() {
        DownloadElement element = null;
        Path stateFile = Path.of(STATE_FILE);
        if (isStateFile()) {
            restoreState(stateFile, SessionBean.class);
            if (!beans.isEmpty()) {
                element = new DownloadElement();
                SessionBean csvBean = (SessionBean) beans.get(0);
                element.setFinalLink(csvBean.getFinalLink());
                element.setResume(csvBean.isResume());
                element.setFileName(csvBean.getFileName());
                element.setDataOffset(csvBean.getDataOffset());
                element.setCaptchaRequestUrl(csvBean.getCaptchaRequestUrl());
            }
        }
        return element;
    }

    public static void main(String[] args) throws IOException {
        Path directory = Path.of("config");
        if (!Files.exists(directory)) Files.createDirectory(directory);
        Path file = Path.of("test.csv");
        Path path = directory.resolve(file);

        DownloadElement downloadElement = new DownloadElement();
        downloadElement.setResume(true);
        downloadElement.setDataOffset(2096810);
        downloadElement.setFileName("Roni Size - Reprazent New Forms 2.rar");
        downloadElement.setCaptchaRequestUrl("https://ulozto.net/download-dialog/free/download?fileSlug=lXCHnL50f1pA");
        downloadElement.setFinalLink("https://download.uloz.to/Ps;Hs;up=0;cid=285193758;uip=31.178.230.216;aff=ulozto.net;did=ulozto-net;fide=pyfQcbR;fs=lXCHnL50f1pA;hid=9ZBoYgR;rid=400232830;tm=1662480426;ut=f;rs=0;fet=download_free;He;ch=acab65706281d0a8985df82c762bc188;Pe/file/lXCHnL50f1pA/roni-size-reprazent-new-forms-2-rar?bD&c=285193758&De");
        CsvService csvService = new CsvService();
        csvService.createCsvObject(downloadElement);

        csvService.saveState(path);
        System.out.println(Files.exists(path));
        csvService.restoreState(path, SessionBean.class);

    }
}
