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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.Collection;
import java.util.Vector;

import javax.swing.*;

/**
 * @version $Id: ListChooser.java,v 1.1 2006/02/17 05:36:05 default Exp $
 */
public class ListChooser extends AggressiveDialog implements ActionListener {
	private JList list;
	private JButton okay;
	private JButton cancel;
	private Vector selected;

	private MouseAdapter doubleClick = new MouseAdapter() {
		public void mouseClicked(MouseEvent ev) {
			if (ev.getClickCount() == 2) {
				doOkay();
			}
		}
	};

	public ListChooser(JFrame parent, String title, Object[] items) {
		super(parent, title, true);
		list = new JList(items);
		initComponents();
	}

	public ListChooser(JFrame parent, String title, Vector items) {
		super(parent, title, true);
		list = new JList(items);
		initComponents();
	}

	public ListChooser(JFrame parent, String title, Collection items) {
		super(parent, title, true);
		list = new JList(new Vector(items));
		initComponents();
	}

	public void setSelectionMode(int val) {
		list.setSelectionMode(val);
	}

	public void selectAll() {
//		list.getSelectionModel().;
	}

	public void initComponents() {
		list.setBorder(BorderFactory.createEmptyBorder());

		Dimension d = new Dimension(80, 25);
		okay = new JButton("OK");
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

		getContentPane().setLayout(new BorderLayout());
		setSize(new Dimension(300, 400));

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(cancel);
		box.add(Box.createHorizontalGlue());
		box.add(okay);
		box.add(Box.createHorizontalGlue());
		getContentPane().add(box, "South");

		getContentPane().add(new JScrollPane(list), "Center");

		getRootPane().setDefaultButton(okay);
	}

	/**
	 * @return		A Vector of selected objects, or null if no selection was made or cancel was pressed.
	 */
	public Vector getSelectedObjects() {
		return selected;
	}

	public Vector getSelectedItems() {
		if (!list.isSelectionEmpty()) {
			Object[] items = list.getSelectedValues();
			Vector itemsVector = null;
			if (items != null) {
				itemsVector = new Vector(items.length);
				for (int i = 0; i < items.length; i++) {
					itemsVector.addElement(items[i]);
				}
			}
			return itemsVector;
		}
		return null;
	}
	
	public Object getSelectedItem() {
		if (list.isSelectionEmpty()) return null;
		return list.getSelectedValue();
	}
	
	private void doOkay() {
		selected = getSelectedItems();
		setVisible(false);
		dispose();
	}

	private void doCancel() {
		selected = null;
		setVisible(false);
		dispose();
	}

	public void actionPerformed(ActionEvent ev) {
		Object source = ev.getSource();
		if (source == okay) {
			doOkay();
		} else if (source == cancel) {
			doCancel();
		}
	}

	/**
	 * Setting this to true allows the user to double-click to make a selection
	 */
	public void setDoubleClickEnabled(boolean val) {
		list.removeMouseListener(doubleClick);
		if (val) {
			list.addMouseListener(doubleClick);
		}
	}
}