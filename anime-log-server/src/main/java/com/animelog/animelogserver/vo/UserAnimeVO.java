package com.animelog.animelogserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户追番记录")
public class UserAnimeVO {
    @Schema(description = "追番记录ID")
    private Long id;
    @Schema(description = "番剧ID")
    private Long animeId;
    @Schema(description = "番剧名称")
    private String animeName;
    @Schema(description = "封面地址")
    private String coverUrl;
    @Schema(description = "总集数")
    private Integer totalEpisodes;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "追番状态")
    private Integer watchStatus;
    @Schema(description = "当前看到第几集")
    private Integer currentEpisode;
    @Schema(description = "个人评分")
    private BigDecimal score;
    @Schema(description = "是否收藏")
    private Integer isFavorite;
    @Schema(description = "开始追番日期")
    private LocalDate startDate;
    @Schema(description = "结束追番日期")
    private LocalDate finishDate;
    @Schema(description = "最后观看时间")
    private LocalDateTime lastWatchTime;
    @Schema(description = "备注")
    private String remark;
}
