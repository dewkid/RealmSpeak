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

import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingHumanGroup extends SpellTargetingSingle {

	public SpellTargetingHumanGroup(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		// Giants, or Ogres, or Native Group
		ArrayList<RealmComponent> allDenizens = battleModel.getAllBattleParticipants(true);
		String ownerId = activeParticipant.getGameObject().getStringId();
		for (RealmComponent rc:allDenizens) {
			if (!rc.isCharacter() && (rc.getOwnerId()==null || rc.getOwnerId().equals(ownerId))) {
				String groupName = null;
				if (rc.isMonster() && !rc.isPlayerControlledLeader()) {
					MonsterChitComponent monster = (MonsterChitComponent)rc;
					String icon = rc.getGameObject().getAttribute(rc.getThisBlock(),"icon_type");
					if ("giant".equals(icon) || "frostgiant".equals(icon)) {
						groupName = monster.isTremendous()?"Giants":"Ogres";
					}
				}
				else if (rc.isNative()) {
					groupName = rc.getGameObject().getAttribute(rc.getThisBlock(),"native");
				}
				
				if (groupName!=null) {
					ArrayList list = (ArrayList)secondaryTargets.get(groupName);
					if (list==null) {
						list = new ArrayList();
						secondaryTargets.put(groupName,list);
					}
					list.add(rc);
				}
			}
		}
		return true;
	}
}