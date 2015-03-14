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
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ImageCache;
import com.robin.general.util.StringUtilities;

public class MoveMarker extends JComponent {
	private static final Font font = new Font("Dialog",Font.BOLD,12);
	
	private ImageIcon backing;
	private String tip;
	private int cost;
	
	public MoveMarker(String name,int val) {
		tip = StringUtilities.capitalize(name)+" movement cost = ";
		setCost(val);
		backing = ImageCache.getIcon("phases/"+name);
		ComponentTools.lockComponentSize(this,backing.getIconWidth(),backing.getIconHeight());
		setBorder(BorderFactory.createEtchedBorder());
	}
	public void setCost(int val) {
		cost = val;
		setToolTipText(tip+cost);
		repaint();
	}
	public void paintComponent(Graphics g) {
		g.drawImage(backing.getImage(),0,0,null);
		g.setFont(font);
		g.setColor(Color.white);
		GraphicsUtil.drawCenteredString(g,16,16,12,12,String.valueOf(cost));
	}
}