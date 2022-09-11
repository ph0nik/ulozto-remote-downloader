package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaptchaResponseDto {

    @JsonProperty("captcha_value")
    private String captchaValue;

    @JsonProperty("captcha_reload")
    private boolean captchaReload;

    public String getCaptchaValue() {
        return captchaValue;
    }

    public void setCaptchaValue(String captchaValue) {
        this.captchaValue = captchaValue;
    }

    public boolean isCaptchaReload() {
        return captchaReload;
    }

    public void setCaptchaReload(boolean captchaReload) {
        this.captchaReload = captchaReload;
    }

    @Override
    public String toString() {
        return "CaptchaResponseDto{" +
                "captchaValue='" + captchaValue + '\'' +
                ", captchaReload=" + captchaReload +
                '}';
    }
}
