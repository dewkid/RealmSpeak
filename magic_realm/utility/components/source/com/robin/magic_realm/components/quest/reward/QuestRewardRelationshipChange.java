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
import com.robin.magic_realm.components.quest.GainType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardRelationshipChange extends QuestRewardRelationshipSet {
	public static final String GAIN_TYPE = "_gt";
	public static final String RELATIONSHIP_CHANGE = "_rc";
	
	public QuestRewardRelationshipChange(GameObject go) {
		super(go);
	}
	public void processReward(JFrame frame,CharacterWrapper character) {
		ArrayList<GameObject> representativeNativesToChange = getRepresentativeNatives(character);
		if (representativeNativesToChange==null) return;
		
		// Do the change
		int sign = getGainType()==GainType.Gain?1:-1;
		for(GameObject denizen:representativeNativesToChange) {
			character.changeRelationship(denizen,sign*getRelationshipChange());
		}
	}
	
	public String getDescription() {
		int val = getRelationshipChange();
		String group = getNativeGroup();
		if (group.equals("Clearing")) group = "all natives in the clearing.";
		else group = "the "+group;
		return (getGainType()==GainType.Gain?"Gain":"Lose")+" "+val+" level"+(val==1?"":"s")+" of friendliness with "+group;
	}
	
	public RewardType getRewardType() {
		return RewardType.RelationshipChange;
	}
	
	public GainType getGainType() {
		return GainType.valueOf(getString(GAIN_TYPE));
	}
	
	public int getRelationshipChange() {
		return getInt(RELATIONSHIP_CHANGE);
	}
}