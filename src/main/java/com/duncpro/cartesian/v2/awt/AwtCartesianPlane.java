package com.duncpro.cartesian.v2.awt;

import com.duncpro.cartesian.v2.Axis;
import com.duncpro.cartesian.v2.CartesianPlane;
import com.duncpro.cartesian.v2.Direction;
import com.duncpro.cartesian.v2.LabeledPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Function;

import static com.duncpro.cartesian.v2.Direction.HORIZONTAL;
import static com.duncpro.cartesian.v2.Direction.VERTICAL;
import static com.duncpro.cartesian.v2.awt.AwtUtil.drawPixel;
import static java.lang.Math.abs;
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

    public int getXAxisPositionPx() {
        return (getHeight() / 2) + plane.getViewOffset(Direction.VERTICAL);
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
        paintXAxis(graphics);
        paintYAxis(graphics);
        plane.getFunctions().values().forEach(f -> paintFunction(graphics, f));
        plane.getPoints().forEach(point -> paintPoint(graphics, point));
    }

    private void paintXAxis(Graphics graphics) {
        graphics.drawLine(0, getXAxisPositionPx(), getWidth(), getXAxisPositionPx());

        int ticksPerQuadrant = (int) (plane.getAxisSize(Axis.X) / plane.getStepSize(Axis.X));
        for (int tick = 0; tick < ticksPerQuadrant; tick++) {
            int xPxNegative = getYAxisPositionPx() - (tick * convert.toPx(plane.getStepSize(Axis.X), Axis.X));
            int xPxPositive = getYAxisPositionPx() + (tick * convert.toPx(plane.getStepSize(Axis.X), Axis.X));
            graphics.drawLine(xPxNegative, getXAxisPositionPx() - 5, xPxNegative, getXAxisPositionPx() + 5);
            graphics.drawLine(xPxPositive, getXAxisPositionPx() - 5, xPxPositive, getXAxisPositionPx() + 5);
        }
    }

    private void paintYAxis(Graphics graphics) {
        graphics.drawLine(getYAxisPositionPx(), 0, getYAxisPositionPx(), getHeight());

        int ticksPerQuadrant = (int) (plane.getAxisSize(Axis.Y) / plane.getStepSize(Axis.Y));
        for (int tick = 0; tick < ticksPerQuadrant; tick++) {
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
            double x = xPosToStep(xPx);
            double y = function.apply(x);
            drawPixel(graphics, xPx, yToPos(y));
        }
    }

    public static void present(CartesianPlane plane) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "CartesianJ");
        final var window = new JFrame();
        window.setJMenuBar(new JMenuBar());
        final var viewMenu = new JMenu("View");

        // Axis Size Settings
        final var increaseAxisSize = new JMenuItem("Increase Axis Size");
        increaseAxisSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseAxisSize.addActionListener(event -> {
            plane.setAxisSize(Axis.X, plane.getAxisSize(Axis.X) + 1);
            plane.setAxisSize(Axis.Y, plane.getAxisSize(Axis.Y) + 1);
        });
        viewMenu.add(increaseAxisSize);
        final var decreaseAxisSize = new JMenuItem("Decrease Axis Size");
        decreaseAxisSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseAxisSize.addActionListener(event -> {
            plane.setAxisSize(Axis.X, plane.getAxisSize(Axis.X) - 1);
            plane.setAxisSize(Axis.Y, plane.getAxisSize(Axis.Y) - 1);
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
        Dimension screenDimensions = window.getToolkit().getScreenSize();
        int windowSize = Math.min(screenDimensions.height, screenDimensions.width) / 2;
        window.setSize(windowSize, windowSize);
        window.getContentPane().add(new AwtCartesianPlane(plane));
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
