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

/**
 * A class to act like a socket, but not use the network!
 */
public class NetFreeSocket extends Socket {
	
	private PipedOutputStream outStream;
	private PipedInputStream inStream;
	
	public NetFreeSocket() {
		outStream = new PipedOutputStream();
		inStream = new PipedInputStream();
	}
	public void connect(NetFreeSocket socket) {
		try {
			outStream.connect(socket.inStream);
			inStream.connect(socket.outStream);
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public InputStream getInputStream() throws IOException {
		return inStream;
	}

	public OutputStream getOutputStream() throws IOException {
		return outStream;
	}
	
	public static void main(String[] args) {
		// Can I connect a server and client without using a connected socket?  Let's try!
		try {
			NetFreeSocket server = new NetFreeSocket();
			NetFreeSocket client = new NetFreeSocket();
			server.connect(client);
			System.out.println("writing...");
			ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
			out.writeObject("Test");
			out.writeObject("One");
			out.writeObject("Three");
			System.out.println("done");
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			System.out.println("read="+in.readObject());
			System.out.println("read="+in.readObject());
			System.out.println("read="+in.readObject());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}