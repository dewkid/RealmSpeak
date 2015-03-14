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
import java.awt.Rectangle;
import java.awt.Shape;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.util.StringUtilities;

public class BoonChitComponent extends ChitComponent {
	/*
	 * "this" block contains
	 * 	boon
	 * 	native = order
	 * 	cost = 12
	 */
	public BoonChitComponent(GameObject obj) {
		super(obj);
		lightColor = MagicRealmColor.PINK;
		darkColor = MagicRealmColor.PINK;
	}

	public String getName() {
		return BOON;
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		int pos = 5;
		int margin = 0;
		TextType tt;
		
		// Title
		tt = new TextType("Boon", getChitSize(), "TITLE");
		tt.draw(g, margin, pos, Alignment.Center);
		pos += tt.getHeight(g);
		
		// Draw the description
		String nativeGroup = StringUtilities.capitalize(gameObject.getThisAttribute("boon"));
		String cost = gameObject.getThisAttribute("base_price");
		StringBuffer sb = new StringBuffer();
		sb.append("Repay this boon to the ");
		sb.append(nativeGroup);
		sb.append(" to regain friendliness.");
		tt = new TextType(sb.toString(),getChitSize()-10,"NORMAL");
		tt.draw(g,margin+5,pos,Alignment.Center);
		pos += tt.getHeight(g);
		
		// Cost
		pos = getChitSize() - 20;
		tt = new TextType(cost+" gold",getChitSize(),"TITLE_RED");
		tt.draw(g,margin,pos,Alignment.Center);
	}

	public int getChitSize() {
		return 100;
	}

	public String getDarkSideStat() {
		return "this";
	}

	public String getLightSideStat() {
		return "this";
	}

	public Shape getShape(int x, int y, int size) {
		return new Rectangle(x,y,size,size);
	}
}