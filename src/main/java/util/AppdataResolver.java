package util;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AppdataResolver {
    private static final String DIR = "config";

    @PostConstruct
    private void initDirectory() {
        Path dir = Path.of(DIR);
        if (!Files.exists(dir)) {
            System.out.println("[ appdata ] directory not found");
            try {
                System.out.println("[ appdata ] creating directory: '" + dir + "'");
                Files.createDirectory(dir);
            } catch (IOException e) { // TODO pass to the user
                System.out.println("[ appdata ] " + e.getMessage());
            }
        } else {
            System.out.println("[ appdata ] directory present");
        }
    }
}
