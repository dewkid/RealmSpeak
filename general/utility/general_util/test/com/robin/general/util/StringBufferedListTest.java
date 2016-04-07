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

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link StringBufferedList}.
 */
public class StringBufferedListTest extends AbstractTest {

    private static final String UNEX_SIZE = "unexpected size";
    private static final String UNEX_STR = "unexpected toString";

    private static final String RUBY = "Ruby";
    private static final String EMERALD = "Emerald";
    private static final String DIAMOND = "Diamond";
    private static final String SAPPHIRE = "Sapphire";

    private StringBufferedList sbl;

    @Test
    public void basic() {
        title("basic");
        sbl = new StringBufferedList();
        print(sbl);
        assertEquals(UNEX_SIZE, 0, sbl.size());
    }

    @Test
    public void single() {
        title("single");
        sbl =  new StringBufferedList();
        sbl.append(RUBY);
        print(sbl);
        assertEquals(UNEX_SIZE, 1, sbl.size());
        assertEquals(UNEX_STR, "Ruby", sbl.toString());
    }

    @Test
    public void two() {
        title("two");
        sbl =  new StringBufferedList();
        sbl.append(RUBY);
        sbl.append(DIAMOND);
        print(sbl);
        assertEquals(UNEX_SIZE, 2, sbl.size());
        assertEquals(UNEX_STR, "Ruby, and Diamond", sbl.toString());
    }

    @Test
    public void three() {
        title("three");
        sbl =  new StringBufferedList();
        sbl.append(RUBY);
        sbl.append(DIAMOND);
        sbl.append(SAPPHIRE);
        print(sbl);
        assertEquals(UNEX_SIZE, 3, sbl.size());
        assertEquals(UNEX_STR, "Ruby, Diamond, and Sapphire", sbl.toString());
    }

    @Test
    public void gemsGalore() {
        title("gemsGalore");
        sbl =  new StringBufferedList();
        sbl.append(RUBY);
        sbl.append(RUBY);
        sbl.append(RUBY);
        sbl.append(DIAMOND);
        sbl.append(DIAMOND);
        sbl.append(SAPPHIRE);
        print(sbl);
        assertEquals(UNEX_SIZE, 6, sbl.size());
        assertEquals(UNEX_STR,
                "Ruby, Ruby, Ruby, Diamond, Diamond, and Sapphire", sbl.toString());

        sbl.countIdenticalItems();
        print(sbl);
        assertEquals(UNEX_SIZE, 3, sbl.size());
        assertEquals(UNEX_STR,
                "3 Rubys, 2 Diamonds, and Sapphire", sbl.toString());
    }
}
