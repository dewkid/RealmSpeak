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
package com.robin.game.GameBuilder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import com.robin.general.swing.*;
import com.robin.general.util.OrderedHashtable;

public class AttributeEditor extends AggressiveDialog {
	private enum EditType {
		String, ArrayList,
	}

	private OrderedHashtable block;
	private String key;
	private EditType editType = EditType.String;
	private Object initialValue = null;
	private Object newValue = null;

	private JToggleButton editTypeButton;
	private JTextField[] editBox;
	private JPanel editPanel;
	private JPanel buttonPanel;
	private JPanel addSubButtons;
	private JButton addButton;
	private JButton subButton;

	private JButton okayButton;
	private JButton cancelButton;
	
	private boolean pressedOkay = false;

	private AttributeEditor(JFrame frame, String title, OrderedHashtable block, String key) {
		super(frame, true);
		this.block = block;
		this.key = key;
		setTitle("Input");
		if (block.containsKey(key)) {
			initialValue = block.get(key);
			if (initialValue instanceof ArrayList) {
				editType = EditType.ArrayList;
			}
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		initComponents(title);
		
		editBox[0].setCaretPosition(0);
		editBox[0].setSelectionStart(0);
		editBox[0].setSelectionEnd(editBox[0].getText().length());
	}

	private void initComponents(String title) {
		setLayout(new BorderLayout(10, 10));

		Box top = Box.createHorizontalBox();
		editTypeButton = new JToggleButton();
		editTypeButton.setFocusable(false);
		editTypeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (editType == EditType.String) {
					editType = EditType.ArrayList;
				}
				else {
					editType = EditType.String;
					editBox = createEditBoxes(editBox,1);
				}
				updateEditFields();
			}
		});
		ComponentTools.lockComponentSize(editTypeButton, 100, 23);
		top.add(Box.createHorizontalGlue());
		top.add(editTypeButton);
		add(top, "North");

		Box left = Box.createVerticalBox();
		left.add(Box.createVerticalStrut(5));
		left.add(new JLabel("     " + title));
		add(left, "West");

		Box bottom = Box.createHorizontalBox();
		bottom.add(Box.createHorizontalGlue());
		okayButton = new JButton("OK");
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				okay();
			}
		});
		bottom.add(okayButton);
		bottom.add(Box.createHorizontalStrut(5));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				close();
			}
		});
		bottom.add(cancelButton);
		bottom.add(Box.createHorizontalGlue());
		add(bottom, "South");

		addButton = new JButton(IconFactory.findIcon("icons/plus.gif"));
		addButton.setFocusable(false);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				editBox = createEditBoxes(editBox, editBox.length + 1);
				updateEditFields();
			}
		});
		subButton = new JButton(IconFactory.findIcon("icons/minus.gif"));
		subButton.setFocusable(false);
		subButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				editBox = createEditBoxes(editBox, editBox.length - 1);
				updateEditFields();
			}
		});
		addSubButtons = new JPanel(new GridLayout(1, 2));
		addSubButtons.add(addButton);
		addSubButtons.add(subButton);

		if (editType == EditType.String) {
			editBox = new JTextField[1];
			if (initialValue != null) {
				editBox[0] = createNewEditBox();
				editBox[0].setText(initialValue.toString());
			}
		}
		else {
			ArrayList list = (ArrayList) initialValue;
			editBox = new JTextField[list.size()];
			for (int i = 0; i < list.size(); i++) {
				String val = (String) list.get(i);
				editBox[i] = createNewEditBox();
				editBox[i].setText(val);
			}
		}

		editPanel = new JPanel();
		buttonPanel = new JPanel();
		add(buttonPanel, "East");
		add(editPanel, "Center");
		
		getRootPane().setDefaultButton(okayButton);

		updateEditFields();
	}

	private JTextField createNewEditBox() {
		JTextField field = new JTextField();
		ComponentTools.lockComponentSize(field, 300, 23);
		return field;
	}

	private JTextField[] createEditBoxes(JTextField[] current, int newLength) {
		JTextField[] newField = new JTextField[newLength];
		for (int i = 0; i < newLength; i++) {
			if (i < current.length) {
				newField[i] = current[i];
			}
		}
		return newField;
	}

	private void updateEditFields() {
		editTypeButton.setText(editType == EditType.String ? "-> ArrayList" : "-> String");
		ArrayList current = readCurrentFields();
		editPanel.removeAll();
		buttonPanel.removeAll();
		editPanel.setLayout(new GridLayout(editBox.length, 1));
		buttonPanel.setLayout(new GridLayout(editBox.length, 1));
		for (int i = 0; i < editBox.length; i++) {
			String currentString = i < current.size() ? (String) current.get(i) : "";
			if (editBox[i] == null) {
				editBox[i] = createNewEditBox();
			}
			editBox[i].setText(currentString);
			editPanel.add(editBox[i]);
			if (i == 0 && editType == EditType.ArrayList) {
				buttonPanel.add(addSubButtons);
			}
			else {
				buttonPanel.add(Box.createGlue());
			}
		}
		pack();
		setSize(getPreferredSize());
		updateControls();
	}

	private void updateControls() {
		subButton.setEnabled(editBox.length > 1);
	}

	private ArrayList readCurrentFields() {
		ArrayList list = new ArrayList();
		if (editBox != null) {
			for (int i = 0; i < editBox.length; i++) {
				if (editBox[i] != null) {
					String text = editBox[i].getText().trim();
					if (text.length() > 0) {
						list.add(text);
					}
				}
			}
		}
		return list;
	}

	private boolean doEdit() {
		setVisible(true);
		return pressedOkay;
	}

	private void okay() {
		if (editType == EditType.String) {
			newValue = editBox[0].getText();
		}
		else {
			newValue = readCurrentFields();
		}
		block.put(key, newValue);
		pressedOkay = true;
		close();
	}

	private void close() {
		setVisible(false);
		dispose();
	}

	public static boolean editBlock(JFrame frame, JComponent component, String title, OrderedHashtable block, String key) {
		AttributeEditor editor = new AttributeEditor(frame, title, block, key);
		editor.setLocationRelativeTo(component);
		return editor.doEdit();
	}

	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		JFrame frame = new JFrame();
		String key = "test";
		OrderedHashtable block = new OrderedHashtable();
		ArrayList list = new ArrayList();
		list.add("this is a really really long string that you want to see the beginning of");
		list.add("is");
		list.add("a");
		list.add("list");
		block.put(key, list);
		boolean result = editBlock(frame, null, "Value", block, key);
		System.out.println("result = "+result);
		System.exit(0);
	}
}