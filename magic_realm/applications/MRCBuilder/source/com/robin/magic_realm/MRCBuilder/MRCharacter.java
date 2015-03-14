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

import java.io.*;
import java.util.Vector;

public class MRCharacter {

	public MRCharacter() {
		init();
	}

	public void init() {
		creator = "";
		name = "";
		symbolPath = "";
		symbolMeaning = "";
		vulnerability = "";
		specialAdvantages = new Vector();
		optionalAdvantages = new Vector();
		devName = new String[4];
		devAdd = new String[4];
		for (int i = 0; i < 4; i++) {
			devName[i] = "";
			devAdd[i] = "";
		}

		chit = new Chit[12];
		for (int j = 0; j < 12; j++)
			chit[j] = new Chit();

		startingInfo = "";
		allied = "";
		friendly = "";
		unfriendly = "";
		enemy = "";
	}

	public void readFields(MRCBuilder mrcbuilder) {
		creator = mrcbuilder.creatorField.getText();
		name = mrcbuilder.nameField.getText().toUpperCase();
		symbolPath = mrcbuilder.symbolPathField.getText();
		symbolMeaning = mrcbuilder.symbolMeaningField.getText().toUpperCase();
		vulnerability = (String) mrcbuilder.weightVulnerability.getSelectedItem();
		specialAdvantages = mrcbuilder.specialAdvantagesList.getData();
		optionalAdvantages = mrcbuilder.optionalAdvantagesList.getData();
		devName = new String[4];
		devAdd = new String[4];
		for (int i = 0; i < 4; i++) {
			devName[i] = mrcbuilder.devName[i].getText().toUpperCase();
			devAdd[i] = mrcbuilder.devAdd[i].getText();
		}

		chit = new Chit[12];
		for (int j = 0; j < 12; j++)
			chit[j] = mrcbuilder.chitButton[j].getChit();

		startingInfo = mrcbuilder.startingInfoField.getText();
		allied = mrcbuilder.nativeFriendlinessTable.getAllied();
		friendly = mrcbuilder.nativeFriendlinessTable.getFriendly();
		unfriendly = mrcbuilder.nativeFriendlinessTable.getUnfriendly();
		enemy = mrcbuilder.nativeFriendlinessTable.getEnemy();
	}

	public void setFields(MRCBuilder mrcbuilder) {
		mrcbuilder.creatorField.setText(creator);
		mrcbuilder.nameField.setText(name);
		mrcbuilder.symbolPathField.setText(symbolPath);
		mrcbuilder.symbolMeaningField.setText(symbolMeaning);
		mrcbuilder.weightVulnerability.setSelectedItem(vulnerability);
		mrcbuilder.specialAdvantagesList.setData(specialAdvantages);
		mrcbuilder.optionalAdvantagesList.setData(optionalAdvantages);
		for (int i = 0; i < 4; i++) {
			mrcbuilder.devName[i].setText(devName[i]);
			mrcbuilder.devAdd[i].setText(devAdd[i]);
		}

		for (int j = 0; j < 12; j++) {
			mrcbuilder.chitButton[j].getChit().setTextLines(chit[j].getTextLines());
			chit[j] = mrcbuilder.chitButton[j].getChit();
		}

		mrcbuilder.startingInfoField.setText(startingInfo);
		mrcbuilder.nativeFriendlinessTable.reset();
		mrcbuilder.nativeFriendlinessTable.setAllied(allied);
		mrcbuilder.nativeFriendlinessTable.setFriendly(friendly);
		mrcbuilder.nativeFriendlinessTable.setUnfriendly(unfriendly);
		mrcbuilder.nativeFriendlinessTable.setEnemy(enemy);
	}

	public void save(File file) {
		try {
			PrintStream printstream = new PrintStream(new FileOutputStream(file));
			printstream.println("MRCharacter Save File v.1_0_0");
			printstream.println(creator);
			printstream.println(name);
			printstream.println(symbolPath);
			printstream.println(symbolMeaning);
			printstream.println(vulnerability);
			printstream.println(specialAdvantages.size());
			for (int i = 0; i < specialAdvantages.size(); i++) {
				OutlineEntry outlineentry = (OutlineEntry) specialAdvantages.elementAt(i);
				printstream.println(outlineentry.getHeader());
				printstream.println(outlineentry.getContent());
			}

			printstream.println(optionalAdvantages.size());
			for (int j = 0; j < optionalAdvantages.size(); j++) {
				OutlineEntry outlineentry1 = (OutlineEntry) optionalAdvantages.elementAt(j);
				printstream.println(outlineentry1.getHeader());
				printstream.println(outlineentry1.getContent());
			}

			for (int k = 0; k < 4; k++) {
				printstream.println(devName[k]);
				printstream.println(devAdd[k]);
			}

			for (int l = 0; l < 12; l++)
				printstream.println(chit[l].getTextLines());

			printstream.println(startingInfo);
			printstream.println(allied);
			printstream.println(friendly);
			printstream.println(unfriendly);
			printstream.println(enemy);
			printstream.close();
		}
		catch (IOException _ex) {
		}
	}

	public int readInteger(String s) {
		try {
			Integer integer = Integer.valueOf(s);
			return integer.intValue();
		}
		catch (NumberFormatException _ex) {
			return -1;
		}
	}

	public void load(File file) {
		init();
		try {
			BufferedReader bufferedreader = new BufferedReader(new FileReader(file));
			String s = bufferedreader.readLine().trim();
			if (s.equals("MRCharacter Save File v.1_0_0")) {
				creator = bufferedreader.readLine().trim();
				name = bufferedreader.readLine().trim();
				symbolPath = bufferedreader.readLine().trim();
				symbolMeaning = bufferedreader.readLine().trim();
				vulnerability = bufferedreader.readLine().trim();
				int i = readInteger(bufferedreader.readLine().trim());
				for (int j = 0; j < i; j++) {
					String s1 = bufferedreader.readLine().trim();
					String s2 = bufferedreader.readLine().trim();
					specialAdvantages.addElement(new OutlineEntry(s1, s2));
				}

				int k = readInteger(bufferedreader.readLine().trim());
				for (int l = 0; l < k; l++) {
					String s3 = bufferedreader.readLine().trim();
					String s4 = bufferedreader.readLine().trim();
					optionalAdvantages.addElement(new OutlineEntry(s3, s4));
				}

				devName = new String[4];
				devAdd = new String[4];
				for (int i1 = 0; i1 < 4; i1++) {
					devName[i1] = bufferedreader.readLine().trim();
					devAdd[i1] = bufferedreader.readLine().trim();
				}

				chit = new Chit[12];
				for (int j1 = 0; j1 < 12; j1++)
					chit[j1] = new Chit(bufferedreader.readLine().trim());

				startingInfo = bufferedreader.readLine().trim();
				allied = bufferedreader.readLine().trim();
				friendly = bufferedreader.readLine().trim();
				unfriendly = bufferedreader.readLine().trim();
				enemy = bufferedreader.readLine().trim();
			}
			bufferedreader.close();
		}
		catch (FileNotFoundException _ex) {
		}
		catch (IOException _ex) {
		}
	}

	public String getCreator() {
		return creator;
	}

	public String getName() {
		return name;
	}

	public String getSymbolPath() {
		return symbolPath;
	}

	public String getSymbolMeaning() {
		return symbolMeaning;
	}

	public String getVulnerability() {
		return vulnerability;
	}

	public int specialAdvantagesCount() {
		if (specialAdvantages != null)
			return specialAdvantages.size();
		else
			return 0;
	}

	public OutlineEntry getSpecialAdvantage(int i) {
		if (specialAdvantages != null && i < specialAdvantages.size())
			return (OutlineEntry) specialAdvantages.elementAt(i);
		else
			return null;
	}

	public Vector getSpecialAdvantages() {
		return specialAdvantages;
	}

	public int optionalAdvantagesCount() {
		if (optionalAdvantages != null)
			return optionalAdvantages.size();
		else
			return 0;
	}

	public OutlineEntry getOptionalAdvantage(int i) {
		if (optionalAdvantages != null && i < optionalAdvantages.size())
			return (OutlineEntry) optionalAdvantages.elementAt(i);
		else
			return null;
	}

	public Vector getOptionalAdvantages() {
		return optionalAdvantages;
	}

	public String getDevName(int i) {
		if (devName[i] != null && devName[i].length() > 0)
			return devName[i];
		else
			return "";
	}

	public String getDevAdd(int i) {
		if (devAdd[i] != null && devAdd[i].length() > 0)
			return devAdd[i];
		else
			return null;
	}

	public Chit getChit(int i) {
		return chit[i];
	}

	public String getStartingInfo() {
		return startingInfo;
	}

	public String getAllied() {
		return allied;
	}

	public String getFriendly() {
		return friendly;
	}

	public String getUnfriendly() {
		return unfriendly;
	}

	public String getEnemy() {
		return enemy;
	}

	public void setCreator(String s) {
		creator = s;
	}

	public void setName(String s) {
		name = s;
	}

	public void setSymbolPath(String s) {
		symbolPath = s;
	}

	public void setSymbolMeaning(String s) {
		symbolMeaning = s;
	}

	public void setVulnerability(String s) {
		vulnerability = s;
	}

	public void setSpecialAdvantages(Vector vector) {
		specialAdvantages = vector;
	}

	public void setOptionalAdvantages(Vector vector) {
		optionalAdvantages = vector;
	}

	public void setDevNames(String as[]) {
		devName = as;
	}

	public void setDevAdd(String as[]) {
		devAdd = as;
	}

	public void setChit(Chit achit[]) {
		chit = achit;
	}

	public void setStartingInfo(String s) {
		startingInfo = s;
	}

	public void setAllied(String s) {
		allied = s;
	}

	public void setFriendly(String s) {
		friendly = s;
	}

	public void setUnfriendly(String s) {
		unfriendly = s;
	}

	public void setEnemy(String s) {
		enemy = s;
	}

//	private static final String HEADER = "MRCharacter Save File v.1_0_0";
	private String creator;
	private String name;
	private String symbolPath;
	private String symbolMeaning;
	private String vulnerability;
	private Vector specialAdvantages;
	private Vector optionalAdvantages;
	private String devName[];
	private String devAdd[];
	private Chit chit[];
	private String startingInfo;
	private String allied;
	private String friendly;
	private String unfriendly;
	private String enemy;
}