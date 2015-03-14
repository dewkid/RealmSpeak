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

public enum SearchResultType {
	Any,
	Awaken,
	Clues,
	Counters,
	Curse,
	DiscoverChits,
	HiddenEnemies,
	LearnAndAwaken,
	Passages,
	Paths,
	PerceiveSpell,
	TakeTopTreasure,
	Take2ndTreasure,
	Take3rdTreasure,
	Take4thTreasure,
	Take5thTreasure,
	Take6thTreasure,
	TreasureCards,
	
	CaveTeleport,
	PeerEnchantAnyClearing,
	PowerOfThePit,
	
	Gold,
	
	Wish,
	Heal,
	
	Rest,
	RemoveCurse,
	Wound,
	;
	public boolean canGetTreasure() {
		switch(this) {
			case TakeTopTreasure:
			case Take2ndTreasure:
			case Take3rdTreasure:
			case Take4thTreasure:
			case Take5thTreasure:
			case Take6thTreasure:
			case TreasureCards:
			case Counters:
				return true;
		}
		return false;
	}
	public boolean canGetSpell() {
		switch(this) {
			case LearnAndAwaken:
			case Awaken:
			case PerceiveSpell:
				return true;
		}
		return false;
	}
	public static String[] optionalValues() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("");
		for(SearchResultType rt:values()) {
			list.add(rt.toString());
		}
		return list.toArray(new String[0]);
	}
	public static SearchResultType getLootSearchResultType(int treasureNumber) {
		switch(treasureNumber) {
			case 1:		return SearchResultType.TakeTopTreasure;
			case 2:		return SearchResultType.Take2ndTreasure;
			case 3:		return SearchResultType.Take3rdTreasure;
			case 4:		return SearchResultType.Take4thTreasure;
			case 5:		return SearchResultType.Take5thTreasure;
			case 6:		return SearchResultType.Take6thTreasure;
		}
		return null;
	}
	public static SearchResultType[] getSearchResultTypes(SearchTableType table) {
		ArrayList<SearchResultType> list = new ArrayList<SearchResultType>();
		list.add(SearchResultType.Any);
		switch(table) {
			case Any:
				return SearchResultType.values();
			case Locate:
				list.add(SearchResultType.Passages);
				list.add(SearchResultType.Clues);
				list.add(SearchResultType.DiscoverChits);
				break;
			case Loot:
				list.add(SearchResultType.TakeTopTreasure);
				list.add(SearchResultType.Take2ndTreasure);
				list.add(SearchResultType.Take3rdTreasure);
				list.add(SearchResultType.Take4thTreasure);
				list.add(SearchResultType.Take5thTreasure);
				list.add(SearchResultType.Take6thTreasure);
				break;
			case MagicSight:
				list.add(SearchResultType.Counters);
				list.add(SearchResultType.TreasureCards);
				list.add(SearchResultType.PerceiveSpell);
				list.add(SearchResultType.DiscoverChits);
				break;
			case Peer:
				list.add(SearchResultType.Clues);
				list.add(SearchResultType.Paths);
				list.add(SearchResultType.HiddenEnemies);
				break;
			case ReadRunes:
				list.add(SearchResultType.LearnAndAwaken);
				list.add(SearchResultType.Awaken);
				list.add(SearchResultType.Curse);
				break;
			case ToadstoolCircle:
				list.add(SearchResultType.Counters);
				list.add(SearchResultType.TreasureCards);
				list.add(SearchResultType.CaveTeleport);
				list.add(SearchResultType.PeerEnchantAnyClearing);
				list.add(SearchResultType.PowerOfThePit);
				break;
			case CryptOfTheKnight:
				list.add(SearchResultType.Counters);
				list.add(SearchResultType.TreasureCards);
				list.add(SearchResultType.Gold);
				list.add(SearchResultType.Curse);
				break;
			case EnchantedMeadow:
				list.add(SearchResultType.Counters);
				list.add(SearchResultType.Wish);
				list.add(SearchResultType.Heal);
				list.add(SearchResultType.Curse);
				break;
			case FountainOfHealth:
				list.add(SearchResultType.Heal);
				list.add(SearchResultType.Rest);
				list.add(SearchResultType.RemoveCurse);
				list.add(SearchResultType.Wound);
				break;
			case ArcheologicalDig:
				list.add(SearchResultType.DiscoverChits);
				list.add(SearchResultType.Clues);
				list.add(SearchResultType.Gold);
				list.add(SearchResultType.Wound);
				break;
		}
		
		return list.toArray(new SearchResultType[list.size()]);
	}
}