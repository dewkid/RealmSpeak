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

/**
 * Denotes random number generator types.
 */
public enum RandomNumberType {
    /**
     * The system-provided generator {@link java.util.Random}.
     */
    System,

    /**
     * R250/521 algorithm.
     * <p>
     * See http://en.literateprograms.org/R250/521_(Java)
     */
    R250_521,

    /**
     * Mersenne Twister algorithm.
     * <p>
     * See https://en.wikipedia.org/wiki/Mersenne_Twister
     */
    MersenneTwister,
}