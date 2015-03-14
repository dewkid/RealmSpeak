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
import java.util.Iterator;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.IconGroup;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class ReadRunes extends RealmTable {
	/*
	 * Must choose topmost spell card on treasure setup, OR any awakened artifact/book spell.
	 * 
	 * Can only learn spells that the character has the appropriate chit for
	 * 
	 * Characters can learn a maximum of 14 spells - they can ditch one for another, however.
	 */
	private GameObject spellLocation;
	private GameObject topSpell;
	private GameObject targetSpell;
	
	public ReadRunes(JFrame frame,GameObject spellLocation) {
		super(frame,null);
		this.spellLocation = spellLocation;
		
		for (Iterator i=spellLocation.getHold().iterator();i.hasNext();) {
			GameObject spell = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(spell);
			if (rc.isSpell()) {
				if (!spell.hasThisAttribute(Constants.SPELL_AWAKENED)) { // select the first non-activated spell
					topSpell = spell;
					break;
				}
			}
		}
	}
	public String getTableName(boolean longDescription) {
		// Show awakened/not awakened
		int total = SpellUtility.getSpellCount(spellLocation,null,false);
		int awakened = SpellUtility.getSpellCount(spellLocation,Boolean.TRUE,false);
		String awake = "\n(Awakened "+awakened+"/"+total+")";
		if (spellLocation.hasThisAttribute("spell_site")) awake = ""; // Don't report awakened spells on spell sites!
		
		return "Read Runes on "+spellLocation.getName()+(longDescription?awake:"");
	}
	public String getTableKey() {
		return "ReadRunes";
	}
	public String apply(CharacterWrapper character,DieRoller roller) {
//	System.err.println("REMOVE THIS LINE!!!! ReadRunes");
//	roller.setValue(0,5);
//	roller.setValue(1,5);
		// Before rolling, you must select a target spell, which for artifacts/books includes AWAKENED spells
		targetSpell = selectFromAllAwakenedSpells(character);
		return super.apply(character,roller);
	}
	public String applyOne(CharacterWrapper character) {
		// Learn and Awaken
		return learnAndAwaken(character);
	}

	public String applyTwo(CharacterWrapper character) {
		// Learn and Awaken
		return learnAndAwaken(character);
	}

	public String applyThree(CharacterWrapper character) {
		// Learn and Awaken
		return learnAndAwaken(character);
	}

	public String applyFour(CharacterWrapper character) {
		// Awaken
		return awaken(character,"Awaken");
	}

	public String applyFive(CharacterWrapper character) {
		// Curse
		setNewTable(new Curse(getParentFrame()));
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.Curse;
		qp.searchHadAnEffect = true; // (ogh) (ogh?) (OGGGGHHHH!!)
		character.testQuestRequirements(getParentFrame(),qp);
		
		return "Curse!";
	}

	public String applySix(CharacterWrapper character) {
		// Nothing
		return "Nothing";
	}
	private String getSpellName() {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(targetSpell.getGameData());
		if (hostPrefs.hasPref(Constants.HOUSE1_NO_SECRETS)) {
			return targetSpell.getName();
		}
		return "##a Spell|"+targetSpell.getName()+"##";
	}
	private String learnAndAwaken(CharacterWrapper character) {
		if (targetSpell==null) {
			return "Learn and Awaken (nothing)";
		}
		/*
		 * In addition to awaken (see below), the character learns the spell if he/she has a chit (or artifact/book - optional rule) to accomodate.
		 */
		awaken(character,"Learn and Awaken");
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.LearnAndAwaken;
		qp.targetOfSearch = spellLocation;
		
		String ret = "(Learn and) Awaken "+getSpellName();
		if (character.canLearn(targetSpell)) {
			character.recordNewSpell(getParentFrame(),targetSpell);
			ret = "Learn and Awaken "+getSpellName()+" - learned!";
			qp.objectList.add(targetSpell);
			qp.searchHadAnEffect = true;
		}
		character.testQuestRequirements(getParentFrame(),qp);
		return ret;
	}
	private GameObject selectFromAllAwakenedSpells(CharacterWrapper character) {
		ArrayList list = new ArrayList();
		for (Iterator i=spellLocation.getHold().iterator();i.hasNext();) {
			GameObject spell = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(spell);
			if (rc.isSpell() && character.canLearn(spell) && rc.getGameObject().hasThisAttribute(Constants.SPELL_AWAKENED)) {
				list.add(spell);
			}
		}
		if (topSpell!=null) {
			list.add(topSpell);
		}
		if (list.size()>0) {
			if (list.size()==1) { // the choice is obvious
				return (GameObject)list.get(0);
			}
			
			// otherwise:
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getParentFrame(),"Choose a spell:",false);
			for (Iterator i=list.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (go.hasThisAttribute(Constants.SPELL_AWAKENED)) {
					chooser.addRealmComponent(rc);
				}
				else {
					String key = chooser.generateOption("Top Spell");
					chooser.addRealmComponentToOption(key, rc,RealmComponentOptionChooser.DisplayOption.Flipside);
				}
			}
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				return chooser.getFirstSelectedComponent().getGameObject();
			}
		}
		return null;
	}
	private String awaken(CharacterWrapper character,String title) {
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.Awaken;
		qp.targetOfSearch = spellLocation;
		
		String ret = "Awaken (nothing)";
		if (targetSpell!=null && topSpell==targetSpell) {
			/*
			 * If belongs to an artifact or book, then the spell moves to that item (tag with "awakened")
			 * If belongs to a site, then it is put on the bottom of the spell pile (having been seen) - NEVER tagged!
			 */
			if (!spellLocation.hasThisAttribute("treasure_location") && !targetSpell.hasThisAttribute(Constants.SPELL_AWAKENED)) { // Not a SITE
				targetSpell.setThisAttribute(Constants.SPELL_AWAKENED);
				qp.objectList.add(targetSpell);
				qp.searchHadAnEffect = true;
			}
			spellLocation.add(targetSpell); // Moves topSpell to bottom of pile
			RealmComponent rc = RealmComponent.getRealmComponent(targetSpell);
			IconGroup group = new IconGroup(rc.getIcon(),IconGroup.VERTICAL,1);
			group.addIcon(getRollerImage());
			JOptionPane.showMessageDialog(getParentFrame(),title,"Read Runes",JOptionPane.INFORMATION_MESSAGE,group);
			ret = "Awaken "+getSpellName();
		}
		
		character.testQuestRequirements(getParentFrame(),qp);
		
		return ret;
	}
	@Override
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		list.add(getIconForSearch(RealmComponent.getRealmComponent(spellLocation)));
		return list;
	}
}