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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link LoggingFormatter}.
 */
public class LoggingFormatterTest extends AbstractTest {

    private static final String E_LOG = "unexpected log message";

    private static final String MSG = "Some Message, Yo!";
    private static final String LOGGER_NAME = "John";
    private static final String METHOD_NAME = "testingMethod";

    private static final Throwable THROWABLE =
            new RuntimeException("Oops");

    private LoggingFormatter lf;
    private String s;

    enum Config {
        DEFAULT,
        LOG_NAME,
        CLASS_NAME,
        METHOD_NAME,
        THROWABLE
    }

    @Before
    public void setUp() {
        lf = new LoggingFormatter();
    }

    private LogRecord fakeLogRecord(Config config) {
        LogRecord rec = new LogRecord(Level.INFO, MSG);
        switch (config) {

            case DEFAULT:
                break;

            case LOG_NAME:
                rec.setLoggerName(LOGGER_NAME);
                break;

            case CLASS_NAME:
                rec.setSourceClassName(getClass().getName());
                break;

            case METHOD_NAME:
                rec.setSourceClassName(getClass().getName());
                rec.setSourceMethodName(METHOD_NAME);
                break;

            case THROWABLE:
                rec.setLoggerName(LOGGER_NAME);
                rec.setThrown(THROWABLE);
                break;

            default:
                Assert.fail("unknown: " + config);
        }

        return rec;
    }

    @Test
    public void basic() {
        title("basic");
        s = lf.format(fakeLogRecord(Config.DEFAULT));
        print(s);
        assertEquals(E_LOG, "INFO\tnull\tSome Message, Yo!\n", s);
    }

    @Test
    public void logName() {
        title("logName");
        s = lf.format(fakeLogRecord(Config.LOG_NAME));
        print(s);
        assertEquals(E_LOG, "INFO\tJohn\tSome Message, Yo!\n", s);
    }

    @Test
    public void className() {
        title("className");
        s = lf.format(fakeLogRecord(Config.CLASS_NAME));
        print(s);
        assertEquals(E_LOG, "INFO\tLoggingFormatterTest\tSome Message, Yo!\n", s);
    }

    @Test
    public void methodName() {
        title("methodName");
        s = lf.format(fakeLogRecord(Config.METHOD_NAME));
        print(s);
        assertEquals(E_LOG, "INFO\tLoggingFormatterTest.testingMethod(*)\tSome Message, Yo!\n", s);
    }

    @Test
    public void throwable() {
        title("throwable");
        s = lf.format(fakeLogRecord(Config.THROWABLE));
        print(s);
        assertTrue(E_LOG, s.startsWith("INFO\tJohn\tSome Message, Yo!\t"));
        assertTrue(E_LOG, s.contains("java.lang.RuntimeException"));
        assertTrue(E_LOG, s.contains("com.robin.general.io.LoggingFormatterTest.<clinit>"));
    }

}
