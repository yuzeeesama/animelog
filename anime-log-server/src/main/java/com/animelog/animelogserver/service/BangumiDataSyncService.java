package com.animelog.animelogserver.service;

import com.animelog.animelogserver.entity.Anime;
import com.animelog.animelogserver.vo.AnimeVO;

import java.util.List;

public interface BangumiDataSyncService {
    void syncByKeyword(String keyword);

    List<AnimeVO> searchFallback(String keyword);

    Anime ensureLocalAnimeByKeyword(String keyword);
}
