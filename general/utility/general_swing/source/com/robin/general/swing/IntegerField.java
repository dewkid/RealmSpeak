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

import javax.swing.*;
import javax.swing.text.*;

public class IntegerField extends JTextField {
	public IntegerField() {
		this("");
	}
	public IntegerField(int num) {
		this(""+num);
	}
	public IntegerField(Integer num) {
		this(num.toString());
	}
	public IntegerField(String text) {
		setDocument(new PlainDocument() {
			public void insertString (int offset, String  str, AttributeSet attr) throws BadLocationException {
				if (str == null) return;
				StringBuffer temp=new StringBuffer();
				for (int i=0;i<str.length();i++) {
					if (str.charAt(i)>='0'&&str.charAt(i)<='9')
						temp.append(str.charAt(i));
				}
				if (temp.length()>0)
					super.insertString(offset,temp.toString(),attr);
			}
		});
		setText(text);
	}
	public int getInt() {
		String text = getText();
		if (text.length()>0) {
			return Integer.valueOf(getText()).intValue();
		}
		return 0;
	}
	public Integer getInteger() {
		String text = getText().trim();
		if (text.length()>0) {
			return Integer.valueOf(text);
		}
		return new Integer(0);
	}
}