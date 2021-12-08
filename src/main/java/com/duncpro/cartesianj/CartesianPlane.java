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
    private final Set<Point> points = new HashSet<>();
    public void plot(Point point) {
        requireNonNull(point);
        points.add(point);
        changeListeners.forEach(Runnable::run);
    }
    public Set<Point> getPoints() {
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

    private int yTickWidth = 20;
    private int xTickWidth = 20;
    public void setTickWidth(Axis axis, int size) {
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
    public int getTickWidth(Axis axis) {
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
    public int getViewportOffset(Direction direction) {
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

    /**
     * The viewport offset shifts the given dimension of the viewport by the given number of pixels.
     * For example, for a horizontal viewport offset of 2, the y axis would appear 2 units closer
     * to the right edge of the plane.
     * For a vertical offset of 2, the x axis would appear 2 units closer to the bottom edge of the plane.
     */
    public void setViewportOffset(Direction dimension, int newOffset) {
        requireNonNull(dimension);
        switch (dimension) {
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
