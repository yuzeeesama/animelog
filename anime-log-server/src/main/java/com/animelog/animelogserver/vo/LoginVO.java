package com.animelog.animelogserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录返回结果")
public class LoginVO {
    @Schema(description = "JWT 令牌")
    private String token;
    @Schema(description = "当前登录用户信息")
    private UserInfoVO userInfo;
}
