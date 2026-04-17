package com.sleep.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.sleep.platform.mapper")
@ConfigurationPropertiesScan("com.sleep.platform.config")
public class SleepPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SleepPlatformApplication.class, args);
    }
}
