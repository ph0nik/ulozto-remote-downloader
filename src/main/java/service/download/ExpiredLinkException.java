package service.download;

import java.io.IOException;

public class ExpiredLinkException extends IOException {
    public ExpiredLinkException(String message) {
        super(message);
    }
}
