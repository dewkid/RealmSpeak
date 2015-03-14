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

import org.junit.Assert;
import org.junit.Test;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.*;

public class DieRuleTest extends TestBaseWithLoader {
	
	@Test
	public void testOneDie() {
		DieRule dr = new DieRule(null,"1d:hide:all");
		Assert.assertTrue(dr.isOneDie());
	}

	@Test
	public void testMinusOne() {
		DieRule dr = new DieRule(null,"-1:hide:all");
		Assert.assertTrue(dr.isMinusOne());
	}

	@Test
	public void testPlusOne() {
		DieRule dr = new DieRule(null,"+1:hide:all");
		Assert.assertTrue(dr.isPlusOne());
	}
	
	@Test
	public void testMinusTwo() {
		DieRule dr = new DieRule(null,"-2:peer:all");
		Assert.assertTrue(dr.isMinusTwo());
	}
	
	@Test
	public void testRuinsTileName() {
		GameObject go = findGameObject("Ruins");
		TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
		ArrayList<String> list = tile.getChitDescriptionList();
		DieRule dr = new DieRule(null,"-1:locate:%ruins%");
		Assert.assertTrue(dr.conditionsMet("locate",list));
	}
	@Test
	public void testRuinsChit() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("cliff");
		list.add("ruins m");
		list.add("flutter");
		DieRule dr = new DieRule(null,"-1:locate:%ruins%");
		Assert.assertTrue(dr.conditionsMet("locate",list));
		
	}
	@Test
	public void testLostCityChit() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("cliff");
		list.add("lost city b");
		list.add("flutter");
		DieRule dr = new DieRule(null,"-1:locate:lost city%");
		Assert.assertTrue(dr.conditionsMet("locate",list));
	}
	@Test
	public void testWoodsTile() {
		GameObject go = findGameObject("Deep Woods");
		TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
		ArrayList<String> list = tile.getChitDescriptionList();
		DieRule dr = new DieRule(null,"-1:locate:% woods");
		Assert.assertTrue(dr.conditionsMet("locate",list));
	}
	@Test
	public void testNotWoods() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("cliff");
		list.add("lost city b");
		list.add("flutter");
		list.add("woodsgirl's cache");
		DieRule dr = new DieRule(null,"-1:locate:% woods");
		Assert.assertTrue(!dr.conditionsMet("locate",list));
	}
	@Test
	public void testAllDieModsLists() {
		ArrayList<String> query = new ArrayList<String>();
		query.add(Constants.DIEMOD);
		ArrayList<GameObject> dieModObjs = findGameObjects(query);
		for (GameObject go:dieModObjs) {
			for (Iterator i=go.getAttributeBlockNames().iterator();i.hasNext();) {
				String blockName = (String)i.next();
				if (!go.hasAttribute(blockName,Constants.DIEMOD)) continue;
				go.getAttributeList(blockName,Constants.DIEMOD);
			}
		}
	}
}