package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("superpower_catalog")
public class SuperpowerCatalogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String superpowerKey;
    private String name;
    private String description;
    private String category;
    private String tier;
    private String version;
    private Boolean enabled;
    @TableField("config_schema_json")
    private String configSchemaJson;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
