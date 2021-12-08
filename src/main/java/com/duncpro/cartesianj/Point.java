package com.duncpro.cartesianj;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class Point {
    private final double x;
    private final double y;

    public Point(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
