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
package com.robin.magic_realm.RealmQuestBuilder;

import java.util.ArrayList;

import javax.swing.JFrame;

import com.robin.game.objects.*;
import com.robin.magic_realm.RealmCharacterBuilder.EditPanel.CompanionEditPanel;
import com.robin.magic_realm.RealmQuestBuilder.QuestPropertyBlock.FieldType;
import com.robin.magic_realm.components.attribute.RelationshipType;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementLocation;
import com.robin.magic_realm.components.quest.reward.*;

public class QuestRewardEditor extends QuestBlockEditor {

	private Quest quest;
	private QuestReward reward;

	// RewardGroups allow characters to choose a reward. This is also helpful if
	// there is a starting reward that you get when you start the quest.
	private static final String[] RewardGroups = { QuestReward.ALL_REWARD_GROUP, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public QuestRewardEditor(JFrame parent, GameData realmSpeakData, Quest quest, QuestReward reward) {
		super(parent, realmSpeakData, reward);
		this.quest = quest;
		this.reward = reward;
		setLocationRelativeTo(parent);
		read();
	}

	public String getEditorTitle() {
		return "Quest Reward: " + reward.getRewardType();
	}

	public boolean getCanceledEdit() {
		return canceledEdit;
	}

	protected ArrayList<QuestPropertyBlock> createPropertyBlocks() {
		ArrayList<QuestPropertyBlock> list = new ArrayList<QuestPropertyBlock>();
		list.add(new QuestPropertyBlock(QuestReward.REWARD_GROUP, "Reward group", FieldType.StringSelector, RewardGroups));
		switch (reward.getRewardType()) {
			case Information:
				list.add(new QuestPropertyBlock(QuestRewardInformation.INFORMATION_TEXT, "Information to provide", FieldType.TextArea));
				break;
			case Item:
				list.add(new QuestPropertyBlock(QuestRewardItem.GAIN_TYPE, "Gain or lose item", FieldType.StringSelector, GainType.values()));
				list.add(new QuestPropertyBlock(QuestRewardItem.ITEM_DESC, "Description", FieldType.TextLine));
				list.add(new QuestPropertyBlock(QuestRewardItem.ITEM_CHITTYPES, "Item Type Restriction", FieldType.ChitType));
				list.add(new QuestPropertyBlock(QuestRewardItem.ITEM_REGEX, "Item RegEx", FieldType.Regex));
				break;
			case Attribute:
				list.add(new QuestPropertyBlock(QuestRewardAttribute.ATTRIBUTE_TYPE, "Affected Attribute", FieldType.StringSelector, new Object[] { AttributeType.Fame, AttributeType.Notoriety, AttributeType.Gold }));
				list.add(new QuestPropertyBlock(QuestRewardAttribute.GAIN_TYPE, "Gain or lose", FieldType.StringSelector, GainType.values()));
				list.add(new QuestPropertyBlock(QuestRewardAttribute.ATTRIBUTE_CHANGE, "Amount", FieldType.Number));
				break;
			case Hireling:
				list.add(new QuestPropertyBlock(QuestRewardHireling.HIRELING_REGEX, "Native RegEx", FieldType.Regex, null, new String[] { "native,rank" }));
				list.add(new QuestPropertyBlock(QuestRewardHireling.ACQUISITION_TYPE, "Method to acquire hireling", FieldType.StringSelector, ChitAcquisitionType.values()));
				list.add(new QuestPropertyBlock(QuestRewardHireling.TERM_OF_HIRE, "Term of hire", FieldType.StringSelector, TermOfHireType.values()));
				break;
			case Visitor:
				list.add(new QuestPropertyBlock(QuestRewardVisitor.VISITOR_REGEX, "Visitor RegEx", FieldType.Regex, null, new String[] { "visitor" }));
				list.add(new QuestPropertyBlock(QuestRewardVisitor.ACQUISITION_TYPE, "Method to acquire hireling", FieldType.StringSelector, ChitAcquisitionType.values()));
				break;
			case Companion:
				list.add(new QuestPropertyBlock(QuestRewardCompanion.COMPANION_NAME, "Companion", FieldType.CompanionSelector, getAllCompanionKeyValues()));
				list.add(new QuestPropertyBlock(QuestRewardCompanion.GAIN_TYPE, "Gain or lose", FieldType.StringSelector, GainType.values()));
				break;
			case Teleport:
				list.add(new QuestPropertyBlock(QuestRequirementLocation.LOCATION, "Teleport to", FieldType.GameObjectWrapperSelector, quest.getLocations().toArray()));
				break;
			case RelationshipSet:
				list.add(new QuestPropertyBlock(QuestRewardRelationshipSet.NATIVE_GROUP, "Native group", FieldType.StringSelector, getRelationshipNames()));
				list.add(new QuestPropertyBlock(QuestRewardRelationshipSet.RELATIONSHIP_SET, "Relationship to set", FieldType.StringSelector, RelationshipType.RelationshipNames));
				break;
			case RelationshipChange:
				list.add(new QuestPropertyBlock(QuestRewardRelationshipChange.NATIVE_GROUP, "Native group", FieldType.StringSelector, getRelationshipNames()));
				list.add(new QuestPropertyBlock(QuestRewardRelationshipChange.GAIN_TYPE, "Gain or lose", FieldType.StringSelector, GainType.values()));
				list.add(new QuestPropertyBlock(QuestRewardRelationshipChange.RELATIONSHIP_CHANGE, "Levels of Friendliness", FieldType.Number));
				break;
			case QuestComplete:
				break;
			case QuestFailed:
				break;
			case SummonGuardian:
				list.add(new QuestPropertyBlock(QuestRequirementLocation.LOCATION, "Summon Guardian for ", FieldType.GameObjectWrapperSelector, quest.getLocations().toArray()));
				break;
			case AlterHide:
				list.add(new QuestPropertyBlock(QuestRewardAlterHide.GAIN_TYPE, "Hide status", FieldType.StringSelector, GainType.values()));
				break;
			case MinorCharacter:
				list.add(new QuestPropertyBlock(QuestRewardMinorCharacter.MINOR_CHARACTER, "Minor character ", FieldType.SmartTextLine, quest.getMinorCharacters().toArray()));
				list.add(new QuestPropertyBlock(QuestRewardMinorCharacter.GAIN_TYPE, "Gain or lose", FieldType.StringSelector, GainType.values()));
				break;
			case ScareMonsters:
				break;
			case MarkDenizen:
				list.add(new QuestPropertyBlock(QuestRewardMarkDenizen.DENIZEN_REGEX, "Denizen name filter (regex)", FieldType.Regex, null, new String[] { "denizen" }));
				break;
			case StripInventory:
				list.add(new QuestPropertyBlock(QuestRewardStripInventory.STRIP_GOLD, "Strip Gold", FieldType.Boolean));
				break;
			case LostInventoryToLocation:
				list.add(new QuestPropertyBlock(QuestRewardLostInventoryToLocation.LOCATION, "Send inventory to", FieldType.GameObjectWrapperSelector, quest.getLocations().toArray()));
				break;
			case LostInventoryToDefault:
				break;
			case AlterBlock:
				list.add(new QuestPropertyBlock(QuestRewardAlterBlock.GAIN_TYPE, "Block status", FieldType.StringSelector, GainType.values()));
				break;
			case Journal:
				list.add(new QuestPropertyBlock(QuestRewardJournal.JOURNAL_KEY, "Journal Key (no spaces)", FieldType.NoSpacesTextLine));
				list.add(new QuestPropertyBlock(QuestRewardJournal.ENTRY_TYPE, "Entry type", FieldType.StringSelector, new String[] { QuestStepState.Pending.toString(), QuestStepState.Finished.toString(), QuestStepState.Failed.toString() }));
				list.add(new QuestPropertyBlock(QuestRewardJournal.TEXT, "Text", FieldType.TextLine));
				break;
			case PathsPassages:
				list.add(new QuestPropertyBlock(QuestRewardPathsPassages.DISCOVERY_TYPE, "Road type to discover", FieldType.StringSelector, RoadDiscoveryType.values()));
				list.add(new QuestPropertyBlock(QuestRewardPathsPassages.DISCOVERY_SCOPE, "Scope of discovery", FieldType.StringSelector, MapScopeType.values()));
				break;
			case ChooseNextStep:
				list.add(new QuestPropertyBlock(QuestRewardChooseNextStep.TEXT, "Text", FieldType.TextLine));
				break;
			case TreasureFromSite:
				list.add(new QuestPropertyBlock(QuestRewardTreasureFromSite.SITE_REGEX, "Site RegEx", FieldType.Regex, null, new String[] { "treasure_location,!treasure_within_treasure,!cannot_move","visitor=scholar","dwelling,!native" }));
				list.add(new QuestPropertyBlock(QuestRewardTreasureFromSite.DRAW_TYPE, "Draw Type", FieldType.StringSelector, DrawType.values()));
				break;
			case SpellFromSite:
				list.add(new QuestPropertyBlock(QuestRewardTreasureFromSite.SITE_REGEX, "Site RegEx", FieldType.Regex, null, new String[] { "spell_site","visitor,!name=Scholar","artifact","book,magic" }));
				list.add(new QuestPropertyBlock(QuestRewardTreasureFromSite.DRAW_TYPE, "Draw Type", FieldType.StringSelector, DrawType.values()));
				break;
			case ActivateQuest:
				break;
			case ResetQuest:
				break;
		}
		return list;
	}

	private KeyValuePair[] getAllCompanionKeyValues() {
		ArrayList<KeyValuePair> companions = new ArrayList<KeyValuePair>();
		for (String[] section : CompanionEditPanel.COMPANIONS) {
			boolean first = true;
			for (String name : section) {
				// skip first every time
				if (first) {
					first = false;
					continue;
				}
				String[] ret = name.split(":");
				if (ret.length == 2) {
					companions.add(new KeyValuePair(ret[0], ret[1]));
				}
				else {
					companions.add(new KeyValuePair(ret[0], "Name=" + ret[0]));
				}
			}
		}
		return companions.toArray(new KeyValuePair[0]);
	}

	private String[] getRelationshipNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add("Clearing");
		GamePool pool = new GamePool(realmSpeakData.getGameObjects());
		for (GameObject go : pool.find("native,rank=HQ")) {
			names.add(go.getThisAttribute("native"));
		}
		for (GameObject go : pool.find("visitor")) {
			names.add(go.getName());
		}
		return names.toArray(new String[0]);
	}
}