package com.animelog.animelogserver.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EpisodeLog {
    private Long id;
    private Long userId;
    private Long animeId;
    private Long userAnimeId;
    private Integer episodeNo;
    private String episodeTitle;
    private String content;
    private String moodTag;
    private BigDecimal score;
    private Integer isHighlight;
    private LocalDateTime watchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
