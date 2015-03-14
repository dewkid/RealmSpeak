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

import javax.swing.JOptionPane;

import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingTile extends SpellTargetingSpecial {

	public SpellTargetingTile(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}
	
	public boolean populate(BattleModel battleModel, RealmComponent activeParticipant) {
		// Target the spellcaster's clearing
		TileLocation loc = battleModel.getBattleLocation();
		spell.addTarget(combatFrame.getHostPrefs(),loc.tile.getGameObject(),true);
		CombatFrame.broadcastMessage(activeParticipant.getGameObject().getName(),"Targets the "+loc.tile.getGameObject().getName());
		JOptionPane.showMessageDialog(combatFrame,"The current tile was selected as the target.");
		return true;
	}
}