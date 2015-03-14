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
package com.robin.magic_realm.components.quest;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.robin.game.objects.*;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.general.swing.ImageCache;
import com.robin.general.util.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestLocation extends GameObjectWrapper {
	private static String TAG_FRONT = "<";
	private static String TAG_END = ">";
	
	private static final String TYPE = "_t";
	private static final String LOCK_ADDRESS = "_la";
	private static final String CHOICE_ADDRESSES = "_ca";
	
	private static final String SAME_TILE = "_st";
	private static final String LOC_CLEARING_TYPE = "_ct";
	private static final String LOC_TILE_SIDE_TYPE = "_tst";
	
	public QuestLocation(GameObject go) {
		super(go);
	}
	public String getDescription() {
		ArrayList list = getChoiceAddresses();
		String locList = list == null ? "" : StringUtilities.collectionToString(list, ",");
		LocationType type = getLocationType();
		LocationClearingType lc = getLocationClearingType();
		LocationTileSideType ts = getLocationTileSideType();
		
		StringBuilder sb = new StringBuilder();
		sb.append(type.getDescriptionPrefix());
		sb.append(" ");
		if (ts != LocationTileSideType.Any) {
			sb.append(ts.toString().toLowerCase());
			sb.append(" ");
		}
		if (lc != LocationClearingType.Any) {
			sb.append(lc.toString().toLowerCase());
			sb.append(" ");
		}
		sb.append("clearing");
		if (locList.trim().length()>0) {
			if (isSameTile()) {
				sb.append(" in the same tile as ");
			}
			else {
				sb.append(" containing ");
			}
			sb.append(locList);
		}
		sb.append(".");
		return sb.toString();
	}
	public Quest getParentQuest() {
		GameObject quest = getGameObject().getHeldBy();
		return new Quest(quest);
	}
	private String getQuestName() {
		GameObject quest = getGameObject().getHeldBy();
		return quest.getName();
	}
	private ArrayList<String> getValidAddresses() {
		ArrayList<String> addresses = new ArrayList<String>();
		String lock = getLockAddress();
		if (lock!=null) {
			addresses.add(lock);
		}
		else {
			ArrayList<String> choices = getChoiceAddresses();
			if (choices!=null) {
				addresses.addAll(choices); // the only time this should happen, is with "Any" or "Lock"
			}
		}
		return addresses;
	}
	public RealmComponent[] allPiecesForLocation(JFrame frame,CharacterWrapper character) {
		if (needsResolution()) {
			if (getLocationType()==LocationType.Lock) {
				RealmLogging.logMessage(QuestConstants.QUEST_ERROR,"Can't fetch chits for a LOCK type of location without requiring the character to first visit that location.");
				return null;
			}
			resolveStepStart(frame,character);
		}
		
		ArrayList<RealmComponent> allPieces = new ArrayList<RealmComponent>();
		ArrayList<String> addresses = getValidAddresses();
		for(String address:addresses) {
			ArrayList<RealmComponent> pieces = fetchPieces(getGameData(),address,false);
			if (pieces!=null) {
				allPieces.addAll(pieces);
			}
			else {
				TileLocation tl = fetchTileLocation(getGameData(),address);
				for(RealmComponent rc:tl.clearing.getClearingComponents()) {
					if (rc.isChit()) {
						allPieces.add((ChitComponent)rc);
					}
				}
			}
		}
		return allPieces.toArray(new RealmComponent[0]);
	}
	/**
	 * This will test to see if the character is at a location supported by this object.  If the location type is "Lock" then any success will also lock the address.
	 * 
	 * Note that if specificObject is used, then ONLY that component is considered (like when doing a search)
	 * 
	 * @return		true if the character is at "this" location
	 */
	public boolean locationMatchAddress(JFrame frame,CharacterWrapper character) {
		return locationMatchAddress(frame,character,null);
	}
	public boolean locationMatchAddress(JFrame frame,CharacterWrapper character,GameObject specificObject) {
		LocationType type = getLocationType();
		if (type!=LocationType.Lock && needsResolution()) { 
			// If this is not a "Lock" type, and still needs resolution (random or choice), then resolve that first.
			resolveStepStart(frame,character);
		}
		
		ArrayList<String> addressesToTest = getValidAddresses();
		
		TileLocation current = character.getCurrentLocation();
		if (!current.isInClearing()) return false; // let's assume for now that you HAVE to be in a clearing to satisfy a location requirement
		
		LocationClearingType clearingType = getLocationClearingType();
		LocationTileSideType tileSideType = getLocationTileSideType();
		
		if (clearingType!=LocationClearingType.Any && !clearingType.matches(current.clearing)) return false;
		if (tileSideType!=LocationTileSideType.Any && !tileSideType.matches(current.tile)) return false;
		
		if (addressesToTest.isEmpty()) return true; // If there are NO addresses, then anything is allowed
		
		ArrayList<RealmComponent> clearingComponents;
		if (specificObject!=null) {
			clearingComponents= new ArrayList<RealmComponent>();
			clearingComponents.add(RealmComponent.getRealmComponent(specificObject));
		}
		else {
			if (isSameTile()) {
				clearingComponents = current.tile.getAllClearingComponents(); 
			}
			else {
				clearingComponents = current.tile.getOffroadRealmComponents(); // state chits without a clearing
				clearingComponents.addAll(current.clearing.getClearingComponents());
			}
			for(GameObject go:character.getInventory()) {
				clearingComponents.add(RealmComponent.getRealmComponent(go));
			}
		}
		String matchingAddress = null;
		for(String address:addressesToTest) {
			TileLocation tl = fetchTileLocation(getGameData(),address);
			if (tl!=null) {
				if ((isSameTile() && tl.tile.equals(current.tile))
					|| tl.equals(current)) {
						matchingAddress = address;
						break;
				}
			}
			ArrayList<RealmComponent> pieces = fetchPieces(getGameData(),address,true);
			if (CollectionUtility.containsAny(clearingComponents,pieces)) {
				matchingAddress = address;
				break;
			}
		}
		if (matchingAddress!=null) {
			if (type==LocationType.Lock && getLockAddress()==null && addressesToTest.size()>1) {
				// Lock address down
				setLockAddress(matchingAddress);
				String message = getTagName()+" is at the "+matchingAddress;
				character.addNote(getGameObject(),getQuestName(),message);
			}
			return true;
		}
		
		return false;
	}
	public void resolveQuestStart(JFrame frame,CharacterWrapper character) {
		ArrayList choices = getChoiceAddresses();
		if (choices==null || choices.size()==0) return;
		if (choices.size()==1) {
			// This is easy
			setLockAddress((String)choices.get(0));
			return;
		}
		
		// More than one choice...
		LocationType type = getLocationType();
		if (type==LocationType.QuestRandom) {
			int r = RandomNumber.getRandom(choices.size());
			setLockAddress((String)choices.get(r));
			String message = getTagName()+" is at the "+getLockAddress().toUpperCase();
			character.addNote(getGameObject(),getQuestName(),message);
			Quest.showQuestMessage(frame,getParentQuest(),message,getGameObject().getHeldBy().getName());
			return;
		}
		if (type==LocationType.QuestChoice) {
			// Allow the player to pick from the list
			forcePlayerPick(frame,choices);
			String message = getTagName()+" is at the "+getLockAddress().toUpperCase();
			character.addNote(getGameObject(),getQuestName(),message);
		}
		// All others are ignored at this point
	}
	public void resolveStepStart(JFrame frame,CharacterWrapper character) {
		if (!needsResolution()) return;
		ArrayList<String> choices = getChoiceAddresses();
		LocationType type = getLocationType();
		if (type==LocationType.StepRandom) {
			int r = RandomNumber.getRandom(choices.size());
			setLockAddress((String)choices.get(r));
			String message = getTagName()+" is at the "+getLockAddress().toUpperCase();
			character.addNote(getGameObject(),getQuestName(),message);
			Quest.showQuestMessage(frame,getParentQuest(),message,getGameObject().getHeldBy().getName());
			return;
		}
		if (type==LocationType.StepChoice) {
			// Allow the player to pick from the list
			forcePlayerPick(frame,choices);
			String message = getTagName()+" is at the "+getLockAddress().toUpperCase();
			character.addNote(getGameObject(),getQuestName(),message);
		}
	}
	private void forcePlayerPick(JFrame frame,ArrayList<String> choices) {
		// assume that the location type was already verified (needs to be QuestChoice or StepChoice) ...
		GameObject quest = getGameObject().getHeldBy();
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,ImageCache.getIcon("quests/token"),"Which location would you like to choose for "+getTagName()+"?",quest.getName(),false);
		for(String choice:choices) {
			chooser.addSelectionObject(choice);
		}
		chooser.setVisible(true);
		String selected = chooser.getSelectedObject().toString();
		setLockAddress(selected);
	}
	public boolean needsResolution() {
		return getLockAddress()==null && getLocationType()!=LocationType.Any;
	}
	public String toString() {
		return getName();
	}
	public void init() {
		getGameObject().setThisAttribute(Quest.QUEST_LOCATION);
	}
	public String getBlockName() {
		return Quest.QUEST_BLOCK;
	}
	
	public String getTagName() {
		return TAG_FRONT + getName() + TAG_END;
	}
	
	public void setLocationType(LocationType type) {
		setString(TYPE,type.toString());
	}
	
	public LocationType getLocationType() {
		String val = getString(TYPE);
		return LocationType.valueOf(val);
	}
	
	public void setLockAddress(String val) {
		setString(LOCK_ADDRESS,val);
	}
	
	public String getLockAddress() {
		return getString(LOCK_ADDRESS);
	}
	
	public void addChoiceAddresses(String val) {
		addListItem(CHOICE_ADDRESSES,val);
	}
	
	public void clearChoiceAddresses() {
		clear(CHOICE_ADDRESSES);
	}
	
	public ArrayList<String> getChoiceAddresses() {
		return getList(CHOICE_ADDRESSES);
	}
	
	public void setSameTile(boolean val) {
		setBoolean(SAME_TILE,val);
	}
	
	public boolean isSameTile() {
		return getBoolean(SAME_TILE);
	}
	
	public void setLocationClearingType(LocationClearingType lt) {
		setString(LOC_CLEARING_TYPE,lt.toString());
	}
	
	public LocationClearingType getLocationClearingType() {
		String val = getString(LOC_CLEARING_TYPE);
		return val==null?LocationClearingType.Any:LocationClearingType.valueOf(val);
	}
	
	public void setLocationTileSideType(LocationTileSideType le) {
		setString(LOC_TILE_SIDE_TYPE,le.toString());
	}
	
	public LocationTileSideType getLocationTileSideType() {
		String val = getString(LOC_TILE_SIDE_TYPE);
		return val==null?LocationTileSideType.Any:LocationTileSideType.valueOf(val);
	}
	
	public static TileLocation fetchTileLocation(GameData gameData,String val) {
		// Tile coordinate (like AV2)
		try {
			return TileLocation.parseTileLocationNoPartway(gameData,val.toUpperCase());
		}
		catch(Exception ex) {
			// ignore exception - this just means its NOT a tile coordinate
		}
		
		// Tile name and clearing (like Awful Valley 2) - what about tile name alone?
		Pattern pattern = Pattern.compile("([a-zA-Z\\s]+)(\\d*)");
		Matcher match = pattern.matcher(val);
		if (match.matches()) {
			String tileName = match.group(1).trim();
			String clearingNumString = match.group(2).trim();
			int clearingNum = clearingNumString.length()>0 ? Integer.valueOf(clearingNumString) : -1;
			GameObject go = gameData.getGameObjectByNameIgnoreCase(tileName);
			if (go!=null) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc.isTile()) {
					TileComponent tile = (TileComponent)rc;
					ClearingDetail clearing = clearingNum>0?tile.getClearing(clearingNum):null;
					return new TileLocation(tile,clearing,false);
				}
			}
		}
		return null;
	}

	public static ArrayList<RealmComponent> fetchPieces(GameData gameData, String val,boolean onlySeen) {
		ArrayList<GameObject> gos = gameData.getGameObjectsByNameIgnoreCase(val);
		if (gos.isEmpty()) return null;
		ArrayList<RealmComponent> ret = new ArrayList<RealmComponent>();
		for (GameObject go : gos) {
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc==null) continue;
			if (rc.isStateChit() || rc.isDwelling() || rc.isMonster() || rc.isNative() || rc.isItem() || rc.isGoldSpecial()) {
				if (!onlySeen || !rc.isStateChit() || rc.getGameObject().hasThisAttribute("seen")) {
					ret.add(rc);
				}
			}
		}
		return ret.isEmpty() ? null : ret;
	}
	public static boolean validLocation(GameData gameData,String val) {
		return fetchTileLocation(gameData,val)!=null || fetchPieces(gameData,val,false)!=null;
	}
}