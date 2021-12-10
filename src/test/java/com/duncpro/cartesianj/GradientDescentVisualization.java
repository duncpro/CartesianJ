package com.duncpro.cartesianj;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.duncpro.cartesianj.CartesianJ.present;
import static com.duncpro.cartesianj.MathUtil.round;
import static java.lang.Math.pow;

public class GradientDescentVisualization {
    static List<Point> readObservations() {
        final List<Point> observations;
        try (final var is = GradientDescentVisualization.class.getResourceAsStream("/ice_cream_sales.csv")) {
            observations = Arrays.stream(new String(is.readAllBytes()).split("\n"))
                    .map(line -> line.split(","))
                    .map(cols -> Arrays.stream(cols).map(Double::parseDouble).collect(Collectors.toList()))
                    .map(cols -> new Point(cols.get(0), cols.get(1)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return observations;
    }

    static Function<Double, Double> line(double slope, double yIntercept) {
        return (x) -> (x * slope) + yIntercept;
    }

    static double loss(Function<Double, Double> line, List<Point> observations) {
        return observations.stream()
                .map(point -> pow(point.getY() - line.apply(point.getX()), 2))
                .reduce(Double::sum)
                .orElseThrow();
    }

    static void visualizeGradientDescent(List<Point> observations, CartesianPlane dataPlot, CartesianPlaneViewport slopeLossPlot, CartesianPlaneViewport interceptLossPlot) {
        final var learningRate = 0.0001;

        final BiFunction<Double, Double, Double> betterIntercept = (m, b) ->
                observations.stream()
                        .map(p -> -2 * (p.getY() - ((m * p.getX()) + b)))
                        .reduce(Double::sum)
                        .map(ssr -> b - (ssr * learningRate))
                        .map(v -> round(v, 6))
                        .orElseThrow();

        final BiFunction<Double, Double, Double> betterSlope = (m, b) ->
                observations.stream()
                        .map(p -> -2 * p.getX() * (p.getY() - ((m * p.getX()) + b)))
                        .reduce(Double::sum)
                        .map(ssr -> m - (ssr * learningRate))
                        .map(v -> round(v, 6))
                        .orElseThrow();

        double idealIntercept = 0;
        double idealSlope = 0;
        double prevIntercept;
        double prevSlope;
        do {
            prevIntercept = idealIntercept;
            prevSlope = idealSlope;
            idealIntercept = betterIntercept.apply(prevSlope, prevIntercept);
            idealSlope = betterSlope.apply(prevSlope, idealIntercept);
            System.out.println("slope: " + idealSlope + ", intercept: " + idealIntercept);

            final var model = line(idealSlope, idealIntercept);
            dataPlot.plot("f", model);
            slopeLossPlot.plot(new Point(idealSlope, loss(model, observations)));
            interceptLossPlot.plot(new Point(idealIntercept, loss(model, observations)));
        } while (idealIntercept != prevIntercept || idealSlope != prevSlope);
    }

    public static void main(String[] args) {
        final var observations = readObservations();

        final var dataPlot = present(new CartesianPlane("Data"));
        final var slopeLossPlot = present(new CartesianPlane("Slope"));
        final var interceptLossPlot = present(new CartesianPlane("Y-Intercept"));

        observations.forEach(dataPlot::plot);
        dataPlot.fitData();

        visualizeGradientDescent(observations, dataPlot.getPlane(), slopeLossPlot, interceptLossPlot);
    }
}
