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

/**
 * A specialization of {@link ArrayList} that only adds items if they are
 * not already in the list.
 *
 * @param <T> type of list item
 */
public class UniqueArrayList<T> extends ArrayList<T> {
    /*
     * This isn't a complete implementation, but works for what I want right now.
     */
    private boolean allowNull;

    /**
     * Creates a unique array list where null is allowed as an item.
     */
    public UniqueArrayList() {
        this(true);
    }

    /**
     * Creates a unique array list. If the given parameter is true, null
     * will be allowed in the list, otherwise it won't.
     *
     * @param allowNull true to allow nulls in the list
     */
    public UniqueArrayList(boolean allowNull) {
        super();
        this.allowNull = allowNull;
    }

    @Override
    public boolean add(T obj) {
        if ((allowNull || obj != null) && !contains(obj)) {
            return super.add(obj);
        }
        return false;
    }
}