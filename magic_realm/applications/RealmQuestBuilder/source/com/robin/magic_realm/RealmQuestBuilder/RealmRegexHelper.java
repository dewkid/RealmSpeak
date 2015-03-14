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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.robin.general.swing.AggressiveDialog;

public class RealmRegexHelper extends AggressiveDialog {

	private static Font titleFont = new Font("Dialog",Font.BOLD,14);
	private static Font regexFont = new Font("Dialog",Font.PLAIN,18);
	private static Font monoFont = new Font("Monospaced",Font.PLAIN,12);
	private String originalText;
	private ArrayList<String> names;
	private JTextField testField;
	private JList list;
	
	public RealmRegexHelper(JFrame owner,String text,ArrayList<String> names) {
		super(owner,true);
		this.originalText = text;
		this.names = names;
		initComponents();
		updateList();
	}
	public String getText() {
		return testField.getText();
	}
	private void updateList() {
		Pattern pattern;
		try{
			pattern = Pattern.compile(testField.getText());
			testField.setForeground(Color.black);
		}
		catch(PatternSyntaxException ex) {
			testField.setForeground(Color.red);
			return;
		}
		ArrayList<String> matches = new ArrayList<String>();
		for(String val:names) {
			Matcher match = pattern.matcher(val);
			if (match.find()) {
				matches.add(val);
			}
		}
		
		list.setListData(matches.toArray());
	}
	private void initComponents() {
		setSize(800,600);
		setLayout(new BorderLayout());
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalStrut(10));
		box.add(createTitleLabel("Regular Expression:"));
		box.add(Box.createHorizontalStrut(10));
		testField = new JTextField();
		testField.setText(originalText);
		testField.setFont(regexFont);
		testField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateList();
			}
		});
		box.add(testField);
		box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		add(box,BorderLayout.NORTH);
		add(createListPanel(),BorderLayout.CENTER);
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JButton doneButton = new JButton("Keep Changes");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		getRootPane().setDefaultButton(doneButton);
		box.add(doneButton);
		box.add(Box.createHorizontalStrut(10));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				testField.setText(originalText);
				setVisible(false);
			}
		});
		box.add(cancelButton);
		add(box,BorderLayout.SOUTH);
		add(createGuidePanel(),BorderLayout.EAST);
	}
	private JLabel createTitleLabel(String val) {
		JLabel label = new JLabel(val);
		label.setFont(titleFont);
		return label;
	}
	private JPanel createListPanel() {
		JPanel panel = new JPanel(new BorderLayout(5,5));
		list = new JList();
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()!=2) return;
				Object sel = list.getSelectedValue();
				if (sel!=null) {
					testField.setText(sel.toString());
				}
			}
		});
		panel.add(new JScrollPane(list),BorderLayout.CENTER);
		panel.add(createTitleLabel("RealmSpeak pieces that match:"),BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		return panel;
	}
	private JPanel createGuidePanel() {
		StringBuilder sb = new StringBuilder();
		sb.append(".                 Match any single character\n");
  	    sb.append(".*                Match 0 or more characters\n");
		sb.append(".+                Match 1 or more characters\n");
		sb.append("\\w                Match any word character (alphanumeric and underscore)\n");
		sb.append("\\W                Match any non-word character\n");
		sb.append("\\d                Match any numeric character\n");
		sb.append("\\D                Match any non-numeric character\n");
		sb.append("\\s                Match any whitespace character\n");
		sb.append("\\S                Match any non-whitespace character\n");
		sb.append("[ABC]             Match any single character in the set\n");
		sb.append("[^ABC]            Match any single character not in the set\n");
		sb.append("[a-z]             Match any single character in the range a-z\n");
		sb.append("[a-zA-Z]          Match any single character in the range a-z or A-Z\n");
		sb.append("[^d-t]            Match any single character not in the range d-t\n");
		sb.append("[0-9]             Match any single character in the range 0-9\n");
		sb.append("[\\s0-4-]          Match any single whitespace, digit from 0-4, or a dash\n");
		sb.append("(A|B)             Match either A or B\n");
		sb.append("^                 Match the beginning of a string\n");
		sb.append("$                 Match the end of a string\n");
		sb.append("---- Examples -----\n");
		sb.append("(D|d)ragon$       Match any string that ends with Dragon or dragon\n");
		sb.append("(Boots|Gloves)    Match any string that contains the words Boots or Gloves\n");
		JPanel panel = new JPanel(new BorderLayout(5,5));
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setFont(monoFont);
		area.setText(sb.toString());
		area.setOpaque(false);
		panel.add(area,BorderLayout.CENTER);
		panel.add(createTitleLabel("RegEx Help:"),BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		return panel;
	}
	public static void main(String[] args) {
		ArrayList<String> names = new ArrayList<String>();
		names.add("One");
		names.add("Two");
		names.add("Three");
		names.add("Four");
		names.add("Five");
		names.add("Six");
		names.add("Seven");
		names.add("Eight");
		RealmRegexHelper helper = new RealmRegexHelper(new JFrame(),"",names);
		helper.setLocationRelativeTo(null);
		helper.setVisible(true);
		System.exit(0);
	}
}