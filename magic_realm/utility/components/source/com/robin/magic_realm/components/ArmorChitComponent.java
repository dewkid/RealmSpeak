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
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.TreasureUtility;
import com.robin.magic_realm.components.utility.TreasureUtility.ArmorType;

public class ArmorChitComponent extends RoundChitComponent {
	public static final String DAMAGED = LIGHT_SIDE_UP;
	public static final String INTACT = DARK_SIDE_UP;

	public ArmorChitComponent(GameObject obj) {
		super(obj);
		
		try {
			lightColor = MagicRealmColor.getColor(getAttribute("damaged","chit_color"));
			darkColor = MagicRealmColor.getColor(getAttribute("intact","chit_color"));
		}
		catch(Exception ex) {
			System.out.println("problem with "+obj.getName()+": "+ex);
		}
	}
	public boolean isShieldType() {
		return TreasureUtility.getArmorType(getGameObject())==ArmorType.Shield;
	}
	public boolean isHelmetType() {
		return TreasureUtility.getArmorType(getGameObject())==ArmorType.Helmet;
	}
	public boolean isBreastplateType() {
		return TreasureUtility.getArmorType(getGameObject())==ArmorType.Breastplate;
	}
	public boolean isSuitOfArmorType() {
		return TreasureUtility.getArmorType(getGameObject())==ArmorType.Armor;
	}
	public String getLightSideStat() {
		return "damaged";
	}
	public String getDarkSideStat() {
		return "intact";
	}
	public void setIntact(boolean val) {
		if (val) {
			setDarkSideUp();
		}
		else {
			setLightSideUp();
		}
	}
	public boolean isDamaged() {
		return isLightSideUp();
	}
	public String getName() {
	    return ARMOR;
	}
	public int getChitSize() {
		return 75;
	}

	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		super.paintComponent(g);
		
		// Draw image
		String icon_type = (String)gameObject.getThisAttribute(Constants.ICON_TYPE);
		if (icon_type!=null) {
			drawIcon(g,"armor",icon_type,0.5);
		}
		
		// Draw Stats
		String vulnerability = getAttribute("this","vulnerability");
		
		TextType tt = new TextType(vulnerability,getChitSize(),"BIG_BOLD");
		tt.draw(g,0,getChitSize()-(getChitSize()>>3)-tt.getHeight(g),Alignment.Center);
		
		if (isDamaged()) {
			tt = new TextType("DAMAGED",getChitSize(),"TITLE_GRAY");
			tt.draw(g,0,getChitSize()>>2);
		}
		
		// If magic, draw name
		if (gameObject.hasKey("magic")) {
			String name = gameObject.getName();
			int space = name.indexOf(" ");
			if (space>=0) { // this is a weak solution
				name = name.substring(0,space);
				tt = new TextType(name,getChitSize(),"TITLE_RED");
				tt.draw(g,getChitSize()-10-tt.getWidth(g),(getChitSize()>>2)+10,Alignment.Left);
			}
		}
		
		drawDamageAssessment(g);
	}
	public Strength getVulnerability() {
		return new Strength(getAttribute("this", "vulnerability"));
	}
}