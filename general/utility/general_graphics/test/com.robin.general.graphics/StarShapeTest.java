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

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import static com.robin.general.graphics.GraphicsUtil.saveImageToPNG;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Unit tests for {@link StarShape}.
 */
public class StarShapeTest extends AbstractGraphicsTest {

    private Collection<Point> polyPoints(Polygon poly) {
        Collection<Point> points = new ArrayList<>(poly.npoints);
        for (int i = 0; i < poly.npoints; i++) {
            points.add(p(poly.xpoints[i], poly.ypoints[i]));
        }
        return points;
    }

    @Test
    public void basic() {
        title("Basic");
        Polygon star = new StarShape(0, 0, 4, 10);
        print(star);

        Collection<Point> points = polyPoints(star);
        // 4 outer points, and 4 inner points...
        assertThat(points.size(), is(8));
        assertThat(points, hasItems(p(0, 10), p(10, 0), p(0, -10), p(-10, 0)));
    }

    @Test
    public void sevenStar() {
        title("Seven star");
        Polygon star = new StarShape(100, 100, 7, 50);
        print(star);

        Collection<Point> points = polyPoints(star);
        assertThat(points.size(), is(14));
        assertThat(points, hasItems(p(100, 50), p(109, 79), p(138, 69)));

        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.fill(star);

        g2.setColor(Color.YELLOW);
        star = new StarShape(12, 12, 5, 7);
        g2.fill(star);

        saveImageToPNG(new ImageIcon(bi), outputFile("testStar7.png"));
    }
}
