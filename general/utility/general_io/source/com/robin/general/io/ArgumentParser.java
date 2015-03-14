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
package com.robin.general.io;

import java.util.*;

/**
 * This is a class that can be used to turn an array of command
 * line arguments into a Properties hash.  Values like "name=xxxx" will
 * be entered as key-value pairs.  Lone values like "auto" will be
 * stored as a key with an empty string as a value.
 */
public class ArgumentParser {
	private Properties props;
	private Vector errors;
	public ArgumentParser(String[] args) {
		parseArgs(args);
	}
	public ArgumentParser(String[] args,String[] req) {
		parseArgs(args);
		validateArgs(req);
	}
	private void parseArgs(String[] args) {
		props = new Properties();
		errors = new Vector();
		if (args!=null) {
			for (int i=0;i<args.length;i++) {
				KeyValuePair kvp = new KeyValuePair(args[i]);
				if (kvp.getKey()!=null) {
					props.put(kvp.getKey(),kvp.getValue());
				}
				else {
					addError("Invalid argument:  "+args[i]);
				}
			}
		}
	}
	private void validateArgs(String[] req) {
		for (int i=0;i<req.length;i++) {
			if (props.get(req[i])==null) {
				addError("Required argument <"+req[i]+"> is missing.");
			}
		}	
	}
	
	// Error handling
	public void addError(String error) {
		errors.addElement(error);
	}
	public String[] getErrors() {
		return (String[])errors.toArray(new String[0]);
	}
	public boolean hasErrors() {
		return (errors.size()>0);
	}
	public String getErrorString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator i=errors.iterator();i.hasNext();) {
			sb.append((String)i.next()+"\n");
		}
		return sb.toString();
	}
	
	// Getter methods
	public Properties getProperties() {
		return props;
	}
	public String getValueForKey(String key) {
		return props.getProperty(key);
	}
	public boolean hasKey(String key) {
		return props.containsKey(key);
	}
	
	// INNER CLASS
	
	private class KeyValuePair {
		private String key=null;
		private String value=null;
		public KeyValuePair(String string) {
			if (string!=null) {
				StringTokenizer st = new StringTokenizer(string,"=");
				if (st.countTokens()==1) {
					key = st.nextToken().trim();
					value = "";
				}
				else if (st.countTokens()==2) {
					key = st.nextToken().trim();
					value = st.nextToken().trim();
				}
			}
		}
		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
	}
}