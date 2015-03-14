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
package com.robin.magic_realm.components.quest.rule;

import java.util.Hashtable;

import com.robin.game.objects.GameObject;

/**
 * Quest rules are active as soon as the quest is taken.
 */
public class QuestRule {
	
	public enum RuleType {
		ActiveMonster,				// Specified monster type regenerates every 7th day regardless of monster roll
		FameNotorietyRestricted,
		MovementRestricted,			// no fly or horse/pony bonus
		OpenVault,
		;
		public boolean affectsAllPlayers() {
			return this==ActiveMonster; // eventually others here...
		}
	}
	
	public void updateIds(Hashtable<Long, GameObject> lookup) {
		// override if IDs need to be updated!
	}
}