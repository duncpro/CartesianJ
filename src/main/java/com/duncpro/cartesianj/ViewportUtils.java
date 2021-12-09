package com.duncpro.cartesianj;

import java.util.Comparator;
import java.util.function.Supplier;

public class ViewportUtils {
    public static void fitViewportToPoints(CartesianPlaneViewport viewport) {
        viewport.setQuantitativeStepSize(Axis.X, 1);
        viewport.setQuantitativeStepSize(Axis.Y, 1);

        final Supplier<Integer> maxX = () -> viewport.getPlane().getPoints().stream()
                .max(Comparator.comparing(Point::getX))
                .map(Point::getX)
                .map(x -> viewport.getConverter().toPx(x, Axis.X))
                .map(x -> x + viewport.getYAxisPosition())
                .orElse(0);

        while (maxX.get() >= viewport.getWidth()) {
            viewport.incrementQuantitativeStepSize(Axis.X);
        }

        final Supplier<Integer> maxY = () -> viewport.getPlane().getPoints().stream()
                .max(Comparator.comparing(Point::getY))
                .map(Point::getY)
                .map(y -> viewport.getConverter().toPx(y, Axis.Y))
                .map(y -> y + viewport.getXAxisPosition())
                .orElse(0);

        while (maxY.get() >= viewport.getHeight()) {
            viewport.incrementQuantitativeStepSize(Axis.Y);
        }
    }
}
