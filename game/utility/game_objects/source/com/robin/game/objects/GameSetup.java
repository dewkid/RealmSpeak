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
package com.robin.game.objects;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Attribute;
import org.jdom.Element;

import com.robin.general.io.ModifyableObject;

public class GameSetup extends ModifyableObject implements Serializable {
	public static final String ALL = "ALL";

	protected String name="Untitled Setup";
	protected ArrayList<GameCommand> gameCommands;
	
	protected GameData parent;
	
	protected Hashtable pools;
	
	public GameSetup(GameData parentData) {
		parent = parentData;
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				parent.setModified(true);
			}
		});
		gameCommands = new ArrayList<GameCommand>();
		reset();
		setModified(true);
	}
	public GameData getGameData() {
		return parent;
	}
	public void reset() {
		gameCommands.clear();
	}
	public void setName(String val) {
		if (val!=null) {
			name = val;
			setModified(true);
		}
	}
	public String getName() {
		return name;
	}
	public String getFullTitle() {
		return parent.getGameName()+":  "+name;
	}
	public Element getXML() {
		Element element = new Element("GameSetup");
		element.setAttribute(new Attribute("name",name));
		
		// Add all commands
		for (GameCommand command:gameCommands) {
			element.addContent(command.getXML());
		}
		
		// return
		return element;
	}
	public void setXML(Element element) {
		reset();
		Attribute nameAtt = element.getAttribute("name");
		if (nameAtt!=null) {
			setName((String)nameAtt.getValue());
		}
		
		// Read all commands
		Collection commands = element.getChildren();
		gameCommands.clear();
		for (Iterator i=commands.iterator();i.hasNext();) {
			Element command = (Element)i.next();
			GameCommand newCommand = GameCommand.createFromXML(this,command);
//			GameCommand newCommand = new GameCommand(this);
//			newCommand.setXML(command);
			gameCommands.add(newCommand);
		}
		
		setModified(true);
	}
	public String toString() {
		return name;
	}
	public ArrayList<GameCommand> getGameCommands() {
		return gameCommands;
	}
	public int getCommandCount() {
		return gameCommands.size();
	}
	public void add(GameCommand command) {
		gameCommands.add(command);
	}
	public boolean removeCommand(GameCommand command) {
		return gameCommands.remove(command);
	}
	public boolean updateCommand(GameCommand original,GameCommand updated) {
		int index = gameCommands.indexOf(original);
		if (index>=0) {
			gameCommands.set(index,updated);
		}
		setModified(true);
		return false;
	}
	public GameCommand createNewCommand() {
		return createNewCommand(-1);
	}
	public GameCommand createNewCommand(int row) {
		GameCommand command = new GameCommandCreate(this);
		if (row==-1) {
			gameCommands.add(command);
		}
		else {
			gameCommands.add(row,command);
		}
		setModified(true);
		return command;
	}
	public void copyCommandsFrom(GameSetup setup) {
		ArrayList commands = setup.getGameCommands();
		for (Iterator i=commands.iterator();i.hasNext();) {
			GameCommand command = (GameCommand)i.next();
			GameCommand newCommand = GameCommand.getCommandForName(this,command.getTypeName());
			gameCommands.add(newCommand);
			newCommand.copyFrom(command);
		}
		setModified(true);
	}
	public ArrayList processSetup(StringBuffer result,ArrayList gameObjects) {
		pools = new Hashtable();
		pools.put(ALL,new GamePool(gameObjects));
		result.append("Pool ALL was created: "+gameObjects.size()+"\n");
		for (GameCommand command:gameCommands) {
			result.append(command.doCommand(gameObjects));
		}
		result.append("---DONE---");
		ArrayList keys = new ArrayList(pools.keySet());
		Collections.sort(keys);
		for (Iterator i=keys.iterator();i.hasNext();) {
			String key = (String)i.next();
			GamePool pool = (GamePool)pools.get(key);
			result.append(key+": "+pool.size()+" left\n");
		}
		return gameObjects;
	}
	public void createPool(String poolName) {
		if (pools.get(poolName)==null) {
			pools.put(poolName,new GamePool());
		}
	}
	public GamePool getPool(String poolName) {
		return (GamePool)pools.get(poolName);
	}
	public void moveObjectsBefore(ArrayList objects,GameCommand indexObject) {
		moveObjects(objects,indexObject,true);
	}
	public void moveObjectsAfter(ArrayList objects,GameCommand indexObject) {
		moveObjects(objects,indexObject,false);
	}
	/**
	 * Moves the objects to the position BEFORE the GameCommand with an id==idPosition
	 */
	private void moveObjects(ArrayList objects,GameCommand indexObject,boolean before) {
		// First, verify ALL objects are in the list, and that the list is uniqued
		ArrayList validCommands = new ArrayList();
		for (Iterator i=objects.iterator();i.hasNext();) {
			GameCommand command = (GameCommand)i.next();
			if (command.parent==this && gameCommands.contains(command) && !validCommands.contains(command)) {
				validCommands.add(command);
			}
		}
		if (validCommands.size()!=objects.size()) {
			throw new IllegalStateException("Invalid object set to move!");
		}
		
		// Find the index of the specified id
		if (indexObject==null) {
			throw new IllegalStateException("Invalid indexObject!");
		}
		
		// Remove all valid objects
		gameCommands.removeAll(validCommands);
		
		int index = gameCommands.indexOf(indexObject);
		if (!before) index++;
		
		// Reinsert into specified position
		gameCommands.addAll(index,validCommands);
	}
	// Serializable interface
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	public void expandSetup(ArrayList<String> nameAppends) {
		expandSetup(nameAppends,new ArrayList<String>(),null);
	}
	public void expandSetup(ArrayList<String> nameAppends,ArrayList<String> tiedPools,String tiedKey) { // Hey!  Tide Pools!
		if (tiedPools==null || (!tiedPools.isEmpty() && tiedKey==null)) {
			throw new IllegalArgumentException("Invalid use of expandSetup: "+tiedPools+","+tiedKey);
		}
		ArrayList<GameCommand> expanded = new ArrayList<GameCommand>();
		for (GameCommand command:gameCommands) {
			expanded.add(command);
			if (command.usesTargetObject()) {
				// Duplicate for each append, but locate a new targetObject using nameAppend
				for (String nameAppend:nameAppends) {
					GameCommand dupCommand = GameCommand.getCommandForName(this,command.getTypeName());
					dupCommand.copyFrom(command);
					GameObject targObj = command.getTargetObject();
					GameObject newTargObj = parent.getGameObjectByName(targObj.getName()+nameAppend);
					dupCommand.setTargetObject(newTargObj);
					if (dupCommand.usesFrom() && tiedPools.contains(dupCommand.getFrom())) {
						dupCommand.setFrom(dupCommand.getFrom()+nameAppend);
					}
					expanded.add(dupCommand);
				}
			}
			else if (command.isCreate() && tiedPools.contains(command.getNewPool())) {
				for (String nameAppend:nameAppends) {
					GameCommand dupCommand = GameCommand.getCommandForName(this,command.getTypeName());
					dupCommand.copyFrom(command);
					dupCommand.setNewPool(command.getNewPool()+nameAppend);
					expanded.add(dupCommand);
				}
			}
			else if (command.isExtract() && tiedPools.contains(command.getTo())) {
				for (String nameAppend:nameAppends) {
					GameCommand dupCommand = GameCommand.getCommandForName(this,command.getTypeName());
					dupCommand.copyFrom(command);
					dupCommand.setTo(command.getTo()+nameAppend);
					dupCommand.addKeyVal(tiedKey,nameAppend.trim());
					expanded.add(dupCommand);
				}
				command.addKeyVal("!"+tiedKey);
			}
			else if (command.usesCount()) {
				// Don't duplicate, but multiply the count
				command.setCount(command.getCount()*(nameAppends.size()+1));
			}
		}
		gameCommands.clear();
		gameCommands = expanded;
	}
}