package service.download;

import download.BinaryFileWriter;
import download.ProgressCallback;
import model.DownloadElement;
import model.Status;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import service.CancelService;
import service.RootFolderResolver;
import service.StateService;
import util.ElementValidator;
import websocket.NotificationDispatcher;
import websocket.model.DispatchStatus;
import websocket.model.DownloadStatus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class FileDownloadService {
    public final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.83";
    public final int retryInterval = 5 * 60 * 1000; // milliseconds between retry
    public final int retryAttempts = 3;
    private int counter;
    @Autowired
    private NotificationDispatcher dispatcher;
    @Autowired
    private RootFolderResolver rootFolderResolver;
    @Autowired
    @Qualifier("xmlService")
    private StateService stateService;
    @Autowired
    private ElementValidator elementValidator;
    @Autowired
    private CancelService cancelService;
    private DownloadElement currentDownloadElement;

    public boolean isCancel() {
        return cancelService.isCancel();
//        return cancel;
    }

    public DownloadElement getCurrentDownloadElement() {
        return currentDownloadElement;
    }

    public void setCurrentDownloadElement(DownloadElement currentDownloadElement) {
        this.currentDownloadElement = currentDownloadElement;
    }

    @Async
    public Future<Long> generalDownload(DownloadElement downloadElement) {
        // if no root folder found throw exception
        if (getDownloadFolder().isEmpty()) {
            String message = "[ general_download ] No download folder found";
            System.out.println(message);
            dispatchErrorStatus(message);
            return new AsyncResult<>(0L);
        }
        currentDownloadElement = downloadElement;
        cancelService.reset();
        if (downloadElement.isResume()) resumeDispatch(currentDownloadElement); // send resume info
        currentDownloadElement.setDataOffset(getFileLength(currentDownloadElement.getFileName()));// get real offset from file length
        long totalBytes = downloadWithFolder(currentDownloadElement.getFileName(), getDownloadFolder(),
                currentDownloadElement.getFinalLink(), currentDownloadElement.getDataOffset());
        stateService.saveState(renameFile(currentDownloadElement));
        currentDownloadElement = null;
        return new AsyncResult<>(totalBytes);
    }

    /*
     * Rename file name from temporary to final one when the download is complete.
     * */
    public DownloadElement renameFile(DownloadElement downloadElement) {
        // run only if download finished
        if (downloadElement.getDataTotalSize() == downloadElement.getDataOffset()) {
            System.out.println("[ rename_file ] Download finished.");
            String targetFileName = downloadElement.getFileName()
                    .replace(ElementDownloadDetailsService.TEMP_EXT, "");
            Path fileToMovePath = Path.of(getDownloadFolder())
                    .resolve(downloadElement.getFileName());
            Path targetPath = Path.of(getDownloadFolder())
                    .resolve(targetFileName);
            System.out.println("[ rename_file ] "
                    + downloadElement.getFileName()
                    + " => " + targetFileName);
            try {
                Files.move(fileToMovePath, targetPath);
                downloadElement.setFileName(targetFileName);
            } catch (IOException e) {
                String message = "[ rename_file ] Error: " + e.getMessage();
                System.out.println(message);
                dispatchErrorStatus(message);
            }
        }
        return downloadElement;
    }

    /*
     * Returns user defined download folder
     * */
    public String getDownloadFolder() {
        return rootFolderResolver.getUserFolder();
    }

    /*
     * Get file size in bytes
     * */
    long getFileLength(String fileName) {
        try {
            Path file = Path.of(getDownloadFolder()).resolve(fileName);
            return Files.size(file);
        } catch (IOException e) {
            System.out.println("[ get_length ] No file found: " + e.getMessage());
        }
        return 0L;
    }

    public List<DownloadElement> getDownloadElements() {
        return stateService.getStateList()
                .stream()
                .map(this::isPathValid)
                .map(this::isStatusValid)
                .collect(Collectors.toList());
    }

    public DownloadElement isStatusValid(DownloadElement de) {
        if (de.getStatus() == Status.STARTED
                && de.getDataTotalSize() != de.getDataOffset())
            de.setStatus(Status.INTERRUPTED);
        return de;
    }

    /*
     * Check if given file exists within folder defined as download folder
     * */
    public DownloadElement isPathValid(DownloadElement de) {
        boolean isValid = elementValidator.isPathValid(de, getDownloadFolder());
        de.setValidPath(isValid);
        if (isValid) de.setDataOffset(getFileLength(de.getFileName()));
        return de;
    }

    /*
     * Prints download file progress in console, for every 500 kbytes written
     * */
    void printProgress(long current, double total, double transferRate,
                       String fileName, String fileSource) {
        if (counter == 500 || current == total) {
//            printToConsole(current, total);
            dispatchOkStatus(current, total, fileName, fileSource, transferRate);
            counter = 0;
        }
        counter++;
    }

    private void printToConsole(long current, double total) {
        StringBuilder sb = new StringBuilder();
        long percent = Math.round((current / total) * 100.0);
        System.out.print("\r");
        sb.append(current)
                .append(" / ")
                .append(Math.round(total))
                .append(" bytes ")
                .append("[")
                .append(percent)
                .append("%]");
        System.out.print(sb);
    }

    private void resumeDispatch(DownloadElement downloadElement) {
        dispatchOkStatus(downloadElement.getDataOffset(),
                downloadElement.getDataTotalSize(),
                downloadElement.getFileName(),
                downloadElement.getFinalLink(),
                0);
    }

    private void dispatchWaitStatus(DownloadElement downloadElement, long counter) {
        DispatchStatus status = new DispatchStatus();
        status.setTotalBytes(downloadElement.getDataTotalSize())
                .setLength(downloadElement.getDataTotalSize())
                .setFileName(downloadElement.getFileName())
                .setFileSource(downloadElement.getFinalLink())
                .setTransferRate(counter)
                .setDownloadStatus(DownloadStatus.WAIT);
        dispatcher.dispatch(status);
    }

    private void dispatchErrorStatus(String message) {
        DispatchStatus status = new DispatchStatus();
        status.setErrorMessage(message)
                .setDownloadStatus(DownloadStatus.ERROR);
        dispatcher.dispatch(status);
    }

    private void dispatchOkStatus(long current, double total, String fileName,
                                  String fileSource, double transferRate) {
        DispatchStatus status = new DispatchStatus();
        status.setTotalBytes(current)
                .setLength(total)
                .setFileName(fileName)
                .setFileSource(fileSource)
                .setTransferRate(transferRate)
                .setDownloadStatus(DownloadStatus.OK);
        dispatcher.dispatch(status);
    }

    public long downloadWithFolder(String fileName, String folderName,
                                   String url, long offset) {
        String s = Path.of(folderName).resolve(fileName).toString();
        long dataOffset = downloadWithOkhttp(s, url, offset);
//        int errorCounter = retryAttempts;
        int errorCounter = 1;
        while (!isCancel() && errorCounter <= retryAttempts
                && currentDownloadElement.getDataTotalSize() > currentDownloadElement.getDataOffset()) {
            try {
                System.out.println("[ download_retry ] Wait for retry... [" + (retryInterval * errorCounter) / 1000 + " seconds]");
                downloadDelay(currentDownloadElement, errorCounter++);
                System.out.println("[ download_retry ] Attempting resume download");
                dataOffset = downloadWithOkhttp(s, url, dataOffset);
            } catch (InterruptedException e) {
                // sleep
            }
        }
        if (!isCancel() && errorCounter == 0
                && currentDownloadElement.getDataTotalSize() > currentDownloadElement.getDataOffset()) {
            dispatchErrorStatus("[ download_retry ] Too many attempts, aborting...");
            System.out.println("[ download_retry ] Too many attempts, aborting...");
        }
        return dataOffset;
    }

    private void downloadDelay(DownloadElement downloadElement, int errorCounter) throws InterruptedException {
        long delayAmount = (long) retryInterval * errorCounter;
        long releaseTime = System.currentTimeMillis() + delayAmount;
        while (System.currentTimeMillis() < releaseTime || !cancelService.isCancel()) {
            if (cancelService.isCancel()) throw new InterruptedException("Retry has been canceled.");
            long count = releaseTime - System.currentTimeMillis(); // time remaining
            dispatchWaitStatus(downloadElement, count);
            Thread.sleep(1000);
        }
    }

    public long downloadWithOkhttp(String fileName, String url, long downloadOffset) {
        long downloadedBytes = 0;
        long actualContentLength = 0;
        ProgressCallback progressCallback = (x, y, z) -> printProgress(x, y, z, fileName, url);
        // build client
        OkHttpClient client = new OkHttpClient().newBuilder()
//                .cookieJar(RequestService.getCookieJar())
                .followRedirects(true)
                .readTimeout(Duration.ofSeconds(30))
                .build();
        // build request
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", userAgent);

        // request range from server
        if (downloadOffset != 0)
            requestBuilder.addHeader("Range", "bytes=" + downloadOffset + "-");
        boolean resume = downloadOffset != 0;

        Request request = requestBuilder
                .get()
                .build();
        // execute call with request and initiate binary file writer with file output stream and callback
        // possible to get filename from header of response - Content-Disposition: attachment; filename*=UTF-8''Morbid%20Angel%20Discography%20%281985%20-%202020%29.rar
        try (
                Response response = client.newCall(request).execute();
                BinaryFileWriter binaryFileWriter =
                        new BinaryFileWriter(new FileOutputStream(fileName, resume),
                                progressCallback,
                                downloadOffset)
        ) {
            System.out.println("download headers : " + response.headers());
            actualContentLength = Long.parseLong(
                    Objects.requireNonNull(response.header(HttpHeaders.CONTENT_LENGTH, "1"))
            );
            long expectedContentLength = currentDownloadElement.getDataTotalSize() - downloadOffset;
            ResponseBody body = response.body();
            if (body == null || response.isRedirect()) { // response has no body or has wrong code
                System.out.println("[ download ] Aborted, response doesn't contain a file");
                cancelService.cancel();
                return 0L;
            }
            if (currentDownloadElement.getDataTotalSize() != 0 // response has wrong size
                    && actualContentLength != expectedContentLength) {
                System.out.println("Expected content length is wrong.");
                cancelService.cancel();
                return 0L;
            }
            if (currentDownloadElement.getDataTotalSize() < 1) // for new download set content length
                currentDownloadElement.setDataTotalSize(actualContentLength);
            currentDownloadElement.setStatus(Status.STARTED);
            stateService.saveState(currentDownloadElement); // save before download
            // start writing file
            downloadedBytes = binaryFileWriter.write(body.byteStream(), actualContentLength, cancelService);
//            downloadedBytes = binaryFileWriter.write(body.byteStream(), actualContentLength, this);
        } catch (IOException ioException) {
            currentDownloadElement.setStatusMessage(ioException.getMessage());
            String message = "[ download ] Connectivity problem or timeout " + ioException.getMessage();
            System.out.println("[ download ] connectivity problem or timeout " + ioException.getMessage());
            dispatchErrorStatus(message);
        }
        long totalBytesWritten = downloadOffset + downloadedBytes;
        if (currentDownloadElement.getDataTotalSize() != totalBytesWritten) {
            if (isCancel()) currentDownloadElement.setStatus(Status.PAUSED);
            else currentDownloadElement.setStatus(Status.INTERRUPTED);
        } else {
            currentDownloadElement.setStatus(Status.FINISHED);
        }
        currentDownloadElement.setDataOffset(totalBytesWritten);
        progressCallback.onProgress(totalBytesWritten, actualContentLength, 0);
        return totalBytesWritten;
    }

}
