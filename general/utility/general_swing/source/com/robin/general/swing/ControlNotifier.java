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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

public class ControlNotifier implements ActionListener,CaretListener,ChangeListener {
	
	private ArrayList<ActionListener> actionListeners;
	
	public ControlNotifier() {
	}
	public void addActionListener(ActionListener actionListener) {
		if (actionListeners==null) {
			actionListeners = new ArrayList<ActionListener>();
		}
		if (!actionListeners.contains(actionListener)) {
			actionListeners.add(actionListener);
		}
	}
	public void removeActionListener(ActionListener actionListener) {
		if (actionListeners!=null) {
			actionListeners.remove(actionListener);
			if (actionListeners.isEmpty()) {
				actionListeners = null;
			}
		}
	}
	public void actionPerformed(ActionEvent ev) {
		fireActionPerformed();
	}
	public void caretUpdate(CaretEvent e) {
		fireActionPerformed();
	}
	public void stateChanged(ChangeEvent e) {
		fireActionPerformed();
	}
	private void fireActionPerformed() {
		if (actionListeners==null) return;
		ActionEvent ev = new ActionEvent(this,0,"");
		for (ActionListener actionListener:actionListeners) {
			actionListener.actionPerformed(ev);
		}
	}
	public JTextField getTextField() {
		JTextField field = new JTextField();
		field.addCaretListener(this);
		field.addActionListener(this);
		return field;
	}
	public IntegerField getIntegerField() {
		IntegerField field = new IntegerField();
		field.addCaretListener(this);
		field.addActionListener(this);
		return field;
	}
	public JButton getButton(String name) {
		JButton button = new JButton(name);
		button.addActionListener(this);
		return button;
	}
	public JCheckBox getCheckBox(String name) {
		JCheckBox button = new JCheckBox(name);
		button.addActionListener(this);
		return button;
	}
	public JRadioButton getRadioButton(String name) {
		return getRadioButton(name,false);
	}
	public JRadioButton getRadioButton(String name,boolean checked) {
		JRadioButton button = new JRadioButton(name,checked);
		button.addActionListener(this);
		return button;
	}
	public JSlider getSlider(int min,int max,int value) {
		JSlider slider = new JSlider(min,max,value);
		slider.addChangeListener(this);
		return slider;
	}
	public JComboBox getComboBox(Object[] array) {
		JComboBox cb = new JComboBox(array);
		cb.addActionListener(this);
		return cb;
	}
}