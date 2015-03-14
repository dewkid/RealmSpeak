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

import com.robin.game.objects.GameObject;
import com.robin.general.util.StringBufferedList;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.RealmComponentDisplayDialog;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class MagicSight extends Search {
	public MagicSight(JFrame frame) {
		super(frame);
	}
	public String getTableName(boolean longDescription) {
		return "Magic Sight";
	}
	public String getTableKey() {
		return "MagicSight";
	}
//public String apply(CharacterWrapper character,DieRoller roller) {
//System.out.println("MagicSight:  REMOVE THIS CODE");
//this.roller = roller;
//return applyThree(character);
//}
	
	public String applyOne(CharacterWrapper character) {
		// Choice
		return doChoice(character);
	}

	public String applyTwo(CharacterWrapper character) {
		// Counters
		
		boolean foundEnemies = false;
		// 1)	Find hidden enemies, but only those that have weapon, armor, or horse counters
		for (Iterator i=character.getCurrentLocation().clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.isCharacter() || rc.isNative()) {
				for (Iterator n=rc.getGameObject().getHold().iterator();n.hasNext();) {
					RealmComponent item = RealmComponent.getRealmComponent((GameObject)n.next());
					if (item.isWeapon() || item.isArmor() || item.isHorse()) {
						character.addFoundHiddenEnemy(rc.getGameObject());
						foundEnemies = true;
						break; // "enemy" that meets condition was found, so stop searching hold
					}
				}
			}
		}
		
		// 2)	Take topmost "counter" (weapon, armor, horse) from any discovered Site in your clearing or cache of belongings
		ArrayList clearingLoot = new ArrayList();
		ArrayList components = new ArrayList();
		for (Iterator i=character.getCurrentLocation().clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.getGameObject().hasThisAttribute("treasure_location")) {
				if (!rc.getGameObject().hasThisAttribute("discovery") ||
						character.hasTreasureLocationDiscovery(rc.getGameObject().getName())) {
					// can't loot sites that still need to be opened (crypt, vault)
					if (!rc.getGameObject().hasThisAttribute(Constants.NEEDS_OPEN)) {
						// Treasure location possibility, but does it have any counters to loot?
						if (getTreasureCounterCount(rc.getGameObject())>0) {
							// Does NOT include SITE cards!
							if (!rc.getGameObject().hasAttributeBlock("table")) {
								components.add(rc);
							}
						}
					}
				}
			}
			else if (rc.isWeapon() || rc.isArmor() || rc.isHorse()) {
				clearingLoot.add(rc);
			}
		}
		GameObject topmostCounter = null;
		String lootResult = "Nothing";
		if (components.size()>0 || clearingLoot.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getParentFrame(),"Magic Sight: Counters",false);
			if (components.size()>0) {
				chooser.addRealmComponents(components,false);
			}
			if (clearingLoot.size()>0) {
				chooser.addOption("clearingLoot","Clearing ("+clearingLoot.size()+" counter"+(clearingLoot.size()==1?"":"s")+")");
			}
			chooser.setVisible(true);
			String optionKey = chooser.getSelectedOptionKey();
			if (optionKey!=null) { // It better be, without a cancel button!!
				Loot loot;
				if ("clearingLoot".equals(optionKey)) {
					RealmComponent rc = (RealmComponent)clearingLoot.iterator().next();
					topmostCounter = rc.getGameObject();
					loot = (Loot)RealmTable.loot(getParentFrame(),character,character.getCurrentLocation(),getListener());
				}
				else {
					RealmComponent rc = chooser.getFirstSelectedComponent();
					topmostCounter = (GameObject)getTreasureCounters(rc.getGameObject()).iterator().next();
					loot = (Loot)RealmTable.loot(getParentFrame(),character,rc.getGameObject(),getListener());
				}
				if (loot.fulfilledPrerequisite(getParentFrame(),character)) {
					lootResult = loot.characterFindsItem(character,character.getCurrentLocation().clearing,topmostCounter);
				}
				else {
					// FIXME How to handle magic sight looting where prerequisites are NOT met?
				}
			}
		}
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.Counters;
		qp.objectList.add(topmostCounter);
		qp.searchHadAnEffect = topmostCounter!=null || foundEnemies;
		character.testQuestRequirements(getParentFrame(),qp);
		
		return "Counters - "+lootResult;
	}

	public String applyThree(CharacterWrapper character) {
		// Treasure Cards
		
		boolean foundEnemies = false;
		// 1)	Find hidden enemies, but only those that have treasure cards
		for (Iterator i=character.getCurrentLocation().clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.isCharacter() || rc.isNative()) {
				for (Iterator n=rc.getGameObject().getHold().iterator();n.hasNext();) {
					RealmComponent item = RealmComponent.getRealmComponent((GameObject)n.next());
					if (item.isTreasure()) {
						character.addFoundHiddenEnemy(rc.getGameObject());
						foundEnemies = true;
						break; // "enemy" that meets condition was found, so stop searching hold
					}
				}
			}
		}
		
		// 2)	Take topmost "treasure card" from any discovered Site in your clearing or cache of belongings
		ArrayList clearingLoot = new ArrayList();
		ArrayList components = new ArrayList();
		for (Iterator i=character.getCurrentLocation().clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			boolean added = false;
			if (rc.getGameObject().hasThisAttribute("treasure_location")) {
				if (!rc.getGameObject().hasThisAttribute("discovery") ||
						character.hasTreasureLocationDiscovery(rc.getGameObject().getName())) {
					// can't loot sites that still need to be opened (crypt, vault)
					if (!rc.getGameObject().hasThisAttribute(Constants.NEEDS_OPEN)) {
						// Treasure location possibility, but does it have any treasure cards to loot?
						if (TreasureUtility.getTreasureCardCount(rc.getGameObject())>0) {
							// Does NOT include SITE cards!
							if (!rc.getGameObject().hasAttributeBlock("table")) {
								components.add(rc);
								added = true;
							}
						}
					}
				}
			}
			if (!added && rc.isTreasure()) {
				if (!rc.isPlainSight() && !rc.getGameObject().hasThisAttribute(Constants.CANNOT_MOVE)) {
					clearingLoot.add(rc);
				}
			}
		}
		GameObject topmostTreasureCard = null;
		String lootResult = "Nothing";
		if (components.size()>0 || clearingLoot.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getParentFrame(),"Magic Sight: Treasure Cards",false);
			if (components.size()>0) {
				chooser.addRealmComponents(components,false);
			}
			if (clearingLoot.size()>0) {
				chooser.addOption("clearingLoot","Clearing ("+clearingLoot.size()+" card"+(clearingLoot.size()==1?"":"s")+")");
			}
			chooser.setVisible(true);
			String optionKey = chooser.getSelectedOptionKey();
			if (optionKey!=null) { // It better, without a cancel button!!
				//GameObject topmostTreasureCard;
				Loot loot;
				if ("clearingLoot".equals(optionKey)) {
					RealmComponent rc = (RealmComponent)clearingLoot.iterator().next();
					topmostTreasureCard = rc.getGameObject();
					loot = (Loot)RealmTable.loot(getParentFrame(),character,character.getCurrentLocation(),getListener());
				}
				else {
					RealmComponent rc = chooser.getFirstSelectedComponent();
					topmostTreasureCard = (GameObject)TreasureUtility.getTreasureCards(rc.getGameObject()).iterator().next();
					loot = (Loot)RealmTable.loot(getParentFrame(),character,rc.getGameObject(),getListener());
				}
				if (loot.fulfilledPrerequisite(getParentFrame(),character)) {
					lootResult = loot.characterFindsItem(character,character.getCurrentLocation().clearing,topmostTreasureCard);
					RealmTable newTable = loot.getNewTable();
					while(newTable!=null) {
						newTable.apply(character, DieRollBuilder.getDieRollBuilder(getParentFrame(),character).createRoller(newTable));
						newTable = newTable.getNewTable();
					}
				}
				else {
					// FIXME How to handle magic sight looting where prerequisites are NOT met?
				}
			}
		}
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.TreasureCards;
		qp.objectList.add(topmostTreasureCard);
		qp.searchHadAnEffect = topmostTreasureCard!=null || foundEnemies;
		character.testQuestRequirements(getParentFrame(),qp);
		
		return "Treasure Cards - "+lootResult;
	}

	public String applyFour(CharacterWrapper character) {
		// Perceive Spell
		
		boolean foundEnemies = false;
		// 1)	Find hidden enemies, but only those that have recorded spells
		for (Iterator i=character.getCurrentLocation().clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.isCharacter()) { // only characters will have recorded spells
				CharacterWrapper enemyChar = new CharacterWrapper(rc.getGameObject());
				if (enemyChar.hasSpells()) {
					character.addFoundHiddenEnemy(rc.getGameObject());
					foundEnemies = true;
				}
			}
		}
		
		// 2)	Look at any one activated artifact/spellbook, or discovered site's spells, and learn any one you want
		ArrayList components = new ArrayList();
		for (Iterator i=character.getCurrentLocation().clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.getGameObject().hasThisAttribute("treasure_location")) {
				if (!rc.getGameObject().hasThisAttribute("discovery") ||
						character.hasTreasureLocationDiscovery(rc.getGameObject().getName())) {
					// Site possibility, but does it have any spells?
					Collection c = SpellUtility.getSpells(rc.getGameObject(),null,true,false);
					if (c.size()>0) {
						// Don't worry if there are no spells to learn here
						components.add(rc);
					}
				}
			}
		}
		// check player inventory
		for (Iterator i=character.getInventory().iterator();i.hasNext();) {
			GameObject item = (GameObject)i.next();
			if (item.hasThisAttribute(Constants.ACTIVATED) && SpellUtility.getSpellCount(item,null,true)>0) {
				components.add(RealmComponent.getRealmComponent(item));
			}
		}
		GameObject perceivedSpell = null;
		String ret = "Perceive Spell - Nothing";
		if (components.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getParentFrame(),"Magic Sight: Perceive Spell",false);
			chooser.addRealmComponents(components,false,RealmComponentOptionChooser.DisplayOption.Darkside); // always face up
			chooser.setVisible(true);
			
			// Since there is no cancel button, the user MUST have selected a site
			RealmComponent site = chooser.getFirstSelectedComponent();
			ArrayList<GameObject> spells = SpellUtility.getSpells(site.getGameObject(),null,true,false);
			
			// Show ALL spells to player (43.6/5)
			RealmComponentDisplayDialog dialog = new RealmComponentDisplayDialog(getParentFrame(),
					"Perceive Spell","The "+site.getGameObject().getName()+" holds the following spells:");
			dialog.addGameObjects(spells);
			dialog.setVisible(true);
			
			// Add a character note
			StringBufferedList note = new StringBufferedList();
			for (GameObject spell:spells) {
				note.append(spell.getName());
			}
			character.addNote(site.getGameObject(),"Perceive Spell",note.toString());
			
			// Only offer learnable spells!
			ArrayList learnableSpells = new ArrayList();
			for (Iterator i=spells.iterator();i.hasNext();) {
				GameObject spell = (GameObject)i.next();
				if (character.canLearn(spell)) {
					learnableSpells.add(spell);
				}
			}
			
			if (learnableSpells.size()>0) {
				// Choose a spell to learn
				RealmComponentOptionChooser spellChooser = new RealmComponentOptionChooser(getParentFrame(),"Select a Spell to LEARN",false);
				spellChooser.addGameObjects(learnableSpells,false);
				spellChooser.setVisible(true);
				
				// Again, no cancel button, so a spell was selected
				RealmComponent spell = spellChooser.getFirstSelectedComponent();
				perceivedSpell = spell.getGameObject();
				character.recordNewSpell(getParentFrame(),spell.getGameObject());
				ret = "Perceive Spell - Learned "+spell.getGameObject().getName();
			}
		}
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.PerceiveSpell;
		qp.objectList.add(perceivedSpell);
		qp.searchHadAnEffect = perceivedSpell!=null || foundEnemies;
		character.testQuestRequirements(getParentFrame(),qp);
		
		return ret;
	}

	public String applyFive(CharacterWrapper character) {
		// Discover Chits
		return doDiscoverChits(character);
	}

	public String applySix(CharacterWrapper character) {
		// Nothing
		return "Nothing";
	}

	public static Collection getTreasureCounters(GameObject treasureLocation) {
		ArrayList list = new ArrayList();
		for (Iterator i=treasureLocation.getHold().iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(obj);
			if (rc.isWeapon() || rc.isArmor() || rc.isHorse()) {
				list.add(obj);
			}
		}
		return list;
	}

	public static int getTreasureCounterCount(GameObject treasureLocation) {
		return getTreasureCounters(treasureLocation).size();
	}
	
	@Override
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		for(RealmComponent rc:getAllDiscoverableChits(character,false)) {
			list.add(getIconForSearch(rc));
		}
		return list;
	}
}