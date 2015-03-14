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
import com.robin.magic_realm.components.quest.QuestStep;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.DayKey;

public class QuestRequirementTimePassed extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementTimePassed.class.getName());
	
	public static final String VALUE = "_rq";

	public QuestRequirementTimePassed(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		int daysNeeded = getValue();
		QuestStep step = getParentStep();
		DayKey start = step.getQuestStepStartTime();
		if (start==null) {
			logger.fine("Quest step has no start time?  This is a bug: contact Robin.");
			return false;
		}
		DayKey now = new DayKey(reqParams.dayKey);
		int daysPassed = now.compareTo(start);
		boolean ret = daysPassed>=daysNeeded;
		if (!ret) {
			logger.fine("Only "+daysPassed+" days have passed.  Expecting "+daysNeeded+".");
		}
		return ret;
	}

	protected String buildDescription() {
		int val = getValue();
		StringBuilder sb = new StringBuilder();
		sb.append("Must wait ");
		sb.append(val);
		sb.append(" day");
		sb.append(val==1?"":"s");
		sb.append(".");
		return sb.toString();
	}

	public RequirementType getRequirementType() {
		return RequirementType.TimePassed;
	}
	public int getValue() {
		return getInt(VALUE);
	}
}