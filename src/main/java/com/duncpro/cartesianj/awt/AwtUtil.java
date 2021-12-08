package com.duncpro.cartesianj.awt;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class AwtUtil {
    public static void drawPixel(Graphics graphics, int x, int y) {
        requireNonNull(graphics);
        for (int xo = -1; xo <= 1; xo++) {
            for (int yo = -1; yo <= 1; yo++) {
                graphics.drawLine(x + xo, y + yo, x + xo, y + yo);
            }
        }
    }

    public static Function<Integer, Color> generalPurposeColorMap() {
        final var colors = Stream.of(Color.RED, Color.BLUE, Color.ORANGE, Color.YELLOW,
                Color.CYAN, Color.MAGENTA, Color.BLACK, Color.GREEN)
                .collect(Collectors.toList());
        return i -> colors.get(i % colors.size());
    }
}
