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
package com.robin.magic_realm.components;

import java.awt.*;

import javax.swing.UIManager;

public class EmptyTileComponent extends TileComponent {
	private static final Stroke POSITION_HIGHLIGHT_STROKE
		= new BasicStroke(10,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	
	private static final Color INVISIBLE_COLOR = UIManager.getColor("ScrollPane.background");
	
	private boolean valid = false;
	public EmptyTileComponent() {
		super();
		lightColor = Color.darkGray;
		darkColor = Color.darkGray;
	}
	public String toString() {
		return "EmptyTileComponent:"+valid;
	}
	public void setValidPosition(boolean val) {
		valid = val;
	}
	public boolean isValidPosition() {
		return valid;
	}
	public void paintTo(Graphics g,int x,int y,int w,int h) {
		paint(g.create(x,y,w,h));
	}
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		if (valid) {
			Shape shape = getShape(10,10,getChitSize()-20);
			g.setStroke(POSITION_HIGHLIGHT_STROKE);
			g.setColor(Color.yellow);
			g.draw(shape);
		}
		else {
			// for debugging
			Shape shape = getShape(10,10,getChitSize()-20);
			g.setStroke(POSITION_HIGHLIGHT_STROKE);
			g.setColor(INVISIBLE_COLOR);
			g.draw(shape);
		}
	}
	public boolean isLightSideUp() {
		return true;
	}
	public boolean isDarkSideUp() {
		return false;
	}
	public ClearingDetail findClearing(Point relativePoint) {
		return null;
	}
}