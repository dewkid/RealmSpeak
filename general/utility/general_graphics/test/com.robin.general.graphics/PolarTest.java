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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link Polar}.
 */
public class PolarTest extends AbstractTest {

    private static Point p(int x, int y) {
        return new Point(x, y);
    }

    private Polar polar;

    @Test
    public void basic() {
        title("Basic");
        polar = new Polar();
        print(polar);
        assertThat(polar.getLength(), is(0));
        assertThat(polar.getAngle(), is(0));
        assertThat(polar.getOrigin(), is(p(0, 0)));
        assertThat(polar.getRect(), is(p(0, 0)));
        assertThat(polar.getX(), is(0));
        assertThat(polar.getY(), is(0));
    }

    @Test
    public void constructOne() {
        title("Construct one");
        polar = new Polar(10, 10, 50, 30);
        print(polar);
        assertThat(polar.getLength(), is(50));
        assertThat(polar.getAngle(), is(30));
        assertThat(polar.getOrigin(), is(p(10, 10)));
        assertThat(polar.getRect(), is(p(53, 34)));
        assertThat(polar.getX(), is(53));
        assertThat(polar.getY(), is(34));
    }

    @Test
    public void copyConstruct() {
        title("Copy Construct");
        polar = new Polar(0, 0, 100, 180);
        Polar copy = new Polar(polar);
        print(copy);
        assertThat(copy.getLength(), is(polar.getLength()));
        assertThat(copy.getAngle(), is(polar.getAngle()));
        assertThat(copy.getOrigin(), is(polar.getOrigin()));
        assertThat(copy.getRect(), is(polar.getRect()));
    }

    @Test
    public void constructFromCenterAndPointOne() {
        title("Construct from center and point: 1");
        polar = new Polar(p(-5, -5), p(25, 35));
        print(polar);
        assertThat(polar.getLength(), is(50));
        assertThat(polar.getAngle(), is(53));
    }

    @Test
    public void constructFromCenterAndPointZero() {
        title("Construct from center and point: 0");
        polar = new Polar(p(1, 2), p(1, 2));
        print(polar);
        assertThat(polar.getLength(), is(0));
        assertThat(polar.getAngle(), is(0));
    }

    @Test
    public void angleOverage() {
        title("Angle overage");
        polar = new Polar(0, 0, 100, 375);
        print(polar);
        assertThat(polar.getAngle(), is(15));
    }

    @Test
    public void angleUnderage() {
        title("Angle underage");
        polar = new Polar(0, 0, 100, -340);
        print(polar);
        assertThat(polar.getAngle(), is(20));
    }

    // No unit tests for translate() -- deprecated
    // No unit tests for midpoint() -- deprecated
    // No unit tests for flip() -- deprecated

    @Test
    public void polarSetLength() {
        title("polar set length");
        polar = new Polar();
        print(polar);
        assertThat(polar.getLength(), is(0));
        assertThat(polar.getRect(), is(p(0, 0)));

        polar.setLength(12);
        print(polar);
        assertThat(polar.getLength(), is(12));
        assertThat(polar.getRect(), is(p(12, 0)));
    }

    @Test
    public void polarSetAngle() {
        title("polar set angle");
        polar = new Polar();
        assertThat(polar.getAngle(), is(0));
        polar.setAngle(90);
        assertThat(polar.getAngle(), is(90));
        polar.setAngle(370);
        assertThat(polar.getAngle(), is(10));
    }

    @Test
    public void polarAddAngle() {
        title("polar add angle");
        polar = new Polar();
        assertThat(polar.getAngle(), is(0));
        polar.addAngle(60);
        assertThat(polar.getAngle(), is(60));
        polar.addAngle(-20);
        assertThat(polar.getAngle(), is(40));
        polar.addAngle(180);
        assertThat(polar.getAngle(), is(220));
        polar.addAngle(155);
        assertThat(polar.getAngle(), is(15));
    }

    // no unit tests for setOrigin(Point) -- deprecated
    // no unit tests for setOrigin(int,int) -- deprecated

}
