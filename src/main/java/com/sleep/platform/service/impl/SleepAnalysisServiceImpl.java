package com.sleep.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.entity.DaytimeNapResultEntity;
import com.sleep.platform.domain.entity.SleepAnalysisResultEntity;
import com.sleep.platform.domain.entity.SleepRawMotionSegmentEntity;
import com.sleep.platform.domain.entity.SleepRawPhysiologicalSegmentEntity;
import com.sleep.platform.domain.entity.SleepRawSessionEntity;
import com.sleep.platform.domain.enums.SourceType;
import com.sleep.platform.domain.model.DailySleepAggregationResult;
import com.sleep.platform.domain.model.MainSleepSummaryResult;
import com.sleep.platform.domain.model.SleepOnsetDetectionResult;
import com.sleep.platform.domain.model.SleepScoreResult;
import com.sleep.platform.domain.model.WakeUpDetectionResult;
import com.sleep.platform.domain.request.MotionSegmentInput;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import com.sleep.platform.domain.request.SleepAnalysisRequest;
import com.sleep.platform.domain.response.DaytimeNapResultItem;
import com.sleep.platform.domain.response.SleepAnalysisResponse;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import com.sleep.platform.domain.response.SleepStageTimelineItem;
import com.sleep.platform.engine.DailySleepAggregationEngine;
import com.sleep.platform.engine.DaytimeNapDetectionEngine;
import com.sleep.platform.engine.SleepOnsetDetectionEngine;
import com.sleep.platform.engine.SleepScoringEngine;
import com.sleep.platform.engine.SleepSegmentPipelineEngine;
import com.sleep.platform.engine.SleepStateSmoothingEngine;
import com.sleep.platform.engine.SleepSummaryEngine;
import com.sleep.platform.engine.WakeUpDetectionEngine;
import com.sleep.platform.mapper.DaytimeNapResultMapper;
import com.sleep.platform.mapper.SleepAnalysisResultMapper;
import com.sleep.platform.mapper.SleepRawMotionSegmentMapper;
import com.sleep.platform.mapper.SleepRawPhysiologicalSegmentMapper;
import com.sleep.platform.mapper.SleepRawSessionMapper;
import com.sleep.platform.service.SleepAnalysisService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SleepAnalysisServiceImpl implements SleepAnalysisService {

    private final SleepAnalysisProperties sleepAnalysisProperties;
    private final SleepRawSessionMapper sleepRawSessionMapper;
    private final SleepRawPhysiologicalSegmentMapper sleepRawPhysiologicalSegmentMapper;
    private final SleepRawMotionSegmentMapper sleepRawMotionSegmentMapper;
    private final SleepAnalysisResultMapper sleepAnalysisResultMapper;
    private final DaytimeNapResultMapper daytimeNapResultMapper;
    private final SleepSegmentPipelineEngine sleepSegmentPipelineEngine;
    private final SleepStateSmoothingEngine sleepStateSmoothingEngine;
    private final SleepOnsetDetectionEngine sleepOnsetDetectionEngine;
    private final WakeUpDetectionEngine wakeUpDetectionEngine;
    private final DaytimeNapDetectionEngine daytimeNapDetectionEngine;
    private final SleepSummaryEngine sleepSummaryEngine;
    private final DailySleepAggregationEngine dailySleepAggregationEngine;
    private final SleepScoringEngine sleepScoringEngine;
    private final ObjectMapper objectMapper;

    public SleepAnalysisServiceImpl(SleepAnalysisProperties sleepAnalysisProperties,
                                    SleepRawSessionMapper sleepRawSessionMapper,
                                    SleepRawPhysiologicalSegmentMapper sleepRawPhysiologicalSegmentMapper,
                                    SleepRawMotionSegmentMapper sleepRawMotionSegmentMapper,
                                    SleepAnalysisResultMapper sleepAnalysisResultMapper,
                                    DaytimeNapResultMapper daytimeNapResultMapper,
                                    SleepSegmentPipelineEngine sleepSegmentPipelineEngine,
                                    SleepStateSmoothingEngine sleepStateSmoothingEngine,
                                    SleepOnsetDetectionEngine sleepOnsetDetectionEngine,
                                    WakeUpDetectionEngine wakeUpDetectionEngine,
                                    DaytimeNapDetectionEngine daytimeNapDetectionEngine,
                                    SleepSummaryEngine sleepSummaryEngine,
                                    DailySleepAggregationEngine dailySleepAggregationEngine,
                                    SleepScoringEngine sleepScoringEngine,
                                    ObjectMapper objectMapper) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
        this.sleepRawSessionMapper = sleepRawSessionMapper;
        this.sleepRawPhysiologicalSegmentMapper = sleepRawPhysiologicalSegmentMapper;
        this.sleepRawMotionSegmentMapper = sleepRawMotionSegmentMapper;
        this.sleepAnalysisResultMapper = sleepAnalysisResultMapper;
        this.daytimeNapResultMapper = daytimeNapResultMapper;
        this.sleepSegmentPipelineEngine = sleepSegmentPipelineEngine;
        this.sleepStateSmoothingEngine = sleepStateSmoothingEngine;
        this.sleepOnsetDetectionEngine = sleepOnsetDetectionEngine;
        this.wakeUpDetectionEngine = wakeUpDetectionEngine;
        this.daytimeNapDetectionEngine = daytimeNapDetectionEngine;
        this.sleepSummaryEngine = sleepSummaryEngine;
        this.dailySleepAggregationEngine = dailySleepAggregationEngine;
        this.sleepScoringEngine = sleepScoringEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public SleepAnalysisResponse analyzeSleep(SleepAnalysisRequest request) {
        Long sessionId = createSession(request);
        saveRawPhysiological(sessionId, request.getPhysiologicalSegmentList());
        saveRawMotion(sessionId, request.getMotionSegmentList());

        List<SleepSegmentAnalysisResult> segmentResultList = new ArrayList<>();
        for (PhysiologicalSegmentInput physiologicalSegmentInput : request.getPhysiologicalSegmentList()) {
            SleepSegmentAnalysisResult previous = segmentResultList.isEmpty() ? null : segmentResultList.get(segmentResultList.size() - 1);
            SleepSegmentAnalysisResult segmentResult = sleepSegmentPipelineEngine.analyzeSingleSegment(
                    sessionId,
                    physiologicalSegmentInput,
                    request.getMotionSegmentList(),
                    request.getBaselineProfile(),
                    previous == null ? null : previous.getSmoothedStage()
            );
            segmentResultList.add(segmentResult);
        }

        List<SleepSegmentAnalysisResult> smoothedResultList = sleepStateSmoothingEngine.smooth(segmentResultList);
        LocalDateTime firstSegmentTime = smoothedResultList.get(0).getSegmentStartTime();
        SleepOnsetDetectionResult onsetResult = sleepOnsetDetectionEngine.detect(smoothedResultList, request.getBaselineProfile(), firstSegmentTime);

        int sleepStartIndex = findSegmentIndex(smoothedResultList, onsetResult.getMainSleepStartTime());
        WakeUpDetectionResult wakeUpResult = wakeUpDetectionEngine.detect(smoothedResultList, sleepStartIndex);
        List<DaytimeNapResultItem> napResultList = daytimeNapDetectionEngine.detect(smoothedResultList);

        MainSleepSummaryResult mainSleepSummaryResult = sleepSummaryEngine.summarize(smoothedResultList, onsetResult, wakeUpResult);
        DailySleepAggregationResult dailyAggregationResult = dailySleepAggregationEngine.aggregate(mainSleepSummaryResult, napResultList);
        SleepScoreResult scoreResult = sleepScoringEngine.score(mainSleepSummaryResult, dailyAggregationResult, smoothedResultList);

        saveAnalysisResult(sessionId, mainSleepSummaryResult, dailyAggregationResult, scoreResult,
                onsetResult, wakeUpResult, napResultList, smoothedResultList);
        saveNapResults(sessionId, napResultList);

        return buildResponse(request, sessionId, mainSleepSummaryResult, dailyAggregationResult,
                scoreResult, onsetResult, wakeUpResult, napResultList, smoothedResultList);
    }

    private Long createSession(SleepAnalysisRequest request) {
        SleepRawSessionEntity sessionEntity = new SleepRawSessionEntity();
        sessionEntity.setId(IdWorker.getId());
        sessionEntity.setUserId(request.getUserId());
        sessionEntity.setAnalysisDate(request.getAnalysisDate());
        sessionEntity.setSourceType(SourceType.WATCH.name());
        sessionEntity.setModelVersion(sleepAnalysisProperties.getCurrentModelVersion());
        sessionEntity.setRuleVersion(sleepAnalysisProperties.getCurrentRuleVersion());
        sleepRawSessionMapper.insert(sessionEntity);
        return sessionEntity.getId();
    }

    private void saveRawPhysiological(Long sessionId, List<PhysiologicalSegmentInput> physiologicalSegmentList) {
        for (PhysiologicalSegmentInput input : physiologicalSegmentList) {
            SleepRawPhysiologicalSegmentEntity entity = new SleepRawPhysiologicalSegmentEntity();
            entity.setId(IdWorker.getId());
            entity.setSessionId(sessionId);
            entity.setSegmentStartTime(input.getSegmentStartTime());
            entity.setRriJson(writeJson(input.getRriMsList()));
            entity.setAverageHeartRateBpm(BigDecimal.valueOf(input.getAverageHeartRateBpm()));
            sleepRawPhysiologicalSegmentMapper.insert(entity);
        }
    }

    private void saveRawMotion(Long sessionId, List<MotionSegmentInput> motionSegmentList) {
        for (MotionSegmentInput input : motionSegmentList) {
            SleepRawMotionSegmentEntity entity = new SleepRawMotionSegmentEntity();
            entity.setId(IdWorker.getId());
            entity.setSessionId(sessionId);
            entity.setMotionSegmentStartTime(input.getMotionSegmentStartTime());
            entity.setStepsInEightMinutes(input.getStepsInEightMinutes());
            sleepRawMotionSegmentMapper.insert(entity);
        }
    }

    private void saveAnalysisResult(Long sessionId,
                                    MainSleepSummaryResult summary,
                                    DailySleepAggregationResult daily,
                                    SleepScoreResult score,
                                    SleepOnsetDetectionResult onset,
                                    WakeUpDetectionResult wake,
                                    List<DaytimeNapResultItem> napResultList,
                                    List<SleepSegmentAnalysisResult> segmentResultList) {
        SleepAnalysisResultEntity entity = new SleepAnalysisResultEntity();
        entity.setId(IdWorker.getId());
        entity.setSessionId(sessionId);
        entity.setMainSleepStartTime(summary.getMainSleepStartTime());
        entity.setMainSleepWakeUpTime(summary.getMainSleepWakeUpTime());
        entity.setMainSleepLatencyMinutes(summary.getMainSleepLatencyMinutes());
        entity.setMainSleepTotalMinutes(summary.getMainSleepTotalMinutes());
        entity.setMainSleepDeepMinutes(summary.getMainSleepDeepMinutes());
        entity.setMainSleepLightMinutes(summary.getMainSleepLightMinutes());
        entity.setMainSleepRemMinutes(summary.getMainSleepRemMinutes());
        entity.setMainSleepAwakeMinutes(summary.getMainSleepAwakeMinutes());
        entity.setMainSleepQualityScore(BigDecimal.valueOf(score.getMainSleepQualityScore()));
        entity.setNightlyRecoveryScore(BigDecimal.valueOf(score.getNightlyRecoveryScore()));
        entity.setNightlyFatigueScore(BigDecimal.valueOf(score.getNightlyFatigueScore()));
        entity.setDataQualityScore(BigDecimal.valueOf(score.getDataQualityScore()));
        entity.setMainSleepEfficiency(BigDecimal.valueOf(summary.getMainSleepEfficiency()));
        entity.setMainSleepAwakenCount(summary.getMainSleepAwakenCount());
        entity.setDailyTotalSleepMinutes(daily.getDailyTotalSleepMinutes());
        entity.setDaytimeNapTotalMinutes(daily.getDaytimeNapTotalMinutes());
        entity.setDaytimeNapCount(daily.getDaytimeNapCount());
        entity.setScoreExplanationJson(writeJson(score.getScoreExplanation()));
        entity.setSleepOnsetReasonJson(writeJson(Map.of("tags", onset.getSleepOnsetReasonTags(), "text", onset.getSleepOnsetExplainText())));
        entity.setWakeUpReasonJson(writeJson(Map.of("tags", wake.getWakeUpReasonTags(), "text", wake.getWakeUpExplainText())));
        entity.setDaytimeNapSummaryJson(writeJson(buildNapSummary(napResultList)));
        entity.setAlignmentExplanationJson(writeJson(buildAlignmentExplanation(segmentResultList)));
        sleepAnalysisResultMapper.insert(entity);
    }

    private void saveNapResults(Long sessionId, List<DaytimeNapResultItem> napResultList) {
        for (DaytimeNapResultItem item : napResultList) {
            DaytimeNapResultEntity entity = new DaytimeNapResultEntity();
            entity.setId(IdWorker.getId());
            entity.setSessionId(sessionId);
            entity.setNapStartTime(item.getNapStartTime());
            entity.setNapEndTime(item.getNapEndTime());
            entity.setNapTotalMinutes(item.getNapTotalMinutes());
            entity.setNapStageSummaryJson(writeJson(item.getNapStageTimeline()));
            daytimeNapResultMapper.insert(entity);
        }
    }

    private SleepAnalysisResponse buildResponse(SleepAnalysisRequest request,
                                                Long sessionId,
                                                MainSleepSummaryResult summary,
                                                DailySleepAggregationResult daily,
                                                SleepScoreResult score,
                                                SleepOnsetDetectionResult onset,
                                                WakeUpDetectionResult wake,
                                                List<DaytimeNapResultItem> napResultList,
                                                List<SleepSegmentAnalysisResult> segmentResultList) {
        SleepAnalysisResponse response = new SleepAnalysisResponse();
        response.setSessionId(sessionId);
        response.setUserId(request.getUserId());
        response.setAnalysisDate(request.getAnalysisDate());
        response.setMainSleepStartTime(summary.getMainSleepStartTime());
        response.setMainSleepWakeUpTime(summary.getMainSleepWakeUpTime());
        response.setMainSleepLatencyMinutes(summary.getMainSleepLatencyMinutes());
        response.setMainSleepTotalMinutes(summary.getMainSleepTotalMinutes());
        response.setMainSleepDeepMinutes(summary.getMainSleepDeepMinutes());
        response.setMainSleepLightMinutes(summary.getMainSleepLightMinutes());
        response.setMainSleepRemMinutes(summary.getMainSleepRemMinutes());
        response.setMainSleepAwakeMinutes(summary.getMainSleepAwakeMinutes());
        response.setMainSleepQualityScore(score.getMainSleepQualityScore());
        response.setNightlyRecoveryScore(score.getNightlyRecoveryScore());
        response.setNightlyFatigueScore(score.getNightlyFatigueScore());
        response.setMainSleepAwakenCount(summary.getMainSleepAwakenCount());
        response.setMainSleepEfficiency(summary.getMainSleepEfficiency());
        response.setDailyTotalSleepMinutes(daily.getDailyTotalSleepMinutes());
        response.setDaytimeNapTotalMinutes(daily.getDaytimeNapTotalMinutes());
        response.setDaytimeNapCount(daily.getDaytimeNapCount());
        response.setDataQualityScore(score.getDataQualityScore());
        response.setDaytimeNapResultList(napResultList);
        response.setSleepSegmentResultList(segmentResultList);
        response.setSleepStageTimeline(buildTimeline(segmentResultList));
        response.setScoreExplanation(score.getScoreExplanation());
        Map<String, Object> onsetMap = new HashMap<>();
        onsetMap.put("tags", onset.getSleepOnsetReasonTags());
        onsetMap.put("text", onset.getSleepOnsetExplainText());
        response.setSleepOnsetReason(onsetMap);
        Map<String, Object> wakeMap = new HashMap<>();
        wakeMap.put("tags", wake.getWakeUpReasonTags());
        wakeMap.put("text", wake.getWakeUpExplainText());
        response.setWakeUpReason(wakeMap);
        response.setAlignmentExplanation(buildAlignmentExplanation(segmentResultList));
        return response;
    }

    private List<SleepStageTimelineItem> buildTimeline(List<SleepSegmentAnalysisResult> segmentResultList) {
        List<SleepStageTimelineItem> timeline = new ArrayList<>();
        for (SleepSegmentAnalysisResult result : segmentResultList) {
            SleepStageTimelineItem item = new SleepStageTimelineItem();
            item.setSegmentStartTime(result.getSegmentStartTime());
            item.setStage(result.getSmoothedStage());
            item.setConfidenceScore(result.getConfidenceScore());
            timeline.add(item);
        }
        return timeline;
    }

    private Map<String, Object> buildNapSummary(List<DaytimeNapResultItem> napResultList) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("daytimeNapCount", napResultList.size());
        int total = 0;
        for (DaytimeNapResultItem item : napResultList) {
            total += item.getNapTotalMinutes() == null ? 0 : item.getNapTotalMinutes();
        }
        summary.put("daytimeNapTotalMinutes", total);
        summary.put("napList", napResultList);
        return summary;
    }

    private Map<String, Object> buildAlignmentExplanation(List<SleepSegmentAnalysisResult> segmentResultList) {
        int total = segmentResultList.size();
        int lowConfidenceCount = 0;
        for (SleepSegmentAnalysisResult result : segmentResultList) {
            if (result.getQualityRemark() != null && result.getQualityRemark().contains("运动对齐置信度=0.")) {
                lowConfidenceCount++;
            }
        }
        String policy = lowConfidenceCount > total / 2
                ? "运动对齐置信度整体偏低，结果主要依赖生理证据。"
                : "运动对齐可辅助分期边界，但最终仍以生理证据为主。";
        Map<String, Object> map = new HashMap<>();
        map.put("segmentCount", total);
        map.put("lowAlignmentSegmentCount", lowConfidenceCount);
        map.put("policy", policy);
        return map;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private int findSegmentIndex(List<SleepSegmentAnalysisResult> segmentResultList, LocalDateTime time) {
        for (int i = 0; i < segmentResultList.size(); i++) {
            if (segmentResultList.get(i).getSegmentStartTime().equals(time)) {
                return i;
            }
        }
        return 0;
    }
}
