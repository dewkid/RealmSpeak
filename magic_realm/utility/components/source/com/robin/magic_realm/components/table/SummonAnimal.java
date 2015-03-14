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

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class SummonAnimal extends MonsterTable {

	public static final String KEY = "SummonAnimal";
	
	private enum AnimalType {
		Basilisk,
		Eagle,
		Bear,
		Wolf,
		Hawk,
		Squirrel,
	}
	
	public SummonAnimal(JFrame frame) {
		super(frame);
	}
	public String getTableKey() {
		return KEY;
	}
	public String getTableName(boolean longDescription) {
		return "Summon Animal";
	}
	public String getMonsterKey() {
		return "summoned_animal";
	}
	public String applyOne(CharacterWrapper character) {
		summonAnimal(character,AnimalType.Basilisk);
		return "Basilisk Summoned";
	}
	public String applyTwo(CharacterWrapper character) {
		summonAnimal(character,AnimalType.Eagle);
		return "Giant Eagle Summoned";
	}
	public String applyThree(CharacterWrapper character) {
		summonAnimal(character,AnimalType.Bear);
		return "Bear Summoned";
	}
	public String applyFour(CharacterWrapper character) {
		summonAnimal(character,AnimalType.Wolf);
		return "Wolf Summoned";
	}
	public String applyFive(CharacterWrapper character) {
		summonAnimal(character,AnimalType.Hawk);
		return "Hawk Summoned";
	}
	public String applySix(CharacterWrapper character) {
		summonAnimal(character,AnimalType.Squirrel);
		return "Squirrel Summoned";
	}
	private void summonAnimal(CharacterWrapper character, AnimalType type) {
		GameData data = character.getGameObject().getGameData();
		GameObject animal = getMonsterCreator().createOrReuseMonster(data);
		switch(type) {
			case Basilisk:
				getMonsterCreator().setupGameObject(animal,"Basilisk","basilisk","T",true);
				getMonsterCreator().setupSide(animal,"light","T",0,5,0,6,"lightgreen");
				getMonsterCreator().setupSide(animal,"dark","RED",0,5,0,6,"red");
				animal.setAttribute("dark","pins");
				break;
			case Eagle:
				getMonsterCreator().setupGameObject(animal,"Giant Eagle","eagle","H",false,true);
				getMonsterCreator().setupSide(animal,"light","M",0,3,0,4,"lightgreen");
				getMonsterCreator().setupSide(animal,"dark","M",0,3,0,4,"forestgreen");
				break;
			case Bear:
				getMonsterCreator().setupGameObject(animal,"Bear","bear","H",false);
				getMonsterCreator().setupSide(animal,"light","H",0,3,0,4,"lightgreen");
				getMonsterCreator().setupSide(animal,"dark","H",0,3,0,4,"forestgreen");
				break;
			case Wolf:
				getMonsterCreator().setupGameObject(animal,"Wolf","wolf","M",false);
				getMonsterCreator().setupSide(animal,"light","M",0,4,0,4,"lightgreen");
				getMonsterCreator().setupSide(animal,"dark","M",0,4,0,4,"forestgreen");
				break;
			case Hawk:
				getMonsterCreator().setupGameObject(animal,"Hawk","hawk","L",false,true);
				getMonsterCreator().setupSide(animal,"light","L",0,2,0,2,"lightgreen");
				getMonsterCreator().setupSide(animal,"dark","L",0,2,0,2,"forestgreen");
				break;
			case Squirrel:
				getMonsterCreator().setupGameObject(animal,"Squirrel","squirrel","L",false);
				getMonsterCreator().setupSide(animal,"light",null,0,0,0,2,"lightgreen");
				getMonsterCreator().setupSide(animal,"dark",null,0,0,0,2,"forestgreen");
				break;
		}
		TileLocation tl = character.getCurrentLocation();
		character.addHireling(animal);
		CombatWrapper combat = new CombatWrapper(animal);
		combat.setSheetOwner(true);
		if (tl!=null && tl.isInClearing()) {
			tl.clearing.add(animal,null);
		}
		character.getGameObject().add(animal); // so that you don't have to assign as a follower right away
	}
}