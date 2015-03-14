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
import com.robin.magic_realm.components.utility.Constants;

public class GoldChitComponent extends StateChitComponent {
	protected GoldChitComponent(GameObject obj) {
		super(obj);
		darkColor = MagicRealmColor.YELLOW;
	}
	public String getName() {
	    return GOLD;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		TextType tt;
		
		if (getGameObject().hasThisAttribute(Constants.ALWAYS_VISIBLE) || isFaceUp()) {
			tt = new TextType("GOLD",getChitSize(),"BOLD");
			tt.draw(g,0,12,Alignment.Center);
			
			String gold = getAttribute("this","gold");
			tt = new TextType(gold,getChitSize(),"BOLD");
			tt.draw(g,0,12+tt.getHeight(g),Alignment.Center);
		}
	}
}