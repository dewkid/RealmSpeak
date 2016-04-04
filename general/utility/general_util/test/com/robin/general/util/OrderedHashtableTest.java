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
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link OrderedHashtable}.
 */
public class OrderedHashtableTest extends AbstractTest {

    private enum Hiker {ARTHUR, FORD, MARVIN, TRILLIAN, ZAPHOD}

    private static final String TEA = "Tea";
    private static final String DRESSING_GOWN = "Dressing Gown";
    private static final String CUTE = "Cute";
    private static final String SMART = "Smart";
    private static final String ALIEN = "Alien";
    private static final String DEPRESSED = "Depressed";
    private static final String OBLIVIOUS = "Oblivious";


    private OrderedHashtable<Hiker, String> oh;
    private Collection<String> vals;

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

        // while we are here... let's clear also
        oh.clear();
        print(oh);
        assertEquals("not empty", 0, oh.size());
        assertEquals("ordered list not empty", 0, oh.orderedKeys().size());
    }

    @Test
    public void putAll() {
        title("putAll");
        Map<Hiker, String> map = new HashMap<>();
        map.put(Hiker.ARTHUR, TEA);
        map.put(Hiker.MARVIN, DEPRESSED);
        map.put(Hiker.ZAPHOD, OBLIVIOUS);

        print(oh);
        // note, there is nothing in the ordered hash.

        oh.putAll(map);
        print(oh);
        assertEquals("not empty", 0, oh.size());

        // we have to populate the ordered hash first...
        oh.put(Hiker.ZAPHOD, ALIEN);
        oh.put(Hiker.ARTHUR, DRESSING_GOWN);
        print(oh);
        assertEquals("not two", 2, oh.size());

        // NOW, when we use the map, we put all elements of the map
        //      FOR WHICH there ALREADY EXISTS a key in the ordered map
        oh.putAll(map);
        print(oh);
        Iterator<Hiker> iter = oh.keySet().iterator();
        checkHiker(iter.next(), Hiker.ZAPHOD, OBLIVIOUS);
        checkHiker(iter.next(), Hiker.ARTHUR, TEA);
        assertEquals("more hikers?", false, iter.hasNext());
    }

    private void checkHiker(Hiker next, Hiker expKey, String expValue) {
        assertEquals("unexpected key", expKey, next);
        assertEquals("unexpected value", expValue, oh.get(next));
    }

    @Test
    public void values() {
        title("values");
        oh.put(Hiker.ARTHUR, DRESSING_GOWN);
        oh.put(Hiker.TRILLIAN, SMART);
        oh.put(Hiker.ZAPHOD, OBLIVIOUS);
        print(oh);


        vals = oh.values();
        print(vals);
        checkValues(vals, DRESSING_GOWN, SMART, OBLIVIOUS);
    }

    private void initMZTA() {
        oh.put(Hiker.MARVIN, DEPRESSED);
        oh.put(Hiker.ZAPHOD, OBLIVIOUS);
        oh.put(Hiker.TRILLIAN, SMART);
        oh.put(Hiker.ARTHUR, DRESSING_GOWN);
        checkValues(oh.values(), DEPRESSED, OBLIVIOUS, SMART, DRESSING_GOWN);
    }

    @Test
    public void removalByKey() {
        title("removalByKey");
        initMZTA();
        print(oh);

        String v = oh.remove(Hiker.TRILLIAN);
        assertEquals("not trillian val", SMART, v);
        print(oh);
        checkValues(oh.values(), DEPRESSED, OBLIVIOUS, DRESSING_GOWN);
    }

    @Test
    @Ignore
    public void removalByIndex() {
        title("removalByIndex");
        initMZTA();
        print(oh);

        // FIXME: fix Object remove(int) -- assumes key is a string
        Object v = oh.remove(1);
        print(oh);
        print(v);
        assertEquals("not zaph", OBLIVIOUS, v);
    }

    private void checkValues(Collection<String> v, String... expected) {
        assertArrayEquals("wrong values", expected, v.toArray());

    }

    @Test
    public void getKeyByIndex() {
        title("getKeyInt");
        initMZTA();

        Object k = oh.getKey(2);
        assertEquals("not trill", Hiker.TRILLIAN, k);
    }

    @Test
    public void getValueByIndex() {
        title("getValueByIndex");
        initMZTA();

        Object v = oh.getValue(2);
        assertEquals("not smart", SMART, v);
    }

    @Test
    public void indexOf() {
        title("indexOf");
        initMZTA();

        assertEquals("wrong arthur index", 3, oh.indexOf(Hiker.ARTHUR));
        assertEquals("not missing", -1, oh.indexOf(Hiker.FORD));
    }

    @Test
    public void orderedKeys() {
        title("orderedKeys");
        initMZTA();

        ArrayList<Hiker> okeys = oh.orderedKeys();
        Iterator<Hiker> iter = okeys.iterator();
        assertEquals("wrong hiker", Hiker.MARVIN, iter.next());
        assertEquals("wrong hiker", Hiker.ZAPHOD, iter.next());
        assertEquals("wrong hiker", Hiker.TRILLIAN, iter.next());
        assertEquals("wrong hiker", Hiker.ARTHUR, iter.next());
        assertEquals("too many hikers", false, iter.hasNext());
    }

    @Test
    @Ignore
    public void immutableOrderedKeys() {
        title("immutableOrderedKeys");  // NOT!
        initMZTA();

        assertEquals("trillian at 2", 2, oh.indexOf(Hiker.TRILLIAN));

        // oops, we are passing a reference to internal state...
        oh.orderedKeys().set(2, Hiker.FORD);
        // FIXME: do not give away references!!

        // sadly, no more trillian
        assertEquals("trillian at 2", 2, oh.indexOf(Hiker.TRILLIAN));
    }

    @Test
    public void insert() {
        title("insert");
        initMZTA();
        print(oh);

        Object v = oh.insert(1, Hiker.FORD, ALIEN);
        print(v);
        assertEquals("non null", null, v);  // no previous mapping for FORD
        print(oh);
        checkIndices(Hiker.MARVIN, Hiker.FORD, Hiker.ZAPHOD,
                Hiker.TRILLIAN, Hiker.ARTHUR);
    }

    private void checkIndices(Hiker... hikers) {
        for (int i = 0; i < hikers.length; i++) {
            assertEquals("hiker bad index", i, oh.indexOf(hikers[i]));
        }
    }

    @Test
    @Ignore
    public void replace() {
        title("replace");
        initMZTA();
        print(oh);
        // FIXME: replace casts key to String
        oh.replace(1, Hiker.FORD, ALIEN);
        print(oh);

    }

    @Test
    public void sortKeys() {
        title("sortKeys");
        initMZTA();

        oh.sortKeys((o1, o2) -> o1.ordinal() - o2.ordinal());
        checkIndices(Hiker.ARTHUR, Hiker.MARVIN, Hiker.TRILLIAN, Hiker.ZAPHOD);
    }

    /* === NOTES:
            class methods should be properly typed with T and U

            orderedKeys won't be null, so checks for null are unnecessary:
                put(T, U)
                values()

            remove(int) casts result to String
            replace(int, T, U) casts currentKey to String
    */

}
