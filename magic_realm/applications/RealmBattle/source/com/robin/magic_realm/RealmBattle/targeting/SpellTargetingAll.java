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

import java.util.Iterator;

import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public abstract class SpellTargetingAll extends SpellTargeting {
	public SpellTargetingAll(CombatFrame combatFrame,SpellWrapper spell) {
		super(combatFrame,spell);
	}
	public boolean hasTargets() {
		return !gameObjects.isEmpty();
	}
	public boolean assign(HostPrefWrapper hostPrefs,CharacterWrapper activeCharacter) {
		boolean ignorebattle = spell.getGameObject().hasThisAttribute("nobattle");
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject theTarget = (GameObject)i.next();
			spell.addTarget(hostPrefs,theTarget,ignorebattle);
			if (!ignorebattle) {
				combatFrame.makeWatchfulNatives(RealmComponent.getRealmComponent(theTarget),true);
			}
			CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Targets the "+theTarget.getName()+" with "+spell.getGameObject().getName());
		}
		JOptionPane.showMessageDialog(combatFrame,"All valid targets are selected.",spell.getName()+" Selects ALL",JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
}