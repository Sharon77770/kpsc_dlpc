package com.shaorn77770.kpsc_wargame.init;

import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.shaorn77770.kpsc_wargame.data_class.Domain;
import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.service.DockerService;
import com.shaorn77770.kpsc_wargame.database.service.UserService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResetApplication {
    private static final Logger logger = LogManager.getLogger(ResetApplication.class);
    
    private final UserService userService;
    private final DockerService dockerService;
    private final Domain domain;

    @PostConstruct
    public void init() {
        try {
            var userList = userService.getAllowedUsers();

            for (UserData userData : userList) {
                String dockerUrl = dockerService.makeContainer(userData, domain.getDomain());
                
                if(dockerUrl == null) {
                    logger.warn("컨테이너 재생성 실패: {}", userData.getApiKey());
                    continue;
                }

                userData.setJupyterUrl(dockerUrl);
                userService.save(userData);
                logger.info("컨테이너 재생성 및 유저 업데이트: {}", userData.getApiKey());
            }
        } catch (Exception e) {
            logger.error("ResetApplication 초기화 중 예외 발생", e);
        }
    }
}
