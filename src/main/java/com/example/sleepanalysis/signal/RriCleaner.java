package com.example.sleepanalysis.signal;

/**
 * RRI（RR Interval）清洗器。
 * <p>
 * 处理步骤：
 * 1. 剔除小于 300ms 或大于 1500ms 的异常值。
 * 2. 对有效序列执行中值滤波。
 * </p>
 */
public class RriCleaner {

    /** RRI 最小有效值（毫秒）。 */
    private static final double MIN_RRI_MS = 300.0D;

    /** RRI 最大有效值（毫秒）。 */
    private static final double MAX_RRI_MS = 1500.0D;

    /** 默认中值滤波窗口。 */
    private static final int DEFAULT_MEDIAN_WINDOW = 5;

    /**
     * 执行 RRI 清洗。
     *
     * @param rriMillis 原始 RRI（毫秒）
     * @return 清洗后的 RRI（毫秒）
     */
    public double[] clean(double[] rriMillis) {
        return clean(rriMillis, DEFAULT_MEDIAN_WINDOW);
    }

    /**
     * 执行 RRI 清洗并指定中值窗口。
     *
     * @param rriMillis 原始 RRI（毫秒）
     * @param medianWindow 中值滤波窗口（建议奇数）
     * @return 清洗后的 RRI（毫秒）
     */
    public double[] clean(double[] rriMillis, int medianWindow) {
        if (rriMillis == null || rriMillis.length == 0) {
            return new double[0];
        }

        int validCount = 0;
        for (int i = 0; i < rriMillis.length; i++) {
            double value = rriMillis[i];
            if (value >= MIN_RRI_MS && value <= MAX_RRI_MS) {
                validCount++;
            }
        }

        if (validCount == 0) {
            return new double[0];
        }

        double[] validRri = new double[validCount];
        int idx = 0;
        for (int i = 0; i < rriMillis.length; i++) {
            double value = rriMillis[i];
            if (value >= MIN_RRI_MS && value <= MAX_RRI_MS) {
                validRri[idx++] = value;
            }
        }

        return medianFilter(validRri, medianWindow);
    }

    /**
     * 中值滤波。
     *
     * @param input 输入数组
     * @param windowSize 窗口长度
     * @return 滤波结果
     */
    private double[] medianFilter(double[] input, int windowSize) {
        int n = input.length;
        if (n == 0) {
            return new double[0];
        }

        int actualWindow = normalizeWindow(windowSize);
        if (actualWindow <= 1 || n < 3) {
            double[] copy = new double[n];
            System.arraycopy(input, 0, copy, 0, n);
            return copy;
        }

        int half = actualWindow / 2;
        double[] output = new double[n];
        double[] buffer = new double[actualWindow];

        for (int i = 0; i < n; i++) {
            int start = i - half;
            int end = i + half;
            if (start < 0) {
                start = 0;
            }
            if (end >= n) {
                end = n - 1;
            }

            int len = end - start + 1;
            for (int j = 0; j < len; j++) {
                buffer[j] = input[start + j];
            }

            insertionSort(buffer, len);
            output[i] = buffer[len / 2];
        }

        return output;
    }

    /**
     * 归一化窗口大小，确保为大于等于 1 的奇数。
     */
    private int normalizeWindow(int windowSize) {
        if (windowSize <= 1) {
            return 1;
        }
        if ((windowSize & 1) == 0) {
            return windowSize + 1;
        }
        return windowSize;
    }

    /**
     * 原地插入排序（仅排序前 len 个元素）。
     */
    private void insertionSort(double[] values, int len) {
        for (int i = 1; i < len; i++) {
            double key = values[i];
            int j = i - 1;
            while (j >= 0 && values[j] > key) {
                values[j + 1] = values[j];
                j--;
            }
            values[j + 1] = key;
        }
    }
}
