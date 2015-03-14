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

public class GameAttributeBlockChange extends GameObjectChange {
	
	private Long sourceId;
	private String from;
	private String to;
	
	public GameAttributeBlockChange(GameObject go) {
		super(go);
		sourceId = null;
		from = null;
		to = null;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		if (sourceId==null) {
			sb.append(":  rename attribute block "+from+" to "+to);
		}
		else {
			sb.append(":  copy attribute block "+from+" from gameobject "+sourceId);
		}
		return sb.toString();
	}
	public void rename(String inFrom,String inTo) {
		this.sourceId = null;
		this.from = inFrom;
		this.to = inTo;
	}
	public void copyFrom(GameObject go,String blockName) {
		sourceId = new Long(go.getId());
		from = blockName;
		to = null;
	}
	protected void applyChange(GameData data, GameObject go) {
		if (sourceId==null) {
			go._renameAttributeBlock(from,to);
		}
		else {
			GameObject source = data.getGameObject(sourceId);
			go._copyAttributeBlockFrom(source,from);
		}
	}
	protected void rebuildChange(GameData data, GameObject go) {
		if (sourceId==null) {
			go.renameAttributeBlock(from,to);
		}
		else {
			GameObject source = data.getGameObject(sourceId);
			go.copyAttributeBlockFrom(source,from);
		}
	}
//	public boolean equals(Object o) {
//		if (o instanceof GameAttributeBlockChange) {
//			GameAttributeBlockChange gab = (GameAttributeBlockChange)o;
//			return (gab.getId()==getId() && gab.from.equals(from) && gab.to.equals(to));
//		}
//		return false;
//	}
//	public boolean sameTypeOfChange(GameObjectChange other) {
//		return false;
//	}
}