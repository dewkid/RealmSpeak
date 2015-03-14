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

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.quest.GamePhaseType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementGamePhase extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementGamePhase.class.getName());
	
	public static final String ATTRIBUTE_TYPE = "_at";
	public static final String GAME_PHASE_TYPE = "_gpt";

	public QuestRequirementGamePhase(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		GamePhaseType expecting = getGamePhaseType();
		boolean ret = reqParams!=null && reqParams.timeOfCall==expecting;
		
		if (!ret) {
			String phase = (reqParams==null || reqParams.timeOfCall==null) ? "Unknown" : reqParams.timeOfCall.toString();
			logger.fine("Incorrect game phase "+phase+".  Expecting: "+expecting);
		}
		
		return ret;
	}

	protected String buildDescription() {
		return "Only at "+getGamePhaseType();
	}

	public RequirementType getRequirementType() {
		return RequirementType.GamePhase;
	}
	
	public GamePhaseType getGamePhaseType() {
		return GamePhaseType.valueOf(getString(GAME_PHASE_TYPE));
	}
}