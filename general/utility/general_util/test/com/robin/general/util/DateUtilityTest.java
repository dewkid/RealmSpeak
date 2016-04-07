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

package com.robin.general.util;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link DateUtility}.
 */
public class DateUtilityTest extends AbstractTest {

    private static final Date XMAS_2000 = setXmas2010();

    private static Date setXmas2010() {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.set(2000, Calendar.DECEMBER, 25, 15, 1, 0);
        // make sure milliseconds are zeroed out...
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Test
    public void now() {
        // NOTE: difficult to unit test real-time-based functions without
        //       mocking the time source.
        title("now");
        Date now = DateUtility.getNow();
        print("now as a date: >%s<", now);
        print("now formatted: >%s<", DateUtility.convertDate2String(now));
    }

    @Test
    public void conversion() {
        title("conversion");
        String s = DateUtility.convertDate2String(XMAS_2000);
        print("xmas string: >%s<", s);
        assertEquals("wrong xms string", "12.25.2000 15:01:00", s);

        Date x = DateUtility.convertString2Date(s);
        print("xmas date  : >%s<", x);
        print("EXP: %d", XMAS_2000.getTime());
        print("ACT: %d", x.getTime());
        assertEquals("wrong xms date", XMAS_2000, x);
    }

    @Test
    public void badDate() {
        title("badDate");
        assertNull("what?", DateUtility.convertString2Date("Foo"));
    }
}
