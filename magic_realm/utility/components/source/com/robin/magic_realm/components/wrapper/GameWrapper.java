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
package com.robin.magic_realm.components.wrapper;

import java.util.*;

import com.robin.game.objects.*;
import com.robin.general.swing.DieRoller;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmCalendar;

public class GameWrapper extends GameObjectWrapper {

	public static final String GAME_BLOCK = "_gb__";
	public static final String GAME_VERSION = "_v_";
	public static final String GAME_VERSION_CHANGED_FROM = "_vcf_";
	public static final String GAME_VERSION_IGNORE_CHANGE = "_vic_";
	public static final String GAME_DAY = "_d__";
	public static final String GAME_MONSTER_DIE = "_md__";
	public static final String GAME_MONTH = "_m__";
	public static final String GAME_STATE = "_gs__";
	public static final String GAME_TURN_COUNT = "_turn__";
	public static final String GAME_DAY_TURN_COUNT = "_dturn__";
	public static final String CHAR_POOL_LOCK = "_cp_lock_";
	public static final String GAME_PLACE_GS = "_p_gs_";
	public static final String GAME_STARTED = "_start_";
	public static final String GAME_ENDED = "_end_";
	public static final String GAME_MAP_BUILDER = "_mapb_";
	public static final String GAME_SEASON_OFFSET = "_seasoff_";
	public static final String GAME_WEATHER = "_weth_";
	public static final String GAME_WEATHER_CHIT = "_wtdi_";
	public static final String GAME_REVEALED = "_revv_";
	public static final String GAME_Q_ID = "_gqid_";
	public static final String GAME_Q_PREFIX = "_gqp__";
	public static final String GAME_LAST_REGEN = "_lrg__"; // keeps track of denizens that regenerate on the 7th day
	public static final String GAME_CLIENTS_TAKEN_TURN = "_ctt__";
	
	public static final String GAME_TRAVELERS = "_gtv__";
	
	public static final String MAP_REPAINT = "_mr__";
	public static final String MAP_CURRENT_RATING = "_cmr__";

	public static final int GAME_STATE_RECORDING	= 1;		// Recording turns
	public static final int GAME_STATE_PLAYING	= 2;		// Playing turns
	public static final int GAME_STATE_RESOLVING	= 3;		// Resolving combat
	public static final int GAME_STATE_DAYEND	= 4;		// Doing day end trading/rearrangement
	
	public static final int GAME_STATE_GAMEOVER		=10;
	
	public GameWrapper(GameObject obj) {
		super(obj);
	}
	public void setInitialValues() {
		setVersion(Constants.REALM_SPEAK_VERSION);
		setDay(0);
		setMonth(1);
		setState(GameWrapper.GAME_STATE_RECORDING);
		setWeather(RealmCalendar.WEATHER_CLEAR); // default starting weather
		updateWeatherChit(); // Even if weather isn't in play, set this up here
	}
	public String getBlockName() {
		return GAME_BLOCK;
	}
	public boolean isRecording() {
		return getState()==GAME_STATE_RECORDING;
	}
	public boolean isDaylight() {
		return getState()==GAME_STATE_PLAYING;
	}
	public boolean inCombat() {
		return getState()==GAME_STATE_RESOLVING;
	}
	public boolean isDayEnd() {
		return getState()==GAME_STATE_DAYEND;
	}
	public boolean isGameOver() {
		return getState()==GAME_STATE_GAMEOVER;
	}
	public static Collection getKeyVals() {
		ArrayList keyVals = new ArrayList();
		keyVals.add(GAME_STATE);
		return keyVals;
	}
	
	// Getters
	public String getVersion() {
		return getString(GAME_VERSION);
	}
	public String getVersionChangedFrom() {
		return getString(GAME_VERSION_CHANGED_FROM);
	}
	public boolean isIgnoreVersionChange() {
		return getBoolean(GAME_VERSION_IGNORE_CHANGE);
	}
	public int getDay() {
		return getInt(GAME_DAY);
	}
	public int getMonth() {
		return getInt(GAME_MONTH);
	}
	public int getState() {
		return getInt(GAME_STATE);
	}
	public DieRoller getMonsterDie() {
		String string = getString(GAME_MONSTER_DIE);
		if (string!=null) {
			return new DieRoller(string,25,5);
		}
		return null;
	}
	public int getTurnCount() {
		return getInt(GAME_TURN_COUNT);
	}
	public int getDayTurnCount() {
		return getInt(GAME_DAY_TURN_COUNT);
	}
	public boolean getCharacterPoolLock() {
		return getBoolean(CHAR_POOL_LOCK);
	}
	public boolean getPlaceGoldSpecials() {
		return getBoolean(GAME_PLACE_GS);
	}
	public boolean getGameStarted() {
		return getBoolean(GAME_STARTED);
	}
	public boolean getGameEnded() {
		return getBoolean(GAME_ENDED);
	}
	public String getGameMapBuilder() {
		return getString(GAME_MAP_BUILDER);
	}
	public int getSeasonOffset() {
		return getInt(GAME_SEASON_OFFSET);
	}
	public boolean hasSeasonOffset() {
		return getBoolean(GAME_SEASON_OFFSET);
	}
	public String getWeather() {
		return getString(GAME_WEATHER);
	}
	public int getWeatherChit() {
		return getInt(GAME_WEATHER_CHIT);
	}
	public int getCurrentMapRating() {
		return getInt(MAP_CURRENT_RATING);
	}
	
	// Setters
	public void setVersion(String val) {
		setString(GAME_VERSION,val);
	}
	public void setVersionChangedFrom(String val) {
		setString(GAME_VERSION_CHANGED_FROM,val);
	}
	public void setIgnoreVersionChange(boolean val) {
		setBoolean(GAME_VERSION_IGNORE_CHANGE,val);
	}
	public void setDay(int val) {
		setInt(GAME_DAY,val);
	}
	public void setMonth(int val) {
		setInt(GAME_MONTH,val);
	}
	public void setState(int val) {
		setInt(GAME_STATE,val);
	}
	public void setMonsterDie(DieRoller roller) {
		setString(GAME_MONSTER_DIE,roller.getStringResult());
	}
	private void setTurnCount(int val) { // private so that this isn't set manually!
		setInt(GAME_TURN_COUNT,val);
	}
	public void setDayTurnCount(int val) {
		setInt(GAME_DAY_TURN_COUNT,val);
	}
	public void setCharacterPoolLock(boolean val) {
		setBoolean(CHAR_POOL_LOCK,val);
	}
	public void setPlaceGoldSpecials(boolean val) {
		setBoolean(GAME_PLACE_GS,val);
	}
	public void setGameStarted(boolean val) {
		setBoolean(GAME_STARTED,val);
	}
	public void setGameEnded(boolean val) {
		setBoolean(GAME_ENDED,val);
	}
	public void setGameMapBuilder(String val) {
		setString(GAME_MAP_BUILDER,val);
	}
	public void setSeasonOffset(int val) {
		setInt(GAME_SEASON_OFFSET,val);
	}
	public void setWeather(String val) {
		setString(GAME_WEATHER,val);
	}
	public void setWeatherChit(int val) {
		setInt(GAME_WEATHER_CHIT,val);
	}
	public void updateWeatherChit() {
		setWeatherChit(RandomNumber.getDieRoll(6));
	}
	public void setCurrentMapRating(int val) {
		setInt(MAP_CURRENT_RATING,val);
	}
	
	// Adders
	public void addDay(int val) {
		int newDay = getDay()+val;
		while(newDay>RealmCalendar.DAYS_IN_A_MONTH) {
			addMonth(1);
			newDay -= RealmCalendar.DAYS_IN_A_MONTH;
		}
		setDay(newDay);
		
		addTurnCount(val);
		setDayTurnCount(0);
	}
	public void addMonth(int val) {
		setMonth(getMonth()+val);
	}
	private void addTurnCount(int val) {
		setTurnCount(getTurnCount()+1);
	}
	private void incrementDayTurnCount() {
		setDayTurnCount(getDayTurnCount()+1);
	}
	public int getNextDayTurnCount() {
		incrementDayTurnCount();
		return getDayTurnCount();
	}
	public void setGameRevealed(boolean val) {
		setBoolean(GAME_REVEALED,val);
	}
	public boolean hasBeenRevealed() {
		return getBoolean(GAME_REVEALED);
	}
	public void bumpMapRepaint() {
		int lastVal = getInt(MAP_REPAINT);
		setInt(MAP_REPAINT,lastVal+1);
	}
	public int getMapRepaint() {
		return getInt(MAP_REPAINT);
	}
	
	public int getNextQuestionId() {
		int qid = getInt(GAME_Q_ID);
		qid++;
		setInt(GAME_Q_ID,qid);
		return qid;
	}
	
	public void addQuestion(String askingPlayerName,String answeringPlayerName,String question) {
		int qid = getNextQuestionId();
		String qAttribute = GAME_Q_PREFIX+qid;
		addListItem(qAttribute,answeringPlayerName);
		addListItem(qAttribute,askingPlayerName);
		addListItem(qAttribute,question);
	}
	
	public void clearRegeneratedDenizens() {
		setBoolean(GAME_LAST_REGEN,false);
	}
	public void addRegeneratedDenizen(GameObject go) {
		addListItem(GAME_LAST_REGEN,go.getStringId());
	}
	public ArrayList<GameObject> getRegeneratedDenizens() {
		ArrayList ids = getList(GAME_LAST_REGEN);
		if (ids!=null) {
			if (!ids.isEmpty()) {
				ArrayList<GameObject> list = new ArrayList<GameObject>();
				for (Iterator i=ids.iterator();i.hasNext();) {
					String id = (String)i.next();
					list.add(getGameObject().getGameData().getGameObject(Long.valueOf(id)));
				}
				return list;
			}
		}
		return null;
	}
	public void clearClientTakenTurn() {
		setBoolean(GAME_CLIENTS_TAKEN_TURN,false);
	}
	public void addClientTakenTurn(String clientName) {
		addListItem(GAME_CLIENTS_TAKEN_TURN,clientName);
	}
	public boolean isClientTakenTurn(String clientName) {
		return hasListItem(GAME_CLIENTS_TAKEN_TURN,clientName);
	}
	public void addTravelerKnowledge(GameObject traveler) {
		addListItem(GAME_TRAVELERS,traveler.getStringId());
	}
	public boolean hasTravelerKnowledge(GameObject traveler) {
		return hasListItem(GAME_TRAVELERS,traveler.getStringId());
	}
	public ArrayList<GameObject> getTravelerKnowledge() {
		ArrayList<GameObject> travelers = new ArrayList<GameObject>();
		ArrayList list = getList(GAME_TRAVELERS);
		if (list!=null) {
			for(Iterator i=list.iterator();i.hasNext();) {
				travelers.add(getGameObject().getGameObjectFromId((String)i.next()));
			}
		}
		return travelers;
	}

	
	/**
	 * @return		A string array where the first element is the askingPlayerName, and the second element is the question
	 */
	public String[] getNextQuestion(String answeringPlayerName) {
		int qid = getInt(GAME_Q_ID);
		// I don't like the loop, but considering that there isn't likely to be a lot of questions, this should be sufficient
		for (int i=0;i<=qid;i++) {
			String qAttribute = GAME_Q_PREFIX+i;
			if (getGameObject().hasAttribute(GAME_BLOCK,qAttribute)) {
				ArrayList list = getList(qAttribute);
				String test = (String)list.get(0);
				if (test.equals(answeringPlayerName)) {
					String[] ret = new String[2];
					ret[0] = (String)list.get(1); // askingPlayerName
					ret[1] = (String)list.get(2); // question
					getGameObject().removeAttribute(GAME_BLOCK,qAttribute); // remove so it doesn't get asked twice!
					return ret;
				}
			}
		}
		return null;
	}
	
	///////////////////////////////////////////////
	public static Long GAME_ID = null;
	public static GameWrapper findGame(GameData data) {
		if (GAME_ID==null) {
			GamePool pool = new GamePool(data.getGameObjects());
			ArrayList list = pool.find(getKeyVals());
			if (list.size()==1) {
				GameObject gw = (GameObject)list.get(0);
				GAME_ID = new Long(gw.getId());
				return new GameWrapper(gw);
			}
		}
		else {
			return new GameWrapper(data.getGameObject(GAME_ID));
			/*
			 * Hi Robin - Are you here because of a null game object?  Did you happen to have just run the TileEditor?  There's a bug
			 * there that adds a game object to the base XML before saving it, and that confuses THIS code.  Not sure why, but I don't feel
			 * like fixing it at this moment, and this comment should be sufficient to prevent me from wasting time on this problem
			 * the next time around!!
			 */
		}
		
		// None found?  Better make one.
		GameObject go = data.createNewObject();
		go.setName("Created by GameWrapper");
		
		GameWrapper game = new GameWrapper(go);
		game.setInitialValues();
		GAME_ID = new Long(go.getId());
		
		return game;
	}
}