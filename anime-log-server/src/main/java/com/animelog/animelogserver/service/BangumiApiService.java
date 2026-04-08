package com.animelog.animelogserver.service;

import com.animelog.animelogserver.vo.AnimeVO;

import java.util.List;

public interface BangumiApiService {
    List<AnimeVO> search(String keyword);

    AnimeVO getDetail(Long subjectId);
}
