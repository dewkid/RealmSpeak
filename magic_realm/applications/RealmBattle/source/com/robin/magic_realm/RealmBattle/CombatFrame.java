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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.server.GameClient;
import com.robin.general.io.*;
import com.robin.general.swing.*;
import com.robin.general.util.*;
import com.robin.magic_realm.RealmBattle.MoveActivator.MoveActionResult;
import com.robin.magic_realm.RealmBattle.targeting.SpellTargeting;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.table.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class CombatFrame extends JFrame {
	
	private static final String DATA_NAME = "CombatFrame";
	
	public static final int PARTICIPANT_ROW_HEIGHT = 60;

	private static Logger logger = Logger.getLogger(CombatFrame.class.getName());
	
	private static final Font COMBAT_ROUND_FONT = new Font("Dialog",Font.BOLD|Font.ITALIC,20);
	private static final Font INSTRUCTION_FONT = new Font("Dialog",Font.BOLD,16);
	
	private static final Border LOCK_OFF_BORDER = BorderFactory.createLineBorder(UIManager.getColor("Panel.background"),3);
	private static final Border LOCK_ON_BORDER = BorderFactory.createLineBorder(Color.red,3);
	
	private static Rectangle lastKnownLocation = null;
	private static boolean interactiveFrame;
	private static boolean isGameHost;
	private static boolean closeableFrame = false; // If this is true, there is a button for ending the simulation
	
	private RealmObjectPanel denizenPanel;
	private JScrollPane denizenScroll;
	
	private ArrayList<RealmComponent> allParticipants;
	private boolean[] participantHasHotspots;
	private JTable participantTable;
	private BattleParticipantTableModel participantTableModel;
	private JScrollPane participantTableSp;
	private JPanel combatSheetPanel;
	private JLabel roundLabel;
	private JLabel instructionLabel;
	
	private JPanel sidePanel;
	private Box controlPanel;
	
	private BattleModel currentBattleModel;
	
	private GameData gameData;
	private HostPrefWrapper hostPrefs;
	private GameWrapper theGame;
	private ActionListener finishedActionListener;
	private int actionState;
	private boolean activeCharacterIsHere = true;
	private boolean activeCharacterIsTransmorphed = false;
	private CharacterWrapper activeCharacter = null;
	private RealmComponent activeParticipant = null;
	private CombatSheet activeCombatSheet = null;
	
	private JPanel gameControls;
	private JPanel showPanel;
	private JButton showChitsButton;
	private JButton showInventoryButton;
	private JButton exportButton;
	private JButton combatSummaryButton;
	private JToggleButton lockNextButton;
	private JButton undoButton;
	private JButton textButton;
	private FlashingButton endButton; // if combat is not necessary (ie., all characters are hidden)
	private FlashingButton nextButton;
	
	private JSplitPane sideBarCenterPanel;
	private RollerResults rollerResults;
	private JScrollPane rollerResultsScroll;
	
	private boolean changes;
	private boolean nonaffectingChanges; // this is so the reset button can be lit without disabling all the buttons
	
	private FileManager exportFileManager;
	
	private JLabel[] stateLight;
	private static final String[] STATELIGHT_NAME = {
		"prebattle",
		"luring",
		"random",
		"deploy",
		"actions",
		"assign",
		"position",
		"tactics",
		"results",
		"fatigue",
		"disengage",
	};
	private static final int[] STATELIGHT_VAL = {
		Constants.COMBAT_PREBATTLE,
		Constants.COMBAT_LURE,
		Constants.COMBAT_RANDOM_ASSIGN,
		Constants.COMBAT_DEPLOY,
		Constants.COMBAT_ACTIONS,
		Constants.COMBAT_ASSIGN,
		Constants.COMBAT_POSITIONING,
		Constants.COMBAT_TACTICS,
		Constants.COMBAT_RESOLVING,
		Constants.COMBAT_FATIGUE,
		Constants.COMBAT_DISENGAGE,
	};
	// other controls
	
	private JButton endSimulationButton;
	private JButton saveSimulationButton;
	
	private JButton refreshDisplayButton;
	
	private JCheckBox treacheryOption;
	
	// charge/deploy
	private JButton chargeButton;
	
	// actions
	private JButton runAwayButton;
	private JButton alertWeaponButton;
	private JButton castSpellButton;
	private JButton activateInactivateButton;
	private JButton pickupItemButton;
	private JButton dropBelongingsButton;
	private JButton abandonBelongingsButton;
	private JButton suggestButton;
	
	// assign targets
	private JButton selectTargetFromUnassignedButton;
	private JButton selectSpellTargetsButton;
	private JButton cancelSpellButton;
	
	// Change tactics for transmorphed players
	private JButton changeTacticsButton;
	private JButton useColorChitButton;
	
	private CombatSuggestionAi suggestionAi;
	
	// These are needed to differentiate all the players during results
	private String playerName;
	
	private EndCombatFrame endCombatFrame = null;
	
	protected static DieRoller runAwayRoll = null;
	protected static int runAwayFatigue;
	public void setRunAwayRoll(DieRoller roller) {
		runAwayRoll = roller;
	}
	public void setRunAwayFatigue(int val) {
		runAwayFatigue = val;
	}
	public void clearRunaway() {
		runAwayRoll = null;
		runAwayFatigue = 0;
	}
	public void madeChanges() {
		changes = true;
	}
	
	protected static DieRoller ambushRoll = null;
	protected static RealmComponent ambusher = null;
	public static boolean successfulAmbush() {
		return ambushRoll!=null && ambushRoll.getHighDieResult()<6;
	}
	/**
	 * This is NOT the opposite of successfulAmbush, because ambushRoll might be null!
	 */
	public static boolean unsuccessfulAmbush() {
		return ambushRoll!=null && ambushRoll.getHighDieResult()>=6;
	}
	
	private static CombatFrame singleton = null;
	public static CombatFrame getSingleton(JFrame frame,String playerName,ActionListener listener) {
		if (singleton==null) {
			singleton = new CombatFrame(frame,playerName,listener);
		}
		singleton.playerName = playerName; // this probably isn't necessary, but to be consistent, this should happen
		return singleton;
	}
	public static void resetSingleton() {
		if (singleton!=null) {
			singleton.setVisible(false);
			singleton.dispose();
			singleton = null;
		}
	}
	public static boolean isSingletonShowing() {
		return singleton!=null && singleton.isVisible();
	}
	public static CombatFrame getSingleton() {
		return singleton;
	}
	public static CombatFrame getShowingSingleton() {
		if (isSingletonShowing()) {
			return singleton;
		}
		return null;
	}
	public static void showCombatFrameIfNeeded() {
		if (isSingletonShowing()) {
			singleton.toFront();
		}
	}
	public static void closeCombatFrameIfNeeded() {
		if (isSingletonShowing()) {
			singleton.closeFrame();
		}
	}
	
	private MouseInputListener mouseListener = new MouseInputAdapter() {
		public void mousePressed(MouseEvent ev) {
			if (activeCombatSheet!=null) {
				activeCombatSheet.handleClick(ev.getPoint());
				activeCombatSheet.updateMouseHover(ev.getPoint(),ev.isShiftDown());
			}
		}
		public void mouseMoved(MouseEvent ev) {
			if (activeCombatSheet!=null) {
				activeCombatSheet.updateMouseHover(ev.getPoint(),ev.isShiftDown());
			}
		}
		public void mouseExited(MouseEvent ev) {
			if (activeCombatSheet!=null) {
				activeCombatSheet.updateMouseHover(null);
			}
		}
	};
	private ChangeListener combatListener = new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
			// does nothing
		}
	};
	
	/**
	 * Sole constructor
	 * 
	 * @see #getSingleton(String, ActionListener)
	 */
	private CombatFrame(JFrame frame,String playerName,ActionListener listener) {
		super("RealmSpeak Combat Frame for "+playerName);
		
		// this will guarantee no lockup... but then dialogs are lying around??
		//setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		suggestionAi = new CombatSuggestionAi(this);
		
		this.playerName = playerName;
		this.finishedActionListener = listener;
		allParticipants = new ArrayList<RealmComponent>();
		if (lastKnownLocation==null) {
			lastKnownLocation = ComponentTools.findPreferredRectangle(970,Integer.MAX_VALUE);
		}
		initComponents();
		setSize(lastKnownLocation.width,lastKnownLocation.height);
		setLocation(lastKnownLocation.x,lastKnownLocation.y);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		exportFileManager = new FileManager(this,"Export Destination",null);
	}
	public BattleModel getBattleModel() {
		return currentBattleModel;
	}
	public void madeChange() {
		changes = true;
		updateControls();
	}
	public boolean allowsTreachery() {
		return treacheryOption!=null && treacheryOption.isSelected();
	}
	/**
	 * Cleans up all the panes
	 */
	private void reset() {
		denizenPanel.removeAll();
		allParticipants.clear();
		combatSheetPanel.removeAll();

		treacheryOption = null;
		chargeButton = null;
		runAwayButton = null;
		castSpellButton = null;
		selectSpellTargetsButton = null;
		cancelSpellButton = null;
		alertWeaponButton = null;
		activateInactivateButton = null;
		pickupItemButton = null;
		dropBelongingsButton = null;
		abandonBelongingsButton = null;
		suggestButton = null;
		selectTargetFromUnassignedButton = null;
		changeTacticsButton = null;
		useColorChitButton = null;
		
		if (participantTable!=null) {
			participantTable.clearSelection();
		}
	}
	public void removeDenizen(RealmComponent rc) {
		denizenPanel.removeGameObject(rc.getGameObject());
		denizenPanel.clearSelected();
		denizenPanel.repaint();
	}
	public Collection getUnassignedDenizens() {
		return Arrays.asList(denizenPanel.getComponents());
	}
	public void refreshParticipants() {
		allParticipants.clear();
		// Build BattleParticipant list by examining flag for sheetOwner
		ArrayList<RealmComponent> chars = new ArrayList<RealmComponent>();
		ArrayList<RealmComponent> everyoneElse = new ArrayList<RealmComponent>();
		for (Iterator i=currentBattleModel.getAllBattleParticipants(true).iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			if (rc.isCharacter()) {
				chars.add(rc);
			}
			else if (combat.isSheetOwner()) {
				everyoneElse.add(rc);
			}
		}
		// Sort chars by combat order, and add them first
		Collections.sort(chars,new Comparator() {
			public int compare(Object o1,Object o2) {
				int ret = 0;
				RealmComponent rc1 = (RealmComponent)o1;
				RealmComponent rc2 = (RealmComponent)o2;
				CharacterWrapper c1 = new CharacterWrapper(rc1.getGameObject());
				CharacterWrapper c2 = new CharacterWrapper(rc2.getGameObject());
				ret = c1.getCombatPlayOrder()-c2.getCombatPlayOrder();
				return ret;
			}
		});
		allParticipants.addAll(chars);
		allParticipants.addAll(everyoneElse);
		
		participantHasHotspots = new boolean[allParticipants.size()];
		updateHotspotIndicators();
		
		int row = allParticipants.indexOf(activeParticipant);
		if (row<0 || row>=participantTable.getRowCount()) {
			// Set a default in case none are found
			row = 1;
			
			// Didn't find one?  Try checking to see if activeParticipant is the owner of one of the participants
			for (int i=0;i<allParticipants.size();i++) {
				RealmComponent rc = allParticipants.get(i);
				RealmComponent owner = rc.getOwner();
				if (owner!=null && owner.equals(activeParticipant)) {
					// first one is fine
					row = i+1;
					break;
				}
			}
		}
		else {
			row += 1;
		}
		participantTable.clearSelection();
		if (row<participantTable.getRowCount()) {
			participantTable.setRowSelectionInterval(row,row);
		}
	}
	public void updateHotspotIndicators() {
		int n=0;
		for (RealmComponent rc:new ArrayList<RealmComponent>(allParticipants)) {
			CombatSheet cs = CombatSheet.createCombatSheet(this,currentBattleModel,rc,interactiveFrame);
			participantHasHotspots[n++] = cs.hasHotspots();
		}
		participantTable.revalidate();
		participantTable.repaint();
	}
	private void updateStateLights() {
		for (int i=0;i<STATELIGHT_NAME.length;i++) {
			String postfix = STATELIGHT_VAL[i]==actionState?"1":"0";
			stateLight[i].setIcon(ImageCache.getIcon("combat/buttons/"+STATELIGHT_NAME[i]+postfix));
		}
	}
	public void updateDenizenPanel() {
		BattleGroup denizenGroup = currentBattleModel.getDenizenBattleGroup();
		if (denizenGroup!=null && denizenGroup.size()>0) {
			for (Iterator i=denizenGroup.getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent denizen = (RealmComponent)i.next();
				CombatWrapper combat = new CombatWrapper(denizen.getGameObject());
				if (denizen.getTarget()==null && !combat.isSheetOwner()) {
					denizenPanel.addRealmComponent(denizen);
				}
			}
		}
		denizenPanel.repaint();
	}
	public void updateFrame(GameData data) {
		changes = false;
		nonaffectingChanges = false;
		clearRunaway();
		ambushRoll = null;
		ambusher = null;
		gameControls.setVisible(interactiveFrame);
		reset();
		gameData = data;
		theGame = GameWrapper.findGame(gameData);
		hostPrefs = HostPrefWrapper.findHostPrefs(gameData);
		TileLocation currentCombatLocation = RealmBattle.getCurrentCombatLocation(gameData);
		logger.finer("currentCombatLocation="+currentCombatLocation);
		
		if (currentCombatLocation!=null && currentCombatLocation.hasClearing()) {
			// Build uncontrolled/unassigned denizen panel
			currentBattleModel = RealmBattle.buildBattleModel(currentCombatLocation,gameData);
			
			// Test for a PEACE condition
			CombatWrapper tile = new CombatWrapper(currentCombatLocation.tile.getGameObject());
			if (tile.isPeaceClearing(currentCombatLocation.clearing.getNum())) {
				currentBattleModel.makePeace();
			}
			
			// Understand the current state (ie., luring, deploying, etc)
			HashLists lists = RealmBattle.findCharacterStates(currentCombatLocation,gameData);
			if (lists.isEmpty()) {
				// This happens when a player's hireling is asking to END combat when the owner is not present.
				// Not sure why, but doing a "return" here solves the problem.
				return;
			}
			ArrayList states = new ArrayList(lists.keySet());
			Collections.sort(states);
			
			Integer firstState = (Integer)states.iterator().next();
			if (firstState.intValue()>=Constants.COMBAT_WAIT) {
				return;
//				// Shoudn't encounter a WAIT state here!
//				throw new IllegalStateException("firstState is a wait state!  This shouldn't happen! "+(firstState.intValue()-Constants.COMBAT_WAIT));
			}
			else if (firstState.intValue()==0) {
				throw new IllegalStateException("firstState is ZERO!  This shouldn't happen!");
			}
			else {
				actionState = firstState.intValue();
				updateStateLights();
				
				updateDenizenPanel();
				
				// activeCharacter is the character that is viewing the frame, EXCEPT in the case where everyone
				// is viewing the results.  In THIS case, activeCharacter is simply the first one in the list, which
				// doesn't really mean ANYTHING.
				ArrayList characterList = lists.getList(firstState);
				activeCharacter = (CharacterWrapper)characterList.iterator().next();
				activeParticipant = RealmComponent.getRealmComponent(activeCharacter.getGameObject());
				activeCharacterIsHere = currentBattleModel.getBattleGroup(activeParticipant).getCharacterInBattle()!=null;
				activeCharacterIsTransmorphed = activeCharacter.getTransmorph()!=null;
				
				for (Iterator i=characterList.iterator();i.hasNext();) {
					CharacterWrapper character = (CharacterWrapper)i.next();
					if (character.getDoInstantPeer()) {
						// Only process if the character belongs to THIS player
						if (character.getPlayerName().equals(GameClient.GetMostRecentClient().getClientName())) {
							character.setDoInstantPeer(false);
							// This is lame, but seeing as this is the only spell (Wise Bird) that has this requirement, I'm going to go
							// ahead and do it this way, especially if it resolves the problems I'm having with the trade interface.
							TileLocation current = character.getCurrentLocation();
							CenteredMapView map = CenteredMapView.getSingleton();
							map.setMarkClearingAlertText("Wise bird tells "+character.getGameObject().getName()+" about which clearing?");
							map.markAllClearings(true);
							TileLocationChooser chooser = new TileLocationChooser(this,map,current,
									character.getCharacterName()+": Choose a Tile to PEER");
							chooser.setVisible(true);
							TileLocation sel = chooser.getSelectedLocation();
							map.markAllClearings(false);
							
							Peer peer = new Peer(this,sel.clearing);
							DieRoller roller = DieRollBuilder.getDieRollBuilder(this,character).createRoller(peer);
							String result = peer.apply(character,roller);
							
							JOptionPane.showMessageDialog(this,result,"PEER "+sel.toString(),JOptionPane.PLAIN_MESSAGE,roller.getIcon());
						}
					}
				}
					
				logger.finer("actionState = "+actionState);
				
				// Action Controls
				ArrayList controls = createControls();
				controlPanel.removeAll();
				for (Iterator i=controls.iterator();i.hasNext();) {
					JPanel panel = new JPanel(new BorderLayout());
					JComponent component = (JComponent)i.next();
					panel.add(component,"Center");
					controlPanel.add(panel);
				}
			}
			
			// Character Table and controls
			if (participantTableModel == null) {
					participantTableModel = new BattleParticipantTableModel(this);
					participantTable = new JTable(participantTableModel);
					participantTable.setRowHeight(PARTICIPANT_ROW_HEIGHT);
					participantTable.getColumnModel().getColumn(0).setMaxWidth(30);
					participantTable.getColumnModel().getColumn(1).setMaxWidth(30);
					participantTable.getColumnModel().getColumn(2).setMaxWidth(PARTICIPANT_ROW_HEIGHT);
					ComponentTools.lockColumnWidth(participantTable,4,100);
					participantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					participantTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent ev) {
							updateSelection();
						}
					});
				
				sideBarCenterPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				ComponentTools.lockComponentSize(sideBarCenterPanel,Constants.COMBAT_SIDEBAR_WIDTH,5000);
						
				participantTableSp = new JScrollPane(participantTable);
				sideBarCenterPanel.add(participantTableSp);
				
				// Setup Roller Results Frame
				rollerResults = new RollerResults();
				rollerResultsScroll = new JScrollPane(rollerResults);
				sideBarCenterPanel.add(rollerResultsScroll);
			
				sidePanel.add(sideBarCenterPanel,"Center");
			}
			else {
				participantTableModel.fireTableDataChanged();
			}
			combatSummaryButton.setVisible(actionState==Constants.COMBAT_RESOLVING);
			rollerResultsScroll.setVisible(actionState==Constants.COMBAT_RESOLVING);
			sideBarCenterPanel.setVisible(true);
			
			// Check to see if this is a random assignment stage
			updateRandomAssignment();
			
			refreshParticipants();
		}
		denizenScroll.setVisible(denizenPanel.getComponentCount()>0);
		updateControls();
		
		// Check for ask demon questions
		String myName = "test";
		if (GameClient.GetMostRecentClient()!=null) {
			myName = GameClient.GetMostRecentClient().getClientName();
		}
		String[] ret = theGame.getNextQuestion(myName);
		if (ret!=null) {
			// Must answer a question here!
			
			// Show a dialog with the question, and options for YES, NO, or a #
			DemonResponseDialog dialog = new DemonResponseDialog(this,ret[0]+" Asks Demon",ret[1]);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
			String answer = myName+" answers "+dialog.getAnswer();
			
			// Broadcast the question/answer to the specific client
			String key = Constants.BROADCAST_PRIVATE_MESSAGE+ret[0];
			broadcastMessage(key,answer);
			broadcastMessage(RealmLogging.BATTLE,myName+" answered a question.");
		}
//		if (actionState==Constants.COMBAT_RESOLVING) {
//			showCombatSummary();
//		}
	}
	public void setVisible(boolean val) {
		super.setVisible(val);
		if (val) {
			boolean battleRolls = false;
			if (actionState==Constants.COMBAT_RESOLVING) {
				if (allParticipants!=null) {
					for (RealmComponent rc:allParticipants) {
						CombatSheet sheet = CombatSheet.createCombatSheet(this,currentBattleModel,rc,interactiveFrame);
						if (sheet.hasBattleRolls()) {
							battleRolls = true;
							break;
						}
					}
				}
			}
			if (battleRolls) {
				sideBarCenterPanel.setDividerLocation(0.6);
			}
			else {
				sideBarCenterPanel.setDividerLocation(1.0);
			}
		}
	}
	public boolean hasRandomAssignment() {
		ArrayList list = activeCharacter.getGameObject().getThisAttributeList(Constants.RANDOM_ASSIGNMENT_WINNER);
		return (list!=null && list.size()>0);
	}
	public void updateRandomAssignment() {
		ArrayList list = activeCharacter.getGameObject().getThisAttributeList(Constants.RANDOM_ASSIGNMENT_WINNER);
		if (list!=null) {
			list = new ArrayList(list);
			if (list.size()>0) {
				String denizenId = (String)list.remove(0);
				activeCharacter.getGameObject().setThisAttributeList(Constants.RANDOM_ASSIGNMENT_WINNER,list);
				GameObject go = gameData.getGameObject(Long.valueOf(denizenId));
				RealmComponent denizen = RealmComponent.getRealmComponent(go);
				denizenPanel.setSelected(denizen);
				denizenPanel.disableSelection(); // prevents changing selections
			}
			else {
				activeCharacter.getGameObject().removeThisAttribute(Constants.RANDOM_ASSIGNMENT_WINNER);
			}
		}
	}
	public int getCurrentRound() {
		return currentBattleModel.getBattleRound(actionState);
	}
	private JButton getEndSimulationButton() {
		if (endSimulationButton==null) {
			endSimulationButton = new JButton("End Simulation");
			endSimulationButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					int ret = JOptionPane.showConfirmDialog(null,"End Simulation Now?","End Simulation",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
					if (ret==JOptionPane.YES_OPTION) {
						setVisible(false);
						dispose();
						close();
						RealmLogWindow.getSingleton().clearLog();
					}
				}
			});
		}
		return endSimulationButton;
	}
	private JButton getSaveSimulationButton() {
		if (saveSimulationButton==null) {
			saveSimulationButton = new JButton("Save Current Combat");
			saveSimulationButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					gameData.commit();
					saveBattle(CombatFrame.this,gameData);
				}
			});
		}
		return saveSimulationButton;
	}
	private JButton getRefreshDisplayButton() {
		if (refreshDisplayButton==null) {
			refreshDisplayButton = new JButton("Refresh Display");
			refreshDisplayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					refresh();
				}
			});
		}
		return refreshDisplayButton;
	}
	private JCheckBox getTreacheryButton() {
		if (treacheryOption==null) {
			treacheryOption = new JCheckBox("Allow Treachery");
			treacheryOption.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					activeCharacter.setTreacheryPreference(treacheryOption.isSelected());
					updateHotspotIndicators();
					updateSelection();
				}
			});
		}
		if (treacheryOption.isSelected()!=activeCharacter.getTreacheryPreference()) {
			treacheryOption.doClick();
		}
		return treacheryOption;
	}
	private JButton getChargeButton() {
		if (chargeButton==null) {
			chargeButton = new JButton("Charge Character");
			chargeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					doChargeCharacter();
				}
			});
		}
		return chargeButton;
	}
	private JButton getRunAwayButton() {
		if (runAwayButton==null) {
			runAwayButton = new JButton("Run Away");
			runAwayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					runAway();
				}
			});
		}
		return runAwayButton;
	}
	private JButton getAlertWeaponButton() {
		if (alertWeaponButton==null) {
			alertWeaponButton = new JButton("Alert Weapon/Chit");
			alertWeaponButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					alert();
				}
			});
		}
		return alertWeaponButton;
	}
	private JButton getCastSpellButton() {
		if (castSpellButton==null) {
			castSpellButton = new JButton("Cast a Spell");
			castSpellButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					castSpell();
				}
			});
		}
		return castSpellButton;
	}
	private JButton getActivateInactivateButton() {
		if (activateInactivateButton==null) {
			activateInactivateButton = new JButton("Activate/Inactivate");
			activateInactivateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					activateInactivate();
				}
			});
		}
		return activateInactivateButton;
	}
	private JButton getPickupItemButton() {
		if (pickupItemButton==null) {
			pickupItemButton = new JButton("Pickup Item");
			pickupItemButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					pickupItem();
				}
			});
		}
		return pickupItemButton;
	}
	private JButton getDropBelongingsButton() {
		if (dropBelongingsButton==null) {
			dropBelongingsButton = new JButton("Drop Belongings");
			dropBelongingsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					abandonBelongings(true);
				}
			});
		}
		return dropBelongingsButton;
	}
	private JButton getAbandonBelongingsButton() {
		if (abandonBelongingsButton==null) {
			abandonBelongingsButton = new JButton("Abandon Belongings");
			abandonBelongingsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					abandonBelongings(false);
				}
			});
		}
		return abandonBelongingsButton;
	}
	private JButton getSelectTargetFromUnassignedButton() {
		if (selectTargetFromUnassignedButton==null) {
			selectTargetFromUnassignedButton = new JButton("Select Unassigned Target");
			selectTargetFromUnassignedButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					ArrayList targetList = new ArrayList();
					Component[] c = denizenPanel.getComponents();
					for (int i=0;i<c.length;i++) {
						if (c[i] instanceof RealmComponent) {
							targetList.add(c[i]);
						}
					}
					
					// Choosing an unassigned target puts them on their OWN sheet (sucker punch)
					// unless watchful natives is in play
					RealmComponent theTarget = assignTarget(targetList);
					CombatWrapper targetCombat = theTarget==null?null:(new CombatWrapper(theTarget.getGameObject()));
					
					if (theTarget!=null && (!theTarget.isNative() || !hostPrefs.hasPref(Constants.TE_WATCHFUL_NATIVES))) {
						targetCombat.setSheetOwner(true);
						targetCombat.setCombatBox(1);
						changes = true;
						removeDenizen(theTarget);
						refreshParticipants();
						repaint();
						updateControls();
					}
				}
			});
		}
		return selectTargetFromUnassignedButton;
	}
	private JButton getSelectSpellTargetsButton() {
		if (selectSpellTargetsButton==null) {
			selectSpellTargetsButton = new JButton("Spell Targets");
			selectSpellTargetsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					assignSpellTargets();
				}
			});
		}
		return selectSpellTargetsButton;
	}
	private JButton getCancelSpellButton() {
		if (cancelSpellButton==null) {
			cancelSpellButton = new JButton("Cancel Spell");
			cancelSpellButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					cancelCastSpell();
				}
			});
		}
		return cancelSpellButton;
	}
	private JButton getChangeTacticsButton() {
		if (changeTacticsButton==null) {
			changeTacticsButton = new JButton("Change Tactics");
			changeTacticsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					GameObject transmorph = activeCharacter.getTransmorph();
					if (transmorph!=null) { // shouldn't really need this check, but oh well
						MonsterChitComponent monster = (MonsterChitComponent)RealmComponent.getRealmComponent(transmorph);
						boolean pinningNow = monster.isPinningOpponent();
						selectDeploymentSide(monster);
						if (pinningNow && !monster.isPinningOpponent()) { // switched from pinning side
							RealmComponent target = activeParticipant.getTarget();
							CombatWrapper combat = new CombatWrapper(target.getGameObject());
							combat.removeAttacker(activeParticipant.getGameObject());
							activeParticipant.clearTarget();
							updateSelection();
						}
					}
				}
			});
		}
		return changeTacticsButton;
	}
	private JButton getUseColorChitButton() {
		if (useColorChitButton==null) {
			useColorChitButton = new JButton("Play Color Chit");
			useColorChitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					MagicChit colorChit = RealmUtility.burnColorChit(CombatFrame.this,theGame,activeCharacter);
					if (colorChit!=null) {
						CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
						combat.addUsedChit(colorChit.getGameObject());
						combat.setBurnedColor(true);
					}
					nonaffectingChanges = true;
					updateControls();
					repaint();
				}
			});
		}
		return useColorChitButton;
	}
	private JPanel getShowPanel() {
		if (showPanel==null) {
			showPanel = new JPanel(new GridLayout(1,3));
			showPanel.add(getShowChitsButton());
			showPanel.add(getExportButton());
			showPanel.add(getShowInventoryButton());
		}
		return showPanel;
	}
	private JButton getShowChitsButton() {
		if (showChitsButton==null) {
			showChitsButton = new JButton(ImageCache.getIcon("tab/chits"));
			showChitsButton.setToolTipText("Show Chits");
			showChitsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					showChits();
				}
			});
		}
		return showChitsButton;
	}
	private JButton getShowInventoryButton() {
		if (showInventoryButton==null) {
			showInventoryButton = new JButton(ImageCache.getIcon("tab/inventory"));
			showInventoryButton.setToolTipText("Show Inventory");
			showInventoryButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					showInventory();
				}
			});
		}
		return showInventoryButton;
	}
	private JButton getExportButton() {
		if (exportButton==null) {
			exportButton = new JButton(ImageCache.getIcon("interface/export"));
			exportButton.setToolTipText("Export HTML");
			exportButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					File dest = exportFileManager.getSaveDirectory("Export HTML Game Summary");
					if (dest!=null) {
						String initialFolderName = "RSCombat_M"+theGame.getMonth()+"D"+theGame.getDay()+"_Round_"+getCurrentRound();
						String folderName = JOptionPane.showInputDialog("Folder Name:",initialFolderName);
						if (folderName!=null) {
							BattleHtmlGenerator generator = new BattleHtmlGenerator(gameData,RealmLogWindow.getSingleton().getHtmlString(),true,currentBattleModel,actionState,activeParticipant);
							generator.setCombatSheets(getAllCombatSheets());
							String path = dest.getAbsolutePath()+File.separator+folderName;
							generator.saveHtml(path);
							JOptionPane.showMessageDialog(CombatFrame.this,"Export Done:\n\n     "+path);
						}
					}
				}
			});		
		}
		return exportButton;
	}
	private JButton getSuggestButton() {
		if (suggestButton==null) {
			suggestButton = new JButton("Suggest Action");
			suggestButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					suggestionAi.suggestAction();
				}
			});
		}
		return suggestButton;
	}
	
	private ArrayList createControls() {
		ArrayList list = new ArrayList();
		roundLabel = new JLabel("Round "+getCurrentRound(),JLabel.CENTER);
		roundLabel.setFont(COMBAT_ROUND_FONT);
		roundLabel.setForeground(Color.blue);
		roundLabel.setBackground(Color.yellow);
		roundLabel.setOpaque(true);
		list.add(roundLabel);
		
		if (closeableFrame) {
			list.add(getEndSimulationButton());
			list.add(getSaveSimulationButton());
		}
		
		list.add(getRefreshDisplayButton());
		
		if (interactiveFrame) {
			// Special "Change Tactics" button for transmorphed players
			
			switch(actionState) {
				case Constants.COMBAT_LURE:
					instructionLabel = new JLabel("Lure Denizens",IconFactory.findIcon("icons/arrow4.gif"),JLabel.LEADING);
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					break;
				case Constants.COMBAT_DEPLOY:
					instructionLabel = new JLabel("Deploy/Charge",IconFactory.findIcon("icons/arrow2.gif"),JLabel.LEADING);
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					list.add(getChargeButton());
					break;
				case Constants.COMBAT_ACTIONS:
					instructionLabel = new JLabel("Actions",IconFactory.findIcon("icons/arrow2.gif"),JLabel.LEADING);
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					if (activeCharacterIsHere && activeCharacter.canChangeTactics()) {
						list.add(getChangeTacticsButton());
					}
					list.add(getRunAwayButton());
					list.add(getAlertWeaponButton());
					list.add(getCastSpellButton());
					list.add(getActivateInactivateButton());
					if (hostPrefs.hasPref(Constants.ADV_DROPPING)) {
						list.add(getPickupItemButton());
						list.add(getDropBelongingsButton());
					}
					list.add(getAbandonBelongingsButton());

					if (activeCharacterIsHere) {
						list.add(getUseColorChitButton());
					}
					break;
				case Constants.COMBAT_ASSIGN:
					instructionLabel = new JLabel("Assign Targets",IconFactory.findIcon("icons/arrow4.gif"),JLabel.LEADING);
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					list.add(getTreacheryButton());
					if (activeCharacterIsHere && activeCharacter.canChangeTactics()) {
						list.add(getChangeTacticsButton());
					}
					CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
					if (denizenPanel.getComponentCount()>0 && (!activeCharacterIsHere || combat.getCastSpell()==null)) {
						instructionLabel = new JLabel("Unassigned Targets",IconFactory.findIcon("icons/arrow2.gif"),JLabel.LEADING);
						instructionLabel.setFont(INSTRUCTION_FONT);
						list.add(instructionLabel);
						list.add(getSelectTargetFromUnassignedButton());
					}
					if (activeCharacterIsHere && combat.getCastSpell()!=null) {
						list.add(getSelectSpellTargetsButton());
						list.add(getCancelSpellButton());
					}
					break;
				case Constants.COMBAT_POSITIONING:
					instructionLabel = new JLabel("Attack/Maneuver",IconFactory.findIcon("icons/arrow4.gif"),JLabel.LEADING);
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					if (activeCharacterIsHere && activeCharacter.canChangeTactics()) {
						list.add(getChangeTacticsButton());
					}
					break;
				case Constants.COMBAT_TACTICS:
					// This stage can ONLY happen if the character has a special item (ie., Battle Bracelets)
					instructionLabel = new JLabel("Change Tactics",IconFactory.findIcon("icons/arrow4.gif"),JLabel.LEADING);
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					break;
				case Constants.COMBAT_RESOLVING:
					instructionLabel = new JLabel("Results");
					instructionLabel.setFont(INSTRUCTION_FONT);
					list.add(instructionLabel);
					break;
				case Constants.COMBAT_FATIGUE:
					break;
			}
			
			if (actionState!=Constants.COMBAT_RESOLVING) {
				list.add(getSuggestButton());
			}
			list.add(getShowPanel());
		}
		else {
			instructionLabel = new JLabel("Observing");
			instructionLabel.setFont(INSTRUCTION_FONT);
			list.add(instructionLabel);
		}
		return list;
	}
	private void showChits() {
		ChitStateViewer viewer = new ChitStateViewer(this,activeCharacter);
		viewer.setVisible(true);
	}
	private void showInventory() {
		// Use the trade dialog to show detail
		RealmTradeDialog viewer = new RealmTradeDialog(this,"Inventory for the "+activeCharacter.getCharacterName(),false,false,false);
		viewer.setTradeObjects(activeCharacter.getInventory());
		viewer.setVisible(true);
	}
	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		
		// State Lights
		Box stateLightPanel = Box.createVerticalBox();
		stateLight = new JLabel[STATELIGHT_NAME.length];
		for (int i=0;i<STATELIGHT_NAME.length;i++) {
			stateLight[i] = new JLabel(ImageCache.getIcon("combat/buttons/"+STATELIGHT_NAME[i]+"0"));
			stateLightPanel.add(stateLight[i]);
		}
		stateLightPanel.add(Box.createVerticalGlue());
		getContentPane().add(stateLightPanel,"West");
		
		JPanel superPanel = new JPanel(new BorderLayout());
		
		// Uncontrolled Denizen Area
		denizenPanel = new RealmObjectPanel(true,false);
		denizenPanel.activateFlipView();
		denizenPanel.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				updateControls();
			}
		});
		denizenScroll = new JScrollPane(denizenPanel);
		superPanel.add(denizenScroll,"North");
		
		// Combat view
		combatSheetPanel = new JPanel(new BorderLayout());
		superPanel.add(combatSheetPanel,"Center");
		
		sidePanel = new JPanel(new BorderLayout());
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		lockNextButton = new JToggleButton("Lock Next",IconFactory.findIcon("icons/lock.gif"),false);
		lockNextButton.setBorder(LOCK_OFF_BORDER);
		ComponentTools.lockComponentSize(lockNextButton,180,40);
		lockNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setLockNext(lockNextButton.isSelected());
			}
		});
		bottomPanel.add(lockNextButton,"North");
		combatSummaryButton = new JButton("Round Summary",IconFactory.findIcon("images/combat/combatsummary.gif"));
		combatSummaryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				showCombatSummary();
			}
		});
		bottomPanel.add(combatSummaryButton,"Center");
		gameControls = new JPanel(new GridLayout(2,2));
		bottomPanel.add(gameControls,"South");
		
		undoButton = new JButton("Reset");
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				gameData.rollback();
				refresh();
				CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Presses the RESET combat button.");
			}
		});
		gameControls.add(undoButton);
		textButton = new JButton("Details");
		textButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmLogWindow log = RealmLogWindow.getSingleton();
				log.setVisible(true);
				log.toFront();
				log.scrollToEnd();
			}
		});
		gameControls.add(textButton);
		
		// Skip Combat Button
		endButton = new FlashingButton("End");
		endButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				endCombat();
			}
		});
		endButton.setFont(INSTRUCTION_FONT);
		gameControls.add(endButton);
		
		// Done Button
		nextButton = new FlashingButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doNext();
			}
		});
		nextButton.setFont(INSTRUCTION_FONT);
		gameControls.add(nextButton);
		ComponentTools.lockComponentSize(gameControls,180,80);
		sidePanel.add(bottomPanel,"South");
		
		controlPanel = Box.createVerticalBox();
		sidePanel.add(controlPanel,"North");
		
		getContentPane().add(superPanel,"Center");
		
		getContentPane().add(sidePanel,"East");
		
		pack();
	}
	private void makeDefault(JButton button) {
		getRootPane().setDefaultButton(button);
		button.requestFocusInWindow();
	}
	public void refresh() {
		updateFrame(gameData);
		validate();
		repaintAll();
	}
	private void setLockNext(boolean val) {
		lockNextButton.setSelected(val);
		lockNextButton.setBorder(val?LOCK_ON_BORDER:LOCK_OFF_BORDER);
		CombatWrapper combat = new CombatWrapper(getSelectedParticipant().getGameObject());
		combat.setLockNext(val);
		if (participantTable!=null) {
			participantTable.repaint();
		}
	}
	private synchronized void checkAmbush() {
		if (ambushRoll!=null) {
			boolean hiddenStatus;
			if (successfulAmbush()) {
				JOptionPane.showMessageDialog(this,"You stay hidden!","Ambush!",JOptionPane.INFORMATION_MESSAGE,ambushRoll.getIcon());
				hiddenStatus = true;
				broadcastMessage(ambusher.getGameObject().getName(),"Remains hidden!  (Ambush rules)");
			}
			else {
				JOptionPane.showMessageDialog(this,"You are discovered!","Ambush!",JOptionPane.INFORMATION_MESSAGE,ambushRoll.getIcon());
				broadcastMessage(ambusher.getGameObject().getName(),"Is discovered!  (Ambush rules)"); // NPE here... synchronize?
				hiddenStatus = false;
			}
			if (!hiddenStatus) {
				broadcastMessage(ambusher.getGameObject().getName(),"Becomes unhidden.");
			}
			ambusher.setHidden(hiddenStatus);
		}
	}
	private void checkRunAway() {
		if (runAwayRoll!=null) {
			boolean success = runAwayRoll.getHighDieResult()<7;
			String runMessage = activeCharacter.getGameObject().getName()+"'s attempt to run was "+(success?"successful.":"cancelled.");
			broadcastMessage(activeCharacter.getGameObject().getName(),runMessage+": "+runAwayRoll.getDescription());
			JOptionPane.showMessageDialog(
					this,
					runMessage,
					activeCharacter.getGameObject().getName()+" Stumble Roll",
					JOptionPane.INFORMATION_MESSAGE,
					runAwayRoll.getIcon());
			
		}
		if (runAwayFatigue>0) {
			doFatigueWounds(this,activeCharacter);
		}
	}
	private void doNext() {
		if (activeCharacterIsHere) activeCharacter.testQuestRequirements(this);
		if (okayToContinue()) {
			checkAmbush();
			checkRunAway();
			if (actionState==Constants.COMBAT_ASSIGN || actionState==Constants.COMBAT_DEPLOY) {
				handleWatchfulNatives();
			}
			gameControls.setVisible(false);
			if (activeCombatSheet!=null) {
				activeCombatSheet.removeMouseListener(mouseListener);
				activeCombatSheet.removeMouseMotionListener(mouseListener);
			}
			// Exit the dialog, and allow the changes to be sent to the server...
			finishAction();
			Point p = getLocation();
			Dimension size = getSize();
			lastKnownLocation.x = p.x;
			lastKnownLocation.y = p.y;
			lastKnownLocation.width = size.width;
			lastKnownLocation.height = size.height;
			commit();
		}
	}
	private void commit() {
		finishedActionListener.actionPerformed(new ActionEvent(this,0,"")); // does the submit in RealmSpeak
	}
	private void closeFrame() {
		setVisible(false);
		dispose();
	}
	public void updateSelection() {
		if (activeCombatSheet!=null) {
			activeCombatSheet.removeMouseListener(mouseListener);
			activeCombatSheet.removeMouseMotionListener(mouseListener);
		}
		activeCombatSheet = null;
		if (activeParticipant!=null) {
			int row = participantTable.getSelectedRow();
			combatSheetPanel.removeAll();
			lockNextButton.setVisible(false);
			if (row>=0) {
				if (row==0) {
					ArrayList characters = new ArrayList();
					for (Iterator i=currentBattleModel.getAllParticipatingCharacters().iterator();i.hasNext();) {
						RealmComponent rc = (RealmComponent)i.next();
						characters.add(new CharacterWrapper(rc.getGameObject()));
					}
					combatSheetPanel.add(new JScrollPane(new CombatSummarySheet(characters)));
				}
				else {
					RealmComponent rc = allParticipants.get(row-1);
					activeCombatSheet = CombatSheet.createCombatSheet(this,currentBattleModel,rc,interactiveFrame);
					activeCombatSheet.addMouseListener(mouseListener);
					activeCombatSheet.addMouseMotionListener(mouseListener);
					combatSheetPanel.add(new JScrollPane(activeCombatSheet),"Center");
					
					if (rc.isCharacter()) {
						CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
						if (character.getPlayerName().equals(playerName)) { // FIXME NPE when adding, and then removing a character in the battle builder, and then playing a battle.
							lockNextButton.setEnabled(true);
							CombatWrapper combat = new CombatWrapper(rc.getGameObject());
							setLockNext(combat.isLockNext());
							
							// Before making the button visible, make sure that this isn't the LAST playing character
							int count = 1;
							Collection current = currentBattleModel.getAllParticipatingCharacters();
							current.remove(rc);
							for (Iterator i=current.iterator();i.hasNext();) {
								RealmComponent cc = (RealmComponent)i.next();
								CombatWrapper cw = new CombatWrapper(cc.getGameObject());
								if (!cw.isLockNext()) {
									count++;
								}
							}
							
							lockNextButton.setVisible(count>1);
						}
					}
				}
			}
			combatSheetPanel.revalidate();
			combatSheetPanel.repaint();
			updateControls();
		}
	}
	private RealmComponent getSelectedParticipant() {
		int row = participantTable.getSelectedRow();
		if (row>0) {
			return allParticipants.get(row-1);
		}
		return null;
	}
	/**
	 * @return			true if the activeCharacter is being pinned by a monster on his/her sheet
	 */
	private boolean activeHasRedSideUpMonster() {
		if (activeCharacterIsHere) { // no need to search if not here!
			for (Iterator i=currentBattleModel.getAllBattleParticipants(true).iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isMonster()) {
					RealmComponent target = rc.getTarget();
					if (target!=null && target.equals(activeParticipant)) {
						MonsterChitComponent monster = (MonsterChitComponent)rc;
						if (monster.isPinningOpponent()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	protected void updateControls() {
		nextButton.setEnabled(interactiveFrame);
		textButton.setEnabled(true);
		if (exportButton!=null) exportButton.setEnabled(isGameHost);
		if (activeParticipant!=null) {
			RealmComponent selectedParticipant = getSelectedParticipant();
			CombatWrapper combat = new CombatWrapper(activeParticipant.getGameObject());
			boolean randomAss = hasRandomAssignment();
			if (actionState!=Constants.COMBAT_RANDOM_ASSIGN) {
				denizenPanel.enableSelection();
			}
			if (endButton!=null) {
				boolean canEnd =
					interactiveFrame													// Not observing
					&& endCombatFrame==null												// Didn't already request END
					&& !randomAss														// Not doing random assignment (or kicking some random ass?)
					&& currentBattleModel.canSkipCombat(actionState)					// BattleModel allows skip combat
					&& !changes;														// Are no changes
				endButton.setEnabled(canEnd);
			}
			if (chargeButton!=null) {
				chargeButton.setEnabled(endCombatFrame==null
						&& !combat.isPeaceful()											// can't charge if peaceful!
						&& selectedParticipant!=null
						&& selectedParticipant.isCharacter()
						&& activeCharacterIsHere										// can only charge if the character is actually there!
						&& !activeHasRedSideUpMonster()									// can't charge with a red-side-up monster on your sheet!
						&& !combat.getHasCharged() && !changes);						// need to disable if already charged
			}
			if (runAwayButton!=null) {
				// make sure the character is here AND there are NO red-side-up monsters!
				runAwayButton.setEnabled(endCombatFrame==null
						&& activeCharacterIsHere
						&& !activeCharacter.isFortified()
						&& !combat.getHasCharged()
						&& !activeHasRedSideUpMonster()
						&& !changes);
			}
			if (castSpellButton!=null) {
				Collection castableSpellSets = null;
				if (activeCharacterIsHere) {
					castableSpellSets = activeCharacter.getCastableSpellSets();
				}
				castSpellButton.setEnabled(endCombatFrame==null && !combat.isPeaceful() && activeCharacterIsHere && !combat.getHasCharged() && !activeCharacterIsTransmorphed && castableSpellSets.size()>0 && !changes);
			}
			if (selectSpellTargetsButton!=null) {
				GameObject go = combat.getCastSpell();
				SpellWrapper spell = go==null?null:new SpellWrapper(go);
				boolean isSpell = endCombatFrame==null && activeCharacterIsHere && spell!=null && spell.getTargets().isEmpty();
				selectSpellTargetsButton.setEnabled(isSpell);
				cancelSpellButton.setEnabled(isSpell);
			}
			if (alertWeaponButton!=null) {
				alertWeaponButton.setEnabled(endCombatFrame==null && activeCharacterIsHere && !combat.getHasCharged() && !activeCharacterIsTransmorphed && !changes);
			}
			if (activateInactivateButton!=null) {
				activateInactivateButton.setEnabled(endCombatFrame==null && activeCharacterIsHere && !combat.getHasCharged() && !activeCharacterIsTransmorphed && !changes);
			}
			if (pickupItemButton!=null) {
				pickupItemButton.setEnabled(!changes && activeCharacterIsHere && !combat.getHasCharged());
			}
			if (dropBelongingsButton!=null) {
				dropBelongingsButton.setEnabled(!changes && activeCharacterIsHere && !combat.getHasCharged());
			}
			if (abandonBelongingsButton!=null) {
				abandonBelongingsButton.setEnabled(!changes && activeCharacterIsHere && !combat.getHasCharged());
			}
			if (selectTargetFromUnassignedButton!=null) {
				selectTargetFromUnassignedButton.setEnabled(!changes && activeCharacterIsHere);
			}
			if (useColorChitButton!=null) {
				useColorChitButton.setEnabled(activeCharacter.getColorMagicChits().size()>0);
			}
			undoButton.setEnabled(interactiveFrame && endCombatFrame==null && (changes || nonaffectingChanges));
		}
		if (endCombatFrame==null && currentBattleModel!=null) {
			TileLocation tl = currentBattleModel.getBattleLocation();
			CombatWrapper tile = new CombatWrapper(tl.tile.getGameObject());
			if (tile.isPeaceClearing(tl.clearing.getNum())) {
				nextButton.setEnabled(interactiveFrame);
				endButton.setEnabled(interactiveFrame);
				if (castSpellButton!=null) {
					castSpellButton.setEnabled(false);
				}
			}
		}
		nextButton.setFlashing(false);
		endButton.setFlashing(false);
		if (nextButton.isEnabled() && advanceError()==null) {
			nextButton.setFlashing(interactiveFrame);
		}
		else if (endButton.isEnabled()) {
			endButton.setFlashing(interactiveFrame);
		}
		if (!endButton.isEnabled()) {
			lockNextButton.setVisible(false);
		}
		
		if (interactiveFrame) makeDefault(nextButton);
	}
	private boolean okayToContinue() {
		if (currentBattleModel!=null) {
			TileLocation tl = currentBattleModel.getBattleLocation();
			CombatWrapper tile = new CombatWrapper(tl.tile.getGameObject());
			if (tile.isPeaceClearing(tl.clearing.getNum())) {
				return true;
			}
		}
		
		RealmComponentError error = advanceError();
		if (error!=null) {
			if (error.getRc()!=null) {
				setSelected(error.getRc());
			}
			return error.showDialog(this);
		}
		return true;
	}
	private RealmComponentError advanceError() {
		if (actionState==Constants.COMBAT_RANDOM_ASSIGN) {
			if (denizenPanel.getSelectedCount()>0) {
				return new RealmComponentError(null,"Random Assignment Incomplete","You must complete the random assignment phase before continuing.");
			}
		}
		else if (actionState==Constants.COMBAT_POSITIONING) {
			// Verify that everything that needs to be done, has been done (but only worry about friendly sheets)
			ArrayList friendly = CombatSheet.filterFriends(activeParticipant,allParticipants);
			for (Iterator i=friendly.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				CombatSheet sheet = CombatSheet.createCombatSheet(this,currentBattleModel,rc,interactiveFrame);
				
				// Verify that all denizens are positioned
				if (sheet.hasUnpositionedDenizens()) {
					return new RealmComponentError(rc,"Unpositioned Denizens","There are unpositioned denizens on your sheet.");
				}
				
				// Verify that as many combat boxes as can be used, ARE used
				if (!sheet.usesMaxCombatBoxes() && !hostPrefs.hasPref(Constants.OPT_NO_BATTLE_DIST)) {
					return new RealmComponentError(rc,"Not using enough boxes","You must place targets in as many boxes as possible (up to 3) before continuing.");
				}
			}
			// Verify that attack spell (if any) was placed
			CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
			GameObject sgo = combat.getCastSpell();
			if (sgo!=null) {
				SpellWrapper spell = new SpellWrapper(sgo);
				if (spell.isAlive() && spell.isAttackSpell() && spell.getAttackCombatBox()==0) {
					return new RealmComponentError(null,"Missing spell attack","You must place your spell attack before continuing.");
				}
			}
			
			// Make sure the character has placed an attack
			if (activeCharacterIsHere && activeParticipant.getTarget()!=null) {
				CombatWrapper charCombat = new CombatWrapper(activeParticipant.getGameObject());
				if (!charCombat.getPlayedAttack() && canPlayAttack(0)) {
					return new RealmComponentError(activeParticipant,"Missing Attack Placement","The active character needs to place an attack.  Do you want to continue anyway?",true);
				}
			}
		}
		else if (actionState==Constants.COMBAT_ASSIGN) {
			if (selectSpellTargetsButton!=null && selectSpellTargetsButton.isEnabled()) {
				return new RealmComponentError(null,"Missing spell attack","You must place your spell attack before continuing.");
			}
			
			ArrayList friendly = CombatSheet.filterFriends(activeParticipant,allParticipants);
			for (Iterator i=friendly.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				CombatSheet sheet = CombatSheet.createCombatSheet(this,currentBattleModel,rc,interactiveFrame);
				
				// Verify that all targets (that need to be) are assigned
				if (sheet.needsTargetAssignment()) {
					return new RealmComponentError(rc,"Missing Target Assignment","The sheet owner should select a target.");
				}
			}
		}
		return null;
	}
	private void setSelected(RealmComponent participant) {
		int rownum=0;
		for (RealmComponent rc:allParticipants) {
			if (rc.equals(participant)) {
				participantTable.setRowSelectionInterval(rownum+1,rownum+1);
				return;
			}
			rownum++;
		}
	}
	private int getAvailableEffort() {
		// Determine what effort limit is (usually 2)
		int effortLimit = activeCharacter.getEffortLimit();
		
		// Total effort used
		Effort totalEffort = BattleUtility.getEffortUsed(activeCharacter);
		
		// Available is the difference
		return effortLimit-totalEffort.getAsterisks();
	}
	/**
	 * Removes any pieces that have already been played this round
	 */
	private void filterUsedOptions(ArrayList list) {
		// Be sure to remove any pieces that have already been played!
		CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
		for (Iterator i=combat.getUsedChits().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			list.remove(rc);
		}
	}
	/**
	 * @param box				The box to which the maneuver will be placed.
	 * @param includeHorses		Include Horses in the maneuver possibilities
	 * 
	 * @return					A collection of all maneuver possibilities for the chosen box.
	 */
	public Collection getAvailableManeuverOptions(int box,boolean includeHorses) {
		return getAvailableManeuverOptions(box,includeHorses,true);
	}
	public Collection getAvailableManeuverOptions(int box,boolean includeHorses,boolean limitEffort) {
		ArrayList list = new ArrayList();
		GameObject transmorph = activeCharacter.getTransmorph();
		if (transmorph==null) {
			int effortLeft = limitEffort?getAvailableEffort():2;
			
			// Limit maneuver choices to those that can handle the heaviest piece of inventory
			Strength heaviestInventory = activeCharacter.getNeededSupportWeight();
			
			// Find all active chits that have less than (effortLimit-totalEffort) asterisks
			Collection c = activeCharacter.getActiveMoveChits();
			c.addAll(activeCharacter.getFlyChits());
			for (Iterator i=c.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isActionChit()) {
					CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
					if (chit.getEffortAsterisks()<=effortLeft && chit.getStrength().strongerOrEqualTo(heaviestInventory)) {
						// Check the box_constraint (important for the DUCK chit)
						int constraint = chit.getGameObject().getThisInt("box_constraint");
						if (constraint==0 || constraint==box) {
							if (!chit.getGameObject().hasThisAttribute(Constants.UNPLAYABLE)) {
								CombatWrapper combat = new CombatWrapper(chit.getGameObject());
								if (!combat.getPlacedAsFight()) { // Don't use a chit that was already placed as an attack!! (Fight/Move chits)
									list.add(chit);
								}
							}
						}
					}
				}
				else if (rc.isFlyChit()) {
					// The only "Fly Chit" besides action chits right now is the Broomstick, which doesn't really have asterisks or constraints
					list.add(rc);
				}
			}
			
			// Add any boots cards or horses or flying carpets
			for (Iterator i=activeCharacter.getActiveInventory().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (go.hasThisAttribute("boots")) {
					Strength bootStrength = new Strength(go.getThisAttribute("strength"));
					if (bootStrength.strongerOrEqualTo(heaviestInventory)) {
						list.add(rc);
					}
				}
				else if (includeHorses && rc.isHorse()) {
					// Check strength on both sides to see which is stronger (when CHARGING, is coded, may have to determine if horse was used)
					Strength trotStrength = new Strength(go.getAttribute("trot","strength"));
					Strength gallopStrength = new Strength(go.getAttribute("gallop","strength"));
					if (trotStrength.strongerOrEqualTo(heaviestInventory) || gallopStrength.strongerOrEqualTo(heaviestInventory)) {
						list.add(rc);
					}
				}
				else if (go.hasThisAttribute("fly_strength")) {
					Strength flyStrength = new Strength(go.getThisAttribute("fly_strength"));
					if (flyStrength.strongerOrEqualTo(heaviestInventory)) {
						list.add(rc);
					}
				}
			}
			
			filterUsedOptions(list);
		}
		else {
			// Character is transmorphed!
			RealmComponent rc = RealmComponent.getRealmComponent(transmorph);
			if (rc.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)rc;
				list.add(monster.getMoveChit());
			}
		}
		
		return list;
	}
	
	public Collection getAvailableFightOptions(int box) {
		return getAvailableFightOptions(box,true);
	}
	public Collection getAvailableFightOptions(int box,boolean limitEffort) {
		ArrayList list = new ArrayList();
		GameObject transmorph = activeCharacter.getTransmorph();
		if (transmorph==null) {
			int effortLeft = limitEffort?getAvailableEffort():2;
			
			// Limit fight choices to those that can handle the weight of the active weapon
			Strength weaponWeight = activeCharacter.getActiveWeaponWeight();
			
			// Find all active chits that have less than (effortLimit-totalEffort) asterisks
			for (Iterator i=activeCharacter.getActiveFightChits().iterator();i.hasNext();) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
				if (chit.getEffortAsterisks()<=effortLeft && chit.getStrength().strongerOrEqualTo(weaponWeight)) {
					// Check the box_constraint (for fight_lock type options - custom characters only)
					int constraint = chit.getGameObject().getThisInt("box_constraint");
					if (constraint==0 || constraint==box) {
						if (!chit.getGameObject().hasThisAttribute(Constants.UNPLAYABLE)) {
							CombatWrapper combat = new CombatWrapper(chit.getGameObject());
							if (!combat.getPlacedAsMove()) { // Don't use a chit that was already placed as a maneuver!! (Fight/Move chits)
								list.add(chit);
							}
						}
					}
				}
			}
			
			// Add any gloves cards
			for (Iterator i=activeCharacter.getActiveInventory().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (go.hasThisAttribute("gloves")) {
					Strength gloveStrength = new Strength(go.getThisAttribute("strength"));
					if (gloveStrength.strongerOrEqualTo(weaponWeight)) {
						list.add(rc);
					}
				}
			}
			
			filterUsedOptions(list);
		}
		else {
			// Character is transmorphed!
			RealmComponent rc = RealmComponent.getRealmComponent(transmorph);
			if (rc.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)rc;
				list.add(monster.getFightChit());
			}
		}
		return list;
	}
	public String getActionName() {
		switch(actionState) {
			case Constants.COMBAT_LURE:			return "Lure";
			case Constants.COMBAT_RANDOM_ASSIGN:	return "Random";
			case Constants.COMBAT_DEPLOY:			return "Deploy";
			case Constants.COMBAT_ACTIONS:		return "Encounter";
			case Constants.COMBAT_ASSIGN:			return "Assign";
			case Constants.COMBAT_POSITIONING:		return "Position";
			case Constants.COMBAT_TACTICS:		return "Tactics";
			case Constants.COMBAT_RESOLVING:		return "Results";
			case Constants.COMBAT_FATIGUE:		return "Fatigue";
			case Constants.COMBAT_DISENGAGE:		return "Disengage";
		}
		return "?";
	}
	private void finishAction() {
		int nextStatus = RealmBattle.getNextWaitState(actionState);
		// Set status for ALL characters on the same client as activeCharacter on RESOLVING
		if (actionState==Constants.COMBAT_RESOLVING) {
			Collection allCharacters = currentBattleModel.getAllOwningCharacters();
			for (Iterator i=allCharacters.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
				
				if (playerName.equals(character.getPlayerName())) {
					// This guarantees ALL characters on a particular client finish looking at results at the same time
					character.setCombatStatus(nextStatus);
				}
			}
		}
		else {
			activeCharacter.setCombatStatus(nextStatus);
		}
	}
	public boolean areDenizensToLure(RealmComponent lurer) {
		return denizenPanel.getComponentCount()>0 || !findCharactersWithDenizenAttackers().isEmpty();
	}
	public int selectedDenizenCount() {
		return denizenPanel.getSelectedComponents().size();
	}
	private ArrayList findCharactersWithDenizenAttackers() {
		ArrayList list = new ArrayList();
		if (currentBattleModel.areDenizens()) {
			ArrayList denizens = new ArrayList(currentBattleModel.getDenizenBattleGroup().getBattleParticipants());
			for (Iterator i=currentBattleModel.getAllParticipatingCharacters().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				for (Iterator n=denizens.iterator();n.hasNext();) {
					RealmComponent den = (RealmComponent)n.next();
					if (den.targeting(rc)) {
						if (den.isMonster() && ((MonsterChitComponent)den).isPinningOpponent()) {
							// Ignore red side up monsters (can't be lured)
							continue;
						}
						// Only add characters that have at least one denizen attacker
						if (!rc.isMonster() || !((MonsterChitComponent)rc).isPinningOpponent()) {
							// Don't include red-side-up monsters
							list.add(rc);
							break;
						}
					}
				}
			}
		}
		return list;
	}
	public void lureDenizens(RealmComponent lurer,int box,boolean lureMultiple) {
		if (denizenPanel.getSelectedCount()>0) {
			lureSelectedDenizens(lurer,box);
			changes = true;
		}
		else {
			// Pick from characters sheets
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Lure from which Character Sheet?",true);
			ArrayList list = findCharactersWithDenizenAttackers();
			list.remove(lurer);
			chooser.addRealmComponents(list,true);
			if (chooser.hasOptions()) {
				chooser.setVisible(true);
				if (chooser.getSelectedText()!=null) {
					RealmComponent rc = chooser.getFirstSelectedComponent();
					ArrayList attackers = currentBattleModel.getAttackersFor(rc);
					ArrayList denizens = new ArrayList(currentBattleModel.getDenizenBattleGroup().getBattleParticipants());
					denizens.retainAll(attackers); // intersection to find all denizen attackers
					// Be sure to strip out any red-side-up monsters
					ArrayList remove = new ArrayList();
					for (Iterator i=denizens.iterator();i.hasNext();) {
						RealmComponent dc = (RealmComponent)i.next();
						if (dc.isMonster() && ((MonsterChitComponent)dc).isPinningOpponent()) {
							remove.add(dc);
						}
					}
					denizens.removeAll(remove);
					
					String title = "Lure which denizen"+((denizens.size()>1 && lureMultiple)?"s":"")+"?";
						
					RealmObjectChooser chooser2 = new RealmObjectChooser(title,gameData,!lureMultiple);
					chooser2.addComponentsToChoose(denizens);
					chooser2.setVisible(true);
					Collection c = chooser2.getChosenObjects();
					if (c!=null && !c.isEmpty()) {
						for (Iterator i=c.iterator();i.hasNext();) {
							GameObject go = (GameObject)i.next();
							RealmComponent luree = RealmComponent.getRealmComponent(go);
							if (lureDenizen(lurer,box,luree)) {
								changes = true;
							}
						}
					}
				}
			}
		}
		updateSelection();
	}
	private void lureSelectedDenizens(RealmComponent lurer,int box) {
		Collection denizens = denizenPanel.getSelectedComponents();
		denizenPanel.clearSelected();
		for (Iterator i=denizens.iterator();i.hasNext();) {
			RealmComponent denizen = (RealmComponent)i.next();
			lureDenizen(lurer,box,denizen);
		}
	}
	private boolean lureDenizen(RealmComponent lurer,int box,RealmComponent denizen) {
		if (denizen.isMistLike()) {
			String message = "The "+lurer.getGameObject().getName()
							+" cannot lure the "+denizen.getGameObject().getName()
							+" because it has been transmorphed into mist.";
			JOptionPane.showMessageDialog(this,message,"Melted into Mist",JOptionPane.WARNING_MESSAGE,denizen.getIcon());
			return false;
		}
		if (!lurer.isImmuneTo(denizen)) {
			CombatFrame.broadcastMessage(lurer.getGameObject().getName(),"Lures the "+denizen.getGameObject().getName());
			
			RealmComponent target = denizen.getTarget();
			if (target!=null) {
				CombatWrapper combat = new CombatWrapper(target.getGameObject());
				combat.removeAttacker(denizen.getGameObject());
			}
			
			denizen.setTarget(lurer);
			CombatWrapper combat = new CombatWrapper(denizen.getGameObject());
			combat.setCombatBox(box);
			denizenPanel.removeGameObject(denizen.getGameObject());
			denizenPanel.repaint();
			if (lurer.isHidden()) {
				lurer.setHidden(false);
			}
		}
		else {
			String message = "The "+lurer.getGameObject().getName()
							+" cannot lure the "+denizen.getGameObject().getName()
							+" because of immunity to "+denizen.getGameObject().getName()+"s";
			JOptionPane.showMessageDialog(this,message,"Demon Immunity",JOptionPane.WARNING_MESSAGE,denizen.getIcon());
			return false;
		}
		if (denizen.isNative()) {
			if (!activeCharacter.isBattling(denizen.getGameObject())) {
				activeCharacter.addBattlingNative(denizen.getGameObject());
			}
		}
		if (denizen.isPacifiedBy(activeCharacter)) {
			// Luring a pacified monster or native will break the spell
			SpellWrapper spell = denizen.getPacificationSpell(activeCharacter);
			if (spell!=null) {
				spell.expireSpell();
				JOptionPane.showMessageDialog(this,spell.getName()+" was broken!");
			}
		}
		return true;
	}
	private void doChargeCharacter() {
		boolean found = false;
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Charge Character",true);
		for (RealmComponent rc:findCanBeSeen(currentBattleModel.getAllParticipatingCharacters(),false)) {
			if (!rc.isHorse() && !rc.equals(activeParticipant)) {
				chooser.addRealmComponent(rc);
				found = true;
			}
		}
		if (!found){
			JOptionPane.showMessageDialog(
					this,
					"There are no available characters to charge.\n(Remember that hidden characters cannot be charged\nunless you have found hidden enemies)",
					"Charge Character",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		chooser.setVisible(true);
		RealmComponent charged = chooser.getFirstSelectedComponent();
		if (charged==null) return;
		
		// Select an active MOVE chit to charge with
		MoveActivator activator = new MoveActivator(this);
		if (MoveActionResult.SUCCESSFUL == activator.playedValidMoveChit(
				"Charge "+charged.getGameObject().getName(),
				"You cannot charge, because you do not have a fast enough move to play.\n(Check your inventory!)",
				false)) {
			CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Charges the "+charged.getGameObject().getName());
			if (activeCharacter.isHidden()) {
				activeCharacter.setHidden(false);
				CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Is unhidden!");
			}
			RealmComponent rc = activator.getSelectedMoveChit();
			CombatWrapper chargedWrap = new CombatWrapper(charged.getGameObject());
			chargedWrap.addChargeChit(rc.getGameObject());
			CombatWrapper activeWrap = new CombatWrapper(activeCharacter.getGameObject());
			activeWrap.setHasCharged(true);
			changes = true;
		}
		updateSelection();
	}
	private ArrayList getSelectedCombatSheetParticipants() {
		ArrayList list = new ArrayList();
		for (Iterator i=activeCombatSheet.getAllParticipantsOnSheet().iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			
			if (!rc.equals(activeParticipant) && (rc.isMonster() || rc.isNative())) {
				list.add(rc);
			}
		}
		return CombatSheet.filterEnemies(activeParticipant,list);
	}
	public void assignSpellTargets() {
		CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
		GameObject go = combat.getCastSpell();
		SpellWrapper spell = new SpellWrapper(go);
		
		// Create a SpellTargeting object, and populate it
		SpellTargeting spellTargeting = SpellTargeting.getTargeting(this,spell);
		if (spellTargeting==null) {
			return;
		}
		if (!spellTargeting.populate(currentBattleModel,activeParticipant)) {
			return;
		}
		
		// Make sure that targeting can happen
		if (!spellTargeting.hasTargets()) {
			JOptionPane.showMessageDialog(this,"There are no valid targets for this spell!.","Unable to Select Targets for "+spell.getName(),JOptionPane.ERROR_MESSAGE);
			spell.expireSpell();
			if (selectSpellTargetsButton!=null) {
				selectSpellTargetsButton.setVisible(false);
				cancelSpellButton.setVisible(false);
				selectSpellTargetsButton = null;
				cancelSpellButton = null;
			}
			return;
		}
		
		// Assign targets
		BattleUtility.treacheryFlag = false;
		if (spellTargeting.assign(hostPrefs,activeCharacter)) {
			if (BattleUtility.treacheryFlag) {
				refreshParticipants();
				repaint();
			}
			// Casting a spell causes you to become unhidden, if it targets an individual (not clearing)
			if (spell.targetsCharacterOrDenizen() && activeCharacter.isHidden()) {
				if (spell.getTargetCount()<=1 && hostPrefs.hasPref(Constants.ADV_AMBUSHES)) {
					// You get an ambush roll to see if you stay hidden
					ambushRoll = DieRollBuilder.getDieRollBuilder(this,activeCharacter).createHideRoller();
					ambusher = RealmComponent.getRealmComponent(activeCharacter.getGameObject());
				}
				else {
					activeCharacter.setHidden(false,false);
					refreshParticipants();
					repaint();
				}
			}
			
			changes = true;
			updateSelection();
			updateDenizenPanel();
		}
	}
	protected void cancelCastSpell() {
		CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
		GameObject go = combat.getCastSpell();
		SpellWrapper spell = new SpellWrapper(go);
		
		int ret = JOptionPane.showConfirmDialog(this,"This will cancel "+spell.getName()+".  Are you sure?","Cancel "+spell.getName(),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		
		if (ret==JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(this,"Spell was cancelled and will NOT be cast.","Cancel "+spell.getName(),JOptionPane.ERROR_MESSAGE);
			RealmLogging.logMessage(activeCharacter.getGameObject().getName(),"Cancels "+spell.getName());
			spell.expireSpell();
			if (selectSpellTargetsButton!=null) {
				selectSpellTargetsButton.setVisible(false);
				cancelSpellButton.setVisible(false);
				selectSpellTargetsButton = null;
				cancelSpellButton = null;
			}
		}
	}
	protected void assignTarget() {
		ArrayList list = getSelectedCombatSheetParticipants();
		assignTarget(list);
	}
	protected RealmComponent assignTarget(Collection list) {
		return assignTarget(activeParticipant,list);
	}
	public boolean canBeSeen(RealmComponent rc,boolean magicAttack) {
		return !rc.isMistLike() && (!magicAttack || !rc.hasMagicProtection())
				&& (!rc.isHidden() || activeCharacter.foundHiddenEnemy(rc.getGameObject()) || rc.getOwner()==activeParticipant);
	}
	/**
	 * @return			The list of RealmComponents that can be seen by the activeCharacter.
	 */
	public ArrayList<RealmComponent> findCanBeSeen(Collection list,boolean magicAttack) {
		ArrayList<RealmComponent> ret = new ArrayList<RealmComponent>();
		for (Iterator i=list.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			// Make sure participant is "visible" to attacker
			if (canBeSeen(rc,magicAttack)) {
				ret.add(rc);
				if (hostPrefs.hasPref(Constants.OPT_RIDING_HORSES)) {
					RealmComponent horse = (RealmComponent)rc.getHorse();
					if (horse!=null) {
						ret.add(horse);
					}
				}
			}
		}
		return ret;
	}
	protected RealmComponent assignTarget(RealmComponent attacker,Collection list) {
		if (list!=null && list.size()>0) {
			ArrayList visibleList = findCanBeSeen(list,false);
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Select a Target:",true);
			chooser.addRealmComponents(visibleList,true);
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				RealmComponent theTarget = chooser.getFirstSelectedComponent();
				String append = "";
				if (theTarget.getGameObject().hasThisAttribute(Constants.NUMBER)) {
					append = " "+theTarget.getGameObject().getThisAttribute(Constants.NUMBER);
				}
				if (hostPrefs.hasPref(Constants.OPT_RIDING_HORSES)) {
					if (theTarget.isHorse() || theTarget.isNativeHorse()) {
						// Change target to native
						theTarget = RealmComponent.getRealmComponent(theTarget.getGameObject().getHeldBy());
						append = " (aiming for horse)";
					}
					else if (theTarget.getHorse()!=null) {
						CombatWrapper combat = new CombatWrapper(theTarget.getGameObject());
						combat.setTargetingRider(attacker.getGameObject());
						append = " (aiming for rider)";
					}
				}
				attacker.setTarget(theTarget);
				broadcastMessage(attacker.getGameObject().getName(),"Attacks the "+theTarget.getGameObject().getName()+append);
				makeTarget(this,hostPrefs,attacker,theTarget,theGame);
				if (theTarget.isNative() && theTarget.getOwnerId()==null) {
					// non-battling unhired natives will begin battling the character immediately if attacked
					if (!activeCharacter.isBattling(theTarget.getGameObject())) {
						activeCharacter.addBattlingNative(theTarget.getGameObject());
					}
					makeWatchfulNatives(theTarget,true);
				}
				if (theTarget.isPacifiedBy(activeCharacter)) {
					// Luring a pacified monster or native will break the spell
					SpellWrapper spell = theTarget.getPacificationSpell(activeCharacter);
					if (spell!=null) { // Might be null if Giants are pacified by the Pretty Rock
						spell.expireSpell();
						broadcastMessage(spell.getName()+" was broken!");
						JOptionPane.showMessageDialog(this,spell.getName()+" was broken!");
					}
				}
				if (theTarget.ownedBy(activeParticipant)) {
					BattleUtility.processTreachery(activeCharacter,theTarget);
				}
				changes = true;
				updateControls();
				repaintAll();
				return theTarget;
			}
		}
		return null;
	}
	public void makeWatchfulNatives(RealmComponent theTarget,boolean makeTargetWatchful) {
		// Check for watchful natives
		if (hostPrefs.hasPref(Constants.TE_WATCHFUL_NATIVES)) {
			if (theTarget.getOwner()!=null) return; // this doesn't apply to hirelings!!
			String targetedGroup = theTarget.getGameObject().getThisAttribute("native");
			/* Get all the unassigned natives of this group in the clearing, and
			 * make them "watchful"
			 */
			
			BattleGroup group = currentBattleModel.getDenizenBattleGroup();
			if (group!=null) {
				for (Iterator i=group.getBattleParticipants().iterator();i.hasNext();) {
					RealmComponent member = (RealmComponent)i.next();
					if (makeTargetWatchful || !member.equals(theTarget)) {
						String groupName = member.getGameObject().getThisAttribute("native");
						if (member.isNative() && groupName.equals(targetedGroup)) {
							CombatWrapper combat = new CombatWrapper(member.getGameObject());
							combat.setWatchful(true);
						}
					}
				}
				denizenPanel.repaint();
			}
		}
	}
	private void handleWatchfulNatives() {
		if (hostPrefs.hasPref(Constants.TE_WATCHFUL_NATIVES)) {
			BattleGroup group = currentBattleModel.getDenizenBattleGroup();
			if (group!=null) {
				// Determine available hirelings
				ArrayList availableHirelings = new ArrayList();
				ArrayList hirelings = getHirelings();
				for (Iterator n=hirelings.iterator();n.hasNext();) {
					RealmComponent rc = (RealmComponent)n.next();
					if (!rc.isHidden()) {
						availableHirelings.add(rc);
					}
				}
				
				// Cycle through denizens, and assign watchful natives if necessary
				for (Iterator i=group.getBattleParticipants().iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					if (!rc.isNative()) continue;
					NativeChitComponent member = (NativeChitComponent)rc;
					if (!activeCharacter.isBattling(member.getGameObject())) continue; // watchful natives only attack those they are battling
					CombatWrapper combat = new CombatWrapper(member.getGameObject());
					if (!combat.isPeaceful() && combat.isWatchful() && member.getTarget()==null && (!combat.isSheetOwner() || combat.getAttackerCount()==0)) { // unassigned and watchful
						/*
						 * Okay, now we have a watchful native, which means one of his/her group was attacked
						 * AFTER the assignment round by the activeCharacter or minion.  What should happen is:
						 * 
						 * 1)	All watchfuls attack the character, if present and unhidden
						 * 2)	If (1) fails, then they attack the character's hirelings, as determined by player
						 * 3)	If (2) fails, then nothing happens.
						 */
					 	
						if (activeCharacterIsHere && (!activeCharacter.isHidden() || unsuccessfulAmbush())) {
							// This is easy: simply assign to activeCharacter
							denizenPanel.remove(member);
							denizenPanel.repaint();
							member.setTarget(activeParticipant);
						}
						else {
							RealmComponent target = null;
							// Need to assign to hirelings, if any are present and unhidden
							if (!availableHirelings.isEmpty()) {
								// Okay, assign all watchfuls to the available hireings.  Supposed to be playing
								// character's choice...
								if (availableHirelings.size()==1) {
									// The choice is obvious
									target = (RealmComponent)availableHirelings.get(0);
								}
								else {
									// FIXME Active character decides how to distribute the watchfuls ... somehow
									// For now, just assign them randomly...
									int r = RandomNumber.getRandom(availableHirelings.size());
									target = (RealmComponent)availableHirelings.get(r);
								}
							} // else nothing!
							if (target!=null) {
								denizenPanel.remove(member);
								denizenPanel.repaint();
								member.setTarget(target);
								// Need to move target onto their own sheet....??
								BattleUtility.moveToNewSheet(target,false,true);
								target.setTarget(member);
							}
						}
					}
				}
			}
		}
	}
//	/**
//	 * This method moves the specified target to its own sheet, and deals with the old sheet, if necessary.
//	 */
//	protected void moveTargetToNewSheet(RealmComponent deployTarget,boolean keepTarget) {
//		// Move the target to its own sheet (if necessary), and handle old sheet if needed
//		CombatWrapper combat = new CombatWrapper(deployTarget.getGameObject());
//		RealmComponent deployTargetsTarget = deployTarget.getTarget();
//		if (!combat.isSheetOwner()) {
//			combat.setSheetOwner(true); // well it is NOW!
//			
//			if (deployTargetsTarget!=null && !deployTargetsTarget.isCharacter()) {
//				ArrayList attackers = currentBattleModel.getAttackersFor(deployTargetsTarget,true,false);
//				if (attackers.size()==1) { // deployTarget IS the last attacker
//					CombatWrapper dttc = new CombatWrapper(deployTargetsTarget.getGameObject());
//					
//					// Move off the sheet...
//					dttc.setSheetOwner(false);
//					
//					// ... and onto the new one as the last attacker
//					deployTargetsTarget.setTarget(deployTarget);
//				}
//			}
//		}
//		if (keepTarget) {
//			// I think the ONLY time this can happen, is when 2 T monsters hit the native... Can THAT happen?
//			if (deployTargetsTarget!=null && !deployTargetsTarget.isCharacter()) {
//				// If the deployTarget is keeping his target (RED-side monster), then the deployTargetsTarget should
//				// move off the sheet anyway, dragging its attackers with it
//				CombatWrapper dttc = new CombatWrapper(deployTargetsTarget.getGameObject());
//				dttc.setSheetOwner(false);
//			}
//		}
//		else {
//			// Clear the target
//			deployTarget.clearTarget();
//		}
//	}
	public static void makeTarget(JFrame parent,HostPrefWrapper hostPrefs,RealmComponent theAttacker,RealmComponent theTarget,GameWrapper theGame) {
		// A hidden attacker becomes unhidden on targeting (AMBUSH rules will change how this works somewhat)
		if (theAttacker.isHidden()) {
			boolean hiddenStatus = false;
			
			if (theAttacker.isCharacter() && parent!=null && hostPrefs!=null) {
				CharacterChitComponent charChit = (CharacterChitComponent)theAttacker;
				CharacterWrapper character = new CharacterWrapper(charChit.getGameObject());
				if (charChit.isMissile() && hostPrefs.hasPref(Constants.ADV_AMBUSHES)) {
					// You get an ambush roll to see if you stay hidden
					ambushRoll = DieRollBuilder.getDieRollBuilder(parent,character).createHideRoller();
					ambusher = theAttacker;
					hiddenStatus = true; // postpone until hide roll is performed
				}
			}
			if (!hiddenStatus) {
				broadcastMessage(theAttacker.getGameObject().getName(),"Becomes unhidden.");
			}
			theAttacker.setHidden(hiddenStatus);
		}
		
		CombatWrapper combat = new CombatWrapper(theTarget.getGameObject());
		combat.addAttacker(theAttacker.getGameObject());
	}
	public void replaceManeuver(int box) {
		Collection list = getAvailableManeuverOptions(box,true,false);
		
		// Find out which maneuver is already placed, and change the box
		for (Iterator i=list.iterator();i.hasNext();) {
			RealmComponent maneuver = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(maneuver.getGameObject());
			if (combat.getCombatBox()>0) {
				combat.setCombatBox(box);
			}
		}
		updateSelection();
	}
	public void playManeuver(int box) {
		// First, clear out any chits already in play for maneuver
		Collection c = activeCharacter.getActiveMoveChits();
		c.addAll(activeCharacter.getFlyChits());
		for (Iterator i=c.iterator();i.hasNext();) {
			RealmComponent chit = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(chit.getGameObject());
			if (combat.getPlacedAsMove()) {
				CombatWrapper.clearRoundCombatInfo(chit.getGameObject());
			}
		}
		
		Collection moveOptions = getAvailableManeuverOptions(box,!activeHasRedSideUpMonster());
		
		// Clear out all options already in play
		for (Iterator i=moveOptions.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			if (!rc.isActionChit() || combat.getPlacedAsMove()) {
				CombatWrapper.clearRoundCombatInfo(rc.getGameObject());
			}
		}
		
		// Select one
		if (moveOptions.size()>0) {
			boolean separateRider = false;
			if (hostPrefs.hasPref(Constants.OPT_RIDING_HORSES)) {
				BattleHorse horse  = activeCharacter.getActiveSteed();
				if (horse!=null) {
					ArrayList horseOnly = new ArrayList();
					horseOnly.add(horse);
					RealmComponent chit = playManeuver(box,horseOnly);
					if (chit==null) {
						moveOptions.clear();
					}
					separateRider = true;
					moveOptions.remove(horse);
				}
			}
			
			if (moveOptions.size()>0) {
				RealmComponent chit = playManeuver(box,moveOptions);
				if (chit!=null && separateRider) {
					positionExtra(chit,0,false,false);
				}
			}
			updateSelection();
		}
		else {
			JOptionPane.showMessageDialog(this,"There are no move options available to you.\n(Check your inventory!)","Cannot Maneuver",JOptionPane.ERROR_MESSAGE);
		}
	}
	private RealmComponent playManeuver(int box,Collection moveOptions) {
		RealmComponentOptionChooser chooser = MoveActivator.getChooserForMoveOptions(this,activeCharacter,moveOptions,true);
		chooser.setVisible(true);
		String selText = chooser.getSelectedText();
		if (selText!=null) {
			// Tag chit, so it will appear in the correct box
			RealmComponent chit = chooser.getFirstSelectedComponent();
			if (selText.endsWith(MoveActivator.FLIP_SIDE_TEXT)) {
				chit.flip();
			}
			CombatWrapper combat = new CombatWrapper(chit.getGameObject());
			if (chit instanceof MonsterMoveChitComponent) {
				// Maneuvers are stored in character object in this rare case (transmorphed character)
				combat = new CombatWrapper(activeCharacter.getGameObject());
			}
			combat.setCombatBox(box);
			combat.setPlacedAsMove(true);
			return chit;
		}
		return null;
	}
	public void replaceAttack(int box) {
		RealmComponent weapon = activeCharacter.getActiveWeapon();
		
		Collection fightOptions = getAvailableFightOptions(box,false);
		
		// Find out which piece is placed, and change the box
		for (Iterator i=fightOptions.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			if (combat.getCombatBox()>0) {
				combat.setCombatBox(box);
				if (weapon!=null) {
					combat = new CombatWrapper(weapon.getGameObject());
					combat.setCombatBox(box);
				}
			}
		}
		updateSelection();
	}
	public boolean canPlayAttack(int box) {
		CombatWrapper charCombat = new CombatWrapper(activeCharacter.getGameObject());
		GameObject go = charCombat.getCastSpell();
		SpellWrapper spell = go==null?null:new SpellWrapper(go);
		if (spell!=null) {
			return spell.isAttackSpell();
		}
		Collection fightOptions = getAvailableFightOptions(box);
		return fightOptions.size()>0;
	}
	public void playAttack(int box) {
		CombatWrapper charCombat = new CombatWrapper(activeCharacter.getGameObject());
		GameObject go = charCombat.getCastSpell();
		SpellWrapper spell = go==null?null:new SpellWrapper(go);
		if (spell!=null) {
			CombatWrapper.clearRoundCombatInfo(spell.getIncantationObject());
			
			// Can't play a normal attack if a spell was cast this round!
			
			// If the spell is an attack spell, then place it now
			if (spell.isAttackSpell()) {
				GameObject incantationObject = spell.getIncantationObject();
				CombatWrapper combat = new CombatWrapper(incantationObject);
				combat.setCombatBox(box);
				charCombat.setPlayedAttack(true);
				updateSelection();
			}
			else {
				JOptionPane.showMessageDialog(this,"There are no fight options available to you.","Cannot Attack",JOptionPane.ERROR_MESSAGE);
			}
			
			return;
		}
		
		// First, clear out any chits already in play for attack
		for (Iterator i=activeCharacter.getActiveFightChits().iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			CombatWrapper combat = new CombatWrapper(chit.getGameObject());
			if (combat.getPlacedAsFight()) {
				CombatWrapper.clearRoundCombatInfo(chit.getGameObject());
			}
		}
		
		Collection fightOptions = getAvailableFightOptions(box);
		
		// Clear out any piece already in play for attack
		for (Iterator i=fightOptions.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			if (!rc.isActionChit() || combat.getPlacedAsFight()) {
				CombatWrapper.clearRoundCombatInfo(rc.getGameObject());
			}
		}
		RealmComponent weaponCard = null;
		RealmComponent weapon = activeCharacter.getActiveWeapon();
		if (weapon!=null) {
			// Clear out weapon, if any played
			CombatWrapper.clearRoundCombatInfo(weapon.getGameObject());
		}
		else {
			// Check for Treasure Weapons (Alchemists Mixture)
			for (Iterator n=activeCharacter.getActiveInventory().iterator();n.hasNext();) {
				GameObject item = (GameObject)n.next();
				if (item.hasThisAttribute("attack")) { // ONLY the Alchemists Mixture has this, for now! - Now the Holy Hand Grenade
					weaponCard = RealmComponent.getRealmComponent(item);
				}
			}
		}
		
		if (fightOptions.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Select Attack:",true);
			int keyN = 0;
			for (Iterator i=fightOptions.iterator();i.hasNext();) {
				RealmComponent chit = (RealmComponent)i.next();
				
				// Normal Weapon
				String key = "N"+(keyN++);
				chooser.addOption(key,"");
				chooser.addRealmComponentToOption(key,chit);
				if (weapon!=null) {
					chooser.addRealmComponentToOption(key,weapon);
				}
				else if (weaponCard!=null) {
					chooser.addRealmComponentToOption(key,weaponCard);
				}
				
			}
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				charCombat.setPlayedAttack(true);
				RealmComponent chit = chooser.getFirstSelectedComponent();
				CombatWrapper combat = new CombatWrapper(chit.getGameObject());
				combat.setCombatBox(box);
				combat.setPlacedAsFight(true);
				
				if (chit instanceof MonsterFightChitComponent) {
					// Might need to place a monster part too!
					MonsterChitComponent monster = (MonsterChitComponent)RealmComponent.getRealmComponent(chit.getGameObject());
					MonsterPartChitComponent monsterWeapon = monster.getWeapon();
					if (monsterWeapon!=null) {
						if (monsterWeapon.isLightSideUp()) {
							monsterWeapon.flip();
						}
						positionExtra(monster,0,false,false);
					}
				}
				
				RealmComponent other = chooser.getLastSelectedComponent();
				if (other!=null) {
					combat = new CombatWrapper(other.getGameObject());
					combat.setCombatBox(box);
				}
			}
			else {
				charCombat.setPlayedAttack(false);
			}
			updateSelection();
		}
		else {
			JOptionPane.showMessageDialog(this,"There are no fight options available to you.","Cannot Attack",JOptionPane.ERROR_MESSAGE);
		}
	}
	public static final String[] BOX_NAME = {"Thrust/Charge","Swing/Dodge","Smash/Duck"};
	public void positionTarget(int box,ArrayList targets,boolean includeFlipside,boolean horseSameBox) {
		RealmComponent target = null;
		if (targets.size()==1) {
			target = (RealmComponent)targets.iterator().next();
		}
		else {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Select Target to Position",true);
			chooser.addRealmComponents(targets,false);
			chooser.addOption("all","ALL");
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				if ("ALL".equals(chooser.getSelectedText())) {
					for (Iterator i=targets.iterator();i.hasNext();) {
						RealmComponent rc = (RealmComponent)i.next();
						ArrayList targs = new ArrayList();
						targs.add(rc);
						positionTarget(box,targs,includeFlipside,horseSameBox); // recursive
					}
					return;
				}
				target = chooser.getFirstSelectedComponent();
			}
		}
		if (target!=null) {
			if (!positionExtra(target,box,true,horseSameBox)) {
				return;
			}
			
			CombatWrapper combat = new CombatWrapper(target.getGameObject());
			combat.setCombatBox(box);
			updateSelection();
		}
	}
	/**
	 * Select sides for the attacker being deployed (include horse if appropriate)
	 */
	public boolean selectDeploymentSide(ChitComponent attacker) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Deploy side?",true);
		String base = "n"+attacker.getGameObject().getStringId();
		chooser.addOption(base,"");
		chooser.addRealmComponentToOption(base,attacker);
		boolean includeFlipSide = true;
		if (attacker.isMonster()) { // Controlled monsters
			MonsterChitComponent monster = (MonsterChitComponent)attacker;
			if (monster.canPinOpponent() && !monster.isPinningOpponent()) {
				includeFlipSide = false;
			}
		}
		if (attacker.isTraveler()) {
			includeFlipSide = false;
		}
		if (includeFlipSide) {
			chooser.addOption(base+"f","");
			chooser.addRealmComponentToOption(base+"f",attacker,RealmComponentOptionChooser.DisplayOption.Flipside);
		}
		ChitComponent horse = (ChitComponent)attacker.getHorse();
		if (horse!=null) {
			// Add unflipped horse to both existing
			chooser.addRealmComponentToOption(base,horse);
			chooser.addRealmComponentToOption(base+"f",horse);
			
			// Add new options, with flipped horse
			chooser.addOption(base+"h","");
			chooser.addRealmComponentToOption(base+"h",attacker);
			chooser.addRealmComponentToOption(base+"h",horse,RealmComponentOptionChooser.DisplayOption.Flipside);
			chooser.addOption(base+"fh","");
			chooser.addRealmComponentToOption(base+"fh",attacker,RealmComponentOptionChooser.DisplayOption.Flipside);
			chooser.addRealmComponentToOption(base+"fh",horse,RealmComponentOptionChooser.DisplayOption.Flipside);
		}
		chooser.setVisible(true);
		String selKey = chooser.getSelectedOptionKey();
		if (selKey!=null) {
			if (selKey.endsWith("fh")) {
				attacker.flip();
				horse.flip();
			}
			else if (selKey.endsWith("h")) {
				horse.flip();
			}
			else if (selKey.endsWith("f")) {
				attacker.flip();
			}
			// else nothing
			return true;
		}
		return false;
	}
	public boolean positionExtra(RealmComponent target,int box,boolean allowCancel,boolean horseSameBox) {
		// allow current box, ONLY if target belongs to activeParticipant
		boolean allowCurrentBox = box==0;
		RealmComponent targetOwner = target.getOwner();
		if (targetOwner!=null && targetOwner.getGameObject().equals(activeCharacter.getGameObject())) {
			allowCurrentBox = true;
		}
		
		boolean includeFlipside = false;
		
		RealmComponent extra = null;
		String extraName = null;
		boolean includeCurrentBox = false;
		if (target.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)target;
			extra = monster.getWeapon();
			extraName = "monster weapon";
			if (monster.getOwnerId()!=null || monster.isAbsorbed()) { // Any type of hired or controlled monster should be able to flip its weapon anytime
				includeFlipside = true;
			}
		}
		else if (target.isNative() || target.isTraveler()) {
			extra = (RealmComponent)target.getHorse();
			extraName = "native horse";
			includeCurrentBox = true;
			if (extra!=null && horseSameBox) {
				CombatWrapper combat = new CombatWrapper(extra.getGameObject());
				combat.setCombatBox(box);
				return true;
			}
		}
		else {
			extra = target;
			extraName = "rider maneuver";
			includeCurrentBox = allowCurrentBox;
		}
		
		if (extra!=null) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Select location for "+extraName,allowCancel);
			int keyN = 0;
			int boxConstraint = target.getGameObject().getThisInt("box_constraint");
			for (int i=1;i<=3;i++) {
				if (boxConstraint==0 || i==boxConstraint) {
					if (includeCurrentBox || box!=i) {
						String key = "C"+(keyN++);
						chooser.addOption(key,"Box "+i+" ("+BOX_NAME[i-1]+")");
						chooser.addRealmComponentToOption(key,extra);
						
						if (includeFlipside) {
							chooser.addOption(key+"f","Box "+i+" ("+BOX_NAME[i-1]+")");
							chooser.addRealmComponentToOption(key+"f",extra,RealmComponentOptionChooser.DisplayOption.Flipside);
						}
					}
				}
			}
			chooser.setVisible(true);
			String text = chooser.getSelectedText();
			if (text!=null) {
				for (int i=0;i<3;i++) {
					if (text.indexOf(BOX_NAME[i])>=0) {
						if (chooser.getSelectedOptionKey().endsWith("f")) {
							extra.flip();
						}
						CombatWrapper combat = new CombatWrapper(extra.getGameObject());
						combat.setCombatBox(i+1);
						break;
					}
				}
			}
			else {
				// Cancelled
				return false;
			}
		}
		return true;
	}
	public void positionAttacker(ArrayList attackers,int box,boolean includeFlipSide,boolean horseSameBox) {
		// Native/Horse positioning
		int count = 0;
		RealmComponent lonePiece = null;
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Position which?",true);
		for (Iterator i=attackers.iterator();i.hasNext();) {
			RealmComponent attacker = (RealmComponent)i.next();
			lonePiece = attacker;
			String option = "n"+attacker.getGameObject().getStringId();
			count++;
			chooser.addOption(option,"");
			chooser.addRealmComponentToOption(option,attacker);
			if (includeFlipSide) {
				boolean okayToFlip = true;
				if (attacker.isMonster()) {
					MonsterChitComponent monster = (MonsterChitComponent)attacker;
					if (monster.canPinOpponent()) {
						// Can't intentionally flip monsters to RED side
						okayToFlip = false;
					}
				}
				
				if (okayToFlip) {
					count++;
					chooser.addOption(option+"f","");
					chooser.addRealmComponentToOption(option+"f",attacker,RealmComponentOptionChooser.DisplayOption.Flipside);
				}
			}
		}
		if (count==1) {
			if (!positionExtra(lonePiece,box,true,horseSameBox)) {
				return;
			}
			
			CombatWrapper combat = new CombatWrapper(lonePiece.getGameObject());
			combat.setCombatBox(box);
		}
		else {
			chooser.setVisible(true);
			String selKey = chooser.getSelectedOptionKey();
			if (selKey!=null) {
				RealmComponent rc = chooser.getFirstSelectedComponent();
				if (rc.isChit()) { // TODO When the heck would it ever be anything BUT a chit?  It was a TreasureCardComponent once (see BUG #1374), but how?
					ChitComponent chit = (ChitComponent)rc;
	
					if (!positionExtra(chit,box,true,horseSameBox)) {
						return;
					}
					
					if (selKey.endsWith("f")) {
						chit.flip();
					}
					
					CombatWrapper combat = new CombatWrapper(chit.getGameObject());
					combat.setCombatBox(box);
				}
			}
		}
	}
	public void endCombat() {
		CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Presses the END combat button.");
		endButton.setEnabled(false);
		nextButton.setEnabled(false);
		ArrayList playersToRespond = new ArrayList();
		Collection chars = currentBattleModel.getAllOwningCharacters();
		for (Iterator i=chars.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			if (!combat.isLockNext()) {
				CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
				String charPlayerName = character.getPlayerName();
				if (!charPlayerName.equals(playerName) && !playersToRespond.contains(charPlayerName)) {
					playersToRespond.add(charPlayerName);
				}
			}
		}
		if (playersToRespond.isEmpty()) {
			endCombatNow();
		}
		else {
			// Throw up a frame showing all players and their responses.
			endCombatFrame = new EndCombatFrame(this,playersToRespond);
			endCombatFrame.setVisible(true);
			
			// Send a yes/no query to all players
			RealmDirectInfoHolder info = new RealmDirectInfoHolder(gameData,playerName);
			info.setCommand(RealmDirectInfoHolder.QUERY_YN);
			info.setString(endCombatFrame.getId()+":"+playerName+" wants to END combat.  Do you agree?");
			for (Iterator i=playersToRespond.iterator();i.hasNext();) {
				String charPlayerName = (String)i.next();
				GameClient.GetMostRecentClient().sendInfoDirect(charPlayerName,info.getInfo());
			}
			
			updateControls();
		}
	}
	public void handleQueryResponse(RealmDirectInfoHolder info) {
		String command = info.getCommand();
		if (command.equals(RealmDirectInfoHolder.QUERY_RESPONSE)) {
			String respondingPlayer = info.getPlayerName();
			String response = info.getString();
			if (endCombatFrame!=null) {
				endCombatFrame.updateResponse(respondingPlayer,response);
			}
		}
	}
	public void cancelEndCombat() {
		endCombatFrame = null;
		updateControls();
	}
	public void endCombatNow() {
		endCombatFrame = null;
		CombatFrame.broadcastMessage(activeCharacter.getGameObject().getName(),"Combat has ended.");
		Collection chars = currentBattleModel.getAllOwningCharacters();
		for (Iterator i=chars.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			
			// Set every character in combat to skip combat, and to the next wait state
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			combat.setSkipCombat(true);
			CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
			character.setCombatStatus(RealmBattle.getNextWaitState(actionState));
		}
		RealmBattle.newClearingCombat = true;
		doNext();
	}
	public void runAway() {
		RealmComponent discoverToLeave = ClearingUtility.findDiscoverToLeaveComponent(getBattleModel().getBattleLocation(),activeCharacter);
		if (discoverToLeave!=null) {
			JOptionPane.showMessageDialog(
					this,"You are trapped in the "+discoverToLeave.getGameObject().getName()+", and cannot RUN.",
					"Trapped!",JOptionPane.PLAIN_MESSAGE,discoverToLeave.getFaceUpIcon());
			return;
		}
		
		MoveActivator activator = new MoveActivator(this);
		MoveActionResult result = activator.playedValidMoveChit("Run Away","You cannot run away, because you do not have a fast enough move to play.\n(Check your inventory!)");
		if (result!=MoveActionResult.NO_MOVE_POSSIBLE) {
			TileLocation runToClearing = chooseClearingToRunTo(activator.isFly());
			
			if (result==MoveActionResult.SUCCESSFUL) {
				activator.prepareFatigue();
				// Set the character's location between clearings (or tiles)
				doRun(runToClearing,activator.getFly(),activator.getAttackers(),activator.getSelectedMoveChit());
			}
			changes = true;
			updateControls();
		}
	}
	private TileLocation chooseClearingToRunTo(boolean isFly) {
		TileLocation runToClearing = null;
		ArrayList<TileLocation> runToClearingOptions = findClearingsToRunTo(activeCharacter,currentBattleModel.getBattleLocation(),isFly);
		if (runToClearingOptions==null) {
			clearRunaway();
			return null;
		}
		
		if (runToClearingOptions.isEmpty()) {
			// Still empty?  Get out of here.
			JOptionPane.showMessageDialog(this,"For some unknown reason, there are no clearings to run to.  This is probably a bug.","No Available Clearings",JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		// Pick a clearing to run to
		if (runToClearingOptions.size()==1) {
			runToClearing = (TileLocation)runToClearingOptions.iterator().next();
		}
		else {
			CenteredMapView.getSingleton().setMarkClearingAlertText("Run towards which clearing?");
			CenteredMapView.getSingleton().markAllClearings(false);
			for (Iterator i=runToClearingOptions.iterator();i.hasNext();) {
				TileLocation tl = (TileLocation)i.next();
				if (tl.isInClearing()) {
					ClearingDetail clearing = tl.clearing;
					clearing.setMarked(true);
				}
				else if (tl.isTileOnly()) {
					tl.tile.setMarked(true);
				}
			}
			
			TileLocationChooser clearingChooser = new TileLocationChooser(this,CenteredMapView.getSingleton(),activeCharacter.getCurrentLocation());
			clearingChooser.setVisible(true);
			CenteredMapView.getSingleton().markAllClearings(false);
			CenteredMapView.getSingleton().markAllTiles(false);
			
			runToClearing = clearingChooser.getSelectedLocation();
			if (isFly) {
				runToClearing.setFlying(true);
			}
		}
		return runToClearing;
	}
	public ArrayList<TileLocation> findClearingsToRunTo(CharacterWrapper activeCharacter,TileLocation from,boolean isFly) {
		// Find all clearing options for running to
		GameData gameData = activeCharacter.getGameObject().getGameData();
		ArrayList<TileLocation> runToClearingOptions = new ArrayList<TileLocation>();
		
		// First check the special condition that the character can walk the woods
		boolean walkingWoods = false;
		if (activeCharacter.canWalkWoods(from.tile)) {
			int ret = JOptionPane.showConfirmDialog(
					this,
					"Run into the woods?",
					"Run Away",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (ret==JOptionPane.YES_OPTION) {
				walkingWoods= true;
				runToClearingOptions.add(new TileLocation(from.tile));
			}
		}
		
		// Determine if character moved here "today"
		ArrayList moveHistory = activeCharacter.getMoveHistory();
		if (moveHistory==null) { // this happens in the combat simulator
			runToClearingOptions.add(null);
		}
		else if (isFly) {
			TileLocation bl = from;
			// Find all adjacent tiles
			for (Iterator i=bl.tile.getAllAdjacentTiles().iterator();i.hasNext();) {
				TileComponent atile = (TileComponent)i.next();
				TileLocation tl = new TileLocation(atile,true);
				runToClearingOptions.add(tl);
			}
		}
		else if (!walkingWoods) {
			String lastMoveAction = (String)moveHistory.get(moveHistory.size()-1); // moveHistory should NEVER be empty
			if (!CharacterWrapper.MOVE_HISTORY_DAY.equals(lastMoveAction)) { // If no moves today, then the lastMoveAction==MOVE_HISTORY_DAY
				// Now we need to determine from *where* by searching entire moveHistory backwards
				// Since partway moves are recorded here, we simply need to find the next move back
				// that is not a MOVE_HISTORY_DAY.  MOVE_HISTORY_JUMP should stop the search
				String previousMoveAction = null;
				for (int n = moveHistory.size() - 2 ; n >= 0 ; n --) {
					String moveAction = (String)moveHistory.get(n);
					if (CharacterWrapper.MOVE_HISTORY_JUMP.equals(moveAction)) {
						break;
					}
					else if (!CharacterWrapper.MOVE_HISTORY_DAY.equals(moveAction)) {
						TileLocation movedFrom = TileLocation.parseTileLocation(gameData,moveAction);
						if (movedFrom.isInClearing()) {
							if (!movedFrom.equals(from)) { // Only consider OTHER clearings
								previousMoveAction = moveAction;
								break;
							}
						}
						else if (movedFrom.isBetweenClearings()) {
							// This proves that you left the clearing, even if by running.
							previousMoveAction = moveAction;
							break;
						}
					}
				}
				if (previousMoveAction!=null) {
					// This determines the only clearing you can run to, because you moved here today
					TileLocation movedFrom = TileLocation.parseTileLocation(gameData,previousMoveAction);
					if (movedFrom.isBetweenClearings()) {
						// If the most recent move out of the clearing was a run-away, then we'll have this condition.
						// To resolve it, simply choose the clearing that is NOT the current one, and all is good.
						ClearingDetail clearing = movedFrom.clearing.equals(from.clearing)?movedFrom.getOther().clearing:movedFrom.clearing;
						movedFrom = clearing.getTileLocation();
					}
					
					// However, it is possible that the path from which you used no longer exists, in which
					// case you cannot run.  For an example, take a look at Deep Woods 1 and the edge.
					PathDetail path = movedFrom.clearing.getConnectingPath(from.clearing);
					if (path!=null) {
						if (activeCharacter.validPath(path)) {
							runToClearingOptions.add(movedFrom);
						}
						else {
							JOptionPane.showMessageDialog(this,"The path you took to get here is now an undiscovered path, and you cannot run.","Path Gone",JOptionPane.WARNING_MESSAGE);
							return null;
						}
					}
					else {
						JOptionPane.showMessageDialog(this,"The path you took to get here is gone, and you cannot run.","Path Gone",JOptionPane.WARNING_MESSAGE);
						return null;
					}
				}
			}
		}
		if (runToClearingOptions.isEmpty()) {
			// what ARE the choices?
			for (Iterator i=activeCharacter.findAvailableClearingMoves(true).iterator();i.hasNext();) {
				ClearingDetail clearing = (ClearingDetail)i.next();
				TileLocation tl = new TileLocation(clearing);
				runToClearingOptions.add(tl);
			}
		}
		return runToClearingOptions;
	}
	/**
	 * Finishes the run activity after going through the choices
	 */
	private boolean doRun(TileLocation runToClearing,Fly fly,ArrayList attackers,RealmComponent moveChit) {
		// If ran away, then there are no attackers anymore.
		for (Iterator i=attackers.iterator();i.hasNext();) {
			RealmComponent attacker = (RealmComponent)i.next();
			CombatWrapper combat = new CombatWrapper(attacker.getGameObject());
			combat.setWatchful(false); // just in case they were watchful - shouldn't be anymore.
			attacker.clearTarget();
		}
		
		// Be sure to abandon heavy stuff
		if (fly!=null) {
			ArrayList<GameObject> toDrop = RealmUtility.dropNonFlyableStuff(this,activeCharacter,fly,currentBattleModel.getBattleLocation());
			if (toDrop==null) {
				JOptionPane.showMessageDialog(this,"Cancelled run action.","Run Away",JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			for (GameObject item:toDrop) {
				broadcastMessage(activeCharacter.getGameObject().getName(),item.getName()+" was left behind!");
			}
		}
		
		ClearingUtility.moveToLocation(activeCharacter.getGameObject(),runToClearing,true);
		activeCharacter.addMoveHistory(activeCharacter.getCurrentLocation());
		
		if (moveChit instanceof CharacterActionChitComponent) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)moveChit;
			if (chit.expireMoveSpells()) {
				activeCharacter.updateChitEffects();
			}
		}
		
		if (fly!=null) {
			fly.useFly();
		}
		
		broadcastMessage(activeCharacter.getGameObject().getName(),"Ran away!");
		
		// All following hirelings need to remain behind
		TileLocation battleLocation = getBattleModel().getBattleLocation();
		for (Iterator i=activeCharacter.getFollowingHirelings().iterator();i.hasNext();) {
			RealmComponent hireling = (RealmComponent)i.next();
			battleLocation.clearing.add(hireling.getGameObject(),null);
			if (hireling.getGameObject().hasThisAttribute(Constants.CAPTURE)) {
				activeCharacter.removeHireling(hireling.getGameObject());
				RealmLogging.logMessage(activeCharacter.getGameObject().getName(),"The "+hireling.getGameObject().getName()+" escaped!");
			}
		}
		
		CombatWrapper.clearAllCombatInfo(activeCharacter.getGameObject());
		
		// Need to disengage any participants who are targeting the runner!
		for (Iterator i=currentBattleModel.getAllBattleParticipants(true).iterator();i.hasNext();) {
			RealmComponent bp = (RealmComponent)i.next();
			RealmComponent bpTarget = bp.getTarget();
			if (bpTarget!=null && bpTarget.equals(activeParticipant)) {
				bp.clearTarget();
			}
		}
		
		// Also, disengage the runner
		activeParticipant.clearTarget();
//		activeCharacter.clearCombat(); // can't clear out combat status: messes up hirelings left behind
		
		// Need to check battle model - if nobody is left in the clearing to fight, things should get reset
		RealmBattle.testCombatInClearing(currentBattleModel.getBattleLocation(),gameData);
		
		return true;
	}
	public void moveOnIfNoBattle() {
		if (!RealmBattle.testCombatInClearing(currentBattleModel.getBattleLocation(),gameData)) {
			endCombatNow();
		}
	}
	public void alert() {
		// Verify that the character has an active weapon or berserk chit to alert
		WeaponChitComponent weapon = activeCharacter.getActiveWeapon();
		
		// Find fastest attacker move speed on your sheet
		MoveActivator activator = new MoveActivator(this);
		Speed fastest = activator.getFastestAttackerMoveSpeed();
		
		// Find all playable options
		Collection fightAlertChits = activeCharacter.getActiveFightAlertChits(fastest);
		if (weapon==null && fightAlertChits.isEmpty()) {
			JOptionPane.showMessageDialog(this,"You have nothing to Alert or Unalert.","Alert/Berserk",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Collection fightSpeedOptions = activeCharacter.getFightSpeedOptions(fastest,true);
		Collection availableFightOptions = getAvailableFightOptions(0);
		fightSpeedOptions.retainAll(availableFightOptions); // Intersection between the two
		if (fightSpeedOptions.size()>0 || fightAlertChits.size()>0) {
			// Choose one
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Select an option:",true);
			int keyN = 0;
			// Weapon alert options
			if (weapon!=null) {
				String string = weapon.isAlerted()?"Unalert":"Alert";
				for (Iterator i=fightSpeedOptions.iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					String key = "C"+(keyN++);
					chooser.addOption(key,string);
					chooser.addRealmComponentToOption(key,rc);
					chooser.addRealmComponentToOption(key,weapon);
				}
			}
			// Chit alert options (BERSERK)
			for (Iterator i=fightAlertChits.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				String key = "C"+(keyN++);
				chooser.addOption(key,StringUtilities.capitalize(rc.getGameObject().getThisAttribute("action")));
				chooser.addRealmComponentToOption(key,rc);
			}
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				RealmComponent fightToPlay = chooser.getFirstSelectedComponent();
				
				CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
				combat.addUsedChit(fightToPlay.getGameObject());
				
				if (weapon!=null && chooser.getSelectedComponents().size()==2) {
					// Alert or unalert the weapon
					weapon.setAlerted(!weapon.isAlerted());
					String word = weapon.isAlerted()?"Alerts":"Unalerts";
					broadcastMessage(activeCharacter.getGameObject().getName(),word+" the "+weapon.getGameObject().getName());
				}
				else {
					// Kinda HAS to be the BERSERK chit here, because nothing else applies!
					if (fightToPlay.isActionChit()) {
						CharacterActionChitComponent chit = (CharacterActionChitComponent)fightToPlay;
						if (chit.isFightAlert()) {
							broadcastMessage(activeCharacter.getGameObject().getName(),chit.getGameObject().getThisAttribute("action").toUpperCase()+"!");
							broadcastMessage(activeCharacter.getGameObject().getName(),"New vulnerability: "+chit.getFightAlertVulnerability());
							activeCharacter.getGameObject().setThisAttribute(Constants.ENHANCED_VULNERABILITY,chit.getFightAlertVulnerability());
						}
					}
				}
				changes = true;
				updateControls();
			}
		}
		else {
			JOptionPane.showMessageDialog(this,"You cannot do this, because you do not have a fast enough fight to play.","Alert/Berserk",JOptionPane.INFORMATION_MESSAGE);
		}
	}
	public void castSpell() {
		Collection castableSpellSets = activeCharacter.getCastableSpellSets();
		if (castableSpellSets.size()>0) {
			// Find fastest attacker move speed
			MoveActivator activator = new MoveActivator(this);
			Speed fastest = activator.getFastestAttackerMoveSpeed();
			
			// create a hash list of spells (filtering on speed)
			Hashtable spellHash = new Hashtable();
			HashLists spellSetHashlists = new HashLists();
			for (Iterator i=castableSpellSets.iterator();i.hasNext();) {
				SpellSet set = (SpellSet)i.next(); // by definition, the set is castable, but we need to test the speed
				// Speed must be equal to or faster than all move speeds on sheet
				set.filterSpeed(fastest);
				if (set.canBeCast()) {
					spellHash.put(set.getSpell().getName(),set.getSpell());
					spellSetHashlists.put(set.getSpell().getName(),set);
				}
			}
			
			if (spellSetHashlists.size()>0) {
				// First, choose a spell (keeps the choices minimal)
				RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Cast a Spell:",true);
				chooser.addGameObjects(spellHash.values(),false);
				chooser.setVisible(true);
				if (chooser.getSelectedText()!=null) {
					SpellWrapper spell = new SpellWrapper(chooser.getFirstSelectedComponent().getGameObject());
					ArrayList list = spellSetHashlists.getList(spell.getGameObject().getName());
					chooser = new RealmComponentOptionChooser(this,"Choose Casting Options for "+spell.getName()+":",true);
					// Then choose a set
					Hashtable setHash = new Hashtable();
					int keyN = 0;
					for (Iterator i=list.iterator();i.hasNext();) {
						SpellSet set = (SpellSet)i.next(); // by definition, the set is castable
						
						for (Iterator t=set.getValidTypeObjects().iterator();t.hasNext();) {
							GameObject type = (GameObject)t.next();
							if (set.getInfiniteSource()!=null) {
								// No need to pick a color chit
								String key = "P"+(keyN++);
								chooser.addOption(key,"");
								chooser.addRealmComponentToOption(key,RealmComponent.getRealmComponent(type));
								setHash.put(key, set);
							}
							else {
								// add options for every color chit (will this be too much??)
								for (MagicChit chit:set.getValidColorChits()) {
									String key = "P"+(keyN++);
									chooser.addOption(key,"");
									chooser.addRealmComponentToOption(key,RealmComponent.getRealmComponent(type));
									chooser.addRealmComponentToOption(key,(RealmComponent)chit);
									setHash.put(key, set);
								}
							}
						}
					}
					chooser.setVisible(true);
					if (chooser.getSelectedText()!=null) {
						// Make sure we get the spell from the correct set!
						String key = chooser.getSelectedOptionKey();
						SpellSet set = (SpellSet)setHash.get(key);
						spell = new SpellWrapper(set.getSpell());
						
						// CAST THE SPELL
						CombatWrapper combat = new CombatWrapper(activeCharacter.getGameObject());
						Collection c = chooser.getSelectedComponents();
						Iterator i=c.iterator();
						// Magic chits/treasure fatigue at the end of the spell, so tie it to spell here (both ways)
						RealmComponent incantationComponent = (RealmComponent)i.next();
						
						if (!incantationComponent.isActionChit()) {
							// If it's anything but an action chit, then tag it as having been used this evening
							
							// Before we do, however, check and see if there is already an older entry
							String dayKey = activeCharacter.getCurrentDayKey();
							String usedSpell = incantationComponent.getGameObject().getThisAttribute(Constants.USED_SPELL);
							if (usedSpell!=null && !usedSpell.equals(dayKey)) {
								// Yep, there is.  Clear out the used magic type list
								incantationComponent.getGameObject().removeThisAttribute(Constants.USED_MAGIC_TYPE_LIST);
							}
							
							// Now we can mark the artifact used for this magic type
							incantationComponent.getGameObject().setThisAttribute(Constants.USED_SPELL,dayKey);
							incantationComponent.getGameObject().addThisAttributeListItem(Constants.USED_MAGIC_TYPE_LIST,set.getCastMagicType());
						}
						
						GameObject incantationObject = incantationComponent.getGameObject();
						
						spell = spell.castSpell(incantationObject); // spell object might change if using Enhanced Magic
						if (spell.getGameObject().hasThisAttribute("table")) {
							// Does caster have Magic Wand?
							GameObject go  = activeCharacter.getActiveInventoryThisKey(Constants.RED_DIE);
							String redDie = go==null?null:go.getThisAttribute(Constants.RED_DIE);
							String spellKey = spell.getGameObject().getThisAttribute("table");
							if (redDie!=null && (redDie.indexOf(spellKey)>=0)) {
								int n = SpellUtility.chooseRedDie(this,spellKey,activeCharacter);
								spell.setRedDieLock(n);
							}
							else {
								spell.setRedDieLock(-1); // this value prevents the spell from being modified by someone ELSE with the wand
							}
						}
						if (i.hasNext()) {
							// Color chits fatigue when they are used (if any was used)
							MagicChit colorChit =  (MagicChit)i.next();
							colorChit.makeFatigued();
							RealmUtility.reportChitFatigue(activeCharacter,colorChit,"Fatigued color chit: ");
						}
						combat.setCastSpell(spell.getGameObject());
						changes = true;
						updateControls();
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(this,"There are no spellcasting options available to you.","Cannot Cast Spell",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	public void activateInactivate() {
		if (activeCharacter.getInventory().size()==0) {
			JOptionPane.showMessageDialog(this,"You have no inventory to activate or inactivate!","Activate/Inactivate",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		/*
		 * Show one dialog with all activate and inactivate options.  Don't do any automatic inactivating (like for weapons
		 * and armor).  Simply exclude those options from the dialog.
		 * 
		 * A second dialog is shown with options opposite whatever the character chose (ie., only activate, if the character
		 * chose an inactivate).  Again, impossible options are excluded.
		 * 
		 * Cancel is provided on both dialogs, which cancel any action here.  A "Skip this Round" button allows you to
		 * skip a dialog (and lose it for good!)
		 */
		
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Select an option below:",true);
		int keyN = 0;
		for (Iterator i=activeCharacter.getInactiveInventory(true).iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			String key = "C"+(keyN++);
			chooser.addOption(key,"Activate");
			chooser.addRealmComponentToOption(key,rc);
		}
		for (Iterator i=activeCharacter.getActiveInventory().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			Inventory inv = new Inventory(go);
			if (inv.canDeactivate()) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				String key = "C"+(keyN++);
				chooser.addOption(key,"Inactivate");
				chooser.addRealmComponentToOption(key,rc);
			}
		}
		chooser.setVisible(true);
		String selText = chooser.getSelectedText();
		if (selText!=null) {
			boolean activate = "Activate".equals(selText);
			RealmComponent rcToActivate = activate?chooser.getFirstSelectedComponent():null;
			RealmComponent rcToInactivate = activate?null:chooser.getFirstSelectedComponent();
			chooser = new RealmComponentOptionChooser(this,"Select an option below:",true);
			Collection c = activate?activeCharacter.getActiveInventory():activeCharacter.getInactiveInventory();
			if (c.size()>0) {
				for (Iterator i=c.iterator();i.hasNext();) {
					GameObject go = (GameObject)i.next();
					RealmComponent rc = RealmComponent.getRealmComponent(go);
					String key = "C"+(keyN++);
					chooser.addOption(key,activate?"Inactivate":"Activate");
					chooser.addRealmComponentToOption(key,rc);
				}
				chooser.addOption("nothing","Skip "+(activate?"Inactivate":"Activate"));
				chooser.setVisible(true);
				selText = chooser.getSelectedText();
				if (selText!=null) {
					if (!selText.startsWith("Skip")) {
						if (activate) {
							rcToInactivate = chooser.getFirstSelectedComponent();
						}
						else {
							rcToActivate = chooser.getFirstSelectedComponent();
						}
					}	
				}
				else {
					JOptionPane.showMessageDialog(this,"Cancelled action.  No inventory was activated/inactivated.","Activate/Inactivate Cancelled",JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			// do it!
			if (rcToActivate!=null) {
				broadcastMessage(activeCharacter.getGameObject().getName(),"Activates the "+rcToActivate.getGameObject().getName());
				if (rcToActivate.getGameObject().hasThisAttribute(Constants.AUTO_FLEE)) {
					Fly fly = activeCharacter.mustFly()?(new Fly()):null;
					TileLocation runToClearing = chooseClearingToRunTo(fly!=null);
					if (!doRun(runToClearing,fly,currentBattleModel.getAttackersFor(activeParticipant),null)) {
						JOptionPane.showMessageDialog(this,"Cancelled action.  No inventory was activated/inactivated.","Activate/Inactivate Cancelled",JOptionPane.INFORMATION_MESSAGE);
						broadcastMessage(activeCharacter.getGameObject().getName(),"Unable to activate the "+rcToActivate.getGameObject().getName());
						return;
					}
				}
				TreasureUtility.doActivate(this,activeCharacter,rcToActivate.getGameObject(),combatListener,true);
			}
			if (rcToInactivate!=null) {
				broadcastMessage(activeCharacter.getGameObject().getName(),"Inactivates the "+rcToInactivate.getGameObject().getName());
				TreasureUtility.doDeactivate(this,activeCharacter,rcToInactivate.getGameObject());
			}
			changes = true;
			updateControls();
		}
		else {
			JOptionPane.showMessageDialog(this,"Cancelled action.  No inventory was activated/inactivated.","Activate/Inactivate Cancelled",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	}
	private void pickupItem() {
		TileLocation current = activeCharacter.getCurrentLocation();
		if (current.isInClearing()) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(this,"Choose an item to pick up:",true);
			ArrayList<RealmComponent> list = current.clearing.getClearingComponents(false);
			for(RealmComponent item:list) {
				if (item.isPlainSight()) {
					String text = item.isAtYourFeet(activeCharacter)?"":"Req MOVE";
					chooser.addRealmComponent(item,text);
				}
			}
			if (chooser.hasOptions()) {
				chooser.setVisible(true);
				String option = chooser.getSelectedText();
				if (option!=null) {
					RealmComponent rc = chooser.getFirstSelectedComponent();
					if (option.endsWith("MOVE")) {
						MoveActivator activator = new MoveActivator(this);
						MoveActionResult result = activator.playedValidMoveChit("Pick Up Item","You cannot pick up an item not at your feet, because you do not have a fast enough move to play.\n(Check your inventory!)");
						if (result!=MoveActionResult.SUCCESSFUL) {
							return;
						}
						activator.prepareFatigue();
					}
					rc.getGameObject().removeThisAttribute(Constants.PLAIN_SIGHT);
					rc.getGameObject().removeThisAttribute(Constants.DROPPED_BY);
					
					Loot.addItemToCharacter(this,combatListener,activeCharacter,rc.getGameObject(),hostPrefs);
					broadcastMessage(activeCharacter.getGameObject().getName(),"Picks up the "+rc.getGameObject().getName());
					
					changes = true;
					updateControls();
				}
			}
			else {
				JOptionPane.showMessageDialog(this,"There are no items to pick up!","Pickup Item",JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	public void abandonBelongings(boolean plainSight) {
		String title = (plainSight?"Drop":"Abandon")+" Belongings";
		RealmObjectChooser chooser = new RealmObjectChooser(title,gameData,false);
		
		ArrayList list = new ArrayList();
		for (Iterator i=activeCharacter.getInventory().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isItem()) { // not a boon or phase chit!
				list.add(go);
			}
		}
		
		chooser.addObjectsToChoose(list);
		chooser.setVisible(true);
		
		if (chooser.pressedOkay()) {
			TileLocation current = activeCharacter.getCurrentLocation();
			Collection toDrop = chooser.getChosenObjects();
			for (Iterator i=toDrop.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				
				boolean dropOkay = true;
				// Deactivate first (but only if activated!)
				if (go.hasThisAttribute(Constants.ACTIVATED)) {
					dropOkay = TreasureUtility.doDeactivate(this,activeCharacter,go);
				}
				if (dropOkay) {
					// Then drop
					current.clearing.add(go,plainSight?activeCharacter:null);
					broadcastMessage(activeCharacter.getGameObject().getName(),title+"s the "+go.getName());
				}
			}
			changes = true;
			updateControls();
		}
		else {
			JOptionPane.showMessageDialog(this,"Cancelled action.  No inventory was dropped.",title,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	}
	/**
	 * @return Returns the activeParticipant.
	 */
	public RealmComponent getActiveParticipant() {
		return activeParticipant;
	}
	/**
	 * @return Returns the hirelings for the active participant
	 */
	public ArrayList getHirelings() {
		BattleGroup battleGroup = currentBattleModel.getBattleGroup(activeParticipant);
		ArrayList hirelings = new ArrayList();
		if (battleGroup!=null) {
			hirelings.addAll(battleGroup.getHirelings());
		}
		return hirelings;
	}
	public ArrayList<RealmComponent> getAllParticipants() {
		return allParticipants;
	}
	public int getAllParticipantCount() {
		return allParticipants.size();
	}
	public boolean getParticipantHasHotspots(int row) {
		return participantHasHotspots[row];
	}
	public ArrayList<CombatSheet> getAllCombatSheets() {
		ArrayList<CombatSheet> sheets = new ArrayList<CombatSheet>();
		// Actually creates them
		for (RealmComponent rc:allParticipants) {
			sheets.add(CombatSheet.createCombatSheet(this,currentBattleModel,rc,interactiveFrame));
		}
		return sheets;
	}
	/**
	 * @return Returns the actionState.
	 */
	public int getActionState() {
		return actionState;
	}
	private void repaintAll() {
		denizenScroll.setVisible(denizenPanel.getComponentCount()>0);
		denizenPanel.revalidate();
		denizenPanel.repaint();
		refreshParticipants();
		repaint();
	}
	private synchronized static void doFatigueWounds(JFrame parent,CharacterWrapper character) {
		CombatWrapper combat = new CombatWrapper(character.getGameObject());
		
		int healing = combat.getHealing(); // i.e., Drain Life
		if (healing>0) {
			broadcastMessage(character.getGameObject().getName(),"Healing "+healing+" asterisk"+(healing==1?"":"s")+".");
			ChitRestManager rester = new ChitRestManager(parent,character,healing);
			rester.setVisible(true);
		}
		
		int newWounds = combat.getNewWounds();
		if (newWounds>0) {
			broadcastMessage(character.getGameObject().getName(),"Wounding "+newWounds+" chit"+(newWounds==1?"":"s")+".");
		}
		Effort effortUsed = BattleUtility.getEffortUsed(character);
		int free = character.getEffortFreeAsterisks();
		int needToFatigue = effortUsed.getNeedToFatigue(free);
		needToFatigue += runAwayFatigue;
		if (needToFatigue>0) {
			broadcastMessage(character.getGameObject().getName(),"Fatiguing "+needToFatigue+" asterisk"+(needToFatigue==1?"":"s")+".");
		}
		
		boolean dead = false;
		if (needToFatigue>0) {
			int runFatigueUsed = runAwayFatigue==0?0:runAwayFatigue+character.getEffortFreeAsterisks();
			ChitFatigueManager fatiguer = new ChitFatigueManager(parent,character,needToFatigue,effortUsed.getMoveAsterisks()+runFatigueUsed,effortUsed.getFightAsterisks(),0);
			fatiguer.setVisible(true);
			// Test for death
			if (fatiguer.isDead()) {
				dead = true;
			}
		}
		if (!dead && newWounds>0) {
			ChitWoundManager wounder = new ChitWoundManager(parent,character,newWounds);
			wounder.setVisible(true);
			
			// Test for death
			if (wounder.isDead()) {
				dead = true;
			}
			else if (character.hasHealing()) {
				// Convert wounds to fatigue, thanks to vial of healing
				character.doHealWoundsToFatigue();
			}
		}
		
		if (dead) {
			ArrayList list = combat.getHitByList();
			GameObject lastKiller = (GameObject)list.get(list.size()-1);
			combat.setKilledBy(lastKiller);
			RealmComponent rc = RealmComponent.getRealmComponent(character.getGameObject());
			JOptionPane.showMessageDialog(parent,character.getCharacterName()+" was wounded to death.","Wounded to Death",JOptionPane.INFORMATION_MESSAGE,rc.getIcon());
		}
	}
	/**
	 * Does the right thing to handle the current combat stage
	 * 
	 * @return		true on success
	 */
	public static boolean doDisplayInteractive(JFrame parent,GameData data,String playerName,ActionListener listener,boolean local,boolean isHost) {
		return doDisplay(parent,data,playerName,listener,true,local,isHost);
	}
	public static boolean doDisplayObserving(JFrame parent,GameData data,String playerName,ActionListener listener,boolean local,boolean isHost) {
		return doDisplay(parent,data,playerName,listener,false,local,isHost);
	}
	/**
	 * @param interactive			If false, then this is observation mode.
	 */
	private static boolean doDisplay(JFrame parent,GameData data,String playerName,ActionListener listener,boolean interactive,boolean local,boolean isHost) {
		logger.fine("Display new combat frame");
		
		isGameHost = isHost;
		
		if (interactive && !local) {
			SoundUtility.playAttention();
		}
		
		interactiveFrame = interactive;
		if (!interactiveFrame) {
			CombatFrame frame = CombatFrame.getSingleton(parent,playerName,listener);
			frame.updateFrame(data);
			frame.updateControls();
			frame.setVisible(true);
			
			// Test combat - if none, then endcombat now!!
			frame.moveOnIfNoBattle();
			
			return true;
		}
		
		// ALWAYS show the combat frame
		CombatFrame frame = CombatFrame.getSingleton(parent,playerName,listener);
		frame.updateFrame(data);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				singleton.setVisible(true);
				FrameManager.refreshAll(); // just in case
			}
		});
		
		HashLists lists = RealmBattle.currentCombatHashLists(data);
		Integer firstState = null;
		if (lists!=null && !lists.isEmpty()) {
			ArrayList states = new ArrayList(lists.keySet());
			Collections.sort(states);
			firstState = (Integer)states.iterator().next();
		}
		else {
			// This can happen when the character gets a "I wish I were elsewhere" result on the WISH table
			
			// But what to do here?
			return false;
		}
		if (firstState.intValue()==Constants.COMBAT_PREBATTLE) {
			logger.finer("handling prebattle");
			ArrayList list = lists.getList(firstState);
			CharacterWrapper character = (CharacterWrapper)list.iterator().next();
			processBattlingNatives(frame,character,data);
			character.setCombatStatus(Constants.COMBAT_WAIT+Constants.COMBAT_LURE);
			listener.actionPerformed(new ActionEvent(parent,0,"")); // does the submit in RealmSpeak
		}
		else if (firstState.intValue()==Constants.COMBAT_FATIGUE) {
			logger.finer("handling fatigue/wounds");
			ArrayList list = lists.getList(firstState);
			CharacterWrapper character = (CharacterWrapper)list.iterator().next();
			doFatigueWounds(frame,character);
			character.setCombatStatus(Constants.COMBAT_WAIT+Constants.COMBAT_DISENGAGE);
			listener.actionPerformed(new ActionEvent(parent,0,"")); // does the submit in RealmSpeak
		}
		logger.fine("***** Done Display new combat frame");
		return true;
	}
	private static void processBattlingNatives(JFrame parent,CharacterWrapper character,GameData data) {
		TileLocation current = RealmBattle.getCurrentCombatLocation(data);
		HashLists unhiredNatives = RealmUtility.getUnhiredNatives(current.clearing.getClearingComponents());
		if (unhiredNatives.size()>0) {
			for (Iterator i=unhiredNatives.keySet().iterator();i.hasNext();) {
				String groupName = (String)i.next();
				String capGroupName = StringUtilities.capitalize(groupName);
				ArrayList list = unhiredNatives.getList(groupName);
				
				RealmComponent firstNative  = (RealmComponent)list.get(0);
				Meeting meeting = Meeting.createMeetingTable(parent,character,current,firstNative,null,null,RelationshipType.FRIENDLY);
				
				ArrayList rolls = new ArrayList();
				while(meeting!=null) {
					
					DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,character).createRoller(meeting.getTableKey(),current);
					String message = meeting.apply(character,roller);
					
					rolls.add(new RollerResult("Roll "+(rolls.size()+1)+": "+meeting.getMeetingTableName(),roller.getStringResult(),message));
					broadcastMessage(
							character.getGameObject().getName(),
							"Meeting "
							+capGroupName+" on the "
							+meeting.getMeetingTableName()+" table");
					if (meeting.isBlockBattle()) {
						// Battling!  Add all rolls
						for (int n=0;n<rolls.size();n++) {
							RollerResult rr = (RollerResult)rolls.get(n);
							character.addBattlingNativeRoll(groupName,rr.getResult(),rr.getSubtitle());
						}
						
						character.changeRelationship(firstNative.getGameObject(),0); // This is needed so that natives starting a fight don't affect the relationship with extended grudges
						
						// Show all roll results
						RollerResults rrPanel = new RollerResults();
						rrPanel.setBattleRolls(rolls);
						JPanel messagePanel = new JPanel(new BorderLayout());
						messagePanel.add(rrPanel,"West");
						messagePanel.add(new JLabel("The "+capGroupName+" battle the "+character.getGameObject().getName()+"!"),"Center");
						
						JOptionPane.showMessageDialog(
								parent,
								messagePanel,
								character.getGameObject().getName()+" vs "+capGroupName,
								JOptionPane.INFORMATION_MESSAGE);
					}
					broadcastMessage(
							character.getGameObject().getName(),
							roller.getDescription(false)
							+" = "
							+message);
					meeting = (Meeting)meeting.getNewTable();
				}
			}
		}
	}
	private static final String testPlayerName = "test";
	private static final String LAST_SAVE_LOCATION = "lastSaveLocation";
	public static void main(String[] args) {
		closeableFrame = true;
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.findImagesFolderOrExit();
		RealmCharacterBuilderModel.loadAllCustomCharacters();
		prefMan = RealmUtility.getRealmBattlePrefs();
		if (prefMan.canLoad()) {
			prefMan.loadPreferences();
		}
		String val = prefMan.get(LAST_SAVE_LOCATION);
		if (val!=null) {
			lastSaveGame = new File(val);
		}
		
		// Load the chit style prefs based on the last RealmSpeak game
		PreferenceManager gamePrefMan = RealmUtility.getRealmSpeakPrefs();
		if (gamePrefMan.canLoad()) {
			gamePrefMan.loadPreferences();
		}
		switch(gamePrefMan.getInt("chitDisplayStyle")) {
			case RealmComponent.DISPLAY_STYLE_CLASSIC:
				RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_CLASSIC;
				break;
			case RealmComponent.DISPLAY_STYLE_COLOR:
				RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_COLOR;
				break;
			case RealmComponent.DISPLAY_STYLE_FRENZEL:
				RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_FRENZEL;
				break;
			default:
				RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_CLASSIC;
				break;
		}
		
		RealmUtility.setupTextType();
		DebugUtility.setupArgs(args);
		LoggingHandler.initLogging();
		FlashingButton.setFlashEnabled(false); // don't use flashing on combat emulator
		
		// Find out what the player wants to do.
		ButtonOptionDialog dialog = new ButtonOptionDialog(new JFrame(),IconFactory.findIcon("images/logo/realmbox.jpg"),"Version "+Constants.REALM_SPEAK_VERSION,"RealmSpeak Battle Builder",false);
		dialog.setButtonFont(new Font("Dialog",Font.PLAIN,18));
		dialog.addSelectionObject("New Battle");
		dialog.addSelectionObject("Edit Battle");
		dialog.addSelectionObject("Play Battle");
		dialog.addSelectionObject("Exit");
		dialog.setVisible(true);
		String result = (String)dialog.getSelectedObject();
		
		if (result!=null) {
			if (result.equals("New Battle")) {
				BattleBuilder.constructBattleSituation();
			}
			else if (result.equals("Edit Battle")) {
				GameData loadedData = loadBattleSituation();
				if (loadedData==null) {
					close();
				}
				BattleBuilder.constructBattleSituation(loadedData);
			}
			else if (result.equals("Play Battle")) {
				GameData loadedData = loadBattleSituation();
				startCombat(loadedData);
			}
			else {
				DebugUtility.shutDown();
				System.exit(0);
			}
		}
		else {
			// This should never happen
			DebugUtility.shutDown();
			System.exit(0);
		}
	}
	private static GameData realmBattleData;
	private static ActionListener finishListener;
	public static void startCombat(GameData loadedData) {
		final JFrame dummyFrame = new JFrame();
		dummyFrame.setTitle("dummyFrame");
		if (loadedData!=null) {
			RealmUtility.resetGame();
			if (lastSaveGame!=null) {
				prefMan.set(LAST_SAVE_LOCATION,lastSaveGame.getAbsolutePath());
				prefMan.savePreferences();
			}
			loadedData.setDataName(DATA_NAME);
			loadedData.setTracksChanges(true);
			realmBattleData = loadedData;
			
			GameWrapper game = GameWrapper.findGame(realmBattleData);
			
			// Initialize combat
			RealmBattle.resetCombat(realmBattleData);
			RealmBattle.initCombatOrder(realmBattleData);
			SpellMasterWrapper.getSpellMaster(realmBattleData).energizePermanentSpells(dummyFrame,game);
			
			// Setup listener
			finishListener = new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					if (RealmBattle.nextCombatAction(null,realmBattleData)) {
						realmBattleData.commit(); // commit right before display, so that reset works.
						CombatFrame.doDisplayInteractive(dummyFrame,realmBattleData,testPlayerName,this,true,true); // recursive! (sort of...)
					}
					else {
						JOptionPane.showMessageDialog(null,"Combat has Ended");
						if (isSingletonShowing()) {
							getShowingSingleton().setVisible(false);
						}
						close();
					}
				}
			};
			
			// Start the combat cycle
			finishListener.actionPerformed(new ActionEvent("",0,""));
		}
		else {
			close();
		}
	}
	public static void close() {
		DebugUtility.shutDown();
		RealmUtility.resetGame();
		String[] args = new String[0];
		main(args);
	}
	private static GameData loadBattleSituation() {
		return loadBattle(new JFrame());
	}
	protected static PreferenceManager prefMan;
	protected static File lastSaveGame = null;
	protected static FileFilter saveGameFileFilter = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory() || (f.isFile() && f.getPath().endsWith(".battle"));
		}

		public String getDescription() {
			return "RealmSpeak Battle Files (*.battle)";
		}
	};
	private static GameData loadBattle(JFrame frame) {
		JFileChooser chooser;
		if (lastSaveGame!=null) {
			String filePath = FileUtilities.getFilePathString(lastSaveGame,false,false);
			chooser = new JFileChooser(new File(filePath));
		}
		else {
			chooser = new JFileChooser();
		}
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(saveGameFileFilter);
		if (chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) {
			lastSaveGame = FileUtilities.fixFileExtension(chooser.getSelectedFile(),".battle");
			if (lastSaveGame.exists()) {
				GameData data = new GameData();
				data.zipFromFile(lastSaveGame);
				return data;
			}
			else {
				JOptionPane.showMessageDialog(frame,"File not found:  "+lastSaveGame.getPath());
			}
		}
		return null;
	}
	public static void saveBattle(JFrame frame,GameData data) {
		JFileChooser chooser;
		if (lastSaveGame!=null) {
			String filePath = FileUtilities.getFilePathString(lastSaveGame,false,false);
			chooser = new JFileChooser(new File(filePath));
			chooser.setSelectedFile(lastSaveGame);
		}
		else {
			chooser = new JFileChooser();
			chooser.setSelectedFile(new File("RealmBattle.battle"));
		}
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(saveGameFileFilter);
		if (chooser.showSaveDialog(frame)==JFileChooser.APPROVE_OPTION) {
			lastSaveGame = FileUtilities.fixFileExtension(chooser.getSelectedFile(),".battle");
			data.zipToFile(lastSaveGame);
		}
	}
	public CharacterWrapper getActiveCharacter() {
		return activeCharacter;
	}
	public RollerResults getRollerResults() {
		return rollerResults;
	}
	public HostPrefWrapper getHostPrefs() {
		return hostPrefs;
	}
	public boolean getActiveCharacterIsHere() {
		return activeCharacterIsHere;
	}
	public void handleMissingMonsters() {
		// What about doing a total cleanup of targets that have been teleported...
		ArrayList all = currentBattleModel.getAllBattleParticipants(true);
		for (Iterator i=all.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			RealmComponent target = rc.getTarget();
			if (target!=null) {
				if (!currentBattleModel.getBattleLocation().equals(target.getCurrentLocation())) {
					CombatWrapper combat = new CombatWrapper(rc.getGameObject());
					if (!combat.isSheetOwner()) {
						combat.setSheetOwner(true);
						combat.setCombatBox(1);
					}
					rc.clearTarget();
				}
			}
		}
		
	}
	public void showCombatSummary() {
		BattleSummaryWrapper bsw = new BattleSummaryWrapper(theGame.getGameObject());
		BattleSummary bs = bsw.getBattleSummary();
		BattleSummaryIcon icon = new BattleSummaryIcon(bs);
		JScrollPane sp = new JScrollPane(new JLabel(icon));
		ComponentTools.lockComponentSize(sp,640,480);
		JOptionPane.showMessageDialog(this,sp,"Round Summary",JOptionPane.PLAIN_MESSAGE);
	}
	public static void broadcastMessage(String message) {
		broadcastMessage(RealmLogging.BATTLE,message);
	}
	public static void broadcastMessage(String key,String message) {
		RealmLogging.logMessage(key,message);
	}
}