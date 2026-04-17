package com.sleep.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.entity.SleepAlgorithmConfigEntity;
import com.sleep.platform.domain.request.SleepConfigUpdateRequest;
import com.sleep.platform.domain.response.SleepConfigResponse;
import com.sleep.platform.mapper.SleepAlgorithmConfigMapper;
import com.sleep.platform.service.SleepConfigService;
import org.springframework.stereotype.Service;

@Service
public class SleepConfigServiceImpl implements SleepConfigService {

    private final SleepAnalysisProperties sleepAnalysisProperties;
    private final SleepAlgorithmConfigMapper sleepAlgorithmConfigMapper;

    public SleepConfigServiceImpl(SleepAnalysisProperties sleepAnalysisProperties,
                                  SleepAlgorithmConfigMapper sleepAlgorithmConfigMapper) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
        this.sleepAlgorithmConfigMapper = sleepAlgorithmConfigMapper;
    }

    @Override
    public SleepConfigResponse getCurrentConfig() {
        return toResponse(sleepAnalysisProperties);
    }

    @Override
    public SleepConfigResponse updateConfig(SleepConfigUpdateRequest request) {
        applyPropertyValue(request.getConfigKey(), request.getConfigValue());

        SleepAlgorithmConfigEntity entity = sleepAlgorithmConfigMapper.selectOne(
                new LambdaQueryWrapper<SleepAlgorithmConfigEntity>()
                        .eq(SleepAlgorithmConfigEntity::getConfigKey, request.getConfigKey())
                        .last("limit 1"));
        if (entity == null) {
            entity = new SleepAlgorithmConfigEntity();
            entity.setConfigKey(request.getConfigKey());
            entity.setConfigValue(request.getConfigValue());
            entity.setConfigDesc(request.getConfigDesc());
            entity.setEnabled(request.getEnabled());
            sleepAlgorithmConfigMapper.insert(entity);
        } else {
            entity.setConfigValue(request.getConfigValue());
            entity.setConfigDesc(request.getConfigDesc());
            entity.setEnabled(request.getEnabled());
            sleepAlgorithmConfigMapper.updateById(entity);
        }
        return toResponse(sleepAnalysisProperties);
    }

    private void applyPropertyValue(String key, String value) {
        switch (key) {
            case "currentModelVersion" -> sleepAnalysisProperties.setCurrentModelVersion(value);
            case "currentRuleVersion" -> sleepAnalysisProperties.setCurrentRuleVersion(value);
            case "minValidRriCount" -> sleepAnalysisProperties.setMinValidRriCount(Integer.parseInt(value));
            case "maxAllowedMissingRate" -> sleepAnalysisProperties.setMaxAllowedMissingRate(Double.parseDouble(value));
            case "wakeStepThreshold" -> sleepAnalysisProperties.setWakeStepThreshold(Integer.parseInt(value));
            case "sleepOnsetMaxAlignedStepThreshold" -> sleepAnalysisProperties.setSleepOnsetMaxAlignedStepThreshold(Integer.parseInt(value));
            case "wakeUpAlignedStepThreshold" -> sleepAnalysisProperties.setWakeUpAlignedStepThreshold(Integer.parseInt(value));
            case "deepHfcLfcRatioThreshold" -> sleepAnalysisProperties.setDeepHfcLfcRatioThreshold(Double.parseDouble(value));
            case "remSd2Sd1RatioThreshold" -> sleepAnalysisProperties.setRemSd2Sd1RatioThreshold(Double.parseDouble(value));
            case "deepSampleEntropyUpperThreshold" -> sleepAnalysisProperties.setDeepSampleEntropyUpperThreshold(Double.parseDouble(value));
            case "remSampleEntropyLowerThreshold" -> sleepAnalysisProperties.setRemSampleEntropyLowerThreshold(Double.parseDouble(value));
            case "hysteresisRequiredSegments" -> sleepAnalysisProperties.setHysteresisRequiredSegments(Integer.parseInt(value));
            case "sleepOnsetRequiredSegments" -> sleepAnalysisProperties.setSleepOnsetRequiredSegments(Integer.parseInt(value));
            case "wakeUpRequiredSegments" -> sleepAnalysisProperties.setWakeUpRequiredSegments(Integer.parseInt(value));
            case "sleepOnsetHeartRateDeltaThreshold" -> sleepAnalysisProperties.setSleepOnsetHeartRateDeltaThreshold(Double.parseDouble(value));
            case "wakeUpHeartRateDeltaThreshold" -> sleepAnalysisProperties.setWakeUpHeartRateDeltaThreshold(Double.parseDouble(value));
            case "minStableSleepMinutesForOnset" -> sleepAnalysisProperties.setMinStableSleepMinutesForOnset(Integer.parseInt(value));
            case "minStableWakeMinutesForWakeUp" -> sleepAnalysisProperties.setMinStableWakeMinutesForWakeUp(Integer.parseInt(value));
            case "daytimeNapMinMinutes" -> sleepAnalysisProperties.setDaytimeNapMinMinutes(Integer.parseInt(value));
            case "daytimeNapMaxAlignedStepThreshold" -> sleepAnalysisProperties.setDaytimeNapMaxAlignedStepThreshold(Integer.parseInt(value));
            case "daytimeNapRequiredSegments" -> sleepAnalysisProperties.setDaytimeNapRequiredSegments(Integer.parseInt(value));
            case "daytimeNapMergeGapMinutes" -> sleepAnalysisProperties.setDaytimeNapMergeGapMinutes(Integer.parseInt(value));
            case "motionAlignmentLookbackMinutes" -> sleepAnalysisProperties.setMotionAlignmentLookbackMinutes(Integer.parseInt(value));
            case "motionAlignmentLookaheadMinutes" -> sleepAnalysisProperties.setMotionAlignmentLookaheadMinutes(Integer.parseInt(value));
            case "lowAlignmentConfidenceMotionWeight" -> sleepAnalysisProperties.setLowAlignmentConfidenceMotionWeight(Double.parseDouble(value));
            default -> {
            }
        }
    }

    private SleepConfigResponse toResponse(SleepAnalysisProperties properties) {
        SleepConfigResponse response = new SleepConfigResponse();
        response.setCurrentModelVersion(properties.getCurrentModelVersion());
        response.setCurrentRuleVersion(properties.getCurrentRuleVersion());
        response.setMinValidRriCount(properties.getMinValidRriCount());
        response.setMaxAllowedMissingRate(properties.getMaxAllowedMissingRate());
        response.setWakeStepThreshold(properties.getWakeStepThreshold());
        response.setSleepOnsetMaxAlignedStepThreshold(properties.getSleepOnsetMaxAlignedStepThreshold());
        response.setWakeUpAlignedStepThreshold(properties.getWakeUpAlignedStepThreshold());
        response.setDeepHfcLfcRatioThreshold(properties.getDeepHfcLfcRatioThreshold());
        response.setRemSd2Sd1RatioThreshold(properties.getRemSd2Sd1RatioThreshold());
        response.setDeepSampleEntropyUpperThreshold(properties.getDeepSampleEntropyUpperThreshold());
        response.setRemSampleEntropyLowerThreshold(properties.getRemSampleEntropyLowerThreshold());
        response.setHysteresisRequiredSegments(properties.getHysteresisRequiredSegments());
        response.setSleepOnsetRequiredSegments(properties.getSleepOnsetRequiredSegments());
        response.setWakeUpRequiredSegments(properties.getWakeUpRequiredSegments());
        response.setSleepOnsetHeartRateDeltaThreshold(properties.getSleepOnsetHeartRateDeltaThreshold());
        response.setWakeUpHeartRateDeltaThreshold(properties.getWakeUpHeartRateDeltaThreshold());
        response.setMinStableSleepMinutesForOnset(properties.getMinStableSleepMinutesForOnset());
        response.setMinStableWakeMinutesForWakeUp(properties.getMinStableWakeMinutesForWakeUp());
        response.setDaytimeNapMinMinutes(properties.getDaytimeNapMinMinutes());
        response.setDaytimeNapMaxAlignedStepThreshold(properties.getDaytimeNapMaxAlignedStepThreshold());
        response.setDaytimeNapRequiredSegments(properties.getDaytimeNapRequiredSegments());
        response.setDaytimeNapMergeGapMinutes(properties.getDaytimeNapMergeGapMinutes());
        response.setMotionAlignmentLookbackMinutes(properties.getMotionAlignmentLookbackMinutes());
        response.setMotionAlignmentLookaheadMinutes(properties.getMotionAlignmentLookaheadMinutes());
        response.setLowAlignmentConfidenceMotionWeight(properties.getLowAlignmentConfidenceMotionWeight());
        return response;
    }
}
