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
package com.robin.magic_realm.RealmBattle.targeting;

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingCharacter extends SpellTargetingSingle {
	
	private boolean lightOnly;

	public SpellTargetingCharacter(CombatFrame combatFrame, SpellWrapper spell,boolean lightOnly) {
		super(combatFrame, spell);
		this.lightOnly = lightOnly;
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		ArrayList allCharacters = combatFrame.findCanBeSeen(battleModel.getAllParticipatingCharacters(),true);
		for (Iterator i=allCharacters.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
			if (!lightOnly || !character.getVulnerability().strongerThan(new Strength("L"))) {
				gameObjects.add(rc.getGameObject());
			}
		}
		return true;
	}
}