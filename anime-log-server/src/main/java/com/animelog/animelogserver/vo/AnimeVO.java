package com.animelog.animelogserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "番剧信息")
public class AnimeVO {
    @Schema(description = "番剧ID")
    private Long id;
    @Schema(description = "番剧名称")
    private String name;
    @Schema(description = "原始名称")
    private String originalName;
    @Schema(description = "封面地址")
    private String coverUrl;
    @Schema(description = "总集数")
    private Integer totalEpisodes;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "来源类型")
    private String sourceType;
    @Schema(description = "上映年份")
    private Integer releaseYear;
    @Schema(description = "季度")
    private String season;
    @Schema(description = "简介")
    private String synopsis;
    @Schema(description = "数据源提供方")
    private String sourceProvider;
    @Schema(description = "外部条目ID")
    private Long sourceSubjectId;
    @Schema(description = "外部数据同步时间")
    private LocalDateTime sourceSyncedAt;
    @Schema(description = "是否外部条目")
    private Boolean external;
    @Schema(description = "回退数据源标记")
    private String fallbackSource;
    @Schema(description = "放送日期")
    private LocalDate airDate;
    @Schema(description = "评分")
    private BigDecimal ratingScore;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
