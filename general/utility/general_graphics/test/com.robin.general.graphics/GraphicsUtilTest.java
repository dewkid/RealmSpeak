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

import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

import static com.robin.general.graphics.GraphicsUtil.radians_degrees60;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link GraphicsUtil}.
 */
public class GraphicsUtilTest extends AbstractGraphicsTest {

    @Test
    public void basic() {
        title("basic");
        Point2D start = pointDouble(5, 1);
        Point2D center = pointDouble(5, 5);
        Point2D rotated = GraphicsUtil.rotate(start, center, radians_degrees60 * 1);
        print(rotated);
        assertEqualPoint("bad rotation", pointDouble(8.464101615, 3), rotated);
    }

    @Test
    @Ignore("Slow test")
    public void saveImageToPng() {
        title("saveImageToPng");
        GraphicsUtil.saveImageToPNG(testImage(), outputFile("testPNG.png"));
    }

    @Test
    @Ignore("Slow test")
    public void saveImageToJpg() {
        title("saveImageToJpg");
        GraphicsUtil.saveImageToJPG(testImage(), outputFile("testJPG.jpg"));
        // TODO: This seems to be broken (black 200x200)
    }

    @Test
    @Ignore("Slow test")
    public void componentToIcon() {
        title("componentToIcon");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JButton("My Action Button", testIcon()));
        panel.add(Box.createVerticalStrut(12));
        panel.add(new JRadioButton("My Radio Button"));

        // stuff the component in a frame, so it gets painted...
        JFrame frame = new JFrame("Test frame");
        frame.getContentPane().add(panel);
        frame.pack();

        ImageIcon comp = GraphicsUtil.componentToIcon(panel);
        GraphicsUtil.saveImageToPNG(comp, outputFile("testComponent.png"));
    }


    private static final Color RED = new Color(0xff, 0x00, 0x00);
    private static final Color BLUE = new Color(0x00, 0x00, 0xff);
    private static final Color BLACK = new Color(0x00, 0x00, 0x00);
    private static final Color WHITE = new Color(0xff, 0xff, 0xff);

    @Test
    public void convertColorZeroPercent() {
        title("convert color 0 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, 0);
        print("RED/BLUE/0 is %s", result);
        assertThat(result, is(RED));
    }

    @Test
    public void convertColorNegPercent() {
        title("convert color -5 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, -5);
        print("RED/BLUE/-5 is %s", result);
        assertThat(result, is(RED));
    }

    @Test
    public void convertColorHundredPercent() {
        title("convert color 100 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, 100);
        print("RED/BLUE/100 is %s", result);
        assertThat(result, is(BLUE));
    }

    @Test
    public void convertColorOverPercent() {
        title("convert color 150 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, 150);
        print("RED/BLUE/150 is %s", result);
        assertThat(result, is(BLUE));
    }

    @Test
    public void convertColor50Percent() {
        title("convert color 50 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, 50);
        print("RED/BLUE/50 is %s", result);
        assertThat(result, is(new Color(0x7f, 0, 0x7f)));
    }

    @Test
    public void convertColor25Percent() {
        title("convert color 25 %");
        Color result = GraphicsUtil.convertColor(BLACK, WHITE, 25);
        print("BLACK/WHITE/25 is %s", result);
        assertThat(result, is(new Color(0x3f, 0x3f, 0x3f)));
    }

    @Test
    public void convertColor75Percent() {
        title("convert color 75 %");
        Color result = GraphicsUtil.convertColor(BLACK, WHITE, 75);
        print("BLACK/WHITE/75 is %s", result);
        assertThat(result, is(new Color(0xbf, 0xbf, 0xbf)));
    }
}
