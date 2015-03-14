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
package com.robin.magic_realm.components.quest.requirement;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementColorMagic extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementActive.class.getName());

	public static final String COLOR_KEY = "_clrk";
	
	public QuestRequirementColorMagic(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		ColorMagic test = getColorMagic();
		if (reqParams.burnedColor!=null && reqParams.burnedColor.sameColorAs(test)) return true;
		
		ArrayList<ColorMagic> colors;
		TileLocation tl = character.getCurrentLocation();
		if (!tl.isInClearing()) {
			colors = tl.tile.getAllSourcesOfColor();
		}
		else {
			colors = tl.clearing.getAllSourcesOfColor(true);
		}
		
		boolean found = false;
		for(ColorMagic cm:colors) {
			if (cm.sameColorAs(test)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			logger.fine(getColorName()+" magic is not present.");
			return false;
		}
		
		return true;
	}

	protected String buildDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Only when in the presence of ");
		sb.append(getColorName());
		sb.append(" magic.");
		return sb.toString();
	}

	public RequirementType getRequirementType() {
		return RequirementType.ColorMagic;
	}
	
	public String getColorName() {
		return getString(COLOR_KEY);
	}
	
	public ColorMagic getColorMagic() {
		return ColorMagic.makeColorMagic(getColorName(),true);
	}
}