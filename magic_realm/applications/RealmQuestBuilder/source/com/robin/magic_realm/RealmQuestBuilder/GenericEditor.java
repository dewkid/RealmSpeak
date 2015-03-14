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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robin.game.objects.GameData;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;

public abstract class GenericEditor extends AggressiveDialog {
	
	protected GameData realmSpeakData;
	protected boolean canceledEdit;
	
	protected JButton okButton;
	protected JButton cancelButton;
	
	protected abstract void save();
	protected abstract boolean isValidForm();
	
	public GenericEditor(JFrame parent,GameData realmSpeakData) {
		super(parent,true);
		this.realmSpeakData = realmSpeakData;
	}
	public boolean getCanceledEdit() {
		return canceledEdit;
	}
	protected Box buildOkCancelLine() {
		Box bottom = Box.createHorizontalBox();
		bottom.add(Box.createHorizontalGlue());
		okButton = new JButton("Ok");
		ComponentTools.lockDialogButtonSize(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (!isValidForm()) return;
				canceledEdit = false;
				save();
				setVisible(false);
			}
		});
		getRootPane().setDefaultButton(okButton);
		bottom.add(okButton);
		bottom.add(Box.createHorizontalStrut(20));
		cancelButton = new JButton("Cancel");
		ComponentTools.lockDialogButtonSize(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				canceledEdit = true;
				setVisible(false);
			}
		});
		bottom.add(cancelButton);
		return bottom;
	}
}