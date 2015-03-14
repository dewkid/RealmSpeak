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

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import com.robin.game.objects.*;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestDeck extends GameObjectWrapper {
	
	private static String QUEST_DECK_KEY = "__qd_key_";
	private static String QUEST_CARD_LIST = "_cl";
	private static String QUEST_ALL_PLAY_LIST = "_ap";
	private static String QUEST_DISCARDS = "_qdisc";
	private static String QUEST_CARD_TEMPLATE = "_qtemplate";
	private static String QUEST_UNIQUE_ID_GENERATOR = "_uidg";
	
	public QuestDeck(GameObject go) {
		super(go);
	}
	public String getBlockName() {
		return "QuestDeck";
	}
	private int generateUniqueId() {
		int id = getInt(QUEST_UNIQUE_ID_GENERATOR);
		setInt(QUEST_UNIQUE_ID_GENERATOR,id+1);
		return id;
	}
	public void addCards(Quest quest,int count) {
		quest.setBoolean(QUEST_CARD_TEMPLATE,true);
		
		// Assign a unique id for this card type
		quest.setInt(Quest.QUEST_UNIQUE_ID,generateUniqueId());
		
		for(int i=0;i<count;i++) {
			addListItem(QUEST_CARD_LIST,quest.getGameObject().getStringId());
		}
	}
	public void shuffle() {
		for(int i=0;i<3;i++) doShuffle(); // shuffle 3 times to make Steve S happy... :-)
	}
	private void doShuffle() {
		ArrayList list = getList(QUEST_CARD_LIST);
		if (list==null) return;
		ArrayList shuffled = new ArrayList();
		while(list.size()>0) {
			int r = RandomNumber.getRandom(list.size());
			shuffled.add(list.remove(r));
		}
		setList(QUEST_CARD_LIST,shuffled);
	}
	public int getCardCount() {
		return getListCount(QUEST_CARD_LIST);
	}
	public void addAllPlayCard(Quest quest) {
		quest.setBoolean(QUEST_CARD_TEMPLATE,true);
		quest.setInt(Quest.QUEST_UNIQUE_ID,generateUniqueId()); // All Play cards get a unique id on entry.
		addListItem(QUEST_ALL_PLAY_LIST,quest.getGameObject().getStringId());
	}
	public void discardCard(Quest quest) {
		quest.reset(); // clears out the quest so it can be used again
		addListItem(QUEST_DISCARDS,quest.getGameObject().getStringId());
	}

	public void setupAllPlayCards(JFrame frame,CharacterWrapper character) {
		for(Quest card:getAllPlayCards()) {
			if (card.getState()!=QuestState.New) continue; // skip all play cards that are no longer new (completed or failed)
			Quest quest = card.copyQuestToGameData(getGameData());
			quest.setState(QuestState.Assigned, character.getCurrentDayKey(), character); // indicates when the quest was first assigned
			character.addQuest(frame,quest);
		}
	}
	private ArrayList<GameObject> getAllPlayCardsAsObjects() {
		ArrayList<GameObject> allPlay = new ArrayList<GameObject>();
		ArrayList list = getList(QUEST_ALL_PLAY_LIST);
		if (list!=null && list.size()>0) {
			for(Iterator i=list.iterator();i.hasNext();) {
				String questId = (String)i.next();
				GameObject go = getGameData().getGameObject(Long.valueOf(questId));
				allPlay.add(go);
			}
		}
		return allPlay;
	}
	public ArrayList<Quest> getAllPlayCards() {
		ArrayList<Quest> allPlay = new ArrayList<Quest>();
		for(GameObject go:getAllPlayCardsAsObjects()) {
			Quest quest = new Quest(go);
			allPlay.add(quest);
		}
		return allPlay;
	}
	
	private void reshuffle() {
		ArrayList discards = getList(QUEST_DISCARDS);
		if (discards==null || discards.size()==0) return; // if there are no discards, then there are more player quest slots than the deck can handle, and nothing happens.
		setList(QUEST_CARD_LIST,new ArrayList(discards));
		clear(QUEST_DISCARDS);
		shuffle();
	}
	
	/**
	 * This will select a random quest card, remove it from the "deck", and add it to the current GameData collection.
	 */
	public Quest drawCard() {
		ArrayList list = getList(QUEST_CARD_LIST);
		if (list!=null && list.size()>0) {
			//int r = RandomNumber.getRandom(list.size());
			int r = 0; // just take the top card - the deck is "shuffled" after all!
			String questId = (String)list.get(r);
			GameObject go = getGameData().getGameObject(Long.valueOf(questId));
			Quest card = new Quest(go);
			
			// Remove the card from the deck
			removeListItem(QUEST_CARD_LIST,questId);
			
			// If this is the last card, then "reshuffle" with discards
			if (getListCount(QUEST_CARD_LIST)==0) reshuffle();
			
			if (card.getBoolean(QUEST_CARD_TEMPLATE)) {
				// Since this is just a card template, need to make a physical copy
				card = card.copyQuestToGameData(getGameData());
			}
			return card;
		}
		return null;
	}
	public int drawCards(JFrame frame,CharacterWrapper character) {
		int cardsDrawn = 0;
		int n = character.getQuestSlotCount() - character.getUnfinishedQuestCount();
		while(n>0 && getCardCount()>0) {
			Quest quest = drawCard();
			if (quest==null) break; // shouldn't happen, but just in case!
			quest.setState(QuestState.Assigned, character.getCurrentDayKey(), character); // indicates when the quest was first assigned
			character.addQuest(frame,quest);
			cardsDrawn++;
			n--;
		}
		return cardsDrawn;
	}
	
	///////////////////////////////////////////////
	public static Long DECK_ID = null;
	public static QuestDeck findDeck(GameData data) {
		if (DECK_ID==null) {
			GamePool pool = new GamePool(data.getGameObjects());
			GameObject go = pool.findFirst(QUEST_DECK_KEY);
			if (go!=null) {
				DECK_ID = go.getId();
				return new QuestDeck(go);
			}
		}
		else {
			return new QuestDeck(data.getGameObject(DECK_ID));
		}
		
		// None found?  Better make one.
		GameObject go = data.createNewObject();
		go.setName("Created by QuestDeck");
		go.setThisAttribute(QUEST_DECK_KEY);
		
		QuestDeck deck = new QuestDeck(go);
		DECK_ID = new Long(go.getId());
		
		return deck;
	}
}