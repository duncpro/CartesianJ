package com.duncpro.cartesianj;

import static com.duncpro.cartesianj.MathUtil.mod;
import static com.duncpro.cartesianj.awt.AwtCartesianPlane.present;
import static java.lang.Math.*;

public class PlotFunctionsDemo {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
        plane.plot("g", x -> pow(x, 3));
        plane.plot("h", x -> pow(x, 3) + x);
        plane.plot("f", x -> mod(pow(x, 2), 7));
        present(plane);
    }
}
