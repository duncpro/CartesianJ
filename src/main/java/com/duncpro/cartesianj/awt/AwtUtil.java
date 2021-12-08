package com.duncpro.cartesianj.awt;

import java.awt.*;

import static java.util.Objects.requireNonNull;

public class AwtUtil {
    public static void drawPixel(Graphics graphics, int x, int y) {
        requireNonNull(graphics);
        graphics.drawLine(x, y, x, y);
    }
}
