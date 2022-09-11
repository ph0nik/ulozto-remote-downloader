package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class RootFolderResolver {

    public static final String FOLDER = "download.folder";

    private String userFolder;

    @Autowired
    private Environment env;

    @PostConstruct
    private void setUserFolder() {
        userFolder = (env.containsProperty(FOLDER) && validateFolder(env.getProperty(FOLDER))) ? env.getProperty(FOLDER) : "";
    }

    /*
    * Check if given path exists
    * */
    private boolean validateFolder(String userPath) {
        return Files.exists(Paths.get(userPath));
    }

    /*
    * Check if user folder is non-empty and if it exists
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
