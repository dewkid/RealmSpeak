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
import com.robin.magic_realm.components.utility.TreasureUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardStripInventory extends QuestReward {
	public static final String STRIP_GOLD = "_sg";

	public QuestRewardStripInventory(GameObject go) {
		super(go);
	}

	public void processReward(JFrame frame,CharacterWrapper character) {
		ArrayList<GameObject> list = new ArrayList<GameObject>(character.getInventory());
		for(GameObject go:list) {
			if (TreasureUtility.doDeactivate(null,character,go)) { // null JFrame, so that the character isn't hit with any message popups!
				lostItem(go);
			}
		}
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("All inventory ");
		if (isStripGold()) sb.append("and gold ");
		sb.append("is stripped from the character.");
		return sb.toString();
	}

	public RewardType getRewardType() {
		return RewardType.StripInventory;
	}
	
	public void setStripGold(boolean val) {
		setBoolean(STRIP_GOLD,val);
	}
	
	public boolean isStripGold() {
		return getBoolean(STRIP_GOLD);
	}
}