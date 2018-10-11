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

import java.awt.*;

// Not used in the codebase.
@Deprecated
public class Line {
    public Point p1;
    public Point p2;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean isStraightLine() {
        return p1.x == p2.x || p1.y == p2.y;
    }

    public boolean isEndpoint(Point p) {
        return p.equals(p1) || p.equals(p2);
    }

    public boolean contains(Point p) {
        if (isStraightLine()) {
            if ((p1.x == p2.x && p.x == p2.x && between(p.y, p1.y, p2.y)) ||
                    (p1.y == p2.y && p.y == p2.y && between(p.x, p1.x, p2.x))) {
                return true;
            }
        } else {
            // TODO Finish this code someday ...
        }
        return false;
    }

    private boolean between(int n, int n1, int n2) {
        return (n1 < n2 ? (n >= n1 && n <= n2) : (n >= n2 && n <= n1));
    }
}