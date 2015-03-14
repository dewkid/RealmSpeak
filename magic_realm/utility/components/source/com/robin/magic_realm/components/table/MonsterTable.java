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

import java.util.ArrayList;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.MonsterCreator;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public abstract class MonsterTable extends RealmTable {

	public abstract String getMonsterKey();

	private MonsterCreator monsterCreator;
	
	protected MonsterTable(JFrame frame) {
		super(frame, null);
		monsterCreator = new MonsterCreator(getMonsterKey());
	}
	public MonsterCreator getMonsterCreator() {
		return monsterCreator;
	}
	public ArrayList<GameObject> getOneOfEach(CharacterWrapper character) {
		applyOne(character);
		applyTwo(character);
		applyThree(character);
		applyFour(character);
		applyFive(character);
		applySix(character);
		return monsterCreator.getMonstersCreated();
	}
}