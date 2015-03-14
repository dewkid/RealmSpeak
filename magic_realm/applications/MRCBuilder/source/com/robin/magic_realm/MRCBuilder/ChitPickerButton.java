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

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class ChitPickerButton extends JButton {
	private class ChitPicker extends JDialog implements ActionListener, CaretListener {

		public void initComponents() {
			Dimension dimension = new Dimension(100, 25);
			chitText = new JTextField(text);
			chitText.setMinimumSize(dimension);
			chitText.setMaximumSize(dimension);
			chitText.setPreferredSize(dimension);
			chitText.addCaretListener(this);
			dimension = new Dimension(80, 25);
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
			setSize(new Dimension(200, 100));
			setResizable(false);
			getContentPane().add(new JLabel("Chit text:"), "North");
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(chitText);
			box.add(Box.createHorizontalGlue());
			getContentPane().add(box, "Center");
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(cancel);
			box.add(okay);
			getContentPane().add(box, "South");
			getRootPane().setDefaultButton(okay);
			verifyText();
			setVisible(true);
			addWindowListener(new WindowAdapter() {

				public void windowClosing(WindowEvent windowevent) {
				}

			});
		}

		public boolean verifyText() {
			return Chit.parseText(chitText.getText()) != null;
		}

		public void caretUpdate(CaretEvent caretevent) {
			if (verifyText()) {
				chitText.setBackground(Color.white);
				okay.setEnabled(true);
			}
			else {
				chitText.setBackground(lightRed);
				okay.setEnabled(false);
			}
		}

		public String getText() {
			return chitText.getText();
		}

		public void actionPerformed(ActionEvent actionevent) {
			Object obj = actionevent.getSource();
			if (obj == okay) {
				text = chitText.getText();
				setVisible(false);
				dispose();
			}
			else if (obj == cancel) {
				text = null;
				setVisible(false);
				dispose();
			}
		}

		JTextField chitText;
		JButton okay;
		JButton cancel;
		Color lightRed;
		String text;

		public ChitPicker(String s) {
			lightRed = new Color(255, 220, 220);
			text = null;
			text = s.trim();
			setModal(true);
			initComponents();
		}
	}

	public ChitPickerButton() {
		this("");
	}

	public ChitPickerButton(String s) {
		super("");
		init();
		chit = new Chit(s);
	}

	public boolean isFocusTraversable() {
		return false;
	}

	public void init() {
		Dimension dimension = new Dimension(45, 45);
		setMinimumSize(dimension);
		setMaximumSize(dimension);
		setPreferredSize(dimension);
		setFont(Chit.font);
		setBackground(Color.white);
		setForeground(Color.black);
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent) {
				ChitPicker chitpicker = new ChitPicker(chit.getTextLines());
				chit.setTextLines(chitpicker.getText());
			}

		});
	}

	public void setChit(Chit chit1) {
		chit = chit1;
	}

	public Chit getChit() {
		return chit;
	}

	public void paint(Graphics g) {
		super.paint(g);
		Chit.draw(g, chit);
	}

	Chit chit;
}