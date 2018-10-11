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

import java.text.NumberFormat;

/**
 * Represents a Force Vector. Each instance has a force (magnitude) and
 * direction (0-360 degrees). Note that the coordinate space has
 * X increasing left-to-right, and Y increasing top-to-bottom:
 * <pre>
 *               90-deg
 *                 | -Y
 *                 |
 *                 |
 *                 |(0,0)
 * 180-deg  -------+------->  0-deg
 *          -X     |      +X
 *                 |
 *                 |
 *                 V +Y
 *              270-deg
 * </pre>
 *
 * @deprecated Does not appear to be used anywhere in the codebase
 */
@Deprecated
public class ForceVector {

    private static final double DEGREES_0 = 0.0;
    private static final double DEGREES_360 = 360.0;

    private static final NumberFormat NF_2DP = NumberFormat.getInstance();

    static {
        NF_2DP.setMinimumFractionDigits(2);
        NF_2DP.setMaximumFractionDigits(2);
    }

    private double force;
    private double direction;
    private double dx;
    private double dy;

    /**
     * Creates a force vector with the given force and direction.
     * <p>
     * Force is an arbitrary value that you choose.
     * Direction is in degrees from 0-360, where 0 is pointing to the right,
     * and 90 is pointing straight up.
     *
     * @param force     the force magnitude
     * @param direction direction in degrees (0-360)
     */
    public ForceVector(double force, double direction) {
        this.force = force;
        this.direction = direction;
        initRectangularCoordinates();
    }

    /**
     * This constructor is used internally, and should NOT be used by the
     * application.  No validation is done here, so the values are assumed
     * to be accurate.
     *
     * @param force     the force magnitude
     * @param direction direction in degrees (0-360)
     * @param dx        delta x
     * @param dy        delta y
     */
    private ForceVector(double force, double direction, double dx, double dy) {
        this.force = force;
        this.direction = direction;
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Adds the given force vector to this force vector, returning the result.
     *
     * @param fv the force vector to add to this one.
     * @return force vector representing the sum of this and the other vectors
     */
    public ForceVector add(ForceVector fv) {
        // Get the final position, by simply adding the two rectangular coordinates
        // We can do this, because ForceVectors all have the same origin: (0,0)
        double fx = dx + fv.dx;
        double fy = dy + fv.dy;

        // Now we simply need to identify the new force and direction
        double fForce = Math.sqrt((fx * fx) + (fy * fy));
        double fDirection = toDegrees(Math.atan2(fy, fx));

        return new ForceVector(fForce, fDirection, fx, fy);
    }

    // makes sure the angle is between 0 and 360
    private void normalizeAngle() {
        while (direction < DEGREES_0) {
            direction += DEGREES_360;
        }
        while (direction >= DEGREES_360) {
            direction -= DEGREES_360;
        }
    }

    // compute x,y coordinates of vector (assuming 0,0 origin)
    private void initRectangularCoordinates() {
        normalizeAngle();
        double radians = toRadians(direction);
        dx = force * Math.cos(radians);
        dy = force * Math.sin(radians);
    }


    @Override
    public String toString() {
        return "p(" + NF_2DP.format(force) + "," + NF_2DP.format(direction) +
                "o)==r(" + NF_2DP.format(dx) + "," + NF_2DP.format(dy) + ")";
    }

    /**
     * Returns the force (magnitude) of this force vector.
     *
     * @return the force
     */
    public double getForce() {
        return force;
    }

    /**
     * Returns the direction of this force vector, in degrees (0-360).
     *
     * @return the direction
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Returns the x component of this force vector.
     *
     * @return the x component
     */
    public double getDx() {
        return dx;
    }

    /**
     * Returns the y component of this force vector.
     *
     * @return the y component
     */
    public double getDy() {
        return dy;
    }

    /**
     * Converts the given radians to degrees.
     *
     * @param rad radians
     * @return value in degrees
     */
    public static double toDegrees(double rad) {
        // The minus sign flips the angle so
        // that 90 degrees is pointing straight up
        return -(rad * 180) / Math.PI;
    }

    /**
     * Converts the given degrees to radians.
     *
     * @param deg degrees
     * @return value in radians
     */
    public static double toRadians(double deg) {
        // The minus sign flips the angle so
        // that 90 degrees is pointing straight up
        return -(Math.PI * deg) / 180.0;
    }


    // TODO: remove, in deference to unit tests
    @Deprecated
    public static void main(String[] args) {
        System.out.println(new ForceVector(1.0, 0.0));
        System.out.println(new ForceVector(1.0, 90.0));
        System.out.println(new ForceVector(1.0, 180.0));
        System.out.println(new ForceVector(1.0, 270.0));

        ForceVector fv1 = new ForceVector(1.0, 0.0);
        ForceVector fv2 = new ForceVector(1.0, 90.0);
        System.out.println(fv1.add(fv2));
    }
}