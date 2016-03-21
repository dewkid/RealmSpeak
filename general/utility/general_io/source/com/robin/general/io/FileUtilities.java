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

import java.io.*;

/**
 * Provides convenient methods for manipulating file names.
 */
public class FileUtilities {
    private static final int PATH = 0;
    private static final int FILENAME = 1;
    private static final int EXTENSION = 2;

    /**
     * Returns pieces of the filename in a string array; path in element 0,
     * base filename in element 1, and file extension in element 2.
     *
     * @param file the file to parse
     * @return array of filename pieces
     */
    private static String[] _splitPathName(File file) {
        String totalPath = file.getAbsolutePath();
        int sIndex = totalPath.lastIndexOf(File.separator);
        if (sIndex >= 0) {
            String[] piece = new String[3]; // path - filename - ext
            String pathOnly = totalPath.substring(0, sIndex) + File.separator;
            piece[PATH] = pathOnly;
            String filename = totalPath.substring(sIndex + 1);
            int eIndex = filename.lastIndexOf(".");
            piece[FILENAME] = eIndex < 0 ? filename : filename.substring(0, eIndex);
            piece[EXTENSION] = eIndex < 0 ? "" : filename.substring(eIndex);
            return piece;
        }
        return null;
    }

    /**
     * Returns the filename from a file, possibly with an extension.
     *
     * @param file          the file
     * @param withExtension indicates whether the extension should be included
     * @return the filename as a string
     */
    public static String getFilename(File file, boolean withExtension) {
        String[] piece = _splitPathName(file);
        if (withExtension) {
            return piece[FILENAME] + piece[EXTENSION];
        }
        return piece[FILENAME];
    }

    /**
     * Returns the file path as a string, possibly with a filename, and
     * possibly with the extension.
     *
     * @param file          the file
     * @param withFilename  include the filename
     * @param withExtension include the extension (withFilename must be true)
     */
    public static String getFilePathString(File file, boolean withFilename,
                                           boolean withExtension) {
        if (withExtension && !withFilename) {
            throw new IllegalArgumentException("withFilename MUST be true, if withExtension is true");
        }
        String[] piece = _splitPathName(file);

        if (withFilename) {
            if (withExtension) {
                return piece[PATH] + piece[FILENAME] + piece[EXTENSION];
            } else {
                return piece[PATH] + piece[FILENAME];
            }
        } else {
            return piece[PATH];
        }
    }

    /**
     * Makes sure that the given file ends with the given extension. If it
     * does not, the given extension is appended to the file path.
     *
     * @param file      the file
     * @param extension the desired extension
     * @return the file with desired extension
     */
    public static File fixFileExtension(File file, String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        extension = extension.toLowerCase();
        String path = file.getPath();
        while (path.endsWith(File.separator) || path.endsWith(".")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.toLowerCase().endsWith(extension)) {
            path = path + extension;
        }
        return new File(path);
    }
}