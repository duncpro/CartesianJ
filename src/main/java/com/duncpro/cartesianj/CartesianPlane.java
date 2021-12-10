package com.duncpro.cartesianj;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents a view of a Cartesian Plane. Can be used to plot functions as well as points.
 */
public class CartesianPlane {
    private final String title;

    public CartesianPlane(String title) {
        this.title = title;
    }

    public CartesianPlane() {
        this(null);
    }

    private final Set<Point> points = new CopyOnWriteArraySet<>();
    public void plot(Point point) {
        requireNonNull(point);
        points.add(point);
        changeListeners.forEach(Runnable::run);
    }
    public Set<Point> getPlottedPoints() {
        return Set.copyOf(points);
    }

    private final Map<String, Function<Double, Double>> functions = new HashMap<>();
    public void plot(String label, Function<Double, Double> f) {
        requireNonNull(f);
        requireNonNull(label);
        functions.put(label, f);
        changeListeners.forEach(Runnable::run);
    }
    public Map<String, Function<Double, Double>> getPlottedFunctions() {
        return Map.copyOf(functions);
    }

    private final Set<Runnable> changeListeners = new HashSet<>();
    public void addChangeListener(Runnable onChange) {
        changeListeners.add(onChange);
    }
    public void removeChangeListener(Runnable onChange) {
        changeListeners.remove(onChange);
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }
}
