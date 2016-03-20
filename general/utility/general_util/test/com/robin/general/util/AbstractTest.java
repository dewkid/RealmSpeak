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

/**
 * Abstract base class for unit tests.
 */
public abstract class AbstractTest {

    /**
     * Tolerance for double comparisons.
     */
    protected static final double TOLERANCE = 1e-9;

    /**
     * Prints formatted output.
     *
     * @see String#format(String, Object...)
     * @param fmt format string
     * @param items positional items
     */
    protected void print(String fmt, Object... items) {
        System.out.println(String.format(fmt, items));
    }

    /**
     * Prints the string value of the given object.
     *
     * @param object the object to print
     */
    protected void print(Object object) {
        print("%s", object);
    }
}
