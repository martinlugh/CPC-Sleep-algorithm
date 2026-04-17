package com.sleep.platform.domain.model;

import lombok.Data;

import java.util.Map;

@Data
public class ReplayComparisonResult {

    private Long originalSessionId;
    private Long replaySessionId;
    private Map<String, Object> compareMetrics;
}
