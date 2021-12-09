package com.duncpro.cartesianj;

import com.duncpro.cartesianj.awt.AwtCartesianPlaneViewport;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;

import static com.duncpro.cartesianj.Direction.HORIZONTAL;
import static com.duncpro.cartesianj.Direction.VERTICAL;
import static com.duncpro.cartesianj.ViewportUtils.fitViewportToPoints;

public class CartesianJ {
    public static CartesianPlaneViewport present(CartesianPlane plane) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "CartesianJ");

        final var window = new JFrame();
        final var viewport = new AwtCartesianPlaneViewport(plane);
        window.getContentPane().add(viewport);
        Dimension screenDimensions = window.getToolkit().getScreenSize();
        int windowSize = Math.min(screenDimensions.height, screenDimensions.width) / 2;
        window.setSize(windowSize, windowSize);

        window.setJMenuBar(new JMenuBar());

        // Viewport Settings
        final var viewportMenu = new JMenu("Viewport");
        window.getJMenuBar().add(viewportMenu);
        final var moveViewportRight = new JMenuItem("Move Viewport Right");
        moveViewportRight.setAccelerator(KeyStroke.getKeyStroke("RIGHT"));
        moveViewportRight.addActionListener(event -> viewport.setOffset(HORIZONTAL, viewport.getOffset(HORIZONTAL) - 1));
        viewportMenu.add(moveViewportRight);

        final var moveViewportLeft = new JMenuItem("Move Viewport Left");
        moveViewportLeft.setAccelerator(KeyStroke.getKeyStroke("LEFT"));
        moveViewportLeft.addActionListener(event -> viewport.setOffset(HORIZONTAL, viewport.getOffset(HORIZONTAL) + 1));
        viewportMenu.add(moveViewportLeft);

        final var moveViewportUp = new JMenuItem("Move Viewport Up");
        moveViewportUp.setAccelerator(KeyStroke.getKeyStroke("UP"));
        moveViewportUp.addActionListener(event -> viewport.setOffset(VERTICAL, viewport.getOffset(VERTICAL) + 1));
        viewportMenu.add(moveViewportUp);

        final var moveViewportDown = new JMenuItem("Move Viewport Down");
        moveViewportDown.setAccelerator(KeyStroke.getKeyStroke("DOWN"));
        moveViewportDown.addActionListener(event -> viewport.setOffset(VERTICAL, viewport.getOffset(VERTICAL) - 1));
        viewportMenu.add(moveViewportDown);

        viewportMenu.add(new JSeparator());

        final var encapsulateDataPoints = new JMenuItem("Fit Points");
        encapsulateDataPoints.addActionListener(event -> fitViewportToPoints(viewport));
        viewportMenu.add(encapsulateDataPoints);

        // Step Settings
        final var stepMenu = new JMenu("Step");
        window.getJMenuBar().add(stepMenu);
        // Step Size (in units)
        final var increaseStepSize = new JMenuItem("Increase Step Size");
        increaseStepSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseStepSize.addActionListener(event -> viewport.incrementQuantitativeStepSize());
        stepMenu.add(increaseStepSize);
        final var decreaseStepSize = new JMenuItem("Decrease Step Size");
        decreaseStepSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseStepSize.addActionListener(event -> viewport.decrementQuantitativeStepSize());
        stepMenu.add(decreaseStepSize);
        stepMenu.add(new JSeparator());
        // Tick Frequency (measured in total pixels appearing between two consecutive ticks)
        final var increaseTickFrequency = new JMenuItem("Increase Tick Frequency");
        increaseTickFrequency.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increaseTickFrequency.addActionListener(event -> viewport.decrementVisualStepSize());
        stepMenu.add(increaseTickFrequency);
        final var decreaseTickFrequency = new JMenuItem("Decrease Tick Frequency");
        decreaseTickFrequency.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decreaseTickFrequency.addActionListener(event -> viewport.incrementVisualStepSize());
        stepMenu.add(decreaseTickFrequency);
        stepMenu.add(new JSeparator());

        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        return viewport;
    }
}
