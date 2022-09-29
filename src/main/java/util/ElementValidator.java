package util;

import model.DownloadElement;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ElementValidator {

    public boolean isPathValid(DownloadElement downloadElement, String rootFolder) {
        Path resolve = Path.of(rootFolder).resolve(Path.of(downloadElement.getFileName()));
        return Files.exists(resolve);
    }

}
