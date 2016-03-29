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

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for (@link ResourceFinder}.
 */
public class ResourceFinderTest extends AbstractFileTest {

    private static final String RESOURCE_ROOT = "test/testres/resource_finder/";
    private static final String ONE = "one.txt";
    private static final String ALPHA = "alpha.txt";
    private static final String ABSENT = "absent.file";

    private static final String PATH_ONE = RESOURCE_ROOT + ONE;
    private static final String PATH_ALPHA = RESOURCE_ROOT + ALPHA;
    private static final String PATH_ABSENT = RESOURCE_ROOT + ABSENT;

    private static final int ASCII_HASH = 35; // '#'

    @BeforeClass
    public static void beforeClass() {
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
        pwd = pwd + RESOURCE_ROOT;
        print("PWD is now >%s<", pwd);
    }

    @Test
    public void exists() {
        title("exists");
        assertEquals("missing file", true, ResourceFinder.exists(PATH_ONE));
        assertEquals("what?", false, ResourceFinder.exists(PATH_ABSENT));
    }

    @Test
    public void nullStream() {
        title("nullStream");
        InputStream is = ResourceFinder.getInputStream(ABSENT);
        assertEquals("found stream?", null, is);
    }

    @Test
    public void oneStream() throws IOException {
        title("oneStream");
        InputStream is = ResourceFinder.getInputStream(PATH_ONE);
        assertNotNull("no stream?", is);
        int b = is.read();
        assertEquals("wrong byte", ASCII_HASH, b);
        is.close();
    }

    @Test
    public void oneString() {
        title("oneString");
        String s = ResourceFinder.getString(PATH_ONE);
        assertNotNull("no string", s);
        print(s);
        assertTrue("wrong first line", s.startsWith("# no ending blank line"));
    }

    @Test
    public void alphaString() {
        title("alphaString");
        String s = ResourceFinder.getString(PATH_ALPHA);
        assertNotNull("no string", s);
        print(s);
        assertTrue("wrong first line", s.startsWith("# ends with a blank line"));
    }

    @Test
    public void noString() {
        title("noString");
        String s = ResourceFinder.getString(PATH_ABSENT);
        assertNull("not empty", s);
    }
}
