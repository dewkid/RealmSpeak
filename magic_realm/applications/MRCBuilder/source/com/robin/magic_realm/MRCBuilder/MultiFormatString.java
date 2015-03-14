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
package com.robin.magic_realm.MRCBuilder;

import java.awt.Font;
import java.awt.Graphics;
import java.util.Vector;

public class MultiFormatString {
	private class FormattedString {

		public String getText() {
			return text;
		}

		public int getType() {
			return type;
		}

		public void append(String s) {
			text = text + s;
		}

		public boolean sameType(int i) {
			return type == i;
		}

		public boolean sameType(FormattedString formattedstring) {
			return type == formattedstring.type;
		}

		public String toString() {
			switch (type) {
				case 0: // '\0'
					return "(PLAIN):\"" + text + "\"";

				case 1: // '\001'
					return "(BOLD):\"" + text + "\"";

				case 2: // '\002'
					return "(ITALIC):\"" + text + "\"";
			}
			return null;
		}

		String text;
		int type;

		public FormattedString(String s) {
			this(s, 0);
		}

		public FormattedString(String s, int i) {
			text = s;
			type = i;
		}
	}

	public MultiFormatString() {
		this("");
	}

	public MultiFormatString(String s) {
		fontName = "Serif";
		fontSize = 10;
		init(s);
	}

	private void init(String s) {
		formattedStrings = new Vector();
		addFormattedText(s);
		initFonts();
	}

	public void setFontAttributes(String s, int i) {
		fontName = s;
		fontSize = i;
		initFonts();
	}

	public void setFontName(String s) {
		fontName = s;
		initFonts();
	}

	public void setFontSize(int i) {
		fontSize = i;
		initFonts();
	}

	public void initFonts() {
		normalFont = new Font(fontName, 0, fontSize);
		boldFont = new Font(fontName, 1, fontSize);
		italicFont = new Font(fontName, 2, fontSize);
	}

	public int draw(Graphics g, int i, int j, int k) {
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		for (int k1 = 0; k1 < formattedStrings.size(); k1++) {
			FormattedString formattedstring = (FormattedString) formattedStrings.elementAt(k1);
			g.setFont(getFont(formattedstring));
			j1 = g.getFontMetrics().getAscent();
			String s1;
			for (String s = formattedstring.getText(); s.length() > 0; s = s1) {
				s1 = drawCroppedString(s, g, i + l, j + i1, k - l);
				if (s1.length() == 0) {
					l += g.getFontMetrics().stringWidth(s);
				}
				else {
					l = 0;
					i1 += j1;
				}
			}

		}

		return i1 + j1;
	}

	public String drawCroppedString(String s, Graphics g, int i, int j, int k) {
		int l = s.length();
		boolean flag = false;
		boolean flag1 = true;
		while (!flag && g.getFontMetrics().stringWidth(s.substring(0, l)) > k) {
			if (!flag1)
				l--;
			else
				flag1 = false;
			l = s.lastIndexOf(" ", l);
			if (l < 0) {
				l = s.indexOf(" ");
				flag = true;
			}
		}
		if (l < 0) {
			g.drawString(s, i, j);
			return "";
		}
		else {
			g.drawString(s.substring(0, l), i, j);
			return s.substring(l);
		}
	}

	public Font getFont(FormattedString formattedstring) {
		switch (formattedstring.getType()) {
			case 1: // '\001'
				return boldFont;

			case 2: // '\002'
				return italicFont;
		}
		return normalFont;
	}

	public void addFormattedText(String s) {
		while (s != null && s.length() > 0) {
			s.trim();
			int i = s.indexOf("<");
			if (i > 0)
				addNormal(s.substring(0, i));
			if (i >= 0 && i + 2 < s.length()) {
				if (s.charAt(i + 2) == '>') {
					byte byte0 = 0;
					String s1 = null;
					switch (s.toLowerCase().charAt(i + 1)) {
						case 98: // 'b'
							s1 = "</b>";
							byte0 = 1;
							break;

						case 105: // 'i'
							s1 = "</i>";
							byte0 = 2;
							break;
					}
					int j = -1;
					if (s1 != null)
						j = s.toLowerCase().indexOf(s1);
					if (j >= 0) {
						add(s.substring(i + 3, j), byte0);
						if (j + 4 < s.length())
							s = s.substring(j + 4);
						else
							s = null;
					}
					else {
						addNormal(s.substring(i));
						s = null;
					}
				}
			}
			else {
				addNormal(s);
				s = null;
			}
		}
	}

	public void addNormal(String s) {
		add(s, 0);
	}

	public void addBold(String s) {
		add(s, 1);
	}

	public void addItalic(String s) {
		add(s, 2);
	}

	private void add(String s, int i) {
		FormattedString formattedstring = null;
		if (formattedStrings.size() > 0)
			formattedstring = (FormattedString) formattedStrings.lastElement();
		if (formattedstring != null && formattedstring.sameType(i))
			formattedstring.append(s);
		else
			formattedStrings.addElement(new FormattedString(s, i));
	}

	public static String[] breakupString(String s) {
		Vector vector = new Vector();
		String s1 = new String(s);
		for (int i = s1.indexOf(" "); i >= 0; i = s1.indexOf(" ")) {
			vector.addElement(s1.substring(0, i + 1));
			if (i + 1 > s1.length())
				s1 = s1.substring(i + 1);
			else
				s1 = "";
		}

		if (s1.length() > 0)
			vector.addElement(s1);
		String as[] = new String[vector.size()];
		for (int j = 0; j < vector.size(); j++)
			as[j] = vector.elementAt(j).toString();

		return as;
	}

	public String toString() {
		StringBuffer stringbuffer = new StringBuffer("MultiFormatString:");
		for (int i = 0; i < formattedStrings.size(); i++) {
			FormattedString formattedstring = (FormattedString) formattedStrings.elementAt(i);
			stringbuffer.append("  " + formattedstring.toString() + "\n");
		}

		return stringbuffer.toString();
	}

	private Font normalFont;
	private Font boldFont;
	private Font italicFont;
	private String fontName;
	private int fontSize;
	private Vector formattedStrings;
}