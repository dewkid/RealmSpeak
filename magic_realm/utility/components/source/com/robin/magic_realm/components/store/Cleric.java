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
package com.robin.magic_realm.components.store;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.TravelerChitComponent;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Cleric extends Store {
	
	private CharacterWrapper character;
	private double gold;
	private boolean needsHeal;
	private boolean needsCurseRemoval;
	private boolean needsCursedItemRemoval;
	
	public Cleric(TravelerChitComponent traveler, CharacterWrapper character) {
		super(traveler);
		this.character = character;
		setupStore();
	}
	private void setupStore() {
		gold = character.getGold();
		
		int wounds = character.getWoundedChits().size();
		int fatigue = character.getFatiguedChits().size();
		needsHeal = wounds>0 || fatigue>0;
		needsCurseRemoval = character.hasCurses();
		needsCursedItemRemoval = character.getAllCursedStuff().size()>0;
		
		int minCost = needsHeal?10:5;
		if (!needsHeal && !needsCurseRemoval && !needsCursedItemRemoval) {
			reasonStoreNotAvailable = "You have no need of the "+getTraderName()+"'s services at this time.";
		}
		else if (minCost>gold) {
			reasonStoreNotAvailable = "You cannot afford the "+getTraderName()+"'s services at this time.";
		}
	}
	
	public String doService(JFrame frame) {
		RealmComponent rc = RealmComponent.getRealmComponent(character.getGameObject());
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Which Service?",true);
		if (needsHeal) {
			chooser.addOption("heal","Heal (10 gold)");
			chooser.addRealmComponentToOption("heal",rc);
		}
		if (needsCurseRemoval) {
			for (String curse:character.getAllCurses()) {
				chooser.addOption(curse,"Remove "+curse+" Curse (5 gold)");
				chooser.addRealmComponentToOption(curse,rc);
			}
		}
		if (needsCursedItemRemoval) {
			for (GameObject thing:character.getAllCursedStuff()) {
				chooser.addOption(thing.getStringId(),"Remove & Destroy "+thing.getName()+" (5 gold)");
				chooser.addRealmComponentToOption(thing.getStringId(),RealmComponent.getRealmComponent(thing));
			}
		}
		chooser.setVisible(true);
		String optionKey = chooser.getSelectedOptionKey();
		if (optionKey!=null) {
			if ("heal".equals(optionKey)) {
				character.addGold(-10);
				character.doHeal();
				return "Completely healed the "+character.getGameObject().getName();
			}
			else if (character.getAllCurses().contains(optionKey)) {
				character.addGold(-5);
				character.removeCurse(optionKey);
				return "Removed the "+optionKey+" curse.";
			}
			else {
				character.addGold(-5);
				for (GameObject thing:character.getAllCursedStuff()) {
					if (optionKey.equals(thing.getStringId())) {
						TreasureUtility.removeCursedItem(character,thing);
						return "The "+thing.getName()+" is destroyed by the Cleric";
					}
				}
			}
		}
		return null;
	}
}