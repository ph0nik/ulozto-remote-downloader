package model;

import java.sql.Timestamp;

public class DownloadElement {

    private RequestElementForm requestElementForm;
    private String fileName;
    private String captchaRequestUrl;
    private String finalLink;
    private long dataOffset;
    private long dataTotalSize;
    private boolean resume;
    private int statusCode;
    private String statusMessage;
    private Timestamp timestamp;

    private boolean validPath;

    public boolean isValidPath() {
        return validPath;
    }

    public void setValidPath(boolean validPath) {
        this.validPath = validPath;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public double getPercentageProgress() {
        if (dataTotalSize >= dataOffset) {
            double v = (dataOffset * 100d) / dataTotalSize;
            return Math.round(v * 100.0) / 100;
        }
        return 0;
    }

    public long getDataTotalSize() {
        return dataTotalSize;
    }

    public void setDataTotalSize(long dataTotalSize) {
        this.dataTotalSize = dataTotalSize;
    }

    public RequestElementForm getRequestElementForm() {
        return requestElementForm;
    }

    public void setRequestElementForm(RequestElementForm requestElementForm) {
        this.requestElementForm = requestElementForm;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCaptchaRequestUrl() {
        return captchaRequestUrl;
    }

    public void setCaptchaRequestUrl(String captchaRequestUrl) {
        this.captchaRequestUrl = captchaRequestUrl;
    }

    public String getFinalLink() {
        return finalLink;
    }

    public void setFinalLink(String finalLink) {
        this.finalLink = finalLink;
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(long dataOffset) {
        this.dataOffset = dataOffset;
    }

    public boolean isResume() {
        return resume;
    }

    public void setResume(boolean resume) {
        this.resume = resume;
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

    @Override
    public String toString() {
        return "DownloadElement{" +
                "requestElementForm=" + requestElementForm +
                ", fileName='" + fileName + '\'' +
                ", captchaRequestUrl='" + captchaRequestUrl + '\'' +
                ", finalLink='" + finalLink + '\'' +
                ", dataOffset=" + dataOffset +
                ", dataTotalSize=" + dataTotalSize +
                ", resume=" + resume +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", timestamp=" + timestamp +
                ", validPath=" + validPath +
                '}';
    }
}
