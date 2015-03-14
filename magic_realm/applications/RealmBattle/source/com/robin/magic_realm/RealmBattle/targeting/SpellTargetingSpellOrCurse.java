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

import java.util.Collection;
import java.util.Iterator;

import com.robin.game.objects.GameData;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellMasterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingSpellOrCurse extends SpellTargetingSingle {

	public SpellTargetingSpellOrCurse(CombatFrame combatFrame,SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		String targetType = spell.getGameObject().getThisAttribute("target");
		GameData gameData = spell.getGameObject().getGameData();
		if (targetType.indexOf("spell")>=0) {
			SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(gameData);
			for (Iterator i=sm.getAllSpellsInClearing(battleModel.getBattleLocation(),true).iterator();i.hasNext();) {
				SpellWrapper targetSpell = (SpellWrapper)i.next();
				if (targetSpell.isAlive()) {
					identifiers.add(targetSpell.getTargetsName());
					gameObjects.add(targetSpell.getGameObject());
				}
			}
		}
		if (targetType.indexOf("curse")>=0) {
			for (Iterator i=battleModel.getAllParticipatingCharacters().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
				Collection curses = character.getAllCurses();
				if (curses.size()>0) {
					for (Iterator n=curses.iterator();n.hasNext();) {
						String curse = (String)n.next();
						identifiers.add(curse);
						gameObjects.add(rc.getGameObject());
					}
				}
			}
		}
		return true;
	}
}