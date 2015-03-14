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
package com.robin.magic_realm.components.table;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.TravelerChitComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Capture extends RealmTable {
	
	private int captureMod;
	private TravelerChitComponent traveler;

	protected Capture(JFrame frame, TravelerChitComponent traveler) {
		super(frame, null);
		this.traveler = traveler;
		captureMod = traveler.getGameObject().getThisInt(Constants.CAPTURE);
	}

	public String getTableKey() {
		return "Capture";
	}

	public String getTableName(boolean longDescription) {
		String modString = ((captureMod>=0)?"+":"")+captureMod;
		return "Capture the "+traveler.getGameObject().getName()+(longDescription?(" ("+modString+")"):"");
	}
	
	public String apply(CharacterWrapper character, DieRoller inRoller) {
		inRoller.setModifier(inRoller.getModifier()+captureMod);
		return super.apply(character,inRoller);
	}
	
	public String applyOne(CharacterWrapper character) {
		return doCapture(character);
	}

	public String applyTwo(CharacterWrapper character) {
		return doCapture(character);
	}

	public String applyThree(CharacterWrapper character) {
		return doCapture(character);
	}

	public String applyFour(CharacterWrapper character) {
		return doCapture(character);
	}

	public String applyFive(CharacterWrapper character) {
		return doCapture(character);
	}

	public String applySix(CharacterWrapper character) {
		return "The "+traveler.getGameObject().getName()+" escaped!";
	}
	
	private String doCapture(CharacterWrapper character) {
		// Add as a hireling
		character.addHireling(traveler.getGameObject());
		
		// Auto-assign to follow character
		character.getGameObject().add(traveler.getGameObject());
		if (traveler.getGameObject().hasThisAttribute(Constants.EXTRA_ACTIONS)) {
			character.setNeedsActionPanelUpdate(true);
		}
		
		return "You captured the "+traveler.getGameObject().getName()+"!";
	}
	@Override
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		list.add(getIconForSearch(traveler));
		return list;
	}
}