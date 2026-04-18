package com.hrv;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.hrv.executive.Shichen;
import com.hrv.executive.ShichenBaselineAnalyzer;
import com.hrv.executive.ShichenHealthJudgement;

/**
 * 可执行示例入口：
 * 演示“全天12个时辰 + 3天基线”健康判断。
 */
public class App {
    public static void main(String[] args) {
        Map<Shichen, List<double[]>> dayMinus2 = buildMockDay(1.00);
        Map<Shichen, List<double[]>> dayMinus1 = buildMockDay(1.02);
        Map<Shichen, List<double[]>> dayMinus0 = buildMockDay(0.98);
        Map<Shichen, List<double[]>> today = buildMockDay(1.05);

        List<Map<Shichen, List<double[]>>> baseline3Days = List.of(dayMinus2, dayMinus1, dayMinus0);

        Map<Shichen, ShichenHealthJudgement> report = ShichenBaselineAnalyzer.evaluateAllDay(today, baseline3Days);

        System.out.println("=== 全天12时辰健康判断（3天基线）===");
        for (Shichen s : Shichen.values()) {
            ShichenHealthJudgement r = report.get(s);
            System.out.printf("%s %s | 组数=%d | 颜色=%s | 提示=%s%n",
                    s.cnName(), s.range(), r.dataGroupCount(), r.promptColor(), r.message());
        }
    }

    private static Map<Shichen, List<double[]>> buildMockDay(double scale) {
        Map<Shichen, List<double[]>> day = new EnumMap<>(Shichen.class);
        for (Shichen s : Shichen.values()) {
            List<double[]> groups = new ArrayList<>();
            // 每个时辰至少 6 组（满足阈值）
            for (int i = 0; i < 6; i++) {
                groups.add(new double[]{
                        550 * scale, 558 * scale, 551 * scale, 553 * scale, 595 * scale, 585 * scale,
                        605 * scale, 657 * scale, 616 * scale, 645 * scale, 640 * scale, 632 * scale
                });
            }
            day.put(s, groups);
        }
        return day;
    }
}
