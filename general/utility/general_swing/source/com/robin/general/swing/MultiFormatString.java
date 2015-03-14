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

import java.util.*;
import java.awt.*;

public class MultiFormatString {

	private Font normalFont;
	private Font boldFont;
	private Font italicFont;
	private String fontName = "Serif";
	private int fontSize = 10;
	private Vector formattedStrings;
	private int extraLineSpacing = 0;
	
	public MultiFormatString() {
		this("");
	}
	
	/**
	 * Sample formattedString = "This is <b>bold</b>, <i>italic</i> and normal."
	 */
	public MultiFormatString(String formattedText) {
		init(formattedText);
	}
	
	private void init(String formattedText) {
		formattedStrings = new Vector();
		addFormattedText(formattedText);
		initFonts();
	}
	
	public void setFontAttributes(String name,int size) {
		fontName = name;
		fontSize = size;
		initFonts();
	}
	
	public void setFontName(String name) {
		fontName = name;
		initFonts();
	}
	
	public void setFontSize(int size) {
		fontSize = size;
		initFonts();
	}
	
	public void initFonts() {
		normalFont = new Font(fontName,Font.PLAIN,fontSize);
		boldFont = new Font(fontName,Font.BOLD,fontSize);
		italicFont = new Font(fontName,Font.ITALIC,fontSize);
	}
	
	public int draw(Graphics g,int x,int y,int maxW) {
		int xoff=0;
		int yoff=0;
		int h=0;
		for (int i=0;i<formattedStrings.size();i++) {
			FormattedString fs = (FormattedString)formattedStrings.elementAt(i);
			g.setFont(getFont(fs));
			h = (g.getFontMetrics()).getAscent();
			String text = fs.getText();
			StringTokenizer tokens = new StringTokenizer(text,"\n",true);
			while(tokens.hasMoreTokens()) {
				String toDraw = tokens.nextToken();
				if (toDraw.equals("\n")) {
					xoff = 0;
					yoff += h;
					yoff += extraLineSpacing;
					continue;
				}
				while(toDraw.length()>0) {
					String temp = drawCroppedString(toDraw,g,x+xoff,y+yoff,maxW-xoff);
					if (temp.length()==0) {
						// The whole string was drawn, but may or may not have filled the width
						xoff += (g.getFontMetrics()).stringWidth(toDraw);
					}
					else {
						xoff = 0;
						yoff += h;
						yoff += extraLineSpacing;
					}
					toDraw = temp;
				}
			}
		}
		return yoff+h;
	}
	
	/**
	 * Draws the portion of the string that will fit, returning the rest.  All
	 * font formatting is left up to the calling function.
	 */
	public String drawCroppedString(String string,Graphics g,int x,int y,int maxW) {
		// First, find the longest portion of the string that doesn't exceed maxW
		int space = string.length();
		boolean escape = false;
		boolean first = true;
		while(!escape && (g.getFontMetrics()).stringWidth(string.substring(0,space))>maxW) {
			if (!first) {
				space--;
			}
			else {
				first = false;
			}
			space = string.lastIndexOf(" ",space);
			if (space<0) {
				space = string.indexOf(" ");
				escape = true;
			}
		}
		if (space<0) {
			g.drawString(string,x,y);
			return "";
		}
		g.drawString(string.substring(0,space),x,y);
		return string.substring(space).trim();
	}
	
	public Font getFont(FormattedString fs) {
		switch(fs.getType()) {
			case Font.BOLD: return boldFont;
			case Font.ITALIC: return italicFont;
		}
		return normalFont;
	}
	
	public void addFormattedText(String text) {
		while(text!=null && text.length()>0) {
			text.trim();
			int startTag = text.indexOf("<");
			if (startTag>0) {
				addNormal(text.substring(0,startTag));
			}
			if (startTag>=0 && (startTag+2)<text.length()) {
				if (text.charAt(startTag+2)=='>') {
					int type=Font.PLAIN;
					String endTagName = null;
					switch(text.toLowerCase().charAt(startTag+1)) {
						case 'b':
							endTagName = "</b>";
							type = Font.BOLD;
							break;
						case 'i':
							endTagName = "</i>";
							type = Font.ITALIC;
							break;
					}
					int endTag = -1;
					if (endTagName!=null) {
						endTag = text.toLowerCase().indexOf(endTagName);
					}
					if (endTag>=0) {
						add(text.substring(startTag+3,endTag),type);
						if ((endTag+4)<text.length()) {
							text = text.substring(endTag+4);
						}
						else {
							text = null;
						}
					}
					else {
						addNormal(text.substring(startTag));
						text = null;
					}
				}
			}
			else {
				addNormal(text);
				text=null;
			}
		}
	}
	
	public void addNormal(String text) {
		add(text,Font.PLAIN);
	}
	
	public void addBold(String text) {
		add(text,Font.BOLD);
	}
	
	public void addItalic(String text) {
		add(text,Font.ITALIC);
	}
	
	private void add(String text,int type) {
		FormattedString last=null;
		if (formattedStrings.size()>0) {
			last = (FormattedString)formattedStrings.lastElement();
		}
		if (last!=null && last.sameType(type)) {
			last.append(text);
		}
		else {
			formattedStrings.addElement(new FormattedString(text,type));
		}
	}
	
	public static String[] breakupString(String text) {
		Vector v = new Vector();
		String string = new String(text);
		int space = string.indexOf(" ");
		while(space>=0) {
			v.addElement(string.substring(0,space+1));
			if (space+1>string.length()) {
				string = string.substring(space+1);
			}
			else {
				string = "";
			}
			space = string.indexOf(" ");
		}
		if (string.length()>0) {
			v.addElement(string);
		}
		String[] strings = new String[v.size()];
		for (int i=0;i<v.size();i++) {
			strings[i] = v.elementAt(i).toString();
		}
		return strings;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("MultiFormatString:");
		for (int i=0;i<formattedStrings.size();i++) {
			FormattedString fs = (FormattedString)formattedStrings.elementAt(i);
			sb.append("  "+fs.toString()+"\n");
		}
		return sb.toString();
	}
	
	private class FormattedString {
	
		String text;
		int type;
		
		public FormattedString(String text) {
			this(text,Font.PLAIN);
		}
		
		public FormattedString(String text,int type) {
			this.text = text;
			this.type = type;
		}
		public String getText() {
			return text;
		}
		public int getType() {
			return type;
		}
		
		public void append(String newText) {
			text = text + newText;
		}
		
		public boolean sameType(int newType) {
			return (type==newType);
		}
		
		public boolean sameType(FormattedString fs) {
			return (type==fs.type);
		}
		public String toString() {
			switch(type) {
				case Font.PLAIN:	return "(PLAIN):\""+text+"\"";
				case Font.BOLD:	return "(BOLD):\""+text+"\"";
				case Font.ITALIC:	return "(ITALIC):\""+text+"\"";
			}
			return null;
		}
	}

	/**
	 * @return the extraLineSpacing
	 */
	public int getExtraLineSpacing() {
		return extraLineSpacing;
	}

	/**
	 * @param extraLineSpacing the extraLineSpacing to set
	 */
	public void setExtraLineSpacing(int extraLineSpacing) {
		this.extraLineSpacing = extraLineSpacing;
	}
}