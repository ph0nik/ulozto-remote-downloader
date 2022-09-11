package download;

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
    public long write(InputStream inputStream, double length) throws IOException {
        long totalBytes = 0;
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            byte[] dataBuffer = new byte[CHUNK_SIZE];
            int readBytes;
            while ((readBytes = input.read(dataBuffer)) != -1 || Thread.currentThread().isInterrupted()) {
                totalBytes += readBytes;
                outputStream.write(dataBuffer, 0, readBytes);
                progressCallback.onProgress(offset + totalBytes, offset + length);
            }
//            return totalBytes;
        } catch (IOException ioException) {
            System.out.println("\nWriter error: " + ioException.getMessage());
        }
        return totalBytes;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
