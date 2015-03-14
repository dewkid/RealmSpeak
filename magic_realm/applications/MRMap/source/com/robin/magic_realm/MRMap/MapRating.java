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
package com.robin.magic_realm.MRMap;

import java.util.ArrayList;

import com.robin.game.objects.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.ClearingUtility;
import com.robin.magic_realm.components.utility.RealmObjectMaster;

public class MapRating {
	public static int getMapRating(GameData data) {
		/*
		 * For every valley tile, count the number of accessible tiles from the highest connected clearing,
		 * without crossing a cave clearing, any hidden path or secret passage.  The lowest count of tiles
		 * for any one of those, will be the rating for the entire map.
		 */
		
		ClearingUtility.initAdjacentTiles(data);
		
		ArrayList<GameObject> tiles = RealmObjectMaster.getRealmObjectMaster(data).getTileObjects();
		GamePool pool = new GamePool(tiles);
		ArrayList<GameObject> valleyTiles = pool.find("tile,tile_type=V");
		int rating = Integer.MAX_VALUE;
		for (GameObject go:valleyTiles) {
			rating = Math.min(rating,getTileRating(go));
		}
		
		return rating;
	}
	private static int getTileRating(GameObject go) {
		TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
		int clearingNum = ClearingUtility.recommendedClearing(go);
		ArrayList<ClearingDetail> search = new ArrayList<ClearingDetail>();
		search.add(tile.getClearing(clearingNum)); // seed clearing
		
		// First, find ALL connected clearings to the start point
		ArrayList<ClearingDetail> found = new ArrayList<ClearingDetail>();
		while(!search.isEmpty()) {
			ArrayList<ClearingDetail> next = new ArrayList<ClearingDetail>();
			for (ClearingDetail clearing:search) {
				for (PathDetail path:clearing.getAllConnectedPaths()) {
					if (path.isHidden() || path.isSecret()) continue;
					ClearingDetail otherEnd = path.findConnection(clearing);
					if (otherEnd==null || otherEnd.isCave() || otherEnd.isEdge() || found.contains(otherEnd)) continue;
					
					found.add(otherEnd);
					next.add(otherEnd);
				}
			}
			search = next;
		}
		
		// Now, count the number of individual tiles involved
		ArrayList<TileComponent> connectedTiles = new ArrayList<TileComponent>();
		for (ClearingDetail clearing:found) {
			if (!connectedTiles.contains(clearing.getParent())) {
				connectedTiles.add(clearing.getParent());
			}
		}
//System.out.println(go.getName()+":  "+found.size()+" clearings, and "+connectedTiles.size()+" tiles");
		return connectedTiles.size()-1;		// don't count the original tile
	}
}