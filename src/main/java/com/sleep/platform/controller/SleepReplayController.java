package com.sleep.platform.controller;

import com.sleep.platform.common.ApiResponse;
import com.sleep.platform.domain.request.SleepReplayRequest;
import com.sleep.platform.domain.response.SleepReplayResponse;
import com.sleep.platform.service.SleepReplayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sleep")
public class SleepReplayController {

    private final SleepReplayService sleepReplayService;

    public SleepReplayController(SleepReplayService sleepReplayService) {
        this.sleepReplayService = sleepReplayService;
    }

    @PostMapping("/replay")
    public ApiResponse<SleepReplayResponse> replay(@Valid @RequestBody SleepReplayRequest request) {
        return ApiResponse.success(sleepReplayService.createReplayTask(request));
    }
}
