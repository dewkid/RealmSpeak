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
package com.robin.magic_realm.components.table;

import java.util.Collection;

import javax.swing.JFrame;

import com.robin.magic_realm.components.attribute.TradeInfo;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class CommerceNone extends Commerce {
	public CommerceNone(JFrame frame,TradeInfo tradeInfo,Collection merchandise,HostPrefWrapper hostPrefs) {
		super(frame,tradeInfo,merchandise,hostPrefs);
	}
	public boolean hideRoller() {
		return true;
	}
	public String getCommerceTableName() {
		return "";
	}
	public String applyOne(CharacterWrapper character) {
		return process(character,0);
	}

	public String applyTwo(CharacterWrapper character) {
		return process(character,0);
	}

	public String applyThree(CharacterWrapper character) {
		return process(character,0);
	}

	public String applyFour(CharacterWrapper character) {
		return process(character,0);
	}

	public String applyFive(CharacterWrapper character) {
		return process(character,0);
	}

	public String applySix(CharacterWrapper character) {
		return process(character,0);
	}
}