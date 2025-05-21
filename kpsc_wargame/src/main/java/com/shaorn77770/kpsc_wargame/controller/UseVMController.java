package com.shaorn77770.kpsc_wargame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shaorn77770.kpsc_wargame.DTO.TokenDTO;
import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequiredArgsConstructor
public class UseVMController {
    private final UserService userService;
    
    @GetMapping("/vm")
    public String vm() {
        return "login_vm.html";
    }
    
    @PostMapping("/vm/login")
    public String accessVM(TokenDTO tokenData, Model model) {
        if(!userService.contains(tokenData.getToken())) {
            model.addAttribute("errorMessage", "존재하지 않는 계정입니다. 다시 시도해주세요.");
            return "error.html";
        }

        UserData user = userService.findByKey(tokenData.getToken());

        if(!user.isAllow()) {
            model.addAttribute("errorMessage", "아직 승인되지 않은 계정입니다.");
            return "error.html";
        }

        return "redirect:" + user.getJupyterUrl();
    }
    
}
