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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.general.util.StringBufferedList;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.CharacterActionChitComponent;

public class ChitEditDialog extends AggressiveDialog {
	
	private CharacterActionChitComponent chit;
	
	private JLabel chitView;
	
	private Box strengthLine;
	private Box magicLine;
	private Box specialLine;
	
	private ButtonPanel typeSelector;
	private ButtonPanel strengthSelector;
	private ButtonPanel magicSelector;
	private ButtonPanel speedSelector;
	private ButtonPanel effortSelector;
	
	private JComboBox specialSelections;
	
	private JButton doneButton;
	
	private ArrayList<String> reservedChitNames;
	
	private SpecialSelect[] specialSelect;
	private static final String[][] SPECIAL_SELECT = {
		{" - SELECT - "},
		{"Adjust Vulnerability","fight_alert",""},
		{"MOVE chit - only CHARGE","move_lock","box_constraint=1"},
		{"MOVE chit - only DODGE","move_lock","box_constraint=2"},
		{"MOVE chit - only DUCK","move_lock","box_constraint=3"},
		{"FIGHT chit - only THRUST","fight_lock","box_constraint=1"},
		{"FIGHT chit - only SWING","fight_lock","box_constraint=2"},
		{"FIGHT chit - only SMASH","fight_lock","box_constraint=3"},
	};
	
	public ChitEditDialog(JDialog frame,CharacterActionChitComponent chit) {
		super(frame,"Edit Chit",true);
		init(chit);
	}
	public ChitEditDialog(JFrame frame,CharacterActionChitComponent chit) {
		super(frame,"Edit Chit",true);
		init(chit);
	}
	private void init(CharacterActionChitComponent chit) {
		this.chit = chit;
		reservedChitNames = new ArrayList<String>();
		reservedChitNames.add("MOVE");
		reservedChitNames.add("FIGHT");
		reservedChitNames.add("MAGIC");
		reservedChitNames.add("FLY");
		initComponents();
	}
	private void initComponents() {
		setSize(300,220);
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		chitView = new JLabel();
		ComponentTools.lockComponentSize(chitView,60,60);
		box.add(chitView);
		box.add(Box.createVerticalGlue());
		add(box,"West");
		
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		box.add(doneButton);
		box.add(Box.createHorizontalGlue());
		add(box,"South");
		
		int w = 160;
		
		box = Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		UniformLabelGroup group = new UniformLabelGroup();
		Box line;
			typeSelector = new ButtonPanel(RealmCharacterConstants.CHIT_TYPES);
			typeSelector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					String action = ev.getActionCommand();
					if (action.equals("SPECIAL")) {
						String chitName;
						while (reservedChitNames.contains((chitName = JOptionPane.showInputDialog("Chit Name?")).toUpperCase())) {
							JOptionPane.showMessageDialog(ChitEditDialog.this,"You cannot use any of the reserved chit names:  "+reservedChitNames,"Invalid Name",JOptionPane.ERROR_MESSAGE);
						}
						
						chit.getGameObject().setThisAttribute("action",chitName.toLowerCase());
						
						/*
						 * Special types:
						 * 		fight_alert (like BERSERK)
						 * 		move_lock (like DUCK)  box_constraint (maybe this will work automatically for fight?)
						 * 		fight_lock (no example of this)
						 */
					}
					else {
						clearChitSpecials();
						chit.getGameObject().setThisAttribute("action",action.toLowerCase());
					}
					
					if (chit.isMagic()) {
						chit.getGameObject().removeThisAttribute("strength");
						chit.getGameObject().setThisAttribute("magic",magicSelector.getSelectedItem().toString());
					}
					else {
						chit.getGameObject().removeThisAttribute("magic");
						chit.getGameObject().setThisAttribute("strength",strengthSelector.getSelectedItem().toString());
					}
					updateChitName();
					updateControls();
				}
			});
			ComponentTools.lockComponentSize(typeSelector,250,25);
			add(typeSelector,"North");
		strengthLine = group.createLabelLine("Strength");
			strengthSelector = new ButtonPanel(RealmCharacterConstants.STRENGTHS);
			strengthSelector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					chit.getGameObject().setThisAttribute("strength",strengthSelector.getSelectedItem().toString());
					updateChitName();
					updateControls();
				}
			});
			ComponentTools.lockComponentSize(strengthSelector,w,25);
			strengthLine.add(strengthSelector);
		box.add(strengthLine);
		box.add(Box.createVerticalGlue());
		magicLine = group.createLabelLine("Magic");
			magicSelector = new ButtonPanel(RealmCharacterConstants.MAGICS);
			magicSelector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					chit.getGameObject().setThisAttribute("magic",magicSelector.getSelectedItem().toString());
					updateChitName();
					updateControls();
				}
			});
			ComponentTools.lockComponentSize(magicSelector,w,25);
			magicLine.add(magicSelector);
		box.add(magicLine);
		box.add(Box.createVerticalGlue());
		line = group.createLabelLine("Speed");
			speedSelector = new ButtonPanel(RealmCharacterConstants.SPEEDS);
			speedSelector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					chit.getGameObject().setThisAttribute("speed",speedSelector.getSelectedItem().toString());
					updateChitName();
					updateControls();
				}
			});
			ComponentTools.lockComponentSize(speedSelector,w,25);
			line.add(speedSelector);
		box.add(line);
		box.add(Box.createVerticalGlue());
		line = group.createLabelLine("Effort");
			effortSelector = new ButtonPanel(RealmCharacterConstants.EFFORTS);
			effortSelector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					chit.getGameObject().setThisAttribute("effort",effortSelector.getSelectedItem().toString().length());
					updateChitName();
					updateControls();
				}
			});
			ComponentTools.lockComponentSize(effortSelector,w,25);
			line.add(effortSelector);
		box.add(line);
		specialLine = group.createLabelLine("Special");
			specialSelect = new SpecialSelect[SPECIAL_SELECT.length];
			for (int i=0;i<specialSelect.length;i++) {
				specialSelect[i] = new SpecialSelect(SPECIAL_SELECT[i]);
			}
			specialSelections = new JComboBox(specialSelect);
			specialSelections.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					SpecialSelect ss = (SpecialSelect)specialSelections.getSelectedItem();
					ss.apply(chit.getGameObject());
					updateChitName();
					updateControls();
				}
			});
			ComponentTools.lockComponentSize(specialSelections,w,25);
			specialLine.add(specialSelections);
		box.add(specialLine);
		box.add(Box.createVerticalGlue());
		add(box,"Center");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		initControls();
		updateControls();
	}
	private void updateChitName() {
		StringBuffer sb = new StringBuffer();
		sb.append(chit.getGameObject().getThisAttribute("action"));
		sb.append(" ");
		if (chit.isMagic()) {
			sb.append(chit.getGameObject().getThisAttribute("magic"));
		}
		else {
			sb.append(chit.getGameObject().getThisAttribute("strength"));
		}
		sb.append(chit.getGameObject().getThisAttribute("speed"));
		sb.append(StringUtilities.getRepeatString("*",chit.getGameObject().getThisInt("effort")));
		chit.getGameObject().setName(sb.toString());
	}
	private void initControls() {
		if (chit.getGameObject().hasThisAttribute("strength")) {
			strengthSelector.setSelectedItem(chit.getGameObject().getThisAttribute("strength"));
		}
		if (chit.getGameObject().hasThisAttribute("magic")) {
			magicSelector.setSelectedItem(chit.getGameObject().getThisAttribute("magic"));
		}
		speedSelector.setSelectedItem(chit.getGameObject().getThisAttribute("speed"));
		int effort = chit.getGameObject().getThisInt("effort");
		effortSelector.setSelectedItem("***".substring(3-effort));
		String action = chit.getGameObject().getThisAttribute("action").toUpperCase();
		if (reservedChitNames.contains(action)) {
			typeSelector.setSelectedItem(action);
		}
		else {
			typeSelector.setSelectedItem("SPECIAL");
			for (int i=specialSelect.length-1;i>=0;i--) { // do the reverse
				if (specialSelect[i].matches(chit.getGameObject())) {
					specialSelections.setSelectedIndex(i);
					break;
				}
			}
		}
	}
	private void updateControls() {
		chitView.setIcon(chit.getIcon());
		boolean magic = chit.isMagic();
		strengthLine.setVisible(!magic);
		magicLine.setVisible(magic);
		specialLine.setVisible("SPECIAL".equals(typeSelector.getSelectedItem()));
	}
	private void clearChitSpecials() {
		chit.getGameObject().removeThisAttribute("fight_alert");
		chit.getGameObject().removeThisAttribute("move_lock");
		chit.getGameObject().removeThisAttribute("fight_lock");
		chit.getGameObject().removeThisAttribute("box_constraint");
	}
	private class SpecialSelect {
		private String[] args;
		public SpecialSelect(String[] args) {
			this.args = args;
		}
		public String toString() {
			return args[0];
		}
		public void apply(GameObject go) {
			clearChitSpecials();
			for (int i=1;i<args.length;i++) {
				go.setThisKeyVals(args[i]);
			}
		}
		public boolean matches(GameObject go) {
			StringBufferedList sb = new StringBufferedList(",","");
			for (int i=1;i<args.length;i++) {
				sb.append(args[i]);
			}
			return go.hasAllKeyVals(sb.toString());
		}
	}
	public static void main(String[] args) {
		ChitEditDialog ced = new ChitEditDialog(new JFrame(),null);
		ced.setLocationRelativeTo(null);
		ced.setVisible(true);
	}
}