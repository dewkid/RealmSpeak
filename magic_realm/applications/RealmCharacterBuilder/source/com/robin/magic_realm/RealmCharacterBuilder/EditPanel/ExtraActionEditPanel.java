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

import java.awt.GridLayout;
import java.util.*;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ExtraActionEditPanel extends AdvantageEditPanel {
	
	private ButtonGroup group;
	private Hashtable<String,JRadioButton> actionHash;

	public ExtraActionEditPanel(CharacterWrapper pChar,String levelKey) {
		super(pChar,levelKey);
		setLayout(new GridLayout(12,1));
		
		group = new ButtonGroup();
		actionHash = new Hashtable<String,JRadioButton>();
		addOption("HIDE","H",true); // default
		addOption("MOVE","M");
		addOption("SEARCH","S");
		addOption("TRADE","T");
		addOption("REST","R");
		addOption("ALERT","A");
		addOption("HIRE","HR");
		addOption("SPELL","SP");
		addOption("PEER","P");
		addOption("FLY","FLY");
		addOption("REMOTE SP","RS");
		addOption("CACHE","C");
		
		// Initialize, if you can
		ArrayList extra = getAttributeList(Constants.EXTRA_ACTIONS);
		if (extra!=null) {
			for (Iterator i=extra.iterator();i.hasNext();) {
				String extraAction = (String)i.next();
				JRadioButton button = actionHash.get(extraAction);
				button.setSelected(true);
				break; // assume only ONE per list
			}
		}
	}
	private void addOption(String name,String action) {
		addOption(name,action,false);
	}
	private void addOption(String name,String action,boolean selected) {
		JRadioButton button = new JRadioButton(name,selected);
		group.add(button);
		add(button);
		actionHash.put(action,button);
	}
	public String toString() {
		return "Extra Action";
	}
	public boolean isCurrent() {
		return hasAttribute(Constants.EXTRA_ACTIONS);
	}
	protected void applyAdvantage() {
		for (String action:actionHash.keySet()) {
			JRadioButton button = actionHash.get(action);
			if (button.isSelected()) {
				ArrayList list = new ArrayList();
				list.add(action);
				setAttributeList(Constants.EXTRA_ACTIONS,list);
				break;
			}
		}
	}
	public String getSuggestedDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Gets an extra ");
		for (String action:actionHash.keySet()) {
			JRadioButton button = actionHash.get(action);
			if (button.isSelected()) {
				sb.append(button.getText().toUpperCase());
			}
		}
		sb.append(" phase.");
		
		return sb.toString();
	}
}