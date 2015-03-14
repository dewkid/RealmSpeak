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
package com.robin.game.GameSetupEncoder;

import java.util.*;

import com.robin.general.util.*;
import com.robin.game.objects.*;

public class PrintGrouping extends Properties {
	
	protected String groupName;

	public PrintGrouping(String groupName) {
		this.groupName = groupName;
	}
	public String getName() {
		return groupName;
	}
	public Collection getKeyVals() {
		ArrayList keyVals = new ArrayList();
		for (Enumeration e=keys();e.hasMoreElements();) {
			String key = (String)e.nextElement();
			String val = getProperty(key);
			if (val.trim().length()==0) {
				keyVals.add(key);
			}
			else {
				keyVals.add(key+"="+val);
			}
		}
		return keyVals;
	}
	
	public String print(GamePool pool) {
		StringBuffer sb = new StringBuffer(groupName);
		sb.append(Encoder.LINE_END);
		sb.append(StringUtilities.getRepeatString("-",79));
		sb.append(Encoder.LINE_END);
		
		ArrayList toPrint = pool.find(getKeyVals());
//		System.out.println(toPrint.size()+" objects to print.");
		
		// Print objects
		for (Iterator i=toPrint.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			sb.append(obj+":  ");
			// Print hold codes
			for (Iterator c=obj.getHold().iterator();c.hasNext();) {
				GameObject held = (GameObject)c.next();
				sb.append(Coding.getCode(held));
			}
			sb.append(Encoder.LINE_END);
		}
		sb.append(Encoder.LINE_END);
		return sb.toString();
	}
}