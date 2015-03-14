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
package com.robin.magic_realm.components.quest.reward;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.DrawType;
import com.robin.magic_realm.components.quest.Quest;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.table.Loot;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardTreasureFromSite extends QuestReward {
	
	public static final String SITE_REGEX = "_siterx";
	public static final String DRAW_TYPE = "_dt";

	public QuestRewardTreasureFromSite(GameObject go) {
		super(go);
	}

	@Override
	public void processReward(JFrame frame, CharacterWrapper character) {
		GamePool pool = new GamePool(getGameData().getGameObjects());
		ArrayList<GameObject> sourceObjects = pool.find("treasure_location,!treasure_within_treasure,!cannot_move");
		sourceObjects.addAll(pool.find("visitor=scholar")); 
		sourceObjects.addAll(pool.find("dwelling,!native")); 
		ArrayList<GameObject> objects = getObjectList(sourceObjects,getSiteRegex());
		if (objects.isEmpty()) {
			JOptionPane.showMessageDialog(frame,"The site specified by the reward was not found!","Quest Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		GameObject selected = null;
		if (objects.size()==1) {
			selected = objects.get(0);
		}
		else {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,getTitleForDialog()+" Select a Site to recieve treasure from:",false);
			chooser.addGameObjects(objects,true);
			chooser.setVisible(true);
			selected = chooser.getFirstSelectedComponent().getGameObject();
		}
		
		ArrayList hold = selected.getHold();
		ArrayList<GameObject> treasures = new ArrayList<GameObject>();
		for(Object o:hold) {
			RealmComponent rc = RealmComponent.getRealmComponent((GameObject)o);
			if (rc.isTreasure()) {
				treasures.add((GameObject)o);
			}
		}
		if (treasures.isEmpty()) {
			Quest.showQuestMessage(frame,getParentQuest(),"The site specified by the reward was empty.",getTitleForDialog(),RealmComponent.getRealmComponent(selected));
			return;
		}
		GameObject treasure = null;
		switch(getDrawType()) {
			case Top:
				treasure = (GameObject)hold.get(0);
				break;
			case Bottom:
				treasure = (GameObject)hold.get(hold.size()-1);
				break;
			case Random:
				treasure = (GameObject)hold.get(RandomNumber.getRandom(hold.size()));
				break;
			case Choice:
				RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,getTitleForDialog()+" Which treasure?",false);
				chooser.addGameObjects(hold,true);
				chooser.setVisible(true);
				chooser.getFirstSelectedComponent().getGameObject();
				break;
		}		
		if (treasure!=null) { // shouldn't ever be null
			Loot.addItemToCharacter(frame,null,character,treasure);
		}
	}
	
	@Override
	public RewardType getRewardType() {
		return RewardType.TreasureFromSite;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		switch(getDrawType()) {
			case Top:
				sb.append("Take the top");
				break;
			case Bottom:
				sb.append("Take the bottom");
				break;
			case Choice:
				sb.append("Choose a");
				break;
			case Random:
				sb.append("Get a random");
		}
		sb.append(" treasure from /");
		sb.append(getSiteRegex());
		sb.append("/.");
		return sb.toString();
	}
	
	public DrawType getDrawType() {
		return DrawType.valueOf(getString(DRAW_TYPE));
	}
	
	public String getSiteRegex() {
		return getString(SITE_REGEX);
	}
	public static ArrayList<GameObject> getObjectList(ArrayList<GameObject> sourceObjects,String regEx) {
		Pattern pattern = (regEx==null || regEx.length()==0)?null:Pattern.compile(regEx);
		ArrayList<GameObject> objects = new ArrayList<GameObject>();
		for(GameObject obj:sourceObjects) {
			if (pattern==null || pattern.matcher(obj.getName()).find()) {
				objects.add(obj);
			}
		}
		return objects;
	}
}