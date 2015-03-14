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
import java.util.Collections;

import javax.swing.JFrame;

import com.robin.game.objects.*;
import com.robin.magic_realm.RealmQuestBuilder.QuestPropertyBlock.FieldType;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.quest.requirement.*;

public class QuestRequirementEditor extends QuestBlockEditor {

	private Quest quest;
	private QuestRequirement requirement;

	public QuestRequirementEditor(JFrame parent, GameData realmSpeakData, Quest quest, QuestRequirement requirement) {
		super(parent, realmSpeakData, requirement);
		this.quest = quest;
		this.requirement = requirement;
		setLocationRelativeTo(parent);
		read();
	}

	public String getEditorTitle() {
		return "Quest Requirement: " + requirement.getRequirementType();
	}

	public boolean getCanceledEdit() {
		return canceledEdit;
	}

	protected ArrayList<QuestPropertyBlock> createPropertyBlocks() {
		ArrayList<QuestPropertyBlock> list = new ArrayList<QuestPropertyBlock>();
		list.add(new QuestPropertyBlock(QuestRequirement.NOT,"NOT",FieldType.Boolean));
		switch (requirement.getRequirementType()) {
			case Attribute:
				list.add(new QuestPropertyBlock(QuestRequirementAttribute.ATTRIBUTE_TYPE, "Which attribute to measure", FieldType.StringSelector, AttributeType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementAttribute.TARGET_VALUE_TYPE, "Only count points gained during the", FieldType.StringSelector, TargetValueType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementAttribute.VALUE, "How much should it be", FieldType.Number));
				list.add(new QuestPropertyBlock(QuestRequirementAttribute.REGEX_FILTER, "Filter points to what things (regex)", FieldType.Regex, null, new String[] { "item", "spell", "denizen" }));
				list.add(new QuestPropertyBlock(QuestRequirementAttribute.AUTO_JOURNAL, "Auto Journal Entry", FieldType.Boolean));
				break;
			case OccupyLocation:
				list.add(new QuestPropertyBlock(QuestRequirementLocation.LOCATION, "Quest Location", FieldType.GameObjectWrapperSelector, quest.getLocations().toArray()));
				break;
			case Loot:
				list.add(new QuestPropertyBlock(QuestRequirementLoot.TREASURE_TYPE, "Type of Loot to acquire", FieldType.StringSelector, TreasureType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementLoot.REGEX_FILTER, "Loot name filter (regex)", FieldType.Regex, null, new String[] { "item","treasure_within_treasure" }));
				break;
			case Kill:
				list.add(new QuestPropertyBlock(QuestRequirementKill.REGEX_FILTER, "Denizen name filter (regex)", FieldType.Regex, null, new String[] { "denizen" }));
				list.add(new QuestPropertyBlock(QuestRequirementKill.VALUE, "How many (" + QuestConstants.ALL_VALUE + " means ALL)", FieldType.Number));
				list.add(new QuestPropertyBlock(QuestRequirementKill.STEP_ONLY_KILLS, "Only count kills for this step", FieldType.Boolean));
				list.add(new QuestPropertyBlock(QuestRequirementKill.REQUIRE_MARK, "Mark is required", FieldType.Boolean));
				break;
			case TimePassed:
				list.add(new QuestPropertyBlock(QuestRequirementTimePassed.VALUE, "How many days", FieldType.Number));
				break;
			case GamePhase:
				list.add(new QuestPropertyBlock(QuestRequirementGamePhase.GAME_PHASE_TYPE, "Only at", FieldType.StringSelector, GamePhaseType.values()));
				break;
			case Active:
				break;
			case SearchResult:
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.REQ_TABLENAME, "Search table", FieldType.StringSelector, SearchTableType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.LOCATION, "Location of search", FieldType.GameObjectWrapperSelector, getOptionalQuestLocationArray().toArray()));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.TARGET_LOC, "Target of search", FieldType.GameObjectWrapperSelector, getOptionalQuestLocationArray().toArray()));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.TARGET_REGEX, "OR", FieldType.Regex, null, new String[] { "ts_section,!dwelling,!summon,!guild" }));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.RESULT1, "Search result", FieldType.StringSelector, SearchResultType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.RESULT2, "OR", FieldType.StringSelector, SearchResultType.optionalValues()));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.RESULT3, "OR", FieldType.StringSelector, SearchResultType.optionalValues()));
				list.add(new QuestPropertyBlock(QuestRequirementSearchResult.REQUIRES_GAIN, "Require search effect", FieldType.Boolean));
				break;
			case Inventory:
				list.add(new QuestPropertyBlock(QuestRequirementInventory.TREASURE_TYPE, "Type of inventory", FieldType.StringSelector, TreasureType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementInventory.REGEX_FILTER, "Inventory name filter (regex)", FieldType.Regex, null, new String[] { "item","treasure_within_treasure" }));
				list.add(new QuestPropertyBlock(QuestRequirementInventory.NUMBER, "How many in inventory?", FieldType.Number));
				list.add(new QuestPropertyBlock(QuestRequirementInventory.ITEM_ACTIVE, "Require activated?", FieldType.Boolean));
				break;
			case MissionCampaign:
				list.add(new QuestPropertyBlock(QuestRequirementMissionCampaign.ACTION_TYPE, "Mission/Campaign action", FieldType.StringSelector, CharacterActionType.mcValues()));
				list.add(new QuestPropertyBlock(QuestRequirementMissionCampaign.DISABLE_ON_PICKUP, "Disable on Pickup", FieldType.Boolean));
				list.add(new QuestPropertyBlock(QuestRequirementMissionCampaign.REGEX_FILTER, "Mission/Campaign filter (regex)", FieldType.Regex, null, new String[] { "mission","campaign" }));
				break;
			case Trade:
				list.add(new QuestPropertyBlock(QuestRequirementTrade.TRADE_TYPE, "Buy or Sell", FieldType.StringSelector,  TradeType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementTrade.TRADE_ITEM_REGEX, "Item Traded", FieldType.Regex, null, new String[] { "item" }));
				list.add(new QuestPropertyBlock(QuestRequirementTrade.TRADE_WITH_REGEX, "Trade With", FieldType.Regex, null, new String[] { "native,rank=HQ","visitor" }));
				break;
			case Discovery:
				list.add(new QuestPropertyBlock(QuestRequirementDiscovery.DISCOVERY_KEY, "Discovery", FieldType.StringSelector, getDiscoveryStrings().toArray()));
				break;
			case NoDenizens:
				list.add(new QuestPropertyBlock(QuestRequirementNoDenizens.NO_MONSTERS, "No Monsters", FieldType.Boolean));
				list.add(new QuestPropertyBlock(QuestRequirementNoDenizens.NO_NATIVES, "No Natives", FieldType.Boolean));
				list.add(new QuestPropertyBlock(QuestRequirementNoDenizens.TILE_WIDE, "All Tile Clearings", FieldType.Boolean));
				break;
			case LearnAwaken:
				list.add(new QuestPropertyBlock(QuestRequirementLearnAwaken.REGEX_FILTER, "Spell filter (regex)", FieldType.Regex, null, new String[] { "spell,learnable" }));
				list.add(new QuestPropertyBlock(QuestRequirementLearnAwaken.MUST_LEARN, "Must Learn Spell", FieldType.Boolean));
				break;
			case MinorCharacter:
				list.add(new QuestPropertyBlock(QuestRequirementMinorCharacter.MINOR_CHARACTER, "Minor character ", FieldType.SmartTextLine, quest.getMinorCharacters().toArray()));
				break;
			case Path:
				list.add(new QuestPropertyBlock(QuestRequirementPath.PATH, "Specific Path (like \"CV1 CV3 CV6 CV4 CV5\")", FieldType.SmartTextArea, getAllClearingCodes()));
				list.add(new QuestPropertyBlock(QuestRequirementPath.TIME_RESTRICTION, "Only count moves made during the", FieldType.StringSelector, TargetValueType.values()));
				list.add(new QuestPropertyBlock(QuestRequirementPath.CHECK_REVERSE, "Either direction", FieldType.Boolean));
				list.add(new QuestPropertyBlock(QuestRequirementPath.ALLOW_TRANSPORT, "Allow teleport", FieldType.Boolean));
				break;
			case ColorMagic:
				list.add(new QuestPropertyBlock(QuestRequirementColorMagic.COLOR_KEY, "In the presence of color magic", FieldType.StringSelector, new String[]{"White","Grey","Gold","Purple","Black"}));
				break;
		}
		return list;
	}
	
	private String[] getAllClearingCodes() {
		ArrayList<String> list = new ArrayList<String>();
		GamePool pool = new GamePool(realmSpeakData.getGameObjects());
		for(GameObject go:pool.find("tile")) {
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
			String code = tile.getTileCode();
			for (ClearingDetail clearing:tile.getClearings()) {
				list.add(code+clearing.getNumString());
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	private ArrayList<String> getDiscoveryStrings() {
		ArrayList<String> list = new ArrayList<String>();
		
		// Build the discovery lists
		GamePool pool = new GamePool(realmSpeakData.getGameObjects());
		for(GameObject go:pool.find("treasure_location,discovery")) {
			list.add(go.getName());
		}
		for(GameObject go:pool.find("red_special")) {
			list.add(go.getName());
		}
		Collections.sort(list);
		
		ArrayList<String> sublist = new ArrayList<String>();
		for(GameObject go:pool.find("tile")) {
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
			for(PathDetail path:tile.getHiddenPaths()) {
				sublist.add(path.getFullPathKey());
			}
			for(PathDetail path:tile.getSecretPassages()) {
				sublist.add(path.getFullPathKey());
			}
		}
		Collections.sort(sublist);
		list.addAll(sublist);
		
		return list;
	}

	private ArrayList<QuestLocation> getOptionalQuestLocationArray() {
		ArrayList<QuestLocation> list = new ArrayList<QuestLocation>();
		ArrayList<QuestLocation> locations = quest.getLocations();
		if (locations != null) {
			list.addAll(locations);
		}
		list.add(0, null);
		return list;
	}
}