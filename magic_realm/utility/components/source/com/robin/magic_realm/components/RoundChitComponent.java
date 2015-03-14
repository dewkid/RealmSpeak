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
package com.robin.magic_realm.components;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import com.robin.game.objects.*;

public abstract class RoundChitComponent extends ChitComponent {

	protected RoundChitComponent(GameObject obj) {
		super(obj);
	}
	
	public Shape getShape(int x,int y,int size) {
		return new Ellipse2D.Float(x,y,size-1,size-1);
	}
	protected int getSortOrder() {
		return super.getSortOrder()+500;
	}
}