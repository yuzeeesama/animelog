package com.animelog.animelogserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "当前用户信息")
public class UserInfoVO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像地址")
    private String avatarUrl;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "个人简介")
    private String bio;
    @Schema(description = "角色 0普通用户 1管理员")
    private Integer role;
}
