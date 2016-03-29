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
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;


// NOTE: be sure and set an environment variable:
//      -Dcom.foo=FINE
// and run these tests, checking for:
//      "Logging for com.foo: FINE"
// written to stdout

/**
 * Unit tests for {@link LoggingHandler}.
 * Well, actually, for the logging system in general.
 */
public class LoggingHandlerTest extends AbstractTest {

    private static final String TEST_CLASS_NAME =
            LoggingHandlerTest.class.getName();

    private Logger testLogger;


    @Before
    public void setUp() {
        LoggingHandler.initLogging();
        testLogger = Logger.getLogger(TEST_CLASS_NAME);
        testLogger.setLevel(Level.FINEST);
    }

    @Test
    public void basic() {
        title("basic");
        testLogger.severe("severe test");
        testLogger.warning("warning test");
        testLogger.info("info test");
        testLogger.fine("fine test");
        testLogger.finer("finer test");
        testLogger.finest("finest test");
    }
}
