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

import java.util.*;

/**
 * This is a class that can be used to turn an array of command
 * line arguments into a {@link Properties} hash.
 * Values like {@code "name=xxxx"} will be entered as key-value pairs.
 * Lone values like {@code "auto"} will be stored as a key with an empty
 * string as a value.
 */
public class ArgumentParser {
    private Properties props;
    private Vector errors;

    /**
     * Constructs an argument parser for the given set of arguments.
     *
     * @param args the arguments
     */
    public ArgumentParser(String[] args) {
        parseArgs(args);
    }

    /**
     * Constructs an argument parser for the given set of arguments,
     * which must include each of the specified required arguments.
     *
     * @param args the arguments
     * @param req  required arguments
     */
    public ArgumentParser(String[] args, String[] req) {
        parseArgs(args);
        validateArgs(req);
    }

    /**
     * Parse the arguments into key/value pairs (properties).
     *
     * @param args the arguments
     */
    private void parseArgs(String[] args) {
        props = new Properties();
        errors = new Vector();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                KeyValuePair kvp = new KeyValuePair(args[i]);
                if (kvp.getKey() != null) {
                    props.put(kvp.getKey(), kvp.getValue());
                } else {
                    addError("Invalid argument:  " + args[i]);
                }
            }
        }
    }

    /**
     * Ensure that the specified required arguments are present.
     *
     * @param req required argument keys
     */
    private void validateArgs(String[] req) {
        for (int i = 0; i < req.length; i++) {
            if (props.get(req[i]) == null) {
                addError("Required argument <" + req[i] + "> is missing.");
            }
        }
    }

    /**
     * Adds the given error string to our collection of errors.
     *
     * @param error the error string
     */
    public void addError(String error) {
        errors.addElement(error);
    }

    /**
     * Returns our collection of errors as an array of strings.
     *
     * @return the error strings
     */
    public String[] getErrors() {
        return (String[]) errors.toArray(new String[0]);
    }

    /**
     * Returns true if we have any errors.
     *
     * @return true if there are errors
     */
    public boolean hasErrors() {
        return (errors.size() > 0);
    }

    /**
     * Return our collection of errors as a multi-line string.
     *
     * @return the errors in a multi-line string
     */
    public String getErrorString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = errors.iterator(); i.hasNext(); ) {
            sb.append((String) i.next() + "\n");
        }
        return sb.toString();
    }

    /**
     * Return the parsed arguments as properties.
     *
     * @return the argument properties
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * Return the value associated with the given key.
     *
     * @param key the key
     * @return the corresponding value
     */
    public String getValueForKey(String key) {
        return props.getProperty(key);
    }

    /**
     * Returns true if an argument for the given key exists
     *
     * @param key the key
     * @return true if key exists
     */
    public boolean hasKey(String key) {
        return props.containsKey(key);
    }


    /**
     * Inner class encapsulating a key/value pair.
     */
    private class KeyValuePair {
        private String key = null;
        private String value = null;

        /**
         * Constructs a key/value pair by parsing the input string.
         *
         * @param string input string
         */
        public KeyValuePair(String string) {
            if (string != null) {
                StringTokenizer st = new StringTokenizer(string, "=");
                if (st.countTokens() == 1) {
                    key = st.nextToken().trim();
                    value = "";
                } else if (st.countTokens() == 2) {
                    key = st.nextToken().trim();
                    value = st.nextToken().trim();
                }
            }
        }

        /**
         * Returns the key for this key/value pair.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the value for this key/value pair.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }
}