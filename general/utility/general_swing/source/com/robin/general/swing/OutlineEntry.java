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

public class OutlineEntry {
	String header;
	String content;
	public OutlineEntry() {
		this(null,null);
	}
	public OutlineEntry(String h,String c) {
		header = stripCR(h);
		content = stripCR(c);
	}
	public String getHeader() {
		return header;
	}
	public String getContent() {
		return content;
	}
	public String toString() {
		return header+":  "+content;
	}
    public static String stripCR(String in) {
    	if (in!=null) {
		    int ix=0;
		    int i2=in.indexOf("\r",ix);
		    int i3=in.indexOf("\n",ix);
		    while ((i2>=0) || (i3>=0)) {
				if (i3==-1 || (i2!=-1 && i2<i3)) {
					ix=i2;
				}
				else {
					ix=i3;
				}
				i2=in.indexOf("\r",ix);
				i3=in.indexOf("\n",ix);
				in=in.substring(0,ix)+" "+in.substring(ix+1);
		    }
		    return in;
		}
		return "";
    }
}