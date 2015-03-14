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

import java.util.*;

import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.wrapper.*;

public class SpellTargetingClearing extends SpellTargetingSpecial {

	public SpellTargetingClearing(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}
	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		
		// Assume that activeParticipant IS character
		CharacterWrapper character = new CharacterWrapper(activeParticipant.getGameObject());
	
		ArrayList<String> clearingTargetType = spell.getGameObject().getThisAttributeList("target_clearing");
		if (clearingTargetType.contains("combatants")) {
			ArrayList allBattleParticipants = battleModel.getAllBattleParticipants(true); // clearing affects everything, including hidden!!!
			for (Iterator i=allBattleParticipants.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				gameObjects.add(rc.getGameObject());
			}
		}
		if (clearingTargetType.contains("monsters")) {
			ArrayList allBattleParticipants = battleModel.getAllBattleParticipants(true); // clearing affects everything, including hidden!!!
			for (Iterator i=allBattleParticipants.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isMonster()) {
					gameObjects.add(rc.getGameObject());
				}
			}
		}
		if (clearingTargetType.contains("spells")) {
			SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(spell.getGameObject().getGameData());
			for (Iterator i=sm.getAllSpellsInClearing(battleModel.getBattleLocation(),true).iterator();i.hasNext();) {
				SpellWrapper sw = (SpellWrapper)i.next();
				gameObjects.add(sw.getGameObject());
			}
		}
		if (clearingTargetType.contains("curses")) {
			for (Iterator i=battleModel.getAllParticipatingCharacters().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				CharacterWrapper thisCharacter = new CharacterWrapper(rc.getGameObject());
				Collection curses = thisCharacter.getAllCurses();
				if (curses.size()>0) {
					gameObjects.add(rc.getGameObject());
				}
			}
		}
		boolean ignorebattle = spell.getGameObject().hasThisAttribute("nobattle");
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject theTarget = (GameObject)i.next();
			spell.addTarget(combatFrame.getHostPrefs(),theTarget,ignorebattle);
			if (!ignorebattle) {
				combatFrame.makeWatchfulNatives(RealmComponent.getRealmComponent(theTarget),true);
			}
			CombatFrame.broadcastMessage(character.getGameObject().getName(),"Targets the "+theTarget.getName()+" with "+spell.getGameObject().getName());
		}
		if (!gameObjects.isEmpty()) {
			JOptionPane.showMessageDialog(combatFrame,"All valid targets are selected.",spell.getName(),JOptionPane.INFORMATION_MESSAGE);
		}
		if (clearingTargetType.contains("clearing")) {
			// Affects the clearing itself
			TileLocation loc = battleModel.getBattleLocation();
			
			spell.addTarget(combatFrame.getHostPrefs(),loc.tile.getGameObject(),true);
			spell.setExtraIdentifier(String.valueOf(loc.clearing.getNum()));
			
			JOptionPane.showMessageDialog(combatFrame,"The current clearing is selected.",spell.getName(),JOptionPane.INFORMATION_MESSAGE);
			CombatFrame.broadcastMessage(character.getGameObject().getName(),"Targets clearing "
					+loc.clearing.getNum()
					+" of the "+loc.tile.getGameObject().getName()+".");
		}
		return true;
	}
}