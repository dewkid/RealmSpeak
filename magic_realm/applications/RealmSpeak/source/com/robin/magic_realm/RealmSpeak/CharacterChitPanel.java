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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.LegendLabel;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.ChitStateViewer;
import com.robin.magic_realm.components.swing.RealmObjectPanel;
import com.robin.magic_realm.components.utility.RealmUtility;

public class CharacterChitPanel extends CharacterFramePanel {
	protected RealmObjectPanel chitHolderPanel;
	protected JButton fatigueChitButton;
	protected JButton chitDetailButton;
	public CharacterChitPanel(CharacterFrame parent) {
		super(parent);
		init();
	}
	private void init() {
		setLayout(new BorderLayout(5,5));
		chitHolderPanel = new RealmObjectPanel(true,false);
		chitHolderPanel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
		chitHolderPanel.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				getCharacterFrame().updateControls();
			}
		});
		add(chitHolderPanel,"Center");
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(new LegendLabel(MagicRealmColor.CHIT_ALERTED,"Alerted"));
			box.add(Box.createHorizontalGlue());
			box.add(new LegendLabel(MagicRealmColor.CHIT_FATIGUED,"Fatigued"));
			box.add(Box.createHorizontalGlue());
			box.add(new LegendLabel(MagicRealmColor.CHIT_WOUNDED,"Wounded"));
			box.add(Box.createHorizontalGlue());
		add(box,"North");
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
				chitDetailButton = new JButton("Chit Detail");
				chitDetailButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showChitDetail();
					}
				});
			box.add(chitDetailButton);
			box.add(Box.createHorizontalGlue());
				fatigueChitButton = new JButton("Play Color Chit");
				fatigueChitButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						burnColorChit();
					}
				});
			box.add(fatigueChitButton);
			box.add(Box.createHorizontalGlue());
		add(box,"South");
	}
	public void updateControls() {
		boolean onlyColorChits = false;
		boolean followingActive = getCharacter().isFollowingCharacterPlayingTurn();
		boolean playingTurn = getCharacterFrame().getTurnPanel()!=null && getCharacterFrame().getTurnPanel().hasActionsLeft();
		if ((playingTurn || followingActive) && !getCharacter().isGone() && getGameHandler().getGame().isDaylight()) {
			TileLocation tl = getCharacter().getCurrentLocation();
			if (tl!=null && !tl.isBetweenClearings() && !tl.isBetweenTiles()) {
				RealmComponent rc = chitHolderPanel.getSelectedComponent();
				if (rc!=null) {
					onlyColorChits = true;
					if (rc.isMagicChit()) {
						MagicChit chit = (MagicChit)rc;
						if (!chit.isColor()) {
							onlyColorChits = false;
						}
					}
				}
			}
		}
		fatigueChitButton.setEnabled(onlyColorChits);
	}
	public void updatePanel() {
		// Refresh the chit panel
		chitHolderPanel.removeAll();
		ArrayList allChits = new ArrayList(getCharacter().getCompleteChitList());
		for (Iterator i=allChits.iterator();i.hasNext();) {
			RealmComponent chit = (RealmComponent)i.next();
			chitHolderPanel.add(chit);
		}
		for (GameObject go:getCharacter().getInventory()) {
			RealmComponent item = RealmComponent.getRealmComponent(go);
			if (item.isEnchanted()) {
				chitHolderPanel.add(item);
			}
		}
		getCharacterFrame().updateControls();
	}
	public void showChitDetail() {
		ChitStateViewer viewer = new ChitStateViewer(getMainFrame(),getCharacter());
		viewer.setVisible(true);
	}
	public void burnColorChit() {
		// This will provide the character a way to recover their color chits
		// without having to use them in a spell
		MagicChit chit = (MagicChit)chitHolderPanel.getSelectedComponent();
		// because of button disabling, we know this is a color chit
		
		ArrayList se = getCharacter().getSpellExtras();
		int seBefore = se==null?0:se.size();
		
		RealmUtility.burnColorChit(getGameHandler().getMainFrame(),getGameHandler().getGame(),getCharacter(),chit);
		
		if (getCharacterFrame().getTurnPanel()!=null) { // only worry about this if playing a turn
			se = getCharacter().getSpellExtras();
			int seAfter = se==null?0:se.size();
			if (seAfter>seBefore) {
				// A spell (or spells) were energized manually during the turn.  Make sure these
				// make it into the PhaseManager
				ArrayList ses = getCharacter().getSpellExtraSources();
				for (int i=seBefore;i<seAfter;i++) {
					String seAction = (String)se.get(i);
					GameObject seGo = (GameObject)ses.get(i);
					getCharacterFrame().getTurnPanel().getPhaseManager().addFreeAction(seAction,seGo);
				}
			}
		}
		
		getCharacterFrame().updateActiveCurses(); // in case any curses are nullified
		chitHolderPanel.clearSelected();
		getCharacterFrame().updateCharacter();
//		updatePanel();
		getGameHandler().getInspector().redrawMap();
		getGameHandler().submitChanges();
	}
}