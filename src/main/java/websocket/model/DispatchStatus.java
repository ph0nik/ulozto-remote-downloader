package websocket.model;

public class DispatchStatus {

    private DownloadStatus downloadStatus;
    private String fileSource;
    private String fileName;
    private String folderName;
    private long totalBytes;
    private double length;
    private double transferRate;
    private String errorMessage;
    private long errorValue;

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public DispatchStatus setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DispatchStatus setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public long getErrorValue() {
        return errorValue;
    }

    public DispatchStatus setErrorValue(long errorValue) {
        this.errorValue = errorValue;
        return this;
    }

    public double getTransferRate() {
        return transferRate;
    }

    public DispatchStatus setTransferRate(double transferRate) {
        this.transferRate = transferRate;
        return this;
    }

    public String getFolderName() {
        return folderName;
    }

    public DispatchStatus setFolderName(String folderName) {
        this.folderName = folderName;
        return this;
    }

    public boolean isResumable() {
        return length - totalBytes == 0;
    }

    public String getFileSource() {
        return fileSource;
    }

    public DispatchStatus setFileSource(String fileSource) {
        this.fileSource = fileSource;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public DispatchStatus setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public DispatchStatus setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }

    public double getLength() {
        return length;
    }

    public DispatchStatus setLength(double length) {
        this.length = length;
        return this;
    }

    @Override
    public String toString() {
        return "DispatchStatus{" +
                "fileSource='" + fileSource + '\'' +
                ", fileName='" + fileName + '\'' +
                ", folderName='" + folderName + '\'' +
                ", totalBytes=" + totalBytes +
                ", length=" + length +
                ", trasnferRate=" + transferRate +
                '}';
    }
}
