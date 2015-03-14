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
package com.robin.magic_realm.RealmBattle.targeting;

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.TileComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.RealmObjectMaster;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingIndividualPlusHex extends SpellTargetingIndividual {
	public SpellTargetingIndividualPlusHex(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}
	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		super.populate(battleModel,activeParticipant);
		
		secondaryTargetChoiceString = "Select a tile to FLY the target to:";
		TileLocation here = battleModel.getBattleLocation();
		
		ArrayList adjTiles = new ArrayList();
		for (Iterator i=here.tile.getAllAdjacentTiles().iterator();i.hasNext();) {
			TileComponent tile = (TileComponent)i.next();
			adjTiles.add(tile.getGameObject());
		}
		
		if (adjTiles.isEmpty()) { // this only happens during battle simulator
			RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(battleModel.getGameData());
			adjTiles.addAll(rom.getTileObjects());
		}
		
		for (GameObject go : gameObjects) {
			identifiers.add(go.getName());
			secondaryTargets.put(go.getName(),adjTiles);
		}
		return true;
	}
}