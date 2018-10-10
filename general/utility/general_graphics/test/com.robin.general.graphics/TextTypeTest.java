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

import com.robin.general.graphics.TextType.Alignment;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.robin.general.graphics.GraphicsUtil.saveImageToPNG;

/**
 * Unit tests for {@link TextType}.
 */
public class TextTypeTest extends AbstractGraphicsTest {

    @Test
    public void basic() {
        title("Basic");
        BufferedImage bi = createBufferedImage();
        Graphics g = bi.getGraphics();

        g.drawRect(20, 20, 100, 100);
        String text = "Hello, Sailor!";

        TextType.drawText(g, text, 20, 40, 100, 0, Alignment.Left);
        TextType.drawText(g, text, 20, 60, 100, 0, Alignment.Center);
        TextType.drawText(g, text, 20, 80, 100, 0, Alignment.Right);

        g.setColor(Color.CYAN);
        TextType.drawText(g, text, 20, 120, 100, 90, Alignment.Left);
        TextType.drawText(g, text, 40, 120, 100, 60, Alignment.Left);
        TextType.drawText(g, text, 60, 120, 100, 30, Alignment.Left);
        TextType.drawText(g, text, 80, 120, 100, -30, Alignment.Left);

        saveImageToPNG(new ImageIcon(bi), outputFile("tt1.png"));
    }
}
