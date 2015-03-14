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

import java.util.ArrayList;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Merchant extends Store {

	private CharacterWrapper character;
	private ArrayList<GameObject> itemsToSell;
	
	public Merchant(TravelerChitComponent traveler,CharacterWrapper character) {
		super(traveler);
		this.character = character;
		setupStore();
	}
	private void setupStore() {
		itemsToSell = new ArrayList<GameObject>();
		for (GameObject go:character.getSellableInventory()) {
			itemsToSell.add(go);
		}
		if (itemsToSell.isEmpty()) {
			reasonStoreNotAvailable = "You have no items to sell.";
		}
	}

	public String doService(JFrame frame) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Sell which?",true);
		for (GameObject go:itemsToSell) {
			int basePrice = TreasureUtility.getBasePrice(null,RealmComponent.getRealmComponent(go));
			chooser.addGameObject(go,"Sell for "+basePrice+" gold");
		}
		chooser.setVisible(true);
		if (chooser.getSelectedText()!=null) {
			RealmComponent rc = chooser.getFirstSelectedComponent();
			GameObject thing = rc.getGameObject();
			if (thing.hasThisAttribute(Constants.ACTIVATED)) {
				if (!TreasureUtility.doDeactivate(frame,character,thing)) {
					return "You were unable to deactivate the "+thing.getName(); // This should never happen.
				}
			}
			
			int basePrice = TreasureUtility.getBasePrice(null,rc);
			character.addGold(basePrice);
			
			if (rc.isArmor()) {
				// Make sure armor is no longer damaged
				ArmorChitComponent armor = (ArmorChitComponent)rc;
				armor.setActivated(false);
				armor.setIntact(true);
			}
			
			GameObject dwelling = getRandomDwelling();
			thing.removeThisAttribute(Constants.DEAD);
			dwelling.add(thing);
			RealmLogging.logMessage(
					character.getGameObject().getName(),
					"Sold "+thing.getName()+" regenerates at the "+dwelling.getName()+".");
			return "Sold the "+thing.getName()+" to the "+getTraderName();
		}
		return null;
	}
	private GameObject getRandomDwelling() {
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(character.getGameData());
		ArrayList<GameObject> dwellings = new ArrayList<GameObject>(); 
		for(GameObject go:rom.getDwellingObjects()) {
			if (!go.hasThisAttribute("general_dwelling")) { // exclude s_fire, l_fire, and hut!
				dwellings.add(go);
			}
		}
		int r = RandomNumber.getRandom(dwellings.size());
		return dwellings.get(r);
	}
}