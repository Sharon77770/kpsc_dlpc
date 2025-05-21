package com.shaorn77770.kpsc_wargame.data_class;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@ConfigurationProperties(prefix = "admin")
@RequiredArgsConstructor
public class Admin {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

