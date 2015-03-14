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

import java.awt.event.*;

public class MouseUtility {
	// This behavior is not introduced until 1.4, so I have to emulate it myself
	public static final int NOBUTTON = 0;
	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 3;
	
	/**
	 * @deprecated		No longer needed, now that I'm all 1.5.
	 */
	public static int getMouseButton(MouseEvent ev) {
		int mod = ev.getModifiers();
		if ((mod & InputEvent.BUTTON1_MASK)>0) {
			return BUTTON1;
		}
		else if ((mod & InputEvent.BUTTON2_MASK)>0) {
			return BUTTON2;
		}
		else if ((mod & InputEvent.BUTTON3_MASK)>0) {
			return BUTTON3;
		}
		return NOBUTTON;
	}
	public static boolean isRightClick(MouseEvent ev) {
		int button = ev.getButton();
		return button!=MouseEvent.NOBUTTON && button!=MouseEvent.BUTTON1;
	}
	public static boolean isRightOrControlClick(MouseEvent ev) {
		return ev.isControlDown() || isRightClick(ev);
	}
}