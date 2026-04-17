package com.sleep.platform.domain.response;

import lombok.Data;

@Data
public class SuperpowerCatalogItemResponse {
    private String superpowerKey;
    private String name;
    private String description;
    private String category;
    private String tier;
    private String version;
    private boolean installed;
}
