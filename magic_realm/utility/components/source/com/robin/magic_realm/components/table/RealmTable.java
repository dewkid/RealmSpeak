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
package com.robin.magic_realm.components.table;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.server.GameClient;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.IconGroup;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.RealmDirectInfoHolder;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public abstract class RealmTable {
	
	private RealmTable newTable = null; // sometimes a roll results in a roll on a new table
	private JFrame parentFrame;
	private ChangeListener listener;
	protected DieRoller roller = null;
	
	protected RealmTable(JFrame frame,ChangeListener listener) {
		this.parentFrame = frame;
		this.listener = listener;
	}
	public boolean hideRoller() {
		return false;
	}
	protected ChangeListener getListener() {
		return listener;
	}
	
	protected JFrame getParentFrame() {
		return parentFrame;
	}
	public ImageIcon getRollerImage() {
		if (roller!=null) {
			return roller.getIcon();
		}
		return null;
	}
	public ImageIcon getHintIcon(CharacterWrapper character) {
		return getIconFromList(getHintIcons(character));
	}
	protected ImageIcon getIconFromList(ArrayList<ImageIcon> list) {
		if (list!=null && list.size()>0) {
			if (list.size()==1) return list.get(0);
			IconGroup group = new IconGroup(IconGroup.HORIZONTAL,2);
			for(ImageIcon icon:list) {
				group.addIcon(icon);
			}
			return group;
		}
		return null;
	}
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		return null;
	}
	protected void sendMessage(GameData data,String clientName,String title,String message) {
		ArrayList strings = new ArrayList();
		strings.add(title);
		strings.add(message);
		strings.add(roller==null?"":roller.getStringResult());
		RealmDirectInfoHolder holder = new RealmDirectInfoHolder(data);
		holder.setCommand(RealmDirectInfoHolder.POPUP_MESSAGE);
		holder.setStrings(strings);
		if (GameClient.GetMostRecentClient()!=null && !GameClient.GetMostRecentClient().getClientName().equals(clientName)) {
			GameClient.GetMostRecentClient().sendInfoDirect(clientName, holder.getInfo(),true);
		}
		else {
			// Running in Battle Simulator or getting a curse during the day
			RealmUtility.popupMessage(null,holder); // DO NOT CHANGE THE --NULL-- to parentFrame.  This breaks the Curse logic
		}
	}
	
	/**
	 * @param long		If true, provide the long description
	 */
	public abstract String getTableName(boolean longDescription);
	public abstract String getTableKey();
	public abstract String applyOne(CharacterWrapper character);
	public abstract String applyTwo(CharacterWrapper character);
	public abstract String applyThree(CharacterWrapper character);
	public abstract String applyFour(CharacterWrapper character);
	public abstract String applyFive(CharacterWrapper character);
	public abstract String applySix(CharacterWrapper character);
	
	/**
	 * Default implementation always returns true.
	 */
	public boolean fulfilledPrerequisite(JFrame frame,CharacterWrapper character) {
		return true;
	}
	protected int getResult(DieRoller inRoller) {
		int result = inRoller.getHighDieResult();
		if (result<1) {
			result = 1;
		}
		if (result>6) {
			result = 6;
		}
		return result;
	}
	public String apply(CharacterWrapper character, DieRoller inRoller) {
		this.roller = inRoller;
		int result = getResult(roller);
		String message = null;
		switch(result) {
			case 1:
				message = applyOne(character);
				break;
			case 2:
				message = applyTwo(character);
				break;
			case 3:
				message = applyThree(character);
				break;
			case 4:
				message = applyFour(character);
				break;
			case 5:
				message = applyFive(character);
				break;
			case 6:
				message = applySix(character);
				break;
		}
		return message;
	}
	
	public String toString() {
		return getTableName(true);
	}
	/**
	 * @return Returns the newTable.
	 */
	public RealmTable getNewTable() {
		return newTable;
	}
	/**
	 * @param newTable The newTable to set.
	 */
	public void setNewTable(RealmTable newTable) {
		this.newTable = newTable;
	}
	
	// STATIC METHODS
	
	public static RealmTable peer(JFrame frame,ClearingDetail clearing) {
		return new Peer(frame,clearing);
	}
	public static RealmTable mountainPeer(JFrame frame) {
		return new MountainPeer(frame);
	}
	public static RealmTable peerAny(JFrame frame) {
		return new AnyClearingPeer(frame);
	}
	public static RealmTable locate(JFrame frame,ClearingDetail clearing) {
		return new Locate(frame,clearing);
	}
	public static RealmTable loot(JFrame frame,CharacterWrapper character,TileLocation tl,ChangeListener listener) {
		return new Loot(frame,character,tl,listener);
	}
	public static RealmTable loot(JFrame frame,CharacterWrapper character,GameObject treasureLocation,ChangeListener listener) {
		if (treasureLocation.hasAttributeBlock("table")) {
			return new TableLoot(frame,treasureLocation,listener);
		}
		return new Loot(frame,character,treasureLocation,listener);
	}
	public static RealmTable readRunes(JFrame frame,GameObject spellLocation) {
		return new ReadRunes(frame,spellLocation);
	}
	public static RealmTable magicSight(JFrame frame) {
		return new MagicSight(frame);
	}
	public static RealmTable capture(JFrame frame,TravelerChitComponent traveler) {
		return new Capture(frame,traveler);
	}
	protected ImageIcon getIconForSearch(RealmComponent rc) {
		if (rc.isCard() || rc.isTraveler() || rc.isGuild()) return rc.getMediumIcon();
		return rc.getIcon();
	}
}