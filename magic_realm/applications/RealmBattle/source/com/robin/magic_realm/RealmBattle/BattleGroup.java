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

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.DieRollBuilder;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class BattleGroup implements Comparable {

	private static final int HORSE_FLIP = 0;
	private static final int HORSE_WALK = 1;
	private static final int HORSE_GALLOP = 2;
	
	private BattleModel model;
	private RealmComponent owningCharacter;
	private ArrayList<RealmComponent> battleParticipants;
	
	public int size() {
		return battleParticipants.size();
	}
	/**
	 * @param owningCharacter		The owning character (present or not), or null if denizen
	 * @param query					The BattleQuery object to use
	 */
	public BattleGroup(RealmComponent owningCharacter) {
		this.owningCharacter = owningCharacter;
		this.battleParticipants = new ArrayList<RealmComponent>();
	}
	public String toString() {
		return "BattleGroup:"+owningCharacter+":"+battleParticipants.size();
	}
	public boolean foundHiddenEnemies() {
		return false;
	}
	public boolean isDenizen() {
		return owningCharacter==null;
	}
	public void addBattleParticipant(BattleChit bp) {
		if (bp.isDenizen()==isDenizen()) {
			battleParticipants.add((RealmComponent)bp);
		}
		else {
			throw new IllegalArgumentException("RealmComponent does not match group type");
		}
	}
	public ArrayList<RealmComponent> getHirelings() {
		ArrayList<RealmComponent> ret = new ArrayList<RealmComponent>();
		ret.addAll(battleParticipants);
		if (owningCharacter!=null) {
			ret.remove(owningCharacter);
		}
		return ret;
	}
	public ArrayList<RealmComponent> getBattleParticipants() {
		return battleParticipants;
	}
	public boolean contains(RealmComponent rc) {
		return battleParticipants.contains(rc);
	}
	/**
	 * @return	The Character if present in the battle group, or null
	 */
	public CharacterChitComponent getCharacterInBattle() {
		for (RealmComponent bp:battleParticipants) {
			if (bp.isCharacter()) { // only one character per BattleGroup (by definition)
				return (CharacterChitComponent)bp;
			}
		}
		return null; // possible for no character to be present
	}
	/**
	 * @return		The appropriate DieRoller for this BattleGroup
	 */
	public DieRoller createDieRoller(String reason) {
		DieRoller roller;
		RealmComponent bp = getCharacterInBattle();
		if (bp!=null) {
			// Create a roller from the character, if he/she is present
			CharacterWrapper character = new CharacterWrapper(bp.getGameObject());
			roller = DieRollBuilder.getDieRollBuilder(null,character).createRoller(reason);
		}
		else {
			// Create a roller for the clearing only if character is not present
			roller = model.createClearingRoller(2,reason);
		}
		
		return roller;
	}
	public void allHorsesWalk() {
		allHorsesFlip(HORSE_WALK);
	}
	public void allHorsesGallop() {
		allHorsesFlip(HORSE_GALLOP);
	}
	public void allHorsesFlip() {
		allHorsesFlip(HORSE_FLIP);
	}
	private void allHorsesFlip(int type) {
		for (RealmComponent bp:battleParticipants) {
			if (bp.hasHorse()) {
				switch(type) {
					case HORSE_FLIP: // This only affects hirelings
						if (bp.isNative()) {
							bp.getHorse().flip();
						}
						break;
					case HORSE_WALK:	bp.getHorse().setWalk(); break;
					case HORSE_GALLOP:	bp.getHorse().setGallop(); break;
				}
			}
		}
	}
	
	////////////////////////////////////////////////
	// Combat Methods
	////////////////////////////////////////////////
	
	/**
	 * Return true if either the character or any of the hirelings are unhidden
	 */
	public boolean hasAvailableParticipant(RealmComponent attacker) {
		CharacterWrapper character = new CharacterWrapper(owningCharacter.getGameObject());
		if (attacker.isNative()) {
			// Check to see if native is Battling the character
			if (!character.isBattling(attacker.getGameObject())) {
				return false;
			}
		}
		
		if (attacker.isPacifiedBy(character)) {
			return false;
		}
		
		for (RealmComponent bp:battleParticipants) {
			if (!bp.isHidden()
					&& !bp.isMistLike()
					&& !bp.isImmuneTo(attacker)
					&& !hasPinningAttacker(bp)) {
				
				return true;
			}
		}
		return false;
	}
	private boolean hasPinningAttacker(RealmComponent bp) {
		ArrayList<RealmComponent> attackers = model.getAttackersFor(bp);
		for (RealmComponent attacker:attackers) {
			if (attacker.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)attacker;
				if (monster.isPinningOpponent()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns true if the character or any hireling in this battle group can be attacked.
	 */
	public boolean canBeAttackedBy(RealmComponent attacker) {
		CharacterChitComponent cc = getCharacterInBattle();
		if (cc!=null && !cc.isHidden() && !cc.isMistLike() && !cc.isImmuneTo(attacker)) {
			return true;
		}
		for (RealmComponent bp:getBattleParticipants()) {
			if (!bp.isCharacter() && !bp.isHidden() && !bp.isMistLike()) {
				return true;
			}
		}
		return false;
	}
	public RealmComponent getAvailableParticipant(RealmComponent attacker) {
		// First, search for the character, and return if not hidden.
		RealmComponent character = getCharacterInBattle();
		if (character!=null && !character.isHidden() && !character.isMistLike()) {
			// Make sure there is not a demon immunity thing
			if (!character.isImmuneTo(attacker)) {
				return character;
			}
		}
		
		// Character not found/unhidden?  Find all unhidden hirelings, and query character
		ArrayList unhiddenHirelings = new ArrayList();
		for (Iterator i=getBattleParticipants().iterator();i.hasNext();) {
			RealmComponent bp = (RealmComponent)i.next();
			if (!bp.isCharacter() && !bp.isHidden()) {
				// Make sure the hireling isn't already fighting a RED-side-up monster
				if (!hasPinningAttacker(bp)) {
					unhiddenHirelings.add(bp);
				}
			}
		}
		
		if (unhiddenHirelings.isEmpty()) {
			return attacker;
		}
		else if (unhiddenHirelings.size()==1) { // its obvious if only one hireling
			return (RealmComponent)unhiddenHirelings.get(0);
		}
		
		return null;  // This indicates that the character must pick an unhidden hireling
	}
	
	/**
	 * @return Returns the owningCharacter.
	 */
	public RealmComponent getOwningCharacter() {
		return owningCharacter;
	}
	public boolean isCharacterInBattle() {
		return getCharacterInBattle()!=null;
	}
	public BattleModel getModel() {
		return model;
	}
	public void setModel(BattleModel model) {
		this.model = model;
	}
	public boolean equals(Object arg0) {
		if (arg0 instanceof BattleGroup) {
			BattleGroup bg = (BattleGroup)arg0;
			return owningCharacter.getGameObject().equals(bg.owningCharacter.getGameObject());
		}
		return false;
	}
	public int compareTo(Object arg0) {
		int ret = 0;
		if (arg0 instanceof BattleGroup) {
			BattleGroup bg = (BattleGroup)arg0;
			ret = owningCharacter.getGameObject().getName().compareTo(bg.owningCharacter.getGameObject().getName());
		}
		return ret;
	}
}