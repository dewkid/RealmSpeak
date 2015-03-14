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
package com.robin.magic_realm.components.attribute;

import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class DevelopmentProgress {
	
	private HostPrefWrapper hostPrefs;
	private CharacterWrapper character;
	
	private int baseVps;		// This is the bottom of the current level.  For example, Level 1, this is zero, Level 2, this is 3, Level 3, this is 9, and so on.
	private int highestVps;
	private int currentVps;
	private int baseStage;
	private int currentStage;
	private int vpsPerStage;	// This is equal to the current level.  For example, Level 1, is 1/stage, Level 2, is 2/stage, and so on.
	
	private int vpsToNextStage;
	private int vpsToNextLevel;
	
	public static DevelopmentProgress createDevelopmentProgress(HostPrefWrapper hostPrefs,CharacterWrapper character) {
		boolean restrictToAssigned = !hostPrefs.getRequiredVPsOff() && !hostPrefs.hasPref(Constants.HOUSE3_NO_RESTRICT_VPS_FOR_DEV);
		int evps = character.getTotalEarnedVps(restrictToAssigned,hostPrefs.hasPref(Constants.EXP_DEV_EXCLUDE_SW));
		int maxEvps = character.getHighestEarnedVps();
		if (evps>maxEvps) {
			character.setHighestEarnedVps(evps);
			maxEvps = evps;
		}
		return new DevelopmentProgress(hostPrefs,character,character.getStartingStage(),evps,maxEvps);
	}
	
	private DevelopmentProgress(HostPrefWrapper hostPrefs,CharacterWrapper character,int startingStage,int current,int highest) {
		this.hostPrefs = hostPrefs;
		this.character = character;
		updateVps(startingStage,current,highest);
	}
	public void updateVps(int startingStage,int current,int highest) {
		if (current>highest) {
			throw new IllegalArgumentException("current cannot be greater than highest!");
		}
		currentVps = current;
		highestVps = highest;
		
		// calculate baseVps, vpsPerStage, and currentStage based on highest
		boolean noRamp = hostPrefs.hasPref(Constants.HOUSE3_NO_VP_DEVELOPMENT_RAMP);
		int vps = 0;
		int count = 0;
		int stage = startingStage;
		int level = startingStage / Constants.STAGES_PER_LEVEL;
        int startingLevel = level;
		while(vps<highest) {
			vps++;
			count++;
			if (count==(noRamp?1:level)) {
				count=0;
				stage++;
				if (stage%Constants.STAGES_PER_LEVEL==0) {
					level++;
					baseVps = vps;
					baseStage = stage;
				}
			}
		}
		currentStage = stage;
		vpsPerStage = noRamp ? 1 : level;
		
		vpsToNextStage = vpsPerStage - count;
		
        // Special case: until the character gains a level,
        // vpsToNextLevel must be adjusted downwards to account
        // for any bonus chits selected during character creation.
        int startStageAdjust = 0;
        if (level == startingLevel)
            startStageAdjust = (startingStage % Constants.STAGES_PER_LEVEL) * startingLevel;
        vpsToNextLevel = (baseVps + vpsPerStage * Constants.STAGES_PER_LEVEL) - currentVps - startStageAdjust;
	}
	public void updateStage() {
		int stage = character.getCharacterStage();
		
		if (currentStage>stage) {
			int maxStage = hostPrefs.getMaximumCharacterLevel()*Constants.STAGES_PER_LEVEL;
		
			if (currentStage>maxStage) {
				currentStage = maxStage;
			}
			
			if (currentStage>stage) {
				character.setCharacterStage(currentStage);
			}
		}
	}
	public int getBaseStage() {
		return baseStage;
	}
	public int getBaseVps() {
		return baseVps;
	}
	public int getCurrentStage() {
		return currentStage;
	}
	public int getCurrentVps() {
		return currentVps;
	}
	public int getHighestVps() {
		return highestVps;
	}
	public int getVpsPerStage() {
		return vpsPerStage;
	}
	public int getVpsToNextStage() {
		return vpsToNextStage;
	}
	public int getVpsToNextLevel() {
		return vpsToNextLevel;
	}
}