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

package com.robin.general.io;

import com.robin.general.util.AbstractTest;
import org.junit.Test;

import static com.robin.general.io.RecordParser.stripEndQuotes;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link RecordParser}.
 */
public class RecordParserTest extends AbstractTest {

    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String SPACE = " ";
    private static final String EMPTY = "";

    private static final String TEST_STRING_ONE = "one,two,three,four";
    private static final String EXPECTED_ONE[] = {"one", "two", "three", "four"};

    private static final String MISSING_VALUES = "one:::four";
    private static final String EXPECTED_MV[] = {"one", SPACE, SPACE, "four"};

    private static final String THREE_MISSING = "::";
    private static final String EXPECTED_THREE[] = {SPACE, SPACE, SPACE};

    private RecordParser parser;

    @Test
    public void basic() {
        title("basic");

        parser = new RecordParser(TEST_STRING_ONE, COMMA);
        print(parser);
        assertEquals("unex field count", 4, parser.totalFields());

        for (int i = 0; i < parser.totalFields(); i++) {
            String f = parser.getField(i);
            print("field %d >%s<", i, parser.getField(i));
            assertEquals("wrong val", EXPECTED_ONE[i], f);
        }
    }

    @Test
    public void stripQuotes() {
        title("stripQuotes");
        assertEquals("badquotes1", "hello", stripEndQuotes("\"hello\""));
        assertEquals("badquotes2", "\"hello", stripEndQuotes("\"hello"));
        assertEquals("badquotes2", "hello\"", stripEndQuotes("hello\""));
    }

    @Test
    public void outOfBounds() {
        title("outOfBounds");
        parser = new RecordParser(TEST_STRING_ONE, COMMA);
        assertEquals("not empty", EMPTY, parser.getField(-1));
        assertEquals("not empty", EMPTY, parser.getField(EXPECTED_ONE.length));
    }

    @Test
    public void missingValues() {
        title("missingValues");
        parser = new RecordParser(MISSING_VALUES, COLON);
        assertEquals("wrong field count", 4, parser.totalFields());

        for (int i = 0; i < parser.totalFields(); i++) {
            String f = parser.getField(i);
            print("field %d >%s<", i, parser.getField(i));
            assertEquals("wrong val", EXPECTED_MV[i], f);
        }
    }

    @Test
    public void missingThree() {
        title("missingThree");
        parser = new RecordParser(THREE_MISSING, COLON);
        assertEquals("wrong field count", 3, parser.totalFields());

        for (int i = 0; i < parser.totalFields(); i++) {
            String f = parser.getField(i);
            print("field %d >%s<", i, parser.getField(i));
            assertEquals("wrong val", EXPECTED_THREE[i], f);
        }

    }
}
