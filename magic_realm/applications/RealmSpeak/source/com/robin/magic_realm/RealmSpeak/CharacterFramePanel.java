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
package com.robin.magic_realm.RealmSpeak;

import javax.swing.JPanel;

import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.*;

public abstract class CharacterFramePanel extends JPanel {
	
	private CharacterFrame parent;
	
	public abstract void updatePanel();
	
	protected CharacterFramePanel(CharacterFrame parent) {
		this.parent = parent;
	}
	public CharacterFrame getCharacterFrame() {
		return parent;
	}
	public CharacterWrapper getCharacter() {
		return parent.character;
	}
	public RealmComponent getRealmComponent() {
		return RealmComponent.getRealmComponent(parent.character.getGameObject());
	}
	public RealmGameHandler getGameHandler() {
		return parent.gameHandler;
	}
	public RealmSpeakFrame getMainFrame() {
		return parent.gameHandler.getMainFrame();
	}
	public HostPrefWrapper getHostPrefs() {
		return parent.hostPrefs;
	}
	public GameWrapper getGame() {
		return parent.gameHandler.getGame();
	}
}