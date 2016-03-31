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

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link OrderedHashtable}.
 */
public class OrderedHashtableTest extends AbstractTest {

    private enum Hiker {ARTHUR, FORD, ZAPHOD, TRILLIAN}

    private static final String TEA = "Tea";
    private static final String CUTE = "Cute";
    private static final String ALIEN = "Alien";


    private OrderedHashtable<Hiker, String> oh;

    @Before
    public void before() {
        oh = new OrderedHashtable<>();
    }

    @Test
    public void basic() {
        title("basic");
        print(oh);
        assertEquals("non-empty", 0, oh.size());
    }

    @Test
    public void order() {
        title("order");
        oh.put(Hiker.ARTHUR, TEA);
        oh.put(Hiker.TRILLIAN, CUTE);
        oh.put(Hiker.FORD, ALIEN);
        print(oh);

        // we expect the keys to come out in the same order as they went in
        Set<Hiker> keys = oh.keySet();
        Iterator<Hiker> iter = keys.iterator();
        assertEquals("not arthur", Hiker.ARTHUR, iter.next());
        assertEquals("not trillian", Hiker.TRILLIAN, iter.next());
        assertEquals("not ford", Hiker.FORD, iter.next());
    }

    // ======================================================================
    // TODO: (1) write tests to assert sortedness of keys
    //       possibly override toString() to use order of keys

    // TODO: (2) re-write guts of OrderedHashtable to use TreeMap internally

    // TODO: (3) incrementally wean production code off OrderedHashtable
    //           in deference to using TreeMap where needed.
}
