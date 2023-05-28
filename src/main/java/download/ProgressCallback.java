package download;

@FunctionalInterface
public interface ProgressCallback {
    void onProgress(long totalBytes, double length, double transferRate);

}
