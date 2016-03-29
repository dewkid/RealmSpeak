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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ZipUtilities}.
 */
public class ZipUtilitiesTest extends AbstractFileTest {

    private static final String RESOURCE_ROOT = "test/testres/zip_utilities/";
    private static final File OUT_DIR = new File(RESOURCE_ROOT, "out/");

    private static final String ONE_TXT = "one.txt";
    private static final String TWO_TXT = "two.txt";
    private static final String DOCUMENT_GIF = "document.gif";


    private static final File FILE_ONE = new File(RESOURCE_ROOT, ONE_TXT);
    private static final File FILE_TWO = new File(RESOURCE_ROOT, TWO_TXT);
    private static final File FILE_DOC = new File(RESOURCE_ROOT, DOCUMENT_GIF);

    private static final File[] TO_ZIP = {FILE_ONE, FILE_TWO, FILE_DOC};

    private static final File ZIP_FILE = new File(OUT_DIR, "zippity.zip");


    @BeforeClass
    public static void beforeClass() {
        title("beforeClass");
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
        pwd = pwd + RESOURCE_ROOT;
        print("PWD is now >%s<", pwd);
        ensureDirectory(OUT_DIR);
    }

    @Before
    public void setUp() {
        // always start with an empty output directory
        deleteFilesFromDirectory(OUT_DIR);
    }

    @Test
    public void zippity() {
        title("zippity");
        assertFalse("zip file exists at start", ZIP_FILE.exists());

        ZipUtilities.zip(ZIP_FILE, TO_ZIP);
        assertTrue("no zip file exists", ZIP_FILE.exists());

        assertEquals("extra files?", 1, fileCount(OUT_DIR));

        File[] unzipped = ZipUtilities.unzip(ZIP_FILE);
        assertEquals("missing files?", 4, fileCount(OUT_DIR));
        assertContains(OUT_DIR, ONE_TXT, TWO_TXT, DOCUMENT_GIF);
        assertEquals("unexpected return count", 3, unzipped.length);
        print("returned files...");
        for (File f : unzipped) {
            print(" > %s", f);
        }
    }
}
