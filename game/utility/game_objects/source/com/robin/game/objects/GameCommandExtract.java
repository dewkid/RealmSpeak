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

public class GameCommandExtract extends GameCommand {

	public static String NAME = "Extract";
	
	public GameCommandExtract(GameSetup gameSetup) {
		super(gameSetup);
	}
	public String getTypeName() {
		return NAME;
	}
	protected String process(ArrayList allGameObjects) {
		GamePool fromPool = parent.getPool(from);
		GamePool toPool = parent.getPool(to);
		return extract(fromPool,toPool);
	}
	public String extract(GamePool fromPool,GamePool toPool) {
		ArrayList extracted = fromPool.extract(keyVals,count);
		toPool.addAll(extracted);
		return "Extracted:  "+extracted.size()+":  "+from+"="+fromPool.size()+"   "+to+"="+toPool.size()+"\n";
	}
	public boolean usesFrom() {
		return true;
	}
	public boolean usesTo() {
		return true;
	}
	public boolean usesKeyVals() {
		return true;
	}
	public boolean isExtract() {
		return true;
	}
}