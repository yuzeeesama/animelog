package com.animelog.animelogserver.mapper;

import com.animelog.animelogserver.entity.EpisodeLog;
import com.animelog.animelogserver.vo.EpisodeLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EpisodeLogMapper {
    int insert(EpisodeLog episodeLog);

    int update(EpisodeLog episodeLog);

    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    EpisodeLog selectById(@Param("id") Long id);

    EpisodeLog selectByUniqueKey(@Param("userId") Long userId,
                                 @Param("animeId") Long animeId,
                                 @Param("episodeNo") Integer episodeNo);

    List<EpisodeLogVO> selectByAnimeId(@Param("userId") Long userId, @Param("animeId") Long animeId);

    EpisodeLogVO selectDetail(@Param("id") Long id, @Param("userId") Long userId);

    List<EpisodeLogVO> selectTimeline(@Param("userId") Long userId);

    List<EpisodeLogVO> selectHighlightList(@Param("userId") Long userId);
}
