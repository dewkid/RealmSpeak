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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.robin.general.swing.IconFactory;

public abstract class QuestTableEditorPanel extends JPanel {
	
	private Action addAction;
	private Action editAction;
	private Action deleteAction;
	
	public abstract void add();
	public abstract void edit();
	public abstract void delete();
	
	private Action moveUpAction;
	private Action moveDownAction;
	private JTable table;
	
	public void moveUp() {
		// override
	}
	public void moveDown() {
		// override
	}
	
	public QuestTableEditorPanel(String toolbarName,JTable table) {
		this(toolbarName,table,false);
	}
	public QuestTableEditorPanel(String toolbarName,JTable table,boolean useMovers) {
		initComponents(toolbarName,table,useMovers);
	}
	private void initComponents(String toolbarName,JTable table,boolean useMovers) {
		this.table = table;
		setLayout(new BorderLayout());
		add(buildToolbar(toolbarName,useMovers),BorderLayout.NORTH);
		add(new JScrollPane(table));
	}
	private JToolBar buildToolbar(String toolbarName,boolean useMovers) {
		JToolBar toolbar = new JToolBar(toolbarName);
		addAction = new AbstractAction("Add",IconFactory.findIcon("icons/plus.gif")) {
			public void actionPerformed(ActionEvent e) {
				add();
				table.clearSelection();
				int sel = table.getModel().getRowCount()-1;
				table.getSelectionModel().addSelectionInterval(sel,sel);
				table.revalidate();
			}
		};
		toolbar.add(new JButton(addAction));
		toolbar.add(Box.createHorizontalStrut(10));
		deleteAction = new AbstractAction("Remove",IconFactory.findIcon("icons/minus.gif")) {
			public void actionPerformed(ActionEvent e) {
				int selRow = table.getSelectedRow();
				delete();
				table.clearSelection();
				int rows = table.getModel().getRowCount();
				if (selRow>=rows) selRow = rows-1;
				if (selRow>=0) table.getSelectionModel().addSelectionInterval(selRow,selRow);
			}
		};
		toolbar.add(new JButton(deleteAction));
		toolbar.add(Box.createHorizontalStrut(10));
		editAction = new AbstractAction("Edit",IconFactory.findIcon("icons/dots.gif")) {
			public void actionPerformed(ActionEvent e) {
				int selRow = table.getSelectedRow();
				edit();
				table.clearSelection();
				table.getSelectionModel().addSelectionInterval(selRow,selRow);
			}
		};
		toolbar.add(new JButton(editAction));
		if (useMovers) {
			toolbar.add(Box.createHorizontalStrut(10));
			moveUpAction = new AbstractAction("Move Up",IconFactory.findIcon("icons/s_arrow8.gif")) {
				public void actionPerformed(ActionEvent e) {
					moveUp();
				}
			};
			toolbar.add(new JButton(moveUpAction));
			toolbar.add(Box.createHorizontalStrut(10));
			moveDownAction = new AbstractAction("Move Down",IconFactory.findIcon("icons/s_arrow2.gif")) {
				public void actionPerformed(ActionEvent e) {
					moveDown();
				}
			};
			toolbar.add(new JButton(moveDownAction));
		}
		return toolbar;
	}
	public Action getAddAction() {
		return addAction;
	}
	public Action getEditAction() {
		return editAction;
	}
	public Action getDeleteAction() {
		return deleteAction;
	}
	public Action getMoveUpAction() {
		return moveUpAction;
	}
	public Action getMoveDownAction() {
		return moveDownAction;
	}
}