package model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class RequestElementForm {

    @JsonProperty("salt")
    private String salt;

    @JsonProperty("_do")
    private String captchaDo;

    @JsonProperty("captcha_type")
    private String captchaType;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("picture")
    private String pictureLink;

    @JsonProperty("sound")
    private String audioLink;

    @JsonProperty("captcha_value")
    private String captchaValue;

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }

    public String getCaptchaValue() {
        return captchaValue;
    }

    public void setCaptchaValue(String captchaValue) {
        this.captchaValue = captchaValue;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getCaptchaDo() {
        return captchaDo;
    }

    public void setCaptchaDo(String captchaDo) {
        this.captchaDo = captchaDo;
    }

    public String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        this.captchaType = captchaType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPictureLink() {
        return pictureLink;
    }

    public void setPictureLink(String pictureLink) {
        this.pictureLink = pictureLink;
    }

    @Override
    public String toString() {
        return "RequestElementForm{" +
                "salt='" + salt + '\'' +
                ", captchaDo='" + captchaDo + '\'' +
                ", captchaType='" + captchaType + '\'' +
                ", hash='" + hash + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", pictureLink='" + pictureLink + '\'' +
                ", audioLink='" + audioLink + '\'' +
                ", captchaValue='" + captchaValue + '\'' +
                '}';
    }
}
