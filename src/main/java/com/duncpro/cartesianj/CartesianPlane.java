package com.duncpro.cartesianj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents a view of a Cartesian Plane. Can be used to plot functions as well as points.
 */
public class CartesianPlane {
    private final Set<LabeledPoint> points = new HashSet<>();
    public void plot(LabeledPoint point) {
        requireNonNull(point);
        points.add(point);
        changeListeners.forEach(Runnable::run);
    }
    public Set<LabeledPoint> getPoints() {
        return Set.copyOf(points);
    }

    private final Map<String, Function<Double, Double>> functions = new HashMap<>();
    public void plot(String label, Function<Double, Double> f) {
        requireNonNull(f);
        requireNonNull(label);
        functions.put(label, f);
        changeListeners.forEach(Runnable::run);
    }
    public Map<String, Function<Double, Double>> getFunctions() {
        return Map.copyOf(functions);
    }

    private final Set<Runnable> changeListeners = new HashSet<>();
    public void addChangeListener(Runnable onChange) {
        changeListeners.add(onChange);
    }
    public void removeChangeListener(Runnable onChange) {
        changeListeners.remove(onChange);
    }

    private double yTickWidth = 20;
    private double xTickWidth = 20;
    public void setTickWidth(Axis axis, double size) {
        if (size <= 0) throw new IllegalArgumentException();
        switch (axis) {
            case X:
                xTickWidth = size;
                break;
            case Y:
                yTickWidth = size;
                break;
        }
        changeListeners.forEach(Runnable::run);
    }
    public double getTickWidth(Axis axis) {
        requireNonNull(axis);
        switch (axis) {
            case X:
                return xTickWidth;
            case Y:
                return yTickWidth;
        }
        throw new AssertionError();
    }

    private double xAxisStepSize = 1;
    private double yAxisStepSize = 1;
    public void setStepSize(Axis axis, double stepSize) {
        if (stepSize <= 0) throw new IllegalArgumentException();

        switch (axis) {
            case X:
                xAxisStepSize = stepSize;
                break;
            case Y:
                yAxisStepSize = stepSize;
                break;
        }
        changeListeners.forEach(Runnable::run);
    }
    public double getStepSize(Axis axis) {
        requireNonNull(axis);
        switch (axis) {
            case X:
                return xAxisStepSize;
            case Y:
                return yAxisStepSize;
        }
        throw new AssertionError();
    }

    int horizontalOffset = 0;
    int verticalOffset = 0;
    public int getViewOffset(Direction direction) {
        requireNonNull(direction);
        switch (direction) {
            case HORIZONTAL:
                return horizontalOffset;
            case VERTICAL:
                return verticalOffset;
        }
        changeListeners.forEach(Runnable::run);
        throw new AssertionError();
    }
    public void setViewOffset(Direction direction, int newOffset) {
        requireNonNull(direction);
        switch (direction) {
            case HORIZONTAL:
                horizontalOffset = newOffset;
                break;
            case VERTICAL:
                verticalOffset = newOffset;
                break;
        }
        changeListeners.forEach(Runnable::run);
    }
}
