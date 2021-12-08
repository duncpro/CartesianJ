package com.duncpro.cartesianj.awt;

import com.duncpro.cartesianj.Axis;
import com.duncpro.cartesianj.CartesianPlane;
import com.duncpro.cartesianj.LabeledPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private void autofitPointsUsingStepSize() {
        double maxX = plane.getPoints().stream()
                .max(Comparator.comparing(LabeledPoint::getX))
                .map(LabeledPoint::getX)
                .orElse(0d);

        while (getXAxisPositionPx() + convert.toPx(maxX, Axis.X) > getWidth()) {
            plane.setStepSize(Axis.X, plane.getStepSize(Axis.X) + 1);
        }

        double maxY = plane.getPoints().stream()
                .max(Comparator.comparing(LabeledPoint::getY))
                .map(LabeledPoint::getY)
                .orElse(0d);

        while (getYAxisPositionPx() + convert.toPx(maxY, Axis.Y) > getHeight()) {
            plane.setStepSize(Axis.Y, plane.getStepSize(Axis.Y) + 1);
        }
    }

    public int getXAxisPositionPx() {
        return (getHeight() / 2) + plane.getViewOffset(VERTICAL);
    }

    public int getYAxisPositionPx() {
        return (getWidth() / 2) - plane.getViewOffset(HORIZONTAL);
    }

    /**
     * Calculates an approximate value (in steps on the x-axis) for the given pixel position.
     * The returned value is an approximation and will very rarely be an evenly divisible number,
     * instead it is more likely to be a long ugly decimal.
     */
    public double xPosToStep(int xPx) {
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

        int ticksPerQuadrant = (int) (getWidth() / 2 / plane.getTickWidth(Axis.X));
        for (int tick = 0; tick <= ticksPerQuadrant; tick++) {
            int xPxNegative = getYAxisPositionPx() - (tick * convert.toPx(plane.getStepSize(Axis.X), Axis.X));
            int xPxPositive = getYAxisPositionPx() + (tick * convert.toPx(plane.getStepSize(Axis.X), Axis.X));
            graphics.drawLine(xPxNegative, getXAxisPositionPx() - 5, xPxNegative, getXAxisPositionPx() + 5);
            graphics.drawLine(xPxPositive, getXAxisPositionPx() - 5, xPxPositive, getXAxisPositionPx() + 5);
        }
    }

    private void paintYAxis(Graphics graphics) {
        graphics.drawLine(getYAxisPositionPx(), 0, getYAxisPositionPx(), getHeight());
        int ticksPerQuadrant = (int) (getHeight() / 2 / plane.getTickWidth(Axis.Y));
        for (int tick = 0; tick <= ticksPerQuadrant; tick++) {
            int yPx = tick * convert.toPx(plane.getStepSize(Axis.Y), Axis.Y);
            graphics.drawLine(getYAxisPositionPx() - 5, getXAxisPositionPx() + yPx, getYAxisPositionPx() + 5, getXAxisPositionPx() + yPx);
            graphics.drawLine(getYAxisPositionPx() - 5, getXAxisPositionPx() - yPx, getYAxisPositionPx() + 5, getXAxisPositionPx() - yPx);
        }
    }

    private void paintPoint(Graphics graphics, LabeledPoint point) {
        graphics.drawOval(xToPos(point.getX()), yToPos(point.getY()), 5, 5);
    }

    private void paintFunction(Graphics graphics, Function<Double, Double> function) {
        for (int xPx = 0; xPx < getWidth(); xPx++) {
            double x1 = xPosToStep(xPx);
            double y1 = function.apply(x1);

            double x2 = xPosToStep(xPx + 1);
            double y2 = function.apply(x2);

            if (Double.isNaN(y1) || Double.isNaN(y2)) continue;

            graphics.drawLine(xToPos(x1), yToPos(y1), xToPos(x2), yToPos(y2));
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
        final var autofitPointsButton = new JMenuItem("Autofit Points");
        autofitPointsButton.addActionListener(event -> awtCartesianPlane.autofitPointsUsingStepSize());
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
        moveViewportRight.addActionListener(event -> plane.setViewOffset(HORIZONTAL, plane.getViewOffset(HORIZONTAL) + 1));
        viewMenu.add(moveViewportRight);

        final var moveViewportLeft = new JMenuItem("Move Viewport Left");
        moveViewportLeft.setAccelerator(KeyStroke.getKeyStroke("LEFT"));
        moveViewportLeft.addActionListener(event -> plane.setViewOffset(HORIZONTAL, plane.getViewOffset(HORIZONTAL) - 1));
        viewMenu.add(moveViewportLeft);

        final var moveViewportUp = new JMenuItem("Move Viewport Up");
        moveViewportUp.setAccelerator(KeyStroke.getKeyStroke("UP"));
        moveViewportUp.addActionListener(event -> plane.setViewOffset(VERTICAL, plane.getViewOffset(VERTICAL) + 1));
        viewMenu.add(moveViewportUp);

        final var moveViewportDown = new JMenuItem("Move Viewport Down");
        moveViewportDown.setAccelerator(KeyStroke.getKeyStroke("DOWN"));
        moveViewportDown.addActionListener(event -> plane.setViewOffset(VERTICAL, plane.getViewOffset(VERTICAL) - 1));
        viewMenu.add(moveViewportDown);

        window.getJMenuBar().add(viewMenu);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
