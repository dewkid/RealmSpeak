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

import com.robin.general.util.AbstractTest;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;

/**
 * Base class for graphics unit tests.
 */
public class AbstractGraphicsTest extends AbstractTest {

    /**
     * Returns a double precision point.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the point
     */
    protected Point2D pointDouble(double x, double y) {
        return new Point2D.Double(x, y);
    }

    /**
     * Asserts that the two given points are equal. That is to say, the
     * difference between each of the expected and actual coordinates is
     * less than {@link #TOLERANCE}.
     *
     * @param msg failure message
     * @param exp expected point
     * @param act actual point
     */
    protected void assertEqualPoint(String msg, Point2D exp, Point2D act) {
        double expX = exp.getX();
        double expY = exp.getY();
        double actX = act.getX();
        double actY = act.getY();
        assertEquals(msg + "[X]", expX, actX, TOLERANCE);
        assertEquals(msg + "[Y]", expY, actY, TOLERANCE);
    }
}
