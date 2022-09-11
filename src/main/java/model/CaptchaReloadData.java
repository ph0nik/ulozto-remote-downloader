package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaptchaReloadData {

    @JsonProperty("salt")
    private String image;

    @JsonProperty("salt")
    private String sound;

    @JsonProperty("salt")
    private String hash;

    @JsonProperty("salt")
    private Long timestamp;

    @JsonProperty("salt")
    private String salt;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {
        return "CaptchaReloadData{" +
                "image='" + image + '\'' +
                ", sound='" + sound + '\'' +
                ", hash='" + hash + '\'' +
                ", timestamp=" + timestamp +
                ", salt='" + salt + '\'' +
                '}';
    }
}
