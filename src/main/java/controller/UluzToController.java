package controller;

import model.CaptchaRequestDto;
import model.DownloadElement;
import model.RequestElementForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.StateService;
import service.download.ElementDownloadDetailsService;
import service.download.FileDownloadService;
import service.events.CustomEventPublisher;
import util.LinkValidator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    @Qualifier("xmlService")
    StateService stateService;

    @Autowired
    CustomEventPublisher customEventPublisher;

    private DownloadElement downloadElement;

    private List<DownloadElement> downloadElementList;

    private Future<Long> future;

//    private Boolean resumable = false;

    private Boolean nowDownloading = false;
    private Boolean downloadHistory = true;
    private Boolean insertLink = true;
    private Boolean downloadFinished = false;

    private Boolean generalErrorStatus = false;

    @ModelAttribute("download_finished")
    public Boolean elementDownloadFinished() {
        return downloadFinished;
    }

    @ModelAttribute("elements_history")
    public List<DownloadElement> loadDownloadHistory() {
        if (stateService != null) {
            downloadElementList = stateService.getStateList();
            return fileDownloadService.isPathValid(downloadElementList);
        }
        return List.of(new DownloadElement());
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

    @ModelAttribute("showInsertLink")
    public boolean showInsertLink() {
        return insertLink;
    }

    @ModelAttribute("showNowDownloading")
    public boolean showNowDownloading() {
        return nowDownloading;
    }

    @ModelAttribute("showDownloadHistory")
    public boolean showDownloadHistory() {
        return downloadHistory;
    }

    @GetMapping("/")
    public String startPage(Model model) {
        loadDownloadHistory();
        model.addAttribute("elements_history", downloadElementList);
        if (future != null && !future.isDone()) {
            insertLink = downloadHistory = false;
            nowDownloading = true;
        } else {
            insertLink = downloadHistory = true;
            nowDownloading = false;
        }
        if (downloadElement != null) {
            model.addAttribute("generalError", downloadElement.getStatusMessage());
            model.addAttribute("generalErrorStatus", true);
        }
        model.addAttribute("showInsertLink", insertLink);
        model.addAttribute("showNowDownloading", nowDownloading);
        model.addAttribute("showDownloadHistory", downloadHistory);
        return "source_link";
    }

    @GetMapping("/resume_download/{id}")
    public String resumeCurrent(@PathVariable("id") int id, Model model) {
        System.out.println("[ controller_resume ] id: " + id);
        System.out.println("[ controller_resume ] " + downloadElementList.get(id));
        downloadElement = downloadElementList.get(id);
        if (elementDownloadDetailsService.isValidDownload(downloadElement)) {
            return "redirect:/ulozto/download";
        } else {
            //TODO get the same file offset and rerun
            downloadElement.setStatusMessage("Link expired");
            System.out.println("expired");
            return "redirect:/ulozto/";
        }
    }

    @GetMapping("/delete_element/{id}")
    public String deleteElement(@PathVariable("id") int id, Model model) {
        System.out.println("[ controller_delete ] id: " + id);
        System.out.println("[ controller_delete ] " + downloadElementList.get(id));
        downloadElementList = stateService.removeSelectedElement(id);
        return "redirect:/ulozto/";
    }

    @GetMapping("/download")
    public String getDownloadPage(Model model) {
        if (downloadElement.getFinalLink() != null && !downloadElement.getFinalLink().isEmpty()) {
            if (elementDownloadDetailsService.hasProperLocation(downloadElement)) {
                future = fileDownloadService.generalDownload(downloadElement);
                insertLink = downloadHistory = downloadFinished = false;
                nowDownloading = true;
                model.addAttribute("showInsertLink", insertLink);
                model.addAttribute("showNowDownloading", nowDownloading);
                model.addAttribute("showDownloadHistory", downloadHistory);
                model.addAttribute("download_finished", downloadFinished);
            } else {
                downloadElement = elementDownloadDetailsService.setErrorMessage(downloadElement);
                model.addAttribute("download_element", downloadElement);
            }
            return "source_link";
        }
        CaptchaRequestDto captchaDto = elementDownloadDetailsService.getCaptchaDto(downloadElement);
        model.addAttribute("captcha_dto", captchaDto);
        return "captcha_input";
    }

    @PostMapping("/get_link/page={page}")
    public String getPageLink(@RequestParam("page") String page, Model model) throws IOException {
        if (!LinkValidator.isProperUloztoLink(page)) {
            System.out.println("[ get_page_link ] invalid link format: " + page);
            return "redirect:/ulozto/";
        }
        downloadElement = elementDownloadDetailsService.getDownloadInfo(new DownloadElement(), page);
        if (downloadElement.getStatusCode() > 400) {
            return "redirect:/ulozto/";
        }
        return "redirect:/ulozto/download";
    }

    /*
     * Dummy get mapping for repeated request (refresh page)
     * */
    @GetMapping(value = {"/get_link/page={page}"})
    public String getPageLinkDummy(@PathVariable(value = "page", required = false) String page) {
        return "redirect:/ulozto/";
    }

    @PostMapping("/send_captcha/captcha={captcha}")
    public String getCaptchaValue(@RequestParam("captcha") String captcha, Model model) throws IOException {
        downloadElement.getRequestElementForm().setCaptchaValue(captcha);
        downloadElement = elementDownloadDetailsService.sendPostWithOkHttp(downloadElement);
        if (downloadElement.getStatusCode() != 418) {
            return "redirect:/ulozto/download";
        } else {
            return "redirect:/ulozto/reload";
        }
    }

    @GetMapping("/reload")
    public String reloadCaptcha(Model model) throws IOException {
        CaptchaRequestDto captchaRequestDto;
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
        customEventPublisher.cancelEvent();
        while (!future.isDone()) {
//            Wait for async function to finish
        }
        try {
            long bytesDownloaded = future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[ cancel_controller ] Unable to get future result: " + e.getMessage());
        }
        return "redirect:/ulozto/";
    }

    @GetMapping("/clear_history")
    public String clearHistory(Model model) {
        downloadElementList = stateService.removeFinishedElements().removeInvalidElements().getStateList();
        return "redirect:/ulozto/";
    }
}
