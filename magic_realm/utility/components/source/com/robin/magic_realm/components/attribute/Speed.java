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
package com.robin.magic_realm.components.attribute;

public class Speed implements Comparable {
	
	private static int DEFAULT_SPEED = 8; // Infinitely slow, for all intents and purposes
	
	private boolean infinitelySlow = false;
	private int num = DEFAULT_SPEED; // default
	
	public Speed() {
		num = DEFAULT_SPEED;
		infinitelySlow = true;
	}
	public Speed(String val) {
		this();
		if (val!=null) {
			num = Integer.valueOf(val).intValue(); // NumberFormatException here is desired if val is not a number!
			infinitelySlow = false;
		}
	}
	public Speed(int val) {
		this();
		num = val;
		infinitelySlow = false;
	}
	public Speed(Integer val) {
		this();
		if (val!=null) {
			num = val.intValue();
			infinitelySlow = false;
		}
	}
	public Speed(Integer val,int modifier) {
		this(val);
		if (!infinitelySlow) {
			num += modifier;
		}
	}
	public boolean isInfinitelySlow() {
		return infinitelySlow;
	}
	public String toString() {
		if (infinitelySlow) {
			return "Not Moving";
		}
		return "Speed "+num;
	}
	public String getSpeedString() {
		if (!infinitelySlow && num>0) {
			return String.valueOf(num);
		}
		return "";
	}
	public int compareTo(Object o1) {
		int ret = 0;
		if (o1 instanceof Speed) {
			Speed s = (Speed)o1;
			ret = num - s.num;
		}
		return ret;
	}
	public int getNum() {
		return num;
	}
	public boolean equals(Object o1) {
		if (o1 instanceof Speed) {
			return equalTo((Speed)o1);
		}
		return false;
	}
	public boolean equalTo(Speed other) {
		return num == other.getNum();
	}
	public boolean fasterThan(Speed speed) {
		if (infinitelySlow) {
			return false;
		}
		if (speed.isInfinitelySlow()) {
			return true;
		}
		return num<speed.getNum();
	}
	public boolean fasterThanOrEqual(Speed speed) {
		if (infinitelySlow) {
			return false;
		}
		if (speed.isInfinitelySlow()) {
			return true;
		}
		return num<=speed.getNum();
	}
}