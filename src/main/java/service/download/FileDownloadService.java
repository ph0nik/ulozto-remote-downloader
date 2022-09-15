package service.download;

import download.BinaryFileWriter;
import download.ProgressCallback;
import model.DownloadElement;
import model.RequestElementForm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import service.CsvService;
import service.RootFolderResolver;
import service.request.RequestService;
import websocket.NotificationDispatcher;
import websocket.model.DispatchStatus;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Future;

@Service
public class FileDownloadService {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.83";

    private int counter;

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private RootFolderResolver rootFolderResolver;

    @Autowired
    private CsvService csvService;

    private RequestElementForm currentDownloadStatus;

    private DownloadElement currentDownloadElement;
    private String downloadFolder;

    public DownloadElement getCurrentDownloadStatus() {
        return currentDownloadElement;
    }

    // TODO set csv file with info
//    @Async
//    public Future<RequestElementForm> generalDownload(RequestElementForm requestElementForm) {
//        currentDownloadStatus = requestElementForm;
//        Long totalBytes = 0L;
////        downloadFolder = rootFolderResolver.getUserFolder();
////        "R:\\Temp\\";
//        try {
//            totalBytes = downloadWithFolder(requestElementForm.getFileName(), downloadFolder, requestElementForm.getFinalLink(), requestElementForm.getDataOffset());
//        } catch (IOException e) {
//            System.out.println("future / general: " + e.getMessage());
//        }
//        currentDownloadStatus.setResume(Thread.currentThread().isInterrupted());
//        currentDownloadStatus.setDataOffset(totalBytes);
//        csvService.saveState(currentDownloadStatus);
//        return new AsyncResult<>(requestElementForm);
//    }

    @Async
    public Future<DownloadElement> generalDownload(DownloadElement downloadElement) {
        currentDownloadElement = downloadElement;
        Long totalBytes = 0L;
        try {
            totalBytes = downloadWithFolder(downloadElement.getFileName(), downloadFolder, downloadElement.getFinalLink(), downloadElement.getDataOffset());
            currentDownloadElement.setResume(Thread.currentThread().isInterrupted());
        } catch (IOException e) {
            currentDownloadElement.setResume(true);
            System.out.println("[ general_download ] " + e.getMessage());
        }
//        currentDownloadElement.setResume(Thread.currentThread().isInterrupted());
        currentDownloadElement.setDataOffset(totalBytes);
        // TODO return
        csvService.saveState(currentDownloadElement);
        return new AsyncResult<>(downloadElement);
    }

    // new downlaod - get captcha -> redirect -> link -> download
    // resume download -> use existing redirect -> link -> download
    // resume after shutdown -> captcha -> redirect -> link -> download
    // abort -> wipe status

    @PostConstruct
    private void setUpDownloadFolder() {
        downloadFolder = rootFolderResolver.getUserFolder();
        System.out.println("[ download_folder ] path: " + downloadFolder);
        System.out.println("[ download_folder ] exist? " + rootFolderResolver.isFolderValid());
    }

    public String getDownloadFolder() {
        if (downloadFolder != null) return downloadFolder;
        return "none";
    }

    /*
    * Prints download file progress in console, for every 500 bytes written
    * */
    void printProgress(long current, double total, String fileName, String fileSource) {
        if (counter == 500 || current == total) {
            printToConsole(current, total);
            dispatchStatus(current, total, fileName, fileSource);
            counter = 0;
        }
        counter++;
    }

    private void printToConsole(long current, double total) {
        StringBuilder sb = new StringBuilder();
        long percent = Math.round((current / total) * 100.0);
        System.out.print("\r");
        sb.append(current).append(" / ").append(Math.round(total)).append(" bytes ").append("[").append(percent).append("%]");
        System.out.print(sb);
    }

    private void dispatchStatus(long current, double total, String fileName, String fileSource) {
        DispatchStatus status = new DispatchStatus();
        status.setTotalBytes(current);
        status.setLength(total);
        status.setFileName(fileName);
        status.setFileSource(fileSource);
        dispatcher.dispatch(status);
    }

    public Long downloadWithFolder(String fileName, String folderName, String url, long offset) throws IOException {
        String s = Path.of(folderName).resolve(Path.of(fileName)).toString();
        return downloadWithOkhttp(s, url, offset);
    }

    public Long downloadWithOkhttp(String fileName, String url, long downloadOffset) throws IOException {
        long downloadedBytes;
        ProgressCallback progressCallback = (x, y) -> printProgress(x, y, fileName, url);
        // build client
        OkHttpClient client = new OkHttpClient().newBuilder()
                .cookieJar(RequestService.getCookieJar())
                .build();
        // build request
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT);

        // request range from server
        if (downloadOffset != 0 ) requestBuilder.addHeader("Range", "bytes=" + String.valueOf(downloadOffset) + "-");
        boolean resume = downloadOffset != 0;

        Request request = requestBuilder
                .get()
                .build();
        // execute call with request and initiate binary file writer with file output stream and callback
        try (
                Response response = client.newCall(request).execute();
                BinaryFileWriter binaryFileWriter = new BinaryFileWriter(new FileOutputStream(fileName, resume), progressCallback, downloadOffset)
        ) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new IllegalStateException("[ download ] Response doesn't contain a file");
            }
            double length = Double.parseDouble(Objects.requireNonNull(response.header(HttpHeaders.CONTENT_LENGTH, "1")));
            currentDownloadElement.setDataTotalSize((long) length);
            downloadedBytes = binaryFileWriter.write(body.byteStream(), length);
        }
        return downloadedBytes;

    }

    public DownloadElement sendPostWithOkHttp(DownloadElement downloadElement) throws IOException {
        return RequestService.sendPostWithOkHttp(downloadElement);
    }

    public static void main(String[] args) throws IOException {
        FileDownloadService fileDownloadService = new FileDownloadService();
        String url = "http://ipv4.download.thinkbroadband.com/20MB.zip";
        String folder = "R:\\Temp\\";
        String file = "20MB.zip";
//        fileDownloadService.downloadWithFolder(file, folder, url);
//        fileDownloadService.downloadWithOkhttp(file, url);
    }

}
