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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import com.robin.general.util.StringBufferedList;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class DieModEditPanel extends AdvantageEditPanel {
	
	private static final String[][] TABLE = {
		{"Actions","Hide","Hire","Trade"},
		{"Battle","Missile","Thrown","Fumble","Stumble"},
		{"Spells","Curse","PoP:powerofthepit","Wish"},
		{"Natives","Commerce","Meeting"},
		{"Search","Locate","Loot","Magic Sight:magicsight","Peer","Read Runes:readrunes"},
		{"Expansion","Raise Dead:raisedead","Summon Elemental:summonelemental","Summon Animal:summonanimal","Capture"},
	};
	
	private static final String[][] CONDITION = {
		{"Clearings","Woods:>woods","Caves:>caves","Mountains:>mountain"},
		{"Civilizations","Lost City:lost city%","Lost Castle:lost castle%","Ruins:%ruins%"},
		{"Tiles","Woods:% woods","Valley:% valley","Swamp:% swamp"},
		{"Warnings","Stink:stink %","Smoke:smoke %","Bones:bones %","Dank:dank %","Ruins:ruins %"},
	};
	
	// Type of die mod
	private JRadioButton oneDieOption;
	private JRadioButton minusOneOption;
	private JRadioButton plusOneOption;
	
	// Affected Tables/Actions
	private AllCheckBox allTablesOption;
	private ArrayList<JCheckBox> specificTableOptions;
	
	private AllCheckBox allConditionsOption;
	private ArrayList<JCheckBox> specificConditionOptions;
	
	private Hashtable<String,JCheckBox> optionsHash;

	public DieModEditPanel(CharacterWrapper pChar,String levelKey) {
		super(pChar,levelKey);
		
		setLayout(new BorderLayout());
		
		ButtonGroup group = new ButtonGroup();
		JPanel typePanel = new JPanel(new BorderLayout());
		Box innerTypePanel = Box.createVerticalBox();
		oneDieOption = new JRadioButton("One Die",true);
		innerTypePanel.add(oneDieOption);
		group.add(oneDieOption);
		minusOneOption = new JRadioButton("Minus One");
		innerTypePanel.add(minusOneOption);
		group.add(minusOneOption);
		plusOneOption = new JRadioButton("Plus One (disadvantage)");
		innerTypePanel.add(plusOneOption);
		group.add(plusOneOption);
		typePanel.add(innerTypePanel,"Center");
		innerTypePanel.add(Box.createHorizontalGlue());
		typePanel.setBorder(BorderFactory.createTitledBorder("Mod Type"));
		
		JPanel mainPanel = new JPanel(new GridLayout(1,3));
		mainPanel.add(typePanel);
		
		optionsHash = new Hashtable<String,JCheckBox>();
		
		JPanel tableBox = new JPanel(new BorderLayout());
		allTablesOption = new AllCheckBox("All Tables");
		allTablesOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (!allTablesOption.isSelected()) {
					allTablesOption.setSelected(true);
				}
				for (JCheckBox option:specificTableOptions) {
					option.setSelected(false);
				}
			}
		});
		tableBox.add(allTablesOption,"North");
		JPanel innerTableBox = new JPanel(new GridLayout(1,2));
		tableBox.add(innerTableBox,"Center");
		specificTableOptions = new ArrayList<JCheckBox>();
		allTablesOption.setOptions(specificTableOptions);
		Box column = null;
		for (int i=0;i<TABLE.length;i++) {
			if (i==0 || i==3) {
				column = Box.createVerticalBox();
				column.add(Box.createVerticalGlue());
				innerTableBox.add(column);
			}
			column.add(makeBox(allTablesOption,specificTableOptions,TABLE[i]),0);
		}
		tableBox.setBorder(BorderFactory.createTitledBorder("Tables/Actions"));
		mainPanel.add(tableBox);
		
		JPanel conditionBox = new JPanel(new BorderLayout());
		allConditionsOption = new AllCheckBox("All Conditions");
		allConditionsOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (!allConditionsOption.isSelected()) {
					allConditionsOption.setSelected(true);
				}
				for (JCheckBox option:specificConditionOptions) {
					option.setSelected(false);
				}
			}
		});
		conditionBox.add(allConditionsOption,"North");
		JPanel innerConditionBox = new JPanel(new GridLayout(1,2));
		conditionBox.add(innerConditionBox,"Center");
		specificConditionOptions = new ArrayList<JCheckBox>();
		allConditionsOption.setOptions(specificConditionOptions);
		column = null;
		for (int i=0;i<CONDITION.length;i++) {
			if (i==0 || i==2) {
				column = Box.createVerticalBox();
				column.add(Box.createVerticalGlue());
				innerConditionBox.add(column);
			}
			column.add(makeBox(allConditionsOption,specificConditionOptions,CONDITION[i]),0);
		}
		conditionBox.setBorder(BorderFactory.createTitledBorder("Conditions"));
		mainPanel.add(conditionBox);
		
		add(mainPanel,"Center");
		
		// Initialize
		ArrayList diemodList = getAttributeList(Constants.DIEMOD);
		if (diemodList!=null && !diemodList.isEmpty()) {
			String diemod = (String)diemodList.get(0); // only ever ONE of these
			StringTokenizer tokens = new StringTokenizer(diemod,":");
			if (tokens.countTokens()==3) {
				String typeKey = tokens.nextToken();
				if ("1d".equals(typeKey)) {
					oneDieOption.setSelected(true);
				}
				else if ("-1".equals(typeKey)) {
					minusOneOption.setSelected(true);
				}
				else {
					plusOneOption.setSelected(true);
				}
				String tableKey = tokens.nextToken();
				initOptions(tableKey,allTablesOption,specificTableOptions);
				String conditionKey = tokens.nextToken();
				initOptions(conditionKey,allConditionsOption,specificConditionOptions);
			}
		}
		else {
			allTablesOption.setSelected(true);
			allConditionsOption.setSelected(true);
		}
	}
	private void initOptions(String key,JCheckBox allOption,ArrayList<JCheckBox> targetOptions) {
		StringTokenizer tokens = new StringTokenizer(key,",");
		while(tokens.hasMoreTokens()) {
			String option = tokens.nextToken();
			if ("all".equals(option)) {
				allOption.setSelected(true);
			}
			else {
				JCheckBox optionBox = optionsHash.get(option);
				if (targetOptions.contains(optionBox)) {
					optionBox.setSelected(true);
				}
			}
		}
	}
	private Box makeBox(AllCheckBox allButton,ArrayList<JCheckBox> list,String[] options) {
		Box box = Box.createVerticalBox();
		
		for (int i=1;i<options.length;i++) {
			String name;
			String key;
			if (options[i].indexOf(':')>=0) {
				StringTokenizer tokens = new StringTokenizer(options[i],":");
				name = tokens.nextToken();
				key = tokens.nextToken();
			}
			else {
				name = options[i];
				key = options[i].toLowerCase();
			}
			JCheckBox option = new JCheckBox(name);
			option.addActionListener(allButton);
			list.add(option);
			box.add(option);
			optionsHash.put(key,option);
		}
		
		box.setBorder(BorderFactory.createTitledBorder(options[0]));
		return box;
	}
	public String toString() {
		return "Die Modifiers";
	}
	public boolean isCurrent() {
		return hasAttribute(Constants.DIEMOD);
	}
	protected void applyAdvantage() {
		String typeKey;
		if (oneDieOption.isSelected()) {
			typeKey = "1d";
		}
		else if (minusOneOption.isSelected()) {
			typeKey = "-1";
		}
		else {
			typeKey = "+1";
		}
		StringBufferedList tableKey = new StringBufferedList(",","");
		if (allTablesOption.isSelected()) {
			tableKey.append("all");
		}
		else {
			for (String option:optionsHash.keySet()) {
				JCheckBox control = optionsHash.get(option);
				if (control.isSelected() && specificTableOptions.contains(control)) {
					tableKey.append(option);
				}
			}
		}
		
		StringBufferedList conditionKey = new StringBufferedList(",","");
		if (allConditionsOption.isSelected()) {
			conditionKey.append("all");
		}
		else {
			for (String option:optionsHash.keySet()) {
				JCheckBox control = optionsHash.get(option);
				if (control.isSelected() && specificConditionOptions.contains(control)) {
					conditionKey.append(option);
				}
			}
		}
		
		// Finally
		StringBufferedList fullKey = new StringBufferedList(":","");
		fullKey.append(typeKey);
		fullKey.append(tableKey.toString());
		fullKey.append(conditionKey.toString());
		ArrayList list = new ArrayList();
		list.add(fullKey.toString());
		setAttributeList(Constants.DIEMOD,list);
	}
	public String getSuggestedDescription() {
		StringBuffer sb = new StringBuffer();
		
		if (oneDieOption.isSelected()) {
			sb.append("Rolls 1 die instead of 2 for all rolls");
		}
		else if (minusOneOption.isSelected()) {
			sb.append("Subtracts one from all rolls");
		}
		else {
			sb.append("Adds one to all rolls");
		}
		
		if (!allTablesOption.isSelected()) {
			sb.append(" on the ");
			int count = 0;
			StringBufferedList list = new StringBufferedList(", ","and ");
			for (String option:optionsHash.keySet()) {
				JCheckBox control = optionsHash.get(option);
				if (control.isSelected() && specificTableOptions.contains(control)) {
					list.append(control.getText().toUpperCase());
					count++;
				}
			}
			sb.append(list.toString());
			sb.append(" table");
			sb.append(count==1?"":"s");
		}
		if (!allConditionsOption.isSelected()) {
			sb.append(" in the ");
			StringBufferedList list = new StringBufferedList(", ","or ");
			for (String option:optionsHash.keySet()) {
				JCheckBox control = optionsHash.get(option);
				if (control.isSelected() && specificConditionOptions.contains(control)) {
					list.append(control.getText());
				}
			}
			sb.append(list.toString());
		}
		sb.append(".");
		return sb.toString();
	}
	
	private class AllCheckBox extends JCheckBox implements ActionListener {
		private ArrayList<JCheckBox> options;
		public AllCheckBox(String title) {
			super(title);
		}
		public void setOptions(ArrayList<JCheckBox> in) {
			options = in;
		}
		public void actionPerformed(ActionEvent ev) {
			boolean oneSelected = false;
			for(JCheckBox box:options) {
				if (box.isSelected()) {
					oneSelected = true;
					break;
				}
			}
			setSelected(!oneSelected);
		}
	}
}