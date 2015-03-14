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
package com.robin.magic_realm.components.utility;

import java.util.*;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.DieRoller;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.table.RealmTable;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class DieRollBuilder {
	
	private static Hashtable<Long,DieRollBuilder> builderHash = null;
	public static DieRollBuilder getDieRollBuilder(JFrame parent,CharacterWrapper character) {
		return getDieRollBuilder(parent,character,0);
	}
	public static DieRollBuilder getDieRollBuilder(JFrame parent,CharacterWrapper character,int redDie) {
		if (builderHash==null) {
			builderHash = new Hashtable<Long,DieRollBuilder>();
		}
		DieRollBuilder drb = builderHash.get(character.getGameObject().getId());
		if (drb == null) {
			drb = new DieRollBuilder(parent,character,redDie);
			builderHash.put(character.getGameObject().getId(),drb);
		}
		drb.parent = parent;
		drb.redDie = redDie;
		return drb;
	}
	public static void reset() {
		if (builderHash!=null) builderHash.clear();
		builderHash = null;
	}
	
	private JFrame parent;
	private CharacterWrapper character;
	private int redDie; // if zero, then it does nothing
	
	public DieRollBuilder(JFrame parent,CharacterWrapper character,int redDie) {
		this.parent = parent;
		this.character = character;
		this.redDie = redDie;
	}
	public DieRoller createRoller(RealmTable table) {
		return createRoller(table.getTableKey());
	}
	public DieRoller createRoller(String key) {
		return createRoller(key,character.getCurrentLocation());
	}
	public DieRoller createRoller(String key,TileLocation tl) {
		int dice = 2;
		int mod = 0;
		boolean controlsRed = false;
		
		// key refers to a source table, or type of action that the roller will be used for
		key = key.toLowerCase();
		
		// The following will be a list of strings that contains tilename and all state chit names:
		//  For example:
		//			High Pass:Ruins:Lost City:Hoard:Lair:Patter:Flutter:Ruins V
		ArrayList<String> chitDescriptionList = tl.tile.getChitDescriptionList();
		
		// Cycle through all activated treasures, bewitching spells, and character to determine modifiers
		ArrayList objectsToTest = new ArrayList();
		if (character!=null) {
			objectsToTest.add(character.getGameObject()); // the character
			objectsToTest.addAll(character.getEnhancingItems()); // active treasures and travelers
			TileLocation current = character.getCurrentLocation();
			for (SpellWrapper spell:SpellUtility.getBewitchingSpells(character.getGameObject())) {
				objectsToTest.add(spell.getGameObject());
			}
			for (SpellWrapper spell:SpellUtility.getBewitchingSpells(current.tile.getGameObject())) {
				objectsToTest.add(spell.getGameObject());
			}
		}
		else {
			// If there is no character, then you can only use the clearing as a reference!
			mod += ClearingUtility.getClearingDieMod(tl);
		}
		
		for (Iterator i=objectsToTest.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			
			ArrayList list = null;
			if (go.hasThisAttribute(Constants.DIEMOD)) {
				list = new ArrayList(go.getThisAttributeList(Constants.DIEMOD));
			}
			if (go.hasAttribute(Constants.OPTIONAL_BLOCK,Constants.DIEMOD)) {
				if (list==null) {
					list = new ArrayList();
				}
				list.addAll(go.getAttributeList(Constants.OPTIONAL_BLOCK,Constants.DIEMOD));
			}
			
			if (list!=null) {
				String boardNumber = RealmUtility.updateNameToBoard(go,"");
				for (Iterator n=list.iterator();n.hasNext();) {
					String rule = (String)n.next();
					rule = StringUtilities.findAndReplace(rule,Constants.BOARD_NUMBER_REPLACE_PATTERN,boardNumber);
					DieRule dieRule = new DieRule(tl,rule);
					if (dieRule.conditionsMet(key,chitDescriptionList)) {
						if (dieRule.isMinusOne()) {
							mod--; // -1 results ARE cumulative
						}
						else if (dieRule.isMinusTwo()) {
							mod -= 2; // -2 results ARE cumulative
						}
						else if (dieRule.isOneDie()) {
							dice = 1;
						}
						else if (dieRule.isPlusOne()) {
							mod++; // +1 results ARE cumulative
						}
					}
				}
			}
			
			String redDieControl = go.getThisAttribute(Constants.RED_DIE);
			if (redDieControl!=null && (redDieControl.indexOf(key)>=0)) {
				controlsRed = true;
			}
		}
		
		if (character!=null && character.isCharacter() && tl.hasClearing() && !"wounds".equals(key)) {
			// Only characters are affected by the Cloven Hoof.  Hired Leaders and Controlled monsters are not.
			// Serious wounds are also not affected, as this isn't technically "rolling on a table".
			mod += ClearingUtility.getClearingDieMod(tl);
		}
		
		return createRoller(dice,mod,controlsRed,key);
	}
		/*
		 * Things that affect die rolls
		 * 		Lucky Charm (1 die on ALL rolls) one_die=All
		 * 		Cloven Hoof (add one to ALL rolls - affects every character in the clearing!) plus_one=Clearing
		 * 		Deft Gloves (Loot with 1 die) one_die=Loot
		 * 		Map of Lost City (-1 to Locate in Lost City Tile) minus_one=Lost City
		 * 		Map of Lost Castle (-1 to Locate in Lost Castle Tile) minus_one=Lost Castle
		 * 		Map of Ruins (-1 to Locate in any RUINS clearing - The Ruins Tile or any tile with a RUINS warning) minus_one=Ruins
		 * 		Magic Wand (control RED die on Spell rolls - must choose BEFORE rolling white die, assuming 2 dice are required) red_die
		 * 
		 * Character specials
		 * 		minus_one=Missile
		 * 			Amazon gets -1 to MISSILE table rolls
		 * 			Black Knight gets -1 to MISSILE table rolls
		 * 			Captain gets -1 to MISSILE table rolls
		 * 
		 * 		one_die=Missile
		 * 			Elf rolls one die for any MISSILE table roll
		 * 			Woodsgirl rolls one die for any MISSILE table roll
		 * 
		 * 		minus_one=Meeting
		 *	 		White Knight gets -1 to all MEETING rolls
		 * 
		 * 		one_die=Meeting
		 * 			Black Knight rolls one die for any MEETING
		 * 
		 * 		one_die=Hide
		 * 			Druid rolls one die for all HIDE
		 * 
		 * 		minus_one=ReadRunes
		 * 			Magician gets -1 for any READRUNES
		 * 			Witch gets -1 for any READRUNES
		 * 
		 * 		one_die=ReadRunes
		 * 			Pilgrim rolls one die for any READRUNES
		 * 			Sorceror rolls one die for any READRUNES
		 * 			Wizard rolls one die for any READRUNES
		 * 
		 * 		one_die=Trade
		 * 			Swordsman rolls one die for any TRADE
		 * 
		 * 		woodstile_one_die=search,hide,meeting
		 * 			Woodsgirl rolls one die in any WOODS tile for any SEARCH, MEETING or HIDE
		 * 
		 * 		caveclearing_one_die=search,hide,meeting
		 * 			Dwarf rolls one die in a cave for any SEARCH, MEETING or HIDE
		 */
	public DieRoller createHideRoller() {
		return createRoller("Hide");
	}
	public DieRoller createFortifyRoller() {
		return createRoller("Fortify");
	}
	private DieRoller createRoller(int numDice,int mod,boolean controlsRed,String text) {
		DieRoller roller = new DieRoller();
		roller.adjustDieSize(25, 6);
		roller.addRedDie();
		if (numDice == 2) {
			roller.addWhiteDie();
		}
		roller.setModifier(mod);
		roller.rollDice(StringUtilities.capitalize(text));
		if (redDie>0) {
			roller.setValue(0,redDie);
		}
		if (controlsRed && redDie==0) {
			if (parent!=null) {
				// This should only happen when being hit by the Imp Curse, or Demon PoP
				int n = SpellUtility.chooseRedDie(parent,text,character);
				roller.setValue(0,n);
			}
			else {
				// HOPEFULLY, we never get HERE anymore.
				
				// If character cannot choose, then assume they want "1". BAD ASSUMPTION: sometimes you'll want a 6!!
				// I think the ONLY time parent is null now, is if rolling for a magicmissile
				// NEW:  parent can be null if rolling for a COMBAT_HIDE (ie., World Fades spell) <-- this shouldn't use the wand!
				roller.setValue(0,1);
			}
		}
		return roller;
	}
}