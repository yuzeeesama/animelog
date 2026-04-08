package com.animelog.animelogserver.mapper;

import com.animelog.animelogserver.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int insert(User user);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);
}
