package com.animelog.animelogserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "单集日志新增或修改参数")
public class EpisodeLogSaveDTO {
    @Schema(description = "番剧ID", example = "1")
    @NotNull(message = "番剧ID不能为空")
    private Long animeId;

    @Schema(description = "用户追番记录ID", example = "1")
    private Long userAnimeId;

    @Schema(description = "集数", example = "12")
    @NotNull(message = "集数不能为空")
    @Min(value = 1, message = "集数必须大于0")
    private Integer episodeNo;

    @Schema(description = "本集标题", example = "真正的勇者")
    @Size(max = 100, message = "标题长度不能超过100")
    private String episodeTitle;

    @Schema(description = "日志内容", example = "这一集情绪堆得特别好，最后那段太戳人了。")
    @NotBlank(message = "日志内容不能为空")
    private String content;

    @Schema(description = "心情标签", example = "泪目")
    @Size(max = 50, message = "心情标签长度不能超过50")
    private String moodTag;

    @Schema(description = "本集评分", example = "10.0")
    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 10, message = "评分不能大于10")
    private BigDecimal score;

    @Schema(description = "是否神回 0否 1是", example = "1")
    @Min(value = 0, message = "神回状态不正确")
    @Max(value = 1, message = "神回状态不正确")
    private Integer isHighlight;

    @Schema(description = "观看时间", example = "2026-04-07 21:10:00")
    private LocalDateTime watchedAt;
}
