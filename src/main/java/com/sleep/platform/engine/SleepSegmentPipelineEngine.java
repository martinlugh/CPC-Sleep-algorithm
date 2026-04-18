package com.sleep.platform.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.domain.entity.SleepSegmentResultEntity;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.CpcAnalysisResult;
import com.sleep.platform.domain.model.MotionAlignmentResult;
import com.sleep.platform.domain.model.QualityGateResult;
import com.sleep.platform.domain.model.SleepCalibrationResult;
import com.sleep.platform.domain.model.SleepDecisionResult;
import com.sleep.platform.domain.model.SleepExplainabilityResult;
import com.sleep.platform.domain.model.UserSleepBaselineProfile;
import com.sleep.platform.domain.request.MotionSegmentInput;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import com.sleep.platform.feature.HrvStatisticsCalculator;
import com.sleep.platform.feature.PoincareAnalysis;
import com.sleep.platform.feature.SampleEntropyCalculator;
import com.sleep.platform.mapper.SleepSegmentResultMapper;
import com.sleep.platform.signal.RespiratoryExtractor;
import com.sleep.platform.signal.RriCleaner;
import com.sleep.platform.signal.SignalResampler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SleepSegmentPipelineEngine {

    private final MultiWindowAlignmentEngine multiWindowAlignmentEngine;
    private final QualityGateEngine qualityGateEngine;
    private final RriCleaner rriCleaner;
    private final SignalResampler signalResampler;
    private final RespiratoryExtractor respiratoryExtractor;
    private final HrvStatisticsCalculator hrvStatisticsCalculator;
    private final PoincareAnalysis poincareAnalysis;
    private final SampleEntropyCalculator sampleEntropyCalculator;
    private final CpcAlgorithmEngine cpcAlgorithmEngine;
    private final SleepDecisionEngine sleepDecisionEngine;
    private final SleepCalibrationEngine sleepCalibrationEngine;
    private final SleepExplainabilityEngine sleepExplainabilityEngine;
    private final SleepSegmentResultMapper sleepSegmentResultMapper;
    private final ObjectMapper objectMapper;

    public SleepSegmentPipelineEngine(MultiWindowAlignmentEngine multiWindowAlignmentEngine,
                                      QualityGateEngine qualityGateEngine,
                                      RriCleaner rriCleaner,
                                      SignalResampler signalResampler,
                                      RespiratoryExtractor respiratoryExtractor,
                                      HrvStatisticsCalculator hrvStatisticsCalculator,
                                      PoincareAnalysis poincareAnalysis,
                                      SampleEntropyCalculator sampleEntropyCalculator,
                                      CpcAlgorithmEngine cpcAlgorithmEngine,
                                      SleepDecisionEngine sleepDecisionEngine,
                                      SleepCalibrationEngine sleepCalibrationEngine,
                                      SleepExplainabilityEngine sleepExplainabilityEngine,
                                      SleepSegmentResultMapper sleepSegmentResultMapper,
                                      ObjectMapper objectMapper) {
        this.multiWindowAlignmentEngine = multiWindowAlignmentEngine;
        this.qualityGateEngine = qualityGateEngine;
        this.rriCleaner = rriCleaner;
        this.signalResampler = signalResampler;
        this.respiratoryExtractor = respiratoryExtractor;
        this.hrvStatisticsCalculator = hrvStatisticsCalculator;
        this.poincareAnalysis = poincareAnalysis;
        this.sampleEntropyCalculator = sampleEntropyCalculator;
        this.cpcAlgorithmEngine = cpcAlgorithmEngine;
        this.sleepDecisionEngine = sleepDecisionEngine;
        this.sleepCalibrationEngine = sleepCalibrationEngine;
        this.sleepExplainabilityEngine = sleepExplainabilityEngine;
        this.sleepSegmentResultMapper = sleepSegmentResultMapper;
        this.objectMapper = objectMapper;
    }

    public SleepSegmentAnalysisResult analyzeSingleSegment(Long sessionId,
                                                           PhysiologicalSegmentInput physiologicalSegmentInput,
                                                           List<MotionSegmentInput> motionSegmentList,
                                                           UserSleepBaselineProfile baselineProfile,
                                                           SleepStage previousSmoothedStage) {
        MotionAlignmentResult alignmentResult = multiWindowAlignmentEngine.alignSingleSegment(physiologicalSegmentInput, motionSegmentList);
        QualityGateResult qualityGateResult = qualityGateEngine.evaluate(physiologicalSegmentInput, alignmentResult);

        RriCleaner.CleaningResult cleaningResult = rriCleaner.clean(physiologicalSegmentInput.getRriMsList());
        double[] resampled = signalResampler.resampleTo4Hz(cleaningResult.getRriMsArray());
        double[] respiration = respiratoryExtractor.extractDerivedRespiration(resampled, 4.0);

        HrvStatisticsCalculator.HrvStatistics hrvStatistics = hrvStatisticsCalculator.calculate(cleaningResult.getRriMsArray());
        PoincareAnalysis.PoincareResult poincareResult = poincareAnalysis.analyze(cleaningResult.getRriMsArray());
        double sampleEntropy = sampleEntropyCalculator.calculate(resampled);
        CpcAnalysisResult cpcAnalysisResult = cpcAlgorithmEngine.analyze(resampled, respiration);

        double baselineHr = baselineProfile == null || baselineProfile.getRestingHeartRateBpm() == null
                ? physiologicalSegmentInput.getAverageHeartRateBpm() : baselineProfile.getRestingHeartRateBpm();

        SleepDecisionResult decisionResult = sleepDecisionEngine.decideInitialStage(
                qualityGateResult,
                cpcAnalysisResult,
                physiologicalSegmentInput.getAverageHeartRateBpm(),
                sampleEntropy,
                poincareResult.getSd2Sd1Ratio(),
                alignmentResult.getAlignedStepsInFiveMinutes(),
                baselineHr
        );

        SleepCalibrationResult calibrationResult = sleepCalibrationEngine.calibrate(
                decisionResult.getStageBeforeCalibration(),
                decisionResult.getConfidenceScore(),
                physiologicalSegmentInput.getAverageHeartRateBpm(),
                baselineHr,
                sampleEntropy,
                poincareResult.getSd2Sd1Ratio(),
                alignmentResult.getAlignedStepsInFiveMinutes(),
                qualityGateResult,
                previousSmoothedStage
        );

        SleepExplainabilityResult explainabilityResult = sleepExplainabilityEngine.build(
                calibrationResult.getSmoothedStage(),
                physiologicalSegmentInput.getAverageHeartRateBpm(),
                sampleEntropy,
                poincareResult.getSd2Sd1Ratio(),
                cpcAnalysisResult,
                qualityGateResult,
                alignmentResult.getAlignedStepsInFiveMinutes()
        );

        SleepSegmentAnalysisResult response = new SleepSegmentAnalysisResult();
        response.setSegmentStartTime(physiologicalSegmentInput.getSegmentStartTime());
        response.setRawRriCount(cleaningResult.getRawCount());
        response.setCleanedRriCount(cleaningResult.getCleanedCount());
        response.setHfcPower(cpcAnalysisResult.getHfcPower());
        response.setLfcPower(cpcAnalysisResult.getLfcPower());
        response.setVlfcPower(cpcAnalysisResult.getVlfcPower());
        response.setHfcLfcRatio(cpcAnalysisResult.getHfcLfcRatio());
        response.setSd1Ms(poincareResult.getSd1Ms());
        response.setSd2Ms(poincareResult.getSd2Ms());
        response.setSd2Sd1Ratio(poincareResult.getSd2Sd1Ratio());
        response.setSampleEntropy(sampleEntropy);
        response.setStageBeforeCalibration(decisionResult.getStageBeforeCalibration());
        response.setStageAfterCalibration(calibrationResult.getStageAfterCalibration());
        response.setSmoothedStage(calibrationResult.getSmoothedStage());
        response.setConfidenceScore(calibrationResult.getConfidenceScore());
        response.setQualityPassed(qualityGateResult.isQualityPassed());
        response.setQualityRemark(qualityGateResult.getQualityRemark() + "；" + explainabilityResult.getExplainText());

        persistResult(sessionId, physiologicalSegmentInput, alignmentResult, response, explainabilityResult);
        return response;
    }

    private void persistResult(Long sessionId,
                               PhysiologicalSegmentInput physiologicalSegmentInput,
                               MotionAlignmentResult alignmentResult,
                               SleepSegmentAnalysisResult response,
                               SleepExplainabilityResult explainabilityResult) {
        SleepSegmentResultEntity entity = new SleepSegmentResultEntity();
        entity.setSessionId(sessionId);
        entity.setSegmentStartTime(physiologicalSegmentInput.getSegmentStartTime());
        entity.setAlignedStepsInFiveMinutes(BigDecimal.valueOf(alignmentResult.getAlignedStepsInFiveMinutes()));
        entity.setMotionAlignmentConfidence(BigDecimal.valueOf(alignmentResult.getMotionAlignmentConfidence()));
        entity.setRawRriCount(response.getRawRriCount());
        entity.setCleanedRriCount(response.getCleanedRriCount());
        entity.setAverageHeartRateBpm(BigDecimal.valueOf(physiologicalSegmentInput.getAverageHeartRateBpm()));
        entity.setHfcPower(BigDecimal.valueOf(response.getHfcPower()));
        entity.setLfcPower(BigDecimal.valueOf(response.getLfcPower()));
        entity.setVlfcPower(BigDecimal.valueOf(response.getVlfcPower()));
        entity.setHfcLfcRatio(BigDecimal.valueOf(response.getHfcLfcRatio()));
        entity.setSd1Ms(BigDecimal.valueOf(response.getSd1Ms()));
        entity.setSd2Ms(BigDecimal.valueOf(response.getSd2Ms()));
        entity.setSd2Sd1Ratio(BigDecimal.valueOf(response.getSd2Sd1Ratio()));
        entity.setSampleEntropy(BigDecimal.valueOf(response.getSampleEntropy()));
        entity.setStageBeforeCalibration(response.getStageBeforeCalibration().name());
        entity.setStageAfterCalibration(response.getStageAfterCalibration().name());
        entity.setSmoothedStage(response.getSmoothedStage().name());
        entity.setConfidenceScore(BigDecimal.valueOf(response.getConfidenceScore()));
        entity.setQualityPassed(response.getQualityPassed());
        entity.setQualityRemark(response.getQualityRemark());
        entity.setExplainTagsJson(writeJson(explainabilityResult.getExplainTags()));
        sleepSegmentResultMapper.insert(entity);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }
}
