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

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GameObjectWrapper;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.general.swing.IconGroup;
import com.robin.general.util.HashLists;
import com.robin.magic_realm.components.quest.requirement.*;
import com.robin.magic_realm.components.quest.requirement.QuestRequirement.RequirementType;
import com.robin.magic_realm.components.quest.reward.QuestReward;
import com.robin.magic_realm.components.quest.reward.QuestReward.RewardType;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.DayKey;

public class QuestStep extends GameObjectWrapper {
	private static Logger logger = Logger.getLogger(QuestStep.class.getName());

	private static final String ID = "_id";
	private static final String STATE = "_state";
	private static final String LOGIC_TYPE = "_type";
	private static final String DESCRIPTION = "_d";
	private static final String REQ_STEPS = "_rs";
	private static final String PREEMPT_STEPS = "_ps";
	private static final String FAIL_STEPS = "_fs";
	private static final String REQ_TYPE = "_rtype";
	
	public static QuestStep currentStep;

	private ArrayList<QuestRequirement> requirements;
	private ArrayList<QuestReward> rewards;

	public QuestStep(GameObject go) {
		super(go);
		refresh();
	}

	public String toString() {
		return "Step " + getId();
	}

	public void refresh() {
		requirements = new ArrayList<QuestRequirement>();
		rewards = new ArrayList<QuestReward>();
		for (Iterator i = getGameObject().getHold().iterator(); i.hasNext();) {
			GameObject held = (GameObject) i.next();
			if (held.hasThisAttribute(Quest.QUEST_REQUIREMENT)) {
				String val = held.getThisAttribute(Quest.QUEST_REQUIREMENT);
				if (val.equals("Location"))
					val = "OccupyLocation";
				RequirementType type = RequirementType.valueOf(val);
				requirements.add(QuestRequirement.getRequirement(type, held));
			}
			else if (held.hasThisAttribute(Quest.QUEST_REWARD)) {
				RewardType type = RewardType.valueOf(held.getThisAttribute(Quest.QUEST_REWARD));
				rewards.add(QuestReward.getReward(type, held));
			}
		}
	}

	public void init() {
		getGameObject().setThisAttribute(Quest.QUEST_STEP);
	}
	
	public String getBlockName() {
		return Quest.QUEST_BLOCK;
	}

	public boolean usesMinorCharacter(QuestMinorCharacter mc) {
		for (QuestRequirement req : requirements) {
			if (req.usesMinorCharacter(mc))
				return true;
		}
		for (QuestReward reward : rewards) {
			if (reward.usesMinorCharacter(mc))
				return true;
		}
		return false;
	}

	public boolean usesLocationTag(String tag) {
		String desc = getDescription();
		if (desc != null && desc.contains(tag)) {
			return true;
		}
		for (QuestRequirement req : requirements) {
			if (req.usesLocationTag(tag))
				return true;
		}
		for (QuestReward reward : rewards) {
			if (reward.usesLocationTag(tag))
				return true;
		}
		return false;
	}

	public void setId(int id) {
		setInt(ID, id);
	}

	public int getId() {
		return getInt(ID);
	}

	protected void clearStates() {
		clear(STATE);
		for (QuestStepState state : QuestStepState.values()) {
			clear(state.toString());
		}
		GameObject questObject = getGameObject().getHeldBy();
		questObject.removeThisAttribute(Quest.QUEST_FINISHED_STEP_COUNT);
	}
	
	public void activateRequirements(CharacterWrapper character) {
		for(QuestRequirement requirement:requirements) {
			requirement.activate(character);
		}
	}

	public void setState(QuestStepState state, String dayKey) {
		setString(STATE, state.toString());
		setString(state.toString(), dayKey);
		if (requirements.isEmpty()) return; // no requirements, then nothing significant to note
		if (state==QuestStepState.Finished) {// || state==QuestStepState.Failed) {
			String key = Quest.QUEST_FINISHED_STEP_COUNT;
			GameObject questObject = getGameObject().getHeldBy();
			questObject.setThisAttribute(key,questObject.getThisInt(key)+1);
		}
	}

	public QuestStepState getState() {
		String val = getString(STATE);
		return val == null ? QuestStepState.None : QuestStepState.valueOf(val);
	}

	public void setLogicType(QuestStepType type) {
		setString(LOGIC_TYPE, type.toString());
	}

	public QuestStepType getLogicType() {
		String val = getString(LOGIC_TYPE);
		return val == null ? QuestStepType.And : QuestStepType.valueOf(val);
	}

	public void setReqType(QuestStepType type) {
		setString(REQ_TYPE, type.toString());
	}

	public QuestStepType getReqType() {
		String val = getString(REQ_TYPE);
		return val == null ? QuestStepType.And : QuestStepType.valueOf(val);
	}

	public DayKey getQuestStartTime() {
		return getStateTime(QuestStepState.Pending);
	}

	public DayKey getQuestStepStartTime() {
		return getStateTime(QuestStepState.Ready);
	}

	public DayKey getQuestStepFinishTime() {
		return getStateTime(QuestStepState.Finished);
	}

	private DayKey getStateTime(QuestStepState state) {
		String dayKey = getString(state.toString());
		return dayKey == null ? null : new DayKey(dayKey);
	}

	public void setDescription(String description) {
		setString(DESCRIPTION, description);
	}

	public String getDescription() {
		return getString(DESCRIPTION);
	}

	public void addRequiredStep(QuestStep step) {
		addListItem(REQ_STEPS, step.getGameObject().getStringId());
	}

	public void removeRequiredStep(QuestStep step) {
		removeListItem(REQ_STEPS, step.getGameObject().getStringId());
	}

	public void clearRequiredSteps() {
		clear(REQ_STEPS);
	}

	public ArrayList getRequiredSteps() {
		return getList(REQ_STEPS);
	}

	public boolean isOrigin() {
		ArrayList requiredSteps = getRequiredSteps();
		ArrayList failSteps = getFailSteps();
		return (requiredSteps == null || requiredSteps.size() == 0) && (failSteps == null || failSteps.size() == 0);
	}

	public boolean requires(QuestStep step) {
		ArrayList requiredSteps = getRequiredSteps();
		return requiredSteps != null && requiredSteps.contains(step.getGameObject().getStringId());
	}
	
	public boolean requiresFail(QuestStep step) {
		ArrayList failSteps = getFailSteps();
		return failSteps != null && failSteps.contains(step.getGameObject().getStringId());
	}

	public void addPreemptedStep(QuestStep step) {
		addListItem(PREEMPT_STEPS, step.getGameObject().getStringId());
	}

	public void removePreemptedStep(QuestStep step) {
		removeListItem(PREEMPT_STEPS, step.getGameObject().getStringId());
	}

	public void clearPreemptedSteps() {
		clear(PREEMPT_STEPS);
	}

	public ArrayList getPreemptedSteps() {
		return getList(PREEMPT_STEPS);
	}

	public void addFailStep(QuestStep step) {
		addListItem(FAIL_STEPS, step.getGameObject().getStringId());
	}

	public void removeFailStep(QuestStep step) {
		removeListItem(FAIL_STEPS, step.getGameObject().getStringId());
	}

	public void clearFailSteps() {
		clear(FAIL_STEPS);
	}

	public ArrayList getFailSteps() {
		return getList(FAIL_STEPS);
	}

	public ArrayList<QuestRequirement> getRequirements() {
		return requirements;
	}

	public QuestRequirement createRequirement(RequirementType type) {
		QuestRequirement requirement = QuestRequirement.getRequirement(type, getGameData().createNewObject());
		requirement.init();
		getGameObject().add(requirement.getGameObject());
		requirements.add(requirement);
		return requirement;
	}

	public void deleteRequirement(QuestRequirement requirement) {
		requirements.remove(requirement);
		requirement.getGameObject().delete();
	}

	public ArrayList<QuestReward> getRewards() {
		return rewards;
	}

	public QuestReward createReward(RewardType type) {
		QuestReward reward = QuestReward.getReward(type, getGameData().createNewObject());
		reward.init();
		getGameObject().add(reward.getGameObject());
		rewards.add(reward);
		return reward;
	}

	public void deleteReward(QuestReward reward) {
		rewards.remove(reward);
		reward.getGameObject().delete();
	}

	public void updateIds(Hashtable<Long, GameObject> lookup) {
		updateIds(lookup, REQ_STEPS);
		updateIds(lookup, FAIL_STEPS);
		updateIds(lookup, PREEMPT_STEPS);
		for (QuestRequirement req : getRequirements()) {
			req.updateIds(lookup);
		}
		for (QuestReward rew : getRewards()) {
			rew.updateIds(lookup);
		}
	}

	private void updateIds(Hashtable<Long, GameObject> lookup, String key) {
		ArrayList oldIds = getList(key);
		if (oldIds == null || oldIds.size() == 0)
			return;
		clear(key);
		for (Object obj : oldIds) {
			String stringId = (String) obj;
			Long oldId = Long.valueOf(stringId);
			if (!lookup.containsKey(oldId)) {
				throw new IllegalStateException("cannot find conversion for id " + oldId + " in quest step " + getId() + " of quest " + getGameObject().getHeldBy());
			}
			addListItem(key, lookup.get(oldId).getStringId());
		}
	}

	private static String getClassName(Class c) {
		String className = c.getName();
		int firstChar;
		firstChar = className.lastIndexOf('.') + 1;
		if (firstChar > 0) {
			className = className.substring(firstChar);
		}
		return className;
	}

	public boolean fulfillsRequirements(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		ArrayList<QuestRequirement> reqs = getRequirements();
		if (reqs.isEmpty()) return true; // no requirements means auto-success
		QuestStepType type = getReqType();
		if (type == QuestStepType.And) {
			boolean resetTime = false;
			boolean ret = true;
			for (QuestRequirement req : reqs) {
				if (req.fulfillsRequirement(frame, character, reqParams)) {
					logger.fine(getClassName(req.getClass()) + " SUCCESS");
				}
				else {
					if (!(req instanceof QuestRequirementTimePassed)) {
						resetTime = true;
					}
					ret = false;
					// we continue the loop here in case there are any "Auto-Journal" requirements that need updates
				}
			}
			if (resetTime) {
				setState(QuestStepState.Ready, character.getCurrentDayKey());
			}
			return ret;
		}
		else { // OR - any requirement fulfilled is a success
			boolean oneFulfilled = false;
			for (QuestRequirement req : reqs) {
				if (req.fulfillsRequirement(frame, character, reqParams)) {
					logger.fine("Requirement " + req.getName() + " fulfilled.");
					oneFulfilled = true;
					// we continue here in case there are any "Lock" type
					// requirements!
				}
			}
			return oneFulfilled;
		}
	}

	public void preemptSteps(ArrayList<QuestStep> steps, String dayKey) {
		ArrayList preempt = getPreemptedSteps();
		if (preempt == null)
			return;
		Hashtable<String, QuestStep> lookup = new Hashtable<String, QuestStep>();
		for (QuestStep step : steps) {
			lookup.put(step.getGameObject().getStringId(), step);
		}
		for (Iterator i = preempt.iterator(); i.hasNext();) {
			String reqId = (String) i.next();
			QuestStep preemptedStep = lookup.get(reqId);
			preemptedStep.setState(QuestStepState.Failed, dayKey);
		}
	}

	public void doRewards(JFrame frame, CharacterWrapper character) {
		HashLists<String, QuestReward> rewardGroups = new HashLists<String, QuestReward>();
		for (QuestReward reward : getRewards()) {
			rewardGroups.put(reward.getRewardGroup(), reward);
		}
		if (rewardGroups.size() == 0)
			return; // No rewards. Awww, how sad.
		
		// First, complete all the "ALL REWARD" rewards.
		ArrayList<QuestReward> allRewards = rewardGroups.getList(QuestReward.ALL_REWARD_GROUP);
		if (allRewards!=null) {
			for(QuestReward reward:allRewards) {
				reward.processReward(frame,character);
			}
		}
		
		// If there are any group choice awards, get a choice (if needed) and proceed.
		String selectedRewardGroup = chooseReward(frame, rewardGroups);
		ArrayList<QuestReward> list = new ArrayList<QuestReward>();
		if (selectedRewardGroup != QuestReward.ALL_REWARD_GROUP) {
			list.addAll(rewardGroups.getList(selectedRewardGroup));
		}
		for (QuestReward reward : list) {
			reward.processReward(frame, character);
		}
	}

	private String chooseReward(JFrame frame, HashLists<String, QuestReward> rewardGroups) {
		// First see if the choice is obvious
		ArrayList<String> keys = new ArrayList<String>(rewardGroups.keySet());
		keys.remove(QuestReward.ALL_REWARD_GROUP);
		if (keys.size() == 1)
			return keys.get(0);
		if (keys.size() == 0)
			return QuestReward.ALL_REWARD_GROUP;

		// Nope! Better ask the player then
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame, null, "Select a reward group:", "Reward Chooser", false);
		keys = new ArrayList<String>(rewardGroups.keySet());
		Collections.sort(keys);
		Hashtable<String, String> reverseLookup = new Hashtable<String, String>();
		for (String key : keys) {
			if (QuestReward.ALL_REWARD_GROUP.equals(key))
				continue;
			ArrayList<QuestReward> rewards = new ArrayList<QuestReward>();
			rewards.addAll(rewardGroups.getList(key));
			StringBuilder sb = new StringBuilder();
			IconGroup icon = null;
			for (QuestReward reward : rewards) {
				if (!reward.getRewardType().isShown())
					continue;
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(reward.getDescription());
				ImageIcon rewardIcon = reward.getIcon();
				if (rewardIcon!=null) {
					if (icon==null) icon = new IconGroup(IconGroup.HORIZONTAL,0);
					icon.addIcon(rewardIcon);
				}
			}
			String object = sb.toString();
			chooser.addSelectionObject(object);
			if (icon!=null) chooser.setSelectionObjectIcon(object,icon);
			reverseLookup.put(object, key);
		}
		chooser.setVisible(true);
		String selectedString = (String) chooser.getSelectedObject();
		return reverseLookup.get(selectedString);
	}
	/**
	 * Find all the pending steps that are dependent on this step's failure
	 */
	public ArrayList<QuestStep> findPendingFailTriggeredSteps(ArrayList<QuestStep> steps) {
		String id = getGameObject().getStringId();
		ArrayList<QuestStep> ret = new ArrayList<QuestStep>();
		for(QuestStep step:steps) {
			if (step.getState()!=QuestStepState.Pending) continue;
			ArrayList list = step.getFailSteps();
			if (list!=null && list.contains(id)) ret.add(step);
		}
		return ret;
	}
}