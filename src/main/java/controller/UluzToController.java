package controller;

import exceptions.NetworkException;
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
    private Future<Long> future;

    @ModelAttribute("download_finished")
    public Boolean elementDownloadFinished() {
        return !futureStatus();
    }

    @ModelAttribute("elements_history")
    public List<DownloadElement> getDownloadHistory() {
        return fileDownloadService.getDownloadElements();
    }

    @ModelAttribute("future_status")
    public boolean futureStatus() {
        return future == null || future.isDone();
    }

    @ModelAttribute("download_folder")
    public String setDownloadFolder() {
        return fileDownloadService.getDownloadFolder();
    }

    @GetMapping("/")
    public String startPage(Model model) {
        if (!futureStatus()) {
            return "download_info";
        }
        return "source_link";
    }

    @GetMapping("/resume_download/{id}")
    public String resumeCurrent(@PathVariable("id") int id, Model model) {
        DownloadElement downloadElement = getDownloadHistory().get(id);
        if (elementDownloadDetailsService.isValidDownload(downloadElement)) {
            fileDownloadService.setCurrentDownloadElement(downloadElement);
            return "redirect:/ulozto/download";
        } else {
            return "redirect:/ulozto/refresh_link/" + id;
        }
    }

    @GetMapping("/error_test")
    public String checkErrorResponse() throws NetworkException {
        return "redirect:/ulozto/";
    }

    @GetMapping("/delete_element/{id}")
    public String deleteElement(@PathVariable("id") int id, Model model) {
        stateService.removeSelectedElement(id);
        return "redirect:/ulozto/";
    }

    @GetMapping("/download")
    public String getDownloadPage(Model model) throws NetworkException {
        DownloadElement downloadElement = fileDownloadService.getCurrentDownloadElement();
        if (downloadElement.getFinalLink() != null && !downloadElement.getFinalLink().isEmpty()) {
            if (elementDownloadDetailsService.hasProperLocation(downloadElement)) {
                future = fileDownloadService.generalDownload(downloadElement);
                model.addAttribute("future_status", futureStatus());
            } else {
                throw new NetworkException("Too many connections");
            }
            return "download_info";
        }
        CaptchaRequestDto captchaDto = elementDownloadDetailsService.getCaptchaDto(downloadElement);
        model.addAttribute("captcha_dto", captchaDto);
        return "captcha_input";
    }

    @PostMapping("/send_captcha/captcha={captcha}")
    public String getCaptchaValue(@RequestParam("captcha") String captcha, Model model) throws IOException {
        DownloadElement downloadElement = fileDownloadService.getCurrentDownloadElement();
        downloadElement.getRequestElementForm().setCaptchaValue(captcha);
        downloadElement = elementDownloadDetailsService.sendPostWithOkHttp(downloadElement);
        if (downloadElement.getStatusCode() != 418) {
            return "redirect:/ulozto/download";
        } else {
            return "redirect:/ulozto/reload";
        }
    }

    @GetMapping("/refresh_link/{id}")
    public String refreshLink(@PathVariable("id") int id, Model model) {
        DownloadElement downloadElement = getDownloadHistory().get(id);
        fileDownloadService.setCurrentDownloadElement(elementDownloadDetailsService.getCaptchaOrDownloadLink(downloadElement));
        return "redirect:/ulozto/download";
    }

    @PostMapping("/get_link/page={page}")
    public String getPageLink(@RequestParam("page") String page, Model model) throws IOException {
        if (!LinkValidator.isProperUloztoLink(page)) {
            System.out.println("[ get_page_link ] invalid link format: " + page);
            return "redirect:/ulozto/";
        }
        DownloadElement downloadElement = elementDownloadDetailsService.getDownloadInfo(page);
        fileDownloadService.setCurrentDownloadElement(downloadElement);
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

    @GetMapping("/reload")
    public String reloadCaptcha(Model model) {
        CaptchaRequestDto captchaRequestDto;
        DownloadElement downloadElement = fileDownloadService.getCurrentDownloadElement();
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
            System.out.printf("[ cancel_controller ] downloaded: %d%n", future.get());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[ cancel_controller ] Unable to get future result: " + e.getMessage());
        }
        return "redirect:/ulozto/";
    }

    @GetMapping("/clear_history")
    public String clearHistory() {
        stateService.removeFinishedElements()
                .removeInvalidElements()
                .saveState();
        return "redirect:/ulozto/";
    }
}
