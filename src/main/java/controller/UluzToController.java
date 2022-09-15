package controller;

import model.CaptchaRequestDto;
import model.DownloadElement;
import model.RequestElementForm;
import org.jsoup.HttpStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.CsvService;
import service.download.ElementDownloadDetailsService;
import service.download.FileDownloadService;
import util.LinkValidator;

import java.io.IOException;
import java.util.concurrent.Future;

@Controller
@RequestMapping(value = "/ulozto")
@Validated
public class UluzToController {

    @Autowired
    ElementDownloadDetailsService elementDownloadDetailsService;

    @Autowired
    FileDownloadService fileDownloadService;

    @Autowired
    CsvService csvService;

//    private DownloadStatusDto downloadStatusDto;

    private DownloadElement downloadElement;

    private Future<DownloadElement> future;

    private Boolean resumable = false;

    @ModelAttribute("download_finished")
    public Boolean elementDownloadFinished() {
        return downloadElement.getDataTotalSize() - downloadElement.getDataOffset() == 0;
    }

    @ModelAttribute("resume")
    public Boolean getResumeStatus() {
        return resumable;
    }

    @ModelAttribute("future_status")
    public Boolean futureStatus() {
        return future == null || future.isDone();
    }

    @ModelAttribute("download_element")
    public DownloadElement setCurrentElement() {
        return downloadElement;
    }

    @ModelAttribute("download_folder")
    public String setDownloadFolder() {
        return fileDownloadService.getDownloadFolder();
    }

    @GetMapping("/")
    public String startPage(Model model) {
        if (downloadElement == null) {
            if (csvService.isStateFile()) {
                downloadElement = csvService.getLatestDownload();
                resumable = downloadElement.isResume();
            } else {
                resumable = false;
            }
        }
        return "source_link";
    }

    @GetMapping("/resume_download")
    public String resumeCurrent(Model model) {
        // show offset when resume
        System.out.println("[ controller_resume ]" + downloadElement);
        future = fileDownloadService.generalDownload(downloadElement);
        boolean futureStatus = future == null || future.isDone();
        model.addAttribute("future_status", futureStatus);
//        model.addAttribute("resume", resumable);
        return "source_link";
    }

    /*
     * Dummy get mapping for repeated request (refresh page)
     * */
    @GetMapping(value = {"/get_link/page={page}"})
    public String getPageLinkDummy(@PathVariable(value = "page", required = false) String page) {
        return "redirect:/ulozto/";
    }

    @PostMapping("/get_link/page={page}")
    public String getPageLink(@RequestParam("page") String page, Model model) throws IOException {
        if (!LinkValidator.isProperUloztoLink(page)) {
            System.out.println("[ get_page_link ] invalid link format: " + page);
            return "redirect:/ulozto/";
        }
//        downloadInfo = new RequestElementForm();
        try {
            downloadElement = elementDownloadDetailsService.getDownloadInfo(new DownloadElement(), page);
        } catch (HttpStatusException e) {
            downloadElement.setStatusCode(e.getStatusCode());
            downloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ get_download_info ] " + e.getMessage());
        } catch (IOException e) {
            downloadElement.setStatusMessage(e.getMessage());
            System.out.println("[ get_download_info ] " + e.getMessage());
        }
        System.out.println(downloadElement);
        if (downloadElement.getFinalLink() != null && !downloadElement.getFinalLink().isEmpty()) {
            if (elementDownloadDetailsService.hasProperLocation(downloadElement)) {
                future = fileDownloadService.generalDownload(downloadElement);
                boolean futureStatus = future == null || future.isDone();
                model.addAttribute("future_status", futureStatus);
            } else {
                downloadElement = elementDownloadDetailsService.setErrorMessage(downloadElement);
                model.addAttribute("download_element", downloadElement);
            }
            return "source_link";
        } else {
            CaptchaRequestDto captchaDto = elementDownloadDetailsService.getCaptchaDto(downloadElement);
            model.addAttribute("captcha_dto", captchaDto);
            return "captcha_input";
        }
    }

    @PostMapping("/send_captcha/captcha={captcha}")
    public String getCaptchaValue(@RequestParam("captcha") String captcha, Model model) throws IOException {
        downloadElement.getRequestElementForm().setCaptchaValue(captcha);
        downloadElement = elementDownloadDetailsService.sendPostWithOkHttp(downloadElement);
        if (elementDownloadDetailsService.hasProperLocation(downloadElement)) {
            future = fileDownloadService.generalDownload(downloadElement);
        } else {
            // TODO set new DTO for status of current file if present
            String errMessage = "Limit exceed, please select another link";
//            model.addAttribute("error_link", downloadInfo.getFinalLink());
//            model.addAttribute("error_mess", errMessage);
        }
        boolean futureStatus = future == null || future.isDone();
        model.addAttribute("future_status", futureStatus);
        return "source_link";
    }

    @GetMapping("/reload")
    public String reloadCaptcha(Model model) throws IOException {
        CaptchaRequestDto captchaRequestDto;
//        System.out.println("reload");
        if (downloadElement != null && downloadElement.getRequestElementForm() != null) {
            RequestElementForm requestElementForm = downloadElement.getRequestElementForm();
            requestElementForm = elementDownloadDetailsService.reloadCaptcha(requestElementForm);
            downloadElement.setRequestElementForm(requestElementForm);
            captchaRequestDto = elementDownloadDetailsService.getCaptchaDto(downloadElement);
            model.addAttribute("captcha_dto", captchaRequestDto);
            return "captcha_input";
        } else {
            return "redirect:/ulozto/";
        }
    }

    @GetMapping("/cancel_current")
    public String cancelCurrentDownload(Model model) {
        future.cancel(true);
        while (!fileDownloadService.getCurrentDownloadStatus().isResume()) {
            // wait untill async task has finished
        }
        downloadElement = fileDownloadService.getCurrentDownloadStatus();
        resumable = downloadElement.isResume();
        boolean futureStatus = future == null || future.isDone();
        model.addAttribute("future_status", futureStatus);
        model.addAttribute("resume", resumable);
        return "source_link";
    }

}
