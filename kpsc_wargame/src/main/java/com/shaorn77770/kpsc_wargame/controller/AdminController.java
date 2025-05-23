package com.shaorn77770.kpsc_wargame.controller;

import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shaorn77770.kpsc_wargame.DTO.AdminLoginDTO;
import com.shaorn77770.kpsc_wargame.data_class.Admin;
import com.shaorn77770.kpsc_wargame.data_class.ContainerData;
import com.shaorn77770.kpsc_wargame.data_class.Domain;
import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.service.DockerService;
import com.shaorn77770.kpsc_wargame.database.service.UserService;
import com.shaorn77770.kpsc_wargame.utill.JWTManager;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);

    private final Admin adminAccount;
    private final JWTManager jwtManager;
    private final UserService userService;
    private final Domain domain;
    private final DockerService dockerService;
    
    @GetMapping("/login")
    public String login(Model model) {
        return "admin_login.html";
    }
    
    @PostMapping("/login")
    public String tryLogin(AdminLoginDTO data, Model model, HttpServletResponse response) {
        try {
            if(adminAccount.getUsername().equals(data.getUsername()) && 
                    adminAccount.getPassword().equals(data.getPassword())) {
                logger.info("관리자 로그인 성공: {}", data.getUsername());
                var jwt = jwtManager.createToken(data.getUsername(), data.getPassword());
                
                ResponseCookie cookie = ResponseCookie.from("Auth", jwt)
                    .httpOnly(true) // HttpOnly 설정
                    .secure(false) // Secure 설정
                    .path("/") // 쿠키의 유효 경로 설정
                    .maxAge(60*60) // 만료 시간 설정 (초 단위)
                    .sameSite("Lax") // SameSite 속성 설정
                    .build();

                response.addHeader("Set-Cookie", cookie.toString());

                return "redirect:/admin";
            }
            logger.warn("관리자 로그인 실패: {}", data.getUsername());
            model.addAttribute("errorMessage", "잘못된 정보가 입력되었습니다.");
            return "error.html";
        } catch (Exception e) {
            logger.error("관리자 로그인 중 예외 발생", e);
            model.addAttribute("errorMessage", "로그인 처리 중 오류가 발생했습니다.");
            return "error.html";
        }
    }

    @GetMapping("")
    public String getMethodName(HttpServletRequest request) {
        if(!isLogin(request.getCookies())) {
            return "redirect:/admin/login";
        }

        return "admin_main.html";
    }   

    @GetMapping("/requests")
    public String requestsAccount(HttpServletRequest request, Model model) {
        if(!isLogin(request.getCookies())) {
            return "redirect:/admin/login";
        }

        model.addAttribute("pendingUsers", userService.getNotAllowedUsers());

        return "admin_requests.html";
    }
    
    @PostMapping("/requests/approve")
    public String approveAccount(@RequestParam(name = "key") String apiKey, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            if(!isLogin(request.getCookies())) {
                return "redirect:/admin/login";
            }

            if(!userService.contains(apiKey)) {
                logger.warn("승인 시도: 존재하지 않는 유저({})", apiKey);
                model.addAttribute("errorMessage", "존재하지 않는 유저입니다.");
                return "error.html";
            }
            
            UserData user = userService.findByKey(apiKey);
            String dockerUrl = dockerService.makeContainer(user, domain.getDomain());
            
            if(dockerUrl == null) {
                logger.error("컨테이너 생성 실패: {}", apiKey);
                model.addAttribute("errorMessage", "컨테이너 생성 실패.");
                return "error.html";
            }

            user.setAllow(true);
            user.setJupyterUrl(dockerUrl);
            userService.save(user);
            logger.info("유저 승인 완료: {}", apiKey);
            redirectAttributes.addFlashAttribute("successMessage", "승인 완료");

            return "redirect:/admin/requests";
        } catch (Exception e) {
            logger.error("유저 승인 처리 중 예외 발생: {}", apiKey, e);
            model.addAttribute("errorMessage", "승인 처리 중 오류가 발생했습니다.");
            return "error.html";
        }
    }
    
    @PostMapping("/requests/reject")
    public String rejectAccount(@RequestParam(name = "key") String apiKey, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            if(!isLogin(request.getCookies())) {
                return "redirect:/admin/login";
            }

            if(!userService.contains(apiKey)) {
                logger.warn("거절 시도: 존재하지 않는 유저({})", apiKey);
                model.addAttribute("errorMessage", "존재하지 않는 유저입니다.");
                return "error.html";
            }

            userService.remove(apiKey);
            logger.info("유저 거절 및 삭제: {}", apiKey);
            redirectAttributes.addFlashAttribute("successMessage", "거절 완료");        
            
            return "redirect:/admin/requests";
        } catch (Exception e) {
            logger.error("유저 거절 처리 중 예외 발생: {}", apiKey, e);
            model.addAttribute("errorMessage", "거절 처리 중 오류가 발생했습니다.");
            return "error.html";
        }
    }

    @GetMapping("/users")
    public String userList(HttpServletRequest request, Model model) {
        if(!isLogin(request.getCookies())) {
            return "redirect:/admin/login";
        }

        model.addAttribute("allUsers", userService.getAllowedUsers());

        return "admin_users.html";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam(name = "key") String apiKey, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            if(!isLogin(request.getCookies())) {
                return "redirect:/admin/login";
            }

            if(!userService.contains(apiKey)) {
                logger.warn("유저 삭제 시도: 존재하지 않는 유저({})", apiKey);
                model.addAttribute("errorMessage", "존재하지 않는 유저입니다.");
                return "error.html";
            }

            UserData user = userService.findByKey(apiKey);

            if(!dockerService.removeContainer(user)) {
                logger.error("도커 삭제 실패: {}", apiKey);
                model.addAttribute("errorMessage", "도커 삭제 실패.");
                return "error.html";
            }

            userService.remove(apiKey);
            logger.info("유저 및 컨테이너 삭제 완료: {}", apiKey);
            redirectAttributes.addFlashAttribute("success", "유저를 삭제했습니다.");
            
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.error("유저 삭제 처리 중 예외 발생: {}", apiKey, e);
            model.addAttribute("errorMessage", "유저 삭제 처리 중 오류가 발생했습니다.");
            return "error.html";
        }
    }

    @GetMapping("/vmlogs")
    public String showVmLogs(HttpServletRequest request,Model model) {
        if(!isLogin(request.getCookies())) {
            return "redirect:/admin/login";
        }
        List<ContainerData> containers = dockerService.getAllContainers();
        model.addAttribute("containers", containers);
        return "admin_containers.html";
    }

    @PostMapping("/vmlogs/stop")
    public String stopContainer(@RequestParam(name = "key") String containerId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            if(!isLogin(request.getCookies())) {
                return "redirect:/admin/login";
            }

            boolean result = dockerService.stopContainer(containerId);
            if (result) {
                logger.info("컨테이너 중지 성공: {}", containerId);
                redirectAttributes.addFlashAttribute("success", "컨테이너 중지 성공");
            } else {
                logger.error("컨테이너 중지 실패: {}", containerId);
                redirectAttributes.addFlashAttribute("error", "중지 실패");
            }
            return "redirect:/admin/vmlogs";
        } catch (Exception e) {
            logger.error("컨테이너 중지 처리 중 예외 발생: {}", containerId, e);
            redirectAttributes.addFlashAttribute("error", "중지 처리 중 오류가 발생했습니다.");
            return "redirect:/admin/vmlogs";
        }
    }

    @PostMapping("vmlogs/start")
    public String startContainer(@RequestParam(name = "key") String containerId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            if(!isLogin(request.getCookies())) {
                return "redirect:/admin/login";
            }

            boolean result = dockerService.startContainer(containerId);
            if (result) {
                logger.info("컨테이너 실행 성공: {}", containerId);
                redirectAttributes.addFlashAttribute("success", "컨테이너 실행 성공");
            } else {
                logger.error("컨테이너 실행 실패: {}", containerId);
                redirectAttributes.addFlashAttribute("error", "중지 실패");
            }
            return "redirect:/admin/vmlogs";
        } catch (Exception e) {
            logger.error("컨테이너 실행 처리 중 예외 발생: {}", containerId, e);
            redirectAttributes.addFlashAttribute("error", "실행 처리 중 오류가 발생했습니다.");
            return "redirect:/admin/vmlogs";
        }
    }

    public Boolean isLogin(Cookie[] cookies) {;		
		if(cookies == null) return false;
		
		String jwt = getJWT(cookies);
		if(jwt == null) return false;
		
		var claims = jwtManager.getClaims(jwt);
		if(claims == null) return false;
		if( !(jwtManager.isEnd(claims)) ) return true;
		return false;
	}

    public String getJWT(Cookie[] cookies) {
		for(var cookie : cookies) {
			if(cookie.getName().equals("Auth")) {
				return cookie.getValue();
			}
		}
		
		return null;
	}
}
