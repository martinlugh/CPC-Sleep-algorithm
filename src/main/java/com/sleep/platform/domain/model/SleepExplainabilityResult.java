package com.sleep.platform.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class SleepExplainabilityResult {

    private List<String> explainTags;
    private String explainText;
}
