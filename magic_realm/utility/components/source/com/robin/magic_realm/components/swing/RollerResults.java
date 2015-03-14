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
package com.robin.magic_realm.components.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.attribute.RollerResult;
import com.robin.magic_realm.components.utility.Constants;

public class RollerResults extends JPanel {
	public static final int ROLLER_OFFSET = 58;
	public static final Color ALTERNATING_COLOR1 = new Color(120,120,255,50);
	public static final Color ALTERNATING_COLOR2 = new Color(120,255,120,100);
	
	private int width;
	private ArrayList battleRolls;
	
	public RollerResults() {
		setOpaque(true);
		setBackground(Color.white);
	}
	public boolean isEmpty() {
		return battleRolls==null || battleRolls.isEmpty();
	}
	
	/**
	 * @param in		Collection of RollerResult objects
	 */
	public void setBattleRolls(ArrayList in) {
		battleRolls = new ArrayList(in);
		ComponentTools.lockComponentSize(this,Constants.COMBAT_SIDEBAR_WIDTH,battleRolls.size()*ROLLER_OFFSET);
		repaint();
	}
	public void paint(Graphics g) {
		super.paint(g);
		Dimension size = getSize();
		width = size.width;
		if (battleRolls!=null && !battleRolls.isEmpty()) {
			int offset = 0;
			boolean altColor = false;
			int px = (width>>1);
			int py = (ROLLER_OFFSET>>1);
			for (Iterator i=battleRolls.iterator();i.hasNext();) {
				RollerResult rr = (RollerResult)i.next();
				rr.draw(this,g,px,py+offset,true,altColor);
				offset+=ROLLER_OFFSET;
				altColor = !altColor;
			}
		}
	}
}