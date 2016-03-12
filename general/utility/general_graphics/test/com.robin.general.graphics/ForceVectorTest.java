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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ForceVector}.
 */
public class ForceVectorTest extends AbstractTest {

    private static final double ROOT_2 = Math.sqrt(2.0);
    private static final double HALF_ROOT_2 = ROOT_2 / 2.0;

    private ForceVector v;

    private void checkVector(ForceVector v, double expF, double expD,
                        double expX, double expY) {
        print(v);
        assertEquals("bad force", expF, v.getForce(), TOLERANCE);
        assertEquals("bad direction", expD, v.getDirection(), TOLERANCE);
        assertEquals("bad dx", expX, v.getDx(), TOLERANCE);
        assertEquals("bad dy", expY, v.getDy(), TOLERANCE);
    }

    @Test
    public void unitVectors() {
        checkVector(new ForceVector(1.0, 0.0),   1.0,   0.0,  1.0,  0.0);
        checkVector(new ForceVector(1.0, 90.0),  1.0,  90.0,  0.0, -1.0);
        checkVector(new ForceVector(1.0, 180.0), 1.0, 180.0, -1.0,  0.0);
        checkVector(new ForceVector(1.0, 270.0), 1.0, 270.0,  0.0,  1.0);
    }

    @Test
    public void addTwoUnitVectors() {
        ForceVector a = new ForceVector(1.0, 0.0);  // pointing right (+X)
        ForceVector b = new ForceVector(1.0, 90.0); // pointing up (-Y)
        checkVector(a.add(b), ROOT_2, 45.0, 1.0, -1.0);
    }

    @Test
    public void normalizeSmall() {
        v = new ForceVector(1.0, -45.0);    // 315 deg
        checkVector(v, 1.0, 315.0, HALF_ROOT_2, HALF_ROOT_2);
    }

    @Test
    public void normalizeLarge() {
        v = new ForceVector(1.0, 765.0);    // 45 deg
        checkVector(v, 1.0, 45.0, HALF_ROOT_2, -HALF_ROOT_2);
    }

}
