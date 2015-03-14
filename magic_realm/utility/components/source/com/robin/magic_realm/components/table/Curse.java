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

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Curse extends RealmTable {
	
	public static final String KEY = "Curse";
	
	private boolean harm;
	private boolean cursed;
	
	public Curse(JFrame frame) {
		super(frame,null);
	}
	public boolean harmWasApplied() {
		return harm;
	}
	public String getTableName(boolean longDescription) {
		return "Curse!";
	}
	public String getTableKey() {
		return KEY;
	}
	public String getDestClientName(GameObject target) {
		RealmComponent targetRc = RealmComponent.getRealmComponent(target);
		// Determine the destination client
		RealmComponent destOwner = targetRc.getOwner();
		// destOwner should NOT be null at this point!
		CharacterWrapper destCharacter = new CharacterWrapper(destOwner.getGameObject());
		return destCharacter.getPlayerName();
	}
	public String apply(CharacterWrapper character,DieRoller roller) {
		harm = false; // by default
		if (character.isCharacter() && !character.isMistLike() && !character.hasMagicProtection()) {
			cursed = false;
			String result = super.apply(character,roller);
			if (!cursed) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is hit with "+result+", but it has no effect!");
				result = result + ", but not affected.";
			}
			return result;
		}
		sendMessage(character.getGameObject().getGameData(),
				getDestClientName(character.getGameObject()),
				getCurseTitle(character),
				"The "+character.getCharacterName()+" is hit with a curse, but it has no effect!");
		return "Unaffected";
	}
	private String getCurseTitle(CharacterWrapper character) {
		return character.getGameObject().getName()+"'s Curse!";
	}
	public String applyOne(CharacterWrapper character) {
		if (!character.hasCurse(Constants.EYEMIST) && !character.immuneToCurses()) { // only inform them if they don't already have the curse
			cursed = true;
			// No search activity, except enhanced PEER
			character.applyCurse(Constants.EYEMIST);
			if (getParentFrame()!=null) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is cursed with "+Constants.EYEMIST+", and cannot do searches.");
			}
		}
		return "Eyemist";
	}

	public String applyTwo(CharacterWrapper character) {
		if (!character.hasCurse(Constants.SQUEAK) && !character.immuneToCurses()) { // only inform them if they don't already have the curse
			cursed = true;
			// Cannot be hidden
			character.applyCurse(Constants.SQUEAK);
			
			// Unhide character if hidden
			if (character.isHidden()) {
				character.setHidden(false);
			}
			if (getParentFrame()!=null) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is cursed with "+Constants.SQUEAK+", and cannot be hidden.");
			}
		}
		return "Squeak";
	}

	public String applyThree(CharacterWrapper character) {
		if (!character.hasCurse(Constants.WITHER) && !character.immuneToCurses()) { // only inform them if they don't already have the curse
			cursed = true;
			// Cannot have ANY active effort asterisks
			character.applyCurse(Constants.WITHER);
			
			ArrayList toFatigue = new ArrayList();
			toFatigue.addAll(character.getActiveEffortChits());
			toFatigue.addAll(character.getAlertedChits());
			toFatigue.addAll(character.getColorChits());
			
			// Fatigue all active effort asterisks here
			for (Iterator i=toFatigue.iterator();i.hasNext();) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
				chit.makeFatigued();
				harm = true;
			}
			if (getParentFrame()!=null) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is cursed with "+Constants.WITHER+", and cannot have any active effort chits.");
			}
		}
		return "Wither";
	}

	public String applyFour(CharacterWrapper character) {
		if (!character.hasCurse(Constants.ILL_HEALTH) && !character.immuneToCurses()) { // only inform them if they don't already have the curse
			cursed = true;
			// Cannot REST
			character.applyCurse(Constants.ILL_HEALTH);
			if (getParentFrame()!=null) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is cursed with "+Constants.ILL_HEALTH+", and cannot rest.");
			}
		}
		return "Ill Health";
	}

	public String applyFive(CharacterWrapper character) {
		if (!character.hasCurse(Constants.ASHES) && !character.immuneToCurses()) { // only inform them if they don't already have the curse
			cursed = true;
			// GOLD is worthless - can add to it, but not subtract
			character.applyCurse(Constants.ASHES);
			if (getParentFrame()!=null) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is cursed with "+Constants.ASHES+", making GOLD worthless.");
			}
		}
		return "Ashes";
	}

	public String applySix(CharacterWrapper character) {
		if (!character.hasCurse(Constants.DISGUST) && !character.immuneToCurses()) { // only inform them if they don't already have the curse
			cursed = true;
			// FAME is worthless - can add to it, but not subtract
			character.applyCurse(Constants.DISGUST);
			if (getParentFrame()!=null) {
				sendMessage(character.getGameObject().getGameData(),
						getDestClientName(character.getGameObject()),
						getCurseTitle(character),
						"The "+character.getCharacterName()+" is cursed with "+Constants.DISGUST+", making FAME worthless.");
			}
		}
		return "Disgust";
	}
	public static Curse doNow(JFrame parent,GameObject attacker,GameObject target) {
		Curse curse = new Curse(parent);
		CharacterWrapper victim = new CharacterWrapper(target);
		// Use the "victim" here instead of the caster, because the victim is the one rolling for the curse (coming from an Imp!!)
		DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,victim).createRoller(curse);
		String result = curse.apply(victim,roller);
		RealmLogging.logMessage(target.getName(),"Cursed with "+result);
		return curse;
	}
}