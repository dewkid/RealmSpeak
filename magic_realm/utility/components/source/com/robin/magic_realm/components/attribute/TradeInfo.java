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
package com.robin.magic_realm.components.attribute;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.RealmUtility;

public class TradeInfo {
	private static final int NULL = -999;
	private int relationship = NULL;
	private String groupName;
	private int groupCount = 0;
	private RealmComponent trader;
	protected String noDrinksReason;
	
	private int hireGroupSize = 0;
	
	public TradeInfo(RealmComponent trader) {
		this.trader = trader;
	}
	
	public String getName() {
		return trader.getGameObject().getName();
	}
	
	public String getThisAttribute(String key) {
		return trader.getGameObject().getThisAttribute(key);
	}
	
	public GameObject getGameObject() {
		return trader.getGameObject();
	}
	
	public GameData getGameData() {
		return trader.getGameObject().getGameData();
	}
	
	public String getDisplayName() {
		if (trader.isMonster()) {
			return StringUtilities.capitalize(groupName+(hireGroupSize>1?"s":""));
		}
		return StringUtilities.capitalize(groupName);
	}

	public int getRelationship() {
		return relationship;
	}
	
	public void addRelationship(int val) {
		relationship += val;
	}
	
	public String getRelationshipName() {
		return RealmUtility.getRelationshipNameFor(relationship);
	}
	
	public int getRelationshipType() {
		if (isEnemy()) return RelationshipType.ENEMY;
		if (isUnfriendly()) return RelationshipType.UNFRIENDLY;
		if (isFriendly()) return RelationshipType.FRIENDLY;
		if (isAlly()) return RelationshipType.ALLY;
		return RelationshipType.NEUTRAL;
	}
	
	public boolean isEnemy() {
		return relationship<=RelationshipType.ENEMY;
	}
	public boolean isUnfriendly() {
		return relationship==RelationshipType.UNFRIENDLY;
	}
	public boolean isNeutral() {
		return relationship==RelationshipType.NEUTRAL;
	}
	public boolean isFriendly() {
		return relationship==RelationshipType.FRIENDLY;
	}
	public boolean isAlly() {
		return relationship>=RelationshipType.ALLY;
	}
	
	public void setRelationship(int relationship) {
		this.relationship = relationship;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public void bumpGroupCount() {
		this.groupCount++;
	}

	public RealmComponent getTrader() {
		return trader;
	}

	public void setHireGroupSize(int hireGroupSize) {
		this.hireGroupSize = hireGroupSize;
	}

	public String getNoDrinksReason() {
		return noDrinksReason;
	}

	public void setNoDrinksReason(String noDrinksReason) {
		this.noDrinksReason = noDrinksReason;
	}
}