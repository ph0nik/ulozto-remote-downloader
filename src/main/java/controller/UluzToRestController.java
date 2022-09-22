package controller;

import model.CaptchaRequestDto;
import model.CaptchaResponseDto;
import model.DownloadElement;
import model.RequestElementForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import service.download.ElementDownloadDetailsService;
import service.download.ExpiredLinkException;
import service.download.FileDownloadService;

import java.io.IOException;

@RestController
@RequestMapping(value = "/rest", produces = MediaType.APPLICATION_JSON_VALUE)
public class UluzToRestController {
    @Autowired
    ElementDownloadDetailsService elementDownloadDetailsService;

    @Autowired
    FileDownloadService fileDownloadService;

//    private RequestElementForm downloadInfo;

    private DownloadElement downloadElement;


    /*
    * Controller made for test purposes only
    * */
    @PostMapping("/")
    public CaptchaRequestDto getPageLink(@RequestParam("page") String page) throws ExpiredLinkException {
//        downloadInfo = new RequestElementForm();

            downloadElement = elementDownloadDetailsService.getDownloadInfo(new DownloadElement(), page);
//            tempCookies = elementDownloadDetailsService.getCookieStore();

        CaptchaRequestDto captchaDto = elementDownloadDetailsService.getCaptchaDto(downloadElement);
        if (downloadElement.getFinalLink() != null && !downloadElement.getFinalLink().isEmpty()) {
            fileDownloadService.generalDownload(downloadElement);
            captchaDto.setResponseMessage(downloadElement.getFinalLink());
        }
        return captchaDto;
    }

    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendCaptchaValue(@RequestBody CaptchaResponseDto captchaResponseDto) throws IOException {
        downloadElement.getRequestElementForm().setCaptchaValue(captchaResponseDto.getCaptchaValue());
        downloadElement = elementDownloadDetailsService.sendPostWithOkHttp(downloadElement);
        fileDownloadService.generalDownload(downloadElement);
        return downloadElement.getFinalLink();
    }

    @GetMapping(value = "/reload")
    @ResponseBody
    public CaptchaRequestDto reloadCaptcha() throws IOException {
        if (downloadElement != null && downloadElement.getRequestElementForm() != null) {
            RequestElementForm requestElementForm = downloadElement.getRequestElementForm();
            requestElementForm = elementDownloadDetailsService.reloadCaptcha(requestElementForm);
            downloadElement.setRequestElementForm(requestElementForm);
            return elementDownloadDetailsService.getCaptchaDto(downloadElement);
        } else {
            return new CaptchaRequestDto();
        }
    }


}
