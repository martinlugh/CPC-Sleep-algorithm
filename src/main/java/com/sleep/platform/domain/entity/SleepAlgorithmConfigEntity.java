package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sleep_algorithm_config")
public class SleepAlgorithmConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String configKey;
    private String configValue;
    private String configDesc;
    private Boolean enabled;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
