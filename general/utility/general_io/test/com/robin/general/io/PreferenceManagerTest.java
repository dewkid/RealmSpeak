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
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PreferenceManager}.
 */
public class PreferenceManagerTest extends AbstractFileTest {

    private static final String TEST_ROOT = "test/testres/pref_manager/";
    private static final String PREFS_DIR = "prefs_dir";

    private static final String PREFS_FILE = "unitTest.prefs";

    private static final String K_FOO = "foo";
    private static final String K_BAR = "bar";
    private static final String K_BAZ = "baz";
    private static final String K_GOO = "goo";
    private static final String K_ZOO = "zoo";

    private static final String V_STR = "~VaLuE~";
    private static final int V_INT = 42;
    private static final double V_DOUBLE = 3.1416;

    private static String prefsDir;

    private PreferenceManager pm;
    private Properties props;


    @BeforeClass
    public static void beforeClass() {
        title("beforeClass");
        AbstractFileTest.beforeClass();
        print("PWD is >%s<", pwd);
        pwd = pwd + TEST_ROOT;
        print("PWD now set to >%s<", pwd);
        relativeFromHome();
        print("fromHome is now >%s<", fromHome);

        // set up prefs directory relative path from users home dir..
        prefsDir = fromHome + PREFS_DIR;
        print("prefs-dir now set to >%s<", prefsDir);
    }

    @Before
    public void setUp() {
        // always start with an empty prefs directory
        File pdir = new File(pwd + PREFS_DIR);
        if (pdir.isDirectory()) {
            for (File file : pdir.listFiles()) {
                file.delete();
            }
        } else {
            print("!!! Prefs Dir not setup: %s", pdir);
        }
        assertEquals("files not cleaned up", 0, pdir.listFiles().length);
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

    @Test
    public void basic() {
        title("basic");
        initPm();

        // TODO: consider PreferenceManager.toString()
        print(pm);
        print(props);
        assertEquals("props not empty", 0, props.size());
    }

    @Test
    public void addValues() {
        title("addValues");
        initPm();
        setBaseProps();

        print(props);
        assertEquals("wrong count", 3, props.size());
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
        assertEquals("props not empty", 0, props.size());

        pm.loadPreferences();
        props = pm.getProperties();
        print(props);
        assertEquals("no props", 3, props.size());
        assertBaseProps();
    }

    // TODO: complete coverage...
}
