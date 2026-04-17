package com.example.sleepanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 睡眠分析系统启动类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SleepAnalysisApplication {

    /**
     * 应用程序入口方法。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SleepAnalysisApplication.class, args);
    }
}
