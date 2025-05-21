package com.shaorn77770.kpsc_wargame.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorController {
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        System.err.println("Error occurred: " + ex.getMessage());
        return "error.html";
    }
}
