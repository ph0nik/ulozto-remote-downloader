package model;

import util.TimestampAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Timestamp;

@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadElement {
    private RequestElementForm requestElementForm;
    private String fileName;
    private String captchaRequestUrl;
    private String finalLink;
    private String originalLink;
    private long dataOffset;
    private long dataTotalSize;
    private boolean resume;
    private Status status;
    private int statusCode;
    private String statusMessage;
    @XmlJavaTypeAdapter(TimestampAdapter.class)
    private Timestamp timestamp;

    private boolean validPath;

    public DownloadElement() {
        this.status = Status.READY;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public boolean isValidPath() {
        return validPath;
    }

    public void setValidPath(boolean validPath) {
        this.validPath = validPath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
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
        return status != Status.EXPIRED
                && status != Status.FINISHED;
//        return resume;
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
                ", originalLink='" + originalLink + '\'' +
                ", dataOffset=" + dataOffset +
                ", dataTotalSize=" + dataTotalSize +
                ", resume=" + resume +
                ", status=" + status +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", timestamp=" + timestamp +
                ", validPath=" + validPath +
                '}';
    }
}
