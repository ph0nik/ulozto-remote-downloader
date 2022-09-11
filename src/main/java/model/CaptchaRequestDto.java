package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaptchaRequestDto {

    @JsonProperty("response_code")
    private int responseCode;
    @JsonProperty("response_message")
    private String responseMessage;
    @JsonProperty("captcha_picture")
    private String captchaPicture;
    @JsonProperty("captcha_sound")
    private String captchaSound;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getCaptchaPicture() {
        return captchaPicture;
    }

    public void setCaptchaPicture(String captchaPicture) {
        this.captchaPicture = captchaPicture;
    }

    public String getCaptchaSound() {
        return captchaSound;
    }

    public void setCaptchaSound(String captchaSound) {
        this.captchaSound = captchaSound;
    }

    @Override
    public String toString() {
        return "CaptchaDto{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                ", captchaPicture='" + captchaPicture + '\'' +
                ", captchaSound='" + captchaSound + '\'' +
                '}';
    }
}
