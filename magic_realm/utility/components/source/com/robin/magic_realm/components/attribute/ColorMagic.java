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

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import com.robin.general.swing.ImageCache;

public class ColorMagic implements Comparable {
	public static final int WHITE = 1;
	public static final int GRAY = 2;
	public static final int GOLD = 3;
	public static final int PURPLE = 4;
	public static final int BLACK = 5;
	
	private int color;
	private boolean infinite;
	public ColorMagic(int color,boolean infinite) {
		this.color = color;
 		this.infinite = infinite;
 		if (color<WHITE || color>BLACK) {
 			throw new IllegalArgumentException("Invalid color");
 		}
	}
	public boolean equals(Object o1) {
		if (o1 instanceof ColorMagic) {
			ColorMagic cm = (ColorMagic)o1;
			return (cm.color==color && cm.infinite==infinite);
		}
		return false;
	}
	public boolean sameColorAs(ColorMagic other) {
		return other.color==color;
	}
	public int compareTo(Object o1) {
		int ret=0;
		if (o1 instanceof ColorMagic) {
			ColorMagic cm = (ColorMagic)o1;
			ret = color - cm.color;
		}
		return ret;
	}
	public int getColorNumber() {
		return color;
	}
//	public boolean compatibleWith(MagicChit chit) {
//		return (chit.getMagicNumber()==color);
//	}
	public String getColorName() {
		switch(color) {
			case WHITE:			return "White";
			case GRAY:			return "Grey";
			case GOLD:			return "Gold";
			case PURPLE:		return "Purple";
			case BLACK:			return "Black";
		}
		return null; // this shouldn't happen
	}
	public String toString() {
		return "ColorMagic:"+getColorName()+(infinite?"":"(chit)");
	}
	public void setInfinite(boolean val) {
		infinite = val;
	}
	/**
	 * @return		true if the color is infinite (like a treasure or tile)
	 */
	public boolean isInfinite() {
		return infinite;
	}
	public ImageIcon getSmallIcon() {
		Image i = getIcon().getImage();
		return new ImageIcon(i.getScaledInstance(24,24,Image.SCALE_SMOOTH));
	}
	public ImageIcon getIcon() {
		switch(color) {
			case WHITE:			return infinite?ImageCache.getIcon("colormagic/white"):ImageCache.getIcon("colormagic/whitechit");
			case GRAY:			return infinite?ImageCache.getIcon("colormagic/gray"):ImageCache.getIcon("colormagic/graychit");
			case GOLD:			return infinite?ImageCache.getIcon("colormagic/gold"):ImageCache.getIcon("colormagic/goldchit");
			case PURPLE:			return infinite?ImageCache.getIcon("colormagic/purple"):ImageCache.getIcon("colormagic/purplechit");
			case BLACK:			return infinite?ImageCache.getIcon("colormagic/black"):ImageCache.getIcon("colormagic/blackchit");
		}
		return null; // this shouldn't happen
	}
	public Color getColor() {
		switch(color) {
			case WHITE:			return Color.white;
			case GRAY:			return Color.gray;
			case GOLD:			return Color.yellow;
			case PURPLE:			return Color.magenta;
			case BLACK:			return Color.black;
		}
		return null;
	}
	public static ColorMagic makeColorMagic(String colorName,boolean infinite) {
		if (colorName!=null) {
			int color = 0;
			colorName = colorName.toLowerCase();
			if ("white".equals(colorName)) {
				color = WHITE;
			}
			else if ("gray".equals(colorName) || "grey".equals(colorName)) { // Support both spellings
				color = GRAY;
			}
			else if ("gold".equals(colorName)) {
				color = GOLD;
			}
			else if ("purple".equals(colorName)) {
				color = PURPLE;
			}
			else if ("black".equals(colorName)) {
				color = BLACK;
			}
			if (color>0) {
				return new ColorMagic(color,infinite);
			}
		}
		return null;
	}
	public static String getColorName(String colorName) {
		ColorMagic cm = ColorMagic.makeColorMagic(colorName,true);//.getColorName();
		if (cm!=null) {
			return cm.getColorName();
		}
		return colorName; // no conversion
	}
}