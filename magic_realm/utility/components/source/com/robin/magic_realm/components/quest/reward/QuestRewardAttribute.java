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

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.quest.AttributeType;
import com.robin.magic_realm.components.quest.GainType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardAttribute extends QuestReward {
	public static final String ATTRIBUTE_TYPE = "_at";
	public static final String GAIN_TYPE = "_gt";
	public static final String ATTRIBUTE_CHANGE = "_ac";
	
	public QuestRewardAttribute(GameObject go) {
		super(go);
	}
	
	public void processReward(JFrame frame,CharacterWrapper character) {
		int val = getAttributeChange();
		val = getGainType()==GainType.Gain?val:-val;
		switch(getAttributeType()) {
			case Fame:
				character.addFame(val);
				break;
			case Notoriety:
				character.addNotoriety(val);
				break;
			case Gold:
				character.addGold(val);
				break;
		}
	}

	public String getDescription() {
		int val = getAttributeChange();
		AttributeType type = getAttributeType();
		return (getGainType()==GainType.Gain?"Gain":"Lose")+" "+val+" "+type.toString();
	}
	
	public RewardType getRewardType() {
		return RewardType.Attribute;
	}
	
	public void setAttributeType(AttributeType attributeType) {
		setString(ATTRIBUTE_TYPE,attributeType.toString());
	}
	
	public AttributeType getAttributeType() {
		return AttributeType.valueOf(getString(ATTRIBUTE_TYPE));
	}
	
	public void setGainType(GainType gainType) {
		setString(GAIN_TYPE,gainType.toString());
	}
	
	public GainType getGainType() {
		return GainType.valueOf(getString(GAIN_TYPE));
	}
	
	public void setAttributeChange(int val) {
		setInt(ATTRIBUTE_CHANGE,val);
	}
	
	public int getAttributeChange() {
		return getInt(ATTRIBUTE_CHANGE);
	}
}