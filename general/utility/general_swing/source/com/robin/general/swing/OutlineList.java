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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

public class OutlineList extends JPanel implements ActionListener {
	Vector data;

	JTable table;
	JButton addButton;
	JButton insertButton;
	JButton editButton;
	JButton deleteButton;
	JButton moveUpButton;
	JButton moveDnButton;
	
	public OutlineList() {
		super(new BorderLayout());
		init();
	}
	public Vector getData() {
		return data;
	}
	public void setData(Vector d) {
		data = d;
		((AbstractTableModel)table.getModel()).fireTableDataChanged();
	}
	public void reset() {
		setData(new Vector());
	}
	public void init() {
		table = new JTable();
		table.setModel(new AbstractTableModel() {
			public int getRowCount() {
				return data.size();
			}
			public int getColumnCount() {
				return 3;
			}
			public String getColumnName(int column) {
				switch(column) {
					case 0:		return "#";
					case 1:		return "Header";
					case 2:		return "Content";
				}
				return "";
			}
			public Object getValueAt(int row, int column) {
				if (data!=null && row<data.size() && column<6) {
					OutlineEntry e = (OutlineEntry)data.elementAt(row);
					switch(column) {
						case 0:		return new Integer(row+1);
						case 1:
							String h = e.getHeader();
							if (h!=null) {
								return h;
							}
							break;
						case 2:
							String c = e.getContent();
							if (c!=null) {
								return c;
							}
							break;
					}
				}
				return "";
			}
		});
  		TableColumn column;
  		for (int i=0;i<3;i++) {
	  		column = table.getColumnModel().getColumn(i);
	  		if (i==0) {
		  		column.setPreferredWidth(1);
		  	}
	  		else if (i==1) {
		  		column.setPreferredWidth(50);
		  	}
		  	else {
		  		column.setPreferredWidth(250);
		  	}
	  	}
	  	table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		addButton = new JButton("Add");
		insertButton = new JButton("Insert");
		editButton = new JButton("Edit");
		deleteButton = new JButton("Delete");
		moveUpButton = new JButton("Up");
		moveDnButton = new JButton("Down");
		
		JPanel controls = new JPanel(new GridLayout(4,2));
			controls.add(moveUpButton);
		controls.add(addButton);
			controls.add(Box.createGlue());
		controls.add(insertButton);
			controls.add(Box.createGlue());
		controls.add(editButton);
			controls.add(moveDnButton);
		controls.add(deleteButton);
		add(controls,"East");
		add(new JScrollPane(table),"Center");
		
		addButton.addActionListener(this);
		insertButton.addActionListener(this);
		editButton.addActionListener(this);
		deleteButton.addActionListener(this);
		moveUpButton.addActionListener(this);
		moveDnButton.addActionListener(this);
		
		reset();
	}
	
	private void addAction() {
		OutlineEntryPicker picker = new OutlineEntryPicker();
		OutlineEntry e = picker.getEntry();
		if (e!=null) {
			data.addElement(e);
			((AbstractTableModel)table.getModel()).fireTableDataChanged();
			setSelectedRow(9000);
		}
	}
	
	private void insertAction() {
		int row = table.getSelectedRow();
		if (row>=0 && row<data.size()) {
			OutlineEntryPicker picker = new OutlineEntryPicker();
			OutlineEntry e = picker.getEntry();
			if (e!=null) {
				data.insertElementAt(e,row);
				((AbstractTableModel)table.getModel()).fireTableDataChanged();
				setSelectedRow(row);
			}
		}
		else {
			addAction();
		}
	}
	
	private void editAction() {
		int row = table.getSelectedRow();
		if (row>=0 && row<data.size()) {
			OutlineEntry e = (OutlineEntry)data.elementAt(row);
			OutlineEntryPicker picker = new OutlineEntryPicker(e);
			OutlineEntry newEntry = picker.getEntry();
			if (newEntry!=null) {
				data.setElementAt(newEntry,row);
				((AbstractTableModel)table.getModel()).fireTableDataChanged();
				setSelectedRow(row);
			}
		}
	}
	
	private void deleteAction() {
		int row = table.getSelectedRow();
		if (row>=0 && row<data.size()) {
			if (saidYes("Delete selected line?")) {
				data.removeElementAt(row);
				((AbstractTableModel)table.getModel()).fireTableDataChanged();
				setSelectedRow(row);
			}
		}
	}
	
	private void moveUpAction() {
		int row = table.getSelectedRow();
		if (row>0 && row<data.size()) {
			OutlineEntry e = (OutlineEntry)data.elementAt(row);
			data.removeElementAt(row);
			data.insertElementAt(e,row-1);
			((AbstractTableModel)table.getModel()).fireTableDataChanged();
			setSelectedRow(row-1);
		}
	}
	
	private void moveDnAction() {
		int row = table.getSelectedRow();
		if (row>=0 && row<(data.size()-1)) {
			OutlineEntry e = (OutlineEntry)data.elementAt(row);
			data.removeElementAt(row);
			data.insertElementAt(e,row+1);
			((AbstractTableModel)table.getModel()).fireTableDataChanged();
			setSelectedRow(row+1);
		}
	}
	
	public void setSelectedRow(int row) {
		if (row>=table.getRowCount()) {
			row = table.getRowCount()-1;
		}
		table.getSelectionModel().setSelectionInterval(row,row);
	}
	
	public boolean saidYes(String message) {
		if (JOptionPane.showConfirmDialog(this,message,"Warning!",
					JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
			return true;
		}
		return false;
	}
	
	public void actionPerformed(ActionEvent ev) {
		Object source = ev.getSource();
		if (source==table) {
		}
		else if (source==addButton) {
			addAction();
		}
		else if (source==insertButton) {
			insertAction();
		}
		else if (source==editButton) {
			editAction();
		}
		else if (source==deleteButton) {
			deleteAction();
		}
		else if (source==moveUpButton) {
			moveUpAction();
		}
		else if (source==moveDnButton) {
			moveDnAction();
		}
	}
	
	private class OutlineEntryPicker extends JDialog implements ActionListener {
		OutlineEntry entry;
		
		JTextField header;
		JTextArea content;
		JButton okay;
		JButton cancel;

		public OutlineEntryPicker() {
			this(null,null);
		}
		public OutlineEntryPicker(String h,String c) {
			this(new OutlineEntry(h,c));
		}
		public OutlineEntryPicker(OutlineEntry e) {
			super();
			entry = e;
			init();
		}
		public void init() {
			Dimension d = new Dimension(80,25);
			okay = new JButton("Okay");
			okay.setMinimumSize(d);
			okay.setMaximumSize(d);
			okay.setPreferredSize(d);
			okay.setDefaultCapable(true);
			okay.addActionListener(this);
			
			cancel = new JButton("Cancel");
			cancel.setMinimumSize(d);
			cancel.setMaximumSize(d);
			cancel.setPreferredSize(d);
			cancel.addActionListener(this);
		
			setLocation(new Point(100,100));
			setTitle("");
			getContentPane().setLayout(new BorderLayout());
			setSize(new java.awt.Dimension(400,200));
			setResizable(false);
			
			UniformLabelGroup group = new UniformLabelGroup();
			Box vBox = Box.createVerticalBox();
				Box box = group.createLabelLine("Header");
				header = new JTextField();
				ComponentTools.lockComponentSize(header,200,25);
				box.add(header);
				box.add(Box.createHorizontalGlue());
			vBox.add(box);
				box = group.createLabelLine("Content");
				content = new JTextArea(3,50);
				content.setLineWrap(true);
				content.setWrapStyleWord(true);
				box.add(new JScrollPane(content));
				box.add(Box.createHorizontalGlue());
			vBox.add(box);
				
			getContentPane().add(vBox,"Center");
			
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(cancel);
			box.add(okay);
			getContentPane().add(box,"South");
			
			getRootPane().setDefaultButton(okay);
			fillFields();
			setModal(true);
			setVisible(true);
			
			addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
				}
			});
		}
		public void fillFields() {
			String h = entry.getHeader();
			if (h!=null) {
				header.setText(h);
			}
			String c = entry.getContent();
			if (c!=null) {
				content.setText(c);
			}
		}
		public OutlineEntry getEntry() {
			return entry;
		}
		public void actionPerformed(ActionEvent ev)
		{
			Object source = ev.getSource();
			if (source == okay) {
				entry = new OutlineEntry(header.getText(),content.getText());
				setVisible(false);
				dispose();
			}
			else if (source == cancel) {
				entry=null;
				setVisible(false);
				dispose();
			}
		}
	}
}