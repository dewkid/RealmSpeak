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

import java.util.Collection;

/**
 * Collection-based utility methods.
 */
public class CollectionUtility {

    /**
     * Returns true if the given collection contains <b>any</b> value in
     * the specified query; false otherwise.
     *
     * @param collection the collection to examine
     * @param query      the set of items to check for
     * @param <T>        collection type
     * @return true if the collection contains any of the query items
     */
    public static <T> boolean containsAny(Collection<T> collection, Collection<T> query) {
        if (collection == null || collection.isEmpty() || query == null || query.isEmpty())
            return false;
        for (T val : query) {
            if (collection.contains(val))
                return true;
        }
        return false;
    }
}
