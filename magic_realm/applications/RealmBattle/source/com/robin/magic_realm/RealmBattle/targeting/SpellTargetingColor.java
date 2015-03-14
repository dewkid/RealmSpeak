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

import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.ColorMod;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingColor extends SpellTargetingSingle {

	protected SpellTargetingColor(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		ColorMod colorMod = ColorMod.createColorMod(spell.getGameObject());
		
		// Character Chits
		for(RealmComponent rc:battleModel.getAllParticipatingCharacters()){
			CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
			for (CharacterActionChitComponent chit:character.getColorChits()) {
				if (colorMod.willAffect(chit.getColorMagic())) {
					gameObjects.add(chit.getGameObject());
				}
			}
		}
		
		// Enchanted Tile
		if (battleModel.getBattleLocation().tile.isEnchanted()) {
			TileComponent tile = battleModel.getBattleLocation().tile;
			gameObjects.add(tile.getGameObject());
		}
		
		// Permanent sources (color_source)
		for (RealmComponent rc:battleModel.getBattleLocation().clearing.getDeepClearingComponents()) {
			ColorMagic cm = SpellUtility.getColorMagicFor(rc);
			if (cm!=null && colorMod.willAffect(cm)) {
				gameObjects.add(rc.getGameObject());
			}
		}
		
		return true;
	}
}