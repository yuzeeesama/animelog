package com.animelog.animelogserver.dto;

import com.animelog.animelogserver.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EpisodeLogQueryDTO extends PageQuery {
    private Long animeId;
}
