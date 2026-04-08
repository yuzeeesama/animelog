package com.animelog.animelogserver.dto;

import com.animelog.animelogserver.common.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAnimeQueryDTO extends PageQuery {
    @Min(value = 0, message = "追番状态不正确")
    @Max(value = 4, message = "追番状态不正确")
    private Integer watchStatus;

    private String keyword;
}
