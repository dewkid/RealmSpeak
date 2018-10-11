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

/**
 * New coordinate class for handling polar coordinates, with easy conversions
 * to rectangular coordinate systems.
 * <p>
 * Note that instances of this class are mutable.
 *
 * @author Robin Warren
 */
public class Polar {
    private static final int MAX_DEGREES = 360;

    // lookup tables
    private static final double cos[] = new double[MAX_DEGREES];
    private static final double sin[] = new double[MAX_DEGREES];

    static {
        for (int theta = 0; theta < MAX_DEGREES; theta++) {
            cos[theta] = Math.cos(toRadians(theta));
            sin[theta] = Math.sin(toRadians(theta));
        }
    }

    private int length;
    private int angle;
    private Point origin;
    private Point rect;


    /**
     * Creates a default polar instance; length 0, angle 0, at the origin.
     */
    public Polar() {
        length = 0;
        angle = 0;
        origin = new Point(0, 0);
        updateRectPoint();
    }

    /**
     * Creates a polar instance with origin at x, y, with length r and
     * angle theta (degrees).
     *
     * @param x     x-coordinate of origin
     * @param y     y-coordinate of origin
     * @param r     radius (length)
     * @param theta angle (in degrees)
     */
    public Polar(int x, int y, int r, int theta) {
        length = r;
        angle = theta;
        normalizeAngle();
        origin = new Point(x, y);
        updateRectPoint();
    }

    /**
     * Copy constructor.
     *
     * @param p polar instance to copy
     */
    public Polar(Polar p) {
        length = p.length;
        angle = p.angle;
        origin = new Point(p.origin);
        rect = new Point(p.getRect());
    }

    /**
     * Creates a polar instance with origin at the specified center, and
     * length/angle computed for the given point, relative to that center.
     *
     * @param center the origin
     * @param coord  the end point of the polar instance
     */
    public Polar(Point center, Point coord) {
        int dx = coord.x - center.x;
        int dy = coord.y - center.y;
        if (dx == 0 && dy == 0) {
            angle = 0;
            length = 0;
        } else {
            length = (int) Math.sqrt((dx * dx) + (dy * dy));
            angle = toDegrees(Math.atan2((double) dy, (double) dx));
        }
        origin = center;
        updateRectPoint();
    }

    /**
     * Ensures the angle is within the range 0..359.
     */
    private void normalizeAngle() {
        angle = normalAngle(angle);
    }

    /**
     * Computes and caches the rectangular coordinates for this polar instance.
     */
    private void updateRectPoint() {
        rect = computeRectPoint();
    }

    /**
     * Computes the rectangular coordinates for this polar instance.
     *
     * @return the point representing the rectangular coordinates for this
     * polar instance
     */
    private Point computeRectPoint() {
        normalizeAngle();
        int x = ((int) ((double) length * cos[angle])) + origin.x;
        int y = ((int) ((double) length * sin[angle])) + origin.y;
        return new Point(x, y);
    }


    /**
     * Modifies this polar instance to rotate the angle by 180 degrees.
     */
    // Not used within the codebase
    @Deprecated
    public void flip() {
        addAngle(180);
    }

    /**
     * Sets the length of this polar instance.
     *
     * @param val the length value
     */
    public void setLength(int val) {
        length = val;
        rect = null;
    }

    /**
     * Sets the angle (degrees) of this polar instance.
     *
     * @param val the angle value
     */
    public void setAngle(int val) {
        angle = val;
        normalizeAngle();
        rect = null;
    }

    /**
     * Adds the given angle to this polar instance.
     *
     * @param val the angle to add
     */
    public void addAngle(int val) {
        angle += val;
        normalizeAngle();
        rect = null;
    }

    /**
     * Sets the origin of this polar instance.
     *
     * @param o the new origin
     */
    // Not used in the codebase
    @Deprecated
    public void setOrigin(Point o) {
        origin = o;
        rect = null;
    }

    /**
     * Sets the origin of this polar instance.
     *
     * @param x origin x-coordinate
     * @param y origin y-coordinate
     */
    // Not used in the codebase
    @Deprecated
    public void setOrigin(int x, int y) {
        origin = new Point(x, y);
        rect = null;
    }

    /**
     * Returns the length of this polar instance.
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the angle (degrees) of this polar instance.
     *
     * @return the angle in degrees
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Returns the origin of this polar instance.
     *
     * @return the origin
     */
    public Point getOrigin() {
        return origin;
    }

    /**
     * Returns the x-coordinate of the origin of this polar instance.
     *
     * @return the x-coordinate of the origin
     */
    // Not used in the codebase
    @Deprecated
    public int getOriginX() {
        return origin.x;
    }

    /**
     * Returns the y-coordinate of the origin of this polar instance.
     *
     * @return the y-coordinate of the origin
     */
    // Not used in the codebase
    @Deprecated
    public int getOriginY() {
        return origin.y;
    }

    /**
     * Returns the (rectangular) x-coordinate of this polar instance.
     *
     * @return the x-coordinate
     */
    public int getX() {
        if (rect == null) {
            updateRectPoint();
        }
        return rect.x;
    }

    /**
     * Returns the (rectangular) y-coordinate of this polar instance.
     *
     * @return the y-coordinate
     */
    public int getY() {
        if (rect == null) {
            updateRectPoint();
        }
        return rect.y;
    }

    /**
     * Returns the point representing the rectangular coordinates of this
     * polar instance.
     *
     * @return the rectangular coordinates
     */
    public Point getRect() {
        if (rect == null) {
            updateRectPoint();
        }
        return rect;
    }

    @Override
    public String toString() {
        return "Polar(" + length + ",<" + angle + ")";
    }

    /**
     * Returns equivalent angle in the range 0..359.
     *
     * @param theta the angle
     * @return normalized angle
     */
    private static int normalAngle(int theta) {
        while (theta < 0) {
            theta += 360;
        }
        return theta % 360;
    }

    /**
     * Converts the given degrees into radians.
     *
     * @param deg degrees
     * @return angle expressed as radians
     */
    private static double toRadians(int deg) {
        return (Math.PI * (double) deg) / 180;
    }

    /**
     * Converts the given radians into degrees.
     *
     * @param rad radians
     * @return angle expressed as degrees
     */
    private static int toDegrees(double rad) {
        return normalAngle((int) ((rad * 180) / Math.PI));
    }

    /**
     * Returns a polar instance that represents a point along the line between
     * from and to, where 0% is from, and 100% is to.
     *
     * @param from    the start point
     * @param to      the end point
     * @param percent the percentage along the line
     * @return a polar instance at the computed point
     */
    // Not used within the codebase
    @Deprecated
    public static Polar translate(Polar from, Polar to, int percent) {
        percent = percent < 0 ? 0 : percent;
        percent = percent > 100 ? 100 : percent;

        if (percent == 0) {
            return from;
        } else if (percent == 100) {
            return to;
        } else {
            if (from != null && to != null) {
                Point fromR = from.getRect();
                Point toR = to.getRect();
                int dx = toR.x - fromR.x;
                int dy = toR.y - fromR.y;

                int currentX = fromR.x + ((dx * percent) / 100);
                int currentY = fromR.y + ((dy * percent) / 100);

                return new Polar(currentX, currentY, 0, 0);
            }
        }
        return null;
    }

    /**
     * Returns a polar instance representing the mid point between the
     * given instances.
     *
     * @param from the first polar instance
     * @param to   the second polar instance
     * @return a polar instance representing the midpoint between the two
     */
    // Not used within the codebase
    @Deprecated
    public static Polar midpoint(Polar from, Polar to) {
        if (from != null && to != null) {
            Point fromR = from.getRect();
            Point toR = to.getRect();
            int currentX = (fromR.x + toR.x) / 2;
            int currentY = (fromR.y + toR.y) / 2;

            return new Polar(currentX, currentY, 0, 0);
        }
        return null;
    }

}
