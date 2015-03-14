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
import java.util.*;

import javax.swing.*;

import com.robin.general.util.StringBufferedList;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class MonsterInteractionEditPanel extends AdvantageEditPanel {
	
	private static final String[][] MONSTERS = {
		{"Animals","Giant Bat","Wolf","T Serpent","H Serpent","H Spider","T Spider","Viper","Octopus","Crow","Scorpion","Carnoplant","Sabertooth","Wasp Queen"},
		
		{"Dragons","T Flying Dragon","T Dragon","H Flying Dragon","H Dragon","Firedrake","Wyrm","Basilisk"},
		{"Fantastic","Minotaur","Griffon","Behemoth","Cockatrice","Harpy","Gargrath","Swamp Thing"},
		
		{"Humanoids","Giant","Ogre","Spear Goblin","Axe Goblin","Sword Goblin","T Troll","H Troll","Lizardman","Rat Man","Sword Orc","Orc Archer","Kobold","Frost Giant"},
		
		{"Spirits/Undead","Ghost","Shade","Skeleton","Skeletal Archer","Skeletal Swordsman","Swamp Haunt","Tomb Guard","Wraith","Zombie"},
		
		{"Demons","Winged Demon","Demon","Imp","Balrog"},
		{"Elementals","Earth Elemental","Air Elemental","Fire Elemental","Water Elemental"},
	};

	private Hashtable<String,JCheckBox> hash;
	
	public MonsterInteractionEditPanel(CharacterWrapper pChar, String levelKey) {
		super(pChar, levelKey);
		hash = new Hashtable<String,JCheckBox>();
		setLayout(new BorderLayout());
		
		JPanel main = new JPanel(new GridLayout(1,5));
		
		Box box = Box.createVerticalBox();
		addOptionList(box,MONSTERS[0]);
		box.add(Box.createVerticalGlue());
		main.add(box);
		
		box = Box.createVerticalBox();
		addOptionList(box,MONSTERS[1]);
		addOptionList(box,MONSTERS[2]);
		box.add(Box.createVerticalGlue());
		main.add(box);
		
		box = Box.createVerticalBox();
		addOptionList(box,MONSTERS[3]);
		box.add(Box.createVerticalGlue());
		main.add(box);
		
		box = Box.createVerticalBox();
		addOptionList(box,MONSTERS[4]);
		box.add(Box.createVerticalGlue());
		main.add(box);
		
		box = Box.createVerticalBox();
		addOptionList(box,MONSTERS[5]);
		addOptionList(box,MONSTERS[6]);
		box.add(Box.createVerticalGlue());
		main.add(box);
		
		add(main,"Center");
		
		ArrayList list = getAttributeList(Constants.MONSTER_IMMUNITY);
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				String name = (String)i.next();
				JCheckBox option = hash.get(name);
				if (option!=null) {
					option.setSelected(true);
				}
			}
		}
	}
	private void addOptionList(Box box,String[] list) {
		JPanel panel = new JPanel(new GridLayout(list.length-1,1));
		panel.setBorder(BorderFactory.createTitledBorder(list[0]));
		for (int i=1;i<list.length;i++) {
			String name = list[i];
			JCheckBox option = new JCheckBox(name);
			panel.add(option);
			hash.put(name,option);
		}
		box.add(panel);
	}

	protected void applyAdvantage() {
		ArrayList list = new ArrayList();
		for (String name:hash.keySet()) {
			JCheckBox option = hash.get(name);
			if (option.isSelected()) {
				list.add(name);
			}
		}
		setAttributeList(Constants.MONSTER_IMMUNITY,list);
	}
	public String getSuggestedDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Is immune to the ");
		StringBufferedList list = new StringBufferedList(", ","and ");
		for (String name:hash.keySet()) {
			JCheckBox option = hash.get(name);
			if (option.isSelected()) {
				list.append(option.getText());
			}
		}
		sb.append(list.toString());
		sb.append(".");
		
		return sb.toString();
	}

	public boolean isCurrent() {
		return hasAttribute(Constants.MONSTER_IMMUNITY);
	}
	
	public String toString() {
		return "Monster Immunity";
	}
}