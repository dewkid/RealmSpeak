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
package com.robin.game.GameSetupEncoder;

import java.io.*;
import java.util.*;

import com.robin.game.objects.*;
import com.robin.general.util.*;

public class Encoder {

	protected GameData data;
	protected ArrayList printGroupings;
	protected ArrayList codings;
	protected Collection filter = null;
	
	protected static final String LINE_END = "\r\n";
	
	public Encoder() {
		printGroupings = new ArrayList();
		codings = new ArrayList();
	}
	public GameData getGameData() {
		return data;
	}
	public void loadFromPath(String xmlPathname) {
		data = new GameData();
		File xmlFile = new File(xmlPathname);
		System.out.println("Loading "+xmlFile.getPath());
		data.loadFromFile(xmlFile);
		System.out.println("Loaded "+data.getGameObjects().size()+" objects.");
	}
	public void loadFromStream(InputStream stream) {
		data = new GameData();
		data.loadFromStream(stream);
		System.out.println("Loaded "+data.getGameObjects().size()+" objects.");
	}
	public void addPrintGrouping(PrintGrouping grouping) {
		printGroupings.add(grouping);
	}
	public void addCoding(Coding coding) {
		codings.add(coding);
	}
	public void setFilter(Collection keyVals) {
		filter = keyVals;
	}
	public boolean writeFile(String setupName,String filename) {
		StringBuffer printString = new StringBuffer();

		ArrayList<String> query = new ArrayList<String>();
		query.add("original_game");
	
		ArrayList list = data.doSetup(setupName,query);
		System.out.println("Finished setup");
		if (list!=null) {
			GamePool pool = new GamePool(list);
			if (filter!=null) {
				pool = new GamePool(pool.extract(filter));
			}
			
			printString.append(StringUtilities.getRepeatString("*",79));
			printString.append(LINE_END);
			printString.append("**  Result of game setup \""+setupName+"\":"+LINE_END);
			printString.append(StringUtilities.getRepeatString("*",79));
			printString.append(LINE_END);
			printString.append(LINE_END);
			
			// Encode objects
			StringBuffer codingResult = new StringBuffer();
			for (Iterator i=codings.iterator();i.hasNext();) {
				Coding coding = (Coding)i.next();
				codingResult.append(coding.encode(pool));
			}
			
			// Lay out groups
			for (Iterator i=printGroupings.iterator();i.hasNext();) {
				PrintGrouping grouping = (PrintGrouping)i.next();
				printString.append(grouping.print(pool));
			}
			
			printString.append(LINE_END);
			printString.append(StringUtilities.getRepeatString("*",79));
			printString.append(LINE_END);
			printString.append("**  Code translations:"+LINE_END);
			printString.append(StringUtilities.getRepeatString("*",79));
			printString.append(LINE_END);
			printString.append(LINE_END);
			
			// Print coding
			printString.append(codingResult.toString());
		}
		
		// Save to file
		try {
			PrintStream stream = new PrintStream(new FileOutputStream(new File(filename)));
			stream.print(printString);
			stream.close();
			return true;
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	public static void main(String[]args) {
	}
}