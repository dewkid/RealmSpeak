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
import java.util.Collection;
import java.util.Iterator;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.SoundChitComponent;
import com.robin.magic_realm.components.TileComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.RealmObjectMaster;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingSound extends SpellTargetingSingle {

	protected SpellTargetingSound(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		secondaryTargetChoiceString = "Select a tile to move the sound to:";
		TileLocation here = battleModel.getBattleLocation();
		GameData gameData = spell.getGameObject().getGameData();
		GamePool pool = new GamePool(gameData.getGameObjects());
		Collection tiles = RealmObjectMaster.getRealmObjectMaster(gameData).getTileObjects();
		ArrayList sixClearingTiles = new ArrayList();
		for (Iterator i=tiles.iterator();i.hasNext();) {
			GameObject tile = (GameObject)i.next();
			TileComponent tc = (TileComponent)RealmComponent.getRealmComponent(tile);
			if (tc.getClearingCount()==6) {
				sixClearingTiles.add(tile);
			}
		}
		Collection c = pool.find("sound,chit");
		for (Iterator i=c.iterator();i.hasNext();) {
			GameObject soundChitObject = (GameObject)i.next();
			GameObject tile = soundChitObject.getHeldBy();
			if (tile!=null && !tile.hasThisAttribute("tile")) {
				tile = tile.getHeldBy(); // this jumps up one from lost castle or city
			}
			if (tile!=null) {
				SoundChitComponent soundChit = (SoundChitComponent)RealmComponent.getRealmComponent(soundChitObject);
				if (soundChit.isFaceUp()) {
					gameObjects.add(soundChitObject);
					identifiers.add(tile.getName());
					ArrayList tileChoices = new ArrayList();
					if (here.tile.getGameObject().equals(tile)) {
						// Moving sound from here to somewhere else
						tileChoices.addAll(sixClearingTiles);
						tileChoices.remove(here.tile.getGameObject());
					}
					else {
						// Moving sound from somewhere else to here
						tileChoices.add(here.tile.getGameObject());
					}
					secondaryTargets.put(tile.getName(),tileChoices);
				}
			}
		}
		return true;
	}
}