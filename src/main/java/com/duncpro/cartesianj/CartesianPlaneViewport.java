package com.duncpro.cartesianj;

import com.duncpro.cartesianj.awt.AwtPixelConverter;

import java.util.Comparator;

public interface CartesianPlaneViewport {
    void setVisualStepSize(Axis axis, int size);

    int getVisualStepSize(Axis axis);

    void setQuantitativeStepSize(Axis axis, double stepSize);

    double getQuantitativeStepSize(Axis axis);

    int getOffset(Direction direction);

    /**
     * This function the given dimension of the viewport by the given number of pixels.
     * For example, when setting a horizontal viewport offset of 2, the y axis will appear 2 pixels closer
     * to the right edge of the plane. For a vertical offset of 2, the x axis would appear 2 pixels closer to the
     * bottom edge of the plane.
     */
    void setOffset(Direction dimension, int newOffset);

    AwtPixelConverter getConverter();

    int getWidth();

    int getHeight();

    int getXAxisPosition();

    int getYAxisPosition();

    CartesianPlane getPlane();

    default void decrementQuantitativeStepSize(Axis axis) {
        double current = getQuantitativeStepSize(axis);

        if (current > 1) {
            setQuantitativeStepSize(axis, current - 1);
        }
    }

    default void decrementQuantitativeStepSize() {
        decrementQuantitativeStepSize(Axis.X);
        decrementQuantitativeStepSize(Axis.Y);
    }

    default void incrementQuantitativeStepSize(Axis axis) {
        double current = getQuantitativeStepSize(axis);
        setQuantitativeStepSize(axis, current + 1);
    }

    default void incrementQuantitativeStepSize() {
        incrementQuantitativeStepSize(Axis.X);
        incrementQuantitativeStepSize(Axis.Y);
    }

    default void decrementVisualStepSize(Axis axis) {
        int current = getVisualStepSize(axis);

        if (current > 1) {
            setVisualStepSize(axis, current - 1);
        }
    }

    default void decrementVisualStepSize() {
        decrementVisualStepSize(Axis.X);
        decrementVisualStepSize(Axis.Y);
    }

    default void incrementVisualStepSize(Axis axis) {
        int current = getVisualStepSize(axis);
        setVisualStepSize(axis, current + 1);
    }

    default void incrementVisualStepSize() {
        incrementVisualStepSize(Axis.X);
        incrementVisualStepSize(Axis.Y);
    }

    default void plot(Point point) {
        getPlane().plot(point);
        fitData();
    }

    default void fitData() {
        setOffset(Direction.HORIZONTAL, 0);
        setOffset(Direction.VERTICAL, 0);

        int xStepsPerQuadrant = getWidth() / 2 / getVisualStepSize(Axis.X);
        getPlane().getPlottedPoints().stream()
                .max(Comparator.comparing(Point::getX))
                .map(Point::getX)
                .map(Math::abs)
                .map(maxX -> maxX / xStepsPerQuadrant)
                .map(Math::ceil)
                .map(newStepSize -> Math.max(newStepSize, getQuantitativeStepSize(Axis.X)))
                .ifPresent(stepSize -> setQuantitativeStepSize(Axis.X, stepSize));

        int yStepsPerQuadrant = getHeight() / 2 / getVisualStepSize(Axis.Y);
        getPlane().getPlottedPoints().stream()
                .max(Comparator.comparing(Point::getY))
                .map(Point::getY)
                .map(Math::abs)
                .map(maxY -> maxY / yStepsPerQuadrant)
                .map(Math::ceil)
                .map(newStepSize -> Math.max(newStepSize, getQuantitativeStepSize(Axis.Y)))
                .ifPresent(stepSize -> setQuantitativeStepSize(Axis.Y, stepSize));
    }
}
