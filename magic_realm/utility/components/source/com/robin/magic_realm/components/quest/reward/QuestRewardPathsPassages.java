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

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.PathDetail;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.MapScopeType;
import com.robin.magic_realm.components.quest.RoadDiscoveryType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardPathsPassages extends QuestReward {
	
	public static String DISCOVERY_TYPE = "_dst";
	public static String DISCOVERY_SCOPE = "_dss";

	public QuestRewardPathsPassages(GameObject go) {
		super(go);
	}
	
	public void processReward(JFrame frame, CharacterWrapper character) {
		MapScopeType mapScope = getScopeType();
		RoadDiscoveryType discoveryType = getDiscoveryType();
		
		TileLocation current = character.getCurrentLocation();
		
		ArrayList<PathDetail> roads = new ArrayList<PathDetail>();
		if (mapScope==MapScopeType.Clearing) {
			if (!current.isInClearing()) return;
			for(PathDetail path:current.clearing.getAllConnectedPaths()) {
				if (discoveryType.matches(path)) {
					roads.add(path);
				}
			}
		}
		else { // Tile
			if (discoveryType.matchesSecretPassages()) {
				roads.addAll(current.tile.getSecretPassages(true));
			}
			if (discoveryType.matchesHiddenPaths()) {
				roads.addAll(current.tile.getHiddenPaths(true));
			}
		}
		
		for(PathDetail road:roads) {
			String key = road.getFullPathKey();
			if (road.isSecret() && !character.hasSecretPassageDiscovery(key)) {
				character.addSecretPassageDiscovery(key);
			}
			if (road.isHidden() && !character.hasHiddenPathDiscovery(key)) {
				character.addHiddenPathDiscovery(key);
			}
		}
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Immediately discover all ");
		RoadDiscoveryType rdt = getDiscoveryType();
		switch(rdt) {
			case HiddenPaths:
				sb.append("hidden paths");
				break;
			case SecretPassages:
				sb.append("secret passages");
				break;
			case PathsOrPassages:
				sb.append("hidden paths and secret passages");
				break;
		}
		MapScopeType scope = getScopeType();
		switch(scope) {
			case Clearing:
				sb.append(" in the current clearing.");
				break;
			case Tile:
				sb.append(" in the current tile.");
				break;
		}
		return sb.toString();
	}

	public RewardType getRewardType() {
		return RewardType.PathsPassages;
	}
	
	public RoadDiscoveryType getDiscoveryType() {
		return RoadDiscoveryType.valueOf(getString(DISCOVERY_TYPE));
	}
	
	public MapScopeType getScopeType() {
		return MapScopeType.valueOf(getString(DISCOVERY_SCOPE));
	}
}