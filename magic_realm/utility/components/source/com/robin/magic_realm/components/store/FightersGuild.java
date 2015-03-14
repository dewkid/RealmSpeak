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
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class FightersGuild extends GuildStore {
	
	private static int FAME_PRICE = 60;
	
	private static String REST_SERVICE = "Rest all fatigued FIGHT/MOVE chits for 5 gold.";
	private static String REPAIR_SERVICE = "Repair all active armor for 10 gold.";
	private static String ADVANCEMENT_SERVICE = "Pay "+FAME_PRICE+" FAME to advance to next level.";
	
	private ArrayList<CharacterActionChitComponent> restableChits;
	private ArrayList<ArmorChitComponent> repairableArmor;
	
	public FightersGuild(GuildChitComponent guild, CharacterWrapper character) {
		super(guild, character);
	}
	protected void setupGuildSpecific() {
		if (character.hasCurse(Constants.ASHES)) {
			reasonStoreNotAvailable = "The "+getTraderName()+" does not like your ASHES curse!";
			return;
		}
		if (character.hasCurse(Constants.DISGUST)) {
			reasonStoreNotAvailable = "The "+getTraderName()+" does not like your DISGUST curse!";
			return;
		}
		
		restableChits = new ArrayList<CharacterActionChitComponent>();
		if (!character.hasCurse(Constants.WITHER)) {
			restableChits.addAll(character.getFatiguedChits());
		}
		
		repairableArmor = new ArrayList<ArmorChitComponent>();
		for(GameObject go:character.getActiveInventory()) {
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isArmor()) {
				repairableArmor.add((ArmorChitComponent)rc);
			}
		}
	}
	protected String doGuildService(JFrame frame,int level) {
		int gold = (int)character.getGold();
		int fame = (int)character.getFame();
		
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,trader.getIcon(),"Which service?",getTraderName()+" Services",true);
		if (level<3) chooser.addSelectionObject(ADVANCEMENT_SERVICE,fame>=FAME_PRICE);
		updateButtonChooser(chooser,level);
		if (level>=1) chooser.addSelectionObject(REST_SERVICE,(gold>=5) && !restableChits.isEmpty());
		if (level>=2) chooser.addSelectionObject(REPAIR_SERVICE,(gold>=10) && !repairableArmor.isEmpty());
		chooser.setVisible(true);
		
		String selected = (String)chooser.getSelectedObject();
		if (selected!=null) {
			boolean freeAdvancement = isFreeAdvancement(selected);
			if (REST_SERVICE.equals(selected)) {
				character.addGold(-5);
				for (CharacterActionChitComponent chit:restableChits) {
					chit.makeActive();
				}
				return "Rested all MOVE/FIGHT chits.";
			}
			else if (REPAIR_SERVICE.equals(selected)) {
				character.addGold(-10);
				for (ArmorChitComponent armor:repairableArmor) {
					armor.setIntact(true);
				}
				return "Repaired all active armor.";
			}
			else if (freeAdvancement || ADVANCEMENT_SERVICE.equals(selected)) {
				if (!freeAdvancement) character.addFame(-FAME_PRICE);
				int newLevel = character.getCurrentGuildLevel()+1;
				character.setCurrentGuildLevel(newLevel);
				chooseFriendlinessGain(frame);
				if (newLevel==3) {
					GameObject go = getNewCharacterChit();
					Strength vul = new Strength(character.getGameObject().getThisAttribute("vulnerability"));
					if (!vul.isTremendous()) {
						vul = vul.addStrength(1);
					}
					go.setThisAttribute("action","fight");
					go.setThisAttribute("speed","3");
					go.setThisAttribute("strength",vul.toString());
					go.setThisAttribute("effort","2");
					go.setName(character.getCharacterLevelName(4)+" FIGHT "+vul.toString()+"3**");
					RealmLogging.logMessage(character.getGameObject().getName(),"Gained a "+go.getName()+" chit.");
				}
				return "Advanced to "+character.getCurrentGuildLevelName()+"!";
			}
		}
		
		return null;
	}
}