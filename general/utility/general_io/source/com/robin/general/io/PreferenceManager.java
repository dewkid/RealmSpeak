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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * Manages user preferences.
 */
public class PreferenceManager {

    private String header =
            "Preference file created by com.robin.general.io.PreferenceManager";
    private String prefDirName;
    private String prefFileName;
    private Properties preferences;
    private File prefDir = null;
    private File prefFile = null;

    /**
     * Creates a preference manager that persists preference data to a file in
     * a subdirectory of the user's home directory. Note that no extension is
     * added to the preference filename, so add one yourself if you need one.
     *
     * @param dirName  the name of the preferences directory
     * @param fileName the preferences file name
     */
    public PreferenceManager(String dirName, String fileName) {
        this.prefDirName = dirName;
        this.prefFileName = fileName;
        preferences = new Properties();
    }

    /**
     * Returns the preferences properties object of this preference manager.
     *
     * @return the preferences
     */
    public Properties getProperties() {
        return preferences;
    }

    /**
     * Sets the header for this preferences manager.
     *
     * @param val the header value
     */
    public void setHeader(String val) {
        if (val != null && val.trim().length() > 0) {
            header = val;
        }
    }

    // returns the preferences directory
    private File getPrefDir() {
        if (prefDir == null) {
            prefDir = createPrefDirFile(prefDirName);
        }
        return prefDir;
    }

    // returns the file, either local or from user home prefs directory
    private File getFile(String filename) {
        // First check locally
        File file = new File(filename);
        if (!file.exists()) {
            // Not local?  Use user home.
            if (getPrefDir() != null) {
                file = new File(getPrefDir().getPath() + File.separator + filename);
            }
        }
        return file;
    }

    /**
     * Returns the preferences file.
     *
     * @return the preferences file
     */
    public File getPrefFile() {
        if (prefFile == null) {
            prefFile = getFile(prefFileName);
        }
        return prefFile;
    }

    /**
     * Returns true if the preferences file exists and can be loaded.
     *
     * @return true, if the preferences can be loaded
     */
    public boolean canLoad() {
        return getPrefFile().exists();
    }

    /**
     * Loads the preferences. If no preference file currently exists, a new
     * one will be created with default values for all preferences.
     */
    public void loadPreferences() {
        if (canLoad()) {
            try {
                preferences = new Properties();
                FileInputStream stream = new FileInputStream(getPrefFile());
                preferences.load(stream);
                stream.close();
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
        } else {
            preferences = new Properties();
            createDefaultPreferences(preferences);
        }
    }

    /**
     * Attempts to persist the current preferences to the preferences file
     * on disk.
     */
    public void savePreferences() {
        try {
            FileOutputStream stream = new FileOutputStream(getPrefFile());
            preferences.store(stream, "# " + header);
            stream.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    /**
     * Attempts to remove the persisted preferences file from disk.
     *
     * @return true, if the file was deleted
     */
    public boolean erasePreferences() {
        boolean ret = false;
        try {
            ret = getPrefFile().delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    /**
     * Creates the default preferences. This default implementation does
     * nothing. Subclasses should override this method to populate the given
     * properties object with default values.
     *
     * @param props the properties to populate with default values
     */
    protected void createDefaultPreferences(Properties props) {
        /** this implementation does nothing */
    }

    /**
     * Returns the current value for the preference with the given key. This
     * method will return null if no such preference exists.
     *
     * @param key preference key
     * @return current value for that preference
     */
    public String get(String key) {
        return preferences.getProperty(key);
    }

    /**
     * Returns the current value for the preference with the given key. If
     * no such value exists, the supplied default value will be returned.
     *
     * @param key          preference key
     * @param defaultValue value to return if no such preference found
     * @return current value, or default value
     */
    public String get(String key, String defaultValue) {
        return preferences.getProperty(key, defaultValue);
    }

    /**
     * Adds an item to the list (erasing all previously remembered items).
     *
     * @param key the list key
     * @param val the value to add to the list
     * @see #addListItem(String, String, int)
     * @see #getList(String)
     */
    public void addListItem(String key, String val) {
        addListItem(key, val, 0);
    }

    /**
     * Adds an item to the list with the given key, remembering no more than
     * the specified buffer size. A buffer size of less than 1 will be
     * interpreted as 1 (erasing all previously remembered items).
     *
     * @param key        the preference key
     * @param val        the preference initial value
     * @param bufferSize the maximum number of values allowed in the list
     * @see #addListItem(String, String)
     * @see #getList(String)
     */
    public void addListItem(String key, String val, int bufferSize) {
        if (bufferSize < 1) bufferSize = 1;
        ArrayList<String> list = getList(key);
        while (list.remove(val)) ;
        list.add(0, val);
        while (bufferSize > 0 && list.size() > bufferSize) {
            list.remove(list.size() - 1);
        }
        int n = 0;
        for (String item : list) {
            preferences.setProperty(key + n, item);
            n++;
        }
        while (true) {
            if (preferences.getProperty(key + n) != null) {
                preferences.remove(key + n);
            } else {
                break;
            }
            n++;
        }
    }

    /**
     * Returns the list of items associated with the given key. Note that the
     * items are returned in LIFO order; that is, the last item to be added to
     * the list will be at index 0.
     *
     * @param key the list key
     * @return the items in the list (in LIFO order)
     * @see #addListItem(String, String)
     * @see #addListItem(String, String, int)
     */
    public ArrayList<String> getList(String key) {
        ArrayList<String> list = new ArrayList<String>();
        int n = 0;
        while (true) {
            String val = preferences.getProperty(key + n);
            if (val == null) {
                break;
            }
            list.add(val);
            n++;
        }
        return list;
    }

    /**
     * Returns the preference for the given key as an integer value.
     * If there is no such preference stored, or if the stored value is not
     * parseable as an integer, 0 is returned.
     *
     * @param key preference key
     * @return the preference value as an integer
     */
    public int getInt(String key) {
        String val = get(key);
        if (val != null) {
            try {
                return Integer.valueOf(val).intValue();
            } catch (NumberFormatException ex) {
                // nothing
            }
        }
        return 0;
    }

    /**
     * Returns the preference for the given key as a double value.
     * If there is no such preference stored, or if the stored value is not
     * parseable as a double, 0.0 is returned.
     *
     * @param key preference key
     * @return the preference value as a double
     */
    public double getDouble(String key) {
        String val = get(key);
        if (val != null) {
            try {
                return Double.valueOf(val).doubleValue();
            } catch (NumberFormatException ex) {
                // nothing
            }
        }
        return 0;
    }

    /**
     * Returns the preference for the given key as a boolean value.
     * If there is no such preference stored, or if the stored value is not
     * parseable as a boolean, false is returned.
     *
     * @param key preference key
     * @return the preference value; true or false
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Returns the preference for the given key as a boolean value. If no
     * such preference is stored, the given default value is returned instead.
     *
     * @param key        preference key
     * @param defaultVal value to return if no such preference
     * @return the preference value; true or false
     */
    public boolean getBoolean(String key, boolean defaultVal) {
        String val = get(key);
        if (val != null) {
            return val.equals("true");
        }
        return defaultVal;
    }

    /**
     * Removes the preference with the given key.
     *
     * @param key key of preference to remove
     */
    public void remove(String key) {
        preferences.remove(key);
    }

    /**
     * Sets the preference with the given key to the specified value.
     *
     * @param key preference key
     * @param val new value to set
     */
    public void set(String key, String val) {
        preferences.setProperty(key, val);
    }

    /**
     * Sets the preference with the given key to the specified integer value.
     *
     * @param key preference key
     * @param val new integer value to set
     */
    public void set(String key, int val) {
        preferences.setProperty(key, "" + val);
    }

    /**
     * Sets the preference with the given key to the specified boolean value.
     *
     * @param key preference key
     * @param val new boolean value to set
     */
    public void set(String key, boolean val) {
        preferences.setProperty(key, val ? "true" : "false");
    }

    /**
     * Clears all preferences; that is, removes them all.
     */
    public void clear() {
        preferences.clear();
    }

    /**
     * Removes all preferences that have keys that begin with the given string.
     *
     * @param start the string to match
     */
    public void clearStartsWith(String start) {
        ArrayList keysToRemove = new ArrayList();
        for (Iterator i = preferences.keySet().iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            if (key.startsWith(start)) {
                keysToRemove.add(key);
            }
        }
        for (Iterator i = keysToRemove.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            preferences.remove(key);
        }
    }

    /**
     * Returns true if the preference with the given key exists and is
     * currently set as "true".
     *
     * @param key preference key
     * @return true if this preference has a value of true
     */
    public boolean isPref(String key) {
        String val = preferences.getProperty(key);
        if (val != null) {
            val = val.toLowerCase();
            return val.equals("true");
        }
        return false;
    }

    /**
     * Returns a list of preference files currently stored in the
     * specified directory.
     *
     * @param dirName directory name
     * @return list of preference files stored there
     */
    public static String[] getPreferenceFilesAt(String dirName) {
        File dir = createPrefDirFile(dirName);
        return dir.list();
    }

    // create if needed, and return the preferences directory
    private static File createPrefDirFile(String dirName) {
        File dir = new File(System.getProperty("user.home") + File.separator + dirName + File.separator);
        if (!dir.exists()) {
            // directory doesn't exist?  create it.
            if (!dir.mkdir()) {
                // problem creating directory?  Let the user know...
                System.out.println("Unable to create preference directory:  " + dir.getPath());
                // ... and just save the preference file locally.
                dir = null;
            }
        }
        return dir;
    }
}