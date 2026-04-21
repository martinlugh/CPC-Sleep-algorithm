-- 睡眠原始会话表：记录每次分析请求的基本信息
CREATE TABLE IF NOT EXISTS sleep_raw_session (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    user_id VARCHAR(64) NOT NULL COMMENT '用户唯一标识',
    analysis_date DATE NOT NULL COMMENT '分析日期（一天一条，与source_type联合唯一）',
    source_type VARCHAR(32) NOT NULL COMMENT '数据来源类型：WATCH/BAND/MANUAL_IMPORT',
    model_version VARCHAR(64) NOT NULL COMMENT '使用的算法模型版本号',
    rule_version VARCHAR(64) NOT NULL COMMENT '使用的规则版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    UNIQUE KEY uk_user_date_source (user_id, analysis_date, source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠分析原始会话表';

-- 生理信号原始片段表：存储每个5分钟窗口的心率及RRI原始数据
CREATE TABLE IF NOT EXISTS sleep_raw_physiological_segment (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    session_id BIGINT NOT NULL COMMENT '关联的会话ID',
    segment_start_time DATETIME NOT NULL COMMENT '该生理片段的起始时间（5分钟窗口）',
    rri_json JSON NOT NULL COMMENT 'RRI数组（毫秒），JSON格式，如 [800,810,795,...]',
    average_heart_rate_bpm DECIMAL(10, 4) NOT NULL COMMENT '该片段的平均心率（次/分钟）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    KEY idx_session_segment_time (session_id, segment_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠生理信号原始片段表（5分钟窗口）';

-- 运动信号原始片段表：存储每个8分钟窗口的步数数据
CREATE TABLE IF NOT EXISTS sleep_raw_motion_segment (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    session_id BIGINT NOT NULL COMMENT '关联的会话ID',
    motion_segment_start_time DATETIME NOT NULL COMMENT '该运动片段的起始时间（8分钟窗口）',
    steps_in_eight_minutes INT NOT NULL COMMENT '8分钟内的总步数，用于运动强度对齐判断',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    KEY idx_session_motion_time (session_id, motion_segment_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠运动信号原始片段表（8分钟窗口）';

-- 睡眠分析结果汇总表：存储主睡眠及全天的综合分析结论
CREATE TABLE IF NOT EXISTS sleep_analysis_result (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    session_id BIGINT NOT NULL COMMENT '关联的会话ID',
    main_sleep_start_time DATETIME COMMENT '主睡眠入睡时间（由入睡检测引擎识别）',
    main_sleep_wake_up_time DATETIME COMMENT '主睡眠起床时间（由起床检测引擎识别）',
    main_sleep_latency_minutes INT COMMENT '入睡潜伏期（分钟），从躺下到入睡的时间',
    main_sleep_total_minutes INT COMMENT '主睡眠有效时长（分钟），不含清醒时间',
    main_sleep_deep_minutes INT COMMENT '深睡阶段时长（分钟）',
    main_sleep_light_minutes INT COMMENT '浅睡阶段时长（分钟）',
    main_sleep_rem_minutes INT COMMENT 'REM快速眼动睡眠时长（分钟）',
    main_sleep_awake_minutes INT COMMENT '睡眠期间清醒时长（分钟）',
    main_sleep_quality_score DECIMAL(10, 4) COMMENT '主睡眠质量综合得分（0~100）',
    nightly_recovery_score DECIMAL(10, 4) COMMENT '夜间恢复指数得分（0~100），反映自主神经恢复水平',
    nightly_fatigue_score DECIMAL(10, 4) COMMENT '夜间疲劳指数得分（0~100），与恢复指数互补',
    data_quality_score DECIMAL(10, 4) COMMENT '数据质量综合得分（0~100），反映原始信号可信度',
    main_sleep_efficiency DECIMAL(10, 4) COMMENT '睡眠效率（0.0~1.0），有效睡眠时长/总卧床时长',
    main_sleep_awaken_count INT COMMENT '睡眠中途清醒次数（不含入睡前和起床后）',
    daily_total_sleep_minutes INT COMMENT '全天总睡眠时长（分钟），含主睡眠和所有午睡',
    daytime_nap_total_minutes INT COMMENT '白天午睡总时长（分钟）',
    daytime_nap_count INT COMMENT '白天午睡次数',
    score_explanation_json JSON COMMENT '得分明细说明，JSON格式，含各子项权重及计算依据',
    sleep_onset_reason_json JSON COMMENT '入睡识别原因，含tags（标签列表）和text（文字说明）',
    wake_up_reason_json JSON COMMENT '起床识别原因，含tags（标签列表）和text（文字说明）',
    daytime_nap_summary_json JSON COMMENT '午睡汇总信息，含次数、总时长及各段时间轴',
    alignment_explanation_json JSON COMMENT '运动对齐策略说明，含低置信度片段占比及采用策略',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠分析结果汇总表';

-- 片段级分析结果表：存储每个5分钟窗口的详细特征与分期结论
CREATE TABLE IF NOT EXISTS sleep_segment_result (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    session_id BIGINT NOT NULL COMMENT '关联的会话ID',
    segment_start_time DATETIME NOT NULL COMMENT '该片段的起始时间',
    aligned_steps_in_five_minutes DECIMAL(10, 4) COMMENT '对齐到5分钟窗口的等效步数（由8分钟运动窗口插值得出）',
    motion_alignment_confidence DECIMAL(10, 4) COMMENT '运动对齐置信度（0.0~1.0），反映运动与生理窗口匹配质量',
    raw_rri_count INT COMMENT 'RRI原始数据点数量',
    cleaned_rri_count INT COMMENT 'RRI清洗后有效数据点数量',
    average_heart_rate_bpm DECIMAL(10, 4) COMMENT '该片段平均心率（次/分钟）',
    hfc_power DECIMAL(16, 6) COMMENT 'CPC高频耦合功率（HFC），对应深睡特征',
    lfc_power DECIMAL(16, 6) COMMENT 'CPC低频耦合功率（LFC），对应浅睡/清醒特征',
    vlfc_power DECIMAL(16, 6) COMMENT 'CPC超低频耦合功率（VLFC）',
    hfc_lfc_ratio DECIMAL(16, 6) COMMENT 'HFC/LFC功率比值，比值越高越倾向深睡',
    sd1_ms DECIMAL(16, 6) COMMENT 'Poincaré图SD1（毫秒），反映短期HRV，REM期较高',
    sd2_ms DECIMAL(16, 6) COMMENT 'Poincaré图SD2（毫秒），反映长期HRV',
    sd2_sd1_ratio DECIMAL(16, 6) COMMENT 'SD2/SD1比值，比值越高越倾向REM睡眠',
    sample_entropy DECIMAL(16, 6) COMMENT '样本熵，反映RRI信号复杂度，深睡时偏低，REM时偏高',
    stage_before_calibration VARCHAR(32) COMMENT '校准前初始分期结果：WAKE/LIGHT/DEEP/REM/UNKNOWN',
    stage_after_calibration VARCHAR(32) COMMENT '校准后分期结果（经迟滞校准处理）',
    smoothed_stage VARCHAR(32) COMMENT '平滑后最终分期结果（经时间一致性平滑处理）',
    confidence_score DECIMAL(10, 4) COMMENT '分期置信度（0.0~1.0），综合生理和运动证据计算',
    quality_passed TINYINT(1) COMMENT '质量门控是否通过：1=通过，0=未通过',
    quality_remark VARCHAR(255) COMMENT '质量门控详情说明（含RRI数量、缺失率、心率可用性等）',
    explain_tags_json JSON COMMENT '分期可解释性标签列表，JSON格式，如["DEEP_HFC_DOMINANT","LOW_MOTION"]',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    KEY idx_session_segment (session_id, segment_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠片段级分析结果表（5分钟窗口）';

-- 用户睡眠基线配置表：存储个性化基线参数，用于分期和入睡判定
CREATE TABLE IF NOT EXISTS sleep_user_baseline (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    user_id VARCHAR(64) NOT NULL COMMENT '用户唯一标识',
    resting_heart_rate_bpm DECIMAL(10, 4) COMMENT '静息心率基线（次/分钟），用于清醒/睡眠判定阈值',
    typical_sleep_start_time TIME COMMENT '惯常入睡时间，用于辅助入睡窗口判断',
    typical_wake_time TIME COMMENT '惯常起床时间，用于辅助起床窗口判断',
    baseline_hrv_sdnn_ms DECIMAL(10, 4) COMMENT '基线HRV（SDNN，毫秒），用于恢复指数计算参照',
    baseline_sleep_latency_minutes INT COMMENT '历史平均入睡潜伏期（分钟）',
    baseline_sleep_efficiency DECIMAL(10, 4) COMMENT '历史平均睡眠效率（0.0~1.0）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户睡眠基线配置表';

-- 算法配置表：支持运行时动态调整算法阈值参数
CREATE TABLE IF NOT EXISTS sleep_algorithm_config (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    config_key VARCHAR(128) NOT NULL COMMENT '配置项键名，对应SleepAnalysisProperties中的字段名',
    config_value VARCHAR(512) NOT NULL COMMENT '配置项值（字符串形式存储，读取时按类型转换）',
    config_desc VARCHAR(255) COMMENT '配置项说明，描述该参数的作用和合理取值范围',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用，0=禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠算法动态配置表';

-- 睡眠分析重放任务表：记录重放任务的执行状态和对比结果
CREATE TABLE IF NOT EXISTS sleep_analysis_replay_task (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    session_id BIGINT NOT NULL COMMENT '关联的原始会话ID',
    replay_status VARCHAR(32) NOT NULL COMMENT '重放状态：RUNNING/SUCCESS/FAILED',
    request_payload_json JSON COMMENT '重放请求参数，含forceReplay等控制标志',
    result_payload_json JSON COMMENT '重放对比结果，含新旧分析结论的差异指标',
    error_message VARCHAR(512) COMMENT '重放失败时的错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    KEY idx_session_status (session_id, replay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠分析重放任务表';

-- 白天午睡结果表：存储识别出的每段午睡的时间和阶段信息
CREATE TABLE IF NOT EXISTS daytime_nap_result (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    session_id BIGINT NOT NULL COMMENT '关联的会话ID',
    nap_start_time DATETIME NOT NULL COMMENT '午睡开始时间',
    nap_end_time DATETIME NOT NULL COMMENT '午睡结束时间',
    nap_total_minutes INT NOT NULL COMMENT '午睡总时长（分钟）',
    nap_stage_summary_json JSON COMMENT '午睡阶段时间轴，JSON格式，含每个5分钟片段的分期结果',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    KEY idx_session_nap_time (session_id, nap_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='白天午睡识别结果表';
