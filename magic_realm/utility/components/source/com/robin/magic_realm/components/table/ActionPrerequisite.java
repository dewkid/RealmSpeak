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
package com.robin.magic_realm.components.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ActionPrerequisite {
	private GameObject source;
	private String messageText;
	
	private boolean fatigueAsterisk = false;
	private boolean fatigueT = false;
	private boolean needKey = false;
	
	private StringBuffer failReason = new StringBuffer();

	private ActionPrerequisite(GameObject source,String input,String messageText) {
		this.source = source;
		this.messageText = messageText;
		fatigueAsterisk = input.indexOf(Constants.FATIGUE_ASTERISK)>=0;
		fatigueT = input.indexOf(Constants.FATIGUE_TREMENDOUS)>=0;
		needKey = input.indexOf(Constants.KEY)>=0;
	}
	public String toString() {
		return "fa("+fatigueAsterisk+"),ft("+fatigueT+"),nk("+needKey+")";
	}
	
	public String getFailReason() {
		return failReason.toString();
	}
	private boolean hasLostKeys(CharacterWrapper character) {
		if (character.getGameObject().hasThisAttribute(Constants.PICKS_LOCKS)) {
			// this new custom ability can unlock anything
			return true;
		}
		
		// Otherwise, look for the lost keys
		GamePool pool = new GamePool();
		pool.addAll(character.getActivatedTreasureObjects());
		
		ArrayList query = new ArrayList();
		query.add("key");
		if (source.hasThisAttribute(Constants.BOARD_NUMBER)) {
			query.add(Constants.BOARD_NUMBER+"="+source.getThisAttribute(Constants.BOARD_NUMBER));
		}
		else {
			query.add("!"+Constants.BOARD_NUMBER);
		}
		
		return (!pool.find(query).isEmpty());
	}
	public boolean canFullfill(JFrame frame,CharacterWrapper character,ChangeListener listener) {
		return fullfilled(frame,character,listener,false);
	}
	public boolean fullfilled(JFrame frame,CharacterWrapper character,ChangeListener listener) {
		return fullfilled(frame,character,listener,true);
	}
	private boolean fullfilled(JFrame frame,CharacterWrapper character,ChangeListener listener,boolean performAction) {
		boolean success = false;
		failReason = new StringBuffer();
		if (character.hasActiveInventoryThisKey(Constants.LOCKPICK)) {
			fatigueAsterisk = true;
		}
		if (needKey) {
			success = testKey(character);
		}
		if (!success && fatigueT) { // only check if no success yet
			success = testFatigueTremendous(frame,character,listener,performAction);
		}
		if (!success && fatigueAsterisk) {
			success = testFatigueAsterisk(frame,character,performAction);
		}
		return success;
	}
	private boolean testKey(CharacterWrapper character) {
		boolean success = false;
		// First, see if character has Lost Keys treasure
		if (hasLostKeys(character)) {
			success = true;
		}
		else {
			String boardNum = "";
			if (source.hasThisAttribute(Constants.BOARD_NUMBER)) {
				boardNum = " "+source.getThisAttribute(Constants.BOARD_NUMBER);
			}
			failReason.append("you don't have the Lost Keys"+boardNum+" activated");
		}
		// else Perform and Non-perform success!
		return success;
	}
	private boolean testFatigueTremendous(JFrame frame,CharacterWrapper character,ChangeListener listener,boolean performAction) {
		boolean success = false;
		// Try fatiguing T
		Strength tStrength = new Strength("T");
		Strength wishStrength = character.getWishStrength();
		if (tStrength.equalTo(character.getMoveStrength(false,true)) || tStrength.equalTo(character.getFightStrength(false,true))) {
			// Having strength from horse,boots is enough to satisfy the requirement (see rule 9.3/3b)
			success = true;
		}
		else {
			// Instead, you need to fatigue a T chit
			ArrayList tremendousChits = new ArrayList();
			Collection active = character.getActiveChits();
			for (Iterator i=active.iterator();i.hasNext();) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
				if ("T".equals(chit.getStrength().toString())) {
					tremendousChits.add(chit);
				}
			}
			boolean hasTWishStrength = wishStrength!=null && wishStrength.strongerOrEqualTo(tStrength);
			if (!tremendousChits.isEmpty() || hasTWishStrength) {
				if (performAction) {
					RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Select a chit to fatigue:",true);
					chooser.addRealmComponents(tremendousChits,false);
					if (hasTWishStrength) {
						chooser.addOption("WISH_","WISH Strength");
					}
					chooser.setVisible(true);
					String selText = chooser.getSelectedText();
					if (selText!=null) {
						if ("WISH Strength".equals(selText)) {
							success = true;
							character.clearWishStrength();
						}
						else {
							GameObject toFatigue = chooser.getFirstSelectedComponent().getGameObject();
							if (toFatigue!=null) {
								CharacterActionChitComponent chit = (CharacterActionChitComponent)RealmComponent.getRealmComponent(toFatigue);
								chit.makeFatigued();
								RealmUtility.reportChitFatigue(character,chit,"Fatigued chit: ");
								if (chit.isFight()) {
									character.clearWishStrength(); // in case that was used
								}
								listener.stateChanged(new ChangeEvent(this));
								success = true;
							}
						}
					}
					else {
						if (failReason.length()>0) {
							failReason.append(" and");
						}
						failReason.append(" you didn't fatigue any T chits");
					}
				}
				else {
					success = true;
				}
			}
			else {
				// no T chits available
				if (failReason.length()>0) {
					failReason.append(" and");
				}
				failReason.append(" you don't have any active Tremendous items, chits, or hirelings to use");
			}
		}
		return success;
	}
	private boolean testFatigueAsterisk(JFrame frame,CharacterWrapper character,boolean performAction) {
		boolean success = false;
		if (character.isHiredLeader()
				|| character.isControlledMonster()
				|| character.getTransmorph()!=null
				|| character.hasActiveInventoryThisKey(Constants.NO_FATIGUE)) {
			// Leaders, monsters, and transmorphed ALWAYS fatigue for free
			// If you have the NO_FATIGUE option active, that's good too!
			success = true;
		}
		else {
			if (performAction) {
				if (selectAndFatigueChit(frame,character)) {
					success = true;
				}
				else {
					if (failReason.length()>0) {
						failReason.append(" and");
					}
					failReason.append(" you didn't fatigue any effort chits.");
				}
			}
			else {
				if (character.getActiveEffortChits().isEmpty()) {
					if (failReason.length()>0) {
						failReason.append(" and");
					}
					failReason.append(" you don't have any active effort asterisks to fatigue");
				}
				else {
					success = true;
				}
			}
		}
		return success;
	}
	private boolean selectAndFatigueChit(JFrame frame,CharacterWrapper character) {
		Collection active = character.getActiveEffortChits();
		if (active.isEmpty()) {
			JOptionPane.showMessageDialog(frame,"You don't have any active chits to fatigue!","No Chits to Fatigue",JOptionPane.WARNING_MESSAGE);
			return false;
		}
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"You must fatigue a chit to "+messageText+" this site:",false);
		int keyN = 0;
		for (Iterator i=active.iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			String key = "N"+(keyN++);
			chooser.addOption(key,"Fatigue");
			chooser.addRealmComponentToOption(key,chit);
		}
		chooser.setVisible(true);
		String text = chooser.getSelectedText();
		if (text!=null) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)chooser.getFirstSelectedComponent();
			
			if (chit.getEffortAsterisks()==2) {
				// Need to make change
				Collection fatigued = character.getFatiguedChits(); // In case you need to make change
				ArrayList singleAsteriskFatiguedChits = new ArrayList();
				for (Iterator i=fatigued.iterator();i.hasNext();) {
					CharacterActionChitComponent fatiguedChit = (CharacterActionChitComponent)i.next();
					if (fatiguedChit.getEffortAsterisks()==1) {
						singleAsteriskFatiguedChits.add(fatiguedChit);
					}
				}
				if (singleAsteriskFatiguedChits.isEmpty()) {
					int ret = JOptionPane.showConfirmDialog(frame,"There are no single asterisk chits to make change.  Fatigue anyway?","Fatiguing Two Asterisk Chit Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if (ret==JOptionPane.NO_OPTION) {
						return false;
					}
				}
				else {
					chooser = new RealmComponentOptionChooser(frame,"Select a fatigued chit for which to make change:",true);
					chooser.addRealmComponents(singleAsteriskFatiguedChits,false);
					chooser.setVisible(true);
					if (chooser.getSelectedText()!=null) {
						CharacterActionChitComponent fatiguedChit = (CharacterActionChitComponent)chooser.getFirstSelectedComponent();
						fatiguedChit.makeActive();
					}
					else {
						// Cancelled
						return false;
					}
				}
			}
			
			chit.makeFatigued();
			RealmUtility.reportChitFatigue(character,chit,"Fatigued chit: ");
			return true;
		}
		return false;
	}
	public static ActionPrerequisite getActionPrerequisite(GameObject source,String input,String messageText) {
		if (input != null) {
			return new ActionPrerequisite(source,input,messageText);
		}
		return null;
	}
}