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
import com.robin.magic_realm.components.attribute.Speed;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class MonsterPartChitComponent extends MonsterChitComponent {
	private HostPrefWrapper hostPrefs = null;
	protected MonsterPartChitComponent(GameObject obj) {
	    super(obj);
	}
	public String getName() {
	    return MONSTER_PART;
	}
	private MonsterChitComponent getWielder() {
		return (MonsterChitComponent)RealmComponent.getRealmComponent(getGameObject().getHeldBy());
	}
	protected int sizeModifier() {
		return getWielder().sizeModifier();
	}
	protected int speedModifier() {
		return getWielder().speedModifier();
	}
	public Strength getStrength() {
		Strength strength = super.getStrength();
		if (strength.getChar()!="T" && getGameObject().getHeldBy().hasThisAttribute(Constants.STRONG_MF)) {
			strength.modify(1);
		}
		return strength;
	}

	public Strength getVulnerability() {
		Strength vul = new Strength("M"); // This is the default "size" for a monster weapon
		vul.modify(sizeModifier());
		return vul;
	}
	public Speed getMoveSpeed() {
		return null;
	}
	public Speed getFlySpeed() {
		return null;
	}
	public Integer getLength() {
		if (hostPrefs==null) {
			hostPrefs = HostPrefWrapper.findHostPrefs(getGameObject().getGameData());
		}
		if (hostPrefs.hasPref(Constants.ADV_DRAGON_HEADS)) {
			Integer dragon_length = getFaceAttributeInteger("dragon_length");
			if (dragon_length!=null) {
				return dragon_length;
			}
		}
		return super.getLength();
	}
	public boolean isMissile() {
		if (hostPrefs==null) {
			hostPrefs = HostPrefWrapper.findHostPrefs(getGameObject().getGameData());
		}
		if (hostPrefs.hasPref(Constants.ADV_DRAGON_HEADS)) {
			if (getGameObject().hasThisAttribute("dragon_missile")) {
				return true;
			}
		}
		return super.isMissile();
	}
}