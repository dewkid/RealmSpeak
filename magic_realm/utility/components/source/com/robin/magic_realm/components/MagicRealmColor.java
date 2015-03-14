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

import java.awt.Color;

public class MagicRealmColor {
	public static final Color BLACK			= new Color(  0,  0,  0);
	public static final Color BLUE			= new Color(119,199,234);
	public static final Color LIGHTBLUE		= new Color(222,239,247);
	public static final Color GREEN			= new Color( 99,180, 98);
	public static final Color LIGHTGREEN	= new Color(186,231,164);
	public static final Color LIMEGREEN		= new Color(159,214, 98);
	public static final Color FORESTGREEN	= new Color( 90,180, 75);
	public static final Color RED			= new Color(214, 93, 70);
	public static final Color PINK			= new Color(239,190,221);
	public static final Color ORANGE		= new Color(214,148, 72);
	public static final Color LIGHTORANGE	= new Color(239,193,142);
	public static final Color PURPLE		= new Color(180,145,180);
	public static final Color YELLOW		= new Color(239,235,123);
	public static final Color PALEYELLOW	= new Color(255,255,200);
	public static final Color PEACH			= new Color(229,186,136);
	public static final Color TAN			= new Color(198,167, 85);
	public static final Color GOLD			= new Color(239,204,110);
	public static final Color BROWN			= new Color(194,128, 80);
	public static final Color GRAY			= new Color(214,214,214);
	public static final Color DARKGRAY		= new Color(170,170,170);
	public static final Color WHITE			= new Color(255,255,255);
	
	public static final Color DISCOVERY_HIGHLIGHT_COLOR = new Color(102,255,153);
	
	public static final Color CHIT_COMMITTED = Color.blue;
	public static final Color CHIT_ALERTED = Color.green;
	public static final Color CHIT_FATIGUED = Color.orange;
	public static final Color CHIT_WOUNDED = Color.red;
//	public static final Color CHIT_COMMITTED = new Color(0,0,255,100); //Color.blue;
//	public static final Color CHIT_ALERTED = new Color(0,255,0,100); //Color.green;
//	public static final Color CHIT_FATIGUED = new Color(255,255,0,100); //Color.orange;
//	public static final Color CHIT_WOUNDED = new Color(255,0,0,100); //Color.red;
	
	public static Color getColor(String string) {
		string = string.trim().toUpperCase();
		if (string.equals("BLACK")) {
			return BLACK;
		}
		else if (string.equals("BLUE")) {
			return BLUE;
		}
		else if (string.equals("LIGHTBLUE")) {
			return LIGHTBLUE;
		}
		else if (string.equals("GREEN")) {
			return GREEN;
		}
		else if (string.equals("LIGHTGREEN")) {
			return LIGHTGREEN;
		}
		else if (string.equals("LIMEGREEN")) {
			return LIMEGREEN;
		}
		else if (string.equals("FORESTGREEN")) {
			return FORESTGREEN;
		}
		else if (string.equals("RED")) {
			return RED;
		}
		else if (string.equals("PINK")) {
			return PINK;
		}
		else if (string.equals("ORANGE")) {
			return ORANGE;
		}
		else if (string.equals("LIGHTORANGE")) {
			return LIGHTORANGE;
		}
		else if (string.equals("PURPLE")) {
			return PURPLE;
		}
		else if (string.equals("YELLOW")) {
			return YELLOW;
		}
		else if (string.equals("PEACH")) {
			return PEACH;
		}
		else if (string.equals("TAN")) {
			return TAN;
		}
		else if (string.equals("GOLD")) {
			return GOLD;
		}
		else if (string.equals("BROWN")) {
			return BROWN;
		}
		else if (string.equals("GRAY")) {
			return GRAY;
		}
		else if (string.equals("DARKGRAY")) {
			return DARKGRAY;
		}
		else if (string.equals("WHITE")) {
			return WHITE;
		}
		else if (string.equals("PALEYELLOW")) {
			return PALEYELLOW;
		}
		throw new IllegalArgumentException("Invalid color: "+string);
	}
}