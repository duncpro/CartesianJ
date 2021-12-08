package com.duncpro.cartesian;

import com.duncpro.cartesian.v2.CartesianPlane;

import static com.duncpro.cartesian.v2.awt.AwtCartesianPlane.present;
import static java.lang.Math.*;

public class ImperativeTest {
    public static void main(String[] args) {
        final var plane = new CartesianPlane();
        plane.plot("f", x -> pow(x, 3));
        plane.plot("y", x -> (2 * x) + 3);
        present(plane);
    }
}
