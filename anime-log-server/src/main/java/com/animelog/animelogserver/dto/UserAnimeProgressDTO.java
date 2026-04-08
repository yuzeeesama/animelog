package com.animelog.animelogserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "观看进度更新参数")
public class UserAnimeProgressDTO {
    @Schema(description = "当前看到第几集", example = "14")
    @NotNull(message = "当前集数不能为空")
    @Min(value = 0, message = "当前集数不能小于0")
    private Integer currentEpisode;
}
