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
package com.robin.magic_realm.MRCBuilder;

public class OutlineEntry {

	public OutlineEntry() {
		this(null, null);
	}

	public OutlineEntry(String s, String s1) {
		header = stripCR(s);
		content = stripCR(s1);
	}

	public String getHeader() {
		return header;
	}

	public String getContent() {
		return content;
	}

	public String toString() {
		return header + ":  " + content;
	}

	public static String stripCR(String s) {
		if (s != null) {
			int i = 0;
			int k = s.indexOf("\r", i);
			for (int l = s.indexOf("\n", i); k >= 0 || l >= 0;) {
				int j;
				if (l == -1 || k != -1 && k < l)
					j = k;
				else
					j = l;
				k = s.indexOf("\r", j);
				l = s.indexOf("\n", j);
				s = s.substring(0, j) + " " + s.substring(j + 1);
			}

			return s;
		}
		else {
			return "";
		}
	}

	String header;
	String content;
}