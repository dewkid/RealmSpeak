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


public abstract class GameObjectWrapper {

	private GameObject gameObject;
	
	public GameObjectWrapper(GameObject obj) {
		gameObject = obj;
		if (gameObject==null) {
			throw new IllegalArgumentException("Can't make a wrapper with a null GameObject!!");
		}
	}
	public boolean equals(Object o1) {
		if (o1 instanceof GameObjectWrapper) {
			if (o1.getClass().getName().equals(getClass().getName())) {
				return gameObject.equals(((GameObjectWrapper)o1).gameObject);
			}
		}
		return false;
	}
	/**
	 * @return	Returns the underlying object
	 */
	public GameObject getGameObject() {
		return gameObject;
	}
	public GameData getGameData() {
		return gameObject.getGameData();
	}
	public abstract String getBlockName();
	
	public void clear(String key) {
		gameObject.removeAttribute(getBlockName(),key);
	}
	public void setName(String name) {
		getGameObject().setName(name);
	}
	
	public String getName() {
		return getGameObject().getName();
	}
	
	
	/**
	 * Utility method for extracting the int value
	 */
	public int getInt(String key) {
		return gameObject.getInt(getBlockName(),key);
	}
	/**
	 * Utility method for extracting the int value
	 */
	public double getDouble(String key) {
		String val = (String)gameObject.getAttribute(getBlockName(),key);
		if (val!=null) {
			try {
				return Double.valueOf(val).doubleValue();
			}
			catch(NumberFormatException ex) {
			}
		}
		return 0.0;
	}
	/**
	 * Utility method for extracting the String value
	 */
	public String getString(String key) {
		return gameObject.getAttribute(getBlockName(),key);
	}
	public boolean getBoolean(String key) {
		return gameObject.hasAttribute(getBlockName(),key);
	}
	public ArrayList getList(String key) {
		return gameObject.getAttributeList(getBlockName(),key);
	}
	public int getListCount(String key) {
		ArrayList list = getList(key);
		return list==null?0:list.size();
	}
	public void setInt(String key,int val) {
		gameObject.setAttribute(getBlockName(),key,String.valueOf(val));
	}
	public void setDouble(String key,double val) {
		gameObject.setAttribute(getBlockName(),key,String.valueOf(val));
	}
	public void setString(String key,String val) {
		if (val==null) {
			gameObject.removeAttribute(getBlockName(),key);
		}
		else {
			gameObject.setAttribute(getBlockName(),key,val);
		}
	}
	public void setBoolean(String key,boolean val) {
		if (val) {
			gameObject.setAttribute(getBlockName(),key);
		}
		else {
			gameObject.removeAttribute(getBlockName(),key);
		}
	}
	public void setList(String key,ArrayList in) {
		gameObject.setAttributeList(getBlockName(),key,in);
	}
	public void addListItem(String key,String val) {
		gameObject.addAttributeListItem(getBlockName(),key,val);
	}
	public boolean removeListItem(String key,String val) {
		boolean ret = false;
		ArrayList list = getList(key);
		if (list!=null && list.contains(val)) {
			list = new ArrayList(list);
			ret = list.remove(val);
			setList(key,list);
		}
		return ret;
	}
	public boolean hasListItem(String key,String val) {
		return gameObject.hasAttributeListItem(getBlockName(),key,val);
	}
	public void removeAttribute(String key) {
		if (gameObject.hasAttribute(getBlockName(),key)) {
			gameObject.removeAttribute(getBlockName(),key);
		}
	}
	
	/////////////////////////////////
	
//	private static HashMap cache = null;
//	public static Wrapper getCachedWrapper(String name,GameData data) {
//		return null;
//	}
//	public static void cacheWrapper(String name,GameData data,Wrapper wrapper) {
//		if (cache==null) {
//			cache = new HashMap();
//		}
//		String key = name+data.dataid;
//		cache.put(key,wrapper);
//	}
//	public static void clearCaches() {
//		if (cache!=null) {
//			cache.clear();
//			cache = null;
//		}
//	}
}