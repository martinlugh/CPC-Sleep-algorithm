package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sleep_raw_session")
public class SleepRawSessionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private LocalDate analysisDate;
    private String sourceType;
    private String modelVersion;
    private String ruleVersion;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
