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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Base class for graphics unit tests.
 */
public class AbstractGraphicsTest extends AbstractTest {

    private static final String IMAGE_OUTPUT_DIR = "test/imgout";

    private static final int SQ_SIZE = 100;
    private static final int IMG_DIM = SQ_SIZE * 2;
    private static final int ICON_DIM = 32;

    private static final Color SMOKEY = new Color(0, 0, 0, 128);


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

    protected void fillImage(Graphics2D g2, Color background) {
        g2.setColor(background);
        g2.fillRect(0, 0, IMG_DIM, IMG_DIM);
    }

    private void addSquare(Graphics2D g2, int x, int y, Color c) {
        g2.setColor(c);
        g2.fillRect(x, y, SQ_SIZE, SQ_SIZE);
    }

    protected BufferedImage createBufferedImage() {
        return createBufferedImage(IMG_DIM);
    }

    protected BufferedImage createBufferedImage(int dim) {
        BufferedImage bi = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
        fillImage(bi.createGraphics(), Color.BLACK);
        return bi;
    }

    /**
     * Creates a 200x200 test image.
     *
     * @return the test image
     */
    protected ImageIcon testImage() {
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        addSquare(g2, 0, 0, Color.RED);
        addSquare(g2, SQ_SIZE, 0, Color.GREEN);
        addSquare(g2, 0, SQ_SIZE, Color.BLUE);
        addSquare(g2, SQ_SIZE, SQ_SIZE, Color.YELLOW);
        addSquare(g2, SQ_SIZE / 2, SQ_SIZE / 2, SMOKEY);
        return new ImageIcon(bi);
    }

    /**
     * Creates a 32x32 test icon.
     *
     * @return the test image
     */
    protected ImageIcon testIcon() {
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        addSquare(g2, 0, 0, Color.RED);
        addSquare(g2, 10, 10, Color.GREEN);
        addSquare(g2, 20, 20, Color.BLUE);
        return new ImageIcon(bi);
    }

    protected ImageIcon testBigIcon() {
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(Color.ORANGE);
        g2.fillRect(20, 20, 160, 160);
        return new ImageIcon(bi);
    }

    protected ImageIcon testSmallIcon() {
        BufferedImage bi = createBufferedImage(50);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(new Color(0x00, 0x88, 0xcc));
        g2.fillRect(5, 5, 40, 40);
        return new ImageIcon(bi);
    }

    /**
     * Returns a file instance destined for the (test) image output directory.
     *
     * @param name the name for the file
     * @return the file instance
     */
    protected File outputFile(String name) {
        return new File(IMAGE_OUTPUT_DIR, name);
    }

}
