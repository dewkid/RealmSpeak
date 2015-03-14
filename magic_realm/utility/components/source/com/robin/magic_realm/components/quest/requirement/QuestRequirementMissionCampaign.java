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

import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.GoldSpecialChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementMissionCampaign extends QuestRequirement {

	private static Logger logger = Logger.getLogger(QuestRequirementLoot.class.getName());

	public static final String ACTION_TYPE = "_at";
	public static final String REGEX_FILTER = "_regex";
	public static final String DISABLE_ON_PICKUP = "_dop";

	public QuestRequirementMissionCampaign(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		CharacterActionType actionType = getActionType();
		if (reqParams != null && reqParams.actionType == actionType) {
			String regex = getRegExFilter();
			boolean match = regex == null || regex.length() == 0 ? true : Pattern.matches(regex, reqParams.actionName);
			if (!match) {
				logger.fine(character.getName()+" did not interact with correct mission/campaign matching "+regex+":"+reqParams.actionName);
			}
			
			if (isDisabledOnPickup()) {
				GoldSpecialChitComponent gs = (GoldSpecialChitComponent)RealmComponent.getRealmComponent(reqParams.targetOfSearch);
				gs.expireEffect(character);
			}
			
			return match;
		}
		else {
			logger.fine(character.getName()+" did not do the correct action: "+actionType.toString());
		}
		return false;
	}

	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		CharacterActionType actionType = getActionType();
		sb.append(actionType.getDescriptor());
		String regex = getRegExFilter();
		if (regex==null || regex.length()==0) {
			sb.append("any chit");
		}
		else {
			sb.append("chit matching /");
			sb.append(getRegExFilter());
			sb.append("/");
		}
		if (isDisabledOnPickup() && actionType==CharacterActionType.PickUpMissionCampaign) {
			sb.append(" (normal function disabled)");
		}
		sb.append(".");
		return sb.toString();
	}

	public RequirementType getRequirementType() {
		return RequirementType.MissionCampaign;
	}

	public CharacterActionType getActionType() {
		return CharacterActionType.valueOf(getString(ACTION_TYPE));
	}

	public String getRegExFilter() {
		return getString(REGEX_FILTER);
	}
	
	public boolean isDisabledOnPickup() {
		return getBoolean(DISABLE_ON_PICKUP);
	}
}