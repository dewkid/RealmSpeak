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

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GameObjectChange;

/**
 * This class will "host" a game, by providing a connection listener, servers, and
 * controlled data management for multiple clients.
 */
public class GameHost {
	public static final String DATA_NAME = "host";
	
	public static GameHost mostRecentHost = null; // FIXME Should use a Singleton pattern here!
	
	private static Logger logger = Logger.getLogger(GameHost.class.getName());

	public static final int DEFAULT_PORT = 47474;
	
	protected GameData masterData;	// loaded once, never changed - used to determine changes for new clients
	protected GameData gameData;	// ever changing gameData
	protected String gameTitle;
	protected String password;
	
	protected String hostName = null;
	
	protected GameConnector connector;
	
	protected ArrayList<GameServer> servers;
	
	protected ArrayList gameHostListeners;

	public GameHost(String dataPath,String gameTitle,String password) {
		mostRecentHost = this;
		masterData = new GameData();
		masterData.loadFromPath(dataPath);
		masterData.setDataName("master");
		gameData = new GameData();
		gameData.loadFromPath(dataPath);
		gameData.setDataName(DATA_NAME);
		init(gameTitle,password);
	}
	public GameHost(GameData master,GameData data,String gameTitle,String password) {
		mostRecentHost = this;
		masterData = master;
		gameData = data;
		gameData.setDataName(DATA_NAME);
		init(gameTitle,password);
	}
	public boolean isNameUnique(GameServer ignoreServer,String name) {
		for (GameServer server:servers) {
			String clientName = server.getClientName();
			if (!server.equals(ignoreServer) && clientName!=null && clientName.equals(name)) {
				return false;
			}
		}
		return true;
	}
	private void init(String title,String pass) {
		this.connector = null;
		this.gameTitle = title;
		this.password = pass;
		servers = new ArrayList<GameServer>();
	}
	public String getGameTitle() {
		return gameTitle;
	}
	public String getPassword() {
		return password;
	}
	public void addGameHostListener(GameHostListener listener) {
		if (gameHostListeners == null) {
			gameHostListeners = new ArrayList();
		}
		gameHostListeners.add(listener);
	}
	public boolean removeGameHostListener(GameHostListener listener) {
		boolean success = false;
		if (gameHostListeners != null) {
			success = gameHostListeners.remove(listener);
			if (gameHostListeners.size()==0) {
				gameHostListeners = null;
			}
		}
		return success;
	}
	public void fireHostOnly(InfoObject io) {
		if (gameHostListeners!=null) {
			for (Iterator i=gameHostListeners.iterator();i.hasNext();) {
				GameHostListener listener = (GameHostListener)i.next();
				listener.handleHostOnlyInfo(io);
			}
		}
	}
	public void fireHostModified() {
		if (gameHostListeners!=null) {
			fireHostModified(new GameHostEvent(this));
		}
	}
	public void fireHostModified(GameHostEvent event) {
		if (gameHostListeners!=null) {
			for (Iterator i=gameHostListeners.iterator();i.hasNext();) {
				GameHostListener listener = (GameHostListener)i.next();
				listener.hostModified(event);
			}
		}
	}
	public void fireServerLost(GameServer server) {
		if (gameHostListeners!=null) {
			GameHostEvent event = new GameHostEvent(this,server,GameHostEvent.NOTICE_LOST_CONNECTION);
			for (Iterator i=gameHostListeners.iterator();i.hasNext();) {
				GameHostListener listener = (GameHostListener)i.next();
				listener.serverLost(event);
			}
		}
	}
	
	public void startListening() {
		startListening(DEFAULT_PORT);
	}
	/**
	 * Launches a GameConnector, listening on given port.
	 */
	public void startListening(int port) {
		if (connector==null) {
			connector = new GameConnector(this,port);
			connector.start();
			try {
				Thread.sleep(500); // Give the connector a chance to start!
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.fine("Started listening on port "+port);
		}
	}
	public void stopListening() {
		if (connector!=null) {
			connector.kill();
			connector = null;
		}
		logger.fine("Stopped listening.");
		mostRecentHost = null;
	}
	public GameConnector getConnector() {
		return connector;
	}
	
	/**
	 * Assigns a server to the provided connection
	 */
	public void addConnection(Socket connection) {
		GameServer server = new GameServer(this,connection);
		server.setClientHostName(hostName);
		server.start();
		servers.add(server);
		fireHostModified(new GameHostEvent(this,server,GameHostEvent.NOTICE_NEW_CONNECTION));
	}
	public void removeServer(GameServer server) {
		if (servers.remove(server)) {
			logger.info("Removing server for client "+server.getClientName());
			fireServerLost(server);
		}
//		else {
//			logger.info("Unable to remove server for client "+server.getClientName()+"!!");
//			throw new IllegalStateException("Unable to remove server for client "+server.getClientName()+"!!");
//		}
	}
	public void killAllOutsideConnections() {
		// Assume that the first connection is the host's player, and shut down all the rest.
		ArrayList<GameServer> list = new ArrayList<GameServer>();
		list.add(servers.remove(0));
		shutdown();
		servers = list;
	}
	public void killConnection(GameServer server) {
		server.kill();
		servers.remove(server);
	}
	public void shutdown() {
		for (GameServer server:servers) {
			server.kill();
		}
		servers.clear();
	}
	public ArrayList<GameServer> getServers() {
		return servers;
	}
	
	public GameData getGameData() {
		return gameData;
	}
	public GameObject getGameObject(Long id) {
		return gameData.getGameObject(id);
	}
	
	public synchronized boolean applyChanges(GameServer activeServer,ArrayList changes) {
		return applyChanges(activeServer,changes,true);
	}
	public synchronized boolean applyChanges(GameServer activeServer,ArrayList changes,boolean fireChange) {
		if (changes!=null && !changes.isEmpty()) {
//			for (Iterator i=changes.iterator();i.hasNext();) {
//				GameObjectChange change = (GameObjectChange)i.next();
//				if (!change.testVersion(gameData)) {
//					// Version inconsistency
//					return false;
//				}
//			}
			logger.fine("Host apply changes: "+changes.size()+" changes.");
			for (Iterator i=changes.iterator();i.hasNext();) {
				GameObjectChange action = (GameObjectChange)i.next();
				logger.finer("--> "+action);
				action.applyChange(gameData);
			}
//			gameData.rebuildChanges(); // This breaks things fairly badly!
			logger.fine("Host apply changes: DONE.");
			
			// Update all servers (except the originating server) with the changes
			for (GameServer server:servers) {
				logger.fine("activeServer="+activeServer);
				logger.fine("server="+server);
				if (activeServer==null || !server.equals(activeServer)) {
					logger.fine("updating a server with "+changes.size());
					server.addObjectChanges(changes);
				}
			}
			
			if (activeServer!=null && fireChange) {
				// only fire this event if the changes came from another server
				fireHostModified();
			}
		}
		return true;
	}
	public void broadcast(String key,String message) {
		for (GameServer server:servers) {
			server.broadcast(key,message);
		}
	}
	public void distributeInfo(InfoObject io) {
		if (io.isForHost()) {
			fireHostOnly(io);
		}
		else {
			for (GameServer server:servers) {
				if (server==null) continue; // would this EVER happen?
				String serverClientName = server.getClientName()==null?"":server.getClientName();
				String ioClientName = io.getDestClientName()==null?null:io.getDestClientName();
				if (serverClientName.equals(ioClientName)) {
//					boolean sameThread = server==Thread.currentThread();
					server.addInfoDirect(io);
//					if (sameThread) {
////System.out.println("Same thread");
//						// Same thread, so drive the server manually
//						try {
//							while(!server.isInfoDirectSentAndReceived()) {
//								server.processNextRequest();
//							}
//						}
//						catch (Exception ex) {
//							ex.printStackTrace();
//						}
//					}
//					else {
////System.out.println("Diff thread");
//						// Different thread, so let the server do it itself, but wait until it is done
//						try {
//							while(!server.isInfoDirectSentAndReceived()) {
//								Thread.sleep(200);
//							}
//						}
//						catch (InterruptedException ex) {
//							ex.printStackTrace();
//						}
//					}
					break; // no need to keep searching if the server was found
				}
			}
		}
	}
	public synchronized ArrayList getMasterToGameChanges() {
		return masterData.buildChanges(gameData);
	}
	public void _testBuildChanges() {
		ArrayList changes = getMasterToGameChanges();
		System.out.println("changes="+changes.size());
		for (Iterator i=changes.iterator();i.hasNext();) {
			GameObjectChange change = (GameObjectChange)i.next();
			System.out.println(change);
		}
		changes.clear();
	}
	/**
	 * Only used when connecting locally
	 */
	public void connectClient(GameClient client) {
		if (client.connection==null) {
			NetFreeSocket serverSide = new NetFreeSocket();
			NetFreeSocket clientSide = new NetFreeSocket();
			serverSide.connect(clientSide);
			client.connection = clientSide;
			client.connected = true;
			addConnection(serverSide);
		}
		else {
			throw new IllegalStateException("Client is already connected!!");
		}
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
}