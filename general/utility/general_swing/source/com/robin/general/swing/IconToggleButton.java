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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class IconToggleButton extends JComponent {
	private static final int BORDER = 2;
	private static final int EDGE = 1;
	
	private Icon icon;
	private boolean selected;
	
	private Collection actionListeners = null;

	public IconToggleButton(Icon icon) {
		this(icon,false);
	}
	public IconToggleButton(Icon icon,boolean sel) {
		this.icon = icon;
		this.selected = sel;
		Dimension buttonSize = new Dimension(icon.getIconWidth()+(BORDER<<1)+(EDGE<<1),icon.getIconHeight()+(BORDER<<1)+(EDGE<<1));
		setMinimumSize(buttonSize);
		setMaximumSize(buttonSize);
		setPreferredSize(buttonSize);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				toggleSelection();
			}
		});
	}
	public void addActionListener(ActionListener listener) {
		if (actionListeners==null) {
			actionListeners = new ArrayList();
		}
		actionListeners.add(listener);
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean val) {
		selected = val;
		repaint();
		fireActionPerformed();
	}
	private void toggleSelection() {
		selected = !selected;
		repaint();
		fireActionPerformed();
	}
	private void fireActionPerformed() {
		if (actionListeners!=null) {
			ActionEvent ev = new ActionEvent(this,0,"");
			for (Iterator i=actionListeners.iterator();i.hasNext();) {
				ActionListener listener = (ActionListener)i.next();
				listener.actionPerformed(ev);
			}
		}
	}
	public void paintComponent(Graphics g) {
		if (selected) {
			g.setColor(Color.yellow);
			g.fillRect(EDGE,EDGE,icon.getIconWidth()+(BORDER<<1),icon.getIconHeight()+(BORDER<<1));
		}
		icon.paintIcon(this,g,BORDER+EDGE,BORDER+EDGE);
	}
}