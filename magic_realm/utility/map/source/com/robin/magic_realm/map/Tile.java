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
package com.robin.magic_realm.map;

import java.awt.Point;
import java.util.*;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;

public class Tile {
	public static final String ANCHOR_TILENAME = "Borderland";
	
	public static boolean debug = false;
	
	//										0  1  2  3  4  5  
	//										0  3  6  9 12 15  
	private static final String EDGE_MAP = "_S__SW_NW_N__NE_SE_";
	public static final String MAP_GRID = "mapGrid";
	public static final String MAP_POSITION = "mapPosition";
	public static final String MAP_ROTATION = "mapRotation";
	public static final int ROTATION_N		= 3;
	public static final int ROTATION_NE		= 4;
	public static final int ROTATION_NW		= 2;
	
	public static final int ROTATION_S		= 0;
	public static final int ROTATION_SE		= 5;
	public static final int ROTATION_SW		= 1;
	public static final int SIDE_ENCHANTED	= 1;
	
	public static final int SIDE_NORMAL		= 0;
	
	public static Point getAdjacentPosition(Point pos,int rot) {
		switch(rot) {
			case 0:		return new Point(pos.x,pos.y+1);
			case 1:		return new Point(pos.x-1,pos.y+1);
			case 2:		return new Point(pos.x-1,pos.y);
			case 3:		return new Point(pos.x,pos.y-1);
			case 4:		return new Point(pos.x+1,pos.y-1);
			case 5:		return new Point(pos.x+1,pos.y);
		}
		return null;
	}
	
	public static String getEdgeName(int edge) {
		String edgeName=null;
		switch(edge) {
			case 0:		edgeName = "S";   break;
			case 1:		edgeName = "SW";  break;
			case 2:		edgeName = "NW";  break;
			case 3:		edgeName = "N";   break;
			case 4:		edgeName = "NE";  break;
			case 5:		edgeName = "SE";  break;
		}
		return edgeName;
	}
	public static Point getPositionFromGameObject(GameObject obj) {
		String pos = obj.getAttribute(MAP_GRID,MAP_POSITION);
		StringTokenizer st = new StringTokenizer(pos,",");
		int px = Integer.valueOf(st.nextToken()).intValue();
		int py = Integer.valueOf(st.nextToken()).intValue();
		return new Point(px,py);
	}
	public static int getRelativeEdgeNumber(String val) {
		int index = EDGE_MAP.indexOf("_"+val+"_");
		if (index>=0) {
			return index/3;
		}
		throw new IllegalArgumentException("invalid edgename");
	}
	public static int getRotatedEdgeNumber(String val,int rot) {
		return (getRelativeEdgeNumber(val)+rot)%6;
	}
	public static int getRotationFromGameObject(GameObject obj) {
		String rot = obj.getAttribute(MAP_GRID,MAP_ROTATION);
		return Integer.valueOf(rot).intValue();
	}
	public static String convertEdge(String val,int rot) {
		return getEdgeName(getRotatedEdgeNumber(val,rot));
	}
	public static String matchingEdge(String val) {
		int n = getRelativeEdgeNumber(val);
		return getEdgeName((n+3)%6);
	}
	/**
	 * Reconstructs the mapGrid hash from the prebuilt gameobjects.  Note that this method can only be called
	 * AFTER setting up the map with buildMap
	 */
	public static Hashtable readMap(GameData data,Collection keyVals) {
		Hashtable mapGrid = new Hashtable();
		// loop through all gameObjects to get tiles
		GamePool pool = new GamePool(data.getGameObjects());
		for (Iterator i=pool.extract(keyVals).iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			if (obj.hasKey("tile")) {
				Tile tile = new Tile(obj);
				tile.readFromGameObject();
				mapGrid.put(tile.getMapPosition(),tile);
			}
		}
		return mapGrid;
	}
	
	protected ArrayList clearings;

	protected GameObject gameObject;
	
	protected String name;
	
	protected Hashtable[] paths;
	protected Point position;
	protected int rotation = ROTATION_S;
	protected int side = SIDE_NORMAL;
	protected boolean[] unrotatedPathState;
	
	public Tile(GameObject obj) {
		gameObject = obj;
		name = gameObject.getName();
		position = null;
		unrotatedPathState = new boolean[6];
		Arrays.fill(unrotatedPathState,false);
		build();
		if ("light".equals(obj.getThisAttribute("facing"))) {
			side = SIDE_NORMAL;
		}
		else {
			side = SIDE_ENCHANTED;
		}
	}
	public void build() {
		clearings = new ArrayList();
		paths = new Hashtable[2];
		paths[SIDE_NORMAL] = new Hashtable();
		buildPaths(paths[SIDE_NORMAL],gameObject.getAttributeBlock("normal"));
		paths[SIDE_ENCHANTED] = new Hashtable();
		buildPaths(paths[SIDE_ENCHANTED],gameObject.getAttributeBlock("enchanted"));
	}
	public void buildPaths(Hashtable pathHash,Hashtable objHash) {
		int n=1;
		while(true) {
			String baseKey = "path_"+n;
			if (objHash.get(baseKey+"_type")!=null) {
				String from = (String)objHash.get(baseKey+"_from");
				String to = (String)objHash.get(baseKey+"_to");
				
				updatePathHash(pathHash,from,to);
				updatePathHash(pathHash,to,from);
				updateClearingList(from);
				updateClearingList(to);
			}
			else {
				break;
			}
			n++;
		}
	}
	
	public void changeName(String name) {
		this.name = name;
	}
	
	public boolean connectsToTilename(Hashtable mapGrid,String clearingKey,String tilename) {
if (debug) System.out.println("------------------");
		return connectsToTilename(mapGrid,clearingKey,tilename,new ArrayList());
	} 
	private boolean connectsToTilename(Hashtable mapGrid,String clearingKey,String tilename,ArrayList touchedClearings) {
if (debug) System.out.println(name+":"+clearingKey);
		touchedClearings.add(name+":"+clearingKey);

		// Check the obvious
		if (name.equals(tilename)) {
			// This clearing is on the tile named tilename, so return true
			return true;
		}
	
		// Get all clearings connected to this clearing
		Collection c = getConnected(clearingKey);
		
		// Remove any clearings already "touched"
		if (c!=null && c.size()>0) {
			// Cycle through connected clearings
			for (Iterator i=c.iterator();i.hasNext();) {
				String connectedClearing = (String)i.next();
				if (!touchedClearings.contains(name+":"+connectedClearing)) {
					if (isEdge(connectedClearing)) {
						touchedClearings.add(name+":"+connectedClearing);
						// if path connects to edge, use mapGrid to determine new Tile and
						// call that tile's connectsToTilename method
						
						// First find the tile that connects on that side
						int realEdge = getRealEdgeNumber(connectedClearing);
						Point adjPos = getAdjacentPosition(position,realEdge);
						Tile adjTile = (Tile)mapGrid.get(adjPos);
						
						if (adjTile!=null) {
							// Find the edge of the adjacent tile that touches this tile
							int adjTileRealEdge = (realEdge+3)%6;
							int adjTileRelativeEdge = adjTileRealEdge-adjTile.getRotation();
							while(adjTileRelativeEdge<0) adjTileRelativeEdge+=6;
							Collection newTileClearings = adjTile.getConnected(getEdgeName(adjTileRelativeEdge));
							if (newTileClearings!=null) {
								for (Iterator nt=newTileClearings.iterator();nt.hasNext();) {
									String newTileClearing = (String)nt.next();
									if (adjTile.connectsToTilename(mapGrid,newTileClearing,tilename,touchedClearings)) {
										// The connected clearing on the adjacent tile connects to tilename, so this connects.
										return true;
									}
								}
							}
						}
					}
					else {
						if (connectsToTilename(mapGrid,connectedClearing,tilename,touchedClearings)) {
							// The connected clearing connects to tilename, so this connects.
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	// These methods are just here so I can code - their purpose will be coded later
	public int getClearingCount() {
		return clearings.size();
	}
	
	public Collection getConnected(String clearing) {
		return (Collection)paths[side].get(clearing);
	}
	public GameObject getGameObject() {
		return gameObject;
	}
	public Point getMapPosition() {
		return position;
	}
	/**
	 * Returns the edge path state (after rotation)
	 */
	public boolean[] getPathState() {
		boolean[] pathState = new boolean[6];
		for (int i=0;i<6;i++) {
			pathState[(i+rotation)%6] = unrotatedPathState[i];
		}
		return pathState;
	}
	public boolean getPathState(int edge) {
		boolean[] pathState = getPathState();
		return pathState[edge];
	}
	public int getRealEdgeNumber(String val) {
		return getRotatedEdgeNumber(val,rotation);
	}
	public int getRotation() {
		return rotation;
	}
	public boolean isEdge(String val) {
		return EDGE_MAP.indexOf("_"+val+"_")>=0;
	}
	public void readFromGameObject() {
		setMapPosition(getPositionFromGameObject(gameObject));
		setRotation(getRotationFromGameObject(gameObject));
	}
	public void setMapPosition(Point p) {
		position = p;
	}
	public void setRotation(int rot) {
		rotation = rot;
	}
	public String toString() {
		return name+":  "+position+"=="+rotation;
	}
	private void updateClearingList(String clearing) {
		if (EDGE_MAP.indexOf("_"+clearing+"_")==-1) {
			// Not an edge
			if (!clearings.contains(clearing)) {
				clearings.add(clearing);
			}
		}
	}
	
	private void updatePathHash(Hashtable pathHash,String from,String to) {
		ArrayList list = (ArrayList)pathHash.get(from);
		if (list==null) {
			list = new ArrayList();
			pathHash.put(from,list);
		}
		if (!list.contains(to)) {
			list.add(to);
		}
		int index = EDGE_MAP.indexOf("_"+from+"_");
		if (index>=0) {
			unrotatedPathState[index/3]=true;
		}
	}
	public void writeToGameObject() {
		gameObject.setAttribute(MAP_GRID,MAP_POSITION,position.x+","+position.y);
		gameObject.setAttribute(MAP_GRID,MAP_ROTATION,String.valueOf(rotation));
	}
	
//	private static final int[] REQ_CLEARINGS = {2,4,5,6};
	/**
	 * @param mapGrid	The map Hash of Point keys to Tile objects
	 * @param tile		The Tile object to be tested
	 * @param pos		The Point to test the Tile object
	 * @param rot		The rotation to use
	 * 
	 * @return		true if the Tile object will fit at the specified location and rotation.
	 */
	public static boolean isMappingPossibility(Hashtable mapGrid,Tile tile,Point pos,int rot) {
		// Setup the position
		tile.setMapPosition(pos);
		tile.setRotation(rot);
		
		// First test the join
		boolean joinError = false;
		for (int edge=0;edge<6;edge++) {
			Tile adjTile = (Tile)mapGrid.get(Tile.getAdjacentPosition(pos,edge));
			// Only need to test joins where there is a tile
			if (adjTile!=null) {
				if (tile.getPathState(edge)!=adjTile.getPathState((edge+3)%6)) {
					if (debug) {
						System.out.println(tile.name+" doesn't line up with "+adjTile.name);
						System.out.println(tile.name+" path state for "+edge+" is "+tile.getPathState(edge));
						System.out.println(adjTile.name+" path state for "+((edge+3)%6)+" is "+adjTile.getPathState((edge+3)%6));
					}
					return false; // if they don't line up, there is no need to continue here!!
				}
			}
		}
		
		boolean allConnect = true;
		boolean anyConnect = false;
		
		for (int i=0;i<6;i++) {
			if (tile.connectsToTilename(mapGrid,"clearing_"+(i+1),ANCHOR_TILENAME)) {
				anyConnect = true;
			}
			else {
				allConnect = false;
			}
		}
		
		// Now, if the tile has 6-clearings, check to be sure the paths
		// lead back to the borderland tile.
		if (tile.getClearingCount()==6) {
			// I think I only need to check clearings 2 and 6 (or something like that)
			if (!allConnect) {
				if (debug) System.out.println(tile.name+" doesn't have all 6 clearings connecting");
				joinError = true;
			}
		}
		else {
			if (!anyConnect) {
				if (debug) System.out.println(tile.name+" doesn't have any clearings connecting");
				joinError = true;
			}
		}
		
		// If the tile has no join errors, save the result
		// (no need to check if adjacent to two tiles here)
		return !joinError;
	}
	/**
	 * @return		A Collection of Point objects that reference possible map placements
	 */
	public static ArrayList findAvailableMapPositions(Hashtable mapGrid) {
		ArrayList availableMapPositions = new ArrayList();
		for (Iterator t=mapGrid.values().iterator();t.hasNext();) {
			Tile tile = (Tile)t.next();
			Point pos = tile.getMapPosition();
			
			// Cycle through all adjacent positions to the mapped tile
			for (int edge=0;edge<6;edge++) {
				Point adjPos = Tile.getAdjacentPosition(pos,edge);
				// only empty places
				if (mapGrid.get(adjPos)==null) {
					// only undiscovered places
					if (!availableMapPositions.contains(adjPos)) {
						// only joinable places
						if (tile.getPathState(edge)) {
							// Count adjacent tiles (joined or not)
							int adjCount = 0;
							for (int adj=0;adj<6;adj++) {
								Tile adjTile = (Tile)mapGrid.get(Tile.getAdjacentPosition(adjPos,adj));
								if (adjTile!=null) {
									adjCount++;
								}
							}
							// only places adjacent to two tiles (unless only one tile on map)
							if (mapGrid.size()==1 || adjCount>1) {
								availableMapPositions.add(adjPos);
							}
						}
					}
				}
			}
		}
		return availableMapPositions;
	}
}