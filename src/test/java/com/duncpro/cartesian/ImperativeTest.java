package com.duncpro.cartesian;

import com.duncpro.cartesianj.CartesianPlane;

import static com.duncpro.cartesianj.awt.AwtCartesianPlane.present;
import static java.lang.Math.pow;

public class ImperativeTest {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
        plane.plot("f", x -> pow(x, 3));
        plane.plot("y", x -> (2 * x) + 3);
        present(plane);
    }
}
