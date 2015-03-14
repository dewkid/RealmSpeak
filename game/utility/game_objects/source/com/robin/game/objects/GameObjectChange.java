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

import java.io.Serializable;

public abstract class GameObjectChange implements Serializable {
	
	private static long c_changeid = 0;
	
	protected long changeid = c_changeid++;
	
	private long version;
	private long id;
	private String name;
	
	/**
	 * This method is called when the change is to be "finally" applied to the game object.  The uncommitted
	 * object (if any) is discarded, though any changes in the data object are left alone.
	 */
	protected abstract void applyChange(GameData data,GameObject go);
	
	/**
	 * This method should do a soft change (calling non-underscore methods), but otherwise do exactly the same
	 * thing as the applyChange method above.  In fact, this is REQUIRED, or hell will descend apon you!!
	 * 
	 * This method is called when the client GameData object has unapplied changes, and is receiving a batch of new changes.
	 * The new changes are process through applyChange, and then the uncommitted object is rebuilt using this method.
	 */
	protected abstract void rebuildChange(GameData data,GameObject go);
	
	public GameObjectChange(GameObject go) {
		version = go.getVersion();
		id = go.getId();
		name = go.getName();
	}
//	public abstract boolean equals(Object o);
//	public abstract boolean sameTypeOfChange(GameObjectChange other);
	public boolean equals(Object o) {
		if (o instanceof GameObjectChange) {
			GameObjectChange goc = (GameObjectChange)o;
			return goc.changeid == changeid;
		}
		return false;
	}
	public boolean nullEquals(Object o1,Object o2) {
		return o1==null?o2==null:o1.equals(o2);
	}
	public long getId() {
		return id;
	}
	public long getVersion() {
		return version;
	}
//	public void finalize() throws Throwable {
//		System.out.println("dying");
//		super.finalize();
//	}
	public String toString() {
		return name+" (#"+id+" version "+version+")";//+(dead?" - dead":"");
	}
	public boolean testVersion(GameData data) {
//		GameObject go = data.getGameObject(id); // FIXME For now, accept all versions
//		if (go!=null && go.getVersion()!=version) {
//System.err.println(go.getName()+" version "+go.getVersion()+" != change version "+version);// XXX DEBUG
//System.err.println(toString()+" will not happen");
//			return false;
//		}
		return true; // null or matching version are valid
	}
	public GameObject getGameObject(GameData data) {
		GameObject go = data.getGameObject(id);
		if (go==null) {
			go = data.createNewObject(id);
		}
		return go;
	}
	public void applyChange(GameData data) {
		GameObject go = data.getGameObject(id);
		if (go==null) {
			go = data.createNewObject(id);
		}
		go.setName(name);
		go._setVersion(version);
		applyChange(data,go);
	}
	public void rebuildChange(GameData data) {
		GameObject go = data.getGameObject(id);
		// since we are rebuilding, we can safely assume that the game object already exists,
		// and setName and setVersion are already done
		rebuildChange(data,go);
	}
}