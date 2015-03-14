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
package com.robin.magic_realm.RealmBattle;

import java.util.Comparator;

import com.robin.magic_realm.components.RealmComponent;

public class TargetIndexComparator implements Comparator {
	public int compare(Object o1,Object o2) {
		int ret = 0;
		RealmComponent rc1 = (RealmComponent)o1;
		RealmComponent rc2 = (RealmComponent)o2;
		ret = rc1.getTargetIndex()-rc2.getTargetIndex();
		return ret;
	}
}