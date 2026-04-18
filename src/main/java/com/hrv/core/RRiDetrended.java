package com.hrv.core;

/** 去趋势后的 RRi。 */
public class RRiDetrended extends RRi {
    public RRiDetrended(double[] rri, double[] time, boolean interpolated) {
        super(rri, time, true, interpolated);
    }
}
