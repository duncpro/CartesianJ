package com.duncpro.cartesianj;

import static com.duncpro.cartesianj.CartesianJ.present;
import static com.duncpro.cartesianj.MathUtil.mod;
import static java.lang.Math.*;

public class PlotFunctionsDemo {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
//        plane.plot("z", x -> pow(x, 2));
//        plane.plot("p", x -> pow(x, 3));
//        plane.plot("j", x -> sqrt(x));
        plane.plot("x", x -> sin(x));
        plane.plot("s", x -> - sin(x));
        plane.plot(new Point(-1.5, 0.0));
        plane.plot(new Point(1.5, 0.0));
        plane.plot("b1", x -> {
            if (abs(x) >= 1 && abs(x) <= 2) {
                return 1.5;
            }
            return Double.NaN;
        });


        final var viewport = present(plane);
    }
}
