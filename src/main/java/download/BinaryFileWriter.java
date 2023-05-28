package download;

import service.CancelService;
import service.download.FileDownloadService;
import util.BytesCounter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryFileWriter implements AutoCloseable {
    private final OutputStream outputStream;
    private final ProgressCallback progressCallback;
    private final Long offset;

    public BinaryFileWriter(OutputStream outputStream,
                            ProgressCallback progressCallback,
                            Long offset) {
        this.offset = offset;
        this.outputStream = outputStream;
        this.progressCallback = progressCallback;
    }

    /*
     * Passes data from input stream to output stream.
     * Takes input stream and data length as arguments.
     * Returns total bytes written.
     * */
    public long write(InputStream inputStream,
                      double length,
                      FileDownloadService fileDownloadService) {
        long totalBytes = 0;
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            int chunkSize = 1024;
            byte[] dataBuffer = new byte[chunkSize];
            int readBytes;
            BytesCounter bytesCounter = new BytesCounter(chunkSize);
            while ((readBytes = input.read(dataBuffer)) != -1
                    && !fileDownloadService.isCancel()) {
                bytesCounter.setMark();
                totalBytes += readBytes;
                outputStream.write(dataBuffer, 0, readBytes);
                progressCallback.onProgress(offset + totalBytes, offset + length, bytesCounter.getBitrate());
            }
        } catch (IOException ioException) {
            System.out.println("[ binary_writer ] " + ioException.getMessage());
            // if timeout change static value
        }
        return totalBytes;
    }

    public long write(InputStream inputStream,
                      double length,
                      CancelService cancelService) {
        long totalBytes = 0;
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            int chunkSize = 1024;
            byte[] dataBuffer = new byte[chunkSize];
            int readBytes;
            BytesCounter bytesCounter = new BytesCounter(chunkSize);
            while ((readBytes = input.read(dataBuffer)) != -1
                    && !cancelService.isCancel()) {
                bytesCounter.setMark();
                totalBytes += readBytes;
                outputStream.write(dataBuffer, 0, readBytes);
                progressCallback.onProgress(offset + totalBytes, offset + length, bytesCounter.getBitrate());
            }
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
