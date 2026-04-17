package com.sleep.platform.feature;

import com.sleep.platform.domain.enums.SleepPeriodType;
import com.sleep.platform.domain.enums.SleepStage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Component
public class CircadianPriorAdjuster {

    public Map<SleepStage, Double> adjust(Map<SleepStage, Double> inputPrior, LocalDateTime segmentStartTime,
                                          SleepPeriodType periodType) {
        EnumMap<SleepStage, Double> adjusted = new EnumMap<>(SleepStage.class);
        for (SleepStage stage : SleepStage.values()) {
            adjusted.put(stage, inputPrior.getOrDefault(stage, 0.0));
        }

        int hour = segmentStartTime.getHour();
        if (hour >= 22 || hour < 2) {
            adjusted.put(SleepStage.DEEP, adjusted.get(SleepStage.DEEP) * 1.20);
        }
        if (hour >= 2 && hour < 7) {
            adjusted.put(SleepStage.REM, adjusted.get(SleepStage.REM) * 1.25);
        }
        if (hour >= 9 && hour < 19) {
            adjusted.put(SleepStage.REM, adjusted.get(SleepStage.REM) * 0.70);
            if (periodType == SleepPeriodType.DAYTIME_NAP) {
                adjusted.put(SleepStage.REM, adjusted.get(SleepStage.REM) * 1.10);
            }
        }
        normalize(adjusted);
        return adjusted;
    }

    private void normalize(EnumMap<SleepStage, Double> map) {
        double sum = 0.0;
        for (double value : map.values()) {
            sum += Math.max(0.0, value);
        }
        if (sum <= 0.0) {
            double equal = 1.0 / SleepStage.values().length;
            for (SleepStage stage : SleepStage.values()) {
                map.put(stage, equal);
            }
            return;
        }
        for (SleepStage stage : SleepStage.values()) {
            map.put(stage, Math.max(0.0, map.get(stage)) / sum);
        }
    }
}
