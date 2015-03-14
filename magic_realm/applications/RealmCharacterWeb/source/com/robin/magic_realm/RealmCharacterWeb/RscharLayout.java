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
package com.robin.magic_realm.RealmCharacterWeb;

import java.io.File;

import org.jdom.Element;

import com.robin.general.io.FileUtilities;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class RscharLayout implements Comparable<RscharLayout> {
	
	private String webFolder;
	private File file;
	private RealmCharacterBuilderModel model;
	
	private String status = null;
	
	public RscharLayout(File file) {
		this.file = file;
		webFolder = "Unknown";
		model = RealmCharacterBuilderModel.createFromFile(file);
	}
	public RscharLayout(File folder,Element element) {
		String path = folder.getAbsolutePath()+File.separator+element.getAttributeValue("file");
		file = new File(path);
		model = RealmCharacterBuilderModel.createFromFile(file);
		webFolder = element.getAttributeValue("webfolder");
	}
	public void clearStatus() {
		status = null;
	}
	public void setStatus(String s) {
		status = s;
	}
	public String getStatus() {
		return status;
	}
	public String toString() {
		return "RSCHAR: "+getFileName()+", "+webFolder;
	}
	public RealmCharacterBuilderModel getModel() {
		return model;
	}
	public int compareTo(RscharLayout rl) {
		int ret = webFolder.compareToIgnoreCase(rl.webFolder);
		if (ret==0) {
			ret = file.compareTo(rl.file);
		}
		return ret;
	}
	public CharacterWrapper getCharacter() {
		return model.getCharacter();
	}
	public void setWebFolder(String text) {
		webFolder = text;
	}
	public String getWebFolder() {
		return webFolder;
	}
	public File getFile() {
		return file;
	}
	public String getFileName() {
		return FileUtilities.getFilename(file,true);
	}
	public Element getElement() {
		Element element = new Element("rschar");
		element.setAttribute("file",FileUtilities.getFilename(file,true));
		element.setAttribute("webfolder",webFolder);
		return element;
	}
}
/*

<webLayout>
   <rschar file="Bard.rschar" webfolder="Robin" />
</webLayout>

*/