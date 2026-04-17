package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sleep_raw_motion_segment")
public class SleepRawMotionSegmentEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private LocalDateTime motionSegmentStartTime;
    private Integer stepsInEightMinutes;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
