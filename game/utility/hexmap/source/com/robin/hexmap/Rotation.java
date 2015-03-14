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
package com.robin.hexmap;

public class Rotation {
	public static final int POSITIONS = 6;
	
	public static final int CLOCKWISE = 1;
	public static final int COUNTERCLOCKWISE = -1;
	
	private int position;
	
	public Rotation() {
		this(0);
	}
	public Rotation(int position) {
		this.position = position;
	}
	public void rotate(int direction) {
		if (direction==CLOCKWISE || direction==COUNTERCLOCKWISE) {
			position += direction;
			if (position<0) {
				position = POSITIONS - 1;
			}
			else {
				position %= POSITIONS;
			}
		}
		else {
			throw new IllegalArgumentException("direction "+direction+" is not a valid argument for Rotation.rotate(int direction)");
		}
	}
	public int getCWTurns() {
		return position;
	}
}