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

import java.awt.Point;
import java.util.*;

import com.robin.game.objects.*;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmObjectMaster;
import com.robin.magic_realm.map.Tile;

public class MapBuilder {
	
	public static ArrayList startTileList(GameData data,Collection keyVals) {
		ArrayList tiles = new ArrayList();
		Collection c = RealmObjectMaster.getRealmObjectMaster(data).getTileObjects();
		for (Iterator i=c.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			tiles.add(new Tile(obj));
		}
		return tiles;
	}
	public static Tile findBorderland(Collection tiles) {
		// Find the Borderland tile, and start it at position 0,0 with a random rotation
		for (Iterator i=tiles.iterator();i.hasNext();) {
			Tile tile = (Tile)i.next();
			if (tile.getGameObject().getName().equals("Borderland")) {
				return tile;
			}
		}
		throw new IllegalStateException("Borderland is missing from tiles!!");
	}

	public static boolean autoBuildMap(GameData data,Collection keyVals) {
		return autoBuildMap(data,keyVals,null);
	}
	public static boolean autoBuildMap(GameData data,Collection keyVals,MapProgressReportable reporter) {
		ArrayList tiles = startTileList(data,keyVals);
		
		// Find the Borderland tile, and start it at position 0,0 with a random rotation
		Hashtable mapGrid = new Hashtable();
		Tile borderland = findBorderland(tiles);
		mapGrid.put(new Point(0,0),borderland);
		borderland.setMapPosition(new Point(0,0));
		borderland.setRotation(RandomNumber.getRandom(6));
		
		if (reporter!=null) {
			reporter.setProgress(1,tiles.size());
		}
		
		// Cycle until the mapGrid has all the tiles
System.out.println();
		while(mapGrid.size()<tiles.size()) {
			if (reporter!=null) {
				reporter.setProgress(mapGrid.size(),tiles.size());
			}
System.out.print(mapGrid.size()+":");
			// First, identify all connectable map placement locations
			//		- Have paths leading to them
			//		- Adjacent to at least two tiles (unless only one tile on map)
			ArrayList availableMapPositions = Tile.findAvailableMapPositions(mapGrid);
			
			// Cycle through every available (unplaced) tile
			ArrayList allTileResults = new ArrayList();
			for (Iterator t=tiles.iterator();t.hasNext();) {
				Tile tile = (Tile)t.next();
				
				// Only use unmapped tiles
				if (!mapGrid.contains(tile)) {
					ArrayList tileResults = new ArrayList();
					
					// Try the tile in every available position
					for (Iterator a=availableMapPositions.iterator();a.hasNext();) {
						Point pos = (Point)a.next();
						
						// Try every rotation
						for (int rot=0;rot<6;rot++) {
							// Test the tile at pos, with rotation rot
							if (Tile.isMappingPossibility(mapGrid,tile,pos,rot)) {
								tileResults.add(new TileMappingPossibility(tile,pos,rot));
							}
						}
					}
					if (tileResults.size()>0) {
						// Adding the tile results in by tile prevents unfair weighting per tile
						allTileResults.add(tileResults);
					}
				}
			}
			
			if (allTileResults.size()>0) {
				// First, pick a random tile result set
				ArrayList tileResults = (ArrayList)allTileResults.get(RandomNumber.getRandom(allTileResults.size()));
				
				// Then pick a random MappingResult from the set for this tile
				TileMappingPossibility tmp = (TileMappingPossibility)tileResults.get(RandomNumber.getRandom(tileResults.size()));
				
				// Add it to the grid
				Tile tile = tmp.getTile();
				if (tile.getClearingCount()==6) {
					/*
					 * This is a TOTAL hack, but should improve the speed of map building...
					 * 
					 * Basically by renaming all successfully placed Tile objects with 6 clearings as the anchor (Borderland),
					 * any logic that searches for connections to the anchor will stop when one of these tiles is located,
					 * shortening EVERY search.
					 * 
					 * Note that by renaming the Tile object, the GameObject is unaffected, so there is no harm.  Might be
					 * confusing if someone were to try to debug this code (How many friggen Borderlands are there!!) but
					 * I'm guessing that will never happen.  Famous last words....?
					 */
					tile.changeName(Tile.ANCHOR_TILENAME);
				}
				Point pos = tmp.getPosition();
				int rot = tmp.getRotation();
				
				tile.setRotation(rot);
				tile.setMapPosition(pos);
				
				mapGrid.put(pos,tile);
			}
			else {
				// It is bad if the allTileResults collection is empty at this point - means that none
				// of the remaining tiles can be placed at all!  We have a dead map, so return false.
				System.out.println(" - no more tile placement options!");
				return false;
			}
		}
		
		for (Iterator i=mapGrid.values().iterator();i.hasNext();) {
			Tile tile = (Tile)i.next();
			tile.writeToGameObject();
		}
		System.out.println();
		return true;
	}
	public static void main(String[]args) {
	    RealmLoader loader = new RealmLoader();
		GameData data = loader.getData();
		System.out.println("loaded "+data.getGameObjects().size());
		ArrayList keyVals = new ArrayList();
		keyVals.add("original_game");
		while(!MapBuilder.autoBuildMap(data,keyVals));
		System.out.println();
		for (Iterator i=data.getGameObjects().iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			if (obj.hasKey("tile")) {
				System.out.println(obj+":   "+obj.getAttributeBlock("mapGrid"));
			}
		}
	}
}