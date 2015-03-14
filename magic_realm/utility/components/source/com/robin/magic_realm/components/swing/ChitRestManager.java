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
package com.robin.magic_realm.components.swing;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class ChitRestManager extends ChitManager {
	
	public ChitRestManager(JFrame parent,CharacterWrapper character,int currentCount) {
		super(parent,"Rest Chits",true,character,currentCount,true);
		initialize();
	}
	protected boolean canPressOkay() {
		return currentCount>=0;
	}
	protected boolean validateOkayButton() {
		if (currentCount>0) {
			int ret = JOptionPane.showConfirmDialog(parent,"You still have rests left.  Exit anyway?","Done Resting",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
			if (ret==JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}
	protected String getActionName() {
		return "resting";
	}
	protected int totalPossibleCount() {
		int val = 0;
		if (!character.hasCurse(Constants.WITHER)) { // only include fatigued chits if WITHER isn't in effect
			for (Iterator i=fatiguedChits.getAllChits().iterator();i.hasNext();) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
				val += chit.getEffortAsterisks();
			}
		}
		for (Iterator i=woundedChits.getAllChits().iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			int effort = chit.getEffortAsterisks();
			if (effort==0) {
				// effortless chits rest once to active
				val++;
			}
			else {
				// can rest twice to get back to active
				val += (effort*2);
			}
		}
		return val;
	}
	protected boolean canClickActive(CharacterActionChitComponent clickedChit) {
		if (currentCount==-1) { // making change
			if (clickedChit!=null) {
				int effort = clickedChit.getEffortAsterisks();
				if (effort==1) {
					if (!clickedChit.isColor() || !areActiveEffortChitsForChange()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	protected boolean canClickFatigue(CharacterActionChitComponent clickedChit) {
		if (currentCount>0) {
			if (clickedChit!=null) {
				int effort = clickedChit.getEffortAsterisks();
				
				// make sure character isn't cursed with WITHER (no active effort chits allowed)
				if (!character.hasCurse(Constants.WITHER)) {
					// go ahead
					if ((currentCount-effort)>=-1) { // allowed to dip one into the hole
						return true;
					}
				}
			}
		}
		return false;
	}
	protected boolean canClickWound(CharacterActionChitComponent clickedChit) {
		if (currentCount>0) {
			if (clickedChit!=null) {
				int effort = clickedChit.getEffortAsterisks();
				if (effort==0 || (currentCount-effort)>=-1) {
					return true;
				}
			}
		}
		return false;
	}
	protected void activeClick(CharacterActionChitComponent clickedChit) {
		if (currentCount==-1) { // making change
			if (clickedChit!=null) {
				int effort = clickedChit.getEffortAsterisks();
				if (effort==1) {
					moveChit(clickedChit,activeChits,fatiguedChits);
					currentCount += effort;
				}
			}
		}
	}
	protected void fatigueClick(CharacterActionChitComponent clickedChit) {
		if (currentCount>0) {
			if (clickedChit!=null) {
				int effort = clickedChit.getEffortAsterisks();
				
				// make sure character isn't cursed with WITHER (no active effort chits allowed)
				if (character.hasCurse(Constants.WITHER)) {
					JOptionPane.showMessageDialog(this,"Cannot have any active effort chits!","CURSE - WITHER",JOptionPane.WARNING_MESSAGE);
				}
				else if ((currentCount-effort)>=-1) { // allowed to dip one into the hole
					moveChit(clickedChit,fatiguedChits,activeChits);
					currentCount -= effort;
				}
			}
		}
	}
	protected void woundClick(CharacterActionChitComponent clickedChit) {
		if (currentCount>0) {
			if (clickedChit!=null) {
				int effort = clickedChit.getEffortAsterisks();
				if (effort==0) {
					moveChit(clickedChit,woundedChits,activeChits);
					effort = 1; // resting a wounded non-effort chit still requires one rest!
				}
				else if ((currentCount-effort)>=-1) { // allowed to dip one into the hole
					moveChit(clickedChit,woundedChits,fatiguedChits);
				}
				currentCount -= effort;
			}
		}
	}
	protected void updateStatusLabel(JLabel label) {
		if (currentCount==0) {
			label.setText("");
		}
		else if (currentCount>0) {
			label.setText("Rest "+currentCount+" asterisk"+(currentCount==1?"":"s"));
			label.setForeground(Color.black);
		}
		else {
			label.setText("Make change for "+(-currentCount)+" asterisk"+(currentCount==-1?"":"s"));
			label.setForeground(Color.red);
		}
		
	}
	private boolean areActiveEffortChitsForChange() {
		for (Iterator i=activeChits.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent aChit = (CharacterActionChitComponent)chit;
				if (!aChit.isColor() && aChit.getEffortAsterisks()==1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static CharacterWrapper TestWhiteKnight(RealmLoader loader,HostPrefWrapper hostPrefs) {
		GameObject character = loader.getData().getGameObjectByName("White Knight");
		System.out.println(character);
		
		// artifically fatigue and wound some chits
		CharacterWrapper wrapper = new CharacterWrapper(character);
		wrapper.setCharacterLevel(4);
		wrapper.updateLevelAttributes(hostPrefs);
		wrapper.initChits();
		ArrayList list = new ArrayList(wrapper.getAllChits());
		for (int i=2;i<5;i+=2) {
			CharacterActionChitComponent aChit = (CharacterActionChitComponent)list.get(i);
			aChit.makeFatigued();
		}
		for (int i=8;i<10;i++) {
			CharacterActionChitComponent aChit = (CharacterActionChitComponent)list.get(i);
			aChit.makeWounded();
		}
//		(new Curse(new JFrame())).applyThree(wrapper);
		return wrapper;
	}
	
	private static CharacterWrapper TestMagician(RealmLoader loader,HostPrefWrapper hostPrefs) {
		GameObject character = loader.getData().getGameObjectByName("Magician");
		System.out.println(character);
		
		CharacterWrapper wrapper = new CharacterWrapper(character);
		wrapper.setCharacterLevel(4);
		wrapper.updateLevelAttributes(hostPrefs);
		wrapper.initChits();
		for(CharacterActionChitComponent chit:wrapper.getAllChits()) {
			if (chit.isMagic() && chit.getMagicType().equals("VII")) {
				chit.makeFatigued();
			}
			else if (chit.isMagic() && chit.getMagicNumber()<5) {
				chit.enchant();
			}
			else if (chit.getEffortAsterisks()>0) {
				chit.makeFatigued();
			}
		}
		return wrapper;
	}
	
	/*
	 * Testing
	 */
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		ComponentTools.setSystemLookAndFeel();
		
		System.out.print("loading...");
		RealmLoader loader = new RealmLoader();
		HostPrefWrapper hostPrefs = HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		System.out.println("Done");
		
		CharacterWrapper character;
		character = TestWhiteKnight(loader,hostPrefs);
		character = TestMagician(loader,hostPrefs);
		
		ChitRestManager man = new ChitRestManager(new JFrame(),character,1);
		man.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.out.println("Exiting");
				System.exit(0);
			}
		});
		System.out.println("Count Too Large = "+man.isCountTooLarge());
		man.setVisible(true);
		
		if (!man.isFinished()) {
			System.out.println("Cancelled!");
		}
		System.exit(0);
	}
}