package service;

import model.DownloadElement;
import model.Downloads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.AppdataResolver;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class XmlService implements StateService {

    private static final String FILENAME = "download_state.xml";
    private static final String DIR = "config";
    private Downloads downloads;

    @Autowired
    private AppdataResolver appdataResolver;
    private final Path stateFilePath;

    public XmlService() {
        downloads = new Downloads();
        downloads.setDownloadElements(new LinkedList<>());
        stateFilePath = Path.of(DIR).resolve(Path.of(FILENAME));
    }

    @PostConstruct
    private void loadState() {
        if (Files.exists(stateFilePath)) {
            System.out.println("[ init_xml ] file exists");
            restoreState(stateFilePath);
            downloads.getDownloadElements().sort(Comparator.comparing(DownloadElement::getTimestamp));
            System.out.println("[ init_xml ] state file loaded [OK]");
        } else {
            System.out.println("[ init_xml ] no state file found");
        }
    }

    /*
    * Checks if list contains element with given file name, if so element
    * is deleted from list, and given element is marked with current timestamp and
    * added to the list.
    * */
    public void addNewElement(DownloadElement downloadElement) {
        deleteElement(downloadElement);
        downloadElement.setTimestamp(Timestamp.from(Instant.now()));
        downloads.getDownloadElements().add(downloadElement);
    }


    /*
    * Returns list of all elements beside given one.
    * */
    public DownloadElement deleteElement(DownloadElement downloadElement) {
        downloads.setDownloadElements(
                downloads.getDownloadElements()
                        .stream()
                        .filter(x -> !x.getFileName().equals(downloadElement.getFileName()))
                        .collect(Collectors.toList()));
        return downloadElement;
    }

    @Override
    public StateService removeFinishedElements() {
        downloads.setDownloadElements(
                downloads.getDownloadElements()
                        .stream()
                        .filter(DownloadElement::isResume)
                        .collect(Collectors.toList()));
        saveState();
        return this;
    }

    @Override
    public StateService removeInvalidElements() {
        downloads.setDownloadElements(
                downloads.getDownloadElements()
                        .stream()
                        .filter(DownloadElement::isValidPath)
                        .collect(Collectors.toList()));
        saveState();
        return this;
    }

    @Override
    public List<DownloadElement> removeSelectedElement(int i) {
        downloads.getDownloadElements().remove(i);
        saveState();
        return getStateList();
    }

    @Override
    public boolean isStateFile() {
        return Files.exists(stateFilePath);
    }

    @Override
    public List<DownloadElement> getStateList() {
        return downloads.getDownloadElements();
    }

    @Override
    public void saveState(DownloadElement downloadElement) {
        System.out.println("[ save_state ] Adding element: " + downloadElement.getFileName());
        addNewElement(downloadElement);
        saveState();
    }

    private void saveState() {
        try {
            System.out.println("[ save_state ] saving...");
            JAXBContext context = JAXBContext.newInstance(Downloads.class);
            Marshaller mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            mar.marshal(downloads, stateFilePath.toFile());
        } catch (JAXBException e) {
            System.out.println("[ save_state ] " + e.getMessage());
        }
    }

    private void restoreState(Path path) {
        try {
            JAXBContext context = JAXBContext.newInstance(Downloads.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File file = stateFilePath.toFile();
            List<DownloadElement> temp = new LinkedList<>();
//            temp = (Downloads) unmarshaller.unmarshal(file);
            downloads = (Downloads) unmarshaller.unmarshal(file);
            System.out.println(downloads.getDownloadElements());
        } catch (JAXBException e) {
            System.out.println("[ save_state ] " + e.getMessage());
        }
    }
//
//
//    public static void main(String[] args) {
//        DownloadElement de = new DownloadElement();
//        de.setStatusCode(200);
//        de.setStatusMessage("status message");
//        de.setTimestamp(Timestamp.from(Instant.now()));
//        de.setResume(true);
//        de.setFileName("some filename with special characters '");
//        de.setDataTotalSize(0);
//        de.setFinalLink("final link");
//        XmlService xmlService = new XmlService();
//        xmlService.addNewElement(de);
//        try {
//            xmlService.saveElements();
//        } catch (JAXBException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
