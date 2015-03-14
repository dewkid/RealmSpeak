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
package com.robin.magic_realm.components.table;

import javax.swing.JFrame;
import javax.swing.event.ChangeListener;

import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class MonsterGrow extends RealmTable {
	private MonsterChitComponent monster;
	public MonsterGrow(JFrame frame, ChangeListener listener,MonsterChitComponent monster) {
		super(frame, listener);
		this.monster = monster;
	}

	public static final String KEY = "MonsterGrow";
	
	public String getTableKey() {
		return KEY;
	}

	public String getTableName(boolean longDescription) {
		return "Monster Grow";
	}
	
	private String doGrow(int sizes) {
		Strength tremendous = new Strength("T");
		Strength strength = monster.getVulnerability();
		Strength newStrength = monster.getVulnerability();
		newStrength.modify(sizes);
		if (newStrength.strongerThan(tremendous)) {
			newStrength = tremendous;
		}
		int actualSizes = newStrength.getLevels() - strength.getLevels();
		int attackSpeed = monster.getAttackSpeed().getNum();
		Strength attackStrength = monster.getStrength();
		attackStrength.modify(actualSizes);
		monster.getGameObject().setThisAttribute("vulnerability",String.valueOf(newStrength.getChar()));
		adjustBothSides("strength",attackStrength.getChar());
		adjustBothSides("attack_speed",String.valueOf(attackSpeed + actualSizes));
		
		monster.updateChit();
		
		return monster.getGameObject().getName()+" grows by "+sizes+(sizes!=actualSizes?" (actually "+actualSizes+")":"");
	}
	private void adjustBothSides(String key,String val) {
		monster.getGameObject().setAttribute("light",key,val);
		monster.getGameObject().setAttribute("dark",key,val);
	}
	
	public String applyOne(CharacterWrapper character) {
		return doGrow(2);
	}

	public String applyTwo(CharacterWrapper character) {
		return doGrow(2);
	}

	public String applyThree(CharacterWrapper character) {
		return doGrow(1);
	}

	public String applyFour(CharacterWrapper character) {
		return doGrow(1);
	}

	public String applyFive(CharacterWrapper character) {
		return "Nothing";
	}

	public String applySix(CharacterWrapper character) {
		return "Nothing";
	}
}