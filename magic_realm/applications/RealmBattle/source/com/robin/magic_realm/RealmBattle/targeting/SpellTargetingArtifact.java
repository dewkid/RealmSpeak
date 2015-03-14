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

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingArtifact extends SpellTargetingSingle {

	protected SpellTargetingArtifact(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		// Assume that activeParticipant IS character
		CharacterWrapper character = new CharacterWrapper(activeParticipant.getGameObject());
		secondaryTargetChoiceString = "Select a spell to enchant artifact with:";
		ArrayList spellPossibilities = new ArrayList(character.getAllSpells());
		// TODO Eliminate the casting spell?  Maybe not...
		if (spellPossibilities.size()>0) { // can't enchant an artifact with a recorded spell, if you have none!
			for (Iterator i=character.getInventory().iterator();i.hasNext();) {
				GameObject item = (GameObject)i.next();
				if (item.hasThisAttribute("artifact") || item.hasThisAttribute("book")) {
					RealmComponent rc = RealmComponent.getRealmComponent(item);
					if (!rc.isEnchanted()) {
						identifiers.add("");
						gameObjects.add(item);
						secondaryTargets.put("",spellPossibilities); // the spell possibilities are the same each time
					}
				}
			}
		}
		return true;
	}
}