package com.duncpro.cartesianj;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.duncpro.cartesianj.awt.AwtCartesianPlane.present;

public class GradientDescentVisualization {
    public static class IceCreamDataPoint {
        public final double totalSales;
        public final double temperature;

        public IceCreamDataPoint(double totalSales, double temperature) {
            this.totalSales = totalSales;
            this.temperature = temperature;
        }
    }

    public static void main(String[] args) throws IOException {
        final List<IceCreamDataPoint> observations;
        try (final var is = GradientDescentVisualization.class.getResourceAsStream("/ice_cream_sales.csv")) {
            observations = Arrays.stream(new String(is.readAllBytes()).split("\n"))
                    .map(line -> line.split(","))
                    .map(cols -> Arrays.stream(cols).map(Double::parseDouble).collect(Collectors.toList()))
                    .map(cols -> new IceCreamDataPoint(cols.get(1), cols.get(0)))
                    .collect(Collectors.toList());
        }

        final var plane = new CartesianPlane();
        observations.forEach(dp -> plane.plot(new LabeledPoint(dp.temperature, dp.totalSales, null)));
        present(plane);
    }
}
