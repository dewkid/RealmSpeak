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
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementDiscovery extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementDiscovery.class.getName());
	
	public static final String DISCOVERY_KEY = "_drk";
	
	public QuestRequirementDiscovery(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		ArrayList<String> discoveryKeys = character.getAllDiscoveryKeys();
		String keyToFind = getDiscoveryKey();
		for (String val:discoveryKeys) {
			if (val.startsWith(keyToFind)) { // Using startsWith, to ignore board number (for now)
				return true;
			}
		}
		logger.fine("Discovery \""+keyToFind+"\" was not found.");
		return false;
	}

	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Must have discovery for \"");
		sb.append(getDiscoveryKey());
		sb.append("\".");
		return sb.toString();
	}

	public RequirementType getRequirementType() {
		return RequirementType.Discovery;
	}
	
	public String getDiscoveryKey() {
		return getString(DISCOVERY_KEY);
	}
}