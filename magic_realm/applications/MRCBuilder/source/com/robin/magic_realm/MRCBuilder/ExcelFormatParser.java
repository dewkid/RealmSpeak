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

public class ExcelFormatParser {

	public ExcelFormatParser(File file1) {
		character = null;
		file = file1;
		Vector vector = readIt();
		if (vector != null)
			buildCharacter(vector);
	}

	public Vector readIt() {
		try {
			BufferedReader bufferedreader = new BufferedReader(new FileReader(file));
			Vector vector = new Vector();
			String s;
			while ((s = bufferedreader.readLine()) != null)
				vector.addElement(new RecordParser(s, "\t"));
			bufferedreader.close();
			return vector;
		}
		catch (FileNotFoundException _ex) {
		}
		catch (IOException _ex) {
		}
		return null;
	}

	public String getField(Vector vector, int i, int j) {
		return getField(vector, new FieldPos(i, j));
	}

	public String getField(Vector vector, FieldPos fieldpos) {
		return getField(vector, fieldpos, 0);
	}

	public String getField(Vector vector, FieldPos fieldpos, int i) {
		if (vector != null && fieldpos.getRow() < vector.size()) {
			RecordParser recordparser = (RecordParser) vector.elementAt(fieldpos.getRow());
			if (fieldpos.getCol() < recordparser.totalFields())
				return recordparser.getField(fieldpos.getCol() + i);
		}
		return null;
	}

	public String getNativeString(Vector vector, FieldPos fieldpos) {
		StringBuffer stringbuffer = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			String s = MRCBuilder.matchNat(getField(vector, fieldpos, i * 2));
			if (s != null) {
				if (stringbuffer.length() > 0)
					stringbuffer.append(",");
				stringbuffer.append(s);
			}
		}

		return stringbuffer.toString();
	}

	public void buildCharacter(Vector vector) {
		if (vector != null) {
			character = new MRCharacter();
			character.setName(getField(vector, NAME));
			character.setCreator(getField(vector, CREATOR));
			character.setVulnerability(MRCBuilder.matchVul(getField(vector, VUL)));
			character.setSymbolMeaning(getField(vector, MEANING));
			boolean aflag[] = new boolean[8];
			for (int i = 0; i < 8; i++)
				aflag[i] = false;

			Chit achit[] = new Chit[12];
			for (int j = 0; j < 12; j++) {
				int k = 8 + j;
				RecordParser recordparser = (RecordParser) vector.elementAt(k);
				achit[j] = new Chit(recordparser);
				if (achit[j].getTextLine(0).toUpperCase().trim().equals("MAGIC")) {
					String s = achit[j].getTextLine(1).toUpperCase().trim();
					if (s.startsWith("III"))
						aflag[2] = true;
					else if (s.startsWith("IV"))
						aflag[3] = true;
					else if (s.startsWith("II"))
						aflag[1] = true;
					else if (s.startsWith("I"))
						aflag[0] = true;
					else if (s.startsWith("VIII"))
						aflag[7] = true;
					else if (s.startsWith("VII"))
						aflag[6] = true;
					else if (s.startsWith("VI"))
						aflag[5] = true;
					else if (s.startsWith("V"))
						aflag[4] = true;
				}
			}

			character.setChit(achit);
			character.setAllied(getNativeString(vector, ALLIED).toString());
			character.setFriendly(getNativeString(vector, FRIENDLY).toString());
			character.setUnfriendly(getNativeString(vector, UNFRIENDLY).toString());
			character.setEnemy(getNativeString(vector, ENEMY).toString());
			Vector vector1 = new Vector();
			for (int l = 0; l < 2; l++) {
				FieldPos fieldpos = new FieldPos(SPECIAL.getRow() + 1 + l, SPECIAL.getCol() + 1);
				String s1 = getField(vector, fieldpos);
				StringBuffer stringbuffer1 = new StringBuffer();
				for (int j1 = 2; j1 <= 7; j1++) {
					if (stringbuffer1.length() > 0)
						stringbuffer1.append(" ");
					stringbuffer1.append(getField(vector, fieldpos, j1));
				}

				vector1.addElement(new OutlineEntry(s1, stringbuffer1.toString()));
			}

			character.setSpecialAdvantages(vector1);
			StringBuffer stringbuffer = new StringBuffer();
			for (int i1 = 0; i1 <= 7; i1 += 2) {
				String s2 = getField(vector, STARTLOC, i1).toUpperCase().trim();
				if (s2.length() > 0) {
					if (stringbuffer.length() > 0)
						stringbuffer.append(",");
					stringbuffer.append("<b>" + s2 + "</b>");
				}
			}

			character.setStartingInfo(replaceLastComma(stringbuffer.toString()));
			StringBuffer stringbuffer2 = new StringBuffer();
			for (int k1 = 0; k1 < 8; k1++)
				if (aflag[k1]) {
					if (stringbuffer2.length() > 0)
						stringbuffer2.append(",");
					else
						stringbuffer2.append("Type ");
					switch (k1) {
						case 0: // '\0'
							stringbuffer2.append("I");
							break;

						case 1: // '\001'
							stringbuffer2.append("II");
							break;

						case 2: // '\002'
							stringbuffer2.append("III");
							break;

						case 3: // '\003'
							stringbuffer2.append("IV");
							break;

						case 4: // '\004'
							stringbuffer2.append("V");
							break;

						case 5: // '\005'
							stringbuffer2.append("VI");
							break;

						case 6: // '\006'
							stringbuffer2.append("VII");
							break;

						case 7: // '\007'
							stringbuffer2.append("VIII");
							break;
					}
				}

			String s3 = "(?)";
			if (stringbuffer2.length() > 0)
				s3 = replaceLastComma("(" + stringbuffer2.toString() + ")");
			String as[] = new String[4];
			for (int l1 = 0; l1 < 4; l1++) {
				FieldPos fieldpos1 = new FieldPos(DEVADD.getRow() + 1 + l1, DEVADD.getCol() + 1);
				StringBuffer stringbuffer3 = new StringBuffer();
				for (int i2 = 0; i2 < 7; i2 += 2) {
					String s4 = getField(vector, fieldpos1, i2).trim();
					if (s4.length() > 0 && stringbuffer.length() > 0)
						stringbuffer3.append(" ");
					stringbuffer3.append(s4);
				}

				String s5 = "";
				String s6 = getField(vector, fieldpos1, 7);
				try {
					Integer integer = Integer.valueOf(s6);
					if (integer.intValue() > 0)
						s5 = integer.toString().trim() + " <i>Spell" + plural(integer.intValue()) + "</i> " + s3;
				}
				catch (NumberFormatException _ex) {
				}
				as[l1] = stringbuffer3.toString() + "g" + " " + s5;
			}

			character.setDevAdd(as);
		}
	}

	public String replaceLastComma(String s) {
		int i = s.lastIndexOf(",");
		if (i >= 0)
			s = s.substring(0, i) + " and " + s.substring(i + 1);
		return s;
	}

	public String plural(int i) {
		if (i > 1)
			return "s";
		else
			return "";
	}

	public MRCharacter getCharacter() {
		return character;
	}

	public static final int CHIT_BASE = 8;
	public static final FieldPos NAME = new FieldPos(1, 3);
	public static final FieldPos CREATOR = new FieldPos(1, 5);
	public static final FieldPos VUL = new FieldPos(3, 4);
	public static final FieldPos MEANING = new FieldPos(2, 4);
	public static final int MAX_NPG = 4;
	public static final FieldPos ALLIED = new FieldPos(30, 2);
	public static final FieldPos FRIENDLY = new FieldPos(31, 2);
	public static final FieldPos UNFRIENDLY = new FieldPos(33, 2);
	public static final FieldPos ENEMY = new FieldPos(34, 2);
	public static final int MAX_SPECIAL = 2;
	public static final FieldPos SPECIAL = new FieldPos(38, 2);
	public static final FieldPos OPTIONAL = new FieldPos(40, 2);
	public static final FieldPos DEVADD = new FieldPos(24, 2);
	public static final FieldPos STARTLOC = new FieldPos(35, 2);
	private File file;
	private MRCharacter character;

}