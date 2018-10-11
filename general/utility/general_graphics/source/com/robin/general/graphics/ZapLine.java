/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.graphics;

import java.awt.*;
import java.util.Random;

// Not used in the codebase
@Deprecated
public class ZapLine {

    protected static Random random = new Random();

    protected int twiggle = 1;
    protected Point start;
    protected Point end;
    protected Color color = Color.red;
    protected Point offset;

    protected Point[] node = null;

    public ZapLine(Point p1, Point p2) {
        this(p1.x, p1.y, p2.x, p2.y);
//        start = p1;
//        end = p2;
    }

    public ZapLine(int x1, int y1, int x2, int y2) {
        start = new Point(x1, y1);
        end = new Point(x2, y2);
        offset = new Point(0, 0);
//        int dx = Math.abs(start.x-end.x);
//        int dy = Math.abs(start.y-end.y);
//        int div = (int)Math.sqrt((dx*dx)+(dy*dy));
//        setDivisions(div/10);
//        setTwiggle(div/10);
        setDivisions(6);
        setTwiggle(10);
    }

    public void setDivisions(int div) {
        if (div > 0) {
            node = new Point[div + 1];

            int lenx = end.x - start.x;
            double dx = (double) lenx / (double) div;
            int leny = end.y - start.y;
            double dy = (double) leny / (double) div;

            for (int i = 0; i < (div + 1); i++) {
                int tx = start.x + (int) (dx * i);
                int ty = start.y + (int) (dy * i);
                node[i] = new Point(tx, ty);
            }
        }
    }

    public void setColor(Color c) {
        color = c;
    }

    public void setTwiggle(int t) {
        twiggle = t;
    }

    public int getRandomTwiggle() {
        return (random.nextInt(twiggle) - (twiggle >> 1));
    }

    public Point twiggleNode(Point aNode) {
        return new Point(aNode.x + getRandomTwiggle(), aNode.y + getRandomTwiggle());
    }

    public void _draw(Graphics g1) {
        g1.drawLine(start.x, start.y, end.x, end.y);
    }

    public void draw(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
//        g.setColor(Color.black);
//        g.drawOval(start.x-3,start.y-3,7,7);
//        g.drawOval(end.x-3,end.y-3,7,7);

        if (node != null) {
            g.setColor(color);
            g.setStroke(new BasicStroke(3));
            Point last = null;
            for (int i = 0; i < node.length; i++) {
                Point p = node[i];
                if (i > 0 && i < (node.length - 1)) {
                    p = twiggleNode(p);
                }
                if (last != null) {
//                    Point c = twiggleNode(new Point(p.x+((p.x-last.x)>>1),p.y+((p.y-last.y)>>1)));
//                    QuadCurve2D.Float f = new QuadCurve2D.Float(last.x,last.y,c.x,c.y,p.x,p.y);
//                    g.draw(f);
                    g.drawLine(offset.x + last.x, offset.y + last.y, offset.x + p.x, offset.y + p.y);
                }
                last = p;
            }
        }
    }

    /**
     * @return Returns the end.
     */
    public Point getEnd() {
        return end;
    }

    /**
     * @return Returns the start.
     */
    public Point getStart() {
        return start;
    }

    public boolean isSame(Point s, Point e) {
        return start.equals(s) && end.equals(e);
    }

    public void setOffset(Point offset) {
        this.offset = offset;
    }
}