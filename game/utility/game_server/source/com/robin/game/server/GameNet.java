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

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public abstract class GameNet extends Thread {
	
	public static int DEFAULT_TIMEOUT_MS = 10000; // a 10 second timeout should be good enough
	
	protected Socket connection;
	protected ObjectOutputStream out = null;
	protected ObjectInputStream in = null;
	
	protected ObjectOutputStream getOutputStream() throws IOException {
		if (out==null) {
			out = new ObjectOutputStream(connection.getOutputStream());
		}
		return out;
	}
	protected ObjectInputStream getInputStream() throws IOException {
		if (in==null) {
			in = new ObjectInputStream(connection.getInputStream());
		}
		return in;
	}
	protected void flush() throws IOException {
		getOutputStream().flush();
		getOutputStream().reset();
	}
	protected void writeCollection(ArrayList c) throws IOException {
		getOutputStream().writeInt(c.size());
		while(!c.isEmpty()) {
			getOutputStream().writeObject(c.remove(0));
		}
		flush();
	}
	protected ArrayList readCollection() throws IOException,ClassNotFoundException {
		int size = getInputStream().readInt();
		ArrayList list = new ArrayList();
		for (int i=0;i<size;i++) {
			list.add(getInputStream().readObject());
		}
		return list;
	}
}