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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link TextType}.
 */
public class TextTypeTest extends AbstractGraphicsTest {

    private static final String SOME_TEXT = "Yo Mamma!";
    private static final String SOME_TYPE = "someType";
    private static final Font SOME_FONT = TextType.DEFAULT_FONT.deriveFont(20.0f);
    private static final Color SOME_COLOR = Color.ORANGE;

    private static final String FOX_TYPE = "FOX";
    private static final Font FOX_FONT =
            TextType.DEFAULT_FONT.deriveFont(Font.ITALIC, 14.0f);
    private static final Color FOX_COLOR = new Color(0xee, 0x99, 0x44);
    private static final String FOX_TEXT =
            "The quick brown fox jumped over the lazy dog.";
    private static final String CAT_TEXT = "meow,meow,meow,meow";

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

    @Test
    public void basicTwo() {
        title("Basic Two");
        TextType.resetCachedFontsAndColors();
        TextType tt = new TextType(SOME_TEXT, 20, SOME_TYPE);
        assertThat(tt.getText(), is(SOME_TEXT));

        Font font = tt.getFont();
        assertThat(font.getName(), is("Dialog"));
        assertThat(font.getStyle(), is(Font.PLAIN));
        assertThat(font.getSize(), is(11));

        assertThat(tt.getColor(), is(Color.BLACK));
    }

    @Test
    public void cachedType() {
        title("Cached Type");
        TextType.resetCachedFontsAndColors();
        assertThat(TextType.getTypeFontsSize(), is(0));
        assertThat(TextType.getTypeColorsSize(), is(0));

        TextType.addType(SOME_TYPE, SOME_FONT, SOME_COLOR);
        assertThat(TextType.getTypeFontsSize(), is(1));
        assertThat(TextType.getTypeColorsSize(), is(1));

        TextType tt = new TextType(SOME_TEXT, 20, SOME_TYPE);
        assertThat(tt.getText(), is(SOME_TEXT));

        Font font = tt.getFont();
        assertThat(font.getName(), is("Dialog"));
        assertThat(font.getStyle(), is(Font.PLAIN));
        assertThat(font.getSize(), is(20));

        assertThat(tt.getColor(), is(Color.ORANGE));
    }

    @Test
    public void quickBrownFox() {
        title("Quick Brown Fox");
        TextType.resetCachedFontsAndColors();
        TextType.addType(FOX_TYPE, FOX_FONT, FOX_COLOR);
        TextType tt = new TextType(FOX_TEXT, 100, FOX_TYPE);


        BufferedImage bi = createBufferedImage();
        Graphics g = bi.getGraphics();
        g.drawRect(20, 0, 100, 198);

        assertThat(tt.getWidth(g), is(88));
        assertThat(tt.getHeight(g), is(56));

        tt.draw(g, 20, 0);
        tt.draw(g, 20, 70, Alignment.Left);
        tt.draw(g, 20, 140, Alignment.Right, Color.CYAN);

        saveImageToPNG(new ImageIcon(bi), outputFile("ttFox.png"));
    }

    @Test
    public void meow() {
        title("Cat's Meow");
        TextType.resetCachedFontsAndColors();
        TextType tt = new TextType(CAT_TEXT, 100, "none");

        BufferedImage bi = createBufferedImage();
        Graphics g = bi.getGraphics();
        g.drawRect(50, 0, 100, 198);

        tt.draw(g, 50, 0, Color.YELLOW);

        TextType tt2 = new TextType(CAT_TEXT, 100, "none");
        tt2.setDelims(",");
        tt2.setSpace("-+-");
        tt2.draw(g, 50, 50, Color.YELLOW);

        TextType tt3 = new TextType(CAT_TEXT, 100, "none");
        tt3.setDelims(",");
        tt3.setRotate(45);
        tt3.draw(g, 50, 100, Color.YELLOW);

        saveImageToPNG(new ImageIcon(bi), outputFile("ttCat.png"));
    }
}
