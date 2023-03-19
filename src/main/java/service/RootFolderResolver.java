package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class RootFolderResolver {

    public static final String FOLDER = "dpath";

    private String userFolder;

    @Autowired
    private Environment env;

    @PostConstruct
    private void setUserFolder() {
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
    * Returns true if user folder is non-empty and if it exists
    * */
    public boolean isFolderValid() {
        if (userFolder == null || userFolder.isEmpty()) return false;
        return validateFolder(userFolder);
    }

    /*
    * Set user folder based of environment variable
    * */
    public String getUserFolder() {
        return userFolder;
    }


}
