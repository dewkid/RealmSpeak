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

import com.robin.magic_realm.components.MagicRealmColor;

public class ChatStyle {
	
	public static ChatStyle[] styles = {
		new ChatStyle("blue",Color.blue),
		new ChatStyle("fgreen",MagicRealmColor.FORESTGREEN),
		new ChatStyle("brown",MagicRealmColor.BROWN),
		new ChatStyle("red",Color.red),
		new ChatStyle("darkgray",MagicRealmColor.DARKGRAY),
		new ChatStyle("orange",MagicRealmColor.ORANGE),
		new ChatStyle("purple",MagicRealmColor.PURPLE),
		new ChatStyle("green",MagicRealmColor.GREEN),
		new ChatStyle("black",Color.black),
	};
	
	private String styleName;
	private Color color;
	
	public ChatStyle(String styleName,Color color) {
		this.styleName = styleName;
		this.color = color;
	}
	public String getStyleName() {
		return styleName;
	}
	public Color getColor() {
		return color;
	}
}