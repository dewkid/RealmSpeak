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
package com.robin.game.GameBuilder;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ColumnSizable;

public class GameObjectTableModel extends AbstractTableModel implements ColumnSizable {

	protected String[] columnHeaders = {
		"ID",
		"Object name",
		"Held by",
		"Holds"
	};
	
	protected ArrayList data;

	public GameObjectTableModel(ArrayList data) {
		this.data = data;
	}
	public int getRowCount() {
		return data.size();
	}
	public int getColumnCount() {
		return columnHeaders.length;
	}
	public String getColumnName(int col) {
		return columnHeaders[col];
	}
	public Class getColumnClass(int col) {
		switch(col) {
			case 0:
				return Long.class;
			case 3:
				return Integer.class;
			default:
				return String.class;
		}
	}
	public Object getValueAt(int row,int col) {
		if (row<data.size()) {
			GameObject obj = (GameObject)data.get(row);
			switch(col) {
				case 0:
					return new Long(obj.getId());
				case 1:
					return obj.getName();
				case 2:
					GameObject hb = obj.getHeldBy();
					return hb==null?"":hb.toString();
				case 3:
					return new Integer(obj.getHoldCount());
				default:
					throw new IllegalArgumentException("Invalid column index");
			}
		}
		return null;
	}
	
	// ColumnSizable interface
	public void setTableHeaderSize(JTable table) {
		TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(0).setMaxWidth(40);
		colModel.getColumn(3).setMaxWidth(40);
	}
}