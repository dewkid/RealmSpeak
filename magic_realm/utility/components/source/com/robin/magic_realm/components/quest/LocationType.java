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

public enum LocationType {
	Any,
	Lock,
	QuestChoice,
	StepChoice,
	QuestRandom,
	StepRandom
	;
	
	public String getDescriptionPrefix() {
		switch(this) {
			case Any:
				return "Any";
			case Lock:
				return "The first";
			case QuestChoice:
				return "At quest start, player selected";
			case QuestRandom:
				return "At quest start, randomly chosen";
			case StepChoice:
				return "At start of step, player selected";
			case StepRandom:
				return "At start of step, randomly chosen";
		}
		return "ERROR - No description!!";
	}
	
	public String getDescription() {
		switch(this) {
			case Any:
				return "Any location in the list is valid at any time during the quest.";
			case Lock:
				return "The first location in the list that a requirement is completed for becomes locked.";
			case QuestChoice:
				return "Player must pick a location from the list at the start of the quest.";
			case QuestRandom:
				return "A location is chosen at random from the list at the start of the quest.";
			case StepChoice:
				return "Player must pick a location from the list at the start of the first step that references it.";
			case StepRandom:
				return "A location is chosen at random from the list at the start of the first step that references it.";
		}
		return "ERROR - No description!!";
	}	
}