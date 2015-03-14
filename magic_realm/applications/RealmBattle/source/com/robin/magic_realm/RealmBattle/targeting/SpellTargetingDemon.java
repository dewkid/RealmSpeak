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

import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmObjectMaster;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellTargetingDemon extends SpellTargetingSpecial {

	public SpellTargetingDemon(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}
	public boolean hasTargets() {
		return !gameObjects.isEmpty();  // always true
	}

	public boolean populate(BattleModel battleModel,RealmComponent activeParticipant) {
		ArrayList allDenizens = combatFrame.findCanBeSeen(battleModel.getAllBattleParticipants(true),true);
		ArrayList allParticipantsSansDenizens = combatFrame.findCanBeSeen(battleModel.getAllBattleParticipants(false),true);
		allDenizens.removeAll(allParticipantsSansDenizens);
		for (Iterator i=allDenizens.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.isMonster()) {
				String icon = rc.getGameObject().getAttribute(rc.getThisBlock(),"icon_type");
				if (icon.startsWith("demon")) {
					gameObjects.add(rc.getGameObject());
				}
			}
		}
		if (gameObjects.isEmpty()) {
			return true;
		}
		
		// Pick Demon
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(combatFrame,"Select a Target for "+spell.getName()+":",true);
		chooser.addGameObjects(gameObjects,false);
		chooser.setVisible(true);
		if (chooser.getSelectedText()==null) {
			return false;
		}
		MonsterChitComponent demon = (MonsterChitComponent)chooser.getFirstSelectedComponent();
		
		// Pick a Player
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(battleModel.getGameData()).getPlayerCharacterObjects());
		ArrayList<String> playerNames = new ArrayList<String>();
		ArrayList<GameObject> list = pool.find(CharacterWrapper.NAME_KEY);
		for (GameObject go:list) {
			CharacterWrapper character = new CharacterWrapper(go);
			String name = character.getPlayerName();
			if (!playerNames.contains(name)) {
				playerNames.add(name);
			}
		}
//		playerNames.remove((new CharacterWrapper(activeParticipant.getGameObject())).getPlayerName());
		if (playerNames.isEmpty()) {
			gameObjects.clear();
			return true;
		}
		
		ButtonOptionDialog nameChooser = new ButtonOptionDialog(combatFrame,demon.getIcon(),"Which player do you want information from?","Ask Demon",true);
		nameChooser.addSelectionObjects(playerNames);
		nameChooser.setVisible(true);
		String playerName = (String)nameChooser.getSelectedObject();
		if (playerName==null) {
			return false;
		}
		
		// Select a Question
		String input = (String)JOptionPane.showInputDialog(
							combatFrame,
							"You must ask a question that can be answered with a\n"
							+"yes or a no, or a number.  You CANNOT ask about future\n"
							+"intents, only about past or present conquests.\n\nWhat do you ask?",
							"Ask Demon",
							JOptionPane.QUESTION_MESSAGE,demon.getIcon(),null,null);
		
		if (input==null) {
			return false;
		}
		
		// Finally, we can assign this info
		spell.addTarget(combatFrame.getHostPrefs(),demon.getGameObject());
		spell.setExtraIdentifier(playerName + Constants.DEMON_Q_DELIM + input);
		return true;
	}
}