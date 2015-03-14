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
package com.robin.magic_realm.RealmBattle;

import java.util.*;

import com.robin.game.objects.GameObject;

public class BattleSummary {
	
	private ArrayList<GameObject> orderedAttackers;
	private Hashtable<GameObject,ArrayList<GameObject>> hash;
	
	public BattleSummary() {
		orderedAttackers = new ArrayList<GameObject>();
		hash = new Hashtable<GameObject,ArrayList<GameObject>>();
	}
	public void addAttackerTarget(GameObject attacker,GameObject target) {
		ArrayList<GameObject> targets = hash.get(attacker);
		if (targets==null) {
			targets = new ArrayList<GameObject>();
			hash.put(attacker,targets);
			orderedAttackers.add(attacker);
		}
		targets.add(target);
	}
	public ArrayList<BattleSummaryRow> getSummaryRows() {
		ArrayList<BattleSummaryRow> list =  new ArrayList<BattleSummaryRow>();
		int n=0;
		for (GameObject attacker:orderedAttackers) {
			for (GameObject target:hash.get(attacker)) {
				list.add(new BattleSummaryRow(attacker,target,n++));
			}
		}
		Collections.sort(list);
		return list;
	}
}