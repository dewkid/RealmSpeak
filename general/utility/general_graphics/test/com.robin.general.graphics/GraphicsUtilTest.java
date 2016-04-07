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

import java.awt.geom.Point2D;

import static com.robin.general.graphics.GraphicsUtil.radians_degrees60;
import static com.robin.general.graphics.GraphicsUtil.rotate;

/**
 * Unit tests for {@link GraphicsUtil}.
 */
public class GraphicsUtilTest extends AbstractGraphicsTest {

    @Test
    public void basic() {
        title("basic");
        Point2D start = pointDouble(5, 1);
        Point2D center = pointDouble(5, 5);
        Point2D rotated = rotate(start, center, radians_degrees60 * 1);
        print(rotated);
        assertEqualPoint("bad rotation", pointDouble(8.464101615, 3), rotated);
    }

}
