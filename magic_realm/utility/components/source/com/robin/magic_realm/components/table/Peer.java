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

import com.robin.magic_realm.components.ClearingDetail;
import com.robin.magic_realm.components.PathDetail;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.SearchResultType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.PathIcon;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Peer extends Search {
	public Peer(JFrame frame) {
		this(frame,null);
	}
	public Peer(JFrame frame,ClearingDetail clearing) {
		super(frame,clearing);
	}
	public String getTableName(boolean longDescription) {
		return "Peer"+(longDescription?"\n(Hidden Paths, Hidden Enemies)":"");
	}
	public String getTableKey() {
		return "Peer";
	}

	public String applyOne(CharacterWrapper character) {
		// Choice
		return doChoice(character);
	}

	public String applyTwo(CharacterWrapper character) {
		// Clues, Paths
		doClues(character);
		String pathRes = doPaths(character);
		if (pathRes!=null) {
			return "Clues and "+pathRes;
		}
		return "Clues";
	}

	public String applyThree(CharacterWrapper character) {
		// Hidden Enemies, Paths
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.HiddenEnemies;
		qp.searchHadAnEffect = !character.foundHiddenEnemies();
		character.testQuestRequirements(getParentFrame(),qp);
		
		character.setFoundHiddenEnemies(true); // marks ALL hidden enemies
		
		String pathRes = doPaths(character);
		if (pathRes!=null) {
			return "Found hidden enemies and "+pathRes;
		}
		return "Found hidden enemies";
	}

	public String applyFour(CharacterWrapper character) {
		// Found Hidden Enemies
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.HiddenEnemies;
		qp.searchHadAnEffect = !character.foundHiddenEnemies();
		character.testQuestRequirements(getParentFrame(),qp);
		
		character.setFoundHiddenEnemies(true); // marks ALL hidden enemies
		
		return "Found hidden enemies";
	}

	public String applyFive(CharacterWrapper character) {
		// Clues
		doClues(character);
		return "Clues";
	}

	public String applySix(CharacterWrapper character) {
		// Nothing
		return "Nothing";
	}

	@Override
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		for(PathDetail path:getAllUndiscoveredPaths(character)) {
			list.add(new PathIcon(path));
		}
		return list;
	}

	protected String doPaths(CharacterWrapper character) {
		ArrayList<PathDetail> list = getAllUndiscoveredPaths(character);
		for (PathDetail path:list) {
			character.addHiddenPathDiscovery(path.getFullPathKey());
		}
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = getTableKey();
		qp.actionType = CharacterActionType.SearchTable;
		qp.searchType = SearchResultType.Paths;
		String ret = "Path(s)";
		if (list.size() > 0) {
			ret = "Found " + list.size() + " path(s)";
			qp.searchHadAnEffect = true;
		}
		character.testQuestRequirements(getParentFrame(),qp);
		return ret;
	}
}