package model;

import com.opencsv.bean.CsvBindByName;

public class SessionBean extends CsvBean {

    @CsvBindByName
    private String fileName;

    @CsvBindByName
    private String captchaRequestUrl;

    @CsvBindByName
    private String finalLink;

    @CsvBindByName
    private long dataOffset;

    @CsvBindByName
    private boolean resume;

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

    @Override
    public String toString() {
        return "CsvObject{" +
                "fileName='" + fileName + '\'' +
                ", captchaRequestUrl='" + captchaRequestUrl + '\'' +
                ", finalLink='" + finalLink + '\'' +
                ", dataOffset=" + dataOffset +
                ", resume=" + resume +
                '}';
    }
}
