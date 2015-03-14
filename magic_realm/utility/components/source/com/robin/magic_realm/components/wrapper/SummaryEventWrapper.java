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
package com.robin.magic_realm.components.wrapper;

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.game.objects.*;

public class SummaryEventWrapper extends GameObjectWrapper {

	public SummaryEventWrapper(GameObject obj) {
		super(obj);
	}
	public String getBlockName() {
		return "summev";
	}
	public void addSummaryEvent(String val) {
		addListItem("event",val);
	}
	public ArrayList<String> getSummaryEvents() {
		ArrayList<String> ret = new ArrayList<String>();

		ArrayList list = getList("event");
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				String  val = (String)i.next();
				ret.add(val);
			}
		}
		
		return ret;
	}
	
	private static final String SUMMARY_EVENT_WRAPPER = "__SummEventWrapper_";
	public static Long SEW_ID = null;
	public static SummaryEventWrapper getSummaryEventWrapper(GameData data) {
		if (SEW_ID==null) {
			GamePool pool = new GamePool(data.getGameObjects());
			ArrayList list = pool.find(SUMMARY_EVENT_WRAPPER);
			GameObject gm = null;
			if (list!=null && list.size()==1) {
				gm = (GameObject)list.iterator().next();
			}
			if (gm==null) {
				gm = data.createNewObject();
				gm.setName(SUMMARY_EVENT_WRAPPER);
				gm.setThisAttribute(SUMMARY_EVENT_WRAPPER);
			}
			SEW_ID = new Long(gm.getId());
			return new SummaryEventWrapper(gm);
		}
		else {
			return new SummaryEventWrapper(data.getGameObject(SEW_ID));
		}
	}
}