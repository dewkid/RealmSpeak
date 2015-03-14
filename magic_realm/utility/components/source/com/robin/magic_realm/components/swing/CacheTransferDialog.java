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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.CacheChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class CacheTransferDialog extends AggressiveDialog {
	
	private ChangeListener listener;
	
	private CharacterWrapper character;
	private CharacterWrapper cache;
	
	private JTable inventoryTable;
	private CacheTableModel inventoryTableModel;
	private JTable cacheTable;
	private CacheTableModel cacheTableModel;
	
	private JButton doneButton;
	
	public CacheTransferDialog(JFrame frame,CharacterWrapper character,CharacterWrapper cache,ChangeListener listener) {
		super(frame,"Cache Transfer",true);
		
		this.listener = listener;
		
		this.character = character;
		this.cache = cache;
		
		initComponents();
		setLocationRelativeTo(frame);
	}
	private void initComponents() {
		setSize(800,600);
		getContentPane().setLayout(new BorderLayout());
		
		JLabel instruction = new JLabel("Click items to transfer");
		instruction.setForeground(Color.red);
		instruction.setFont(new Font("Dialog",Font.BOLD,12));
		getContentPane().add(instruction,"North");
		
		JPanel centerPanel = new JPanel(new GridLayout(1,2));
		
		JPanel invPanel = new JPanel(new BorderLayout());
		invPanel.add(getTitleLabel("Inventory"),"North");
		inventoryTableModel = new CacheTableModel(character,false);
		inventoryTable = new JTable(inventoryTableModel);
		inventoryTable.setDefaultRenderer(String.class, new CacheTableStringRenderer(inventoryTable));
		inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inventoryTable.setRowHeight(100);
		inventoryTable.setFont(new Font("Dialog",Font.BOLD,18));
		inventoryTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				transfer(inventoryTable,cacheTable);
			}
		});
		inventoryTableModel.updateRowHeights(inventoryTable);
		invPanel.add(new JScrollPane(inventoryTable),"Center");
		
		JPanel cachePanel = new JPanel(new BorderLayout());
		cachePanel.add(getTitleLabel("Cache"),"North");
		cacheTableModel = new CacheTableModel(cache,true);
		cacheTable = new JTable(cacheTableModel);
		cacheTable.setDefaultRenderer(String.class, new CacheTableStringRenderer(cacheTable));
		cacheTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cacheTable.setRowHeight(100);
		cacheTable.setFont(new Font("Dialog",Font.BOLD,18));
		cacheTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				transfer(cacheTable,inventoryTable);
			}
		});
		cacheTableModel.updateRowHeights(cacheTable);
		cachePanel.add(new JScrollPane(cacheTable),"Center");
		
		centerPanel.add(invPanel);
		centerPanel.add(cachePanel);
		getContentPane().add(centerPanel,"Center");
		
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				finalizeTransfer();
				setVisible(false);
			}
		});
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(doneButton);
		getContentPane().add(box,"South");
	}
	private void finalizeTransfer() {
		// transfer EVERYTHING from cache to character, and then transfer the cache back (if any)
		
		// Transfer ALL gold
		character.addGold(cache.getGold());
		cache.setGold(0.0);
		
		// Transfer appropriate gold back (if any)
		character.addGold(-cacheTableModel.getGold());
		cache.addGold(cacheTableModel.getGold());
		
		ArrayList<GameObject> cacheInventory = cache.getInventory();
		ArrayList<GameObject> cacheFinalInventory = cacheTableModel.getInventory();
		
		// First, transfer all items in cache that the character is taking
		ArrayList<GameObject> getting = new ArrayList<GameObject>(cacheInventory);
		getting.removeAll(cacheFinalInventory);
		RealmUtility.transferInventory((JFrame)parent, cache, character, getting, listener,false);
		cacheInventory.removeAll(getting);
		
		// Transfer appropriate items back (in order!)
		RealmUtility.transferInventory((JFrame)parent,character,cache,cacheFinalInventory,listener,false);
	}
	
	private void transfer(JTable from,JTable to) {
		CacheTableModel fromModel = (CacheTableModel)from.getModel();
		CacheTableModel toModel = (CacheTableModel)to.getModel();
		int row = from.getSelectedRow();
		if (row==0) {
			int gold = (int)fromModel.getGold();
			if (gold>0) {
				String transfer = JOptionPane.showInputDialog(
								"How much gold to transfer?",
								String.valueOf(gold));
				
				try {
					int tgold = Integer.valueOf(transfer).intValue();
					if (tgold<0 || tgold>gold) {
						JOptionPane.showMessageDialog(CacheTransferDialog.this,"Transfer amount must be a value from 0 to "+gold,"Error!",JOptionPane.ERROR_MESSAGE);
					}
					else {
						fromModel.addGold(-tgold);
						toModel.addGold(tgold);
					}
				}
				catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(CacheTransferDialog.this,"Transfer amount must be a number!","Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else {
			toModel.addInventory(fromModel.removeInventory(row-1));
		}
		inventoryTableModel.updateRowHeights(inventoryTable);
		cacheTableModel.updateRowHeights(cacheTable);
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
	
	private class CacheTableModel extends AbstractTableModel {
		private final String[] COL_HEADER = {
				"Position",
				"Name",
				" ",
		};
		private final Class[] COL_CLASS = {
				String.class,
				String.class,
				ImageIcon.class,
		};
		
		private double gold;
		private ArrayList<GameObject> inventory;
		private boolean includePosition;
		
		public CacheTableModel(CharacterWrapper bin,boolean includePosition) {
			inventory = new ArrayList<GameObject>();
			for (GameObject go:bin.getInventory()) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc.isItem()) {
					inventory.add(go);
				}
			}
			gold = bin.hasCurse(Constants.WITHER)?0:bin.getGold();
			this.includePosition = includePosition;
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
		public double getGold() {
			return gold;
		}
		public void addGold(double val) {
			gold += val;
			fireTableDataChanged();
		}

		public int getColumnCount() {
			return COL_HEADER.length - (includePosition?0:1);
		}
		public String getColumnName(int col) {
			return COL_HEADER[col + (includePosition?0:1)];
		}
		public Class getColumnClass(int col) {
			return COL_CLASS[col + (includePosition?0:1)];
		}
		public int getRowCount() {
			return inventory.size()+1;
		}
		public Object getValueAt(int row, int col) {
			if (row<inventory.size()+1) {
				int vgold = (int)Math.floor(gold);
				if (row==0) {
					switch(col + (includePosition?0:1)) {
						case 0:
							return vgold>0?"Top":"";
						case 1:
							return vgold+" gold";
						case 2:
							return null;
					}
				}
				else {
					GameObject go = (GameObject)inventory.get(row-1);
					RealmComponent rc = RealmComponent.getRealmComponent(go);
					switch(col + (includePosition?0:1)) {
						case 0:
							return vgold>0?String.valueOf(row+1):(row==1?"Top":String.valueOf(row));
						case 1:
							String name = go.getName()+(rc.isActivated()?" (Activated)":"");
							return wrapString(name);
						case 2:
							return rc.getIcon();
					}
				}
			}
			return null;
		}
		private String wrapString(String val) {
			return StringUtilities.findAndReplace(val," ","\n");
		}
		public void updateRowHeights(JTable table) {
			int maxIconColWidth = 0;
			table.setRowHeight(0,50);
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
	private class CacheTableStringRenderer extends JTextArea implements TableCellRenderer {
		private JTable theTable;
		public CacheTableStringRenderer(JTable table) {
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
		CharacterWrapper character = new CharacterWrapper(loader.getData().getGameObjectByName("Wizard"));
		character.setPlayerName("name");
		character.setGold(10);
		character.setCharacterLevel(4);
		character.fetchStartingInventory(new JFrame(),loader.getData(),false);
		
		GameObject go = character.getGameObject().getGameData().createNewObject();
		go.setName(character.getGameObject().getName()+"'s Cache");
		go.setThisAttribute(RealmComponent.CACHE_CHIT);
		go.setThisAttribute("clearing","1");
		go.setThisAttribute(RealmComponent.TREASURE_LOCATION,character.getGameObject().getName());
		go.setThisAttribute("cache_number",character.getNextCacheNumber());
		CharacterWrapper cacheCharacter = new CharacterWrapper(go);
		cacheCharacter.setPlayerName(character.getPlayerName());
		CacheChitComponent cache = (CacheChitComponent)RealmComponent.getRealmComponent(go);
		cache.setOwner(RealmComponent.getRealmComponent(character.getGameObject()));
		cache.setFaceUp();
		
		CacheTransferDialog dialog = new CacheTransferDialog(new JFrame(),character,cacheCharacter,null);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		System.out.println("Character gold = "+character.getGold());
		System.out.println("Cache gold = "+cacheCharacter.getGold());
		System.out.println("Character inv = "+character.getInventory());
		System.out.println("Cache inv = "+cacheCharacter.getInventory());
	}
}