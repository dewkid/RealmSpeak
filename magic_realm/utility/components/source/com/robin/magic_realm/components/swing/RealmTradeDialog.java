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
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class RealmTradeDialog extends AggressiveDialog {
	private RealmComponent trader = null; // could be null
	private ArrayList tradeComponents;
	private JTable tradeTable;
	private int maxHeight;
	
	private Collection selectedTradeComponents = null;
	
	private JButton cancelButton;
	private JButton okayButton;
	
	private boolean allowCancel;
	private boolean allowMultiple;
	private boolean forceSelection;
	
	private boolean revealAll;
	private boolean repairMode;
	
	private CharacterWrapper dealingCharacter;
	
	public RealmTradeDialog(JFrame parent,String title,boolean allowMultiple,boolean allowCancel,boolean forceSelection) {
		super(parent,title,true);
		this.allowMultiple = allowMultiple;
		this.allowCancel = allowCancel;
		this.forceSelection = forceSelection;
		this.revealAll = true;
		this.repairMode = false;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	public void setRevealAll(boolean val) {
		revealAll = val;
	}
	public void setRepairMode(boolean val) {
		repairMode = val;
	}
	public void setDealingCharacter(CharacterWrapper character) {
		dealingCharacter = character;
	}
	public RealmComponent getFirstSelectedRealmComponent() {
		ArrayList<RealmComponent> sel = getSelectedRealmComponents();
		if (sel!=null && sel.size()>0) {
			return sel.get(0);
		}
		return null;
	}
	public ArrayList<RealmComponent> getSelectedRealmComponents() {
		if (selectedTradeComponents!=null) {
			ArrayList<RealmComponent> ret = new ArrayList<RealmComponent>();
			for (Iterator i=selectedTradeComponents.iterator();i.hasNext();) {
				TradeComponent tradeComponent = (TradeComponent)i.next();
				ret.add(tradeComponent.getRealmComponent());
			}
			return ret;
		}
		return null;
	}
	public ArrayList<GameObject> getSelectedObjects() {
		if (selectedTradeComponents!=null) {
			ArrayList<GameObject> ret = new ArrayList<GameObject>();
			for (Iterator i=selectedTradeComponents.iterator();i.hasNext();) {
				TradeComponent tradeComponent = (TradeComponent)i.next();
				ret.add(tradeComponent.getRealmComponent().getGameObject());
			}
			return ret;
		}
		return null;
	}
	public void setTradeComponents(Collection components) {
		setTradeComponents(components,true);
	}
	public void setTradeComponents(Collection components,boolean sort) {
		maxHeight = 0;
		tradeComponents = new ArrayList();
		for (Iterator i=components.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			TradeComponent tc = new TradeComponent(rc);
			if (tc.getSide1().getIconHeight()>maxHeight) {
				maxHeight = tc.getSide1().getIconHeight();
			}
			tradeComponents.add(tc);
		}
		buildDialog(sort);
	}
	public void setTradeObjects(Collection objects) {
		maxHeight = 0;
		tradeComponents = new ArrayList();
		for (Iterator i=objects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			TradeComponent tc = new TradeComponent(rc);
			if (tc.getSide1().getIconHeight()>maxHeight) {
				maxHeight = tc.getSide1().getIconHeight();
			}
			tradeComponents.add(tc);
		}
		buildDialog(true);
	}
	private void buildDialog(boolean sort) {
		if (sort) {
			Collections.sort(tradeComponents,new Comparator() {
				public int compare(Object o1,Object o2) {
					TradeComponent t1 = (TradeComponent)o1;
					int bp1 = t1.getBasePrice()==null?0:t1.getBasePrice();
					TradeComponent t2 = (TradeComponent)o2;
					int bp2 = t2.getBasePrice()==null?0:t2.getBasePrice();
					return bp1-bp2;
				}
			});
		}
		getContentPane().setLayout(new BorderLayout());
		setSize(800,600);
		tradeTable = new JTable(new TradeTableModel());
		if (allowMultiple) {
			tradeTable.setSelectionModel(new DefaultListSelectionModel() {
				public void setSelectionInterval(int i0,int i1) {
					if (i0==i1) {
						if (isSelectedIndex(i0)) {
							removeSelectionInterval(i0,i1);
						}
						else {
							addSelectionInterval(i0,i1);
						}
					}
					else {
						addSelectionInterval(i0,i1);
					}
				}
			});
		}
		tradeTable.setFont(new Font("Dialog",Font.BOLD,24));
		tradeTable.setDefaultRenderer(String.class,new TradeTableStringRenderer());
		tradeTable.setDefaultRenderer(Integer.class,new TradeTableIntegerRenderer());
		int maxIconColWidth = 0;
		for (int i=0;i<tradeComponents.size();i++) {
			TradeComponent tc = (TradeComponent)tradeComponents.get(i);
			tradeTable.setRowHeight(i,tc.getSide1().getIconHeight()+2);
			if (tc.getSide1().getIconWidth()>maxIconColWidth) {
				maxIconColWidth = tc.getSide1().getIconWidth();
			}
		}
		maxIconColWidth += 5;
		int maxPriceWidth = 80;
		tradeTable.getColumnModel().getColumn(1).setMinWidth(maxPriceWidth);
		tradeTable.getColumnModel().getColumn(1).setMaxWidth(maxPriceWidth);
		tradeTable.getColumnModel().getColumn(2).setMinWidth(maxIconColWidth);
		tradeTable.getColumnModel().getColumn(2).setMaxWidth(maxIconColWidth);
		tradeTable.getColumnModel().getColumn(3).setMinWidth(maxIconColWidth);
		tradeTable.getColumnModel().getColumn(3).setMaxWidth(maxIconColWidth);
		tradeTable.setSelectionMode(allowMultiple?ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:ListSelectionModel.SINGLE_SELECTION);
		tradeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				updateControls();
			}
		});
		if (!allowMultiple) {
			tradeTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent ev) {
					if (ev.getClickCount()==2) {
						int selRow = tradeTable.getSelectedRow();
						if (selRow>=0) {
							selectedTradeComponents = new ArrayList();
							selectedTradeComponents.add((TradeComponent)tradeComponents.get(selRow));
							close();
						}
					}
				}
			});
		}
		getContentPane().add(new JScrollPane(tradeTable),"Center");
		
		Box line = Box.createHorizontalBox();
		line.add(Box.createHorizontalGlue());
		if (allowCancel) {
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						selectedTradeComponents = null;
						close();
					}
				});
			line.add(cancelButton);
		}
			okayButton = new JButton("Okay");
			okayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					selectedTradeComponents = new ArrayList();
					int[] selRow = tradeTable.getSelectedRows();
					for (int i=0;i<selRow.length;i++) {
						selectedTradeComponents.add((TradeComponent)tradeComponents.get(selRow[i]));
					}
					close();
				}
			});
		line.add(okayButton);
		getContentPane().add(line,"South");
		
		setLocationRelativeTo(parent);
		
		updateControls();
	}
	private void close() {
		setVisible(false);
		dispose();
	}
	private boolean actionableItemsToTrade() {
		if (tradeTable.getSelectedRowCount()==0) return false;

		for (int selRow:tradeTable.getSelectedRows()) {
			TradeComponent tc = (TradeComponent)tradeComponents.get(selRow);
			if (tc.isNoDeal()) return false;
		}
		
		return true;
	}
	private void updateControls() {
		okayButton.setEnabled(!forceSelection || actionableItemsToTrade());
	}
	private class TradeComponent {
		private RealmComponent realmComponent;
		private ImageIcon side1;
		private ImageIcon side2;
		private Integer basePrice;
		private boolean noDeal = false;
		public TradeComponent(RealmComponent rc) {
			this.realmComponent = rc;
			side1 = rc.getIcon();
			if (revealAll && rc.isTreasure()) {
				TreasureCardComponent card = (TreasureCardComponent)rc;
				if (!card.isFaceUp()) {
					side1 = card.getFlipSideIcon();
				}
			}
			if (rc.isHorse() || rc.isWeapon() || rc.isNative() || rc.isMonster() || rc.isMonsterPart() || rc.isNativeHorse()) {
				ChitComponent chit = (ChitComponent)rc;
				side2 = chit.getFlipSideIcon();
			}
			else {
				side2 = null;
			}
			if (repairMode && rc.isArmor() && ((ArmorChitComponent)rc).isDamaged()) {
				basePrice = new Integer(TreasureUtility.getBaseRepairPrice((ArmorChitComponent)rc));
			}
			else {
				basePrice = new Integer(TreasureUtility.getBasePrice(trader,rc));
			}
			if (dealingCharacter!=null && rc.isSpell() && !dealingCharacter.canLearn(rc.getGameObject())) {
				noDeal = true;
			}
			if (basePrice==0) {
				if (!realmComponent.getGameObject().hasThisAttribute("base_price") && !realmComponent.isArmor()) {
					basePrice = null;
				}
			}
		}
		public String toString() {
			return realmComponent.toString();
		}
		public RealmComponent getRealmComponent() {
			return realmComponent;
		}
		public ImageIcon getSide1() {
			return side1;
		}
		public ImageIcon getSide2() {
			return side2;
		}
		public Integer getBasePrice() {
			return basePrice;
		}
		public boolean isNoDeal() {
			return noDeal;
		}
		public String getOtherInformation() {
			StringBuffer sb = new StringBuffer();
			if (realmComponent.isWeapon()) {
				sb.append("Length="+realmComponent.getGameObject().getThisAttribute("length"));
			}
			else if (realmComponent.isArmor()) {
				if (realmComponent.getGameObject().hasAttribute("destroyed","base_price")) {
					int bp = realmComponent.getGameObject().getAttributeInt("destroyed","base_price");
					sb.append("DestroyedValue="+bp);
				}
			}
			int fame = realmComponent.getGameObject().getThisInt("fame");
			int not = realmComponent.getGameObject().getThisInt("notoriety");
			if (fame>0) {
				sb.append("\nFame="+fame);
			}
			if (not>0) {
				sb.append("\nNotoriety="+not);
			}
			return sb.toString();
		}
	}
	
	private static final Color background = UIManager.getColor("Table.background");
	private static final Color foreground = UIManager.getColor("Table.foreground");
	private static final Color selBackground = UIManager.getColor("Table.selectionBackground");
	private static final Color selForeground = UIManager.getColor("Table.selectionForeground");
	private class TradeTableStringRenderer extends JTextArea implements TableCellRenderer {
		public TradeTableStringRenderer() {
			setEditable(false);
			setFont(new Font("Dialog",Font.PLAIN,20));
			setWrapStyleWord(true);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setBackground(isSelected?selBackground:background);
			setForeground(isSelected?selForeground:foreground);
			Rectangle rect = tradeTable.getCellRect(row,column,true);
			setSize(rect.width,rect.height);
			setText(value!=null?value.toString():"");
			return this;
		}
 	}
	
	private class TradeTableIntegerRenderer extends DefaultTableCellRenderer {
		public TradeTableIntegerRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			TradeComponent tc = (TradeComponent)tradeComponents.get(row);
			if (tc.isNoDeal()) {
				cell.setForeground(Color.red);
			}
			else {
				cell.setForeground(Color.black);
			}
			return cell;
		}
	}
	
	private class TradeTableModel extends AbstractTableModel {
		private final String[] COL_HEADER = {
				"Name",
				"Base Price",
				"Side 1",
				"Side 2",
				" "
		};
		private final Class[] COL_CLASS = {
				String.class,
				Integer.class,
				ImageIcon.class,
				ImageIcon.class,
				String.class
		};
		
		public TradeTableModel() {
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
			return tradeComponents==null?0:tradeComponents.size();
		}
		public Object getValueAt(int row, int col) {
			if (row<getRowCount()) {
				TradeComponent dsc = (TradeComponent)tradeComponents.get(row);
				RealmComponent rc = dsc.getRealmComponent();
				boolean showAll = revealAll || !rc.isTreasure() || ((TreasureCardComponent)rc).isFaceUp();
				switch(col) {
					case 0:
						if (dsc.isNoDeal()) return "Not Available";
						String name = showAll?rc.getGameObject().getName()+(rc.isActivated()?" (Activated)":""):"Treasure";
						return wrapString(name);
					case 1:
						return showAll?dsc.getBasePrice():null;
					case 2:
						return dsc.getSide1();
					case 3:
						return dsc.getSide2();
					case 4:
						return showAll?wrapString(dsc.getOtherInformation()):null;
				}
			}
			return null;
		}
		private String wrapString(String val) {
			return StringUtilities.findAndReplace(val," ","\n");
		}
	}
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();

		System.out.print("loading...");
		RealmLoader loader = new RealmLoader();
		System.out.println("Done");
		
		ArrayList objects = new ArrayList();
		GamePool pool = new GamePool(loader.getData().getGameObjects());
		objects.addAll(pool.find("armor,!character"));
		objects.addAll(pool.find("horse,!native"));
		objects.addAll(pool.find("weapon,!character"));
		
		JFrame frame = new JFrame();
		frame.setLocationRelativeTo(null);
		RealmTradeDialog dialog = new RealmTradeDialog(frame,"Sell these",true,true,true);
		dialog.setTradeObjects(objects);
		dialog.setVisible(true);
		
		System.out.println("Selected = "+dialog.getSelectedRealmComponents());
		
		System.exit(0);
	}
	public void setTrader(RealmComponent trader) {
		this.trader = trader;
	}
}