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

import java.util.ArrayList;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardMinorCharacter extends QuestReward {
	public static final String MINOR_CHARACTER = "_mc";
	public static final String GAIN_TYPE = "_gt";

	public QuestRewardMinorCharacter(GameObject go) {
		super(go);
		autoRepair(); // to fix any quest with an id instead of a name
	}
	private void autoRepair() {
		String id = getString(MINOR_CHARACTER);
		if (id==null) return;
		try {
			long sid = Long.valueOf(id);
			GameObject go = getGameData().getGameObject(sid);
			if (go!=null) {
				setString(MINOR_CHARACTER,go.getName());
			}
		}
		catch(NumberFormatException ex) {
			// ignore
		}
	}
	
	public void processReward(JFrame frame,CharacterWrapper character) {
		QuestMinorCharacter minorCharacter = getQuestMinorCharacter();
		if (minorCharacter==null) return;
		if (getGainType()==GainType.Gain) {
			minorCharacter.getGameObject().setThisAttribute(Constants.ACTIVATED);
			minorCharacter.setupAbilities();
			character.getGameObject().add(minorCharacter.getGameObject());
		}
		else {
			character.getGameObject().remove(minorCharacter.getGameObject());
		}
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(getString(MINOR_CHARACTER));
		sb.append(getGainType()==GainType.Gain?" joins ":" leaves ");
		sb.append("the character.");
		return sb.toString();
	}

	public RewardType getRewardType() {
		return RewardType.MinorCharacter;
	}

	public QuestMinorCharacter getQuestMinorCharacter() {
		String id = getString(MINOR_CHARACTER);
		if (id==null) return null;

		CharacterWrapper character = getParentQuest().getOwner();
		if (character==null) return null; // what to do here?  shouldn't ever happen - the reward shouldn't be given while this quest is still a template!
		
		ArrayList<String> query = new ArrayList<String>();
		query.add(Quest.QUEST_MINOR_CHARS);
		query.add("name="+id);
		
		
		// Try the quest FIRST
		GamePool pool = new GamePool(getParentQuest().getGameObject().getHold());
		GameObject mc = pool.findFirst(query);
		if (mc==null) {
			// Try the character inventory
			pool = new GamePool(character.getInventory());
			mc = pool.findFirst(query);
		}
		// TODO Test the entire game library or not?  Maybe only matches that aren't assigned?
		
		if (mc!=null) {
			//setString(MINOR_CHARACTER,mc.getStringId()); // save for future reference?
			return new QuestMinorCharacter(mc);
		}
		return null;
	}	
	
	public GainType getGainType() {
		return GainType.valueOf(getString(GAIN_TYPE));
	}
}