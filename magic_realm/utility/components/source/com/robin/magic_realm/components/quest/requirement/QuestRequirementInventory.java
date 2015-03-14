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

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.TreasureType;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementInventory extends QuestRequirementLoot {
	private static Logger logger = Logger.getLogger(QuestRequirementInventory.class.getName());
	
	public static final String NUMBER = "_num";
	public static final String ITEM_ACTIVE = "_iact";

	public QuestRequirementInventory(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		boolean reqActive = mustBeActive();
		ArrayList<GameObject> matches;
		if (reqActive && reqParams.actionType==CharacterActionType.ActivatingItem) {
			reqActive = false; // since we already know we are activating the item, don't test for it later (actually causes a problem with the Chest which is opened instead of activated!)
			matches = filterObjectsForRequirement(character,reqParams.objectList,logger);
		}
		else {
			matches = filterObjectsForRequirement(character,character.getInventory(),logger);
		}
		int n = getNumber();
		if (matches.size()>=n) {
			if (reqActive) {
				for(GameObject match:matches) {
					if (!match.hasThisAttribute(Constants.ACTIVATED)) {
						logger.fine(match.getName()+" must be activated.");
						return false;
					}
				}
			}
			
			return true;
		}
		logger.fine("Only "+matches.size()+" items were found, when "+n+" was expected.");
		return false;
	}

	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Must ");
		sb.append(mustBeActive()?"activate ":"own ");
		int num = getNumber();
		sb.append(num);
		TreasureType tt = getTreasureType();
		if (tt!=TreasureType.Any) {
			sb.append(" ");
			sb.append(getTreasureType().toString().toLowerCase());
		}
		sb.append(" treasure");
		sb.append(num==1?"":"s");
		String regex = getRegExFilter();
		if (regex!=null && regex.trim().length()>0) {
			sb.append(", matching /");
			sb.append(regex);
			sb.append("/");
		}
		sb.append(".");
		return sb.toString();

	}

	public RequirementType getRequirementType() {
		return RequirementType.Inventory;
	}
	public int getNumber() {
		return getInt(NUMBER);
	}
	public boolean mustBeActive() {
		return getBoolean(ITEM_ACTIVE);
	}
}