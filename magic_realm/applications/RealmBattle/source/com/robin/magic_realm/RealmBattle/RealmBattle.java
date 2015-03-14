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
package com.robin.magic_realm.RealmBattle;

import java.util.*;
import java.util.logging.Logger;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.game.server.GameHost;
import com.robin.general.util.HashLists;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.Effort;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class RealmBattle {
	
	public static boolean newClearingCombat = false;
	
	private static Logger logger = Logger.getLogger(RealmBattle.class.getName());
	
	/**
	 * Clears out any current combat variables
	 */
	public static void resetCombat(GameData data) {
		logger.fine("-------");
		RealmComponent.resetTargetIndex(data);
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(data).getPlayerCharacterObjects());
		Collection characterGameObjects = pool.find(CharacterWrapper.getKeyVals());
		for (Iterator i=characterGameObjects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			
			// Clear combat info
			CharacterWrapper character = new CharacterWrapper(go);
			logger.finer(character.getCharacterName());
			character.clearCombat();
			character.setCombatCount(0);
			CombatWrapper.clearAllCombatInfo(character.getGameObject());
			
			// Flip monster weapons light side up
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)rc;
				MonsterPartChitComponent weapon = monster.getWeapon();
				if (weapon!=null && weapon.isDarkSideUp()) {
					weapon.setLightSideUp();
				}
			}
		}
	}
	
	public static int getNextWaitState(int nonWaitState) {
		switch(nonWaitState) {
			case Constants.COMBAT_PREBATTLE:		return Constants.COMBAT_WAIT+Constants.COMBAT_LURE; // Should only happen once per evening
			case Constants.COMBAT_LURE:			return Constants.COMBAT_WAIT+Constants.COMBAT_RANDOM_ASSIGN;
			case Constants.COMBAT_RANDOM_ASSIGN:	return Constants.COMBAT_WAIT+Constants.COMBAT_DEPLOY;
			case Constants.COMBAT_DEPLOY:			return Constants.COMBAT_WAIT+Constants.COMBAT_ACTIONS;
			case Constants.COMBAT_ACTIONS:		return Constants.COMBAT_WAIT+Constants.COMBAT_ASSIGN;
			case Constants.COMBAT_ASSIGN:			return Constants.COMBAT_WAIT+Constants.COMBAT_POSITIONING;
			case Constants.COMBAT_POSITIONING:		return Constants.COMBAT_WAIT+Constants.COMBAT_TACTICS;
			case Constants.COMBAT_TACTICS:		return Constants.COMBAT_WAIT+Constants.COMBAT_RESOLVING;
			case Constants.COMBAT_RESOLVING:		return Constants.COMBAT_WAIT+Constants.COMBAT_FATIGUE;
			case Constants.COMBAT_FATIGUE:		return Constants.COMBAT_WAIT+Constants.COMBAT_DISENGAGE;
			case Constants.COMBAT_DISENGAGE:		return Constants.COMBAT_WAIT+Constants.COMBAT_LURE;
		}
		throw new IllegalArgumentException("Invalid nonWaitState: "+nonWaitState);
	}
	public static BattlesWrapper getBattles(GameData data) {
		GameWrapper game = GameWrapper.findGame(data);
		BattlesWrapper battles = new BattlesWrapper(game.getGameObject());
		return battles;
	}
	public static void initCombatOrder(GameData data) {
		logger.fine("-------");
		
		BattlesWrapper battles = getBattles(data);
		
		ArrayList battleLocations = new ArrayList();
		
		// Find all combat clearings by locating all active characters and leaders
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(data).getPlayerCharacterObjects());
		Collection characterGameObjects = pool.extract(CharacterWrapper.getKeyVals());
		for (Iterator i=characterGameObjects.iterator();i.hasNext();) {
			CharacterWrapper character = new CharacterWrapper((GameObject)i.next());
			if (character.isActive()) {
				TileLocation tl = character.getCurrentLocation();
				if (tl!=null && tl.hasClearing()) {
					BattleModel battleModel = buildBattleModel(tl,data);
					boolean combatIsOn = false;
					if (battleModel.getGroupCount(true)>1 || character.getWantsCombat()) { // has to be more than one group to have a fight! FIXME (ignores treachery for now)
						combatIsOn = true;
						if (battleModel.getDenizenBattleGroup()==null && !character.getWantsCombat()) {
							// make sure there is at least one character battling another (might all be friendly)
							boolean battlingChars = false;
							ArrayList leaders = battleModel.getAllLeaders();
							for (Iterator n=leaders.iterator();n.hasNext();) {
								RealmComponent rc1 = (RealmComponent)n.next();
								CharacterWrapper char1 = new CharacterWrapper(rc1.getGameObject());
								if (char1.getWantsCombat()) {
									// If somebody wants combat, then get out of here anyway
									battlingChars = true;
								}
								else {
									for (Iterator t=leaders.iterator();t.hasNext();) {
										RealmComponent rc2 = (RealmComponent)t.next();
										if (char1.isEnemy(rc2.getGameObject())) {
											battlingChars = true;
											break;
										}
									}
								}
								if (battlingChars) {
									break;
								}
							}
							if (!battlingChars) {
								combatIsOn = false;
							}
						}
					}
					
					if (!combatIsOn) {
						logger.finer("no combat!");
					}
					else {
						// COMBAT_PREBATTLE will be tested in the nextCombatAction method, and will be skipped if unnecessary
						if (!battleLocations.contains(tl)) {
							battleLocations.add(tl);
							logger.finer("combat in "+tl);
						}
					}
				}
			}
		}
		// Assign random clearing order (TODO Need to check the rules here)
		battles.clearBattles();
		if (battleLocations.size()>0) {
			while(battleLocations.size()>0) {
				int r = RandomNumber.getRandom(battleLocations.size());
				TileLocation location = (TileLocation)battleLocations.remove(r);
				battles.addBattleLocation(location,data);
			}
			battles.initNextBattleLocation(data); // this starts it off
		}
	}
	/**
	 * @return		The current TileLocation (clearing) where combat is taking place, or null if none
	 */
	public static TileLocation getCurrentCombatLocation(GameData data) {
		BattlesWrapper battles = getBattles(data);
		return battles.getCurrentBattleLocation(data);
	}
	public static HashLists findCharacterStates(TileLocation currentCombatLocation,GameData data) {
		// Get all characters involved in battle
		BattleModel model = buildBattleModel(currentCombatLocation,data);
		Collection c = model.getAllOwningCharacters();
		HashLists lists = new HashLists();
		for (Iterator n=c.iterator();n.hasNext();) {
			RealmComponent rc = (RealmComponent)n.next();
			CharacterWrapper aChar = new CharacterWrapper(rc.getGameObject());
			int astate = aChar.getCombatStatus();
			if (astate>0) {
				lists.put(new Integer(astate),aChar);
			}
		}
		return lists;
	}
	/**
	 * Sets all clearing orders down by one
	 */
	public static void updateClearingOrder(GameData data) {
		BattlesWrapper battles = getBattles(data);
		battles.initNextBattleLocation(data);
	}
	public static HashLists currentCombatHashLists(GameData data) {
		logger.fine("-------");
		TileLocation currentCombatLocation = getCurrentCombatLocation(data);
		if (currentCombatLocation!=null && currentCombatLocation.hasClearing()) {
			HashLists lists = findCharacterStates(currentCombatLocation,data);
			return lists;
		}
		return null;
	}
	public static void LogStage(String stage) {
		RealmLogging.logMessage(RealmLogging.BATTLE," - - - - - "+stage);
	}
	/**
	 * Determines the current state of combat, and advances to the next step.
	 * 
	 * @return			false when there is no further action to take
	 */
	public static boolean nextCombatAction(GameHost host,GameData data) {
		logger.fine("-------");
		TileLocation currentCombatLocation = getCurrentCombatLocation(data);
		if (currentCombatLocation!=null && currentCombatLocation.hasClearing()) {
			
			HashLists lists = findCharacterStates(currentCombatLocation,data);
			
			ArrayList states = new ArrayList(lists.keySet());
			
			if (states.isEmpty()) { // this can happen when a character runs!  (I think...)
				updateClearingOrder(data);
				return nextCombatAction(host,data);
			}
			
			Collections.sort(states);
			
			// Determine the "first" state
			Integer firstState = (Integer)states.iterator().next();
			
			// Get all those characters in the first state
			ArrayList choices = lists.getList(firstState);
				
			logger.finer("firstState="+firstState+" for "+choices);
			
			if (firstState.intValue()<Constants.COMBAT_WAIT // This first test seems to be useless....
					&& firstState.intValue()==Constants.COMBAT_RESOLVING) {
				// Might be stuck in a loop if all characters are hidden!
				CharacterWrapper active = (CharacterWrapper)choices.iterator().next();
				if (!requiresCombatInteraction(currentCombatLocation,active)) {
					active.setCombatStatus(getNextWaitState(firstState.intValue()));
					logger.finer(active.getCharacterName()+" No interaction needed, moving to next state...");
					return nextCombatAction(host,data); // recurses!
				}
			}
			else {
				// Subtracting WAIT from the wait state yields the active state
				int actionState = firstState.intValue(); // might already be active state if handling character requests!
				if (actionState>Constants.COMBAT_WAIT) {
					actionState -= Constants.COMBAT_WAIT;
				}
				
				// Check to see if everyone is on a single wait state
				if (lists.size()==1) {
					// Only one state, means everyone is the same.
					// There is a special case where every character chooses to skip combat.  Check for this.
					boolean skipCombat = true;
					for (Iterator i=choices.iterator();i.hasNext();) {
						CharacterWrapper character = (CharacterWrapper)i.next();
						CombatWrapper combat = new CombatWrapper(character.getGameObject());
						if (!combat.getSkipCombat()) {
							skipCombat = false;
							break;
						}
					}
					for (Iterator i=choices.iterator();i.hasNext();) {
						CharacterWrapper character = (CharacterWrapper)i.next();
						CombatWrapper combat = new CombatWrapper(character.getGameObject());
						combat.setSkipCombat(false); // reset it now
					}
					if (skipCombat) {
						// All characters agreed to skip combat, so end it!
						endCombatInClearing(currentCombatLocation,data);
						updateClearingOrder(data);
						return nextCombatAction(host,data); // recurses!
					}
					
					// Check special inits before moving onto actionState
					switch(actionState) {
						case Constants.COMBAT_PREBATTLE:
							break;
						case Constants.COMBAT_LURE:
							// need to do preparation before moving onto LURE
							preparation(currentCombatLocation,data);
							LogStage("Luring");
							break;
						case Constants.COMBAT_RANDOM_ASSIGN:
							// need to do random assignment before moving onto RANDOM_ASSIGN stage
							randomAssignment(currentCombatLocation,data);
							break;
						case Constants.COMBAT_DEPLOY:
							LogStage("Deployment");
							break;
						case Constants.COMBAT_ACTIONS:
							LogStage("Actions");
							break;
						case Constants.COMBAT_ASSIGN:
							// need to init melee before moving into target assignment
							LogStage("Assign Targets");
							initMelee(currentCombatLocation,data);
							break;
						case Constants.COMBAT_POSITIONING:
							LogStage("Positioning");
							// need to energize spells here
							energizeSpells(currentCombatLocation,data);
							fixSheetOwners(currentCombatLocation,data);
							break;
						case Constants.COMBAT_TACTICS:
							LogStage("Reposition/Tactics");
							repositionTactics(currentCombatLocation,data);
							break;
						case Constants.COMBAT_RESOLVING:
							// need to resolve attacks before informing players of the resolution
							LogStage("Resolve Combat");
							resolveCombat(currentCombatLocation,data);
							break;
						case Constants.COMBAT_FATIGUE:
							expireWishStrength(currentCombatLocation,data);
							break;
						case Constants.COMBAT_DISENGAGE:
							if (disengage(currentCombatLocation,data)) {
								// reset back to LURE
								for (Iterator n=choices.iterator();n.hasNext();) {
									CharacterWrapper one = (CharacterWrapper)n.next();
									one.setCombatStatus(Constants.COMBAT_LURE+Constants.COMBAT_WAIT);
								}
							}
							HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
							if (hostPrefs.hasPref(Constants.OPT_ENHANCED_MAGIC)) {
								// For Enhanced Magic, incantations are broken every round to free up magic chits again!
								SpellMasterWrapper.getSpellMaster(data).breakAllIncantations(false);
							}
							SpellMasterWrapper.getSpellMaster(data).expirePhaseSpells();
							return nextCombatAction(host,data); // recurses!
					}
				}
				
				CharacterWrapper active = null;
				if (actionState==Constants.COMBAT_RESOLVING) {
					// RESOLVING happens simultaneously
					for (Iterator n=choices.iterator();n.hasNext();) {
						active = (CharacterWrapper)n.next();
						active.setCombatStatus(actionState);
					}
					logger.finer("all.setCombatStatus "+actionState);
				}
				else { // Might be empty if removed above
					
					// Need to preserve the DAYLIGHT order, so sort on getCombatPlayOrder
					// TODO But DONT preserve the daylight order for TARGET selection...
					
					if (actionState==Constants.COMBAT_ASSIGN) {
						Collections.sort(choices,new Comparator() {
							public int compare(Object o1,Object o2) {
								int ret = 0;
								CharacterWrapper c1 = (CharacterWrapper)o1;
								CharacterWrapper c2 = (CharacterWrapper)o2;
								ret = c1.getMeleePlayOrder()-c2.getMeleePlayOrder();
								return ret;
							}
						});
					}
					else {
						Collections.sort(choices,new Comparator() {
							public int compare(Object o1,Object o2) {
								int ret = 0;
								CharacterWrapper c1 = (CharacterWrapper)o1;
								CharacterWrapper c2 = (CharacterWrapper)o2;
								ret = c1.getCombatPlayOrder()-c2.getCombatPlayOrder();
								return ret;
							}
						});
					}
					
					// Take the first one (because of the sort, should be the next in line)
					active = (CharacterWrapper)choices.iterator().next();
					active.setCombatStatus(actionState);
					logger.finer(active.getCharacterName()+" setCombatStatus "+actionState);
				}
				
				// Should test whether the actionState will do anything, and if not, skip to the next action
				if (active!=null && !requiresCombatInteraction(currentCombatLocation,active)) {
					active.setCombatStatus(getNextWaitState(actionState));
					logger.finer(active.getCharacterName()+" No interaction needed, moving to next state...");
					return nextCombatAction(host,data); // recurses!
				}
			}
			
			return true;
		}
		return false;
	}
	/**
	 * Tests to see if the character actually needs to do something with the current action state
	 * 
	 * @return			true if the Character needs to be prompted with the CombatDialog
	 */
	public static boolean requiresCombatInteraction(TileLocation current,CharacterWrapper character) {
		CombatWrapper combat = new CombatWrapper(character.getGameObject());
		if (character.isDead() || character.isGone()) { // character is dead or gone
			return false;
		}
		if (combat.isDead() && character.getCombatStatus()<Constants.COMBAT_RESOLVING) {
			return false;
		}
		if (combat.isLockNext()) {
			return false;
		}
		CombatWrapper tile = new CombatWrapper(current.tile.getGameObject());
		boolean peace = tile.isPeaceClearing(current.clearing.getNum());
		BattleModel model = buildBattleModel(current,character.getGameObject().getGameData());
		boolean canSkipCombat = model.canSkipCombat(character.getCombatStatus());
		BattleGroup denizens = model.getDenizenBattleGroup();
		ArrayList sheetOwners = new ArrayList();
		for (Iterator i=model.getAllBattleParticipants(true).iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper rcCombat = new CombatWrapper(rc.getGameObject());
			if (rcCombat.isSheetOwner()) {
				sheetOwners.add(rc);
			}
		}
		RealmComponent charRc = RealmComponent.getRealmComponent(character.getGameObject());
		boolean activeCharacterIsHere = model.getBattleGroup(charRc).getCharacterInBattle()!=null;
		boolean activeUnmistyCharacterIsHere = activeCharacterIsHere && !character.isMistLike();
		logger.finer(character.getGameObject().getName()+" is here = "+activeCharacterIsHere);
		switch(character.getCombatStatus()) {
			case Constants.COMBAT_PREBATTLE:
				// If no unhired natives, then prebattle is not necessary
				if (model.areUnhiredNatives()) {
					return true;
				}
				break;
			case Constants.COMBAT_LURE:
				if (combat.isPeaceful()) {
					return false;
				}
				// Check to see if any of the character's hirelings can lure
				boolean hirelingsCanLure = false;
				for (Iterator i=model.getBattleGroup(charRc).getBattleParticipants().iterator();i.hasNext();) {
					RealmComponent test = (RealmComponent)i.next();
					if (!test.isCharacter()) {
						if (model.getAttackersFor(test).size()==0) { // found one!
							hirelingsCanLure = true;
							break;
						}
					}
				}
				
				if ((!activeCharacterIsHere || character.isMistLike()) && !hirelingsCanLure) {
					return false;
				}
			
				// Check for unassigned denizens
				RealmComponent characterTarget = null;
				if (denizens!=null && denizens.size()>0) {
					for (Iterator i=denizens.getBattleParticipants().iterator();i.hasNext();) {
						RealmComponent rc = (RealmComponent)i.next();
						RealmComponent target = rc.getTarget();
						if (target==null) { // only need one unassigned denizen
							return true;
						}
						else if (target.isCharacter()) {
							if (!rc.isMonster() || !((MonsterChitComponent)rc).isPinningOpponent()) {
								// Don't include red-side-up monsters
								if (!target.getGameObject().equals(character.getGameObject())) {
									// At least one denizen is targeting a character that is not THIS character
									return true;
								}
								characterTarget = target;
							}
						}
					}
				}
				if (characterTarget!=null) { // there is at least one denizen on a character sheet
					// If one of the current character's hirelings can lure, then return true here
					return hirelingsCanLure;
				}
				break;
			case Constants.COMBAT_RANDOM_ASSIGN:
				// Only true if the character "wins" a random assignment, and needs to select an
				// appropriate hireling because he/she is absent or hidden
				return character.getGameObject().hasThisAttribute(Constants.RANDOM_ASSIGNMENT_WINNER);
			case Constants.COMBAT_DEPLOY:
				/*
				 * Should only return true if there is more than one character, or there are any unassigned
				 * hired natives, or if combat can be skipped
				 */
				int count = 0;
				for (Iterator i=model.getAllParticipatingCharacters().iterator();i.hasNext();) {
					CharacterChitComponent chit = (CharacterChitComponent)i.next();
					if (!chit.isMistLike()) {
						count++;
					}
				}
				boolean multipleCharacters = count>1;
				BattleGroup battleGroup = model.getParticipantsBattleGroup(RealmComponent.getRealmComponent(character.getGameObject()));
				Collection hirelings = battleGroup.getHirelings();
				boolean unassignedHirelings = false;
				for (Iterator i=hirelings.iterator();i.hasNext();) {
					RealmComponent hireling = (RealmComponent)i.next();
					if (model.getAttackersFor(hireling).size()==0) {
						unassignedHirelings = true;
						break;
					}
				}
				return multipleCharacters || unassignedHirelings;
			case Constants.COMBAT_ACTIONS:
				// Don't get an encounter phase if you aren't actually there! (unless combat can be skipped)
				return canSkipCombat || (activeCharacterIsHere && combat.getUsedChitCount()==0);
			case Constants.COMBAT_ASSIGN: // assigning targets
				if (!peace && !combat.isPeaceful()) {
					// First check to see if there are any unassigned denizens or an active combat spell
					if (activeUnmistyCharacterIsHere && (model.areUnassignedDenizens() || combat.getCastSpell()!=null)) {
						return true;
					}
					
					// Test to see if the character is present and has potential targets to assign
					BattleGroup group = model.getBattleGroup(charRc);
					if (activeUnmistyCharacterIsHere) {
						Collection c = model.getAllOtherBattleParticipants(group,true,character.getTreacheryPreference());
						if (c.size()>0) {
							return true;
						}
					}
					else {
						// Test to see if any hireling has any potential targets (more than one attacker)
						for (Iterator i=group.getBattleParticipants().iterator();i.hasNext();) {
							RealmComponent rc = (RealmComponent)i.next();
							Collection c = model.getAttackersFor(rc);
							if (c.size()>1) {
								return true;
							}
						}
					}
				}
				return false;
			case Constants.COMBAT_POSITIONING: // playing attacks and manuevers
				return true; // Show positioning every round - this allows hidden characters to extend combat by fatiguing on purpose...
			case Constants.COMBAT_TACTICS: // flips denizens, repositions, allows characters that *can* to replace MOVE or FIGHT
				Collection attackers = model.getAttackersFor(RealmComponent.getRealmComponent(character.getGameObject()));
				RealmComponent attacker = RealmComponent.getRealmComponent(character.getGameObject());
				RealmComponent target = attacker.getTarget();
				return (activeCharacterIsHere 
						&& ( (target!=null && character.canReplaceFight(target))
								|| character.canReplaceMove(attackers)));
			case Constants.COMBAT_RESOLVING: // determines hits, show results
				return true; // Show resolution every round - this guarantees that everything is cleaned up properly.
			case Constants.COMBAT_FATIGUE:
				// Need input here if wounds were taken, or chits were fatigued, and the character is NOT dead.
				if (combat.getKilledBy()==null) {
					int healing = combat.getHealing();
					int newWounds = combat.getNewWounds();
					Effort effortUsed = BattleUtility.getEffortUsed(character);
					int free = character.getEffortFreeAsterisks();
					return (healing>0 || newWounds>0 || effortUsed.getNeedToFatigue(free)>0);
				}
				return false;
			case Constants.COMBAT_DONE:
				return false; // has to stop somewhere...
		}
		return false;
	}
	private static final String UNCONTROLLED = "UNCONTROLLED";
	
	/**
	 * Seems dumb to have to build the BattleModel over and over, but this is the only way I can persist these
	 * things.  I suppose if the speed isn't affected, then I shouldn't be too concerned about optimizing this.
	 * 
	 * @param tl			Builds a BattleModel for the specified location
	 * @param data			The GameData
	 */
	public static BattleModel buildBattleModel(TileLocation tl,GameData data) {
		HashLists lists = new HashLists();
		Collection c = ClearingUtility.getCombatantsInClearing(tl);
		
		// Hash all combatants by ownerid - uncontrolled denizens will be owned by UNCONTROLLED
		for (Iterator i=c.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			String ownerid = rc.getOwnerId(); // Note:  characters own themselves
			if (ownerid==null) {
				// uncontrolled denizen
				ownerid = UNCONTROLLED;
			}
			lists.put(ownerid,rc);
		}
		
		// Initialize a BattleModel
		BattleModel model = new BattleModel(data,tl);
		
		// Iterate through the owner lists, and create separate BattleGroup objects for each
		for (Iterator i=lists.keySet().iterator();i.hasNext();) {
			String ownerid = (String)i.next();
			
			// Create a BattleGroup for the owner
			RealmComponent owner = RealmComponent.getRealmComponentFromId(data,ownerid); // could be null
			BattleGroup group = new BattleGroup(owner);
			
			// Put everyone in the list into the group
			ArrayList list = lists.getList(ownerid);
			for (Iterator n=list.iterator();n.hasNext();) {
				RealmComponent rc = (RealmComponent)n.next();
				group.addBattleParticipant((BattleChit)rc);
			}
			
			// Add the group to the model
			model.addBattleGroup(group);
		}
		return model;
	}
	
	/**
	 * @param location		The location (clearing) of interest
	 * @param data			The GameData object
	 */
	public static void preparation(TileLocation location,GameData data) {
		BattleModel model = buildBattleModel(location,data);
		CombatWrapper tile = new CombatWrapper(location.tile.getGameObject());
		model.doPreparation(tile.getHitResultCount()+1);
	}
	
	public static void randomAssignment(TileLocation location,GameData data) {
		BattleModel model = buildBattleModel(location,data);
		model.doRandomAssignment();
	}

	public static void initMelee(TileLocation location,GameData data) {
		BattleModel model = buildBattleModel(location,data);
		model.doMeleeSetup();
	}
	
	public static void energizeSpells(TileLocation location,GameData data) {
		BattleModel model = buildBattleModel(location,data);
		model.doEnergizeSpells();
	}
	
	public static void fixSheetOwners(TileLocation location,GameData data) {
		BattleModel model = buildBattleModel(location,data);
		model.doFixSheetOwners();
	}
	
	public static void repositionTactics(TileLocation location,GameData data) {
		logger.fine("----");
		BattleModel model = buildBattleModel(location,data);
		model.doRepositioningAndTactics();
	}
	
	public static void resolveCombat(TileLocation location,GameData data) {
		logger.fine("----");
		CombatWrapper tile = new CombatWrapper(location.tile.getGameObject());
		BattleModel model = buildBattleModel(location,data);
		
		boolean fatigue = tile.getWasFatigue(); // from running characters (rare situation)
		boolean spellCasting = false;
		if (!fatigue) { // only continue if we got false on fatigue
			ArrayList characters = model.getAllParticipatingCharacters();
			for (Iterator i=characters.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
				Effort effortUsed = BattleUtility.getEffortUsed(character);
				int free = character.getEffortFreeAsterisks();
				if (effortUsed.getAsterisks()>free) {
					fatigue = true;
					break; // need only one character to fatigue for this to be true
				}
				
				CombatWrapper combat = new CombatWrapper(rc.getGameObject());
				if (combat.getCastSpell()!=null || combat.isBurnedColor()) {
					spellCasting = true;
					break;
				}
			}
		}
		else {
			tile.setWasFatigue(false);
		}
		
		int hits = model.doResolveAttacks(tile.getHitResultCount()+1);
		if (hits>0 || fatigue || spellCasting || model.wasSpellCasting()) {
			tile.addHitResult();
		}
		else {
			tile.addMissResult();
		}
	}
	public static boolean expireWishStrength(TileLocation location,GameData data) {
		BattleModel model = buildBattleModel(location,data);
		model.doExpireWishStrength();
		return true;
	}
	/**
	 * @return		true if combat will continue in this clearing
	 */
	public static boolean disengage(TileLocation location,GameData data) {
		logger.fine("----");
		BattleModel model = buildBattleModel(location,data);
		model.doDisengagement();
		
		// First test to see if there were two rounds without any hits
		CombatWrapper tile = new CombatWrapper(location.tile.getGameObject());
		
		// Rebuild model, and test whether combat is still alive
		model = buildBattleModel(location,data);
		
		// Report rounds-of-missing
		int rom = tile.getRoundsOfMissing();
		if (rom>0) {
			CombatFrame.broadcastMessage(RealmLogging.BATTLE,rom+" consecutive round"+(rom==1?"":"s")+" of no damage, fatigue, or spellcasting.");
		}

		TileLocation test = getCurrentCombatLocation(data); // if test is null, then all characters in the clearing are dead!
		if (test==null || (model.getAllOwningCharacters().isEmpty()) || (tile.lastTwoAreMisses() && !model.arePinningMonsters())) {
			// Combat is over.  Move to the next clearing.
			endCombatInClearing(location,data);
			updateClearingOrder(data);
			return false;
		}
		else {
			logger.finer("Combat continues in "+location);
		}
		return true;
	}
	/**
	 * This method will test whether the given clearing still has combat participants, and if not, ends it.
	 * This will be called whenever someone runs away.
	 * 
	 * @return		true if combat still active
	 */
	public static boolean testCombatInClearing(TileLocation location,GameData data) {
//System.out.println("RealmBattle:  testCombatInClearing for data "+data.toIdentifier());
		BattleModel battleModel = buildBattleModel(location,data);
//System.out.println("RealmBattle:  group count="+battleModel.getGroupCount(true));
		if (battleModel.getGroupCount(false)==0) {
//System.out.println("RealmBattle:  endCombatInClearing");
			endCombatInClearing(location,data);
			return false;
		}
		return true;
	}
	/**
	 * Performs all the necessary cleanup to end combat in the clearing.
	 */
	public static void endCombatInClearing(TileLocation location,GameData data) {
		logger.finer("Combat ended in "+location);
		
		// Clear out battles
		BattlesWrapper battles = getBattles(data);
		battles.clearBattleInfo(location,data);
		
		// Clear out tile object (only the round stuff - don't mess with PEACE, now that it is clearing by clearing)
//		CombatWrapper.clearAllCombatInfo(location.tile.getGameObject());
		CombatWrapper cw = new CombatWrapper(location.tile.getGameObject());
		cw.clearHitResults();
		cw.setWasFatigue(false);
		
		// Need to clear out all monster combat info here too!
		for (Iterator i=location.clearing.getClearingComponents().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper.clearAllCombatInfo(rc.getGameObject());
			rc.clearTarget();
			
			// Return monsters/natives to light side
			if (rc.isMonster() || rc.isNative()) {
				ChitComponent chit = (ChitComponent)rc;
				chit.setLightSideUp();
				if (rc.isMonster()) {
					MonsterChitComponent monster = (MonsterChitComponent)rc;
					MonsterPartChitComponent weapon = monster.getWeapon();
					if (weapon!=null) {
						weapon.setLightSideUp();
					}
				}
				else if (rc.isNative()) {
					BattleHorse horse = rc.getHorse();
					if (horse!=null) {
						horse.setWalk();
					}
				}
			}
		}
	}
}