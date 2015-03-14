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

import java.awt.*;
import javax.swing.*;

public class IconCounter extends JComponent {

	private Icon icon;
	private int value;
	private int iconSpacer;
	
	public IconCounter(Icon icon) {
		this.icon = icon;
		this.value = 0;
		this.iconSpacer = 1;
		updateSize();
	}
	private void updateSize() {
		Dimension s = new Dimension((icon.getIconWidth()+iconSpacer)*value,icon.getIconHeight());
		setMinimumSize(s);
		setMaximumSize(s);
		setPreferredSize(s);
		revalidate();
		repaint();
	}
	public void paintComponent(Graphics g) {
		Dimension d = getSize();
		int dx = icon.getIconWidth()+iconSpacer;
		int y=0;
		for (int x=0;x<d.width;x+=dx) {
			icon.paintIcon(this,g,x,y);
		}
	}
	public void setValue(int val) {
		value = val;
		updateSize();
	}
	public void addValue(int val) {
		value += val;
		updateSize();
	}
	public int getValue() {
		return value;
	}
}