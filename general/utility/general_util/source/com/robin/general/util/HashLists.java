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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A utility class for handling and populating lists of objects hashed by key.
 * Multiple objects can be added for a single key.
 * Note that the lists are uniqued by default, unless specified otherwise.
 */
public class HashLists<K, T> implements Map {

    private boolean forceUnique;
    private Hashtable<K, ArrayList<T>> hash;

    /**
     * Constructs a new hash of lists, forcing uniqueness.
     */
    public HashLists() {
        this(true);
    }

    /**
     * Constructs a new hash of lists, specifying whether uniqueness should
     * be enforced.
     *
     * @param forceUnique indicates whether uniqueness should be enforced
     */
    public HashLists(boolean forceUnique) {
        this.forceUnique = forceUnique;
        hash = new Hashtable<K, ArrayList<T>>();
    }

    @Override
    public Object put(Object key, Object val) {
        ArrayList<T> list = getList(key);
        if (list == null) {
            list = new ArrayList<T>();
            hash.put((K) key, list);
        }
        if (!forceUnique || !list.contains(val)) {
            list.add((T) val);
        }
        return null;
    }

    /**
     * Replaces a complete list for the given key.
     *
     * @param key the key
     * @param list the list to insert under that key
     */
    public void putList(K key, ArrayList<T> list) {
        hash.put(key, list);
    }

    @Override
    public Object get(Object key) {
        return hash.get(key);
    }

    /**
     * Returns the list of values for the given key.
     *
     * @param key the key
     * @return the corresponding list of values
     */
    public ArrayList<T> getList(Object key) {
        return hash.get(key);
    }

    /**
     * Returns an empty list if a list for the given key exists; else
     * returns null.
     *
     * @param key the key
     * @return an empty list if the key exists; null otherwise
     */
    public ArrayList<T> getListAsNew(Object key) {
        ArrayList<T> list = getList(key);
        if (list != null) {
            return new ArrayList<T>(list);
        }
        return null;
    }

    @Override
    public int size() {
        return hash.size();
    }

    @Override
    public void clear() {
        hash.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return hash.containsKey(key);
    }

    /**
     * Returns true if the specified values exists in any of the lists
     * in this hash.
     *
     * @param val the value to search for
     * @return true if the value is in a list in the hash; false otherwise
     */
    @Override
    public boolean containsValue(Object val) {
        for (Iterator i = hash.values().iterator(); i.hasNext(); ) {
            ArrayList list = (ArrayList) i.next();
            if (list.contains(val)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return hash.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return hash.keySet();
    }

    @Override
    public void putAll(Map map) {
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            ArrayList list = getList(key);
            Object val = map.get(key);
            if (val instanceof Collection) {
                list.addAll((Collection) val);
            } else {
                list.add(val);
            }
        }
    }

    @Override
    public Set entrySet() {
        return hash.entrySet();
    }

    @Override
    public Object remove(Object key) {
        return hash.remove(key);
    }

    /**
     * Removes the given value from the list with the given key. If this
     * results in the list becoming empty, then the list is removed also.
     *
     * @param key
     * @param val
     */
    public void removeKeyValue(Object key, Object val) {
        if (key != null) {
            ArrayList list = getList(key);
            if (list != null && list.contains(val)) {
                list.remove(val);
                if (list.isEmpty()) {
                    remove(key);
                }
            }
        }
    }

    /**
     * Removes the given value from all lists in the hash.
     *
     * @param val
     */
    public void removeValue(Object val) {
        for (Iterator i = hash.values().iterator(); i.hasNext(); ) {
            ArrayList list = (ArrayList) i.next();
            if (list.contains(val)) {
                list.remove(val);
            }
        }
    }

    @Override
    public Collection values() {
        return hash.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HashLists:").append(EOL);
        Set<K> keySet = new TreeSet<>(hash.keySet());
        if (keySet.isEmpty()) {
            sb.append("  (empty)").append(EOL);
        } else {
            for (K key : keySet) {
                sb.append("  ").append(key).append(" => ")
                        .append(hash.get(key)).append(EOL);
            }
        }
        return sb.toString();
    }

    private static final String EOL = String.format("%n");
}