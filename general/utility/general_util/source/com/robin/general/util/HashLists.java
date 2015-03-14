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

import java.util.*;

/**
 * A utility class for handling and populating lists of objects hashed by key.  Multiple objects can be added
 * for a single key.  Note that the lists are uniqued by default, unless specified otherwise.
 */
public class HashLists<K,T> implements Map {
    
	private boolean forceUnique;
    private Hashtable<K,ArrayList<T>> hash;
    
	public HashLists() {
		this(true);
	}
	public HashLists(boolean forceUnique) {
		this.forceUnique = forceUnique;
	    hash = new Hashtable<K,ArrayList<T>>();
	}
	public Object put(Object key,Object val) {
	    ArrayList<T> list = getList(key);
	    if (list==null) {
	        list = new ArrayList<T>();
	        hash.put((K)key,list);
	    }
	    if (!forceUnique || !list.contains(val)) {
		    list.add((T)val);
	    }
	    return null;
	}
	public void putList(K key,ArrayList<T> list) {
		hash.put(key,list);
	}
	public Object get(Object key) {
	    return hash.get(key);
	}
	public ArrayList<T> getList(Object key) {
	    return hash.get(key);
	}
	public ArrayList<T> getListAsNew(Object key) {
		ArrayList<T> list = getList(key);
		if (list!=null) {
			return new ArrayList<T>(list);
		}
		return null;
	}
	public int size() {
	    return hash.size();
	}
	public void clear() {
	    hash.clear();
	}
	public boolean containsKey(Object key) {
	    return hash.containsKey(key);
	}
	public boolean containsValue(Object val) {
	    for (Iterator i=hash.values().iterator();i.hasNext();) {
	        ArrayList list = (ArrayList)i.next();
	        if (list.contains(val)) {
	            return true;
	        }
	    }
	    return false;
	}
	public boolean isEmpty() {
	    return hash.isEmpty();
	}
	public Set<K> keySet() {
	    return hash.keySet();
	}
	public void putAll(Map map) {
	    for (Iterator i=map.keySet().iterator();i.hasNext();) {
	        Object key = i.next();
	        ArrayList list = getList(key);
	        Object val = map.get(key);
	        if (val instanceof Collection) {
	            list.addAll((Collection)val);
	        }
	        else {
	            list.add(val);
	        }
	    }
	}
	public Set entrySet() {
	    return hash.entrySet();
	}
	public Object remove(Object key) {
	    return hash.remove(key);
	}
	public void removeKeyValue(Object key,Object val) {
		if (key!=null) {
			ArrayList list = getList(key);
			if (list!=null && list.contains(val)) {
				list.remove(val);
				if (list.isEmpty()) {
					remove(key);
				}
			}
		}
	}
	public void removeValue(Object val) {
	    for (Iterator i=hash.values().iterator();i.hasNext();) {
	        ArrayList list = (ArrayList)i.next();
	        if (list.contains(val)) {
	            list.remove(val);
	        }
	    }
	}
	public Collection values() {
	    return hash.values();
	}
}