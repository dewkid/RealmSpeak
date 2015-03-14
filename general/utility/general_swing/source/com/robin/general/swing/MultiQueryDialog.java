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
import javax.swing.text.*;

public class MultiQueryDialog extends AggressiveDialog {

	protected Hashtable textComponents = new Hashtable();
	protected Hashtable comboBoxes = new Hashtable();
	protected ArrayList requiredInputComponents = new ArrayList();
	
	protected Box layoutBox;
	protected JButton okay;
	protected JButton cancel;
	protected UniformLabelGroup group = new UniformLabelGroup();
	protected boolean okayPressed = false;

	public MultiQueryDialog(JFrame parent,String title) {
		super(parent,title);
		layoutBox = Box.createVerticalBox();
		layoutBox.add(Box.createVerticalGlue());
		Box controls = Box.createHorizontalBox();
		controls.add(Box.createHorizontalGlue());
			cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					okayPressed = false;
					close();
				}
			});
		controls.add(cancel);
		controls.add(Box.createHorizontalGlue());
			okay = new JButton("Okay");
			okay.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					okayPressed = true;
					close();
				}
			});
		controls.add(okay);
		controls.add(Box.createHorizontalGlue());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(layoutBox,"Center");
		getContentPane().add(controls,"South");
                getRootPane().setDefaultButton(okay);
		setModal(true);
	}
	public void updateSize() {
		int width = group.getMaxPixelWidth()+200;
		int height = (layoutBox.getComponentCount()+3)*25;
		setSize(width,height);
	}
	public void close() {
		setVisible(false);
	}
	public void updateButtons() {
		boolean allClear = true;
		for (Iterator i=requiredInputComponents.iterator();i.hasNext();) {
			JTextComponent tc = (JTextComponent)i.next();
			if (tc.getText().trim().length()==0) {
				allClear = false;
				break;
			}
		}
		okay.setEnabled(allClear);
	}
	/**
	 * Adds the component
	 */
	private void addComponent(String label,JComponent component) {
		component.setMaximumSize(new Dimension(200,25));
		int count = layoutBox.getComponentCount();
		Box line = group.createLabelLine(label);
		line.add(component);
		layoutBox.add(line,count-1); // adds the line before the glue
		updateSize();
		updateButtons();
	}
	public void addQueryLine(String key,String label,JTextComponent textComponent) {
		this.addQueryLine(key,label,textComponent,false);
	}
	public void addQueryLine(String key,String label,JTextComponent textComponent,boolean requireInput) {
		if (requireInput) {
			textComponent.addCaretListener(new CaretListener() {
				public void caretUpdate(CaretEvent ev) {
					updateButtons();
				}
			});
			requiredInputComponents.add(textComponent);
		}
		addComponent(label,textComponent);
		textComponents.put(key,textComponent);
	}
	public void addQueryLine(String key,String label,JComboBox comboBox) {
		addComponent(label,comboBox);
		comboBoxes.put(key,comboBox);
	}
	public String getText(String key) {
		JTextComponent textComponent = (JTextComponent)textComponents.get(key);
		if (textComponent!=null) {
			return textComponent.getText().trim();
		}
		return null;
	}
	public Object getComboChoice(String key) {
		JComboBox comboBox = (JComboBox)comboBoxes.get(key);
		if (comboBox!=null) {
			return comboBox.getSelectedItem();
		}
		return null;
	}
	public boolean saidOkay() {
		return okayPressed;
	}
	public static void main(String[]args) {
		MultiQueryDialog dialog = new MultiQueryDialog(new JFrame(),"test");
		dialog.addQueryLine("name","Name",new JTextField(),true);
		dialog.addQueryLine("address","Address",new JTextField(),true);
			JComboBox cb = new JComboBox();
			cb.addItem("Northern");
			cb.addItem("Southern");
			cb.addItem("Norweestum");
		dialog.addQueryLine("county","County",cb);
		dialog.setVisible(true);
		
		if (dialog.saidOkay()) {
			System.out.println("Name:  "+dialog.getText("name"));
			System.out.println("Address:  "+dialog.getText("address"));
			System.out.println("County:  "+dialog.getComboChoice("county"));
		}
	}
}