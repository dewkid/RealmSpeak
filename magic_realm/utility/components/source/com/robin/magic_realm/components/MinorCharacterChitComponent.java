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

public class MinorCharacterChitComponent extends RoundChitComponent {

	protected MinorCharacterChitComponent(GameObject go) {
		super(go);
		lightColor = MagicRealmColor.GOLD;
		darkColor = MagicRealmColor.BROWN;
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
		
		String icon_type = gameObject.getThisAttribute(Constants.ICON_TYPE);
		String iconDir = gameObject.getThisAttribute(Constants.ICON_FOLDER);
		drawIcon(g, iconDir, icon_type, 0.7);

		int pos = (M_CHIT_SIZE>>1)-10;
		TextType tt = new TextType(getGameObject().getName(),M_CHIT_SIZE-10,"WHITE_NOTE");
		tt.draw(g,5,pos,Alignment.Center);
		pos += (tt.getHeight(g)*2);
	}
}