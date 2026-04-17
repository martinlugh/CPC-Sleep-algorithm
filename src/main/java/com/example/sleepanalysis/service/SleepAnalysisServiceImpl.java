package com.example.sleepanalysis.service;

import com.example.sleepanalysis.domain.request.SleepAnalysisRequest;
import com.example.sleepanalysis.domain.request.SleepSegmentInput;
import com.example.sleepanalysis.domain.request.UserSleepBaselineProfile;
import com.example.sleepanalysis.domain.response.SleepAnalysisResponse;
import com.example.sleepanalysis.domain.response.SleepSegmentAnalysisResult;
import com.example.sleepanalysis.domain.response.SleepStageTimelineItem;
import com.example.sleepanalysis.engine.CpcAlgorithmEngine;
import com.example.sleepanalysis.engine.QualityGateEngine;
import com.example.sleepanalysis.engine.SleepCalibrationEngine;
import com.example.sleepanalysis.engine.SleepDecisionEngine;
import com.example.sleepanalysis.engine.SleepStateSmoothingEngine;
import com.example.sleepanalysis.engine.SleepSummaryEngine;
import com.example.sleepanalysis.enums.SleepStage;
import com.example.sleepanalysis.feature.HrvStatisticsCalculator;
import com.example.sleepanalysis.feature.PoincareAnalysis;
import com.example.sleepanalysis.feature.SampleEntropyCalculator;
import com.example.sleepanalysis.signal.RespiratoryExtractor;
import com.example.sleepanalysis.signal.RriCleaner;
import com.example.sleepanalysis.signal.SignalResampler;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 睡眠分析服务实现。
 */
@Service
public class SleepAnalysisServiceImpl implements SleepAnalysisService {

    /** 固定重采样率。 */
    private static final double RESAMPLE_RATE_HZ = 4.0D;

    private final QualityGateEngine qualityGateEngine;
    private final RriCleaner rriCleaner;
    private final SignalResampler signalResampler;
    private final RespiratoryExtractor respiratoryExtractor;
    private final PoincareAnalysis poincareAnalysis;
    private final SampleEntropyCalculator sampleEntropyCalculator;
    private final HrvStatisticsCalculator hrvStatisticsCalculator;
    private final CpcAlgorithmEngine cpcAlgorithmEngine;
    private final SleepDecisionEngine sleepDecisionEngine;
    private final SleepCalibrationEngine sleepCalibrationEngine;
    private final SleepStateSmoothingEngine sleepStateSmoothingEngine;
    private final SleepSummaryEngine sleepSummaryEngine;

    public SleepAnalysisServiceImpl() {
        this.qualityGateEngine = new QualityGateEngine();
        this.rriCleaner = new RriCleaner();
        this.signalResampler = new SignalResampler();
        this.respiratoryExtractor = new RespiratoryExtractor();
        this.poincareAnalysis = new PoincareAnalysis();
        this.sampleEntropyCalculator = new SampleEntropyCalculator();
        this.hrvStatisticsCalculator = new HrvStatisticsCalculator();
        this.cpcAlgorithmEngine = new CpcAlgorithmEngine();
        this.sleepDecisionEngine = new SleepDecisionEngine();
        this.sleepCalibrationEngine = new SleepCalibrationEngine();
        this.sleepStateSmoothingEngine = new SleepStateSmoothingEngine();
        this.sleepSummaryEngine = new SleepSummaryEngine();
    }

    @Override
    public SleepAnalysisResponse analyzeSleep(SleepAnalysisRequest request) {
        SleepAnalysisResponse response = new SleepAnalysisResponse();
        response.setAnalysisId(request.getAnalysisId());
        response.setUserId(request.getUserId());
        response.setAnalyzedAt(OffsetDateTime.now());

        List<SleepSegmentInput> segments = request.getSleepSegmentInputs();
        if (segments == null || segments.isEmpty()) {
            response.setAnalysisStatus("EMPTY_INPUT");
            response.setSleepStageTimeline(Collections.emptyList());
            response.setSleepSegmentAnalysisResults(Collections.emptyList());
            response.setDominantSleepStage(SleepStage.UNKNOWN);
            response.setMessage("无可分析片段");
            return response;
        }

        List<SleepStageTimelineItem> rawTimeline = new ArrayList<>(segments.size());
        List<SleepSegmentAnalysisResult> segmentResults = new ArrayList<>(segments.size());
        UserSleepBaselineProfile baselineProfile = request.getUserSleepBaselineProfile();

        for (int i = 0; i < segments.size(); i++) {
            SleepSegmentInput segment = segments.get(i);
            QualityGateEngine.QualityGateResult qualityGateResult = qualityGateEngine.evaluate(segment);

            SegmentDecisionContext decisionContext;
            if (!qualityGateResult.isPassed()) {
                decisionContext = new SegmentDecisionContext(
                        qualityGateResult.getForcedStage(),
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D,
                        qualityGateResult.getMessage());
            } else {
                decisionContext = analyzeOneSegment(segment, baselineProfile);
            }

            SleepSegmentAnalysisResult segmentAnalysisResult = new SleepSegmentAnalysisResult();
            segmentAnalysisResult.setSegmentId(segment.getSegmentId());
            segmentAnalysisResult.setPredictedSleepStage(decisionContext.stage);
            segmentAnalysisResult.setStageConfidence(decisionContext.confidence);
            segmentAnalysisResult.setAverageHeartRate(decisionContext.averageHeartRate);
            segmentAnalysisResult.setAverageRespirationRate(decisionContext.averageRespirationRate);
            segmentAnalysisResult.setAverageBodyMovement(decisionContext.averageBodyMovement);
            segmentAnalysisResult.setAverageSpo2(decisionContext.averageSpo2);
            segmentAnalysisResult.setRemark(decisionContext.remark);
            segmentResults.add(segmentAnalysisResult);

            SleepStageTimelineItem timelineItem = new SleepStageTimelineItem();
            timelineItem.setSegmentId(segment.getSegmentId());
            timelineItem.setStageStartTime(segment.getSegmentStartTime());
            timelineItem.setStageEndTime(segment.getSegmentEndTime());
            timelineItem.setSleepStage(decisionContext.stage);
            timelineItem.setStageConfidence(decisionContext.confidence);
            rawTimeline.add(timelineItem);
        }

        List<SleepStageTimelineItem> smoothedTimeline = sleepStateSmoothingEngine.smooth(rawTimeline);

        // 将平滑后状态同步回分段结果。
        for (int i = 0; i < segmentResults.size() && i < smoothedTimeline.size(); i++) {
            segmentResults.get(i).setPredictedSleepStage(smoothedTimeline.get(i).getSleepStage());
        }

        response.setSleepStageTimeline(smoothedTimeline);
        response.setSleepSegmentAnalysisResults(segmentResults);

        sleepSummaryEngine.summarize(response, smoothedTimeline, segmentResults);

        if (response.getDominantSleepStage() == null) {
            response.setDominantSleepStage(SleepStage.UNKNOWN);
        }

        response.setAnalysisStatus("COMPLETED");
        return response;
    }

    /**
     * 单片段分析。
     */
    private SegmentDecisionContext analyzeOneSegment(SleepSegmentInput segment, UserSleepBaselineProfile baselineProfile) {
        double[] heartRate = toPrimitive(segment.getHeartRateSeries());
        double[] respirationRate = toPrimitive(segment.getRespirationRateSeries());
        double[] bodyMovement = toPrimitive(segment.getBodyMovementSeries());
        double[] spo2 = toPrimitive(segment.getSpo2Series());

        double[] rriMillis = heartRateToRriMillis(heartRate);
        double[] cleanedRri = rriCleaner.clean(rriMillis);
        if (cleanedRri.length < 8) {
            return new SegmentDecisionContext(
                    SleepStage.UNKNOWN,
                    0.05D,
                    average(heartRate),
                    average(respirationRate),
                    average(bodyMovement),
                    average(spo2),
                    "有效RRI长度不足");
        }

        double[] resampledRriSec = signalResampler.resampleTo4HzSeconds(cleanedRri);
        if (resampledRriSec.length < 16) {
            return new SegmentDecisionContext(
                    SleepStage.UNKNOWN,
                    0.10D,
                    average(heartRate),
                    average(respirationRate),
                    average(bodyMovement),
                    average(spo2),
                    "重采样后数据不足");
        }

        double[] respiratorySignal = respiratoryExtractor.extractRespiratorySignal(resampledRriSec, RESAMPLE_RATE_HZ);

        CpcAlgorithmEngine.CpcMetrics cpcMetrics = cpcAlgorithmEngine.analyze(respiratorySignal, RESAMPLE_RATE_HZ);
        PoincareAnalysis.PoincareMetrics poincareMetrics = poincareAnalysis.analyze(cleanedRri);
        double sampleEntropy = sampleEntropyCalculator.calculate(cleanedRri);
        HrvStatisticsCalculator.HrvStatistics hrvStatistics = hrvStatisticsCalculator.analyze(cleanedRri);

        OffsetDateTime timestamp = segment.getSegmentStartTime();
        SleepDecisionEngine.SleepDecision initialDecision = sleepDecisionEngine.decideInitialStage(
                cpcMetrics,
                poincareMetrics,
                hrvStatistics,
                sampleEntropy,
                baselineProfile,
                timestamp);

        SleepDecisionEngine.SleepDecision calibratedDecision = sleepCalibrationEngine.calibrate(
                initialDecision,
                poincareMetrics,
                sampleEntropy,
                timestamp);

        String remark = "CPC[hfc=" + format(cpcMetrics.getHfcPower())
                + ",lfc=" + format(cpcMetrics.getLfcPower())
                + ",ratio=" + format(cpcMetrics.getHfcLfcRatio())
                + "] SampEn=" + format(sampleEntropy)
                + " SD1=" + format(poincareMetrics.getSd1())
                + " SD2=" + format(poincareMetrics.getSd2());

        return new SegmentDecisionContext(
                calibratedDecision.getStage(),
                calibratedDecision.getConfidence(),
                average(heartRate),
                average(respirationRate),
                average(bodyMovement),
                average(spo2),
                remark);
    }

    /**
     * 心率转 RRI(ms)。
     */
    private double[] heartRateToRriMillis(double[] heartRateBpm) {
        if (heartRateBpm == null || heartRateBpm.length == 0) {
            return new double[0];
        }

        double[] rri = new double[heartRateBpm.length];
        int idx = 0;
        for (int i = 0; i < heartRateBpm.length; i++) {
            double hr = heartRateBpm[i];
            if (hr > 1.0D) {
                rri[idx++] = 60000.0D / hr;
            }
        }

        if (idx == rri.length) {
            return rri;
        }

        double[] compacted = new double[idx];
        System.arraycopy(rri, 0, compacted, 0, idx);
        return compacted;
    }

    /**
     * List<Double> 转 double[]，自动滤除 null/NaN/Infinite。
     */
    private double[] toPrimitive(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new double[0];
        }

        double[] temp = new double[values.size()];
        int idx = 0;
        for (int i = 0; i < values.size(); i++) {
            Double value = values.get(i);
            if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                temp[idx++] = value;
            }
        }

        double[] out = new double[idx];
        System.arraycopy(temp, 0, out, 0, idx);
        return out;
    }

    /**
     * 计算均值。
     */
    private double average(double[] values) {
        if (values == null || values.length == 0) {
            return 0.0D;
        }

        double sum = 0.0D;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum / values.length;
    }

    /**
     * 小数格式化。
     */
    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    /**
     * 片段决策上下文。
     */
    private static class SegmentDecisionContext {
        private final SleepStage stage;
        private final double confidence;
        private final double averageHeartRate;
        private final double averageRespirationRate;
        private final double averageBodyMovement;
        private final double averageSpo2;
        private final String remark;

        private SegmentDecisionContext(
                SleepStage stage,
                double confidence,
                double averageHeartRate,
                double averageRespirationRate,
                double averageBodyMovement,
                double averageSpo2,
                String remark) {
            this.stage = stage;
            this.confidence = confidence;
            this.averageHeartRate = averageHeartRate;
            this.averageRespirationRate = averageRespirationRate;
            this.averageBodyMovement = averageBodyMovement;
            this.averageSpo2 = averageSpo2;
            this.remark = remark;
        }
    }
}
