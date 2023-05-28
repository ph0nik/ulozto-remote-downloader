package service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class RootFolderResolver {
    public static final String FOLDER = "dpath";
    private String userFolder;
    private final Environment env;

    public RootFolderResolver(Environment env) {
        this.env = env;
        userFolder = (env.containsProperty(FOLDER) && validateFolder(env.getProperty(FOLDER))) ? env.getProperty(FOLDER) : "";
        if (userFolder.isEmpty()) {
            System.out.println("[ root_folder ] path not found");
        } else {
            System.out.println("[ root_folder ] User defined download path: " + userFolder);
        }
    }

    /*
    * Returns true if given path exists and if it points do directory, otherwise returns false
    * */
    private boolean validateFolder(String userPath) {
        Path path = Path.of(userPath);
        return Files.exists(path) && Files.isDirectory(path);
    }

    /*
    * Set user folder based of environment variable
    * */
    public String getUserFolder() {
        if (userFolder.isEmpty() && env.containsProperty(FOLDER)) {
            String folder = env.getProperty(FOLDER);
            userFolder = (validateFolder(folder)) ? folder : "";
        }
        return userFolder;
    }

}
