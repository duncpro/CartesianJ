package com.duncpro.cartesianj.awt;

import com.duncpro.cartesianj.Axis;
import com.duncpro.cartesianj.CartesianPlane;
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
import static com.duncpro.cartesianj.awt.AwtUtil.generalPurposeColorMap;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public class AwtCartesianPlane extends Canvas {
    private final CartesianPlane plane;
    private final UnitConverter convert;

    public AwtCartesianPlane(CartesianPlane plane) {
        this.plane = requireNonNull(plane);
        
        this.convert = new UnitConverter(plane) {
            @Override
            protected int getViewDimensionPx(Axis axis) {
                requireNonNull(axis);
                switch (axis) {
                    case X:
                        return getWidth();
                    case Y:
                        return getHeight();
                }
                throw new AssertionError();
            }
        };

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

    private void autofitPoints() {
        double maxX = plane.getPoints().stream()
                .max(Comparator.comparing(Point::getX))
                .map(Point::getX)
                .orElse(0d);

        while (getXAxisPositionPx() + convert.toPx(maxX, Axis.X) > getWidth()) {
            plane.setStepSize(Axis.X, plane.getStepSize(Axis.X) + 1);
        }

        double maxY = plane.getPoints().stream()
                .max(Comparator.comparing(Point::getY))
                .map(Point::getY)
                .orElse(0d);

        while (getYAxisPositionPx() + convert.toPx(maxY, Axis.Y) > getHeight()) {
            plane.setStepSize(Axis.Y, plane.getStepSize(Axis.Y) + 1);
        }
    }

    public int getXAxisPositionPx() {
        return (getHeight() / 2) + plane.getViewportOffset(VERTICAL);
    }

    public int getYAxisPositionPx() {
        return (getWidth() / 2) + plane.getViewportOffset(HORIZONTAL);
    }

    /**
     * Calculates an approximate value (in steps on the x-axis) for the given pixel position.
     * The returned value is an approximation and will very rarely be an evenly divisible number,
     * instead it is more likely to be a long ugly decimal.
     */
    public double xPosToX(int xPx) {
        return convert.toStep(xPx - getYAxisPositionPx(), Axis.X);
    }

    public int yToPos(double step) {
        return getXAxisPositionPx() - convert.toPx(step, Axis.Y);
    }

    public int xToPos(double step) {
        return getYAxisPositionPx() + convert.toPx(step, Axis.X);
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
        final var stepSizeX = "Step Size (X): " + plane.getStepSize(Axis.X);
        final var stepSizeY = "Step Size (Y): " + plane.getStepSize(Axis.Y);
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
        graphics.drawLine(0, getXAxisPositionPx(), getWidth(), getXAxisPositionPx());
        int visibleLeftEdgeX = 0;
        int visibleRightEdgeX = visibleLeftEdgeX + getWidth();
        int yAxisX = getYAxisPositionPx();

        // Quadrants I & IV
        int distanceFromYAxisToRightEdgePx = visibleRightEdgeX - yAxisX;

        int distanceFromYAxisToRightEdgeTicks = distanceFromYAxisToRightEdgePx / plane.getTickWidth(Axis.X);
        for (int tick = 0; tick <= distanceFromYAxisToRightEdgeTicks; tick++) {
            int xPx = getYAxisPositionPx() + (tick * plane.getTickWidth(Axis.X));
            graphics.drawLine(xPx, getXAxisPositionPx() - 5, xPx, getXAxisPositionPx() + 5);
        }

        // Quadrants II & III
        int distanceFromYAxisToLeftEdgePx = yAxisX - visibleLeftEdgeX;
        int distanceFromYAxisToLeftEdgeTicks = distanceFromYAxisToLeftEdgePx / plane.getTickWidth(Axis.X);
        for (int tick = 0; tick <= distanceFromYAxisToLeftEdgeTicks; tick++) {
            int xPx = getYAxisPositionPx() - (tick * convert.toPx(plane.getStepSize(Axis.X), Axis.X));
            graphics.drawLine(xPx, getXAxisPositionPx() - 5, xPx, getXAxisPositionPx() + 5);
        }
    }

    private void paintYAxis(Graphics graphics) {
        graphics.drawLine(getYAxisPositionPx(), 0, getYAxisPositionPx(), getHeight());

        int topEdgeY = 0;
        int bottomEdgeY = topEdgeY + getHeight();
        int xAxisY = getXAxisPositionPx();

        int distanceFromXAxisToTopEdgePx = xAxisY - topEdgeY;
        int distanceFromXAxisToTopEdgeTicks = distanceFromXAxisToTopEdgePx / plane.getTickWidth(Axis.Y);
        for (int tick = 0; tick <= distanceFromXAxisToTopEdgeTicks; tick++) {
            int yPx = getXAxisPositionPx() - tick * convert.toPx(plane.getStepSize(Axis.Y), Axis.Y);
            graphics.drawLine(getYAxisPositionPx() - 5, yPx, getYAxisPositionPx() + 5, yPx);
        }

        int distanceFromXAxisToBottomEdgePx = bottomEdgeY - xAxisY;
        int distanceFromXAxisToBottomEdgeTicks = distanceFromXAxisToBottomEdgePx / plane.getTickWidth(Axis.Y);
        for (int tick = 0; tick <= distanceFromXAxisToBottomEdgeTicks; tick++) {
            int yPx = getXAxisPositionPx() + (tick * convert.toPx(plane.getStepSize(Axis.Y), Axis.Y));
            graphics.drawLine(getYAxisPositionPx() - 5, yPx, getYAxisPositionPx() + 5, yPx);
        }
    }

    final int POINT_SIZE = 8;
    private void paintPoint(Graphics graphics, Point point) {
        graphics.fillOval(xToPos(point.getX()) - (POINT_SIZE / 2), yToPos(point.getY()) - (POINT_SIZE / 2), POINT_SIZE, POINT_SIZE);
    }

    private void paintFunction(Graphics graphics, Function<Double, Double> function) {
        List<Point> points = new ArrayList<>();

        for (double x = 0; x <= convert.toStep(getWidth(), Axis.X); x += plane.getStepSize(Axis.X)) {
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


    public static void present(CartesianPlane plane) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "CartesianJ");

        final var window = new JFrame();
        final var awtCartesianPlane = new AwtCartesianPlane(plane);
        window.getContentPane().add(awtCartesianPlane);
        Dimension screenDimensions = window.getToolkit().getScreenSize();
        int windowSize = Math.min(screenDimensions.height, screenDimensions.width) / 2;
        window.setSize(windowSize, windowSize);

        window.setJMenuBar(new JMenuBar());
        final var viewMenu = new JMenu("View");

        // Step Size Settings
        final var increaseStepSize = new JMenuItem("Increase Step Size");
        increaseStepSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseStepSize.addActionListener(event -> {
            plane.setStepSize(Axis.X, plane.getStepSize(Axis.X) + 1);
            plane.setStepSize(Axis.Y, plane.getStepSize(Axis.Y) + 1);
        });
        viewMenu.add(increaseStepSize);
        final var decreaseStepSize = new JMenuItem("Decrease Step Size");
        decreaseStepSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseStepSize.addActionListener(event -> {
            plane.setStepSize(Axis.X, plane.getStepSize(Axis.X) - 1);
            plane.setStepSize(Axis.Y, plane.getStepSize(Axis.Y) - 1);
        });
        viewMenu.add(decreaseStepSize);
        viewMenu.add(new JSeparator());

        // Autofit Points
        final var autofitPointsButton = new JMenuItem("Autofit Points in Viewport");
        autofitPointsButton.addActionListener(event -> awtCartesianPlane.autofitPoints());
        viewMenu.add(autofitPointsButton);
        viewMenu.add(new JSeparator());

        // Axis Size Settings
        final var increaseAxisSize = new JMenuItem("Increase Tick Frequency");
        increaseAxisSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseAxisSize.addActionListener(event -> {
            plane.setTickWidth(Axis.X, plane.getTickWidth(Axis.X) - 1);
            plane.setTickWidth(Axis.Y, plane.getTickWidth(Axis.Y) - 1);
        });
        viewMenu.add(increaseAxisSize);
        final var decreaseAxisSize = new JMenuItem("Decrease Tick Frequency");
        decreaseAxisSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseAxisSize.addActionListener(event -> {
            plane.setTickWidth(Axis.X, plane.getTickWidth(Axis.X) + 1);
            plane.setTickWidth(Axis.Y, plane.getTickWidth(Axis.Y) + 1);
        });
        viewMenu.add(decreaseAxisSize);
        viewMenu.add(new JSeparator());

        // Offset Settings
        final var moveViewportRight = new JMenuItem("Move Viewport Right");
        moveViewportRight.setAccelerator(KeyStroke.getKeyStroke("RIGHT"));
        moveViewportRight.addActionListener(event -> plane.setViewportOffset(HORIZONTAL, plane.getViewportOffset(HORIZONTAL) - 1));
        viewMenu.add(moveViewportRight);

        final var moveViewportLeft = new JMenuItem("Move Viewport Left");
        moveViewportLeft.setAccelerator(KeyStroke.getKeyStroke("LEFT"));
        moveViewportLeft.addActionListener(event -> plane.setViewportOffset(HORIZONTAL, plane.getViewportOffset(HORIZONTAL) + 1));
        viewMenu.add(moveViewportLeft);

        final var moveViewportUp = new JMenuItem("Move Viewport Up");
        moveViewportUp.setAccelerator(KeyStroke.getKeyStroke("UP"));
        moveViewportUp.addActionListener(event -> plane.setViewportOffset(VERTICAL, plane.getViewportOffset(VERTICAL) + 1));
        viewMenu.add(moveViewportUp);

        final var moveViewportDown = new JMenuItem("Move Viewport Down");
        moveViewportDown.setAccelerator(KeyStroke.getKeyStroke("DOWN"));
        moveViewportDown.addActionListener(event -> plane.setViewportOffset(VERTICAL, plane.getViewportOffset(VERTICAL) - 1));
        viewMenu.add(moveViewportDown);

        window.getJMenuBar().add(viewMenu);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
