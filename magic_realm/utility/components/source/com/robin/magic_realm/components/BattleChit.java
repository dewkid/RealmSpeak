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
import com.robin.magic_realm.components.attribute.Speed;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public interface BattleChit {
	
	// Battle
	public RealmComponent getTarget();
	public GameObject getGameObject();
	public void changeWeaponState(boolean hit);
	public boolean isImmuneTo(RealmComponent rc);
	
    // Chit handling
	public void flip();
	public void setFacing(String val);
	public String getName();
	public boolean isDenizen();
	public boolean isCharacter();
	
	// Stats
	public Integer getLength();
	public Speed getMoveSpeed();
	public Speed getFlySpeed();
	public Speed getAttackSpeed();
	public Harm getHarm();
	public String getMagicType();
	public int getManeuverCombatBox();
	public int getAttackCombatBox();
	public boolean isMissile();
	public String getMissileType();
	public boolean hitsOnTie();
	public boolean isMonster();
	public boolean hasAnAttack();
	
	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs,BattleChit attacker,int box,Harm attackerHarm,int attackOrderPos);
}