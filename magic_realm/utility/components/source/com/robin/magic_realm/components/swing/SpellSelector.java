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

import com.robin.game.objects.*;
import com.robin.general.swing.*;
import com.robin.general.util.HashLists;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class SpellSelector extends AggressiveDialog {
	
	private static final int LABEL_HEADER_WIDTH = 80;
	
	private static final Font INSTRUCTION_FONT = new Font("Dialog",Font.BOLD,18);
	
	private int currentPicks;
	private int totalPicks;
	private GameData data;
	private ArrayList<GameObject> spellChoices;
	
	private JPanel enchantPanel;
	private ChitBinPanel chitBinPanel;
	
	private JPanel pickPanel;
	private JPanel spellPickGridPanel;
	private JPanel availSpellPanel;
	private JTabbedPane fromTabPanel;
	private RealmObjectPanel[] fromPanel;
	private JPanel slotSpellPanel;
	private RealmObjectPanel toPanel;
	
	private JButton doneButton;
	private JButton resetButton;
	
	private ArrayList<GameObject> spellSelection = null;
	
	private boolean allowAddSpell;
	
	private MouseListener selectSpellListener = new MouseAdapter() {
		public void mousePressed(MouseEvent ev) {
			RealmObjectPanel source = (RealmObjectPanel)ev.getSource();
			if (!MouseUtility.isRightOrControlClick(ev)) {
				if (allowAddSpell) {
					addSelection(source,ev.getPoint());
					updateControls();
				}
				source.clearSelected();
			}
			else {
				showSpell(getSpellFromPanel(source,ev.getPoint()));
			}
		}
	};
	
	
	public SpellSelector(JFrame parent,GameData data,ArrayList<GameObject> spellChoices,int totalPicks) {
		super(parent,"Spell Selector",true);
		this.spellChoices = spellChoices;
		this.totalPicks = totalPicks;
		this.data = data;
		currentPicks = 0;
		initComponents();
	}
	private void refreshFromPanel() {
		fromTabPanel.removeAll();
		
		HashLists hashList = new HashLists();
		for (GameObject go:spellChoices) {
			hashList.put(go.getThisAttribute("spell"),go);
		}
		
		ArrayList types = new ArrayList(hashList.keySet());
		Collections.sort(types); // not QUITE right, I think...
		fromPanel = new RealmObjectPanel[types.size()];
		int n=0;
		for (Iterator i=types.iterator();i.hasNext();){
			String type = (String)i.next();
			fromPanel[n] = new RealmObjectPanel(false,false);
			fromPanel[n].addMouseListener(selectSpellListener);
			ArrayList<GameObject> spells = hashList.getList(type);
			Collections.sort(spells,new Comparator<GameObject>() {
				public int compare(GameObject g1,GameObject g2) {
					return g1.getName().compareTo(g2.getName());
				}
			});
			for (GameObject spell:spells) {
				RealmComponent rc = RealmComponent.getRealmComponent(spell);
				fromPanel[n].add(rc);
			}
			fromPanel[n].adjustSize();
			fromPanel[n].revalidate();
			fromPanel[n].repaint();
			fromTabPanel.addTab(type,new JScrollPane(fromPanel[n]));
			n++;
		}
		updateControls();
	}
	private JLabel getTitle(String title) {
		JLabel label = new JLabel(title);
		label.setFont(new Font("Dialog",Font.BOLD,18));
		label.setOpaque(true);
		label.setBackground(MagicRealmColor.PALEYELLOW);
		label.setBorder(BorderFactory.createEtchedBorder());
		return label;
	}
	private void initComponents() {
		setSize(840,600);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		
		enchantPanel = new JPanel(new BorderLayout());
			JLabel enchantLabel = new JLabel("Enchant",JLabel.CENTER);
			enchantLabel.setVerticalAlignment(JLabel.CENTER);
			enchantLabel.setFont(INSTRUCTION_FONT);
			ComponentTools.lockComponentSize(enchantLabel,LABEL_HEADER_WIDTH,100);
		enchantPanel.add(enchantLabel,"West");
			RealmCalendar calendar = RealmCalendar.getCalendar(data);
			if (calendar!=null) {
				GameWrapper game = GameWrapper.findGame(data);
				int month = game.getMonth();
				int seventhDay = game.getDay();
				while((seventhDay%7)>0) seventhDay++;
				while(seventhDay>28) {
					month++;
					seventhDay-=7;
				}
				String seventhDayMagic = calendar.getColorMagicName(month,seventhDay);
				if (seventhDayMagic!=null && seventhDayMagic.trim().length()>0) {
					JLabel label = new JLabel("Next MAGIC day is "+seventhDayMagic,JLabel.CENTER);
					label.setFont(new Font("Dialog",Font.BOLD|Font.ITALIC,12));
					label.setForeground(Color.blue);
					enchantPanel.add(label,"North");
				}
			}
		getContentPane().add(enchantPanel,"North");
		
		pickPanel = new JPanel(new BorderLayout());
			JLabel pickLabel = new JLabel("Pick",JLabel.CENTER);
			pickLabel.setVerticalAlignment(JLabel.CENTER);
			pickLabel.setFont(INSTRUCTION_FONT);
			ComponentTools.lockComponentSize(pickLabel,LABEL_HEADER_WIDTH,100);
		pickPanel.add(pickLabel,"West");
			spellPickGridPanel = new JPanel(new GridLayout(1,2));
				availSpellPanel = new JPanel(new BorderLayout());
					fromTabPanel = new JTabbedPane(JTabbedPane.LEFT);
					fromTabPanel.setFont(Constants.FORTRESS_FONT);
				availSpellPanel.add(fromTabPanel,"Center");
				availSpellPanel.add(getTitle("Available Spells"),"North");
			spellPickGridPanel.add(availSpellPanel);
				slotSpellPanel = new JPanel(new BorderLayout());
					toPanel = new RealmObjectPanel(false,false);
					toPanel.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent ev) {
							if (!MouseUtility.isRightOrControlClick(ev)) {
								updateControls();
								removeSelection(ev.getPoint());
							}
							else {
								showSpell(getSpellFromPanel(toPanel,ev.getPoint()));
							}
						}
					});
					toPanel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
				slotSpellPanel.add(new JScrollPane(toPanel),"Center");
				slotSpellPanel.add(getTitle("Spell Slots"),"North");
			spellPickGridPanel.add(slotSpellPanel);
		pickPanel.add(spellPickGridPanel,"Center");
		getContentPane().add(pickPanel,"Center");
		
		JPanel bottom = new JPanel(new GridLayout(1,4));
		bottom.add(Box.createGlue());
			resetButton = new JButton("Reset");
			resetButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					currentPicks = 0;
					toPanel.removeAll();
					toPanel.adjustSize();
					toPanel.revalidate();
					toPanel.repaint();
					maintainSlots(new ArrayList());
					updateControls();
				}
			});
		bottom.add(resetButton);
		bottom.add(Box.createGlue());
			doneButton = new JButton("Done");
			doneButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					if (!isReady()) return;
					spellSelection = new ArrayList<GameObject>();
					Collection all = new ArrayList(Arrays.asList(toPanel.getComponents()));
					for (Iterator i=all.iterator();i.hasNext();) {
						SpellCardComponent sc = (SpellCardComponent)i.next();
						spellSelection.add(sc.getGameObject());
					}
					setVisible(false);
					dispose();
				}
			});
		bottom.add(doneButton);
		getContentPane().add(bottom,"South");
		
		refreshFromPanel();
		updateControls();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		maintainSlots(new ArrayList()); // initialize it
	}
	private boolean isReady() {
		if (chitBinPanel==null) return true;
		
		boolean foundEnchantable = false;
		for(ChitComponent chit:chitBinPanel.getAllChits()) {
			if (chit.isEnchanted()) return true;
			if (!foundEnchantable && (chit instanceof CharacterActionChitComponent)) {
				CharacterActionChitComponent aChit = (CharacterActionChitComponent)chit;
				if (aChit.isEnchantable()) {
					foundEnchantable = true;
				}
			}
		}
		
		if (foundEnchantable) { // If you are here, then you found enchantables, but none of them were enchanted!
			int ret = JOptionPane.showConfirmDialog(
					this,
					"You didn't enchant ANY of your MAGIC chits.  Are you sure you are done?",
					"No Enchanted MAGIC",
					JOptionPane.YES_NO_OPTION);
			if (ret==JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}
	public ArrayList<GameObject> getSpellSelection() {
		return spellSelection;
	}
	private SpellCardComponent getSpellFromPanel(RealmObjectPanel panel,Point p) {
		Component c = panel.getComponentAt(p);
		if (c!=null && c instanceof SpellCardComponent) {
			return (SpellCardComponent)c;
		}
		return null;
	}
	private void showSpell(SpellCardComponent sc) {
		if (sc!=null) {
			SpellWrapper spell = new SpellWrapper(sc.getGameObject());
			SpellInfoDialog.showSpellInfo(parent, spell,true);
		}
	}
	private void maintainSlots(ArrayList all) {
		int change = totalPicks-all.size();
		if (change<0) {
			// Remove empties
			ArrayList newList = new ArrayList();
			for (Iterator i=all.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (change<0 && rc instanceof EmptyCardComponent) {
					change++;
				}
				else {
					newList.add(rc);
				}
			}
			all = newList;
		}
		else if (change>0) {
			// Add empties
			for (int i=0;i<change;i++) {
				all.add(new EmptyCardComponent());
			}
		}
		
		// update the panel
		toPanel.removeAll();
		for (Iterator i=all.iterator();i.hasNext();) {
			CardComponent nsc = (CardComponent)i.next();
			toPanel.addRealmComponent(nsc);
		}
		
		toPanel.revalidate();
		toPanel.repaint();
		updateControls();
	}
	private void addSelection(RealmObjectPanel source,Point p) {
		SpellCardComponent sc = getSpellFromPanel(source,p);
		if (sc!=null) {
			SpellCardComponent nsc = new SpellCardComponent(sc.getGameObject());
			
			ArrayList all = new ArrayList(Arrays.asList(toPanel.getComponents()));
			for (int i=0;i<all.size();i++) {
				CardComponent card = (CardComponent)all.get(i);
				if (card instanceof EmptyCardComponent) {
					all.remove(i);
					all.add(i,nsc);
					break;
				}
			}
			currentPicks++;
			maintainSlots(all);
		}
	}
	private void removeSelection(Point p) {
		SpellCardComponent sc = getSpellFromPanel(toPanel,p);
		if (sc!=null) {
			ArrayList all = new ArrayList(Arrays.asList(toPanel.getComponents()));
			int n = all.indexOf(sc);
			all.remove(n);
			all.add(n,new EmptyCardComponent());
//			toPanel.removeAll();
//			for (Iterator i=all.iterator();i.hasNext();) {
//				CardComponent nsc = (CardComponent)i.next();
//				toPanel.addRealmComponent(nsc);
//			}
			currentPicks--;
			maintainSlots(all);
		}
	}
	public void setChits(CharacterWrapper character) {
		ArrayList<CharacterActionChitComponent> chits = character.getAllMagicChits();
		ChitBinLayout layout = new ChitBinLayout(chits);
		chitBinPanel = new ChitBinPanel(layout,12,1) {
			public boolean canClickChit(ChitComponent aChit) {
				return true;
			}
			public void handleClick(Point p) {
				ChitComponent chit = chitBinPanel.getClickedChit(p);
				if (chit!=null) {
					if (chit.isActionChit()) {
						CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
						if (achit.isColor()) {
							achit.makeActive();
						}
						else if (achit.isActive() && achit.isEnchantable()) {
							achit.enchant();
						}
					}
				}
				repaint();
			}
		};
		chitBinPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				chitBinPanel.handleClick(ev.getPoint());
			}
		});
		for (int i=0;i<chits.size();i++) {
			ChitComponent chit = (ChitComponent)chits.get(i);
			chitBinPanel.addChit(chit, i);
		}
		enchantPanel.add(chitBinPanel,"Center");
		
		setTitle("Enchant and Select Spells for the "+character.getGameObject().getName());
	}
	private void updateControls() {
		allowAddSpell = currentPicks<totalPicks;
		
		for (int i=0;i<fromPanel.length;i++) {
			fromPanel[i].setEnabled(allowAddSpell);
		}
		
		resetButton.setEnabled(currentPicks>0);
		doneButton.setEnabled(currentPicks==totalPicks);
	}
	
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		RealmLoader loader = new RealmLoader();
		GameData data = loader.getData();
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> choices = new ArrayList<GameObject>();//pool.find("spell");
		choices.addAll(pool.find("spell=V"));
		choices.addAll(pool.find("spell=VI"));
		choices.addAll(pool.find("spell=VII"));
		choices.addAll(pool.find("spell=VIII"));
		JFrame dummy = new JFrame();
		SpellSelector sel = new SpellSelector(dummy,data,choices,4);
		ArrayList<GameObject> chits = pool.find("character_chit=witch_king,action=magic");
		Collections.sort(chits,new Comparator<GameObject>() {
			public int compare(GameObject o1, GameObject o2) {
				int m1 = RealmUtility.convertMod(o1.getThisAttribute("magic"));
				int m2 = RealmUtility.convertMod(o2.getThisAttribute("magic"));
				int ret = m1 - m2;
				if (ret==0) {
					int s1 = o1.getThisInt("speed");
					int s2 = o2.getThisInt("speed");
					ret = s1 - s2;
				}
				return ret;
			}
		});
		GameObject go = loader.getData().getGameObjectByName("Witch King");
		CharacterWrapper character = new CharacterWrapper(go);
		character.setCharacterLevel(4);
		character.initChits();
		sel.setChits(character);
		sel.setVisible(true);
		System.exit(0);
	}
}