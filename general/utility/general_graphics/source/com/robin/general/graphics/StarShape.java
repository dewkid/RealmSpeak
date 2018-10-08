/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.graphics;

import java.awt.*;

/**
 * A polygon in the shape of a star.
 */
public class StarShape extends Polygon {

    private static final double TOP_ANGLE = 270.0;
    private static final double FULL_ROTATION = 360.0;

    private final int cx;
    private final int cy;
    private final int np;
    private final int radius;

    /**
     * Creates a star shape with the given parameters. Note that the star is
     * aligned so that the first point is at the top.
     *
     * @param centerX x-coordinate of center
     * @param centerY y-coordinate of center
     * @param nPoints the number of points
     * @param radius  the radius
     */
    public StarShape(int centerX, int centerY, int nPoints, int radius) {
        this.cx = centerX;
        this.cy = centerY;
        this.np = nPoints;
        this.radius = radius;

        init(centerX, centerY, nPoints, radius);
    }

    private void init(int cx, int cy, int np, int radius) {
        Point center = new Point(cx, cy);
        double angle = TOP_ANGLE;
        Polar p = new Polar(cx, cy, radius, (int) angle);
        double angleSize = FULL_ROTATION / np;
        Point[] outerPoints = new Point[np];

        // compute all the outer (pointy) points...
        for (int i = 0; i < np; i++) {
            p.setAngle((int) angle);
            outerPoints[i] = p.getRect();
            angle += angleSize;
        }

        // compute all the inner points...
        for (int i = 0; i < outerPoints.length; i++) {
            addPoint(outerPoints[i].x, outerPoints[i].y);
            AveragePoint ap = new AveragePoint(center);
            // add the center again, to pull the inner points further in...
            ap.addPoint(center);
            // now average between this outer point and the next:
            ap.addPoint(outerPoints[i]);
            ap.addPoint(outerPoints[(i + 1) % outerPoints.length]);
            addPoint(ap.x, ap.y);
        }
    }

    @Override
    public String toString() {
        return "StarShape{<" + cx + "," + cy + "> " + np + ": " + radius + "}";
    }
}