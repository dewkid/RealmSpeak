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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.attribute.DayAction.ActionId;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.GamePhaseType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.table.Loot;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper.ActionState;

/**
 * This class will handle a getCharacter() turn.  It will allow the player to process each action in order, and handle the
 * various situations (ie., blocking, luring, hide rolls, etc.)
 */
public class RealmTurnPanel extends CharacterFramePanel {
	
	public static final String TAB_NAME = "Your Turn";
	public static final String PLAY_NEXT = "Play Next";

	private GameWrapper game;
	private int month;
	private RealmCalendar calendar;
	private SpellMasterWrapper spellMaster;

	private ActionRowTableModel model;
	private JTable actionTable;

	private ArrayList<ActionRow> actionRows;
	private int currentActionRow;
	
	private JPanel top;
	private CharacterActionControlManager acm;
	private JToolBar actionToolBar;
	
	private FlashingButton playNextButton;
	private JButton playAllButton;
	private JButton postponeTurnButton; // Swordsman's ability
	private JButton ditchFollowersButton;
	private JButton landButton; // only when Timeless Jewel is in play, and you are flying
	
	private JButton openButton; // for opening crypt, or vault
	private JButton pickupItemButton;
	private JButton dropInventoryButton; // for dropping items in the clearing
	private JButton abandonInventoryButton;
	private FlashingButton finishedPlayButton;
	
	private boolean isFollowing;
	
	private ArrayList<CharacterWrapper> actionFollowers;
	
	private PhaseManager phaseManager;
	private JLabel blockWarningLabel;
	
	private boolean getsTurn; // If you are blocked before your turn starts, then you do NOT get a turn, and do NOT summon monsters
	
	private HostPrefWrapper hostPrefs;
	
	private Timer activatePlayNextTimer = null;
	private ActionListener activatePlayNextListener = new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
			activatePlayNextTimer = (Timer)ev.getSource();
			playNextButton.setEnabled(true);
			playNextButton.setFlashing(true);
			playNextButton.setText(PLAY_NEXT);
			activatePlayNextTimer.stop();
			activatePlayNextTimer.removeActionListener(activatePlayNextListener);
			activatePlayNextTimer = null;
		}
	};

	public RealmTurnPanel(CharacterFrame parentFrame,GameWrapper game,HostPrefWrapper hostPrefs) {
		super(parentFrame);
		this.game = game;
		this.month = game.getMonth();
		this.actionFollowers = getCharacter().getActionFollowers();
		this.spellMaster = SpellMasterWrapper.getSpellMaster(game.getGameObject().getGameData());
		this.phaseManager = getCharacter().getPhaseManager(false);
		this.hostPrefs = hostPrefs;
		calendar = RealmCalendar.getCalendar(game.getGameObject().getGameData());
		updatePhaseManagerCurrentLocation();
		
		initActionRows();
		initComponents();
		
		getsTurn = !getCharacter().isBlocked();
		
		if (getCharacter().canDoDaytimeRecord()) {
			startDaytimeRecord();
		}
		
		if (getCharacter().isSleep()) {
			JOptionPane.showMessageDialog(
					getMainFrame(),
					"The "+getCharacter().getGameObject().getName()+" is sleeping.  Check log for reason.",
					"Sleeping",JOptionPane.INFORMATION_MESSAGE);
		}
		
		if (getMainFrame().getGameHandler().isOption(RealmSpeakOptions.TURN_END_RESULTS)) {
			String clientName = getGameHandler().getClient().getClientName();
			if (!game.isClientTakenTurn(clientName)) {
				game.addClientTakenTurn(clientName);
				ArrayList<GameObject> list = game.getRegeneratedDenizens();
				if (list!=null) {
					RealmObjectPanel panel = new RealmObjectPanel();
					panel.addObjects(list);
					JScrollPane sp = new JScrollPane(panel);
					ComponentTools.lockComponentSize(sp,400,300);
					JOptionPane.showMessageDialog(getGameHandler().getMainFrame(),sp,"Regenerated this turn",JOptionPane.PLAIN_MESSAGE,null);
				}
			}
		}
		updateNextPendingAction();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showFollowers();
			}
		});
	}
	private void updateNextPendingAction() {
		ActionRow next = nextAction();
		getCharacter().setNextPendingAction(next==null?null:next.getAction());
	}
	private void updatePhaseManagerCurrentLocation() {
		TileLocation current = getCharacter().getCurrentLocation();
		phaseManager.updateClearing(current,true);
	}
	private static final Font titleFont = new Font("dialog",Font.BOLD,12);
	private static final Font followFont = new Font("dialog",Font.PLAIN,12);
	public void showFollowers() {
		if (actionFollowers.size()>0) {
			Box box = Box.createVerticalBox();
			Box line = Box.createHorizontalBox();
			JLabel label = new JLabel("The "+getCharacter().getGameObject().getName()+" is being followed by:");
			label.setFont(titleFont);
			line.add(label);
			line.add(Box.createHorizontalGlue());
			box.add(line);
			for (CharacterWrapper follower:actionFollowers) {
				RealmComponent rc = RealmComponent.getRealmComponent(follower.getGameObject());
				label = new JLabel("The "+follower.getGameObject().getName(),rc.getSmallIcon(),JLabel.LEADING);
				label.setFont(followFont);
				line = Box.createHorizontalBox();
				line.add(Box.createHorizontalStrut(20));
				line.add(label);
				line.add(Box.createHorizontalGlue());
				box.add(line);
			}
			JOptionPane.showMessageDialog(getGameHandler().getMainFrame(),box,getCharacter().getGameObject().getName()+" is being Followed!",JOptionPane.WARNING_MESSAGE);
		}
	}
	private ActionRow initActionRow(String action,String actionTypeCode) {
		ActionRow ar = new ActionRow(this,getCharacter(),action,actionTypeCode,isFollowing);
		ActionId actionId = CharacterWrapper.getIdForAction(action);
		if (actionId==ActionId.Follow) {
			// Ignore follow action, except to tag as a follower
			isFollowing = true;
			ar.setCompleted(true);
			actionRows.add(ar);
		}
		else {
			if (actionId == ActionId.Rest) {
				// Rests are special, because they work better in groups
				ActionRow lastActionRow = getLastActionRow();
				if (lastActionRow!=null && lastActionRow.isPending() && lastActionRow.getAction().equals(DayAction.REST_ACTION.getCode())) {
					lastActionRow.incrementCount();
					ar = null;
				}
			}
			if (ar!=null) {
				actionRows.add(ar);
				if (actionId == ActionId.Move
						|| actionId == ActionId.Fly
						|| actionId == ActionId.EnhPeer
						|| actionId == ActionId.RemSpell) {
					
					if (actionId==ActionId.Move && action.indexOf('!')==1) {
						ar.setPonyLock(true);
					}
					
					// Deduce clearing info from action (don't use clearingPlot!)
					TileLocation tl = ClearingUtility.deduceLocationFromAction(getGameHandler().getClient().getGameData(),action);
					if (actionId==ActionId.Fly) {
						tl.setFlying(true);
					}
					ar.setLocation(tl);
				}
			}
		}
		return ar;
	}
	private void initActionRows() {
		// Do this first thing, in case no orders were submitted
		(new ActionRow(this,getCharacter(),null,null,false)).checkSleep();
			
		// Now init
		ActionRow.askAboutAbandoningFollowers = true;
		actionRows = new ArrayList<ActionRow>();
		ArrayList actions = getCharacter().getCurrentActions();
		if (actions!=null) {
			ArrayList actionTypeCodes = getCharacter().getCurrentActionTypeCodes();
			if (actionTypeCodes==null) {
				actionTypeCodes = new ArrayList();
			}
			isFollowing = false; // followers are exempt from blocking rules because the action is simultaneous with the guide.
			for (int i=0;i<actions.size();i++) {
				String action = (String)actions.get(i);
				String actionTypeCode = (String)actionTypeCodes.get(i);
				ActionRow ar = initActionRow(action,actionTypeCode);
				if (ar!=null && !ar.isFollow()) {
					CharacterWrapper.ActionState state = getCharacter().getStateForAction(action,i);
					ar.setActionState(state);
					if (state!=ActionState.Pending) {
						ar.setResult(getCharacter().getMessageForAction(i));
						ar.setRoller(getCharacter().getRollerForAction(i));
					}
				}
			}
		}
	}
	private boolean isAwaitingFollowersResting() {
		for (CharacterWrapper follower:actionFollowers) {
			if (follower.getFollowRests()>0) {
				return true;
			}
		}
		return false;
	}
	private boolean isAwaitingBlockDecision() {
		if (getRealmComponent().isPlayerControlledLeader()) {
			blockWarningLabel.setText("");
			if (!getCharacter().isBlocked() && getCharacter().hasDoneActionsToday()) {
				TileLocation current = getCharacter().getCurrentLocation();
				if (current!=null && current.isInClearing()) {
					for (RealmComponent rc:current.clearing.getClearingComponents()) {
						if (!rc.getGameObject().equals(getCharacter().getGameObject())
								&& (rc.isPlayerControlledLeader())) {
							CharacterWrapper target = new CharacterWrapper(rc.getGameObject());
							if (target.isBlocking()) {
								if (!getCharacter().isHidden() || target.foundHiddenEnemy(getCharacter().getGameObject())) {
									if (!target.hasBlockDecision(getCharacter().getGameObject())) {
										blockWarningLabel.setText(target.getGameObject().getName()+" is blocking.  Awaiting decision...");
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public void updateControls() {
		TileLocation current = getCharacter().getCurrentLocation();
		
		boolean waitingForSingleButton = getCharacterFrame().isWaitingForSingleButton();
		boolean controlsLocked = isAwaitingBlockDecision() || waitingForSingleButton;
		
		boolean playedAnAction = actionRows.size()>0 && !(actionRows.get(0)).isPending();
		
		boolean areActionFollowersToLeaveBehind = false;
		if (getCharacter().isHidden()) {
			for (CharacterWrapper follower:actionFollowers) {
				if (!follower.foundHiddenEnemy(getCharacter().getGameObject())) {
					areActionFollowersToLeaveBehind = true;
					break;
				}
			}
		}
		String me = getCharacter().getPlayerName();
		boolean haveFollowersThatHaveFollowRests = isAwaitingFollowersResting();
		boolean beingFollowedByOtherPlayers = false;
		for (CharacterWrapper follower:actionFollowers) {
			if (!follower.getPlayerName().equals(me)) {
				beingFollowedByOtherPlayers = true;
				break;
			}
		}
		playNextButton.setText(haveFollowersThatHaveFollowRests?"Followers Resting...":PLAY_NEXT);
		
		boolean actionsLeft = isNextAction();
		playNextButton.setEnabled(actionsLeft && !waitingForSingleButton && activatePlayNextTimer==null && !haveFollowersThatHaveFollowRests);
		playNextButton.setFlashing(playNextButton.isEnabled());
		playAllButton.setEnabled(!controlsLocked && actionsLeft && !beingFollowedByOtherPlayers);
		finishedPlayButton.setEnabled(!controlsLocked && !actionsLeft && (current==null || (!current.isBetweenClearings() && !current.isBetweenTiles())));
		finishedPlayButton.setFlashing(finishedPlayButton.isEnabled());
		
		if (playNextButton.isEnabled()) makeDefault(playNextButton);
		if (finishedPlayButton.isEnabled()) makeDefault(finishedPlayButton);
		
		if (playNextButton.isEnabled() && beingFollowedByOtherPlayers) {
			playNextButton.setText("Wait....");
			playNextButton.setEnabled(false);
			playNextButton.setFlashing(false);
			activatePlayNextTimer = new Timer(5000,activatePlayNextListener);
			activatePlayNextTimer.start();
		}
		
		/* canPostpone only if ALL of the following is true:
		 * 
		 * 	1)	Have this ability
		 * 	2)	Haven't played an action yet
		 * 	3)	Aren't already the last player (irrelevant otherwise!)
		 */
		
		boolean canPostpone = getCharacter().affectedByKey(Constants.CHOOSE_TURN)
							&& !getCharacter().isLastPlayer()
							&& !playedAnAction;
		postponeTurnButton.setEnabled(!controlsLocked && canPostpone);
		
		boolean objectsNeedOpen = !getCharacter().getAllOpenableSites().isEmpty();
		openButton.setEnabled(objectsNeedOpen);
		
		landButton.setEnabled(getCharacter().canDoDaytimeRecord() && current.isFlying() && !current.isBetweenTiles());
		landButton.setVisible(landButton.isEnabled());
		if (pickupItemButton!=null) {
			boolean canPickup = !landButton.isEnabled() && current!=null && current.isInClearing();
			pickupItemButton.setEnabled(canPickup);
			pickupItemButton.setVisible(canPickup);
		}
		if (dropInventoryButton!=null) {
			dropInventoryButton.setEnabled(current!=null && !current.isBetweenClearings() && !getCharacter().isBlocked());
		}
		abandonInventoryButton.setEnabled(current!=null && !current.isBetweenClearings() && !getCharacter().isBlocked());
		
		ditchFollowersButton.setEnabled(areActionFollowersToLeaveBehind);
		
		if (acm!=null) {
			acm.updateControls(phaseManager,!isFollowing,false);
		}
		getCharacterFrame().setCurrentPhaseManager(phaseManager);
		
		actionTable.repaint();
	}
	private void makeDefault(JButton button) {
		getCharacterFrame().getRootPane().setDefaultButton(button);
		button.grabFocus();
	}
	
	public void pressPlayNext() {
		playNextButton.doClick();
	}
	public void pressPlayAll() {
		playAllButton.doClick();
	}

	private void initComponents() {
		setSize(400, 400);
		setLayout(new BorderLayout());
		
		// Top
		top = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(1,4));
		playNextButton = new FlashingButton(PLAY_NEXT) {
			public void doWhenFlash() {
				SoundUtility.playClick();
			}
		};
		playNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (!isAwaitingBlockDecision()) {
					playNext(false);
					getGameHandler().getInspector().redrawMap();
					updateControls();
				}
			}
		});
		panel.add(playNextButton);
		playAllButton = new JButton("Play All");
		playAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				playAll();
				getGameHandler().getInspector().redrawMap();
				updateControls();
			}
		});
		panel.add(playAllButton);
		postponeTurnButton = new JButton("Postpone Turn");
		postponeTurnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				getGameHandler().broadcast(getCharacter().getGameObject().getName(),"Postpones turn.");
				getCharacterFrame().hideYourTurn();
				getCharacter().setPlayOrder(-1); // set to -1 so that the RealmHostPanel will promote turn order properly
				getCharacter().setLastPlayer(false);
				getGameHandler().submitChanges();
			}
		});
		panel.add(postponeTurnButton);
//		Die monsterDie = new Die(25,5,Color.red,Color.white);
//		monsterDie.setFace(game.getMonsterDie());
		DieRoller monsterDieRoller = game.getMonsterDie();
		monsterDieRoller.setAllRed();
		JLabel monsterDieLabel = new JLabel("Monster Die:",monsterDieRoller.getIcon(),SwingConstants.CENTER);
		monsterDieLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		panel.add(monsterDieLabel);
		top.add(panel,"Center");
		
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		blockWarningLabel = new JLabel();
		blockWarningLabel.setFont(new Font("Dialog",Font.BOLD,14));
		blockWarningLabel.setForeground(Color.red);
		box.add(Box.createHorizontalGlue());
		box.add(blockWarningLabel);
		top.add(box,"North");
		
		add(top,"North");
		
		// Table
		model = (ActionRowTableModel) new ActionRowTableModel();
		actionTable = new JTable(model);
		actionTable.setDefaultRenderer(ImageIcon.class,new ActionIconRenderer());
		actionTable.setDefaultRenderer(JComponent.class,new ComponentRenderer());
		actionTable.getColumnModel().getColumn(1).setCellRenderer(new TextAreaTableCellRenderer());
		actionTable.setRowHeight(32);
		actionTable.getColumnModel().getColumn(0).setMaxWidth(45);
		actionTable.getColumnModel().getColumn(2).setMaxWidth(100);
		actionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		actionTable.getColumnModel().getColumn(3).setMaxWidth(45);
		add(new JScrollPane(actionTable),"Center");
		
		// Bottom
		boolean allowDropping = hostPrefs.hasPref(Constants.ADV_DROPPING);
		//panel = new JPanel(new GridLayout(1,allowDropping?6:5));
		panel = new JPanel(new GridLayout(2,3));
		openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				openSite();
			}
		});
		panel.add(openButton);
		
		if (allowDropping) {
			dropInventoryButton = new JButton("Drop Item");
			dropInventoryButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					doDrop(true);
				}
			});
			panel.add(dropInventoryButton);
		}
		else {
			panel.add(Box.createGlue());
		}

		JPanel swapPanel = new JPanel(new BorderLayout());
			if (allowDropping) {
				pickupItemButton = new JButton("Pickup Item");
				pickupItemButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						doPickup();
					}
				});				
				swapPanel.add(pickupItemButton,"Center");
			}
		
			landButton = new JButton("Land Now");
			landButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					if (getCharacter().land(getMainFrame())) {
						getGameHandler().getInspector().redrawMap();
						updateControls();
					}
				}
			});
			swapPanel.add(landButton,"East");
		panel.add(swapPanel);
		
		ditchFollowersButton = new JButton("Ditch Follower");
		ditchFollowersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				verifyAbandonActionFollowers();
				updateControls();
			}
		});
		panel.add(ditchFollowersButton);
		
		abandonInventoryButton = new JButton("Abandon Item");
		abandonInventoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doDrop(false);
			}
		});
		panel.add(abandonInventoryButton);
		
		finishedPlayButton = new FlashingButton("Done") {
			public void doWhenFlash() {
				SoundUtility.playClick();
			}
		};
		finishedPlayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				turnDone();
			}
		});
		panel.add(finishedPlayButton);
		add(panel, "South");
		
		updateControls();
	}
	private void doPickup() {
		TileLocation current = getCharacter().getCurrentLocation();
		String where = getCharacter().isHidden()?"at your feet":"in plain sight";
		if (current.isInClearing()) {
			ArrayList<RealmComponent> list = current.clearing.getClearingComponentsInPlainSight(getCharacter());
			
			if (list.size()>0) {
				// Pickup an item (one at a time)
				StringBuffer sb = new StringBuffer();
				sb.append("Select item ");
				sb.append(where);
				sb.append(" to pick up:");
				RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getGameHandler().getMainFrame(),sb.toString(),true);
				chooser.addRealmComponents(list,false);
				chooser.setVisible(true);
				
				if (chooser.getSelectedText()!=null) {
					GameObject thing = chooser.getFirstSelectedComponent().getGameObject();
					thing.removeThisAttribute(Constants.PLAIN_SIGHT);
					thing.removeThisAttribute(Constants.DROPPED_BY);
					
					Loot.addItemToCharacter(getMainFrame(),getGameHandler().getUpdateFrameListener(),getCharacter(),thing,hostPrefs);
					getGameHandler().getUpdateFrameListener().stateChanged(new ChangeEvent(getCharacter()));
				}
			}
			else {
				JOptionPane.showMessageDialog(getMainFrame(),"There are no items "+where+" to pick up!","Pickup Item",JOptionPane.WARNING_MESSAGE);
			}
		}
		
		// If no other characters in the clearing, then you can pickup anything you want
		// If other characters, and you are unhidden, then they have a chance to block you EACH item!
	}
	private void doDrop(boolean plainSight) {
		TileLocation tl = getCharacter().getCurrentLocation();
		if (!tl.isBetweenClearings() && !tl.isBetweenTiles()) {
			String message = "Select Item to "+(plainSight?"Drop":"Abandon")+":";
			RealmTradeDialog chooser = new RealmTradeDialog(new JFrame(),message,false,true,true);
			chooser.setTradeObjects(getCharacter().getDroppableInventory());
			chooser.setVisible(true);
			
			RealmComponent rc = chooser.getFirstSelectedRealmComponent();
			
			if (rc!=null) {
				if (rc.isGoldSpecial()) {
					plainSight = false;
				}
				GameObject thing = rc.getGameObject();
				Inventory selInv = new Inventory(thing);
				if (selInv.canDeactivate()) {
					if (!TreasureUtility.doDeactivate(getMainFrame(),getCharacter(),selInv.getGameObject())) {
						return;
					}
				}
				
				TreasureUtility.doDrop(getCharacter(),thing,getGameHandler().getUpdateFrameListener(),plainSight);
				
				if (selInv.getRealmComponent().isGoldSpecial()) {
					GoldSpecialChitComponent gs = (GoldSpecialChitComponent)selInv.getRealmComponent();
					gs.expireEffect(getCharacter());
					
					QuestRequirementParams qp = new QuestRequirementParams();
					qp.actionName = thing.getName();
					qp.actionType = CharacterActionType.AbandonMissionCampaign;
					qp.targetOfSearch = gs.getGameObject();
					if (getCharacter().testQuestRequirements(getMainFrame(),qp)) {
						getCharacterFrame().updateCharacter();
					}
				}				
			}
		}
	}
	private boolean playNext(boolean fromPlayAll) {
		rollPendingActionsIntoView();
		ActionRow ar = nextAction();
		if (!getCharacter().isActive()) {
			ar.setCancelled(true);
			return true;
		}
		ar.landCharacterIfNeeded();
		
		GameObject requiredObject = null; // Get this BEFORE doing the action, in case an item is sold during the action
		
		int afterRowIndex = currentActionRow+1;
		boolean pony = getCharacter().isPonyActive();
		if (!ar.isSpawned() && !ar.getIsFollowing()) { // don't do this test for followers!
			// Need to verify that action can actually occur
			boolean valid = getCharacter().actionIsValid(ar.getAction(),getCharacter().getCurrentLocation());
			TileLocation current = getCharacter().getCurrentLocation();
			boolean isInCave = current.isInClearing() && current.clearing.isCave();
			boolean isOutside = !current.isInside(hostPrefs.hasPref(Constants.HOUSE2_RED_SPECIAL_SHELTER));
			boolean willBeOutside = false;
			if (ar.getAction().startsWith("M")) {
				TileLocation target = ar.getLocation();
				if ((!target.isInClearing() || !target.clearing.isCave()) && !target.clearing.holdsDwelling()) {
					willBeOutside = true;
				}
			}
			if (valid) {
				// It's valid, let's just make sure you aren't trying to use a sunlight phase in a cave, or bonus phases outside after using sheltered phases
				if ((isInCave || ar.willMoveToCave()) && (phaseManager.hasUsedSunlight() || phaseManager.willRequireSunlight(ar.getAction()))) {
					if (fromPlayAll) {
						JOptionPane.showMessageDialog(this,"The next action ("+ar.getAction()+") is currently invalid.  Press \"Play Next\" to continue when you are ready.","Invalid Phase",JOptionPane.WARNING_MESSAGE);
						// Rather than make a BLANK phase automatically, return and let the player deal with the problem
						return false;
					}
						
					// Change into a BLANK phase
					ar.makeBlankPhase("No sunlight phases in caves!");
				}
				else if ((isOutside || willBeOutside) && phaseManager.hasUsedSheltered()) {
					if (fromPlayAll) {
						JOptionPane.showMessageDialog(this,"The next action ("+ar.getAction()+") is currently invalid.  Press \"Play Next\" to continue when you are ready.","Invalid Phase",JOptionPane.WARNING_MESSAGE);
						// Rather than make a BLANK phase automatically, return and let the player deal with the problem
						return false;
					}
						
					// Change into a BLANK phase
					ar.makeBlankPhase("No outdoor phases allowed after sheltered!");
				}
				else {
					if (phaseManager.canAddAction(ar.getAction(),pony,getGameHandler().getMainFrame())) {
						requiredObject = phaseManager.getNextRequiredObject(ar.getAction(),pony);
						int count = ar.getCount(); // The only time this will be >0 is when its a REST phase
						int allowed = phaseManager.getNumberOfActionsAllowed(ar.getAction(),pony);
						if (allowed>0 && (ar.willHavePhaseEndUpdates() || calendar.isFatiguePhasesType(month))) {
							allowed = 1;
						}
						if (allowed<count) {
							ar.setCount(allowed);
							ActionRow newAction = ar.makeCopy();
							newAction.setCount(count-allowed);
							actionRows.add(afterRowIndex,newAction); // add after this row
							model.fireTableDataChanged();
						}
					}
					else if (!current.isBetweenClearings()) {
						if (fromPlayAll) {
							JOptionPane.showMessageDialog(this,"The next action ("+ar.getAction()+") is currently invalid.  Press \"Play Next\" to continue when you are ready.","Invalid Phase",JOptionPane.WARNING_MESSAGE);
							// Rather than make a BLANK phase automatically, return and let the player deal with the problem
							return false;
						}
						
						// Change into an INVALID phase
						ar.makeInvalidPhase();
					}
				}
			}
			else {
				if (fromPlayAll) {
					JOptionPane.showMessageDialog(this,"The next action ("+ar.getAction()+") is currently invalid.  Press \"Play Next\" to continue when you are ready.","Invalid Phase",JOptionPane.WARNING_MESSAGE);
					// Rather than make a BLANK phase automatically, return and let the player deal with the problem
					return false;
				}
				// Change into a BLANK phase or INVALID
				ar.makeBlankPhase("Invalid phase!");
			}
		}
		
		TileLocation locationBeforeAction = getCharacter().getCurrentLocation();
		
		ar.process();
		if (fromPlayAll && ar.isCancelled() && ar.getActionId()==ActionId.Move) {
			// This undoes an invalid move action (sort of)
			ar.setResult("");
			ar.setCancelled(false);
			ar.setCompleted(false);
			JOptionPane.showMessageDialog(this,"The next action ("+ar.getAction()+") is currently invalid.  Press \"Play Next\" to continue when you are ready.","Invalid Phase",JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		TileLocation locationAfterAction = getCharacter().getCurrentLocation();
		if (ar.isCompleted() && calendar.hasSpecial(month)) {
			if (calendar.isFatiguePhasesOutside(month)) {
				if (!locationAfterAction.isInside(hostPrefs.hasPref(Constants.HOUSE2_RED_SPECIAL_SHELTER))) {
					getCharacter().setWeatherFatigue(ar.getPhaseCount()*ar.getCount());
				}
			}
			if (calendar.isFatiguePhasesMountain(month)) {
				if (locationAfterAction.isInClearing() && locationAfterAction.clearing.isMountain()) {
					getCharacter().setWeatherFatigue(ar.getPhaseCount()*ar.getCount());
				}
			}
			
			getCharacterFrame().fatigueToContinue();
		}
		
		updateNextPendingAction();
		getCharacterFrame().updateCharacter(); // added this 7/15/2005 - will this be too CPU intensive?
		QuestRequirementParams params = new QuestRequirementParams();
		params.timeOfCall = GamePhaseType.EndOfPhase;
		if (getCharacter().testQuestRequirements(getMainFrame(),params)) {
			getCharacterFrame().updateCharacter();
		}
		getGameHandler().submitChanges(); // Will this work okay?
		
		actionTable.repaint();
		if (!ar.isPending()) {
			// Summon monsters for any action followers who stopped following!
			DieRoller monsterDieRoller = game.getMonsterDie();
			for (CharacterWrapper follower:getCharacter().getStoppedActionFollowers()) {
				getCharacter().removeActionFollower(follower,monsterDieRoller);
			}
		
			// Collect any newActions that may have spawned (ie., Curse as the result of a search)
			ArrayList<ActionRow> newActions = new ArrayList<ActionRow>();
			ActionRow newAction = ar.getNewAction();
			while(newAction!=null) {
				newAction.setSpawned(true);
				newAction.logAction(); // new actions wont have been processed directly, so need a log (I think)
				newActions.add(newAction);
				newAction = newAction.getNewAction();
			}
			if (!newActions.isEmpty()) {
				actionRows.addAll(afterRowIndex,newActions); // add after this row
				model.fireTableDataChanged();
			}
			
			// Energize any permanent spells
			ArrayList se = getCharacter().getSpellExtras();
			int seBefore = se==null?0:se.size();
			spellMaster.energizePermanentSpells(getGameHandler().getMainFrame(),game);
			ar.updateBlocked(); // check block status AFTER energizing spells - BUG 1624
			getCharacterFrame().updateControls();
			se = getCharacter().getSpellExtras();
			int seAfter = se==null?0:se.size();
			if (seAfter>seBefore) {
				// A spell (or spells) were energized automatically during the turn.  Make sure these
				// make it into the PhaseManager
				ArrayList ses = getCharacter().getSpellExtraSources();
				for (int i=seBefore;i<seAfter;i++) {
					String seAction = (String)se.get(i);
					GameObject seGo = (GameObject)ses.get(i);
					phaseManager.addFreeAction(seAction,seGo);
				}
			}
			
			// Make sure action gets added to the phase manager
			if (acm==null || !ar.isCancelled()) { // don't add canceled phases when using timeless jewel
				if (ar.isBlankPhase()) {
					phaseManager.addPerformedPhase(ar.getAction(),(GameObject)null,pony,locationAfterAction);
				}
				else {
					// Separate phases in case there is a mountain move or rest block
					String phase = ar.getAction();
					if (phase!=null) {
						ArrayList separatePhases = new ArrayList();
						for (int i=0;i<ar.getCount();i++) {
							if (phase.indexOf(",")>=0) {
								StringTokenizer phases = new StringTokenizer(phase,",");
								while(phases.hasMoreTokens()) {
									separatePhases.add(phases.nextToken());
								}
							}
							else {
								separatePhases.add(phase);
							}
						}
						
						int ponyMovesBefore = phaseManager.getPonyMoves();
						for (Iterator i=separatePhases.iterator();i.hasNext();) {
							String aPhase = (String)i.next();
							phaseManager.addPerformedPhase(aPhase,requiredObject,pony,locationAfterAction);
							
							// refresh the required object, so that freeActions isn't used more than is available!
							requiredObject = phaseManager.getNextRequiredObject(ar.getAction(),pony);
							updatePhaseManagerCurrentLocation();
						}
						int ponyMovesAfter = phaseManager.getPonyMoves();
						
						if (ponyMovesBefore>ponyMovesAfter) {
							// Need to "ditch" all pony-less followers
							for (Iterator i=getCharacter().getActionFollowers().iterator();i.hasNext();) {
								CharacterWrapper follower = (CharacterWrapper)i.next();
								BattleHorse horse = follower.getActiveSteed();
								if (horse==null || horse.isDead() || !horse.doublesMove()) {
									ClearingUtility.moveToLocation(follower.getGameObject(), locationBeforeAction);
									getCharacter().removeActionFollower(follower, game.getMonsterDie());
									getGameHandler().broadcast(follower.getGameObject().getName(),"Unable to follow Guide:  Guide is on a Pony");
								}
							}
							for (Iterator i=getCharacter().getFollowingHirelings().iterator();i.hasNext();) {
								RealmComponent hireling = (RealmComponent)i.next();
								BattleHorse horse = hireling.getHorse(false); // don't check location here!  If guide can do it, so can natives
								if (horse==null || horse.isDead() || !horse.doublesMove()) {
									// moving to the clearing is sufficient here
									ClearingUtility.moveToLocation(hireling.getGameObject(), locationBeforeAction);
									getGameHandler().broadcast(hireling.getGameObject().getName(),"Unable to follow Guide:  Guide is on a Pony");
								}
							}
						}
						
						if (spellMaster.expirePhaseSpells()) {
							getCharacterFrame().updateCharacter();
						}
//						getCharacter().doEndActivePhaseChits();
					}
				}
			}
			
			return true;
		}
		return false;
	}
	
	private void playAll() {
		while(nextAction()!=null && !isAwaitingBlockDecision() && !isAwaitingFollowersResting()) {
			if (!playNext(true)) {
				// player cancelled action, or awaiting input (like transport to caves result in TableLoot)
				break;
			}
		}
	}
	
	public boolean hasActionsLeft() {
		return (getCharacter().canDoDaytimeRecord() && phaseManager.hasActionsLeft()) || isNextAction();
	}
	private boolean isNextAction() {
		for (ActionRow ar:actionRows) {
			if (ar.isPending()) {
				return true;
			}
		}
		return false;
	}
	private ActionRow nextAction() {
		int n=0;
		for (ActionRow ar:actionRows) {
			if (ar.isPending()) {
				currentActionRow = n;
				return ar;
			}
			n++;
		}
		return null;
	}
	
	private void openSite() {
		Collection openable = getCharacter().getAllOpenableSites();
		if (TreasureUtility.openOneObject(getGameHandler().getMainFrame(),getCharacter(),openable,getGameHandler().getUpdateFrameListener(),false)!=null) {
			updateControls();
		}
	}
	
	private Collection getUnavailableManeuverOptions() {
		// Limit maneuver choices to those that can handle the heaviest piece of inventory
		Strength heaviestInventory = getCharacter().getNeededSupportWeight();
		ArrayList list = new ArrayList();
		for (Iterator i=getCharacter().getActiveMoveChits().iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			if (!chit.getStrength().strongerOrEqualTo(heaviestInventory)) {
				list.add(chit);
			}
		}
		// Add any boots cards or horses
		for (Iterator i=getCharacter().getActiveInventory().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (go.hasThisAttribute("boots")) {
				Strength bootStrength = new Strength(go.getThisAttribute("strength"));
				if (!bootStrength.strongerOrEqualTo(heaviestInventory)) {
					list.add(rc);
				}
			}
			else if (rc.isHorse()) {
				// Check strength on trot side only (usually stronger than gallop)
				Strength trotStrength = new Strength(go.getAttribute("trot","strength"));
				if (!trotStrength.strongerOrEqualTo(heaviestInventory)) {
					list.add(rc);
				}
			}
		}
		return list;
	}
	
	private void turnDone() {
		// Assign combat order here
		getCharacter().setCombatPlayOrder(game.getNextDayTurnCount());
		
		// Check to see if any chits are going to be hindered in combat due to inventory, and report it here
		Collection c = getUnavailableManeuverOptions();
		if (getGameHandler().isOption(RealmSpeakOptions.HEAVY_INV_WARNING) && c.size()>0) {
			RealmComponentDisplayDialog dialog = new RealmComponentDisplayDialog(
					getGameHandler().getMainFrame(),
					"Warning!",
					"The following options will be unavailable for combat due to heavy inventory:");
			dialog.addRealmComponents(c);
			dialog.setVisible(true);
		}
		
		getGameHandler().broadcast(getCharacter().getGameObject().getName(),"Ends turn: "+getCharacter().getCurrentLocation());
		
		// If the getCharacter() was blocked, be sure to note this in the action list
		if (actionRows.size()>0) {
			// NOTE:  current action type codes will be off, but I don't think it matters anymore...
			// Actually, it does, so keep these up to date
			Collection atc = getCharacter().getCurrentActionTypeCodes();
			Collection oldCodes = new ArrayList();
			if (atc!=null) {
				oldCodes.addAll(atc);
			}
			
			// First, blow away current actions...
			getCharacter().clearCurrentActions();

			// Now, rebuild them from actionRows
			boolean blocked = false;
			Iterator fi = oldCodes.iterator();
			for (ActionRow ar:actionRows) {
				if (!blocked && ar.isCancelled()) {
					blocked = true;
					getCharacter().addCurrentAction(Constants.BLOCKED);
					getCharacter().addCurrentActionValid(false);
					getCharacter().addCurrentActionTypeCode("XX");
				}
				String action = ar.getAction();
				if (action!=null) { // might be null if there was a re-roll on a table
					String actionTypeCode = "*"; // actionTypeCode is the type of clearing from which the action was performed
					try {
						actionTypeCode = (String)fi.next(); // NoSuchElementException!?!?
					}
					catch(NoSuchElementException ex) {
						// Well this will tell me what's happening if it happens again
						System.err.println("oldCodes: "+oldCodes);
						System.err.println("action: "+action);
						System.err.println("ActionRow: "+ar.toString());
						System.err.println("blocked: "+blocked);
						System.err.println("currentActions: "+getCharacter().getCurrentActions());
						System.err.println("currentActionValids: "+getCharacter().getCurrentActionValids());
						System.err.println("currentActionTypeCodes: "+getCharacter().getCurrentActionTypeCodes());
						ex.printStackTrace();
					}
					for (int n=0;n<ar.getCount();n++) {
						if (ar.isBlankPhase()) {
							getCharacter().addCurrentAction("("+action+")");
							getCharacter().addCurrentActionValid(false);
						}
						else {
							getCharacter().addCurrentAction(action);
							getCharacter().addCurrentActionValid(true);
						}
						getCharacter().addCurrentActionTypeCode(actionTypeCode);
					}
				}
			}
		}
		
		// play turn is over
		getCharacter().resetClearingPlot(); // all moved, so clear this out
		
		// If the getCharacter() was flying, make sure he/she has landed!
		getCharacter().land(getGameHandler().getMainFrame());
		TileLocation current = getCharacter().getCurrentLocation();
		
		// Test requirements (in case any are dependent on end of turn)
		QuestRequirementParams params = new QuestRequirementParams();
		params.timeOfCall = GamePhaseType.EndOfTurn;
		if (getCharacter().testQuestRequirements(getMainFrame(),params)) {
			getCharacterFrame().updateCharacter();
		}
		
		// Flip chits, and summon monsters/natives
		if (getsTurn && current!=null && !getCharacter().isMinion() && !getCharacter().isSleep()) {
			// UPDATE - According to RH, it doesn't matter whether or not you completed actions: if you
			// fall asleep, you simply aren't there until Sunset
			
			if (!getCharacter().isHidden() || !hostPrefs.hasPref(Constants.OPT_QUIET_MONSTERS)) {
				ArrayList<StateChitComponent> flipped = current.tile.setChitsFaceUp();
				for(StateChitComponent chit:flipped) {
					getGameHandler().broadcast(getCharacter().getGameObject().getName(),"Reveals: "+chit.getGameObject().getName());
				}
			}
			DieRoller monsterDieRoller = game.getMonsterDie();
			ArrayList<GameObject> summoned = new ArrayList<GameObject>();
			SetupCardUtility.summonMonsters(hostPrefs,summoned,getCharacter(),monsterDieRoller.getValue(0));
			if (monsterDieRoller.getNumberOfDice()==2 && monsterDieRoller.getValue(0)!=monsterDieRoller.getValue(1)) {
				SetupCardUtility.summonMonsters(hostPrefs,summoned,getCharacter(),monsterDieRoller.getValue(1));
			}
			if (getMainFrame().getGameHandler().isOption(RealmSpeakOptions.TURN_END_RESULTS)) {
				if (!summoned.isEmpty()) {
					RealmObjectPanel panel = new RealmObjectPanel();
					panel.addObjects(summoned);
					JScrollPane sp = new JScrollPane(panel);
					ComponentTools.lockComponentSize(sp,400,300);
					JOptionPane.showMessageDialog(getGameHandler().getMainFrame(),sp,"Summoned this turn",JOptionPane.PLAIN_MESSAGE,null);
				}
			}

			ArrayList<GameObject> list = getCharacter().getAllActiveInventoryThisKeyAndValue(Constants.DWELLING_GOLD,null);
			if (!list.isEmpty()) {
				GameObject dwelling = ClearingUtility.findDwellingInClearing(getCharacter().getCurrentLocation());
				if (dwelling!=null) {
					for (GameObject go:list) {
						String roll = go.getThisAttribute(Constants.DWELLING_GOLD);
						int gain = RandomNumber.getFromDieString(roll);
						getCharacter().addGold(gain);
						getGameHandler().broadcast(
								getCharacter().getGameObject().getName(),
								"The "+go.getName()+" acquired "+gain+" gold for the "+getCharacter().getName()+" at the "+dwelling.getName());
					}
				}
			}
		}
		
		// Test requirements one more time (in case any are dependent on start of evening)
		params.timeOfCall = GamePhaseType.StartOfEvening; // technically not evening until all players are done with their turn, but this is good enough I think
		getCharacter().testQuestRequirements(getMainFrame(),params);
		
		// cleanup and notify host
		getCharacterFrame().hideYourTurn();
		getCharacter().setPlayOrder(0);
		getCharacter().setLastPlayer(false);
		getCharacter().setLastPreemptivePlayer(false);
		getGameHandler().submitChanges();
	}
	
	private class ActionIconRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setHorizontalAlignment(SwingConstants.CENTER);
			setIcon((ImageIcon)value);
			setText("");
			return this;
		}
	}
	
	private class ComponentRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value!=null && value instanceof JComponent) {
				return (JComponent)value;
			}
			setText("");
			setIcon(null);
			return this;
		}
	}

	private static final String[] ACTION_ROW_HEADER = { "Action", "Description","Dice", "Done" };
	private static final Class[] ACTION_ROW_CLASS = { ImageIcon.class, String.class, JComponent.class,ImageIcon.class };

	private class ActionRowTableModel extends AbstractTableModel {
		public ActionRowTableModel() {
		}

		public int getColumnCount() {
			return ACTION_ROW_HEADER.length;
		}

		public String getColumnName(int index) {
			return ACTION_ROW_HEADER[index];
		}

		public Class getColumnClass(int index) {
			return ACTION_ROW_CLASS[index];
		}

		public int getRowCount() {
			return actionRows.size();
		}

		public Object getValueAt(int row, int col) {
			if (row < actionRows.size()) {
				ActionRow ar = actionRows.get(row);
				switch (col) {
					case 0:
						return ar.getIcon();
					case 1:
						return ar.getDescription();
					case 2:
						return ar.getRoller();
					case 3:
						return ar.getStatusIcon();
				}
			}
			return null;
		}
	}
	public void verifyAbandonActionFollowers() {
		ArrayList canLeaveBehind = new ArrayList();
		for (CharacterWrapper follower:actionFollowers) {
			if (!follower.foundHiddenEnemy(getCharacter().getGameObject())) {
				canLeaveBehind.add(follower);
			}
		}
		int totalCanLeaveBehind = canLeaveBehind.size();
		if (totalCanLeaveBehind>0) {
			// Opportunity to leave followers behind here!  Rule 27.6/1a
			StringBuffer message = new StringBuffer();
			message.append("There ");
			message.append(totalCanLeaveBehind==1?"is ":"are ");
			message.append(totalCanLeaveBehind);
			message.append(" follower");
			message.append(totalCanLeaveBehind==1?"":"s");
			message.append(" following that haven't found hidden enemies.");
			message.append("\nDo you want to leave ");
			message.append(totalCanLeaveBehind==1?"that follower":"one or more of them");
			message.append(" behind?");
			
			int ret = JOptionPane.showConfirmDialog(
						getGameHandler().getMainFrame(),
						message.toString(),
						"Unwanted Followers?",
						JOptionPane.YES_NO_OPTION);
			if (ret==JOptionPane.YES_OPTION) {
				doAbandonActionFollowers();
				actionFollowers = getCharacter().getActionFollowers();
			}
		}
	}
	public void doAbandonActionFollowers() {
		ArrayList toRemove = new ArrayList();
		if (actionFollowers.size()==1) {
			toRemove.add(actionFollowers.get(0));
		}
		else {
			ArrayList list = new ArrayList();
			for (CharacterWrapper aFollower:actionFollowers) {
				list.add(aFollower.getGameObject());
			}
			RealmObjectChooser chooser = new RealmObjectChooser("Which follower(s) do you want to leave behind?",getCharacter().getGameObject().getGameData(),false);
			chooser.addObjectsToChoose(list);
			chooser.setVisible(true);
			if (chooser.pressedOkay()) {
				for (Iterator i=chooser.getChosenObjects().iterator();i.hasNext();) {
					GameObject go = (GameObject)i.next();
					toRemove.add(new CharacterWrapper(go));
				}
			}
		}
		DieRoller monsterDieRoller = game.getMonsterDie();
		for (Iterator i=toRemove.iterator();i.hasNext();) {
			CharacterWrapper aFollower = (CharacterWrapper)i.next();
			getCharacter().removeActionFollower(aFollower,monsterDieRoller);
		}
		
		// Reset the list
		actionFollowers = getCharacter().getActionFollowers();
	}
	public void updatePanel() {
	}
	/**
	 * @return Returns the phaseManager.
	 */
	public PhaseManager getPhaseManager() {
		return phaseManager;
	}
	public void updatePhaseManagerInactiveThings() {
		if (phaseManager!=null) {
			phaseManager.updateInactiveThings();
		}
	}
	public void refreshPhaseManagerIcon() {
		getCharacterFrame().setCurrentPhaseManager(phaseManager);
	}
	public ActionRow getLastActionRow() {
		if (!actionRows.isEmpty()) {
			return actionRows.get(actionRows.size()-1);
		}
		return null;
	}
	/**
	 * This gets called when the player records a new action LIVE, like they will when the spell Prophecy is active,
	 * or they own the Timeless Jewel.
	 */
	public void processNewAction(String recordAction,String actionTypeCode,int count) {
		ActionRow ar = initActionRow(recordAction,actionTypeCode);
		ar.setCount(count);
		playNext(false);
		if (!ar.isCompleted() || ar.isCancelled()) { // moves down undiscovered paths are canceled but completed!
			actionRows.remove(actionRows.size()-1);
		}
		if (nextAction()!=null) { // This happens when you specify too many rests!
			playNext(false);
		}
		getGameHandler().getInspector().redrawMap();
		updateControls();
		model.fireTableDataChanged();
	}
	public void startDaytimeRecord() {
		if (!isFollowing) {
			// First, remove all pending actions
			Collection atcc = getCharacter().getCurrentActionTypeCodes();
			ArrayList atc = new ArrayList();
			if (atcc!=null) {
				atc.addAll(atcc);
			}
			Iterator n=atc.iterator();
			getCharacter().clearCurrentActions();
			ArrayList<ActionRow> toRemove = new ArrayList<ActionRow>();
			for (ActionRow ar:actionRows) {
				if (ar.getAction()!=null) { // ignore null action rows
					if (ar.isPending()) {
						toRemove.add(ar);
						String removed = ar.getAction();
						if (removed.startsWith(DayAction.MOVE_ACTION.getCode()) || removed.startsWith(DayAction.FLY_ACTION.getCode())) {
							// deleting a move, so delete a clearing plot
							getCharacter().chompClearingPlot();
						}
					}
					else {
						// Add it back
						getCharacter().addCurrentAction(ar.getAction());
						getCharacter().addCurrentActionValid(true);
						getCharacter().addCurrentActionTypeCode((String)n.next()); // TODO Check this
					}
				}
			}
			actionRows.removeAll(toRemove);
		
			// Next, start the toolbar
			startToolBar();
			updateControls();
		}
	}
	public void stopDaytimeRecord() {
		// Block the character
		getCharacter().setBlocked(true);
		updateControls();
	}
	private void startToolBar() {
		acm = new CharacterActionControlManager(getGameHandler(),getCharacter());
		acm.setRealmTurnPanel(this);
		acm.setIconStyle(getGameHandler().getIconSize());
		actionToolBar = new JToolBar();
		actionToolBar.setFloatable(false);
		acm.addActionButtons(actionToolBar);
		top.add(actionToolBar,"South");
	}
	public CharacterActionControlManager getActionControlManager() {
		return acm;
	}
	private void rollPendingActionsIntoView() {
		int showIndex = currentActionRow+2;
		while (showIndex>=actionTable.getRowCount()) {
			showIndex--;
		}
		Rectangle r = actionTable.getCellRect(showIndex,0,true);
		actionTable.scrollRectToVisible(r);
		actionTable.repaint();
	}
	public void doLosePhases(int phases) {
		// Find pending phases
		int pendingCount = 0;
		ArrayList<ActionRow> pendingPhases = new ArrayList<ActionRow>();
		for (ActionRow ar:actionRows) {
			if (ar.isPending()) {
				pendingPhases.add(ar);
				pendingCount += (ar.getPhaseCount()*ar.getCount());
			}
		}
		
		// Lose them
		if (phases>=pendingCount) {
			// Cancel all remaining phases
			for (ActionRow ar:pendingPhases) {
				ar.setCancelled(true);
			}
			JOptionPane.showMessageDialog(
					getMainFrame(),
					"Violent Storm caused the loss of all remaining phases.",
					"Violent Storm!!",
					JOptionPane.WARNING_MESSAGE);
		}
		else {
			ActionRowChooser chooser = new ActionRowChooser(getMainFrame(),"Violent Storm!!",phases);
			chooser.setActionRows(pendingPhases);
			chooser.setLocationRelativeTo(getMainFrame());
			chooser.setVisible(true);
		}
	}
}