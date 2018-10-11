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
import java.awt.image.BufferedImage;

import static com.robin.general.graphics.GraphicsUtil.asPoint;
import static com.robin.general.graphics.GraphicsUtil.connectRect;
import static com.robin.general.graphics.GraphicsUtil.convertColor;
import static com.robin.general.graphics.GraphicsUtil.distancePoint2Line;
import static com.robin.general.graphics.GraphicsUtil.drawArrowLine;
import static com.robin.general.graphics.GraphicsUtil.drawCenteredImageIcon;
import static com.robin.general.graphics.GraphicsUtil.drawCenteredString;
import static com.robin.general.graphics.GraphicsUtil.drawCenteredStrings;
import static com.robin.general.graphics.GraphicsUtil.drawDashedLine;
import static com.robin.general.graphics.GraphicsUtil.drawRightJustifiedString;
import static com.robin.general.graphics.GraphicsUtil.drawSoftenedShape;
import static com.robin.general.graphics.GraphicsUtil.equalColor;
import static com.robin.general.graphics.GraphicsUtil.getPointOnLine;
import static com.robin.general.graphics.GraphicsUtil.getStringDimension;
import static com.robin.general.graphics.GraphicsUtil.midPoint;
import static com.robin.general.graphics.GraphicsUtil.radians_degrees60;
import static com.robin.general.graphics.GraphicsUtil.rotate;
import static com.robin.general.graphics.GraphicsUtil.saveImageToPNG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link GraphicsUtil}.
 */
public class GraphicsUtilTest extends AbstractGraphicsTest {

    private static final double PI_BY_2 = Math.PI / 2.0;

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
    @Ignore(SLOW)
    public void saveImageToPng() {
        title("saveImageToPng");
        saveImageToPNG(testImage(), outputFile("testPNG.png"));
    }

    @Test
    @Ignore(SLOW)
    public void saveImageToJpg() {
        title("saveImageToJpg");
        GraphicsUtil.saveImageToJPG(testImage(), outputFile("testJPG.jpg"));
        // TODO: This seems to be broken (black 200x200)
    }

    @Test
    @Ignore(SLOW)
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
        saveImageToPNG(comp, outputFile("testComponent.png"));
    }


    private static final Color RED = new Color(0xff, 0x00, 0x00);
    private static final Color GREEN = new Color(0x00, 0xff, 0x00);
    private static final Color BLUE = new Color(0x00, 0x00, 0xff);
    private static final Color SEA = new Color(0x00, 0xd0, 0xe0);
    private static final Color MUD = new Color(0x90, 0x70, 0x10);
    private static final Color HOG = new Color(0xd0, 0x80, 0x80);

    private static final Color[] SOURCES = {RED, GREEN, BLUE};
    private static final Color[] TARGETS = {SEA, MUD, HOG};
    private static final int[] PERCENTAGES = {0, 25, 50, 90, 100};

    private static Color c(int r, int g, int b) {
        return new Color(r, g, b);
    }

    private static final Color[][][] EXP_CONVERT = {
            {
                    {RED, c(0xbf, 0x34, 0x38), c(0x7f, 0x68, 0x70), c(0x19, 0xbb, 0xc9), SEA,},
                    {RED, c(0xe3, 0x1c, 0x04), c(0xc7, 0x38, 0x08), c(0x9b, 0x64, 0x0e), MUD,},
                    {RED, c(0xf3, 0x20, 0x20), c(0xe7, 0x40, 0x40), c(0xd4, 0x73, 0x73), HOG,},
            },
            {
                    {GREEN, c(0x00, 0xf3, 0x38), c(0x00, 0xe7, 0x70), c(0x00, 0xd4, 0xc9), SEA,},
                    {GREEN, c(0x24, 0xdb, 0x04), c(0x48, 0xb7, 0x08), c(0x81, 0x7e, 0x0e), MUD,},
                    {GREEN, c(0x34, 0xdf, 0x20), c(0x68, 0xbf, 0x40), c(0xbb, 0x8c, 0x73), HOG,},
            },
            {
                    {BLUE, c(0x00, 0x34, 0xf7), c(0x00, 0x68, 0xef), c(0x00, 0xbb, 0xe3), SEA,},
                    {BLUE, c(0x24, 0x1c, 0xc3), c(0x48, 0x38, 0x87), c(0x81, 0x64, 0x27), MUD,},
                    {BLUE, c(0x34, 0x20, 0xdf), c(0x68, 0x40, 0xbf), c(0xbb, 0x73, 0x8c), HOG,},
            },
    };


    private String c2str(Color c) {
        return "{" + String.format("%02x", c.getRed()) + "," +
                String.format("%02x", c.getGreen()) + "," +
                String.format("%02x", c.getBlue()) + "}";
    }

    private String p2str(Point p) {
        return p == null ? "<null>" : "[" + p.x + "," + p.y + "]";
    }

    private void printConvertResults(Color s, Color t, int p, Color result) {
        print("%s / %s / %s = %s", c2str(s), c2str(t), p, c2str(result));
    }

    @Test
    public void convertColors() {
        title("convert colors");
        for (int is = 0; is < SOURCES.length; is++) {
            Color source = SOURCES[is];

            for (int it = 0; it < TARGETS.length; it++) {
                Color target = TARGETS[it];

                for (int ip = 0; ip < PERCENTAGES.length; ip++) {
                    int percent = PERCENTAGES[ip];

                    Color computed = convertColor(source, target, percent);
                    Color expected = EXP_CONVERT[is][it][ip];
                    printConvertResults(source, target, percent, computed);
                    assertThat(computed, is(expected));
                }
                print("");
            }
        }
    }

    @Test
    public void convertColorNegPercent() {
        title("convert color -5 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, -5);
        print("RED/BLUE/-5 is %s", result);
        assertThat(result, is(RED));
    }

    @Test
    public void convertColorOverPercent() {
        title("convert color 150 %");
        Color result = GraphicsUtil.convertColor(RED, BLUE, 150);
        print("RED/BLUE/150 is %s", result);
        assertThat(result, is(BLUE));
    }

    private static final Color[] COLORS = {RED, GREEN, BLUE, SEA, MUD, HOG,};
    private static final Color[] EXP_INVERSE = {
            c(0x00, 0xff, 0xff),
            c(0xff, 0x00, 0xff),
            c(0xff, 0xff, 0x00),
            c(0xff, 0x2f, 0x1f),
            c(0x00, 0x00, 0xef),
            c(0x2f, 0x00, 0x00),
    };

    private void printInvertResults(Color in, Color result) {
        print("%s => %s", c2str(in), c2str(result));
    }

    @Test
    public void invertColors() {
        title("Invert Colors");
        for (int i = 0; i < COLORS.length; i++) {
            Color color = COLORS[i];
            Color computed = GraphicsUtil.inverseColor(color);
            Color expected = EXP_INVERSE[i];
            printInvertResults(color, computed);
            assertThat(computed, is(expected));
        }
    }

    @Test
    public void colorEquals() {
        title("Color Equals");
        assertThat(equalColor(RED, new Color(0xff, 0, 0)), is(true));
        assertThat(equalColor(MUD, new Color(0x90, 0x70, 0x10)), is(true));
        assertThat(equalColor(RED, MUD), is(false));
        assertThat(equalColor(null, GREEN), is(false));
        assertThat(equalColor(BLUE, null), is(false));
        assertThat(equalColor(null, null), is(false));
    }

    @Test
    @Ignore(SLOW)
    public void dashedLineOne() {
        title("Dashed Line One");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        int dlen = 10;
        int slen = 2;
        for (int y = 20; y <= 180; y += 20) {
            drawDashedLine(g2, 20, y, 180, y, dlen, slen);
            dlen += 5;
            slen += 2;
        }

        saveImageToPNG(new ImageIcon(bi), outputFile("testDashOne.png"));
    }

    @Test
    @Ignore(SLOW)
    public void dashedLineTwo() {
        title("Dashed Line Two");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(HOG);
        int dlen = 20;
        int slen = 8;
        for (int i = 50; i < 200; i += 20) {
            drawDashedLine(g2, 10, i, i, 10, dlen, slen);
            dlen += 5;
        }

        saveImageToPNG(new ImageIcon(bi), outputFile("testDashTwo.png"));
    }

    private void verifyPointOnLine(Point from, Point to, int pxOut, Point exp) {
        Point result = getPointOnLine(from, to, pxOut);
        print("%s -> %s (%s) ... %s", from, to, pxOut, result);
        assertThat(result, is(exp));
    }


    @Test
    public void pointOnLine() {
        title("Point On Line");
        verifyPointOnLine(p(0, 0), p(10, 10), 0, p(0, 0));
        verifyPointOnLine(p(0, 0), p(10, 10), 7, p(4, 4));
        verifyPointOnLine(p(0, 0), p(10, 10), 16, p(11, 11));
        verifyPointOnLine(p(10, 10), p(50, 10), 20, p(30, 10));
        verifyPointOnLine(p(10, 10), p(10, 40), 15, p(10, 25));
        // NOTE: if from == to, use angle of 0
        verifyPointOnLine(p(10, 10), p(10, 10), 15, new Point(25, 10));
    }

    private void verifyIntersection(Point l1p1, Point l1p2,
                                    Point l2p1, Point l2p2, Point exp) {
        Point result = GraphicsUtil.lineSegmentIntersection(l1p1, l1p2, l2p1, l2p2);
        print(result);
        assertThat(result, is(exp));
    }

    @Test
    public void lineIntersections() {
        title("Line Intersections");
        verifyIntersection(p(20, 10), p(50, 40), p(20, 40), p(40, 20), p(35, 25));
        verifyIntersection(p(30, 40), p(40, 50), p(10, 45), p(50, 45), p(35, 45));
        verifyIntersection(p(20, 40), p(40, 20), p(30, 40), p(40, 50), null);
        verifyIntersection(p(10, 10), p(20, 20), p(18, 18), p(14, 14), null);
        verifyIntersection(p(10, 40), p(50, 40), p(10, 60), p(50, 60), null);
    }

    private void verifyRectIntersect(Rectangle r, Point p1, Point p2, Point exp) {
        Point result = GraphicsUtil.uniqueLineSegmentRectangleIntersection(p1, p2, r);
        print(result);
        assertThat(result, is(exp));
    }

    @Test
    public void lineRectangle() {
        title("Line Rectangle Intersections");
        verifyRectIntersect(r(60, 10, 30, 20), p(70, 40), p(80, 20), p(75, 30));
        verifyRectIntersect(r(60, 10, 30, 20), p(50, 20), p(60, 20), p(60, 20));
        // NOTE: happens to return the "lower" edge of the rectangle...
        verifyRectIntersect(r(60, 10, 30, 20), p(65, 5), p(65, 35), p(65, 30));
        verifyRectIntersect(r(60, 10, 30, 20), p(50, 25), p(60, 40), null);
    }

    @Test
    @Ignore(SLOW)
    public void thickLines() {
        title("Thick Lines");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        int thick = 1;
        for (int y = 10; y < 200; y += 20) {
            GraphicsUtil.drawThickLine(g2, 20, y, 180, y, thick);
            thick++;
        }
        g2.setColor(Color.green);
        GraphicsUtil.drawThickLine(g2, 20, 20, 180, 180, 8);

        saveImageToPNG(new ImageIcon(bi), outputFile("testThickLines.png"));
    }

    private void verifyConnectRect(Rectangle r1, Rectangle r2, Point expP1, Point expP2) {
        Point[] result = GraphicsUtil.getConnectingLine(r1, r2);
        assertThat(result, is(notNullValue()));
        assertThat(result.length, is(2));
        print("%s -- %s", p2str(result[0]), p2str(result[1]));
        assertThat(result[0], is(expP1));
        assertThat(result[1], is(expP2));
    }

    private void verifyNoConnectRect(Rectangle r1, Rectangle r2) {
        Point[] result = GraphicsUtil.getConnectingLine(r1, r2);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void connectingLine() {
        title("Rectangle Connecting Line");
        verifyConnectRect(r(10, 10, 30, 20), r(60, 10, 30, 20), p(40, 20), p(60, 20));
        verifyConnectRect(r(10, 10, 30, 20), r(30, 40, 20, 20), p(30, 30), p(35, 40));
        verifyNoConnectRect(r(30, 40, 20, 20), r(40, 50, 10, 10));
        // following result a little strange, but okay....
        verifyConnectRect(r(0, 0, 20, 20), r(10, 10, 30, 30), p(20, 20), p(10, 10));
    }

    private void drawR(Graphics2D g2, Rectangle r) {
        g2.drawRect(r.x, r.y, r.width, r.height);
    }

    @Test
    @Ignore(SLOW)
    public void arrowHeads() {
        title("Arrow Heads to Rectangles");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();

        Rectangle r1 = new Rectangle(10, 10, 30, 20);
        Rectangle r2 = new Rectangle(120, 130, 50, 50);
        Rectangle r3 = new Rectangle(130, 50, 55, 20);
        drawR(g2, r1);
        drawR(g2, r2);
        drawR(g2, r3);
        g2.setColor(Color.YELLOW);
        drawArrowLine(g2, r1, true, r2, true);
        drawArrowLine(g2, r1, false, r3, true);
        drawArrowLine(g2, r2, true, r3, false);

        saveImageToPNG(new ImageIcon(bi), outputFile("testArrowHeads.png"));
    }

    private void verifyDistToLine(Point p, Point l1, Point l2, int exp) {
        int result = distancePoint2Line(p.x, p.y, l1.x, l1.y, l2.x, l2.y);
        print("%s ... [%s---%s] = %d", p2str(p), p2str(l1), p2str(l2), result);
        assertThat(result, is(exp));
    }

    @Test
    public void distPointToLine() {
        title("Distance Point to Line");
        verifyDistToLine(p(20, 10), p(10, 20), p(30, 20), 10);
        verifyDistToLine(p(60, 10), p(40, 10), p(60, 30), 14);
        verifyDistToLine(p(40, 10), p(10, 20), p(20, 20), GraphicsUtil.NO_PERP_DROP);
        verifyDistToLine(p(5, 5), p(0, 10), p(0, 10), 7);
        verifyDistToLine(p(10, 10), p(0, 20), p(20, 20), 10);
    }

    @Test
    @Ignore(SLOW)
    public void connectingRectangles() {
        title("Connect Rect");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();

        Rectangle r1 = new Rectangle(10, 10, 30, 20);
        Rectangle r2 = new Rectangle(120, 130, 50, 50);
        Rectangle r3 = new Rectangle(20, 100, 30, 30);

        connectRect(g2, r1, true, r2, true, 8);
        connectRect(g2, r2, true, r3, true, 8);

        saveImageToPNG(new ImageIcon(bi), outputFile("testConnRect.png"));
    }

    private static final Font FONT = UIManager.getFont("Label.font");
    private static final String SAMPLE_TEXT = "hello!";

    @Test
    @Ignore(SLOW)
    public void stringDimsGraphics() {
        title("String Dimensions from graphics");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.setFont(FONT);
        Dimension d = getStringDimension(g2, SAMPLE_TEXT);
        print(d);
        assertThat(d, is(new Dimension(35, 13)));
    }

    @Test
    @Ignore(SLOW)
    public void stringDimsComponent() {
        title("String Dimensions from Component");
        JPanel panel = new JPanel();
        Dimension dim = new Dimension(200, 200);
        panel.setPreferredSize(dim);
        panel.setMinimumSize(dim);
        panel.setFont(FONT);

        Dimension d = getStringDimension(panel, SAMPLE_TEXT);
        print(d);
        assertThat(d, is(new Dimension(35, 13)));
    }

    @Test
    @Ignore(SLOW)
    public void rightJustify() {
        title("Right Justify");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.setFont(FONT);

        Rectangle rect = r(10, 10, 90, 90);
        drawR(g2, rect);
        drawRightJustifiedString(g2, 100, 50, "Booyah");

        g2.setColor(Color.RED);
        g2.drawLine(0, 50, 200, 50);
        saveImageToPNG(new ImageIcon(bi), outputFile("testRightJust.png"));
    }

    @Test
    @Ignore(SLOW)
    public void centerString() {
        title("Centered string");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.setFont(FONT);

        Rectangle rect = r(20, 20, 100, 55);
        drawR(g2, rect);
        drawCenteredString(g2, rect, SAMPLE_TEXT);

        g2.setColor(Color.CYAN);
        rect = r(30, 120, 120, 60);
        drawR(g2, rect);
        drawCenteredString(g2, rect.x, rect.y, rect.width, rect.height, SAMPLE_TEXT);

        saveImageToPNG(new ImageIcon(bi), outputFile("testCenterStr.png"));
    }

    private static final Object[] STRING_ITEMS = {
            12345,
            "Hello, Sailor!",
            "How you doin'?",
            p(32, 55),
    };

    @Test
    @Ignore(SLOW)
    public void centeredStringsPlural() {
        title("Centered Strings (plural)");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();
        g2.setFont(FONT);
        Rectangle rect = r(5, 35, 190, 80);
        drawR(g2, rect);
        g2.setColor(Color.CYAN);

        drawCenteredStrings(g2, rect.x, rect.y, rect.width, rect.height,
                            STRING_ITEMS);

        saveImageToPNG(new ImageIcon(bi), outputFile("testMultiStr.png"));
    }

    @Test
    @Ignore(SLOW)
    public void centeredImageIcon() {
        title("Centered image icon");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();

        BufferedImage small = createBufferedImage(20);
        Graphics2D sg = small.createGraphics();
        fillImage(sg, Color.CYAN);
        ImageIcon ii = new ImageIcon(small);

        Rectangle rect = r(0, 0, 100, 100);
        drawR(g2, rect);
        drawCenteredImageIcon(g2, rect, ii);

        saveImageToPNG(new ImageIcon(bi), outputFile("testCenterIcon.png"));
    }

    private static final int[] P_XPTS = {20, 40, 50, 12};
    private static final int[] P_YPTS = {20, 25, 40, 25};
    private static final int N_PTS = P_XPTS.length;
    private static final Polygon POLY = new Polygon(P_XPTS, P_YPTS, N_PTS);

    private Polygon makePoly(int ox, int oy) {
        int[] xs = new int[N_PTS];
        int[] ys = new int[N_PTS];
        for (int i = 0; i < N_PTS; i++) {
            xs[i] = P_XPTS[i] + ox;
            ys[i] = P_YPTS[i] + oy;
        }
        return new Polygon(xs, ys, N_PTS);
    }

    @Test
    @Ignore(SLOW)
    public void softened() {
        title("Softened Shape");
        BufferedImage bi = createBufferedImage();
        Graphics2D g2 = bi.createGraphics();

        Rectangle top = r(10, 10, 150, 80);
        g2.setColor(Color.GRAY);
        g2.fillRect(top.x, top.y, top.width, top.height);

        drawSoftenedShape(g2, makePoly(10, 0), Color.BLUE, true);
        drawSoftenedShape(g2, makePoly(60, 0), Color.BLUE, false);

        saveImageToPNG(new ImageIcon(bi), outputFile("testSoften.png"));
    }

    @Test
    @Ignore(SLOW)
    public void overlayImageTest() {
        title("Overlay Images");
        ImageIcon result = GraphicsUtil.overlayImages(testBigIcon(), testSmallIcon());
        saveImageToPNG(result, outputFile("testOverlay.png"));
    }

    private void verifyRotation(double px, double py,
                                double cx, double cy,
                                double radians,
                                double expX, double expY) {
        Point2D p = new Point2D.Double(px, py);
        Point2D c = new Point2D.Double(cx, cy);
        Point2D result = rotate(p, c, radians);
        print(result);
        assertThat(result.getX(), is(expX));
        assertThat(result.getY(), is(expY));
    }

    @Test
    public void rotatePoints() {
        title("Rotate points");
        verifyRotation(20, 20, 0, 0, PI_BY_2, -20, 20);
        verifyRotation(30, -10, 10, -10, PI_BY_2, 10, 10);
    }

    private void verifyMidPoint(Point a, Point b, Point exp) {
        Point result = midPoint(a, b);
        print(result);
        assertThat(result, is(exp));
    }

    @Test
    public void midPoints() {
        title("Mid points");
        verifyMidPoint(p(10, 30), p(50, 50), p(30, 40));
        verifyMidPoint(p(-10, -10), p(0, 20), p(-5, 5));
    }

    private void verifyStrToPoint(String str, Point exp) {
        Point result = asPoint(str);
        print("[%s] --> %s", str, result);
        assertThat(result, is(exp));
    }

    @Test
    public void strToPoint() {
        title("String to Point");
        verifyStrToPoint("1,1", p(1, 1));
        verifyStrToPoint("0,4", p(0, 4));
        verifyStrToPoint("-50,101", p(-50, 101));
    }

    // No unit test for shortenedLineFar() - deprecated

    // No unit test for copyImageIcon() - deprecated

}

