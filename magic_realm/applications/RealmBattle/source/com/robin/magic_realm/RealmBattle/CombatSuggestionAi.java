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

import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.IconFactory;
import com.robin.general.swing.IconGroup;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.BattleUtility;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class CombatSuggestionAi {
	
	private CombatFrame combatFrame;
	
	private IconGroup iconGroup;
	private StringBuffer suggestion;
	
	public CombatSuggestionAi(CombatFrame combatFrame) {
		this.combatFrame = combatFrame;
	}
	public void suggestAction() {
		iconGroup = null;
		suggestion = new StringBuffer();
		switch(combatFrame.getActionState()) {
			case Constants.COMBAT_LURE:
				testLure();
				break;
			case Constants.COMBAT_DEPLOY:
				testDeploy();
				break;
			case Constants.COMBAT_ACTIONS:
				testAlert();
				break;
			case Constants.COMBAT_ASSIGN:
				testAssign();
				break;
			case Constants.COMBAT_POSITIONING:
				testPosition();
				break;
			case Constants.COMBAT_TACTICS:
				break;
			case Constants.COMBAT_RESOLVING:
				// This never happens
				break;
			case Constants.COMBAT_FATIGUE:
				// This never happens
				break;
		}
		if (suggestion.length()>0) {
			JOptionPane.showMessageDialog(
					combatFrame,
					suggestion.toString(),
					"Suggested Action",
					JOptionPane.INFORMATION_MESSAGE,
					iconGroup);
		}
	}
	private boolean queryHostileCharacters() {
		int ret = JOptionPane.showConfirmDialog(
				combatFrame,
				"Are the other characters in the clearing hostile?",
				"Suggest Action",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return ret == JOptionPane.YES_OPTION;
	}
	private boolean queryAvoidFatigue() {
		int ret = JOptionPane.showConfirmDialog(
				combatFrame,
				"Do you want to avoid fatigue?",
				"Suggest Action",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return ret == JOptionPane.YES_OPTION;
	}
	private void testLure() {
		boolean denizens = combatFrame.getBattleModel().areDenizens();
		if (denizens) {
			boolean enemyNatives = false;
			for (Iterator i=combatFrame.getBattleModel().getDenizenBattleGroup().getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isNative()) {
					enemyNatives = true;
					break;
				}
			}
			if (combatFrame.getActiveCharacterIsHere()) {
				CharacterWrapper activeCharacter = combatFrame.getActiveCharacter();
				CharacterChitComponent cc = (CharacterChitComponent)RealmComponent.getRealmComponent(activeCharacter.getGameObject());
				if (activeCharacter.isHidden()) {
					suggestion.append("No need to do anything, unless you want to\nprotect another character by luring an enemy.");
					if (combatFrame.getHostPrefs().hasPref(Constants.ADV_AMBUSHES)) {
						GameObject go = cc.getActiveWeaponObject();
						if (go!=null && go.hasThisAttribute("missile")) {
							RealmComponent rc = RealmComponent.getRealmComponent(go);
							iconGroup = new IconGroup(rc.getIcon(),IconGroup.VERTICAL,4);
							suggestion.append("\n\nIn fact, if you choose to stay hidden, you might\n");
							suggestion.append("be successful making an ambush attack with your ");
							suggestion.append(go.getName());
							suggestion.append(".");
						}
					}
					else if (enemyNatives && combatFrame.getHostPrefs().hasPref(Constants.TE_WATCHFUL_NATIVES)) {
						suggestion.append("\n\nRemember that Watchful Natives is in play, so attacking from a hidden\n");
						suggestion.append("position will NOT protect you from retaliation.");
					}
				}
				else {
					suggestion.append("Lure what you think you can handle.  Remember that unlured opponents\n");
					suggestion.append("will be assigned randomly to all unhidden characters, so you may\n");
					suggestion.append("end up fighting anyway.");
				}
			}
		}
	}
	private void testDeploy() {
		boolean otherChars = combatFrame.getBattleModel().getGroupCount(false)>1;
		boolean denizens = combatFrame.getBattleModel().areDenizens();
		
		boolean hostileCharacters = otherChars?queryHostileCharacters():false;
		
		if (denizens || (hostileCharacters && otherChars)) {
			BattleGroup yourGroup = combatFrame.getBattleModel().getBattleGroup(combatFrame.getActiveParticipant());
			Collection hirelings = yourGroup.getHirelings();
			if (!hirelings.isEmpty()) {
				for (Iterator i=hirelings.iterator();i.hasNext();) {
					RealmComponent hireling = (RealmComponent)i.next();
					if (hireling.getTarget()==null) {
						CombatWrapper combat = new CombatWrapper(hireling.getGameObject());
						if (combat.getAttackerCount()==0) {
							iconGroup = new IconGroup(hireling.getIcon(),IconGroup.VERTICAL,4);
							suggestion.append("You should deploy your hireling against an enemy.");
							return;
						}
					}
				}
			}
			if (hostileCharacters && otherChars && combatFrame.getActiveCharacterIsHere()) {
				CharacterWrapper activeCharacter = combatFrame.getActiveCharacter();
				CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
				if (combat.getChargeChits().isEmpty()) {
					// Find a good chit to charge with
					Collection c = activeCharacter.getActiveMoveChits();
					c.addAll(activeCharacter.getFlyChits());
					RealmComponent best = null;
					Speed bestSpeed = null;
					for (Iterator i=c.iterator();i.hasNext();) {
						RealmComponent rc = (RealmComponent)i.next();
						Speed speed = null;
						if (rc.isActionChit()) {
							CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
							speed = chit.getSpeed();
						}
						else if (rc.isFlyChit()) {
							FlyChitComponent flyChit = (FlyChitComponent)rc;
							speed = flyChit.getSpeed();
						}
						if (bestSpeed==null || speed.fasterThan(bestSpeed)) {
							best = rc;
							bestSpeed = speed;
						}
					}
					if (best!=null) {
						iconGroup = new IconGroup(best.getIcon(),IconGroup.VERTICAL,4);
						suggestion.append("You might want to charge a hostile character to prevent their escape.");
						return;
					}
				}
			}
		}
	}
	private void testAlert() {
		if (combatFrame.getActiveCharacterIsHere()) {
			CharacterWrapper activeCharacter = combatFrame.getActiveCharacter();
			ArrayList<SpellSet> castableSets = activeCharacter.getCastableSpellSets();
			
			boolean threat = combatFrame.getBattleModel().areDenizens();
			if (threat) {
				WeaponChitComponent weapon = activeCharacter.getActiveWeapon();
				if (weapon!=null && !weapon.isAlerted() && weapon.hasAlertAdvantage()) {
					// Should check to see if you have a chit to do this
					MoveActivator activator = new MoveActivator(combatFrame);
					Speed fastest = activator.getFastestAttackerMoveSpeed();
					
					// Find all playable options
					Collection fightSpeedOptions = activeCharacter.getFightSpeedOptions(fastest,true);
					Collection availableFightOptions = combatFrame.getAvailableFightOptions(0);
					fightSpeedOptions.retainAll(availableFightOptions); // Intersection between the two
					if (!fightSpeedOptions.isEmpty()) {
						// Find the least effort (first) and then the lightest/slowest
						int minEffort = Integer.MAX_VALUE;
						Strength lightest = new Strength("T");
						RealmComponent option = null;
						for (Iterator i=fightSpeedOptions.iterator();i.hasNext();) {
							RealmComponent rc = (RealmComponent)i.next();
							int effort = rc.getGameObject().getThisInt("effort");
							if (effort<minEffort) {
								minEffort = effort;
								option = rc;
							}
							if (minEffort==effort) {
								Strength st = new Strength(rc.getGameObject().getThisAttribute("strength"));
								if (lightest.strongerThan(st)) {
									lightest = st;
									option = rc;
								}
							}
						}
						
						if (option!=null) {
							iconGroup = new IconGroup(option.getIcon(),IconGroup.VERTICAL,4);
							iconGroup.addIcon(weapon.getIcon());
							suggestion.append("You should alert your weapon, to gain an advantage in combat.\n"
									+"Use the least amount of effort, so you don't limit what you can do in battle.");
							return;
						}
					}
				}
				if (castableSets.size()>0) {
					// Should highlight attack spells here
					SpellSet best = null;
					for (SpellSet set:castableSets) {
						if (set.isAttackSpell()) {
							// Get the fastest
							if (best==null || set.getSpeed().fasterThanOrEqual(best.getSpeed())) {
								if (best!=null && set.getSpeed().equalTo(best.getSpeed())) {
									// Find the one that doesn't burn a color chit
									if (best.getInfiniteSource()!=null) {
										// Keep best
										set = best;
									}
								}
								best = set;
							}
						}
					}
					if (best!=null) {
						RealmComponent spell = RealmComponent.getRealmComponent(best.getSpell());
						iconGroup = new IconGroup(spell.getIcon(),IconGroup.VERTICAL,4);
						ArrayList<GameObject> types =  best.getValidTypeObjects();
						Speed bestSpeed = null;
						RealmComponent rcType = null;
						for (GameObject type:types) {
							Speed speed = SpellSet.getSpeedForType(type);
							if (bestSpeed==null || speed.fasterThan(bestSpeed)) {
								bestSpeed = speed;
								rcType = RealmComponent.getRealmComponent(type);
							}
						}
						if (rcType!=null) {
							iconGroup.addIcon(rcType.getIcon());
						}
						if (best.getInfiniteSource()==null) {
							MagicChit color = best.getValidColorChits().get(0);
							iconGroup.addIcon(color.getIcon());
						}
						suggestion.append("You should cast an attack spell.");
					}
				}
			}
		}
		return;
	}
	private void testAssign() {
		BattleModel battleModel = combatFrame.getBattleModel();
		if (battleModel.areDenizens()) {
			boolean denizensOnSheets = false;
			boolean unassignedDenizens = false;
			for (Iterator i=battleModel.getDenizenBattleGroup().getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.getTarget()!=null) {
					denizensOnSheets = true;
				}
				else {
					unassignedDenizens = true;
				}
			}
			
			BattleGroup battleGroup = battleModel.getParticipantsBattleGroup(combatFrame.getActiveParticipant());
			for (Iterator i=battleGroup.getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent participant = (RealmComponent)i.next();
				CombatWrapper combat = new CombatWrapper(participant.getGameObject());
				if (combat.isSheetOwner() && participant.getTarget()==null) {
					ArrayList attackers = combat.getAttackersAsComponents();
					if (!attackers.isEmpty()) {
						iconGroup = new IconGroup(participant.getIcon(),IconGroup.VERTICAL,4);
						suggestion.append("You should assign a target for the "+participant.getGameObject().getName());
						suggestion.append("\nfrom those on his/her sheet.");
						break;
					}
					else if (participant.isCharacter()) {
						iconGroup = new IconGroup(participant.getIcon(),IconGroup.VERTICAL,4);
						suggestion.append("You should assign a target for the "+participant.getGameObject().getName());
						suggestion.append(" from");
						if (unassignedDenizens) {
							suggestion.append(" the unassigned denizens panel");
						}
						if (denizensOnSheets) {
							if (unassignedDenizens) {
								suggestion.append(" or");
							}
							suggestion.append(" another combat sheet");
						}
						if (participant.isHidden()) {
							if (combatFrame.getHostPrefs().hasPref(Constants.ADV_AMBUSHES)) {
								suggestion.append(",\nthough you risk becoming unhidden if the ambush fails.");
							}
							else {
								suggestion.append(",\nthough you will become immediately unhidden.");
							}
						}
						else {
							suggestion.append(".");
						}
						break;
					}
				}
			}
		}
		return;
	}
	private void testPosition() {
		if (combatFrame.getActiveCharacterIsHere()) {
			boolean avoidFatigue = false;
			CharacterWrapper activeCharacter = combatFrame.getActiveCharacter();
			Effort currentEffort = BattleUtility.getEffortUsed(activeCharacter);
			CharacterChitComponent cc = (CharacterChitComponent)RealmComponent.getRealmComponent(activeCharacter.getGameObject());
			CombatWrapper combat = new CombatWrapper(cc.getGameObject());
			if (cc.getManeuverChit()==null && combat.getAttackerCount()>0) {
				// Find a move choice
				Collection c = combatFrame.getAvailableManeuverOptions(3,true);
				if (!c.isEmpty()) {
					avoidFatigue = queryAvoidFatigue();
				}
				Speed bestSpeed = null;
				BattleChit best = null;
				for (Iterator i=c.iterator();i.hasNext();) {
					BattleChit bc = (BattleChit)i.next();
					int totalEffort = bc.getGameObject().getThisInt("effort")+currentEffort.getAsterisks();
					if (!avoidFatigue || totalEffort<2) {
						Speed speed = bc.getMoveSpeed();
						if (bestSpeed==null || speed.fasterThan(bestSpeed)) {
							bestSpeed = speed;
							best = bc;
						}
					}
				}
				if (best!=null) {
					RealmComponent rc = (RealmComponent)best;
					iconGroup = new IconGroup(rc.getIcon(),IconGroup.VERTICAL,4);
					suggestion.append("You should play a maneuver to avoid being hit automatically.");
					return;
				}
			}
			else if (cc.getAttackCombatBox()==0 && cc.getTarget()!=null) {
				if (cc.getManeuverChit()==null) {
					suggestion.append("No need to play a maneuver, because there are no attackers on your sheet.");
				}
				// Find an attack choice
				Collection c = combatFrame.getAvailableFightOptions(0);
				if (!c.isEmpty()) {
					avoidFatigue = queryAvoidFatigue();
				}
				boolean ignoreSpeed = false;
				RealmComponent wrc = null;
				GameObject go = cc.getActiveWeaponObject();
				if (go!=null) {
					wrc = RealmComponent.getRealmComponent(go);
					if (wrc.isWeapon()) {
						WeaponChitComponent weapon = (WeaponChitComponent)wrc;
						ignoreSpeed = weapon.getSpeed()!=null;
					}
				}
				Speed bestSpeed = null;
				BattleChit fastest = null;
				Strength bestStrength = null;
				BattleChit strongest = null;
				for (Iterator i=c.iterator();i.hasNext();) {
					BattleChit bc = (BattleChit)i.next();
					int totalEffort = bc.getGameObject().getThisInt("effort")+currentEffort.getAsterisks();
					if (!avoidFatigue || totalEffort<2) {
						if (!ignoreSpeed) {
							Speed speed = bc.getMoveSpeed();
							if (bestSpeed==null || speed.fasterThanOrEqual(bestSpeed)) {
								BattleChit change = bc;
								if (bestSpeed!=null && speed.equalTo(bestSpeed)) {
									if (fastest.getHarm().getStrength().strongerThan(change.getHarm().getStrength())) {
										change = fastest;
									}
								}
								bestSpeed = speed;
								fastest = change;
							}
						}
						Strength strength = bc.getHarm().getStrength();
						if (bestStrength==null || strength.strongerOrEqualTo(bestStrength)) {
							BattleChit change = bc;
							if (bestStrength!=null && strength.equalTo(bestStrength)) {
								if (strongest.getAttackSpeed().fasterThan(change.getAttackSpeed())) {
									change = strongest;
								}
							}
							bestStrength = strength;
							strongest = change;
						}
					}
				}
				if (fastest!=null || strongest!=null) {
					if (fastest!=null && strongest!=null) {
						if (fastest.getAttackSpeed().equalTo(strongest.getAttackSpeed())
								&& fastest.getHarm().getStrength().equalTo(strongest.getHarm().getStrength())) {
							// Same chit
							strongest = null;
						}
					}
					iconGroup = new IconGroup(IconGroup.VERTICAL,4);
					if (wrc!=null) {
						iconGroup.addIcon(wrc.getIcon());
					}
					if (fastest!=null) {
						iconGroup.addIcon(((RealmComponent)fastest).getIcon());
					}
					if (strongest!=null) {
						if (fastest!=null) {
							iconGroup.addIcon(IconFactory.findIcon("icons/or.gif"));
							if (wrc!=null) {
								iconGroup.addIcon(wrc.getIcon());
							}
						}
						iconGroup.addIcon(((RealmComponent)strongest).getIcon());
					}
					if (suggestion.length()>0) {
						suggestion.append("\nHowever, you");
					}
					else {
						suggestion.append("You");
					}
					suggestion.append(" should play an attack to avoid missing your target automatically.");
					return;
				}
			}
		}
	}
}