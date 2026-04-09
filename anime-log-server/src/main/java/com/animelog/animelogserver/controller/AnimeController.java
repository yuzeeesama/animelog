package com.animelog.animelogserver.controller;

import com.animelog.animelogserver.common.PageResult;
import com.animelog.animelogserver.common.Result;
import com.animelog.animelogserver.common.UserContext;
import com.animelog.animelogserver.dto.AnimeQueryDTO;
import com.animelog.animelogserver.dto.AnimeSaveDTO;
import com.animelog.animelogserver.dto.ExternalAnimeFollowDTO;
import com.animelog.animelogserver.service.AnimeService;
import com.animelog.animelogserver.vo.AnimeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
@Tag(name = "番剧模块", description = "公共番剧库相关接口")
@Slf4j
public class AnimeController {
    private final AnimeService animeService;

    /**
     * 手动新增一条公共番剧数据。
     */
    @Operation(summary = "新增番剧")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody AnimeSaveDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("新增番剧, userId={}, animeName={}", userId, dto.getName());
        animeService.save(userId, dto);
        return Result.success("新增成功", null);
    }

    /**
     * 分页查询番剧列表。
     */
    @Operation(summary = "分页查询番剧列表")
    @GetMapping("/list")
    public Result<PageResult<AnimeVO>> page(@Valid AnimeQueryDTO dto) {
        log.info("分页查询番剧列表, pageNum={}, pageSize={}, name={}, type={}",
                dto.getPageNum(), dto.getPageSize(), dto.getName(), dto.getType());
        return Result.success(animeService.page(dto));
    }

    /**
     * 根据 ID 查询番剧详情。
     */
    @Operation(summary = "查询番剧详情")
    @GetMapping("/{id}")
    public Result<AnimeVO> getById(@PathVariable Long id) {
        log.info("查询番剧详情, animeId={}", id);
        return Result.success(animeService.getById(id));
    }

    /**
     * 查询外部番剧详情。
     */
    @Operation(summary = "查询外部番剧详情")
    @GetMapping("/external/{sourceProvider}/{sourceSubjectId}")
    public Result<AnimeVO> getExternalDetail(@PathVariable String sourceProvider, @PathVariable Long sourceSubjectId) {
        log.info("查询外部番剧详情, sourceProvider={}, sourceSubjectId={}", sourceProvider, sourceSubjectId);
        return Result.success(animeService.getExternalDetail(sourceProvider, sourceSubjectId));
    }

    /**
     * 按关键字模糊搜索番剧。
     */
    @Operation(summary = "搜索番剧")
    @GetMapping("/search")
    public Result<List<AnimeVO>> search(@RequestParam String keyword) {
        log.info("搜索番剧, keyword={}", keyword);
        return Result.success(animeService.search(keyword));
    }

    /**
     * 查询今日放送动画。
     */
    @Operation(summary = "查询今日放送")
    @GetMapping("/calendar/today")
    public Result<List<AnimeVO>> getTodayCalendar() {
        log.info("查询今日放送");
        return Result.success(animeService.getTodayCalendar());
    }

    /**
     * 追踪外部数据源中的番剧到本地库。
     */
    @Operation(summary = "外部番剧追番")
    @PostMapping("/follow-external")
    public Result<Long> followExternal(@Valid @RequestBody ExternalAnimeFollowDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("外部番剧追番, userId={}, sourceProvider={}, sourceSubjectId={}",
                userId, dto.getSourceProvider(), dto.getSourceSubjectId());
        return Result.success("添加成功", animeService.followExternal(userId, dto));
    }
}
