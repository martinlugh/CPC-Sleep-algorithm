package com.example.sleepanalysis.signal;

/**
 * 窗函数应用器（Hanning 窗）。
 */
public class WindowFunctionApplier {

    /**
     * 生成 Hanning 窗。
     *
     * @param size 窗长度
     * @return Hanning 窗数组
     */
    public double[] createHanningWindow(int size) {
        if (size <= 0) {
            return new double[0];
        }

        if (size == 1) {
            return new double[]{1.0D};
        }

        double[] window = new double[size];
        double factor = 2.0D * Math.PI / (size - 1);
        for (int i = 0; i < size; i++) {
            window[i] = 0.5D - 0.5D * Math.cos(factor * i);
        }
        return window;
    }

    /**
     * 原地应用 Hanning 窗。
     *
     * @param signal 输入输出同一数组
     */
    public void applyHanningInPlace(double[] signal) {
        if (signal == null || signal.length == 0) {
            return;
        }

        int n = signal.length;
        if (n == 1) {
            return;
        }

        double factor = 2.0D * Math.PI / (n - 1);
        for (int i = 0; i < n; i++) {
            double w = 0.5D - 0.5D * Math.cos(factor * i);
            signal[i] *= w;
        }
    }

    /**
     * 非原地应用 Hanning 窗。
     *
     * @param signal 输入信号
     * @return 应用窗后的新数组
     */
    public double[] applyHanning(double[] signal) {
        if (signal == null || signal.length == 0) {
            return new double[0];
        }

        double[] copied = new double[signal.length];
        System.arraycopy(signal, 0, copied, 0, signal.length);
        applyHanningInPlace(copied);
        return copied;
    }
}
