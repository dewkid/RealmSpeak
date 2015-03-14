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
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.swing.ComponentTools;
import com.robin.general.util.StringBufferedList;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class ChitFatigueManager extends ChitManager {
	
	private static int INFINITE = 1000;
	
	private int initMove;
	private int initFight;
	private int initMagic;
	
	private int move;
	private int fight;
	private int magic;
	
	private int lostAsterisks;
	
	private JLabel fatigueInfoLabel;
	
	private static final int TYPE_NA = 0;
	private static final int TYPE_MOVE = 1;
	private static final int TYPE_FIGHT = 2;
//	private static final int TYPE_MAGIC = 3;
	private int makeChangeType = TYPE_NA;
	
	public ChitFatigueManager(JFrame parent,CharacterWrapper character,int fatigueCount) {
		this(parent,character,fatigueCount,INFINITE,INFINITE,INFINITE);
	}
	public ChitFatigueManager(JFrame parent,CharacterWrapper character,int fatigueCount,int move,int fight,int magic) {
		super(parent,character.getCharacterName()+" - Fatigue Chits",true,character,fatigueCount);
		this.initMove = move;
		this.initFight = fight;
		this.initMagic = magic;
		lostAsterisks = 0;
		this.move = initMove;
		this.fight = initFight;
		this.magic = initMagic;
		initialize();
		
		StringBufferedList sb = new StringBufferedList();
		if (move>0 && move<INFINITE) {
			sb.append("MOVE "+"******".substring(6-move)); // 6 is a ridiculous amount... that's why I chose it.  :-) 
		}
		if (fight>0 && fight<INFINITE) {
			sb.append("FIGHT "+"******".substring(6-fight)); 
		}
		if (magic>0 && magic<INFINITE) {
			sb.append("MAGIC "+"******".substring(6-magic)); 
		}
		
		String info = sb.size()==0?"":("Used "+sb.toString());
		fatigueInfoLabel = new JLabel(info,JLabel.CENTER);
		southDisplay.add(fatigueInfoLabel,"North");
	}
	protected String getActionName() {
		return "fatiguing";
	}
	protected void resetChits() {
		super.resetChits();
		lostAsterisks = 0;
		makeChangeType = TYPE_NA;
		move = initMove;
		fight = initFight;
		magic = initMagic;
	}
	protected boolean needsToFatigue() {
		return currentCount>0 && move>=0 && fight>=0 && magic>=0;
	}
	protected boolean needsToMakeChange() {
		return currentCount==-1 || move<0 || fight<0 || magic<0;
	}
	protected boolean canMakeChange() {
		for (Iterator i=fatiguedChits.getAllChits().iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			int effort = chit.getEffortAsterisks();
			if (effort==1 && validChit(chit,true)) {
				// They CAN make change
				return true;
			}
		}
		return false;
	}
	protected boolean canPressOkay() {
		if (needsToMakeChange()) {
			return !canMakeChange();
		}
		
		return !needsToFatigue();
	}
	protected boolean validChit(CharacterActionChitComponent chit) {
		return validChit(chit,false);
	}
	protected boolean validChit(CharacterActionChitComponent chit,boolean makingChange) {
		int effort = chit.getEffortAsterisks();
		boolean validMove =
			validEffortAdjustment(move,effort,makingChange)
			&& countsAsMove(chit)
			&& (makeChangeType==TYPE_NA || makeChangeType==TYPE_MOVE);
		boolean validFight =
			validEffortAdjustment(fight,effort,makingChange)
			&& countsAsFight(chit)
			&& (makeChangeType==TYPE_NA || makeChangeType==TYPE_FIGHT);
		boolean validMagic =
			validEffortAdjustment(magic,effort,makingChange)
			&& countsAsMagic(chit);
		return validMove || validFight || validMagic;
	}
	private boolean validEffortAdjustment(int testValue,int effort,boolean makingChange) {
		if (makingChange) {
			return testValue<0 || currentCount==-1;
		}
		return testValue>0 && testValue-effort>=-1;
	}
	protected boolean countsAsMove(CharacterActionChitComponent chit) {
		return chit.isMove() || chit.isFly() || chit.isAnyEffort();
	}
	protected boolean countsAsFight(CharacterActionChitComponent chit) {
		return chit.isFight() || chit.isFightAlert() || chit.isAnyEffort();
	}
	protected boolean countsAsMagic(CharacterActionChitComponent chit) {
		return chit.isColor() || chit.isMagic() || chit.isAnyEffort();
	}
	protected int totalPossibleCount() {
		int val = 0;
		for (Iterator i=activeChits.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit() && validChit((CharacterActionChitComponent)chit)) {
				int effort = ((CharacterActionChitComponent)chit).getEffortAsterisks();
				val += effort;
				val += 1; // Add one more, because each fatigued chit can be wounded to satisfy fatigue, and non-effort chits can be wounded too.
//System.out.println(chit.getGameObject().getName()+" is "+effort+" + 1");
			}
		}
		// fatigued chits are wounded one at a time after this (this can only happen when special weather conditions are in play)
		for (Iterator i=fatiguedChits.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit() && validChit((CharacterActionChitComponent)chit)) {
				val += 1; // effort has no effect at this point
//System.out.println(chit.getGameObject().getName()+" is 1 more");
			}
		}
		
		return val;
	}
	private boolean areActiveEffortChits() {
		for (Iterator i=activeChits.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent aChit = (CharacterActionChitComponent)chit;
				if (validChit(aChit) && !aChit.isColor() && aChit.getEffortAsterisks()>0) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean areColorChits() {
		for (Iterator i=activeChits.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent aChit = (CharacterActionChitComponent)chit;
				if (validChit(aChit) && aChit.isColor()) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean areFatiguedChits() {
		return fatiguedChits.getAllChits().size()>0;
	}
	protected boolean canClickActive(CharacterActionChitComponent clickedChit) {
		if (needsToFatigue()) {
			if (clickedChit!=null) {
				if (validChit(clickedChit)) {
					if (clickedChit.isColor()) {
						if (!areActiveEffortChits()) {
							return true;
						}
					}
					else {
						int effort = clickedChit.getEffortAsterisks();
						if (effort>0) {
							// go ahead
							if ((currentCount-effort)>=-1) { // allowed to dip one into the hole
								return true;
							}
						}
						else if (!areActiveEffortChits() && !areColorChits() && !areFatiguedChits()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	protected boolean canClickFatigue(CharacterActionChitComponent clickedChit) {
		if (needsToMakeChange()) { // making change
			if (clickedChit!=null) {
				if (validChit(clickedChit,true)) {
					int effort = clickedChit.getEffortAsterisks();
					if (effort==1) {
						return true;
					}
				}
			}
		}
		else if (needsToFatigue() && !areActiveEffortChits() && !areColorChits()) {
			return true;
		}
		return false;
	}
	protected boolean canClickWound(CharacterActionChitComponent clickedChit) {
		return false;
	}
	protected void updateEffort(CharacterActionChitComponent clickedChit,int count) {
		currentCount += count;
		if (clickedChit.isAnyEffort()) return;
		if (countsAsMove(clickedChit) && move!=INFINITE) {
			move += count;
			if (move<0 && !canMakeChange()) {
				lostAsterisks -= move;
				currentCount -= move;
				move = 0;
			}
		}
		else if (countsAsFight(clickedChit) && fight!=INFINITE) {
			fight += count;
			if (fight<0 && !canMakeChange()) {
				lostAsterisks -= fight;
				currentCount -= fight;
				fight = 0;
			}
		}
		else if (countsAsMagic(clickedChit) && magic!=INFINITE) {
			magic += count;
			if (magic<0 && !canMakeChange()) {
				lostAsterisks -= magic;
				currentCount -= magic;
				magic = 0;
			}
		}
	}
	protected void activeClick(CharacterActionChitComponent clickedChit) {
		if (needsToFatigue()) {
			if (clickedChit!=null) {
				if (validChit(clickedChit)) {
					int effort = clickedChit.getEffortAsterisks();
					if (clickedChit.isColor()) {
						effort = 1; // color chits only count as one!
					}
					// go ahead
					if ((currentCount-effort)>=-1) { // allowed to dip one into the hole
						if (effort==0) {
							moveChit(clickedChit,activeChits,woundedChits);
							updateEffort(clickedChit,-1);
						}
						else {
							moveChit(clickedChit,activeChits,fatiguedChits);
							updateEffort(clickedChit,-effort);
							makeChangeType = TYPE_NA;
							if (currentCount<0) {
								if (clickedChit.isMove()) {
									makeChangeType = TYPE_MOVE;
								}
								else if (clickedChit.isFight() || clickedChit.isFightAlert()) {
									makeChangeType = TYPE_FIGHT;
								}
							}
						}
					}
				}
			}
		}
	}
	protected void fatigueClick(CharacterActionChitComponent clickedChit) {
		if (needsToMakeChange()) { // making change
			if (clickedChit!=null) {
				if (validChit(clickedChit,true)) {
					int effort = clickedChit.getEffortAsterisks();
					if (effort==1) {
						moveChit(clickedChit,fatiguedChits,activeChits);
						updateEffort(clickedChit,effort);
					}
				}
			}
		}
		else if (needsToFatigue()) { // wounding fatigue chits
			if (clickedChit!=null) {
				if (validChit(clickedChit)) {
					moveChit(clickedChit,fatiguedChits,woundedChits);
					updateEffort(clickedChit,-1);
				}
			}
		}
	}
	protected void woundClick(CharacterActionChitComponent clickedChit) {
		// does nothing
	}
	protected void updateStatusLabel(JLabel label) {
//		System.out.println("M="+move+",F="+fight+",G="+magic+",currentCount="+currentCount+"  NeedsToFatigue="+needsToFatigue()+",NeedsToMakeChange="+needsToMakeChange());
		StringBuffer restrictions = new StringBuffer();
		if (move<0 || makeChangeType==TYPE_MOVE || (move>0 && fight==0)) {
			restrictions.append("MOVE");
		}
		if (fight<0 || makeChangeType==TYPE_FIGHT || (fight>0 && move==0)) {
			if (restrictions.length()>0) {
				restrictions.append(",");
			}
			restrictions.append("FIGHT");
		}
		if (restrictions.length()>0) {
			restrictions.append(" only");
		}
		String warning = restrictions.length()==0?"":(" ("+restrictions.toString()+")");
		String lostAsteriskWarning = lostAsterisks==0?"":" (lost "+lostAsterisks+" asterisk"+(lostAsterisks==1?"":"s")+")";
		warning += lostAsteriskWarning;
				
		if (!needsToFatigue() && !needsToMakeChange()) {
			label.setText(lostAsteriskWarning);
		}
		else if (needsToFatigue()) {
			label.setText("Fatigue "+currentCount+" asterisk"+(currentCount==1?"":"s")+warning);
			label.setForeground(Color.black);
		}
		else if (needsToMakeChange()) {
			if (canMakeChange()) {
				int changeCount = 0;
				if (move<0) changeCount = move;
				if (fight<0) changeCount = fight;
				if (magic<0) changeCount = magic;
				if (changeCount==0) changeCount = currentCount;
				label.setText("Make change for "+(-changeCount)+" asterisk"+(changeCount==-1?"":"s")+warning);
				label.setForeground(Color.red);
			}
			else {
				label.setText("Can't make change, so the extra asterisk will be lost!");
				label.setForeground(Color.red);
			}
		}
	}
	/**
	 * A Wizard with a smattering of problems
	 */
	protected static CharacterWrapper _testScenario1(GameData data) {
		GameObject character = data.getGameObjectByName("Wizard");
		GameObject f1 = data.getGameObjectByName("Test Fly Chit 1");
		character.add(f1);
		System.out.println(character);
		
		CharacterWrapper wrapper = new CharacterWrapper(character);
		wrapper.setCharacterLevel(4);
		wrapper.initChits();
		
		// artifically fatigue and wound some chits
		ArrayList list = new ArrayList(wrapper.getAllChits());
		Collections.sort(list);
		int n=0;
		for (Iterator i=list.iterator();i.hasNext();) {
			CharacterActionChitComponent aChit = (CharacterActionChitComponent)i.next();
			System.out.println((n++)+" "+aChit.getGameObject().getName());
		}
		
		CharacterActionChitComponent aChit = (CharacterActionChitComponent)list.get(1);
		aChit.getGameObject().setThisAttribute("action","FLY");
		aChit.getGameObject().setThisAttribute("effort","1");
		
//		aChit.makeFatigued();
//		for (int i=4;i<11;i++) {
//			aChit = (CharacterActionChitComponent)list.get(i);
//			aChit.makeWounded();
//		}
		aChit = (CharacterActionChitComponent)list.get(11);
		aChit.enchant();
//		(new Curse(new JFrame())).applyThree(wrapper);
		return wrapper;
	}
	protected static CharacterWrapper _testScenario2(GameData data,boolean preFatigue) {
		GameObject character = data.getGameObjectByName("White Knight");
		System.out.println(character);
		
		CharacterWrapper wrapper = new CharacterWrapper(character);
		wrapper.setCharacterLevel(4);
		wrapper.initChits();
		
		// artifically fatigue and wound some chits
		ArrayList list = new ArrayList(wrapper.getAllChits());
		Collections.sort(list);
		if (preFatigue) {
			CharacterActionChitComponent aChit = (CharacterActionChitComponent)list.get(3);
			aChit.makeFatigued();
			aChit = (CharacterActionChitComponent)list.get(7);
			aChit.makeFatigued();
		}
//		//for (int i=4;i<11;i++) {
//			aChit = (CharacterActionChitComponent)list.get(i);
//			aChit.makeWounded();
//		}
//		for (int i=8;i<10;i++) {
//			CharacterActionChitComponent aChit = (CharacterActionChitComponent)list.get(i);
//			aChit.makeWounded();
//		}
//		(new Curse(new JFrame())).applyThree(wrapper);
		return wrapper;
	}
	protected static CharacterWrapper _testScenario3(GameData data) {
		GameObject character = data.getGameObjectByName("Swordsman");
		System.out.println(character);
		
		CharacterWrapper wrapper = new CharacterWrapper(character);
		wrapper.setCharacterLevel(4);
		wrapper.initChits();
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
		System.out.println("Done");
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),true),1,2,0,0);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),true),2,2,1,0);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),false),2,2,1,0);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),true),1);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),false),1);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),false),100);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario2(loader.getData(),true),100);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario1(loader.getData()),100);
		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario1(loader.getData()),1);
//		ChitFatigueManager man = new ChitFatigueManager(new JFrame(),_testScenario3(loader.getData()),1,2,0,0);
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