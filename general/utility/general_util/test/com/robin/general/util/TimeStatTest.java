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

/**
 * Unit tests for {@link TimeStat}.
 */
public class TimeStatTest extends AbstractTest {

    // NOTE: not really unit testable without instrumenting the class
    //       with a mock timesource.

    private static final String FOO = "foo";
    private static final String BAR = "bar";

    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void basic() {
        title("basic");

        TimeStat stat = new TimeStat();
        stat.markStartTime(FOO);
        delay(20);
        stat.markStartTime(BAR);
        delay(10);
        stat.markEndTime(BAR);
        delay(5);
        stat.markStartTime(BAR);
        delay(10);
        stat.markEndTime(BAR);
        // hmmm, spending a lot of time in the BAR :)
        stat.markEndTime(FOO);
        print(stat.getAverageSummary());
    }
}
