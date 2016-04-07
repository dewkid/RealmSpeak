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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link UniqueArrayList}.
 */
public class UniqueArrayListTest extends AbstractTest {

    enum Coin {PENNY, NICKEL, DIME, QUARTER}

    private UniqueArrayList<Coin> unique;


    @Test
    public void allowingNull() {
        title("allowingNull");
        unique = new UniqueArrayList<>();
        unique.add(Coin.PENNY);
        unique.add(Coin.PENNY);
        unique.add(Coin.NICKEL);
        unique.add(null);
        print(unique);
        assertEquals("unexpected count", 3, unique.size());
        assertTrue("no penny", unique.contains(Coin.PENNY));
        assertTrue("no nickel", unique.contains(Coin.NICKEL));
        assertTrue("no null", unique.contains(null));
    }

    @Test
    public void notAllowingNull() {
        title("notAllowingNull");
        unique = new UniqueArrayList<>(false);
        unique.add(Coin.PENNY);
        unique.add(Coin.PENNY);
        unique.add(Coin.NICKEL);
        unique.add(null);
        print(unique);
        assertEquals("unexpected count", 2, unique.size());
        assertTrue("no penny", unique.contains(Coin.PENNY));
        assertTrue("no nickel", unique.contains(Coin.NICKEL));
        assertFalse("no null", unique.contains(null));
    }
}
