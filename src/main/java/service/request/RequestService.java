package service.request;

import model.CustomResponse;
import model.DownloadElement;
import okhttp3.*;
import service.MyCookieJar;
import util.BoundaryGenerator;

import java.io.IOException;

public class RequestService {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.83";
    public static final String CONNECTION = "keep-alive";
    public static final String MULTIPART_BOUNDARY = "multipart/form-data;boundary=--SomeExampleBoundary--";

    private static final MyCookieJar jar = new MyCookieJar();

    /*
    * Sends GET request and returns html page with code 200, direct download link for code 302,
    * and empty string for any other code.
    * */
    public static CustomResponse sendGetWithOkHttp(String url) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .cookieJar(jar)
                .followRedirects(false)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .get()
                .build();
        CustomResponse customResponse = new CustomResponse();
        try (Response response = client.newCall(request).execute()) {
            Headers responseHeaders = response.headers();
//            System.out.println(responseHeaders);
            // set status and message of response
            customResponse.setStatusCode(response.code());
            customResponse.setStatusMessage(response.message());
            /*
             * For codes other than 200 (201 for example), there's location parameter in the header,
             * that points to a file.
             * */
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) customResponse.setResponseBody(responseBody.string());
                if (response.isRedirect()) customResponse.setResponseLink(responseHeaders.get("location"));
            } else {
                customResponse.setStatusCode(response.code());
                customResponse.setStatusMessage(response.message());
            }
            // save cookies from this request
            customResponse.setMyCookieJar(jar);

        }
        return customResponse;
    }

    public static DownloadElement sendPostWithOkHttp(DownloadElement downloadElement) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .cookieJar(jar)
                .followRedirects(false)
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        System.out.println("[ post_request ] creating request body...");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("captcha_value", downloadElement.getRequestElementForm().getCaptchaValue())
                .addFormDataPart("timestamp", downloadElement.getRequestElementForm().getTimestamp())
                .addFormDataPart("salt", downloadElement.getRequestElementForm().getSalt())
                .addFormDataPart("hash", downloadElement.getRequestElementForm().getHash())
                .addFormDataPart("captcha_type", downloadElement.getRequestElementForm().getCaptchaType())
                .addFormDataPart("_do", downloadElement.getRequestElementForm().getCaptchaDo())
                .build();
        System.out.println("[ post_request ] sending request...");
        Request request = new Request.Builder()
                .url(downloadElement.getCaptchaRequestUrl())
                .method("POST", body)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Connection", CONNECTION)
                .addHeader("Content-Type", BoundaryGenerator.getRandomBoundary())
                .build();

        String fileLink = "";
        try (Response response = client.newCall(request).execute()) {
            Headers responseHeaders = response.headers();
            // location name in the response header has link
            for (String name : responseHeaders.names()) {
                if (name.equalsIgnoreCase("location")) {
                    fileLink = responseHeaders.get(name);
                    System.out.println("[ post_request ] file link found: " + fileLink);
                }
            }
            downloadElement.setFinalLink(fileLink);
            if (downloadElement.getFinalLink().equals(downloadElement.getCaptchaRequestUrl())) {
                downloadElement.setStatusMessage("Invalid final link");
                downloadElement.setStatusCode(418);
            }

            /*
             * For codes other than 200 (201 for example), there's location parameter in the header,
             * that points to a file.
             * */
            if (response.isRedirect()) {
                // response body also has link
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    System.out.println("[ post_request ] response body: " + responseBody.string());
                }
            }
        }
//        downloadElement.setFinalLink(fileLink);
        return downloadElement;
    }

    public static MyCookieJar getCookieJar() {
        return jar;
    }

}
