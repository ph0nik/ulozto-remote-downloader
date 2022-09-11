package model;

public enum RequestFormEnum {
    SALT("salt"),
    CAPTCHA_DO("_do"),
    CAPTCHA_TYPE("captcha_type"),
    CAPTCHA_VALUE("captcha_value"),
    HASH("hash"),
    TIMESTAMP("timestamp");


    private String parameterName;

    RequestFormEnum(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return this.parameterName;
    }
}
