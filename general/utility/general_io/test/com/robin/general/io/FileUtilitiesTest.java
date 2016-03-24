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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.robin.general.io.FileUtilities.fixFileExtension;
import static com.robin.general.io.FileUtilities.getFilePathString;
import static com.robin.general.io.FileUtilities.getFilename;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link FileUtilities}.
 */
public class FileUtilitiesTest extends AbstractFileTest {

    private static final File LONDON = new File("world/uk/london.gbr");
    private static final File PARIS = new File("/world/europe/france/paris.fra");
    private static final File MILAN = new File("/world/europe/italy/milan.ita");


    @BeforeClass
    public static void beforeClass() {
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
    }

    @Test
    public void basic() {
        title("basic");
        // re-implements the original main() method
        print("path=" + getFilePathString(new File(""), false, false));
    }

    @Test
    public void nameWithoutExtension() {
        title("nameWithoutExtension");

        String name = getFilename(LONDON, false);
        print(">%s<", name);
        assertEquals("not london", "london", name);

        name = getFilename(PARIS, false);
        print(">%s<", name);
        assertEquals("not paris", "paris", name);
    }

    @Test
    public void nameWithExtension() {
        title("nameWithExtension");

        String name = getFilename(LONDON, true);
        print(">%s<", name);
        assertEquals("not london", "london.gbr", name);

        name = getFilename(PARIS, true);
        print(">%s<", name);
        assertEquals("not paris", "paris.fra", name);
    }

    @Test
    public void filePathBadArgs() {
        title("filePathBadArgs");

        try {
            getFilePathString(LONDON, false, true);
            Assert.fail("No IAE thrown");
        } catch (IllegalArgumentException iae) {
            print(iae);
        }
    }

    @Test
    public void filePathOnly() {
        title("filePathOnly");

        String path = getFilePathString(LONDON, false, false);
        print(path);
        assertEquals("not london path", pwd + "world/uk/", path);

        path = getFilePathString(PARIS, false, false);
        print(path);
        assertEquals("not paris path", "/world/europe/france/", path);
    }

    @Test
    public void filePathWithName() {
        title("filePathWithName");

        String path = getFilePathString(LONDON, true, false);
        print(path);
        assertEquals("not london path", pwd + "world/uk/london", path);

        path = getFilePathString(PARIS, true, false);
        print(path);
        assertEquals("not paris path", "/world/europe/france/paris", path);
    }

    @Test
    public void filePathWithNameAndExt() {
        title("filePathWithNameAndExt");

        String path = getFilePathString(LONDON, true, true);
        print(path);
        assertEquals("not london path", pwd + "world/uk/london.gbr", path);

        path = getFilePathString(PARIS, true, true);
        print(path);
        assertEquals("not paris path", "/world/europe/france/paris.fra", path);
    }

    @Test
    public void fixFileExtSame() {
        title("fixFileExtSame");

        // if the file extension matches, go with it
        File f = fixFileExtension(LONDON, "gbr");
        String actPath = f.getPath();
        print(actPath);
        assertEquals("wrong ext", "world/uk/london.gbr", actPath);
    }

    @Test
    public void fixFileExtDiff() {
        title("fixFileExtDiff");

        // if the file extension does not match, append it
        File f = fixFileExtension(LONDON, "foo");
        String actPath = f.getPath();
        print(actPath);
        assertEquals("wrong ext", "world/uk/london.gbr.foo", actPath);
    }

    @Test
    public void fixFileExtToLower() {
        title("fixFileExtToLower");

        // make sure the file extension is converted to lower case
        File f = fixFileExtension(MILAN, "ITA");
        String actPath = f.getPath();
        print(actPath);
        assertEquals("wrong ext", "/world/europe/italy/milan.ita", actPath);
    }

    @Test
    public void trailingSlash() {
        title("trailingSlash");

        File orig = new File("/foo/bar/");
        File f = fixFileExtension(orig, ".baz");
        String actPath = f.getPath();
        print(actPath);
        assertEquals("wrong path", "/foo/bar.baz", actPath);
    }

    @Test
    public void trailingDot() {
        title("trailingDot");

        File orig = new File("/foo/bar/baz.");
        File f = fixFileExtension(orig, "goo");
        String actPath = f.getPath();
        print(actPath);
        assertEquals("wrong path", "/foo/bar/baz.goo", actPath);
    }

}
