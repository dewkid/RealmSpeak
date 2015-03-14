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
package com.robin.general.util;

import java.util.*;

public class StringUtilities {
	public static String getRepeatString(String string,int count) {
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<count;i++) {
			sb.append(string);
		}
		return sb.toString();
	}
	/**
	 * Convert the first character to uppercase, and all remaining characters to lowercase.
	 */
	public static String capitalize(String string) {
		if (string!=null && string.length()>0) {
			if (string.length()==1) {
				return string.toUpperCase();
			}
			return string.substring(0,1).toUpperCase()+string.substring(1).toLowerCase();
		}
		return string;
	}
	/**
	 * Finds all occurrences of "find" in "fullString", and changes them to "replace"
	 */
	public static String findAndReplace(String fullString,String find,String replace) {
		int found;
		int findLength = find.length();
		while((found=fullString.indexOf(find))>=0) {
			String front = fullString.substring(0,found);
			String back = "";
			if ((front.length()+findLength)<fullString.length()) {
				back = fullString.substring(found+findLength);
			}
			fullString = front + replace + back;
		}
		return fullString;
	}
	public static String collectionToString(Collection c,String delim) {
		StringBuffer sb = new StringBuffer();
		for (Iterator i=c.iterator();i.hasNext();) {
			if (sb.length()>0) {
				sb.append(delim);
			}
			Object o = i.next();
			if (o!=null) {
				sb.append(o.toString());
			}
			else {
				sb.append("null");
			}
		}
		return sb.toString();
	}
	public static String booleanArrayToString(boolean[] b,String delim) {
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<b.length;i++) {
			if (sb.length()>0) {
				sb.append(delim);
			}
			sb.append(b[i]?"T":"F");
		}
		return sb.toString();
	}
	/**
	 * This method will return all the values in a delimited String as a Collection of String objects.  Where delimiters suggest a missing
	 * value, an empty string will be returned.  (i.e., the string ",,," would return four empty strings.)
	 * 
	 * @param string				The string to be parsed
	 * @param delim					The delimiter to use
	 */
	public static ArrayList<String> stringToCollection(String string,String delim) {
		return stringToCollection(string,delim,false);
	}
	public static ArrayList<String> stringToCollection(String string,String delim,boolean convertNulls) {
		ArrayList<String> list = new ArrayList<String>();
		boolean stringAdded = false;
		StringTokenizer tokens = new StringTokenizer(string,delim,true);
		while(tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			if (delim.indexOf(token)>=0) {
				// found a delimiter
				if (!stringAdded) {
					list.add("");
				}
				stringAdded = false;
			}
			else {
				// found a string
				if (convertNulls && "null".equals(token)) {
					token = null;
				}
				list.add(token);
				stringAdded = true;
			}
		}
		if (!stringAdded) {
			list.add("");
		}
		return list;
	}
	public static int[] stringToIntArray(String string,String delim) {
		StringTokenizer tokens = new StringTokenizer(string,delim);
		int[] ret = new int[tokens.countTokens()];
		for (int i=0;i<ret.length;i++) {
			Integer num = Integer.valueOf(tokens.nextToken());
			ret[i] = num.intValue();
		}
		return ret;
	}
	public static boolean[] stringToBooleanArray(String string,String delim) {
		StringTokenizer tokens = new StringTokenizer(string,delim);
		boolean[] ret = new boolean[tokens.countTokens()];
		for (int i=0;i<ret.length;i++) {
			ret[i] = tokens.nextToken().equals("T");
		}
		return ret;
	}
	public static String intArrayToString(int[] array,String delim) {
		ArrayList list = new ArrayList();
		for (int i=0;i<array.length;i++) {
			list.add(new Integer(array[i]));
		}
		return collectionToString(list,delim);
	}
	public static String zeroPaddedInt(int num,int length) {
		String val = String.valueOf(num);
		while(val.length()<length) {
			val = "0"+val;
		}
		return val;
	}
	private static void printCollectionDebug(Collection c) {
		System.out.println(c.size()+":  "+c);
	}
	public static void main(String[] args) {
		printCollectionDebug(stringToCollection("---","-"));
		printCollectionDebug(stringToCollection(",robin,,bluebird,",","));
		printCollectionDebug(stringToCollection("one,two,three;four,five;six",";,"));
		printCollectionDebug(stringToCollection("asdf,asdf,asdf;qweg,wgwe,wgw;ffifi,ff,ff,",";"));
	}
}