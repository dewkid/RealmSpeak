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

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;

import com.robin.magic_realm.components.quest.ChitItemType;

public class ChitTypePanel extends JPanel {
	
	JCheckBox treasureType;
	JCheckBox horseType;
	JCheckBox weaponType;
	JCheckBox armorType;
	
	public ChitTypePanel(ArrayList<String> types) {
		initComponents();
		if (types==null) return;
		for(String type:types) {
			ChitItemType cit = ChitItemType.valueOf(type);
			switch(cit) {
				case None:
					treasureType.setSelected(false);
					horseType.setSelected(false);
					weaponType.setSelected(false);
					armorType.setSelected(false);
					break;
				case Treasure:
					treasureType.setSelected(true);
					break;
				case Horse:
					horseType.setSelected(true);
					break;
				case Weapon:
					weaponType.setSelected(true);
					break;
				case Armor:
					armorType.setSelected(true);
					break;
			}
		}
	}
	private void initComponents() {
		setLayout(new GridLayout(2,2));
		treasureType = new JCheckBox("Treasure");
		add(treasureType);
		horseType = new JCheckBox("Horse");
		add(horseType);
		weaponType = new JCheckBox("Weapon");
		add(weaponType);
		armorType = new JCheckBox("Armor");
		add(armorType);
		setBorder(BorderFactory.createEtchedBorder());
	}
	public ArrayList<ChitItemType> getChitItemTypes() {
		boolean allUnchecked = !treasureType.isSelected() && !horseType.isSelected() && !weaponType.isSelected() && !armorType.isSelected();
		ArrayList<ChitItemType> types = new ArrayList<ChitItemType>();
		if (allUnchecked) {
			types.add(ChitItemType.None);
		}
		else {
			if (treasureType.isSelected()) types.add(ChitItemType.Treasure);
			if (horseType.isSelected()) types.add(ChitItemType.Horse);
			if (weaponType.isSelected()) types.add(ChitItemType.Weapon);
			if (armorType.isSelected()) types.add(ChitItemType.Armor);
		}
		return types;
	}
	public ArrayList<String> getChitTypeList() {
		return ChitItemType.listToStrings(getChitItemTypes());
	}
}