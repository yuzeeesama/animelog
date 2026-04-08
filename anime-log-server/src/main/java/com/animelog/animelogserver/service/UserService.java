package com.animelog.animelogserver.service;

import com.animelog.animelogserver.dto.UserLoginDTO;
import com.animelog.animelogserver.dto.UserPasswordUpdateDTO;
import com.animelog.animelogserver.dto.UserProfileUpdateDTO;
import com.animelog.animelogserver.dto.UserRegisterDTO;
import com.animelog.animelogserver.vo.LoginVO;
import com.animelog.animelogserver.vo.UserInfoVO;

public interface UserService {
    void register(UserRegisterDTO dto);

    LoginVO login(UserLoginDTO dto);

    UserInfoVO getCurrentUserInfo(Long userId);

    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    void updatePassword(Long userId, UserPasswordUpdateDTO dto);
}
