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

import java.awt.Image;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.DebugUtility;

public abstract class StateChitComponent extends SquareChitComponent {

	public static final String FACE_UP = DARK_SIDE_UP;
	public static final String FACE_DOWN = LIGHT_SIDE_UP;
	
	protected StateChitComponent(GameObject obj) {
		super(obj);
		lightColor = MagicRealmColor.WHITE;
	}
	public String getLightSideStat() {
		return "this";
	}
	public String getDarkSideStat() {
		return "this";
	}
	protected void explode() {
		// this implementation does nothing
	}
	public int getChitSize() {
		return S_CHIT_SIZE;
	}
	public boolean isFaceUp() {
		return isDarkSideUp();
	}
	public ImageIcon getNotesIcon() {
		if (isFaceDown()) {
			return new ImageIcon(getFlipSideImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
		}
		return getMediumIcon();
	}
	public Image getFaceUpImage() {
		if (!isFaceUp()) {
			return getFlipSideImage();
		}
		return getImage();
	}
	public boolean isFaceDown() {
		return isLightSideUp();
	}
	public void setFaceUp() {
		setDarkSideUp();
		explode();
		if (!getGameObject().hasThisAttribute("seen")) {
			getGameObject().setThisAttribute("seen");
		}
	}
	public void addSummonedToday(int dieResult) {
		getGameObject().addThisAttributeListItem(Constants.SUMMONED_TODAY,String.valueOf(dieResult));
	}
	public boolean hasSummonedToday(int dieResult) {
		if (DebugUtility.isSummonMultiple()) return false;
		return getGameObject().hasThisAttributeListItem(Constants.SUMMONED_TODAY,String.valueOf(dieResult));
	}
	public void clearSummonedToday() {
		if (getGameObject().hasThisAttribute(Constants.SUMMONED_TODAY)) {
			getGameObject().removeThisAttribute(Constants.SUMMONED_TODAY);
		}
	}
	public boolean hasBeenSeen() {
		return getGameObject().hasThisAttribute("seen");
	}
	/**
	 * Not sure how else to do this - this version of the method will not allow the RedSpecialChitComponent to explode itself
	 */
	public void setFaceUpWithoutExplode() {
		setDarkSideUp();
	}
	public void setFaceDown() {
		setLightSideUp();
	}
	protected String getExtraBoardShadingType() {
		// State chits should NOT reveal their board affiliation when they are face down
		if (isFaceUp()) {
			return super.getExtraBoardShadingType();
		}
		return null;
	}
}