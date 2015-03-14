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
import com.robin.magic_realm.components.quest.QuestConstants;
import com.robin.magic_realm.components.quest.QuestStep;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.DayKey;

public class QuestRequirementKill extends QuestRequirement {
	
	private static Logger logger = Logger.getLogger(QuestRequirementKill.class.getName());

	public static final String REGEX_FILTER = "_regex";
	public static final String REQUIRE_MARK = "_rqm";
	public static final String STEP_ONLY_KILLS = "_sok";
	public static final String VALUE = "_rq";
	
	public QuestRequirementKill(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame,CharacterWrapper character,QuestRequirementParams reqParams) {
		logger.fine(buildDescription());
		QuestStep step = getParentStep();
		DayKey earliestTime = getOnlyCountKillsForStep()?step.getQuestStepStartTime():step.getQuestStartTime();
		if (earliestTime==null) {
			logger.fine("Quest has no start time?  This is a bug: contact Robin.");
			return false;
		}
		
		boolean requireMark = getRequireMark();
		String questId = getParentQuest().getGameObject().getStringId();
		String regex = getRegExFilter().trim();
		Pattern pattern = regex.length()==0?null:Pattern.compile(regex);
		
		int numberOfKillsNeeded = getValue();
		if (numberOfKillsNeeded==QuestConstants.ALL_VALUE) {
			GamePool pool = new GamePool(getGameData().getGameObjects());
			for(GameObject go:pool.find("vulnerability,!weight")) { // stuff that can be killed has a vulnerability, including characters!
				if (pattern!=null && !pattern.matcher(go.getName()).find()) continue;
				if (requireMark) {
					String mark = go.getThisAttribute(QuestConstants.QUEST_MARK);
					if (mark==null || !mark.equals(questId)) continue;
				}
				
				if (!go.hasThisAttribute(Constants.DEAD)) {
					logger.fine(go.getName()+" is still alive.");
					return false;
				}
			}
			return true;
		}
		
		ArrayList<GameObject> validKills = new ArrayList<GameObject>();
		ArrayList allDayKeys = character.getAllDayKeys();
		if (allDayKeys==null) {
			logger.fine("Character hasn't had a turn yet.");
			return false;
		}
		for(Object obj:allDayKeys) {
			String dayKeyString = (String)obj;
			DayKey dayKey = new DayKey(dayKeyString);
			if (dayKey.before(earliestTime)) continue; // ignore kills on days before the earliest allowable date
			
			ArrayList<GameObject> kills = character.getKills(dayKeyString);
			for(GameObject kill:kills) {
				if (pattern!=null && !pattern.matcher(kill.getName()).find()) continue;
				if (requireMark) {
					String mark = kill.getThisAttribute(QuestConstants.QUEST_MARK);
					if (mark==null || !mark.equals(questId)) continue;
				}
				
				validKills.add(kill);
			}
		}
		
		boolean ret = validKills.size()>=numberOfKillsNeeded;
		if (!ret) {
			logger.fine(validKills.size()+" is not enough kills.  Expecting at least "+numberOfKillsNeeded+".");
		}
		return ret;
	}
	
	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Must kill ");
		int val = getValue();
		boolean mark = getRequireMark();
		sb.append(mark?"":"any ");
		sb.append(val==QuestConstants.ALL_VALUE?"ALL":""+val);
		sb.append(mark?" marked":"");
		sb.append(" denizen");
		sb.append(val==1?"":"s");
		sb.append(" that match");
		sb.append(val==1?"es":"");
		sb.append(" regex: /");
		sb.append(getRegExFilter());
		sb.append("/");
		return sb.toString();
	}

	public RequirementType getRequirementType() {
		return RequirementType.Kill;
	}
	
	public String getRegExFilter() {
		return getString(REGEX_FILTER);
	}
	public boolean getOnlyCountKillsForStep() {
		return getBoolean(STEP_ONLY_KILLS);
	}
	public boolean getRequireMark() {
		return getBoolean(REQUIRE_MARK);
	}
	public int getValue() {
		return getInt(VALUE);
	}
}