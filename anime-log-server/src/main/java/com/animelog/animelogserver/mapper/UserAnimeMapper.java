package com.animelog.animelogserver.mapper;

import com.animelog.animelogserver.entity.UserAnime;
import com.animelog.animelogserver.vo.UserAnimeStatisticsVO;
import com.animelog.animelogserver.vo.UserAnimeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserAnimeMapper {
    int insert(UserAnime userAnime);

    int update(UserAnime userAnime);

    int updateProgress(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("currentEpisode") Integer currentEpisode);

    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    UserAnime selectById(@Param("id") Long id);

    UserAnime selectByUserIdAndAnimeId(@Param("userId") Long userId, @Param("animeId") Long animeId);

    UserAnimeVO selectDetail(@Param("id") Long id, @Param("userId") Long userId);

    List<UserAnimeVO> selectPage(@Param("userId") Long userId,
                                 @Param("watchStatus") Integer watchStatus,
                                 @Param("keyword") String keyword);

    UserAnimeStatisticsVO selectStatistics(@Param("userId") Long userId);
}
