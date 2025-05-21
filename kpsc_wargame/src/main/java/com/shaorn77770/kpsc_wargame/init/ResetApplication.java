package com.shaorn77770.kpsc_wargame.init;

import org.springframework.stereotype.Component;

import com.shaorn77770.kpsc_wargame.data_class.Domain;
import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.service.DockerService;
import com.shaorn77770.kpsc_wargame.database.service.UserService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResetApplication {
    
    private final UserService userService;
    private final DockerService dockerService;
    private final Domain domain;

    @PostConstruct
    public void init() {
        var userList = userService.getAllowedUsers();

        for (UserData userData : userList) {
            String dockerUrl = dockerService.makeContianer(userData, domain.getDomain());
            
            if(dockerUrl == null) {
                continue;
            }

            userData.setJupyterUrl(dockerUrl);
            userService.save(userData);
        }
    }
}
