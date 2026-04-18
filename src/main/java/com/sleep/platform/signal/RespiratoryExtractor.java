package com.sleep.platform.signal;

import org.springframework.stereotype.Component;

@Component
public class RespiratoryExtractor {

    private static final double LOW_HZ = 0.15;
    private static final double HIGH_HZ = 0.45;

    public double[] extractDerivedRespiration(double[] resampledRriSeries, double samplingRateHz) {
        if (resampledRriSeries == null || resampledRriSeries.length == 0) {
            return new double[0];
        }
        // 本实现基于RRI中的呼吸性窦性心律不齐（RSA）特征近似提取派生呼吸信号，不等价于直接从原始PPG波形提取EDR，但适配当前后端输入结构。
        double[] highPass = singlePoleHighPass(resampledRriSeries, LOW_HZ, samplingRateHz);
        return singlePoleLowPass(highPass, HIGH_HZ, samplingRateHz);
    }

    private double[] singlePoleHighPass(double[] input, double cutoffHz, double fs) {
        double rc = 1.0 / (2 * Math.PI * cutoffHz);
        double dt = 1.0 / fs;
        double alpha = rc / (rc + dt);
        double[] out = new double[input.length];
        out[0] = input[0];
        for (int i = 1; i < input.length; i++) {
            out[i] = alpha * (out[i - 1] + input[i] - input[i - 1]);
        }
        return out;
    }

    private double[] singlePoleLowPass(double[] input, double cutoffHz, double fs) {
        double rc = 1.0 / (2 * Math.PI * cutoffHz);
        double dt = 1.0 / fs;
        double alpha = dt / (rc + dt);
        double[] out = new double[input.length];
        out[0] = input[0];
        for (int i = 1; i < input.length; i++) {
            out[i] = out[i - 1] + alpha * (input[i] - out[i - 1]);
        }
        return out;
    }
}
