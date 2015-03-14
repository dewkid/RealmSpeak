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
package com.robin.magic_realm.components.wrapper;

import javax.swing.JFrame;

import com.robin.magic_realm.components.table.RealmTable;

/**
 * @deprecated		Use DirectInfo transfers instead
 */
public class CharacterRequest {
	
	private CharacterWrapper character;
	private String reqKey;
	
	public CharacterRequest(CharacterWrapper character,String reqKey) {
		this.character = character;
		this.reqKey = reqKey;
	}
	public String toString() {
		return character.getCharacterName()+" affected by "+reqKey;
	}
	public boolean equals(Object o1) {
		if (o1 instanceof CharacterRequest) {
			CharacterRequest r = (CharacterRequest)o1;
			return r.character.getGameObject().equals(character.getGameObject()) && r.reqKey.equals(reqKey);
		}
		return false;
	}
	/**
	 * @return Returns the character.
	 */
	public CharacterWrapper getCharacter() {
		return character;
	}
//	public void process(JFrame frame,GameWrapper theGame) {
	public RealmTable getRealmTable(JFrame frame) {
//		if (reqKey.startsWith("SP")) {
//			// Spell request
//			GameObject go = (GameObject)character.getGameObject().getGameData().getGameObject(Long.valueOf(reqKey.substring(2)));
//			SpellWrapper spell = new SpellWrapper(go);
//			spell.affectTargets(theGame);
//			spell.expireSpell();
//		}
		
//		return RealmTable.realmTableFromKey(frame,reqKey);
		return null;
	}
}