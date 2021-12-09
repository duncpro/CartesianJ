package com.duncpro.cartesianj;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    public static double mod(double x, double m) {
        boolean isFractionalNumber = x % 1 != 0;

        if (isFractionalNumber) return Double.NaN;

        return x % m;
    }

    public static double round(double x, int precision) {
        var d = BigDecimal.valueOf(x);
        d = d.setScale(precision, RoundingMode.FLOOR);
        return d.doubleValue();
    }
}
