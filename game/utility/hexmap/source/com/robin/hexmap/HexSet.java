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
package com.robin.hexmap;

public abstract class HexSet {
	protected HexMapPoint center;
	protected Rotation rotation;
	
	public boolean overlaps(HexMap map) {
		return overlaps(map,center);
	}
	public boolean overlaps(HexMap map,HexMapPoint test) {
		Placement[] array = getPlacementArray();
		for (int i=0;i<array.length;i++) {
			HexMapPoint pos = test.getPositionFromPlacement(array[i]);
			if (!map.validHex(pos) || map.getHex(pos)!=null) {
				return true;
			}
		}
		return false;
	}
	public boolean adjacentToAnother(HexMap map,HexMapPoint test) {
		// must check overlaps() first, for this to return accurate results!
		Placement[] array = getPlacementArray();
		for (int i=0;i<array.length;i++) {
			if (array[i].isBorderHex()) {
				HexMapPoint pos = test.getPositionFromPlacement(array[i]);
				HexMapPoint[] check = pos.getAdjacentPoints();
				for (int n=0;n<check.length;n++) {
					Hex hex = map.getHex(check[n]);
					if (map.isGameHex(hex)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	public void setCenter(HexMapPoint p) {
		center = p;
	}
	public HexMapPoint getCenter() {
		return center;
	}
	public void setRotation(Rotation r) {
		rotation = r;
	}
	public Rotation getRotation() {
		return rotation;
	}
	public abstract boolean contains(HexMapPoint p);
	public abstract void selectMap(HexMap map);
	public abstract void loadMap(HexMap map);
	public abstract void unloadMap(HexMap map);
	public abstract Placement[] getPlacementArray();
}