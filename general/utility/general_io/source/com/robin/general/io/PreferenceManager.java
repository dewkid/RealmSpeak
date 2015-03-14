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

import java.io.*;
import java.util.*;

public class PreferenceManager {

	private String header = "Preference file created by com.robin.general.io.PreferenceManager";
	private String prefDirName;
	private String prefFileName;
	private Properties preferences;
	private File prefDir = null;
	private File prefFile = null;
	
	/*&
	 * @param dirName			A String that will be used to create a directory at user.home
	 * @param fileName			A String that will be used to create a single file in the directory.  No extension is added
	 *							automatically, so if you need one, add it yourself!
	 */
	public PreferenceManager(String dirName,String fileName) {
		this.prefDirName = dirName;
		this.prefFileName = fileName;
		preferences = new Properties();
	}
	public Properties getProperties() {
		return preferences;
	}
	public void setHeader(String val) {
		if (val!=null && val.trim().length()>0) {
			header = val;
		}
	}
	
	private File getPrefDir() {
		if (prefDir==null) {
			prefDir = createPrefDirFile(prefDirName);
		}
		return prefDir;
	}
	
	private File getFile(String filename) {
		// First check locally
		File file = new File(filename);
		if (!file.exists()) {
			// Not local?  Use user home.
			if (getPrefDir()!=null) {
				file = new File(getPrefDir().getPath()+File.separator+filename);
			}
		}
		return file;
	}
	
	public File getPrefFile() {
		if (prefFile==null) {
			prefFile = getFile(prefFileName);
		}
		return prefFile;
	}
	
	public boolean canLoad() {
		return getPrefFile().exists();
	}
	
	public void loadPreferences() {
		if (canLoad()) {
			try {
				preferences = new Properties();
				FileInputStream stream = new FileInputStream(getPrefFile());
				preferences.load(stream);
				stream.close();
			}
			catch(FileNotFoundException ex) {
			}
			catch(IOException ex) {
			}
		}
		else {
			preferences = new Properties();
			createDefaultPreferences(preferences);
		}
	}
	
	public void savePreferences() {
		try {
			FileOutputStream stream = new FileOutputStream(getPrefFile());
			preferences.store(stream,"# "+header);
			stream.close();
		}
		catch(FileNotFoundException ex) {
		}
		catch(IOException ex) {
		}
	}
	
	public boolean erasePreferences() {
		boolean ret = false;
		try {
			ret = getPrefFile().delete();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	
	protected void createDefaultPreferences(Properties props) {
		/** this implementation does nothing */
	}
	public String get(String key) {
		return preferences.getProperty(key);
	}
	public String get(String key,String defaultValue) {
		return preferences.getProperty(key,defaultValue);
	}
	public void addListItem(String key,String val) {
		addListItem(key,val,0);
	}
	/**
	 * Set a bufferSize of 0 if you don't want to limit the list.
	 */
	public void addListItem(String key,String val,int bufferSize) {
		if (bufferSize<1) bufferSize = 1;
		ArrayList<String> list = getList(key);
		while(list.remove(val));
		list.add(0,val);
		while(bufferSize>0 && list.size()>bufferSize) {
			list.remove(list.size()-1);
		}
		int n=0;
		for (String item:list) {
			preferences.setProperty(key+n,item);
			n++;
		}
		while(true) {
			if (preferences.getProperty(key+n)!=null) {
				preferences.remove(key+n);
			}
			else {
				break;
			}
			n++;
		}
	}
	public ArrayList<String> getList(String key) {
		ArrayList<String> list = new ArrayList<String>();
		int n=0;
		while(true) {
			String val = preferences.getProperty(key+n);
			if (val==null) {
				break;
			}
			list.add(val);
			n++;
		}
		return list;
	}
	public int getInt(String key) {
		String val = get(key);
		if (val!=null) {
			try {
				return Integer.valueOf(val).intValue();
			}
			catch(NumberFormatException ex) {
				// nothing
			}
		}
		return 0;
	}
	public double getDouble(String key) {
		String val = get(key);
		if (val!=null) {
			try {
				return Double.valueOf(val).doubleValue();
			}
			catch(NumberFormatException ex) {
				// nothing
			}
		}
		return 0;
	}
	public boolean getBoolean(String key) {
		return getBoolean(key,false);
	}
	public boolean getBoolean(String key,boolean defaultVal) {
		String val = get(key);
		if (val!=null) {
			return val.equals("true");
		}
		return defaultVal;
	}
	public void remove(String key) {
		preferences.remove(key);
	}
	public void set(String key,String val) {
		preferences.setProperty(key,val);
	}
	public void set(String key,int val) {
		preferences.setProperty(key,""+val);
	}
	public void set(String key,boolean val) {
		preferences.setProperty(key,val?"true":"false");
	}
	public void clear() {
		preferences.clear();
	}
	/**
	 * Clears all keys that start with the provided string
	 */
	public void clearStartsWith(String start) {
		ArrayList keysToRemove = new ArrayList();
		for (Iterator i=preferences.keySet().iterator();i.hasNext();) {
			String key = (String)i.next();
			if (key.startsWith(start)) {
				keysToRemove.add(key);
			}
		}
		for (Iterator i=keysToRemove.iterator();i.hasNext();) {
			String key = (String)i.next();
			preferences.remove(key);
		}
	}
	public boolean isPref(String key) {
		String val = preferences.getProperty(key);
		if (val!=null) {
			val = val.toLowerCase();
			return val.equals("true");
		}
		return false;
	}
	
	public static String[] getPreferenceFilesAt(String dirName) {
		File dir = createPrefDirFile(dirName);
		return dir.list();
	}
	
	private static File createPrefDirFile(String dirName) {
		File dir = new File(System.getProperty("user.home")+File.separator+dirName+File.separator);
		if (!dir.exists()) {
			// directory doesn't exist?  create it.
			if (!dir.mkdir()) {
				// problem creating directory?  Let the user know...
				System.out.println("Unable to create preference directory:  "+dir.getPath());
				// ... and just save the preference file locally.
				dir = null;
			}
		}
		return dir;
	}
}