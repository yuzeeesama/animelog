package com.animelog.animelogserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "animelog.bangumi-data")
public class BangumiDataProperties {
    /**
     * 官方 CDN 数据地址。
     */
    private String url = "https://unpkg.com/bangumi-data@0.3/dist/data.json";

    /**
     * 本地兜底文件，优先按 classpath 资源读取。
     */
    private String localResource = "bangumi/data.json";

    /**
     * 内存缓存时长，单位分钟。
     */
    private Long cacheMinutes = 360L;

    /**
     * 单次搜索最多同步多少条命中数据到本地库。
     */
    private Integer syncLimit = 20;
}
