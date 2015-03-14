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

import java.awt.Color;

public class HexGuide {
	private String name;
	private Token fromToken;
	private Token toToken;
	private Color color;
	private boolean showName;
	private boolean showDistance;
	
	public HexGuide(String name,Token from,Token to,Color c,boolean showName,boolean showDistance) {
		this.name = name;
		this.fromToken = from;
		this.toToken = to;
		this.color = c;
		this.showName = showName;
		this.showDistance = showDistance;
	}
	public HexMapPoint getFrom() {
		return fromToken.getPosition();
	}
	public HexMapPoint getTo() {
		return toToken.getPosition();
	}
	public String getName() {
		return name;
	}
	public Color getColor() {
		return color;
	}
	public boolean getShowName() {
		return showName;
	}
	public boolean getShowDistance() {
		return showDistance;
	}
}