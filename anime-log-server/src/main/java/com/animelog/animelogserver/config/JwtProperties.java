package com.animelog.animelogserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "animelog.jwt")
public class JwtProperties {
    private String secret;
    private Long expireHours;
    private String issuer;
}
