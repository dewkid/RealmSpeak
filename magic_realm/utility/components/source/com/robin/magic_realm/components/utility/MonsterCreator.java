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

import com.robin.game.objects.*;

public class MonsterCreator {
	
	private String monsterKey;
	private ArrayList<GameObject> monstersCreated;
	
	public MonsterCreator(String monsterKey) {
		this.monsterKey = monsterKey;
		monstersCreated = new ArrayList<GameObject>();
	}
	public ArrayList<GameObject> getMonstersCreated() {
		return monstersCreated;
	}
	public GameObject createOrReuseMonster(GameData data) {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList query = new ArrayList();
		query.add(monsterKey);
		query.add(Constants.DEAD);
		GameObject go = pool.findFirst(query);
		if (go==null) {
			go = data.createNewObject();
		}
		monstersCreated.add(go);
		go.removeThisAttribute(Constants.DEAD); // shouldn't be DEAD anymore
		SetupCardUtility.updateGeneratedMonsterInt(go);
		return go;
	}
	public void setupSide(GameObject go,String side,String strength,int sharpness,int attackSpeed,int attackLength,int moveSpeed,String color) {
		go.removeAttribute(side,"strength");
		go.removeAttribute(side,"attack_speed");
		go.removeAttribute(side,"sharpness");
		go.removeAttribute(side,"length");
		if (strength!=null) {
			go.setAttribute(side,"strength",strength);
			go.setAttribute(side,"attack_speed",attackSpeed);
			go.setAttribute(side,"length",attackLength);
			if (sharpness>0) {
				go.setAttribute(side,"sharpness",sharpness);
			}
		}
		go.setAttribute(side,"move_speed",moveSpeed);
		go.setAttribute(side,"chit_color",color);
	}
	public void setupGameObject(GameObject go,String name,String iconType,String vulnerability,boolean armored) {
		setupGameObject(go,name,iconType,vulnerability,armored,false);
	}
	public void setupGameObject(GameObject go,String name,String iconType,String vulnerability,boolean armored,boolean flies) {
		go.setName(name);
		go.setThisAttribute("monster");
		go.setThisAttribute(monsterKey);
		go.setThisAttribute("vulnerability",vulnerability);
		go.setThisAttribute("icon_type",iconType);
		go.setThisAttribute("icon_folder","monsters2");
		go.removeThisAttribute(Constants.ARMORED);
		go.removeThisAttribute("flying");
		if (armored) {
			go.setThisAttribute(Constants.ARMORED);
		}
		if (flies) {
			go.setThisAttribute("flying");
		}
	}
}