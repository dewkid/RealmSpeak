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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class TacticsChangeEditPanel extends AdvantageEditPanel {
	
	private JRadioButton replaceMoveOption;
	private JRadioButton replaceFightOption;
	private JSlider tacticSpeedOption;
	
	private JTextArea descriptionField;

	public TacticsChangeEditPanel(CharacterWrapper pChar, String levelKey) {
		super(pChar, levelKey);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				updateDescription();
			}
		};
		
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		ButtonGroup group = new ButtonGroup();
		replaceMoveOption = new JRadioButton("Replace Move",true);
		replaceMoveOption.addActionListener(al);
		group.add(replaceMoveOption);
		box.add(replaceMoveOption);
		replaceFightOption = new JRadioButton("Replace Fight",true);
		replaceFightOption.addActionListener(al);
		group.add(replaceFightOption);
		box.add(replaceFightOption);
		Box line = Box.createHorizontalBox();
		line.add(new JLabel("Tactic Speed: "));
		tacticSpeedOption = new JSlider(1,5,4);
		tacticSpeedOption.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateDescription();
			}
		});
		tacticSpeedOption.setMajorTickSpacing(1);
		tacticSpeedOption.setSnapToTicks(true);
		tacticSpeedOption.setPaintTicks(true);
		tacticSpeedOption.setPaintLabels(true);
		tacticSpeedOption.setFocusable(false);
		line.add(tacticSpeedOption);
		line.add(Box.createHorizontalGlue());
		box.add(line);
		descriptionField = new JTextArea();
		descriptionField.setEditable(false);
		descriptionField.setLineWrap(true);
		descriptionField.setWrapStyleWord(true);
		box.add(new JScrollPane(descriptionField));
		box.add(Box.createVerticalGlue());
		add(box,"Center");
		
		String val = null;
		if (hasAttribute("replace_move")) {
			replaceMoveOption.setSelected(true);
			val = getAttribute("replace_move");
		}
		else if (hasAttribute("replace_fight")) {
			replaceFightOption.setSelected(true);
			val = getAttribute("replace_fight");
		}
		if (val!=null) {
			tacticSpeedOption.setValue(Integer.valueOf(val));
		}
		
		updateDescription();
	}
	private void updateDescription() {
		descriptionField.setText(getSuggestedDescription());
	}

	protected void applyAdvantage() {
		String key = replaceMoveOption.isSelected()?"replace_move":"replace_fight";
		setAttribute(key,String.valueOf(tacticSpeedOption.getValue()));
	}
	
	public String getSuggestedDescription() {
		StringBuffer sb = new StringBuffer();
		if (replaceMoveOption.isSelected()) {
			sb.append("When all the attacks aimed at you have an attack speed greater than ");
		}
		else {
			sb.append("When all of the maneuver speeds of targets you are attacking are greater than ");
		}
		sb.append(tacticSpeedOption.getValue());
		if (replaceMoveOption.isSelected()) {
			sb.append(", you can shift your manuever to any Maneuver square you choose before battle resolution.");
		}
		else {
			sb.append(", you can shift your attack to any Attack Circle you choose before battle resolution.");
		}
		return sb.toString();
	}

	public boolean isCurrent() {
		return hasAttribute("replace_move") || hasAttribute("replace_fight");
	}

	public String toString() {
		return "Tactics Change";
	}
}