package com.sleep.platform.domain.request;

import lombok.Data;

@Data
public class SuperpowerInstallRequest {
    private String userId;
    private String superpowerKey;
    private String configOverridesJson;
}
