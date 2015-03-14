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
package com.robin.hexmap;

import java.util.ArrayList;
import java.util.Collection;

public class MoveRule {

	private Object userData;
	private float moveLeft;
	private Collection rules;
	private boolean canMoveBoatless;
	
	public MoveRule(Object userData,float moveLeft) {
		this.userData = userData==null?"":userData;
		this.moveLeft = moveLeft;
		this.rules = new ArrayList();
		canMoveBoatless = false;
	}
	public void setUserData(Object userData) {
		this.userData = userData;
	}
	public Object getUserData() {
		return userData;
	}
	public float getMoveLeft() {
		return moveLeft;
	}
	public String getMoveLeftString() {
		return String.valueOf(moveLeft);
	}
	public void addRule(String rule) {
		if (!rules.contains(rule)) {
			rules.add(rule);
		}
	}
	public boolean hasRule(String rule) {
		return rules.contains(rule);
	}
	public String toString() {
		return userData.toString().substring(0,1)+moveLeft;
	}
	public void setCanMoveBoatless(boolean val) {
		canMoveBoatless = val;
	}
	public boolean getCanMoveBoatless() {
		return canMoveBoatless;
	}
}