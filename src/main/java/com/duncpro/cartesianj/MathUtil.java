package com.duncpro.cartesianj;

public class MathUtil {
    public static double mod(double x, double m) {
        boolean isFractionalNumber = x % 1 != 0;

        if (isFractionalNumber) return Double.NaN;

        return x % m;
    }
}
