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

import java.util.ArrayList;

public enum ChitItemType {
	None,
	Treasure,
	Horse,
	Weapon,
	Armor,
	;
	
	static String[] ItemKeyVals = {"item"};
	static String[] TreasureKeyVals = {"item","treasure"};
	static String[] HorseKeyVals = {"item","horse"};
	static String[] WeaponKeyVals = {"item","weapon","!character","!treasure","original_game","!magic"};
	static String[] ArmorKeyVals = {"item","armor","!character","!treasure","original_game","!magic"};
	public String[] getKeyVals() {
		switch(this) {
			case None:		return ItemKeyVals;
			case Treasure:	return TreasureKeyVals;
			case Horse:		return HorseKeyVals;
			case Weapon:	return WeaponKeyVals;
			case Armor:		return ArmorKeyVals;
		}
		throw new IllegalStateException("Unknown ChitItemType?"); // can this even happen?
	}
	public static ArrayList<String> listToStrings(ArrayList<ChitItemType> types) {
		if (types==null) return null;
		ArrayList<String> list = new ArrayList<String>();
		for(ChitItemType cit:types) {
			list.add(cit.toString());
		}
		return list;
	}
	public static ArrayList<ChitItemType> listToTypes(ArrayList<String> strings) {
		if (strings==null) return null;
		ArrayList<ChitItemType> list = new ArrayList<ChitItemType>();
		for(String string:strings) {
			list.add(ChitItemType.valueOf(string));
		}
		return list;
	}
}