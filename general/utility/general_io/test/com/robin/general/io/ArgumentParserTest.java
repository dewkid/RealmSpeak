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

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ArgumentParser}.
 */
public class ArgumentParserTest extends AbstractTest {

    private static final String K_FOO = "foo";
    private static final String K_BAR = "bar";
    private static final String V_FIVE = "five";
    private static final String V_NINE = "nine";

    private static final String[] ONE_KEY_VALUE = {"foo=five"};


    private ArgumentParser parser;
    private Properties props;


    private void checkNoErrors(ArgumentParser ap) {
        assertEquals("errors?", false, ap.hasErrors());
        assertEquals("error count?", 0, ap.getErrors().length);
        assertEquals("error string?", "", ap.getErrorString());
    }

    @Test
    public void basic() {
        title("basic");

        parser = new ArgumentParser(null);
//        print(ap);
        // TODO: consider ArgumentParser.toString()

        checkNoErrors(parser);

        assertEquals("foo key", false, parser.hasKey(K_FOO));
        assertEquals("foo prop", null, parser.getValueForKey(K_FOO));

        props = parser.getProperties();
        assertEquals("non-zero props", 0, props.size());
    }

    @Test
    public void oneKeyValue() {
        title("oneKeyValue");

        parser = new ArgumentParser(ONE_KEY_VALUE);
        checkNoErrors(parser);

        assertEquals("foo key", true, parser.hasKey(K_FOO));
        assertEquals("foo prop", V_FIVE, parser.getValueForKey(K_FOO));

        props = parser.getProperties();
        assertEquals("wrong count", 1, props.size());
    }

    // TODO: complete coverage of the class
}
