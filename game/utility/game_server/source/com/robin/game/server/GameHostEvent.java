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
package com.robin.game.server;import java.util.*;public class GameHostEvent extends EventObject {		public static final int NOTICE_NONE = 0;	public static final int NOTICE_NEW_CONNECTION = 1;	public static final int NOTICE_SERVER_CHANGE = 2;	public static final int NOTICE_LOST_CONNECTION = 3;	public static final int NOTICE_HOST_DIRECT_INFO = 4;		private GameServer server;	private int notice;	public GameHostEvent(Object source) {		this(source,null,NOTICE_NONE);	}	public GameHostEvent(Object source,GameServer server,int notice) {		super(source);		this.server = server;		this.notice = notice;	}	public GameServer getServer() {		return server;	}	public int getNotice() {		return notice;	}}