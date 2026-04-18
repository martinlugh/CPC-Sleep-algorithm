CREATE TABLE IF NOT EXISTS sleep_raw_session (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    analysis_date DATE NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    rule_version VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date_source (user_id, analysis_date, source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_raw_physiological_segment (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    segment_start_time DATETIME NOT NULL,
    rri_json JSON NOT NULL,
    average_heart_rate_bpm DECIMAL(10, 4) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_session_segment_time (session_id, segment_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_raw_motion_segment (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    motion_segment_start_time DATETIME NOT NULL,
    steps_in_eight_minutes INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_session_motion_time (session_id, motion_segment_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_analysis_result (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    main_sleep_start_time DATETIME,
    main_sleep_wake_up_time DATETIME,
    main_sleep_latency_minutes INT,
    main_sleep_total_minutes INT,
    main_sleep_deep_minutes INT,
    main_sleep_light_minutes INT,
    main_sleep_rem_minutes INT,
    main_sleep_awake_minutes INT,
    main_sleep_quality_score DECIMAL(10, 4),
    nightly_recovery_score DECIMAL(10, 4),
    nightly_fatigue_score DECIMAL(10, 4),
    data_quality_score DECIMAL(10, 4),
    main_sleep_efficiency DECIMAL(10, 4),
    main_sleep_awaken_count INT,
    daily_total_sleep_minutes INT,
    daytime_nap_total_minutes INT,
    daytime_nap_count INT,
    score_explanation_json JSON,
    sleep_onset_reason_json JSON,
    wake_up_reason_json JSON,
    daytime_nap_summary_json JSON,
    alignment_explanation_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_segment_result (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    segment_start_time DATETIME NOT NULL,
    aligned_steps_in_five_minutes DECIMAL(10, 4),
    motion_alignment_confidence DECIMAL(10, 4),
    raw_rri_count INT,
    cleaned_rri_count INT,
    average_heart_rate_bpm DECIMAL(10, 4),
    hfc_power DECIMAL(16, 6),
    lfc_power DECIMAL(16, 6),
    vlfc_power DECIMAL(16, 6),
    hfc_lfc_ratio DECIMAL(16, 6),
    sd1_ms DECIMAL(16, 6),
    sd2_ms DECIMAL(16, 6),
    sd2_sd1_ratio DECIMAL(16, 6),
    sample_entropy DECIMAL(16, 6),
    stage_before_calibration VARCHAR(32),
    stage_after_calibration VARCHAR(32),
    smoothed_stage VARCHAR(32),
    confidence_score DECIMAL(10, 4),
    quality_passed TINYINT(1),
    quality_remark VARCHAR(255),
    explain_tags_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_session_segment (session_id, segment_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_user_baseline (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    resting_heart_rate_bpm DECIMAL(10, 4),
    typical_sleep_start_time TIME,
    typical_wake_time TIME,
    baseline_hrv_sdnn_ms DECIMAL(10, 4),
    baseline_sleep_latency_minutes INT,
    baseline_sleep_efficiency DECIMAL(10, 4),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_algorithm_config (
    id BIGINT PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL,
    config_value VARCHAR(512) NOT NULL,
    config_desc VARCHAR(255),
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sleep_analysis_replay_task (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    replay_status VARCHAR(32) NOT NULL,
    request_payload_json JSON,
    result_payload_json JSON,
    error_message VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_session_status (session_id, replay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daytime_nap_result (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    nap_start_time DATETIME NOT NULL,
    nap_end_time DATETIME NOT NULL,
    nap_total_minutes INT NOT NULL,
    nap_stage_summary_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_session_nap_time (session_id, nap_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
