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
package com.robin.magic_realm.components.quest;

import java.util.Hashtable;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GameObjectWrapper;

public abstract class AbstractQuestObject extends GameObjectWrapper {
	
	public AbstractQuestObject(GameObject obj) {
		super(obj);
	}
	public void updateIdsForKey(Hashtable<Long, GameObject> lookup,String key) {
		String stringId = getString(key);
		if (stringId==null) return;
		Long oldId = Long.valueOf(stringId);
		GameObject go = lookup.get(oldId);
		setString(key,go.getStringId());
	}
	public String getBlockName() {
		return Quest.QUEST_BLOCK;
	}
	public Quest getParentQuest() {
		GameObject step = getGameObject().getHeldBy(); 
		GameObject quest = step.getHeldBy();
		return new Quest(quest);
	}
	public QuestStep getParentStep() {
		GameObject step = getGameObject().getHeldBy(); 
		return new QuestStep(step);
	}
	public String getTitleForDialog() {
		GameObject step = getGameObject().getHeldBy(); 
		GameObject quest = step.getHeldBy();
		return quest.getName()+" - "+step.getName();
	}
}