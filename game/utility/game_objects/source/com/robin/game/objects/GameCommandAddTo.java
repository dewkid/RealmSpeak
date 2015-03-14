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

import java.util.ArrayList;
import java.util.Iterator;

public class GameCommandAddTo extends GameCommand {

	public static String NAME = "Add";
	
	public GameCommandAddTo(GameSetup gameSetup) {
		super(gameSetup);
	}
	public String getTypeName() {
		return NAME;
	}
	protected String process(ArrayList allGameObjects) {
		GamePool fromPool = parent.getPool(from);
		return addTo(fromPool,allGameObjects);
	}
	public String addTo(GamePool fromPool,ArrayList allGameObjects) {
		// First find the targetObject copy
		GameObject targetObjectCopy = null;
		for (Iterator i=allGameObjects.iterator();i.hasNext();) {
			GameObject copyObject = (GameObject)i.next();
			if (copyObject.equalsId(targetObject.getId())) {
				targetObjectCopy = copyObject;
				break;
			}
		}
		
		// Now, populate the contains of the copy
		ArrayList picked = fromPool.pick(count,transferType);
		for (Iterator i=picked.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			targetObjectCopy.add(obj);
		}
		return "Picked:  "+picked.size()+":  "+from+"="+fromPool.size()+"\n";
	}
	public boolean usesFrom() {
		return true;
	}
	public boolean usesTargetObject() {
		return true;
	}
	public boolean usesCount() {
		return true;
	}
	public boolean usesTransferType() {
		return true;
	}
}