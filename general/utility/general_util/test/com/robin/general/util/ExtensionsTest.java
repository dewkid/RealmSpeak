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

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Extensions}.
 */
public class ExtensionsTest extends AbstractTest {

    private enum StarWars {LUKE, LEIA, R2D2, C3PO}

    private static final int B0 = 0b00001;
    private static final int B1 = 0b00010;
    private static final int B2 = 0b00100;
    private static final int B3 = 0b01000;
    private static final int B4 = 0b10000;

    private static final int V17 = 0b10001;
    private static final int V10 = 0b01010;

    private Optional<StarWars> opt;

    @Test
    public void coalesceTwoEmpties() {
        assertEquals("not null", null,
                Extensions.coalesce((StarWars) null, null));
    }

    @Test
    public void coalesceNullThenObject() {
        assertEquals("not Leia", StarWars.LEIA,
                Extensions.coalesce(null, StarWars.LEIA));
    }

    @Test
    public void coalesceObjectThenNull() {
        assertEquals("not Luke", StarWars.LUKE,
                Extensions.coalesce(StarWars.LUKE, null));
    }

    @Test
    public void coalesceTwoObjects() {
        assertEquals("not Luke", StarWars.LUKE,
                Extensions.coalesce(StarWars.LUKE, StarWars.LEIA));
    }

    @Test
    public void optionalCoalesceTwoNulls() {
        opt = Extensions.coalesce(Optional.empty(), Optional.empty());
        assertEquals("value?", false, opt.isPresent());
    }

    @Test
    public void optionalCoalesceEmptyThenObject() {
        Optional<StarWars> r2d2 = Optional.of(StarWars.R2D2);
        opt = Extensions.coalesce(Optional.empty(), r2d2);
        assertEquals("no value", true, opt.isPresent());
        assertEquals("not R2?", StarWars.R2D2, opt.get());
    }

    @Test
    public void optionalCoalesceObjectThenEmpty() {
        Optional<StarWars> c3po = Optional.of(StarWars.C3PO);
        opt = Extensions.coalesce(c3po, Optional.empty());
        assertEquals("no value", true, opt.isPresent());
        assertEquals("not 3PO?", StarWars.C3PO, opt.get());
    }

    @Test
    public void optionalCoalesceTwoObjects() {
        Optional<StarWars> c3po = Optional.of(StarWars.C3PO);
        Optional<StarWars> r2d2 = Optional.of(StarWars.R2D2);
        opt = Extensions.coalesce(c3po, r2d2);
        assertEquals("no value", true, opt.isPresent());
        assertEquals("not 3PO?", StarWars.C3PO, opt.get());
    }

    @Test
    public void flags() {
        assertEquals("b0 in 17", true, Extensions.hasFlag(V17, B0));
        assertEquals("b1 in 17", false, Extensions.hasFlag(V17, B1));
        assertEquals("b2 in 17", false, Extensions.hasFlag(V17, B2));
        assertEquals("b3 in 17", false, Extensions.hasFlag(V17, B3));
        assertEquals("b4 in 17", true, Extensions.hasFlag(V17, B4));

        assertEquals("b0 in 10", false, Extensions.hasFlag(V10, B0));
        assertEquals("b1 in 10", true, Extensions.hasFlag(V10, B1));
        assertEquals("b2 in 10", false, Extensions.hasFlag(V10, B2));
        assertEquals("b3 in 10", true, Extensions.hasFlag(V10, B3));
        assertEquals("b4 in 10", false, Extensions.hasFlag(V10, B4));
    }
}
