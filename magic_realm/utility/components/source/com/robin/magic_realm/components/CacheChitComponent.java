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
import java.awt.Graphics2D;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CacheChitComponent extends TreasureLocationChitComponent {
	public static String DEPLETED_CACHE = "depleted_cache";
	protected CacheChitComponent(GameObject obj) {
		super(obj);
		darkColor = MagicRealmColor.GOLD;
	}
	public String getName() {
	    return CACHE_CHIT;
	}
	/**
	 * Test to see if cache is empty.  If it is, have it remove itself from the clearing.
	 */
	public void testEmpty() {
		if (isEmpty()) {
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(getGameObject().getHeldBy());
			tile.getGameObject().remove(getGameObject());
			getGameObject().removeThisAttribute("clearing");
			getGameObject().setThisAttribute(DEPLETED_CACHE);
			clearOwner();
		}
	}
	public boolean isEmpty() {
		return getGameObject().getHoldCount()==0 && getGold()==0.0;
	}
	public double getGold() {
		CharacterWrapper cache = new CharacterWrapper(getGameObject());
		return cache.getGold();
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		
		TextType tt;
		
		// Draw Image
		String drawText = null;
		String icon_type;
		RealmComponent owner = getOwner();
		if (owner.isCharacter()) {
			icon_type = owner.getGameObject().getThisAttribute("icon_type");
			if (icon_type != null) {
				String iconFolder = owner.getGameObject().getThisAttribute("icon_folder");
				drawIcon(g, iconFolder, icon_type, .4);
			}
		}
		else if (owner.isNative()) {
			drawText = owner.getGameObject().getName().substring(0,1)+owner.getGameObject().getThisAttribute("rank");
		}
		else if (owner.isMonster()) {
			drawText = owner.getGameObject().getName().substring(0,6);
		}
		if (drawText!=null) {
			tt = new TextType(drawText,getChitSize(),"BOLD");
			tt.draw(g,0,15,Alignment.Center);
		}
		
		tt = new TextType("CACHE",getChitSize(),"BOLD_BLUE");
		tt.draw(g,0,2,Alignment.Center);
		
		if (getGameObject().hasThisAttribute(Constants.ALWAYS_VISIBLE) || isFaceUp()) {
			int half = getChitSize()>>1;
			
			g.setColor(BACKING);
			g.fillOval(half+5,half+5,15,15);
			
			String clearing = getAttribute("this","cache_number");
			tt = new TextType(clearing,half,"BOLD_BLUE");
			tt.draw(g,half+1,18+tt.getHeight(g),Alignment.Center);
		}
	}
}