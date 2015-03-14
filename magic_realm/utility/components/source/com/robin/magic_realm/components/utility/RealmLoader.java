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
import java.util.Iterator;

import com.robin.game.objects.*;

public class RealmLoader {

	public static final String DATA_PATH = "data/MagicRealmData.xml";

	private GameData master; // needed to determine changes
	private GameData data;

	public RealmLoader() {
		master = new GameData();
		master.loadFromPath(DATA_PATH);

		data = new GameData();
		data.loadFromPath(DATA_PATH);
	}
	
	public void cleanupData(String keyVals) {
		long maxid = master.getMaxId();
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> found = pool.find(keyVals);
		ArrayList<GameObject> toDelete = new ArrayList<GameObject>();
		for (Iterator i=data.getGameObjects().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			if (go.getId()<=maxid) { // only consider objects in the master
				if (!found.contains(go)) {
					// Make sure it isn't held by...
					GameObject hb = go;
					while(hb.getHeldBy()!=null) {
						hb = hb.getHeldBy();
					}
					if (!found.contains(hb)) {
						toDelete.add(go);
					}
				}
			}
		}
		for (GameObject go:toDelete) {
			data.removeObject(go);
		}
	}

	public GameData getMaster() {
		return master;
	}

	public GameData getData() {
		return data;
	}
	
	public static void main(String[] args) {
		RealmLoader loader = new RealmLoader();
		GamePool pool = new GamePool(loader.getData().getGameObjects());
		ArrayList<String> query = new ArrayList<String>();
		query.add("rw_expansion_1");
		query.add("treasure");
		String tab = "\t";
		System.out.println(
				"Name"
				+tab+"Great"
				+tab+"Large"
				+tab+"Discard"
				+tab+"Weight"
				+tab+"Fame Reward"
				+tab+"Fame"
				+tab+"Notoriety"
				+tab+"Gold"
				+tab+"Text"
				);
		for(GameObject go:pool.find(query)) {
			int twt = go.getThisInt("treasure_within_treasure");
			String great = go.hasThisAttribute("great")?"Great":" ";
			String large = twt==0?(go.getThisAttribute("treasure").equals("large")?"Large":" "):("P"+twt);
			String discard = go.getThisAttribute("discard");
			if (discard == null) discard = " ";
			String nat = go.getThisAttribute("native");
			if (nat==null) nat = " ";
			System.out.println(go.getName()
					+tab+great
					+tab+large
					+tab+discard
					+tab+go.getThisAttribute(Constants.WEIGHT)
					+tab+nat
					+tab+go.getThisInt("fame")
					+tab+go.getThisInt("notoriety")
					+tab+go.getThisInt("base_price")
					+tab+go.getThisAttribute("text"));
		}
	}
}