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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public abstract class FixedSizeComponent extends JComponent {

	private static final Insets NO_INSETS = new Insets(0,0,0,0);

	private ArrayList changeListeners = null;
	
	public abstract int getComponentWidth();
	public abstract int getComponentHeight();

	public FixedSizeComponent() {
	}
	protected Insets getBorderInsets() {
		Border border = getBorder();
		if (border!=null) {
			return border.getBorderInsets(this);
		}
		return NO_INSETS;
	}
	public void setBorder(Border b) {
		super.setBorder(b);
		updateComponentSize();
	}
	protected void updateComponentSize() {
		Insets in = getBorderInsets();
		
		int cw = getComponentWidth()+in.left+in.right;
		int ch = getComponentWidth()+in.top+in.bottom;
		
		Dimension d = new Dimension(cw,ch);
		setMaximumSize(d);
		setMinimumSize(d);
		setPreferredSize(d);
	}
	public int getWidth() {
		Insets in = getBorderInsets();
		return getComponentWidth()+in.left+in.right;
	}
	public int getHeight() {
		Insets in = getBorderInsets();
		return getComponentHeight()+in.top+in.bottom;
	}
	public void addChangeListener(ChangeListener listener) {
		if (changeListeners==null) {
			changeListeners = new ArrayList();
		}
		if (!changeListeners.contains(listener)) {
			changeListeners.add(listener);
		}
	}
	public void removeChangeListener(ChangeListener listener) {
		if (changeListeners!=null) {
			changeListeners.remove(listener);
			if (changeListeners.size()==0) {
				changeListeners = null;
			}
		}
	}
	protected void fireStateChanged() {
		if (changeListeners!=null && changeListeners.size()>0) {
			ChangeEvent ev = new ChangeEvent(this);
			for (Iterator i=changeListeners.iterator();i.hasNext();) {
				ChangeListener listener = (ChangeListener)i.next();
				listener.stateChanged(ev);
			}
		}
	}
}