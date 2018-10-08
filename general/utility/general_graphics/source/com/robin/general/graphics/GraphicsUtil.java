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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Useful graphics utilities.
 */
public class GraphicsUtil {

    /**
     * Returns a color that is a mix of the specified source and target colors,
     * based on the given percentage. A percent value of 100 will return the
     * target; a percent value of 0 will return the source. Something in
     * between will return the appropriately mixed color.
     *
     * @param source  the source color
     * @param target  the target color
     * @param percent a value from 0..100
     * @return the mixed color
     */
    public static Color convertColor(Color source, Color target, int percent) {
        float r1, g1, b1;
        float r2, g2, b2;
        int r3, g3, b3;
        float p;

        percent = percent > 100 ? 100 : percent;
        percent = percent < 0 ? 0 : percent;

        if (percent == 0) {
            return source;
        } else if (percent == 100) {
            return target;
        }

        r1 = source.getRed();
        g1 = source.getGreen();
        b1 = source.getBlue();

        r2 = target.getRed();
        g2 = target.getGreen();
        b2 = target.getBlue();

        p = (float) percent / 100;

        r3 = (int) (r1 + ((r2 - r1) * p));
        g3 = (int) (g1 + ((g2 - g1) * p));
        b3 = (int) (b1 + ((b2 - b1) * p));

        r3 = r3 > 255 ? 255 : r3;
        g3 = g3 > 255 ? 255 : g3;
        b3 = b3 > 255 ? 255 : b3;
        r3 = r3 < 0 ? 0 : r3;
        g3 = g3 < 0 ? 0 : g3;
        b3 = b3 < 0 ? 0 : b3;

        return (new Color(r3, g3, b3));
    }

    /**
     * Returns a compatible inverse color for the given source.
     *
     * @param source the source color
     * @return a compatible inverse color
     */
    public static Color inverseColor(Color source) {
        int r = source.getRed();
        int g = source.getGreen();
        int b = source.getBlue();
        if (Math.abs(r - 128) < 20) {
            r = 255;
        }
        if (Math.abs(g - 128) < 20) {
            g = 255;
        }
        if (Math.abs(b - 128) < 20) {
            b = 255;
        }
        return new Color(255 - r, 255 - g, 255 - b);
    }

    /**
     * Returns true if the two specified colors are precisely the same.
     * If either, or both, arguments are null, returns false.
     *
     * @param c1 the first color
     * @param c2 the second color
     * @return true if the colors are equal
     */
    public static boolean equalColor(Color c1, Color c2) {
        // TODO: re-write using Color.equals()
        if (c1 != null && c2 != null) {
            int r1 = c1.getRed();
            int g1 = c1.getGreen();
            int b1 = c1.getBlue();

            int r2 = c2.getRed();
            int g2 = c2.getGreen();
            int b2 = c2.getBlue();

            if (r1 == r2 && g1 == g2 && b1 == b2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Draws a dashed line into the given graphics context, with the specified
     * start and end points, and dash/space distances.
     *
     * @param g           the graphics context
     * @param x1          x-coordinate of start point
     * @param y1          y-coordinate of start point
     * @param x2          x-coordinate of end point
     * @param y2          y-coordinate of end point
     * @param dashlength  length of individual dashes
     * @param spacelength length of spacing between dashes
     */
    public static void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2,
                                      double dashlength, double spacelength) {
        if ((dashlength + spacelength) > 0) {
            double linelength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            double xincdashspace = (x2 - x1) / (linelength / (dashlength + spacelength));
            double yincdashspace = (y2 - y1) / (linelength / (dashlength + spacelength));
            double xincdash = (x2 - x1) / (linelength / (dashlength));
            double yincdash = (y2 - y1) / (linelength / (dashlength));
            int counter = 0;
            for (double i = 0; i < linelength - dashlength; i += dashlength + spacelength) {
                g.drawLine((int) (x1 + xincdashspace * counter),
                           (int) (y1 + yincdashspace * counter),
                           (int) (x1 + xincdashspace * counter + xincdash),
                           (int) (y1 + yincdashspace * counter + yincdash));
                counter++;
            }
            if ((dashlength + spacelength) * counter <= linelength) {
                g.drawLine((int) (x1 + xincdashspace * counter),
                           (int) (y1 + yincdashspace * counter), x2, y2);
            }
        } else {
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Returns the point on a line which is a specified number of pixels out
     * from one end.
     *
     * @param from      the start point of the line
     * @param to        the end point of the line
     * @param pixelsOut the distance (in pixels) along the line
     * @return the computed point
     */
    // NOTE: replaces shortenedLineFar() which has crummy accuracy.
    public static Point getPointOnLine(Point from, Point to, int pixelsOut) {
        double angle;
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        if (dx == 0 && dy == 0) {
            angle = 0;
        } else {
            angle = Math.atan2((double) dy, (double) dx);
        }

        int x = (int) ((pixelsOut * Math.cos(angle)) + from.x);
        int y = (int) ((pixelsOut * Math.sin(angle)) + from.y);

        return new Point(x, y);
    }

    /**
     * Returns the point of intersection of two line segments, or null if the
     * segments do not cross.
     *
     * @param line1P1 start of first line segment
     * @param line1P2 end of first line segment
     * @param line2P1 start of second line segment
     * @param line2P2 end of second line segment
     * @return intersection point (or null if none exists)
     */
    public static Point lineSegmentIntersection(Point line1P1, Point line1P2,
                                                Point line2P1, Point line2P2) {
        double numA = ((line2P2.x - line2P1.x) * (line1P1.y - line2P1.y)) -
                ((line2P2.y - line2P1.y) * (line1P1.x - line2P1.x));
        double numB = ((line1P2.x - line1P1.x) * (line1P1.y - line2P1.y)) -
                ((line1P2.y - line1P1.y) * (line1P1.x - line2P1.x));
        double den = ((line2P2.y - line2P1.y) * (line1P2.x - line1P1.x)) -
                ((line2P2.x - line2P1.x) * (line1P2.y - line1P1.y));
        if ((numA == 0 || numB == 0) && den == 0) {
            // Coincident lines
            return null; // there is no precise intersection
        } else if (den == 0) {
            // Parallel lines
            return null;
        } else {
            // The lines intersect...
            double ua = numA / den;
            double ub = numB / den;

            // ...but do the line SEGMENTS intersect?
            if (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0) {
                // Yes, the line segements intersect
                int x = line1P1.x + (int) (ua * (double) (line1P2.x - line1P1.x));
                int y = line1P1.y + (int) (ua * (double) (line1P2.y - line1P1.y));
                return new Point(x, y);
            }
        }
        return null; // no intersection
    }

    /**
     * Returns the unique intersection point of a line segment and a rectangle,
     * or null if no such intersection exists.
     *
     * @param p1 start of line segment
     * @param p2 end of line segment
     * @param r  rectangle
     * @return intersection point (or null of none exists)
     */
    public static Point uniqueLineSegmentRectangleIntersection(Point p1, Point p2,
                                                               Rectangle r) {
        // Only four corners on a rectangle (well, duh!)
        Point[] corner = new Point[4];
        corner[0] = new Point(r.x, r.y);
        corner[1] = new Point(r.x + r.width, r.y);
        corner[2] = new Point(r.x, r.y + r.height);
        corner[3] = new Point(r.x + r.width, r.y + r.height);

        // Only four lines in the rectangle to intersect with
        Point[] intersect = new Point[4];
        intersect[0] = lineSegmentIntersection(corner[0], corner[1], p1, p2);
        intersect[1] = lineSegmentIntersection(corner[0], corner[2], p1, p2);
        intersect[2] = lineSegmentIntersection(corner[1], corner[3], p1, p2);
        intersect[3] = lineSegmentIntersection(corner[2], corner[3], p1, p2);

        int count = 0;
        Point found = null;
        for (int i = 0; i < 4; i++) {
            if (intersect[i] != null) {
                found = intersect[i];
                count++;
            }
        }
        if (count > 0) {
            // there is at least one intersection - doesn't matter if there
            // are more - just return one!
            return found;
        }
        return null;
    }

    /**
     * Draws a line segment of the specified thickness into the given
     * graphics context.
     *
     * @param g         graphics context
     * @param x1        x-coordinate of start point
     * @param y1        y-coordinate of start point
     * @param x2        x-coordinate of end point
     * @param y2        y-coordinate of end point
     * @param thickness line thickness
     */
    public static void drawThickLine(Graphics g, int x1, int y1,
                                     int x2, int y2, int thickness) {
        // End1
        Polar theLine = new Polar(new Point(x1, y1), new Point(x2, y2));
        Polar end1 = new Polar(theLine);
        end1.setLength(thickness >> 1);
        end1.addAngle(90);
        int angle = end1.getAngle();
        end1 = new Polar(end1.getRect(), new Point(0, 0));
        end1.setLength(thickness);
        end1.setAngle(angle + 180);

        // End2
        theLine = new Polar(new Point(x2, y2), new Point(x1, y1));
        Polar end2 = new Polar(theLine);
        end2.setLength(thickness >> 1);
        end2.addAngle(90);
        angle = end2.getAngle();
        end2 = new Polar(end2.getRect(), new Point(0, 0));
        end2.setLength(thickness);
        end2.setAngle(angle + 180);

        Polygon theThickLine = new Polygon();
        theThickLine.addPoint(end1.getOrigin().x, end1.getOrigin().y);
        theThickLine.addPoint(end1.getRect().x, end1.getRect().y);
        theThickLine.addPoint(end2.getOrigin().x, end2.getOrigin().y);
        theThickLine.addPoint(end2.getRect().x, end2.getRect().y);
        g.fillPolygon(theThickLine);
    }

    /*
     * Find the shortest connecting line between two rectangles.
     *
     * @return An array of two coordinates describing the line
     */

    /**
     * Returns a 2-element array containing the end points of the line segment
     * that connects two rectangles, coincident with the line that passes
     * through the rectangles' centers.
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return the connecting line segment endpoints
     */
    public static Point[] getConnectingLine(Rectangle r1, Rectangle r2) {
        // Find centers of rectangles
        int x1 = r1.x + (r1.width >> 1);
        int y1 = r1.y + (r1.height >> 1);
        int x2 = r2.x + (r2.width >> 1);
        int y2 = r2.y + (r2.height >> 1);

        // Find the intersection with each of the rectangles,
        // and the line between the two centers
        Point[] p = new Point[2];
        p[0] = uniqueLineSegmentRectangleIntersection(new Point(x1, y1), new Point(x2, y2), r1);
        p[1] = uniqueLineSegmentRectangleIntersection(new Point(x1, y1), new Point(x2, y2), r2);
        if (p[0] != null && p[1] != null) {
            return p;
        }
        return null;
    }

    /**
     * Draws a line into the given graphics context, that connects two
     * rectangles, with optional arrowheads on either end.
     *
     * @param g          the graphics context
     * @param r1         rectangle #1
     * @param arrowHead1 true to add an arrowhead pointing to rectangle #1
     * @param r2         rectangle #2
     * @param arrowHead2 true to add an arrowhead pointing to rectangle #2
     */
    public static void drawArrowLine(Graphics g,
                                     Rectangle r1, boolean arrowHead1,
                                     Rectangle r2, boolean arrowHead2) {
        Point[] p = getConnectingLine(r1, r2);

        if (p != null) {
            // Draw the line using the intersection points
            drawArrowLine(g, p[0], arrowHead1, p[1], arrowHead2);
        }
    }

    /**
     * Draws a line into the given graphics context, between two specified
     * points, with optional arrowheads on either end.
     *
     * @param g          the graphics context
     * @param p1         point #1
     * @param arrowHead1 true to add an arrowhead pointing to point #1
     * @param p2         point #2
     * @param arrowHead2 true to add an arrowhead pointing to point #2
     */
    public static void drawArrowLine(Graphics g,
                                     Point p1, boolean arrowHead1,
                                     Point p2, boolean arrowHead2) {
        Polar end1 = new Polar(p2, p1);
        Polar end2 = new Polar(p1, p2);

        Point adjP1 = end1.getRect();
        Point adjP2 = end2.getRect();
        g.drawLine(adjP1.x, adjP1.y, adjP2.x, adjP2.y);

        // Draw arrowheads if desired
        int arrowBreadth = 60;
        int arrowLength = 10;
        Polygon head;
        // TODO: Refactor to remove duplicate code
        if (arrowHead1) {
            head = new Polygon();
            head.addPoint(adjP1.x, adjP1.y);
            int angle = end1.getAngle();
            end1 = new Polar(end1.getRect(), new Point(0, 0));
            end1.setAngle(angle + 180);
            end1.setLength(arrowLength);
            end1.addAngle(arrowBreadth >> 1);
            Point arrow = end1.getRect();
            head.addPoint(arrow.x, arrow.y);
            end1.addAngle(-arrowBreadth);
            arrow = end1.getRect();
            head.addPoint(arrow.x, arrow.y);
            g.fillPolygon(head);
        }
        if (arrowHead2) {
            head = new Polygon();
            head.addPoint(adjP2.x, adjP2.y);
            int angle = end2.getAngle();
            end2 = new Polar(end2.getRect(), new Point(0, 0));
            end2.setAngle(angle + 180);
            end2.setLength(arrowLength);
            end2.addAngle(arrowBreadth >> 1);
            Point arrow = end2.getRect();
            head.addPoint(arrow.x, arrow.y);
            end2.addAngle(-arrowBreadth);
            arrow = end2.getRect();
            head.addPoint(arrow.x, arrow.y);
            g.fillPolygon(head);
        }
    }

    /**
     * Value representing no perpendicular drop to specified line segment.
     *
     * @see #distancePoint2Line(int, int, int, int, int, int)
     */
    public static final int NO_PERP_DROP = 999999;

    /**
     * Returns the shortest distance (in pixels) of a point (px, py) to a line
     * segment (x1, y1) to (x2, y2). Returns 9999999 if the perpendicular drop
     * from p does not intersect the segment (?).
     *
     * @param px x-coordinate of the point
     * @param py y-coordinate of the point
     * @param x1 x-coordinate of one end of the line segment
     * @param y1 y-coordinate of one end of the line segment
     * @param x2 x-coordinate of the other end of the line segment
     * @param y2 y-coordinate of the other end of the line segment
     * @return the distance between the point and the line segment
     */
    public static int distancePoint2Line(int px, int py,
                                         int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int intersectionX;
        int intersectionY;
        if (dx == 0 && dy == 0) {
            intersectionX = x1;
            intersectionY = y1;
        } else {
            double u = ((px - x1) * dx) + ((py - y1) * dy);
            u = u / ((dx * dx) + (dy * dy));
            if (u < 0.0 || u > 1.0) {
                return NO_PERP_DROP;
            }
            intersectionX = (int) ((double) x1 + (u * (double) dx));
            intersectionY = (int) ((double) y1 + (u * (double) dy));
        }
        dx = intersectionX - px;
        dy = intersectionY - py;
        return (int) Math.sqrt((dx * dx) + (dy * dy));
    }


    /**
     * Captures the graphics in a component to a file. Specify true as the
     * overwrite parameter
     *
     * @param c         the component to capture
     * @param filename  the output file path
     * @param overwrite true to allow overwriting an existing file
     * @return true on success
     */
    // does not appear to be called from anywhere
    @Deprecated
    public static boolean componentToGif(Component c, String filename,
                                         boolean overwrite) {
        // TODO: alternative implementation (using standard Java libraries)
/*
        Dimension d = c.getSize();
        Image image = c.createImage(d.width,d.height);
        Graphics ig = image.getGraphics();
        c.paint(ig);
        try {
            File file = new File("test.gif");
            if (overwrite || !file.exists()) {
                System.out.println("writing file...");
                FileOutputStream stream = new FileOutputStream(filename);
                GifEncoder ge = new GifEncoder(image,stream);
                ge.encode();
                stream.close();
                System.out.println("done writing file.");
                return true;
            }
        } catch(IOException err) {
        }
*/
        return false;
    }

    /**
     * Draws two rectangles into the given graphics context, and connects them
     * with a directional line, adjusted to compensate for a border around the
     * rectangles. Optionally, arrowheads can be added to the ends of the line.
     *
     * @param g          the graphics context
     * @param r1         rectangle #1
     * @param arrow1     true to add arrowhead pointing to rectangle #1
     * @param r2         rectangle #2
     * @param arrow2     true to add arrowhead pointing to rectangle #2
     * @param borderSize width of border from rectangles' bounding box
     */
    // does not appear to be called from anywhere!
    @Deprecated
    public static void connectRect(Graphics g, Rectangle r1, boolean arrow1,
                                   Rectangle r2, boolean arrow2, int borderSize) {
        // TODO: Huh? Why create new instances?
        r1 = new Rectangle(r1);
        r2 = new Rectangle(r2);
        g.drawRect(r1.x, r1.y, r1.width, r1.height);
        g.drawRect(r2.x, r2.y, r2.width, r2.height);
        if (r2.x < r1.x) {
            // swap 'em.
            // TODO: Huh? Why create new instances?
            Rectangle r = new Rectangle(r1);
            r1 = new Rectangle(r2);
            r2 = new Rectangle(r);
        }

        Point p1 = new Point(r1.x + r1.width + borderSize, r1.y + (r1.height >> 1));
        Point p2 = new Point(r2.x - borderSize, r2.y + (r2.height >> 1));
        drawArrowLine(g, p1, arrow1, p2, arrow2);
    }

    /**
     * Returns the dimensions a given string would have if rendered
     * in the specified graphics context.
     *
     * @param g      the graphics context
     * @param string the string
     * @return the string's dimensions
     */
    public static Dimension getStringDimension(Graphics g, String string) {
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(string);
        int h = fm.getAscent();
        return new Dimension(w, h);
    }

    /**
     * Returns the dimensions a given string would have if rendered into the
     * specified component.
     *
     * @param c      the component
     * @param string the string
     * @return the string's dimensions
     */
    public static Dimension getStringDimension(JComponent c, String string) {
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int w = fm.stringWidth(string);
        int h = fm.getAscent();
        return new Dimension(w, h);
    }

    /**
     * Draws a right-justified string into the given graphics context.
     *
     * @param g      the graphics context
     * @param x      the right-justified x coordinate
     * @param y      the baseline y coordinate
     * @param string the string to render
     */
    // does not appear to be called from anywhere
    @Deprecated
    public static void drawRightJustifiedString(Graphics g, int x, int y,
                                                String string) {
        Dimension d = getStringDimension(g, string);
        g.drawString(string, x - d.width, y);
    }

    /**
     * Draws a string into the given graphics context, centered in the
     * specified rectangle.
     *
     * @param g      the graphics context
     * @param r      the rectangle
     * @param string the string to render
     */
    public static void drawCenteredString(Graphics g, Rectangle r, String string) {
        drawCenteredString(g, r.x, r.y, r.width, r.height, string);
    }

    /**
     * Draws a string into the given graphics context, centered in the
     * specified rectangle.
     *
     * @param g      the graphics context
     * @param x      x-coordinate of top left of the rectangle
     * @param y      y-coordinate of top left of the rectangle
     * @param w      width of the rectangle
     * @param h      height of the rectangle
     * @param string the string to render
     */
    public static void drawCenteredString(Graphics g, int x, int y, int w, int h,
                                          String string) {
        Dimension d = getStringDimension(g, string);
        int offx = x + (w >> 1) - (d.width >> 1);
        int offy = y + (h >> 1) + (d.height >> 1);
        g.drawString(string, offx, offy);
    }

    /**
     * Centers and draws the string representations of an array of objects,
     * into the given graphics context, based on the specified rectangle.
     *
     * @param g      the graphics context
     * @param x      x-coordinate of top left of the rectangle
     * @param y      y-coordinate of top left of the rectangle
     * @param w      width of the rectangle
     * @param h      height of the rectangle
     * @param string the objects
     */
    // Does not appear to be called from anywhere
    @Deprecated
    public static void drawCenteredStrings(Graphics g, int x, int y, int w, int h,
                                           Object[] string) {
        Dimension d = getStringDimension(g, string[0].toString());
        int totalHeight = (string.length - 1) * d.height;
        int offy = y + (h >> 1) - (totalHeight >> 1);// + (d.height>>1);
        for (int i = 0; i < string.length; i++) {
            d = getStringDimension(g, string[i].toString());
            int offx = x + (w >> 1) - (d.width >> 1);
            g.drawString(string[i].toString(), offx, offy);
            offy += d.height;
        }
    }

    /**
     * Centers and draws an image into the given graphics context, based on
     * the specified rectangle.
     *
     * @param g     the graphics context
     * @param r     the rectangle to center on
     * @param image the image
     */
    // Does not appear to be called from anywhere
    @Deprecated
    public static void drawCenteredImageIcon(Graphics g, Rectangle r,
                                             ImageIcon image) {
        int offx = r.x + (r.width >> 1) - (image.getIconWidth() >> 1);
        int offy = r.y + (r.height >> 1) - (image.getIconHeight() >> 1);
        g.drawImage(image.getImage(), offx, offy, null);
    }

    private static final Color SOFT_BLACK = new Color(0, 0, 0, 100);
    private static final Color SOFT_WHITE = new Color(255, 255, 255, 100);

    /**
     * Draws a shape into the given graphics context, with the specified color,
     * but with a stroke color that has been softened. Specify dark as true
     * to use a soft color based on muted black; false for muted white.
     *
     * @param g         the graphics context
     * @param shape     the shape to render
     * @param mainColor main fill color
     * @param dark      true for muted black, false for muted white
     */
    // does not appear to be called from anywhere
    @Deprecated
    public static void drawSoftenedShape(Graphics2D g, Shape shape,
                                         Color mainColor, boolean dark) {
        Color softColor = dark ? SOFT_BLACK : SOFT_WHITE;
        g.setColor(mainColor);
        g.fill(shape);
        g.setColor(softColor);
        g.draw(shape);
    }

    /**
     * Renders an overlay image onto a base image and returns the result.
     *
     * @param base    the base image
     * @param overlay the overlay image
     * @return the combined image
     */
    public static ImageIcon overlayImages(ImageIcon base, ImageIcon overlay) {
        BufferedImage bi = new BufferedImage(base.getIconWidth(),
                                             base.getIconHeight(),
                                             BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.drawImage(base.getImage(), 0, 0, null);
        g.drawImage(overlay.getImage(), 0, 0, null);
        return new ImageIcon(bi);
    }

    /**
     * Rotates a point around the specified center by the given number
     * of radians. Note that positive radians rotate counter-clockwise.
     *
     * @param point        the point to rotate
     * @param center       the center about which to rotate
     * @param angleRadians the number of radians to rotate
     * @return the location of the rotated point
     */
    public static Point2D rotate(Point2D point, Point2D center,
                                 double angleRadians) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(angleRadians, center.getX(), center.getY());
        return transform.transform(point, new Point2D.Double());
    }

    /**
     * Returns the point that lies midway between the two given points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the midway point
     */
    public static Point midPoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x) >> 1, (p1.y + p2.y) >> 1);
    }

    /**
     * Parses the given string as comma-separated integer x and y coordinates
     * and returns the corresponding point.
     *
     * @param s the string to parse
     * @return the corresponding point
     */
    public static Point asPoint(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        int x = Integer.valueOf(st.nextToken()).intValue();
        int y = Integer.valueOf(st.nextToken()).intValue();
        return new Point(x, y);
    }

    /**
     * Returns a point that lies the specified distance along a given
     * line segment.
     *
     * @param p1          one end of the line segment
     * @param p2          the other end of the line segment
     * @param pixelsShort distance along the line, from the first point
     * @return the point on the line
     * @deprecated in deference to {@link #getPointOnLine}
     */
    @Deprecated
    public static Point shortenedLineFar(Point p1, Point p2, int pixelsShort) {
        Polar theLine = new Polar(p1, p2);
        int length = theLine.getLength() - pixelsShort;
        if (length < 0) {
            length = 0;
        }
        theLine.setLength(length);
        return theLine.getRect();
    }

    /**
     * Returns a copy of the given image.
     *
     * @param icon the image to copy
     * @return a copy of the image
     */
    // does not appear to be called from anywhere
    @Deprecated
    public static ImageIcon copyImageIcon(ImageIcon icon) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(),
                                             icon.getIconHeight(),
                                             BufferedImage.TYPE_4BYTE_ABGR);
        bi.getGraphics().drawImage(icon.getImage(), 0, 0, null);
        return new ImageIcon(bi);
    }

    /**
     * Converts the given component to an image.
     *
     * @param component the component
     * @return the image
     */
    @Deprecated
    public static ImageIcon componentToIcon(JComponent component) {
        Dimension s = component.getPreferredSize();
        BufferedImage bi = new BufferedImage(s.width, s.height,
                                             BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bi.getGraphics();
        component.paint(g);
        return new ImageIcon(bi);
    }

    /**
     * Saves the given image as a PNG file.
     *
     * @param imageIcon the image to save
     * @param file      the output file
     * @return true if successful
     */
    public static boolean saveImageToPNG(ImageIcon imageIcon, File file) {
        return saveImageToFile(imageIcon, file, "PNG");
    }

    /**
     * Saves the given image as a JPG file.
     *
     * @param imageIcon the image to save
     * @param file      the output file
     * @return true if successful
     */
    public static boolean saveImageToJPG(ImageIcon imageIcon, File file) {
        return saveImageToFile(imageIcon, file, "JPG");
    }

    private static boolean saveImageToFile(ImageIcon imageIcon, File file,
                                           String type) {
        try {
            // Why this is necessary, I have no idea, but it solves my problem
            // with blank images being written
            BufferedImage bi = new BufferedImage(imageIcon.getIconWidth(),
                                                 imageIcon.getIconHeight(),
                                                 BufferedImage.TYPE_4BYTE_ABGR);
            bi.getGraphics().drawImage(imageIcon.getImage(), 0, 0, null);

            FileOutputStream fileoutputstream = new FileOutputStream(file);
            ImageIO.write(bi, type, fileoutputstream);

            fileoutputstream.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 60 degrees expressed as radians.
     */
    protected static final double radians_degrees60 = (Math.PI * 60.0) / 180.0;
}