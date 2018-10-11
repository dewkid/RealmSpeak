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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Base class for an object that tracks whether it has been modified.
 */
public class ModifyableObject {

    /**
     * Monotonically increasing barcode number for this class.
     */
    protected static long cum_barcode = 0;

    /**
     * The barcode for this instance.
     */
    protected long barcode = cum_barcode++;

    /**
     * Indicates whether this instance has been modified.
     */
    protected boolean modified = false;

    /**
     * Folks listening for changes to us get notified when our modified
     * status changes.
     */
    protected ArrayList changeListeners;

    /**
     * Sets our modified status to the specified value.
     *
     * @param val modified status value
     */
    public void setModified(boolean val) {
//System.out.println(toString()+" was set to modified="+val);
        modified = val;
        fireChange();
    }

    /**
     * Returns true if this instance has been marked as modified.
     *
     * @return true, if modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Adds all the change listeners of the specified modifyable object as
     * listeners of this instance.
     *
     * @param mo the other modifyable object
     */
    protected void copyChangeListeners(ModifyableObject mo) {
        if (mo.changeListeners != null) {
            for (Iterator i = mo.changeListeners.iterator(); i.hasNext(); ) {
                addChangeListener((ChangeListener) i.next());
            }
        }
    }

    /**
     * Adds a change listener to this instance.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(ChangeListener listener) {
        if (changeListeners == null) {
            changeListeners = new ArrayList();
        }
        changeListeners.add(listener);
//System.out.println(toString()+" has "+changeListeners.size()+" listeners");
    }

    /**
     * Removes a change listener from this instance.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(ChangeListener listener) {
        if (changeListeners != null) {
            changeListeners.remove(listener);
            if (changeListeners.size() == 0) {
                changeListeners = null;
            }
        }
    }

    /**
     * Fires a {@link ChangeEvent} off to all registered listeners.
     */
    protected void fireChange() {
//System.out.println(toString()+" fireChange()");
        if (changeListeners != null) {
            ChangeEvent event = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ChangeListener listener = (ChangeListener) i.next();
                listener.stateChanged(event);
//System.out.println("listener "+listener);
            }
        }
    }

    /**
     * Returns a unique identifier for this instance.
     */
    public String getBarcode() {
        return "BARCODE" + barcode;
    }
}