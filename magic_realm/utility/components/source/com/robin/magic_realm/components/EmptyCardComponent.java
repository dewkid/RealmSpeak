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

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import com.robin.game.objects.GameObject;

public class EmptyCardComponent extends CardComponent {
	
	private Border border;
	
	public EmptyCardComponent() {
		super(GameObject.createEmptyGameObject());
		border = BorderFactory.createLoweredBevelBorder();
	}

	public String getAdditionalInfo() {
		return null;
	}

	public Color getBackingColor() {
		return null;
	}

	public String getCardTypeName() {
		return null;
	}

	public String getName() {
		return null;
	}
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		border.paintBorder(this,g,0,0,CARD_WIDTH,CARD_HEIGHT);
	}
}