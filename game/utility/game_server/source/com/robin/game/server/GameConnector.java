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

import java.net.*;

public class GameConnector extends Thread {
	private static final String THREAD_NAME = "GameConnector.ThreadName";

	protected GameHost host;
	protected int port;
	protected ServerSocket listener;
	protected String ipAddress;
	
	protected boolean alive;
	
	public GameConnector(GameHost host,int port) {
		this.host = host;
		this.port = port;
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		}
		catch(UnknownHostException ex) {
			ex.printStackTrace();
		}
		setName(THREAD_NAME);
	}
	public int getPort() {
		return port;
	}
	public String getIPAddress() {
		return ipAddress;
	}
	public void kill() {
		alive = false; // this won't have an affect until AFTER the next accepted connection!
		// so lets force it!
		try {
			Socket sock = new Socket(ipAddress,port);
			sock.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public void run() {
		try {
			listener = new ServerSocket(port);
			System.out.println("Listening on port " + listener.getLocalPort());
			alive = true;
			while(alive) {
				Socket connection = listener.accept();
				if (alive) {
					try {
						connection.setSoTimeout(GameNet.DEFAULT_TIMEOUT_MS);
						connection.setTcpNoDelay(true); // Data will be sent earlier, at the cost of an increase in bandwidth consumption.
														// See http://www.davidreilly.com/java/java_network_programming/#3.3
						host.addConnection(connection);
					}
					catch(SocketException ex) {
						System.err.println("Unable to accept connection from "+connection.getInetAddress()+".  Stack trace follows:");
						ex.printStackTrace();
					}
				}
			}
			listener.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}