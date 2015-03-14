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

import java.awt.Graphics;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;

// This isn't a "REAL" RealmComponent, because it doesn't wrap a GameObject.  I'm just borrowing the SquareChitComponent paint logic.
public class WeatherChit extends StateChitComponent {
	private int val;
	public WeatherChit(int val) {
		super(GameObject.createEmptyGameObject());
		this.val = val;
		darkColor = MagicRealmColor.RED;
	}
	public String getName() {
	    return "Weather Chit";
	}
	public boolean isLightSideUp() {
		return false;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		TextType tt = new TextType(String.valueOf(val),getChitSize()-4,"STAT_BLACK");
		tt.draw(g,2,(getChitSize()>>2)+2,Alignment.Center);
	}
}