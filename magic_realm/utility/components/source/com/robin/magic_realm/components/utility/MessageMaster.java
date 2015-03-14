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

import javax.swing.*;

import com.robin.general.swing.FrameManager;

public class MessageMaster implements Runnable {
	private JFrame parent;
	private String message;
	private String title;
	private int type;
	private Icon icon;
	
	private MessageMaster(JFrame parent,String message,String title,int type,Icon icon) {
		this.parent = parent;
		this.message = message;
		this.title = title;
		this.type = type;
		this.icon = icon;
	}
	public void run() {
		if (icon==null) {
			switch(type) {
				case JOptionPane.INFORMATION_MESSAGE:
					icon = UIManager.getIcon("OptionPane.informationIcon");
					break;
				case JOptionPane.WARNING_MESSAGE:
					icon = UIManager.getIcon("OptionPane.warningIcon");
					break;
				case JOptionPane.ERROR_MESSAGE:
					icon = UIManager.getIcon("OptionPane.errorIcon");
					break;
				case JOptionPane.QUESTION_MESSAGE:
					icon = UIManager.getIcon("OptionPane.questionIcon");
					break;
			}
		}
		
		FrameManager.showDefaultManagedFrame(parent,message,title,icon,true);
	}
	
	public static void showMessage(JFrame parent,String message,String title,int type) {
		showMessage(parent,message,title,type,null);
	}
	public static void showMessage(JFrame parent,String message,String title,int type,Icon icon) {
		MessageMaster mm = new MessageMaster(parent,message,title,type,icon);
		SwingUtilities.invokeLater(mm);
	}
}