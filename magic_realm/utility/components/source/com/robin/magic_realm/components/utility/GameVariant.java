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

public class GameVariant {
	public static GameVariant ORIGINAL_GAME_VARIANT = new GameVariant("Original Game","original_game","standard_game",true); 
	public static GameVariant PRUITTS_GAME_VARIANT = new GameVariant("Pruitt's Monsters","alt_monsters1_game","standard_game",true); 
	public static GameVariant EXP1_GAME_VARIANT = new GameVariant("Expansion One","rw_expansion_1","rw_expansion_1_setup",false); 
	
	private String title;
	private String keyVals;
	private String setup;
	private boolean allowBoardVariants;

	public GameVariant(String title, String keyVals, String setup, boolean allowAddExpansionMix) {
		this.title = title;
		this.keyVals = keyVals;
		this.setup = setup;
		this.allowBoardVariants = allowAddExpansionMix;
	}

	public String toString() {
		return title;
	}
	
	public String getTitle() {
		return title;
	}

	public String getKeyVals() {
		return keyVals;
	}

	public String getSetup() {
		return setup;
	}

	public boolean getAllowBoardVariants() {
		return allowBoardVariants;
	}
}