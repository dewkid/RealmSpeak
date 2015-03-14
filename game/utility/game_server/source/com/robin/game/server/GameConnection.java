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
package com.robin.game.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class GameConnection extends Thread {
	
	private Socket connection;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public abstract void run();
	
	public void setConnection(Socket con) {
		connection = con;
	}
	public Socket getConnection() {
		return connection;
	}
	public ObjectOutputStream getOutputStream() throws Exception {
		if (out==null) {
			out = new ObjectOutputStream(connection.getOutputStream());
		}
		return out;
	}
	public ObjectInputStream getInputStream() throws Exception {
		if (in==null) {
			in = new ObjectInputStream(connection.getInputStream());
		}
		return in;
	}
}