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

import java.util.Collection;
import java.util.Collections;

import static com.robin.general.util.CollectionUtility.containsAny;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link CollectionUtility}.
 */
public class CollectionUtilityTest extends AbstractTest {

    private static final String SAY_WHAT = "say, what?!";

    private enum Xoo {BOO, COO, FOO, GOO, MOO, QUEUE, ZOO}

    private static final Collection<Xoo> BASE =
            asList(Xoo.BOO, Xoo.COO, Xoo.FOO, Xoo.GOO, Xoo.MOO, Xoo.ZOO);
    private static final Collection<Xoo> QUERY = asList(Xoo.MOO, Xoo.QUEUE);
    private static final Collection<Xoo> EMPTY = Collections.emptyList();

    @Test
    public void nullCollection() {
        assertFalse(SAY_WHAT, containsAny(null, QUERY));
    }

    @Test
    public void emptyCollection() {
        assertFalse(SAY_WHAT, containsAny(EMPTY, QUERY));
    }

    @Test
    public void nullQuery() {
        assertFalse(SAY_WHAT, containsAny(BASE, null));
    }

    @Test
    public void emptyQuery() {
        assertFalse(SAY_WHAT, containsAny(BASE, EMPTY));
    }

    @Test
    public void partialQuery() {
        assertTrue(SAY_WHAT, containsAny(BASE, QUERY));
    }

    @Test
    public void hasThisOne() {
        assertTrue(SAY_WHAT, containsAny(BASE, asList(Xoo.ZOO)));
    }

    @Test
    public void doesntHaveThisOne() {
        assertFalse(SAY_WHAT, containsAny(BASE, asList(Xoo.QUEUE)));
    }
}
