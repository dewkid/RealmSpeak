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
package com.robin.magic_realm.RealmGm;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

public class RockerSwitch extends JPanel {
	
	private JToggleButton offButton;
	private JToggleButton onButton;
	
	private ArrayList<ActionListener> actionListeners;
	
	public RockerSwitch() {
		this(false);
	}
	public boolean isOn() {
		return onButton.isSelected();
	}
	public void doClickOn() {
		onButton.doClick();
	}
	public void doClickOff() {
		offButton.doClick();
	}
	public RockerSwitch(boolean defaultOn) {
		initComponents(defaultOn);
		actionListeners = new ArrayList<ActionListener>();
	}
	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}
	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}
	protected void fireActionPerformed() {
		ActionEvent ev = new ActionEvent(this,0,"");
		for(ActionListener listener:actionListeners) {
			listener.actionPerformed(ev);
		}
	}
	private void initComponents(boolean defaultOn) {
		setLayout(new GridLayout(1,2));
		offButton = new JToggleButton("OFF",!defaultOn);
		offButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onButton.setSelected(!offButton.isSelected());
				fireActionPerformed();
			}
		});
		add(offButton);
		onButton = new JToggleButton("ON",defaultOn);
		onButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				offButton.setSelected(!onButton.isSelected());
				fireActionPerformed();
			}
		});
		add(onButton);
	}
	public static void main(String[] args) {
		JOptionPane.showMessageDialog(new JFrame(),new RockerSwitch(true));
	}
}