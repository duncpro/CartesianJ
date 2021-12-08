package com.duncpro.cartesianj.awt;

import com.duncpro.cartesianj.*;
import com.duncpro.cartesianj.Point;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.duncpro.cartesianj.Direction.HORIZONTAL;
import static com.duncpro.cartesianj.Direction.VERTICAL;
import static com.duncpro.cartesianj.ViewportUtils.fitAllPoints;
import static com.duncpro.cartesianj.awt.AwtUtil.generalPurposeColorMap;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public class AwtCartesianPlaneViewport extends Canvas implements CartesianPlaneViewport {
    private final CartesianPlane plane;
    private final UnitConverter converter;

    public AwtCartesianPlaneViewport(CartesianPlane plane) {
        this.plane = requireNonNull(plane);

        this.converter = new UnitConverter(this);

        // To make working CartesianPlane more ergonomic, the class is mutable.
        // It is possible to add or remove functions from the CartesianPlane, among other things.
        // Therefore, tis AwtCartesianPlaneView must monitor the given CartesianPlane for changes,
        // and redraw the view when they occur.
        final Runnable onChange = this::repaint;
        plane.addChangeListener(onChange);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                plane.addChangeListener(onChange);
            }
            @Override
            public void componentHidden(ComponentEvent e) {
                plane.removeChangeListener(onChange);
            }
        });
    }

    public int getXAxisPosition() {
        return (getHeight() / 2) + getOffset(VERTICAL);
    }

    public int getYAxisPosition() {
        return (getWidth() / 2) + getOffset(HORIZONTAL);
    }

    /**
     * Calculates an approximate value (in steps on the x-axis) for the given pixel position.
     * The returned value is an approximation and will very rarely be an evenly divisible number,
     * instead it is more likely to be a long ugly decimal.
     */
    public double xPosToX(int xPx) {
        return converter.toUnits(xPx - getYAxisPosition(), Axis.X);
    }

    public int yToPos(double step) {
        return getXAxisPosition() - converter.toPx(step, Axis.Y);
    }

    public int xToPos(double step) {
        return getYAxisPosition() + converter.toPx(step, Axis.X);
    }

    @Override
    public void paint(Graphics graphics) {
        ((Graphics2D) graphics).setStroke(new BasicStroke(1));
        paintXAxis(graphics);
        paintYAxis(graphics);
        ((Graphics2D) graphics).setStroke(new BasicStroke(3));
        for (int i = 0; i < plane.getFunctions().size(); i++) {
            Color c = generalPurposeColorMap().apply(i);
            Function<Double, Double> f = plane.getFunctions().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList())
                    .get(i);
            graphics.setColor(c);
            paintFunction(graphics, f);
            graphics.setColor(Color.black);

        }
        plane.getPoints().forEach(point -> paintPoint(graphics, point));
        paintStepSize(graphics);
    }

    private void paintStepSize(Graphics graphics) {
        final var stepSizeX = "Step Size (X): " + getQuantitativeStepSize(Axis.X);
        final var stepSizeY = "Step Size (Y): " + getQuantitativeStepSize(Axis.Y);
        final var textWidth = max(
                graphics.getFontMetrics().stringWidth(stepSizeX),
                graphics.getFontMetrics().stringWidth(stepSizeY)
        );
        final var x = getWidth() - (textWidth + 10 /* padding */);
        final var y = getHeight() - (2 * graphics.getFontMetrics().getHeight());
        graphics.drawString(stepSizeX, x, y);
        graphics.drawString(stepSizeY, x, y + graphics.getFontMetrics().getHeight());
    }

    private void paintXAxis(Graphics graphics) {
        graphics.drawLine(0, getXAxisPosition(), getWidth(), getXAxisPosition());
        int visibleLeftEdgeX = 0;
        int visibleRightEdgeX = visibleLeftEdgeX + getWidth();
        int yAxisX = getYAxisPosition();

        // Quadrants I & IV
        int distanceFromYAxisToRightEdgePx = visibleRightEdgeX - yAxisX;

        int distanceFromYAxisToRightEdgeTicks = distanceFromYAxisToRightEdgePx / getVisualStepSize(Axis.X);
        for (int tick = 0; tick <= distanceFromYAxisToRightEdgeTicks; tick++) {
            int xPx = getYAxisPosition() + (tick * getVisualStepSize(Axis.X));
            graphics.drawLine(xPx, getXAxisPosition() - 5, xPx, getXAxisPosition() + 5);
        }

        // Quadrants II & III
        int distanceFromYAxisToLeftEdgePx = yAxisX - visibleLeftEdgeX;
        int distanceFromYAxisToLeftEdgeTicks = distanceFromYAxisToLeftEdgePx / getVisualStepSize(Axis.X);
        for (int tick = 0; tick <= distanceFromYAxisToLeftEdgeTicks; tick++) {
            int xPx = getYAxisPosition() - (tick * converter.toPx(getQuantitativeStepSize(Axis.X), Axis.X));
            graphics.drawLine(xPx, getXAxisPosition() - 5, xPx, getXAxisPosition() + 5);
        }
    }

    private void paintYAxis(Graphics graphics) {
        graphics.drawLine(getYAxisPosition(), 0, getYAxisPosition(), getHeight());

        int topEdgeY = 0;
        int bottomEdgeY = topEdgeY + getHeight();
        int xAxisY = getXAxisPosition();

        int distanceFromXAxisToTopEdgePx = xAxisY - topEdgeY;
        int distanceFromXAxisToTopEdgeTicks = distanceFromXAxisToTopEdgePx / getVisualStepSize(Axis.Y);
        for (int tick = 0; tick <= distanceFromXAxisToTopEdgeTicks; tick++) {
            int yPx = getXAxisPosition() - tick * converter.toPx(getQuantitativeStepSize(Axis.Y), Axis.Y);
            graphics.drawLine(getYAxisPosition() - 5, yPx, getYAxisPosition() + 5, yPx);
        }

        int distanceFromXAxisToBottomEdgePx = bottomEdgeY - xAxisY;
        int distanceFromXAxisToBottomEdgeTicks = distanceFromXAxisToBottomEdgePx / getVisualStepSize(Axis.Y);
        for (int tick = 0; tick <= distanceFromXAxisToBottomEdgeTicks; tick++) {
            int yPx = getXAxisPosition() + (tick * converter.toPx(getQuantitativeStepSize(Axis.Y), Axis.Y));
            graphics.drawLine(getYAxisPosition() - 5, yPx, getYAxisPosition() + 5, yPx);
        }
    }

    final int POINT_SIZE = 8;
    private void paintPoint(Graphics graphics, Point point) {
        graphics.fillOval(xToPos(point.getX()) - (POINT_SIZE / 2), yToPos(point.getY()) - (POINT_SIZE / 2), POINT_SIZE, POINT_SIZE);
    }

    private void paintFunction(Graphics graphics, Function<Double, Double> function) {
        List<Point> points = new ArrayList<>();

        for (double x = 0; x <= converter.toUnits(getWidth(), Axis.X); x += getQuantitativeStepSize(Axis.X)) {
            double yP = function.apply(x);
            double yN = function.apply(x * -1);

            points.add(new Point(x, yP));
            points.add(new Point(x * -1, yN));
            paintPoint(graphics, new Point(x, yP));
            paintPoint(graphics, new Point(x * -1, yN));
        }

        points.sort(Comparator.comparingDouble(Point::getX));

        // Fill in points between
        Set<Point> inbetweenSteps = new HashSet<>();
        for (int i = 0; i + 1 < points.size(); i++) {
            final var p1 = points.get(i);
            final var p2 = points.get(i + 1);

            if (Double.isNaN(p1.getY())) continue;
            if (Double.isNaN(p2.getY())) continue;

            for (int xPos = xToPos(p1.getX()); xPos <= xToPos(p2.getX()); xPos++) {
                double x = xPosToX(xPos);
                inbetweenSteps.add(new Point(x, function.apply(x)));
            }
        }

        points.addAll(inbetweenSteps);
        points.sort(Comparator.comparing(Point::getX));

        for (int i = 0; i + 1 < points.size(); i++) {
            final var p1 = points.get(i);
            final var p2 = points.get(i + 1);

            if (Double.isNaN(p1.getY())) continue;
            if (Double.isNaN(p2.getY())) continue;

            graphics.drawLine(xToPos(p1.getX()), yToPos(p1.getY()), xToPos(p2.getX()), yToPos(p2.getY()));

        }

    }

    private int yTickWidth = 20;
    private int xTickWidth = 20;
    @SuppressWarnings("DuplicatedCode")
    public void setVisualStepSize(Axis axis, int size) {
        if (size <= 0) throw new IllegalArgumentException();
        switch (axis) {
            case X:
                xTickWidth = size;
                break;
            case Y:
                yTickWidth = size;
                break;
        }
        repaint();
    }
    public int getVisualStepSize(Axis axis) {
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
    @SuppressWarnings("DuplicatedCode")
    public void setQuantitativeStepSize(Axis axis, double stepSize) {
        if (stepSize <= 0) throw new IllegalArgumentException();
        switch (axis) {
            case X:
                xAxisStepSize = stepSize;
                break;
            case Y:
                yAxisStepSize = stepSize;
                break;
        }
        repaint();
    }
    public double getQuantitativeStepSize(Axis axis) {
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
    public int getOffset(Direction direction) {
        requireNonNull(direction);
        switch (direction) {
            case HORIZONTAL:
                return horizontalOffset;
            case VERTICAL:
                return verticalOffset;
        }
        repaint();
        throw new AssertionError();
    }

    /**
     * The viewport offset shifts the given dimension of the viewport by the given number of pixels.
     * For example, for a horizontal viewport offset of 2, the y axis would appear 2 units closer
     * to the right edge of the plane.
     * For a vertical offset of 2, the x axis would appear 2 units closer to the bottom edge of the plane.
     */
    public void setOffset(Direction dimension, int newOffset) {
        requireNonNull(dimension);
        switch (dimension) {
            case HORIZONTAL:
                horizontalOffset = newOffset;
                break;
            case VERTICAL:
                verticalOffset = newOffset;
                break;
        }
        repaint();
    }

    @Override
    public UnitConverter getConverter() {
        return converter;
    }

    @Override
    public CartesianPlane getPlane() {
        return plane;
    }
}
