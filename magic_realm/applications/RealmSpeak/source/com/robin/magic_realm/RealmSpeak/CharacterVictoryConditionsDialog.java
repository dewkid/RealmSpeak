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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.general.swing.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class CharacterVictoryConditionsDialog extends AggressiveDialog {
	private Font INFO_FONT = new Font("Dialog",Font.BOLD,12);
	
	private JLabel requiredPoints;
	private JLabel totalPoints;
	
	private JSpinner questPoints;
	private JSpinner greatTreasures;
	private JSpinner usableSpells;
	private JSpinner famePoints;
	private JSpinner notorietyPoints;
	private JSpinner usableGold;

	private JButton cancelButton;
	private JButton okayButton;
	
	private CharacterWrapper character;
	private Integer required;
	
	public CharacterVictoryConditionsDialog(JFrame frame,CharacterWrapper character,Integer required) {
		super(frame,"Victory Requirements",true);
		this.character = character;
		if (required!=null && required.intValue()<1) {
			required = null;
		}
		this.required = required;
		initComponents();
	}
	
	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		UniformLabelGroup group = new UniformLabelGroup();
		Box line;
		
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(character.getGameData());
		
		JPanel topPanel = new JPanel(new GridLayout(3,1));
			requiredPoints = new JLabel("");
			requiredPoints.setFont(INFO_FONT);
			if (required!=null) {
				requiredPoints.setText("Required Points: "+required);
			}
		topPanel.add(requiredPoints);
			totalPoints = new JLabel("");
			totalPoints.setFont(INFO_FONT);
		topPanel.add(totalPoints);
		topPanel.add(Box.createGlue());
		getContentPane().add(topPanel,"North");
		Box box = Box.createVerticalBox();
		if (hostPrefs.hasPref(Constants.QST_QUEST_CARDS)) {
			setSize(200,130);
			line = group.createLabelLine("Quest Points");
				questPoints = createSpinner();
			line.add(questPoints);
			line.add(new JLabel("  x 1"));
			line.add(Box.createHorizontalGlue());
			box.add(line);
		}
		else {
			setSize(200,250);
			line = group.createLabelLine("Great Treasures");
				greatTreasures = createSpinner();
			line.add(greatTreasures);
			line.add(new JLabel("  x 1"));
			line.add(Box.createHorizontalGlue());
			box.add(line);
			
			line = group.createLabelLine("Usable Spells");
				usableSpells = createSpinner();
			line.add(usableSpells);
			line.add(new JLabel("  x 2"));
			line.add(Box.createHorizontalGlue());
			box.add(line);
			
			line = group.createLabelLine("Fame");
				famePoints = createSpinner();
			line.add(famePoints);
			line.add(new JLabel("  x 10"));
			line.add(Box.createHorizontalGlue());
			box.add(line);

			line = group.createLabelLine("Notoriety");
				notorietyPoints = createSpinner();
			line.add(notorietyPoints);
			line.add(new JLabel("  x 20"));
			line.add(Box.createHorizontalGlue());

			box.add(line);

			line = group.createLabelLine("Gold");
				usableGold = createSpinner();
			line.add(usableGold);
			line.add(new JLabel("  x 30"));
			line.add(Box.createHorizontalGlue());
			box.add(line);
		}
		box.add(Box.createVerticalGlue());
		getContentPane().add(box,"Center");
			line = Box.createHorizontalBox();
			line.add(Box.createHorizontalGlue());
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						setVisible(false);
						dispose();
					}
				});
			line.add(cancelButton);
				okayButton = new JButton("Okay");
				okayButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						// Apply victory requirements to character here
						character.addVictoryRequirements(
														getIntFromSpinner(questPoints),
														getIntFromSpinner(greatTreasures),
														getIntFromSpinner(usableSpells),
														getIntFromSpinner(famePoints),
														getIntFromSpinner(notorietyPoints),
														getIntFromSpinner(usableGold));
						character.clearNewVPRequirement();
						
						// Close
						setVisible(false);
						dispose();
					}
				});
			line.add(okayButton);
		getContentPane().add(line,"South");
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		updateControls();
	}
	private JSpinner createSpinner() {
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				updateControls();
			}
		});
		return spinner;
	}
	private void updateControls() {
		int assigned = (getIntFromSpinner(questPoints)+
						getIntFromSpinner(greatTreasures)+
						getIntFromSpinner(usableSpells)+
						getIntFromSpinner(famePoints)+
						getIntFromSpinner(notorietyPoints)+
						getIntFromSpinner(usableGold));
		
		totalPoints.setText("Total Points Assigned: "+assigned);
		
		if (required!=null) {
			okayButton.setEnabled(assigned==required.intValue());
		}
	}
	private int getIntFromSpinner(JSpinner spinner) {
		return spinner==null?0:(Integer)spinner.getValue();
	}
	
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		
		RealmLoader loader = new RealmLoader();
//		RealmGameHandler handler = new RealmGameHandler(null,"ip",47474,"name","pass",false);
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		CharacterWrapper active = new CharacterWrapper(loader.getData().getGameObjectByName("Wizard"));
		active.setPlayerName("name");
		active.setGold(10);
		active.setCharacterLevel(4);
		active.fetchStartingInventory(new JFrame(),loader.getData(),false);
		CharacterVictoryConditionsDialog vc = new CharacterVictoryConditionsDialog(new JFrame(),active,new Integer(5));
		vc.setLocationRelativeTo(null);
		vc.setVisible(true);
		System.exit(0);
	}
}