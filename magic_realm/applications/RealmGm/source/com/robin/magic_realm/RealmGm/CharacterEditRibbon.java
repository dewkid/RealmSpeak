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
package com.robin.magic_realm.RealmGm;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.*;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.*;

public class CharacterEditRibbon extends JPanel {
	
	private JFrame parentFrame;
	
	private CharacterWrapper character;
	private ChitBinPanel chitsPanel;
	private RelationshipTable relationshipTable;
	private ArrayList<String[]> relationshipNames;
	
	private ArrayList<GameObject> spellChoices;
		
	private JList ownedSpellsList;
	private OwnedSpellsListModel ownedSpellsListModel;
	private ArrayList<SpellWrapper> ownedSpells;
	
	private JList breakableSpellsList;
	private BreakableSpellsListModel breakableSpellsListModel;
	private ArrayList<SpellWrapper> breakableSpells;
	private ArrayList<StateChooser> stateChoosers = new ArrayList<StateChooser>();

	private JLabel notorietyAmount;
	private JLabel fameAmount;
	private JLabel goldAmount;
	
	private JToggleButton noColor;
	private JToggleButton colorWhite;
	private JToggleButton colorGray;
	private JToggleButton colorGold;
	private JToggleButton colorPurple;
	private JToggleButton colorBlack;
	
	public CharacterEditRibbon(JFrame parentFrame,CharacterWrapper character) {
		this.parentFrame = parentFrame;
		this.character = character;
		GamePool pool = new GamePool(character.getGameData().getGameObjects());
		spellChoices = pool.find("spell,learnable,!virtual,!instance");
		initComponents();
	}
	private void initComponents() {
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		setBorder(BorderFactory.createTitledBorder(character.getName()));
		if (character.hasCheated()) {
			Box line = Box.createHorizontalBox();
			JButton removeCheaterFlag = new JButton("Remove \"Cheater!\" flag from character.");
			removeCheaterFlag.setBorder(BorderFactory.createLineBorder(Color.red,2));
			removeCheaterFlag.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					character.clearCheater();
					JButton button = (JButton)ev.getSource();
					button.setVisible(false);
				}
			});
			ComponentTools.lockComponentSize(removeCheaterFlag,250,30);
			line.add(removeCheaterFlag);
			line.add(Box.createHorizontalGlue());
			box.add(line);
		}
		box.add(buildStateEditor());
		box.add(buildChitEditor());
		add(box,BorderLayout.WEST);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buildStatsEditor(),BorderLayout.NORTH);
		panel.add(buildRelationshipEditor(),BorderLayout.CENTER);
		panel.add(buildSpellManager(),BorderLayout.SOUTH);
		add(panel,BorderLayout.CENTER);
		updatePanel();
	}
	private void updateOwnedSpells() {
		ownedSpells = new ArrayList<SpellWrapper>();
		for(GameObject go:character.getAllSpells()) {
			ownedSpells.add(new SpellWrapper(go));
		}
		ownedSpellsList.setModel(ownedSpellsListModel = new OwnedSpellsListModel());
	}
	private void updateBreakableSpells() {
		SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(character.getGameData());
		breakableSpells = spellMaster.getAffectingSpells(character.getGameObject());
		for(SpellWrapper spell:character.getAliveSpells()) { // let's also add those spells that this character cast, but may not be bewitching the character.
			boolean found = false;
			for(SpellWrapper bw:breakableSpells) {
				if (bw.getGameObject().getStringId().equals(spell.getGameObject().getStringId())) {
					found = true;
					break;
				}
			}
			if (!found) breakableSpells.add(spell);
		}
		breakableSpellsList.setModel(breakableSpellsListModel = new BreakableSpellsListModel());
	}
	public void refresh() {
		updatePanel();
		for(StateChooser chooser:stateChoosers) {
			chooser.revertToData();
		}
		repaint();
	}
	private void updatePanel() {
		notorietyAmount.setText(character.getNotorietyString());
		fameAmount.setText(character.getFameString());
		goldAmount.setText(character.getGoldString());
		String colorSource = character.getGameObject().getThisAttribute("color_source");
		noColor.setSelected(colorSource==null);
		colorWhite.setSelected("white".equals(colorSource));
		colorGray.setSelected("gray".equals(colorSource));
		colorGold.setSelected("gold".equals(colorSource));
		colorPurple.setSelected("purple".equals(colorSource));
		colorBlack.setSelected("black".equals(colorSource));
	}
	private JPanel buildChitEditor() {
		JPanel chitMain = new JPanel(new BorderLayout());
		ChitBinLayout layout = new ChitBinLayout(character.getCompleteChitList());
		chitsPanel = new ChitBinPanel(layout) {
			public boolean canClickChit(ChitComponent aChit) {
				return true;
			}
			public void handleClick(Point p) {
				ChitComponent chit = chitsPanel.getClickedChit(p);
				if (chit!=null && chit.isActionChit()) {
					CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
					if (achit.isActive()) {
						if (achit.isEnchantable()) {
							achit.enchant();
						}
						else if (achit.isMagic()) {
							achit.makeAlerted();
						}
						else {
							achit.makeFatigued();
							if (!achit.isFatigued()) { // some chits cannot be fatigued
								achit.makeWounded();
							}
						}
					}
					else if (achit.isColor()) {
						achit.makeAlerted();
					}
					else if (achit.isAlerted()) {
						achit.makeFatigued();
						if (!achit.isFatigued()) { // some chits cannot be fatigued
							achit.makeWounded();
						}
					}
					else if (achit.isFatigued()) {
						achit.makeWounded();
					}
					else if (achit.isWounded()) {
						achit.makeActive();
					}
				}
				chitsPanel.repaint();
			}
		};
		chitsPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				chitsPanel.handleClick(ev.getPoint());
			}
		});
		chitMain.add(chitsPanel,BorderLayout.CENTER);
		JLabel chitsPanelLabel = new JLabel("Click chits to change state");
		chitsPanelLabel.setForeground(Color.red);
		chitMain.add(chitsPanelLabel,BorderLayout.NORTH);
		chitsPanel.addChits(character.getCompleteChitList());
		return chitMain;
	}
	/*
	 * Curses: 6
	 * Blocked - table
	 * Hidden - table
	 * Find Hidden Enemies
	 */
	private Box buildStateEditor() {
		stateGroup = new UniformLabelGroup();
		//stateGroup.setLabelFont(new Font("Dialog",Font.BOLD,18));
		Box box = Box.createVerticalBox();
		Box line;
		line = Box.createVerticalBox();
		line.add(createStateChooser("Found Hidden Enemies",character.getBlockName(),CharacterWrapper.FOUND_HIDDEN_ENEMIES));
		line.add(Box.createVerticalStrut(10));
		line.add(createStateChooser("Eyemist",CharacterWrapper.CURSES_BLOCK,Constants.EYEMIST));
		line.add(createStateChooser("Squeak",CharacterWrapper.CURSES_BLOCK,Constants.SQUEAK));
		line.add(createStateChooser("Wither",CharacterWrapper.CURSES_BLOCK,Constants.WITHER));
		line.add(createStateChooser("Ill Health",CharacterWrapper.CURSES_BLOCK,Constants.ILL_HEALTH));
		line.add(createStateChooser("Ashes",CharacterWrapper.CURSES_BLOCK,Constants.ASHES));
		line.add(createStateChooser("Disgust",CharacterWrapper.CURSES_BLOCK,Constants.DISGUST));
		line.add(Box.createVerticalGlue());
		box.add(line);
		
		box.add(Box.createVerticalGlue());
		
		return box;
	}
	private StateChooser createStateChooser(String title,String block,String key) {
		StateChooser chooser = new StateChooser(title,character,block,key);
		stateChoosers.add(chooser);
		return chooser;
	}
	private JComponent buildStatsEditor() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel statsPanel = new JPanel(new GridLayout(5,3));
		notorietyAmount = addStatAdjusters(statsPanel,"Recorded Notoriety:",new UpDownButton() {
			public void changeAmount(int value) {
				character.addNotoriety(value);
				updatePanel();
			}
		});
		fameAmount = addStatAdjusters(statsPanel,"Recorded Fame:",new UpDownButton() {
			public void changeAmount(int value) {
				character.addFame(value);
				updatePanel();
			}
		});
		goldAmount = addStatAdjusters(statsPanel,"Recorded Gold:",new UpDownButton() {
			public void changeAmount(int value) {
				character.addGold(value);
				updatePanel();
			}
		});
		for (int i=0;i<6;i++) statsPanel.add(Box.createGlue());
		statsPanel.setBorder(BorderFactory.createTitledBorder("Recorded Stats"));
		panel.add(statsPanel,BorderLayout.CENTER);
		
		JPanel colorPanel = new JPanel(new GridLayout(6,1));
		ButtonGroup group = new ButtonGroup();
		noColor = new JToggleButton("No Color");
		noColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.getGameObject().removeThisAttribute("color_source");
			}
		});
		group.add(noColor);
		colorPanel.add(noColor);
		colorWhite = new JToggleButton("White Magic");
		colorWhite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.getGameObject().setThisAttribute("color_source","white");
			}
		});
		group.add(colorWhite);
		colorPanel.add(colorWhite);
		colorGray = new JToggleButton("Gray Magic");
		colorGray.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.getGameObject().setThisAttribute("color_source","gray");
			}
		});
		group.add(colorGray);
		colorPanel.add(colorGray);
		colorGold = new JToggleButton("Gold Magic");
		colorGold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.getGameObject().setThisAttribute("color_source","gold");
			}
		});
		group.add(colorGold);
		colorPanel.add(colorGold);
		colorPurple = new JToggleButton("Purple Magic");
		colorPurple.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.getGameObject().setThisAttribute("color_source","purple");
			}
		});
		group.add(colorPurple);
		colorPanel.add(colorPurple);
		colorBlack = new JToggleButton("Black Magic");
		colorBlack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.getGameObject().setThisAttribute("color_source","black");
			}
		});
		group.add(colorBlack);
		colorPanel.add(colorBlack);
		colorPanel.setBorder(BorderFactory.createTitledBorder("Color Magic"));
		panel.add(colorPanel,BorderLayout.EAST);
		
		return panel;
	}
	private JLabel addStatAdjusters(JPanel panel,String title,UpDownButton adjusters) {
		panel.add(new JLabel(title,JLabel.RIGHT));
		Box line = Box.createHorizontalBox();
		JLabel label;
		line.add(label = new JLabel("",JLabel.RIGHT));
		label.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLoweredBevelBorder(),
				BorderFactory.createEmptyBorder(2,2,2,2)));
		ComponentTools.lockComponentSize(label,50,25);
		ComponentTools.lockComponentSize(adjusters,100,25);
		line.add(adjusters);
		line.add(Box.createHorizontalGlue());
		panel.add(line);
		panel.add(new JLabel());
		return label;
	}
	private JScrollPane buildRelationshipEditor() {
		relationshipNames = getRelationshipNames();
		relationshipTable = new RelationshipTable(relationshipNames,character);
		relationshipTable.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent ev) {
				updateRelationshipMouse(ev);
			}
		});
		relationshipTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				updateRelationshipMouse(ev);
			}
		});
		return new JScrollPane(relationshipTable);
	}
	private JComponent buildSpellManager() {
		JPanel panel = new JPanel(new GridLayout(1,2));
		panel.add(buildBreakableSpellsList(),BorderLayout.CENTER);
		panel.add(buildSpellEditor(),BorderLayout.WEST);
		return panel;
	}
	private JComponent buildSpellEditor() {
		JPanel panel = new JPanel(new BorderLayout());
		ownedSpellsList = new JList();
		updateOwnedSpells();
		panel.add(new JScrollPane(ownedSpellsList));
		JButton addStartSpell = new JButton("Add Starting");
		addStartSpell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				GameObject go = getSelectedSpell();
				character.startingSpell(go);
				updateOwnedSpells();
			}
		});
		JButton addRecordedSpell = new JButton("Add Recorded");
		addRecordedSpell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				GameObject go = getSelectedSpell();
				character.recordNewSpell(parentFrame,go);
				updateOwnedSpells();
			}
		});
		JButton removeSpell = new JButton("Remove");
		removeSpell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int index = ownedSpellsList.getSelectedIndex();
				SpellWrapper spell = ownedSpellsListModel.getSpellAt(index);
				if (spell==null) return;
				if (ownedSpellsListModel.isAlive(spell)) {
					JOptionPane.showMessageDialog(parentFrame,"Break all instances of the spell before removing it.");
					return;
				}
				character.eraseSpell(spell.getGameObject());
				updateOwnedSpells();
			}
		});
		JPanel buttons = new JPanel(new GridLayout(1,3));
		buttons.add(addStartSpell);
		buttons.add(addRecordedSpell);
		buttons.add(removeSpell);
		panel.add(buttons,BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createTitledBorder("Spells"));
		return panel;
	}
	private GameObject getSelectedSpell() {
		SpellSelector selector = new SpellSelector(parentFrame,character.getGameData(),spellChoices,1);
		selector.setVisible(true);
		return selector.getSpellSelection().get(0);
	}
	private JComponent buildBreakableSpellsList() {
		breakableSpellsList = new JList();
		updateBreakableSpells();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(breakableSpellsList),BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createTitledBorder("Active Spells"));
		JButton breakSpellButton = new JButton("Break Selected Spell");
		breakSpellButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev) {
				int index = breakableSpellsList.getSelectedIndex();
				SpellWrapper spell = breakableSpellsListModel.getSpellAt(index);
				if (spell==null) return;
				spell.expireSpell();
				updateBreakableSpells();
				ownedSpellsList.repaint();
			}
		});
		panel.add(breakSpellButton,BorderLayout.SOUTH);
		return panel;
	}
	private void updateRelationshipMouse(MouseEvent ev) {
		Point p = ev.getPoint();
		int row = relationshipTable.rowAtPoint(p);
		int col = relationshipTable.columnAtPoint(p);
		if (row>=0 && row<relationshipTable.getRowCount() && col>0 && col<relationshipTable.getColumnCount()) {
			int rel = col-3;
			//String name = RealmCharacterConstants.DEFAULT_RELATIONSHIPS[row][1].substring(1);
			String[] relationshipName = relationshipNames.get(row);
			setRelationship(relationshipName,rel);
			repaint();
		}
	}
	private void setRelationship(String[] denizen,int rel) {
		if (rel==0) {
			character.getGameObject().removeAttribute(denizen[0],denizen[1].substring(1));
		}
		else {
			character.getGameObject().setAttribute(denizen[0],denizen[1].substring(1),rel);
		}
	}
	private ArrayList<String[]> getRelationshipNames() {
		ArrayList<String[]> relationshipNames;
		ArrayList keyVals = new ArrayList();
		HostPrefWrapper	hostPrefs = HostPrefWrapper.findHostPrefs(character.getGameData());
		keyVals.add(hostPrefs.getGameKeyVals());
		GamePool pool = new GamePool(character.getGameData().getGameObjects());
		relationshipNames = new ArrayList<String[]>();
		for (GameObject nativeLeader : pool.find("native,rank=HQ")) {
			String nativeName = nativeLeader.getThisAttribute("native");
			String relBlock = RealmUtility.getRelationshipBlockFor(nativeLeader);
			String[] ret = new String[2];
			ret[0] = relBlock;
			ret[1] = "N" + StringUtilities.capitalize(nativeName);
			relationshipNames.add(ret);
		}
		for (GameObject visitor : pool.find("visitor")) {
			String visitorName = visitor.getThisAttribute("visitor");
			String relBlock = RealmUtility.getRelationshipBlockFor(visitor);
			String[] ret = new String[2];
			ret[0] = relBlock;
			ret[1] = "V" + StringUtilities.capitalize(visitorName);
			relationshipNames.add(ret);
		}
		Collections.sort(relationshipNames, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				int ret = o1[0].compareTo(o2[0]);
				if (ret == 0) {
					ret = o1[1].compareTo(o2[1]);
				}
				return ret;
			}
		});
		return relationshipNames;
	}
	private static UniformLabelGroup stateGroup;
	private class StateChooser extends JPanel {
		
		private JLabel label;
		private RockerSwitch rocker;
		
		private CharacterWrapper character;
		private String blockName;
		private String keyName;
		
		public StateChooser(String title,CharacterWrapper character,String blockName,String keyName) {
			setLayout(new BorderLayout(5,5));
			
			this.character = character;
			this.blockName = blockName;
			this.keyName = keyName;
			
			label = stateGroup.createLabel(title);
			add(label,BorderLayout.CENTER);
			rocker = new RockerSwitch(character.getGameObject().hasAttribute(blockName,keyName));
			rocker.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					update();
				}
			});
			add(rocker,BorderLayout.EAST);
			update();
		}
		public void revertToData() {
			if (character.getGameObject().hasAttribute(blockName,keyName)) {
				if (!rocker.isOn()) rocker.doClickOn();
			}
			else {
				if (rocker.isOn()) rocker.doClickOff();
			}
		}
		private void update() {
			if (rocker.isOn()) {
				if (!character.getGameObject().hasAttribute(blockName,keyName)) {
					character.getGameObject().setAttribute(blockName,keyName);
				}
				label.setBorder(BorderFactory.createLineBorder(Color.green,3));
			}
			else {
				if (character.getGameObject().hasAttribute(blockName,keyName)) {
					character.getGameObject().removeAttribute(blockName,keyName);
				}
				label.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
			}
		}
	}
	private class OwnedSpellsListModel extends AbstractListModel {
		public int getSize() {
			return ownedSpells.size();
		}
		
		public boolean isAlive(SpellWrapper spell) {
			boolean alive = spell.isAlive();
			if (!alive) {
				ArrayList<GameObject> vs = character.getAllVirtualSpellsFor(spell.getGameObject());
				for(GameObject go:vs) {
					SpellWrapper virt = new SpellWrapper(go);
					if (virt.isAlive()) {
						alive = true;
						break;
					}
				}
			}
			return alive;
		}

		public Object getElementAt(int index) {
			if (index>=ownedSpells.size() || index<0) return null;
			SpellWrapper spell = ownedSpells.get(index);
			boolean starting = character.isStartingSpell(spell.getGameObject());
			boolean alive = isAlive(spell);
			
			return spell.getName()+ (starting?" (starting)":"") + (alive?" ALIVE":"");
		}
		
		public SpellWrapper getSpellAt(int index) {
			if (index>=ownedSpells.size() || index<0) return null;
			return ownedSpells.get(index);
		}
	}
	private class BreakableSpellsListModel extends AbstractListModel {
		public int getSize() {
			return breakableSpells.size();
		}

		public Object getElementAt(int index) {
			if (index>=breakableSpells.size() || index<0) return null;
			SpellWrapper spell = breakableSpells.get(index);
			return spell.getName()+" cast by "+spell.getCaster().getName();
		}
		
		public SpellWrapper getSpellAt(int index) {
			if (index>=breakableSpells.size() || index<0) return null;
			return breakableSpells.get(index);
		}
	}
	private abstract class UpDownButton extends JPanel {
		public abstract void changeAmount(int val);
		public UpDownButton() {
			super(new GridLayout(1,4));
			JButton bigSub = new ForceTextButton("--");
			bigSub.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					changeAmount(-10);
				}
			});
			add(bigSub);
			JButton sub = new ForceTextButton("-");
			sub.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					changeAmount(-1);
				}
			});
			add(sub);
			JButton add = new ForceTextButton("+");
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					changeAmount(1);
				}
			});
			add(add);
			JButton bigAdd = new ForceTextButton("++");
			bigAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					changeAmount(10);
				}
			});
			add(bigAdd);
		}
	}
}