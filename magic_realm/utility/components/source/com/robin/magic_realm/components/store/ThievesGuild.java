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
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ThievesGuild extends GuildStore {
	
	private static int GOLD_PRICE = 100;
	
	private static String MAP_SERVICE_1 = "Learn all hidden paths on a tile (both sides) for 5 gold.";
	private static String MAP_SERVICE_2 = "Learn all secret passages on a tile (both sides) for 10 gold.";
	private static String UNLOCK_SERVICE = "Unlock the Chest for 50 gold.";
	private static String ADVANCEMENT_SERVICE = "Pay "+GOLD_PRICE+" gold to advance to next level.";
	
	private ArrayList<TileComponent> tilesWithUnknownPaths;
	private ArrayList<TileComponent> tilesWithUnknownPassages;
	private ArrayList<GameObject> openable;

	public ThievesGuild(GuildChitComponent guild, CharacterWrapper character) {
		super(guild, character);
	}
	protected void setupGuildSpecific() {
		if (character.hasCurse(Constants.ASHES)) {
			reasonStoreNotAvailable = "The "+getTraderName()+" does not like your ASHES curse!";
			return;
		}
		
		tilesWithUnknownPaths = new ArrayList<TileComponent>();
		tilesWithUnknownPassages = new ArrayList<TileComponent>();
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(character.getGameData());
		for (GameObject go:rom.getTileObjects()) {
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
			if (pathsToDiscover(tile.getHiddenPaths(true)).size()>0) {
				tilesWithUnknownPaths.add(tile);
			}
			if (pathsToDiscover(tile.getSecretPassages(true)).size()>0) {
				tilesWithUnknownPassages.add(tile);
			}
		}
		
		openable = new ArrayList<GameObject>();
		for (GameObject go:character.getInventory()) {
			if (go.getName().startsWith("Chest") && go.hasThisAttribute(Constants.NEEDS_OPEN)) {
				openable.add(go);
			}
		}
	}
	protected ArrayList<String> pathsToDiscover(ArrayList<PathDetail> paths) {
		ArrayList<String> toDiscover = new ArrayList<String>();
		for (PathDetail path:paths) {
			String pathKey = path.getFullPathKey();
			if ((path.isHidden() && !character.hasHiddenPathDiscovery(pathKey))
					|| (path.isSecret() && !character.hasSecretPassageDiscovery(pathKey))) {
				toDiscover.add(pathKey);
			}
		}
		return toDiscover;
	}
	private String revealHidden(JFrame frame,ArrayList<TileComponent> list) {
		return reveal(frame,list,true);
	}
	private String revealSecret(JFrame frame,ArrayList<TileComponent> list) {
		return reveal(frame,list,false);
	}
	private String reveal(JFrame frame,ArrayList<TileComponent> list,boolean hidden) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Which tile to reveal "+(hidden?"hidden paths":"secret passages")+"?",true);
		for (TileComponent tile:list) {
			chooser.addRealmComponent(tile,true);
		}
		chooser.setVisible(true);
		if (chooser.getSelectedText()!=null) {
			TileComponent tile = (TileComponent)chooser.getFirstSelectedComponent();
			reveal(tile,hidden);
			return tile.getGameObject().getName();
		}
		return null;
	}
	private void reveal(TileComponent tile,boolean hidden) {
		if (hidden) {
			for (String pathKey:pathsToDiscover(tile.getHiddenPaths(true))) {
				if (!character.hasHiddenPathDiscovery(pathKey)) {
					character.addHiddenPathDiscovery(pathKey);
				}
			}
		}
		else {
			for (String pathKey:pathsToDiscover(tile.getSecretPassages(true))) {
				if (!character.hasSecretPassageDiscovery(pathKey)) {
					character.addSecretPassageDiscovery(pathKey);
				}
			}
		}
	}
	protected String doGuildService(JFrame frame,int level) {
		int gold = (int)character.getGold();
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,trader.getIcon(),"Which service?",getTraderName()+" Services",true);
		if (level<3) chooser.addSelectionObject(ADVANCEMENT_SERVICE,gold>=GOLD_PRICE);
		updateButtonChooser(chooser,level);
		if (level>=1) chooser.addSelectionObject(MAP_SERVICE_1,(gold>=5) && !tilesWithUnknownPaths.isEmpty());
		if (level>=2) chooser.addSelectionObject(MAP_SERVICE_2,(gold>=10) && !tilesWithUnknownPassages.isEmpty());
		if (level==3) chooser.addSelectionObject(UNLOCK_SERVICE,(gold>=50) && !openable.isEmpty());
		chooser.setVisible(true);
		
		String selected = (String)chooser.getSelectedObject();
		if (selected!=null) {
			boolean freeAdvancement = isFreeAdvancement(selected);
			if (MAP_SERVICE_1.equals(selected)) {
				String tileName = revealHidden(frame,tilesWithUnknownPaths);
				if (tileName!=null) {
					character.addGold(-5);
					return "Learned all hidden paths in the "+tileName;
				}
			}
			else if (MAP_SERVICE_2.equals(selected)) {
				String tileName = revealSecret(frame,tilesWithUnknownPassages);
				if (tileName!=null) {
					character.addGold(-10);
					return "Learned all secret passages in the "+tileName;
				}
			}
			else if (UNLOCK_SERVICE.equals(selected)) {
				character.addGold(-50);
				GameObject opened = TreasureUtility.openOneObject(frame,character,openable,null,true);
				return "Opened the "+opened.getName();
			}
			else if (freeAdvancement || ADVANCEMENT_SERVICE.equals(selected)) {
				if (!freeAdvancement) character.addGold(-GOLD_PRICE);
				int newLevel = character.getCurrentGuildLevel()+1;
				character.setCurrentGuildLevel(newLevel);
				chooseFriendlinessGain(frame);
				if (newLevel==3) {
					GameObject go = getNewCharacterChit();
					Strength vul = new Strength(character.getGameObject().getThisAttribute("vulnerability"));
					if (!vul.isTremendous()) {
						vul = vul.addStrength(1);
					}
					go.setThisAttribute("action","move");
					go.setThisAttribute("speed","2");
					go.setThisAttribute("strength",vul.toString());
					go.setThisAttribute("effort","2");
					go.setName(character.getCharacterLevelName(4)+" MOVE "+vul.toString()+"2**");
					RealmLogging.logMessage(character.getGameObject().getName(),"Gained a "+go.getName()+" chit.");
				}
				return "Advanced to "+character.getCurrentGuildLevelName()+"!";
			}
		}
		return null;
	}
}