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

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class SummonElemental extends MonsterTable {

	public static final String KEY = "SummonElemental";
	
	private enum ElementalType {
		Earth,
		Fire,
		Water,
		Air
	}
	
	public SummonElemental(JFrame frame) {
		super(frame);
	}
	public String getTableKey() {
		return KEY;
	}
	public String getTableName(boolean longDescription) {
		return "Summon Elemental";
	}
	public String getMonsterKey() {
		return "elemental";
	}
//public String apply(CharacterWrapper character, DieRoller inRoller) {
//	System.err.println("REMOVE THIS LINE - SummonElemental");
//	return applyOne(character);
//}
	public String applyOne(CharacterWrapper character) {
		String earth = "Earth Elemental";
		String fire = "Fire Elemental";
		String water = "Water Elemental";
		String air = "Air Elemental";
		
		ButtonOptionDialog chooseSearch = new ButtonOptionDialog(getParentFrame(), null, "Choice:", "Summon Elemental", false);
		chooseSearch.addSelectionObject(earth);
		chooseSearch.addSelectionObject(fire);
		chooseSearch.addSelectionObject(water);
		chooseSearch.addSelectionObject(air);
		chooseSearch.setVisible(true);
		String choice = (String)chooseSearch.getSelectedObject();
		if (choice.equals(earth)) {
			return "Choice - " + applyTwo(character);
		}
		else if (choice.equals(fire)) {
			return "Choice - " + applyThree(character);
		}
		else if (choice.equals(water)) {
			return "Choice - " + applyFour(character);
		}
		else if (choice.equals(air)) {
			return "Choice - " + applyFive(character);
		}
		return null;
	}
	public String applyTwo(CharacterWrapper character) {
		summonElemental(character,ElementalType.Earth);
		return "Earth Elemental Summoned";
	}
	public String applyThree(CharacterWrapper character) {
		summonElemental(character,ElementalType.Fire);
		return "Fire Elemental Summoned";
	}
	public String applyFour(CharacterWrapper character) {
		summonElemental(character,ElementalType.Water);
		return "Water Elemental Summoned";
	}
	public String applyFive(CharacterWrapper character) {
		summonElemental(character,ElementalType.Air);
		return "Air Elemental Summoned";
	}
	public String applySix(CharacterWrapper character) {
		return "No Effect";
	}
	private void summonElemental(CharacterWrapper character, ElementalType type) {
		GameData data = character.getGameObject().getGameData();
		GameObject elemental = getMonsterCreator().createOrReuseMonster(data);
		switch(type) {
			case Earth:
				getMonsterCreator().setupGameObject(elemental,"Earth Elemental","earth","T",true);
				getMonsterCreator().setupSide(elemental,"light","T",0,4,0,6,"tan");
				getMonsterCreator().setupSide(elemental,"dark","RED",0,4,0,6,"red");
				elemental.setAttribute("dark","pins");
				break;
			case Fire:
				getMonsterCreator().setupGameObject(elemental,"Fire Elemental","fire","H",false);
				getMonsterCreator().setupSide(elemental,"light","H",2,6,1,4,"lightorange");
				getMonsterCreator().setupSide(elemental,"dark","H",1,2,1,6,"orange");
				break;
			case Water:
				getMonsterCreator().setupGameObject(elemental,"Water Elemental","water","H",false);
				getMonsterCreator().setupSide(elemental,"light","H",0,4,0,2,"lightblue");
				getMonsterCreator().setupSide(elemental,"dark","H",0,2,0,4,"blue");
				break;
			case Air:
				getMonsterCreator().setupGameObject(elemental,"Air Elemental","air","M",false,true);
				getMonsterCreator().setupSide(elemental,"light","M",0,3,0,4,"white");
				getMonsterCreator().setupSide(elemental,"dark","H",0,4,0,4,"gray");
				break;
		}
		TileLocation tl = character.getCurrentLocation();
		character.addHireling(elemental);
		CombatWrapper combat = new CombatWrapper(elemental);
		combat.setSheetOwner(true);
		if (tl!=null && tl.isInClearing()) {
			tl.clearing.add(elemental,null);
		}
	}
	public ArrayList<GameObject> getOneOfEach(CharacterWrapper character) {
		applyTwo(character);
		applyThree(character);
		applyFour(character);
		applyFive(character);
		return getMonsterCreator().getMonstersCreated();
	}
}