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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

public class FlashingButton extends JButton {
	private static boolean flashEnabled = true;
	public static void setFlashEnabled(boolean val) {
		flashEnabled = val;
	}
	private static final Color DEFAULT_FLASH_COLOR = Color.red;
	private static final int DEFAULT_FLASH_DELAY = 500; // milliseconds
	private static final int DEFAULT_BORDER_THICKNESS = 3;
	private static final int DEFAULT_INITIAL_DELAY = 15000; // milliseconds (15 seconds)
	
	private int flashDelay;
	private ActionListener buttonFlasher = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			toggleFlash();
		}
	};
	private Border defaultBorder;
	private Border flashBorder;
	private Timer buttonTimer = null;
	private int initialDelay;
	private boolean flashStateOn = false;

	//  new Timer(delay, taskPerformer).start();
	public FlashingButton() {
		super();
		init();
	}
	public FlashingButton(Action action) {
		super(action);
		init();
	}
	public FlashingButton(Icon icon) {
		super(icon);
		init();
	}
	public FlashingButton(String in) {
		super(in);
		init();
	}
	public FlashingButton(String in,Icon icon) {
		super(in,icon);
		init();
	}
	private void init() {
		init(DEFAULT_FLASH_COLOR,DEFAULT_BORDER_THICKNESS,DEFAULT_INITIAL_DELAY,DEFAULT_FLASH_DELAY);
		addActionListener(new ActionListener() { // This guarantees that when the button is pressed, it stops the flashing timer.
			public void actionPerformed(ActionEvent ev) {
				setFlashing(false);
			}
		});
	}
	private void init(Color flashColor,int thickness,int initDelay,int delay) {
		this.initialDelay = initDelay;
		this.flashDelay = delay;
		Border FLASH_ON = BorderFactory.createLineBorder(flashColor,thickness);
		Border FLASH_OFF = BorderFactory.createLineBorder(UIManager.getColor("Panel.background"),thickness);
		
		defaultBorder = BorderFactory.createCompoundBorder(FLASH_OFF,getBorder());
		flashBorder = BorderFactory.createCompoundBorder(FLASH_ON,getBorder());
		setBorder(defaultBorder);
	}
	
	private void toggleFlash() {
		flashStateOn = !flashStateOn;
		if (!flashEnabled) {
			flashStateOn = false;
		}
		updateFlashBorder();
	}
	private void updateFlashBorder() {
		setBorder(flashStateOn?flashBorder:defaultBorder);
		if (flashStateOn) doWhenFlash();
	}
	public void doWhenFlash() {
		// Override to do something
	}

	public void setFlashing(boolean val) {
		if (buttonTimer!=null) {
			buttonTimer.stop();
			flashStateOn = false;
			updateFlashBorder();
		}
		if (val) {
			buttonTimer = new Timer(flashDelay,buttonFlasher);
			buttonTimer.setInitialDelay(initialDelay);
			buttonTimer.start();
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(300, 300);
		frame.setLocationRelativeTo(null);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		final FlashingButton button = new FlashingButton("Test");
		box.add(button);
		box.add(Box.createHorizontalGlue());
		final JCheckBox flashOn = new JCheckBox("Flash Button", false);
		flashOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				button.setFlashing(flashOn.isSelected());
			}
		});
		box.add(flashOn);
		frame.getContentPane().add(box, "South");
		frame.setVisible(true);
	}
}