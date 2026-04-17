package com.sleep.platform.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SleepConfigUpdateRequest {

    @NotBlank(message = "配置键不能为空")
    private String configKey;
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    private String configDesc;
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;
}
