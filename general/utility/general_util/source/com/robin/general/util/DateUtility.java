/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2016 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Date-based utility methods.
 */
public class DateUtility {

    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");

    /**
     * Returns the time as is current\ly set on your computer.
     *
     * @return the time now
     */
    public static Date getNow() {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        Date date = cal.getTime();
        return date;
    }

    /**
     * Returns a string representation of the given date. Note that this can be
     * parsed back into a date using {@link #convertString2Date(String)}.
     *
     * @param date the date
     * @return a string representation of the date
     */
    public static String convertDate2String(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Returns a date instance corresponding to the given string value.
     * The format should be that produced by calls to
     * {@link #convertDate2String(Date)}. Returns null if the string cannot
     * be parsed.
     *
     * @param val the string representation
     * @return the corresponding date.
     */
    public static Date convertString2Date(String val) {
        try {
            return dateFormat.parse(val);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}