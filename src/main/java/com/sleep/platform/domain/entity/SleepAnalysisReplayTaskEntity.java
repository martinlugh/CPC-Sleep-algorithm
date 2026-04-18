package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sleep_analysis_replay_task")
public class SleepAnalysisReplayTaskEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private String replayStatus;
    private String requestPayloadJson;
    private String resultPayloadJson;
    private String errorMessage;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
