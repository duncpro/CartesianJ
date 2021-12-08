package com.duncpro.cartesian.v2;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class LabeledPoint {
    private final double x;
    private final double y;
    private final String label;

    public LabeledPoint(Double x, Double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Optional<String> getLabel() {
        return ofNullable(label);
    }
}
