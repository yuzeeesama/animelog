package com.animelog.animelogserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "新增番剧请求参数")
public class AnimeSaveDTO {
    @Schema(description = "番剧名称", example = "葬送的芙莉莲")
    @NotBlank(message = "番剧名称不能为空")
    @Size(max = 100, message = "番剧名称长度不能超过100")
    private String name;

    @Schema(description = "原始名称", example = "Sousou no Frieren")
    @Size(max = 150, message = "原始名称长度不能超过150")
    private String originalName;

    @Schema(description = "封面地址", example = "https://example.com/frieren.jpg")
    @Size(max = 255, message = "封面地址长度不能超过255")
    private String coverUrl;

    @Schema(description = "总集数", example = "28")
    @NotNull(message = "总集数不能为空")
    @Min(value = 0, message = "总集数不能小于0")
    private Integer totalEpisodes;

    @Schema(description = "番剧类型", example = "奇幻")
    @Size(max = 50, message = "类型长度不能超过50")
    private String type;

    @Schema(description = "来源类型", example = "漫画")
    @Size(max = 50, message = "来源类型长度不能超过50")
    private String sourceType;

    @Schema(description = "上映年份", example = "2023")
    @Min(value = 1900, message = "上映年份不正确")
    @Max(value = 2100, message = "上映年份不正确")
    private Integer releaseYear;

    @Schema(description = "季度", example = "秋")
    @Size(max = 20, message = "季度长度不能超过20")
    private String season;

    @Schema(description = "简介", example = "勇者队伍打倒魔王后的故事")
    private String synopsis;
}
