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
package com.robin.magic_realm.components.attribute;

import java.util.ArrayList;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.ClearingDetail;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.TileComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmObjectMaster;

public class TileLocation {
	public TileComponent tile;
	public ClearingDetail clearing;
	
	private boolean flying;
	private TileLocation other;	// null if this location describes a precise clearing, otherwise "other" 
							// defines the other side of a between relationship
	public TileLocation(TileComponent t) {
		this(t,null,false);
	}
	public TileLocation(TileComponent t,boolean flying) {
		this(t,null,flying);
	}
	public TileLocation(ClearingDetail c) {
		this(c.getParent(),c,false);
	}
	public TileLocation(ClearingDetail c,boolean flying) {
		this(c.getParent(),c,flying);
	}
	public TileLocation(TileComponent t,ClearingDetail c,boolean flying) {
		tile = t;
		clearing = c;
		this.flying = flying;
	}
	public boolean contains(TileComponent t) {
		return tile.equals(t) || (other!=null && other.tile.equals(t));
	}
	public boolean contains(ClearingDetail c) {
		return (clearing!=null && clearing.equals(c)) || (other!=null && other.hasClearing() && other.clearing.equals(c));
	}
	public void setOther(TileLocation tl) {
		other = tl;
		other.other = null; // this prevents infinite loops
	}
	public TileLocation getOther() {
		return other;
	}
	public void setFlying(boolean val) {
		flying = val;
	}
	public boolean isFlying() {
		return flying;
	}
	public int hashCode() {
		int code = tile.getGameObject().hashCode();
		if (clearing!=null) {
			code += clearing.getNum();
		}
		if (other!=null && other.clearing!=null) {
			code += other.clearing.getNum();
		}
		return code;
	}
	public boolean equals(Object o) {
		if (o instanceof TileLocation) {
			TileLocation tl = (TileLocation)o;
			if (tile.getGameObject().equals(tl.tile.getGameObject())) {
				if (clearing==null?tl.clearing==null:clearing.equals(tl.clearing)) {
					if (other==null?tl.other==null:other.equals(tl.other)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * @return		true if there is a clearing component to this location
	 */
	public boolean hasClearing() {
		return clearing!=null;
	}
	public boolean isBetweenClearings() {
		return clearing!=null && other!=null && other.clearing!=null;
	}
	public boolean isTileOnly() {
		return clearing==null && other==null;
	}
	public boolean isBetweenTiles() {
		return clearing==null && other!=null && other.clearing==null;
	}
	/**
	 * @return		true if this location describes a precise clearing (not on a roadway or general tile!!)
	 */
	public boolean isInClearing() {
		return clearing!=null && other==null;
	}
	/**
	 * @return		true if this location is in a cave or in a dwelling
	 */
	public boolean isInside(boolean includeRedSpecial) {
		if (isBetweenClearings()) {
			return clearing.isCave() && other.clearing.isCave();
		}
		return isInClearing() && (clearing.isCave() || clearing.holdsDwelling() || (includeRedSpecial && clearing.holdsRedSpecial()));
	}
	public boolean isAtDwelling(boolean includeRedSpecial) {
		return isInClearing() && (clearing.holdsDwelling() || (includeRedSpecial && clearing.holdsRedSpecial()));
	}
	/**
	 * @return		true if this location is in a cave or in the mountains.  Between caves or between mountains is considered the same as being in a cave or mountain clearing.
	 */
	public boolean isShaded() {
		if (isBetweenClearings()) {
			return (clearing.isCave() && other.clearing.isCave()) || (clearing.isMountain() && other.clearing.isMountain());
		}
		return isInClearing() && (clearing.isCave() || clearing.isMountain());
	}
	public String toString() {
		if (isBetweenClearings()) {
			return "Between "+tile.getTileName()+" "+String.valueOf(clearing.getNum())+" and "+other.toString()+(flying?" (Flying)":"");
		}
		else if (isBetweenTiles()) {
			return "Between "+tile.getTileName()+" and "+other.toString()+(flying?" (Flying)":"");
		}
		return tile.getTileName()+" "+(clearing==null?"":String.valueOf(clearing.getNum()))+(flying?" (Flying)":"");
	}
	/**
	 * Returns the location as a parsable key.
	 */
	public String asKey() {
		if (hasClearing()) {
			if (isBetweenClearings()) {
				return "P:"+tile.getTileCode()+clearing.getNumString()+"&"+other.asKey();
			}
			return tile.getTileCode()+clearing.getNumString();
		}
		else {
			String letter = flying?"F":"W";
			if (isBetweenClearings()) {
				return letter+"P:"+tile.getTileCode()+"&"+other.tile.getTileCode();
			}
			return letter+":"+tile.getTileCode(); // flying or walking woods
		}
		
		/*
		 * Examples:  (showing every possibility)
		 * 
		 * 	P:BL6&DW1			Between Borderland 6 and DeepWoods 1
		 * 	BL6				Borderland 6
		 * 	FP:BL&DW			Flying between the Borderland and the DeepWoods
		 * 	F:BL				Flying over the Borderland
		 * 	WP:BL&DW			Walking between the Borderland and the DeepWoods
		 * 	W:BL				Walking woods in the Borderland
		 */
	}
	
	/**
	 * Converts a string designation (from asKey()) into a real tile location.
	 */
	public static TileLocation parseTileLocation(GameData data,String key) {
		TileLocation tl;
		if (key.startsWith("P:")) {
			// Between clearings
			String code = key.substring(2);
			int ampersand = code.indexOf("&");
			String from = code.substring(0,ampersand);
			String to = code.substring(ampersand+1);
			TileLocation fromTl = parseTileLocationNoPartway(data,from);
			TileLocation toTl = parseTileLocationNoPartway(data,to);
			tl = new TileLocation(fromTl.clearing);
			tl.setOther(toTl);
		}
		else if (key.startsWith("F:") || key.startsWith("W:")) {
			// Flying
			String code = key.substring(2);
			TileComponent theTile = getTile(data,code);
			tl = new TileLocation(theTile);
		}
		else if (key.startsWith("FP:") || key.startsWith("WP:")) {
			// Flying between tiles
			String code = key.substring(3);
			int ampersand = code.indexOf("&");
			String from = code.substring(0,ampersand);
			String to = code.substring(ampersand+1);
			tl = new TileLocation(getTile(data,from));
			tl.setOther(new TileLocation(getTile(data,to)));
		}
		else {
			tl = parseTileLocationNoPartway(data,key);
		}
		if (key.startsWith("F")) {
			tl.setFlying(true);
		}
		return tl;
	}
	/**
	 * Makes LOTS of assumptions
	 */
	public static TileLocation parseTileLocationNoPartway(GameData data,String clearingOnlyKey) {
		String code;
		String num;
		int dot = clearingOnlyKey.indexOf('.');
		if (dot<0) {
			if ("0123456789".indexOf(clearingOnlyKey.substring(clearingOnlyKey.length()-1))<0) {
				// No number, so this is a pure tile move
				code = clearingOnlyKey;
				num = null;
			}
			else {
				code = clearingOnlyKey.substring(0,clearingOnlyKey.length()-1);
				num = clearingOnlyKey.substring(clearingOnlyKey.length()-1);
			}
		}
		else {
			code = clearingOnlyKey.substring(0,dot);
			num = clearingOnlyKey.substring(dot);
		}
		TileComponent theTile = getTile(data,code);
		if (num!=null) {
			ClearingDetail clearing = theTile.getClearing(num);
			return new TileLocation(clearing);
		}
		return new TileLocation(theTile);
	}
	public static TileComponent getTile(GameData data,String tileCode) {
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(data).getTileObjects());
		int bracket = tileCode.indexOf("[");
		String boardNum = null;
		if (bracket>0) {
			int ob = tileCode.indexOf("]");
			boardNum = tileCode.substring(bracket+1,ob);
			tileCode = tileCode.substring(0,bracket);
		}
		StringBuffer query = new StringBuffer();
		query.append("code=");
		query.append(tileCode);
		if (boardNum!=null) {
			query.append(",");
			query.append(Constants.BOARD_NUMBER);
			query.append("=");
			query.append(boardNum);
		}
		else {
			query.append(",!");
			query.append(Constants.BOARD_NUMBER);
		}
		ArrayList<GameObject> tiles = pool.find(query.toString());
		if (tiles.isEmpty()) {
			throw new IllegalStateException("Why no tile found for code: "+tileCode+", or query="+query.toString());
		}
		GameObject tile = tiles.get(0);
		return (TileComponent)RealmComponent.getRealmComponent(tile);
	}
}