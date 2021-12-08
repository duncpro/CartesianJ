package com.duncpro.cartesianj;

import java.util.Comparator;

public class ViewportUtils {
    public static void fitAllPoints(CartesianPlaneViewport viewport) {
        viewport.setQuantitativeStepSize(Axis.X, 1);
        viewport.setQuantitativeStepSize(Axis.Y, 1);

        double maxX = viewport.getPlane().getPoints().stream()
                .max(Comparator.comparing(Point::getX))
                .map(Point::getX)
                .orElse(0d);

        while (viewport.getXAxisPosition() + viewport.getConverter().toPx(maxX, Axis.X) > viewport.getWidth()) {
            viewport.setQuantitativeStepSize(Axis.X, viewport.getQuantitativeStepSize(Axis.X) + 1);
        }

        double maxY = viewport.getPlane().getPoints().stream()
                .max(Comparator.comparing(Point::getY))
                .map(Point::getY)
                .orElse(0d);

        while (viewport.getYAxisPosition() + viewport.getConverter().toPx(maxY, Axis.Y) > viewport.getHeight()) {
            viewport.setQuantitativeStepSize(Axis.Y, viewport.getQuantitativeStepSize(Axis.Y) + 1);
        }
    }
}
