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
package com.robin.general.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtility {
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
	
	/**
	 * Returns the time as is currently set on your computer.
	 */
	public static Date getNow() {
		GregorianCalendar cal = (GregorianCalendar)GregorianCalendar.getInstance();
		Date date = cal.getTime();
		return date;
	}
	/**
	 * Turn a date into a string that can be saved and later parsed by convertString2Date().
	 */
	public static String convertDate2String(Date date) {
		return dateFormat.format(date);
	}
	/**
	 * Read a string created by the convertDate2String() method, and return a date.
	 */
	public static Date convertString2Date(String val) {
		try {
			return dateFormat.parse(val);
		}
		catch(ParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}