package com.animelog.animelogserver.service.impl;

import com.animelog.animelogserver.dto.UserLoginDTO;
import com.animelog.animelogserver.dto.UserPasswordUpdateDTO;
import com.animelog.animelogserver.dto.UserProfileUpdateDTO;
import com.animelog.animelogserver.dto.UserRegisterDTO;
import com.animelog.animelogserver.entity.User;
import com.animelog.animelogserver.exception.BusinessException;
import com.animelog.animelogserver.mapper.UserMapper;
import com.animelog.animelogserver.service.UserService;
import com.animelog.animelogserver.util.JwtTokenUtil;
import com.animelog.animelogserver.vo.LoginVO;
import com.animelog.animelogserver.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            log.warn("用户注册失败, 用户名已存在, username={}", dto.getUsername());
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
        log.info("用户注册成功, userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("用户登录失败, 用户名或密码错误, username={}", dto.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户登录失败, 账号已禁用, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功, userId={}, username={}", user.getId(), user.getUsername());
        return new LoginVO(token, toUserInfo(user));
    }

    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        User user = getUserOrThrow(userId);
        return toUserInfo(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        userMapper.updateProfile(user);
        log.info("用户资料修改成功, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UserPasswordUpdateDTO dto) {
        User user = getUserOrThrow(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            log.warn("用户修改密码失败, 旧密码错误, userId={}", userId);
            throw new BusinessException("旧密码不正确");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(dto.getNewPassword()));
        log.info("用户修改密码成功, userId={}", userId);
    }

    private User getUserOrThrow(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private UserInfoVO toUserInfo(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
