package com.animelog.animelogserver.mapper;

import com.animelog.animelogserver.entity.Anime;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnimeMapper {
    int insert(Anime anime);

    Anime selectById(@Param("id") Long id);

    Anime selectBySource(@Param("sourceProvider") String sourceProvider, @Param("sourceSubjectId") Long sourceSubjectId);

    Anime selectByNameAndReleaseYear(@Param("name") String name, @Param("releaseYear") Integer releaseYear);

    List<Anime> searchBangumiCache(@Param("keyword") String keyword);

    List<Anime> selectPage(@Param("name") String name, @Param("type") String type);

    List<Anime> searchByKeyword(@Param("keyword") String keyword);

    int updateSyncFields(Anime anime);
}
