package model;

import websocket.model.DispatchStatus;

public class DownloadStatusDto {

    private DispatchStatus dispatchStatus;

    private String captchaValue;
    private String fileName;
    private String captchaRequestUrl;
    private String finalLink;
    private long dataOffset;

    private boolean resume;
    private String originalReferenceLink;

    public Boolean isResumable() {
        return dispatchStatus.getLength() - dispatchStatus.getTotalBytes() == 0;
    }

    public DispatchStatus getDownloadStatus() {
        return dispatchStatus;
    }

    public void setDownloadStatus(DispatchStatus dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public String getOriginalReferenceLink() {
        return originalReferenceLink;
    }

    public void setOriginalReferenceLink(String originalReferenceLink) {
        this.originalReferenceLink = originalReferenceLink;
    }

    @Override
    public String toString() {
        return "DownloadStatusDto{" +
                "downloadStatus=" + dispatchStatus +
                ", originalReferenceLink='" + originalReferenceLink + '\'' +
                '}';
    }
}
