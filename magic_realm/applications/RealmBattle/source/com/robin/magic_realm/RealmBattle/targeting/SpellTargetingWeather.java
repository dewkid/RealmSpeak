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

import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingWeather extends SpellTargetingSpecial {

	public SpellTargetingWeather(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		// use option pane here to ask "see weather" or "change weather"
		// I really shouldn't put the treasure name here, but I don't see that it will ever matter unless I add new
		// treasures that can control the weather (doubt it)
		ButtonOptionDialog dialog = new ButtonOptionDialog(combatFrame,null,"",spell.getGameObject().getName(),true);
		dialog.addSelectionObject("See the Weather Chit");
		dialog.addSelectionObject("Change the Weather Chit");
		dialog.setVisible(true);
		String val = (String)dialog.getSelectedObject();
		if (val!=null) {
			spell.addTarget(combatFrame.getHostPrefs(), spell.getGameObject());
			spell.setExtraIdentifier(val);
			return true;
		}
		return false;
	}
}