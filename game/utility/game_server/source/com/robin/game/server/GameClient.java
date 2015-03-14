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
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;

import com.robin.game.objects.*;

public abstract class GameClient extends GameNet {
	private static final String THREAD_NAME = "GameClient.ThreadName";
	
	private static final int MILLISECONDS_SLEEP_PER_REQUEST = 50;
	
	public static final String DATA_NAME = "client";
	
	private static GameClient mostRecentClient = null;
	
	private static Logger logger = Logger.getLogger(GameClient.class.getName());
	
	// Client messages
	public static final int REQUEST_IDLE	=   0;
	public static final int REQUEST_LOGIN	=   1;
	
	public static final int SUBMIT_LOGIN	= 100;		// When client is providing name and ip
	public static final int SUBMIT_CHANGES	= 101;
	
	public static final int SUBMIT_DIRECT_INFO	= 110;		// A way for client to send info other than GameData
	public static final int ACCEPTED_DIRECT_INFO	= 111;
	
	public static final int SUBMIT_BROADCAST = 120;
	
	public static final int SUBMIT_GOODBYE	= 199;
	
	private static final RequestObject IDLE_REQ_OBJ = new RequestObject(REQUEST_IDLE);
	
	protected ArrayList<RequestObject> requestQueue = new ArrayList<RequestObject>();
	
	boolean clientDead = false;

	protected String clientName;
	protected String clientPass;
	protected String myIpAddress;
	protected String ipAddress;
	protected int port;
	
	protected GameData gameData;
	protected boolean waitingSubmit = false;
	protected boolean leave = false;
	
	protected boolean connected = false;
	protected boolean dataLoaded = false;
	
	protected boolean hosting = false;
	
	protected ArrayList changeListeners = null;
	
	public GameClient(String dataPath,String ipAddress,String clientName,String clientPass) {
		this(dataPath,ipAddress,clientName,clientPass,GameHost.DEFAULT_PORT);
	}
	public GameClient(String dataPath,String ipAddress,String clientName,String clientPass,int port) {
		GameData data = new GameData();
		data.loadFromPath(dataPath);
		init(data,ipAddress,clientName,clientPass,port);
	}
	public GameClient(GameData data,String ipAddress,String clientName,String clientPass,int port) {
		init(data,ipAddress,clientName,clientPass,port);
	}
//	public void finalize() throws Throwable {
//		System.out.println("Client dies");
//	}
	private void init(GameData data,String ipAddress,String clientName,String clientPass,int port) {
		mostRecentClient = this;
		gameData = data;
		gameData.setDataName(DATA_NAME);
		gameData.setTracksChanges(true);
		this.ipAddress = ipAddress;
		this.port = port;
		this.clientName = clientName;
		this.clientPass = clientPass;
		if (ipAddress==null) {
			myIpAddress = "Direct";
		}
		else {
			try {
				myIpAddress = InetAddress.getLocalHost().getHostAddress();
			}
			catch(UnknownHostException ex) {
				ex.printStackTrace();
			}
		}
		setName(THREAD_NAME);
	}
	public abstract void receiveInfoDirect(ArrayList info);
	public abstract void receiveBroadcast(String key,String message);
	public void sendInfoDirect(String destClientName,String[] info) {
		sendInfoDirect(destClientName,new ArrayList(Arrays.asList(info)));
	}
	public void sendInfoDirect(String destClientName,ArrayList info) {
		sendInfoDirect(destClientName,info,false);
	}
	public void sendInfoDirect(String destClientName,ArrayList info,boolean force) {
		if (force || !clientName.equals(destClientName)) { // only send if a different client!
			requestQueue.add(new RequestObject(SUBMIT_DIRECT_INFO,new InfoObject(destClientName,info)));
		}
	}
	public void broadcast(String key,String message) {
		if (connection instanceof NetFreeSocket) {
			receiveBroadcast(key,message);
		}
		else {
			String[] string = new String[2];
			string[0] = key;
			string[1] = message;
			requestQueue.add(new RequestObject(SUBMIT_BROADCAST,string));
		}
	}
	public void kill() {
		leave = true;
		//System.out.println("Client should die soon");
	}
	public String getClientName() {
		return clientName;
	}
	public String getMyIpAddress() {
		return myIpAddress;
	}
	public boolean isConnected() {
		return connected;
	}
	public boolean isLeave() {
		return leave;
	}
	public boolean isDataLoaded() {
		return dataLoaded;
	}
	public GameData getGameData() {
		return gameData;
	}
	public boolean isHosting() {
		return hosting;
	}
	
	public void addChangeListener(ChangeListener listener) {
		if (changeListeners==null) {
			changeListeners = new ArrayList();
		}
		changeListeners.add(listener);
	}
	public boolean removeChangeListener(ChangeListener listener) {
		boolean ret = false;
		if (changeListeners!=null) {
			ret = changeListeners.remove(listener);
			if (changeListeners.size()==0) {
				changeListeners = null;
			}
		}
		return ret;
	}
	public void fireStateChanged() {
		logger.finer("fireStateChanged");
		if (changeListeners!=null) {
			logger.finer("listenerCount="+changeListeners.size());
			ChangeEvent event = new ChangeEvent(this);
			for (Iterator i=changeListeners.iterator();i.hasNext();) {
				ChangeListener listener = (ChangeListener)i.next();
				StateFireThread thread = new StateFireThread(listener,event);
				thread.start();
			}
		}
		logger.finer("fireStateChanged-done");
	}
	private class StateFireThread extends Thread {
		private ChangeListener listener;
		private ChangeEvent event;
		public StateFireThread(ChangeListener listener,ChangeEvent event) {
			this.listener = listener;
			this.event = event;
		}
		public void run() {
			listener.stateChanged(event);
		}
	}
	public boolean isIdle() {
		return requestQueue.isEmpty();
	}
	public boolean waitingToSubmit() {
		return waitingSubmit;
	}
	private RequestObject getNextInQueue() {
		if (requestQueue.size()>0) {
			// Get next
			return requestQueue.remove(0);
		}
		return IDLE_REQ_OBJ;
	}
	/**
	 * Returns the leave variable - if true, then the client will say goodbye
	 */
	private boolean timeToLeave() {
		return leave;
	}
	/**
	 * Send a request to the server
	 */
	private void send(int request) throws Exception {
		getOutputStream().writeInt(request);
		flush();
	}
	/**
	 * Handles
	 */
	private void handleResponse(RequestObject ro) throws SocketTimeoutException,Exception {
		ArrayList list;
		if (!ro.isIdle()) logger.fine("handleResponse "+ro);
		int response;
		try {
			switch(ro.getRequest()) {
				case REQUEST_IDLE: // this is the default, if the requestQueue is empty
					response = getInputStream().readInt();
					// This is where the client is subverted by the server
					switch(response) {
						case GameServer.RESPOND_IDLE:
							// IDLE and IDLE?  Sleep for a time.
							Thread.sleep(MILLISECONDS_SLEEP_PER_REQUEST);
							break;
						case GameServer.RESPOND_NEED_UPDATE:
							// Instead of loading all the objects, just load changes
							list = readCollection();
							logger.fine("Client received update: "+list.size()+" changes.");
							for (Iterator i=list.iterator();i.hasNext();) {
								GameObjectChange action = (GameObjectChange)i.next();
								logger.finer("   "+action);
								action.applyChange(gameData);
							}
							gameData.rebuildChanges(); // in case it still has some uncommitted changes!
							logger.fine("Client received update: DONE");
							dataLoaded = true;
							fireStateChanged();
							break;
						case GameServer.RESPOND_RECEIVE_DIRECT_INFO:
							logger.fine("Client received direct info");
							list = readCollection();
							receiveInfoDirect(list);
//							getOutputStream().writeInt(ACCEPTED_DIRECT_INFO);
//							flush();
//							logger.fine("Client accepted direct info");
							break;
						case GameServer.RESPOND_BROADCAST:
							String[] string = (String[])getInputStream().readObject();
							receiveBroadcast(string[0],string[1]);
							break;
						case GameServer.RESPOND_GOODBYE:
							leave=true;
							fireStateChanged();
							JOptionPane.showMessageDialog(null,"The server was shut down.  Game over!!");
							logger.fine("Client received goodbye from server");
							break;
					}
					break;
				case SUBMIT_LOGIN:
					getOutputStream().writeObject(clientName);
					getOutputStream().writeObject(myIpAddress);
					flush();
					response = getInputStream().readInt();
					switch(response) {
						case GameServer.RESPOND_ACCEPTED:
							hosting = getInputStream().readBoolean();
							// If accepted, there is no further action here
							break;
						case GameServer.RESPOND_REFUSED:
							// Probably refused because there is already a player with clientName logged in
							JOptionPane.showMessageDialog(
									null,
									"There is already a player logged in with this name: "+clientName,
									"Duplicate Name",
									JOptionPane.ERROR_MESSAGE);
							leave = true;
							fireStateChanged();
							break;
					}
					break;
				case SUBMIT_CHANGES:
					list = (ArrayList)ro.getObject();
					logger.fine("GameClient:  submitting "+list.size()+" changes");
					if (logger.isLoggable(Level.FINER)) {
						for (Iterator i=list.iterator();i.hasNext();) logger.finer("--> "+i.next());
					}
					writeCollection(list);
//					gameData.commit();
					waitingSubmit = false;
					
					logger.fine("GameClient "+clientName+":  done");
					break;
				case SUBMIT_DIRECT_INFO:
					InfoObject io = (InfoObject)ro.getObject();
					io.getInfo().trimToSize();
					getOutputStream().writeObject(io.getDestClientName());
					writeCollection(io.getInfo());
					flush();
					// no need to hear back from server here
					break;
				case SUBMIT_BROADCAST:
					String[] string = (String[])ro.getObject();
					getOutputStream().writeObject(string);
					flush();
					break;
				default:
					break;
			}
			if (!ro.isIdle()) logger.fine("finished "+ro);
		}
		catch(SocketException ex) {
			leave = true;
			fireStateChanged();
			JOptionPane.showMessageDialog(null,"The server was shut down.  Game over!!");
		}
	}
	public void run() {
		int attempts = 0;
		while(connection==null && (attempts++)<5) { // only try 5 times
			if (ipAddress==null) {
				mostRecentClient = null;
				throw new IllegalStateException("Can't start an unconnected GameClient with a null ipAddress!!!");
			}
			logger.info("attempt "+attempts);
			try {
				connection = new Socket(ipAddress,port);
				connection.setSoTimeout(GameNet.DEFAULT_TIMEOUT_MS);
				logger.info("Client connected");
			}
			catch(IOException ex) {
				// Server might not be ready yet...  wait a half-second and try again
				connection = null;
				try {
					Thread.sleep(500);
				}
				catch(InterruptedException iex) {
					// this would be bad, so exit here
					iex.printStackTrace();
					mostRecentClient = null;
					return; // This ends the thread
				}
			}
		}
		
		if (connection==null) {
			// Couldn't get a good connection - ever.
			fireStateChanged();
			mostRecentClient = null;
			return; // This ends the thread
		}

		try {
			// Do login first
			send(REQUEST_LOGIN);
			String password = (String)getInputStream().readObject();
			
			if (clientPass.equals(password)) {
				logger.info("GameClient "+clientName+": logged in");
				// Send name information
				doRequest(new RequestObject(SUBMIT_LOGIN));
				connected = true;
				
				fireStateChanged();
				
				// Now, loop through queue
				logger.info("GameClient "+clientName+": main loop started");
				while(!timeToLeave()) {
					doRequest(getNextInQueue());
					
					// Process any other requests in the queue, if there are any
					while(requestQueue.size()>0) {
						doRequest(getNextInQueue());
					}
//if ((beeper++)%10==0) {System.out.println("BEEP! "+beeper);}					
				}
			}
			else {
				JOptionPane.showMessageDialog(null,"Invalid Password","Login Error",JOptionPane.ERROR_MESSAGE);
			}
			try {
				send(SUBMIT_GOODBYE);
				in.close();
				out.close();
				connection.close();
			}
			catch(SocketException ex) {
				// ignore
			}
			catch(IOException ex) {
				// ignore
			}
			connected = false;
			fireStateChanged();
		}
		catch(SocketTimeoutException ex) {
			System.err.println("Timed out!");
			ex.printStackTrace();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		clientDead = true;
		
		 // This ends the thread
		mostRecentClient = null;
	}
	private void doRequest(RequestObject ro) throws SocketTimeoutException,Exception {
		send(ro.getRequest());
		handleResponse(ro);
	}
	
	// Client actions
	private void submitChanges() {
		if (clientDead) {
			// This is bad
			throw new RuntimeException("This client is DEAD!");
		}
		if (gameData.hasChanges()) {
			ArrayList<GameObjectChange> list = gameData.popAndCommit();
			
			logger.fine("GameClient "+clientName+": Queueing "+list.size()+" changes...");
			if (list!=null && !list.isEmpty()) {
				if (logger.isLoggable(Level.FINER)) {
					for (Iterator i=list.iterator();i.hasNext();) logger.finer("  "+i.next());
				}
				requestQueue.add(new RequestObject(SUBMIT_CHANGES,list));
				waitingSubmit = true;
			}
			logger.fine("GameClient "+clientName+": Done");
		}
		else {
			// Possibly unnecessary, but I want to make sure this is false if there are no changes
			waitingSubmit = false;
		}
	}
	public void setGameData(GameData data) {
		gameData = data;
	}
	public String getDiags() {
		StringBuffer sb = new StringBuffer();
		sb.append("requestQueue="+requestQueue.size());
		return sb.toString();
	}
	
	/**
	 * Submits changes to the client, and waits for them to be live.
	 */
	public synchronized static void submitAndWait(GameClient client) {
		// Test to make sure this is not being called by the same thread as the client!
		if (THREAD_NAME.equals(Thread.currentThread().getName())) {
			throw new IllegalStateException("Ack!  Can't call submitAndWait from the SAME THREAD as the client!");
		}
		
		logger.fine("submitAndWait");
//System.out.println("submitAndWait");
		client.submitChanges();
		try {
			Thread.sleep(MILLISECONDS_SLEEP_PER_REQUEST); // give it a chance...
			while(client.waitingToSubmit()) {
				Thread.sleep(MILLISECONDS_SLEEP_PER_REQUEST); // Without this, it hangs unnecessarily
			}
		}
		catch(Exception ex) {
			// empty
			ex.printStackTrace();
		}
		logger.fine("Done waiting");
//System.out.println("Done waiting");
	}
	
	public static void broadcastClient(String key,String message) {
		if (mostRecentClient!=null) {
			mostRecentClient.broadcast(key,message);
		}
	}
	
	/**
	 * Testing
	 */
	public static void launchBaddie() {
		try {
			Socket connection = new Socket("localhost",47474);
			ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
			out.write(1);
			out.flush();
			while(true) { // wait indefinitely
				Thread.sleep(1000);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static GameClient GetMostRecentClient() {
		if (mostRecentClient!=null && mostRecentClient.clientDead) {
			// This is bad
			throw new RuntimeException("mostRecentClient is DEAD");
		}
		return mostRecentClient;
	}
	public static void main(String[] args) {
		launchBaddie();
	}
	/**
	 * Testing
	 */
	public static void _main(String[] args) {
		System.out.println("Init host");
		GameHost host = new GameHost("mr.xml","nettest","meat");
		System.out.println("Do Setup");
		ArrayList<String> query = new ArrayList<String>();
		query.add("original_game");
		host.getGameData().doSetup("standard_game",query);
//		host.getGameData().getGameObject(0).setAttribute("this","test","1");
		
//		host._testBuildChanges();
//		System.out.println("Done.");
		
		System.out.println("Start Listener");
		host.startListening();
		
		GameClient client = new GameClient("mr.xml","localhost","testClient","meat") {
			public void receiveInfoDirect(ArrayList info) {
				for (Iterator i=info.iterator();i.hasNext();) {
					System.out.println(i.next());
				}
			}
			public void receiveBroadcast(String key,String message) {
				 System.out.println(key+": "+message);
			}
		};
		client.start();
	}
}