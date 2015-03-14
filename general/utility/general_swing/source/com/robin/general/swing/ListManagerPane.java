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
package com.robin.general.swing;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class ListManagerPane extends JPanel {
	private static ImageIcon plus = IconFactory.findIcon("icons/plus.gif");
	private static ImageIcon dup = IconFactory.findIcon("icons/dup.gif");
	private static ImageIcon minus = IconFactory.findIcon("icons/minus.gif");
	private static ImageIcon dots = IconFactory.findIcon("icons/dots.gif");
	private static ImageIcon global = IconFactory.findIcon("icons/global.gif");
	private static ImageIcon globalDel = IconFactory.findIcon("icons/globaldel.gif");
	private static ImageIcon uparrow = IconFactory.findIcon("icons/s_arrow8.gif");
	private static ImageIcon dnarrow = IconFactory.findIcon("icons/s_arrow2.gif");
	
	private JTable table;
	private JButton addButton = null;
	private JButton duplicateButton = null;
	private JButton deleteButton = null;
	private JButton editButton = null;
	private JButton globalEditButton = null;
	private JButton globalAttributeDeleteButton = null;
	private JButton shiftUpButton = null;
	private JButton shiftDownButton = null;
	
	private ArrayList listSelectionListeners;
	
	public ListManagerPane(String title,TableModel model) {
		this(title,model,true,true,true,true,true,true);
	}
	public ListManagerPane(String title,TableModel model,
								boolean useAdd,
								boolean useDup,
								boolean useDel,
								boolean useEdit,
								boolean useGlobalEdit,
								boolean useShift
								) {
		super();
		init(title,model,useAdd,useDup,useDel,useEdit,useGlobalEdit,useShift);
		listSelectionListeners = new ArrayList();
	}
	public void addListSelectionListener(ListSelectionListener listener) {
		if (!listSelectionListeners.contains(listener)) {
			listSelectionListeners.add(listener);
		}
	}
	public void removeListSelectionListener(ListSelectionListener listener) {
		listSelectionListeners.remove(listener);
	}
	public void fireValueChanged(ListSelectionEvent ev) {
		for (Iterator i=listSelectionListeners.iterator();i.hasNext();) {
			ListSelectionListener listener = (ListSelectionListener)i.next();
			listener.valueChanged(ev);
		}
	}
	public void init(String title,TableModel model,boolean useAdd,boolean useDup,boolean useDel,boolean useEdit,boolean useGlobalEdit,boolean useShift) {
		setLayout(new BorderLayout());
		if (title!=null) {
			add(new JLabel(title),"North");
		}
			table = new JTable(model);
			TableSorter.makeSortable(table);
			updateTableColumnSize(model);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent ev) {
					fireValueChanged(ev);
					updateControls();
				}
			});
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent ev) {
					if (ev.getClickCount()==2 && table.getSelectedRowCount()==1) {
						edit();
					}
				}
			});
		add(new JScrollPane(table),"Center");
			Box controls = Box.createHorizontalBox();
			controls.add(Box.createHorizontalGlue());
			if (useAdd) {
				addButton = new JButton(plus);
				addButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						add();
						fireChange();
					}
				});
				addButton.setToolTipText("Add");
				ComponentTools.lockComponentSize(addButton,40,26);
				controls.add(addButton);
			}
			if (useDup) {
				duplicateButton = new JButton(dup);
				duplicateButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						duplicate();
						fireChange();
					}
				});
				duplicateButton.setToolTipText("Duplicate");
				ComponentTools.lockComponentSize(duplicateButton,40,26);
				controls.add(duplicateButton);
			}
			if (useDel) {
				deleteButton = new JButton(minus);
				deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						delete();
						table.clearSelection();
						fireChange();
					}
				});
				deleteButton.setToolTipText("Delete");
				ComponentTools.lockComponentSize(deleteButton,40,26);
				controls.add(deleteButton);
			}
			if (useEdit) {
				editButton = new JButton(dots);
				editButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						edit();
						fireChange();
					}
				});
				editButton.setToolTipText("Edit");
				ComponentTools.lockComponentSize(editButton,40,26);
				controls.add(editButton);
			}
			if (useGlobalEdit) {
				globalEditButton = new JButton(global);
				globalEditButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						globalEdit(false);
						fireChange();
					}
				});
				globalEditButton.setToolTipText("Global Add");
				ComponentTools.lockComponentSize(globalEditButton,40,26);
				controls.add(globalEditButton);
				
				globalAttributeDeleteButton = new JButton(globalDel);
				globalAttributeDeleteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						globalEdit(true);
						fireChange();
					}
				});
				globalAttributeDeleteButton.setToolTipText("Global Delete");
				ComponentTools.lockComponentSize(globalAttributeDeleteButton,40,26);
				controls.add(globalAttributeDeleteButton);
			}
			if (useShift) {
				shiftUpButton = new JButton(uparrow);
				shiftUpButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						shiftBlock(-1);
						fireChange();
					}
				});
				shiftUpButton.setToolTipText("Shift Up");
				ComponentTools.lockComponentSize(shiftUpButton,40,26);
				controls.add(shiftUpButton);

				shiftDownButton = new JButton(dnarrow);
				shiftDownButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						shiftBlock(1);
						fireChange();
					}
				});
				shiftDownButton.setToolTipText("Shift Down");
				ComponentTools.lockComponentSize(shiftDownButton,40,26);
				controls.add(shiftDownButton);
			}
	
		add(controls,"South");
		updateControls();
	}
	public void fireChange() {
		table.revalidate();
		table.repaint();
		updateControls();
	}
	public void setModel(TableModel model) {
		table.setModel(model);
		TableSorter.makeSortable(table);
		updateTableColumnSize(model);
		fireChange();
	}
	public void updateTableColumnSize(TableModel model) {
		if (model instanceof ColumnSizable) {
			((ColumnSizable)model).setTableHeaderSize(table);
		}
	}
	public int getSelectedRow() {
		TableSorter sorter = TableSorter.getSorter(table);
		int viewRow = table.getSelectedRow();
		int modelRow = sorter.convertRowIndexToModel(viewRow);
		return modelRow;
	}
	public int[] getSelectedRows() {
		TableSorter sorter = TableSorter.getSorter(table);
		int[] viewRow = table.getSelectedRows();
		int[] modelRow = new int[viewRow.length];
		for (int i=0;i<viewRow.length;i++) {
			modelRow[i] = sorter.convertRowIndexToModel(viewRow[i]);
		}
		return modelRow;
	}
	public void setSelectedRows(int[] modelRow) {
		TableSorter sorter = TableSorter.getSorter(table);
		table.clearSelection();
		for (int i=0;i<modelRow.length;i++) {
			int newSelRow = sorter.convertRowIndexToView(modelRow[i]);
			table.addRowSelectionInterval(newSelRow,newSelRow);
		}
	}
	public void setSelectedRow(int row) {
		table.clearSelection();
		if (row<table.getRowCount()) {
			table.setRowSelectionInterval(row,row);
		}
	}
	public void setSelectionMode(int mode) {
		table.getSelectionModel().setSelectionMode(mode);
	}
	public void updateControls() {
		int selRowCount = table.getSelectedRowCount();
		if (addButton!=null) addButton.setEnabled(table.getColumnCount()>0);
		if (duplicateButton!=null) duplicateButton.setEnabled(selRowCount>0);
		if (deleteButton!=null) deleteButton.setEnabled(selRowCount>0);
		if (editButton!=null) editButton.setEnabled(selRowCount==1);
		if (globalEditButton!=null) globalEditButton.setEnabled(selRowCount>0);
		if (globalAttributeDeleteButton!=null) globalAttributeDeleteButton.setEnabled(selRowCount>0);
		if (shiftUpButton!=null) shiftUpButton.setEnabled(selRowCount>0);
		if (shiftDownButton!=null) shiftDownButton.setEnabled(selRowCount>0);
	}
	
	public void add() {
		/** this implementation does nothing */
	}
	public void duplicate() {
		/** this implementation does nothing */
	}
	public void delete() {
		/** this implementation does nothing */
	}
	public void edit() {
		/** this implementation does nothing */
	}
	public void globalEdit(boolean removingChange) {
		/** this implementation does nothing */
	}
	public void shiftBlock(int direction) {
		/** this implementation does nothing */
	}
}