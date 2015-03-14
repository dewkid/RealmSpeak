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

import java.util.*;

import com.robin.game.objects.*;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class TemplateLibrary {
	
	public static final String WEAPON_QUERY = "weapon,!character,!magic";
	public static final String CHARACTER_QUERY = "character";
//	public static final String ARMOR_QUERY = "armor,!character,!treasure,!magic"; // Don't think I'll worry about these after all
	
	private static TemplateLibrary singleton = null;
	public static TemplateLibrary getSingleton() {
		if (singleton==null) {
			singleton = new TemplateLibrary();
		}
		return singleton;
	}
	
	private GamePool dataPool;
	private Hashtable<String,GameObject> templateHash;
	
	private TemplateLibrary() {
		RealmLoader loader = new RealmLoader();
		dataPool = new GamePool(loader.getData().getGameObjects());
		templateHash = new Hashtable<String,GameObject>();
		addQuery(dataPool,WEAPON_QUERY);
		addQuery(dataPool,CHARACTER_QUERY);
//		addQuery(pool,ARMOR_QUERY);
	}
	private void addQuery(GamePool pool,String query) {
		ArrayList<GameObject> items = pool.find(query);
		for (GameObject go:items) {
			String name = go.getName();
			if (!templateHash.containsKey(name)) {
				addTemplate(name,go);
			}
		}
	}
	public ArrayList<String> getAllWeaponNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (String name:getAllNames()) {
			GameObject go = getWeaponTemplate(name);
			if (go!=null) {
				names.add(name);
			}
		}
		return names;
	}
	public GameObject getWeaponTemplate(String name) {
		GameObject go = templateHash.get(name);
		if (go!=null && go.hasAllKeyVals(WEAPON_QUERY)) {
			return go;
		}
		return null;
	}
	public boolean hasWeaponTemplate(String name) {
		return getWeaponTemplate(name)!=null;
	}
	public GameObject getCharacterTemplate(String name) {
		GameObject go = templateHash.get(name);
		if (go!=null && go.hasAllKeyVals(CHARACTER_QUERY)) {
			return go;
		}
		return null;
	}
	public ArrayList<String> getAllCharacterTemplateNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (String name:getAllNames()) {
			GameObject go = getCharacterTemplate(name);
			if (go!=null) {
				names.add(name);
			}
		}
		return names;
	}
	public ArrayList<String> getAllNames() {
		return new ArrayList<String>(templateHash.keySet());
	}
	private void addTemplate(String name,GameObject go) {
		templateHash.put(name,go);
	}
	public GameObject getCompanionTemplate(String name,String query) {
		GameObject template = templateHash.get(name);
		if (template==null) {
			if (query.startsWith("Transform|")) {
				ArrayList<GameObject> list = dataPool.find("name=Transform");
				if (list!=null && !list.isEmpty()) {
					GameObject go = list.get(0);
					String block = query.substring(10);
					template = GameObject.createEmptyGameObject();
					SpellWrapper.copyTransformToObject(go,block,template);
				}
			}
			else {
				ArrayList<GameObject> list = dataPool.find(query);
				if (list!=null && !list.isEmpty()) {
					GameObject go = list.get(0);
					template = GameObject.createEmptyGameObject();
					template.copyAttributesFrom(go);
					
					// Get steeds
					for (Iterator i=go.getHold().iterator();i.hasNext();) {
						GameObject held = (GameObject)i.next();
						GameObject heldTemplate = GameObject.createEmptyGameObject();
						heldTemplate.copyAttributesFrom(held);
						heldTemplate.setAttribute("trot","chit_color","paleyellow");
						heldTemplate.setAttribute("gallop","chit_color","yellow");
						heldTemplate.setThisAttribute("companion");
						template.add(heldTemplate);
					}
					
					// update some specific attributes
					template.removeThisAttribute("rank");
					template.removeThisAttribute("setup_start");
					template.removeThisAttribute("monster_die");
					template.removeThisAttribute("hire_type");
				}
			}
			if (template!=null) {
				template.setName(name);
				template.setAttribute("light","chit_color","paleyellow");
				template.setAttribute("dark","chit_color","yellow");
				template.setThisAttribute("companion"); // Makes these guys easy to find
				template.setThisAttribute("query",query);
				
				templateHash.put(name,template);
			}
		}
		return template;
	}
	public GameObject createCompanionFromTemplate(GameData gameData,GameObject go) {
		GameObject companion = gameData.createNewObject();
		companion.copyAttributesFrom(go);
		// Get steeds
		for (Iterator i=go.getHold().iterator();i.hasNext();) {
			GameObject held = (GameObject)i.next();
			GameObject heldTemplate = gameData.createNewObject();
			heldTemplate.copyAttributesFrom(held);
			companion.add(heldTemplate);
		}
		return companion;
	}
}