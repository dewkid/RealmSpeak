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

import java.awt.*;

import com.robin.game.objects.*;
import com.robin.general.graphics.*;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.utility.Constants;

public class TreasureLocationChitComponent extends StateChitComponent {
	protected TreasureLocationChitComponent(GameObject obj) {
		super(obj);
		darkColor = MagicRealmColor.GOLD;
	}
	public String getName() {
	    return TREASURE_LOCATION;
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		
		TextType tt;
		
		if (getGameObject().hasThisAttribute(Constants.ALWAYS_VISIBLE) || isFaceUp()) {
			if (!isCacheChit()) {
				String tl = getAttribute("this","treasure_location");
				if (tl.length()==0) {
					tl = getAttribute("this","minor_tl");
				}
				tt = new TextType(StringUtilities.capitalize(tl),getChitSize(),"BOLD");
				tt.draw(g,0,12,Alignment.Center);
			
				String clearing = getAttribute("this","clearing");
				tt = new TextType(clearing,getChitSize(),"BOLD");
				tt.draw(g,0,12+tt.getHeight(g),Alignment.Center);
			}
			
			if (gameObject.hasThisAttribute(Constants.NEEDS_OPEN)) {
				tt = new TextType("CLOSED",getChitSize(),"TITLE_RED");
				tt.draw(g,0,2,Alignment.Center);
			}
			
			if (gameObject.hasThisAttribute(Constants.DESTROYED)) {
				// draw red X
				int size = getChitSize();
				g.setColor(TRANSPARENT_RED);
				
				g.setStroke(thickLine);
				g.drawLine(0,0,size,size);
				g.drawLine(0,size,size,0);
			}
		}
	}
}