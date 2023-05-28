package controller;

import exceptions.NetworkException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = {NetworkException.class})
    protected String getErrorMessage(Exception ex, Model model) {
        model.addAttribute("download_error", ex.getMessage());
        return "error";
    }


}
