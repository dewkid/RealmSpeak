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
import com.robin.magic_realm.components.ClearingDetail;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class EnhancedPeer extends Peer {
	public EnhancedPeer(JFrame parent,ClearingDetail clearing) {
		super(parent,clearing);
	}
	public String getTableName(boolean longDescription) {
		return targetClearing.getDescription();
	}
	public String apply(CharacterWrapper character, DieRoller inRoller) {
		if (targetClearing.getParent().getGameObject().hasThisAttribute(Constants.SP_NO_PEER)) {
			return "Cannot Peer Here: FOG";
		}
		return super.apply(character,inRoller);
	}
	@Override
	protected ArrayList<ImageIcon> getHintIcons(CharacterWrapper character) {
		return new ArrayList<ImageIcon>();
	}
}