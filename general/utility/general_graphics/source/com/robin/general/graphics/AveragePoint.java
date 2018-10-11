/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2016 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.graphics;

import java.awt.*;
import java.util.ArrayList;

/**
 * Represents the center point of all the points in its collection.
 */
public class AveragePoint extends Point {

    /**
     * Points in this collection.
     */
    private final java.util.List<Point> points = new ArrayList<>();

    /**
     * Creates an average point instance with a single starting point.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public AveragePoint(int x, int y) {
        super(x, y);
        addPoint(x, y);
    }

    /**
     * Creates an average point instance withe a single starting point.
     *
     * @param p the starting point
     */
    public AveragePoint(Point p) {
        super(p);
        addPoint(p);
    }

    /**
     * Adds a point to this collection.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void addPoint(int x, int y) {
        addPoint(new Point(x, y));
    }

    /**
     * Adds a point to this collection.
     *
     * @param p the point to add
     */
    public void addPoint(Point p) {
        points.add(p);
        refresh();
    }

    /**
     * Returns a polygon composed of the points in this collection, in the
     * order that the points were originally added.
     *
     * @return a polygon for these points
     */
    public Polygon getPolygon() {
        Polygon poly = new Polygon();
        for (Point p : points) {
            poly.addPoint(p.x, p.y);
        }
        return poly;
    }

    /**
     * Updates the location of this average point based on the current
     * collection of points, resulting in the "center" point.
     */
    private void refresh() {
        int tx = 0;
        int ty = 0;
        int n = 0;
        for (Point p : points) {
            tx += p.x;
            ty += p.y;
            n++;
        }
        this.x = tx / n;
        this.y = ty / n;
    }

    /**
     * Returns true if this average point has the same coordinates as
     * the specified point.
     *
     * @param p point to compare with
     * @return true if this point is equivalent
     */
    public boolean sameAs(Point p) {
        return (p.x == x && p.y == y);
    }
}