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

public class StarShape extends Polygon {
	public StarShape(int centerx,int centery,int points,int radius) {
		super();
		init(centerx,centery,points,radius);
	}
	public void init(int centerx,int centery,int points,int radius) {
		Point center = new Point(centerx,centery);
		double angle = 270; // always start first point on top
		Polar p = new Polar(centerx,centery,radius,(int)angle);
		double angleSize = 360.0/points;
		Point[] pointPoints = new Point[points];
		for (int i=0;i<points;i++) {
			p.setAngle((int)angle);
			pointPoints[i] = p.getRect();
			angle += angleSize;
		}
		for (int i=0;i<pointPoints.length;i++) {
			addPoint(pointPoints[i].x,pointPoints[i].y);
			AveragePoint ap = new AveragePoint(center);
			ap.addPoint(center);
			ap.addPoint(pointPoints[i]);
			ap.addPoint(pointPoints[(i+1)%pointPoints.length]);
			addPoint(ap.x,ap.y);
		}
	}
}