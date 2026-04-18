package com.sleep.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.domain.entity.SleepAnalysisReplayTaskEntity;
import com.sleep.platform.domain.enums.ReplayStatus;
import com.sleep.platform.domain.model.ReplayComparisonResult;
import com.sleep.platform.domain.request.SleepReplayRequest;
import com.sleep.platform.domain.response.SleepReplayResponse;
import com.sleep.platform.engine.SleepReplayEngine;
import com.sleep.platform.mapper.SleepAnalysisReplayTaskMapper;
import com.sleep.platform.service.SleepReplayService;
import org.springframework.stereotype.Service;

@Service
public class SleepReplayServiceImpl implements SleepReplayService {

    private final SleepAnalysisReplayTaskMapper sleepAnalysisReplayTaskMapper;
    private final SleepReplayEngine sleepReplayEngine;
    private final ObjectMapper objectMapper;

    public SleepReplayServiceImpl(SleepAnalysisReplayTaskMapper sleepAnalysisReplayTaskMapper,
                                  SleepReplayEngine sleepReplayEngine,
                                  ObjectMapper objectMapper) {
        this.sleepAnalysisReplayTaskMapper = sleepAnalysisReplayTaskMapper;
        this.sleepReplayEngine = sleepReplayEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public SleepReplayResponse createReplayTask(SleepReplayRequest request) {
        SleepAnalysisReplayTaskEntity entity = new SleepAnalysisReplayTaskEntity();
        entity.setId(IdWorker.getId());
        entity.setSessionId(request.getSessionId());
        entity.setReplayStatus(ReplayStatus.RUNNING.name());
        entity.setRequestPayloadJson("{\"forceReplay\":" + request.getForceReplay() + "}");
        sleepAnalysisReplayTaskMapper.insert(entity);

        SleepReplayResponse response = new SleepReplayResponse();
        response.setReplayTaskId(entity.getId());
        response.setSessionId(entity.getSessionId());

        try {
            ReplayComparisonResult replayComparisonResult = sleepReplayEngine.replayAndCompare(request.getSessionId());
            entity.setReplayStatus(ReplayStatus.SUCCESS.name());
            entity.setResultPayloadJson(writeJson(replayComparisonResult));
            sleepAnalysisReplayTaskMapper.updateById(entity);
            response.setReplayStatus(ReplayStatus.SUCCESS);
            response.setMessage("重放完成:" + writeJson(replayComparisonResult.getCompareMetrics()));
        } catch (Exception exception) {
            entity.setReplayStatus(ReplayStatus.FAILED.name());
            entity.setErrorMessage(exception.getMessage());
            sleepAnalysisReplayTaskMapper.updateById(entity);
            response.setReplayStatus(ReplayStatus.FAILED);
            response.setMessage("重放失败:" + exception.getMessage());
        }
        return response;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
