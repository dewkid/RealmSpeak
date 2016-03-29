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

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ImageZip}.
 */
public class ImageZipTest extends AbstractFileTest {

    private static final String IMAGE_ZIP_TEST_ROOT = "test/testres/image_zip/";
    private static final int IMAGE_COUNT = 10;
    private static final int FRAME_SIZE = 120;
    private static final int ORIGIN = 200;
    private static final int OFFSET_X = FRAME_SIZE / 2;
    private static final int OFFSET_Y = OFFSET_X / 2;
    private static final long PAUSE_TO_ADMIRE = 4000;

    private static String norahPath;
    private static String chrisPath;
    private static String testZipPath;

    @BeforeClass
    public static void beforeClass() {
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
        pwd = pwd + IMAGE_ZIP_TEST_ROOT;
        print("PWD now set to >%s<", pwd);
        norahPath = pwd + "norah.png";
        chrisPath = pwd + "chris.png";
        testZipPath = pwd + "test.zip";
    }

    @Test
    public void albumCovers() {
        title("albumCovers");
        assertTrue("no norah", new File(norahPath).exists());
        assertTrue("no chris", new File(chrisPath).exists());
    }

    private Image[] createTestImageArray() {
        Image[] images = new Image[IMAGE_COUNT];
        for (int i = 0; i < IMAGE_COUNT; i++) {
            images[i] = (new ImageIcon(i % 2 == 0 ? norahPath : chrisPath)).getImage();
        }
        return images;
    }

    @Test
    public void replacesOriginalMain() {
        Image[] image = createTestImageArray();

        try {
            ImageZip.zipImages(testZipPath, image);
            Image[] rImage = ImageZip.unzipImages(testZipPath);

            for (int i = 0; i < rImage.length; i++) {
                int jx = ORIGIN + (i * OFFSET_X);
                int jy = ORIGIN + (i * OFFSET_Y);
                JFrame frame = new JFrame();
                frame.setSize(FRAME_SIZE, FRAME_SIZE);
                frame.setLocation(jx, jy);
                frame.getContentPane().add(new JLabel(new ImageIcon(rImage[i])));
                frame.setVisible(true);
            }
            Thread.sleep(PAUSE_TO_ADMIRE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
