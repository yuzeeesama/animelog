package com.animelog.animelogserver.controller;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.common.Result;
import com.animelog.animelogserver.common.UserContext;
import com.animelog.animelogserver.dto.UserAnimeProgressDTO;
import com.animelog.animelogserver.dto.UserAnimeQueryDTO;
import com.animelog.animelogserver.dto.UserAnimeSaveDTO;
import com.animelog.animelogserver.service.UserAnimeService;
import com.animelog.animelogserver.vo.UserAnimeStatisticsVO;
import com.animelog.animelogserver.vo.UserAnimeVO;
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

@Validated
@RestController
@RequestMapping("/api/user-anime")
@RequiredArgsConstructor
@Tag(name = "追番记录模块", description = "用户个人追番列表与进度管理接口")
@Slf4j
public class UserAnimeController {
    private final UserAnimeService userAnimeService;

    /**
     * 将一部番剧加入当前用户的追番列表。
     */
    @Operation(summary = "添加追番记录")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody UserAnimeSaveDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("新增追番记录, userId={}, animeId={}", userId, dto.getAnimeId());
        userAnimeService.save(userId, dto);
        return Result.success("添加成功", null);
    }

    /**
     * 分页查询当前用户的追番列表。
     */
    @Operation(summary = "分页查询我的番剧列表")
    @GetMapping("/list")
    public Result<PageResult<UserAnimeVO>> page(@Valid UserAnimeQueryDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("分页查询追番列表, userId={}, pageNum={}, pageSize={}, watchStatus={}, keyword={}",
                userId, dto.getPageNum(), dto.getPageSize(), dto.getWatchStatus(), dto.getKeyword());
        return Result.success(userAnimeService.page(userId, dto));
    }

    /**
     * 查询一条追番记录详情。
     */
    @Operation(summary = "查询追番记录详情")
    @GetMapping("/{id}")
    public Result<UserAnimeVO> getDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        log.info("查询追番记录详情, userId={}, userAnimeId={}", userId, id);
        return Result.success(userAnimeService.getDetail(userId, id));
    }

    /**
     * 修改追番状态、评分、备注等信息。
     */
    @Operation(summary = "修改追番记录")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserAnimeSaveDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("修改追番记录, userId={}, userAnimeId={}, animeId={}", userId, id, dto.getAnimeId());
        userAnimeService.update(userId, id, dto);
        return Result.success("修改成功", null);
    }

    /**
     * 快速更新当前观看进度。
     */
    @Operation(summary = "更新观看进度")
    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(@PathVariable Long id, @Valid @RequestBody UserAnimeProgressDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("更新观看进度, userId={}, userAnimeId={}, currentEpisode={}", userId, id, dto.getCurrentEpisode());
        userAnimeService.updateProgress(userId, id, dto);
        return Result.success("修改成功", null);
    }

    /**
     * 删除一条追番记录。
     */
    @Operation(summary = "删除追番记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        log.info("删除追番记录, userId={}, userAnimeId={}", userId, id);
        userAnimeService.delete(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取当前用户的追番统计数据。
     */
    @Operation(summary = "获取追番统计")
    @GetMapping("/statistics")
    public Result<UserAnimeStatisticsVO> statistics() {
        Long userId = UserContext.getUserId();
        log.info("查询追番统计, userId={}", userId);
        return Result.success(userAnimeService.statistics(userId));
    }
}
