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

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class PowerOfThePit extends RealmTable {
	
	public static final String KEY = "PowerOfThePit";
	private static final String[] RESULT = {
		"Fiery Chasm Opens",
		"Carried Away",
		"Terror",
		"Blight",
		"Forget",
		"Rust",
	};
	
	private GameObject caster;
	private boolean makeDeadWhenKilled = false; // If PoP is called outside of combat (ie., looting), this should be set to true
	
	private ArrayList<GameObject> kills;
	private boolean harm;
	
	public PowerOfThePit(JFrame frame,GameObject caster) {
		super(frame,null);
		this.caster = caster;
		kills = new ArrayList<GameObject>();
	}
	public boolean harmWasApplied() {
		return kills.size()>0 || harm;
	}
	public String getTableName(boolean longDescription) {
		return "Power of the Pit";
	}
	public String getTableKey() {
		return KEY;
	}
	public void setMakeDeadWhenKilled(boolean makeDeadWhenKilled) {
		this.makeDeadWhenKilled = makeDeadWhenKilled;
	}
	public String getDestClientName(GameObject attacker,GameObject target) {
		RealmComponent attackerRc = RealmComponent.getRealmComponent(attacker);
		RealmComponent targetRc = RealmComponent.getRealmComponent(target);
		// Determine the destination client
		RealmComponent destOwner = attackerRc.getOwner();
		if (destOwner==null) {
			destOwner = targetRc.getOwner();
		}
		// destOwner should NOT be null at this point!  One or the other HAS to be owned
		CharacterWrapper destCharacter = new CharacterWrapper(destOwner.getGameObject());
		return destCharacter.getPlayerName();
	}
	public String apply(CharacterWrapper character,DieRoller roller) {
		harm = false;
//System.out.println("PowerOfThePit:  REMOVE THIS CODE");
//roller.setValue(0,3);if (roller.getNumberOfDice()>1) roller.setValue(1,3);
		if (character.isMistLike()) {
			return "Unaffected - Mist";
		}
		else if (character.hasMagicProtection()) {
			return "Unaffected - Magic Protection";
		}
		else if (character.hasActiveInventoryThisKey(Constants.ABSORB_POP)) {
			GameObject go = character.getActiveInventoryThisKey(Constants.ABSORB_POP);
			go.removeThisAttribute(Constants.ACTIVATED);
			character.getGameObject().remove(go);
			RealmLogging.logMessage(character.getGameObject().getName(),go.getName()+" absorbs Power of the Pit attack!");
			RealmLogging.logMessage(character.getGameObject().getName(),go.getName()+" vanishes.");
			return "Unaffected - "+go.getName();
		}
		return super.apply(character,roller);
	}
	private String getKilledString(ArrayList<RealmComponent> killed) {
		StringBuffer string = new StringBuffer();
		if (!killed.isEmpty()) {
			string.append("\n\n");
			for (RealmComponent rc:killed) {
				string.append("    ");
				string.append(rc.getGameObject().getName());
				string.append(" was killed.\n");
			}
		}
		return string.toString();
	}
	public String applyOne(CharacterWrapper character) {
		// All unhidden characters, natives, and monsters in the clearing are killed.  Visitors, and hidden
		// characters, natives, and monsters are unaffected.
		String destClientName = getDestClientName(caster,character.getGameObject()); // Get this before killing anybody!
		ArrayList<RealmComponent> killed = killEverythingInClearing(character,new Strength("RED"),true,false);
		
		StringBuffer message = new StringBuffer();
		message.append("Fiery Chasm Opens\n\n");
		message.append("All unhidden characters, natives, and monsters in the clearing are killed.\n");
		message.append("Visitors, and hidden characters, natives, and monsters are unaffected");
		message.append(getKilledString(killed));
		
		sendMessage(character.getGameObject().getGameData(),
				destClientName,
				"Power of the Pit",
				message.toString());
		
		return RESULT[0];
	}

	public String applyTwo(CharacterWrapper character) {
		sendMessage(character.getGameObject().getGameData(),
				getDestClientName(caster,character.getGameObject()),
				"Power of the Pit",
				"Carried Away\n\n"
				+"The target is instantly killed.\n\n     "+character.getGameObject().getName()+" was killed.");
		// The target is instantly killed.
		kill(character.getGameObject());
		return RESULT[1];
	}

	public String applyThree(CharacterWrapper character) {
		String destClientName = getDestClientName(caster,character.getGameObject()); // Get this before killing anybody!
		
		// All Light and Medium Monsters, Natives, and Horses in the clearing are killed.
		ArrayList<RealmComponent> killed = killEverythingInClearing(character,new Strength("H"),false,true);
		
		// Each character in the clearing must wound all Light and Medium MOVE/FIGHT chits.
		TileLocation tl = character.getCurrentLocation();
		if (tl.hasClearing() && !tl.isBetweenClearings()) {
			for (RealmComponent rc:tl.clearing.getClearingComponents()) {
				if (rc.isCharacter() && !rc.isMistLike()) {
					CharacterWrapper aChar = new CharacterWrapper(rc.getGameObject());
					boolean hasAtLeastOneGoodChit = aChar.isTransmorphed();
					for (CharacterActionChitComponent chit:aChar.getAllChits()) {
						if (chit.isMove() || chit.isFight()) {
							String str = chit.getGameObject().getThisAttribute("strength");
							if ("L".equals(str) || "M".equals(str)) {
								if (!chit.isWounded()) {
									chit.makeWounded();
									harm = true;
								}
							}
						}
						if (!chit.isWounded()) {
							hasAtLeastOneGoodChit = true;
						}
					}
					if (!hasAtLeastOneGoodChit) {
						kill(rc.getGameObject());
						killed.add(rc);
					}
				}
			}
		}
		
		StringBuffer message = new StringBuffer();
		message.append("Terror\n\n");
		message.append("Each character in the clearing must wound all Light and Medium MOVE/FIGHT chits.\n");
		message.append("All Light and Medium Monsters, Natives, and Horses in the clearing are killed.");
		message.append(getKilledString(killed));
		
		sendMessage(character.getGameObject().getGameData(),
				destClientName,
				"Power of the Pit",
				message.toString());
		
		return RESULT[2];
	}

	public String applyFour(CharacterWrapper character) {
		String destClientName = getDestClientName(caster,character.getGameObject()); // Get this before killing anybody!
		boolean hasChits = character.isCharacter() && !character.isTransmorphed();
		boolean hasAtLeastOneGoodChit = false;
		for (CharacterActionChitComponent chit:character.getAllChits()) {
			if (chit.getEffortAsterisks()>0 && !chit.isWounded()) {
				chit.makeWounded();
				harm = true;
			}
			if (!chit.isWounded()) {
				hasAtLeastOneGoodChit = true;
			}
		}
		StringBuffer message = new StringBuffer();
		message.append("Blight\n\n");
		message.append("All of the target�s active chits that show effort asterisks become wounded.\n");
		message.append("Chits that are already fatigued or that show no asterisks are not affected.");
		if (hasChits && !hasAtLeastOneGoodChit) {
			kill(character.getGameObject());
			ArrayList<RealmComponent> killed = new ArrayList<RealmComponent>();
			killed.add(RealmComponent.getRealmComponent(character.getGameObject()));
			message.append(getKilledString(killed));
		}
		sendMessage(character.getGameObject().getGameData(),
				destClientName,
				"Power of the Pit",
				message.toString());
		return RESULT[3];
	}

	public String applyFive(CharacterWrapper character) {
		sendMessage(character.getGameObject().getGameData(),
				getDestClientName(caster,character.getGameObject()),
				"Power of the Pit",
				"Forget\n\n"
				+"All of the target's active MAGIC chits become fatigued.");
		
		// All of the target's active MAGIC chits become fatigued
		for (Iterator i=character.getActiveMagicChits().iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			if (!chit.isFatigued()) {
				chit.makeFatigued();
				harm = true;
			}
		}
		return RESULT[4];
	}

	public String applySix(CharacterWrapper character) {
		sendMessage(character.getGameObject().getGameData(),
				getDestClientName(caster,character.getGameObject()),
				"Power of the Pit",
				"Rust\n\n"
				+"All of the target�s active armor counters suffer damage. Intact armor counters\n"
				+"become damaged, damaged armor counters are destroyed. Armor cards and inactive\n"
				+"counters are not affected.");
		// The target's active armor counters are damaged.  Armor cards and inactive counters are NOT affected.
		ArrayList<GameObject> destroyed = new ArrayList<GameObject>();
		for (GameObject inv:character.getActiveInventory()) {
			RealmComponent rc = RealmComponent.getRealmComponent(inv);
			if (rc.isArmor()) {
				ArmorChitComponent armor = (ArmorChitComponent)rc;
				if (armor.isDamaged()) {
					if (makeDeadWhenKilled) {
						destroyed.add(inv);
					}
					else {
						CombatWrapper combat = new CombatWrapper(inv);
						combat.setKilledBy(caster);
					}
				}
				else {
					armor.setIntact(false);
				}
				harm = true;
			}
		}
		for (GameObject thing:destroyed) {
			TreasureUtility.handleDestroyedItem(character,thing);
		}
		return RESULT[5];
	}
	private ArrayList<RealmComponent> killEverythingInClearing(CharacterWrapper character,Strength power,boolean hiddenAreSafe,boolean charactersAreSafe) {
		ArrayList<RealmComponent> killed = new ArrayList<RealmComponent>();
		TileLocation tl = character.getCurrentLocation();
		if (tl.isInClearing()) {
//System.out.println("killEverythingInClearing="+tl);
			HashSet<RealmComponent> livingThings = new HashSet<RealmComponent>();
			for (RealmComponent rc:tl.clearing.getClearingComponents()) {
				if (rc.isPlayerControlledLeader()) {
					livingThings.add(rc);
					CharacterWrapper aChar = new CharacterWrapper(rc.getGameObject());
					livingThings.addAll(aChar.getFollowingHirelings());
				}
				if (rc.isNative() || rc.isHorse() || rc.isMonster()) {
					livingThings.add(rc);
				}
			}
			for (RealmComponent rc:livingThings) {
				if (!rc.isMistLike()) {
					if (!hiddenAreSafe || !rc.isHidden()) {
						Strength strength = new Strength(rc.getGameObject().getThisAttribute("vulnerability"));
						if (rc.isCharacter()) {
							CharacterChitComponent achar = (CharacterChitComponent)rc;
							MonsterChitComponent transform = achar.getTransmorphedComponent();
							if (transform!=null) {
								strength = new Strength(transform.getGameObject().getThisAttribute("vulnerability"));
							}
							else if (charactersAreSafe) {
								strength = new Strength("X");
							}
						}
						if (power.strongerThan(strength)) {
							kill(rc.getGameObject());
							killed.add(rc);
						}
					}
				}
			}
		}
		return killed;
	}
	private void kill(GameObject go) {
		RealmComponent attacker =RealmComponent.getRealmComponent(caster); 
		RealmComponent victim = RealmComponent.getRealmComponent(go);
		BattleUtility.handleSpoilsOfWar(attacker,victim);
		
		kills.add(go);
		
		if (makeDeadWhenKilled) {
			RealmUtility.makeDead(RealmComponent.getRealmComponent(go));
		}
		else {
			CombatWrapper combat = new CombatWrapper(go);
			combat.setKilledBy(caster);
			CombatWrapper tile = new CombatWrapper(victim.getCurrentLocation().tile.getGameObject());
			tile.addHitResult();
		}
	}
	public ArrayList<GameObject> getKills() {
		return kills;
	}
	public static PowerOfThePit doNow(JFrame parent,GameObject attacker,GameObject target,boolean casterRolls,int redDie) {
		PowerOfThePit pop = new PowerOfThePit(parent,attacker);
		pop.setMakeDeadWhenKilled(false);
		CharacterWrapper caster = new CharacterWrapper(attacker);
		CharacterWrapper victim = new CharacterWrapper(target);
		DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,casterRolls?caster:victim,redDie).createRoller(pop);
		String result = pop.apply(victim,roller);
		RealmLogging.logMessage(caster.getGameObject().getName(),"Power of the Pit roll: "+roller.getDescription());
		RealmLogging.logMessage(caster.getGameObject().getName(),"Power of the Pit result: "+result);
		return pop;
	}
}