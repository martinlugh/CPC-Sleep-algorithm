package com.hrv.analysis;

import java.util.List;

/**
 * 洛伦兹散点图结果。
 * - points：散点坐标
 * - sd1/sd2：与 Classical.nonLinear 一致的非线性指标
 */
public record LorenzResult(List<LorenzPoint> points, double sd1, double sd2) {
}
