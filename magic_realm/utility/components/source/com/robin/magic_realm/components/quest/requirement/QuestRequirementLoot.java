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
package com.robin.magic_realm.components.quest.requirement;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.quest.TreasureType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementLoot extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementLoot.class.getName());
	
	public static final String TREASURE_TYPE = "_tt";
	public static final String REGEX_FILTER = "_regex";
	
	public QuestRequirementLoot(GameObject go) {
		super(go);
	}
	
	protected boolean testFulfillsRequirement(JFrame frame,CharacterWrapper character,QuestRequirementParams reqParams) {
		if (reqParams!=null && "Loot".equals(reqParams.actionName)) {
			ArrayList<GameObject> matches = filterObjectsForRequirement(character,reqParams.objectList,logger);
			return !matches.isEmpty();
		}
		else {
			logger.fine(character.getName()+" did not Loot.");
		}
		return false;
	}
	
	protected ArrayList<GameObject> filterObjectsForRequirement(CharacterWrapper character,ArrayList<GameObject> objects,Logger myLogger) {
		ArrayList<GameObject> matches = new ArrayList<GameObject>();
		if (objects.isEmpty()) {
			myLogger.fine("No items to test.");
			return matches;
		}
		GamePool pool = new GamePool(objects);
		String query = null;
		TreasureType tt = getTreasureType();
		switch(tt) {
			case Any:
				query = "";
				break;
			case Artifact:
			case Book:
			case Boots:
			case Gloves:
			case Great:
				query = tt.toString().toLowerCase();
				break;
			case Large:
				query="treasure=large";
				break;
			case MagicArmor:
				query="armor,magic";
				break;
			case MagicWeapon:
				query="weapon,magic";
				break;
			case Small:
				query="treasure=small";
				break;
			case TWT:
				query="treasure_within_treasure";
				break;
			case Armor:
				query="armor,!character";
				break;
			case Weapon:
				query="weapon,!character";
				break;
		}
		ArrayList<GameObject> typeMatches = query==null?objects:pool.find(query);
		if (!typeMatches.isEmpty()) {
			String regex = getRegExFilter();
			Pattern pattern = regex==null || regex.trim().length()==0?null:Pattern.compile(regex);
			
			for(GameObject go:typeMatches) {
				if (pattern==null || pattern.matcher(go.getName()).find()) {
					matches.add(go);
				}
			}
			if (matches.isEmpty()) {
				myLogger.fine("None of the treasures tested matched the regex: "+regex);
			}
		}
		else {
			myLogger.fine("None of the treasures tested were the correct treasure type: "+tt.toString());
		}
		return matches;
	}
	
	public RequirementType getRequirementType() {
		return RequirementType.Loot;
	}
	
	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Must loot a");
		TreasureType tt = getTreasureType();
		if (tt!=TreasureType.Any) {
			sb.append(" ");
			sb.append(getTreasureType().toString().toLowerCase());
		}
		sb.append(" treasure");
		String regex = getRegExFilter();
		if (regex!=null && regex.trim().length()>0) {
			sb.append(", matching /");
			sb.append(regex);
			sb.append("/");
		}
		sb.append(".");
		return sb.toString();
	}
	
	public TreasureType getTreasureType() {
		return TreasureType.valueOf(getString(TREASURE_TYPE));
	}
	public String getRegExFilter() {
		return getString(REGEX_FILTER);
	}
}