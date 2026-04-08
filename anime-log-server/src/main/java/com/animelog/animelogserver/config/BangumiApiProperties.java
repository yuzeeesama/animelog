package com.animelog.animelogserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "animelog.bangumi-api")
public class BangumiApiProperties {
    private String baseUrl = "https://api.bgm.tv";
    private String userAgent = "yuze/anime-log-server (https://github.com/yuze/myweb)";
    private Long searchCacheMinutes = 10L;
    private Long searchTimeoutSeconds = 6L;
    private Long detailTimeoutSeconds = 10L;
    private Long refreshCooldownMinutes = 30L;
}
