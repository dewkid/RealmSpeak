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
package com.robin.magic_realm.components.utility;

import com.robin.game.server.GameClient;
import com.robin.magic_realm.components.swing.RealmLogWindow;

public class RealmLogging {
	public static final String BATTLE = "__battle__";
	public static final String LOG_INDENT = "__log_indent__";
	
	public static final String LOG_INDENT_CLEAR = "clear";
	public static final String LOG_INDENT_INCREMENT = "increment";
	
	
	public static void logMessage(String key,String message) {
		if (GameClient.GetMostRecentClient()!=null) {
			GameClient.broadcastClient(key,message);
		}
		else {
			// No game client (combat simulator)?  Report it directly to the log window.
			RealmLogWindow.getSingleton().addMessage(key,message);
		}
	}
	public static void clearIndent() {
		logMessage(LOG_INDENT,LOG_INDENT_CLEAR);
	}
	public static void incrementIndent() {
		logMessage(LOG_INDENT,LOG_INDENT_INCREMENT);
	}
}