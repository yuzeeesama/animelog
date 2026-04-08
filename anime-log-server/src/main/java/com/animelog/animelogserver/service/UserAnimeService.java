package com.animelog.animelogserver.service;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.dto.UserAnimeProgressDTO;
import com.animelog.animelogserver.dto.UserAnimeQueryDTO;
import com.animelog.animelogserver.dto.UserAnimeSaveDTO;
import com.animelog.animelogserver.vo.UserAnimeStatisticsVO;
import com.animelog.animelogserver.vo.UserAnimeVO;

public interface UserAnimeService {
    void save(Long userId, UserAnimeSaveDTO dto);

    PageResult<UserAnimeVO> page(Long userId, UserAnimeQueryDTO dto);

    UserAnimeVO getDetail(Long userId, Long id);

    void update(Long userId, Long id, UserAnimeSaveDTO dto);

    void updateProgress(Long userId, Long id, UserAnimeProgressDTO dto);

    void delete(Long userId, Long id);

    UserAnimeStatisticsVO statistics(Long userId);
}
