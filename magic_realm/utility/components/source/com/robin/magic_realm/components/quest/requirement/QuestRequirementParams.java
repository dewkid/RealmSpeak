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
package com.robin.magic_realm.components.quest.requirement;

import java.util.ArrayList;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.quest.*;

public class QuestRequirementParams {
	// General
	public GamePhaseType timeOfCall = GamePhaseType.Unspecified; // default
	public String actionName;
	public CharacterActionType actionType = CharacterActionType.Unknown; // default
	public String dayKey;

	// Search stuff
	public int dieResult;
	public SearchResultType searchType = SearchResultType.Any;
	public GameObject targetOfSearch;
	public ArrayList<GameObject> objectList =  new ArrayList<GameObject>();
	public boolean searchHadAnEffect;
	public ColorMagic burnedColor;
	
	public QuestRequirementParams copy(GameData gameData) {
		return valueOf(asString(),gameData);
	}
	
	public void clearTables() {
		actionName = null;
		actionType = CharacterActionType.Unknown;
		dieResult = -1;
		searchType = null;
		objectList.clear();
		searchHadAnEffect = false;
		targetOfSearch = null;
	}
	public String asString() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(timeOfCall.toString());
		list.add(actionName);
		list.add(dayKey);
		list.add(actionType.toString());
		list.add(String.valueOf(dieResult));
		list.add(searchType == null ? SearchResultType.Any.toString() : searchType.toString());
		list.add(searchHadAnEffect?"T":"F");
		list.add(targetOfSearch==null?"null":targetOfSearch.getStringId());
		if (objectList!=null) {
			for(GameObject res:objectList) {
				list.add(res.getStringId());
			}
		}
		return StringUtilities.collectionToString(list,"@");
	}
	public static QuestRequirementParams valueOf(String s,GameData gameData) {
		ArrayList<String> list = StringUtilities.stringToCollection(s,"@",true);
		QuestRequirementParams qp = new QuestRequirementParams();
		if (list.size()>=8) {
			qp.timeOfCall = GamePhaseType.valueOf(list.get(0));
			qp.actionName = list.get(1)==null?null:list.get(1);
			qp.dayKey = list.get(2)==null?null:list.get(2);
			qp.actionType = CharacterActionType.valueOf(list.get(3));
			qp.dieResult = Integer.valueOf(list.get(4));
			qp.searchType = SearchResultType.valueOf(list.get(5));
			qp.searchHadAnEffect = "T".equals(list.get(6));
			qp.targetOfSearch = readGameObject(list.get(7),gameData);
		}
		if (list.size()>8) {
			for(String val:list.subList(8,list.size())) {
				qp.objectList.add(readGameObject(val,gameData));
			}
		}
		return qp;
	}
	private static GameObject readGameObject(String val,GameData gameData) {
		if (val==null) return null;
		return gameData.getGameObject(Long.valueOf(val));
	}
}