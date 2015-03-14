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
package com.robin.magic_realm.components.wrapper;

import com.robin.general.util.StringUtilities;

public class DayKey implements Comparable<DayKey> {
	private int month;
	private int day;
	public DayKey(int month,int day) {
		this.month = month;
		this.day = day;
	}
	public DayKey(String dayKeyString) {
		this.month = getMonth(dayKeyString);
		this.day = getDay(dayKeyString);
	}
	public boolean after(DayKey dayKey) {
		return compareTo(dayKey)>0;
	}
	public boolean before(DayKey dayKey) {
		return compareTo(dayKey)<0;
	}
	public boolean equals(DayKey dayKey) {
		return compareTo(dayKey)==0;
	}
	public boolean equals(Object obj) {
		if (obj instanceof DayKey) {
			return equals((DayKey)obj);
		}
		return false;
	}
	public int compareTo(DayKey dayKey) {
		int val = day - dayKey.day;
		val += (month - dayKey.month) * 28;
		return val;
	}
	public String getReadable() {
		return month+"-"+StringUtilities.zeroPaddedInt(day,2);
	}
	public String toString() {
		return getString(month,day);
	}
	public static String getString(int month,int day) {
		return "month_"+month+"_day_"+day;
	}
	public static int getMonth(String dayKey) {
		int dayIndex = dayKey.indexOf("_day_");
		return Integer.valueOf(dayKey.substring(6,dayIndex)).intValue();
	}
	public static int getDay(String dayKey) {
		int dayIndex = dayKey.indexOf("_day_");
		return Integer.valueOf(dayKey.substring(dayIndex+5)).intValue();
	}
}