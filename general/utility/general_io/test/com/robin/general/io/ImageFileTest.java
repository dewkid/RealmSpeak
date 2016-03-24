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

package com.robin.general.io;

import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Unit tests for {@link ImageFile}.
 */
public class ImageFileTest extends AbstractFileTest {

    private static final String IMAGE_FILE_TEST_ROOT = "test/testres/image_file";
    private static final int DIM = 100;
    private static final int HALF = DIM / 2;
    private static final int QUART = HALF / 2;

    private static BufferedImage testImage;

    @BeforeClass
    public static void beforeClass() {
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
        pwd = pwd + IMAGE_FILE_TEST_ROOT;
        print("PWD now set to >%s<", pwd);
        testImage = createTestImage();
    }

    private static BufferedImage createTestImage() {
        BufferedImage bi = new BufferedImage(DIM, DIM,
                BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setColor(Color.RED);
        g2.fill(new Rectangle(HALF, HALF, HALF, HALF));
        g2.setColor(Color.YELLOW);
        g2.fill(new Rectangle(QUART, QUART, HALF, HALF));
        g2.dispose();
        return bi;
    }

    @Test
    public void basic() {
        File out;
        for (int i = 0; i <= 4; i++) {
            float quality = 0.25f * i;
            String name = String.format("basic_test_%d.jpg", i);
            out = new File(pwd, name);
            ImageFile.saveJpeg(testImage, out, quality);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void badQualityHigh() {
        File out = new File(pwd, "bad_image.jpg");
        ImageFile.saveJpeg(testImage, out, 1.1f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badQualityLow() {
        File out = new File(pwd, "bad_image.jpg");
        ImageFile.saveJpeg(testImage, out, -0.1f);
    }

    // TODO: add tests for null parameters
}
