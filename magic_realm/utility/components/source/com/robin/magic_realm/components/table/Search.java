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

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.PathIcon;
import com.robin.magic_realm.components.utility.ClearingUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public abstract class Search extends RealmTable {
	protected ClearingDetail targetClearing;
	
	public Search(JFrame frame) {
		this(frame,null);
	}
	public Search(JFrame frame,ClearingDetail clearing) {
		super(frame,null);
		targetClearing = clearing;
	}
	protected ClearingDetail getCurrentClearing(CharacterWrapper character) {
		return targetClearing==null?character.getCurrentLocation().clearing:targetClearing;
	}
	protected String doChoice(CharacterWrapper character) {
		String peer2String = "Clues and Paths";
		String peer3String = "Hidden Enemies and Paths";
		String locate2String = "Clues and Passages";
		String locate4String = "Discover Chits";
		
		ImageIcon paths = getIconFromList(convertPathDetailToImageIcon(getAllUndiscoveredPaths(character)));
		ImageIcon passages = getIconFromList(convertPathDetailToImageIcon(getAllUndiscoveredPassages(character)));
		ImageIcon chits = getIconFromList(convertRealmComponentToImageIcon(getAllDiscoverableChits(character,true)));
		
		ButtonOptionDialog chooseSearch = new ButtonOptionDialog(getParentFrame(), null, "Choice:", "", false);
		chooseSearch.addSelectionObject(peer2String);
		chooseSearch.setSelectionObjectIcon(peer2String,paths);
		chooseSearch.addSelectionObject(peer3String);
		chooseSearch.setSelectionObjectIcon(peer3String,paths);
		chooseSearch.addSelectionObject(locate2String);
		chooseSearch.setSelectionObjectIcon(locate2String,passages);
		chooseSearch.addSelectionObject(locate4String);
		chooseSearch.setSelectionObjectIcon(locate4String,chits);
		chooseSearch.setVisible(true);
		String choice = (String)chooseSearch.getSelectedObject();
		if (choice.equals(peer2String)) {
			return "Choice - "+RealmTable.peer(getParentFrame(),targetClearing).applyTwo(character);
		}
		else if (choice.equals(peer3String)) {
			return "Choice - "+RealmTable.peer(getParentFrame(),targetClearing).applyThree(character);
		}
		else if (choice.equals(locate2String)) {
			return "Choice - "+RealmTable.locate(getParentFrame(),targetClearing).applyTwo(character);
		}
		else if (choice.equals(locate4String)) {
			return "Choice - "+RealmTable.locate(getParentFrame(),targetClearing).applyFour(character);
		}
		return null;
	}
	protected void doClues(CharacterWrapper character) {
		ClearingDetail currentClearing = getCurrentClearing(character);
		String note = ClearingUtility.showTileChits(getParentFrame(),character,currentClearing,"Clues");
		if (note!=null) {
			character.addNote(currentClearing.getParent(),"Clues",note);
		}
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.Clues;
		qp.searchHadAnEffect = note!=null;
		character.testQuestRequirements(getParentFrame(),qp);
	}
	protected ArrayList<ImageIcon> convertPathDetailToImageIcon(ArrayList<PathDetail> paths) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		if (paths!=null) {
			for (PathDetail path:paths) {
				list.add(new PathIcon(path));
			}
		}
		return list;
	}
	protected ArrayList<ImageIcon> convertRealmComponentToImageIcon(ArrayList<RealmComponent> chits) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		if (chits!=null) {
			for (RealmComponent rc:chits) {
				list.add(getIconForSearch(rc));
			}
		}
		return list;
	}
	protected ArrayList<PathDetail> getAllUndiscoveredPaths(CharacterWrapper character) {
		ArrayList<PathDetail> list = new ArrayList<PathDetail>();
		ClearingDetail currentClearing = getCurrentClearing(character);
		ArrayList passages = currentClearing.getConnectedPaths();
		if (passages==null) return list;
		for (Iterator n=passages.iterator();n.hasNext();) {
			PathDetail path = (PathDetail)n.next();
			if (path.isHidden()) {
				if (!character.hasHiddenPathDiscovery(path.getFullPathKey())) {
					list.add(path);
				}
			}
		}
		return list;
	}
	protected ArrayList<PathDetail> getAllUndiscoveredPassages(CharacterWrapper character) {
		ArrayList<PathDetail> list = new ArrayList<PathDetail>();
		ClearingDetail currentClearing = getCurrentClearing(character);
		ArrayList passages = currentClearing.getConnectedPaths();
		if (passages==null) return list;
		for (Iterator n=passages.iterator();n.hasNext();) {
			PathDetail path = (PathDetail)n.next();
			if (path.isSecret()) {
				if (!character.hasSecretPassageDiscovery(path.getFullPathKey())) {
					list.add(path);
				}
			}
		}
		return list;
	}
	protected ArrayList<RealmComponent> getAllDiscoverableChits(CharacterWrapper character,boolean onlyUndiscovered) {
		ArrayList<RealmComponent> list = new ArrayList<RealmComponent>();
		ClearingDetail currentClearing = getCurrentClearing(character);
		for (RealmComponent rc:currentClearing.getClearingComponents()) {
			if (rc.getGameObject().hasThisAttribute("chit")
					&& rc.getGameObject().hasThisAttribute("seen")) { // Only "seen" chits should be added as hints
				if (rc.getGameObject().hasThisAttribute("discovery")) {
					if (!onlyUndiscovered || !character.hasTreasureLocationDiscovery(rc.getGameObject().getName())) {
						if (rc.getGameObject().getHoldCount()>0) { // no point in hinting when the treasure location is empty!
							list.add(rc);
						}
					}
				}
				else if (rc.getGameObject().hasThisAttribute("gold")) {
					list.add(rc);
				}
				else if (rc.getGameObject().hasThisAttribute("minor_tl")) {
					list.add(rc);
				}
				else if (rc.isGate() || rc.isGuild()) {
					if (!onlyUndiscovered || !character.hasOtherChitDiscovery(rc.getGameObject().getName())) {
						list.add(rc);
					}
				}
			}
		}
		return list;
	}
	protected String doDiscoverChits(CharacterWrapper character) {
		doClues(character);
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.DiscoverChits;
		// Discover treasure locations in current clearing
		String message = "Discover chit(s) - Found ";
		ClearingDetail currentClearing = getCurrentClearing(character);
		Collection allChits = currentClearing.getClearingComponents();
		int count=0;
		for (Iterator n=allChits.iterator();n.hasNext();) {
			RealmComponent rc = (RealmComponent)n.next();
			// only discover discovery chits
			if (rc.getGameObject().hasThisAttribute("chit")) {
				boolean foundSomething = discoverChit(getParentFrame(),character,currentClearing,rc,qp,getListener());
				if (foundSomething) {
					if (count>0) {
						message = message + ",";
					}
					message = message + rc.getGameObject().getName();
					count++;
				}
			}
		}
		if (count==0) {
			message = "Discover chit(s) - none to discover";
		}
		qp.searchHadAnEffect = count>0;
		character.testQuestRequirements(getParentFrame(),qp);
		return message;
	}
	public static boolean discoverChit(JFrame frame,CharacterWrapper character,ClearingDetail currentClearing,RealmComponent rc,QuestRequirementParams qp,ChangeListener listener) {
		String discoveryName = rc.getGameObject().getName();
		if (rc.getGameObject().hasThisAttribute("discovery")) {
			if (!character.hasTreasureLocationDiscovery(rc.getGameObject().getName())) {
				character.addTreasureLocationDiscovery(rc.getGameObject().getName());
				qp.objectList.add(rc.getGameObject());
				return true;
			}
		}
		else if (rc.getGameObject().hasThisAttribute("gold")) {
			int gold = rc.getGameObject().getThisInt("gold");
			character.addGold(gold);
			currentClearing.remove(rc.getGameObject());
			return true;
		}
		else if (rc.getGameObject().hasThisAttribute("minor_tl")) {
			GameObject thing = (GameObject)rc.getGameObject().getHold().get(0); // better be one thing there!
			if (thing.hasThisAttribute("spell")) {
				if (character.canLearn(thing)) {
					discoveryName = discoveryName + " (learned "+thing.getName()+")";
					character.recordNewSpell(frame,thing);
					currentClearing.remove(rc.getGameObject());
					qp.objectList.add(rc.getGameObject());
				}
				else {
					discoveryName = discoveryName + " (cannot learn spell)";
				}
				return true;
			}
			else {
				discoveryName = discoveryName + " ("+thing.getName()+")";
				Loot.addItemToCharacter(frame,listener,character,thing);
				currentClearing.remove(rc.getGameObject());
				qp.objectList.add(rc.getGameObject());
				return true;
			}
		}
		else if (rc.isGate() || rc.isGuild() || rc.isRedSpecial()) {
			if (!character.hasOtherChitDiscovery(rc.getGameObject().getName())) {
				character.addOtherChitDiscovery(rc.getGameObject().getName());
				qp.objectList.add(rc.getGameObject());
				return true;
			}
		}
		return false;
	}
}