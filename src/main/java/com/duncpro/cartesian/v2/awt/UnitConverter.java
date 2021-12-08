package com.duncpro.cartesian.v2.awt;

import com.duncpro.cartesian.v2.Axis;
import com.duncpro.cartesian.v2.CartesianPlane;

import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;

public abstract class UnitConverter {
    private final CartesianPlane plane;

    public UnitConverter(CartesianPlane plane) {
        this.plane = requireNonNull(plane);
    }

    protected abstract int getViewDimensionPx(Axis axis);

    public int toPx(double step, Axis axis) {
        double pxPerStep = getViewDimensionPx(axis) / (2 * (plane.getAxisSize(axis) / plane.getStepSize(axis)));
        return toIntExact(round(step * pxPerStep));
    }

    public double toStep(int px, Axis axis) {
        double stepPerPx = (2 * (plane.getAxisSize(axis) / plane.getStepSize(axis)) / getViewDimensionPx(axis));
        return stepPerPx * px;
    }
}
