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
package com.robin.magic_realm.components.utility;

import java.io.File;

public class HtmlPath {
	private String currentDirectory;

	public HtmlPath(String currentDirectory) {
		this.currentDirectory = currentDirectory;
		File dir = new File(currentDirectory);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
	public String toString() {
		return currentDirectory;
	}

	public HtmlPath newDirectory(String dirName) {
		return new HtmlPath(currentDirectory + File.separator + dirName);
	}
	
	public HtmlPath upOneDirectory() {
		File path = new File(currentDirectory);
		return new HtmlPath(path.getParent());
	}
	
	public String path() {
		return currentDirectory;
	}
	
	public String path(String filename) {
		return currentDirectory + File.separator + filename;
	}
	
	public HtmlPath relativePathTo(HtmlPath path) {
		if (path.currentDirectory.startsWith(currentDirectory)) {
			String ret = path.currentDirectory.substring(currentDirectory.length());
			while(ret.startsWith(File.separator)) ret = ret.substring(1);
			return new HtmlPath(ret);
		}
		return null;
	}
}