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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.robin.game.objects.*;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.Spoils;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.quest.requirement.*;
import com.robin.magic_realm.components.quest.reward.QuestReward;
import com.robin.magic_realm.components.swing.CharacterChooser;
import com.robin.magic_realm.components.table.Loot;
import com.robin.magic_realm.components.table.Search;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class QuestTesterFrame extends JFrame {
	Quest questToTest;

	GameData gameData;
	Quest quest;
	CharacterWrapper character;

	JButton resetButton;
	JLabel questName;
	JButton activateButton;
	JTextArea questDescription;
	QuestStepView questStepView;
	JTextArea stepDetails;

	JTextArea debugOutput;

	// Stats
	JLabel charName;
	JLabel currentLocation;
	JLabel currentDay;
	JLabel gtAmount;
	JLabel spellAmount;
	JLabel fameAmount;
	JLabel notorietyAmount;
	JLabel goldAmount;

	// Inventory
	JList activeInventory;
	JList inactiveInventory;

	// Hirelings
	JList hirelings;
	JList journalList;

	// Clearing
	JList clearingComponents;
	JButton pickupFromClearingButton;
	JButton removeFromClearingButton;
	JButton searchClearingButton;
	JButton killDenizenButton;
	JButton discoverButton;

	JToggleButton unspecifiedTime;
	JToggleButton birdsongTime;
	JToggleButton phaseTime;
	JToggleButton turnTime;
	JToggleButton eveningTime;

	boolean inventorySelectionLock = false;

	public final boolean ready;

	public QuestTesterFrame(Quest quest, String charName) {
		questToTest = quest;
		initComponents();
		redirectSystemStreams();
		ready = initQuestTest(charName);
	}

	private void initComponents() {
		setTitle("RealmSpeak Quest Tester");
		setSize(1200, 768);

		setLayout(new BorderLayout());

		JPanel top = new JPanel(new BorderLayout());
		ComponentTools.lockComponentSize(top, 2000, 200);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		questName = new JLabel();
		questName.setFont(QuestGuiConstants.QuestTitleFont);
		questName.setHorizontalAlignment(SwingConstants.CENTER);
		questName.setVerticalAlignment(SwingConstants.CENTER);
		buttonPanel.add(questName, BorderLayout.CENTER);
		resetButton = new JButton("RESET");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				initQuestTest(character.getGameObject().getName());
			}
		});
		buttonPanel.add(resetButton, BorderLayout.NORTH);
		activateButton = new JButton("Activate!");
		activateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				quest.setState(QuestState.Active, character.getCurrentDayKey(), character);
				retestQuest();
				questName.setIcon(RealmComponent.getRealmComponent(quest.getGameObject()).getFaceUpIcon());
				activateButton.setEnabled(quest.getState() == QuestState.Assigned);
			}
		});
		buttonPanel.add(activateButton, BorderLayout.SOUTH);
		top.add(buttonPanel, BorderLayout.WEST);
		questDescription = new JTextArea();
		questDescription.setEditable(false);
		questDescription.setLineWrap(true);
		questDescription.setWrapStyleWord(true);
		questDescription.setFont(QuestGuiConstants.QuestDescriptionFont);
		questDescription.setBackground(null);
		questDescription.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		top.add(new JScrollPane(questDescription), BorderLayout.CENTER);

		add(top, BorderLayout.NORTH);

		JPanel main = new JPanel(new GridLayout(2, 1));
		JPanel mainTop = new JPanel(new GridLayout(1, 3));
		JPanel debugPanel = new JPanel(new BorderLayout());
		debugOutput = new JTextArea();
		debugOutput.setEditable(false);
		debugOutput.setLineWrap(false);
		debugOutput.setWrapStyleWord(false);
		debugOutput.setFont(QuestGuiConstants.QuestDescriptionFont);
		debugPanel.add(new JScrollPane(debugOutput));
		debugPanel.setBorder(BorderFactory.createTitledBorder("Debug Output"));
		mainTop.add(debugPanel);

		questStepView = new QuestStepView();
		questStepView.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				QuestStep selected = questStepView.getSelectedStep();
				updateStepDetails(selected);
			}
		});
		mainTop.add(questStepView);
		stepDetails = new JTextArea();
		stepDetails.setEditable(false);
		stepDetails.setLineWrap(true);
		stepDetails.setWrapStyleWord(true);
		stepDetails.setFont(QuestGuiConstants.QuestDescriptionFont);
		stepDetails.setBackground(null);
		stepDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainTop.add(new JScrollPane(stepDetails));
		main.add(mainTop);
		main.add(buildCharacterPanel());
		add(main, BorderLayout.CENTER);
	}

	private JPanel buildCharacterPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 4));
		panel.add(buildCharacterStatsPanel());
		panel.add(buildCharacterInventoryPanel());
		panel.add(buildCharacterHirelingPanel());
		panel.add(buildCharacterClearingPanel());
		return panel;
	}

	private JPanel buildCharacterStatsPanel() {
		JPanel superPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new GridLayout(2, 1));
		JButton retestQuestButton = new JButton("Check Quest Now");
		retestQuestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				retestQuest();
			}
		});
		topPanel.add(retestQuestButton);
		JPanel phaseOptions = new JPanel(new GridLayout(1, 5));
		ButtonGroup timeGroup = new ButtonGroup();
		unspecifiedTime = new ForceTextToggle("Any");
		unspecifiedTime.setSelected(true);
		unspecifiedTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				retestQuest();
			}
		});
		timeGroup.add(unspecifiedTime);
		phaseOptions.add(unspecifiedTime);
		birdsongTime = new ForceTextToggle("Birdsong");
		birdsongTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				retestQuest();
			}
		});
		timeGroup.add(birdsongTime);
		phaseOptions.add(birdsongTime);
		phaseTime = new ForceTextToggle("Phase");
		phaseTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				retestQuest();
			}
		});
		timeGroup.add(phaseTime);
		phaseOptions.add(phaseTime);
		turnTime = new ForceTextToggle("Turn");
		turnTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				retestQuest();
			}
		});
		timeGroup.add(turnTime);
		phaseOptions.add(turnTime);
		eveningTime = new ForceTextToggle("Evening");
		eveningTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				retestQuest();
			}
		});
		timeGroup.add(eveningTime);
		phaseOptions.add(eveningTime);
		topPanel.add(phaseOptions);
		superPanel.add(topPanel, BorderLayout.NORTH);
		JPanel panel = new JPanel(new BorderLayout());
		Box box = Box.createVerticalBox();
		UniformLabelGroup group = new UniformLabelGroup();
		Box line;

		line = group.createLabelLine("Name");
		charName = new JLabel();
		line.add(charName);
		line.add(Box.createHorizontalGlue());
		box.add(line);

		line = group.createLabelLine("Current Location");
		currentLocation = new JLabel();
		line.add(currentLocation);
		line.add(Box.createHorizontalGlue());
		JButton changeLocation = new JButton("Change");
		changeLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				chooseNewLocation();
			}
		});
		line.add(changeLocation);
		box.add(line);

		line = group.createLabelLine("Current Day");
		currentDay = new JLabel();
		line.add(currentDay);
		line.add(Box.createHorizontalGlue());
		JButton changeDay = new JButton("Increment");
		changeDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				GameWrapper game = GameWrapper.findGame(gameData);
				game.addDay(1);
				character.setCurrentMonth(game.getMonth());
				character.setCurrentDay(game.getDay());
				character.startNewDay(RealmCalendar.getCalendar(gameData), HostPrefWrapper.findHostPrefs(gameData));
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(changeDay);
		box.add(line);

		line = group.createLabelLine("Great Treasures");
		gtAmount = new JLabel();
		line.add(gtAmount);
		line.add(Box.createHorizontalGlue());
		box.add(line);

		line = group.createLabelLine("Recorded Fame");
		fameAmount = new JLabel();
		line.add(fameAmount);
		line.add(Box.createHorizontalGlue());
		JButton subFame = new JButton("-");
		subFame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.addFame(-5);
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(subFame);
		JButton addFame = new JButton("+");
		addFame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.addFame(5);
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(addFame);
		box.add(line);

		line = group.createLabelLine("Recorded Notoriety");
		notorietyAmount = new JLabel();
		line.add(notorietyAmount);
		line.add(Box.createHorizontalGlue());
		JButton subNot = new JButton("-");
		subNot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.addNotoriety(-5);
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(subNot);
		JButton addNot = new JButton("+");
		addNot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.addNotoriety(5);
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(addNot);
		box.add(line);

		line = group.createLabelLine("Recorded Gold");
		goldAmount = new JLabel();
		line.add(goldAmount);
		line.add(Box.createHorizontalGlue());
		JButton subGold = new JButton("-");
		subGold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.addGold(-5);
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(subGold);
		JButton addGold = new JButton("+");
		addGold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				character.addGold(5);
				updateCharacterPanel();
				retestQuest();
			}
		});
		line.add(addGold);
		box.add(line);

		box.add(Box.createVerticalGlue());
		panel.add(box, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createTitledBorder("Character Stats"));
		superPanel.add(panel, BorderLayout.CENTER);
		return superPanel;
	}

	private JPanel buildCharacterInventoryPanel() {
		JPanel superPanel = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(2, 1));

		activeInventory = new JList();
		activeInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		activeInventory.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (inventorySelectionLock)
					return;
				inventorySelectionLock = true;
				inactiveInventory.clearSelection();
				inventorySelectionLock = false;
			}
		});
		activeInventory.setCellRenderer(new QuestListRenderer());
		panel.add(makeTitledScrollPane("Active Inventory", activeInventory));

		inactiveInventory = new JList();
		inactiveInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inactiveInventory.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (inventorySelectionLock)
					return;
				inventorySelectionLock = true;
				activeInventory.clearSelection();
				inventorySelectionLock = false;
			}
		});
		inactiveInventory.setCellRenderer(new QuestListRenderer());
		panel.add(makeTitledScrollPane("Inactive Inventory", inactiveInventory));

		superPanel.add(panel, BorderLayout.CENTER);

		JPanel controls = new JPanel(new GridLayout(2, 4));
		JButton addNew = new JButton("Item");
		addNew.setToolTipText("Gain an item (treasure/weapon/armor)");
		addNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseSomething();
				if (things == null)
					return;
				for (GameObject thing : things) {
					Loot.addItemToCharacter(QuestTesterFrame.this, null, character, thing, HostPrefWrapper.findHostPrefs(gameData));
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addNew);
		JButton addMc = new JButton("MC");
		addMc.setToolTipText("Add a minor character");
		addMc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {

				String mcName = JOptionPane.showInputDialog("Minor Character Name"); // mcName??!?  Robble robble robble.
				if (mcName == null)
					return;

				QuestMinorCharacter minorCharacter = quest.createMinorCharacter();
				minorCharacter.setName(mcName);
				minorCharacter.getGameObject().setThisAttribute(Constants.ACTIVATED);
				//minorCharacter.setupAbilities();
				character.getGameObject().add(minorCharacter.getGameObject());

				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addMc);
		JButton toggleActive = new JButton("Toggle");
		toggleActive.setToolTipText("Activate/Deactivate the selected item above.");
		toggleActive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (activeInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) activeInventory.getSelectedValue();
					if (TreasureUtility.doDeactivate(QuestTesterFrame.this, character, thing)) {
						updateCharacterPanel();
						retestQuest();
						inactiveInventory.setSelectedValue(thing, true);
					}
				}
				else if (inactiveInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) inactiveInventory.getSelectedValue();
					if (TreasureUtility.doActivate(QuestTesterFrame.this, character, thing, null, false)) {
						updateCharacterPanel();
						retestQuest();
						activeInventory.setSelectedValue(thing, true);
					}
				}
			}
		});
		controls.add(toggleActive);
		JButton remove = new JButton("Remove");
		remove.setToolTipText("Remove the selected item.  Note that it is NOT dropped in the clearing.");
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (activeInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) activeInventory.getSelectedValue();
					if (TreasureUtility.doDeactivate(QuestTesterFrame.this, character, thing)) {
						thing.detach();
						updateCharacterPanel();
						retestQuest();
					}
				}
				else if (inactiveInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) inactiveInventory.getSelectedValue();
					thing.detach();
					updateCharacterPanel();
					retestQuest();
				}
			}
		});
		controls.add(remove);
		controls.add(Box.createGlue());
		JButton buy = new JButton("Buy");
		buy.setEnabled(false); // TODO This doesn't work yet
		buy.setToolTipText("Buy an Item from a Native.");
		buy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
			}
		});
		controls.add(buy);
		JButton sell = new JButton("Sell");
		sell.setToolTipText("Sell the selected item to a Native.");
		sell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (activeInventory.getSelectedIndex() == -1 && inactiveInventory.getSelectedIndex() == -1)
					return;
				ArrayList<GameObject> list = chooseOther("Buyer", "visitor", "native,rank=HQ");
				if (list == null)
					return;
				if (list.size() != 1) {
					JOptionPane.showMessageDialog(QuestTesterFrame.this, "Pick 1");
					return;
				}

				QuestRequirementParams params = new QuestRequirementParams();
				params.actionType = CharacterActionType.Trading;
				params.actionName = TradeType.Sell.toString();
				params.objectList = new ArrayList<GameObject>();
				params.targetOfSearch = list.get(0);

				if (activeInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) activeInventory.getSelectedValue();
					if (TreasureUtility.doDeactivate(QuestTesterFrame.this, character, thing)) {
						thing.detach();
						params.objectList.add(thing);
						character.testQuestRequirements(QuestTesterFrame.this, params);
						updateCharacterPanel();
						retestQuest();
					}
				}
				else if (inactiveInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) inactiveInventory.getSelectedValue();
					thing.detach();
					params.objectList.add(thing);
					character.testQuestRequirements(QuestTesterFrame.this, params);
					updateCharacterPanel();
					retestQuest();
				}
			}
		});
		controls.add(sell);
		JButton drop = new JButton("Drop");
		drop.setToolTipText("Drop the selected item in the clearing.");
		drop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				TileLocation tl = character.getCurrentLocation();
				if (activeInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) activeInventory.getSelectedValue();
					if (TreasureUtility.doDeactivate(QuestTesterFrame.this, character, thing)) {
						tl.clearing.add(thing, character);
						updateCharacterPanel();
						retestQuest();
					}
				}
				else if (inactiveInventory.getSelectedIndex() != -1) {
					GameObject thing = (GameObject) inactiveInventory.getSelectedValue();
					tl.clearing.add(thing, character);
					updateCharacterPanel();
					retestQuest();
				}
			}
		});
		controls.add(drop);
		superPanel.add(controls, BorderLayout.SOUTH);

		return superPanel;
	}

	private JPanel buildCharacterHirelingPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 1));
		hirelings = new JList();
		hirelings.setCellRenderer(new HirelingListRenderer());
		panel.add(makeTitledScrollPane("Hirelings", hirelings));

		journalList = new JList();
		journalList.setCellRenderer(new JournalEntryListRenderer());
		panel.add(makeTitledScrollPane("Journal", journalList));
		return panel;
	}

	private JPanel buildCharacterClearingPanel() {
		JPanel superPanel = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new BorderLayout());

		searchClearingButton = new JButton("Search");
		searchClearingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmComponent rc = (RealmComponent) clearingComponents.getSelectedValue();
				if (rc == null) {
					rc = character.getCurrentTile();
				}
				doSearchOn(rc);
			}
		});
		panel.add(searchClearingButton, BorderLayout.NORTH);

		clearingComponents = new JList();
		clearingComponents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clearingComponents.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateClearingButtons();
			}
		});
		clearingComponents.setCellRenderer(new QuestListRenderer());
		panel.add(new JScrollPane(clearingComponents), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createTitledBorder("Current Clearing"));
		superPanel.add(panel, BorderLayout.CENTER);

		JPanel controls = new JPanel(new GridLayout(3, 4));
		JButton addChit = new JButton("Chit");
		addChit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Chit", "chit,!treasure_location");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addChit);
		JButton addLocation = new JButton("Location");
		addLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Location", "chit,treasure_location");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addLocation);
		JButton addItem = new JButton("Item");
		addItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseSomething();
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addItem);
		JButton addDwelling = new JButton("Dwelling");
		addDwelling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Dwelling", "dwelling,!native");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addDwelling);
		JButton addMonster = new JButton("Monster");
		addMonster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Monster", "monster,!part");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					thing.removeThisAttribute(Constants.DEAD);
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addMonster);
		JButton addNative = new JButton("Native");
		addNative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Native", "native,!treasure,!dwelling,!horse,!boon");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					thing.removeThisAttribute(Constants.DEAD);
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addNative);
		JButton addVisitor = new JButton("Visitor");
		addVisitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Visitor", "visitor");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addVisitor);
		JButton addMission = new JButton("Mission");
		addMission.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<GameObject> things = chooseOther("Mission", "gold_special,!visitor");
				if (things == null)
					return;
				for (GameObject thing : things) {
					thing.setThisAttribute("seen");
					character.getCurrentLocation().clearing.add(thing, null);
				}
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(addMission);

		pickupFromClearingButton = new JButton("Pick Up");
		pickupFromClearingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmComponent rc = (RealmComponent) clearingComponents.getSelectedValue();
				if (rc == null)
					return;
				if (rc.isItem()) {
					Loot.addItemToCharacter(QuestTesterFrame.this, null, character, rc.getGameObject());
					updateCharacterPanel();
					retestQuest();
				}
				else if (rc.isGoldSpecial() && !rc.isVisitor()) {
					Loot.addItemToCharacter(QuestTesterFrame.this, null, character, rc.getGameObject());
					QuestRequirementParams qp = new QuestRequirementParams();
					qp.actionName = rc.getGameObject().getName();
					qp.actionType = CharacterActionType.PickUpMissionCampaign;
					qp.targetOfSearch = rc.getGameObject();
					updateCharacterPanel();
					retestQuest(qp);
				}
			}
		});
		controls.add(pickupFromClearingButton);
		removeFromClearingButton = new JButton("Remove");
		removeFromClearingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmComponent rc = (RealmComponent) clearingComponents.getSelectedValue();
				if (rc == null)
					return;
				rc.getGameObject().detach();
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(removeFromClearingButton);
		discoverButton = new JButton("Discover");
		discoverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmComponent rc = (RealmComponent) clearingComponents.getSelectedValue();
				Search.discoverChit(QuestTesterFrame.this, character, character.getCurrentLocation().clearing, rc, new QuestRequirementParams(), null);
				updateCharacterPanel();
				retestQuest();
			}
		});
		controls.add(discoverButton);
		killDenizenButton = new JButton("Kill");
		killDenizenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmComponent victim = (RealmComponent) clearingComponents.getSelectedValue();
				if (victim == null)
					return;
				int index = clearingComponents.getSelectedIndex();
				String dayKey = character.getCurrentDayKey();
				ArrayList<GameObject> kills = character.getKills(dayKey);
				int killCount = kills == null ? 0 : kills.size();
				victim.getGameObject().setThisAttribute(Constants.DEAD);
				Spoils spoils = new Spoils();
				spoils.addFame(victim.getGameObject().getThisInt("fame"));
				spoils.addNotoriety(victim.getGameObject().getThisInt("notoriety"));
				spoils.setUseMultiplier(true);
				spoils.setMultiplier(killCount + 1);
				character.addKill(victim.getGameObject(), spoils);
				character.addFame(spoils.getFame());
				character.addNotoriety(spoils.getNotoriety());
				victim.getGameObject().detach();
				updateCharacterPanel();
				int listLength = clearingComponents.getModel().getSize();
				if (listLength > 0) {
					if (index >= listLength)
						index = listLength - 1;
					clearingComponents.setSelectedIndex(index);
				}
				retestQuest();
			}
		});
		controls.add(killDenizenButton);
		superPanel.add(controls, BorderLayout.SOUTH);

		return superPanel;
	}

	private ArrayList<GameObject> chooseSomething() {
		GamePool pool = new GamePool(gameData.getGameObjects());
		Hashtable<String, GameObject> hash = new Hashtable<String, GameObject>();
		ArrayList<String> weaponList = new ArrayList<String>();
		ArrayList<String> armorList = new ArrayList<String>();
		ArrayList<String> steedList = new ArrayList<String>();
		ArrayList<String> treasureList = new ArrayList<String>();

		ArrayList<GameObject> all = pool.find("item");
		all.addAll(pool.find("treasure_within_treasure"));
		for (GameObject item : all) {
			String itemKey = getKey(item);
			GameObject held = item.getHeldBy();
			if (held != null && (held == character.getGameObject() || (held.hasThisAttribute("tile") && held.hasThisAttribute("clearing"))))
				continue;

			if (item.hasAllKeyVals("horse,!native"))
				steedList.add(itemKey);
			else if (item.hasAllKeyVals("weapon,!character"))
				weaponList.add(itemKey);
			else if (item.hasAllKeyVals("armor,!treasure,!character"))
				armorList.add(itemKey);
			else
				treasureList.add(itemKey);

			hash.put(itemKey, item);
		}
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(weaponList);
		list.addAll(armorList);
		list.addAll(steedList);
		list.addAll(treasureList);
		Collections.sort(list);
		ListChooser chooser = new ListChooser(this, "Select item:", list);
		chooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		chooser.setDoubleClickEnabled(true);
		chooser.setLocationRelativeTo(this);
		chooser.setVisible(true);
		Vector v = chooser.getSelectedItems();
		if (v != null && !v.isEmpty()) {
			ArrayList<GameObject> ret = new ArrayList<GameObject>();
			for (int i = 0; i < v.size(); i++) {
				ret.add(hash.get(v.get(i)));
			}
			return ret;
		}
		return null;
	}

	private ArrayList<GameObject> chooseOther(String name, String... keyVals) {
		GamePool pool = new GamePool(gameData.getGameObjects());
		Hashtable<String, GameObject> hash = new Hashtable<String, GameObject>();
		ArrayList<String> chitList = new ArrayList<String>();
		for (String kv : keyVals) {
			for (GameObject thing : pool.find(kv)) {
				String itemKey = getKey(thing);
				if (hash.containsKey(itemKey))
					continue;
				chitList.add(itemKey);
				hash.put(itemKey, thing);
			}
		}
		Collections.sort(chitList);
		ListChooser chooser = new ListChooser(this, "Select " + name + ":", chitList);
		chooser.setDoubleClickEnabled(true);
		chooser.setLocationRelativeTo(this);
		chooser.setVisible(true);
		Vector v = chooser.getSelectedItems();
		if (v != null && !v.isEmpty()) {
			ArrayList<GameObject> ret = new ArrayList<GameObject>();
			for (int i = 0; i < v.size(); i++) {
				ret.add(hash.get(v.get(i)));
			}
			return ret;
		}
		return null;
	}

	private void updateStepDetails(QuestStep step) {
		StringBuffer sb = new StringBuffer();
		sb.append(step.getName());
		sb.append("\n\n");
		sb.append(step.getDescription() == null ? "" : step.getDescription());
		sb.append("\n");
		if (step.getRequirements().size() > 0) {
			sb.append("\nREQUIREMENTS(");
			sb.append(step.getReqType());
			sb.append("):\n");
		}
		for (QuestRequirement req : step.getRequirements()) {
			sb.append("[");
			sb.append(req.getRequirementType());
			sb.append("]: ");
			sb.append(req.toString());
			sb.append("\n");
		}
		if (step.getRewards().size() > 0) {
			sb.append("\nREWARDS:\n");
		}
		for (QuestReward reward : step.getRewards()) {
			sb.append("[");
			sb.append(reward.getRewardType());
			sb.append("]: ");
			sb.append(reward.toString());
			sb.append("\n");
		}
		stepDetails.setText(sb.toString());
		stepDetails.setCaretPosition(0);
	}

	private boolean initQuestTest(String characterName) {
		RealmUtility.resetGame();
		RealmLoader loader = new RealmLoader();
		gameData = loader.getData();
		GameWrapper game = GameWrapper.findGame(gameData);
		game.setMonth(1);
		game.setDay(1);
		HostPrefWrapper hostPrefs = HostPrefWrapper.createDefaultHostPrefs(gameData);
		quest = questToTest.copyQuestToGameData(gameData);

		// TODO Choose gameplay options based on what quest uses (or default to single board and original if none)

		//hostPrefs.setBoardAutoSetup(false);
		//RealmSpeakInit init = new RealmSpeakInit(null);
		//		singleBoard.setSelected(quest.getBoolean(QuestConstants.SINGLE_BOARD));
		//		doubleBoard.setSelected(quest.getBoolean(QuestConstants.DOUBLE_BOARD));
		//		tripleBoard.setSelected(quest.getBoolean(QuestConstants.TRIPLE_BOARD));

		ArrayList<GameVariant> variantChoices = new ArrayList<GameVariant>();
		if (quest.getBoolean(QuestConstants.VARIANT_ORIGINAL))
			variantChoices.add(GameVariant.ORIGINAL_GAME_VARIANT);
		if (quest.getBoolean(QuestConstants.VARIANT_PRUITTS))
			variantChoices.add(GameVariant.PRUITTS_GAME_VARIANT);
		if (quest.getBoolean(QuestConstants.VARIANT_EXP1))
			variantChoices.add(GameVariant.EXP1_GAME_VARIANT);
		if (variantChoices.isEmpty())
			variantChoices.add(GameVariant.ORIGINAL_GAME_VARIANT);

		GameVariant useVariant;
		if (variantChoices.size() == 1) {
			useVariant = variantChoices.get(0);
		}
		else {
			ButtonOptionDialog variantChooser = new ButtonOptionDialog(this, null, "Which variant are you testing?", "Choose game variant", false);
			variantChooser.addSelectionObjects(variantChoices);
			variantChooser.setVisible(true);
			useVariant = (GameVariant) variantChooser.getSelectedObject();
		}

		hostPrefs.setGameKeyVals(useVariant.getKeyVals());
		loader.cleanupData(hostPrefs.getGameKeyVals());

		// Choose a character
		GameObject chosen;
		if (characterName == null) {
			GamePool pool = new GamePool(gameData.getGameObjects());
			ArrayList<GameObject> characters = pool.find("character,!" + CharacterWrapper.NAME_KEY);
			characters.addAll(CustomCharacterLibrary.getSingleton().getCharacterTemplateList());
			Collections.sort(characters, new Comparator<GameObject>() {
				public int compare(GameObject go1, GameObject go2) {
					return go1.getName().compareTo(go2.getName());
				}
			});

			CharacterChooser chooser = new CharacterChooser(this, characters, hostPrefs);
			chooser.setVisible(true);
			chosen = chooser.getChosenCharacter();
			if (chosen == null) {
				setVisible(false);
				dispose();
				return false;
			}
		}
		else {
			chosen = gameData.getGameObjectByName(characterName);
		}

		character = new CharacterWrapper(chosen);
		int level = 4;
		character.setStartingLevel(level);
		character.setCharacterLevel(level); // only supports single
											// digit level numbers
											// (for now)
		// Set starting stage based on level and bonus chits
		character.setStartingStage((level * 3));
		character.setCharacterStage((level * 3));
		character.setCharacterExtraChitMarkers((level * 3));
		character.initChits();
		character.fetchStartingInventory(this, gameData, false);
		character.setGold(character.getStartingGold());
		character.setCurrentMonth(game.getMonth());
		character.setCurrentDay(game.getDay());
		TileComponent tile = (TileComponent) RealmComponent.getRealmComponent(gameData.getGameObjectByName("Awful Valley"));
		ClearingDetail clearing = tile.getClearing(1);
		clearing.add(character.getGameObject(), character);
		character.startNewDay(RealmCalendar.getCalendar(gameData), hostPrefs);

		quest.reset();
		character.addQuest(this, quest);
		quest.setState(QuestState.Assigned, character.getCurrentDayKey(), character);
		quest.testRequirements(this, character, new QuestRequirementParams());

		// If quest lacks any "Activate" requirements, then activate by default
		if (!quest.isActivateable()) {
			quest.setState(QuestState.Active, character.getCurrentDayKey(), character);
			retestQuest();
		}

		questName.setIcon(RealmComponent.getRealmComponent(quest.getGameObject()).getFaceUpIcon());
		questDescription.setText(quest.getDescription());
		questDescription.setCaretPosition(0);
		questStepView.updateSteps(quest.getSteps());
		activateButton.setEnabled(quest.getState() == QuestState.Assigned);
		updateCharacterPanel();
		return true;
	}

	private void retestQuest() {
		retestQuest(new QuestRequirementParams());
	}

	private void retestQuest(QuestRequirementParams params) {
		params.timeOfCall = getTimeOfCallFromOptions();
		debugOutput.setText("");
		if (quest.testRequirements(this, character, params)) {
			character.testQuestRequirements(this); // Make sure that all quests get updated (auto-journal)
			questStepView.repaint();
			questName.setIcon(RealmComponent.getRealmComponent(quest.getGameObject()).getFaceUpIcon());

			if (quest.getState() == QuestState.Complete) {
				JOptionPane.showMessageDialog(this, "Quest is Complete!", "Quest Complete", JOptionPane.INFORMATION_MESSAGE, RealmComponent.getRealmComponent(quest.getGameObject()).getFaceUpIcon());
			}
		}
		questStepView.repaint();
		updateCharacterPanel();
		debugOutput.setCaretPosition(0); // why doesn't this work?
	}

	private GamePhaseType getTimeOfCallFromOptions() {
		if (birdsongTime.isSelected())
			return GamePhaseType.Birdsong;
		if (phaseTime.isSelected())
			return GamePhaseType.EndOfPhase;
		if (turnTime.isSelected())
			return GamePhaseType.EndOfTurn;
		if (eveningTime.isSelected())
			return GamePhaseType.StartOfEvening;
		return GamePhaseType.Unspecified;
	}

	private void updateCharacterPanel() {
		charName.setText(character.getCharacterName());
		charName.setIcon(RealmComponent.getRealmComponent(character.getGameObject()).getSmallIcon());
		currentLocation.setText(character.getCurrentLocation().toString());
		currentDay.setText(character.getCurrentDayKey());
		gtAmount.setText(String.valueOf(character.getGreatTreasureScore().getOwnedPoints()));
		fameAmount.setText(String.valueOf((int) character.getFame()));
		notorietyAmount.setText(String.valueOf((int) character.getNotoriety()));
		goldAmount.setText(String.valueOf((int) character.getGold()));

		activeInventory.setListData(new Vector<GameObject>(character.getActiveInventory()));
		inactiveInventory.setListData(new Vector<GameObject>(character.getInactiveInventory()));
		hirelings.setListData(new Vector<RealmComponent>(character.getAllHirelings()));
		journalList.setListData(new Vector<QuestJournalEntry>(quest.getJournalEntries()));

		Vector<RealmComponent> rcs = new Vector<RealmComponent>();
		for (RealmComponent rc : character.getCurrentLocation().clearing.getClearingComponents(true)) {
			if (rc.isCharacter())
				continue;
			rcs.add(rc);
		}
		clearingComponents.setListData(rcs);
		updateClearingButtons();
	}

	private void updateClearingButtons() {
		RealmComponent rc = (RealmComponent) clearingComponents.getSelectedValue();
		pickupFromClearingButton.setEnabled(rc != null);
		removeFromClearingButton.setEnabled(rc != null);
		discoverButton.setEnabled(rc != null);
		killDenizenButton.setEnabled(rc != null && (rc.isMonster() || rc.isNative()));
		searchClearingButton.setEnabled(true); // always on?
	}

	private void chooseNewLocation() {
		GamePool pool = new GamePool(gameData.getGameObjects());
		Hashtable<String, ClearingDetail> hash = new Hashtable<String, ClearingDetail>();
		Vector<String> locationNames = new Vector<String>();
		for (GameObject go : pool.find("tile")) {
			TileComponent tile = (TileComponent) RealmComponent.getRealmComponent(go);
			for (ClearingDetail clearing : tile.getClearings()) {
				String key = clearing.getTileLocation().toString();
				locationNames.add(key);
				hash.put(key, clearing);
			}
		}
		Collections.sort(locationNames);

		ButtonOptionDialog dialog = new ButtonOptionDialog(this, null, "Select new Location:", "Change Location", true, 6);
		dialog.addSelectionObjects(locationNames);
		dialog.setVisible(true);

		String val = (String) dialog.getSelectedObject();
		if (val != null) {
			ClearingDetail clearing = hash.get(val);
			character.moveToLocation(this, clearing.getTileLocation());
			updateCharacterPanel();
			retestQuest();
		}
	}

	private void exitApp() {
		// Should check all GameData windows, and verify that changes have been
		// saved
		setVisible(false);
		dispose();
		System.exit(0);
	}

	static String SEARCH_RESULT_NOTHING = "Nothing";
	static String SEARCH_RESULT_SOMETHING = "Something";
	static String SEARCH_RESULT_TREASURE = "Specific Treasure";
	static String SEARCH_RESULT_SPELL = "Specific Spell";

	private void doSearchOn(RealmComponent rc) {
		ButtonOptionDialog dialog = new ButtonOptionDialog(this, rc.getFaceUpIcon(), "Choose a search table:", "Search Table");
		dialog.addSelectionObjectArray(SearchTableType.values());
		dialog.setVisible(true);
		SearchTableType table = (SearchTableType) dialog.getSelectedObject();
		if (table == null)
			return;
		dialog = new ButtonOptionDialog(this, rc.getFaceUpIcon(), "Choose a search result:", "Search Result");
		dialog.addSelectionObjectArray(SearchResultType.getSearchResultTypes(table));
		dialog.setVisible(true);
		SearchResultType result = (SearchResultType) dialog.getSelectedObject();
		if (result == null)
			return;

		dialog = new ButtonOptionDialog(this, rc.getFaceUpIcon(), "What kind of gain?", "Search Gain", false);
		dialog.addSelectionObject(SEARCH_RESULT_NOTHING);
		dialog.addSelectionObject(SEARCH_RESULT_SOMETHING);
		if (result.canGetTreasure()) {
			dialog.addSelectionObject(SEARCH_RESULT_TREASURE);
		}
		if (result.canGetSpell()) {
			dialog.addSelectionObject(SEARCH_RESULT_SPELL);
		}
		dialog.setVisible(true);
		String gain = (String) dialog.getSelectedObject();

		QuestRequirementParams params = new QuestRequirementParams();
		params.searchType = result;
		params.targetOfSearch = rc.getGameObject();
		params.actionName = table.toString();
		if (SEARCH_RESULT_SOMETHING.equals(gain)) {
			params.searchHadAnEffect = true;
		}
		else if (SEARCH_RESULT_TREASURE.equals(gain)) {
			params.searchHadAnEffect = true;
			ArrayList<GameObject> stuff = chooseSomething();
			if (stuff == null || stuff.size() == 0)
				return;
			params.objectList = stuff;
			for (GameObject thing : stuff) {
				Loot loot = new Loot(this, character, rc.getGameObject(), null);
				loot.handleSpecial(character, character.getCurrentLocation().clearing, thing, true);
				//Loot.addItemToCharacter(this,null,character,thing);
			}
		}
		else if (SEARCH_RESULT_SPELL.equals(gain)) {
			params.searchHadAnEffect = true;
			ArrayList<GameObject> stuff = chooseOther("Spells", "spell,learnable");
			if (stuff == null || stuff.size() == 0)
				return;
			params.objectList = stuff;
			for (GameObject spell : stuff) {
				character.recordNewSpell(this, spell, true); // force learn?
			}
		}
		retestQuest(params);
	}

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				debugOutput.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		//System.setErr(new PrintStream(out, true));
	}

	private static JScrollPane makeTitledScrollPane(String title, JComponent c) {
		JScrollPane sp = new JScrollPane(c);
		sp.setBorder(BorderFactory.createTitledBorder(title));
		return sp;
	}

	private static String getKey(GameObject go) {
		return go.getName() + " [" + go.getId() + "]";
	}

	private class QuestListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			GameObject go = null;
			if (value instanceof RealmComponent) {
				go = ((RealmComponent) value).getGameObject();
			}
			else if (value instanceof GameObject) {
				go = (GameObject) value;
			}
			if (go != null) {
				StringBuffer sb = new StringBuffer();
				sb.append(getKey(go));
				if (character.hasTreasureLocationDiscovery(go.getName())) {
					sb.append(" - Discovered");
				}
				setText(sb.toString());
			}
			return this;
		}
	}

	private class HirelingListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			GameObject go = null;
			if (value instanceof RealmComponent) {
				go = ((RealmComponent) value).getGameObject();
			}
			else if (value instanceof GameObject) {
				go = (GameObject) value;
			}
			if (go != null) {
				StringBuffer sb = new StringBuffer();
				sb.append(getKey(go));
				int days = go.getInt(RealmComponent.REALMCOMPONENT_BLOCK, RealmComponent.OWNER_TERM_OF_HIRE);
				if (days < 1000) {
					sb.append(" for ");
					sb.append(days);
					sb.append(" day");
					sb.append(days == 1 ? "." : "s");
				}
				else {
					sb.append(" (permanent)");
				}
				setText(sb.toString());
			}
			return this;
		}
	}

	private class JournalEntryListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			QuestJournalEntry entry = (QuestJournalEntry) value;
			setText(entry.getText());
			return this;
		}
	}

	private class ForceTextToggle extends JToggleButton {

		Color disabledTextColor = UIManager.getColor("Button.disabledText");

		private String text;

		public ForceTextToggle(String text) {
			super(""); // give NO text to the button itself!
			this.text = text;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Dimension size = getSize();
			g.setColor(isEnabled() ? getForeground() : disabledTextColor); // this isn't tested!!  7/5/2005
			GraphicsUtil.drawCenteredString(g, 0, 0, size.width, size.height, text);
		}
	}

	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();

		GameData data = new GameData();
		data.ignoreRandomSeed = true;
		File file = (args.length > 0 && args[0].trim().length() > 0) ? new File(args[0]) : null;
		if (file != null && data.zipFromFile(file)) {
			Quest aQuest = new Quest((GameObject) data.getGameObjects().iterator().next());
			aQuest.autoRepair(); // Just in case

			final QuestTesterFrame frame = new QuestTesterFrame(aQuest, "Berserker");
			if (!frame.ready)
				return;
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent ev) {
					frame.exitApp();
				}
			});
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
		else {
			if (args.length == 0) {
				System.out.println("No quest file provided!");
			}
			else {
				System.out.println("Unable to open quest: " + args[0]);
			}
		}
	}
}