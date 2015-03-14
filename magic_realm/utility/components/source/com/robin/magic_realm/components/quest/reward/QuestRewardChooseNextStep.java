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
package com.robin.magic_realm.components.quest.reward;

import java.util.ArrayList;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRewardChooseNextStep extends QuestReward {
	
	public static final String TEXT = "_tx";

	public QuestRewardChooseNextStep(GameObject go) {
		super(go);
	}

	@Override
	public void processReward(JFrame frame, CharacterWrapper character) {
		QuestRequirementParams params = new QuestRequirementParams();
		ArrayList<QuestStep> dependentSteps = new ArrayList<QuestStep>();
		for(QuestStep step:Quest.currentQuest.getSteps()) {
			if (step.getState()!=QuestStepState.Pending) continue;
			if (step.requires(QuestStep.currentStep)) {
				dependentSteps.add(step);
			}
		}
		if (dependentSteps.isEmpty()) return;
		RealmComponent rc = RealmComponent.getRealmComponent(Quest.currentQuest.getGameObject());
		ButtonOptionDialog dialog = new ButtonOptionDialog(frame,rc.getIcon(),getString(TEXT),"Choose",false);
		for(QuestStep step:dependentSteps) {
			if (step.fulfillsRequirements(frame,character,params)) {
				dialog.addSelectionObject(step.getName());
			}
		}
		String dayKey = character.getCurrentDayKey();
		String stepName=null;
		if (dialog.getSelectionObjectCount()>0) {
			dialog.setVisible(true);
			stepName = (String)dialog.getSelectedObject();
		}
		for(QuestStep step:dependentSteps) {
			if (!step.getName().equals(stepName)) {
				step.setState(QuestStepState.Failed,dayKey);
			}
		}
	}
	
	@Override
	public RewardType getRewardType() {
		return RewardType.ChooseNextStep;
	}

	@Override
	public String getDescription() {
		return "Choose a path from steps dependent on this step.";
	}
}