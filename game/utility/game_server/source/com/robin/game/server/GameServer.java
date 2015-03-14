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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class GameServer extends GameNet {
	private static final String THREAD_NAME = "GameServer.ThreadName";
	
	private static Logger logger = Logger.getLogger(GameServer.class.getName());
	
	public static long cum = 0; // cum??  :)
	public long id = cum++;
	
	private boolean goodbye = false;
	
	// Server responses
	public static final int RESPOND_NEED_UPDATE			= 1;
	public static final int RESPOND_IDLE				= 2;
	public static final int RESPOND_ACCEPTED			= 3;
	public static final int RESPOND_REFUSED				= 4;
	public static final int RESPOND_RECEIVE_DIRECT_INFO	= 5; // means the server will be sending direct info
//	public static final int RESPOND_SEND_DATA_NOW		= 6; // means the server wants the client to send its data immediately
	public static final int RESPOND_BROADCAST			= 7; // means the server is sending a broadcast
	public static final int RESPOND_GOODBYE				= 8; // means the server is saying goodbye
	
	protected GameHost host;
	
	protected String clientHostName;
	protected boolean hosting = false;
	
	protected String clientName;
	protected String clientIP;
	
	protected ArrayList objectChanges;
	
	protected ArrayList infoDirects;
	protected ArrayList broadcasts;
	protected boolean shuttingDown = false;
	
	protected boolean directSentAndReceived = false;
	
	public GameServer(GameHost host,Socket connection) {
		this.host = host;
		this.connection = connection;
		objectChanges = null;
		infoDirects = new ArrayList(10);
		broadcasts = new ArrayList(50);
		setName(THREAD_NAME);
	}
	public void broadcast(String key,String message) {
		String[] string = new String[2];
		string[0] = key;
		string[1] = message;
		broadcasts.add(string);
	}
	public String toString() {
		return "GameServer["+id+"]";
	}
	private boolean isBroadcast() {
		return !broadcasts.isEmpty();
	}
	private String[] getNextBroadcast() {
		if (isBroadcast()) {
			return (String[])broadcasts.remove(0);
		}
		return null;
	}
	private void doBroadcast() throws IOException {
		String[] string = getNextBroadcast();
		getOutputStream().writeInt(RESPOND_BROADCAST);
		getOutputStream().writeObject(string);
		flush();
		// no feedback needed
	}
	public void addInfoDirect(InfoObject io) {
		infoDirects.add(io);
		directSentAndReceived = false;
	}
	private boolean isInfoDirect() {
		return !infoDirects.isEmpty();
	}
	public boolean isInfoDirectSentAndReceived() {
		return directSentAndReceived;
	}
	private InfoObject getNextInfoDirect() {
		if (!infoDirects.isEmpty()) {
			return (InfoObject)infoDirects.remove(0);
		}
		return null;
	}
	public void doInfoDirect() throws IOException {
		InfoObject io = getNextInfoDirect();
		getOutputStream().writeInt(RESPOND_RECEIVE_DIRECT_INFO);
		writeCollection(io.getInfo());
//		getInputStream().readInt(); // waits until this int is available...
		directSentAndReceived = true;
	}
	public boolean equals(Object o1) {
		if (o1 instanceof GameServer) {
			GameServer other = (GameServer)o1;
			return id==other.id;
		}
		return false;
	}
	public void addObjectChanges(Collection inChanges) {
		if (objectChanges==null) {
			// If objectChanges is null, then we haven't grabbed the master-to-game changes.  Do that now!
			objectChanges = new ArrayList(host.getMasterToGameChanges());
		}
		objectChanges.addAll(inChanges);
	}
	public String getClientName() {
		return clientName;
	}
	public String getClientIP() {
		return clientIP;
	}
	public void processNextRequest() throws SocketTimeoutException,IOException,Exception {

		// Get request
		int request = getInputStream().readInt();
		
		// Respond accordingly
		switch(request) {
			case GameClient.REQUEST_LOGIN:
				getOutputStream().writeObject(host.getPassword());
				flush();
				break;
			case GameClient.SUBMIT_LOGIN:
				// expecting a name and an ip
				clientName = (String)getInputStream().readObject();
				clientIP = (String)getInputStream().readObject();
				if (host.isNameUnique(this,clientName)) {
					hosting = clientName.equals(clientHostName);
					getOutputStream().writeInt(RESPOND_ACCEPTED);
					getOutputStream().writeBoolean(hosting);
					flush();
				}
				else {
					getOutputStream().writeInt(RESPOND_REFUSED);
					flush();
				}
				host.fireHostModified(); // force an update
				broadcast("host","New player joins: "+clientName);
				break;
			case GameClient.REQUEST_IDLE:
				// opportunity for the server to tell the client something
				
				if (objectChanges==null || !objectChanges.isEmpty()) {
					getOutputStream().writeInt(RESPOND_NEED_UPDATE);
					if (objectChanges==null) {
						// If objectChanges is null, then we haven't grabbed the master-to-game changes.  Do that now!
						objectChanges = new ArrayList(host.getMasterToGameChanges());
					}
					logger.fine("Server for "+clientName+" sending update with "+objectChanges.size()+" changes.");
					ArrayList toSend = new ArrayList();
					while(!objectChanges.isEmpty()) {
						toSend.add(objectChanges.remove(0));
					}
					// Using removeAll creates the possibility that there is a comodification error, so
					// to prevent that, I'll do it a different way
//					objectChanges.removeAll(toSend);
					writeCollection(toSend);
				}
				else if (isInfoDirect()) {
					doInfoDirect();
				}
				else if (isBroadcast()) {
					doBroadcast();
				}
				else if (shuttingDown) {
					getOutputStream().writeInt(RESPOND_GOODBYE);
					goodbye = true;
				}
				else {
					getOutputStream().writeInt(RESPOND_IDLE);
				}
				flush();
				break;
			case GameClient.SUBMIT_GOODBYE:
				logger.fine("Server for "+clientName+" received GOODBYE.");
				goodbye = true;
				break;
			case GameClient.SUBMIT_CHANGES:
				ArrayList list = readCollection();
				logger.fine("Server for "+clientName+" received "+list.size()+" changes.");
				host.applyChanges(this,list);
//				if (host.applyChanges(this,list)) { // don't send a response anymore
//					logger.fine("Server for "+clientName+" accepting changes.");
//					getOutputStream().writeInt(RESPOND_ACCEPTED);
//					flush();
//				}
//				else {
//					logger.fine("Server for "+clientName+" refusing changes.");
//					getOutputStream().writeInt(RESPOND_REFUSED);
//					flush();
//				}
				break;
			case GameClient.SUBMIT_DIRECT_INFO:
				String destClientName = (String)getInputStream().readObject();
				ArrayList info = readCollection();
				host.distributeInfo(new InfoObject(destClientName,info));
				break;
			case GameClient.SUBMIT_BROADCAST:
				String[] string = (String[])getInputStream().readObject();
				host.broadcast(string[0],string[1]);
				break;
			default:
				// If a request is received that isn't recognized, the server should shutdown immediately
				goodbye=true;
				break;
		}
	}
	public void kill() {
		shuttingDown = true;
	}
	public void run() {
		try {
			goodbye=false; 
			while(!goodbye) {
				processNextRequest();
			}
			
			logger.info("Server for "+clientName+" shutting down normally.");
		}
		catch(SocketTimeoutException ex) {
			// Oops!  Client hasn't responded for awhile.  Timeout!  Need to tell host.
			logger.info("Server for "+clientName+" timed out!  Shutting down.");
			System.out.println("Server for "+clientName+" timed out!  Shutting down.");
		}
		catch(IOException ex) {
			// Oops!  Client disconnected abnormally!  Need to tell host.
			logger.info("Server for "+clientName+" lost the client!  Shutting down.");
		}
		catch(Exception ex) {
			ex.printStackTrace();
			logger.info("Server for "+clientName+" lost the client with an exception!  Shutting down.");
		}
		if (in!=null) try{ in.close(); }catch(IOException ex){ };
		if (out!=null) try{ out.close(); }catch(IOException ex){ };
		if (connection!=null) try{ connection.close(); }catch(IOException ex){ };
		host.removeServer(this);
	}
	public void setClientHostName(String clientHostName) {
		this.clientHostName = clientHostName;
	}
	public boolean isHosting() {
		return hosting;
	}
}