package com.shaorn77770.kpsc_wargame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shaorn77770.kpsc_wargame.utill.JWTManager;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequiredArgsConstructor
public class MainController {

    private final JWTManager jwtManager;

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        return "main.html";
    }
}
