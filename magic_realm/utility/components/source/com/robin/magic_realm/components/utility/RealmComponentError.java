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
package com.robin.magic_realm.components.utility;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.robin.magic_realm.components.RealmComponent;

public class RealmComponentError {
	private RealmComponent rc;
	private String title;
	private String error;
	private boolean optional = false;
	
	public RealmComponentError(RealmComponent rc,String title,String error) {
		this(rc,title,error,false);
	}
	public RealmComponentError(RealmComponent rc,String title,String error,boolean optional) {
		this.rc = rc;
		this.title = title;
		this.error = error;
		this.optional = optional;
	}

	public String getError() {
		return error;
	}

	public RealmComponent getRc() {
		return rc;
	}

	public String getTitle() {
		return title;
	}
	
	/**
	 * @return		true if the character wants to ignore the dialog (assuming that's an option) and continue
	 */
	public boolean showDialog(JFrame frame) {
		if (optional) {
			int ret = JOptionPane.showConfirmDialog(frame,error,title,JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
			return ret==JOptionPane.YES_OPTION;
		}
		else {
			JOptionPane.showMessageDialog(frame,error,title,JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}