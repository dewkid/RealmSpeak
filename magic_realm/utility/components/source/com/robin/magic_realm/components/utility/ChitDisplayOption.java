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

import com.robin.magic_realm.components.RealmComponent;

public class ChitDisplayOption {
	public boolean characters;
	public boolean monsters;
	public boolean natives;
	public boolean dwellings;
	public boolean tileChits;
	public boolean droppedInventory;
	public boolean siteCards;
	public boolean tileBewitchingSpells;
	
	public ChitDisplayOption() {
		characters = true;
		monsters = true;
		natives = true;
		dwellings = true;
		tileChits = true;
		droppedInventory = true;
		siteCards = true;
		tileBewitchingSpells = true;
	}
	public boolean okayToDraw(RealmComponent rc) {
		return (characters || (!rc.isCharacter() && !rc.isFamiliar() && !rc.isPhantasm()))
					&& (monsters || !rc.isMonster())
					&& (natives || !rc.isNative())
					&& (dwellings || !rc.isDwelling())
					&& (tileChits || !rc.isStateChit())
					&& (siteCards || !(rc.isTreasure() && rc.isTreasureLocation()))
					&& (droppedInventory || !rc.isCollectibleThing());
	}
}