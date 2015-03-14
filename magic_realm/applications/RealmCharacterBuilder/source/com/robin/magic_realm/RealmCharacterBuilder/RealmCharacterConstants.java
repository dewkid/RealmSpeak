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
package com.robin.magic_realm.RealmCharacterBuilder;

import com.robin.magic_realm.components.utility.Constants;

public class RealmCharacterConstants {
	
	public static String CUSTOM_ICON_BASE_PATH = "custom/";
	
	public static String[] CHIT_TYPES = {
		"MOVE",
		"FIGHT",
		"MAGIC",
		"FLY",
		"SPECIAL",
	};
	
	public static String[] MOVE_POSITIONS = {
		"CHARGE",
		"DODGE",
		"DUCK",
	};
	
	public static String[] FIGHT_POSITIONS = {
		"THRUST",
		"SWING",
		"SMASH",
	};
	
	public static String[] ONOFF = {
		"OFF",
		"ON",
	};
	
	public static String[] YESNO = {
		"NO",
		"YES",
	};
	
	public static String[][] DEFAULT_RELATIONSHIPS = {
		{Constants.GAME_RELATIONSHIP,"NBashkars"},
		{Constants.GAME_RELATIONSHIP,"NCompany"},
		{Constants.GAME_RELATIONSHIP,"NGuard"},
		{Constants.GAME_RELATIONSHIP,"NLancers"},
		{Constants.GAME_RELATIONSHIP,"NOrder"},
		{Constants.GAME_RELATIONSHIP,"NPatrol"},
		{Constants.GAME_RELATIONSHIP,"NRogues"},
		{Constants.GAME_RELATIONSHIP,"NSoldiers"},
		{Constants.GAME_RELATIONSHIP,"NWoodfolk"},
		{Constants.GAME_RELATIONSHIP,"VCrone"},
		{Constants.GAME_RELATIONSHIP,"VScholar"},
		{Constants.GAME_RELATIONSHIP,"VShaman"},
		{Constants.GAME_RELATIONSHIP,"VWarlock"},
		{Constants.GAME_RELATIONSHIP,"NDragonmen"},
		{Constants.GAME_RELATIONSHIP,"NMurker"},
	};
	
	public static String[] SPELL_COUNT = {
		"0",
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
	};
	public static String[] SPELL_COUNT_STRING = {
		"zero",
		"one",
		"two",
		"three",
		"four",
		"five",
		"six",
	};
	
	public static String[] SPEEDS = {
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
	};
	public static String[] SPEEDS_W_NEG = {
		" ",
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
	};
	
	public static String[] STRENGTHS = {
		"L",
		"M",
		"H",
		"T",
	};
	
	public static String[] STRENGTHS_PLUS_NEG = {
		"",
		"L",
		"M",
		"H",
		"T",
	};
	
	public static String[] MAGICS = {
		"I",
		"II",
		"III",
		"IV",
		"V",
		"VI",
		"VII",
		"VIII",
	};
	
	public static String[] EFFORTS = {
		"",
		"*",
		"**",
		"***",
	};
	public static String[] SHARPNESS = {
		" ",
		"1",
		"2",
		"3",
	};
}