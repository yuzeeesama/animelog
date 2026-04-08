package com.animelog.animelogserver.controller;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.common.Result;
import com.animelog.animelogserver.common.UserContext;
import com.animelog.animelogserver.dto.EpisodeLogQueryDTO;
import com.animelog.animelogserver.dto.EpisodeLogSaveDTO;
import com.animelog.animelogserver.service.EpisodeLogService;
import com.animelog.animelogserver.vo.EpisodeLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/episode-log")
@RequiredArgsConstructor
@Tag(name = "单集日志模块", description = "用户对单集内容的日志记录接口")
@Slf4j
public class EpisodeLogController {
    private final EpisodeLogService episodeLogService;

    /**
     * 为某部番的某一集新增日志。
     */
    @Operation(summary = "新增单集日志")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody EpisodeLogSaveDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("新增单集日志, userId={}, animeId={}, episodeNo={}", userId, dto.getAnimeId(), dto.getEpisodeNo());
        episodeLogService.save(userId, dto);
        return Result.success("保存成功", null);
    }

    /**
     * 获取单条日志详情。
     */
    @Operation(summary = "查询日志详情")
    @GetMapping("/{id}")
    public Result<EpisodeLogVO> getDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        log.info("查询日志详情, userId={}, logId={}", userId, id);
        return Result.success(episodeLogService.getDetail(userId, id));
    }

    /**
     * 修改单集日志内容。
     */
    @Operation(summary = "修改单集日志")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EpisodeLogSaveDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("修改单集日志, userId={}, logId={}, animeId={}, episodeNo={}", userId, id, dto.getAnimeId(), dto.getEpisodeNo());
        episodeLogService.update(userId, id, dto);
        return Result.success("修改成功", null);
    }

    /**
     * 删除当前用户的一条日志。
     */
    @Operation(summary = "删除单集日志")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        log.info("删除单集日志, userId={}, logId={}", userId, id);
        episodeLogService.delete(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 查询当前用户在指定番剧下的日志列表。
     */
    @Operation(summary = "按番剧查询日志列表")
    @GetMapping("/anime/{animeId}")
    public Result<List<EpisodeLogVO>> listByAnimeId(@PathVariable Long animeId) {
        Long userId = UserContext.getUserId();
        log.info("按番剧查询日志列表, userId={}, animeId={}", userId, animeId);
        return Result.success(episodeLogService.listByAnimeId(userId, animeId));
    }

    /**
     * 按时间倒序查询当前用户的日志时间线。
     */
    @Operation(summary = "查询追番时间线")
    @GetMapping("/timeline")
    public Result<PageResult<EpisodeLogVO>> timeline(@Valid EpisodeLogQueryDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("查询日志时间线, userId={}, pageNum={}, pageSize={}", userId, dto.getPageNum(), dto.getPageSize());
        return Result.success(episodeLogService.timeline(userId, dto));
    }

    /**
     * 查询当前用户标记为神回的日志列表。
     */
    @Operation(summary = "查询神回日志列表")
    @GetMapping("/highlight/list")
    public Result<PageResult<EpisodeLogVO>> highlightList(@Valid EpisodeLogQueryDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("查询神回日志列表, userId={}, pageNum={}, pageSize={}", userId, dto.getPageNum(), dto.getPageSize());
        return Result.success(episodeLogService.highlightList(userId, dto));
    }
}
