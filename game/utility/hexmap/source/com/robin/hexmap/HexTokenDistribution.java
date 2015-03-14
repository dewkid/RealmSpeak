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

import java.awt.Rectangle;

public class HexTokenDistribution {

	private Rectangle hexRect;

	private int total = 0;
	private int current = 0;
	
	private Rectangle[] drawRect;
	
	public HexTokenDistribution(Rectangle r) {
		if (r==null) {
			throw new IllegalArgumentException("Rectangle cannot be null!");
		}
		hexRect = r;
		drawRect = null;
	}
	public void incrementTotal() {
		total++;
		updateRectangles();
	}
	public void incrementCurrent() {
		current++;
	}
	private void updateRectangles() {
		drawRect = new Rectangle[total];
		// for now, simply create a stack
		for (int i=0;i<total;i++) {
			int d = i*4;
			drawRect[i] = new Rectangle(hexRect.x-d,hexRect.y-d,hexRect.width,hexRect.height);
		}
	}
	public Rectangle getNextDrawRect() {
		if (current<drawRect.length) {
			return drawRect[current];
		}
		return null;
	}
}