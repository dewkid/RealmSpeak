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
package com.robin.magic_realm.RealmSpeak;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.MagicRealmColor;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CharacterOptionsDialog extends AggressiveDialog {

	private CharacterWrapper character;

	private ButtonGroup buttonGroup1;
	private JRadioButton[] startChoose;

	private ButtonGroup buttonGroup2;
	private JRadioButton[] chooseLevel;

	private ButtonGroup buttonGroup3;
	private JRadioButton chooseBonusChits[];

	private JCheckBox randomInventorySources;

	private JButton cancelButton;
	private JButton okayButton;

	private boolean okay = false;

	public CharacterOptionsDialog(JFrame frame, CharacterWrapper character, boolean forceInnStart, boolean allowDevelopment, boolean developmentPastFour) {
		super(frame, "Options for " + character.getGameObject().getName(), true);
		this.character = character;
		initComponents(forceInnStart, allowDevelopment, developmentPastFour);
		setLocationRelativeTo(frame);
	}

	private void initComponents(boolean forceInnStart, boolean allowDevelopment, boolean developmentPastFour) {
		setSize(400, 375);
		getContentPane().setLayout(new BorderLayout());

		JPanel sideBarPanel = new JPanel(new GridLayout(2,1));
		
		Box locationPanel = Box.createVerticalBox();
		locationPanel.setBorder(BorderFactory.createTitledBorder("Starting Location"));
		buttonGroup1 = new ButtonGroup();
		String[] locs = character.getStartingLocations(forceInnStart);
		startChoose = new JRadioButton[locs.length];
		for (int i = 0; i < locs.length; i++) {
			startChoose[i] = new JRadioButton(locs[i]);
			buttonGroup1.add(startChoose[i]);
			locationPanel.add(startChoose[i]);
		}
		startChoose[0].setSelected(true);
		locationPanel.setPreferredSize(new Dimension(100,200));
		locationPanel.add(Box.createVerticalGlue());
		sideBarPanel.add(locationPanel);
		
		Box bonusChitPanel = Box.createVerticalBox();
		bonusChitPanel.setBorder(BorderFactory.createTitledBorder("Bonus Chits"));
		buttonGroup3 = new ButtonGroup();
		chooseBonusChits = new JRadioButton[3];
		for (int i = 0; i < 3; i++) {
			chooseBonusChits[i] = new JRadioButton(String.valueOf(i));
			buttonGroup3.add(chooseBonusChits[i]);
			bonusChitPanel.add(chooseBonusChits[i]);
			if (!allowDevelopment)
				chooseBonusChits[i].setEnabled(false);
		}
		chooseBonusChits[0].setSelected(true);
		sideBarPanel.add(bonusChitPanel);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		JPanel levelPanel = new JPanel(new GridLayout(10,1));
		levelPanel.setBorder(BorderFactory.createTitledBorder("Development Level"));
		buttonGroup2 = new ButtonGroup();
		String[] levels = character.getCharacterLevels();
		chooseLevel = new JRadioButton[levels.length];
		for (int i = 0; i < levels.length; i++) {
			chooseLevel[i] = new JRadioButton(levels[i]);
			if (i == 3) {
				chooseLevel[i].setBackground(MagicRealmColor.PEACH);
			}
			buttonGroup2.add(chooseLevel[i]);
			levelPanel.add(chooseLevel[i]);
			if (i < 3 && !allowDevelopment) {
				chooseLevel[i].setEnabled(false);
			}
			if (i > 3 && !(allowDevelopment && developmentPastFour))
				chooseLevel[i].setEnabled(false);
		}
		chooseLevel[3].setSelected(true);

		centerPanel.add(levelPanel,BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
		add(sideBarPanel,BorderLayout.EAST);

		
		Box line = Box.createHorizontalBox();
		randomInventorySources = new JCheckBox("Fetch inventory from random location(s)", false);
		line.add(randomInventorySources);
		line.add(Box.createHorizontalGlue());

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				okay = false;
				setVisible(false);
				dispose();
			}
		});
		line.add(cancelButton);
		okayButton = new JButton("Okay");
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				// Close
				okay = true;
				setVisible(false);
				dispose();
			}
		});
		line.add(okayButton);
		getContentPane().add(line, "South");

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
	}

	public int getChosenBonusChits() {
		for (int i = 0; i < chooseBonusChits.length; i++) {
			if (chooseBonusChits[i].isSelected()) {
				return i;
			}
		}

		return 0;
	}

	public String getChosenStartName() {
		for (int i = 0; i < startChoose.length; i++) {
			if (startChoose[i].isSelected()) {
				return startChoose[i].getText();
			}
		}
		return null;
	}

	public String getChosenLevelName() {
		for (int i = 0; i < chooseLevel.length; i++) {
			if (chooseLevel[i].isSelected()) {
				return chooseLevel[i].getText();
			}
		}
		return null;
	}

	public boolean wantsRandomInventorySourceSelection() {
		return randomInventorySources.isSelected();
	}

	public boolean saidOkay() {
		return okay;
	}

	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmLoader loader = new RealmLoader();
		GameObject go = loader.getData().getGameObjectByName("Wizard");
		CharacterWrapper character = new CharacterWrapper(go);
		// character.setCharacterType("light");
		CharacterOptionsDialog chooser = new CharacterOptionsDialog(new JFrame(), character, false, true, true);
		chooser.setVisible(true);
		System.exit(0);
	}
}