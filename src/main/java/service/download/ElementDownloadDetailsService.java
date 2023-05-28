package service.download;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.*;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import service.request.RequestService;

import javax.naming.LimitExceededException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// send request for captcha and return object tu user
@Service
public class ElementDownloadDetailsService {
    public static final String TEMP_EXT = ".dwn";
    private final String ULUZ_BASE_URL = "https://ulozto.net";
    private final String ULUZ_CAPTCHA_GET = "https://ulozto.net/download-dialog/free/download?fileSlug=";
    private final String ULUZ_CAPTCHA_RELOAD = "https://ulozto.net/reloadXapca.php?rnd=";
    private final String ERROR_CAPTCHA_GET = "User authentication required. We noticed too many download requests from your IP. Confirm that you are not a robot.";
    public final int linkAlivePeriod = 24;

    public ElementDownloadDetailsService() {
    }

    public DownloadElement getDownloadInfo(String inputLink) {
        DownloadElement downloadElement = new DownloadElement();
        downloadElement.setOriginalLink(inputLink);
        try {
            if (inputLink != null) {
                // create temp name
                downloadElement.setFileName(getFileNameFromUrl(inputLink) + TEMP_EXT);
                downloadElement.setCaptchaRequestUrl(
                        createUrlToGetCaptcha(
                                fileIdResolver(inputLink)
                        )
                );
                // wait between get requests
                Thread.sleep(500);
            }
            downloadElement = getCaptchaOrDownloadLink(downloadElement);
        } catch (InterruptedException e) {
            System.out.println("[ get_download_info ] " + e.getMessage()); // sleep
        } catch (HttpStatusException e) { // TODO pass to the user
            downloadElement.setStatusCode(e.getStatusCode());
            downloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ get_download_info ] " + e.getMessage());
        } catch (IOException | LimitExceededException e) { // TODO pass to the user
            downloadElement.setStatusMessage(e.getMessage());
            downloadElement.setStatusCode(418);
            System.out.println("[ get_download_info ] " + e.getMessage());
        }
        return downloadElement;
    }

    public DownloadElement getCaptchaOrDownloadLink(DownloadElement downloadElement) {
        try {
            downloadElement = sendRequestForCaptcha(downloadElement);
        } catch (HttpStatusException e) { // TODO pass to the user
            downloadElement.setStatusCode(e.getStatusCode());
            downloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ get_download_info ] " + e.getMessage());
        } catch (IOException | LimitExceededException e) { // TODO pass to the user
            downloadElement.setStatusMessage(e.getMessage());
            downloadElement.setStatusCode(418);
            System.out.println("[ get_download_info ] " + e.getMessage());
        }
        return downloadElement;
    }

//    public DownloadElement resumeDownloadInfo(DownloadElement downloadElement) {
//        return getDownloadInfo(downloadElement, null);
//    }

    public boolean isValidDownload(DownloadElement downloadElement) {
        long downloadTime = downloadElement.getTimestamp().getTime();
        long currentTime = Timestamp.from(Instant.now()).getTime();
        long deltaHours = (currentTime - downloadTime) / 1000 / 60 / 60;
        return (deltaHours < linkAlivePeriod);
    }

    /*
     * Sends request for a captcha code, if server returns direct response input object is going
     * to be updated with new values, if server returns redirect object is going to be
     * updated with new response link value.
     * */
    private DownloadElement sendRequestForCaptcha(DownloadElement downloadElement)
            throws IOException, LimitExceededException {
        // get custom response object with data
        CustomResponse response = RequestService.sendGetWithOkHttp(downloadElement.getCaptchaRequestUrl());
        // print status code to console
        System.out.println("[ captcha_request ] response: " + response.getStatusCode());
//        downloadElement.setStatusCode(response.getStatusCode());
        downloadElement.setStatusMessage(response.getStatusMessage());
        // if request returnss 200 then process dto object if 3xx download file, if else show error

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            HashMap<String, String> formMap = getFormMap(response.getResponseBody());
            if (!formMap.isEmpty()) {
                System.out.println("[ captcha_request ] filling form...");
                downloadElement.setRequestElementForm(getWebFormFromMap(formMap));
                downloadElement.setFinalLink("");
                downloadElement.setStatusCode(200);
            } else {
                System.out.println("[ captcha_request ] empty form map");
                downloadElement.setStatusMessage(getErrorMessage(response.getResponseBody()));
                downloadElement.setStatusCode(400);
            }
        }
        if (response.getStatusCode() >= 300 && response.getStatusCode() < 400) {
            System.out.println("[ captcha_request ] link obtained without captcha");
            downloadElement.setStatusCode(200);
//                String limitExceed = "limit-exceeded";
//            if (response.getResponseLink().contains("ulozto.net.timeout")) {
//
//            }
            downloadElement.setFinalLink(response.getResponseLink());
        }
        if (response.getStatusCode() >= 400) {
            downloadElement.setStatusCode(response.getStatusCode());
        }
        return downloadElement;
    }

    /*
     * Check if final link is proper file link or is it error notification
     * */
    public boolean hasProperLocation(DownloadElement downloadElement) {
        String limitExceed = "limit-exceeded";
        return !downloadElement.getFinalLink().contains(limitExceed);
    }

    public DownloadElement setErrorMessage(DownloadElement downloadElement) {
        downloadElement.setStatusMessage(ERROR_CAPTCHA_GET);
        return downloadElement;
    }

    /*
     * Sends request for new captcha parameters and returns updated object
     * */
    public RequestElementForm reloadCaptcha(RequestElementForm requestElementForm) {
        // "/reloadXapca.php?rnd=" + Number(new Date()),
        // use new Date().getTime for long value of current date in java
        // send request via link and retrieve json response
        long currentTimeLong = new Date().getTime();
        String reloadUrl = ULUZ_CAPTCHA_RELOAD + currentTimeLong;
//        CustomResponse response = RequestService.sendRequestWithOkHttp(reloadUrl);
        try {
            CustomResponse response = RequestService.sendGetWithOkHttp(reloadUrl);
            CaptchaReloadData captchaReloadData = new Gson().fromJson(response.getResponseBody(), CaptchaReloadData.class);
            requestElementForm.setPictureLink(captchaReloadData.getImage());
            requestElementForm.setTimestamp(captchaReloadData.getTimestamp().toString());
            requestElementForm.setHash(captchaReloadData.getHash());
            requestElementForm.setSalt(captchaReloadData.getSalt());
            requestElementForm.setAudioLink(captchaReloadData.getSound());
        } catch (JsonSyntaxException | IOException ex) { // TODO pass to the user
            System.out.println(ex.getMessage());
        } catch (LimitExceededException e) { // TODO pass to the user
            System.out.println("[ reload_captcha ] Limit exceed");
        }
        return requestElementForm;
    }

    /*
     * Parses RequestElementForm into CaptchaRequestDTO
     * */
    public CaptchaRequestDto getCaptchaDto(DownloadElement downloadElement) {
        CaptchaRequestDto captchaDto = new CaptchaRequestDto();
        captchaDto.setCaptchaPicture(downloadElement.getRequestElementForm().getPictureLink());
        captchaDto.setCaptchaSound(downloadElement.getRequestElementForm().getAudioLink());
        captchaDto.setResponseCode(downloadElement.getStatusCode());
        captchaDto.setResponseMessage(downloadElement.getStatusMessage());
        downloadElement = null;
        return captchaDto;
    }

    /*
     * Get id from provided url, if found returns id otherwise returns empty string.
     * */
    String fileIdResolver(String url) {
        String regex = "file\\/(\\w+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(matcher.groupCount());
        }
        return "";
    }

    /*
     * Create url based on file id
     * */
    String createUrlToGetCaptcha(String id) {
        return ULUZ_CAPTCHA_GET + id;
    }

    /*
     * Get file name from element web page, returns file name with extension if found, otherwise returns empty string
     * */
    String getFileNameFromUrl(String url) throws IOException, LimitExceededException {
        CustomResponse response = RequestService.sendGetWithOkHttp(url);
        String title = Jsoup.parse(response.getResponseBody()).title();
        String illegalCharacters = "[/?<>\\\\*:|\"^]+";
        String regex = "(.*)\\s\\|";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            String group = matcher.group(matcher.groupCount());
            String s = group.replaceAll(illegalCharacters, "_");
            System.out.println(s);
            return s;
        }
        return "";
    }

    public DownloadElement sendPostWithOkHttp(DownloadElement downloadElement) throws IOException {
        return RequestService.sendPostWithOkHttp(downloadElement);
    }

    /*
     * Get form values and picture link from raw document
     * */
    HashMap<String, String> getFormMap(String rawHtml) {
        HashMap<String, String> formValues = new HashMap<>();
        Document doc = Jsoup.parse(rawHtml);
        // get picture
        Elements picture = doc.select("form#frm-freeDownloadForm-form .xapca-image");
        // get sound link
        Elements sound = doc.select("form#frm-freeDownloadForm-form .captcha_sound_player");

        if (picture.size() > 0) {
            String pictureSrc = picture.get(0).attr("src");
            formValues.put("picture", pictureSrc);
            String soundSrc = sound.get(0).attr("src");
            formValues.put("sound", soundSrc);
            Elements select = doc.select("form#frm-freeDownloadForm-form input");
            for (Element element : select) {
                String name = element.attr("name");
                String value = element.attr("value");
                formValues.put(name, value);
            }
        }
        rawHtml = null;
        doc = null;
        return formValues;
    }

    /*
     * Convert map to request form model
     * */
    RequestElementForm getWebFormFromMap(Map<String, String> formSourceMap) {
        RequestElementForm requestElementForm = new RequestElementForm();
        requestElementForm.setSalt(formSourceMap.get("salt"));
        requestElementForm.setCaptchaDo(formSourceMap.get("_do"));
        requestElementForm.setCaptchaType(formSourceMap.get("captcha_type"));
        requestElementForm.setHash(formSourceMap.get("hash"));
        requestElementForm.setPictureLink(formSourceMap.get("picture"));
        requestElementForm.setTimestamp(formSourceMap.get("timestamp"));
        requestElementForm.setAudioLink(formSourceMap.get("sound"));
        return requestElementForm;
    }

    String getErrorMessage(String document) {
        Document parse = Jsoup.parse(document);
        Elements select = parse.select("form#frm-rateLimitingCaptcha-form");
        if (!select.isEmpty())
            return ERROR_CAPTCHA_GET;
        return "";
    }

    String replaceIllegalCharacters(String string) {
        String regex = "[/?<>\\\\*:|\"^]+";
        return string.replaceAll(regex, "_");
    }
}
