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

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.Constants;

public class Inventory {
	
	private boolean active;
	private GameObject go;
	private RealmComponent rc;
	
	public Inventory(GameObject go) {
		this.go = go;
		this.rc = RealmComponent.getRealmComponent(go);
		active = go.hasThisAttribute(Constants.ACTIVATED);
	}
	public GameObject getGameObject() {
		return go;
	}
	public RealmComponent getRealmComponent() {
		return rc;
	}
	public boolean isNew() {
		return go.hasThisAttribute(Constants.TREASURE_NEW);
	}
	public boolean canActivate() {
		return !active && !rc.isMinorCharacter() && !rc.isVisitor() && !rc.isGoldSpecial();
	}
	public boolean canDeactivate() {
		return active
				&& !go.hasThisAttribute("color_source")
				&& !go.hasThisAttribute("potion")
				&& !rc.isNativeHorse()
				&& !rc.isGoldSpecial()
				&& !rc.isMinorCharacter()
				&& !rc.isVisitor()
				&& !rc.isBoon();
	}
	public boolean canDrop() {
		return !rc.isBoon()
				&& !rc.isPhaseChit()
				&& !rc.isNativeHorse()
				&& !rc.isMinorCharacter()
				&& !rc.isVisitor()
				&& (!rc.isEnchanted()) // can't drop an enchanted artifact or book
				&& !(active && go.hasThisAttribute("potion")); // can't drop an active potion
	}
}