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
package com.robin.magic_realm.components.attribute;

import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.RealmComponent;

public class Note {
	
	private int id;
	
	// Only one of these will be not-null
	private GameObject go;
	private TileLocation tl;
	private String playerName;
	
	private ImageIcon icon;
	
	private int month;
	private int day;
	
	private String event;
	private String note;
	
	public Note(int id,GameData data,String source,String event,String date,String note) {
		this.id = id;
		
		String[] s = date.split(":");
		month = Integer.valueOf(s[0]);
		day = Integer.valueOf(s[1]);
		
		if (source.startsWith("PLAYER")) {
			playerName = source.substring(6);
		}
		else {
			try {
				Long gid = Long.valueOf(source);
				go = data.getGameObject(gid);
			}
			catch(NumberFormatException ex) {
				// Must be a TileLocation
				tl = TileLocation.parseTileLocation(data,source);
			}
		}
		this.event = event;
		this.note = note;
		this.icon = null;
	}
	public int getId() {
		return id;
	}
	public GameObject getGameObject() {
		return go;
	}
	public TileLocation getTileLocation() {
		return tl;
	}
	public ImageIcon getIcon() {
		if (icon==null && playerName==null) {
			if (go!=null) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc!=null) {
					icon = rc.getNotesIcon();
				}
				else {
					// If all else fails, use a green check!
					icon = ImageCache.getIcon("actions/greencheck");
				}
			}
			else {
				icon = tl.tile.getNotesIcon();
			}
		}
		return icon;
	}
	public String getSourceName() {
		if (playerName==null) {
			if (go!=null) {
				return go.getName();
			}
			return tl.toString();
		}
		return playerName;
	}
	public int getMonth() {
		return month;
	}
	public int getDay() {
		return day;
	}
	public String getDate() {
		StringBuffer sb = new StringBuffer();
		sb.append(month);
		sb.append("-");
		sb.append(day);
		return sb.toString();
	}
	public String getEvent() {
		return event;
	}
	public String getNote() {
		return note;
	}
	public boolean isCustom() {
		return playerName!=null;
	}
	public String getPlayerName() {
		return playerName;
	}
	public ArrayList<String> getNoteAsList() {
		ArrayList<String> list = new ArrayList<String>();
		for(String n:note.split("(, |and )")) {
			list.add(n.trim());
		}
		return list;
	}
	public static void main(String[] args) {
		String val = "this, that, those and the other";
		String[] token = val.split("(, |and )");
		for(String n:token) {
			System.out.println(n);
		}
	}
}