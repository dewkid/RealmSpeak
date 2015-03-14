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

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingMyItem extends SpellTargetingSingle {
	
	private boolean active;
	private boolean inactive;
	
	public SpellTargetingMyItem(CombatFrame combatFrame, SpellWrapper spell,boolean active,boolean inactive) {
		super(combatFrame, spell);
		this.active = active;
		this.inactive = inactive;
	}
	
	public boolean isAddable(RealmComponent item) {
		return item.isWeapon() || item.isArmor() || item.isTreasure();
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		CharacterWrapper caster = spell.getCaster();
		ArrayList<GameObject> inv = new ArrayList<GameObject>();
		if (active) {
			inv.addAll(caster.getActiveInventory());
		}
		if (inactive) {
			inv.addAll(caster.getInactiveInventory());
		}
		for (GameObject go:inv) {
			RealmComponent itemRc = RealmComponent.getRealmComponent(go);
			if (isAddable(itemRc)) {
				gameObjects.add(go);
			}
		}
		return true;
	}
}