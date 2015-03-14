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
package com.robin.magic_realm.components.table;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.*;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

/**
 * This version of loot takes it's roll result from the game object itself.  Valid roll results are:
 * 
 * 		horse
 * 		armor
 * 		weapon
 * 		treasure
 * 		gold
 * 		curse
 * 		wish
 * 		heal
 *		teleport_cave
 *		peer_any
 *		power_pit
 * 		nothing
 * 
 * This class should be used if the treasureLocation object has a "table" attributeBlock.  This will cover
 * Crypt of the Knight, Enchanted Meadow, and Toadstool Circle
 */
public class TableLoot extends Loot {
	
	private CharacterWrapper transportVictim;
	
	public TableLoot(JFrame frame,GameObject treasureLocation,ChangeListener listener) {
		super(frame,null,treasureLocation,listener);
	}
	
	public String getTableName(boolean longDescription) {
		return "Loot the " + treasureLocation.getName()
				+ (longDescription?("\n(" + treasureLocation.getHoldCount() + " left)"):"");
	}
//	public String getTableKey() { // This would be nice, but is a problem for anything that aids "LOOT" in general.
//		String tlKey = treasureLocation.getThisAttribute("treasure_location");
//		return tlKey==null?super.getTableKey():tlKey;
//	}
//public String apply(CharacterWrapper character,DieRoller roller) {
//	System.err.println("DEBUG _ REMOVE THIS LINE TableLoot.apply");
//	return applyThree(character);
//}
	public String applyOne(CharacterWrapper character) {
		return applyFromTable(character,"roll_1");
	}

	public String applyTwo(CharacterWrapper character) {
		return applyFromTable(character,"roll_2");
	}

	public String applyThree(CharacterWrapper character) {
		return applyFromTable(character,"roll_3");
	}

	public String applyFour(CharacterWrapper character) {
		return applyFromTable(character,"roll_4");
	}

	public String applyFive(CharacterWrapper character) {
		return applyFromTable(character,"roll_5");
	}

	public String applySix(CharacterWrapper character) {
		return applyFromTable(character,"roll_6");
	}
	
	static final String NOTHING = "Nothing";
	protected String applyFromTable(CharacterWrapper character,String attribute) {
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = treasureLocation.getName().replaceAll(" ","");
		qp.actionType = CharacterActionType.SearchTable;
		
		String ret = NOTHING;
		
		GameData data = character.getGameObject().getGameData();
		String result = treasureLocation.getAttribute("table",attribute);
		if ("horse".equals(result)) {
			ret = takeHorse(character);
			qp.searchType = SearchResultType.Counters;
			qp.searchHadAnEffect = NOTHING.equals(ret);
		}
		else if ("armor".equals(result)) {
			ret = takeArmor(character);
			qp.searchType = SearchResultType.Counters;
			qp.searchHadAnEffect = NOTHING.equals(ret);
		}
		else if ("weapon".equals(result)) {
			ret = takeWeapon(character);
			qp.searchType = SearchResultType.Counters;
			qp.searchHadAnEffect = NOTHING.equals(ret);
		}
		else if ("treasure".equals(result)) {
			ret = takeTreasure(character);
			qp.searchType = SearchResultType.TreasureCards;
			qp.searchHadAnEffect = NOTHING.equals(ret);
		}
		else if ("gold".equals(result)) {
			character.addGold(1);
			ret = "Gained 1 gold.";
			qp.searchType = SearchResultType.Gold;
			qp.searchHadAnEffect = true;
		}
		else if ("curse".equals(result)) {
			// Test code for curse result
			setNewTable(new Curse(getParentFrame()));
			ret = "Curse!";
			qp.searchType = SearchResultType.Curse;
			qp.searchHadAnEffect = true;
		}
		else if ("wish".equals(result)) {
			// Test code to do a wish
			setNewTable(new Wish(getParentFrame()));
			ret = "Wish";
			qp.searchType = SearchResultType.Wish;
			qp.searchHadAnEffect = true;
		}
		else if ("heal".equals(result)) {
			character.doHeal();
			ret = "Healed all fatigued and wounded chits.";
			qp.searchType = SearchResultType.Heal;
			qp.searchHadAnEffect = true;
		}
		else if ("teleport_cave".equals(result)) {
			/*
			 * Teleport to any cave on the map and continue turn from there as if nothing else had changed.  If
			 * already in a cave, you can stay there.
			 */
			doTransport(character);
			ret = "Teleport to ANY Cave";
			qp.searchType = SearchResultType.CaveTeleport;
			qp.searchHadAnEffect = true;
		}
		else if ("peer_any".equals(result)) {
			/*
			 * For the remainder of the day, the character may PEER any clearing, including caves.  Also, can use
			 * SP phases in any clearing as if he/she were there.
			 */
			character.setPeerAny(true);
			ret = "PEER any clearing";
			qp.searchType = SearchResultType.PeerEnchantAnyClearing;
			qp.searchHadAnEffect = true;
		}
		else if ("power_pit".equals(result)) {
			// Fire off a power of the pit attack
			PowerOfThePit pop = new PowerOfThePit(getParentFrame(),treasureLocation);
			pop.setMakeDeadWhenKilled(true);
			setNewTable(pop); // Test PowerPit
			ret = "Power of the Pit";
			qp.searchType = SearchResultType.PowerOfThePit;
			qp.searchHadAnEffect = true;
		}
		else if ("heal".equals(result)) {
			SpellUtility.heal(character);
			ret = "Heal";
			qp.searchType = SearchResultType.Heal;
			qp.searchHadAnEffect = true;
		}
		else if (result.startsWith("rest_")){
			int rests = "rest_4".equals(result) ? 4 : 2;
			ChitRestManager rester = new ChitRestManager(getParentFrame(),character,rests);
			rester.setVisible(true);
			ret = "Rested "+rests+" asterisks";
			qp.searchType = SearchResultType.Rest;
			qp.searchHadAnEffect = true;
		}
		else if ("remove_curse".equals(result)) {
			qp.searchType = SearchResultType.RemoveCurse;
			if (character.hasCurses()) {
				ArrayList<String> curses = character.getAllCurses();
				int r = RandomNumber.getRandom(curses.size());
				String curseRemoved = curses.get(r);
				character.removeCurse(curseRemoved);
				ret = "Remove Curse - "+curseRemoved;
				qp.searchHadAnEffect = true;
			}
			else qp.searchHadAnEffect = false;
			return "Remove Curse (no effect)";
		}
		else if ("poison".equals(result)) {
			character.setExtraWounds(1);
			ret = "Poison - Take 1 Wound";
			qp.searchType = SearchResultType.Wound;
			qp.searchHadAnEffect = true;
		}
		else if ("collapse".equals(result)) {
			character.setExtraWounds(2);
			treasureLocation.getHeldBy().remove(treasureLocation);
			treasureLocation.removeThisAttribute("clearing"); // probably not necessary.
			// TODO destroy the site!
			ret = "Collapse - Take 2 Wounds and Site Destroyed";
			qp.searchType = SearchResultType.Wound;
			qp.searchHadAnEffect = true;
		}
		else if ("random_tl".equals(result)) {
			// Discover Random Treasure Location
			GamePool pool = new GamePool(data.getGameObjects());
			ArrayList<GameObject> tls = pool.find("treasure_location");
			int r = RandomNumber.getRandom(tls.size());
			GameObject go = tls.get(r);
			String tlName = go.getName();
			qp.searchType = SearchResultType.DiscoverChits;
			if (!character.hasTreasureLocationDiscovery(tlName)) {
				character.addTreasureLocationDiscovery(tlName);
				ret = "Discover "+tlName;
				qp.searchHadAnEffect = true;
			}
			else {
				ret = "Discover "+tlName+" - No Effect";
				qp.searchHadAnEffect = false;
			}
		}
		else if ("clues_chosen".equals(result)) {
			// Clues in a chosen tile
			ArrayList<GameObject> tiles = RealmObjectMaster.getRealmObjectMaster(data).getTileObjects();
			RealmComponentOptionChooser chooseSearch = new RealmComponentOptionChooser(getParentFrame(),"Clues for which tile:",false);
			Hashtable<String,GameObject> hash = new Hashtable<String,GameObject>();
			for(GameObject tile:tiles) {
				chooseSearch.addOption(chooseSearch.generateOption(),tile.getName());
				hash.put(tile.getName(),tile);
			}
			chooseSearch.setLocationRelativeTo(getParentFrame());
			chooseSearch.pack();
			chooseSearch.setVisible(true);
			
			String selected = (String)chooseSearch.getSelectedText();
			GameObject tile = hash.get(selected);
			String title = "Clues in a Chosen Tile - "+selected;
			doClues(title,character,tile);
			ret = title;
			qp.searchType = SearchResultType.Clues;
			qp.searchHadAnEffect = true;
		}
		else if ("clues_random".equals(result)) {
			// Clues in a random tile (use doClues)
			ArrayList<GameObject> tiles = RealmObjectMaster.getRealmObjectMaster(data).getTileObjects();
			int r = RandomNumber.getRandom(tiles.size());
			GameObject tile = tiles.get(r);
			String title = "Clues in a Random Tile - "+tile.getName();
			doClues(title,character,tile);
			ret = title;
			qp.searchType = SearchResultType.Clues;
			qp.searchHadAnEffect = true;
		}
		character.testQuestRequirements(getParentFrame(),qp);
		return ret;
	}
	private void doClues(String title,CharacterWrapper character,GameObject go) {
		TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
		ClearingDetail clearing = tile.getClearings().get(0); // first real clearing is good enough
		String note = ClearingUtility.showTileChits(getParentFrame(),character,clearing,"Clues at "+go.getName());
		if (note!=null) {
			character.addNote(clearing.getParent(),"Clues",note);
		}
	}
	private void doTransport(CharacterWrapper character) {
		// Get the map to pop to the forefront, centered on the clearing, and the move possibilities marked
		transportVictim = character;
		TileLocation planned = character.getPlannedLocation();
		CenteredMapView.getSingleton().setMarkClearingAlertText("Transport to which cave clearing?");
		CenteredMapView.getSingleton().markClearings("caves",true);
		TileLocationChooser chooser = new TileLocationChooser(getParentFrame(),CenteredMapView.getSingleton(),planned);
		chooser.setVisible(true);
		TileLocation tl = chooser.getSelectedLocation();
		transportVictim.jumpMoveHistory(); // because the victim didn't walk there
		transportVictim.moveToLocation(null,tl);
		CenteredMapView.getSingleton().markClearings("caves",false);
		CenteredMapView.getSingleton().centerOn(tl);
		
		// Followers should stay behind!
		for (CharacterWrapper follower:transportVictim.getActionFollowers()) {
			transportVictim.removeActionFollower(follower,null);
		}
		for (RealmComponent hireling:transportVictim.getFollowingHirelings()) {
			ClearingUtility.moveToLocation(hireling.getGameObject(),planned);
		}
	}
}