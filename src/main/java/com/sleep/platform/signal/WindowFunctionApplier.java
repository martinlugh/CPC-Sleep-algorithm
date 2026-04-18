package com.sleep.platform.signal;

import org.springframework.stereotype.Component;

@Component
public class WindowFunctionApplier {

    public double[] applyHannWindow(double[] data) {
        if (data == null || data.length == 0) {
            return new double[0];
        }
        int n = data.length;
        double[] output = new double[n];
        if (n == 1) {
            output[0] = data[0];
            return output;
        }
        for (int i = 0; i < n; i++) {
            double weight = 0.5 - 0.5 * Math.cos(2 * Math.PI * i / (n - 1));
            output[i] = data[i] * weight;
        }
        return output;
    }
}
