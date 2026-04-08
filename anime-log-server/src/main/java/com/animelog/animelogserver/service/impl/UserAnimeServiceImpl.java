package com.animelog.animelogserver.service.impl;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.dto.UserAnimeProgressDTO;
import com.animelog.animelogserver.dto.UserAnimeQueryDTO;
import com.animelog.animelogserver.dto.UserAnimeSaveDTO;
import com.animelog.animelogserver.entity.Anime;
import com.animelog.animelogserver.entity.UserAnime;
import com.animelog.animelogserver.exception.BusinessException;
import com.animelog.animelogserver.mapper.AnimeMapper;
import com.animelog.animelogserver.mapper.UserAnimeMapper;
import com.animelog.animelogserver.service.UserAnimeService;
import com.animelog.animelogserver.vo.UserAnimeStatisticsVO;
import com.animelog.animelogserver.vo.UserAnimeVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAnimeServiceImpl implements UserAnimeService {
    private final UserAnimeMapper userAnimeMapper;
    private final AnimeMapper animeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long userId, UserAnimeSaveDTO dto) {
        validateAnimeExists(dto.getAnimeId());
        if (userAnimeMapper.selectByUserIdAndAnimeId(userId, dto.getAnimeId()) != null) {
            log.warn("新增追番记录失败, 重复添加, userId={}, animeId={}", userId, dto.getAnimeId());
            throw new BusinessException("该番剧已在你的列表中");
        }
        UserAnime userAnime = new UserAnime();
        BeanUtils.copyProperties(dto, userAnime);
        userAnime.setUserId(userId);
        userAnime.setLastWatchTime(LocalDateTime.now());
        userAnimeMapper.insert(userAnime);
        log.info("新增追番记录成功, userAnimeId={}, userId={}, animeId={}", userAnime.getId(), userId, dto.getAnimeId());
    }

    @Override
    public PageResult<UserAnimeVO> page(Long userId, UserAnimeQueryDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<UserAnimeVO> list = userAnimeMapper.selectPage(userId, dto.getWatchStatus(), dto.getKeyword());
        PageInfo<UserAnimeVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public UserAnimeVO getDetail(Long userId, Long id) {
        UserAnimeVO detail = userAnimeMapper.selectDetail(id, userId);
        if (detail == null) {
            throw new BusinessException(404, "追番记录不存在");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, UserAnimeSaveDTO dto) {
        UserAnime exists = getUserAnimeOrThrow(id, userId);
        validateAnimeExists(dto.getAnimeId());
        UserAnime duplicate = userAnimeMapper.selectByUserIdAndAnimeId(userId, dto.getAnimeId());
        if (duplicate != null && !duplicate.getId().equals(id)) {
            log.warn("修改追番记录失败, 番剧重复, userId={}, userAnimeId={}, animeId={}", userId, id, dto.getAnimeId());
            throw new BusinessException("该番剧已在你的列表中");
        }
        UserAnime userAnime = new UserAnime();
        BeanUtils.copyProperties(dto, userAnime);
        userAnime.setId(exists.getId());
        userAnime.setUserId(userId);
        userAnime.setLastWatchTime(LocalDateTime.now());
        userAnimeMapper.update(userAnime);
        log.info("修改追番记录成功, userId={}, userAnimeId={}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long userId, Long id, UserAnimeProgressDTO dto) {
        getUserAnimeOrThrow(id, userId);
        userAnimeMapper.updateProgress(id, userId, dto.getCurrentEpisode());
        log.info("更新观看进度成功, userId={}, userAnimeId={}, currentEpisode={}", userId, id, dto.getCurrentEpisode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        getUserAnimeOrThrow(id, userId);
        userAnimeMapper.deleteById(id, userId);
        log.info("删除追番记录成功, userId={}, userAnimeId={}", userId, id);
    }

    @Override
    public UserAnimeStatisticsVO statistics(Long userId) {
        UserAnimeStatisticsVO statisticsVO = userAnimeMapper.selectStatistics(userId);
        if (statisticsVO == null) {
            statisticsVO = new UserAnimeStatisticsVO();
        }
        if (statisticsVO.getWantWatchCount() == null) {
            statisticsVO.setWantWatchCount(0L);
        }
        if (statisticsVO.getWatchingCount() == null) {
            statisticsVO.setWatchingCount(0L);
        }
        if (statisticsVO.getWatchedCount() == null) {
            statisticsVO.setWatchedCount(0L);
        }
        if (statisticsVO.getShelvedCount() == null) {
            statisticsVO.setShelvedCount(0L);
        }
        if (statisticsVO.getDroppedCount() == null) {
            statisticsVO.setDroppedCount(0L);
        }
        if (statisticsVO.getTotalEpisodesWatched() == null) {
            statisticsVO.setTotalEpisodesWatched(0L);
        }
        return statisticsVO;
    }

    private UserAnime getUserAnimeOrThrow(Long id, Long userId) {
        UserAnime userAnime = userAnimeMapper.selectById(id);
        if (userAnime == null || !userAnime.getUserId().equals(userId)) {
            log.warn("追番记录不存在或无权限, userId={}, userAnimeId={}", userId, id);
            throw new BusinessException(404, "追番记录不存在");
        }
        return userAnime;
    }

    private void validateAnimeExists(Long animeId) {
        Anime anime = animeMapper.selectById(animeId);
        if (anime == null) {
            log.warn("番剧不存在, animeId={}", animeId);
            throw new BusinessException(404, "番剧不存在");
        }
    }
}
