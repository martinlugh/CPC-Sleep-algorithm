package com.sleep.platform.signal;

import org.springframework.stereotype.Component;

@Component
public class FastFourierTransformerSupport {

    public FftResult fft(double[] realInput) {
        if (realInput == null || realInput.length == 0) {
            return new FftResult(new double[0], new double[0]);
        }
        int n = 1;
        while (n < realInput.length) {
            n <<= 1;
        }
        double[] real = new double[n];
        double[] imag = new double[n];
        System.arraycopy(realInput, 0, real, 0, realInput.length);

        bitReverse(real, imag);
        for (int len = 2; len <= n; len <<= 1) {
            double angle = -2 * Math.PI / len;
            double wLenReal = Math.cos(angle);
            double wLenImag = Math.sin(angle);
            for (int i = 0; i < n; i += len) {
                double wReal = 1.0;
                double wImag = 0.0;
                for (int j = 0; j < len / 2; j++) {
                    int uIndex = i + j;
                    int vIndex = i + j + len / 2;
                    double vReal = real[vIndex] * wReal - imag[vIndex] * wImag;
                    double vImag = real[vIndex] * wImag + imag[vIndex] * wReal;
                    real[vIndex] = real[uIndex] - vReal;
                    imag[vIndex] = imag[uIndex] - vImag;
                    real[uIndex] = real[uIndex] + vReal;
                    imag[uIndex] = imag[uIndex] + vImag;
                    double nextWReal = wReal * wLenReal - wImag * wLenImag;
                    wImag = wReal * wLenImag + wImag * wLenReal;
                    wReal = nextWReal;
                }
            }
        }
        return new FftResult(real, imag);
    }

    private void bitReverse(double[] real, double[] imag) {
        int n = real.length;
        int j = 0;
        for (int i = 1; i < n; i++) {
            int bit = n >> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>= 1;
            }
            j ^= bit;
            if (i < j) {
                double tempReal = real[i];
                real[i] = real[j];
                real[j] = tempReal;
                double tempImag = imag[i];
                imag[i] = imag[j];
                imag[j] = tempImag;
            }
        }
    }

    public static class FftResult {
        private final double[] real;
        private final double[] imag;

        public FftResult(double[] real, double[] imag) {
            this.real = real;
            this.imag = imag;
        }

        public double[] getReal() {
            return real;
        }

        public double[] getImag() {
            return imag;
        }
    }
}
