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

public class GameQuery {
	
	private String blockName;
	
	public GameQuery() {
	}
	public GameQuery(String blockName) {
		this.blockName = blockName;
	}
	public boolean hasGameObjectWithKey(ArrayList<GameObject> list,String key) {
		return firstGameObjectWithKey(list,key)!=null;
	}
	public boolean hasGameObjectWithKeyAndValue(ArrayList<GameObject> list,String key,String value) {
		return firstGameObjectWithKeyAndValue(list,key,value)!=null;
	}
	public GameObject firstGameObjectWithKey(ArrayList<GameObject> list,String key) {
		ArrayList<GameObject> ret = query(list,key,null,true);
		return ret.isEmpty()?null:ret.get(0);
	}
	public GameObject firstGameObjectWithKeyAndValue(ArrayList<GameObject> list,String key,String value) {
		ArrayList<GameObject> ret = query(list,key,value,true);
		return ret.isEmpty()?null:ret.get(0);
	}
	public ArrayList<GameObject> allGameObjectsWithKey(ArrayList<GameObject> list,String key) {
		return query(list,key,null,false);
	}
	public ArrayList<GameObject> allGameObjectsWithKeyAndValue(ArrayList<GameObject> list,String key,String value) {
		return query(list,key,value,false);
	}
	private ArrayList<GameObject> query(ArrayList<GameObject> list,String key,String value,boolean stopAtFirst) {
		ArrayList<GameObject> ret = new ArrayList<GameObject>();
		for (GameObject go:list) {
			ArrayList<String> blockNames = new ArrayList<String>();
			if (blockName!=null) {
				blockNames.add(blockName);
			}
			else {
				blockNames.addAll(go.getAttributeBlockNames());
			}
			for (String bn:blockNames) {
				if (value==null && go.hasAttribute(bn,key)) {
					ret.add(go);
				}
				else if (value!=null) {
					Object val = go.getObject(bn,key);
					boolean found = false;
					if (val instanceof ArrayList) {
						found = ((ArrayList)val).contains(value);
					}
					else {
						found = value.equals(val); 
					}
					if (found) {
						ret.add(go);
					}
				}
				if (stopAtFirst && !ret.isEmpty()) {
					return ret;
				}
			}
		}
		return ret;
	}
}