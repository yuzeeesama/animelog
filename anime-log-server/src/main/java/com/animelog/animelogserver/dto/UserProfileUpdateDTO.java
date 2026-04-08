package com.animelog.animelogserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "个人资料修改参数")
public class UserProfileUpdateDTO {
    @Schema(description = "昵称", example = "小雨")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Schema(description = "头像地址", example = "https://example.com/avatar.jpg")
    @Size(max = 255, message = "头像地址长度不能超过255")
    private String avatarUrl;

    @Schema(description = "邮箱", example = "test@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Schema(description = "个人简介", example = "最喜欢治愈番")
    @Size(max = 255, message = "个人简介长度不能超过255")
    private String bio;
}
