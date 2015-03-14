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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ColorSourceEditPanel extends AdvantageEditPanel implements ActionListener {

	private static final String[] COLOR_MAGIC = {
		"White",
		"Grey", // this matches the rules, but does NOT match the color magic logic, which is "Gray"
		"Gold",
		"Purple",
		"Black",
	};
	
	private static final String[] COLOR_ICON = {
		"white",
		"gray",
		"gold",
		"purple",
		"black",
	};
	
	private JRadioButton[] colorOption;
	private JLabel[] colorIcon;
	private static Font font = new Font("Dialog",Font.PLAIN,18);
		
	public ColorSourceEditPanel(CharacterWrapper pChar, String levelKey) {
		super(pChar, levelKey);
		
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		ButtonGroup group = new ButtonGroup();
		colorOption = new JRadioButton[COLOR_MAGIC.length];
		colorIcon = new JLabel[COLOR_MAGIC.length];
		for (int i=0;i<COLOR_MAGIC.length;i++) {
			ImageIcon icon = ImageCache.getIcon("colormagic/"+COLOR_ICON[i]);
			Box line = Box.createHorizontalBox();
				colorOption[i] = new JRadioButton(COLOR_MAGIC[i],i==0);
				colorOption[i].setFont(font);
				colorOption[i].addActionListener(this);
				line.add(colorOption[i]);
				colorIcon[i] = new JLabel(icon);
				line.add(colorIcon[i]);
				line.add(Box.createHorizontalGlue());
			box.add(line);
			group.add(colorOption[i]);
		}
		box.add(Box.createVerticalGlue());
		add(box,"Center");
		
		updateColorIcon();
		
		String cs = getAttribute("color_source");
		if (cs!=null) {
			for (int i=0;i<COLOR_MAGIC.length;i++) {
				if (cs.equals(COLOR_MAGIC[i].toLowerCase())) {
					colorOption[i].setSelected(true);
					break;
				}
			}
		}
	}
	
	protected void updateColorIcon() {
		for (int i=0;i<COLOR_MAGIC.length;i++) {
			colorIcon[i].setVisible(colorOption[i].isSelected());
		}
	}

	protected void applyAdvantage() {
		for (int i=0;i<COLOR_MAGIC.length;i++) {
			if (colorOption[i].isSelected()) {
				setAttribute("color_source",COLOR_MAGIC[i].toLowerCase());
				break;
			}
		}
	}
	public String getSuggestedDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Is a source of ");
		for (int i=0;i<COLOR_MAGIC.length;i++) {
			if (colorOption[i].isSelected()) {
				sb.append(COLOR_MAGIC[i].toUpperCase());
				break;
			}
		}
		sb.append(" magic.");
		return sb.toString();
	}

	public boolean isCurrent() {
		return hasAttribute("color_source");
	}

	public String toString() {
		return "Color Source";
	}

	public void actionPerformed(ActionEvent arg0) {
		updateColorIcon();
	}
}