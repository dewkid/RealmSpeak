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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.RealmBattle.CombatSheet;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public abstract class SpellTargetingSingle extends SpellTargeting {
	
	protected ArrayList identifiers = new ArrayList();
	protected Hashtable secondaryTargets = new Hashtable(); // A hash of lists by identifier
	protected String secondaryTargetChoiceString = "";

	protected SpellTargetingSingle(CombatFrame combatFrame, SpellWrapper spell) {
		super(combatFrame, spell);
	}
	public boolean hasTargets() {
		return !(gameObjects.isEmpty() && secondaryTargets.isEmpty());
	}
	public boolean assign(HostPrefWrapper hostPrefs, CharacterWrapper activeCharacter) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(combatFrame,"Select a Target for "+spell.getName()+":",true);
		if (identifiers.isEmpty()) {
			if (gameObjects.isEmpty()) {
				ArrayList list = new ArrayList(secondaryTargets.keySet());
				Collections.sort(list);
				chooser.addStrings(list);
			}
			else {
				// Rather than just dump them in, there should be a small icon indicating which sheet they are on
				Hashtable hash = new Hashtable();
				ArrayList combatSheets = combatFrame.getAllCombatSheets();
				for (Iterator i=combatSheets.iterator();i.hasNext();) {
					CombatSheet sheet = (CombatSheet)i.next();
					RealmComponent aSheetOwner = sheet.getSheetOwner();
					Collection c = sheet.getAllParticipantsOnSheet();
					for (Iterator n=c.iterator();n.hasNext();) {
						RealmComponent sp = (RealmComponent)n.next();
						hash.put(sp,aSheetOwner);
					}
				}
				
				for (Iterator i=gameObjects.iterator();i.hasNext();) {
					GameObject gameObject = (GameObject)i.next();
					RealmComponent rc = RealmComponent.getRealmComponent(gameObject);
					RealmComponent aSheetOwner = (RealmComponent)hash.get(rc);
					String option = chooser.generateOption();
					if (aSheetOwner!=null) {
						chooser.addRealmComponentToOption(option,aSheetOwner,RealmComponentOptionChooser.DisplayOption.MediumIcon);
					}
					if (rc.isSpell()) {
						updateChooserWithContent(chooser,option,rc);
					}
					chooser.addRealmComponentToOption(option,rc);
				}
			}
		}
		else {
			for (int i=0;i<identifiers.size();i++) {
				String identifier = (String)identifiers.get(i);
				GameObject pick = (GameObject)gameObjects.get(i);
				RealmComponent rc = RealmComponent.getRealmComponent(pick);
				String option = identifier+i;
				chooser.addOption(option,identifier);
				if (rc.isSpell()) {
					updateChooserWithContent(chooser,option,rc);
				}
				chooser.addRealmComponentToOption(option,rc);
			}
		}
		chooser.setVisible(true);
		String selText = chooser.getSelectedText();
		if (selText!=null) {
			RealmComponent theTarget = chooser.getLastSelectedComponent();
			if (theTarget==null) {
				CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Targets the "+selText+" with "+spell.getGameObject().getName());
				ArrayList list = (ArrayList)secondaryTargets.get(selText);
				for (Iterator i=list.iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					spell.addTarget(hostPrefs,rc.getGameObject());
					combatFrame.makeWatchfulNatives(rc,true);
				}
			}
			else {
				spell.addTarget(hostPrefs,theTarget.getGameObject());
				combatFrame.makeWatchfulNatives(theTarget,true);
				CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Targets the "+theTarget.getGameObject().getName()+" ("+selText+") with "+spell.getGameObject().getName());
				if (selText.trim().length()>0) {
					spell.setExtraIdentifier(selText);
				}
				if (!secondaryTargets.isEmpty()) {
					chooser = new RealmComponentOptionChooser(combatFrame,secondaryTargetChoiceString,false);
					Hashtable hash = new Hashtable();
					ArrayList list = (ArrayList)secondaryTargets.get(selText);
					for (Iterator i=list.iterator();i.hasNext();) {
						GameObject st = (GameObject)i.next();
						String name = st.getName();
						chooser.addOption(name,name); // FIXME This assumes names only here
						hash.put(name,st);
					}
					chooser.setVisible(true);
					selText = chooser.getSelectedText();
					if (selText!=null) {
						GameObject st = (GameObject)hash.get(selText);
						spell.setSecondaryTarget(st);
					} // shouldn't ever be null with no cancel button!
				}
			}
			return true;
		}
		return false;
	}
	private void updateChooserWithContent(RealmComponentOptionChooser chooser,String option,RealmComponent rc) {
		for (Iterator h=rc.getGameObject().getHold().iterator();h.hasNext();) {
			GameObject hgo = (GameObject)h.next();
			chooser.addGameObjectToOption(option,hgo);
		}
	}
}