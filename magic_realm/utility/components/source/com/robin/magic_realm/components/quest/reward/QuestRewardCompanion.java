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

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.GainType;
import com.robin.magic_realm.components.quest.QuestConstants;
import com.robin.magic_realm.components.utility.ClearingUtility;
import com.robin.magic_realm.components.utility.TemplateLibrary;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardCompanion extends QuestReward {
	
	public static final String COMPANION_NAME = "_cn";
	public static final String GAIN_TYPE = "_goc";
	
	public QuestRewardCompanion(GameObject go) {
		super(go);
	}

	public void processReward(JFrame frame,CharacterWrapper character) {
		// TODO Test both branches of logic
		// -- consider a companion that is killed
		if (getGainType()==GainType.Gain) {
			GameObject template = TemplateLibrary.getSingleton().getCompanionTemplate(getCompanionKeyName(),getCompanionQuery());
			GameObject companion = TemplateLibrary.getSingleton().createCompanionFromTemplate(getGameData(),template);
			character.addHireling(companion,99999);
			character.getGameObject().add(companion);
		}
		else {
			GamePool pool = new GamePool(character.getGameObject().getHold());
			GameObject companion = pool.findFirst(getCompanionQuery());
			character.removeHireling(companion);
			// Companions must be removed from the map as well, since they are not rehired!
			ClearingUtility.moveToLocation(companion,null);
		}
	}
	
	public ImageIcon getIcon() {
		GameObject template = TemplateLibrary.getSingleton().getCompanionTemplate(getCompanionKeyName(),getCompanionQuery());
		RealmComponent rc = RealmComponent.getRealmComponent(template);
		return rc.getIcon();
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(getCompanionKeyName());
		if (getGainType()==GainType.Gain) {
			sb.append(" joins as a companion.");
		}
		else {
			sb.append(" leaves the character.");
		}
		return sb.toString();
	}

	public RewardType getRewardType() {
		return RewardType.Companion;
	}
	
	public GainType getGainType() {
		return GainType.valueOf(getString(GAIN_TYPE));
	}

	public String getCompanionKeyName() {
		return getString(QuestConstants.KEY_PREFIX+COMPANION_NAME);
	}
	
	public String getCompanionQuery() {
		return getString(QuestConstants.VALUE_PREFIX+COMPANION_NAME);
	}
}