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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class ApproveInventoryDialog extends AggressiveDialog {
	
	ArrayList<GameObject> toApprove;
	
	private JTable leftTable;
	private ItemTableModel leftTableModel;
	private JTable rightTable;
	private ItemTableModel rightTableModel;
	
	private JButton doneButton;
	
	public ApproveInventoryDialog(JFrame frame,ArrayList<GameObject> toApprove) {
		super(frame,"Approve Inventory",true);
		
		this.toApprove = toApprove;
		initComponents(toApprove);
		setLocationRelativeTo(frame);
	}
	private void initComponents(ArrayList<GameObject> toApprove) {
		setSize(800,600);
		getContentPane().setLayout(new BorderLayout());
		
		JLabel instruction = new JLabel("Click items to approve");
		instruction.setForeground(Color.red);
		instruction.setFont(new Font("Dialog",Font.BOLD,12));
		getContentPane().add(instruction,"North");
		
		JPanel centerPanel = new JPanel(new GridLayout(1,2));
		
		JPanel invPanel = new JPanel(new BorderLayout());
		invPanel.add(getTitleLabel("Rejected"),"North");
		leftTableModel = new ItemTableModel(new ArrayList<GameObject>());
		leftTable = new JTable(leftTableModel);
		leftTable.setDefaultRenderer(String.class, new InventoryTableStringRenderer(leftTable));
		leftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftTable.setRowHeight(100);
		leftTable.setFont(new Font("Dialog",Font.BOLD,18));
		leftTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				transfer(leftTable,rightTable);
			}
		});
		leftTableModel.updateRowHeights(leftTable);
		invPanel.add(new JScrollPane(leftTable),"Center");
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(getTitleLabel("Approved"),"North");
		rightTableModel = new ItemTableModel(toApprove);
		rightTable = new JTable(rightTableModel);
		rightTable.setDefaultRenderer(String.class, new InventoryTableStringRenderer(rightTable));
		rightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rightTable.setRowHeight(100);
		rightTable.setFont(new Font("Dialog",Font.BOLD,18));
		rightTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				transfer(rightTable,leftTable);
			}
		});
		rightTableModel.updateRowHeights(rightTable);
		rightPanel.add(new JScrollPane(rightTable),"Center");
		
		centerPanel.add(invPanel);
		centerPanel.add(rightPanel);
		getContentPane().add(centerPanel,"Center");
		
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(doneButton);
		getContentPane().add(box,"South");
	}
	
	public ArrayList<GameObject> getRejected() {
		return leftTableModel.getInventory();
	}
	
	public ArrayList<GameObject> getApproved() {
		return rightTableModel.getInventory();
	}
	
	private void transfer(JTable from,JTable to) {
		ItemTableModel fromModel = (ItemTableModel)from.getModel();
		ItemTableModel toModel = (ItemTableModel)to.getModel();
		int row = from.getSelectedRow();
		toModel.addInventory(fromModel.removeInventory(row));
		leftTableModel.updateRowHeights(leftTable);
		rightTableModel.updateRowHeights(rightTable);
	}
	private Box getTitleLabel(String title) {
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(Color.blue);
		titleLabel.setFont(new Font("Dialog",Font.BOLD,24));
		box.add(titleLabel);
		box.add(Box.createHorizontalGlue());
		return box;
	}
	
	private class ItemTableModel extends AbstractTableModel {
		private final String[] COL_HEADER = {
				"Name",
				" ",
		};
		private final Class[] COL_CLASS = {
				String.class,
				ImageIcon.class,
		};
		
		private ArrayList<GameObject> inventory;
		
		public ItemTableModel(ArrayList<GameObject> items) {
			inventory = new ArrayList<GameObject>();
			for (GameObject go:items) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc.isItem()) {
					inventory.add(go);
				}
			}
		}
		public ArrayList<GameObject> getInventory() {
			return inventory;
		}
		public GameObject removeInventory(int index) {
			GameObject go = (GameObject)inventory.remove(index);
			fireTableDataChanged();
			return go;
		}
		public void addInventory(GameObject go) {
			inventory.add(go);
			fireTableDataChanged();
		}
		public int getColumnCount() {
			return COL_HEADER.length;
		}
		public String getColumnName(int col) {
			return COL_HEADER[col];
		}
		public Class getColumnClass(int col) {
			return COL_CLASS[col];
		}
		public int getRowCount() {
			return inventory.size();
		}
		public Object getValueAt(int row, int col) {
			if (row<inventory.size()) {
				GameObject go = (GameObject)inventory.get(row);
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				switch(col) {
					case 0:
						String name = go.getName()+(rc.isActivated()?" (Activated)":"");
						return wrapString(name);
					case 1:
						return rc.getIcon();
				}
			}
			return null;
		}
		private String wrapString(String val) {
			return StringUtilities.findAndReplace(val," ","\n");
		}
		public void updateRowHeights(JTable table) {
			int maxIconColWidth = 0;
			for (int i=0;i<inventory.size();i++) {
				GameObject go = (GameObject)inventory.get(i);
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				
				ImageIcon icon = rc.getIcon();
				table.setRowHeight(i+1,icon.getIconHeight()+2);
				if (icon.getIconWidth()>maxIconColWidth) {
					maxIconColWidth = icon.getIconWidth();
				}
			}
		}
	}
	private static final Color background = UIManager.getColor("Table.background");
	private static final Color foreground = UIManager.getColor("Table.foreground");
	private static final Color selBackground = UIManager.getColor("Table.selectionBackground");
	private static final Color selForeground = UIManager.getColor("Table.selectionForeground");
	private class InventoryTableStringRenderer extends JTextArea implements TableCellRenderer {
		private JTable theTable;
		public InventoryTableStringRenderer(JTable table) {
			this.theTable = table;
			setEditable(false);
			setFont(new Font("Dialog",Font.PLAIN,20));
			setWrapStyleWord(true);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setBackground(isSelected?selBackground:background);
			setForeground(isSelected?selForeground:foreground);
			Rectangle rect = theTable.getCellRect(row,column,true);
			setSize(rect.width,rect.height);
			setText(value!=null?value.toString():"");
			return this;
		}
 	}
	
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		
		RealmLoader loader = new RealmLoader();
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		CharacterWrapper character = new CharacterWrapper(loader.getData().getGameObjectByName("Amazon"));
		character.setPlayerName("name");
		character.setCharacterLevel(4);
		character.fetchStartingInventory(new JFrame(),loader.getData(),false);
		
		ApproveInventoryDialog dialog = new ApproveInventoryDialog(new JFrame(),character.getInventory());
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		System.out.println("Rejected:");
		for (GameObject go:dialog.getRejected()) {
			System.out.println(go.getName());
		}
		System.out.println("Approved:");
		for (GameObject go:dialog.getApproved()) {
			System.out.println(go.getName());
		}
	}
}