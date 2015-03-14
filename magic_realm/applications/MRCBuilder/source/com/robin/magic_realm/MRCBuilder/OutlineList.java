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
package com.robin.magic_realm.MRCBuilder;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.robin.general.swing.UniformLabelGroup;

// Referenced classes of package com.robin.apps.util.MRCBuilder:
//            OutlineEntry, MRCBuilder

public class OutlineList extends JPanel implements ActionListener {
	private class OutlineEntryPicker extends JDialog implements ActionListener {

		public void init() {
			Dimension dimension = new Dimension(80, 25);
			okay = new JButton("Okay");
			okay.setMinimumSize(dimension);
			okay.setMaximumSize(dimension);
			okay.setPreferredSize(dimension);
			okay.setDefaultCapable(true);
			okay.addActionListener(this);
			cancel = new JButton("Cancel");
			cancel.setMinimumSize(dimension);
			cancel.setMaximumSize(dimension);
			cancel.setPreferredSize(dimension);
			cancel.addActionListener(this);
			setLocation(new Point(100, 100));
			setTitle("");
			getContentPane().setLayout(new BorderLayout());
			setSize(new Dimension(400, 200));
			setResizable(false);
			UniformLabelGroup uniformlabelgroup = new UniformLabelGroup();
			Box box = Box.createVerticalBox();
			Box box1 = uniformlabelgroup.createLabelLine("Header");
			header = new JTextField();
			MRCBuilder.standardComponentSize(header, 200, 25);
			box1.add(header);
			box1.add(Box.createHorizontalGlue());
			box.add(box1);
			box1 = uniformlabelgroup.createLabelLine("Content");
			content = new JTextArea(3, 50);
			content.setLineWrap(true);
			content.setWrapStyleWord(true);
			box1.add(new JScrollPane(content));
			box1.add(Box.createHorizontalGlue());
			box.add(box1);
			getContentPane().add(box, "Center");
			box1 = Box.createHorizontalBox();
			box1.add(Box.createHorizontalGlue());
			box1.add(cancel);
			box1.add(okay);
			getContentPane().add(box1, "South");
			getRootPane().setDefaultButton(okay);
			fillFields();
			setModal(true);
			setVisible(true);
			addWindowListener(new WindowAdapter() {

				public void windowClosing(WindowEvent windowevent) {
				}

			});
		}

		public void fillFields() {
			String s = entry.getHeader();
			if (s != null)
				header.setText(s);
			String s1 = entry.getContent();
			if (s1 != null)
				content.setText(s1);
		}

		public OutlineEntry getEntry() {
			return entry;
		}

		public void actionPerformed(ActionEvent actionevent) {
			Object obj = actionevent.getSource();
			if (obj == okay) {
				entry = new OutlineEntry(header.getText(), content.getText());
				setVisible(false);
				dispose();
			}
			else if (obj == cancel) {
				entry = null;
				setVisible(false);
				dispose();
			}
		}

		OutlineEntry entry;
		JTextField header;
		JTextArea content;
		JButton okay;
		JButton cancel;

		public OutlineEntryPicker() {
			this(null, null);
		}

		public OutlineEntryPicker(String s, String s1) {
			this(new OutlineEntry(s, s1));
		}

		public OutlineEntryPicker(OutlineEntry outlineentry) {
			entry = outlineentry;
			init();
		}
	}

	public OutlineList() {
		super(new BorderLayout());
		init();
	}

	public Vector getData() {
		return data;
	}

	public void setData(Vector vector) {
		data = vector;
		((AbstractTableModel) table.getModel()).fireTableDataChanged();
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

			public String getColumnName(int j) {
				switch (j) {
					case 0: // '\0'
						return "#";

					case 1: // '\001'
						return "Header";

					case 2: // '\002'
						return "Content";
				}
				return "";
			}

			public Object getValueAt(int j, int k) {
				if (data != null && j < data.size() && k < 6) {
					OutlineEntry outlineentry = (OutlineEntry) data.elementAt(j);
					switch (k) {
						default:
							break;

						case 0: // '\0'
							return new Integer(j + 1);

						case 1: // '\001'
							String s = outlineentry.getHeader();
							if (s != null)
								return s;
							break;

						case 2: // '\002'
							String s1 = outlineentry.getContent();
							if (s1 != null)
								return s1;
							break;
					}
				}
				return "";
			}

		});
		for (int i = 0; i < 3; i++) {
			TableColumn tablecolumn = table.getColumnModel().getColumn(i);
			if (i == 0)
				tablecolumn.setPreferredWidth(1);
			else if (i == 1)
				tablecolumn.setPreferredWidth(50);
			else
				tablecolumn.setPreferredWidth(250);
		}

		table.getSelectionModel().setSelectionMode(0);
		addButton = new JButton("Add");
		insertButton = new JButton("Insert");
		editButton = new JButton("Edit");
		deleteButton = new JButton("Delete");
		moveUpButton = new JButton("Up");
		moveDnButton = new JButton("Down");
		JPanel jpanel = new JPanel(new GridLayout(4, 2));
		jpanel.add(moveUpButton);
		jpanel.add(addButton);
		jpanel.add(Box.createGlue());
		jpanel.add(insertButton);
		jpanel.add(Box.createGlue());
		jpanel.add(editButton);
		jpanel.add(moveDnButton);
		jpanel.add(deleteButton);
		add(jpanel, "East");
		add(new JScrollPane(table), "Center");
		addButton.addActionListener(this);
		insertButton.addActionListener(this);
		editButton.addActionListener(this);
		deleteButton.addActionListener(this);
		moveUpButton.addActionListener(this);
		moveDnButton.addActionListener(this);
		reset();
	}

	private void addAction() {
		OutlineEntryPicker outlineentrypicker = new OutlineEntryPicker();
		OutlineEntry outlineentry = outlineentrypicker.getEntry();
		if (outlineentry != null) {
			data.addElement(outlineentry);
			((AbstractTableModel) table.getModel()).fireTableDataChanged();
			setSelectedRow(9000);
		}
	}

	private void insertAction() {
		int i = table.getSelectedRow();
		if (i >= 0 && i < data.size()) {
			OutlineEntryPicker outlineentrypicker = new OutlineEntryPicker();
			OutlineEntry outlineentry = outlineentrypicker.getEntry();
			if (outlineentry != null) {
				data.insertElementAt(outlineentry, i);
				((AbstractTableModel) table.getModel()).fireTableDataChanged();
				setSelectedRow(i);
			}
		}
		else {
			addAction();
		}
	}

	private void editAction() {
		int i = table.getSelectedRow();
		if (i >= 0 && i < data.size()) {
			OutlineEntry outlineentry = (OutlineEntry) data.elementAt(i);
			OutlineEntryPicker outlineentrypicker = new OutlineEntryPicker(outlineentry);
			OutlineEntry outlineentry1 = outlineentrypicker.getEntry();
			if (outlineentry1 != null) {
				data.setElementAt(outlineentry1, i);
				((AbstractTableModel) table.getModel()).fireTableDataChanged();
				setSelectedRow(i);
			}
		}
	}

	private void deleteAction() {
		int i = table.getSelectedRow();
		if (i >= 0 && i < data.size() && saidYes("Delete selected line?")) {
			data.removeElementAt(i);
			((AbstractTableModel) table.getModel()).fireTableDataChanged();
			setSelectedRow(i);
		}
	}

	private void moveUpAction() {
		int i = table.getSelectedRow();
		if (i > 0 && i < data.size()) {
			OutlineEntry outlineentry = (OutlineEntry) data.elementAt(i);
			data.removeElementAt(i);
			data.insertElementAt(outlineentry, i - 1);
			((AbstractTableModel) table.getModel()).fireTableDataChanged();
			setSelectedRow(i - 1);
		}
	}

	private void moveDnAction() {
		int i = table.getSelectedRow();
		if (i >= 0 && i < data.size() - 1) {
			OutlineEntry outlineentry = (OutlineEntry) data.elementAt(i);
			data.removeElementAt(i);
			data.insertElementAt(outlineentry, i + 1);
			((AbstractTableModel) table.getModel()).fireTableDataChanged();
			setSelectedRow(i + 1);
		}
	}

	public void setSelectedRow(int i) {
		if (i >= table.getRowCount())
			i = table.getRowCount() - 1;
		table.getSelectionModel().setSelectionInterval(i, i);
	}

	public boolean saidYes(String s) {
		return JOptionPane.showConfirmDialog(this, s, "Warning!", 0) == 0;
	}

	public void actionPerformed(ActionEvent actionevent) {
		Object obj = actionevent.getSource();
		if (obj != table)
			if (obj == addButton)
				addAction();
			else if (obj == insertButton)
				insertAction();
			else if (obj == editButton)
				editAction();
			else if (obj == deleteButton)
				deleteAction();
			else if (obj == moveUpButton)
				moveUpAction();
			else if (obj == moveDnButton)
				moveDnAction();
	}

	Vector data;
	JTable table;
	JButton addButton;
	JButton insertButton;
	JButton editButton;
	JButton deleteButton;
	JButton moveUpButton;
	JButton moveDnButton;
}