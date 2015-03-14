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
package com.robin.magic_realm.components;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.attribute.Harm;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public abstract class MonsterActionChitComponent extends StateChitComponent implements BattleChit {
	protected MonsterChitComponent monster;
	public MonsterActionChitComponent(GameObject obj) {
		super(obj);
		monster = (MonsterChitComponent)RealmComponent.getRealmComponent(obj);
		lightColor = monster.lightColor;
		darkColor = monster.darkColor;
	}
	public void changeWeaponState(boolean hit) {
	}
	public boolean hitsOnTie() {
		return false;
	}
	public boolean isMissile() {
		return false;
	}
	public String getMissileType() {
		return null;
	}
	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs, BattleChit attacker, int box, Harm attackerHarm,int attackOrderPos) {
		return monster.applyHit(game,hostPrefs,attacker,box,attackerHarm,attackOrderPos);
	}
	public boolean isActionChit() {
		return true;
	}
}