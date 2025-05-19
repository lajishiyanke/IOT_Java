package com.iot.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改密码DTO")
public class ChangePasswordDTO {
    @Schema(description = "原密码")
    private String oldPassword;
    
    @Schema(description = "新密码")
    private String newPassword;
    
    @Schema(description = "确认密码")
    private String confirmPassword;
} 