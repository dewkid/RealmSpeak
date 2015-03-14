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

/**
 * New coordinate class for handling polar coordinates, with easy conversions
 * to rectangular coordinate systems.
 *
 * @author		Robin Warren
 */
public class Polar {
	public static final int MAX_DEGREES = 360;
	private int length;
	private int angle;
	private Point origin;
	private Point rect;
	
	// lookup tables
	public static double cos[] = null;
	public static double sin[] = null;
	
	public Polar() {
		init();
		length=0;
		angle=0;
		origin = new Point(0,0);
		setRect();
	}
	
	public Polar(int x,int y,int r,int theta) {
		init();
		length = r;
		angle = theta;
		normalizeAngle();
		origin = new Point(x,y);
		setRect();
	}
	
	public Polar(Polar p) {
		init();
		length = p.length;
		angle = p.angle;
		origin = new Point(p.origin);
		rect = new Point(p.getRect());
	}
	
	public Polar(Point center,Point coord) {
		init();
		int dx = coord.x - center.x;
		int dy = coord.y - center.y;
		if (dx==0 && dy==0) {
			angle = 0;
			length = 0;
		}
		else {
			length = (int)Math.sqrt((dx*dx)+(dy*dy));
			angle = toDegrees(Math.atan2((double)dy,(double)dx));
		}
		origin = center;
		setRect();
	}
	
	public void normalize() {
		if (rect==null)
			setRect();
			
		origin.x = rect.x;
		origin.y = rect.y;
		length=0;
	}
	
	public static int normalAngle(int theta) {
		while(theta<0) theta+=360;
		return theta%360;
	}
	
	private void normalizeAngle() {
		angle = normalAngle(angle);
	}
		
	public static double toRadians(int deg) {
		return (Math.PI * (double)deg)/180;
	}
	
	public static int toDegrees(double rad) {
		return normalAngle((int)(((double)rad * 180)/Math.PI));
	}
	
	private void init() {
		if (cos==null) {
			cos = new double[MAX_DEGREES];
			for (int i=0;i<MAX_DEGREES;i++) {
				cos[i] = Math.cos(toRadians(i));
			}
		}
		if (sin==null) {
			sin = new double[MAX_DEGREES];
			for (int i=0;i<MAX_DEGREES;i++) {
				sin[i] = Math.sin(toRadians(i));
			}
		}
	}
	
	private void setRect() {
		rect = getPoint();
	}
		
	private Point getPoint() {
		normalizeAngle();
		int x = ((int)((double)length * cos[angle])) + origin.x;
		int y = ((int)((double)length * sin[angle])) + origin.y;
		return new Point(x,y);
	}
	
	public static Polar translate(Polar from,Polar to,int percent) {
		percent = percent<  0 ?   0 : percent;
		percent = percent>100 ? 100 : percent;
		
		if (percent==0) {
			return from;
		}
		else if (percent==100) {
			return to;
		}
		else {
			if (from!=null && to!=null) {
				Point fromR = from.getRect();
				Point toR = to.getRect();
				int dx = toR.x - fromR.x;
				int dy = toR.y - fromR.y;
				
				int currentX = fromR.x + ((dx*percent)/100);
				int currentY = fromR.y + ((dy*percent)/100);
				
				return new Polar(currentX,currentY,0,0);
			}
		}
		return null;
	}
	
	public static Polar midpoint(Polar from,Polar to) {
		if (from!=null && to!=null) {
			Point fromR = from.getRect();
			Point toR = to.getRect();
			int currentX = (fromR.x + toR.x)/2;
			int currentY = (fromR.y + toR.y)/2;
			
			return new Polar(currentX,currentY,0,0);
		}
		return null;
	}
	
	public void flip() {
		addAngle(180);
	}
	
	// setters
	public void setLength(int val) {length=val;rect=null;}
	public void setAngle(int val) {angle=val;normalizeAngle();rect=null;}
	public void addAngle(int val) {angle+=val;normalizeAngle();rect=null;}
	public void setOrigin(Point o) {origin=o;rect=null;}
	public void setOrigin(int x,int y) {origin=new Point(x,y);rect=null;}
	
	// getters
	public int getLength() {return length;}
	public int getAngle() {return angle;}
	public Point getOrigin() {return origin;}
	public int getOriginX() {return origin.x;}
	public int getOriginY() {return origin.y;}
	public int getX() {if (rect==null) setRect(); return rect.x;}
	public int getY() {if (rect==null) setRect(); return rect.y;}
	public Point getRect() {if (rect==null) setRect(); return rect;}
	
	public String toString() {
		return "Polar("+length+",<"+angle+")";
	}
}