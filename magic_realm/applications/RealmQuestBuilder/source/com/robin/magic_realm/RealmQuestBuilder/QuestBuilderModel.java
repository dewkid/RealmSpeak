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
package com.robin.magic_realm.RealmQuestBuilder;

import com.robin.game.objects.GameData;
import com.robin.magic_realm.components.quest.Quest;

public class QuestBuilderModel {
	
	private Quest quest;
	
	public QuestBuilderModel() {
		initNewQuest();
	}
	public Quest getQuest() {
		return quest;
	}
	public void initNewQuest() {
		GameData gameData = new GameData("QuestBuilder");
		quest = new Quest(gameData.createNewObject());
		quest.init();
		quest.setName("Untitled Quest");
		quest.setDescription("This is a quest.");
		quest.createQuestStep(false);
	}
	public void loadQuest() { // TODO from file? from quest class?  GameData?
	}
	public void saveQuest() {
	}
}