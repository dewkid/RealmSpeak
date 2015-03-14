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
package com.robin.magic_realm.RealmQuestBuilder;

import java.util.*;

import javax.swing.table.DefaultTableModel;

import com.robin.game.objects.GameData;
import com.robin.general.swing.GameOption;
import com.robin.general.swing.GameOptionPane;
import com.robin.magic_realm.components.quest.Quest;
import com.robin.magic_realm.components.swing.HostGameSetupDialog;

public class RuleLimitationTableModel extends DefaultTableModel {
	private static String[] RuleLimitationHeader = { "On", "Off", "Rule Name" };
	private static Class[] RuleLimitationClass = { Boolean.class, Boolean.class, String.class };

	ArrayList<String> keys;
	Hashtable<String, String> descriptions;

	Quest quest;

	public RuleLimitationTableModel(Quest quest, GameData realmSpeakData) {
		this.quest = quest;

		keys = new ArrayList<String>();
		descriptions = new Hashtable<String, String>();

		HostGameSetupDialog dialog = new HostGameSetupDialog(null, null, realmSpeakData);
		GameOptionPane gop = dialog.getGameOptionPane();
		for (Iterator i = gop.getGameOptionKeys().iterator(); i.hasNext();) {
			String key = (String) i.next();
			keys.add(key);
		}
		for (String key : keys) {
			GameOption go = gop.getGameOption(key);
			if (go != null) {
				descriptions.put(key, go.getDescription());
			}
		}
	}

	public void setQuest(Quest quest) {
		this.quest = quest;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return RuleLimitationHeader.length;
	}

	public String getColumnName(int index) {
		return RuleLimitationHeader[index];
	}

	public Class getColumnClass(int index) {
		return RuleLimitationClass[index];
	}

	public int getRowCount() {
		return keys == null ? 0 : keys.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < getRowCount()) {
			String key = keys.get(rowIndex);
			switch (columnIndex) {
				// case 0:
				// return loc.getName();
				// case 1:
				// return loc.getLocationType().toString();
				case 2:
					return descriptions.get(key);
			}
		}
		return null;
	}

	public boolean isCellEditable(int row, int col) {
		return Boolean.class.equals(getColumnClass(col));
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}
}