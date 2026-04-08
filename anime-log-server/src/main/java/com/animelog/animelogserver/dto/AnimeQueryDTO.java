package com.animelog.animelogserver.dto;

import com.animelog.animelogserver.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnimeQueryDTO extends PageQuery {
    private String name;
    private String type;
    private String keyword;
}
