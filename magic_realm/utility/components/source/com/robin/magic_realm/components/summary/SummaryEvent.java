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
package com.robin.magic_realm.components.summary;

public abstract class SummaryEvent {
	
	protected abstract String getKey();
	protected abstract String getDataString();
	
	public SummaryEvent() {
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getKey());
		sb.append(getDataString());
		return sb.toString();
	}
	public static SummaryEvent getSummaryEvent(String val) {
		String key = val.substring(0,3);
		String data = val.substring(3);
		SummaryEvent ev = null;
		if (CharacterMoveEvent.KEY.equals(key)) {
			ev = new CharacterMoveEvent(data);
		}
		return ev;
	}
}