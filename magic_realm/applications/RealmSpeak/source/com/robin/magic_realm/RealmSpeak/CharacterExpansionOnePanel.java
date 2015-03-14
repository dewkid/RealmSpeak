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
package com.robin.magic_realm.RealmSpeak;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.MagicRealmColor;
import com.robin.magic_realm.components.RealmComponent;

public class CharacterExpansionOnePanel extends CharacterFramePanel {
	protected ChitDiscoveryModel gateDiscoveryModel;
	protected ChitDiscoveryModel guildDiscoveryModel;
	protected TravelerInformationModel travelerTableModel;
	protected JTable gateTable;
	protected JTable guildTable;
	protected JTable travelerTable;
	public CharacterExpansionOnePanel(CharacterFrame parent) {
		super(parent);
		init();
	}
	private void init() {
		GamePool pool = getGameHandler().getGamePool();
		
		setLayout(new GridLayout(2,1,5,5));

		JPanel left = new JPanel(new GridLayout(1,2));
			JPanel leftTop = new JPanel(new BorderLayout());
				ArrayList<GameObject> gates = pool.find("gate");
				gateDiscoveryModel = new ChitDiscoveryModel("Gate",gates);
				gateTable = new JTable(gateDiscoveryModel);
				gateTable.setDefaultRenderer(String.class,new ChitDiscoveryRenderer(gates));
			leftTop.add(new JLabel("Celestial Gates:"),"North");
			leftTop.add(new JScrollPane(gateTable),"Center");
		left.add(leftTop);
			JPanel leftBottom = new JPanel(new BorderLayout());
				ArrayList<GameObject> guilds = pool.find("guild");
				guildDiscoveryModel = new ChitDiscoveryModel("Guild",guilds);
				guildTable = new JTable(guildDiscoveryModel);
				guildTable.setDefaultRenderer(String.class,new ChitDiscoveryRenderer(guilds));
			leftBottom.add(new JLabel("The Guilds:"),"North");
			leftBottom.add(new JScrollPane(guildTable),"Center");
		left.add(leftBottom);
		add(left);
		
		JPanel right = new JPanel(new BorderLayout());
		right.add(new JLabel("Traveller Information:"),"North");
		travelerTableModel = new TravelerInformationModel();
		travelerTable = new JTable(travelerTableModel);
		ComponentTools.lockColumnWidth(travelerTable,0,150);
		right.add(new JScrollPane(travelerTable));
		add(right);
	}
	public void updatePanel() {
		if (travelerTable!=null) {
			travelerTableModel.fireTableDataChanged();
		}
	}
	private class ChitDiscoveryModel extends AbstractTableModel {
		private String name;
		private ArrayList list;
		private ArrayList discoveryNamesList;
		public ChitDiscoveryModel(String name,ArrayList list) {
			this.name = name;
			this.list = list;
			discoveryNamesList = new ArrayList();
			for (Iterator i=list.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				discoveryNamesList.add(go.getName());
			}
		}
		public int getRowCount() {
			return list.size();
		}
		public int getColumnCount() {
			return 2;
		}
		public String getColumnName(int column) {
			switch(column) {
				case 0:
					return name;
				case 1:
					return "Location";
			}
			return "";
		}
		public Class getColumnClass(int column) {
			return String.class;
		}
		public Object getValueAt(int row, int column) {
			if (row<list.size()) {
				GameObject go = (GameObject)list.get(row);
				switch(column) {
					case 0:
						String name = go.getName();
						return name;
					case 1:
						if (go.hasThisAttribute("seen")) {
							RealmComponent rc = RealmComponent.getRealmComponent(go);
							return rc.getCurrentLocation().toString();
						}
						return "Unknown";
				}
			}
			return null;
		}
	}
	private class ChitDiscoveryRenderer extends DefaultTableCellRenderer {
		private ArrayList list;
		public ChitDiscoveryRenderer(ArrayList list) {
			this.list = list;
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int column) {
			setForeground(Color.black);
			setBackground(Color.white);
			setText((String)value);
			if (row<list.size()) {
				GameObject go = (GameObject)list.get(row);
				if (getCharacter().hasOtherChitDiscovery(go.getName())) {
					setBackground(MagicRealmColor.DISCOVERY_HIGHLIGHT_COLOR);
				}
			}
			if (column==0) setHorizontalAlignment(JLabel.LEFT);
			else setHorizontalAlignment(JLabel.CENTER);
			return this;
		}
	}
	private static String[] TRAV_INFO_COL = {
		"Traveler",
		"Information",
	};
	private class TravelerInformationModel extends AbstractTableModel {

		public int getColumnCount() {
			return TRAV_INFO_COL.length;
		}
		
		public String getColumnName(int column) {
			return TRAV_INFO_COL[column];
		}

		public Class getColumnClass(int column) {
			return String.class;
		}
		
		public int getRowCount() {
			return getGame().getTravelerKnowledge().size();
		}

		public Object getValueAt(int row, int col) {
			ArrayList<GameObject> travelers = getGame().getTravelerKnowledge();
			if (row<travelers.size()) {
				GameObject traveler = travelers.get(row);
				switch(col) {
					case 0:
						return traveler.getName();
					case 1:
						return traveler.getThisAttribute("text");
				}
			}
			return null;
		}
	}
}