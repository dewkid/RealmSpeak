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
import java.util.Iterator;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public abstract class GuildStore extends Store {
	
	protected static String FREE_ADVANCEMENT = "Advance to next level by giving item to guild: ";
	
	protected CharacterWrapper character;
	protected ArrayList<GameObject> advancementObjects; 
	
	protected abstract void setupGuildSpecific();
	protected abstract String doGuildService(JFrame frame,int level);

	public GuildStore(GuildChitComponent guild,CharacterWrapper character) {
		super(guild);
		this.character = character;
		setupStore();
	}

	private void setupStore() {
		advancementObjects = new ArrayList<GameObject>();
		for (GameObject go:character.getInventory()) {
			if (go.hasThisAttribute(Constants.ADVANCEMENT)) {
				advancementObjects.add(go);
			}
		}
		setupGuildSpecific();
	}
	
	protected boolean isFreeAdvancement(String selected) {
		if (selected.startsWith(FREE_ADVANCEMENT)) {
			String item = selected.substring(FREE_ADVANCEMENT.length());
			for (GameObject go:advancementObjects) {
				if (item.equals(go.getName())) {
					go.getHeldBy().remove(go); // Make object disappear
					return true;
				}
			}
		}
		return false;
	}

	protected void updateButtonChooser(ButtonOptionDialog chooser,int level) {
		if (level>=3) return;
		for (GameObject go:advancementObjects) {
			chooser.addSelectionObject(FREE_ADVANCEMENT+go.getName());
		}
	}
	
	public String doService(JFrame frame) {
		return doGuildService(frame,character.getCurrentGuildLevel());
	}
	protected GameObject getNewCharacterChit() {
		GameObject go = character.getGameData().createNewObject();
		go.setThisAttribute("character_chit");
		go.setThisAttribute(Constants.CHIT_EARNED);
		CharacterActionChitComponent first = character.getAllChits().get(0);
		go.setThisAttribute("icon_folder",first.getGameObject().getThisAttribute("icon_folder"));
		go.setThisAttribute("icon_type",first.getGameObject().getThisAttribute("icon_type"));
		character.getGameObject().add(go);
		CharacterActionChitComponent chit = (CharacterActionChitComponent)RealmComponent.getRealmComponent(go);
		chit.makeActive();
		return go;
	}
	
	protected void chooseFriendlinessGain(JFrame frame) {
		ArrayList list = trader.getGameObject().getThisAttributeList("allies");
		
		GamePool pool = new GamePool(trader.getGameObject().getGameData().getGameObjects());
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"The guild advancement includes one friendliness level for one of the following groups:",false);
		for (Iterator i=list.iterator();i.hasNext();) {
			String groupName = i.next().toString();
			GameObject leader = pool.findFirst("rank=HQ,native="+groupName);
			int rel = character.getRelationship(leader);
			String oldR = RealmUtility.getRelationshipNameFor(rel);
			String newR = RealmUtility.getRelationshipNameFor(rel+1);
			chooser.addGameObject(leader,oldR+" -> "+newR);
		}
		chooser.setVisible(true);
		String selected = chooser.getSelectedText();
		if (selected!=null) {
			GameObject leader = chooser.getFirstSelectedComponent().getGameObject();
			character.changeRelationship(leader,1);
			RealmLogging.logMessage(character.getGameObject().getName(),"+1 friendliness with the "+leader.getThisAttribute("native"));
		}
	}
}