package com.animelog.animelogserver.service;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.dto.EpisodeLogQueryDTO;
import com.animelog.animelogserver.dto.EpisodeLogSaveDTO;
import com.animelog.animelogserver.vo.EpisodeLogVO;

import java.util.List;

public interface EpisodeLogService {
    void save(Long userId, EpisodeLogSaveDTO dto);

    EpisodeLogVO getDetail(Long userId, Long id);

    void update(Long userId, Long id, EpisodeLogSaveDTO dto);

    void delete(Long userId, Long id);

    List<EpisodeLogVO> listByAnimeId(Long userId, Long animeId);

    PageResult<EpisodeLogVO> timeline(Long userId, EpisodeLogQueryDTO dto);

    PageResult<EpisodeLogVO> highlightList(Long userId, EpisodeLogQueryDTO dto);
}
