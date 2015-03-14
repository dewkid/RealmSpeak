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

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GameObjectWrapper;
import com.robin.magic_realm.components.table.RealmTable;

/**
 * @deprecated		Use DirectInfo transfers instead
 */
public class CharRequestWrapper extends GameObjectWrapper {
	public static final String CHAR_REQUEST_BLOCK = "_cr_block_"; // special requests that happen "out-of-sync" with the rest of the game
	
	public static final String CHAR_REQUEST_LIST = "_list_";
	
	public static final String CHAR_ID = "char_id";
	public static final String REQ_KEY = "reqKey";
	
	public CharRequestWrapper(GameWrapper game) {
		super(game.getGameObject());
	}
	public String getBlockName() {
		return CHAR_REQUEST_BLOCK;
	}
	public void addBlockList(String blockKey) {
		addListItem(CHAR_REQUEST_LIST,blockKey);
	}
	public ArrayList getBlockList() {
		ArrayList list = getList(CHAR_REQUEST_LIST);
		if (list==null) {
			list = new ArrayList();
		}
		return list;
	}
	public String getNextAvailableBlockName() {
		ArrayList list = getBlockList();
		int n=0;
		while(n<10000) { // Just so this isn't an infinite loop!
			String testKey = CHAR_REQUEST_BLOCK+n;
			if (!list.contains(testKey)) {
				addBlockList(testKey);
				return testKey;
			}
			n++;
		}
		return null;
	}
//	public void setCharacterRequest(CharacterWrapper character,SpellWrapper spell) {
//		setCharacterRequest(character,"SP"+spell.getGameObject().getStringId());
//	}
	public void setCharacterRequest(CharacterWrapper character,RealmTable table) {
		setCharacterRequest(character,table.getTableKey());
	}
	/**
	 * Adds a character request
	 */
	public void setCharacterRequest(CharacterWrapper character,String reqKey) {
		String block = getNextAvailableBlockName();
		getGameObject().setAttribute(block,CHAR_ID,character.getGameObject().getStringId());
		getGameObject().setAttribute(block,REQ_KEY,reqKey);
	}
	public boolean hasCharacterRequests() {
		for (Iterator i=getBlockList().iterator();i.hasNext();) {
			String block = (String)i.next();
			if (getGameObject().hasAttributeBlock(block)) {
				return true;
			}
		}
		return false;
	}
	private CharacterRequest getCharacterRequest(GameData data,String block) {
		String id = getGameObject().getAttribute(block,CHAR_ID);
		GameObject go = data.getGameObject(Long.valueOf(id));
		CharacterWrapper character = new CharacterWrapper(go);
		String reqKey = getGameObject().getAttribute(block,REQ_KEY);
		return new CharacterRequest(character,reqKey);
	}
	public CharacterRequest getCharacterRequest(CharacterWrapper character) {
		GameData data = getGameObject().getGameData();
		for (Iterator i=getBlockList().iterator();i.hasNext();) {
			String block = (String)i.next();
			if (getGameObject().hasAttributeBlock(block)) {
				CharacterRequest request = getCharacterRequest(data,block);
				if (character.getGameObject().equals(request.getCharacter().getGameObject())) {
					return request;
				}
			}
		}
		return null;
	}
	public boolean clearRequest(CharacterRequest request) {
		GameData data = getGameObject().getGameData();
		for (Iterator i=getBlockList().iterator();i.hasNext();) {
			String block = (String)i.next();
			if (getGameObject().hasAttributeBlock(block)) {
				CharacterRequest aRequest = getCharacterRequest(data,block);
				if (request.equals(aRequest)) {
					getGameObject().removeAttributeBlock(block);
					return true;
				}
			}
		}
		return false;
	}
}