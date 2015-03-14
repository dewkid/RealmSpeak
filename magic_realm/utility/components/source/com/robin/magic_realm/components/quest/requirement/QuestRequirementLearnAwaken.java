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
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementLearnAwaken extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementLearnAwaken.class.getName());

	public static final String MUST_LEARN = "_lrn";
	public static final String REGEX_FILTER = "_regex";

	public QuestRequirementLearnAwaken(GameObject go) {
		super(go);
	}

	@Override
	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		boolean awaken = (reqParams.searchType == SearchResultType.Awaken || reqParams.searchType == SearchResultType.LearnAndAwaken) && reqParams.searchHadAnEffect;
		if (!awaken) {
			logger.fine("Didn't awaken a spell.");
			return false;
		}
		boolean learned = reqParams.searchType == SearchResultType.LearnAndAwaken;
		if (requiresLearn() && !learned) {
			logger.fine("Didn't learn a spell.");
			return false;
		}

		String regex = getRegExFilter();
		if (regex != null && regex.trim().length() > 0) {
			Pattern pattern = Pattern.compile(regex);
			if (reqParams.objectList.size()!=1) {
				logger.fine("Nothing to match to regex /"+regex+"/");
				return false;
			}
			GameObject go = reqParams.objectList.get(0);
			if (!pattern.matcher(go.getName()).find()) {
				logger.fine(go.getName()+" does not match regex /"+regex+"/");
				return false;
			}
		}

		return true;
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.LearnAwaken;
	}

	@Override
	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(requiresLearn() ? "Must learn " : "Must awaken ");
		sb.append("a spell");
		String regex = getRegExFilter();
		if (regex != null && regex.trim().length() > 0) {
			sb.append(" matching /");
			sb.append(regex);
			sb.append("/");
		}
		sb.append(".");
		return sb.toString();
	}

	public boolean requiresLearn() {
		return getBoolean(MUST_LEARN);
	}

	public String getRegExFilter() {
		return getString(REGEX_FILTER);
	}
}