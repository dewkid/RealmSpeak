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

/**
 * A Phase chit is created by a Phase Spell, and is placed into the target's inventory.
 * It can be activated (never traded or dropped), and expires at the end of the phase.
 */
public class PhaseChitComponent extends SquareChitComponent {
	protected PhaseChitComponent(GameObject go) {
		super(go);
		lightColor = MagicRealmColor.YELLOW;
		darkColor = MagicRealmColor.YELLOW;
	}
	public String getLightSideStat() {
		return "this";
	}

	public String getDarkSideStat() {
		return "this";
	}

	public int getChitSize() {
		return M_CHIT_SIZE;
	}

	public String getName() {
		return getGameObject().getName();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int pos = 2;
		
		// Draw the title
		TextType tt = new TextType("Phase Chit",M_CHIT_SIZE-10,"TITLE");
		tt.draw(g,5,pos,Alignment.Center);
		pos += (tt.getHeight(g)*2);
		
		String desc = (String)gameObject.getAttribute("this","text");
		if (desc!=null) {
			tt = new TextType(desc,M_CHIT_SIZE-10,"NORMAL");
			tt.draw(g,5,pos,Alignment.Center);
		}
	}
}