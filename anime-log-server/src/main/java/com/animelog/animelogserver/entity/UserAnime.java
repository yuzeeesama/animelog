package com.animelog.animelogserver.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserAnime {
    private Long id;
    private Long userId;
    private Long animeId;
    private Integer watchStatus;
    private Integer currentEpisode;
    private BigDecimal score;
    private Integer isFavorite;
    private LocalDate startDate;
    private LocalDate finishDate;
    private LocalDateTime lastWatchTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
