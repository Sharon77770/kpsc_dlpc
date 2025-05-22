package com.shaorn77770.kpsc_wargame.controller;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.shaorn77770.kpsc_wargame.DTO.UserDTO;
import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Controller
@RequiredArgsConstructor
public class RegisterController {
    private static final Logger logger = LogManager.getLogger(RegisterController.class);
    private final UserService userService;

    @GetMapping("/register")
    public String register() {
        return "register.html";
    }

    @PostMapping("/register")
    public String registerUser(UserDTO userData, Model model) {
        UserData newUser = new UserData();

        try {

            newUser.setAllow(false);
            newUser.setMajor(userData.getMajor());
            newUser.setUserName(userData.getName());
            newUser.setPhone(userData.getPhone());
            newUser.setStudentNumber(userData.getStudentId());
            
            String secureUuid = "";
            
            do {
                SecureRandom secureRandom = new SecureRandom();
                byte[] randomBytes = new byte[32];  // 128비트
                secureRandom.nextBytes(randomBytes);
                
                secureUuid = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            } while(userService.contains(secureUuid));
            
            newUser.setApiKey(secureUuid);

            userService.save(newUser);
            logger.info("신규 유저 등록 성공: {} ({})", newUser.getUserName(), newUser.getStudentNumber());
        }
        catch(Exception e) {
            logger.error("유저 등록 중 예외 발생", e);
            model.addAttribute("errorMessage", "잘못된 정보를 입력했습니다. 다시 시도해주세요.");
            return "error.html";
        }

        model.addAttribute("apiKey", newUser.getApiKey());

        return "register_success.html";
    }
}
