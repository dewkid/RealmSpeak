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

public class HexTag {
	private String tagString;
	private Color color;
	
	public HexTag(String s,Color c) {
		this.tagString = s;
		this.color = c;
	}
	public String getTagString() {
		return tagString;
	}
	public Color getColor() {
		return color;
	}
	
	public void merge(HexTag tag) {
		tagString = tagString+","+tag.getTagString();
	}
}