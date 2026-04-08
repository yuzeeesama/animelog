package com.animelog.animelogserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "单集日志信息")
public class EpisodeLogVO {
    @Schema(description = "日志ID")
    private Long id;
    @Schema(description = "番剧ID")
    private Long animeId;
    @Schema(description = "用户追番记录ID")
    private Long userAnimeId;
    @Schema(description = "集数")
    private Integer episodeNo;
    @Schema(description = "本集标题")
    private String episodeTitle;
    @Schema(description = "日志内容")
    private String content;
    @Schema(description = "心情标签")
    private String moodTag;
    @Schema(description = "评分")
    private BigDecimal score;
    @Schema(description = "是否神回")
    private Integer isHighlight;
    @Schema(description = "观看时间")
    private LocalDateTime watchedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "番剧名称")
    private String animeName;
    @Schema(description = "番剧封面")
    private String coverUrl;
}
