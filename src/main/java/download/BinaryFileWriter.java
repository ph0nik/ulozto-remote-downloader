package download;

import service.download.FileDownloadService;
import util.BytesCounter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryFileWriter implements AutoCloseable {

    private static final int CHUNK_SIZE = 1024;
    private final OutputStream outputStream;
    private final ProgressCallback progressCallback;
    private final Long offset;

    public BinaryFileWriter(OutputStream outputStream, ProgressCallback progressCallback, Long offset) {
        this.offset = offset;
        this.outputStream = outputStream;
        this.progressCallback = progressCallback;
    }

    /*
     * Passes data from input stream to output stream.
     * Takes input stream and data length as arguments.
     * Returns total bytes written.
     * */
    public long write(InputStream inputStream, double length, FileDownloadService serviceNotifier) {
        long totalBytes = 0;
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            byte[] dataBuffer = new byte[CHUNK_SIZE];
            int readBytes;
            BytesCounter bytesCounter = new BytesCounter();
//            while ((readBytes = input.read(dataBuffer)) != -1 || Thread.currentThread().isInterrupted()) {
            while ((readBytes = input.read(dataBuffer)) != -1) {
                bytesCounter.setMark();
                totalBytes += readBytes;
                outputStream.write(dataBuffer, 0, readBytes);
                // temporarily commented
                progressCallback.onProgress(offset + totalBytes, offset + length, bytesCounter.getBitrate());
                if (serviceNotifier.isCancel()) {
                    inputStream.close();
                }
            }
//            return totalBytes;
        } catch (IOException ioException) {
            System.out.println("[ binary_writer ] " + ioException.getMessage());
        }
        return totalBytes;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
