package com.hrv.analysis;

import java.util.Arrays;

import com.hrv.core.HrvUtils;
import com.hrv.core.RRi;
import com.hrv.core.RRiDetrended;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/** 去趋势算法。 */
public final class Detrend {
    private Detrend() {
    }

    public static RRiDetrended polynomialDetrend(double[] rri, double[] time, int degree) {
        double[] x = HrvUtils.transformRri(rri);
        double[] t = time == null ? HrvUtils.createTimeInfo(x) : Arrays.copyOf(time, time.length);

        double[][] a = new double[t.length][degree + 1];
        for (int i = 0; i < t.length; i++) {
            for (int j = 0; j <= degree; j++) {
                a[i][j] = Math.pow(t[i], degree - j);
            }
        }
        RealMatrix m = new Array2DRowRealMatrix(a, false);
        RealVector y = new ArrayRealVector(x, false);
        RealMatrix ata = m.transpose().multiply(m);
        RealVector aty = m.transpose().operate(y);
        DecompositionSolver solver = new LUDecomposition(ata).getSolver();
        RealVector coef = solver.solve(aty);

        double[] trend = new double[t.length];
        for (int i = 0; i < t.length; i++) {
            double v = 0.0;
            for (int j = 0; j <= degree; j++) {
                v += coef.getEntry(j) * Math.pow(t[i], degree - j);
            }
            trend[i] = v;
        }

        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) out[i] = x[i] - trend[i];
        return new RRiDetrended(out, t, false);
    }

    public static RRiDetrended smoothnessPriors(double[] rri, double[] time, double lambda, double fs) {
        double[] x = HrvUtils.transformRri(rri);
        double[] t = time == null ? HrvUtils.createTimeInfo(x) : Arrays.copyOf(time, time.length);

        PolynomialSplineFunction spline = new SplineInterpolator().interpolate(t, x);
        double step = 1.0 / fs;
        int n = (int) Math.floor((t[t.length - 1] - t[0]) / step) + 1;
        double[] ti = new double[n];
        double[] xi = new double[n];
        for (int i = 0; i < n; i++) {
            ti[i] = t[0] + i * step;
            xi[i] = spline.value(ti[i]);
        }

        RealMatrix iMat = org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix(n);
        RealMatrix d2 = new Array2DRowRealMatrix(n - 2, n);
        for (int r = 0; r < n - 2; r++) {
            d2.setEntry(r, r, 1.0);
            d2.setEntry(r, r + 1, -2.0);
            d2.setEntry(r, r + 2, 1.0);
        }

        RealMatrix invArg = iMat.add(d2.transpose().multiply(d2).scalarMultiply(lambda * lambda));
        RealMatrix inv = new LUDecomposition(invArg).getSolver().getInverse();
        RealMatrix h = iMat.subtract(inv);

        RealVector xv = new ArrayRealVector(xi, false);
        double[] z = h.operate(xv).toArray();
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = xi[i] - z[i];
        return new RRiDetrended(out, ti, true);
    }

    /**
     * 简化版 Savitzky-Golay 去趋势：使用窗口滑动的多项式拟合。
     */
    public static RRiDetrended sgDetrend(double[] rri, double[] time, int windowLength, int polyOrder) {
        double[] x = HrvUtils.transformRri(rri);
        double[] t = time == null ? HrvUtils.createTimeInfo(x) : Arrays.copyOf(time, time.length);
        if (windowLength % 2 == 0) throw new IllegalArgumentException("windowLength 必须是奇数");
        int half = windowLength / 2;

        double[] trend = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            int l = Math.max(0, i - half);
            int r = Math.min(x.length - 1, i + half);
            int n = r - l + 1;
            double[][] a = new double[n][polyOrder + 1];
            double[] y = new double[n];
            for (int k = 0; k < n; k++) {
                double tt = t[l + k] - t[i];
                for (int p = 0; p <= polyOrder; p++) {
                    a[k][p] = Math.pow(tt, p);
                }
                y[k] = x[l + k];
            }
            RealMatrix m = new Array2DRowRealMatrix(a, false);
            RealVector yv = new ArrayRealVector(y, false);
            RealMatrix ata = m.transpose().multiply(m);
            RealVector aty = m.transpose().operate(yv);
            RealVector coef = new LUDecomposition(ata).getSolver().solve(aty);
            trend[i] = coef.getEntry(0);
        }

        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) out[i] = x[i] - trend[i];
        return new RRiDetrended(out, t, false);
    }
}
