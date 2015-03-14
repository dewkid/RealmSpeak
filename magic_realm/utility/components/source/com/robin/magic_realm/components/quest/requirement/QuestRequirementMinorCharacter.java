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
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementMinorCharacter extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementMinorCharacter.class.getName());
	
	public static final String MINOR_CHARACTER = "_mcn";

	public QuestRequirementMinorCharacter(GameObject go) {
		super(go);
	}

	@Override
	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		String test = getMinorCharacterName();
		for (GameObject go:character.getMinorCharacters()) {
			if (test.equals(go.getName())) {
				return true;
			}
		}
		logger.fine(test+" is not present.");
		return false;
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.MinorCharacter;
	}

	@Override
	protected String buildDescription() {
		return "Only when "+getMinorCharacterName()+" is present.";
	}
	
	public String getMinorCharacterName() {
		return getString(MINOR_CHARACTER);
	}
}