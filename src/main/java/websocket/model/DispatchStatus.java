package websocket.model;

public class DispatchStatus {

    private String fileSource;
    private String fileName;
    private String folderName;
    private Long totalBytes;
    private Double length;
    private Double transferRate;

    public Double getTransferRate() {
        return transferRate;
    }

    public void setTransferRate(Double transferRate) {
        this.transferRate = transferRate;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }


    public boolean isResumable() {
        return length - totalBytes == 0;
    }

    public String getFileSource() {
        return fileSource;
    }

    public void setFileSource(String fileSource) {
        this.fileSource = fileSource;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
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
