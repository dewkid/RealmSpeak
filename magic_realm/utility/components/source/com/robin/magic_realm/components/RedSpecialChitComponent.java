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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.utility.Constants;

public class RedSpecialChitComponent extends StateChitComponent {
	protected RedSpecialChitComponent(GameObject obj) {
		super(obj);
		darkColor = MagicRealmColor.RED;
	}
	public String getName() {
	    return RED_SPECIAL;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		TextType tt;
		
		if (getGameObject().hasThisAttribute(Constants.ALWAYS_VISIBLE) || isFaceUp()) {
			int y=5;
			String name = gameObject.getName();
			tt = new TextType(name,getChitSize()-4,"BOLD");
			tt.draw(g,2,y,Alignment.Center);
			
			y += tt.getHeight(g);
			
			String clearing = getAttribute("this","clearing");
			tt = new TextType(clearing,getChitSize()-4,"BOLD");
			tt.draw(g,2,y,Alignment.Center);
		}
	}
	/**
	 * Override method so that red specials can be added to tile
	 */
	protected void explode() {
		addPileToTile();
	}
//	/**
//	 * Override method so that red specials can be added to tile
//	 */
//	public void setFacing(String val) {
//		super.setFacing(val);
//		if (isFaceUp()) {
//			addPileToTile();
//		}
//	}
	/**
	 * Remaps the hold of the red special chit to the tile
	 */
	public void addPileToTile() {
		if (gameObject.getHoldCount()>0) {
			GameObject tile = gameObject.getHeldBy();
			Collection hold = new ArrayList(gameObject.getHold()); // this construction is necessary to prevent concurrent modification errors
			for (Iterator h=hold.iterator();h.hasNext();) {
				GameObject chit = (GameObject)h.next();
				StateChitComponent state = (StateChitComponent)RealmComponent.getRealmComponent(chit);
				state.setFaceUp();
				tile.add(chit);
			}
		}
	}
}