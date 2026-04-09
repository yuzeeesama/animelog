package com.animelog.animelogserver.service;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.dto.AnimeQueryDTO;
import com.animelog.animelogserver.dto.AnimeSaveDTO;
import com.animelog.animelogserver.dto.ExternalAnimeFollowDTO;
import com.animelog.animelogserver.vo.AnimeVO;

import java.util.List;

public interface AnimeService {
    void save(Long userId, AnimeSaveDTO dto);

    PageResult<AnimeVO> page(AnimeQueryDTO dto);

    AnimeVO getById(Long id);

    List<AnimeVO> search(String keyword);

    AnimeVO getExternalDetail(String sourceProvider, Long sourceSubjectId);

    Long followExternal(Long userId, ExternalAnimeFollowDTO dto);

    List<AnimeVO> getTodayCalendar();
}
