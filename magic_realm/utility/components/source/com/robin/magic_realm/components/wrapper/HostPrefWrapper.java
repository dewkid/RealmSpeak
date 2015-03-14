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
import com.robin.magic_realm.components.utility.Constants;

public class HostPrefWrapper extends GameObjectWrapper {

	public static final String HOST_PREF_BLOCK = "_host_preferences_";

	public static final String HOST_PREF_TAG = "_host_prefs_";
	public static final String HOST_NAME_TAG = "h_name__";
	public static final String HOST_EMAIL = "h_email_";
	public static final String SMTP_HOST = "smtp_h_";
	public static final String EMAIL_NOTIFICATIONS = "e_note_";
	public static final String GAME_PORT_TAG = "gp__";
	public static final String GAME_TITLE_TAG = "g_title";
	public static final String GAME_PASS_TAG = "g_pass";
	public static final String GAME_KEYVALS_TAG = "g_keyvals";
	public static final String GAME_SETUP_NAME_TAG = "g_setup";
	public static final String PLAYERS_ALLOWED_TAG = "pl_allowed";
	public static final String CHARACTERS_PER_PLAYER_TAG = "char_per_player";
	public static final String NUMBER_OF_MONTHS_TO_PLAY = "n_mon_play";
	public static final String VPS_TO_ACHIEVE = "vps_2_achv";
	public static final String ENABLE_BATTLES = "en_batt";
	public static final String ENABLE_AUTOSAVE = "en_autosave";
	public static final String ENABLE_AUTO_SETUP = "en_autoset";
	public static final String ENABLE_PLAYER_SETUP = "en_playset";
	public static final String MIX_EXPANSION_TILES = "mx_ex_tiles";
	public static final String INCLUDE_EXPANSION_SPELLS = "inc_ex_spells";
	public static final String ENABLE_MULTI_BOARD = "en_multbrd";
	public static final String NUMBER_OF_BOARDS_TO_USE = "n_brd_use";
	public static final String MINIMUM_MAP_RATING = "min_mr";
	
//	public static final String REQ_VPS_PER_PLAYER = "rq_vps";
	public static final String REQ_VPS_DISABLED = "rq_vps_off";
	
	public static final String START_SEASON = "start_season";
	
	public static final String CHARACTER_KEY = "character_keys";
	
	public HostPrefWrapper(GameObject obj) {
		super(obj);
	}
	public String getBlockName() {
		return HOST_PREF_BLOCK;
	}
	public static Collection getKeyVals() {
		ArrayList keyVals = new ArrayList();
		keyVals.add(HOST_NAME_TAG);
		return keyVals;
	}
	public void setPref(String val,boolean on) {
		if (on!=hasPref(val)) {
			setBoolean(val,on);
		}
	}
	public boolean hasPref(String val) {
		return getBoolean(val);
	}
	
	// Getters
	public String getHostName() {
		return getString(HOST_NAME_TAG);
	}
	public String getHostEmail() {
		return getString(HOST_EMAIL);
	}
	public String getSmtpHost() {
		return getString(SMTP_HOST);
	}
	public boolean isEmailNotifications() {
		return getBoolean(EMAIL_NOTIFICATIONS);
	}
	public int getGamePort() {
		return getInt(GAME_PORT_TAG);
	}
	public String getGameTitle() {
		return getString(GAME_TITLE_TAG);
	}
	public String getGamePass() {
		return getString(GAME_PASS_TAG);
	}
	public String getGameKeyVals() {
		return getString(GAME_KEYVALS_TAG);
	}
	public String getGameSetupName() {
		return getString(GAME_SETUP_NAME_TAG);
	}
	public int getNumberMonthsToPlay() {
		return getInt(NUMBER_OF_MONTHS_TO_PLAY);
	}
	public int getVpsToAchieve() {
		return getInt(VPS_TO_ACHIEVE);
	}
	public boolean getEnableBattles() {
		return getBoolean(ENABLE_BATTLES);
	}
	public boolean getAutosaveEnabled() {
		return getBoolean(ENABLE_AUTOSAVE);
	}
	public boolean getBoardAutoSetup() {
		return getBoolean(ENABLE_AUTO_SETUP);
	}
	public boolean getBoardPlayerSetup() {
		return getBoolean(ENABLE_PLAYER_SETUP);
	}
	public boolean getMixExpansionTilesEnabled() {
		return getBoolean(MIX_EXPANSION_TILES);
	}
	public boolean getIncludeExpansionSpells() {
		return getBoolean(INCLUDE_EXPANSION_SPELLS);
	}
	public boolean getMultiBoardEnabled() {
		return getBoolean(ENABLE_MULTI_BOARD);
	}
	public int getMultiBoardCount() {
		return getInt(NUMBER_OF_BOARDS_TO_USE);
	}
	public int getMinimumMapRating() {
		return getInt(MINIMUM_MAP_RATING);
	}
	public boolean getRequiredVPsOff() {
		return getBoolean(REQ_VPS_DISABLED);
	}
	public boolean isUsingSeasons() {
		String val = getStartingSeason();
		if (val!=null && !"No Seasons".equals(val)) {
			return true;
		}
		return false;
	}
	public String getStartingSeason() {
		return getString(START_SEASON);
	}
	
	// Setters
	public void setHostName(String val) {
		setString(HOST_NAME_TAG,val);
	}
	public void setHostEmail(String val) {
		setString(HOST_EMAIL,val);
	}
	public void setSmtpHost(String val) {
		setString(SMTP_HOST,val);
	}
	public void setEmailNotifications(boolean val) {
		setBoolean(EMAIL_NOTIFICATIONS,val);
	}
	public void setGamePortString(String val) {
		setString(GAME_PORT_TAG,val);
	}
	public void setGamePort(int val) {
		setInt(GAME_PORT_TAG,val);
	}
	public void setGameTitle(String val) {
		setString(GAME_TITLE_TAG,val);
	}
	public void setGamePass(String val) {
		setString(GAME_PASS_TAG,val);
	}
	public void setGameKeyVals(String val) {
		setString(GAME_KEYVALS_TAG,val);
	}
	public void setGameSetupName(String val) {
		setString(GAME_SETUP_NAME_TAG,val);
	}
	public void setNumberMonthsToPlayString(String val) {
		setString(NUMBER_OF_MONTHS_TO_PLAY,val);
	}
	public void setNumberMonthsToPlay(int val) {
		setInt(NUMBER_OF_MONTHS_TO_PLAY,val);
	}
	public void setVpsToAchieveString(String val) {
		setString(VPS_TO_ACHIEVE,val);
	}
	public void setVpsToAchieve(int val) {
		setInt(VPS_TO_ACHIEVE,val);
	}
	public void setEnableBattles(boolean val) {
		setBoolean(ENABLE_BATTLES,val);
	}
	public void setAutosaveEnabled(boolean val) {
		setBoolean(ENABLE_AUTOSAVE,val);
	}
	public void setBoardAutoSetup(boolean val) {
		setBoolean(ENABLE_AUTO_SETUP,val);
	}
	public void setBoardPlayerSetup(boolean val) {
		setBoolean(ENABLE_PLAYER_SETUP,val);
	}
	public void setMixExpansionTilesEnabled(boolean val) {
		setBoolean(MIX_EXPANSION_TILES,val);
	}
	public void setIncludeExpansionSpells(boolean val) {
		setBoolean(INCLUDE_EXPANSION_SPELLS,val);
	}
	public void setMultiBoardEnabled(boolean val) {
		setBoolean(ENABLE_MULTI_BOARD,val);
	}
	public void setMultiBoardCount(int val) {
		setInt(NUMBER_OF_BOARDS_TO_USE,val);
	}
	public void setMinimumMapRating(int val) {
		setInt(MINIMUM_MAP_RATING,val);
	}
	public void setRequiredVPsOff(boolean val) {
		setBoolean(REQ_VPS_DISABLED,val);
	}
	public void setStartingSeason(String val) {
		setString(START_SEASON,val);
	}
	public int getMaximumCharacterLevel() {
		return hasPref(Constants.EXP_DEVELOPMENT_PLUS)?11:4;
	}
	public void clearCharacterKeys() {
		removeAttribute(CHARACTER_KEY);
	}
	public void addCharacterKey(String key) {
		addListItem(CHARACTER_KEY,key);
	}
	public boolean hasCharacterKey(String key) {
		return hasListItem(CHARACTER_KEY,key);
	}
	public ArrayList<String> getAllCharacterKeys() {
		ArrayList list = getList(CHARACTER_KEY);
		ArrayList<String> ret = new ArrayList<String>();
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				String key = (String)i.next();
				ret.add(key);
			}
		}
		return ret;
	}
	
	/**
	 * For testing, mainly
	 */
	public static HostPrefWrapper createDefaultHostPrefs(GameData data) {
		HostPrefWrapper hostPrefs = findHostPrefs(data);
		if (hostPrefs==null) {
			hostPrefs = new HostPrefWrapper(data.createNewObject());
			hostPrefs.setHostName("default");
			hostPrefs.setGameSetupName("standard_game");
			hostPrefs.setGameKeyVals("original_game");
			hostPrefs.setMultiBoardCount(1);
			hostPrefs.setStartingSeason("No Seasons");
		}
		return hostPrefs;
	}
	public static Long HOST_PREF_ID = null; // needs to be renulled when a new game starts or ends!
	/**
	 * Identifies and returns the host preference object, or null if not found.
	 */
	public static HostPrefWrapper findHostPrefs(GameData data) {
		if (HOST_PREF_ID==null) {
			GamePool pool = new GamePool(data.getGameObjects());
			Collection c = pool.extract(HostPrefWrapper.getKeyVals());
			if (c!=null && c.size()==1) {
				GameObject hp = (GameObject)c.iterator().next();
				HOST_PREF_ID = new Long(hp.getId());
				return new HostPrefWrapper(hp);
			}
		}
		else {
			GameObject go = data.getGameObject(HOST_PREF_ID);
			if (go==null || !go.hasAttributeBlock(HOST_PREF_BLOCK)) {
				// Not sure why/how this happens, but this will guarantee it works
				HOST_PREF_ID = null;
				return findHostPrefs(data);
			}
			return new HostPrefWrapper(data.getGameObject(HOST_PREF_ID));
		}
		return null;
	}
}