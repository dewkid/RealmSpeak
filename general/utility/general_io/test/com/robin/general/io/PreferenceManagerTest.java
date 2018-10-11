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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PreferenceManager}.
 */
public class PreferenceManagerTest extends AbstractFileTest {

    private static final String DEFAULT_HEADER =
            "Preference file created by com.robin.general.io.PreferenceManager";
    private static final String ALT_HEADER = "Some other header";
    private static final String HASHES = "## ";

    private static final String TEST_ROOT = "test/testres/pref_manager/";
    private static final String PREFS_DIR = "prefs_dir";

    private static final String PREFS_FILE = "unitTest.prefs";
    private static final String PFILE_1 = "pf1.prefs";
    private static final String PFILE_2 = "pf2.prefs";
    private static final String PFILE_3 = "pf3.prefs";

    private static final String K_FOO = "foo";
    private static final String K_BAR = "bar";
    private static final String K_BAZ = "baz";
    private static final String K_GOO = "goo";
    private static final String K_ZOO = "zoo";
    private static final String K_LIST = "mylist";

    private static final String V_STR = "~VaLuE~";
    private static final int V_INT = 42;
    private static final String V_TRUE = "true";
    private static final String V_NOT_STORED = "not-stored";
    private static final double V_DOUBLE = 2.71828;

    private static String prefsDir;
    private static String absPrefsDir;

    private PreferenceManager pm;
    private Properties props;


    // test preferences class that provides its own defaults.
    private static class MyTestPreferences extends PreferenceManager {

        public MyTestPreferences(String dirName, String fileName) {
            super(dirName, fileName);
        }

        @Override
        protected void createDefaultPreferences(Properties props) {
            props.setProperty(K_FOO, V_STR);
            props.setProperty(K_BAR, Integer.toString(V_INT));
            props.setProperty(K_BAZ, V_TRUE);
        }
    }


    @BeforeClass
    public static void beforeClass() {
        title("beforeClass");
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
        pwd = pwd + TEST_ROOT;
        print("PWD now set to >%s<", pwd);
        relativeFromHome();
        print("fromHome is now >%s<", fromHome);

        // set up prefs directory values
        absPrefsDir = pwd + PREFS_DIR;
        prefsDir = fromHome + PREFS_DIR;
        print("abs-prefs-dir now set to >%s<", absPrefsDir);
        print("prefs-dir now set to >%s<", prefsDir);
    }

    @Before
    public void setUp() {
        // always start with an empty prefs directory
        deleteFilesFromDirectory(new File(absPrefsDir));
    }

    private void initPm() {
        pm = new PreferenceManager(prefsDir, PREFS_FILE);
        props = pm.getProperties();
    }

    private void setBaseProps() {
        pm.set(K_FOO, V_STR);
        pm.set(K_BAR, V_INT);
        pm.set(K_BAZ, true);
    }

    private void assertBaseProps() {
        assertEquals("bad string", V_STR, pm.get(K_FOO));
        assertEquals("bad int", V_INT, pm.getInt(K_BAR));
        assertEquals("bad bool", true, pm.getBoolean(K_BAZ));
    }

    private void assertPropCount(int count) {
        assertEquals("unex prop count", count, pm.getProperties().size());

    }

    private String retrieveHeader(File file) {
        List<String> lines = linesFromFile(file);
        if (lines == null) {
            return null;
        }
        if (lines.size() == 0) {
            return "";
        }
        return lines.get(0);
    }

    @Test
    public void basic() {
        title("basic");
        initPm();

        // TODO: consider PreferenceManager.toString()
        print(pm);
        print(props);
        assertPropCount(0);
    }

    @Test
    public void addValues() {
        title("addValues");
        initPm();
        setBaseProps();

        print(props);
        assertPropCount(3);
        assertBaseProps();
    }

    @Test
    public void savePrefs() {
        title("savePrefs");
        initPm();
        setBaseProps();
        pm.savePreferences();

        assertTrue("no file?", pm.canLoad());
    }

    @Test
    public void loadPrefs() {
        title("loadPrefs");
        initPm();
        setBaseProps();
        print(props);
        pm.savePreferences();

        initPm();
        print(props);
        assertPropCount(0);

        pm.loadPreferences();
        props = pm.getProperties();
        print(props);
        assertPropCount(3);
        assertBaseProps();
    }

    @Test
    public void altHeader() {
        // NOTE: generally it is not good practice to test for explicit
        //       internal encodings (like how the preferences are stored)
        //       as that violates encapsulation... still...

        title("altHeader");
        initPm();
        pm.savePreferences();
        String hdr = retrieveHeader(pm.getPrefFile());
        print("header is ... >%s<", hdr);
        assertEquals("unex default hdr", HASHES + DEFAULT_HEADER, hdr);

        pm.setHeader(ALT_HEADER);
        pm.savePreferences();
        hdr = retrieveHeader(pm.getPrefFile());
        print("header is NOW >%s<", hdr);
        assertEquals("unex hdr", HASHES + ALT_HEADER, hdr);
    }

    @Test
    public void erasePrefs() {
        title("erasePrefs");

        initPm();
        setBaseProps();
        assertPropCount(3);
        assertBaseProps();

        pm.erasePreferences();
        assertEquals("still loadable?", false, pm.canLoad());

        // but note that erasePreferences() just deletes the backing file,
        //  it does not change the current in-memory preferences...
        assertPropCount(3);
        assertBaseProps();

        // BUT - if we CLEAR prefs...
        pm.clear();
        assertPropCount(0);
    }

    @Test
    public void clearSomePrefs() {
        title("clearSomePrefs");
        initPm();
        pm.set("foo_one", "f1");
        pm.set("foo_two", "f2");
        pm.set("foo_three", "f3");
        pm.set("bar_one", "b1");
        pm.set("bar_two", "b2");
        pm.set("goo_one", "g1");

        assertPropCount(6);
        assertEquals("missing prop", "f2", pm.get("foo_two"));

        pm.clearStartsWith("foo_");
        assertPropCount(3);
        assertEquals("prop not cleared", null, pm.get("foo_two"));

        pm.clearStartsWith("bar_");
        assertPropCount(1);
    }

    @Test
    public void defaultPrefs() {
        title("defaultPrefs");

        pm = new MyTestPreferences(prefsDir, PREFS_FILE);
        props = pm.getProperties();
        // when first created, we have nothing...
        assertPropCount(0);

        // and we currently have no backing file:
        assertEquals("backing file?", false, pm.canLoad());

        // but loading should populate with defaults...
        pm.loadPreferences();
        assertPropCount(3);
        assertBaseProps();
    }

    @Test
    public void getWithDefault() {
        title("getWithDefault");
        initPm();
        setBaseProps();
        assertBaseProps();

        // one we have...
        String val = pm.get(K_FOO, V_NOT_STORED);
        assertEquals("unex stored val", V_STR, val);

        // one we know we don't have...
        val = pm.get(K_GOO, V_NOT_STORED);
        assertEquals("didn't return default", V_NOT_STORED, val);
    }

    @Test
    public void noInt() {
        title("noInt");
        initPm();

        int val = pm.getInt(K_GOO);
        assertEquals("non-zero default", 0, val);
    }

    @Test
    public void noBooleans() {
        title("noBooleans");
        initPm();
        assertPropCount(0);
        boolean b = pm.getBoolean(K_ZOO);
        assertEquals("wrong default bool", false, b);

        b = pm.getBoolean(K_ZOO, false);
        assertEquals("wrong supplied default bool", false, b);

        b = pm.getBoolean(K_ZOO, true);
        assertEquals("wrong supplied default bool", true, b);
    }

    @Test
    public void badNumbers() {
        title("badNumbers");
        initPm();

        pm.set(K_GOO, V_STR);

        int val = pm.getInt(K_GOO);
        assertEquals("non-zero default", 0, val);

        double doub = pm.getDouble(K_GOO);
        assertEquals("non-zero default", 0.0, doub, TOLERANCE);
    }

    @Test
    public void doubles() {
        title("doubles");
        initPm();
        assertPropCount(0);

        double val = pm.getDouble(K_ZOO);
        assertEquals("not default 0.0", 0.0, val, TOLERANCE);

        // TODO: oopsie! need to implement PreferenceManager.set(double)
//        pm.set(K_ZOO, V_DOUBLE);
//        val = pm.getDouble(K_ZOO);
//        assertEquals("not e", V_DOUBLE, val, TOLERANCE);

        // could set it as a string..
        pm.set(K_ZOO, "2.71828");
        // and read it as a double
        val = pm.getDouble(K_ZOO);
        assertEquals("not e", V_DOUBLE, val, TOLERANCE);
    }

    @Test
    public void isPref() {
        title("isPref");
        initPm();
        setBaseProps();
        pm.savePreferences();

        // for existing properties...
        assertEquals("exp true", true, pm.isPref(K_BAZ));
        assertEquals("exp false", false, pm.isPref(K_FOO));

        // and for no such prop...
        assertEquals("exp false", false, pm.isPref(K_ZOO));
    }

    @Test
    public void removingKeys() {
        title("removingKeys");
        initPm();
        setBaseProps();
        assertPropCount(3);

        pm.remove(K_FOO);
        assertPropCount(2);
        pm.remove(K_BAZ);
        assertPropCount(1);

        int val = pm.getInt(K_BAR);
        assertEquals("unex k_bar", V_INT, val);

        pm.remove(K_BAR);
        assertPropCount(0);
    }

    @Test
    public void boundedList() {
        title("boundedList");
        initPm();
        pm.addListItem(K_LIST, "AAA", 3);
        pm.addListItem(K_LIST, "BBB", 3);
        pm.addListItem(K_LIST, "CCC", 3);
        pm.addListItem(K_LIST, "DDD", 3);

        // not necessary, but we can look at the file...
        pm.savePreferences();

        // remembers the last 3, most recent first
        List<String> values = pm.getList(K_LIST);
        assertEquals("unex size", 3, values.size());
        assertEquals("el 0", "DDD", values.get(0));
        assertEquals("el 1", "CCC", values.get(1));
        assertEquals("el 2", "BBB", values.get(2));
    }

    @Test
    public void noDupsInList() {
        title("noDupsInList");
        initPm();
        pm.addListItem(K_LIST, "AAA", 7);
        pm.addListItem(K_LIST, "BBB", 7);
        pm.addListItem(K_LIST, "CCC", 7);
        pm.addListItem(K_LIST, "DDD", 7);

        // add BBB again
        pm.addListItem(K_LIST, "BBB", 7);

        List<String> values = pm.getList(K_LIST);
        assertEquals("unex size", 4, values.size());
        assertEquals("el 0", "BBB", values.get(0));
        assertEquals("el 1", "DDD", values.get(1));
        assertEquals("el 2", "CCC", values.get(2));
        assertEquals("el 3", "AAA", values.get(3));
    }

    @Test
    public void shorteningList() {
        title("shorteningList");
        initPm();
        pm.addListItem(K_LIST, "AAA", 4);
        pm.addListItem(K_LIST, "BBB", 4);
        pm.addListItem(K_LIST, "CCC", 4);
        pm.addListItem(K_LIST, "DDD", 4);

        List<String> values = pm.getList(K_LIST);
        assertEquals("unex size", 4, values.size());
        assertEquals("el 0", "DDD", values.get(0));
        assertEquals("el 3", "AAA", values.get(3));

        // now re-use the list but with a smaller bound...
        pm.addListItem(K_LIST, "EEE", 2);
        values = pm.getList(K_LIST);
        assertEquals("unex size", 2, values.size());
        assertEquals("el 0", "EEE", values.get(0));
        assertEquals("el 1", "DDD", values.get(1));
    }

    /*
     * NOTE: original comment on addListItem(String,String,int) was:
     *
     *   "Set a bufferSize of 0 if you don't want to limit the list."
     *
     * But this is not how it is implemented; first line of method:
     *
     *    if (bufferSize < 1) bufferSize = 1;
     *
     * So behaviour is to erase all previously remembered items.
     */
    @Test
    public void forgettingItems() {
        title("forgettingItems");
        initPm();
        pm.addListItem(K_LIST, "AAA", 3);
        pm.addListItem(K_LIST, "BBB", 3);
        pm.addListItem(K_LIST, "CCC", 3);

        List<String> values = pm.getList(K_LIST);
        assertEquals("unex size", 3, values.size());
        assertEquals("el 0", "CCC", values.get(0));
        assertEquals("el 1", "BBB", values.get(1));
        assertEquals("el 2", "AAA", values.get(2));

        // now remember only one item
        pm.addListItem(K_LIST, "DDD");
        values = pm.getList(K_LIST);
        assertEquals("unex size", 1, values.size());
        assertEquals("el 0", "DDD", values.get(0));
    }

    @Test
    public void severalPrefsFiles() {
        title("severalPrefsFiles");
        PreferenceManager pm1 = new PreferenceManager(prefsDir, PFILE_1);
        PreferenceManager pm2 = new PreferenceManager(prefsDir, PFILE_2);
        PreferenceManager pm3 = new PreferenceManager(prefsDir, PFILE_3);

        // not yet saved to disk
        String[] filenames = PreferenceManager.getPreferenceFilesAt(prefsDir);
        print(Arrays.toString(filenames));
        assertEquals("not empty dir", 0, filenames.length);

        // persist
        pm1.savePreferences();
        pm2.savePreferences();
        pm3.savePreferences();

        // list names
        filenames = PreferenceManager.getPreferenceFilesAt(prefsDir);
        print(Arrays.toString(filenames));
        assertEquals("unex count", 3, filenames.length);
        assertEquals("arr 0", PFILE_1, filenames[0]);
        assertEquals("arr 1", PFILE_2, filenames[1]);
        assertEquals("arr 2", PFILE_3, filenames[2]);
    }
}
