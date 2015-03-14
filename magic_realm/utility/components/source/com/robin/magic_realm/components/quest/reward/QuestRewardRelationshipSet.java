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
package com.robin.magic_realm.components.quest.reward;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.NativeChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.RelationshipType;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class QuestRewardRelationshipSet extends QuestReward {
	public static final String NATIVE_GROUP = "_ng";
	public static final String RELATIONSHIP_SET = "_rs";
	
	public QuestRewardRelationshipSet(GameObject go) {
		super(go);
	}

	public void processReward(JFrame frame,CharacterWrapper character) {
		ArrayList<GameObject> representativeNativesToChange = getRepresentativeNatives(character);
		if (representativeNativesToChange==null) return;
		
		// Do the change
		String name = getRelationshipName();
		int targetRel = RelationshipType.getIntFor(name);
		for(GameObject denizen:representativeNativesToChange) {
			int current = character.getRelationship(denizen);
			int diff = targetRel - current;
			character.changeRelationship(denizen,diff);
		}
	}
	
	public ArrayList<GameObject> getRepresentativeNatives(CharacterWrapper character) {
		TileLocation tl = character.getCurrentLocation();
		if (isAllNatives()) {
			if (!tl.isInClearing()) return null;
			return fetchNativesFromClearing(tl, character);
		}
		
		// Fetch the group leader - if multiple boards, then match the warning chit board
		GamePool pool = new GamePool(character.getGameData().getGameObjects());
		ArrayList<String> query = new ArrayList<String>();
		query.add("rank=HQ");
		query.add("native="+getNativeGroup());
		
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(character.getGameData());
		if (hostPrefs.getMultiBoardEnabled()) {
			for(Iterator i=tl.tile.getGameObject().getHold().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				if (go.hasThisAttribute("warning") && go.hasThisAttribute("chit")) {
					String board = go.getThisAttribute(Constants.BOARD_NUMBER);
					if (board!=null) {
						query.add(Constants.BOARD_NUMBER+"="+board);
					}
				}
			}
		}
		
		return pool.find(query);
	}
	
	public boolean isAllNatives() {
		String group = getNativeGroup();
		return group.equals("Clearing");
	}
	
	private ArrayList<GameObject> fetchNativesFromClearing(TileLocation tl, CharacterWrapper character) {
		
		ArrayList<String> groupsToChange = new ArrayList<String>();
		ArrayList<GameObject> representativeNativesToChange = new ArrayList<GameObject>();
		for(RealmComponent rc:tl.clearing.getClearingComponents()) {
			if (!rc.isNative()) continue;
			
			NativeChitComponent nat = (NativeChitComponent)rc;
			String groupName = nat.getGameObject().getThisAttribute("native");
			if (!groupsToChange.contains(groupName)) {
				groupsToChange.add(groupName);
				representativeNativesToChange.add(nat.getGameObject());
			}
		}
		return representativeNativesToChange;
	}
	
	public String getDescription() {
		return "Set relationship with "+getNativeGroup()+" to "+getRelationshipName();
	}
	
	public RewardType getRewardType() {
		return RewardType.RelationshipSet;
	}
	public String getNativeGroup() {
		return getString(NATIVE_GROUP);
	}
	public String getRelationshipName() {
		return getString(RELATIONSHIP_SET);
	}
}