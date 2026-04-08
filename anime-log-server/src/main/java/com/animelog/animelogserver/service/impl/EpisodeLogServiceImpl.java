package com.animelog.animelogserver.service.impl;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.dto.EpisodeLogQueryDTO;
import com.animelog.animelogserver.dto.EpisodeLogSaveDTO;
import com.animelog.animelogserver.entity.EpisodeLog;
import com.animelog.animelogserver.entity.UserAnime;
import com.animelog.animelogserver.exception.BusinessException;
import com.animelog.animelogserver.mapper.EpisodeLogMapper;
import com.animelog.animelogserver.mapper.UserAnimeMapper;
import com.animelog.animelogserver.service.EpisodeLogService;
import com.animelog.animelogserver.vo.EpisodeLogVO;
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
public class EpisodeLogServiceImpl implements EpisodeLogService {
    private final EpisodeLogMapper episodeLogMapper;
    private final UserAnimeMapper userAnimeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long userId, EpisodeLogSaveDTO dto) {
        UserAnime userAnime = validateUserAnime(userId, dto.getAnimeId(), dto.getUserAnimeId());
        EpisodeLog exists = episodeLogMapper.selectByUniqueKey(userId, dto.getAnimeId(), dto.getEpisodeNo());
        if (exists != null) {
            log.warn("新增单集日志失败, 日志已存在, userId={}, animeId={}, episodeNo={}", userId, dto.getAnimeId(), dto.getEpisodeNo());
            throw new BusinessException("该集日志已存在，请使用编辑功能");
        }
        EpisodeLog episodeLog = new EpisodeLog();
        BeanUtils.copyProperties(dto, episodeLog);
        episodeLog.setUserId(userId);
        episodeLog.setUserAnimeId(userAnime.getId());
        if (episodeLog.getWatchedAt() == null) {
            episodeLog.setWatchedAt(LocalDateTime.now());
        }
        if (episodeLog.getIsHighlight() == null) {
            episodeLog.setIsHighlight(0);
        }
        episodeLogMapper.insert(episodeLog);
        log.info("新增单集日志成功, logId={}, userId={}, animeId={}, episodeNo={}",
                episodeLog.getId(), userId, dto.getAnimeId(), dto.getEpisodeNo());
    }

    @Override
    public EpisodeLogVO getDetail(Long userId, Long id) {
        EpisodeLogVO detail = episodeLogMapper.selectDetail(id, userId);
        if (detail == null) {
            log.warn("查询日志详情失败, 日志不存在, userId={}, logId={}", userId, id);
            throw new BusinessException(404, "日志不存在");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, EpisodeLogSaveDTO dto) {
        EpisodeLog exists = getEpisodeLogOrThrow(userId, id);
        UserAnime userAnime = validateUserAnime(userId, dto.getAnimeId(), dto.getUserAnimeId());
        EpisodeLog duplicate = episodeLogMapper.selectByUniqueKey(userId, dto.getAnimeId(), dto.getEpisodeNo());
        if (duplicate != null && !duplicate.getId().equals(id)) {
            log.warn("修改单集日志失败, 日志重复, userId={}, logId={}, animeId={}, episodeNo={}",
                    userId, id, dto.getAnimeId(), dto.getEpisodeNo());
            throw new BusinessException("该集日志已存在");
        }
        EpisodeLog episodeLog = new EpisodeLog();
        BeanUtils.copyProperties(dto, episodeLog);
        episodeLog.setId(exists.getId());
        episodeLog.setUserId(userId);
        episodeLog.setUserAnimeId(userAnime.getId());
        if (episodeLog.getWatchedAt() == null) {
            episodeLog.setWatchedAt(exists.getWatchedAt());
        }
        if (episodeLog.getIsHighlight() == null) {
            episodeLog.setIsHighlight(0);
        }
        episodeLogMapper.update(episodeLog);
        log.info("修改单集日志成功, userId={}, logId={}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        getEpisodeLogOrThrow(userId, id);
        episodeLogMapper.deleteById(id, userId);
        log.info("删除单集日志成功, userId={}, logId={}", userId, id);
    }

    @Override
    public List<EpisodeLogVO> listByAnimeId(Long userId, Long animeId) {
        return episodeLogMapper.selectByAnimeId(userId, animeId);
    }

    @Override
    public PageResult<EpisodeLogVO> timeline(Long userId, EpisodeLogQueryDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<EpisodeLogVO> list = episodeLogMapper.selectTimeline(userId);
        PageInfo<EpisodeLogVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public PageResult<EpisodeLogVO> highlightList(Long userId, EpisodeLogQueryDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<EpisodeLogVO> list = episodeLogMapper.selectHighlightList(userId);
        PageInfo<EpisodeLogVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    private EpisodeLog getEpisodeLogOrThrow(Long userId, Long id) {
        EpisodeLog episodeLog = episodeLogMapper.selectById(id);
        if (episodeLog == null || !episodeLog.getUserId().equals(userId)) {
            log.warn("日志不存在或无权限, userId={}, logId={}", userId, id);
            throw new BusinessException(404, "日志不存在");
        }
        return episodeLog;
    }

    private UserAnime validateUserAnime(Long userId, Long animeId, Long userAnimeId) {
        UserAnime userAnime = userAnimeId != null
                ? userAnimeMapper.selectById(userAnimeId)
                : userAnimeMapper.selectByUserIdAndAnimeId(userId, animeId);
        if (userAnime == null || !userAnime.getUserId().equals(userId) || !userAnime.getAnimeId().equals(animeId)) {
            log.warn("关联追番记录不存在, userId={}, animeId={}, userAnimeId={}", userId, animeId, userAnimeId);
            throw new BusinessException(400, "请先将该番剧加入我的列表");
        }
        return userAnime;
    }
}
