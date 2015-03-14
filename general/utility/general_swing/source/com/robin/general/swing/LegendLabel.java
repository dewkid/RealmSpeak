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
package com.robin.general.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * A class that will show a color box and a label - useful for color legends
 */
public class LegendLabel extends JLabel {
	public LegendLabel(Color color,String text) {
		super(text);
		setIcon(new LegendIcon(color));
	}
	
	private class LegendIcon implements Icon {
		private Color color;
		public LegendIcon(Color color) {
			this.color = color;
		}
		public int getIconHeight() {
			return 15;
		}
		public int getIconWidth() {
			return 15;
		}
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRect(x,y,getIconWidth(),getIconHeight());
			g.setColor(Color.black);
			g.drawRect(x,y,getIconWidth()-1,getIconHeight()-1);
		}
	}
}