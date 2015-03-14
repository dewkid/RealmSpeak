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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

public class NativeFriendlinessTable extends JTable {

	public NativeFriendlinessTable(String as[]) {
		this(as, null);
	}

	public NativeFriendlinessTable(String as[], int ai[]) {
		data = as;
		friendliness = ai;
		if (friendliness == null) {
			friendliness = new int[data.length];
			reset();
			((AbstractTableModel) getModel()).fireTableDataChanged();
		}
		init();
	}

	public void reset() {
		for (int i = 0; i < friendliness.length; i++)
			friendliness[i] = 3;

	}

	public void setAllied(String s) {
		setAllNamesForType(s, 5);
	}

	public void setFriendly(String s) {
		setAllNamesForType(s, 4);
	}

	public void setUnfriendly(String s) {
		setAllNamesForType(s, 2);
	}

	public void setEnemy(String s) {
		setAllNamesForType(s, 1);
	}

	private void setAllNamesForType(String s, int i) {
		StringTokenizer stringtokenizer = new StringTokenizer(s, ",");
		for (int j = 0; stringtokenizer.hasMoreTokens(); j++) {
			int k = indexForName(stringtokenizer.nextToken().trim());
			if (k >= 0) {
				friendliness[k] = i;
				((AbstractTableModel) getModel()).fireTableRowsUpdated(k, k);
			}
		}

	}

	private int indexForName(String s) {
		for (int i = 0; i < data.length; i++)
			if (s.indexOf(data[i]) >= 0)
				return i;

		return -1;
	}

	public String getAllied() {
		return getAllNamesForType(5);
	}

	public String getFriendly() {
		return getAllNamesForType(4);
	}

	public String getUnfriendly() {
		return getAllNamesForType(2);
	}

	public String getEnemy() {
		return getAllNamesForType(1);
	}

	private String getAllNamesForType(int i) {
		StringBuffer stringbuffer = new StringBuffer();
		for (int j = 0; j < friendliness.length; j++)
			if (friendliness[j] == i) {
				if (stringbuffer.length() > 0)
					stringbuffer.append(", ");
				stringbuffer.append(data[j]);
			}

		return stringbuffer.toString();
	}

	public void init() {
		setModel(new AbstractTableModel() {

			public int getRowCount() {
				return data.length;
			}

			public int getColumnCount() {
				return 6;
			}

			public String getColumnName(int j) {
				switch (j) {
					case 0: // '\0'
						return "Name";

					case 5: // '\005'
						return "A";

					case 4: // '\004'
						return "F";

					case 3: // '\003'
						return "N";

					case 2: // '\002'
						return "U";

					case 1: // '\001'
						return "E";
				}
				return "";
			}

			public Object getValueAt(int j, int k) {
				if (data != null && j < data.length && k < 6)
					switch (k) {
						case 0: // '\0'
							return data[j];

						case 1: // '\001'
						case 2: // '\002'
						case 3: // '\003'
						case 4: // '\004'
						case 5: // '\005'
							if (friendliness[j] == k)
								return getFriendlinessName(k);
							break;
					}
				return "";
			}

		});
		for (int i = 0; i < 6; i++) {
			TableColumn tablecolumn = getColumnModel().getColumn(i);
			if (i == 0)
				tablecolumn.setPreferredWidth(200);
			else
				tablecolumn.setPreferredWidth(5);
		}

		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent mouseevent) {
			}

			public void mouseEntered(MouseEvent mouseevent) {
			}

			public void mouseExited(MouseEvent mouseevent) {
			}

			public void mousePressed(MouseEvent mouseevent) {
				java.awt.Point point = mouseevent.getPoint();
				int j = rowAtPoint(point);
				int k = columnAtPoint(point);
				if (k > 0) {
					friendliness[j] = k;
					((AbstractTableModel) getModel()).fireTableRowsUpdated(j, j);
				}
			}

			public void mouseReleased(MouseEvent mouseevent) {
			}

		});
		setRowSelectionAllowed(false);
	}

	public String getFriendlinessName(int i) {
		switch (i) {
			case 5: // '\005'
				return "Allied";

			case 4: // '\004'
				return "Friendly";

			case 3: // '\003'
				return "Neutral";

			case 2: // '\002'
				return "Unfriendly";

			case 1: // '\001'
				return "Enemy";
		}
		return "";
	}

	public static final int NAME = 0;
	public static final int ENEMY = 1;
	public static final int UNFRIENDLY = 2;
	public static final int NEUTRAL = 3;
	public static final int FRIENDLY = 4;
	public static final int ALLIED = 5;
	private String data[];
	private int friendliness[];

}