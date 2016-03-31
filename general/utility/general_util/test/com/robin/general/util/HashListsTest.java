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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link HashLists}.
 */
public class HashListsTest extends AbstractTest {

    // verifies the contents of a hash
    private static class HashVerifier {
        private final HashLists<Jones, String> hash;
        private final List<Entry> entries = new ArrayList<>();
        private boolean checkKeyCount = true;

        HashVerifier(HashLists<Jones, String> hash) {
            this.hash = hash;
        }

        HashVerifier ignoreKeyCount() {
            checkKeyCount = false;
            return this;
        }

        HashVerifier add(Jones key, String... values) {
            entries.add(new Entry(key, values));
            return this;
        }

        private void _verifySize() {
            if (checkKeyCount) {
                assertEquals("wrong entry count", entries.size(), hash.size());
            }
        }

        private void _verifyListEntry(Entry e, List<String> values) {
            assertNotNull("missing list " + e.key, values);
            assertEquals("wrong value count: key=" + e.key,
                    e.values.length, values.size());
        }

        void verifyExact() {
            _verifySize();
            for (Entry e : entries) {
                List<String> values = hash.getList(e.key);
                _verifyListEntry(e, values);
                assertArrayEquals("array mismatch", e.values, values.toArray());
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


    private enum Jones {RAIDERS, TEMPLE, CRUSADE}

    private static final String MEDALLION = "Medallion";
    private static final String MONKEY = "Monkey";
    private static final String SNAKES = "Snakes";
    private static final String ARK = "The_Lost_Ark";

    private static final String ELEPHANT = "Elephant";
    private static final String PALACE = "Palace";
    private static final String BUGS = "Creepy_Crawlies";
    private static final String STONES = "The_Sacred_Stones";

    private static final String CASTLE = "Castle";
    private static final String AIRSHIP = "Airship";
    private static final String UMBRELLA = "Umbrella";
    private static final String GRAIL = "The_Holy_Grail";


    private HashLists<Jones, String> hash;


    private void verifyList(HashLists<Jones, String> hash,
                            Jones key, String... values) {
        new HashVerifier(hash)
                .ignoreKeyCount()
                .add(key, values)
                .verifyExact();
    }

    private void verifyOnlyList(HashLists<Jones, String> hash,
                                Jones key, String... values) {
        new HashVerifier(hash)
                .add(key, values)
                .verifyExact();
    }

    @Before
    public void before() {
        hash = new HashLists<>();
    }

    @Test
    public void basicUnique() {
        title("basicUnique");
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

    @Test
    public void checkUniquePut() {
        title("checkUniquePut");
        hash.put(Jones.RAIDERS, SNAKES);
        hash.put(Jones.RAIDERS, MONKEY);
        hash.put(Jones.RAIDERS, SNAKES);
        print(hash);
        verifyList(hash, Jones.RAIDERS, SNAKES, MONKEY);
    }

    @Test
    public void checkNonUniquePut() {
        title("checkNonUniquePut");
        hash = new HashLists<>(false);
        hash.put(Jones.RAIDERS, SNAKES);
        hash.put(Jones.RAIDERS, MONKEY);
        hash.put(Jones.RAIDERS, SNAKES);
        print(hash);
        verifyList(hash, Jones.RAIDERS, SNAKES, MONKEY, SNAKES);
    }

    @Test
    public void replaceList() {
        title("replaceList");
        hash.put(Jones.RAIDERS, SNAKES);
        hash.put(Jones.RAIDERS, MEDALLION);
        print(hash);
        verifyList(hash, Jones.RAIDERS, SNAKES, MEDALLION);

        List<String> laterInMovie = Arrays.asList(MONKEY, ARK);
        // FIXME: fix HashLists API to use List<T> not ArrayList<T>
        // for now, wrap our list in arraylist...
        ArrayList<String> replacement = new ArrayList<>(laterInMovie);
        hash.putList(Jones.RAIDERS, replacement);
        print(hash);
        verifyList(hash, Jones.RAIDERS, MONKEY, ARK);
    }

    @Test
    public void replaceListViolateUniqueness() {
        title("replaceListViolateUniqueness");
        hash.put(Jones.RAIDERS, ARK);
        hash.put(Jones.RAIDERS, ARK);
        print(hash);
        verifyList(hash, Jones.RAIDERS, ARK);

        // yeah, our hash lists has uniqueness forced
        // BUT...
        // can we break uniqueness?
        List<String> earlierInMovie = Arrays.asList(SNAKES, SNAKES, SNAKES);
        ArrayList<String> replacement = new ArrayList<>(earlierInMovie);
        assertEquals("not enough snakes", 3, replacement.size());
        hash.putList(Jones.RAIDERS, replacement);
        print(hash);

        // FIXME: fix putList() to honor the uniqueness setting
        // so that this following assertion passes...
//        verifyList(hash, Jones.RAIDERS, SNAKES);
    }

    @Test
    public void getListAsNewNoList() {
        title("getListAsNewNoList");
        assertEquals("not null", null, hash.getListAsNew(Jones.CRUSADE));
    }

    @Test
    public void getListAsNew() {
        title("getListAsNew");
        hash.put(Jones.TEMPLE, STONES);
        hash.put(Jones.TEMPLE, BUGS);
        print(hash);
        verifyList(hash, Jones.TEMPLE, STONES, BUGS);

        ArrayList<String> myList = hash.getList(Jones.TEMPLE);
        // we have a reference to the list in the hash, which we can
        // modify externally...
        myList.add(PALACE);
        verifyList(hash, Jones.TEMPLE, STONES, BUGS, PALACE);
        // lose the palace
        hash.removeKeyValue(Jones.TEMPLE, PALACE);

        // now do this again, but with getListAsNew...
        myList = hash.getListAsNew(Jones.TEMPLE);
        verifyList(hash, Jones.TEMPLE, STONES, BUGS);

        // here, we are just modifying our copy, not the list in the hash...
        myList.add(PALACE);
        verifyList(hash, Jones.TEMPLE, STONES, BUGS);
    }

    @Test
    public void clearTheHash() {
        title("clearTheHash");
        hash.put(Jones.RAIDERS, ARK);
        hash.put(Jones.TEMPLE, STONES);
        hash.put(Jones.CRUSADE, GRAIL);
        print(hash);
        assertEquals("not three lists", 3, hash.size());

        // check keySet() while we are here
        Set<Jones> keys = hash.keySet();
        assertEquals("unex key count", 3, keys.size());
        assertEquals("no raiders", true, keys.contains(Jones.RAIDERS));
        assertEquals("no temple", true, keys.contains(Jones.TEMPLE));
        assertEquals("no crusade", true, keys.contains(Jones.CRUSADE));

        hash.clear();
        print(hash);
        assertEquals("not cleared", 0, hash.size());
    }

    @Test
    public void containsValue() {
        title("containsValue");
        hash.put(Jones.RAIDERS, SNAKES);
        hash.put(Jones.RAIDERS, ARK);
        hash.put(Jones.TEMPLE, BUGS);
        hash.put(Jones.TEMPLE, STONES);
        hash.put(Jones.CRUSADE, CASTLE);
        hash.put(Jones.CRUSADE, GRAIL);
        print(hash);

        assertEquals("no bugs found", true, hash.containsValue(BUGS));
        assertEquals("found an umbrella", false, hash.containsValue(UMBRELLA));
    }

    @Test
    @Ignore
    public void putAllBasic() {
        title("putAllBasic");
        Map<Jones, String> map = new HashMap<>();
        map.put(Jones.RAIDERS, MEDALLION);
        map.put(Jones.TEMPLE, ELEPHANT);
        map.put(Jones.CRUSADE, AIRSHIP);

        // FIXME: putAll() throws NPE if list doesn't already exist for a key
        hash.putAll(map);
        print(hash);
        new HashVerifier(hash)
                .add(Jones.RAIDERS, MEDALLION)
                .add(Jones.TEMPLE, ELEPHANT)
                .add(Jones.CRUSADE, AIRSHIP)
                .verifyExact();
    }

    @Test
    @Ignore
    public void putAllWithLists() {
        title("putAllWithLists");
        Map<Jones, List<String>> map = new HashMap<>();
        map.put(Jones.RAIDERS, Arrays.asList(SNAKES, ARK));
        map.put(Jones.TEMPLE, Arrays.asList(ELEPHANT, STONES));
        map.put(Jones.CRUSADE, Arrays.asList(AIRSHIP, GRAIL));

        // FIXME: putAll() throws NPE if list doesn't already exist for a key
        hash.putAll(map);
        print(hash);
        new HashVerifier(hash)
                .add(Jones.RAIDERS, SNAKES, ARK)
                .add(Jones.TEMPLE, ELEPHANT, STONES)
                .add(Jones.CRUSADE, AIRSHIP, GRAIL)
                .verifyExact();
    }

    @Test
    public void removeStuff() {
        title("removeStuff");
        hash.put(Jones.CRUSADE, GRAIL);
        hash.put(Jones.CRUSADE, CASTLE);
        verifyList(hash, Jones.CRUSADE, GRAIL, CASTLE);

        hash.remove(Jones.CRUSADE);
        assertEquals("hash not empty", 0, hash.size());
    }

    @Test
    public void removeKeyValue() {
        title("removeKeyValue");
        hash.put(Jones.TEMPLE, STONES);
        hash.put(Jones.CRUSADE, CASTLE);
        hash.put(Jones.CRUSADE, AIRSHIP);
        hash.put(Jones.CRUSADE, GRAIL);
        print(hash);
        verifyList(hash, Jones.TEMPLE, STONES);
        verifyList(hash, Jones.CRUSADE, CASTLE, AIRSHIP, GRAIL);

        // unknown key has no effect
        hash.removeKeyValue(Jones.RAIDERS, CASTLE);
        verifyList(hash, Jones.TEMPLE, STONES);
        verifyList(hash, Jones.CRUSADE, CASTLE, AIRSHIP, GRAIL);

        // unknown value (from specific list) has no effect
        hash.removeKeyValue(Jones.CRUSADE, STONES);
        verifyList(hash, Jones.TEMPLE, STONES);
        verifyList(hash, Jones.CRUSADE, CASTLE, AIRSHIP, GRAIL);

        // remove from middle of list
        hash.removeKeyValue(Jones.CRUSADE, AIRSHIP);
        print(hash);
        verifyList(hash, Jones.CRUSADE, CASTLE, GRAIL);

        // remove remaining items
        hash.removeKeyValue(Jones.CRUSADE, CASTLE);
        hash.removeKeyValue(Jones.CRUSADE, GRAIL);
        print(hash);
        verifyOnlyList(hash, Jones.TEMPLE, STONES);
    }

    @Test
    public void removeValueFromAllLists() {
        title("removeValueFromAllLists");
        hash.put(Jones.RAIDERS, MONKEY);
        hash.put(Jones.RAIDERS, ARK);
        hash.put(Jones.TEMPLE, STONES);
        hash.put(Jones.TEMPLE, MONKEY);
        hash.put(Jones.CRUSADE, MONKEY);
        print(hash);
        verifyList(hash, Jones.RAIDERS, MONKEY, ARK);
        verifyList(hash, Jones.TEMPLE, STONES, MONKEY);
        verifyList(hash, Jones.CRUSADE, MONKEY);

        hash.removeValue(MONKEY);
        print(hash);
        verifyList(hash, Jones.RAIDERS, ARK);
        verifyList(hash, Jones.TEMPLE, STONES);
        verifyList(hash, Jones.CRUSADE);

        // TODO: Review -- this leaves an empty list in the hash (for CRUSADE)
        // Shouldn't we remove the empty list, to be consistent with the
        // behaviour of removeKeyValue() ?
    }
}
