package com.sleep.platform.controller;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.domain.request.SleepAnalysisRequest;
import com.sleep.platform.domain.request.SleepConfigUpdateRequest;
import com.sleep.platform.domain.request.SleepReplayRequest;
import com.sleep.platform.domain.response.SleepAnalysisResponse;
import com.sleep.platform.domain.response.SleepConfigResponse;
import com.sleep.platform.domain.response.SleepReplayResponse;
import com.sleep.platform.mapper.DaytimeNapResultMapper;
import com.sleep.platform.mapper.SleepAlgorithmConfigMapper;
import com.sleep.platform.mapper.SleepAnalysisReplayTaskMapper;
import com.sleep.platform.mapper.SleepAnalysisResultMapper;
import com.sleep.platform.mapper.SleepRawMotionSegmentMapper;
import com.sleep.platform.mapper.SleepRawPhysiologicalSegmentMapper;
import com.sleep.platform.mapper.SleepRawSessionMapper;
import com.sleep.platform.mapper.SleepSegmentResultMapper;
import com.sleep.platform.mapper.SleepUserBaselineMapper;
import com.sleep.platform.service.SleepAnalysisService;
import com.sleep.platform.service.SleepConfigService;
import com.sleep.platform.service.SleepReplayService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {SleepAnalysisController.class, SleepConfigController.class, SleepReplayController.class},
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, MybatisPlusAutoConfiguration.class}
)
class SleepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SleepAnalysisService sleepAnalysisService;
    @MockBean
    private SleepConfigService sleepConfigService;
    @MockBean
    private SleepReplayService sleepReplayService;

    @MockBean
    private DaytimeNapResultMapper daytimeNapResultMapper;
    @MockBean
    private SleepAlgorithmConfigMapper sleepAlgorithmConfigMapper;
    @MockBean
    private SleepAnalysisReplayTaskMapper sleepAnalysisReplayTaskMapper;
    @MockBean
    private SleepAnalysisResultMapper sleepAnalysisResultMapper;
    @MockBean
    private SleepRawMotionSegmentMapper sleepRawMotionSegmentMapper;
    @MockBean
    private SleepRawPhysiologicalSegmentMapper sleepRawPhysiologicalSegmentMapper;
    @MockBean
    private SleepRawSessionMapper sleepRawSessionMapper;
    @MockBean
    private SleepSegmentResultMapper sleepSegmentResultMapper;
    @MockBean
    private SleepUserBaselineMapper sleepUserBaselineMapper;

    @Test
    void shouldCallAnalyzeApi() throws Exception {
        Mockito.when(sleepAnalysisService.analyzeSleep(Mockito.any(SleepAnalysisRequest.class))).thenReturn(new SleepAnalysisResponse());
        String body = "{\"userId\":\"u1\",\"analysisDate\":\"2026-04-01\","
                + "\"physiologicalSegmentList\":[{\"segmentStartTime\":\"2026-04-01T22:00:00\",\"rriMsList\":[800.0,810.0],\"averageHeartRateBpm\":60.0}],"
                + "\"motionSegmentList\":[{\"motionSegmentStartTime\":\"2026-04-01T22:00:00\",\"stepsInEightMinutes\":10}],"
                + "\"baselineProfile\":{\"restingHeartRateBpm\":58.0}}";
        mockMvc.perform(post("/api/sleep/analyze").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCallConfigApis() throws Exception {
        Mockito.when(sleepConfigService.getCurrentConfig()).thenReturn(new SleepConfigResponse());
        Mockito.when(sleepConfigService.updateConfig(Mockito.any(SleepConfigUpdateRequest.class))).thenReturn(new SleepConfigResponse());
        mockMvc.perform(get("/api/sleep/config/current")).andExpect(status().isOk());

        SleepConfigUpdateRequest request = new SleepConfigUpdateRequest();
        request.setConfigKey("currentModelVersion");
        request.setConfigValue("V2");
        request.setEnabled(true);
        mockMvc.perform(post("/api/sleep/config/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCallReplayApi() throws Exception {
        Mockito.when(sleepReplayService.createReplayTask(Mockito.any(SleepReplayRequest.class))).thenReturn(new SleepReplayResponse());
        SleepReplayRequest request = new SleepReplayRequest();
        request.setSessionId(1L);
        request.setForceReplay(true);
        mockMvc.perform(post("/api/sleep/replay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
