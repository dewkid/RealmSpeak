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
package com.robin.magic_realm.RealmCharacterBuilder.EditPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robin.general.swing.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class GoldAdjustmentEditPanel extends AdvantageEditPanel {
	
	/*
	 * Defines a different starting gold amount than 10.  May restrict gold collection to zero - maybe make this a maximum amount allowed,
	 * and then they can set it to zero if desired.
	 */
	
	private IntegerField startingGoldField;
	private JCheckBox useMaximumOption;
	
	private JLabel maxGoldLabel;
	private IntegerField maxGoldField;

	public GoldAdjustmentEditPanel(CharacterWrapper pChar, String levelKey) {
		super(pChar, levelKey);
		
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		UniformLabelGroup group = new UniformLabelGroup();
		Box line;
			line = group.createLabelLine("Starting Gold");
			startingGoldField = new IntegerField();
			startingGoldField.setText("10");
			ComponentTools.lockComponentSize(startingGoldField,80,25);
			line.add(startingGoldField);
			line.add(Box.createHorizontalGlue());
		box.add(line);
		box.add(Box.createVerticalStrut(20));
			line = group.createLabelLine("Set a Maximum Limit");
			useMaximumOption = new JCheckBox("",false);
			useMaximumOption.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateControls();
				}
			});
			line.add(useMaximumOption);
			line.add(Box.createHorizontalGlue());
		box.add(line);
		box.add(Box.createVerticalStrut(20));
			line = Box.createHorizontalBox();
			maxGoldLabel = group.createLabel("Maximum Allowable Recorded Gold:");
			line.add(maxGoldLabel);
			Dimension d = new Dimension(5,5);
			line.add(new Box.Filler(d,d,d));
			maxGoldField = new IntegerField();
			maxGoldField.setText("0");
			ComponentTools.lockComponentSize(maxGoldField,80,25);
			line.add(maxGoldField);
			line.add(Box.createHorizontalGlue());
		box.add(line);
		box.add(Box.createVerticalGlue());
		add(box);
		
		String startingGold = getAttribute(Constants.STARTING_GOLD);
		if (startingGold!=null) {
			startingGoldField.setText(startingGold);
			String maxGold = getAttribute(Constants.MAXIMUM_GOLD);
			useMaximumOption.setSelected(maxGold!=null);
			if (maxGold!=null) {
				maxGoldField.setText(maxGold);
			}
		}
		
		updateControls();
	}
	private void updateControls() {
		maxGoldLabel.setEnabled(useMaximumOption.isSelected());
		maxGoldField.setEnabled(useMaximumOption.isSelected());
	}

	protected void applyAdvantage() {
		setAttribute(Constants.STARTING_GOLD,startingGoldField.getInteger().toString());
		if (useMaximumOption.isSelected()) {
			setAttribute(Constants.MAXIMUM_GOLD,maxGoldField.getInteger().toString());
		}
	}
	public String getSuggestedDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Starts out with ");
		Integer g = startingGoldField.getInteger();
		sb.append(g);
		sb.append(" gold");
		if (g.intValue()!=10) {
			sb.append(", instead of the usual 10");
		}
		if (useMaximumOption.isSelected()) {
			sb.append(", and has a maximum allowable recorded gold amount of ");
			sb.append(maxGoldField.getInteger());
		}
		sb.append(".");
		return sb.toString();
	}

	public boolean isCurrent() {
		return hasAttribute(Constants.STARTING_GOLD);
	}

	public String toString() {
		return "Recorded Gold";
	}
}