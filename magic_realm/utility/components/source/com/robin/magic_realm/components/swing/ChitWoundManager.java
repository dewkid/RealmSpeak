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
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ChitWoundManager extends ChitManager {
	
	public ChitWoundManager(JFrame parent,CharacterWrapper character,int woundCount) {
		super(parent,character.getCharacterName()+" - Wound Chits",true,character,woundCount);
		initialize();
	}
	protected String getActionName() {
		return "wounding";
	}
	protected boolean canPressOkay() {
		return currentCount==0;
	}
	protected int totalPossibleCount() {
		return activeChits.getAllChits().size() + fatiguedChits.getAllChits().size();
	}
	protected boolean canClickActive(CharacterActionChitComponent clickedChit) {
		if (currentCount>0) {
			if (clickedChit!=null) {
				if (clickedChit.isColor()) {
					// Color chits can only be wounded if there are NO woundable non-color chits
					for (Iterator i=activeChits.getAllChits().iterator();i.hasNext();) {
						ChitComponent chit = (ChitComponent)i.next();
						if (chit.isActionChit()) {
							CharacterActionChitComponent aChit = (CharacterActionChitComponent)chit;
							if (!aChit.isColor()) {
								return false;
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	protected boolean canClickFatigue(CharacterActionChitComponent clickedChit) {
		if (currentCount>0 && activeChits.getAllChits().size()==0) {
			if (clickedChit!=null) {
				return true;
			}
		}
		return false;
	}
	protected boolean canClickWound(CharacterActionChitComponent clickedChit) {
		return false;
	}
	protected void activeClick(CharacterActionChitComponent clickedChit) {
		if (currentCount>0) {
			if (clickedChit!=null) {
				// go ahead
				moveChit(clickedChit,activeChits,woundedChits);
				currentCount -= 1;
			}
		}
	}
	protected void fatigueClick(CharacterActionChitComponent clickedChit) {
		// only allow this if all active chits are wounded.
		if (currentCount>0 && activeChits.getAllChits().size()==0) {
			if (clickedChit!=null) {
				// go ahead
				moveChit(clickedChit,fatiguedChits,woundedChits);
				currentCount -= 1;
			}
		}
	}
	protected void woundClick(CharacterActionChitComponent clickedChit) {
		// does nothing
	}
	protected void updateStatusLabel(JLabel label) {
		if (currentCount==0) {
			label.setText("");
		}
		else if (currentCount>0) {
			label.setText("Wound "+currentCount+" chit"+(currentCount==1?"":"s"));
			label.setForeground(Color.black);
		}
	}
	
	/*
	 * Testing
	 */
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		ComponentTools.setSystemLookAndFeel();
		
		System.out.print("loading...");
		RealmLoader loader = new RealmLoader();
		System.out.println("Done");
		
		GameObject character = loader.getData().getGameObjectByName("Wizard");
		System.out.println(character);
		
		CharacterWrapper wrapper = new CharacterWrapper(character);
		
		// artifically fatigue and wound some chits
		ArrayList list = new ArrayList(wrapper.getAllChits());
		Collections.sort(list);
		int n=0;
		for (Iterator i=list.iterator();i.hasNext();) {
			CharacterActionChitComponent aChit = (CharacterActionChitComponent)i.next();
			System.out.println((n++)+" "+aChit.getGameObject().getName());
		}
		CharacterActionChitComponent aChit = (CharacterActionChitComponent)list.get(3);
		aChit.makeFatigued();
		for (int i=4;i<9;i++) {
			aChit = (CharacterActionChitComponent)list.get(i);
			aChit.makeWounded();
		}
		aChit = (CharacterActionChitComponent)list.get(11);
		aChit.enchant();
//		(new Curse(new JFrame())).applyThree(wrapper);
		
		ChitWoundManager man = new ChitWoundManager(new JFrame(),wrapper,100);
		man.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.out.println("Exiting");
				System.exit(0);
			}
		});
		System.out.println("Count Too Large = "+man.isCountTooLarge());
		man.setVisible(true);
	}
}