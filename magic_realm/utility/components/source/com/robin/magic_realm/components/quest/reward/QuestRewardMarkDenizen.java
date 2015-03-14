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
package com.robin.magic_realm.components.quest.reward;

import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.QuestConstants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardMarkDenizen extends QuestReward {
	
	public static final String DENIZEN_REGEX = "_regex";

	public QuestRewardMarkDenizen(GameObject go) {
		super(go);
	}
	
	public void processReward(JFrame frame,CharacterWrapper character) {
		TileLocation current = character.getCurrentLocation();
		if (!current.isInClearing()) return;
		String regex = getDenizenRegEx().trim();
		Pattern pattern = regex.length()==0?null:Pattern.compile(regex);
		for(RealmComponent rc:current.clearing.getClearingComponents()) {
			if (pattern==null || pattern.matcher(rc.getGameObject().getName()).find()) {
				rc.getGameObject().setThisAttribute(QuestConstants.QUEST_MARK,getParentQuest().getGameObject().getStringId());
			}
		}
	}

	public String getDescription() {
		return "Mark all denizens in current clearing matching name: "+getDenizenRegEx();
	}

	public RewardType getRewardType() {
		return RewardType.MarkDenizen;
	}

	public String getDenizenRegEx() {
		return getString(DENIZEN_REGEX);
	}
}