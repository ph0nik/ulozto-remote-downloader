package model;

import service.MyCookieJar;

public class CustomResponse {

    private int statusCode;
    private String statusMessage;
    private String responseBody;
    private String responseLink;

    private MyCookieJar myCookieJar;

    public MyCookieJar getMyCookieJar() {
        return myCookieJar;
    }

    public void setMyCookieJar(MyCookieJar myCookieJar) {
        this.myCookieJar = myCookieJar;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseLink() {
        return responseLink;
    }

    public void setResponseLink(String responseLink) {
        this.responseLink = responseLink;
    }

    @Override
    public String toString() {
        return "CustomResponse{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", responseLink='" + responseLink + '\'' +
                ", myCookieJar=" + myCookieJar +
                '}';
    }
}
