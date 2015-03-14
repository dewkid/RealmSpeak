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

import java.util.ArrayList;
import com.robin.game.objects.GameObjectChange;

public class RequestObject {
	private Object obj;
	private ArrayList<GameObjectChange> list;
	private int request;
	public RequestObject(int val) {
		request = val;
		obj = null;
		list = null;
	}
	public RequestObject(int val,String[] o) {
		request = val;
		obj = o;
		list = null;
	}
	public RequestObject(int val,InfoObject o) {
		request = val;
		obj = o;
		list = null;
	}
	public RequestObject(int val,ArrayList<GameObjectChange> l) {
		request = val;
		obj = null;
		list = new ArrayList<GameObjectChange>();
		if (l!=null) {
			list.addAll(l);
		}
	}
	public void finalize() throws Throwable {
		if (list!=null) {
			list.clear();
		}
		list = null;
		obj = null;
		super.finalize();
	}
	public String toString() {
		return "RequestObject: "+request+":"+obj;
	}
	public int getRequest() {
		return request;
	}
	public Object getObject() {
		return list==null?obj:list;
	}
	public boolean isIdle() {
		return request == GameClient.REQUEST_IDLE;
	}
}