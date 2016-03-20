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

/**
 * Denotes a component that can be modified.
 */
public interface Modifyable {
    /**
     * Mark this component as modified. The supplied value can be {@code true}
     * to indicate modification, or {@code false} to indicate no modification.
     *
     * @param val true for modified; false otherwise
     */
    void setModified(boolean val);

    /**
     * Returns true if this component has been modified.
     *
     * @return true if modified; false otherwise
     */
    boolean isModified();
}