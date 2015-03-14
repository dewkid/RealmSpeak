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

import java.util.Iterator;

import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingWeapon extends SpellTargetingSingle {

	protected SpellTargetingWeapon(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		// Targets one weapon counter, native counter, Goblin counter, Ogre counter or Giant's club
		TileLocation loc = battleModel.getBattleLocation();
		for (Iterator i=loc.clearing.getDeepClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.isWeapon()) {
				gameObjects.add(rc.getGameObject());
				identifiers.add(rc.getGameObject().getHeldBy().getName());
			}
// Poison cannot be cast on Alchemist's Mixture!!
//			else if (rc.isTreasure() && rc.getGameObject().hasThisAttribute("attack") && rc.getGameObject().hasThisAttribute(Constants.ACTIVATED)) {
//				gameObjects.add(rc.getGameObject());
//				identifiers.add(rc.getGameObject().getHeldBy().getName());
//			}
			else {
				RealmComponent owner = rc.getOwner();
				if (rc.isNative() && (owner==null || allowTargetingHirelings())) {
					gameObjects.add(rc.getGameObject());
					identifiers.add(owner==null?"denizen":owner.getGameObject().getName());
				}
				else if (rc.isMonster()) {
					MonsterChitComponent monster = (MonsterChitComponent)rc;
					String iconType = rc.getGameObject().getThisAttribute("icon_type");
					if (iconType.startsWith("goblin_") 
							|| ("giant".equals(iconType) && !monster.isTremendous())) {
						gameObjects.add(rc.getGameObject());
						identifiers.add(owner==null?"denizen":owner.getGameObject().getName());
					}
					else if ("giant".equals(iconType) && monster.isTremendous()) {
						gameObjects.add(monster.getWeapon().getGameObject());
						identifiers.add(owner==null?"denizen":owner.getGameObject().getName());
					}
				}
			}
		}
		return true;
	}
}