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
package com.robin.magic_realm.RealmCharacterWeb.build;

import java.io.*;

import com.robin.general.io.ZipUtilities;
import com.robin.magic_realm.RealmCharacterBuilder.CharacterInfoCard;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.RealmCharacterWeb.RscharLayout;

public class Page extends Builder {
	
	private String name;
	private RscharLayout layout;
	private File htmlFile;
	
	public Page(RscharLayout layout) {
		this.layout = layout;
		name = layout.getModel().getCharacter().getGameObject().getName();
	}
	public RscharLayout getLayout() {
		return layout;
	}
	public File getHtml() {
		return htmlFile;
	}
	public void create(File folder) {
		createZip(new File(folder.getAbsolutePath()+File.separator+name+".zip"));
		createImage(new File(folder.getAbsolutePath()+File.separator+name+".jpg"));
		htmlFile = new File(folder.getAbsolutePath()+File.separator+name+"_final.html");
		createHtml(htmlFile);
	}
	private void createImage(File file) {
		CharacterInfoCard card = layout.getModel().getCard();
		RealmCharacterBuilderModel.exportImage(file,card.getImageIcon(false),"JPG");
	}
	private void createZip(File file) {
		File[] source = new File[1];
		source[0] = layout.getFile();
		ZipUtilities.zip(file,source);
	}
	private void createHtml(File file) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body background=\"../../backing.png\">\n");
		sb.append("<center><img SRC=\"");
		sb.append(name);
		sb.append(".jpg\">\n");
		sb.append("<br><b><font size=+3>");
		sb.append(name);
		sb.append("</font></b>\n");
		sb.append("<br>");
		sb.append("<font size=+2><a href=\"");
		sb.append(name);
		sb.append(".zip\"");
		sb.append(">Download Now!</a></font>\n");
		sb.append("</center></body></html>");
		dumpString(file,sb.toString());
	}
}
/*
<html><body
background="../../rlmback2.gif">
<center><img SRC="bard.jpg">
<br><b><font size=+3>Bard</font></b>
<br>
<font size=+2><a href="Bard.zip">Download Now!</a></font>
</center></body></html>
*/