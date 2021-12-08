package com.duncpro.cartesianj;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.duncpro.cartesianj.CartesianJ.present;
import static com.duncpro.cartesianj.ViewportUtils.fitAllPoints;
import static java.lang.Math.pow;

public class GradientDescentVisualization {
    public static class IceCreamDataPoint {
        public final double totalSales;
        public final double temperature;

        public IceCreamDataPoint(double totalSales, double temperature) {
            this.totalSales = totalSales;
            this.temperature = temperature;
        }
    }

    public static double loss(Function<Double, Double> model, List<IceCreamDataPoint> observed) {
        double sumOfSquaredResiduals = 0;
        for (IceCreamDataPoint dp : observed) {
            sumOfSquaredResiduals += pow(model.apply(dp.temperature) - dp.totalSales, 2);
        }
        return sumOfSquaredResiduals;
    }

    public static void presentLossFunction() {
        final var plane = new CartesianPlane();
        plane.plot("loss", (yIntercept) -> loss(x -> x * 0.64 + yIntercept, List.of(new IceCreamDataPoint(1, 3))));
        present(plane);
    }

    public static List<IceCreamDataPoint> readObservations() {
        final List<IceCreamDataPoint> observations;
        try (final var is = GradientDescentVisualization.class.getResourceAsStream("/ice_cream_sales.csv")) {
            observations = Arrays.stream(new String(is.readAllBytes()).split("\n"))
                    .map(line -> line.split(","))
                    .map(cols -> Arrays.stream(cols).map(Double::parseDouble).collect(Collectors.toList()))
                    .map(cols -> new IceCreamDataPoint(cols.get(1), cols.get(0)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return observations;
    }

    public static void main(String[] args) throws IOException {
        final var plane = new CartesianPlane();
        readObservations().forEach(dp -> plane.plot(new Point(dp.temperature, dp.totalSales)));
        final var viewport = present(plane);
        fitAllPoints(viewport);
        presentLossFunction();
    }
}
