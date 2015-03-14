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

import java.util.*;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.ImageCache;
import com.robin.general.util.RandomNumber;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class RealmCalendar {
	
	public static final int DAYS_IN_A_MONTH = 28; //28
	public static final int WEEKS_IN_A_MONTH = 4; //4
	private static final ColorMagic DAY_14_COLOR = new ColorMagic(ColorMagic.GRAY,true);
	private static final ColorMagic DAY_21_COLOR = new ColorMagic(ColorMagic.PURPLE,true);
	private static final ColorMagic DAY_28_COLOR = new ColorMagic(ColorMagic.GOLD,true);
	
	/*
	 * Special rules
	 * 
	 * FINISHED - note1 = Fatigue 1 asterisk if outside (!cave && !dwelling) during Birdsong
	 * FINISHED - note2 = Fatigue 1 asterisk/phase outside (even if you are blocked!)
	 * FINISHED - note3 = Hide table cannot be used
	 * FINISHED - note4 = Fatigue 1 asterisk if in the heat (!cave && !mountain) during Birdsong
	 * FINISHED - note5 = Fatigue 1 asterisk/phase in mountains (even if you are blocked!)
	 * FINISHED - note6 = Peer table cannot be used (enhanced peer is okay)
	 */
	
	private static final int FATIGUE_1_OUTSIDE = 1;
	private static final int FATIGUE_PHASES_OUTSIDE = 2;
	private static final int HIDE_DISABLED = 3;
	private static final int FATIGUE_1_HEAT = 4;
	private static final int FATIGUE_PHASES_MOUNTAIN = 5;
	private static final int PEER_DISABLED = 6;
	
	public static final String WEATHER_CLEAR = "clear";
	public static final String WEATHER_SHOWERS = "showers";
	public static final String WEATHER_STORM = "storm";
	public static final String WEATHER_SPECIAL = "special";
	
	private static RealmCalendar currentCalendar = null;
	
	private Hashtable<Integer,GameObject> seasonsHash;
	private GameWrapper game;
	private GameData gameData;
	private String currentWeather;
	private int currentMonth;
	private int seasonOffset;
	private GameObject currentSeason = null;
	private int days;
	private int basic;
	private int sunlight;
	private int sheltered;
	private int mountainMoveCost;
	private int victoryPoints;
	private int specialNotes; // 0 if none in play
	private String seasonDescription;
	private String weatherName;
	private String weatherTypeName;
	private String seasonName;
	private int missionRewards;
	private String foodAlePrimaryTarget;
	private String foodAleSecondaryTarget;
	private String escortPartyPrimaryTarget;
	private String escortPartySecondaryTarget;
	private ImageIcon seasonIcon;
	private ImageIcon fullSeasonIcon;
	private ArrayList<ColorMagic> seventhDayColors;
	private boolean usingWeather;
	
	private RealmCalendar(GameData data,GameWrapper game,HostPrefWrapper hostPrefs) {
		this.gameData = data;
		this.game = game;
		usingWeather = hostPrefs.hasPref(Constants.OPT_WEATHER);
		currentWeather = null;
		loadSeasonsHash();
		if (game.hasSeasonOffset()) {
			seasonOffset = game.getSeasonOffset();
		}
		else {
			String val = hostPrefs.getStartingSeason();
			if (val==null) { // This happens when loading an old savegame, and IGNORE_VERSION is on
				seasonOffset = -1;
				System.err.println("RealmCalendar:  Loading old game");
				// May not work anyway, but at least this wont stop it
			}
			else {
				if (val.toLowerCase().indexOf("random")>=0) {
					seasonOffset = RandomNumber.getRandom(13);
					// set the starting season to 
				}
				else {
					seasonOffset = getIndexOf(val)-1;
				}
			}
			game.setSeasonOffset(seasonOffset);
		}
	}
	public boolean isUsingWeather() {
		return usingWeather;
	}
	private void updateSeason(int month) {
		boolean changes = false;
		if (currentWeather==null || !currentWeather.equals(game.getWeather())) {
			currentWeather = game.getWeather();
			changes = true;
		}
		if (currentMonth!=month || currentSeason==null) {
			currentMonth = month;
			if (seasonOffset==-1) {
				currentSeason = seasonsHash.get(new Integer(0));
			}
			else {
				int n = ((currentMonth+seasonOffset-1)%13)+1;
				currentSeason = seasonsHash.get(new Integer(n));
			}
			changes = true;
		}
		if (changes) {
			updateSeasonAttributes();
		}
	}
	private void updateSeasonAttributes() {
		seasonName = currentSeason.getName();
		if (seasonOffset>=0) {
			seasonIcon = ImageCache.getIcon("season/"+currentSeason.getThisAttribute("icon"),50);
			fullSeasonIcon = ImageCache.getIcon("season/"+currentSeason.getThisAttribute("icon"));
		}
		else {
			seasonIcon = null;
			fullSeasonIcon = null;
		}
		victoryPoints = currentSeason.getInt("this","vps");
		
		if (currentWeather==null) {
			// 3/24/2007 - currentWeather was null? again on 6/23/2007
			// Not sure why it is EVER null, but if this happens, make it clear
			currentWeather = WEATHER_CLEAR;
		}
		
		days = currentSeason.getInt(currentWeather,"days");
		basic = currentSeason.getInt(currentWeather,"basic");
		sunlight = currentSeason.getInt(currentWeather,"sunlight");
		sheltered = currentSeason.getInt(currentWeather,"sheltered");
		mountainMoveCost = currentSeason.getInt("this","mountain_cost");
		weatherName = currentSeason.getAttribute(currentWeather,"name");
		weatherTypeName = StringUtilities.capitalize(currentWeather);
		specialNotes = 0;
		seasonDescription = "";
		if (currentSeason.hasAttribute(currentWeather,"special")) {
			String note = currentSeason.getAttribute(currentWeather,"special"); // note = "note3" (for example)
			specialNotes = Integer.valueOf(note.substring(4)).intValue();
			seasonDescription = currentSeason.getAttribute(currentWeather,"description");
		}
		
		// Mission targets and rewards
		String foodAle = currentSeason.getThisAttribute("food_ale");
		StringTokenizer tokens = new StringTokenizer(foodAle,",");
		foodAlePrimaryTarget = tokens.nextToken();
		foodAleSecondaryTarget = tokens.nextToken();
		
		String escortParty = currentSeason.getThisAttribute("escort_party");
		tokens = new StringTokenizer(escortParty,",");
		escortPartyPrimaryTarget = tokens.nextToken();
		escortPartySecondaryTarget = tokens.nextToken();
		
		missionRewards = currentSeason.getThisInt("reward");
		
		// Magic is a bit more involved
		String magic = currentSeason.getThisAttribute("magic");
		tokens = new StringTokenizer(magic,",");
		seventhDayColors = new ArrayList<ColorMagic>();
		while(tokens.hasMoreTokens()) {
			String val = tokens.nextToken();
			seventhDayColors.add(ColorMagic.makeColorMagic(val,true));
		}
	}
	private void loadSeasonsHash() {
		seasonsHash = new Hashtable<Integer,GameObject>();
		GamePool pool = new GamePool(gameData.getGameObjects());
		ArrayList list = pool.find("season");
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			Integer n = go.getInteger("this","season");
			seasonsHash.put(n,go);
		}
	}
	private int getIndexOf(String seasonName) {
		ArrayList list = getAllSeasons();
		for (int i=0;i<list.size();i++) {
			GameObject go = (GameObject)list.get(i);
			if (go.getName().equals(seasonName)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Invalid argument: "+seasonName);
	}
	public ArrayList<GameObject> getAllSeasons() {
		ArrayList<GameObject> list = new ArrayList<GameObject>(seasonsHash.values());
		Collections.sort(list,seasonComparator);
		return list;
	}
	public GameObject getCurrentSeason(int month) {
		updateSeason(month);
		return currentSeason;
	}
	public String getWeatherName(int month) {
		updateSeason(month);
		return weatherName;
	}
	public String getWeatherTypeName(int month) {
		updateSeason(month);
		return weatherTypeName;
	}
	public String getSeasonName(int month) {
		updateSeason(month);
		return seasonName;
	}
	public ImageIcon getFullSeasonIcon(int month) {
		updateSeason(month);
		return fullSeasonIcon;
	}
	public ImageIcon getSeasonIcon(int month) {
		updateSeason(month);
		return seasonIcon;
	}
	public int getDays(int month) {
		updateSeason(month);
		return days;
	}
	public int getBasicPhases(int month) {
		updateSeason(month);
		return basic;
	}
	public int getSunlightPhases(int month) {
		updateSeason(month);
		return sunlight;
	}
	public int getShelteredPhases(int month) {
		updateSeason(month);
		return sheltered;
	}
	public int getMountainMoveCost(int month) {
		updateSeason(month);
		return mountainMoveCost;
	}
	public int getVictoryPoints(int month) {
		updateSeason(month);
		return victoryPoints;
	}
	public boolean isFagitue1Outside(int month) {
		updateSeason(month);
		return specialNotes == FATIGUE_1_OUTSIDE;
	}
	public boolean isFatiguePhasesOutside(int month) {
		updateSeason(month);
		return specialNotes == FATIGUE_PHASES_OUTSIDE;
	}
	public boolean isHideDisabled(int month) {
		updateSeason(month);
		return specialNotes == HIDE_DISABLED;
	}
	public boolean isFatigue1Heat(int month) {
		updateSeason(month);
		return specialNotes == FATIGUE_1_HEAT;
	}
	public boolean isFatiguePhasesType(int month) {
		return isFatiguePhasesMountain(month) || isFatiguePhasesOutside(month);
	}
	public boolean isFatiguePhasesMountain(int month) {
		updateSeason(month);
		return specialNotes == FATIGUE_PHASES_MOUNTAIN;
	}
	public boolean isPeerDisabled(int month) {
		updateSeason(month);
		return specialNotes == PEER_DISABLED;
	}
	public boolean hasSpecial(int month) {
		updateSeason(month);
		return specialNotes>0;
	}
	public String getSeasonDescription(int month) {
		updateSeason(month);
		return seasonDescription;
	}
	public int getMissionRewards(int month) {
		updateSeason(month);
		return missionRewards;
	}
	public String getMissionPrimaryTarget(int month,String type) {
		updateSeason(month);
		if ("food_ale".equals(type)) {
			return foodAlePrimaryTarget;
		}
		return escortPartyPrimaryTarget;
	}
	public String getMissionSecondaryTarget(int month,String type) {
		updateSeason(month);
		if ("food_ale".equals(type)) {
			return foodAleSecondaryTarget;
		}
		return escortPartySecondaryTarget;
	}
	public String getWeatherAttribute(int month,String key) {
		updateSeason(month);
		return currentSeason.getAttribute(currentWeather,key);
	}
	public ArrayList<ColorMagic> getColorMagic(int month,int day) {
		updateSeason(month);
		ArrayList<ColorMagic> colors = new ArrayList<ColorMagic>();
		
		if (day==7) {
			colors.addAll(seventhDayColors);
		}
		else if (day==14) {
			colors.add(DAY_14_COLOR);
		}
		else if (day==21) {
			colors.add(DAY_21_COLOR);
		}
		else if (day==28) {
			colors.add(DAY_28_COLOR);
		}
		
		return colors;
	}
	public String getColorMagicName(int month,int day) {
		Collection c = getColorMagic(month,day);
		if (c.size()==1) {
			ColorMagic cm = (ColorMagic)c.iterator().next();
			return cm.getColorName();
		}
		else if (c.size()==2) {
			return "White & Black";
		}
		else if (c.size()==4) {
			return "All except White";
		}
		else if (c.size()==5) {
			return "All colors";
		}
		return "";
	}
	public void setWeatherResult(int result) {
		switch(result) {
			default:
				game.setWeather(RealmCalendar.WEATHER_SPECIAL);
				break;
			case 4:
				game.setWeather(RealmCalendar.WEATHER_STORM);
				break;
			case 5:
				game.setWeather(RealmCalendar.WEATHER_SHOWERS);
				break;
			case 6:
				game.setWeather(RealmCalendar.WEATHER_CLEAR);
				break;
		}
	}
	
	public static boolean isSeventhDay(int day) {
		return day==7 || day==14 || day==21 || day==28;
	}
	public static RealmCalendar getCalendar(GameData data) {
		if (currentCalendar==null) {
			GameWrapper game = GameWrapper.findGame(data);
			HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
			if (hostPrefs!=null) {
				currentCalendar = new RealmCalendar(data,game,hostPrefs);
			}
		}
		return currentCalendar;
	}
	public static void reset() {
		currentCalendar = null;
	}
	public static ArrayList findSeasons(GameData data) {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList list = pool.find("season");
		Collections.sort(list,seasonComparator);
		return list;
	}
	private static final Comparator<GameObject> seasonComparator = new Comparator<GameObject>() {
		public int compare(GameObject go1, GameObject go2) {
			int n1 = go1.getThisInt("season");
			int n2 = go2.getThisInt("season");
			return n1-n2;
		}
	};
}