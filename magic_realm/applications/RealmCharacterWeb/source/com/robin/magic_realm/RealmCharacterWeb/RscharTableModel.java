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
package com.robin.magic_realm.RealmCharacterWeb;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class RscharTableModel extends AbstractTableModel {
	
	private static final String[] COLUMN_NAME = {
		"Status",
		"File",
		"Creator",
		"Folder",
	};
	
	private ArrayList<RscharLayout> layoutRecords;
	
	public RscharTableModel() {
		layoutRecords = null;
	}
	public void setRecords(ArrayList<RscharLayout> layoutRecords) {
		this.layoutRecords = layoutRecords;
		fireTableDataChanged();
	}
	
	public Class getColumnClass() {
		return String.class;
	}

	public int getColumnCount() {
		return COLUMN_NAME.length;
	}
	
	public String getColumnName(int index) {
		return COLUMN_NAME[index];
	}

	public int getRowCount() {
		return layoutRecords==null?0:layoutRecords.size();
	}

	public Object getValueAt(int row,int col) {
		if (row<layoutRecords.size()) {
			RscharLayout rec = layoutRecords.get(row);
			switch(col) {
				case 0:
					return rec.getStatus();
				case 1:
					return rec.getFileName();
				case 2:
					return rec.getCharacter().getGameObject().getThisAttribute("creator");
				case 3:
					return rec.getWebFolder();
			}
		}
		return null;
	}
	public void formatColumns(JTable table) {
		table.getColumnModel().getColumn(0).setMaxWidth(50);
	}
}