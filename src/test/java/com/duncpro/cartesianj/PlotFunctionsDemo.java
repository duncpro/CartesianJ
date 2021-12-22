package com.duncpro.cartesianj;

import static com.duncpro.cartesianj.CartesianJ.present;
import static java.lang.Math.*;

public class PlotFunctionsDemo {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
        plane.plot("f", x -> pow(x, 3));
        plane.plot("x", x -> pow(x, 2));

        final var viewport = present(plane);
    }
}
