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
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.Spoils;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.DayKey;

public class QuestRequirementAttribute extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementAttribute.class.getName());

	public static final String ATTRIBUTE_TYPE = "_at";
	public static final String VALUE = "_rq";
	public static final String TARGET_VALUE_TYPE = "_tvt";
	public static final String REGEX_FILTER = "_regex"; // to limit fame/notoriety gain to a particular type of monster or treasure type

	public static final String VALUE_OFFSET = "_vo";

	public QuestRequirementAttribute(GameObject go) {
		super(go);
	}

	public void activate(CharacterWrapper character) {
		TargetValueType tvt = getTargetValueType();
		if (tvt == TargetValueType.Quest) {
			initValueOffset(character);
		}
	}

	private void initValueOffset(CharacterWrapper character) {
		if (hasValueOffset())
			return; // NEVER overwrite it again
		setValueOffset(calculateCurrentPoints(character, null)); // we set daykey to null, so that no time filter is applied
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		TargetValueType tvt = getTargetValueType();
		DayKey earliest = null;
		switch (tvt) {
			case Game:
				earliest = null;
				break;
			case Quest:
				earliest = getParentStep().getQuestStartTime();
				break;
			case Step:
				earliest = getParentStep().getQuestStepStartTime();
				initValueOffset(character);
				break;
		}

		int targetValue = getRealValue(); // this might be different than value when there are other completed quest cards of the same type

		// Test the requirement here
		int preexistingPoints = getValueOffset();
		int currentPoints = calculateCurrentPoints(character, earliest);

		int grandTotal = currentPoints - preexistingPoints;

		boolean success = grandTotal >= targetValue;
		if (!success) {
			int points = targetValue - grandTotal;
			logger.fine("Still need " + (targetValue - grandTotal) + " " + getAttributeType().getDescription(points != 1) + ".");
		}

		boolean autoJournal = isAutoJournal();
		if (autoJournal) {
			if (success) {
				getParentQuest().addJournalEntry("REQ" + getGameObject().getStringId(), QuestStepState.Finished, getDescription(targetValue) + "  Done!");
			}
			else {
				StringBuilder sb = new StringBuilder(getDescription(targetValue));
				sb.append("  Still need ");
				sb.append(targetValue - grandTotal);
				sb.append(".");
				getParentQuest().addJournalEntry("REQ" + getGameObject().getStringId(), QuestStepState.Pending, sb.toString());
			}
		}

		return success;
	}

	private int calculateCurrentPoints(CharacterWrapper character, DayKey earliest) {
		AttributeType attribute = getAttributeType();
		
		boolean regexFilter = hasRegExFilter();
		
		int current = 0;
		if (regexFilter) { // inventory and kills but no recorded points (because the regex disallows them by default)
			current += getInventory(character, attribute, earliest);
			current += getKillsValue(character, attribute, earliest);
		}
		else { // inventory and recorded points, but no kills BECAUSE kills are accounted for in the recorded points, and invalid kills will be included in the offset value!
			current += (int) getRecorded(character, attribute);
			current += getInventory(character, attribute, earliest);
		}

		return current;
	}

	private double getRecorded(CharacterWrapper character, AttributeType attribute) {
		if (hasRegExFilter()) return 0; // If there is some kind of regex filter
		switch (attribute) {
			case Fame:
				return character.hasCurse(Constants.DISGUST) ? -1 : character.getFame();
			case Notoriety:
				return character.getNotoriety();
			case Gold:
				return character.hasCurse(Constants.ASHES) ? -1 : character.getGold();
			case RecordedSpells:
				return character.getRecordedSpellCount();
		}
		return 0; // GreatTreasures is never recorded
	}

	private int getInventory(CharacterWrapper character, AttributeType attribute, DayKey earliest) {
		if (attribute != AttributeType.Fame && attribute != AttributeType.Notoriety && attribute != AttributeType.GreatTreasures)
			return 0;

		String regex = getRegExFilter();
		Pattern pattern = regex == null || regex.trim().length() == 0 ? null : Pattern.compile(regex);
		int total = 0;
		for (GameObject go : character.getActiveInventory()) {
			if (pattern != null && !pattern.matcher(go.getName()).find())
				continue; // skip inventory that doesn't match regex (if used)

			if (earliest != null) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				DayKey acquisitionTime = rc.getCharacterTimestamp(character);
				if (acquisitionTime == null || acquisitionTime.before(earliest)) {
					continue; // skip inventory acquired before the earliest acceptable timestamp
				}
			}

			// If we made it here, then add the value!
			total += getInventoryValue(go, attribute);
		}
		return total;
	}

	private int getInventoryValue(GameObject go, AttributeType attribute) {
		switch (attribute) {
			case Fame:
				return go.getThisInt("fame");
			case Notoriety:
				return go.getThisInt("notoriety");
			case GreatTreasures:
				return go.hasThisAttribute("great") ? 1 : 0;
		}
		return 0; // no value for gold or recorded spells
	}

	private int getKillsValue(CharacterWrapper character, AttributeType attribute, DayKey earliest) {
		if (attribute != AttributeType.Fame && attribute != AttributeType.Notoriety)
			return 0; // TODO What about gold bounties?

		String regex = getRegExFilter();
		Pattern pattern = regex == null || regex.trim().length() == 0 ? null : Pattern.compile(regex);
		int good = 0;
		ArrayList dayKeys = character.getAllDayKeys();
		if (dayKeys == null)
			return 0;
		for (Object obj : dayKeys) {
			String dayKeyString = (String) obj;
			if (!character.areKills(dayKeyString))
				continue;

			DayKey dayKey = new DayKey(dayKeyString);
			if (earliest!=null && dayKey.before(earliest)) continue; // can't count kills before the earliest date (if any)

			ArrayList<GameObject> kills = character.getKills(dayKeyString);
			ArrayList<Spoils> killSpoils = character.getKillSpoils(dayKeyString);
			for (int i = 0; i < kills.size(); i++) {
				GameObject go = kills.get(i);
				Spoils spoils = killSpoils.get(i);
				if (pattern!=null && !pattern.matcher(go.getName()).find()) continue;

				if (attribute == AttributeType.Fame)
					good += (int) spoils.getFame();
				else if (attribute == AttributeType.Notoriety)
					good += (int) spoils.getNotoriety();
			}
		}
		return good;
	}

	/*
	 * 
	 * Game Fame/Notoriety - Need only look at recorded values and inventory
	 * matching regex. Subtract out non-matching kills. Gold - Need only look at
	 * recorded value Spells - Need only look at recorded spells
	 * 
	 * Quest Fame/Notoriety - Look at recorded values and inventory matching
	 * regex and quest timestamp. Subtract out the recorded value set during
	 * quest init. Subtract non-matching and old kills. Gold - Look at recorded
	 * values and subtract out the recorded value set during quest init. Spells
	 * - Look at recorded values and subtract out the recorded value set during
	 * quest init.
	 * 
	 * Step Fame/Notoriety - Look at recorded values and inventory matching
	 * regex and step timestamp. Subtract out the recorded value set during step
	 * init. Subtract non-matching and old kills. Gold - Look at recorded values
	 * and subtract out the recorded value set during step init. Spells - Look
	 * at recorded values and subtract out the recorded value set during step
	 * init.
	 */

	private int getRealValue() { // TODO Still don't like this as a solution...  how can the player know this is happening?
		int val = getValue();
		TargetValueType targetValueType = getTargetValueType();
		if (targetValueType == TargetValueType.Game) {
			CharacterWrapper character = getParentQuest().getOwner();
			if (character != null) {
				// Count the completed clones of this quest in the character hand and add a multiple of value to the val
				int count = 0;
				for (GameObject go : getParentQuest().findClones(character.getAllQuestObjects())) {
					Quest quest = new Quest(go);
					if (quest.getState().isFinished())
						count++;
				}
				val += (val * count);
			}
		}
		return val;
	}

	public RequirementType getRequirementType() {
		return RequirementType.Attribute;
	}

	protected String buildDescription() {
		String desc = getDescription(getValue());
		if (isAutoJournal()) {
			desc += " (Auto Journal ON)";
		}
		return desc;
	}

	private String getDescription(int realValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("Must record ");
		sb.append(realValue);
		sb.append(" ");
		sb.append(getAttributeType().getDescription(realValue != 1));
		TargetValueType tvt = getTargetValueType();
		if (tvt != TargetValueType.Game) {
			sb.append(" during the ");
			sb.append(getTargetValueType().toString());
		}
		sb.append(".");
		return sb.toString();
	}

	public AttributeType getAttributeType() {
		return AttributeType.valueOf(getString(ATTRIBUTE_TYPE));
	}

	public TargetValueType getTargetValueType() {
		return TargetValueType.valueOf(getString(TARGET_VALUE_TYPE));
	}

	public int getValue() {
		return getInt(VALUE);
	}
	
	public boolean hasRegExFilter() {
		String val = getString(REGEX_FILTER);
		return val!=null && val.trim().length()>0;
	}

	public String getRegExFilter() {
		return getString(REGEX_FILTER);
	}

	private void setValueOffset(int val) {
		setInt(VALUE_OFFSET, val);
	}

	private boolean hasValueOffset() {
		return getBoolean(VALUE_OFFSET);
	}

	private int getValueOffset() {
		return getInt(VALUE_OFFSET);
	}
}