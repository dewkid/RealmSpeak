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

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.general.util.OrderedHashtable;

public class GameObjectBlockManager {
	private static final String PREFIX = "_GOBM_XX_";
	
	private String prefix;
	private GameObject gameObject;
	
	public GameObjectBlockManager(GameObject gameObject) {
		this(gameObject,PREFIX);
	}
	public GameObjectBlockManager(GameObject gameObject,String attributeObscuringPrefix) {
		this.gameObject = gameObject;
		this.prefix = attributeObscuringPrefix;
	}
	public void storeGameObjectInBlocks(GameObject go,String baseBlockKey) {
		gameObject.setAttribute(baseBlockKey,prefix+"Name",go.getName());
		for (Iterator i=go.getAttributeBlockNames().iterator();i.hasNext();) {
			String blockName = (String)i.next();
			String newBlockName = baseBlockKey+blockName;
			OrderedHashtable block = go.getAttributeBlock(blockName);
			for (Iterator n=block.keySet().iterator();n.hasNext();) {
				String key = (String)n.next();
				Object val = block.get(key);
				if (val instanceof ArrayList) {
					ArrayList copy = new ArrayList((ArrayList)val);
					gameObject.setAttributeList(newBlockName,prefix+key,copy);
				}
				else {
					gameObject.setAttribute(newBlockName,prefix+key,(String)val);
				}
			}
		}
	}
	public GameObject extractGameObjectFromBlocks(String baseBlockKey,boolean freeStandingGameObject) {
		return extractGameObjectFromBlocks(baseBlockKey,freeStandingGameObject,true);
	}
	public GameObject extractGameObjectFromBlocks(String baseBlockKey,boolean freeStandingGameObject,boolean requireAttributes) {
		GameObject go = freeStandingGameObject?GameObject.createEmptyGameObject():gameObject.getGameData().createNewObject();
		go.setName(gameObject.getAttribute(baseBlockKey,prefix+"Name"));
		boolean foundAttributes = false;
		for (Iterator i=gameObject.getAttributeBlockNames().iterator();i.hasNext();) {
			String blockName = (String)i.next();
			if (!blockName.equals(baseBlockKey) && blockName.startsWith(baseBlockKey)) {
				foundAttributes = true;
				String originalBlockName = blockName.substring(baseBlockKey.length());
				OrderedHashtable block = gameObject.getAttributeBlock(blockName);
				for (Iterator n=block.keySet().iterator();n.hasNext();) {
					String key = (String)n.next();
					Object val = block.get(key);
					String unobscuredKey = key.substring(prefix.length());
					if (val instanceof ArrayList) {
						ArrayList copy = new ArrayList((ArrayList)val);
						go.setAttributeList(originalBlockName,unobscuredKey,copy);
					}
					else {
						go.setAttribute(originalBlockName,unobscuredKey,(String)val);
					}
				}
			}
		}
		return foundAttributes||!requireAttributes ? go : null;
	}
	public void clearBlocks(String baseBlockKey) {
		ArrayList<String> remove = new ArrayList<String>();
		for (Iterator i=gameObject.getAttributeBlockNames().iterator();i.hasNext();) {
			String blockName = (String)i.next();
			if (blockName.startsWith(baseBlockKey)) {
				remove.add(blockName);
			}
		}
		for (String blockName:remove) {
			gameObject.removeAttributeBlock(blockName);
		}
	}
}