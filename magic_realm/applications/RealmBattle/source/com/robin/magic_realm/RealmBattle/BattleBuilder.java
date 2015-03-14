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
package com.robin.magic_realm.RealmBattle;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.*;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

/**
 * A GUI for building battle situations
 */
public class BattleBuilder extends JFrame {
	private static final Font BIG_FONT = new Font("Dialog",Font.PLAIN,18);
	public static final String BATTLE_BUILDER_KEY = "_BATTLE_BUILDER_";
	public static final String BATTLE_CLEARING_KEY = "___BATTLE__Clearing_";
	public static final String CHARACTER_PRESENT = "_CHAR_PRES_";
	
	private static final String testPlayerName = "test";
	
	private GameData gameData;
	private GamePool pool;
	private HostPrefWrapper hostPrefs;
	
	private JTabbedPane tabbedPane;
	
	private JButton changeClearingButton;
	private JLabel clearingTitle;
	private JCheckBox makeDuplicatesOption;
	private JButton addCharacterButton;
	private JButton castSpellButton;
	
	private RealmObjectPanel denizenPanel;
	private JButton addDenizensButton;
	private JButton removeDenizensButton;
	
	private ArrayList<CharacterBattleBuilderPanel> characterPanels;
	
	private JButton cancelButton;
	private JButton editOptionsButton;
	private JButton saveAndFinishButton;
	private JButton finishButton;
	
	// Info
	private ClearingDetail battleClearing;
//	private ClearingDetail alternateClearing; // for handling character "away"
	
	private boolean cancelled = false;
	
	public BattleBuilder() {
		characterPanels = new ArrayList<CharacterBattleBuilderPanel>();
		initComponents();
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public GameData getGameData() {
		return gameData;
	}
	public GamePool getPool() {
		return pool;
	}
	private boolean saidYes(String message) {
		int ret = JOptionPane.showConfirmDialog(null,message,"Realm Battle",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		return ret==JOptionPane.YES_OPTION;
	}
	private void initComponents() {
		setSize(800,650);
		setLocationRelativeTo(null);
		setTitle("RealmSpeak Battle Builder");
		getContentPane().setLayout(new BorderLayout());
		
		Box box;
		JPanel panel;
		
		// Top Controls
		box = Box.createHorizontalBox();
		changeClearingButton = new JButton("Change Battle Clearing:");
		changeClearingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				changeClearing();
			}
		});
		box.add(changeClearingButton);
		box.add(Box.createHorizontalStrut(10));
		clearingTitle = new JLabel("");
		box.add(clearingTitle);
		box.add(Box.createHorizontalGlue());
		
		makeDuplicatesOption = new JCheckBox("Make Duplicates",false);
		box.add(makeDuplicatesOption);
		
		box.add(Box.createHorizontalGlue());
		addCharacterButton = new JButton("Add Character Tab");
		addCharacterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				addCharacter();
			}
		});
		box.add(addCharacterButton);
		box.add(Box.createHorizontalGlue());
		
		castSpellButton = new JButton("Cast Permanent Spell");
		castSpellButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				castSpell();
			}
		});
		box.add(castSpellButton);
		box.add(Box.createHorizontalGlue());
		box.setBorder(BorderFactory.createEtchedBorder());
		getContentPane().add(box,"North");
		
		// Tabbed Pane
		tabbedPane = new JTabbedPane();
		tabbedPane.setFont(BIG_FONT);
		getContentPane().add(tabbedPane,"Center");
		
		// Denizen Panel
		panel = new JPanel(new BorderLayout());
		denizenPanel = new RealmObjectPanel(true,true);
		denizenPanel.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		panel.add(new JScrollPane(denizenPanel),"Center");
		box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		addDenizensButton = new JButton("Add Denizens");
		addDenizensButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				addDenizens();
			}
		});
		box.add(addDenizensButton);
		box.add(Box.createGlue());
		removeDenizensButton = new JButton("Remove Denizens");
		removeDenizensButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				removeDenizens();
			}
		});
		box.add(removeDenizensButton);
		box.add(Box.createGlue());
		panel.add(box,"North");
		tabbedPane.addTab("Denizens",panel);
		
		// Dialog controls
		box = Box.createHorizontalBox();
		cancelButton = new JButton("Cancel");
		cancelButton.setFont(BIG_FONT);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				cancelled = true;
				setVisible(false);
				CombatFrame.close();
			}
		});
		box.add(cancelButton);
		box.add(Box.createHorizontalGlue());
		editOptionsButton = new JButton("Game Options...");
		editOptionsButton.setFont(BIG_FONT);
		editOptionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				HostGameSetupDialog setup = new HostGameSetupDialog(new JFrame(),"Game Options for Battle",gameData);
				setup.loadPrefsFromData();
				setup.setVisible(true);
			}
		});
		box.add(editOptionsButton);
		box.add(Box.createHorizontalGlue());
		saveAndFinishButton = new JButton("Save and Play");
		saveAndFinishButton.setFont(BIG_FONT);
		saveAndFinishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doFinish();
				CombatFrame.saveBattle(new JFrame(),gameData);
				cancelled = false;
				setVisible(false);
				CombatFrame.startCombat(gameData);
			}
		});
		box.add(saveAndFinishButton);
		box.add(Box.createHorizontalStrut(10));
		finishButton = new JButton("Play (no save)");
		finishButton.setFont(BIG_FONT);
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doFinish();
				cancelled = false;
				setVisible(false);
				CombatFrame.startCombat(gameData);
			}
		});
		box.add(finishButton);
		getContentPane().add(box,"South");
	}
	public boolean initialize(GameData data) {
		if (data==null) {
			// Building a new battle
			System.out.print("Loading data...");
			RealmLoader loader = new RealmLoader();
			System.out.println("Done.");
			gameData = loader.getData();
			
			// Set default starting clearing
			GameObject selectedTile = gameData.getGameObjectByName("Crag");
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(selectedTile);
			tile.setDarkSideUp();
			battleClearing = tile.getClearing(2);
			
			// Select game prefs...
			HostGameSetupDialog setup = new HostGameSetupDialog(new JFrame(),"Game Options for Battle",gameData);
			// Setup the local prefs (if any)
			setup.loadPrefsFromLocalConfiguration();
			setup.setVisible(true);
			
			if (setup.getDidStart()) {
				setup.savePrefsToLocalConfiguration();
				hostPrefs = HostPrefWrapper.findHostPrefs(gameData);
				updatePool();
				
				// Add a custom character keylist
				RealmCharacterBuilderModel.addCustomCharacters(hostPrefs,gameData);
						
				// Some items require a spell be cast (Flying Carpet)
				ArrayList keyVals = new ArrayList();
				keyVals.add(hostPrefs.getGameKeyVals());
				keyVals.add(Constants.CAST_SPELL_ON_INIT);
				SpellMasterWrapper.getSpellMaster(gameData); // make sure SpellMaster is created
				Collection needsSpellInit = pool.find(keyVals);
				for (Iterator i=needsSpellInit.iterator();i.hasNext();) {
					GameObject go = (GameObject)i.next();
					for (Iterator n=go.getHold().iterator();n.hasNext();) {
						GameObject sgo = (GameObject)n.next();
						if (sgo.hasThisAttribute("spell")) {
							SpellWrapper spell = new SpellWrapper(sgo);
							spell.castSpellNoEnhancedMagic(go);
							spell.addTarget(hostPrefs,go);
							spell.makeInert(); // starts off as inert
						}
					}
				}
				
				// Convert all traveler templates to travelers, so they can be tested here
				ArrayList<GameObject> travelerChits = pool.find("traveler");
				if (travelerChits.size()>0) {
					GameObject sample = travelerChits.get(0);
					for (GameObject go:pool.find("traveler_template")) {
						go.copyAttributeBlockFrom(sample,"this");
						go.setThisAttribute(Constants.TEMPLATE_ASSIGNED);
						go.removeThisAttribute(Constants.TRAVELER_TEMPLATE);
					}
					for (GameObject go:travelerChits) {
						go.removeThisAttribute("traveler"); // nullify these!
					}
				}
				
				updateControls();
				return true;
			}
			else {
				CombatFrame.close();
			}
		}
		else {
			// Editing an existing battle
			gameData = data;
			hostPrefs = HostPrefWrapper.findHostPrefs(gameData);
			pool = new GamePool(gameData.getGameObjects());
			pool = new GamePool(pool.find(hostPrefs.getGameKeyVals()));
			
			if (setupBuilderWithData()) {
				updateControls();
				return true;
			}
		}
		
		return false;
	}
	private void updatePool() {
		pool = new GamePool(gameData.getGameObjects());
		pool = new GamePool(pool.find(hostPrefs.getGameKeyVals()));
	}
	private boolean setupBuilderWithData() {
		// Fetch battleClearing
		GameObject bcObj = getBattleClearingReferenceObject(gameData);
		String v = bcObj.getThisAttribute("version");
		if (v==null || !v.equals(Constants.REALM_SPEAK_VERSION)) {
			JOptionPane.showMessageDialog(null,"Incompatible save file - wrong version","Invalid Save File",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		String val = bcObj.getThisAttribute("battleClearing");
		TileLocation tl = TileLocation.parseTileLocation(gameData,val);
		battleClearing = tl.clearing;
		
		// Load up the builder
		BattleModel model = RealmBattle.buildBattleModel(tl, gameData);
		
		// Denizens...
		BattleGroup denGroup = model.getDenizenBattleGroup();
		if (denGroup!=null) {
			ArrayList toAdd = new ArrayList();
			for (Iterator i=denGroup.getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				GameObject go = rc.getGameObject();
				go.setThisAttribute(BATTLE_BUILDER_KEY);
				toAdd.add(go);
			}
			denizenPanel.addObjects(toAdd);
		}
		
		// Characters...
		for (Iterator b=model.getAllBattleGroups(false).iterator();b.hasNext();) {
			BattleGroup group = (BattleGroup)b.next();
			RealmComponent rc = group.getOwningCharacter();
			
			// Add tab
			CharacterBattleBuilderPanel panel = new CharacterBattleBuilderPanel(this,hostPrefs,rc.getGameObject());
			characterPanels.add(panel);
			tabbedPane.addTab(rc.getGameObject().getName(),panel);
			tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
			
			if (group.isCharacterInBattle()) {
				rc.getGameObject().setThisAttribute(BATTLE_BUILDER_KEY);
			}
			
			// Tag everyone else
			for (Iterator i=group.getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent bp = (RealmComponent)i.next();
				if (bp!=rc) {
					bp.getGameObject().setThisAttribute(BATTLE_BUILDER_KEY);
				}
			}
			
		}
		return true;
	}
	private void updateControls() {
		clearingTitle.setText(battleClearing.fullString());
		removeDenizensButton.setEnabled(denizenPanel.getSelectedCount()>0);
		
		boolean isCombat = (denizenPanel.getComponentCount()>0 && characterPanels.size()>0) || characterPanels.size()>1;
		saveAndFinishButton.setEnabled(isCombat);
		finishButton.setEnabled(isCombat);
	}
	private void changeClearing() {
		// Choose a tile and clearing
		Collection tiles = pool.find("tile,"+hostPrefs.getGameKeyVals());
		Hashtable tileHash = new Hashtable();
		for (Iterator i=tiles.iterator();i.hasNext();) {
			GameObject tile = (GameObject)i.next();
			tileHash.put(tile.getName(),tile);
		}
		ArrayList tileNames = new ArrayList(tileHash.keySet());
		Collections.sort(tileNames);
		String tileName = (String)JOptionPane.showInputDialog(
				null,
				"Select a tile where combat is occuring:",
				"Select Tile",
				JOptionPane.QUESTION_MESSAGE,
				null,
				tileNames.toArray(),
				tileNames.iterator().next());
		
		if (tileName==null) {
			return;
		}
		GameObject selectedTile = (GameObject)tileHash.get(tileName);
		
		TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(selectedTile);
		if (saidYes("Do you want to use the Enchanted side of the "+selectedTile.getName()+"?")) {
			tile.setDarkSideUp();
		}
		else {
			tile.setLightSideUp();
		}
		
		ArrayList clearingNames = new ArrayList();
		Hashtable clearingHash = new Hashtable();
		for (int i=1;i<=6;i++) {
			ClearingDetail clearing = tile.getClearing(i);
			if (clearing!=null) {
				clearingHash.put(clearing.fullString(),clearing);
				clearingNames.add(clearing.fullString());
			}
		}
		String clearingName = (String)JOptionPane.showInputDialog(
				null,
				"Select a clearing where combat is occurring:",
				"Select Clearing",
				JOptionPane.QUESTION_MESSAGE,
				null,
				clearingNames.toArray(),
				clearingNames.iterator().next());
		if (clearingName==null) {
			return;
		}
		battleClearing = (ClearingDetail)clearingHash.get(clearingName);
		updateControls();
	}
	protected void checkHorses(Collection denizens) {
		ArrayList horses = new ArrayList();
		for (Iterator i=denizens.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			BattleHorse horse = rc.getHorse();
			if (horse!=null) {
				horses.add(horse.getGameObject());
			}
		}
		
		if (!horses.isEmpty()) {
			int ret = JOptionPane.showConfirmDialog(this,"Include horses?","",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
			boolean includeHorses = ret==JOptionPane.YES_OPTION;
			for (Iterator i=horses.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				if (includeHorses) {
					go.removeThisAttribute(Constants.DEAD);
				}
				else {
					go.setThisAttribute(Constants.DEAD);
				}
			}
		}
	}
	private void addDenizens() {
		Collection monsters = pool.find("monster,!part,"+hostPrefs.getGameKeyVals()+",!"+BATTLE_BUILDER_KEY);
		Collection natives = pool.find("native,!horse,!treasure,"+hostPrefs.getGameKeyVals()+",!"+BATTLE_BUILDER_KEY);
		RealmObjectChooser denizenChooser = new RealmObjectChooser("Choose Denizens to Add:",gameData,false);
		denizenChooser.addObjectsToChoose(monsters);
		denizenChooser.addObjectsToChoose(natives);
		denizenChooser.setVisible(true);
		if (denizenChooser.pressedOkay()) {
			Collection chosenDenizens = denizenChooser.getChosenObjects();
			if (chosenDenizens!=null && chosenDenizens.size()>0) {
				chosenDenizens = makeDuplicates(chosenDenizens); // only if the option is selected
				checkHorses(chosenDenizens);
				for (Iterator i=chosenDenizens.iterator();i.hasNext();) {
					GameObject go = (GameObject)i.next();
					go.setThisAttribute(BATTLE_BUILDER_KEY);
				}
				denizenPanel.clearSelected();
				denizenPanel.addObjects(chosenDenizens);
				updateControls();
			}
		}
	}
	public Collection makeDuplicates(Collection in) {
		if (makeDuplicatesOption.isSelected()) {
			ArrayList dups = new ArrayList();
			for (Iterator i=in.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				GameObject dup = gameData.createNewObject(go);
				Collection hold = go.getHold();
				if (!hold.isEmpty()) {
					// This recursive behavior will guarantee that the duplication goes deep
					dup.addAll(makeDuplicates(hold));
				}
				dups.add(dup);
			}
			// Refresh the pool so these new objects can be found
			pool = new GamePool(gameData.getGameObjects());
			return dups;
		}
		return in;
	}
	private void removeDenizens() {
		ArrayList all = new ArrayList(Arrays.asList(denizenPanel.getComponents()));
		Collection selDenizens = denizenPanel.getSelectedComponents();
		for (Iterator i=selDenizens.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			rc.getGameObject().removeThisAttribute(BATTLE_BUILDER_KEY);
			all.remove(rc);
			battleClearing.remove(rc.getGameObject());
		}
		denizenPanel.clearSelected();
		denizenPanel.removeAll();
		denizenPanel.addRealmComponents(all);
		updateControls();
	}
	private void addCharacter() {
		CharacterWrapper lastCharacter = null;
		if (characterPanels.size()>0) {
			CharacterBattleBuilderPanel panel = characterPanels.get(0);
			lastCharacter = panel.getCharacter();
		}
		
		ArrayList<GameObject> characters = pool.find("character,!"+CharacterWrapper.NAME_KEY);
		characters.addAll(CustomCharacterLibrary.getSingleton().getCharacterTemplateList());
		Collections.sort(characters,new Comparator<GameObject>() {
			public int compare(GameObject go1,GameObject go2) {
				return go1.getName().compareTo(go2.getName());
			}
		});
		
		CharacterChooser chooser = new CharacterChooser(this,characters,hostPrefs);
		chooser.setVisible(true);
		
		GameObject chosen = chooser.getChosenCharacter();
		if (chosen!=null) {
			if (chosen.hasThisAttribute(Constants.CUSTOM_CHARACTER)) {
				GameObject newChar = gameData.createNewObject();
				newChar.copyAttributesFrom(chosen);
				RealmComponent.clearOwner(newChar);
				newChar.setThisKeyVals(hostPrefs.getGameKeyVals());
				for (Iterator i=chosen.getHold().iterator();i.hasNext();) {
					GameObject go = (GameObject)i.next();
					if (go.hasThisAttribute("character_chit")) {
						GameObject newChit = gameData.createNewObject();
						newChit.copyAttributesFrom(go);
						newChit.setThisKeyVals(hostPrefs.getGameKeyVals());
						newChar.add(newChit);
					}
				}
				chosen = newChar;
				updatePool();
			}
			
			CharacterWrapper character = new CharacterWrapper(chosen);
			battleClearing.add(character.getGameObject(),null);
			character.setPlayerName(testPlayerName);
			character.setPlayerPassword("");
			character.setPlayerEmail(""); // no e-mail for battle tests
			character.setCharacterLevel(4);
			character.updateLevelAttributes(hostPrefs);
			character.initChits();
			character.fetchStartingInventory(this,gameData,false);
			character.clearRelationships(hostPrefs);
			character.initRelationships(hostPrefs);
			character.setGold(50);
			chosen.setThisAttribute(BATTLE_BUILDER_KEY);
			if (lastCharacter!=null) {
				character.setEnemyCharacter(lastCharacter.getGameObject(),true);
			}
			
			// Choose Spells
			RealmUtility.fetchStartingSpells(this,character,gameData,false);
			
			// Add tab
			CharacterBattleBuilderPanel panel = new CharacterBattleBuilderPanel(this,hostPrefs,chosen);
			characterPanels.add(panel);
			tabbedPane.addTab(chosen.getName(),panel);
			tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
			updateControls();
		}
	}
	private void updateDenizenPanel() {
		denizenPanel.removeAll();
		ArrayList<GameObject> denizens = new ArrayList<GameObject>();
		denizens.addAll(pool.find("monster,!part,"+hostPrefs.getGameKeyVals()+","+BATTLE_BUILDER_KEY));
		denizens.addAll(pool.find("native,!horse,!treasure,"+hostPrefs.getGameKeyVals()+","+BATTLE_BUILDER_KEY));
		for(GameObject denizen:denizens) {
			RealmComponent rc = RealmComponent.getRealmComponent(denizen);
			if (rc.getCurrentLocation()!=null) {
				// absorbed!
				rc.getGameObject().removeThisAttribute(BATTLE_BUILDER_KEY);
				continue;
			}
			denizenPanel.add(rc);
		}
	}
	public void castSpell() {
		SpellWrapper spell = querySpell();
		if (spell==null) return;

		String spellType = spell.getGameObject().getThisAttribute("spell");
		GameObject caster = queryCaster(spellType);
		if (caster==null) return;
		
		GameObject incantation = queryIncantation(caster,spellType);
		if (incantation==null) return;
		
		// Ask for a target
		RealmComponent target = queryTarget(spell.getGameObject().getThisAttribute("target"));
		if (target==null) return;
		
		CharacterWrapper casterCharacter = new CharacterWrapper(caster);
		ClearingUtility.moveToLocation(caster,battleClearing.getTileLocation());
		spell = new SpellWrapper(casterCharacter.recordNewSpell(this,spell.getGameObject(),true));
		
		if (spell.getName().toLowerCase().startsWith("transform")) { // Handle special case
			int redDie = SpellUtility.chooseRedDie(this,"transform",casterCharacter);
			spell.setRedDieLock(redDie);
		}
		
		// Ask if spell is INERT or ALIVE
		int ret = JOptionPane.showConfirmDialog(
				this,
				"Do you want the spell to be ALIVE at the start of combat?",
				"Cast Spell",
				JOptionPane.YES_NO_OPTION);
		
		// Cast the spell
		spell.castSpellNoEnhancedMagic(incantation);
		spell.addTarget(hostPrefs,target.getGameObject());
		caster.addThisAttributeListItem("diemod","1d:all:all");
		spell.affectTargets(this,GameWrapper.findGame(gameData),false);
		caster.removeThisAttributeListItem("diemod","1d:all:all");
		if (ret==JOptionPane.NO_OPTION) {
			spell.unaffectTargets();
			spell.makeInert();
		}
		for(CharacterBattleBuilderPanel panel:characterPanels) {
			panel.refresh();
		}
		updateDenizenPanel();
		ClearingUtility.moveToLocation(caster,null);
		repaint();
	}
	private SpellWrapper querySpell() {
		ArrayList<GameObject> spells = pool.find("spell,duration=permanent");
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		for (GameObject spell:spells) {
			String spellType = spell.getThisAttribute("spell").trim();
			if (spellType.length()==0 || !"IIIVIII".contains(spellType)) {
				toRemove.add(spell);
				continue;
			}
			
			String target = spell.getThisAttribute("target");
			if (!"character,individual,monster".contains(target)) { // I'm only supporting these for the battle builder
				toRemove.add(spell);
				continue;
			}
		}
		spells.removeAll(toRemove);
		Collections.sort(spells,new Comparator<GameObject>() {
			public int compare(GameObject go1,GameObject go2) {
				return go1.getName().compareTo(go2.getName());
			}
		});
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Permanent Spell to Cast:",true);
		chooser.addGameObjects(spells,false);
		chooser.setMaxGroupSize(spells.size());
		//chooser.setForceColumns(5);
		chooser.setVisible(true);
		if (chooser.getSelectedText()!=null) {
			RealmComponent rc = chooser.getFirstSelectedComponent();
			return new SpellWrapper(rc.getGameObject());
		}
		return null;
	}
	private GameObject queryCaster(String spellType) {
		ArrayList<GameObject> characters = pool.find("character,"+BATTLE_BUILDER_KEY);
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Caster:",true);
		chooser.addOption("Other","Other");
		for (GameObject character:characters) {
			CharacterWrapper test = new CharacterWrapper(character);
			boolean hasChits = false;
			for(CharacterActionChitComponent chit:test.getActiveMagicChits()) {
				if (spellType.equals(chit.getMagicType())) {
					hasChits = true;
					break;
				}
			}
			chooser.addOption(character.getName(),character.getName() + (hasChits?"":("(No MAGIC "+spellType+" Chits)")));
		}
		chooser.setVisible(true);
		String key = chooser.getSelectedOptionKey();
		if (key!=null) {
			if ("Other".equals(key)) {
				GameObject other = pool.findFirst("Name=Inn"); // Yes, the Inn will be the "caster" in this case.  :-)
				other.setThisAttribute("character");
				return other;
			}
			return pool.findFirst("Name="+key);
		}
		return null;
	}
	private GameObject queryIncantation(GameObject caster,String spellType) {
		if ("Inn".equals(caster.getName())) {
			return caster;
		}
		CharacterWrapper test = new CharacterWrapper(caster);
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Incantation:",true);
		for(CharacterActionChitComponent chit:test.getActiveMagicChits()) {
			if (spellType.equals(chit.getMagicType())) {
				chooser.addRealmComponent(chit);
			}
		}
		if (!chooser.hasOptions()) return caster; 
		chooser.setVisible(true);
		if (chooser.getSelectedText()!=null) {
			return chooser.getFirstSelectedComponent().getGameObject();
		}
		return null;
	}
	private RealmComponent queryTarget(String targetType) {
		ArrayList<GameObject> choices = new ArrayList<GameObject>();
		boolean individual = "individual".equals(targetType);
		if (individual || "monster".equals(targetType)) {
			choices.addAll(pool.find("monster,"+BATTLE_BUILDER_KEY));
		}
		if (individual || "character".equals(targetType)) {
			choices.addAll(pool.find("character,"+BATTLE_BUILDER_KEY));
		}
		if (individual) {
			choices.addAll(pool.find("native,"+BATTLE_BUILDER_KEY));
		}
//		if ("artifact".equals(targetType)) {
//			choices.addAll(pool.find("artifact,"+BATTLE_BUILDER_KEY));
//			choices.addAll(pool.find("book,"+BATTLE_BUILDER_KEY));
//		}
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Target of Spell:",true);
		chooser.addGameObjects(choices,true);
		if (chooser.hasOptions()) {
			chooser.setVisible(true);
			String selText = chooser.getSelectedText();
			if (selText!=null) {
				return chooser.getFirstSelectedComponent();
			}
		}
		else {
			JOptionPane.showMessageDialog(this,"No valid targets!");
		}
		
		return null;
	}
	public void deleteTab(String name) {
		for (int i=0;i<tabbedPane.getTabCount();i++) {
			String title = tabbedPane.getTitleAt(i);
			if (title.equals(name)) {
				tabbedPane.removeTabAt(i);
				break;
			}
		}
		repaint();
	}
	private void doFinish() {
		Collection everything = pool.find(BATTLE_BUILDER_KEY);
		for (Iterator i=everything.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.removeThisAttribute(BATTLE_BUILDER_KEY);
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isCharacter() || rc.isNative() || rc.isMonster() || rc.isTraveler()) {
				battleClearing.add(go,null);
			}
		}
		
		// Save the battleClearing for reference
		GameObject bcObj = getBattleClearingReferenceObject(gameData);
		TileLocation tl = new TileLocation(battleClearing);
		bcObj.setThisAttribute("version",Constants.REALM_SPEAK_VERSION);
		bcObj.setThisAttribute("battleClearing",tl.asKey());
	}
	
	public static GameObject getBattleClearingReferenceObject(GameData data) {
		GamePool thePool = new GamePool(data.getGameObjects());
		Collection bc = thePool.find(BATTLE_CLEARING_KEY);
		GameObject bcObj = null;
		if (bc.isEmpty()) {
			bcObj = data.createNewObject();
			bcObj.setThisAttribute(BATTLE_CLEARING_KEY);
		}
		else {
			bcObj = (GameObject)bc.iterator().next();
		}
		return bcObj;
	}
	
	public static void constructBattleSituation() {
		constructBattleSituation(null);
	}
	public static void constructBattleSituation(GameData data) {
		BattleBuilder builder = new BattleBuilder();
		if (builder.initialize(data)) {
			builder.setVisible(true);
		}
	}
	
//	public static void main(String[] args) {
//		RealmUtility.setupTextType();
//		RealmUtility.setupArgs(args);
//		LoggingHandler.initLogging();
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		if (constructBattleSituation()!=null) {
//			System.out.println("Combat!");
//		}
//		else {
//			System.out.println("Cancelled");
//		}
//		System.exit(0);
//	}
}