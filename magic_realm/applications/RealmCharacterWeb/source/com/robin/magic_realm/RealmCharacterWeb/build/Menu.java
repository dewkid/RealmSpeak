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

import java.io.File;
import java.util.*;

import com.robin.general.io.FileUtilities;
import com.robin.general.util.HashLists;
import com.robin.general.util.StringUtilities;

public class Menu extends Builder {
	
	private static final String[] HTML_HEAD = {
		"<html><head><title>Menu</title>",
		"<style type=\"text/css\">",
		"<!--",
		"a {",
		"color:blue;",
		"cursor:pointer;",
		"}",
		"#box {",
		"border:1px solid #ccc;",
		"background:#f2f2f2;",
		"padding:10px;",
		"}",
		"-->",
		"</style>",
		"<script type=\"text/javascript\">",
		"<!--",
		"function switchMenu(obj,icon) {",
		"	var el = document.getElementById(obj);",
		"	var eli = document.getElementById(icon);",
		"	if ( el.style.display != \"none\" ) {",
		"		el.style.display = 'none';",
		"		eli.src=\"arrowoff.png\";",
		"	}",
		"	else {",
		"		el.style.display = '';",
		"		eli.src=\"arrow.png\";",
		"	}",
		"}",
		"//-->",
		"</script></head>",
		"<body text=\"#000000\" bgcolor=\"#CFFFCD\" link=\"#3333FF\" vlink=\"#993366\" alink=\"#FF0000\">",
		"<font color=\"#000000\"><font size=+3>Menu:</font></font>",
		"<br>",
	};
	private static final String PERSONKEY = "PERSONKEY";
	private static final String ICONKEY = "ICONKEY";
	private static final String PERSONNAME = "PERSONNAME";
	private static final String ENTRIES = "ENTRIES";
	private static final String[] HTML_PERSON = {
		"<p><a onclick=\"switchMenu('",
		PERSONKEY, // andrewgould
		"','",
		ICONKEY, // andrewgouldicon
		"');\" title=\"",
		PERSONNAME, // Andrew Gould
		"\"><img id=\"",
		ICONKEY, // andrewgouldicon
		"\" src=\"arrowoff.png\"/><img width=10 height=1/><b>",
		PERSONNAME, // Andrew Gould
		":<b></a></p>",
		"<div id=\"",
		PERSONKEY, // andrewgould
		"\" style='display:none'>",
		"<div id=\"box\">",
		ENTRIES,
		"</div></div>",
	};

	private HashLists hash = new HashLists();
	
	public Menu() {
	}
	public void add(Page page) {
		String folder = page.getLayout().getWebFolder();
		hash.put(folder,page);
	}
	private String createPersonKey(String folderName) {
		String strip = StringUtilities.findAndReplace(folderName," ",""); // strip spaces
		return strip.toLowerCase();
	}
	public void create(File file) {
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<HTML_HEAD.length;i++) {
			sb.append(HTML_HEAD[i]);
			sb.append("\n");
		}
		ArrayList folders = new ArrayList(hash.keySet());
		Collections.sort(folders);
		for (Iterator i=folders.iterator();i.hasNext();) {
			String folder = (String)i.next();
			String personKey = createPersonKey(folder);
			String iconKey = personKey+"icon";
			for (int p=0;p<HTML_PERSON.length;p++) {
				String append = HTML_PERSON[p];
				if (PERSONKEY.equals(HTML_PERSON[p])) {
					append = personKey;
				}
				else if (ICONKEY.equals(HTML_PERSON[p])) {
					append = iconKey;
				}
				else if (PERSONNAME.equals(HTML_PERSON[p])) {
					append = folder;
				}
				else if (ENTRIES.equals(HTML_PERSON[p])) {
					ArrayList list = hash.getList(folder);
					for (Iterator n=list.iterator();n.hasNext();) {
						Page page = (Page)n.next();
						String html = FileUtilities.getFilename(page.getHtml(),true);
						String href = "images/"+folder+"/"+html;
						sb.append("<b><font size=-1><a href=\"");
						sb.append(href);
						sb.append("\" TARGET=\"Data\">");
						sb.append(page.getLayout().getModel().getCharacter().getGameObject().getName());
						sb.append("</a></font></b><br>\n");
					}
					continue;
				}
				sb.append(append);
				if (append.endsWith(">")) {
					sb.append("\n");
				}
			}
		}
		sb.append("</body></html>");
		dumpString(file,sb.toString());
	}
}