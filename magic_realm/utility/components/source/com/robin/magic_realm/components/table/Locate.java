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
package com.robin.magic_realm.components.table;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.PathIcon;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Locate extends Search {
	public Locate(JFrame frame) {
		this(frame,null);
	}
	public Locate(JFrame frame,ClearingDetail clearing) {
		super(frame,clearing);
	}
	public String getTableName(boolean longDescription) {
		return "Locate"+(longDescription?"\n(Secret Passages, Treasure Locations)":"");
	}
	public String getTableKey() {
		return "Locate";
	}
//public String apply(CharacterWrapper character,DieRoller roller) {
//System.err.println("Locate.java DEBUGGO");
//return applyFour(character);
//}
	public String applyOne(CharacterWrapper character) {
		// Choice
		return doChoice(character);
	}

	public String applyTwo(CharacterWrapper character) {
		// Clues, Passages
		doClues(character);
		String passageRes = doPassages(character);
		if (passageRes!=null) {
			return "Clues and "+passageRes;
		}
		return "Clues";
	}

	public String applyThree(CharacterWrapper character) {
		// Passages
		return doPassages(character);
	}

	public String applyFour(CharacterWrapper character) {
		// Discover Chits
		return doDiscoverChits(character);
	}

	public String applyFive(CharacterWrapper character) {
		// Nothing
		return "Nothing";
	}

	public String applySix(CharacterWrapper character) {
		// Nothing
		return "Nothing";
	}
	
	@Override
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		for(PathDetail path:getAllUndiscoveredPassages(character)) {
			list.add(new PathIcon(path));
		}
		for(RealmComponent rc:getAllDiscoverableChits(character,true)) {
			list.add(getIconForSearch(rc));
		}
		return list;
	}
	
	protected String doPassages(CharacterWrapper character) {
		ArrayList<PathDetail> list = getAllUndiscoveredPassages(character);
		for (PathDetail path:list) {
			character.addSecretPassageDiscovery(path.getFullPathKey());
		}
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.Passages;
		
		String ret = "Passage(s)";
		if (list.size()>0) {
			ret = "Found "+list.size()+" passage(s)";
			qp.searchHadAnEffect = true;
		}
		character.testQuestRequirements(getParentFrame(),qp);
		return ret;
	}
}