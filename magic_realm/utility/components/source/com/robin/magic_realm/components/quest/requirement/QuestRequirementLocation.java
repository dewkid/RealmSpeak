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

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.QuestLocation;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementLocation extends QuestRequirement {
	
	private static Logger logger = Logger.getLogger(QuestRequirementLocation.class.getName());
	
	public static final String LOCATION = "_l";
	
	public QuestRequirementLocation(GameObject go) {
		super(go);
	}
	
	protected boolean testFulfillsRequirement(JFrame frame,CharacterWrapper character,QuestRequirementParams reqParams) {
		QuestLocation location = getQuestLocation();
		if (!location.locationMatchAddress(frame,character)) {
			logger.fine(character.getName()+" is not at specified location: "+location.getName());
			return false;
		}
		TileLocation tl = character.getCurrentLocation();
		if (!tl.isInClearing()) {
			logger.fine(character.getName()+" is not in a clearing.");
			return false;
		}
		return true;
	}
	
	public boolean usesLocationTag(String tag) {
		QuestLocation loc = getQuestLocation();
		return loc!=null && tag.equals(loc.getName());
	}
	
	public RequirementType getRequirementType() {
		return RequirementType.OccupyLocation;
	}
	
	protected String buildDescription() {
		QuestLocation questLocation = getQuestLocation();
		if (questLocation==null) return "ERROR - No location found!";
		return "Occupy "+getQuestLocation().getName();
	}
	
	public QuestLocation getQuestLocation() {
		String id = getString(LOCATION);
		if (id!=null) {
			GameObject go = getGameData().getGameObject(Long.valueOf(id));
			if (go!=null) {
				return new QuestLocation(go);
			}
		}
		return null;
	}
	
	public void updateIds(Hashtable<Long, GameObject> lookup) {
		updateIdsForKey(lookup,LOCATION);
	}
}