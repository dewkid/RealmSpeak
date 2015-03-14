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

import java.text.NumberFormat;

public class ForceVector {
	
	private double force;
	private double direction;
	private double dx;
	private double dy;
	
	/**
	 * Force is an arbitrary value that you choose.  Direction is in degrees from 0-360, where 0 is pointing to the right,
	 * and 90 is pointing straight up.
	 */
	public ForceVector(double force,double direction) {
		this.force = force;
		this.direction = direction;
		initRectangularCoordinates();
	}
	/**
	 * This constructor is used internally, and should NOT be used by the application.  No validation is done here,
	 * so the values are assumed to be accurate.
	 */
	private ForceVector(double force,double direction,double dx,double dy) {
		this.force = force;
		this.direction = direction;
		this.dx = dx;
		this.dy = dy;
	}
	/**
	 * @return	A new ForceVector representing the addition of the two ForceVector objects
	 */
	public ForceVector add(ForceVector fv) {
		// Get the final position, by simply adding the two rectanglular coordinates
		// We can do this, because ForceVectors all have the same origin: (0,0)
		double fx = dx+fv.getDx();
		double fy = dy+fv.getDy();
		
		// Now we simply need to identify the new force and direction
		double fForce = Math.sqrt((fx*fx)+(fy*fy));
		double fDirection = toDegrees(Math.atan2(fy,fx));
		
		return new ForceVector(fForce,fDirection,fx,fy);
	}
	private void normalizeAngle() {
		while(direction<0.0) direction+=360.0;
		while(direction>=360.0) direction-=360.0;
	}
	private void initRectangularCoordinates() {
		normalizeAngle();
		double radians = toRadians(direction);
		dx = force*Math.cos(radians);
		dy = force*Math.sin(radians);
	}
	public String toString() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return "p("+nf.format(force)+","+nf.format(direction)+"o)==r("+nf.format(dx)+","+nf.format(dy)+")";
	}
	public double getDirection() {
		return direction;
	}
	public double getDx() {
		return dx;
	}
	public double getDy() {
		return dy;
	}
	public double getForce() {
		return force;
	}
	public static double toDegrees(double rad) {
		return -(rad * 180)/Math.PI; // The minus sign flips the angle so that 90 degrees is pointing straight up
	}
	public static double toRadians(double deg) {
		return -(Math.PI * deg)/180.0; // The minus sign flips the angle so that 90 degrees is pointing straight up
	}
	public static void main(String[] args) {
		System.out.println(new ForceVector(1.0,0.0));
		System.out.println(new ForceVector(1.0,90.0));
		System.out.println(new ForceVector(1.0,180.0));
		System.out.println(new ForceVector(1.0,270.0));
		
		ForceVector fv1 = new ForceVector(1.0,0.0);
		ForceVector fv2 = new ForceVector(1.0,90.0);
		System.out.println(fv1.add(fv2));
	}
}