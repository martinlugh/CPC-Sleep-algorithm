package com.sleep.platform.domain.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InstalledSuperpowerResponse {
    private String superpowerKey;
    private String name;
    private String category;
    private String tier;
    private String configOverridesJson;
    private LocalDateTime installedAt;
}
