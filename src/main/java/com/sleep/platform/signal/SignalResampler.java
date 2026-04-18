package com.sleep.platform.signal;

import org.springframework.stereotype.Component;

@Component
public class SignalResampler {

    private static final double TARGET_FREQUENCY_HZ = 4.0;

    public double[] resampleTo4Hz(double[] rriMsArray) {
        if (rriMsArray == null || rriMsArray.length < 2) {
            return new double[0];
        }
        double[] timeSeconds = new double[rriMsArray.length];
        double cumulativeMs = 0.0;
        for (int i = 0; i < rriMsArray.length; i++) {
            cumulativeMs += rriMsArray[i];
            timeSeconds[i] = cumulativeMs / 1000.0;
        }
        double durationSeconds = timeSeconds[timeSeconds.length - 1];
        if (durationSeconds <= 0.5) {
            return new double[0];
        }
        int sampleCount = Math.max(2, (int) Math.floor(durationSeconds * TARGET_FREQUENCY_HZ));
        double[] output = new double[sampleCount];
        double interval = 1.0 / TARGET_FREQUENCY_HZ;
        for (int i = 0; i < sampleCount; i++) {
            double t = i * interval;
            output[i] = cubicHermiteInterpolate(timeSeconds, rriMsArray, t);
        }
        return output;
    }

    private double cubicHermiteInterpolate(double[] x, double[] y, double t) {
        if (t <= x[0]) {
            return y[0];
        }
        if (t >= x[x.length - 1]) {
            return y[y.length - 1];
        }
        int index = 0;
        while (index < x.length - 1 && x[index + 1] < t) {
            index++;
        }
        int i0 = Math.max(0, index - 1);
        int i1 = index;
        int i2 = Math.min(x.length - 1, index + 1);
        int i3 = Math.min(x.length - 1, index + 2);

        double x1 = x[i1];
        double x2 = x[i2];
        double y1 = y[i1];
        double y2 = y[i2];
        double m1 = slope(x, y, i0, i2);
        double m2 = slope(x, y, i1, i3);

        double h = x2 - x1;
        if (h == 0) {
            return y1;
        }
        double s = (t - x1) / h;
        double h00 = 2 * s * s * s - 3 * s * s + 1;
        double h10 = s * s * s - 2 * s * s + s;
        double h01 = -2 * s * s * s + 3 * s * s;
        double h11 = s * s * s - s * s;
        return h00 * y1 + h10 * h * m1 + h01 * y2 + h11 * h * m2;
    }

    private double slope(double[] x, double[] y, int iLeft, int iRight) {
        double deltaX = x[iRight] - x[iLeft];
        if (deltaX == 0) {
            return 0.0;
        }
        return (y[iRight] - y[iLeft]) / deltaX;
    }
}
