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

import java.util.Optional;

public class Extensions {

    /**
     * Returns the first non-null object of the pair specified, or null if
     * both references are null.
     *
     * @param obj1 first object
     * @param obj2 second object
     * @param <T>  object type
     * @return the first non-null object (or null if both are)
     */
    public static <T> T coalesce(T obj1, T obj2) {
        T result = obj1;
        if (result == null) result = obj2;
        return result;
    }

    /**
     * Returns an optional of the given type; either the first one with a
     * value, or an empty optional if neither parameter has a value.
     *
     * @param o1  first optional
     * @param o2  second optional
     * @param <T> type of optional
     * @return the first optional with a value (or empty optional)
     */
    public static <T> Optional<T> coalesce(Optional<T> o1, Optional<T> o2) {
        Optional<T> result = o1;
        if (!result.isPresent()) result = o2;
        return result;
    }

    /**
     * Returns true if the given value has the specified (bit) flag set.
     *
     * @param value the value
     * @param flag  bit flag
     * @return true if the flag is set in the value
     */
    public static boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }
}
