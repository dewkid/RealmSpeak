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

import com.robin.general.graphics.GraphicsUtil;

/**
 * When buttons reach a certain minimum size, the text is no longer printed, and instead
 * an ellipsis is printed (...).   This component gets rid of that ANNOYING behavior.
 * 
 * Update:  I think Java 1.4 does JButton differently, because the ellipses is back!
 * Update 12/31/12:  Fixed this class so it actually works.  (Used by RealmGm)
 */
public class ForceTextButton extends JButton {
	
	private static final Color disabledTextColor = UIManager.getColor("Button.disabledText");

	private String realText;
	
	public ForceTextButton(String text) {
		super(""); // give NO text to the button itself!
		this.realText = text;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension size = getSize();
		g.setColor(isEnabled()?getForeground():disabledTextColor);
		GraphicsUtil.drawCenteredString(g,0,0,size.width,size.height,realText);
	}
	public String getRealText() {
		return realText;
	}
	public void setText(String val) {
		realText = val;
		repaint();
	}
}