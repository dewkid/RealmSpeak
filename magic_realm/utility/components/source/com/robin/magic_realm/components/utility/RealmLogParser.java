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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.TileLocation;

public class RealmLogParser {
	private ArrayList<String> lines;
	public RealmLogParser(String detailLog) {
		lines = StringUtilities.stringToCollection(detailLog,"\n");
	}
	public String getLogFor(int month,int day) {
//<br><b>RealmSpeak</b> - ========================================
//<br><b>RealmSpeak</b> - Month 1, Day 5
//<br><b>RealmSpeak</b> - ========================================
		return "";
	}
	public String getBattleLogFor(int month,int day) {
		return getBattleLogFor(month,day,null,0);
	}
	public String getBattleLogFor(int month,int day,TileLocation tl) {
		return getBattleLogFor(month,day,tl,0);
	}
	public String getBattleLogFor(int month,int day,TileLocation tl,int round) {
		String battleStartPattern = "<br>[\\w\\W]* - Evening of month "+month+", day "+day+", in clearing ";
		if (tl!=null) {
			battleStartPattern += tl.tile.getGameObject().getName()+" "+tl.clearing.getNum()+"$";
		}
		int logStart = getLineNumberFor(battleStartPattern)-1;
		if (round>0) {
			int roundStart = getLineNumberFor("<br>[\\w\\W]*Combat Round "+round+"$",logStart)-1;
			return StringUtilities.collectionToString(lines.subList(roundStart,lines.size()-1),"\n");
		}
		return StringUtilities.collectionToString(lines.subList(logStart,lines.size()-1),"\n");
//<br><font color="red"><b>RealmBattle</b></font> - =======================
//<br><font color="red"><b>RealmBattle</b></font> - Evening of month 1, day 5, in clearing Borderland 3
//<br><font color="red"><b>RealmBattle</b></font> - -----------------------
//<br><font color="red"><b>RealmBattle</b></font> - --  Combat Round 1
//<br><font color="red"><b>RealmBattle</b></font> - -----------------------
//...........
//<br><font color="red"><b>RealmBattle</b></font> - -----------------------
//<br><font color="red"><b>RealmBattle</b></font> - --  Combat Round 2
//<br><font color="red"><b>RealmBattle</b></font> - -----------------------
//		throw new NotImplementedException();
	}
	private int getLineNumberFor(String pattern) {
		return getLineNumberFor(pattern,0);
	}
	private int getLineNumberFor(String pattern,int lineStart) {
		Pattern p = Pattern.compile(pattern);
		for (int i=lineStart;i<lines.size();i++) {
			String line = lines.get(i).trim();
			Matcher m = p.matcher(line);
			if (m.matches()) {
				return i;
			}
		}
		return -1;
	}
}