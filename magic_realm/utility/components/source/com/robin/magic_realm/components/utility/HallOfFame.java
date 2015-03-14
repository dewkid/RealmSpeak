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
package com.robin.magic_realm.components.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.io.PreferenceManager;
import com.robin.general.util.DateUtility;
import com.robin.general.util.OrderedHashtable;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class HallOfFame {
	
	private static final int MAX_ENTRIES_PER_CATEGORY = 10;
	
	public static final String TOTAL_SCORE = "TS";
	public static final String TOTAL_VPS = "TV";
	public static final String PLAYER_NAME = "PN";
	public static final String DAYS_PLAYED = "DP";
	public static final String NEW_ENTRY = "NE";
	public static final String RS_VERSION = "RSV";
	public static final String GAME_DATE = "GDT";
	
	public static final String CAT_OVERALL = "Overall";
	public static final String CAT_MAGIC_USERS = "MagicUser";
	public static final String CAT_FIGHTERS = "Fighter";
	
	private Comparator scoreComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			int ret = 0;
			
			GameObject go1 = (GameObject)o1;
			GameObject go2 = (GameObject)o2;
			
			int s1 = go1.getThisInt(TOTAL_SCORE);
			int s2 = go2.getThisInt(TOTAL_SCORE);
			ret = s2 - s1; // High scores should be first
			
			return ret;
		}
	};
	
//	public static final String V_GREAT_TREASURES = "GT";
//	public static final String V_USABLE_SPELLS = "US";
//	public static final String V_FAME = "F";
//	public static final String V_NOTORIETY = "N";
//	public static final String V_GOLD = "G";
	
	private static HallOfFame singleton = null;
	
	private File zipFile;
	private GameData hallData = null;
	
	private HallOfFame() {
		PreferenceManager prefMan = new PreferenceManager("RealmSpeak","RealmSpeak.hof");
		zipFile = prefMan.getPrefFile();
		hallData = new GameData("RealmSpeak Hall of Fame");
		hallData.ignoreRandomSeed = true;
	}
	
	private void fetch() {
		if (zipFile.exists()) {
			hallData.zipFromFile(zipFile);
		}
	}
	
	public ArrayList getAllCharacterNames() {
		ArrayList names = new ArrayList();
		for (Iterator i=hallData.getGameObjects().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			if (go.getHeldBy()!=null && !names.contains(go.getName())) {
				names.add(go.getName());
			}
		}
		return names;
	}
	
	public GameObject getHolderFor(String name) {
		String hofName = name+"hof";
		GameObject go = hallData.getGameObjectByName(hofName);
		if (go==null) {
			go = hallData.createNewObject();
			go.setName(hofName);
		}
		return go;
	}
	
	private boolean isWorthy(ArrayList list,GameObject go) {
		int score = go.getThisInt(TOTAL_SCORE);
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject test = (GameObject)i.next();
			int ts = test.getThisInt(TOTAL_SCORE);
			if (score>ts) { // only need one!
				return true;
			}
		}
		return false;
	}
	
	private void add(GameObject listGo,GameObject go) {
		GameObject famer = hallData.createNewObject(go);
		listGo.add(famer);
	}
	
	private void updateList(String listName,GameObject go) {
		GameObject listGo = getHolderFor(listName);
		
		ArrayList list = listGo.getHold();
		if (list.size()<MAX_ENTRIES_PER_CATEGORY || isWorthy(list,go)) {
			add(listGo,go);
			Collections.sort(list,scoreComparator);
			while (list.size()>MAX_ENTRIES_PER_CATEGORY) {
				// remove extras here (trim from tail)
				list.remove(list.size()-1);
			}
		}
	}
	
	private void updateLists(HostPrefWrapper hostPrefs,CharacterWrapper character) {
		// Create a dummy game object for comparison
		GameData dummy = new GameData();
		GameObject go = dummy.createNewObject();
		go.setName(character.getGameObject().getName());
		go.setThisAttribute(NEW_ENTRY);
		go.setThisAttribute(CharacterWrapper.V_GREAT_TREASURES,character.getCurrentVictoryRequirement(CharacterWrapper.V_GREAT_TREASURES));
		go.setThisAttribute(CharacterWrapper.V_USABLE_SPELLS,character.getCurrentVictoryRequirement(CharacterWrapper.V_USABLE_SPELLS));
		go.setThisAttribute(CharacterWrapper.V_FAME,character.getCurrentVictoryRequirement(CharacterWrapper.V_FAME));
		go.setThisAttribute(CharacterWrapper.V_NOTORIETY,character.getCurrentVictoryRequirement(CharacterWrapper.V_NOTORIETY));
		go.setThisAttribute(CharacterWrapper.V_GOLD,character.getCurrentVictoryRequirement(CharacterWrapper.V_GOLD));
		go.setThisAttribute(TOTAL_VPS,character.getTotalAssignedVPs());
		go.setThisAttribute(TOTAL_SCORE,character.getTotalScore());
		go.setThisAttribute(PLAYER_NAME,character.getPlayerName());
		go.setThisAttribute(DAYS_PLAYED,character.getAllDayKeys().size());
		go.setThisAttribute(RS_VERSION,Constants.REALM_SPEAK_VERSION);
		go.setThisAttribute(GAME_DATE,DateUtility.convertDate2String(DateUtility.getNow()));
		
		// Tack on hostprefs
		OrderedHashtable block = go.getAttributeBlock(HostPrefWrapper.HOST_PREF_BLOCK);
		block.putAll(hostPrefs.getGameObject().getAttributeBlock(HostPrefWrapper.HOST_PREF_BLOCK));
		
		updateList(CAT_OVERALL,go);
		if (character.getGameObject().hasThisAttribute("fighter")) {
			updateList(CAT_FIGHTERS,go);
		}
		if (character.getGameObject().hasThisAttribute("magicuser")) {
			updateList(CAT_MAGIC_USERS,go);
		}
		updateList(go.getName(),go);
	}
	
	private void saveResults() {
		if (hallData!=null) {
			// First, remove all NEW_ENTRY keys
			for (Iterator i=hallData.getGameObjects().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				go.removeThisAttribute(NEW_ENTRY);
			}
			hallData.zipToFile(zipFile);
		}
	}
	
	public static void reset() {
		singleton = new HallOfFame();
	}
	
	public static HallOfFame getSingleton() {
		if (singleton==null) {
			singleton = new HallOfFame();
			singleton.fetch();
		}
		return singleton;
	}
	
	public static void consider(HostPrefWrapper hostPrefs,CharacterWrapper character) {
		getSingleton().updateLists(hostPrefs,character);
	}
	public static void save() {
		getSingleton().saveResults();
	}
	
	public static void main(String[] args) {
		String val = DateUtility.convertDate2String(DateUtility.getNow());
		System.out.println(val);
		System.out.println(DateUtility.convertString2Date(val));
	}
}