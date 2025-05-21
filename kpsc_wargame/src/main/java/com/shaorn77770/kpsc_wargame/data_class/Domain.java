package com.shaorn77770.kpsc_wargame.data_class;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
@ConfigurationProperties(prefix = "domain")
@RequiredArgsConstructor
@Data
public class Domain {
    private String domain;
}
