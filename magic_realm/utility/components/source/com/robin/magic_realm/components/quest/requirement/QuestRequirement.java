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

import java.util.Hashtable;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

/**
 * Quest requirements determine what must happen before the step can be
 * completed, and rewards (if any) are given.
 */
public abstract class QuestRequirement extends AbstractQuestObject {

	public enum RequirementType {
		Active, // active or inactive
		Attribute,
		ColorMagic,
		Discovery, // must have a specific discovery
		GamePhase, // end of phase, end of day, start of evening, midnight, birdsong
		Inventory, // a requirement that tests what you have in inventory
		Kill, 
		LearnAwaken,
		Loot, // (optional location designation)
		NoDenizens,
		MinorCharacter,
		MissionCampaign,
		OccupyLocation,
		Path, // follows a specific path
		SearchResult, // (optional location designation) Clues, Paths, Passages, Hidden Enemies, Discover Chit(s), Learn and Awaken, Curse!, Awaken, Counters, Treasure Cards,Perceive Spell
		TimePassed,
		Trade,
		;
		public String getDescription() {
			switch (this) {
				case Active:
					return "Tests whether the quest is active or not (needed for Quest Cards)";
				case Attribute:
					return "Tests whether a specific attribute (fame, notoriety, gold) has reached a target number.";
				case GamePhase:
					return "Tests for a specific game phase (Birdsong, End-of-phase, End-of-turn, Evening).";
				case Inventory:
					return "Tests the contents of inventory.";
				case Kill:
					return "Tests for a specific kill or kills.";
				case LearnAwaken:
					return "Tests whether a spell has just been awakened and/or learned.";
				case Loot:
					return "Tests for a specific item result of looting.";
				case OccupyLocation:
					return "Tests whether the character is in a specific location.";
				case SearchResult:
					return "Tests for a specific search result.";
				case TimePassed:
					return "Tests for a specific length of time passed.";
				case MissionCampaign:
					return "Tests for start/stop of Mission/Campaign chits.";
				case Trade:
					return "Tests for a specific TRADE occurrence.";
				case Discovery:
					return "Tests for a specific recorded discovery.";
				case NoDenizens:
					return "Tests for the absence of monster/natives in the clearing or tile.";
				case MinorCharacter:
					return "Tests for the presence of a minor character in the character inventory.";
				case Path:
					return "Tests whether the character has followed a specific path."; 
				case ColorMagic:
					return "Tests whether the character is in the presence of a specific color of magic (either permanent or burning a chit).";
			}
			return "(No Description)";
		}
	}
	
	public String toString() {
		return buildDescription();
	}
	
	public static final String AUTO_JOURNAL = "_aj";
	public static final String NOT = "_not__";

	protected abstract boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams);
	public abstract RequirementType getRequirementType();
	protected abstract String buildDescription();

	public QuestRequirement(GameObject go) {
		super(go);
	}

	public void init() {
		setName("Requirement");
		getGameObject().setThisAttribute(Quest.QUEST_REQUIREMENT, getRequirementType().toString());
	}

	/**
	 * Called when quest is first activated to allow setting up requirement at
	 * quest start.
	 */
	public void activate(CharacterWrapper character) {
		// This implementation does nothing.  Override to add this functionality.
	}

	/**
	 * Override this method if minor character is relevant, and handle
	 * appropriately.
	 */
	public boolean usesMinorCharacter(QuestMinorCharacter mc) {
		return false;
	}

	/**
	 * Override this method if location is relevant, and handle appropriately.
	 */
	public boolean usesLocationTag(String tag) {
		return false;
	}

	public void updateIds(Hashtable<Long, GameObject> lookup) {
		// override if IDs need to be updated!
	}

	public boolean isAutoJournal() {
		return getBoolean(AUTO_JOURNAL);
	}

	public boolean isNot() {
		return getBoolean(NOT);
	}

	public String getDescription() {
		return (isNot() ? "NOT " : "") + buildDescription();
	}

	public boolean fulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		boolean result = testFulfillsRequirement(frame, character, reqParams);
		return isNot() ? !result : result;
	}

	public static QuestRequirement getRequirement(RequirementType type, GameObject go) {
		QuestRequirement requirement = null;
		switch (type) {
			case Attribute:
				requirement = new QuestRequirementAttribute(go);
				break;
			case OccupyLocation:
				requirement = new QuestRequirementLocation(go);
				break;
			case Loot:
				requirement = new QuestRequirementLoot(go);
				break;
			case Kill:
				requirement = new QuestRequirementKill(go);
				break;
			case TimePassed:
				requirement = new QuestRequirementTimePassed(go);
				break;
			case GamePhase:
				requirement = new QuestRequirementGamePhase(go);
				break;
			case Active:
				requirement = new QuestRequirementActive(go);
				break;
			case SearchResult:
				requirement = new QuestRequirementSearchResult(go);
				break;
			case Inventory:
				requirement = new QuestRequirementInventory(go);
				break;
			case MissionCampaign:
				requirement = new QuestRequirementMissionCampaign(go);
				break;
			case Trade:
				requirement = new QuestRequirementTrade(go);
				break;
			case Discovery:
				requirement = new QuestRequirementDiscovery(go);
				break;
			case NoDenizens:
				requirement = new QuestRequirementNoDenizens(go);
				break;
			case LearnAwaken:
				requirement = new QuestRequirementLearnAwaken(go);
				break;
			case MinorCharacter:
				requirement = new QuestRequirementMinorCharacter(go);
				break;
			case Path:
				requirement = new QuestRequirementPath(go);
				break;
			case ColorMagic:
				requirement = new QuestRequirementColorMagic(go);
				break;
			default:
				throw new IllegalArgumentException("Unsupported RequirementType: " + type.toString());
		}
		return requirement;
	}
}