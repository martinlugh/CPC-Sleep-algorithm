package com.sleep.platform.controller;

import com.sleep.platform.common.ApiResponse;
import com.sleep.platform.domain.request.SleepConfigUpdateRequest;
import com.sleep.platform.domain.response.SleepConfigResponse;
import com.sleep.platform.service.SleepConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sleep/config")
public class SleepConfigController {

    private final SleepConfigService sleepConfigService;

    public SleepConfigController(SleepConfigService sleepConfigService) {
        this.sleepConfigService = sleepConfigService;
    }

    @GetMapping("/current")
    public ApiResponse<SleepConfigResponse> getCurrentConfig() {
        return ApiResponse.success(sleepConfigService.getCurrentConfig());
    }

    @PostMapping("/update")
    public ApiResponse<SleepConfigResponse> update(@Valid @RequestBody SleepConfigUpdateRequest request) {
        return ApiResponse.success(sleepConfigService.updateConfig(request));
    }
}
