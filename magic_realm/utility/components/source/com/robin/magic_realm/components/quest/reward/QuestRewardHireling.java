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

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.ChitAcquisitionType;
import com.robin.magic_realm.components.quest.TermOfHireType;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardHireling extends QuestReward {

	public static final String HIRELING_REGEX = "_hrx";
	public static final String ACQUISITION_TYPE = "_goc";
	public static final String TERM_OF_HIRE = "_toh";

	public QuestRewardHireling(GameObject go) {
		super(go);
	}

	public void processReward(JFrame frame, CharacterWrapper character) {
		String actionDescription;
		ChitAcquisitionType at = getAcquisitionType();
		ArrayList<GameObject> objects;
		if (at == ChitAcquisitionType.Lose) {
			actionDescription = ": Select ONE hireling to lose.";
			objects = new ArrayList<GameObject>();
			for (RealmComponent rc : character.getAllHirelings()) {
				objects.add(rc.getGameObject());
			}
		}
		else {
			actionDescription = ": Select ONE hireling to join you.";
			objects = getGameData().getGameObjects();
		}
		ArrayList<GameObject> selectionObjects = getObjectList(objects, at, getHirelingRegex());
		if (selectionObjects.size() == 0)
			return; // no real reward!
		GameObject selected = null;
		if (selectionObjects.size() == 1) {
			selected = selectionObjects.get(0);
		}
		else {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame, getTitleForDialog() + actionDescription, false);
			chooser.addGameObjects(selectionObjects, true);
			chooser.setVisible(true);
			selected = chooser.getFirstSelectedComponent().getGameObject();
		}

		if (at == ChitAcquisitionType.Lose) {
			character.removeHireling(selected);
		}
		else {
			if (at == ChitAcquisitionType.Clone) {
				GameObject go = getGameData().createNewObject();
				go.copyAttributesFrom(selected);
				go.setThisAttribute(Constants.CLONED); // tag as cloned, so that the removeHireling method will expunge the clone
				selected = go;
			}
			character.getCurrentLocation().clearing.add(selected,character);
			RealmComponent rc = RealmComponent.getRealmComponent(selected);
			if (!rc.isNativeLeader()) {
				character.getGameObject().add(selected);
			}
			character.addHireling(selected, getTermOfHireType() == TermOfHireType.Normal ? Constants.TERM_OF_HIRE : 9999); // permanent enough? :-)
		}
	}

	private ArrayList<GameObject> getObjectList(ArrayList<GameObject> sourceObjects, ChitAcquisitionType at, String regEx) {
		Pattern pattern = (regEx == null || regEx.length() == 0) ? null : Pattern.compile(regEx);
		GamePool pool = new GamePool(sourceObjects);
		ArrayList<GameObject> objects = new ArrayList<GameObject>();
		for (GameObject go : pool.find("native,rank")) {
			if (pattern == null || pattern.matcher(go.getName()).find()) {
				if (at == ChitAcquisitionType.Available) {
					// Make sure these are not already hired
					RealmComponent rc = RealmComponent.getRealmComponent(go);
					if (rc.getOwnerId() != null)
						continue;
				}
				objects.add(go);
			}
		}
		return objects;
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(getHirelingRegex());
		ChitAcquisitionType at = getAcquisitionType();
		if (at == ChitAcquisitionType.Lose) {
			sb.append(" leaves the character.");
		}
		else {
			sb.append(" joins as a ");
			sb.append(getTermOfHireType().toString().toLowerCase());
			sb.append(" hireling.");
		}
		return sb.toString();
	}

	public RewardType getRewardType() {
		return RewardType.Hireling;
	}

	public ChitAcquisitionType getAcquisitionType() {
		return ChitAcquisitionType.valueOf(getString(ACQUISITION_TYPE));
	}

	public TermOfHireType getTermOfHireType() {
		return TermOfHireType.valueOf(getString(TERM_OF_HIRE));
	}

	public String getHirelingRegex() {
		return getString(HIRELING_REGEX);
	}
}