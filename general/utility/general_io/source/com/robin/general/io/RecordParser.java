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
package com.robin.general.io;

import java.util.StringTokenizer;

/**
 * Parses field-delimited records.
 *
 * @deprecated not used in the codebase
 */
@Deprecated
public class RecordParser {

    private String[] field;

    /**
     * Creates a parser for the given line using the specified delimiter.
     *
     * @param line  the line to parse
     * @param delim the field delimiter
     */
    public RecordParser(String line, String delim) {
        line = insertSpaces(line, delim);
        StringTokenizer tokens = new StringTokenizer(line.toString(), delim);
        field = new String[tokens.countTokens()];
        for (int i = 0; i < field.length; i++) {
            field[i] = tokens.nextToken();
        }
    }

    // make sure we have spaces in between adjacent delimiters, and at the
    // beginning and end of the record if it starts/ends with delimiters.
    private static String insertSpaces(String val, String delim) {
        if (val.startsWith(delim)) {
            val = " " + val;
        }
        if (val.endsWith(delim)) {
            val = val + " ";
        }
        int index;
        while ((index = val.indexOf(delim + delim)) >= 0) {
            val = val.substring(0, index + 1) + " " + val.substring(index + 1);
        }
        return val;
    }

    /**
     * Returns the total number of fields in the record.
     *
     * @return number of fields
     */
    public int totalFields() {
        return field.length;
    }

    /**
     * Returns the value of the field with the specified index. Index 0 is the
     * first field. If an index is out of bounds (less than 0 or greater than
     * the highest index) an empty string is returned.
     *
     * @param index field index
     * @return value of that field
     */
    public String getField(int index) {
        if (index >= 0 && index < field.length) {
            return stripEndQuotes(field[index]);
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < field.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(field[i]);
        }
        return sb.toString();
    }

    /**
     * Strips the end quotes {@code (")} from the given string if it both
     * starts and ends with them.
     *
     * @param val the string
     * @return the string with end quotes stripped off
     */
    public static String stripEndQuotes(String val) {
        if (val != null && val.length() > 1) {
            if (val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }
        }
        return val;
    }
}