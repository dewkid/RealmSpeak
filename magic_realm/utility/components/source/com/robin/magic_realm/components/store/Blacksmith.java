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
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Blacksmith extends Store {
	
	private CharacterWrapper character;
	private double gold;
	private ArrayList<ArmorChitComponent> repairableArmor;
	
	public Blacksmith(TravelerChitComponent traveler,CharacterWrapper character) {
		super(traveler);
		this.character = character;
		setupStore();
	}
	private void setupStore() {
		if (character.hasCurse(Constants.ASHES)) {
			reasonStoreNotAvailable = "The "+getTraderName()+" does not like your ASHES curse!";
			return;
		}
		gold = character.getGold();
		repairableArmor = new ArrayList<ArmorChitComponent>();
		boolean cannotAfford = false;
		ArrayList<String> distinctNames = new ArrayList<String>(); // so same type isn't shown more than once
		for(GameObject go:character.getInventory()) {
			if (distinctNames.contains(go.getName())) continue;
			if (go.hasAllKeyVals(HELMET_QUERY)
					|| go.hasAllKeyVals(SHIELD_QUERY)
					|| go.hasAllKeyVals(BREASTPLATE_QUERY)
					|| go.hasAllKeyVals(ARMOR_QUERY)) {
				ArmorChitComponent armor = (ArmorChitComponent)RealmComponent.getRealmComponent(go);
				if (armor.isDamaged()) {
					int cost = getCost(go);
					if (cost>gold) {
						cannotAfford = true;
					}
					else {
						distinctNames.add(go.getName());
						repairableArmor.add(armor);
					}
				}
			}
		}
		if (repairableArmor.isEmpty()) {
			reasonStoreNotAvailable = cannotAfford?"You cannot afford to repair any armor!":"You have no armor to repair!";
		}
	}
	private static String HELMET_QUERY = "armor,icon_type=helmet";
	private static String SHIELD_QUERY = "armor,icon_type=shield";
	private static String BREASTPLATE_QUERY = "armor,icon_type=breastplate";
	private static String ARMOR_QUERY = "armor,icon_type=suitofarmor";
	public String doService(JFrame frame) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Which Service?",true);
		for(ArmorChitComponent armor:repairableArmor) {
			int cost = getCost(armor.getGameObject());
			chooser.addRealmComponent(armor,"Repair for "+cost+" gold");
		}
		
		chooser.setVisible(true);
		if (chooser.getSelectedText()!=null) {
			ArmorChitComponent armor = (ArmorChitComponent)chooser.getFirstSelectedComponent();
			int cost = getCost(armor.getGameObject());
			character.addGold(-cost);
			armor.setIntact(true);
			return getTraderName()+" repaired the "+armor.getGameObject().getName();
		}
		return null;
	}
	private int getCost(GameObject go) {
		int cost = 4;
		String iconType = go.getThisAttribute(Constants.ICON_TYPE);
		if ("breastplate".equals(iconType)) {
			cost = 6;
		}
		else if ("suitofarmor".equals(iconType)) {
			cost = 10;
		}
		if (go.hasThisAttribute("magic")) {
			cost *= 2;
		}
		return cost;
	}
}