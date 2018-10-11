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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link StringUtilities}.
 */
public class StringUtilitiesTest extends AbstractTest {

    private static final String UX_R = "Unexpected Result";

    private static final String SAMPLE = "The gold coin and the gold cup.";

    private static final List<String> METALS =
            Arrays.asList("gold", "silver", "bronze");

    private static final List<String> THINGS =
            Arrays.asList("coin", null, "cup");

    private static final boolean[] BOOLS = {true, false, false, true};

    @Test
    public void repeatedString() {
        title("repeatedString");
        String s = StringUtilities.getRepeatString("xo", 4);
        print(s);
        assertEquals(UX_R, "xoxoxoxo", s);
    }

    @Test
    public void capitalized() {
        title("capitalized");
        assertEquals(UX_R, "", StringUtilities.capitalize(""));
        assertEquals(UX_R, "A", StringUtilities.capitalize("A"));
        assertEquals(UX_R, "A", StringUtilities.capitalize("a"));
        assertEquals(UX_R, "Ab", StringUtilities.capitalize("ab"));
        assertEquals(UX_R, "Ab", StringUtilities.capitalize("AB"));
        assertEquals(UX_R, "Ab", StringUtilities.capitalize("aB"));
        assertEquals(UX_R, "Ab", StringUtilities.capitalize("Ab"));
        assertEquals(UX_R, "Magical land",
                StringUtilities.capitalize("magical LAND"));
    }

    @Test
    public void findAndReplace() {
        title("findAndReplace");
        String s = StringUtilities.findAndReplace(SAMPLE, "gold", "silver");
        print(s);
        assertEquals(UX_R, "The silver coin and the silver cup.", s);

        s = StringUtilities.findAndReplace(SAMPLE, "coin", "plate");
        print(s);
        assertEquals(UX_R, "The gold plate and the gold cup.", s);

        s = StringUtilities.findAndReplace(SAMPLE, "dagger", "sword");
        print(s);
        assertEquals(UX_R, SAMPLE, s);
    }

    @Test
    public void collectionToString() {
        title("collectionToString");
        String s = StringUtilities.collectionToString(METALS, ",");
        print(s);
        assertEquals(UX_R, "gold,silver,bronze", s);

        s = StringUtilities.collectionToString(METALS, " -> ");
        print(s);
        assertEquals(UX_R, "gold -> silver -> bronze", s);
    }

    @Test
    public void collectionWithNullToString() {
        title("collectionWithNullToString");
        String s = StringUtilities.collectionToString(THINGS, ", ");
        print(s);
        assertEquals(UX_R, "coin, null, cup", s);
    }

    @Test
    public void boolArrayToString() {
        title("boolArrayToString");
        String s = StringUtilities.booleanArrayToString(BOOLS, "");
        print(s);
        assertEquals(UX_R, "TFFT", s);

        s = StringUtilities.booleanArrayToString(BOOLS, ",");
        print(s);
        assertEquals(UX_R, "T,F,F,T", s);
    }

    private void checkS2CResults(List<String> result, int expSize, String... items) {
        print("  Result> %d : %s", result.size(), result);
        assertEquals("wrong size", expSize, result.size());
        assertArrayEquals("bad parse", items, result.toArray());
    }

    private void checkS2C(String s, String d, int expSize, String... items) {
        List<String> result = StringUtilities.stringToCollection(s, d);
        print("Str<%s>, Delim<%s>", s, d);
        checkS2CResults(result, expSize, items);
    }

    private void checkS2CNulls(String s, String d, int expSize, String... items) {
        List<String> result = StringUtilities.stringToCollection(s, d, true);
        print("Str<%s>, Delim<%s>", s, d);
        checkS2CResults(result, expSize, items);
    }

    @Test
    public void stringToCollection() {
        title("stringToCollection");

        checkS2C("---", "-", 4, "", "", "", "");

        checkS2C(",robin,,bluebird,", ",", 5,
                "", "robin", "", "bluebird", "");

        checkS2C("one,two,three;four,five;six", ";,", 6,
                "one", "two", "three", "four", "five", "six");

        checkS2C("asdf,asdf,asdf;qweg,wgwe,wgw;ffifi,ff,ff,", ";", 3,
                "asdf,asdf,asdf", "qweg,wgwe,wgw", "ffifi,ff,ff,");

        checkS2C("foo,null,bar", ",", 3, "foo", "null", "bar");

        checkS2CNulls("foo,null,bar", ",", 3, "foo", null, "bar");
    }

    private static final int[] ARRAY_OF_INTS = {12, 14, 17};

    @Test
    public void stringToIntArray() {
        title("stringToIntArray");
        int[] result = StringUtilities.stringToIntArray("12,14:17", ",:");
        print(Arrays.toString(result));
        assertArrayEquals("bad int parse", ARRAY_OF_INTS, result);
    }

    private static final boolean[] ARRAY_OF_BOOLS = {true, false, false, true};

    @Test
    public void stringToBoolArray() {
        title("stringToBoolArray");
        boolean[] result = StringUtilities.stringToBooleanArray("T.F.F.T", ".");
        print(Arrays.toString(result));
        assertArrayEquals("bad bool parse", ARRAY_OF_BOOLS, result);
    }

    @Test
    public void intArrayToString() {
        title("intArrayToString");
        String s = StringUtilities.intArrayToString(ARRAY_OF_INTS, ", ");
        print(s);
        assertEquals("wrong format", "12, 14, 17", s);
    }

    @Test
    public void zeroPadInt() {
        title("zeroPadInt");
        String s = StringUtilities.zeroPaddedInt(5, 4);
        print(s);
        assertEquals("bad pad 5/4", "0005", s);

        s = StringUtilities.zeroPaddedInt(42, 3);
        print(s);
        assertEquals("bad pad 42/3", "042", s);

        s = StringUtilities.zeroPaddedInt(42, 1);
        print(s);
        assertEquals("bad pad 42/1", "42", s);
    }
}
