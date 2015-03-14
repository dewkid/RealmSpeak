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
import java.util.*;

/**
 * A AveragePoint is the center point of all the points in its
 * collection.
 */
public class AveragePoint extends Point {
	protected ArrayList points;
	public AveragePoint(int x,int y) {
		super(x,y);
		points = new ArrayList();
		addPoint(x,y);
	}
	public AveragePoint(Point p) {
		super(p);
		points = new ArrayList();
		addPoint(p);
	}
	public void addPoint(int valX,int valY) {
		addPoint(new Point(valX,valY));
	}
	public void addPoint(Point p) {
		points.add(p);
		refresh();
	}
	public Polygon getPolygon() {
		Polygon poly = new Polygon();
		for (Iterator i=points.iterator();i.hasNext();) {
			Point p = (Point)i.next();
			poly.addPoint(p.x,p.y);
		}
		return poly;
	}
	/**
	 * This will update the x and y values to reflect an average
	 * of all the points (equals the center point)
	 */
	private void refresh() {
		int tx = 0;
		int ty = 0;
		int n = 0;
		for (Iterator i=points.iterator();i.hasNext();) {
			Point p = (Point)i.next();
			tx += p.x;
			ty += p.y;
			n++;
		}
		this.x = tx/n;
		this.y = ty/n;
	}
	public boolean equals(AveragePoint p) {
		return (p.x==x && p.y==y);
	}
}