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
import java.awt.geom.Point2D;

import static com.robin.general.graphics.GraphicsUtil.radians_degrees60;

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
    public void saveImageToPng() {
        title("saveImageToPng");
        GraphicsUtil.saveImageToPNG(testImage(), outputFile("testPNG.png"));
    }

    @Test
    public void saveImageToJpg() {
        title("saveImageToJpg");
        GraphicsUtil.saveImageToJPG(testImage(), outputFile("testJPG.jpg"));
        // TODO: This seems to be broken (black 200x200)
    }

    @Test
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

}
