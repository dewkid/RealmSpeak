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
package com.robin.magic_realm.components.utility;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.robin.magic_realm.components.attribute.TileLocation;

public class DieRule {
	private static final int RULE_TYPE_MINUS_ONE = 1;
	private static final int RULE_TYPE_ONE_DIE = 2;
	private static final int RULE_TYPE_PLUS_ONE = 3;
	private static final int RULE_TYPE_MINUS_TWO = 4;
	
	private TileLocation tl;
	private int type;
	private boolean allKeys = false;
	private ArrayList<String> keyList;
	private boolean allLocations = false;
	private ArrayList<String> locationList;
	
	public DieRule(TileLocation tl,String rule) {
		this.tl = tl;
		rule = rule.toLowerCase();
		if (rule.startsWith("-1")) {
			type = RULE_TYPE_MINUS_ONE;
		}
		else if (rule.startsWith("1d")) {
			type = RULE_TYPE_ONE_DIE;
		}
		else if (rule.startsWith("+1")) {
			type = RULE_TYPE_PLUS_ONE;
		}
		else if (rule.startsWith("-2")) {
			type = RULE_TYPE_MINUS_TWO;
		}
		else {
			throw new IllegalArgumentException("Illegal Rule: "+rule);
		}
		StringTokenizer tokens = new StringTokenizer(rule.substring(3),":");
		String keyListString = tokens.nextToken();
		if ("all".equals(keyListString)) {
			allKeys = true;
		}
		else {
			keyList = makeList(keyListString);
		}
		String locationListString = tokens.nextToken();
		if ("all".equals(locationListString)) {
			allLocations = true;
		}
		else {
			locationList = makeList(locationListString);
		}
//System.out.println("keyList = "+keyList);
//System.out.println("locationList = "+locationList);
	}
	public boolean conditionsMet(String key,ArrayList<String> chitDescList) {
		if (key.indexOf(',')>0) {
			StringTokenizer tokens = new StringTokenizer(key,",");
			while(tokens.hasMoreTokens()) {
				if (conditionsMet(tokens.nextToken(),chitDescList)) {
					return true;
				}
			}
			return false;
		}
		boolean validKey = allKeys || keyList.contains(key);
		boolean validLocation = allLocations || locationMatches(chitDescList);
		return validKey && validLocation;
	}
	private boolean locationMatches(ArrayList<String> chitDescList) {
		for (String loc:locationList) {
			if (loc.startsWith(">")) {
				// test clearing
				if (tl.hasClearing() && tl.clearing.getType().equalsIgnoreCase(loc.substring(1))) {
					return true;
				}
			}
			else if (loc.startsWith("%") && loc.endsWith("%")) {
				loc = loc.substring(1,loc.length()-1);
				for (String test:chitDescList) {
					if (test.indexOf(loc)>=0) {
						return true;
					}
				}
			}
			else if (loc.startsWith("%")) {
				loc = loc.substring(1);
				for (String test:chitDescList) {
					if (test.endsWith(loc)) {
						return true;
					}
				}
			}
			else if (loc.endsWith("%")) {
				loc = loc.substring(0,loc.length()-1);
				for (String test:chitDescList) {
					if (test.startsWith(loc)) {
						return true;
					}
				}
			}
			else if (chitDescList.contains(loc)) {
				return true;
			}
		}
		return false;
	}
	private ArrayList<String> makeList(String input) {
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(input,",");
		while(tokens.hasMoreTokens()) {
			list.add(tokens.nextToken());
		}
		return list;
	}
	public boolean isMinusOne() {
		return type==RULE_TYPE_MINUS_ONE;
	}
	public boolean isOneDie() {
		return type==RULE_TYPE_ONE_DIE;
	}
	public boolean isPlusOne() {
		return type==RULE_TYPE_PLUS_ONE;
	}
	public boolean isMinusTwo() {
		return type==RULE_TYPE_MINUS_TWO;
	}
}