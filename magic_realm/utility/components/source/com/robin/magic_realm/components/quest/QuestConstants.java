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

public class QuestConstants {
	public static final String DEFAULT_EXTENSION = "rsqst";
	
	public static final String QUEST_ERROR = "QuestError"; // Used to log error messages to the detail log
	
	public static final String KEY_PREFIX = "k";
	public static final String VALUE_PREFIX = "v";
	public static final String QUEST_MARK = "_qmark__";
	
	public static final int ALL_VALUE = 999;
	
	public static final String CHOICE = "Player Choice";
	public static final String RANDOM = "Randomly Selected";
	
	//-------------------------------
	
	public static final String FLAG_TESTING = "testing";
	public static final String FLAG_BROKEN = "broken";
	
	public static final String ACTIVATEABLE = "_actvb";
	
	public static final String WORKS_WITH_BOQ = "_rboq";
	public static final String WORKS_WITH_QTR = "_rqtr";
	public static final String QTR_ALL_PLAY = "_qallp";
	public static final String QTR_SECRET_QUEST = "_secq";
	public static final String CARD_COUNT = "_ccnt";
	public static final String VP_REWARD = "_vprw";
	public static final String FOR_FIGHTERS_GUILD = "_gfgt";
	public static final String FOR_THIEVES_GUILD = "_gthv";
	public static final String FOR_MAGIC_GUILD = "_gmag";
	
	public static final String VARIANT_ORIGINAL = "_vorg";
	public static final String VARIANT_PRUITTS = "_vprt";
	public static final String VARIANT_EXP1 = "_vexp1";
	
	public static final String SINGLE_BOARD = "_b1x";
	public static final String DOUBLE_BOARD = "_b2x";
	public static final String TRIPLE_BOARD = "_b3x";
	
	public static final String CHARACTER_FIGHTER = "_cfgt";
	public static final String CHARACTER_MAGIC = "_cmag";
	public static final String CHARACTER_MALE = "_cmal";
	public static final String CHARACTER_FEMALE = "_cfem";
	public static final String CHARACTER_SPEC_REGEX = "_crgx";
}