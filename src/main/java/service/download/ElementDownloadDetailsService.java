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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// send request for captcha and return object tu user
@Service
public class ElementDownloadDetailsService {

    private final static String ULUZ_BASE_URL = "https://ulozto.net";
    private final static String ULUZ_CAPTCHA_GET = "https://ulozto.net/download-dialog/free/download?fileSlug=";
    private final static String ULUZ_CAPTCHA_RELOAD = "https://ulozto.net/reloadXapca.php?rnd=";
    private final static String ERROR_CAPTCHA_GET = "User authentication required. We noticed too many download requests from your IP. Confirm that you are not a robot.";

    public ElementDownloadDetailsService() {

    }

    public DownloadElement getDownloadInfo(DownloadElement downloadElement, String downloadFileSlugUrl) {
        try {
            if (downloadFileSlugUrl != null) {
                downloadElement.setFileName(getFileNameFromUrl(downloadFileSlugUrl));
                downloadElement.setCaptchaRequestUrl(createUrlToGetCaptcha(fileIdResolver(downloadFileSlugUrl)));
                // wait between get requests
                Thread.sleep(500);
            }
            downloadElement = sendRequestForCaptcha(downloadElement);
        } catch (InterruptedException e) {
            System.out.println("[ get_download_info ] " + e.getMessage()); // sleep
        } catch (HttpStatusException e) {
            downloadElement.setStatusCode(e.getStatusCode());
            downloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ get_download_info ] " + e.getMessage());
        } catch (IOException e) {
            downloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ get_download_info ] " + e.getMessage());
        }
        return downloadElement;
    }

    public DownloadElement resumeDownloadInfo(DownloadElement downloadElement) {
        return getDownloadInfo(downloadElement, null);
    }

    //    public RequestElementForm getDownloadInfo(String url) throws IOException, InterruptedException {
//        RequestElementForm requestElementForm = new RequestElementForm();
//        String captchaUrl = createUrlToGetCaptcha(fileIdResolver(url));
//        requestElementForm.setFileName(getFileNameFromUrl(url));
//        requestElementForm.setCaptchaRequestUrl(captchaUrl);
//        // wait between get requests
//        Thread.sleep(500);
//
//        requestElementForm = sendRequestForCaptcha(requestElementForm);
//        return requestElementForm;
//    }
    /*
    * Sends request for a captcha code, if server returns direct response input object is going
    * to be updated with new values, if server returns redirect object is going to be
    * updated with new response link value.
    * */
    private DownloadElement sendRequestForCaptcha(DownloadElement downloadElement) throws IOException {
        // get custom response object with data
        CustomResponse response = RequestService.sendGetWithOkHttp(downloadElement.getCaptchaRequestUrl());
        // print status code to console
        System.out.println("[ captcha_request ] response: " + response.getStatusCode());
        downloadElement.setStatusCode(response.getStatusCode());
        downloadElement.setStatusMessage(response.getStatusMessage());
        // if request returnss 200 then process dto object if 3xx download file, if else show error
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            HashMap<String, String> formMap = getFormMap(response.getResponseBody());
            if (!formMap.isEmpty()) {
                System.out.println("[ captcha_request ] filling form...");
                downloadElement.setRequestElementForm(getWebFormFromMap(formMap));
            } else {
                System.out.println("[ captcha_request ] empty form map");
                downloadElement.setStatusMessage(getErrorMessage(response.getResponseBody()));
            }
        }
        if (response.getStatusCode() >= 300 && response.getStatusCode() < 400) {
            System.out.println("[ captcha_request ] link obtained without captcha");
//                String limitExceed = "limit-exceeded";
            downloadElement.setFinalLink(response.getResponseLink());
        }
        return downloadElement;
    }

//    private RequestElementForm sendRequestForCaptcha(RequestElementForm requestElementForm) throws IOException {
//        // get custom response object with data
//        CustomResponse responseString = sendRequestWithOkHttp(requestElementForm.getCaptchaRequestUrl());
//        // print status code to console
//        System.out.println("[ captcha request response ] " + responseString.getStatusCode());
//
//        requestElementForm.setStatusCode(responseString.getStatusCode());
//        requestElementForm.setStatusMessage(responseString.getStatusMessage());
//        // if request returnss 200 then process dto object if 3xx download file, if else show error
//        if (responseString.getStatusCode() >= 200 && responseString.getStatusCode() < 300) {
//            HashMap<String, String> formMap = getFormMap(responseString.getResponseBody());
//            requestElementForm = (!formMap.isEmpty())
//                    ? getWebFormFromMap(requestElementForm, formMap)
//                    : getErrorMessage(requestElementForm, responseString.getResponseBody());
//        }
//        if (responseString.getStatusCode() >= 300 && responseString.getStatusCode() < 400) {
//            String limitExceed = "limit-exceeded";
//            requestElementForm.setFinalLink(responseString.getResponseLink());
//        }
//        return requestElementForm;
//    }

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
        } catch (JsonSyntaxException | IOException ex) {
            System.out.println(ex.getMessage());
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
    String getFileNameFromUrl(String url) throws IOException {
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



    /*
     * Send request
     * */
//    CustomResponse sendRequestWithOkHttp(String url) throws IOException {
//        CustomResponse response = RequestService.sendGetWithOkHttp(url);
//        return response;
//    }

    /*
     * Send POST request and retrieve download link
     * */
//    public RequestElementForm sendPostWithOkHttp(RequestElementForm requestElementForm) throws IOException {
//        return RequestService.sendPostWithOkHttp(requestElementForm);
//    }

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
        return formValues;
    }

    /*
     * Convert map to request form model
     * */
    RequestElementForm getWebFormFromMap(RequestElementForm outerRequestElementForm, Map<String, String> formSourceMap) {
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

    RequestElementForm getErrorMessage(RequestElementForm requestElementForm, String document) {
        Document parse = Jsoup.parse(document);
        Elements select = parse.select("form#frm-rateLimitingCaptcha-form");
//        RequestElementForm form = new RequestElementForm();
        if (!select.isEmpty())
            requestElementForm.setPictureLink(ERROR_CAPTCHA_GET);
//        return elementForm;
        return requestElementForm;
    }

    String replaceIllegalCharacters(String string) {
        String regex = "[/?<>\\\\*:|\"^]+";
        return string.replaceAll(regex, "_");
    }

    public static void main(String[] args) {
        ElementDownloadDetailsService downloadDetailsService = new ElementDownloadDetailsService();
//        String test = "https://ulozto.net/file/xKH3AHVIF26G/metallica-load-zip#!ZGHjAGR1A2R2LGN1ZmOxAmt1A2ZkEmLlEzbhBSyIZmOwZQSu";
//        try {
//            RequestElementForm downloadInfo = downloadDetailsService.getDownloadInfo(test);
//            System.out.println(downloadInfo);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        String test = "Pantera - *** (2012) [FLAC] [16B-44.1kHz].zip/ ? < > \\ : * | \" ^";
        System.out.println(downloadDetailsService.replaceIllegalCharacters(test));

    }
}
