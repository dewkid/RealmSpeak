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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ModifyableObject}.
 */
public class ModifyableObjectTest extends AbstractTest {

    private static class InstrumentedListener implements ChangeListener {

        private final int id;

        private int modifyCount = 0;
        private int resetCount = 0;

        InstrumentedListener(int id) {
            this.id = id;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            ModifyableObject o = (ModifyableObject) e.getSource();
            if (o.isModified()) {
                modifyCount++;
            } else {
                resetCount++;
            }
        }

        private void validate(int expMods, int expResets) {
            assertEquals("wrong mod count [" + id + "]", expMods, modifyCount);
            assertEquals("wrong reset count [" + id + "]", expResets, resetCount);
        }
    }

    private ModifyableObject mo;
    private ModifyableObject moOther;
    private InstrumentedListener ear1;
    private InstrumentedListener ear2;

    @Before
    public void setUp() {
        // make sure we start at the beginning again.
        ModifyableObject.cum_barcode = 0;
    }

    @Test
    public void basic() {
        title("basic");
        mo = new ModifyableObject();
        print(mo);
        // TODO: could probably do with a toString()

        assertEquals("bad flag", false, mo.isModified());
        assertEquals("listeners", null, mo.changeListeners);
        assertEquals("bad codenum", 0, mo.barcode);
        assertEquals("bad barcode", "BARCODE0", mo.getBarcode());
        assertEquals("bad class codenum", 1, ModifyableObject.cum_barcode);
    }

    @Test
    public void twoListeners() {
        title("twoListeners");

        ear1 = new InstrumentedListener(1);
        ear2 = new InstrumentedListener(2);
        ear1.validate(0, 0);
        ear2.validate(0, 0);

        mo = new ModifyableObject();
        mo.addChangeListener(ear1);
        ear1.validate(0, 0);
        ear2.validate(0, 0);

        mo.setModified(true);
        ear1.validate(1, 0);
        ear2.validate(0, 0);

        mo.setModified(false);
        ear1.validate(1, 1);
        ear2.validate(0, 0);

        mo.addChangeListener(ear2);
        mo.setModified(true);
        ear1.validate(2, 1);
        ear2.validate(1, 0);

        // NOTE: setModified OUGHT to be idempotent!!
        // TODO: fix setModified(...)
//        mo.setModified(true);
//        ear1.validate(2, 1);
//        ear2.validate(1, 0);

        mo.removeChangeListener(ear2);
        mo.setModified(false);
        ear1.validate(2, 2);
        ear2.validate(1, 0);

        assertEquals("listener count", 1, mo.changeListeners.size());

        mo.removeChangeListener(ear1);
        mo.setModified(true);
        // no change to our listeners (no longer listening)
        ear1.validate(2, 2);
        ear2.validate(1, 0);

        assertEquals("listeners", null, mo.changeListeners);
    }

    @Test
    public void copyNoListeners() {
        title("copyNoListeners");

        mo = new ModifyableObject();
        moOther = new ModifyableObject();

        assertEquals("listeners", null, mo.changeListeners);
        assertEquals("other listeners", null, moOther.changeListeners);

        mo.copyChangeListeners(moOther);
        assertEquals("listeners", null, mo.changeListeners);
    }

    @Test
    public void copySomeListeners() {
        title("copyListeners");

        mo = new ModifyableObject();
        moOther = new ModifyableObject();
        ear1 = new InstrumentedListener(1);
        moOther.addChangeListener(ear1);

        assertEquals("listeners", null, mo.changeListeners);
        assertEquals("other listeners", 1, moOther.changeListeners.size());

        mo.copyChangeListeners(moOther);
        assertEquals("listeners", 1, mo.changeListeners.size());
        assertEquals("unex listener", ear1, mo.changeListeners.get(0));
    }
}
