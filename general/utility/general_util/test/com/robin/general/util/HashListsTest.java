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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link HashLists}.
 */
public class HashListsTest extends AbstractTest {

    private enum Jones {RAIDERS, TEMPLE, CRUSADE}

    private static final String SNAKES = "Snakes";
    private static final String MEDALLION = "Medallion";
    private static final String MONKEY = "Monkey Spy";
    private static final String ARK = "The Lost Ark";

    private static final String ELEPHANT = "Elephant";
    private static final String STONE = "Sacred Stone";

    private static final String UMBRELLA = "Umbrella";
    private static final String AIRSHIP = "Airship";


    private HashLists<Jones, String> hash;

    @Test
    public void basicUnique() {
        title("basicUnique");
        hash = new HashLists<>();
        print(hash);

        assertEquals("unex size", 0, hash.size());
        assertEquals("not empty", true, hash.isEmpty());
        assertEquals("has raiders?", false, hash.containsKey(Jones.RAIDERS));
        assertEquals("has ark?", false, hash.containsValue(ARK));

        assertEquals("keys", 0, hash.keySet().size());
        assertEquals("values", 0, hash.values().size());
        assertEquals("entries", 0, hash.entrySet().size());
    }

    @Test
    public void basicPutAndGet() {
        title("basicPutAndGet");
        hash = new HashLists<>();

        hash.put(Jones.RAIDERS, ARK);
        hash.put(Jones.RAIDERS, MEDALLION);
        hash.put(Jones.TEMPLE, ELEPHANT);

        print(hash);
        new HashVerifier(hash)
                .add(Jones.RAIDERS, ARK, MEDALLION)
                .add(Jones.TEMPLE, ELEPHANT)
                .verifyExact();

        assertEquals("crusade?", null, hash.get(Jones.CRUSADE));
        assertEquals("empty?", false, hash.isEmpty());
    }

    // ========================================================================
    // verifies the contents of a hash
    private static class HashVerifier {
        private final HashLists<Jones, String> hash;
        private final List<Entry> entries = new ArrayList<>();

        HashVerifier(HashLists<Jones, String> hash) {
            this.hash = hash;
        }

        HashVerifier add(Jones key, String... values) {
            entries.add(new Entry(key, values));
            return this;
        }

        void verifyExact() {
            assertEquals("wrong entry count", entries.size(), hash.size());
            for (Entry e : entries) {
                List<String> values = hash.getList(e.key);
                assertNotNull("missing list " + e.key, values);
                assertEquals("wrong value count: key=" + e.key,
                        e.values.length, values.size());
                for (String s : e.values) {
                    assertTrue("missing value " + s, values.contains(s));
                }
            }
        }

        private class Entry {
            Jones key;
            String[] values;

            Entry(Jones key, String... values) {
                this.key = key;
                this.values = values;
            }
        }
    }
}
