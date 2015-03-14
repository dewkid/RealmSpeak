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

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.swing.RealmObjectPanel;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CompanionEditPanel extends AdvantageEditPanel {
	
	public static final String[][] COMPANIONS = {
		//name=Ogre - OR - icon_type=xxx,spell
		{"Animals",
			"Bird:Transform|roll4",
			"Crow",
			"Eagle:Transform|roll3",
			"Frog:Transform|roll6",
			"Giant Bat",
			"Lion:Transform|roll2",
			"Octopus",
			"Sabertooth",
			"Scorpion",
			"Squirrel:Transform|roll5",
			"H Spider",
			"Viper",
			"Wolf 1:name=Wolf,attack_speed=5",
			"Wolf 2:name=Wolf,!attack_speed=5",
		},
		{"Monsters",
			"Ghost",
			"Axe Goblin",
			"Spear Goblin",
			"Sword Goblin",
			"Imp",
			"Kobold",
			"Ogre",
			"Lizardman:name=Lizardman,alt_monsters1_game",
			"Sword Orc",
			"Orc Archer",
			"H Troll",
			"H Serpent",
			"H Flying Dragon",
			"H Dragon",
			"Minotaur:name=Minotaur,alt_monsters1_game",
		},
		{"Big Monsters",
			"Behemoth",
			"Cockatrice",
			"Griffon",
			"T Spider",
			"T Serpent",
			"T Troll",
			"Demon",
			"Winged Demon",
			"Giant",
			"T Dragon",
			"T Flying Dragon",
			"Balrog",
			"Firedrake",
			"Frost Giant",
		},
		{"People",
			"Archer:native=Rogues,rank=4",
			"Archer2:native=Woodfolk,rank=1",
			"Archer3:native=Woodfolk,rank=HQ",
			"Assassin:native=Rogues,rank=5",
			"Crossbowman:native=Company,rank=6",
			"Crossbowman2:native=Soldiers,rank=3",
			"Great Axeman:icon_type=great_axeman,native,!dwelling", // 2 Rogue
			
			"Great Swordsman:native=Company,rank=5",
			"Great Swordsman2:native=Guard,rank=1",
			"Great Swordsman3:native=Soldiers,rank=HQ",
			"Knight:native=Order,rank=1",
			"Knight2:native=Order,rank=2",
			"Knight3:native=Order,rank=3",
			"Knight4:native=Order,rank=HQ",
			
			"Lancer:native=Lancers,rank=1",
			"Lancer2:native=Lancers,rank=2",
			"Lancer3:native=Lancers,rank=3",
			"Lancer4:native=Lancers,rank=HQ",
			"Pikeman:native=Company,rank=2",
			"Pikeman2:native=Soldiers,rank=1",
			"SwordsmanR:native=Rogues,rank=6", // 2 Rogue
			
			"Raider:native=Bashkars,rank=1",
			"Raider2:native=Bashkars,rank=2",
			"Raider3:native=Bashkars,rank=3",
			"Raider4:native=Bashkars,rank=4",
			"Raider5:native=Bashkars,rank=5",
			"Raider6:native=Bashkars,rank=HQ",
			"Short Swordsman:native=Company,rank=1",
			"Short Swordsman2:native=Company,rank=HQ",
			"Short Swordsman3:native=Patrol,rank=1",
			"Short Swordsman4:native=Patrol,rank=2",
			"Short Swordsman5:native=Patrol,rank=HQ",
			"Short Swordsman6:native=Rogues,rank=3",
		},
		{"Expansion",
			"Minotaur :name=Minotaur,rw_expansion_1",
			"Basilisk",
			"Gargrath",
			"Harpy",
			"Wasp Queen",
			"Shade",
			"Carnoplant",
			"Dragonman:native=Dragonmen,rank=1",
			"Dragonman2:native=Dragonmen,rank=2",
			"Dragonman3:native=Dragonmen,rank=3",
			"Dragonman4:native=Dragonmen,rank=4",
			"Dragonman5:native=Dragonmen,rank=HQ",
			"Swamp Thing",
			"Rat Man",
			"Blow Dart:native=Murker,rank=3",
		},
	};
	
	private JSpinner companionCount; 
	private JLabel lightSideIcon;
	private JLabel darkSideIcon;
	private Hashtable<String,RealmComponent> hash;
	private ArrayList<RealmObjectPanel> allPanels;
	private Hashtable<String,RealmObjectPanel> panelLookup;
	private ChitComponent selected;
	private boolean lockListener = false;
	
	public CompanionEditPanel(CharacterWrapper pChar, String levelKey) {
		super(pChar, levelKey);
		
		hash = new Hashtable<String,RealmComponent>();
		allPanels = new ArrayList<RealmObjectPanel>();
		panelLookup = new Hashtable<String,RealmObjectPanel>();
		
		setLayout(new BorderLayout());
		
		final JTabbedPane tabs = new JTabbedPane();
		
		Box top = Box.createVerticalBox();
		JPanel spinnerLine = new JPanel(new GridLayout(1,2));
		spinnerLine.add(new JLabel("Count:"));
		companionCount = new JSpinner(new SpinnerNumberModel(1,1,20,1));
		companionCount.setFont(new Font("Dialog",Font.BOLD,22));
		ComponentTools.lockComponentSize(spinnerLine,100,35);
		spinnerLine.add(companionCount);
		top.add(spinnerLine);
		lightSideIcon = new JLabel();
		ComponentTools.lockComponentSize(lightSideIcon,100,100);
		top.add(lightSideIcon);
		darkSideIcon = new JLabel();
		ComponentTools.lockComponentSize(darkSideIcon,100,100);
		top.add(darkSideIcon);
		top.add(Box.createVerticalGlue());
		add(top,"West");
		
		ListSelectionListener listener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (lockListener) return;
				lockListener = true;
				RealmObjectPanel thePanel = (RealmObjectPanel)e.getSource();
				clearSelections(thePanel);
				selected = (ChitComponent)thePanel.getSelectedComponent();
				lockListener = false;
				updateIcons();
			}
		};
		
		for (int i=0;i<COMPANIONS.length;i++) {
			RealmObjectPanel panel = new RealmObjectPanel(true,false);
			panel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
			panel.addSelectionListener(listener);
			
			addOptionList(panel,COMPANIONS[i],i==0);
			tabs.addTab(COMPANIONS[i][0],panel);
		}
		
		add(tabs,"Center");
		
		ArrayList list = getAttributeList(Constants.COMPANION_NAME);
		if (list!=null) {
			companionCount.setValue(list.size());
			for (Iterator n=list.iterator();n.hasNext();) {
				String name = (String)n.next();
				RealmComponent rc = hash.get(name);
				RealmObjectPanel panel = panelLookup.get(name);
				if (rc!=null && panel!=null) {
					clearSelections(null);
					panel.setSelected(rc);
					
					GamePool pool = new GamePool(getGameData().getGameObjects());
					ArrayList query = new ArrayList();
					query.add("Name="+name);
					query.add("companion");
					
					// Delete the companion from the gameData object
					GameObject companion = pool.findFirst(query);
					for (Iterator i=companion.getHold().iterator();i.hasNext();) {
						GameObject held = (GameObject)i.next();
						pChar.getGameObject().getGameData().removeObject(held);
					}
					pChar.getGameObject().getGameData().removeObject(companion);
				}
			}
		}
		
		updateIcons();
	}
	private void clearSelections(RealmObjectPanel panel) {
		for(RealmObjectPanel other:allPanels) {
			if (other==panel) continue;
			other.clearSelected();
		}
	}
	private void updateIcons() {
		if (selected!=null) {
			lightSideIcon.setIcon(selected.getIcon());
			darkSideIcon.setIcon(selected.getFlipSideIcon());
		}
	}
	
	private void addOptionList(RealmObjectPanel panel,String[] list,boolean selectFirst) {
		for (int i=1;i<list.length;i++) {
			StringTokenizer tokens = new StringTokenizer(list[i],":");
			String name;
			String query;
			if (tokens.countTokens()==2) {
				name=tokens.nextToken();
				query=tokens.nextToken();
			}
			else {
				name=tokens.nextToken();
				query="Name="+name;
			}
			
			GameObject companion = TemplateLibrary.getSingleton().getCompanionTemplate(name,query);
			if (companion==null) throw new IllegalStateException("Bad query: "+query);
			ChitComponent rc = (ChitComponent)RealmComponent.getRealmComponent(companion);
			companion.setAttribute("CompanionEditPanel","name",name); // hack
			panel.addRealmComponent(rc);

			hash.put(name,rc);
			panelLookup.put(name,panel);
			allPanels.add(panel);
		}
	}
	private String getOptionName() {
		return selected==null?null:selected.getGameObject().getAttribute("CompanionEditPanel","name");
	}

	protected void applyAdvantage() {
		String name = getOptionName();
		if (selected!=null) {
			int cc =getCompanionCount();
			ArrayList list = new ArrayList();
			for (int n=0;n<cc;n++) {
				list.add(name);
				TemplateLibrary.getSingleton().createCompanionFromTemplate(getGameData(),selected.getGameObject());
			}
			setAttributeList(Constants.COMPANION_NAME,list);
		}
	}
	
	private int getCompanionCount() {
		return (Integer)companionCount.getValue();
	}
	
	public String getSuggestedDescription() {
		if (selected==null) return "";
		StringBuffer sb = new StringBuffer();
		sb.append("Has ");
		String companionName = selected.getGameObject().getThisAttribute("icon_type").replace('_',' ');
		int cc =getCompanionCount();
		if (cc==1) {
			if ("aeiou".indexOf(companionName.substring(0,1))>=0) {
				sb.append("an ");
			}
			else {
				sb.append("a ");
			}
		}
		else {
			sb.append(cc);
			sb.append(" ");
		}
		
		sb.append(companionName);
		sb.append(" companion");
		sb.append(cc==1?"":"s");
		sb.append(" as");
		sb.append(cc==1?" a":"");
		sb.append(" permanent hireling");
		sb.append(cc==1?"":"s");
		sb.append(".  If the companion");
		sb.append(cc==1?" is":"s are");
		sb.append(" killed, ");
		sb.append(cc==1?"it is":"they are");
		sb.append(" lost for the remainder of the game, or until you achieve a new level (development rules).");
		return sb.toString();
	}
	
	public boolean isCurrent() {
		return hasAttribute(Constants.COMPANION_NAME);
	}

	public String toString() {
		return "Companions";
	}

	public static void main(String[] args) {
		RealmLoader loader = new RealmLoader();
		GameObject go = loader.getData().getGameObjectByName("Wizard");
		CharacterWrapper c = new CharacterWrapper(go);
		CompanionEditPanel panel = new CompanionEditPanel(c,"");
		JOptionPane.showConfirmDialog(new JFrame(),panel);
		System.exit(0);
	}
}