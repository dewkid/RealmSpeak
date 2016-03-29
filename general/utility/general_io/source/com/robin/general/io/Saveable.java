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

import java.awt.*;

/**
 * Denotes a {@link Component} that can be saved.
 * The {@link #saveAs(Component)} method always prompts the user for the
 * location to save the state, whereas the {@link #save(Component)} method
 * will re-use the last known location, if it has one.
 */
public interface Saveable {
    /**
     * Saves the specified component, using the last known location,
     * returning an indication of success. If no previous location is
     * known, delegate to {@link #saveAs(Component)} to prompt for one
     *
     * @param component the component to be saved
     * @return true for success; false if an error was encountered
     */
    boolean save(Component component);

    /**
     * Saves the specified component, prompting the user to supply a
     * location, returning an indication of success.
     *
     * returning an indication of success.
     *
     * @param component the component to be saved
     * @return true for success; false if an error was encountered
     */
    boolean saveAs(Component component);
}