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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.*;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.*;

public class SpellViewer extends JPanel {
	
	private JLabel spellIcon;
	private JLabel spellTable;
	private JEditorPane spellDetail;
	private JList spellList;
	private SpellListModel spellListModel;
	private JCheckBox typeOption;
	
	private ArrayList<GameObject> spells;
	
	public SpellViewer(GameData data) {
		initComponents();
		initView(data);
		spellList.setSelectedIndex(0);
		updateView();
	}
	private void initComponents() {
		ComponentTools.lockComponentSize(this,1000,600);
		setLayout(new BorderLayout());
		add(getListPanel(),BorderLayout.WEST);
		add(getCenterPanel(),BorderLayout.CENTER);
	}
	private JPanel getListPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		spellListModel = new SpellListModel();
		spellList = new JList(spellListModel);
		spellList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spellList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateView();
				}
			}
		});
		panel.add(new JScrollPane(spellList),BorderLayout.CENTER);
		typeOption = new JCheckBox("Order by Type");
		typeOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				resortList();
			}
		});
		panel.add(typeOption,BorderLayout.SOUTH);
		return panel;
	}
	private void resortList() {
		spellList.clearSelection();
		Collections.sort(spells,new Comparator<GameObject>() {
			public int compare(GameObject go1,GameObject go2) {
				return spellNaming(go1).compareTo(spellNaming(go2));
			}
		});
		repaint();
	}
	private void updateView() {
		int sel = spellList.getSelectedIndex();
		if (sel>=0) {
			GameObject go = spells.get(sel);
			spellIcon.setIcon(RealmComponent.getRealmComponent(go).getFaceUpIcon());
			spellDetail.setText(SpellUtility.getSpellDetail(go));
			spellDetail.setCaretPosition(0);
			ImageIcon table = SpellUtility.getSpellDetailTable(go);
			spellTable.setIcon(table);
			repaint();
		}
	}
	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new BorderLayout(20,20));
		
		spellDetail = new JEditorPane("text/rtf","");
		spellTable = new JLabel();
		panel.add(spellTable,BorderLayout.SOUTH);
		
		JScrollPane sp = new JScrollPane(spellDetail);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(sp,BorderLayout.CENTER);
		spellIcon = new JLabel();
		JPanel left = new JPanel(new BorderLayout());
		left.add(spellIcon,BorderLayout.NORTH);
		panel.add(left,BorderLayout.WEST);
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		return panel;
	}
	private void initView(GameData data) {
		GamePool pool = new GamePool(data.getGameObjects());
		spells = new ArrayList<GameObject>();
		
		for (GameObject go:pool.find("spell")) {
			String type = go.getThisAttribute("spell");
			if (type.length()>0 && !type.equals("*")) {
				spells.add(go);
			}
		}
		resortList();
	}
	private String spellNaming(GameObject go) {
		if (typeOption.isSelected()) {
			return go.getThisAttribute("spell")+" - "+go.getName();
		}
		return go.getName()+" ("+go.getThisAttribute("spell")+")";
	}
	
	private class SpellListModel extends AbstractListModel {

		public Object getElementAt(int index) {
			return spellNaming(spells.get(index));
		}

		public int getSize() {
			return spells.size();
		}
	}
	
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		RealmLoader loader = new RealmLoader();
		SpellViewer viewer = new SpellViewer(loader.getData());
		JFrame dummy = new JFrame();
		JOptionPane.showMessageDialog(dummy,viewer);
		System.exit(0);
	}
}