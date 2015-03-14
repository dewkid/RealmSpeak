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
package com.robin.general.swing;

import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

public class IconFactory {
	private static final ClassLoader sysLoader=ClassLoader.getSystemClassLoader();
	public static ImageIcon findIcon(String path) {
		return findIcon(null,path);
	}
	public static ImageIcon findIcon(Class c, String path) {
		ImageIcon i=null;
		File f = new File(path);
		if (f.exists()) {
			i=new ImageIcon(path);
		}
		else {
			URL u=null;
			if (c==null) {
				u=sysLoader.getResource(path);
			}
			else {
				u=c.getResource(path);
			}
			if (u!=null) {
				i=new ImageIcon(u);
			}
		}
//		if (i==null) { // Sometimes I want a null return without an error, so don't report this!
//			System.err.println("Icon not found for "+path);
//		}
		return i;
	}
}