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
package com.robin.magic_realm.components.quest;

import javax.swing.ImageIcon;

import com.robin.general.swing.ImageCache;

public enum QuestState {
	New,
	Assigned,
	Active,
	Failed,
	Complete,
	;
	public static int tokenSizePercent = 80;
	private static ImageIcon assignedIcon = ImageCache.getIcon("quests/tokenpending",tokenSizePercent);
	private static ImageIcon activeIcon = ImageCache.getIcon("quests/token",tokenSizePercent);
	private static ImageIcon failedIcon = ImageCache.getIcon("quests/tokenfail",tokenSizePercent);
	private static ImageIcon completeIcon = ImageCache.getIcon("quests/tokendone",tokenSizePercent);
	
	private static ImageIcon smallFailedIcon = ImageCache.getIcon("quests/tokenfail",30);
	private static ImageIcon smallCompleteIcon = ImageCache.getIcon("quests/tokendone",30);
	
	public ImageIcon getIcon() {
		switch(this) {
			case New:			return assignedIcon; // not really used anyway
			case Assigned:		return assignedIcon;
			case Active:		return activeIcon;
			case Failed:		return failedIcon;
			case Complete:		return completeIcon;
		}
		return null;
	}
	public ImageIcon getSmallIcon() {
		switch(this) {
			case Failed:		return smallFailedIcon;
			case Complete:		return smallCompleteIcon;
		}
		return null;
	}
	public boolean isFinished() {
		return this==Failed || this==Complete;
	}
}