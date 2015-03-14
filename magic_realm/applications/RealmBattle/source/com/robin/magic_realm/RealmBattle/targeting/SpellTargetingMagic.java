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

import java.util.Collection;
import java.util.Iterator;

import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingMagic extends SpellTargetingMultiple {

	public SpellTargetingMagic(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		// Assume that activeParticipant IS character
		CharacterWrapper character = new CharacterWrapper(activeParticipant.getGameObject());
		String targetType = spell.getGameObject().getThisAttribute("target");
		int paren1 = targetType.indexOf("(");
		int paren2 = targetType.indexOf(")");
		if (paren1>0 && paren2>paren1) {
			String chitList = targetType.substring(paren1+1,paren2);
			Collection allChits = character.getActiveMagicChits();
			Collection types = null;
			if (!"all".equals(chitList)) {
				types = StringUtilities.stringToCollection(chitList,",");
			}
			for (Iterator i=allChits.iterator();i.hasNext();) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
				if (types==null || types.contains(chit.getMagicType())) {
					gameObjects.add(chit.getGameObject());
				}
			}
		}
		return true;
	}
}