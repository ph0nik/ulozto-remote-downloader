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
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import service.CsvService;
import service.RootFolderResolver;
import service.events.CancelEvent;
import util.ElementValidator;
import websocket.NotificationDispatcher;
import websocket.model.DispatchStatus;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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

    @Autowired
    private ElementValidator elementValidator;

    private RequestElementForm currentDownloadStatus;
    private DownloadElement currentDownloadElement;
    private String downloadFolder;

    private static boolean cancel;

    public FileDownloadService() {
        cancel = false;
    }

    public boolean isCancel() {
        return cancel;
    }

    public DownloadElement getCurrentDownloadStatus() {
        return currentDownloadElement;
    }

    @PostConstruct
    private void setUpDownloadFolder() {
        downloadFolder = rootFolderResolver.getUserFolder();
        System.out.println("[ download_folder ] path: " + downloadFolder);
        System.out.println("[ download_folder ] exist? " + rootFolderResolver.isFolderValid());
    }

    @Async
    public Future<DownloadElement> generalDownload(DownloadElement downloadElement) {
        currentDownloadElement = downloadElement;
        cancel = false;
        resumeDispatch(downloadElement);
        Long totalBytes = 0L;
        try {
            totalBytes = downloadWithFolder(downloadElement.getFileName(), downloadFolder, downloadElement.getFinalLink(), downloadElement.getDataOffset());
        } catch (IOException e) {
            currentDownloadElement.setResume(true);
            currentDownloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ general_download ] " + e.getMessage());
        }
        currentDownloadElement.setResume(currentDownloadElement.getDataTotalSize() - totalBytes != 0);
        currentDownloadElement.setDataOffset(totalBytes);
        csvService.saveState(currentDownloadElement);
        return new AsyncResult<>(currentDownloadElement);
    }

    public String getDownloadFolder() {
        if (downloadFolder != null) return downloadFolder;
        return "none";
    }

    public List<DownloadElement> isPathValid(List<DownloadElement> downloadElements) {
        for (DownloadElement elem : downloadElements) {
            elem.setValidPath(elementValidator.isPathValid(elem, downloadFolder));
        }
        return downloadElements;
    }

    /*
    * Prints download file progress in console, for every 500 bytes written
    * */
    void printProgress(long current, double total, double transferRate, String fileName, String fileSource) {
        if (counter == 500 || current == total) {
            printToConsole(current, total);
            dispatchStatus(current, total, fileName, fileSource, transferRate);
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

    void resumeDispatch(DownloadElement downloadElement) {
        if (downloadElement.isResume()) {
            DispatchStatus dispatchStatus = new DispatchStatus();
            dispatchStatus.setFileName(downloadElement.getFileName());
            dispatchStatus.setFileSource(downloadElement.getFinalLink());
            dispatchStatus.setLength((double) downloadElement.getDataTotalSize());
            dispatchStatus.setTotalBytes(downloadElement.getDataOffset());
            dispatcher.dispatch(dispatchStatus);
        }
    }

    private void dispatchStatus(long current, double total, String fileName, String fileSource, double transferRate) {
        DispatchStatus status = new DispatchStatus();
        status.setTotalBytes(current);
        status.setLength(total);
        status.setFileName(fileName);
        status.setFileSource(fileSource);
        status.setTransferRate(transferRate);
        dispatcher.dispatch(status);
    }

    public Long downloadWithFolder(String fileName, String folderName, String url, long offset) throws IOException {
        String s = Path.of(folderName).resolve(Path.of(fileName)).toString();
        return downloadWithOkhttp(s, url, offset);
    }

    @EventListener
    public void handleCancelEvent(CancelEvent cancelEvent) {
        System.out.println("[ event_listener ] canceling...");
//        currentDownloadElement.setResume(true);
        cancel = true;

    }

    public Long downloadWithOkhttp(String fileName, String url, long downloadOffset)  {
        long downloadedBytes = 0;
        ProgressCallback progressCallback = (x, y, z) -> printProgress(x, y, z, fileName, url);
        // build client
        OkHttpClient client = new OkHttpClient().newBuilder()
//                .cookieJar(RequestService.getCookieJar())
                .followRedirects(true)
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
            if (body == null || response.isRedirect()) {
                throw new IllegalStateException("[ download ] Response doesn't contain a file");
            }
            double length = Double.parseDouble(Objects.requireNonNull(response.header(HttpHeaders.CONTENT_LENGTH, "1")));
            currentDownloadElement.setDataTotalSize((long) length);
            downloadedBytes = binaryFileWriter.write(body.byteStream(), length, this);
        } catch (IOException ioException) {
            // TODO resolve timeout issue, download gets interrupted and after resume throws instantly timeout ex
            System.out.println("[ download ] " + ioException.getMessage());
        }
        return downloadedBytes;
    }

    public static void main(String[] args) throws IOException {
        FileDownloadService fileDownloadService = new FileDownloadService();
        String url = "https://download.uloz.to/Ps;Hs;up=0;cid=281468240;uip=31.178.230.216;aff=ulozto.net;did=ulozto-net;fide=SVmGspr;fs=whSDAyWl6tiW;hid=CGAX7Kf;rid=440430961;tm=1664572535;ut=f;rs=0;fet=download_free;He;ch=8980352cb83c6e294876f795d6e5f182;Pe/file/whSDAyWl6tiW/the-requin-thriller-usa-2022-cz-titulky-mkv?bD&c=281468240&De";
        String folder = "R:\\Temp\\";
        String file = "20MB.zip";
        fileDownloadService.downloadWithFolder(file, folder, url, 0);
//        fileDownloadService.downloadWithOkhttp(file, url);
    }

}
