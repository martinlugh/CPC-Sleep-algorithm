package com.sleep.platform.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.domain.entity.SleepAnalysisResultEntity;
import com.sleep.platform.domain.entity.SleepRawMotionSegmentEntity;
import com.sleep.platform.domain.entity.SleepRawPhysiologicalSegmentEntity;
import com.sleep.platform.domain.entity.SleepRawSessionEntity;
import com.sleep.platform.domain.model.ReplayComparisonResult;
import com.sleep.platform.domain.model.UserSleepBaselineProfile;
import com.sleep.platform.domain.request.MotionSegmentInput;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import com.sleep.platform.domain.request.SleepAnalysisRequest;
import com.sleep.platform.domain.response.SleepAnalysisResponse;
import com.sleep.platform.mapper.SleepAnalysisResultMapper;
import com.sleep.platform.mapper.SleepRawMotionSegmentMapper;
import com.sleep.platform.mapper.SleepRawPhysiologicalSegmentMapper;
import com.sleep.platform.mapper.SleepRawSessionMapper;
import com.sleep.platform.service.SleepAnalysisService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SleepReplayEngine {

    private final SleepRawSessionMapper sleepRawSessionMapper;
    private final SleepRawPhysiologicalSegmentMapper sleepRawPhysiologicalSegmentMapper;
    private final SleepRawMotionSegmentMapper sleepRawMotionSegmentMapper;
    private final SleepAnalysisResultMapper sleepAnalysisResultMapper;
    private final SleepAnalysisService sleepAnalysisService;
    private final ObjectMapper objectMapper;

    public SleepReplayEngine(SleepRawSessionMapper sleepRawSessionMapper,
                             SleepRawPhysiologicalSegmentMapper sleepRawPhysiologicalSegmentMapper,
                             SleepRawMotionSegmentMapper sleepRawMotionSegmentMapper,
                             SleepAnalysisResultMapper sleepAnalysisResultMapper,
                             SleepAnalysisService sleepAnalysisService,
                             ObjectMapper objectMapper) {
        this.sleepRawSessionMapper = sleepRawSessionMapper;
        this.sleepRawPhysiologicalSegmentMapper = sleepRawPhysiologicalSegmentMapper;
        this.sleepRawMotionSegmentMapper = sleepRawMotionSegmentMapper;
        this.sleepAnalysisResultMapper = sleepAnalysisResultMapper;
        this.sleepAnalysisService = sleepAnalysisService;
        this.objectMapper = objectMapper;
    }

    public ReplayComparisonResult replayAndCompare(Long sessionId) {
        SleepRawSessionEntity session = sleepRawSessionMapper.selectById(sessionId);
        List<SleepRawPhysiologicalSegmentEntity> physiologicalEntities = sleepRawPhysiologicalSegmentMapper.selectList(
                new LambdaQueryWrapper<SleepRawPhysiologicalSegmentEntity>()
                        .eq(SleepRawPhysiologicalSegmentEntity::getSessionId, sessionId)
                        .orderByAsc(SleepRawPhysiologicalSegmentEntity::getSegmentStartTime));
        List<SleepRawMotionSegmentEntity> motionEntities = sleepRawMotionSegmentMapper.selectList(
                new LambdaQueryWrapper<SleepRawMotionSegmentEntity>()
                        .eq(SleepRawMotionSegmentEntity::getSessionId, sessionId)
                        .orderByAsc(SleepRawMotionSegmentEntity::getMotionSegmentStartTime));

        SleepAnalysisRequest replayRequest = new SleepAnalysisRequest();
        replayRequest.setUserId(session.getUserId());
        replayRequest.setAnalysisDate(session.getAnalysisDate());
        replayRequest.setPhysiologicalSegmentList(convertPhysiological(physiologicalEntities));
        replayRequest.setMotionSegmentList(convertMotion(motionEntities));
        replayRequest.setBaselineProfile(new UserSleepBaselineProfile());

        SleepAnalysisResponse replayResponse = sleepAnalysisService.analyzeSleep(replayRequest);
        SleepAnalysisResultEntity originalResult = sleepAnalysisResultMapper.selectOne(
                new LambdaQueryWrapper<SleepAnalysisResultEntity>()
                        .eq(SleepAnalysisResultEntity::getSessionId, sessionId)
                        .orderByDesc(SleepAnalysisResultEntity::getCreatedAt)
                        .last("limit 1"));

        ReplayComparisonResult comparisonResult = new ReplayComparisonResult();
        comparisonResult.setOriginalSessionId(sessionId);
        comparisonResult.setReplaySessionId(replayResponse.getSessionId());
        comparisonResult.setCompareMetrics(buildComparison(originalResult, replayResponse));
        return comparisonResult;
    }

    private List<PhysiologicalSegmentInput> convertPhysiological(List<SleepRawPhysiologicalSegmentEntity> entities) {
        List<PhysiologicalSegmentInput> list = new ArrayList<>();
        for (SleepRawPhysiologicalSegmentEntity entity : entities) {
            PhysiologicalSegmentInput input = new PhysiologicalSegmentInput();
            input.setSegmentStartTime(entity.getSegmentStartTime());
            input.setAverageHeartRateBpm(entity.getAverageHeartRateBpm().doubleValue());
            input.setRriMsList(parseRri(entity.getRriJson()));
            list.add(input);
        }
        return list;
    }

    private List<MotionSegmentInput> convertMotion(List<SleepRawMotionSegmentEntity> entities) {
        List<MotionSegmentInput> list = new ArrayList<>();
        for (SleepRawMotionSegmentEntity entity : entities) {
            MotionSegmentInput input = new MotionSegmentInput();
            input.setMotionSegmentStartTime(entity.getMotionSegmentStartTime());
            input.setStepsInEightMinutes(entity.getStepsInEightMinutes());
            list.add(input);
        }
        return list;
    }

    private List<Double> parseRri(String rriJson) {
        try {
            return objectMapper.readValue(rriJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (JsonProcessingException exception) {
            return new ArrayList<>();
        }
    }

    private Map<String, Object> buildComparison(SleepAnalysisResultEntity original, SleepAnalysisResponse replay) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("mainSleepTotalMinutes", diff(original == null ? null : original.getMainSleepTotalMinutes(), replay.getMainSleepTotalMinutes()));
        metrics.put("daytimeNapTotalMinutes", diff(original == null ? null : original.getDaytimeNapTotalMinutes(), replay.getDaytimeNapTotalMinutes()));
        metrics.put("dailyTotalSleepMinutes", diff(original == null ? null : original.getDailyTotalSleepMinutes(), replay.getDailyTotalSleepMinutes()));
        BigDecimal originalQuality = original == null ? null : original.getMainSleepQualityScore();
        metrics.put("mainSleepQualityScore", diff(originalQuality == null ? null : originalQuality.doubleValue(), replay.getMainSleepQualityScore()));
        metrics.put("mainSleepStartTime", pair(original == null ? null : original.getMainSleepStartTime(), replay.getMainSleepStartTime()));
        metrics.put("mainSleepWakeUpTime", pair(original == null ? null : original.getMainSleepWakeUpTime(), replay.getMainSleepWakeUpTime()));
        return metrics;
    }

    private Map<String, Object> diff(Number oldValue, Number newValue) {
        double oldDouble = oldValue == null ? 0.0 : oldValue.doubleValue();
        double newDouble = newValue == null ? 0.0 : newValue.doubleValue();
        Map<String, Object> map = new HashMap<>();
        map.put("old", oldValue);
        map.put("new", newValue);
        map.put("delta", newDouble - oldDouble);
        return map;
    }

    private Map<String, Object> pair(Object oldValue, Object newValue) {
        Map<String, Object> map = new HashMap<>();
        map.put("old", oldValue);
        map.put("new", newValue);
        return map;
    }
}
