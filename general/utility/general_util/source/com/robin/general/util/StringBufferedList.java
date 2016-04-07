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

/**
 * Creates a list of items, with the ability to consolidate identical items
 * with a count of those items.
 */
public class StringBufferedList {
    private String comma;
    private String and;
    private ArrayList<String> list;

    /**
     * Create a string buffered list using comma/and separators.
     */
    public StringBufferedList() {
        this(", ", "and ");
    }

    /**
     * Create a string buffered list using the specified separators.
     *
     * @param comma the "comma" separator
     * @param and the "and" separator
     */
    public StringBufferedList(String comma, String and) {
        this.comma = comma;
        this.and = and;
        list = new ArrayList<String>();
    }

    /**
     * Returns the size of the list.
     *
     * @return size of list
     */
    public int size() {
        return list.size();
    }

    /**
     * Appends the given value to the list.
     *
     * @param val value to append
     */
    public void append(String val) {
        list.add(val);
    }

    /**
     * Appends all items from the given list to this list.
     *
     * @param list the items to append
     */
    public void appendAll(Collection<String> list) {
        for (String val : list) {
            append(val);
        }
    }

    /**
     * Converts the list to provide counts of identical items.
     */
    public void countIdenticalItems() {
        HashLists hash = new HashLists();
        ArrayList<String> keys = new ArrayList<String>();
        int n = 0;
        for (String string : list) {
            hash.put(string, "n" + (n++));
            if (!keys.contains(string)) {
                keys.add(string);
            }
        }
        list.clear();
        for (String string : keys) {
            int count = hash.getList(string).size();
            if (count == 1) {
                list.add(string);
            } else {
                list.add(count + " " + string + (count == 1 ? "" : "s"));
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            String val = (String) list.get(i);
            if (sb.length() > 0) {
                sb.append(comma);
                if (i == (list.size() - 1)) {
                    sb.append(and);
                }
            }
            sb.append(val);
        }
        return sb.toString();
    }
}