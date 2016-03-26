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
import java.util.List;

/**
 * Base test class for file tests. Provides utility methods for setting
 * up current working directory.
 */
public class AbstractFileTest extends AbstractTest {

    protected static final String USER_HOME = "user.home";

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
}
