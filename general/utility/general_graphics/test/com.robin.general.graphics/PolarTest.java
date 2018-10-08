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
        assertThat(polar.getX(), is(0));
        assertThat(polar.getY(), is(0));
    }
}
