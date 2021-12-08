package com.duncpro.cartesianj.awt;

import com.duncpro.cartesianj.Axis;
import com.duncpro.cartesianj.CartesianPlane;

import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;

public abstract class UnitConverter {
    private final CartesianPlane plane;

    public UnitConverter(CartesianPlane plane) {
        this.plane = requireNonNull(plane);
    }

    protected abstract int getViewDimensionPx(Axis axis);

    public int toPx(double n, Axis axis) {
        double totalOnScreenTicks = getViewDimensionPx(axis) / plane.getTickWidth(axis);
        double pxPerTick = getViewDimensionPx(axis) / totalOnScreenTicks;
        return toIntExact(round((n / plane.getStepSize(axis)) * pxPerTick));
    }

    public double toStep(int px, Axis axis) {
        double totalOnScreenTicks = getViewDimensionPx(axis) / plane.getTickWidth(axis);
        double tickPerPx = totalOnScreenTicks / getViewDimensionPx(axis);
        return (tickPerPx * px) * plane.getStepSize(axis);
    }
}
