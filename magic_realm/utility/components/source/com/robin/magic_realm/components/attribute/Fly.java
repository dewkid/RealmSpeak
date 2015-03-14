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
package com.robin.magic_realm.components.attribute;

import com.robin.magic_realm.components.*;

public class Fly {
	private RealmComponent rc;
	private Strength strength;
	private Speed speed;
	public Fly() {
		// Used by Smoke Bomb in the case where a character mustFly
	}
	public Fly(RealmComponent rc) {
		this.rc = rc;
		this.strength = _getStrength(rc);
		this.speed = _getSpeed(rc);
	}
	public Fly(StrengthChit sc) {
		this.rc = null;
		this.strength = sc.getStrength();
		this.speed = sc.getSpeed();
	}
	public Strength getStrength() {
		return strength;
	}
	public Speed getSpeed() {
		return speed;
	}
	private Strength _getStrength(RealmComponent rc) {
		if (rc instanceof CharacterActionChitComponent) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
			return chit.getStrength();
		}
		else if (rc instanceof MonsterMoveChitComponent) {
			MonsterMoveChitComponent chit = (MonsterMoveChitComponent)rc;
			return chit.getMoveStrength();
		}
		else if (rc.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)rc;
			return monster.getVulnerability();
		}
		else if (rc.isNative()) {
			NativeChitComponent nativeGuy = (NativeChitComponent)rc;
			return nativeGuy.getVulnerability();
		}
		else if (rc.isTreasure()) {
			return new Strength(rc.getGameObject().getThisAttribute("fly_strength"));
		}
		return new Strength(rc.getGameObject().getThisAttribute("strength"));
	}
	private Speed _getSpeed(RealmComponent rc) {
		if (rc instanceof MonsterMoveChitComponent) {
			MonsterMoveChitComponent chit = (MonsterMoveChitComponent)rc;
			return chit.getFlySpeed();
		}
		else if (rc.isTreasure()) {
			return new Speed(rc.getGameObject().getThisAttribute("fly_speed"));
		}
		else if (rc.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)rc;
			return monster.getFlySpeed();
		}
		else if (rc.isNative()) {
			NativeChitComponent nativeGuy = (NativeChitComponent)rc;
			return nativeGuy.getFlySpeed();
		}
		return new Speed(rc.getGameObject().getThisAttribute("speed"));
	}
	public void useFly() {
		if (rc!=null) {
			if (rc.isFlyChit()) {
				FlyChitComponent flyChit = (FlyChitComponent)rc;
				flyChit.expireSourceSpell();
			}
			else if (rc instanceof CharacterActionChitComponent) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				chit.makeFatigued();
			}
		}
	}
	public static boolean valid(RealmComponent rc) {
		if (rc.isFlyChit()) {
			return true;
		}
		else if (rc.isActionChit()) {
			if ((rc instanceof MonsterActionChitComponent) && rc.getGameObject().hasThisAttribute("flying")) {
				return true;
			}
			else if ((rc instanceof CharacterActionChitComponent) && ((CharacterActionChitComponent)rc).isFly()) {
				return true;
			}
		}
		else if (rc.isTreasure()) { // Flying Carpet
			return rc.getGameObject().hasThisAttribute("fly_strength");
		}
		return false;
	}
}