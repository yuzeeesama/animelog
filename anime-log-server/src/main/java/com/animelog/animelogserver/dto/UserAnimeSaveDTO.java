package com.animelog.animelogserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "追番记录新增或修改参数")
public class UserAnimeSaveDTO {
    @Schema(description = "番剧ID", example = "1")
    @NotNull(message = "番剧ID不能为空")
    private Long animeId;

    @Schema(description = "追番状态 0想看 1在看 2看完 3搁置 4弃番", example = "1")
    @NotNull(message = "追番状态不能为空")
    @Min(value = 0, message = "追番状态不正确")
    @Max(value = 4, message = "追番状态不正确")
    private Integer watchStatus;

    @Schema(description = "当前看到第几集", example = "12")
    @NotNull(message = "当前集数不能为空")
    @Min(value = 0, message = "当前集数不能小于0")
    private Integer currentEpisode;

    @Schema(description = "个人评分", example = "9.5")
    @Min(value = 0, message = "评分不能小于0")
    private BigDecimal score;

    @Schema(description = "是否收藏 0否 1是", example = "1")
    @Min(value = 0, message = "收藏状态不正确")
    @Max(value = 1, message = "收藏状态不正确")
    private Integer isFavorite;

    @Schema(description = "开始追番日期", example = "2026-04-01")
    private LocalDate startDate;
    @Schema(description = "结束追番日期", example = "2026-04-20")
    private LocalDate finishDate;
    @Schema(description = "备注", example = "准备开看")
    private String remark;
}
