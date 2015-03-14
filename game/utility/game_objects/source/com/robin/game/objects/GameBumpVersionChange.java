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
package com.robin.game.objects;

public class GameBumpVersionChange extends GameObjectChange {
	
	public GameBumpVersionChange(GameObject go) {
		super(go);
	}
	protected void applyChange(GameData data,GameObject go) {
		go._bumpVersion();
	}
	protected void rebuildChange(GameData data,GameObject go) {
		go.bumpVersion();
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(": Bump data version to force redraw");
		return sb.toString();
	}
//	public boolean equals(Object o1) {
//		if (o1 instanceof GameBumpVersionChange) {
//			GameBumpVersionChange other = (GameBumpVersionChange)o1;
//			return (other.getId()==getId());
//		}
//		return false;
//	}
//	public boolean sameTypeOfChange(GameObjectChange o1) {
//		return false;
//	}
}