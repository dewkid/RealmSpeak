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
package com.robin.game.objects;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import com.robin.general.swing.DieRoller;
import com.robin.general.swing.DieRollerLoggable;
import com.robin.general.util.RandomNumber;

public class DieRollerLog implements DieRollerLoggable {
	
	private static final String ROLL_LIST = "_rl";
	private static final String REASON_LIST = "_rs";
	
	private boolean dirty = true;
	
	private ArrayList<DieRoller> cache;
	
	private GameObject gameObject;
	
	public DieRollerLog(GameObject go) {
		this.gameObject = go;
	}
	public void addDieRoll(DieRoller roll,String reason) {
		dirty = true;
		gameObject.addThisAttributeListItem(ROLL_LIST,roll.getStringResult());
		gameObject.addThisAttributeListItem(REASON_LIST,reason==null?"":reason);
	}
	public int getTotalRolls() {
		ArrayList rolls = gameObject.getThisAttributeList(ROLL_LIST);
		return rolls==null?0:rolls.size();
	}
	public Integer[] getDieMultiples() {
		ArrayList<Integer> multiples = new ArrayList<Integer>();
		for(DieRoller roller:getDieRollers()) {
			int nod = roller.getNumberOfDice();
			if (!multiples.contains(nod)) {
				multiples.add(nod);
			}
		}
		Collections.sort(multiples);
		return multiples.toArray(new Integer[multiples.size()]);
	}
	public int getTotalRolls(int numberOfDice) {
		int count=0;
		for(DieRoller roller:getDieRollers()) {
			if (roller.getNumberOfDice()==numberOfDice) count++;
		}
		return count;
	}
	public int getTotalDiceRolled() {
		int dice = 0;
		for(DieRoller roller:getDieRollers()) {
			dice += roller.getNumberOfDice();
		}
		return dice;
	}
	public int getFrequencyOfDieResult(int result) {
		int frequency = 0;
		for(DieRoller roller:getDieRollers()) {
			frequency += roller.getDieResultCount(result);
		}
		return frequency;
	}
	public int getFrequencyOfTotal(int numberOfDice,int result,boolean includeModifier) {
		int frequency = 0;
		for(DieRoller roller:getDieRollers()) {
			if (roller.getNumberOfDice()==numberOfDice) {
				int total = roller.getTotal();
				if (!includeModifier) {
					total -= roller.getModifier();
				}
				if (total==result) {
					frequency++;
				}
			}
		}
		return frequency;
	}
	public int getFrequencyOfHighDie(int numberOfDice,int result) {
		int frequency = 0;
		for(DieRoller roller:getDieRollers()) {
			if (roller.getNumberOfDice()==numberOfDice) {
				if (roller.getHighDieResult()==result) {
					frequency++;
				}
			}
		}
		return frequency;
	}
	public ArrayList<String> getReasons() {
		return gameObject.getThisAttributeList(REASON_LIST);
	}
	public ArrayList<DieRoller> getDieRollers() {
		if (cache==null || dirty) {
			cache = new ArrayList<DieRoller>();
			ArrayList<String> rolls = gameObject.getThisAttributeList(ROLL_LIST);
			if (rolls!=null) {
				for(String roll:rolls) {
					cache.add(new DieRoller(roll));
				}
			}
			dirty = false;
		}
		return cache;
	}
	public String getStandardReport(boolean includeTotals,boolean includeHighDie) {
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(2);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("-------------------------\n");
		sb.append("-- DIE ROLL STATISTICS --\n");
		sb.append("-------------------------\n");
		sb.append("Random Number Generator:\n");
		sb.append("      ");
		sb.append(RandomNumber.getRandomNumberGenerator().toString());
		sb.append("\n");
		sb.append("-------------------------\n");
		sb.append("Total rolls: "+getTotalRolls()+"\n");
		sb.append("Total dice: "+getTotalDiceRolled()+"\n");
		sb.append("-------------------------\n");
		for (int i=1;i<=6;i++) {
			int freq = getFrequencyOfDieResult(i);
			double percent = (double)freq/(double)getTotalDiceRolled();
			sb.append(i+"s rolled: "+freq+" or "+percentFormat.format(percent)+" of total dice rolled.\n");
		}
		sb.append("-------------------------\n");
		if (includeTotals) {
			for (int nod:getDieMultiples()) {
				if (nod==1) continue;
				int totalRolls = getTotalRolls(nod);
				sb.append("For "+totalRolls+" rolls with "+nod+" dice:\n");
				for (int i=nod;i<=(nod*6);i++) {
					int freq = getFrequencyOfTotal(nod,i,false);
					double percent = (double)freq/(double)totalRolls;
					sb.append(i+"s rolled: "+freq+" or "+percentFormat.format(percent)+" of total rolls.\n");
				}
				sb.append("-------------------------\n");
			}
		}
		if (includeHighDie) {
			for (int nod:getDieMultiples()) {
				if (nod==1) continue;
				int totalRolls = getTotalRolls(nod);
				sb.append("For "+totalRolls+" rolls with "+nod+" dice:\n");
				for (int i=1;i<=6;i++) {
					int freq = getFrequencyOfHighDie(nod,i);
					double percent = (double)freq/(double)totalRolls;
					sb.append(i+" was the high die "+percentFormat.format(percent)+" of the time ("+freq+" of "+totalRolls+" rolls)\n");
				}
				sb.append("-------------------------\n");
			}
		}
		return sb.toString();
	}
	public String getAllDieRolls() {
		ArrayList<DieRoller> rollers = getDieRollers();
		ArrayList<String> reasons = getReasons();
		if (reasons==null) return "No die rolls have been recorded yet.";
		String defaultReason = "<none>";
		
		int maxReasonLength = defaultReason.length();
		for (int i=0;i<reasons.size();i++){
			maxReasonLength = Math.max(maxReasonLength,reasons.get(i).length());
		}
		maxReasonLength+=2;
		
		StringBuilder sb = new StringBuilder();
		appendField(sb,"Reason",maxReasonLength);
		sb.append("Total  High\n");
		appendField(sb,"------",maxReasonLength-2,'-');
		sb.append("  -----  -----\n");
		for (int i=0;i<rollers.size();i++){
			DieRoller roller = rollers.get(i);
			String reason = reasons.get(i);
			appendField(sb,reason.length()==0?"<none>":reason,maxReasonLength);
			appendField(sb,String.valueOf(roller.getTotal()-roller.getModifier()),7);
			appendField(sb,String.valueOf(roller.getHighDieResult()),7);
			for (int n=0;n<roller.getNumberOfDice();n++) {
				sb.append(roller.getValue(n));
				sb.append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	private void appendField(StringBuilder sb,String field,int length) {
		appendField(sb,field,length,' ');
	}
	private void appendField(StringBuilder sb,String field,int length,char c) {
		sb.append(field);
		while(length-field.length()>0) {
			sb.append(c);
			length--;
		}
	}
	
	public static void main(String[] args) {
		GameObject log = GameObject.createEmptyGameObject();
		DieRollerLog logger = new DieRollerLog(log);
		DieRoller.setDieRollerLog(logger);
		for (int i=0;i<10000;i++) {
			DieRoller roller = new DieRoller();
			roller.addWhiteDie();
			roller.addRedDie();
			roller.rollDice("double");
		}
		for (int i=0;i<100;i++) {
			DieRoller roller = new DieRoller();
			roller.addWhiteDie();
			roller.addRedDie();
			roller.addRedDie();
			roller.rollDice("triple");
		}
		System.out.println(logger.getAllDieRolls());
		System.out.println(logger.getStandardReport(true,true));
	}
}