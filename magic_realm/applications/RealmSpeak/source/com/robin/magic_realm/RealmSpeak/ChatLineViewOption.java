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

import javax.swing.JRadioButton;

public class ChatLineViewOption extends JRadioButton {
	private int lines;
	public ChatLineViewOption(String text,int lines) {
		super(text);
		this.lines = lines;
	}
	public int getLines() {
		return lines;
	}
	
	public static ChatLineViewOption[] generateOptions() {
		return new ChatLineViewOption[] {
			new ChatLineViewOption("10 Lines of Chat",10),
			new	ChatLineViewOption("5 Lines of Chat",5),
			new ChatLineViewOption("3 Lines of Chat",3),
			new ChatLineViewOption("1 Lines of Chat",1),
			new ChatLineViewOption("Chat OFF",0),
		};
	};
}