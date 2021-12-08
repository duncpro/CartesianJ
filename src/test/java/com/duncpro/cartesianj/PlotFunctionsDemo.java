package com.duncpro.cartesianj;

import static com.duncpro.cartesianj.awt.AwtCartesianPlane.present;
import static java.lang.Math.*;

public class PlotFunctionsDemo {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
        plane.plot("f", x -> pow(x, 3));
        plane.plot("X", x -> sqrt(x));
        plane.plot("y", x -> 2 * x);
        present(plane);
    }
}
