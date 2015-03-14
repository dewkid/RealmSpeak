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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GameObjectBlockManager;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.utility.Constants;

public class VersionManager {
	
	// All versions
	public static final String VERSION_1_0 = "RealmCharacterBuilderModel_v1.0";
	public static final String VERSION_1_1 = "RealmCharacterBuilderModel_v1.1";
	public static final String VERSION_1_2 = "RealmCharacterBuilderModel_v1.2";
	public static final String VERSION_1_3 = "RealmCharacterBuilderModel_v1.3";
	public static final String VERSION_1_4 = "RealmCharacterBuilderModel_v1.4";

	public static final String CURRENT_VERSION = VERSION_1_4;
	
	private static boolean wasConverted;
	private static String errorString;
	
	public static String convert(RealmCharacterBuilderModel model) {
		errorString = null;
		wasConverted = false;
		while(_convert(model)); // cycles until all conversions are done
		return errorString;
	}
	public static boolean getWasConverted() {
		return wasConverted;
	}
	/**
	 * @return		true when a conversion has occurred.
	 */
	private static boolean _convert(RealmCharacterBuilderModel model) {
		String version = model.getData().getGameDescription();
		if (CURRENT_VERSION.equals(version)) {
			// We are good - no conversion necessary
		}
		else if (VERSION_1_0.equals(version)) {
			// Convert from 1.0 to 1.1
			GameObject character = model.getCharacter().getGameObject();
			for (int i=1;i<=4;i++) {
				String levelKey = "level_"+i;
				String extraActions = character.getAttribute(levelKey,Constants.EXTRA_ACTIONS);
				//	- extra_actions changes from a string attribute, to an attributeList
				if (extraActions!=null) {
					ArrayList list = new ArrayList();
					list.add(extraActions);
					character.setAttributeList(levelKey,Constants.EXTRA_ACTIONS,list);
				}
				
				// - translate demon_immunity into the more generic monster_immunity
				if (character.hasAttribute(levelKey,"demon_immunity")) {
					character.removeAttribute(levelKey,"demon_immunity");
					ArrayList demons = new ArrayList();
					demons.add("Demon");
					demons.add("Flying Demon");
					demons.add("Imp");
					character.setAttributeList(levelKey,"monster_immunity",demons);
				}
				
				// - fix lost city, lost castle, and ruins diemods
				if (character.hasAttribute(levelKey,Constants.DIEMOD)) {
					ArrayList list = character.getAttributeList(levelKey,Constants.DIEMOD);
					if (!list.isEmpty()) {
						String val = (String)list.get(0);
						StringUtilities.findAndReplace(val,"Lost City","Lost City%");
						StringUtilities.findAndReplace(val,"Lost Castle","Lost Castle%");
						StringUtilities.findAndReplace(val,"%ruins","%ruins%");
					}
				}
			}
			//	- fix Ghost starting location key (Ghost not Ghosts)
			String start = character.getThisAttribute("start");
			start = StringUtilities.findAndReplace(start,"Ghosts","Ghost");
			character.setThisAttribute("start",start);
			
			// - fix character_chit to icon_type
			for (Iterator i=character.getHold().iterator();i.hasNext();) {
				GameObject item = (GameObject)i.next();
				if (item.hasThisAttribute("character_chit")) {
					String iconType = item.getThisAttribute("character_chit");
					item.setThisAttribute("icon_type",iconType);
					item.setThisAttribute("character_chit","");
				}
			}
			
			// Update version number
			updateVersion(model,VERSION_1_1);
			return true;
		}
		else if (VERSION_1_1.equals(version)) {
			// Change "Flying Demon" to "Winged Demon" where appropriate
			GameObject character = model.getCharacter().getGameObject();
			for (int i=1;i<=4;i++) {
				String levelKey = "level_"+i;
				if (character.hasAttribute(levelKey,"monster_immunity")) {
					ArrayList monsters = new ArrayList(character.getAttributeList(levelKey,"monster_immunity"));
					if (monsters.contains("Flying Demon")) {
						monsters.remove("Flying Demon");
						monsters.add("Winged Demon");
					}
					character.setAttributeList(levelKey,"monster_immunity",monsters);
				}
			}
			
			// Update version number
			updateVersion(model,VERSION_1_2);
			return true;
		}
		else if (VERSION_1_2.equals(version)) {
			// Fix how extra chits are stored.
			GameObject character = model.getCharacter().getGameObject();
			for (int i=1;i<=4;i++) {
				String levelKey = "level_"+i;
				if (character.hasAttribute(levelKey,Constants.BONUS_CHIT)) {
					String baseBlockKey = Constants.BONUS_CHIT+levelKey;
					character.renameAttributeBlock(baseBlockKey,baseBlockKey+"this");
					character.setAttribute(baseBlockKey,"Name","Extra Chit");
					GameObjectBlockManager man = new GameObjectBlockManager(character,""); // attributes not obscured
					GameObject chit = man.extractGameObjectFromBlocks(baseBlockKey,true,true);
					man.clearBlocks(baseBlockKey);
					man = new GameObjectBlockManager(character); // attributes are obscured
					man.storeGameObjectInBlocks(chit,baseBlockKey);
				}
			}
			
			// Update version number
			updateVersion(model,VERSION_1_3);
			return true;
		}
		else if (VERSION_1_3.equals(version)) {
			// enh_peer = ALL, rem_spell = ALL is no longer used.  Now use SPECIAL_ACTION
			GameObject character = model.getCharacter().getGameObject();
			for (int i=1;i<=4;i++) {
				String levelKey = "level_"+i;
				if (character.hasAttribute(levelKey,"enh_peer")) {
					character.removeAttribute(levelKey,"enh_peer");
					character.addAttributeListItem(levelKey,Constants.SPECIAL_ACTION,"ENHANCED_PEER");
				}
				if (character.hasAttribute(levelKey,"rem_spell")) {
					character.removeAttribute(levelKey,"rem_spell");
					character.addAttributeListItem(levelKey,Constants.SPECIAL_ACTION,"REMOTE_SPELL");
				}
			}
			
		}
		else {
			errorString = "unknown rschar version:  "+version;
		}
		return false;
	}
	private static void updateVersion(RealmCharacterBuilderModel model,String version) {
		model.getData().setGameDescription(version);
		System.out.println("Converted "+model.getCharacter().getGameObject().getName()+" to "+version);
		wasConverted = true;
	}
}