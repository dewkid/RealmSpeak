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

import com.robin.magic_realm.components.CharacterActionChitComponent;

public class Effort {
	
	private int moveAsterisks = 0;
	private int fightAsterisks = 0;
	private int magicAsterisks = 0;
	private int flyAsterisks = 0;
	
	public Effort() {
	}
	public String toString() {
		return "MOVE="+moveAsterisks+",FIGHT="+fightAsterisks+",MAGIC="+magicAsterisks;
	}
	public void addEffort(CharacterActionChitComponent chit) {
		int val = chit.getEffortAsterisks();
		if (val>0) {
			if (chit.isMove()) {
				addMoveAsterisks(val);
			}
			else if (chit.isFight() || chit.isFightAlert()) {
				addFightAsterisks(val);
			}
			else if (chit.isMagic()) {
				addMagicAsterisks(val);
			}
			else if (chit.isFly()) {
				addFlyAsterisks(val);
			}
		}
	}
	private void addMoveAsterisks(int val) {
		moveAsterisks += val;
	}
	private void addFightAsterisks(int val) {
		fightAsterisks += val;
	}
	private void addMagicAsterisks(int val) {
		magicAsterisks += val;
	}
	private void addFlyAsterisks(int val) {
		flyAsterisks += val;
	}
	public int getAsterisks() {
		return moveAsterisks+fightAsterisks+magicAsterisks+flyAsterisks;
	}
	public boolean hasMove() {
		return moveAsterisks>0;
	}
	public boolean hasFight() {
		return fightAsterisks>0;
	}
	public boolean hasMagic() {
		return magicAsterisks>0;
	}
	public boolean hasFly() {
		return flyAsterisks>0;
	}
	public int getNeedToFatigue(int free) {
		int fatigueable = (moveAsterisks+fightAsterisks); // only move and fight count toward round fatigue (magic and fly fatigue automatically)
		int needToFatigue = fatigueable>free?(fatigueable-free):0;
		return needToFatigue;
	}
	public int getMoveAsterisks() {
		return moveAsterisks;
	}
	public int getFightAsterisks() {
		return fightAsterisks;
	}
	public int getMagicAsterisks() {
		return magicAsterisks;
	}
	public int getFlyAsterisks() {
		return flyAsterisks;
	}
}