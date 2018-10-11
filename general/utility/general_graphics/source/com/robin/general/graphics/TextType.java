/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Represents a piece of text, of varying types.
 */
public class TextType {

    /**
     * Denotes text alignment within its container.
     */
    public enum Alignment {
        Left,
        Center,
        Right
    }


    // package private for unit test access
    static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 11);
    static final Color DEFAULT_COLOR = Color.black;

    private static final String SPACE = " ";

    private static final Map<String, Font> fontMap = new HashMap<>();
    private static final Map<String, Color> colorMap = new HashMap<>();


    private String text;
    private String[] lineArray = null;
    private String type;
    private int width;

    private int rotate = 0;
    private String delims = SPACE;
    private String space = SPACE;

    /**
     * Creates a text type instance for the given string, width and type.
     *
     * @param inText the text
     * @param width  the width (in pixels)
     * @param type   the type of text
     */
    public TextType(String inText, int width, String type) {
        text = inText;
        this.width = width;
        this.type = type;
    }

    /**
     * Sets the rotation of this text, in degrees.
     * Note: positive rotates clockwise; negative counter-clockwise.
     *
     * @param degrees degrees of rotation
     */
    public void setRotate(int degrees) {
        rotate = degrees;
    }

    /**
     * Returns the font associated with this text.
     *
     * @return the associated font
     */
    public Font getFont() {
        Font font = fontMap.get(type);
        return font != null ? font : DEFAULT_FONT;
    }

    /**
     * Returns the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the color associated with this text type.
     * If the type is not defined in the "palette", the default color
     * is returned.
     *
     * @return the text color
     */
    public Color getColor() {
        Color color = colorMap.get(type);
        return color != null ? color : DEFAULT_COLOR;
    }

    /**
     * Computes the width (in pixels) of the longest text line.
     *
     * @param g the graphics context
     * @return the maximum width
     */
    private int getMaxWidth(Graphics g) {
        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        int max = 0;
        for (String aLine : lineArray) {
            int len = metrics.stringWidth(aLine);
            if (len > max) {
                max = len;
            }
        }
        return max;
    }

    /**
     * Computes the lines with the appropriate font, to fit in the
     * configured width.
     *
     * @param g the graphics context
     */
    private void updateSpacing(Graphics g) {
        if (lineArray == null) {
            g.setFont(getFont());
            FontMetrics metrics = g.getFontMetrics();

            // Break up all words
            StringTokenizer tokens = new StringTokenizer(text, delims);
            int nWords = tokens.countTokens();

            String[] word = new String[nWords];
            int[] wordWidth = new int[nWords];
            for (int i = 0; i < nWords; i++) {
                word[i] = tokens.nextToken();
                wordWidth[i] = metrics.stringWidth(word[i]);
            }
            int spaceWidth = metrics.stringWidth(space);

            // Build lines
            int currentWidth = 0;
            StringBuilder sb = new StringBuilder();
            java.util.List<String> lines = new ArrayList<>();
            for (int i = 0; i < nWords; i++) {
                int newWidth = currentWidth + wordWidth[i];

                if (sb.length() > 0 && (newWidth + spaceWidth) > width) {
                    lines.add(sb.toString());
                    sb = new StringBuilder();
                    newWidth = wordWidth[i];
                }
                if (sb.length() > 0) {
                    sb.append(space);
                    newWidth += spaceWidth;
                }
                sb.append(word[i]);
                currentWidth = newWidth;
            }
            if (sb.length() > 0) {
                lines.add(sb.toString());
            }

            lineArray = lines.toArray(new String[0]);
        }
    }

    /**
     * Returns the height of the (multi-line) text, laid out within
     * the given width.
     *
     * @param g the graphics context
     * @return the height if the text (in pixels)
     */
    public int getHeight(Graphics g) {
        updateSpacing(g);
        g.setFont(getFont());
        return g.getFontMetrics().getAscent() * lineArray.length;
    }

    /**
     * Returns the height of a single line of text.
     *
     * @param g the graphics context
     * @return the height of a single line
     */
    private int getLineHeight(Graphics g) {
        g.setFont(getFont());
        return g.getFontMetrics().getAscent();
    }

    /**
     * Returns the width of the (multi-line) text.
     *
     * @param g the graphics context
     * @return the width
     */
    public int getWidth(Graphics g) {
        updateSpacing(g);
        return getMaxWidth(g);
    }

    /**
     * Draws this text instance into the given graphics context (center aligned)
     * at the specified location.
     *
     * @param g the graphics context
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void draw(Graphics g, int x, int y) {
        draw(g, x, y, Alignment.Center, getColor());
    }

    /**
     * Draws this text instance into the given graphics context (center aligned)
     * at the specified location, but using the given color instead of the
     * previously configured color.
     *
     * @param g        the graphics context
     * @param x        x-coordinate
     * @param y        y-coordinate
     * @param override color override
     */
    public void draw(Graphics g, int x, int y, Color override) {
        draw(g, x, y, Alignment.Center, override);
    }

    /**
     * Draws this text instance into the given graphics context, at the given
     * location, using the specified alignment.
     *
     * @param g         the graphics context
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param alignment text alignment
     */
    public void draw(Graphics g, int x, int y, Alignment alignment) {
        draw(g, x, y, alignment, getColor());
    }

    /**
     * Draws this text instance into the given graphics context, at the given
     * location, using the specified alignment and color.
     *
     * @param g         the graphics context
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param alignment text alignment
     * @param override  color override
     */
    public void draw(Graphics g, int x, int y, Alignment alignment, Color override) {
        if (text != null) {
            updateSpacing(g);
            g.setColor(override);
            g.setFont(getFont());
            int lineHeight = getLineHeight(g);
            int ypos = lineHeight;
            for (String aLine : lineArray) {
                drawText(g, aLine, x, y + ypos, width, rotate, alignment);
                ypos += lineHeight;
            }
        }
    }

    /**
     * Sets the delimiters to be used with the string tokenizer for this
     * text instance.
     *
     * @param delims the delimiters
     */
    public void setDelims(String delims) {
        this.delims = delims;
    }

    /**
     * Sets the space to be used to separate words when the text is rendered.
     *
     * @param space the spacing
     */
    public void setSpace(String space) {
        this.space = space;
    }

    /**
     * Draws the specified text string into the graphic context, using the
     * given location (x, y) and width, rotated and aligned as specified.
     * Note that the string is written "as is" (no formatting into multi-line).
     * Rotations are in degrees; positive rotates clockwise, negative counter-
     * clockwise.
     *
     * @param g         the graphics context
     * @param text      the text to render
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param width     width (pixels) to consider for alignment
     * @param rotate    rotation (in degrees)
     * @param alignment text alignment
     */
    public static void drawText(Graphics g, String text, int x, int y,
                                int width, int rotate, Alignment alignment) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform orig = null;
        if (rotate != 0) {
            orig = g2.getTransform();
            AffineTransform rotated = new AffineTransform(orig);
            rotated.rotate(Math.toRadians((double) rotate), x, y);
            g2.setTransform(rotated);
        }
        int offset = 0;
        switch (alignment) {
            case Center:
                offset = (width - g.getFontMetrics().stringWidth(text)) >> 1;
                break;
            case Right:
                offset = width - g.getFontMetrics().stringWidth(text);
                break;
        }
        g2.drawString(text, x + offset, y);
        if (orig != null) {
            g2.setTransform(orig);
        }
    }

    /**
     * Adds a type to the TextType palette.
     */
    public static void addType(String typeName, Font font, Color color) {
        if (font != null) {
            fontMap.put(typeName, font);
        }
        if (color != null) {
            colorMap.put(typeName, color);
        }
    }


    // package-private : to accommodate unit testing
    static int getTypeFontsSize() {
        return fontMap.size();
    }

    static int getTypeColorsSize() {
        return colorMap.size();
    }

    static void resetCachedFontsAndColors() {
        fontMap.clear();
        colorMap.clear();
    }
}