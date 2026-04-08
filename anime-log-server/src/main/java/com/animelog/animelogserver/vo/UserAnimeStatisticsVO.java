package com.animelog.animelogserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "追番统计信息")
public class UserAnimeStatisticsVO {
    @Schema(description = "想看数量")
    private Long wantWatchCount;
    @Schema(description = "在看数量")
    private Long watchingCount;
    @Schema(description = "看完数量")
    private Long watchedCount;
    @Schema(description = "搁置数量")
    private Long shelvedCount;
    @Schema(description = "弃番数量")
    private Long droppedCount;
    @Schema(description = "总观看集数")
    private Long totalEpisodesWatched;
}
