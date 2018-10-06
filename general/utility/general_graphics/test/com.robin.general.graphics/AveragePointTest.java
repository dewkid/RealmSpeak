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
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link AveragePoint}.
 */
public class AveragePointTest extends AbstractTest {

    private AveragePoint ap;
    private AveragePoint other;

    @Test
    public void basic() {
        title("basic");
        ap = new AveragePoint(1, 2);
        ap.addPoint(2, 1);
        print(ap);
        // note integer coordinates and integer division
        assertEquals("wrong point", new Point(1, 1), ap);
    }

    @Test
    public void squareFromCoords() {
        title("squareFromCoords");
        ap = new AveragePoint(100, 100);
        ap.addPoint(300, 100);
        ap.addPoint(300, 300);
        ap.addPoint(100, 300);
        print(ap);
        assertEquals("wrong point", new Point(200, 200), ap);
    }

    @Test
    public void squareFromPoints() {
        title("squareFromPoints");
        ap = new AveragePoint(new Point(100, 100));
        ap.addPoint(new Point(300, 100));
        ap.addPoint(new Point(300, 300));
        ap.addPoint(new Point(100, 300));
        print(ap);
        assertEquals("wrong point", new Point(200, 200), ap);
    }

    @Test
    public void polySquare() {
        title("polySquare");
        ap = new AveragePoint(new Point(10, 10));
        ap.addPoint(new Point(40, 10));
        ap.addPoint(new Point(40, 40));
        ap.addPoint(new Point(10, 40));
        print(ap);
        Polygon poly = ap.getPolygon();
        print(poly);
        assertEquals("wrong bounds", new Rectangle(10, 10, 30, 30),
                     poly.getBounds());
    }

    @Test
    public void equivalence() {
        title("equivalence");
        ap = new AveragePoint(new Point(10, 10));
        ap.addPoint(5, 20);
        ap.addPoint(0, 10);
        print(ap);

        other = new AveragePoint(new Point(0, 10));
        other.addPoint(new Point(5, 20));
        other.addPoint(new Point(10, 10));
        print(other);

        assertEquals("not equiv", true, ap.equals(other));
    }
}
