/* 
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.util;

public class LogicalDirection {
	// Square directions (S,W,E,N)
	public static final int SQUARE_INDEX_SOUTH = 0;
	public static final int SQUARE_INDEX_WEST = 1;
	public static final int SQUARE_INDEX_EAST = 2;
	public static final int SQUARE_INDEX_NORTH = 3;
	public static final int[] SQUARE_DX = {0,-1,1,0};
	public static final int[] SQUARE_DY = {1,0,0,-1};
	
	// Full direction
	public static final int[] DIAG_DX = {-1,0,1,-1,0,1,-1,0,1};
	public static final int[] DIAG_DY = {1,1,1,0,0,0,-1,-1,-1};
	public static final int[] DIAG_CENTER_FIRST_DX = {0,-1,0,1,-1,1,-1,0,1};
	public static final int[] DIAG_CENTER_FIRST_DY = {0,1,1,1,0,0,-1,-1,-1};
	public static final int[] DIAG_NO_CENTER_DX = {-1,0,1,-1,1,-1,0,1};
	public static final int[] DIAG_NO_CENTER_DY = {1,1,1,0,0,-1,-1,-1};
	
	public static final int BINARY_SOUTH = 1;
	public static final int BINARY_WEST = 2;
	public static final int BINARY_EAST = 4;
	public static final int BINARY_NORTH = 8;
	
}