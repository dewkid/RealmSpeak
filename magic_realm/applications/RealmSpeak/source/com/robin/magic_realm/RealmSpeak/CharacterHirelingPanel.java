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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.IconFactory;
import com.robin.general.util.HashLists;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.ClearingUtility;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CharacterHirelingPanel extends CharacterFramePanel {
	
	private JTable hirelingTable;
	private HirelingTableModel tableModel;
	private ArrayList hirelings;
	
	private JButton assignUnderlings;
	private JButton unassignUnderlings;
	private ArrayList<RealmComponent> selectedUnderlings;
	
	public CharacterHirelingPanel(CharacterFrame parent) {
		super(parent);
		init();
	}
	private void init() {
		updatePanel();
		
		// Table
		setLayout(new BorderLayout(5,5));
		tableModel = new HirelingTableModel();
		hirelingTable = new JTable(tableModel);
		hirelingTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
//		renderer.setFont(new Font("Dialog",Font.BOLD,20));
		hirelingTable.setFont(new Font("Dialog",Font.BOLD,20));
		hirelingTable.setDefaultRenderer(Integer.class,renderer);
		hirelingTable.setRowHeight(ChitComponent.M_CHIT_SIZE+10);
		hirelingTable.getColumnModel().getColumn(0).setMaxWidth(60);
		hirelingTable.getColumnModel().getColumn(1).setMaxWidth(60);
		hirelingTable.getColumnModel().getColumn(6).setMaxWidth(50);
		hirelingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		add(new JScrollPane(hirelingTable),"Center");
		
		// Buttons (placeholders for now)
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		assignUnderlings = new JButton("Assign Underlings");
		assignUnderlings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doAssign();
			}
		});
		box.add(assignUnderlings);
		unassignUnderlings = new JButton("Unassign Underlings");
		unassignUnderlings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doUnassign();
			}
		});
		box.add(unassignUnderlings);
		box.add(Box.createHorizontalGlue());
		add(box,"South");
		updateControls();
	}
	public void updatePanel() {
		hirelings = new ArrayList();
		ArrayList allHirelings = getCharacter().getAllHirelings();
		ArrayList fHirelings = getCharacter().getFollowingHirelings(); // likely the same group
		for (Iterator i=fHirelings.iterator();i.hasNext();) {
			RealmComponent fHireling = (RealmComponent)i.next();
			if (!allHirelings.contains(fHireling)) {
				allHirelings.add(fHireling);
			}
		}
		// Sort hirelings here
		Collections.sort(allHirelings,new Comparator() {
			public int compare(Object o1,Object o2) {
				RealmComponent r1 = (RealmComponent)o1;
				RealmComponent r2 = (RealmComponent)o2;
				
				String group1 = r1.getGameObject().getThisAttribute("native");
				if (group1==null) {
					group1 = r1.getGameObject().getName();
				}
				String group2 = r2.getGameObject().getThisAttribute("native");
				if (group2==null) {
					group2 = r1.getGameObject().getName();
				}
				
				int ret = group1.compareTo(group2);
				if (ret==0) {
					String rank1 = r1.getGameObject().getThisAttribute("rank");
					if (rank1==null) {
						rank1="0";
					}
					String rank2 = r2.getGameObject().getThisAttribute("rank");
					if (rank2==null) {
						rank2="0";
					}
					
					int nRank1 = "HQ".equals(rank1)?0:Integer.valueOf(rank1).intValue();
					int nRank2 = "HQ".equals(rank2)?0:Integer.valueOf(rank2).intValue();
					
					ret = nRank1 - nRank2;
				}
				
				return ret;
			}
		});
		for (Iterator i=allHirelings.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			hirelings.add(new HirelingDetailComponent(rc));
		}
		if (tableModel!=null) {
			tableModel.fireTableDataChanged();
		}
		updateControls();
	}
	public void updateControls() {
		if (hirelingTable!=null) {
			selectedUnderlings = new ArrayList<RealmComponent>();
			if (hirelingTable.getSelectedRowCount()>0) {
				int[] selRow = hirelingTable.getSelectedRows();
				for (int i=0;i<selRow.length;i++) {
					HirelingDetailComponent detail = (HirelingDetailComponent)hirelings.get(selRow[i]);
					if (!detail.realmComponent.isNativeLeader()) {
						if (detail.mine && !detail.captured) {
							selectedUnderlings.add(detail.realmComponent);
						}
						else {
							selectedUnderlings.clear();
							break;
						}
					}
				}
			}
			
			assignUnderlings.setEnabled(getCharacter().isActive() && getCharacter().isDoRecord() && !selectedUnderlings.isEmpty());
			unassignUnderlings.setEnabled(!selectedUnderlings.isEmpty());
		}
	}
	private void doUnassign() {
		if (selectedUnderlings.isEmpty()) return;
		
		int ret = JOptionPane.showConfirmDialog(
				getMainFrame(),
				"Are you sure you want to unassign the selected underlings?",
				"Unassign underlings",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
		if (ret!=JOptionPane.YES_OPTION) {
			return;
		}
		
		for (RealmComponent underling:selectedUnderlings) {
			TileLocation location = ClearingUtility.getTileLocation(underling);
			location.clearing.add(underling.getGameObject(),null);
		}
		updatePanel();
		getGameHandler().submitChanges();
		getGameHandler().updateCharacterFrames();
	}
	private void doAssign() {
		if (selectedUnderlings.isEmpty()) return;
		
		// Need to group underlings by location - assignment may not be the same for all
		HashLists underlingHash = new HashLists();
		for (Iterator i=selectedUnderlings.iterator();i.hasNext();) {
			RealmComponent underling = (RealmComponent)i.next();
			TileLocation location = ClearingUtility.getTileLocation(underling);
			if (location!=null && location.isInClearing()) { // This should always be true, I think
				underlingHash.put(location,underling);
			}
		}
		
		// Now, query each group of hirelings according to their clearing
		boolean wasQueried = false; // Need to know this, in case there are no characters/leaders to assign hirelings to
		for (Iterator i=underlingHash.keySet().iterator();i.hasNext();) {
			TileLocation location = (TileLocation)i.next();
			ArrayList list = underlingHash.getList(location);
			Collection guides = ClearingUtility.getGuidesInClearing(location);
			if (!guides.isEmpty()) {
				wasQueried = true;
				RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(
						getGameHandler().getMainFrame(),"Assign hirelings to which guide?",true);
				chooser.setForceColumns(3);
				chooser.setViewComponents(list);
				chooser.addRealmComponents(guides,false);
				chooser.addOption("none","None");
				chooser.setVisible(true);
				String text = chooser.getSelectedText();
				if (text!=null) {
					if ("None".equals(text)) {
						// Unassign
						for (Iterator n=list.iterator();n.hasNext();) {
							RealmComponent underling = (RealmComponent)n.next();
							location.clearing.add(underling.getGameObject(),null);
						}
					}
					else {
						// Assign to selected guide
						RealmComponent guide = chooser.getFirstSelectedComponent();
						boolean extraActions = false;
						for (Iterator n=list.iterator();n.hasNext();) {
							RealmComponent underling = (RealmComponent)n.next();
							guide.getGameObject().add(underling.getGameObject());
							if (underling.getGameObject().hasThisAttribute(Constants.EXTRA_ACTIONS)) {
								extraActions = true;
							}
						}
						if (extraActions && guide.isCharacter()) {
							CharacterWrapper characterGuide = new  CharacterWrapper(guide.getGameObject());
							characterGuide.setNeedsActionPanelUpdate(true);
						}
					}
				}
			}
		}
		
		if (!wasQueried) {
			JOptionPane.showMessageDialog(getGameHandler().getMainFrame(),
					"None of the selected hirelings are in a clearing with a guide.",
					"No Guides Found!",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			updatePanel();
			// Make sure assignments are submitted and updated
			getGameHandler().submitChanges();
			getGameHandler().updateCharacterFrames();
		}
	}
	private class HirelingTableModel extends AbstractTableModel {
		protected String[] columnName = {
			"Owner","Follow"," "," "," "," ","Term"
		};
		public HirelingTableModel() {
		}
		public int getRowCount() {
			return hirelings.size();
		}
		public int getColumnCount() {
			return columnName.length;
		}
		public String getColumnName(int column) {
			return columnName[column];
		}
		public Class getColumnClass(int column) {
			return column==6?Integer.class:ImageIcon.class;
		}
		public Object getValueAt(int row, int column) {
			if (row<hirelings.size()) {
				HirelingDetailComponent detail = (HirelingDetailComponent)hirelings.get(row);
				switch(column) {
					case 0:	return detail.getOwner();
					case 1:	return detail.getFollowing();
					case 2:	return detail.getSide1();
					case 3:	return detail.getSide2();
					case 4:	return detail.getHorseSide1();
					case 5:	return detail.getHorseSide2();
					case 6:	return detail.getTermOfHire();
				}
			}
			return null;
		}
	}
	private class HirelingDetailComponent {
		private RealmComponent realmComponent;
		public boolean mine; // true indicates that the character viewing this row is the owner
		public boolean captured;
		private ImageIcon owner;
		private ImageIcon following;
		private ImageIcon side1;
		private ImageIcon side2;
		private ImageIcon horseSide1;
		private ImageIcon horseSide2;
		private Integer termOfHire;
		public HirelingDetailComponent(RealmComponent rc) {
			this.realmComponent = rc;
			if (rc.isNative() || rc.isMonster() || rc.isTraveler()) {
				// Owner
				RealmComponent o = rc.getOwner();
				if (o==null) return;
				mine = getCharacter().getGameObject().equals(o.getGameObject());
				captured = rc.getGameObject().hasThisAttribute(Constants.CAPTURE);
				owner = o.getMediumIcon();
				
				// Following
				following = null; // by default
				GameObject heldBy = rc.getGameObject().getHeldBy();
				if (heldBy!=null) {
					RealmComponent h = RealmComponent.getRealmComponent(heldBy);
					if (h!=null && h.isPlayerControlledLeader()) {
						following = h.getMediumIcon();
					}
				}
				if (realmComponent.isAnyLeader()) {
					// Leaders never have follow assignments
					following = IconFactory.findIcon("icons/minus.gif");
				}
				
				ChitComponent chit = (ChitComponent)rc;
				side1 = chit.getIcon();
				side2 = rc.isTraveler()?null:chit.getFlipSideIcon();
				ChitComponent horse = (ChitComponent)rc.getHorse();
				if (horse!=null) {
					horseSide1 = horse.getIcon();
					horseSide2 = horse.getFlipSideIcon();
				}
				else {
					horseSide1 = null;
					horseSide2 = null;
				}
				termOfHire = null;
				if (!rc.getGameObject().hasThisAttribute("companion")) {
					termOfHire = new Integer(rc.getTermOfHire());
				}
			}
			else {
				throw new IllegalArgumentException("Not a native or a monster!");
			}
		}
		public ImageIcon getSide1() {
			return side1;
		}
		public ImageIcon getSide2() {
			return side2;
		}
		public ImageIcon getFollowing() {
			return following;
		}
		public ImageIcon getHorseSide1() {
			return horseSide1;
		}
		public ImageIcon getHorseSide2() {
			return horseSide2;
		}
		public ImageIcon getOwner() {
			return owner;
		}
		public Integer getTermOfHire() {
			return termOfHire;
		}
	}
}