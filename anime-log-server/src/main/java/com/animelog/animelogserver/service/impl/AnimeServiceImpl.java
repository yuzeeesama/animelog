package com.animelog.animelogserver.service.impl;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.dto.AnimeQueryDTO;
import com.animelog.animelogserver.dto.AnimeSaveDTO;
import com.animelog.animelogserver.dto.ExternalAnimeFollowDTO;
import com.animelog.animelogserver.entity.Anime;
import com.animelog.animelogserver.entity.UserAnime;
import com.animelog.animelogserver.exception.BusinessException;
import com.animelog.animelogserver.mapper.AnimeMapper;
import com.animelog.animelogserver.service.AnimeService;
import com.animelog.animelogserver.service.BangumiApiService;
import com.animelog.animelogserver.service.BangumiDataSyncService;
import com.animelog.animelogserver.mapper.UserAnimeMapper;
import com.animelog.animelogserver.vo.AnimeVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimeServiceImpl implements AnimeService {
    private final AnimeMapper animeMapper;
    private final UserAnimeMapper userAnimeMapper;
    private final BangumiApiService bangumiApiService;
    private final BangumiDataSyncService bangumiDataSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long userId, AnimeSaveDTO dto) {
        Anime anime = new Anime();
        BeanUtils.copyProperties(dto, anime);
        anime.setCreatedBy(userId);
        animeMapper.insert(anime);
        log.info("番剧新增成功, animeId={}, userId={}, animeName={}", anime.getId(), userId, anime.getName());
    }

    @Override
    public PageResult<AnimeVO> page(AnimeQueryDTO dto) {
        String keyword = StringUtils.hasText(dto.getName()) ? dto.getName() : dto.getKeyword();
        syncBangumiDataIfNeeded(keyword);
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<Anime> animeList = animeMapper.selectPage(keyword, dto.getType());
        PageInfo<Anime> pageInfo = new PageInfo<>(animeList);
        List<AnimeVO> list = animeList.stream().map(this::toVO).toList();
        return new PageResult<>(pageInfo.getTotal(), list);
    }

    @Override
    public AnimeVO getById(Long id) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null) {
            log.warn("查询番剧详情失败, 番剧不存在, animeId={}", id);
            throw new BusinessException(404, "番剧不存在");
        }
        return toVO(anime);
    }

    @Override
    public List<AnimeVO> search(String keyword) {
        try {
            return bangumiApiService.search(keyword).stream()
                    .map(this::attachLocalLink)
                    .toList();
        } catch (Exception ex) {
            log.warn("Bangumi API 搜索失败，将回退到 bangumi-data, keyword={}", keyword, ex);
            return bangumiDataSyncService.searchFallback(keyword);
        }
    }

    @Override
    public AnimeVO getExternalDetail(String sourceProvider, Long sourceSubjectId) {
        if (!"bangumi".equalsIgnoreCase(sourceProvider)) {
            throw new BusinessException(400, "暂不支持该数据源");
        }

        try {
            return attachLocalLink(bangumiApiService.getDetail(sourceSubjectId));
        } catch (Exception ex) {
            log.warn("Bangumi API 详情查询失败，尝试回退本地缓存, sourceProvider={}, sourceSubjectId={}",
                    sourceProvider, sourceSubjectId, ex);
            Anime localAnime = animeMapper.selectBySource(sourceProvider, sourceSubjectId);
            if (localAnime != null) {
                AnimeVO fallback = toVO(localAnime);
                fallback.setFallbackSource("local-cache");
                fallback.setExternal(false);
                return fallback;
            }
            throw new BusinessException(502, "当前无法获取该番剧详情");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long followExternal(Long userId, ExternalAnimeFollowDTO dto) {
        Anime localAnime = ensureLocalAnimeFromExternal(dto.getSourceProvider(), dto.getSourceSubjectId());
        if (localAnime == null) {
            throw new BusinessException(404, "番剧不存在");
        }
        if (userAnimeMapper.selectByUserIdAndAnimeId(userId, localAnime.getId()) != null) {
            throw new BusinessException("该番剧已在你的列表中");
        }

        UserAnime userAnime = new UserAnime();
        userAnime.setUserId(userId);
        userAnime.setAnimeId(localAnime.getId());
        userAnime.setWatchStatus(dto.getWatchStatus());
        userAnime.setCurrentEpisode(dto.getCurrentEpisode());
        userAnime.setScore(dto.getScore());
        userAnime.setIsFavorite(dto.getIsFavorite() == null ? 0 : dto.getIsFavorite());
        userAnime.setStartDate(dto.getStartDate());
        userAnime.setFinishDate(dto.getFinishDate());
        userAnime.setRemark(dto.getRemark());
        userAnime.setLastWatchTime(LocalDateTime.now());
        userAnimeMapper.insert(userAnime);
        return localAnime.getId();
    }

    private void syncBangumiDataIfNeeded(String... keywords) {
        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }
            try {
                bangumiDataSyncService.syncByKeyword(keyword);
            } catch (Exception ex) {
                log.warn("bangumi-data 同步失败，将回退为本地库查询, keyword={}", keyword, ex);
            }
            return;
        }
    }

    private AnimeVO toVO(Anime anime) {
        AnimeVO vo = new AnimeVO();
        BeanUtils.copyProperties(anime, vo);
        vo.setExternal(false);
        return vo;
    }

    private AnimeVO attachLocalLink(AnimeVO vo) {
        if (vo == null || !StringUtils.hasText(vo.getSourceProvider()) || vo.getSourceSubjectId() == null) {
            return vo;
        }
        Anime localAnime = animeMapper.selectBySource(vo.getSourceProvider(), vo.getSourceSubjectId());
        if (localAnime == null && StringUtils.hasText(vo.getName())) {
            localAnime = animeMapper.selectByNameAndReleaseYear(vo.getName(), vo.getReleaseYear());
        }
        if (localAnime != null) {
            vo.setId(localAnime.getId());
        }
        return vo;
    }

    private Anime ensureLocalAnimeFromExternal(String sourceProvider, Long sourceSubjectId) {
        if (!"bangumi".equalsIgnoreCase(sourceProvider)) {
            throw new BusinessException(400, "暂不支持该数据源");
        }

        Anime existing = animeMapper.selectBySource(sourceProvider, sourceSubjectId);
        if (existing != null) {
            return existing;
        }

        AnimeVO detail;
        try {
            detail = bangumiApiService.getDetail(sourceSubjectId);
        } catch (Exception ex) {
            Anime localFallback = animeMapper.selectBySource(sourceProvider, sourceSubjectId);
            if (localFallback != null) {
                return localFallback;
            }
            throw new BusinessException(502, "当前无法同步该番剧信息");
        }

        Anime anime = new Anime();
        anime.setName(detail.getName());
        anime.setOriginalName(detail.getOriginalName());
        anime.setCoverUrl(detail.getCoverUrl());
        anime.setTotalEpisodes(detail.getTotalEpisodes() == null ? 0 : detail.getTotalEpisodes());
        anime.setType(detail.getType());
        anime.setSourceType(detail.getSourceType());
        anime.setReleaseYear(detail.getReleaseYear());
        anime.setSeason(detail.getSeason());
        anime.setSynopsis(detail.getSynopsis());
        anime.setSourceProvider(detail.getSourceProvider());
        anime.setSourceSubjectId(detail.getSourceSubjectId());

        Anime bySource = animeMapper.selectBySource(anime.getSourceProvider(), anime.getSourceSubjectId());
        if (bySource != null) {
            anime.setId(bySource.getId());
            animeMapper.updateSyncFields(anime);
            return animeMapper.selectById(bySource.getId());
        }

        Anime byName = animeMapper.selectByNameAndReleaseYear(anime.getName(), anime.getReleaseYear());
        if (byName != null) {
            anime.setId(byName.getId());
            animeMapper.updateSyncFields(anime);
            return animeMapper.selectById(byName.getId());
        }

        animeMapper.insert(anime);
        return anime;
    }
}
