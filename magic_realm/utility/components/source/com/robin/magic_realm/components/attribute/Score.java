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

import java.util.ArrayList;

import com.robin.game.objects.GameObject;

public class Score {
	private int recordedPoints;
	private int ownedPoints;
	private int mult;
	private int vps;
	private ArrayList<GameObject> scoringGameObjects;
	public Score(int recordedPoints,int ownedPoints,int mult,int vps,ArrayList<GameObject> scoringGameObjects) {
		this.recordedPoints = recordedPoints;
		this.ownedPoints = ownedPoints;
		this.mult = mult;
		this.vps = vps;
		this.scoringGameObjects = scoringGameObjects;
	}
	public ArrayList<GameObject> getScoringGameObjects() {
		return scoringGameObjects;
	}
	public int getRecordedPoints() {
		return recordedPoints;
	}
	public int getOwnedPoints() {
		return ownedPoints;
	}
	public int getPoints() {
		return recordedPoints+ownedPoints;
	}
	public int getScore() {
		int score = getPoints() - getRequired();
		return score<0?score*3:score;
	}
	public boolean hasPenalty() {
		return getScore()<0;
	}
	public int getMultiplier() {
		return mult;
	}
	public int getAssignedVictoryPoints() {
		return vps;
	}
	public int getRequired() {
		return mult*vps;
	}
	public int getBasicScore() {
		double val = (double)getScore()/(double)getMultiplier();
		return (new Double(Math.floor(val))).intValue();
	}
	public int getBonusScore() {
		return vps>0?(getBasicScore()*vps):0;
	}
	public int getTotalScore() {
		return getBasicScore()+getBonusScore();
	}
	public int getEarnedVictoryPoints(boolean restrictToAssigned) {
		return getEarnedVictoryPoints(restrictToAssigned,false);
	}
	public int getEarnedVictoryPoints(boolean restrictToAssigned,boolean excludeStartingWorth) {
		double p = (double)getPoints();
		if (excludeStartingWorth) {
			p -= ownedPoints;
		}
		double val = p/(double)getMultiplier();
		int earnedVps = (new Double(Math.floor(val))).intValue();
		if (restrictToAssigned) {
			if (vps>0) {
				earnedVps = Math.min(earnedVps,vps); // only get credit for the number of points you've assigned
			}
			else {
				earnedVps = 0; // only those categories you've assigned count
			}
		}
		return earnedVps;
	}
	public static void printResult(int score,int mult) {
		double val = (double)score/(double)mult;
		System.out.println("("+score+"/"+mult+")="+val);
		System.out.println("Math.floor("+score+"/"+mult+")="+new Double(Math.floor(val)).intValue());
		System.out.println("Math.ceil("+score+"/"+mult+")="+new Double(Math.ceil(val)).intValue());
		System.out.println("Math.round("+score+"/"+mult+")="+new Double(Math.round(val)).intValue());
	}
	public static void main(String[] args) {
		printResult(-1,30);
		printResult(-27,30);
		printResult(-33,30);
		printResult(27,30);
		printResult(33,30);
	}
}