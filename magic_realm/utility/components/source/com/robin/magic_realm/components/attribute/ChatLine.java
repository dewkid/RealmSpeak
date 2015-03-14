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
package com.robin.magic_realm.components.attribute;

import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ChatLine {
	
	public enum HeaderMode {
		CharacterName,
		PlayerName,
		Both,
	};
	
	public static String BOLD_PREFIX = "b_";
	private static HeaderMode headerMode = HeaderMode.CharacterName;
	public static HeaderMode getHeaderMode() {
		return headerMode;
	}
	public static void setHeaderMode(HeaderMode mode) {
		headerMode = mode;
	}
	
	private CharacterWrapper character;
	private String text;
	public ChatLine(CharacterWrapper character,String text) {
		this.character = character;
		this.text = text;
	}
	public String getHeader() {
		switch(headerMode) {
			case CharacterName:
				return character.getName();
			case PlayerName:
				return character.getPlayerName();
		}
		return character.getName()+" ("+character.getPlayerName()+")";
	}
	public String getHeaderStyleName() {
		return BOLD_PREFIX+character.getChatStyle();
	}
	public String getText() {
		return text;
	}
	public String getTextStyleName() {
		return character.getChatStyle();
	}
	public boolean isValid() {
		return character!=null && text.trim().length()>0;
	}
}