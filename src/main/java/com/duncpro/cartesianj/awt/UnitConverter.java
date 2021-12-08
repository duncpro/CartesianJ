package com.duncpro.cartesianj.awt;

import com.duncpro.cartesianj.Axis;
import com.duncpro.cartesianj.CartesianPlane;
import com.duncpro.cartesianj.CartesianPlaneViewport;

import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;

public class UnitConverter {
    private final CartesianPlaneViewport viewport;

    public UnitConverter(CartesianPlaneViewport viewport) {
        this.viewport = requireNonNull(viewport);
    }

    protected final int getViewDimensionPx(Axis axis) {
        requireNonNull(axis);
        switch (axis) {
            case X:
                return viewport.getWidth();
            case Y:
                return viewport.getHeight();
        }
        throw new AssertionError();
    }

    public final int toPx(double n, Axis axis) {
        double totalOnScreenTicks =  (double) getViewDimensionPx(axis) / viewport.getVisualStepSize(axis);
        double pxPerTick = getViewDimensionPx(axis) / totalOnScreenTicks;
        return toIntExact(round((n / viewport.getQuantitativeStepSize(axis)) * pxPerTick));
    }

    public final double toUnits(int px, Axis axis) {
        double totalOnScreenTicks = (double) getViewDimensionPx(axis) / viewport.getVisualStepSize(axis);
        double tickPerPx = totalOnScreenTicks / getViewDimensionPx(axis);
        return (tickPerPx * px) * viewport.getQuantitativeStepSize(axis);
    }
}
