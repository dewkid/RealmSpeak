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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

/**
 * @deprecated  I don't think this class is needed anymore, now that I've incorporated the enchantments in the spell selector...?
 */
public abstract class ChitChooser extends AggressiveDialog {
	
	private ChitBinPanel chitPanel;
	private RealmObjectPanel spellPanel;
	
	private JButton doneButton;
	
	protected abstract void clickedChit(ChitComponent chit);
	protected abstract boolean canPressOkay();
	
	public ChitChooser(JFrame frame,String title,Collection chits,Collection spells) {
		super(frame,title,true);
		setSize(500,500);
		getContentPane().setLayout(new BorderLayout());
		setLocationRelativeTo(frame);
		ArrayList list = new ArrayList(chits);
		Collections.sort(list);
		
		JPanel grid = new JPanel(new GridLayout(2,1));
		ChitBinLayout layout = new ChitBinLayout(list);
		Box cbox = Box.createHorizontalBox();
		cbox.add(Box.createHorizontalGlue());
		chitPanel = new ChitBinPanel(layout) {
			public boolean canClickChit(ChitComponent aChit) {
				return true;
			}
			public void handleClick(Point p) {
				ChitComponent chit = chitPanel.getClickedChit(p);
				if (chit!=null) {
					clickedChit(chit);
				}
				repaint();
			}
		};
		chitPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				chitPanel.handleClick(ev.getPoint());
			}
		});
		cbox.add(chitPanel);
		cbox.add(Box.createHorizontalGlue());
		grid.add(cbox);
		spellPanel = new RealmObjectPanel(false,false);
		spellPanel.addObjects(spells);
		spellPanel.setBorder(BorderFactory.createTitledBorder("Your Spells"));
		grid.add(spellPanel);
		getContentPane().add(grid,"Center");
		
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		box.add(doneButton);
		getContentPane().add(box,"South");
		
		for (int i=0;i<list.size();i++) {
			ChitComponent chit = (ChitComponent)list.get(i);
			chitPanel.addChit(chit, i);
		}
	}
	
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		ComponentTools.setSystemLookAndFeel();
		
		System.out.print("loading...");
		RealmLoader loader = new RealmLoader();
		System.out.println("Done");
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		
		JFrame dummy = new JFrame();
		
		GameObject go = loader.getData().getGameObjectByName("Witch King");
		CharacterWrapper character = new CharacterWrapper(go);
		character.recordNewSpell(dummy,loader.getData().getGameObjectByName("Transform"));
		character.recordNewSpell(dummy,loader.getData().getGameObjectByName("Transform"));
		character.recordNewSpell(dummy,loader.getData().getGameObjectByName("Transform"));
		character.recordNewSpell(dummy,loader.getData().getGameObjectByName("Transform"));
		
		ChitChooser chooser = new ChitChooser(dummy,"Enchant chits?",character.getEnchantableChits(),character.getRecordedSpells(loader.getData())) {
			protected boolean canPressOkay() {
				return true;
			}
			protected void clickedChit(ChitComponent chit) {
				if (chit.isActionChit()) {
					CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
					if (achit.isColor()) {
						achit.makeActive();
					}
					else if (achit.isActive()) {
						achit.enchant();
					}
				}
			}
		};
		chooser.setVisible(true);
		System.exit(0);
	}
}