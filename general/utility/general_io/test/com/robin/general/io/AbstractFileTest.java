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
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Base test class for file tests. Provides utility methods for setting
 * up current working directory and other stuff.
 */
public class AbstractFileTest extends AbstractTest {

    private static final String NOT_A_DIR = "!!! Not a directory: ";
    private static final String USER_HOME = "user.home";

    protected static String pwd;
    protected static String fromHome;

    @BeforeClass
    public static void beforeClass() {
        pwd = new File(".").getAbsolutePath().replaceFirst("\\.", "");
    }

    /**
     * Sets {@link #fromHome} to be {@link #pwd} as a relative path from
     * the user's home directory.
     */
    protected static void relativeFromHome() {
        String userHome = System.getProperty(USER_HOME);
        if (pwd.startsWith(userHome)) {
            int len = userHome.length() + 1;
            fromHome = pwd.substring(len);
        }
    }

    /**
     * Attempts to read lines from the specified file. Returns the file
     * contents as a list of strings. An empty file will return an empty list.
     * If the file does not exist or is otherwise not readable, null will be
     * returned instead.
     *
     * @param file the file
     * @return the contents of the file as a list of lines
     */
    protected static List<String> linesFromFile(File file) {
        return linesFromFile(file.getAbsolutePath());
    }

    /**
     * Attempts to read lines from the specified file. Returns the file
     * contents as a list of strings. An empty file will return an empty list.
     * If the file does not exist or is otherwise not readable, null will be
     * returned instead.
     *
     * @param path the file path
     * @return the contents of the file as a list of lines
     */
    protected static List<String> linesFromFile(String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ensures that the specified directory exists.
     *
     * @param dir the directory
     */
    protected static void ensureDirectory(File dir) {
        if (dir.isDirectory()) {
            return; // our job is done
        }
        try {
            if (!dir.mkdir()) {
                fail("Failed to create directory: " + dir);
            }
        } catch (Exception e) {
            fail("Failed to create directory: " + dir);
        }
    }

    private static void checkIsDirectory(File dir) {
        if (!dir.isDirectory()) {
            fail(NOT_A_DIR + dir);
        }
    }

    /**
     * Deletes all files in the given directory (not recursive).
     * Typically to clean up test output directories.
     *
     * @param dir the directory to clean
     */
    protected static void deleteFilesFromDirectory(File dir) {
        checkIsDirectory(dir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        assertEquals("files not cleaned up", 0, fileCount(dir));
    }

    /**
     * Returns the number of files in the given directory.
     *
     * @param dir the directory
     * @return number of files in it
     */
    protected static int fileCount(File dir) {
        checkIsDirectory(dir);
        File[] files = dir.listFiles();
        return files == null ? 0 : files.length;
    }


    /**
     * Asserts that the given directory contains files with the given names.
     *
     * @param dir       the directory to examine
     * @param fileNames the list of file names expected to reside there
     */
    protected static void assertContains(File dir, String... fileNames) {
        checkIsDirectory(dir);
        if (fileNames.length < 1) {
            fail("no expected file names supplied");
        }

        File[] files = dir.listFiles();
        if (files == null) {
            fail("No files in directory!");
        }

        Set<String> expected = new HashSet<>(Arrays.asList(fileNames));
        for (File f : files) {
            expected.remove(f.getName());
        }
        if (!expected.isEmpty()) {
            fail("missing these files: " + expected);
        }
    }
}
