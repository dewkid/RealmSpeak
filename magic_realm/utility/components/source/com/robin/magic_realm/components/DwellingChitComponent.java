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
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.utility.Constants;

public class DwellingChitComponent extends SquareChitComponent {
	public static final String NORMAL_SIDE = LIGHT_SIDE_UP;
	public static final String OTHER_SIDE = DARK_SIDE_UP;

	protected DwellingChitComponent(GameObject obj) {
		super(obj);
		
		try {
			lightColor = MagicRealmColor.getColor("tan");
			darkColor = MagicRealmColor.getColor("darkgray");
		}
		catch(Exception ex) {
			System.out.println("problem with "+obj.getName()+": "+ex);
		}
	}
	public String getName() {
	    return DWELLING;
	}
	protected int getSortOrder() {
		return 1000; // want this on the bottom!
	}
	
	public int getChitSize() {
		return T_CHIT_SIZE;
	}
	public String getLightSideStat() {
		return "this";
	}
	public String getDarkSideStat() {
		return "this";
	}
	
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		
		boolean useColor = useColorIcons();
		
		// Draw image
		String icon_type = (String)gameObject.getThisAttribute(Constants.ICON_TYPE);
		if (icon_type!=null) {
			drawIcon(g,"dwellings"+(useColor?"_c":""),icon_type,0.9);
		}
		
		if (useColor) {
			TextType tt = new TextType(getGameObject().getName(),T_CHIT_SIZE,"BOLD");
			tt.draw(g1,5,2,Alignment.Left);
		}
	}
}