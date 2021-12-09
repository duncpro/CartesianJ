package com.duncpro.cartesianj;

import static com.duncpro.cartesianj.CartesianJ.present;
import static com.duncpro.cartesianj.MathUtil.mod;
import static java.lang.Math.*;

public class PlotFunctionsDemo {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
        plane.plot("g", x -> pow(x, 3));
        plane.plot("f", x -> mod(x, 7));
        plane.plot("z", x -> pow(x, 2));

        final var viewport = present(plane);
    }
}
