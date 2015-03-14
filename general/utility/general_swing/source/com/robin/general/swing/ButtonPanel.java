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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import com.robin.general.graphics.GraphicsUtil;

public class ButtonPanel extends JPanel implements ActionListener {
	
	private ForceTextToggle[] choiceButton;
	
	private ArrayList<ActionListener> listeners;
	
	public ButtonPanel(String[] choices) {
		this(choices,choices[0]);
	}
	public ButtonPanel(String[] choices,String choice) {
		setLayout(new GridLayout(1,choices.length));
		ButtonGroup group = new ButtonGroup();
		choiceButton = new ForceTextToggle[choices.length];
		for (int i=0;i<choices.length;i++) {
			choiceButton[i] = new ForceTextToggle(choices[i],choice.equals(choices[i]));
			choiceButton[i].addActionListener(this);
			choiceButton[i].setFocusable(false);
			choiceButton[i].setMinimumSize(new Dimension(5,20));
			group.add(choiceButton[i]);
			add(choiceButton[i]);
		}
	}
	public void setEnabled(boolean val) {
		for (int i=0;i<choiceButton.length;i++) {
			choiceButton[i].setEnabled(val);
		}
	}
	public void setSelectedItem(String val) {
		for (int i=0;i<choiceButton.length;i++) {
			if (val.equals(choiceButton[i].getRealText())) {
				choiceButton[i].setSelected(true);
				return;
			}
		}
	}
	public String getSelectedItem() {
		for (int i=0;i<choiceButton.length;i++) {
			if (choiceButton[i].isSelected()) {
				return choiceButton[i].getRealText();
			}
		}
		return null;
	}
	public void addActionListener(ActionListener listener) {
		if (listeners==null) {
			listeners = new ArrayList<ActionListener>();
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	public void actionPerformed(ActionEvent ev) {
		if (listeners!=null) {
			ForceTextToggle button = (ForceTextToggle)ev.getSource();
			ActionEvent nev = new ActionEvent(this,0,button.getRealText());
			for (ActionListener listener:listeners) {
				listener.actionPerformed(nev);
			}
		}
	}
	
	private class ForceTextToggle extends JToggleButton {
		private String realText;
		public ForceTextToggle(String text,boolean val) {
			super("",val);
			realText = text;
		}
		public String getRealText() {
			return realText;
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Dimension size = getSize();
			GraphicsUtil.drawCenteredString(g,0,0,size.width,size.height,realText);
		}
	}
}