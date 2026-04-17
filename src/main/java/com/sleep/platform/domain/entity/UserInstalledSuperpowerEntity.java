package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_installed_superpower")
public class UserInstalledSuperpowerEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private String superpowerKey;
    @TableField("config_overrides_json")
    private String configOverridesJson;
    @TableField("installed_at")
    private LocalDateTime installedAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
