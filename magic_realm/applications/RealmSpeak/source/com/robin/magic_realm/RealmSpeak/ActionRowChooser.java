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
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import com.robin.general.swing.AggressiveDialog;

public class ActionRowChooser extends AggressiveDialog {
	
	private JLabel tally;
	private int toLose;
	private int lost;
	
	private ActionRowTableModel keepModel;
	private ArrayList<ActionRow> keepList;
	
	private ActionRowTableModel loseModel;
	private ArrayList<ActionRow> loseList;
	
	private JButton doneButton;
	
	private ActionRow emptyRow = new ActionRow("   ","");
	
	public ActionRowChooser(JFrame parent,String title,int toLose) {
		super(parent,title,true);
		this.toLose = toLose;
		lost = 0;
		keepList = new ArrayList<ActionRow>();
		loseList = new ArrayList<ActionRow>();
		initComponents();
		updateTally();
	}
	public void setActionRows(ArrayList<ActionRow> in) {
		keepList.clear();
		loseList.clear();
		for (ActionRow ar:in) {
			int count = ar.getCount();
			for (int i=0;i<count;i++) {
				keepList.add(ar);
			}
		}
		for (int i=0;i<keepList.size();i++) {
			loseList.add(emptyRow);
		}
		keepModel.fireTableDataChanged();
		loseModel.fireTableDataChanged();
		repaint();
	}
	private void updateTally() {
		StringBuffer sb = new StringBuffer();
		int left = toLose-lost;
		if (left<0) left=0;
		sb.append("Choose ");
		sb.append(left);
		sb.append(" phase");
		sb.append(left==1?"":"s");
		sb.append(" to lose.");
		tally.setText(sb.toString());
		doneButton.setEnabled(left==0);
	}
	private void initComponents() {
		setSize(640,480);
		getContentPane().setLayout(new BorderLayout());
		JPanel split = new JPanel(new GridLayout(1,2));
		keepModel = new ActionRowTableModel(keepList);
		loseModel = new ActionRowTableModel(loseList);
		split.add(createTablePanel(keepModel,"Keep",Color.blue));
		split.add(createTablePanel(loseModel,"Lose",Color.red));
		getContentPane().add(split,"Center");
		
		JPanel controlPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Click rows to move actions between tables",JLabel.CENTER);
		controlPanel.add(label,"Center");
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				finish();
			}
		});
		controlPanel.add(doneButton,"East");
		getContentPane().add(controlPanel,"South");
		
		tally = new JLabel("",JLabel.CENTER);
		tally.setFont(new Font("Dialog",Font.BOLD,18));
		getContentPane().add(tally,"North");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	private void finish() {
		for (ActionRow ar:loseList) {
			if (ar!=emptyRow) {
				if (ar.getCount()>1) {
					ar.setCount(ar.getCount()-1);
				}
				else {
					ar.setCancelled(true);
				}
			}
		}
		setVisible(false);
	}
	private void moveRow(int row) {
		if (row>=0 && row<keepList.size()) {
			ActionRow keep = keepList.get(row);
			ActionRow lose = loseList.get(row);
			boolean losing = lose==emptyRow;
			int count = keep==emptyRow?lose.getPhaseCount():keep.getPhaseCount();
			
			if (losing && ((lost+count-1)>toLose || lost==toLose)) {
				return;
			}
			
			keepList.set(row,lose);
			loseList.set(row,keep);
			if (losing) {
				lost += count;
			}
			else {
				lost -= count;
			}
			updateTally();
			repaint();
		}
	}
	private JPanel createTablePanel(ActionRowTableModel model,String title,Color titleColor) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(model.createTable()),"Center");
		JLabel label = new JLabel(title,JLabel.CENTER);
		label.setFont(new Font("Dialog",Font.BOLD,18));
		label.setForeground(titleColor);
		panel.add(label,"North");
		return panel;
	}
	private static final String[] ACTION_ROW_HEADER = { "Action", "Description","Phases"};
	private static final Class[] ACTION_ROW_CLASS = { ImageIcon.class, String.class, Integer.class };

	private class ActionRowTableModel extends AbstractTableModel {
		private ArrayList<ActionRow> list;
		public ActionRowTableModel(ArrayList<ActionRow> list) {
			this.list = list;
		}

		public int getColumnCount() {
			return ACTION_ROW_HEADER.length;
		}

		public String getColumnName(int index) {
			return ACTION_ROW_HEADER[index];
		}

		public Class getColumnClass(int index) {
			return ACTION_ROW_CLASS[index];
		}

		public int getRowCount() {
			return list.size();
		}

		public Object getValueAt(int row, int col) {
			if (row < list.size()) {
				ActionRow ar = list.get(row);
				if (ar!=emptyRow) {
					switch (col) {
						case 0:
							return ar.getIcon();
						case 1:
							int count = ar.getCount();
							ar.setCount(1);
							String s = ar.getDescription();
							ar.setCount(count);
							return s;
						case 2:
							return ar.getPhaseCount();
					}
				}
			}
			return null;
		}
		public JTable createTable() {
			JTable table = new JTable(this);
			table.setRowHeight(32);
			table.getColumnModel().getColumn(0).setMaxWidth(45);
			table.getColumnModel().getColumn(2).setMaxWidth(45);
			table.setFocusable(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent ev) {
					JTable source = (JTable)ev.getSource();
					int selRow = source.getSelectedRow();
					if (selRow>=0) {
						source.clearSelection();
						moveRow(selRow);
					}
				}
				public void mouseReleased(MouseEvent ev) {
					JTable source = (JTable)ev.getSource();
					source.clearSelection();
				}
				public void mouseExited(MouseEvent ev) {
					JTable source = (JTable)ev.getSource();
					source.clearSelection();
				}
			});
			return table;
		}
	}
	public static void main(String[] args) {
		ActionRowChooser chooser = new ActionRowChooser(new JFrame(),"Test",2);
		chooser.setLocationRelativeTo(null);
		ArrayList<ActionRow> list = new ArrayList<ActionRow>();
		list.add(new ActionRow("H",""));
		ActionRow ar = new ActionRow("R","");
		ar.setCount(4);
		list.add(ar);
		list.add(new ActionRow("A,A",""));
		list.add(new ActionRow("S",""));
		list.add(new ActionRow("T",""));
		chooser.setActionRows(list);
		chooser.setVisible(true);
	}
}