package com.animelog.animelogserver.controller;

import com.animelog.animelogserver.common.Result;
import com.animelog.animelogserver.common.UserContext;
import com.animelog.animelogserver.dto.UserLoginDTO;
import com.animelog.animelogserver.dto.UserPasswordUpdateDTO;
import com.animelog.animelogserver.dto.UserProfileUpdateDTO;
import com.animelog.animelogserver.dto.UserRegisterDTO;
import com.animelog.animelogserver.service.UserService;
import com.animelog.animelogserver.vo.LoginVO;
import com.animelog.animelogserver.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "注册、登录和个人信息相关接口")
@Slf4j
public class UserController {
    private final UserService userService;

    /**
     * 注册普通用户账号。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        log.info("收到用户注册请求, username={}", dto.getUsername());
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录并返回 JWT。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        log.info("收到用户登录请求, username={}", dto.getUsername());
        return Result.success("登录成功", userService.login(dto));
    }

    /**
     * 获取当前登录用户的个人资料。
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        log.info("查询当前用户信息, userId={}", userId);
        return Result.success(userService.getCurrentUserInfo(userId));
    }

    /**
     * 更新当前用户的昵称、头像、邮箱和简介。
     */
    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("修改个人资料, userId={}", userId);
        userService.updateProfile(userId, dto);
        return Result.success("修改成功", null);
    }

    /**
     * 修改当前用户密码。
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UserPasswordUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("修改密码, userId={}", userId);
        userService.updatePassword(userId, dto);
        return Result.success("修改成功", null);
    }
}
