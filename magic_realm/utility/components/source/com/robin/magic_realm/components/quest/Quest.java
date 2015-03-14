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

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.*;

import com.robin.game.objects.*;
import com.robin.general.swing.IconGroup;
import com.robin.magic_realm.components.QuestCardComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.requirement.*;
import com.robin.magic_realm.components.quest.reward.QuestReward.RewardType;
import com.robin.magic_realm.components.quest.rule.QuestRule;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.GameVariant;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class Quest extends GameObjectWrapper {
	private static Logger logger = Logger.getLogger(Quest.class.getName());

	public static final String GAME_DATA_NAME = "QuestBuilder";

	public static final String QUEST_STEP = "quest_step";
	public static final String QUEST_LOCATION = "quest_location";
	public static final String QUEST_MINOR_CHARS = "quest_minor_chars";
	public static final String QUEST_ACTION = "quest_action";
	public static final String QUEST_REQUIREMENT = "quest_requirement";
	public static final String QUEST_REWARD = "quest_reward";

	public static final String QUEST_BLOCK = "qb";
	private static final String DESCRIPTION = "_d";
	public static final String STATE = "_q_state";
	private static final String LOST_INVENTORY_RULE = "_li";
	private static final String LOST_INVENTORY_LOCATION = "_lil";
	private static final String REQ_RULES = "_rr";
	private static final String JOURNAL_KEYS = "_jk";
	public static final String QUEST_OWNER = "_qo";
	public static final String QUEST_UNIQUE_ID = "unique_id";
	
	public static final String QUEST_FINISHED_STEP_COUNT = "_fsc";
	
	public static Quest currentQuest;

	// QuestAvailability - Which Guild? Which characters?

	private ArrayList<QuestMinorCharacter> minorCharacters;
	private ArrayList<QuestLocation> locations;
	private ArrayList<QuestStep> steps;
	private ArrayList<QuestRule> questRules;
	
	public String filepath; // This is just here so that the builder can save a quest it just loaded for viewDeck() - not guaranteed!
	
	public Quest(GameObject go) {
		super(go);
		minorCharacters = new ArrayList<QuestMinorCharacter>();
		locations = new ArrayList<QuestLocation>();
		steps = new ArrayList<QuestStep>();
		questRules = new ArrayList<QuestRule>();

		for (Iterator i = go.getHold().iterator(); i.hasNext();) {
			GameObject held = (GameObject) i.next();
			if (held.hasThisAttribute(Quest.QUEST_STEP)) {
				steps.add(new QuestStep(held));
			}
			else if (held.hasThisAttribute(Quest.QUEST_LOCATION)) {
				locations.add(new QuestLocation(held));
			}
			else if (held.hasThisAttribute(Quest.QUEST_MINOR_CHARS)) {
				minorCharacters.add(new QuestMinorCharacter(held));
			}
		}
		Collections.sort(steps, new Comparator<QuestStep>() {
			public int compare(QuestStep q1, QuestStep q2) {
				return q1.getId() - q2.getId();
			}
		});
	}

	public void autoRepair() {
		// Repair broken steps
		for (QuestStep step : steps) {
			ArrayList<QuestStep> req = filterMissingSteps(step.getRequiredSteps());
			step.clearRequiredSteps();
			for (QuestStep rs : req) {
				step.addRequiredStep(rs);
			}
			ArrayList<QuestStep> fail = filterMissingSteps(step.getFailSteps());
			step.clearFailSteps();
			for (QuestStep rs : fail) {
				step.addFailStep(rs);
			}
			ArrayList<QuestStep> pre = filterMissingSteps(step.getPreemptedSteps());
			step.clearPreemptedSteps();
			for (QuestStep rs : pre) {
				step.addPreemptedStep(rs);
			}
		}

		// Remove unused objects (but only when quest is in a gamedata by itself)
		if (!getGameData().getGameName().equals(GAME_DATA_NAME))
			return;
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		for (Iterator i = getGameData().getGameObjects().iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			if (go.getId() > 0 && go.getHeldBy() == null) { // Not the quest, and not held by anything... get rid of it!!
				toRemove.add(go);
			}
		}
		for (GameObject go : toRemove) {
			getGameData().removeObject(go);
		}
		
		updateActivatePossible();
	}
	
	public boolean isActivateable() {
		return getBoolean(QuestConstants.ACTIVATEABLE);
	}
	
	public void updateActivatePossible() {
		boolean foundActive = false;
		for(QuestStep step:getSteps()) {
			for(QuestRequirement req:step.getRequirements()) {
				if (req instanceof QuestRequirementActive) {
					foundActive = true;
					break;
				}
			}
			if (foundActive) break;
		}
		setBoolean(QuestConstants.ACTIVATEABLE,foundActive);
	}

	private ArrayList<QuestStep> filterMissingSteps(ArrayList ids) {
		ArrayList<QuestStep> found = new ArrayList<QuestStep>();
		if (ids != null) {
			for (Iterator i = ids.iterator(); i.hasNext();) {
				String id = (String) i.next();
				for (QuestStep step : steps) {
					if (step.getGameObject().getStringId().equals(id)) {
						found.add(step);
						break;
					}
				}
			}
		}
		return found;
	}

	private void setOwner(CharacterWrapper owner) {
		setString(QUEST_OWNER, owner.getGameObject().getStringId());
	}

	public CharacterWrapper getOwner() {
		String id = getString(QUEST_OWNER);
		if (id == null)
			return null; // Original template card will never have an owner id
		GameObject owner = getGameData().getGameObject(Long.valueOf(id));
		return new CharacterWrapper(owner);
	}

	private void removeFromOwner() {
		CharacterWrapper character = getOwner();
		if (character != null) {
			character.removeQuest(this);
		}
	}

	public String toString() {
		return getName();
	}

	public boolean isAllPlay() {
		return getBoolean(QuestConstants.QTR_ALL_PLAY);
	}
	
	public void clearAllPlay() {
		clear(QuestConstants.QTR_ALL_PLAY);
	}
	
	public boolean isTesting() {
		return getBoolean(QuestConstants.FLAG_TESTING);
	}
	
	public boolean isBroken() {
		return getBoolean(QuestConstants.FLAG_BROKEN);
	}
	
	public boolean isSecretQuest() {
		return getBoolean(QuestConstants.QTR_SECRET_QUEST);
	}

	public boolean isDiscardable() {
		return !isAllPlay() && getState() == QuestState.Assigned;
	}

	public boolean usesMinorCharacter(QuestMinorCharacter mc) {
		for (QuestStep step : steps) {
			if (step.usesMinorCharacter(mc)) {
				return true;
			}
		}

		return false;
	}

	public boolean usesLocationTag(String tag) {
		String desc = getDescription();
		if (desc != null && desc.contains(tag)) {
			return true;
		}

		for (QuestStep step : steps) {
			if (step.usesLocationTag(tag)) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<String> getLocationTags() {
		ArrayList<String> list = new ArrayList<String>();
		for (QuestLocation loc : getLocations()) {
			list.add(loc.getTagName());
		}
		return list;
	}

	public ArrayList<QuestMinorCharacter> getMinorCharacters() {
		return minorCharacters;
	}

	public ArrayList<QuestLocation> getLocations() {
		return locations;
	}

	public ArrayList<QuestStep> getSteps() {
		return steps;
	}

	public ArrayList<QuestRule> getQuestRules() {
		return questRules;
	}

	public void init() {
		getGameObject().setThisAttribute(Constants.QUEST);
	}

	public String getBlockName() {
		return QUEST_BLOCK;
	}

	public void setDescription(String description) {
		setString(DESCRIPTION, description);
	}

	public String getDescription() {
		return getString(DESCRIPTION);
	}

	public void clearStates() {
		clear(STATE);
		for (QuestState state : QuestState.values()) {
			clear(state.toString());
		}
		for (QuestStep step : steps) {
			step.clearStates();
		}
	}
	
	private void activateRequirements(CharacterWrapper character) {
		for(QuestStep step:steps) {
			step.activateRequirements(character);
		}
	}

	public void setState(QuestState state, String dayKey, CharacterWrapper character) {
		setString(STATE, state.toString());
		setString(state.toString(), dayKey);
		if (state == QuestState.Active) {
			activateRequirements(character);
		}
		if (state == QuestState.Complete) revertAllPlay(dayKey,character);
	}
	
	public void revertAllPlay(String dayKey, CharacterWrapper character) {
		if (!isAllPlay()) return;
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(getGameData());
		if (hostPrefs.hasPref(Constants.QST_QUEST_CARDS)) {
			// Get rid of all the clones of this quest by removing from the owning character
			for (GameObject go : findClones(getGameData().getGameObjects())) {
				Quest clone = new Quest(go);
				clone.setState(QuestState.Failed, dayKey, character);
				clone.removeFromOwner();
			}
		}
	}
	
	public int getUniqueId() {
		return getInt(Quest.QUEST_UNIQUE_ID);
	}

	public ArrayList<GameObject> findClones(ArrayList<GameObject> objects) {
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		GamePool pool = new GamePool(objects);
		for (GameObject go : pool.find(Quest.QUEST_UNIQUE_ID + "=" + getInt(Quest.QUEST_UNIQUE_ID))) {
			if (go.equals(getGameObject()))
				continue; // not THIS quest of course!
			list.add(go);
		}
		return list;
	}

	public QuestState getState() {
		String val = getString(STATE);
		return val == null ? QuestState.New : QuestState.valueOf(val);
	}

	/**
	 * Happens in-game ONLY (never during quest builder). Defines the rule to be
	 * applied when the character loses inventory.
	 */
	public void setLostInventoryRule(RewardType rule) {
		if (rule != RewardType.LostInventoryToLocation && rule != RewardType.LostInventoryToDefault) {
			throw new IllegalArgumentException("Illegal lost inventory rule!");
		}
		setString(LOST_INVENTORY_RULE, rule.toString());
	}

	public RewardType getLostInventoryRule() {
		String val = getString(LOST_INVENTORY_RULE);
		if (val == null)
			return RewardType.LostInventoryToDefault;
		return RewardType.valueOf(val);
	}

	/**
	 * Happens in-game ONLY (never during quest builder). Defines the location
	 * to be used when the character loses inventory.
	 */
	public void setLostInventoryLocation(QuestLocation location) {
		setString(LOST_INVENTORY_LOCATION, location.getGameObject().getStringId());
	}

	public QuestLocation getLostInventoryLocation() {
		String id = getString(LOST_INVENTORY_LOCATION);
		if (id != null) {
			GameObject go = getGameData().getGameObject(Long.valueOf(id));
			if (go != null) {
				return new QuestLocation(go);
			}
		}
		return null;
	}

	public void addRequiredRuleKey(String ruleKey) {
		addListItem(REQ_RULES, ruleKey);
	}

	public void clearRequiredRuleKeys() {
		clear(REQ_RULES);
	}

	public ArrayList getRequiredRuleKeys() {
		return getList(REQ_RULES);
	}

	public QuestMinorCharacter createMinorCharacter() {
		QuestMinorCharacter mc = new QuestMinorCharacter(getGameData().createNewObject());
		mc.init();
		int num = 1;
		while (true) {
			String testName = "MinorChar" + (num++);
			for (QuestMinorCharacter test : minorCharacters) {
				if (test.getName().equals(testName)) {
					testName = null;
					break;
				}
			}
			if (testName != null) {
				mc.setName(testName);
				break;
			}
		}
		mc.getGameObject().setThisAttribute(Constants.ICON_FOLDER, "traveler");
		mc.getGameObject().setThisAttribute(Constants.ICON_TYPE, "t1"); // TODO Make this configurable

		getGameObject().add(mc.getGameObject());
		minorCharacters.add(mc);
		return mc;
	}

	public void deleteMinorCharacter(QuestMinorCharacter mc) {
		minorCharacters.remove(mc);
		mc.getGameObject().delete();
	}

	public QuestLocation createQuestLocation() {
		QuestLocation ql = new QuestLocation(getGameData().createNewObject());
		ql.init();
		int num = 1;
		while (true) {
			String testName = "Location" + (num++);
			for (QuestLocation test : locations) {
				if (test.getName().equals(testName)) {
					testName = null;
					break;
				}
			}
			if (testName != null) {
				ql.setName(testName);
				break;
			}
		}
		ql.setLocationType(LocationType.Any);
		getGameObject().add(ql.getGameObject());
		locations.add(ql);
		return ql;
	}

	public void deleteStepAt(int index) {
		QuestStep step = steps.get(index);
		deleteQuestStep(step);
		renumberSteps();
	}

	public void deleteQuestLocation(QuestLocation location) {
		locations.remove(location);
		location.getGameObject().delete();
	}

	public QuestStep createQuestStep(boolean autoConnect) {
		QuestStep step = new QuestStep(getGameData().createNewObject());
		step.init();
		getGameObject().add(step.getGameObject());
		if (autoConnect) {
			QuestStep previousStep = steps.size() > 0 ? steps.get(steps.size() - 1) : null;
			if (previousStep != null) {
				step.addRequiredStep(previousStep);
			}
		}
		steps.add(step);
		step.setId(steps.size());
		step.setName("Quest Step " + step.getId());
		return step;
	}

	public void moveStep(int index, int direction) {
		int newIndex = index + direction;
		if (newIndex < 0 || newIndex >= steps.size())
			return;
		QuestStep moving = steps.remove(index);
		if (newIndex == steps.size()) {
			steps.add(moving);
		}
		else {
			steps.add(newIndex, moving);
		}
		renumberSteps();
	}

	private void renumberSteps() {
		int n = 1;
		for (QuestStep step : steps) {
			step.setId(n++);
		}
	}

	private void deleteQuestStep(QuestStep step) {
		steps.remove(step);
		step.getGameObject().delete();
	}

	public boolean isValid() {
		return steps.size() > 0; // TODO Need some other logic to help decide if a quest is valid...
	}

	public boolean canChooseQuest(CharacterWrapper character, HostPrefWrapper hostPrefs) {
		if (!verifyCharacterForQuest(character)) return false;
		if (!verifyBoardSize(hostPrefs)) return false;
		if (!verifyGameVariant(hostPrefs)) return false;
		
		return true;
	}
	private boolean verifyCharacterForQuest(CharacterWrapper character) {
		// Specific name
		String specificChar = getString(QuestConstants.CHARACTER_SPEC_REGEX);
		if (specificChar!=null && specificChar.trim().length()>0) {
			Pattern pattern = Pattern.compile(specificChar);
			if (!pattern.matcher(character.getName()).find()) return false;
		}
		
		// Gender
		String pronoun = character.getGameObject().getThisAttribute("pronoun");
		boolean male = "he".equals(pronoun);
		boolean forMale = getBoolean(QuestConstants.CHARACTER_MALE);
		boolean forFemale = getBoolean(QuestConstants.CHARACTER_FEMALE);
		boolean forBothGender = (forMale && forFemale) || (!forMale && !forFemale);
		if (!forBothGender && ((!forMale && male) || (!forFemale && !male))) return false;
		
		// Class
		boolean fighter = character.getGameObject().hasThisAttribute("fighter");
		boolean forFighter = getBoolean(QuestConstants.CHARACTER_FIGHTER);
		boolean forMagic = getBoolean(QuestConstants.CHARACTER_MAGIC);
		boolean forBothClass = (forFighter && forMagic) || (!forFighter && !forMagic);
		if (!forBothClass && ((!forFighter && fighter) || (!forMagic && !fighter))) return false;
		
		return true;
	}
	
	private boolean verifyBoardSize(HostPrefWrapper hostPrefs) {
		boolean forSingle = getBoolean(QuestConstants.SINGLE_BOARD);
		boolean forDouble = getBoolean(QuestConstants.DOUBLE_BOARD);
		boolean forTriple = getBoolean(QuestConstants.TRIPLE_BOARD);
		boolean forAny = (forSingle && forDouble && forTriple) || (!forSingle && !forDouble && !forTriple);
		if (forAny) return true;
		
		int num = hostPrefs.getMultiBoardCount();
		if ((num==1 && !forSingle) || (num==2 && !forDouble) || (num==3 && !forTriple)) return false;
		
		return true;
	}
	
	private boolean verifyGameVariant(HostPrefWrapper hostPrefs) {
		boolean forOriginal = getBoolean(QuestConstants.VARIANT_ORIGINAL);
		boolean forPruitts = getBoolean(QuestConstants.VARIANT_PRUITTS);
		boolean forExpansion = getBoolean(QuestConstants.VARIANT_EXP1);
		boolean forAny = (forOriginal && forPruitts && forExpansion) || (!forOriginal && !forPruitts && !forExpansion);
		if (forAny) return true;
		
		String keyVals = hostPrefs.getGameKeyVals();
		boolean original = GameVariant.ORIGINAL_GAME_VARIANT.getKeyVals().equals(keyVals);
		boolean pruitts = GameVariant.PRUITTS_GAME_VARIANT.getKeyVals().equals(keyVals);
		boolean expansion = GameVariant.EXP1_GAME_VARIANT.getKeyVals().equals(keyVals);
		if ((!forOriginal && original) || (!forPruitts && pruitts) || (!forExpansion && expansion)) return false;
		
		return true;
	}

	/**
	 * Initializes the quest for use by a character.
	 */
	public void initialize(JFrame parentFrame, CharacterWrapper character) {
		setOwner(character);
		String dayKey = character.getCurrentDayKey();
		for (QuestStep step : steps) {
			step.setState(QuestStepState.Pending, dayKey);
		}
		updateStepStates(dayKey);
		for (QuestLocation location : getLocations()) {
			location.resolveQuestStart(parentFrame, character);
		}
	}

	/**
	 * Removes all previous quest information, so that it can be used again.
	 */
	public void reset() {
		clear(QUEST_OWNER);
		clearJournalEntries();
		clearStates();
		// How to handle MinorCharacters?  Might they be with the character?  Maybe I can prevent minor characters from moving to character until quest is active (which can't then be discarded?)
	}

	private void updateStepStates(String dayKey) {
		Hashtable<String, QuestStep> lookup = new Hashtable<String, QuestStep>();
		for (QuestStep step : steps) {
			lookup.put(step.getGameObject().getStringId(), step);
		}
		for (QuestStep step : steps) {
			if (step.getState() != QuestStepState.Pending)
				continue; // only check pending steps!!

			QuestStepType stepType = step.getLogicType();

			ArrayList requiredSteps = step.getRequiredSteps();
			boolean markAsReady;
			if (stepType == QuestStepType.And) {
				// For AND, assume true, and mark false if any unfinished steps are found
				markAsReady = true;
			}
			else {
				// For OR, assume false if any required steps, and mark true if any finished steps are found
				markAsReady = requiredSteps == null || requiredSteps.isEmpty();
			}
			ArrayList failSteps = step.getFailSteps();
			if (requiredSteps != null) {
				for (Iterator i = requiredSteps.iterator(); i.hasNext();) {
					String reqId = (String) i.next();
					QuestStep requiredStep = lookup.get(reqId);
					if (requiredStep.getState() == QuestStepState.Finished) {
						if (stepType == QuestStepType.Or) {
							markAsReady = true;
							break;
						}
					}
					else {
						if (stepType == QuestStepType.And) {
							markAsReady = false;
							break;
						}
					}
				}
			}
			if (failSteps!=null && failSteps.size()>0) markAsReady=false; 
			if (markAsReady) {
				step.setState(QuestStepState.Ready, dayKey);
			}
		}
	}

	public void addJournalEntry(String journalKey, QuestStepState entryType, String text) {
		if (!hasListItem(JOURNAL_KEYS, journalKey)) {
			addListItem(JOURNAL_KEYS, journalKey);
		}
		String blockKey = JOURNAL_KEYS + journalKey;
		getGameObject().setAttribute(blockKey, "entryType", entryType.toString());
		getGameObject().setAttribute(blockKey, "text", text);
	}

	private void clearJournalEntries() {
		ArrayList list = getList(JOURNAL_KEYS);
		if (list != null) {
			for (Iterator i = list.iterator(); i.hasNext();) {
				String blockKey = JOURNAL_KEYS + (String) i.next();
				getGameObject().removeAttributeBlock(blockKey);
			}
			clear(JOURNAL_KEYS);
		}
	}

	public ArrayList<QuestJournalEntry> getJournalEntries() {
		ArrayList<QuestJournalEntry> entries = new ArrayList<QuestJournalEntry>();
		ArrayList list = getList(JOURNAL_KEYS);
		if (list != null) {
			for (Iterator i = list.iterator(); i.hasNext();) {
				String blockKey = JOURNAL_KEYS + (String) i.next();
				QuestStepState entryType = QuestStepState.valueOf(getGameObject().getAttribute(blockKey, "entryType"));
				String text = getGameObject().getAttribute(blockKey, "text");
				entries.add(new QuestJournalEntry(entryType, text));
			}
		}
		return entries;
	}

	/**
	 * @return true when rewards are given
	 */
	public boolean testRequirements(JFrame parentFrame, CharacterWrapper character, QuestRequirementParams reqParams) {
		QuestState state = getState();

		boolean canTest = state == QuestState.Active || ((isAllPlay() || isSecretQuest()) && state == QuestState.Assigned);
		if (!canTest) {
			return false;
		}

		if (reqParams.dayKey == null) {
			reqParams.dayKey = character.getCurrentDayKey();
		}
		boolean rewards = false;
		for (QuestStep step : steps) {
			QuestStepState stepState = step.getState();
			if (stepState == QuestStepState.Ready) {
				logger.fine("TESTING " + getGameObject().getName() + " step " + step.getId() + ": " + step.getGameObject().getName());
				if (step.fulfillsRequirements(parentFrame, character, reqParams)) {
					logger.fine("SUCCESS");
					logger.fine("");
					step.preemptSteps(steps, reqParams.dayKey);
					step.setState(QuestStepState.Finished, reqParams.dayKey);
					Quest.currentQuest = this; // I don't like this hack... but it makes it easy to do the special QuestRewardChooseNextStep
					QuestStep.currentStep = step;
					step.doRewards(parentFrame, character);
					Quest.currentQuest = null;
					QuestStep.currentStep = null;
					rewards = true;
				}
				else {
					// Mark any steps dependent on fail as READY
					for(QuestStep ft:step.findPendingFailTriggeredSteps(steps)) {
						ft.setState(QuestStepState.Ready,reqParams.dayKey);
					}
					
					logger.fine("FAIL");
					logger.fine("");
				}
			}
		}
		if (rewards) {
			updateStepStates(reqParams.dayKey);
			if (reqParams != null) {
				reqParams = reqParams.copy(getGameData());
				reqParams.clearTables();
			}
			testRequirements(parentFrame, character, reqParams); // yes, recursive...  Notice that tableName and dieResult are not passed on... wouldn't want to satisfy multiple quest steps at once!!
			return true;
		}
		return false;
	}

	public Quest copyQuestToGameData(GameData gameData) {
		// Duplicate all the objects in the quest
		ArrayList<GameObject> allQuestObjects = getGameObject().getAllContainedGameObjects();
		Hashtable<Long, GameObject> lookup = new Hashtable<Long, GameObject>();
		for (GameObject questGo : allQuestObjects) {
			GameObject go = gameData.createNewObject(questGo);
			lookup.put(questGo.getId(), go);
		}

		// Now make sure the holds are setup correctly
		for (GameObject questGo : allQuestObjects) {
			GameObject go = lookup.get(questGo.getId());
			for (Object obj : questGo.getHold()) {
				GameObject held = (GameObject) obj;
				go.add(lookup.get(held.getId()));
			}
		}
		Quest quest = new Quest(lookup.get(getGameObject().getId()));
		for (QuestStep step : quest.getSteps()) {
			step.updateIds(lookup);
		}
		return quest;
	}

	public static void showQuestMessage(JFrame frame,Quest quest, String message, String title) {
		showQuestMessage(frame,quest,message,title,null);
	}
	public static void showQuestMessage(JFrame frame,Quest quest, String message, String title,RealmComponent rc) {
		JTextArea area = new JTextArea();
		area.setFont(Constants.RESULT_FONT);
		area.setText(message);
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		area.setEditable(false);
		area.setOpaque(false);
		area.setSize(600, 1);
		QuestCardComponent card = (QuestCardComponent)RealmComponent.getRealmComponent(quest.getGameObject());
		ImageIcon icon = null;
		if (rc==null) {
			icon = card.getFaceUpIcon();
		}
		else {
			IconGroup group = new IconGroup(IconGroup.HORIZONTAL,5);
			group.addIcon(card.getFaceUpIcon());
			group.addIcon(rc.getFaceUpIcon());
			icon = group;
		}
		JOptionPane.showMessageDialog(frame, area, title, JOptionPane.PLAIN_MESSAGE, icon);
	}
}