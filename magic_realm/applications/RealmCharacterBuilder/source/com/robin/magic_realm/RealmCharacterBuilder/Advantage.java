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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.attribute.Badge;

public class Advantage {
	private String badgeName;
	private String name = "ADVANTAGE";
	private String description = "Confers an advantage.";
	private ImageIcon badge;

	public Advantage(String badgeName) {
		this.badgeName = badgeName;
		ImageIcon icon = ImageCache.getIcon(badgeName);
		this.badge = icon;
	}
	public void setFullSring(String fullString) {
		StringTokenizer tokens = new StringTokenizer(fullString,":");
		name = tokens.nextToken().trim();
		description = tokens.nextToken().trim();
	}
	public void setNameDesc(String name,String description) {
		this.name = name;
		this.description = description;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ImageIcon getBadge() {
		return badge;
	}

	public String getBadgeName() {
		return badgeName;
	}

	public void setBadgeName(String badgeName,ImageIcon icon) {
		this.badgeName = badgeName;
		this.badge = icon;
		ImageCache._placeImage(badgeName,icon);
	}

	public void setBadge(ImageIcon badge) {
		this.badge = badge;
	}
	
	public static Advantage createFromCharacter(GameObject character,String levelKey) {
		String first = null;
		ArrayList list = character.getAttributeList(levelKey,"advantages");
		if (list!=null && !list.isEmpty()) {
			first = (String)list.get(0); // For new characters, only ONE advantage per level is allowed
		}
		if (first==null) {
			list = character.getAttributeList(levelKey,"optional");
			if (list!=null && !list.isEmpty()) {
				first = (String)list.get(0); // For new characters, only ONE advantage per level is allowed
			}
		}
		if (first!=null) {
			String badgeName = character.getAttribute(levelKey,"badge_icon");
			if (badgeName==null) {
				badgeName = Badge.makeBadgeName(first);
			}
			if (badgeName!=null) {
				Advantage advantage = new Advantage(badgeName);
				advantage.setFullSring(first);
				return advantage;
			}
		}
		return null;
	}
//	public static Advantage createFromCharacter(GameObject character,String levelKey) {
//		String badgeName = character.getAttribute(levelKey,"badge_icon");
//		if (badgeName!=null) {
//			Advantage advantage = new Advantage(badgeName);
//			ArrayList list = character.getAttributeList(levelKey,"advantages");
//			if (list!=null && !list.isEmpty()) {
//				String first = (String)list.get(0); // For new characters, only ONE advantage per level is allowed
//				advantage.setFullSring(first);
//				return advantage;
//			}
//		}
//		return null;
//	}
}