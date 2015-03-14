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
package com.robin.magic_realm.components.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

/**
 * A class to encapsulate the logic of fetching groups of objects, based on current host settings.  It should
 * also be able to "remember" queries, and not requery when the keyVals are the same.  (Have to be careful with
 * that, in case I'm querying for something that may change)
 */
public class RealmObjectMaster {
	
	private static HashMap map = null;
	
	private GameData data = null;
	private HostPrefWrapper hostPrefs = null;
	private int gameObjectCountForPlayers = -1;
	private ArrayList<GameObject> playerCharacterObjects = null;
	private ArrayList<GameObject> denizenObjects = null;
	private ArrayList<GameObject> tileObjects = null;
	private ArrayList<GameObject> dwellingObjects = null;
	
	private RealmObjectMaster(GameData data) {
		this.data = data;
		this.hostPrefs = HostPrefWrapper.findHostPrefs(data);
	}
	
	public static void resetAll() {
		if (map!=null) {
			map.clear();
			map = null;
		}
	}
	
	public ArrayList<GameObject> findObjects(String baseQuery,ArrayList keyVals,boolean asComponents) {
		String query = StringUtilities.collectionToString(keyVals,",");
		if (baseQuery!=null && baseQuery.length()>0) {
			if (query.length()>0) {
				query += ",";
			}
			query += baseQuery;
		}
		return findObjects(query,asComponents);
	}
	
	public ArrayList<GameObject> findObjects(String keyVals,boolean asComponents) {
		keyVals = hostPrefs.getGameKeyVals()+","+keyVals;
//System.out.println("findObjects for "+keyVals);
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> objects = pool.find(keyVals);
		if (asComponents) {
			ArrayList list = new ArrayList();
			for (Iterator i=objects.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				list.add(RealmComponent.getRealmComponent(go));
			}
			return list;
		}
//System.out.println("found: "+objects);
		return objects;
	}
	
	/**
	 * @return		A Collection of all characters (dead or alive), all Native Leaders (hired or not, dead or alive),
	 * 				and all the monsters (they may get controlled!)
	 */
	public ArrayList<GameObject> getPlayerCharacterObjects() {
		int dataSize = data.getGameObjects().size();
		if (gameObjectCountForPlayers!=dataSize) {
			gameObjectCountForPlayers = dataSize;
			playerCharacterObjects = null;
		}
		if (playerCharacterObjects==null) {
			playerCharacterObjects = new ArrayList<GameObject>();
			playerCharacterObjects.addAll(findObjects("character",false));
			playerCharacterObjects.addAll(findObjects("native,rank",false)); // not just leaders anymore, due to Hypnotize spell!
			playerCharacterObjects.addAll(findObjects("monster,!part",false));
//			playerCharacterObjects.addAll(getCachedObjects("familiar",false));
		}
		return playerCharacterObjects;
	}
	
	/**
	 * @return		A Collection of all natives & monsters, dead or alive, controlled/hired or not.
	 */
	public ArrayList<GameObject> getDenizenObjects() {
		if (denizenObjects==null) {
			denizenObjects = new ArrayList<GameObject>();
			denizenObjects.addAll(findObjects("native,rank",false));
			denizenObjects.addAll(findObjects("monster,!part",false));
		}
		return denizenObjects;
	}
	
	/**
	 * @return		A Collection of all the tile objects
	 */
	public ArrayList<GameObject> getTileObjects() {
		if (tileObjects==null) {
			tileObjects = new ArrayList<GameObject>();
			tileObjects.addAll(findObjects("tile",false));
		}
		return tileObjects;
	}
	public void resetTileObjects() {
		tileObjects = null;
	}
	
	/**
	 * @return		A Collection of all the dwelling objects
	 */
	public ArrayList<GameObject> getDwellingObjects() {
		if (dwellingObjects==null) {
			dwellingObjects = new ArrayList<GameObject>();
			dwellingObjects.addAll(findObjects("dwelling",false));
			dwellingObjects.addAll(findObjects("guild",false));
		}
		return dwellingObjects;
	}
	
	/**
	 * @return		The relevant RealmObjectMaster associated with the provided GameData
	 */
	public static RealmObjectMaster getRealmObjectMaster(GameData data) {
		if (map==null) {
			map = new HashMap();
		}
		Long id = new Long(data.getDataId());
		RealmObjectMaster rom = (RealmObjectMaster)map.get(id);
		if (rom==null) {
			rom = new RealmObjectMaster(data);
			map.put(id,rom);
		}
		return rom;
	}
}