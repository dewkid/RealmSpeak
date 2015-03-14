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
import java.util.Hashtable;

import com.robin.general.swing.IconFactory;

/**
 * @deprecated  Use ImageCache!  Why the heck did I do this in two places, anyway...
 */
public class MagicRealmImage {
	
	private static Hashtable images = new Hashtable();
	
	public static void resetImages() {
		images.clear();
		images = new Hashtable();
	}
	public static ImageIcon _getImageIcon(String val) {
		ImageIcon ii = (ImageIcon)images.get(val);
		if (ii==null) {
			ii = IconFactory.findIcon(val);
			if (ii!=null) {
				images.put(val,ii);
			}
		}
		return ii;
	}
	public static ImageIcon _getImageIcon(String val,int percent) {
		String key = val+":"+percent;
		ImageIcon ii = (ImageIcon)images.get(key);
		if (ii==null) {
			ii = _getImageIcon(val);
			if (ii==null) {
				throw new IllegalStateException("Cannot find icon for: "+val);
			}
			int w = (ii.getIconWidth()*percent)/100;
			int h = (ii.getIconHeight()*percent)/100;
			ii = new ImageIcon(ii.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH));
			images.put(key,ii);
		}
		return ii;
	}
}