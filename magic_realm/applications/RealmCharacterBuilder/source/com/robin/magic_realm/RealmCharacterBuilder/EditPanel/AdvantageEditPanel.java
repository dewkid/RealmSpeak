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
package com.robin.magic_realm.RealmCharacterBuilder.EditPanel;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public abstract class AdvantageEditPanel extends JPanel {
	
	protected GameObject character;
	private String levelKey;
	
	public abstract String toString();
	protected abstract void applyAdvantage();
	public abstract boolean isCurrent();
	
	protected AdvantageEditPanel(CharacterWrapper pChar,String levelKey) {
		this.levelKey = levelKey;
		character = pChar.getGameObject();
		setBorder(BorderFactory.createTitledBorder(toString()));
	}
	public boolean equals(Object o) {
		return o!=null && toString().equals(o.toString());
	}
	protected GameData getGameData() {
		return character.getGameData();
	}
	protected String getAttribute(String key) {
		return character.getAttribute(levelKey,key);
	}
	protected boolean hasAttribute(String key) {
		return character.hasAttribute(levelKey,key);
	}
	protected void setAttribute(String key) {
		character.setAttribute(levelKey,key);
	}
	protected void setAttribute(String key,String val) {
		character.setAttribute(levelKey,key,val);
	}
	protected void removeAttribute(String key) {
		character.removeAttribute(levelKey,key);
	}
	protected void setAttributeList(String key,ArrayList list) {
		character.setAttributeList(levelKey,key,list);
	}
	protected ArrayList getAttributeList(String key) {
		if (character.getAttributeBlock(levelKey).get(key) instanceof ArrayList) {
			return character.getAttributeList(levelKey,key);
		}
		return null;
	}
	protected void addAttributeListItem(String key,String val) {
		character.addAttributeListItem(levelKey,key,val);
	}
	protected String getCharacterName() {
		return character.getName();
	}
	public void apply() {
		applyAdvantage();
	}
	public String getSuggestedDescription() {
		return null;
	}
	public String getLevelKey() {
		return levelKey;
	}
}