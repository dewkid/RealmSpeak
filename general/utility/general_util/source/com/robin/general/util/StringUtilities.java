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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Useful string utility functions.
 */
public class StringUtilities {

    /**
     * Returns the given string repeated for the specified count.
     *
     * @param string the string to repeat
     * @param count  the number of times to repeat
     * @return the repeated string
     */
    public static String getRepeatString(String string, int count) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            sb.append(string);
        }
        return sb.toString();
    }

    /**
     * Converts the first character to uppercase, and all others to lowercase.
     *
     * @param string string to transform
     * @return capitalized string
     */
    public static String capitalize(String string) {
        if (string != null && string.length() > 0) {
            if (string.length() == 1) {
                return string.toUpperCase();
            }
            return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
        }
        return string;
    }

    /*
     * Finds all occurrences of "find" in "fullString", and changes them to "replace"
     */

    /**
     * Replaces all occurrences of the specified substring in the given string
     * with the specified replacement string.
     *
     * @param fullString string to modify
     * @param find       substring to find
     * @param replace    replacement
     * @return the updated string
     */
    public static String findAndReplace(String fullString, String find, String replace) {
        int found;
        int findLength = find.length();
        while ((found = fullString.indexOf(find)) >= 0) {
            String front = fullString.substring(0, found);
            String back = "";
            if ((front.length() + findLength) < fullString.length()) {
                back = fullString.substring(found + findLength);
            }
            fullString = front + replace + back;
        }
        return fullString;
    }

    /**
     * Returns a string representation of the given collection, using the
     * specified delimiter.
     *
     * @param c     the collection
     * @param delim the delimiter
     * @return string representation of the collection
     */
    public static String collectionToString(Collection c, String delim) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            Object o = i.next();
            if (o != null) {
                sb.append(o.toString());
            } else {
                sb.append("null");
            }
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of the given array of booleans,
     * using the specified delimiter.
     *
     * @param b     array of booleans
     * @param delim the delimiter to use
     * @return string representation of the booleans
     */
    public static String booleanArrayToString(boolean[] b, String delim) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(b[i] ? "T" : "F");
        }
        return sb.toString();
    }

    /**
     * Parses the given string using the specified delimiter(s) to produce a
     * list of all values in the string.
     * <p>
     * Where delimiters suggest a missing value, an empty string will be given.
     * For example, the string {@code ",,,"} will return four empty strings.
     *
     * @param string the string to parse
     * @param delim  the delimiter to use
     * @return a list of parsed values
     */
    public static ArrayList<String> stringToCollection(String string, String delim) {
        return stringToCollection(string, delim, false);
    }

    /**
     * Parses the given string using the specified delimiter(s) to produce a
     * list of all values in the string. If the {@code convertNulls} parameter
     * is true, any values of the string "null" will be replaced with
     * a {@code null} reference.
     * <p>
     * Where delimiters suggest a missing value, an empty string will be given.
     * For example, the string {@code ",,,"} will return four empty strings.
     *
     * @param string       the string to parse
     * @param delim        the delimiter to use
     * @param convertNulls if true, convert "null" to null
     * @return a list of parsed values
     */
    public static ArrayList<String> stringToCollection(String string, String delim,
                                                       boolean convertNulls) {
        ArrayList<String> list = new ArrayList<String>();
        boolean stringAdded = false;
        StringTokenizer tokens = new StringTokenizer(string, delim, true);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (delim.indexOf(token) >= 0) {
                // found a delimiter
                if (!stringAdded) {
                    list.add("");
                }
                stringAdded = false;
            } else {
                // found a string
                if (convertNulls && "null".equals(token)) {
                    token = null;
                }
                list.add(token);
                stringAdded = true;
            }
        }
        if (!stringAdded) {
            list.add("");
        }
        return list;
    }

    /**
     * Parses the given string using the specified delimiter(s) and returns
     * an array of integers.
     *
     * @param string the string to parse
     * @param delim the delimiter(s) to use
     * @return array of integers
     */
    public static int[] stringToIntArray(String string, String delim) {
        StringTokenizer tokens = new StringTokenizer(string, delim);
        int[] ret = new int[tokens.countTokens()];
        for (int i = 0; i < ret.length; i++) {
            Integer num = Integer.valueOf(tokens.nextToken());
            ret[i] = num.intValue();
        }
        return ret;
    }

    /**
     * Parses the given string using the specified delimiter(s) and returns
     * an array of booleans. It is assumed that true and false are represented
     * by "T" and "F" in the string.
     *
     * @param string the string to parse
     * @param delim the delimiter(s) to use
     * @return array of booleans
     */
    public static boolean[] stringToBooleanArray(String string, String delim) {
        StringTokenizer tokens = new StringTokenizer(string, delim);
        boolean[] ret = new boolean[tokens.countTokens()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = tokens.nextToken().equals("T");
        }
        return ret;
    }

    /**
     * Returns a string representation of the given array of integers, using
     * the specified delimiter.
     *
     * @param array the array of integers
     * @param delim the delimiter
     * @return string representation
     */
    public static String intArrayToString(int[] array, String delim) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            list.add(new Integer(array[i]));
        }
        return collectionToString(list, delim);
    }

    /**
     * Returns the given number padded with leading zeros, for a given length.
     *
     * @param num the number to pad
     * @param length the length of the field
     * @return the zero-padded number
     */
    public static String zeroPaddedInt(int num, int length) {
        String val = String.valueOf(num);
        while (val.length() < length) {
            val = "0" + val;
        }
        return val;
    }
}