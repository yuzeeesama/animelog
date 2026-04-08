package com.animelog.animelogserver.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Anime {
    private Long id;
    private String name;
    private String originalName;
    private String coverUrl;
    private Integer totalEpisodes;
    private String type;
    private String sourceType;
    private Integer releaseYear;
    private String season;
    private String synopsis;
    private String sourceProvider;
    private Long sourceSubjectId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
