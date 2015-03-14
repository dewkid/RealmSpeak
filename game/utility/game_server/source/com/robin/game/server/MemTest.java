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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.*;
import com.robin.general.io.LoggingHandler;

public class MemTest {
	
	private static final String PADDING = "                        ";
	
	private Random random = new Random(0);
	private Runtime runtime;
	private DecimalFormat df = new DecimalFormat("000,000 K");
	
	public MemTest() {
		LoggingHandler.initLogging();
		runtime = Runtime.getRuntime();
	}
	public void runTest() {
		showMem("Start");
		
		//GameData data = populateFakeData();
		
		showMem("After Init");
		
		//data.setTracksChanges(true);
		//doGameObjectChangeTest();
		doClientServerTest(false);
		
//		while(true) {
//			System.out.println("==================");
//			doModsCommit1(data,true);
//			doModsCommit2(data,true);
//			if (data.getObjectChanges()!=null) {
//				System.out.println("changes="+data.getObjectChanges().size());
//			}
//		}
	}
	private GameData populateFakeData() {
		GameData data = new GameData();
		for (int i=0;i<1000;i++) {
			GameObject go = data.createNewObject();
			populateFakeBlock(go,"this");
			for (int b=0;b<random.nextInt()%2;b++) {
				populateFakeBlock(go,"block"+b);
			}
		}
		return data;
	}
	private void populateFakeBlock(GameObject go,String blockName) {
		for (int a=0;a<(2+random.nextInt()%3);a++) {
			go.setAttribute(blockName,"att"+a,"foo"+random.nextInt());
		}
	}
	private void doModsCommit1(GameData data,boolean commit) {
		ArrayList list = new ArrayList(data.getGameObjects());
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.setThisAttribute("zam","foo"+i);
			go.setThisAttribute("zam","moo"+i);
			go.setThisAttribute("zam","zoo"+i);
			for (int n=0;n<3;n++) {
				go.addThisAttributeListItem("test","foo"+n);
			}
			go.getThisAttributeList("test");
//			for (int n=0;n<3;n++) {
//				go.removeThisAttributeListItem("test","foo"+n);
//			}
		}
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.removeThisAttribute("test");
		}
		
		showMem("After Mods");
		if (commit) {
			data.commit();
			showMem("After Commit");
		}
	}
	private void doModsCommit2(GameData data,boolean commit) {
		ArrayList list = new ArrayList(data.getGameObjects());
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.removeAttributeBlock("renamed");
			for (int n=0;n<3;n++) {
				go.setAttribute("block1","moo"+n,n*4);
			}
			go.renameAttributeBlock("block1","renamed");
		}
		showMem("After Mods");
		if (commit) {
			data.commit();
			showMem("After Commit");
		}
	}
	private void doGameObjectChangeTest() {
		GameObject go = GameObject.createEmptyGameObject();

		//Stupid stupid = new Stupid();
		ArrayList list = new ArrayList();
		for (int i=0;i<1000;i++) {
			//list.add(new Stupid());
			list.add(new GameAttributeChange(go));
		}
		list.clear();
		//stupid = null;
		
		while(true) {
			try {
				Thread.sleep(200);
				System.gc();
				
				//System.out.println(GameObjectChange.totalNumberAlive);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private static boolean clientReady = false;
	private void doClientServerTest(boolean local) {
		GameData master = new GameData();
		master.setDataName("master");
		GameData hostData = populateFakeData();
		GameHost host = new GameHost(master,hostData,"test","test");
		GameData clientData = master.copy();
		GameClient client = new GameClient(clientData,"localhost","TestClient","test",GameHost.DEFAULT_PORT) {
			public void receiveInfoDirect(ArrayList info) {
				for (Iterator i=info.iterator();i.hasNext();) {
					System.out.println(i.next());
				}
			}
			public void receiveBroadcast(String key,String message) {
				 System.out.println(key+": "+message);
			}
		};
		if (local) {
			host.connectClient(client);
		}
		else {
			host.startListening(GameHost.DEFAULT_PORT);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		client.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				clientReady = true;
			}
		});
		client.start();
		
		while(!clientReady) {
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Client Ready!");
		
		boolean loop=true;
		while(loop){
			System.out.println("----------------------\n"+master.getChangeInformation()+"\n"+hostData.getChangeInformation()+"\n"+clientData.getChangeInformation());
			showMem("Start");
			doModsCommit1(clientData,false);
			System.out.println("changes="+clientData.getChangeCount()+"  "+client.getDiags());
			//System.out.println("gocs="+GameObjectChange.totalNumberAlive);
			GameClient.submitAndWait(client);
			showMem("After SubmitAndWait");
			//break;
		}
		
//		for (int i=0;i<5;i++) {
//			try {
//				Thread.sleep(1000);
//				System.gc();
//				System.out.println("gocs="+GameObjectChange.totalNumberAlive);
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		
		//client.stop();
		//client.broadcast("test","hear me?");
	}
	private String getMemString() {
		return "F:"+getNumberString(runtime.freeMemory())
			+" , T:"+getNumberString(runtime.totalMemory())
			+" , M:"+getNumberString(runtime.maxMemory());
	}
	private String getNumberString(long val) {
		return df.format(val/1024);
	}
	private void showMem(String title) {
		System.out.println(PADDING.substring(title.length())+title+" = "+getMemString());
	}
	public static void main(String[] args) {
		MemTest memTest = new MemTest();
		memTest.runTest();
	}
	public static class Stupid {
		public Stupid() {
			System.out.println("fee");
		}
		public void finalize() {
			System.out.println("fo");
		}
	}
}