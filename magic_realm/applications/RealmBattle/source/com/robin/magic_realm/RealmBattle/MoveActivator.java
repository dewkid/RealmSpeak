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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class MoveActivator {
	
	public enum MoveActionResult {
		NO_MOVE_POSSIBLE,
		SUCCESSFUL,
		UNSUCCESSFUL,
	};
	
	public static final String FLIP_SIDE_TEXT = "(back)";
	
	private CombatFrame combatFrame;
	private BattleModel battleModel;
	private RealmComponent activeParticipant;
	private CharacterWrapper activeCharacter;
	private HostPrefWrapper hostPrefs;
	
	private RealmComponent selectedMoveChit;
	
	private Fly fly = null;
	private ArrayList attackers;
	
	public MoveActivator(CombatFrame combatFrame) {
		this.combatFrame = combatFrame;
		battleModel = combatFrame.getBattleModel();
		activeParticipant = combatFrame.getActiveParticipant();
		activeCharacter = combatFrame.getActiveCharacter();
		hostPrefs = combatFrame.getHostPrefs();
	}
	public ArrayList getAttackers() {
		return attackers;
	}
	public RealmComponent getSelectedMoveChit() {
		return selectedMoveChit;
	}
	public Fly getFly() {
		return fly;
	}
	public boolean isFly() {
		return fly!=null;
	}
	/**
	 * Takes a collection of BattleChits, and returns only the flyers.
	 */
	private Collection filterFlyers(Collection in) {
		ArrayList list = new ArrayList();
		for (Iterator i=in.iterator();i.hasNext();) {
			BattleChit chit = (BattleChit)i.next();
			RealmComponent rc = (RealmComponent)chit;
			RealmComponent target = chit.getTarget();
			if (target!=null && target.equals(activeParticipant)) {
				// As long as the character is not immune to this monster type, include it
				if (!activeParticipant.isImmuneTo(rc)) {
					Speed speed = chit.getFlySpeed();
					if (speed!=null) {
						list.add(rc);
					}
				}
			}
		}
		return list;
	}
	private Collection getFlyingAttackersOnActive() {
		return filterFlyers(battleModel.getAllBattleParticipants(true));
	}
	private Speed getFastestAttackerFlySpeed() {
		Collection c = getFlyingAttackersOnActive();
		// Find fastest attacker fly speed on your sheet
		Speed fastest = new Speed(); // infinitely slow
		for (Iterator i=c.iterator();i.hasNext();) {
			BattleChit chit = (BattleChit)i.next();
			Speed speed = chit.getFlySpeed();
			if (speed!=null && speed.fasterThan(fastest)) {
				fastest = speed;
			}
		}
		return fastest;
	}
	public Speed getFastestAttackerMoveSpeed() {
		// Find fastest attacker move speed on your sheet
		Speed fastest = new Speed(); // infinitely slow
		for (Iterator i=battleModel.getAllBattleParticipants(true).iterator();i.hasNext();) {
			BattleChit chit = (BattleChit)i.next();
			RealmComponent rc = (RealmComponent)chit;
			RealmComponent target = chit.getTarget();
			if (target!=null && target.equals(activeParticipant)) {
				// As long as the character is not immune to this monster type, include it
				if (!activeParticipant.isImmuneTo(rc)) {
					Speed speed = chit.getMoveSpeed();
					if (speed.fasterThan(fastest)) {
						fastest = speed;
					}
				}
			}
		}
		
		// Don't forget to check charge chits!!
		if (activeParticipant.isCharacter()) {
			CombatWrapper combat = new CombatWrapper(activeParticipant.getGameObject());
			for (Iterator i=combat.getChargeChits().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc.isActionChit()) {
					CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
					Speed moveSpeed = chit.getMoveSpeed();
					if (moveSpeed.fasterThan(fastest)) {
						fastest = moveSpeed;
					}
				}
				else if (rc.isFlyChit()) {
					FlyChitComponent flyChit = (FlyChitComponent)rc;
					Speed flySpeed = flyChit.getSpeed();
					if (flySpeed.fasterThan(fastest)) {
						fastest = flySpeed;
					}
				}
			}
		}
		return fastest;
	}
	public MoveActionResult playedValidMoveChit(String title,String noMoveMessage) {
		return playedValidMoveChit(title,noMoveMessage,true);
	}
	public MoveActionResult playedValidMoveChit(String title,String noMoveMessage,boolean checkStumble) {
		// Find fastest attacker move speed on your sheet
		attackers = battleModel.getAttackersFor(activeParticipant);
		Speed fastest = getFastestAttackerMoveSpeed();
		
		// Also check charge chits (if any)
		CombatWrapper chargeCombat = new CombatWrapper(activeParticipant.getGameObject());
		Collection chargeChits = chargeCombat.getChargeChits();
		for (Iterator i=chargeChits.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			attackers.add(rc);
			if (rc.isActionChit()) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				Speed moveSpeed = chit.getMoveSpeed();
				if (moveSpeed.fasterThan(fastest)) {
					fastest = moveSpeed;
				}
			}
			else if (rc.isFlyChit()) {
				FlyChitComponent flyChit = (FlyChitComponent)rc;
				Speed flySpeed = flyChit.getSpeed();
				if (flySpeed.fasterThan(fastest)) {
					fastest = flySpeed;
				}
			}
		}
		
		// Find all playable options
		Speed speedToBeat = hostPrefs.hasPref(Constants.OPT_STUMBLE)?new Speed():fastest; // Stumble allows any move chit
		Collection moveSpeedOptions = activeCharacter.getMoveSpeedOptions(speedToBeat,true,true);
		Collection availableManeuverOptions = combatFrame.getAvailableManeuverOptions(0,true); // if running away, then the red-side-up check has already been done
		moveSpeedOptions.retainAll(availableManeuverOptions); // Intersection between the two
		
		// Check for flying possibilities
		ArrayList<StrengthChit> flyChits = activeCharacter.getFlyStrengthChits(false);
		
		Speed fastestFlyer = null;
		if (!flyChits.isEmpty()) {
			fastestFlyer = getFastestAttackerFlySpeed();
			Speed flyingSpeedToBeat = hostPrefs.hasPref(Constants.OPT_STUMBLE)?new Speed():fastestFlyer;
			Strength needed = activeCharacter.getNeededSupportWeight();
			// Filter out those flyChits that aren't strong enough or fast enough
			for (StrengthChit sc:flyChits) {
				if (sc.getSpeed().fasterThan(flyingSpeedToBeat) && sc.getStrength().strongerOrEqualTo(needed)) {
					RealmComponent rc = sc.getRealmComponent();
					if (rc.isMonster()) {
						rc = ((MonsterChitComponent)rc).getMoveChit();
					}
					if (!moveSpeedOptions.contains(rc)) {
						moveSpeedOptions.add(rc);
					}
				}
			}
		}
		
		if (moveSpeedOptions.size()>0) {
			if (hostPrefs.hasPref(Constants.OPT_RIDING_HORSES)) {
				// Check for a horse in the move options.  If there is one, that's the ONLY option!
				for (Iterator i=moveSpeedOptions.iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					if (rc.isHorse()) {
						moveSpeedOptions = new ArrayList();
						moveSpeedOptions.add(rc);
						break;
					}
				}
			}
			
			// Choose one
			RealmComponentOptionChooser chooser = getChooserForMoveOptions(combatFrame,activeCharacter,moveSpeedOptions,true);
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				selectedMoveChit = chooser.getFirstSelectedComponent();
				CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
				
				String key = chooser.getSelectedOptionKey();
				if (selectedMoveChit.isHorse()) {
					SteedChitComponent horse = (SteedChitComponent)selectedMoveChit;
					if (key.endsWith("F")) {
						horse.flip();
					}
					if (horse.isGalloping()) {
						combat.setGalloped(true);
					}
				}
				
				fly = null;
				if (Fly.valid(selectedMoveChit)) {
					fly = new Fly(selectedMoveChit);
				}
				
				if (!selectedMoveChit.isHorse()) {
					combat.addUsedChit(selectedMoveChit.getGameObject());
				}
				
				if (fly!=null) {
					// Flying away?  Make some adjustments here...
					fastest = fastestFlyer;
					attackers = new ArrayList(filterFlyers(attackers));
				}
					
				if (checkStumble &&!fastest.isInfinitelySlow() && hostPrefs.hasPref(Constants.OPT_STUMBLE)) {
					// Running might NOT be a success...
					Speed speed = fly!=null?fly.getSpeed():BattleUtility.getMoveSpeed(selectedMoveChit);
					
					int stumbleModifier = speed.getNum()-fastest.getNum();
					
					// Include all attackers, EXCEPT monster weapons
					for (Iterator i=attackers.iterator();i.hasNext();) {
						RealmComponent attacker = (RealmComponent)i.next();
						if (!attacker.isMonsterPart()) {
							stumbleModifier++;
						}
					}
					
					DieRoller runAwayRoll = DieRollBuilder.getDieRollBuilder(combatFrame,activeCharacter).createRoller("stumble");
					runAwayRoll.addModifier(stumbleModifier);
					combatFrame.setRunAwayRoll(runAwayRoll);
					combatFrame.madeChanges();
					boolean success = runAwayRoll.getHighDieResult()<7;
					if (!success) {
						return MoveActionResult.UNSUCCESSFUL;
					}
				}
				return MoveActionResult.SUCCESSFUL;
			}
		}
		else {
			JOptionPane.showMessageDialog(combatFrame,noMoveMessage,title,JOptionPane.INFORMATION_MESSAGE);
		}
		return MoveActionResult.NO_MOVE_POSSIBLE;
	}
	public void prepareFatigue() {
		// Prepare fatigue
		Effort effortUsed = BattleUtility.getEffortUsed(activeCharacter);
		int free = activeCharacter.getEffortFreeAsterisks();
		int runAwayFatigue = effortUsed.getNeedToFatigue(free);
		combatFrame.setRunAwayFatigue(runAwayFatigue);
		if (runAwayFatigue>0) {
			// Make sure that the tile is marked, so that combat can be extended if necessary
			CombatWrapper tileCombat = new CombatWrapper(battleModel.getBattleLocation().tile.getGameObject());
			tileCombat.setWasFatigue(true);
		}
	}
	public static RealmComponentOptionChooser getChooserForMoveOptions(JFrame frame,CharacterWrapper activeCharacter,Collection moveOptions,boolean includeHorseFlip) {
		CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
		boolean canGallop = !combat.hasGalloped();
		Strength heaviestInv = activeCharacter.getNeededSupportWeight();
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Select Maneuver:",true);
		int keyN = 0;
		for (Iterator i=moveOptions.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			String key = "C"+(keyN++);
			if (includeHorseFlip && rc.isHorse()) {
				SteedChitComponent horse = (SteedChitComponent)rc;
				String flipKey = key+"F";
				
				// This complication is to make sure they appear in the same order, and that they are strong enough
				if (horse.isTrotting()) {
					if (horse.getTrotStrength().strongerOrEqualTo(heaviestInv)) {
						chooser.addOption(key,"");
						chooser.addRealmComponentToOption(key,rc);
					}
					if (canGallop && horse.getGallopStrength().strongerOrEqualTo(heaviestInv)) {
						chooser.addOption(flipKey,FLIP_SIDE_TEXT);
						chooser.addRealmComponentToOption(flipKey,rc,RealmComponentOptionChooser.DisplayOption.Flipside);
					}
				}
				else {
					if (horse.getTrotStrength().strongerOrEqualTo(heaviestInv)) {
						chooser.addOption(flipKey,FLIP_SIDE_TEXT);
						chooser.addRealmComponentToOption(flipKey,rc,RealmComponentOptionChooser.DisplayOption.Flipside);
					}
					if (canGallop && horse.getGallopStrength().strongerOrEqualTo(heaviestInv)) {
						chooser.addOption(key,"");
						chooser.addRealmComponentToOption(key,rc);
					}
				}
			}
			else {
				chooser.addOption(key,"");
				chooser.addRealmComponentToOption(key,rc);
			}
		}
		return chooser;
	}
}