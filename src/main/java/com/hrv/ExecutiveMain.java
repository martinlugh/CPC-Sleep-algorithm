package com.hrv;

import java.util.Arrays;

import com.hrv.executive.ExecutiveAssessment;
import com.hrv.executive.ExecutiveHealthAnalyzer;

/**
 * 直接运行示例：
 * 仅输入一组 RRi（ms），打印呼吸率、HRV、压力、情绪、疲劳、恢复、抑郁风险。
 */
public class ExecutiveMain {
    public static void main(String[] args) {
        double[] rri = {
                902, 984, 735, 486, 609, 588, 306, 916, 590, 584,
                603, 611, 610, 618, 634, 640, 639, 625, 645, 651,
                621, 620, 633, 644, 631, 628, 635, 639, 621, 622,
                643, 643, 627, 641, 638, 643, 632, 646
        };

        ExecutiveAssessment result = ExecutiveHealthAnalyzer.analyze(rri);

        System.out.println("=== ExecutiveMain Result ===");
        System.out.println("RRi(ms): " + Arrays.toString(rri));
        System.out.printf("Respiration Rate: %.2f bpm (%s)%n", result.respirationRateBpm(), result.respirationLevel());
        System.out.printf("HRV(RMSSD): %.2f ms (%s)%n", result.hrvMs(), result.hrvLevel());
        System.out.printf("Pressure: %.2f (%s)%n", result.pressureScore(), result.pressureLevel());
        System.out.printf("Emotion: %s%n", result.emotionLevel());
        System.out.printf("Fatigue: %.2f (%s)%n", result.fatigueScore(), result.fatigueLevel());
        System.out.printf("Recovery: %.2f (%s)%n", result.recoveryScore(), result.recoveryLevel());
        System.out.printf("Depression Risk: %.2f (%s)%n", result.depressionScore(), result.depressionLevel());
        System.out.println("Baseline adjusted: " + result.baselineAdjusted());
        System.out.println("Standards: " + result.standardsNote());
    }
}
