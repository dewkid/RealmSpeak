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
package com.robin.magic_realm.RealmSpeak;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

public abstract class RealmSpeakInternalFrame extends JInternalFrame {
	
	protected static final int BOTTOM_HEIGHT = 0;
	
	protected Integer forceWidth = null;
	
	public RealmSpeakInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(title,resizable,closable,maximizable,iconifiable);
	}
	public void clearForceWidth() {
		forceWidth = null;
	}
	public void setForceWidth(Integer val) {
		forceWidth = val;
	}
	/**
	 * Resize according to a set strategy
	 */
	public abstract void organize(JDesktopPane desktop);
	public abstract boolean onlyOneInstancePerGame();
	public abstract String getFrameTypeName();
}