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

import java.awt.Color;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.util.*;

import javax.swing.*;

import com.robin.magic_realm.components.ClearingDetail;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.attribute.DayAction.ActionId;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CharacterActionControlManager {
	private boolean recordingARed = false;
	
	public Action finishAction;		// finished recording
	public Action backAction;
	public Action hideAction;
	public Action moveAction;
	public Action searchAction;
	public Action tradeAction;
	public Action restAction;
	public Action alertAction;
	public Action hireAction;
	public Action followAction;		// select a character from a list (only characters in clearing)
	public Action spellAction;		// the computer will recognize first entry as SPX unless the character has some optional ability
	public Action peerAction;		// disabled if character is not in mountain clearing (I think) - then given a list of nearby clearings
	public Action flyAction;			// select a tile from a list of adjacent tiles
	public Action remoteSpellAction;
	public Action cacheAction;
	
	public Action healAction;
	public Action repairAction;
	public Action fortifyAction;
	
	public ActionIcon finishIcon = new ActionIcon("finish","Send","SND");
	public ActionIcon backIcon = new ActionIcon("backarrow","Delete","DEL");
	public ActionIcon hideIcon = new ActionIcon("hide","Hide",DayAction.HIDE_ACTION.getCode());
	public ActionIcon moveIcon = new ActionIcon("move","Move",DayAction.MOVE_ACTION.getCode());
	public ActionIcon searchIcon = new ActionIcon("search","Search",DayAction.SEARCH_ACTION.getCode());
	public ActionIcon tradeIcon = new ActionIcon("trade","Trade",DayAction.TRADE_ACTION.getCode());
	public ActionIcon restIcon = new ActionIcon("rest","Rest",DayAction.REST_ACTION.getCode());
	public ActionIcon alertIcon = new ActionIcon("alert","Alert",DayAction.ALERT_ACTION.getCode());
	public ActionIcon hireIcon = new ActionIcon("hire","Hire",DayAction.HIRE_ACTION.getCode());
	public ActionIcon followIcon = new ActionIcon("follow","Follow",DayAction.FOLLOW_ACTION.getCode());
	public ActionIcon spellIcon = new ActionIcon("spell","Spell",DayAction.SPELL_ACTION.getCode());
	public ActionIcon peerIcon = new ActionIcon("peer","Peer",DayAction.ENH_PEER_ACTION.getCode());
	public ActionIcon flyIcon = new ActionIcon("fly","Fly",DayAction.FLY_ACTION.getCode());
	public ActionIcon remoteSpellIcon = new ActionIcon("remotespell","Remote SP",DayAction.REMOTE_SPELL_ACTION.getCode());
	public ActionIcon cacheIcon = new ActionIcon("cache","Cache",DayAction.CACHE_ACTION.getCode());
	
	public ActionIcon healIcon = new ActionIcon("heal","Heal",DayAction.HEAL_ACTION.getCode());
	public ActionIcon repairIcon = new ActionIcon("repair","Repair",DayAction.REPAIR_ACTION.getCode());
	public ActionIcon fortifyIcon = new ActionIcon("fortify","Fortify",DayAction.FORTIFY_ACTION.getCode());

	private ActionIcon[] actionIcon = {
			finishIcon,
			backIcon,
			hideIcon,
			moveIcon,
			searchIcon,
			tradeIcon,
			restIcon,
			alertIcon,
			hireIcon,
			followIcon,
			spellIcon,
			peerIcon,
			flyIcon,
			remoteSpellIcon,
			cacheIcon,
			healIcon,
			repairIcon,
			fortifyIcon,
	};
	
	// Only ONE of these two should be NOT NULL
	private CharacterActionPanel cap = null;
	private RealmTurnPanel rtp = null;
	
	private RealmGameHandler gameHandler;
	private CharacterWrapper character;
	private String currentlyRecordingAction = null; // a holder that indicates the player is in the middle of recording an action
	private boolean timeToSend = false;
	
	public CharacterActionControlManager(RealmGameHandler gameHandler,CharacterWrapper character) {
		this.gameHandler = gameHandler;
		this.character = character;
		init();
	}
	public void setCharacterActionPanel(CharacterActionPanel cap) {
		this.cap = cap;
	}
	public void setRealmTurnPanel(RealmTurnPanel rtp) {
		this.rtp = rtp;
	}
	public RealmGameHandler getGameHandler() {
		return gameHandler;
	}
	public CharacterWrapper getCharacter() {
		return character;
	}
	public String getCurrentlyRecordingAction() {
		return currentlyRecordingAction;
	}
	public void setIconStyle(int iconStyle) {
		for (int i=0;i<actionIcon.length;i++) {
			actionIcon[i].setStyle(iconStyle);
		}
	}
	public void init() {
		// Init Actions
		finishAction = new AbstractAction("",finishIcon) {
			public void actionPerformed(ActionEvent ev) {
				if (ignoreHirelingWarning()) {
					doFinish();
				}
			}
		};
		backAction = new AbstractAction("",backIcon) {
			public void actionPerformed(ActionEvent ev) {
				if (character.isDoRecord()) {
					doBackspace();
				}
				else {
					// unsend!
					character.setDoRecord(true);
					getGameHandler().submitChanges();
					cap.getCharacterFrame().updateControls();
				}
			}
		};
		hideAction = new AbstractAction("",hideIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = hideIcon.isWarningOn();
				doRecord(DayAction.HIDE_ACTION.getCode());
			}
		};
		moveAction = new AbstractAction("",moveIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = moveIcon.isWarningOn();
				recordMoveAction();
			}
		};
		moveAction.putValue(Action.SHORT_DESCRIPTION,DayAction.MOVE_ACTION.getName());
		searchAction = new AbstractAction("",searchIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = searchIcon.isWarningOn();
				doRecord(DayAction.SEARCH_ACTION.getCode());
			}
		};
		searchAction.putValue(Action.SHORT_DESCRIPTION,DayAction.SEARCH_ACTION.getName());
		tradeAction = new AbstractAction("",tradeIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = tradeIcon.isWarningOn();
				doRecord(DayAction.TRADE_ACTION.getCode());
			}
		};
		tradeAction.putValue(Action.SHORT_DESCRIPTION,DayAction.TRADE_ACTION.getName());
		restAction = new AbstractAction("",restIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = restIcon.isWarningOn();
				if (rtp!=null) {
					String string = JOptionPane.showInputDialog("How Many?","1");
					if (string==null) {
						return;
					}
					int count = 0;
					try {
						count = Integer.valueOf(string).intValue();
					}
					catch(NumberFormatException ex) {
						// ignore
						return;
					}
					if (count>0) {
						doRecord(DayAction.REST_ACTION.getCode(),null,1,count);
					}
				}
				else {
					doRecord(DayAction.REST_ACTION.getCode());
				}
			}
		};
		restAction.putValue(Action.SHORT_DESCRIPTION,DayAction.REST_ACTION.getName());
		alertAction = new AbstractAction("",alertIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = alertIcon.isWarningOn();
				doRecord(DayAction.ALERT_ACTION.getCode());
			}
		};
		alertAction.putValue(Action.SHORT_DESCRIPTION,DayAction.ALERT_ACTION.getName());
		hireAction = new AbstractAction("",hireIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = hireIcon.isWarningOn();
				doRecord(DayAction.HIRE_ACTION.getCode());
			}
		};
		hireAction.putValue(Action.SHORT_DESCRIPTION,DayAction.HIRE_ACTION.getName());
		followAction = new AbstractAction("",followIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = followIcon.isWarningOn();
				recordFollowAction();
			}
		};
		followAction.putValue(Action.SHORT_DESCRIPTION,DayAction.FOLLOW_ACTION.getName());
		spellAction = new AbstractAction("",spellIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = spellIcon.isWarningOn();
				doRecord(DayAction.SPELL_ACTION.getCode()); // doRecord handles whether or not this is a spell prep
			}
		};
		spellAction.putValue(Action.SHORT_DESCRIPTION,DayAction.SPELL_ACTION.getName());
		peerAction = new AbstractAction("",peerIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = peerIcon.isWarningOn();
				recordEnhancedPeerAction();
			}
		};
		peerAction.putValue(Action.SHORT_DESCRIPTION,DayAction.ENH_PEER_ACTION.getName());
		flyAction = new AbstractAction("",flyIcon) {
			public void actionPerformed(ActionEvent ev) {
				// Select current or any adjacent tile
				recordingARed = flyIcon.isWarningOn();
				recordFlyAction();
			}
		};
		flyAction.putValue(Action.SHORT_DESCRIPTION,DayAction.FLY_ACTION.getName());
		remoteSpellAction = new AbstractAction("",remoteSpellIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = remoteSpellIcon.isWarningOn();
				recordRemoteSpellAction();
			}
		};
		remoteSpellAction.putValue(Action.SHORT_DESCRIPTION,DayAction.REMOTE_SPELL_ACTION.getName());
		cacheAction = new AbstractAction("",cacheIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = cacheIcon.isWarningOn();
				doRecord(DayAction.CACHE_ACTION.getCode());
			}
		};
		cacheAction.putValue(Action.SHORT_DESCRIPTION,DayAction.CACHE_ACTION.getName());
		healAction = new AbstractAction("",healIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = healIcon.isWarningOn();
				doRecord(DayAction.HEAL_ACTION.getCode());
			}
		};
		healAction.putValue(Action.SHORT_DESCRIPTION,DayAction.HEAL_ACTION.getName());
		repairAction = new AbstractAction("",repairIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = repairIcon.isWarningOn();
				doRecord(DayAction.REPAIR_ACTION.getCode());
			}
		};
		repairAction.putValue(Action.SHORT_DESCRIPTION,DayAction.REPAIR_ACTION.getName());
		fortifyAction = new AbstractAction("",fortifyIcon) {
			public void actionPerformed(ActionEvent ev) {
				recordingARed = fortifyIcon.isWarningOn();
				doRecord(DayAction.FORTIFY_ACTION.getCode());
			}
		};
		fortifyAction.putValue(Action.SHORT_DESCRIPTION,DayAction.FORTIFY_ACTION.getName());
	}
	public void addActionButtons(JToolBar toolbar) {
		toolbar.add(hideAction).setToolTipText(hideIcon.getText());
		toolbar.add(moveAction).setToolTipText(moveIcon.getText());
		toolbar.add(searchAction).setToolTipText(searchIcon.getText());
		toolbar.add(restAction).setToolTipText(restIcon.getText());
		toolbar.add(alertAction).setToolTipText(alertIcon.getText());
		toolbar.add(spellAction).setToolTipText(spellIcon.getText());
		toolbar.add(tradeAction).setToolTipText(tradeIcon.getText());
		toolbar.add(hireAction).setToolTipText(hireIcon.getText());
		toolbar.add(followAction).setToolTipText(followIcon.getText());
		toolbar.addSeparator();
		toolbar.add(peerAction).setToolTipText(peerIcon.getText());
		toolbar.add(flyAction).setToolTipText(flyIcon.getText());
		toolbar.add(remoteSpellAction).setToolTipText(remoteSpellIcon.getText());
		toolbar.addSeparator();
		toolbar.add(cacheAction).setToolTipText(cacheIcon.getText());
		if (character.hasSpecialActions()) {
			toolbar.addSeparator();
			if (character.hasSpecialAction(ActionId.Heal)) {
				toolbar.add(healAction).setToolTipText(healIcon.getText());
			}
			if (character.hasSpecialAction(ActionId.Repair)) {
				toolbar.add(repairAction).setToolTipText(repairIcon.getText());
			}
			if (character.hasSpecialAction(ActionId.Fortify)) {
				toolbar.add(fortifyAction).setToolTipText(fortifyIcon.getText());
			}
		}
	}
	private void startMapSelect(String actionName,String text) {
		currentlyRecordingAction = actionName;
		
		if (cap!=null) {
			cap.getCharacterFrame().updateControls();
		}
		if (rtp!=null) {
			rtp.updateControls();
		}
		try {
			// Get the map to pop to the forefront, centered on the clearing, and the move possibilities marked
			getGameHandler().getInspector().setIcon(false);
			getGameHandler().getInspector().toFront();
			getGameHandler().getInspector().getMap().setMarkClearingAlertText(text);
			getGameHandler().getInspector().getMap().addMouseListener(recordMapClickListener);
			getGameHandler().getInspector().setSelected(true);
		}
		catch(PropertyVetoException ex) {
			ex.printStackTrace();
			// huh?  why would this happen?  what should I do if it does?
		}
	}
	private void finishMapSelect(TileLocation tl) {
		String theAction = currentlyRecordingAction;
		currentlyRecordingAction = null;
		getGameHandler().getInspector().getMap().removeMouseListener(recordMapClickListener);
		if (!tl.hasClearing() || !tl.clearing.getMarkColor().equals(Color.red)) { // only record if not a "cancel" clearing
			if (DayAction.MOVE_ACTION.getCode().equals(theAction) || DayAction.FLY_ACTION.getCode().equals(theAction)) {
				
				if (DayAction.FLY_ACTION.getCode().equals(theAction)) {
					tl.setFlying(true);
				}
				int cost = tl.hasClearing()?tl.clearing.moveCost(getCharacter()):1;
				boolean continueWithRecord = true;
				TileLocation current = getCharacter().getPlannedLocation();
				if (current.isBetweenClearings() && tl.hasClearing() && !(current.contains(tl.clearing) && getCharacter().canMoveToClearing(tl.clearing))) {
					
					ArrayList<ClearingDetail> clearings = character.findAvailableClearingMoves();
					if (clearings.size()!=1 || !clearings.get(0).equals(tl.clearing)) {
						JOptionPane.showMessageDialog(
								getGameHandler().getMainFrame(),
								"You MUST record a VALID move when between clearings!",
								"Invalid MOVE",
								JOptionPane.WARNING_MESSAGE);
						continueWithRecord = false;
					}
				}
				else if (current.isBetweenTiles() && tl.hasClearing()) {
					JOptionPane.showMessageDialog(
							getGameHandler().getMainFrame(),
							"You MUST record a VALID fly when between tiles!",
							"Invalid FLY",
							JOptionPane.WARNING_MESSAGE);
					continueWithRecord = false;
				}
				else if (tl.hasClearing() && tl.clearing.isEdge()) {
					if (character.isCharacter()) {
						int ret = JOptionPane.showConfirmDialog(
								getGameHandler().getMainFrame(),
								"You are plotting to leave the map, which will remove your character from the game.\n\n"
								+"Is this what you want to do?",
								character.getGameObject().getName()+" Leaving the Map",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (ret==JOptionPane.NO_OPTION) {
							continueWithRecord = false;
						}
					}
					else {
						JOptionPane.showMessageDialog(
								getGameHandler().getMainFrame(),
								"Non-characters cannot leave the map by themselves.",
								character.getGameObject().getName()+" Leaving the Map",
								JOptionPane.WARNING_MESSAGE);
						continueWithRecord = false;
					}
				}
				if (continueWithRecord) {
					if (doRecord(theAction,tl,cost)) {
						if (getCharacter().getClearingPlot()==null) {
							getCharacter().rebuildClearingPlot();
						}
						getCharacter().getClearingPlot().add(tl); // NPE - is this after reloading a game where actions were recorded?
						if (rtp!=null) {
							updateControls(rtp.getPhaseManager(),true,false);
						}
						if (cap!=null) {
							getGameHandler().getInspector().getMap().setClearingPlot(new ArrayList(getCharacter().getClearingPlot()));
						}
					}
				}
			}
			else {
				doRecord(theAction,tl,1);
			}
		}
		getGameHandler().getInspector().getMap().clearMarkClearingAlertText();
		getGameHandler().getInspector().getMap().markAllTiles(false);
		getGameHandler().getInspector().getMap().markAllClearings(false);
		getGameHandler().getInspector().getMap().markAllMapEdges(false);
		if (cap!=null) {
			cap.getCharacterFrame().updateControls();
			cap.getCharacterFrame().toFront();
			try {
				cap.getCharacterFrame().setSelected(true);
			}
			catch(PropertyVetoException ex) {
				// sheesh!
			}
		}
		if (rtp!=null) {
			rtp.updateControls();
			rtp.getCharacterFrame().toFront();
			try {
				rtp.getCharacterFrame().setSelected(true);
			}
			catch(PropertyVetoException ex) {
				// sheesh!
			}
		}
	}
	private void doFinish() {
		if (getGameHandler().isOption(RealmSpeakOptions.INCOMPLETE_PHASE_WARNING) && canStillRecord()) {
			int ret = JOptionPane.showConfirmDialog(
					getGameHandler().getMainFrame(),
					"You still have phases to record.  Send anyway?",
					"Incomplete Record Warning!",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (ret==JOptionPane.NO_OPTION) {
				return;
			}
		}
		// cap better NOT be null here!
		cap.getCharacterFrame().setCurrentPhaseManager(null);
		cap.scrollActionTableToVisible();
		
		getGameHandler().getInspector().getMap().clearClearingPlot();
		getCharacter().setPonyLock(false);
		getCharacter().setDoRecord(false);
		getGameHandler().submitChanges();
		getGameHandler().showNextRecordFrame();
		
		cap.getCharacterFrame().updateControls();
	}
	private void doBackspace() {
		// cap better NOT be null here!
		cap.scrollActionTableToVisible();
		
		ArrayList actions = new ArrayList();
		Collection c = getCharacter().getCurrentActions();
		if (c!=null && !c.isEmpty()) {
			actions.addAll(c);
			String removed = (String)actions.remove(actions.size()-1);
			if (removed.startsWith(DayAction.MOVE_ACTION.getCode()) || removed.startsWith(DayAction.FLY_ACTION.getCode())) {
				// deleting a move, so delete a clearing plot
				getCharacter().chompClearingPlot();
			}
		}
		if (cap!=null) {
			if (getCharacter().getClearingPlot()==null) {
				getCharacter().rebuildClearingPlot();
			}
			getGameHandler().getInspector().getMap().setClearingPlot(new ArrayList(getCharacter().getClearingPlot()));
		}
		getCharacter().setCurrentActions(actions);
		
		// Don't forget to delete the actionTypeCode entry
		ArrayList actionTypeCodes = new ArrayList();
		c = getCharacter().getCurrentActionTypeCodes();
		if (c!=null && !c.isEmpty()) {
			actionTypeCodes.addAll(c);
			actionTypeCodes.remove(actionTypeCodes.size()-1);
		}
		getCharacter().setCurrentActionTypeCodes(actionTypeCodes);
		
		// And the valids (aigh)
		ArrayList valids = new ArrayList();
		c = getCharacter().getCurrentActionValids();
		if (c!=null && !c.isEmpty()) {
			valids.addAll(c);
			valids.remove(valids.size()-1);
		}
		getCharacter().setCurrentActionValids(valids);
		
		PhaseManager pm = getCharacter().getPhaseManager(true);
		getCharacter().updatePhaseManagerWithCurrentActions(pm);
		cap.getCharacterFrame().setCurrentPhaseManager(pm);
		
		cap.actionHistoryTable.revalidate();
		cap.actionHistoryTable.repaint();
		cap.getCharacterFrame().updateControls();
	}
	private void recordMoveAction() {
		// Check to see if there is a previous HIRE action, and warn the player accordingly
		if (getGameHandler().isOption(RealmSpeakOptions.MOVE_AFTER_HIRE_WARNING)
				&& getCharacter().hasCurrentAction(DayAction.HIRE_ACTION.getCode())) {
			JOptionPane.showMessageDialog(
					getGameHandler().getMainFrame(),
					"If you successfully hire a hireling, and move away, you may be unable to assign them\n"
					+"to an appropriate guide.",
					"Move after Hire Warning!",
					JOptionPane.WARNING_MESSAGE);
		}
		
		// Set up the MOVE action
		if (rtp!=null) { // If this is live, then the clearing plot is meaningless
			getCharacter().resetClearingPlot();
		}
		TileLocation tl = getCharacter().getPlannedLocation();
		ArrayList<ClearingDetail> ac = getCharacter().findAvailableClearingMoves();
		ArrayList<ClearingDetail> pc = getCharacter().findPossibleClearingMoves();
		getGameHandler().getInspector().getMap().markClearings(ac,true);
		getGameHandler().getInspector().getMap().markClearings(pc,true,Color.yellow);
		if (tl.isInClearing()) {
			ClearingDetail cc = tl.clearing;
			cc.setMarked(true);
			cc.setMarkColor(Color.yellow);
		}
		startMapSelect(DayAction.MOVE_ACTION.getCode(),"MOVE to which clearing?");
		getGameHandler().getInspector().getMap().centerOn(tl);
	}
	public void recordExternalMoveAction(TileLocation tl) {
		if (currentlyRecordingAction==null && moveAction.isEnabled()) {
//			ignoreHirelingWarning();
			currentlyRecordingAction = DayAction.MOVE_ACTION.getCode();
			recordingARed = moveIcon.isWarningOn();
			if (!recordingARed) {
				recordingARed = !getCharacter().canMoveToClearing(tl.clearing);
			}
			finishMapSelect(tl);
		}
	}
	private void recordFollowAction() {
		// Find all characters that are not this character in the clearing (no clearing validation needed here)
		ArrayList list = new ArrayList();
		TileLocation current = getCharacter().getCurrentLocation(); // since FOLLOW must the first and only action, current is good
		for (Iterator n=current.clearing.getClearingComponents().iterator();n.hasNext();) {
			RealmComponent rc = (RealmComponent)n.next();
			// Someone, that isn't yourself
			if (rc.isPlayerControlledLeader() && !rc.getGameObject().equals(getCharacter().getGameObject())) {
				list.add(rc);
			}
		}
		
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getGameHandler().getMainFrame(),"Who do you want to follow?",true);
		chooser.addRealmComponents(list,true);
		chooser.setVisible(true);
		String selText = chooser.getSelectedText();
		if (selText!=null) {
			String id = chooser.getFirstSelectedComponent().getGameObject().getStringId();
			doRecord(DayAction.FOLLOW_ACTION.getCode()+"("+selText+")~"+id);
		}
	}
	private void recordFlyAction() {
		TileLocation planned = getCharacter().getPlannedLocation();
		
		if (planned.isBetweenTiles()) {
			// between tiles
			planned.tile.setMarked(true);
			planned.getOther().tile.setMarked(true);
			getGameHandler().getInspector().getMap().setReplot(true);
			getGameHandler().getInspector().getMap().repaint();
		}
		else {
			// Mark tiles
			// TODO Should mark tiles in yellow if unable to fly at time of recording!  green if they are good to go.
			getGameHandler().getInspector().getMap().markAdjacentTiles(planned.tile,true);
			planned.tile.setMarked(true);
//			planned.tile.doRepaint(); // LEAVE FOR AWHILE
		}
		startMapSelect(DayAction.FLY_ACTION.getCode(),"FLY to which tile?");
		getGameHandler().getInspector().getMap().centerOn(planned);
	}
	private void recordEnhancedPeerAction() {
		/* 
		 * Enhance Peer logic: (this was very tricky!)
		 * 
		 * 1)  Suppose Crystal Ball and Telescope are active:  Need to check all previous enh peers.  If
		 * there are any with mountain-to-mountain clearing peers, then the telescope was "used" for that,
		 * and all the rest are Crystal Ball peers.  If all the phases are used up, and none of them were
		 * mountain-to-mountain, then behave like (3) below
		 * 
		 * 2)  Only Crystal Ball:  Nothing special needs be checked or required - all clearings are available
		 * 
		 * 3)  Only Telescope:  Require a peer into a mountain clearing.  Should already be required to
		 * be IN a mountain clearing from the actionCanBeAdded() method.
		 * 
		 * x)  Witch's Familiar or Phantasm can only PEER in current clearing
		 * 
		 * Seems like a check for the telescope is the first order of business.
		 * 
		 * NEW PROBLEM - its possible to record enhanced peer actions anytime, even when actionCanBeAdded()
		 * is false, because a character can *plan* to pick up an item, and gain its behavior later!  Ouch.
		 * Need to make sure that is covered here.
		 */
		
		TileLocation tl = getCharacter().getPlannedLocation();
		if (getCharacter().isMinion()) {
			// no need to ask user for a clearing - it only works in current clearing!
			doRecord(DayAction.ENH_PEER_ACTION.getCode(),tl,1);
		}
		else {
			boolean enhancedPeer = getCharacter().hasActiveInventoryThisKeyAndValue(Constants.SPECIAL_ACTION,"ENHANCED_PEER");
			boolean mtToMtPeer = tl!=null && tl.hasClearing() && tl.clearing.isMountain() && getCharacter().hasActiveInventoryThisKey(Constants.MOUNTAIN_PEER);
			if (mtToMtPeer && enhancedPeer) {
				// The player has the Crystal Ball AND Ancient Telescope.  Now its necessary to see what restrictions there
				// are for this peer.  If the player has done nothing but non-mt-to-mt peers, we need to know if he/she
				// can continue, or must use the Telescope
				PhaseManager pm = getCharacter().getPhaseManager(true);
				getCharacter().updatePhaseManagerWithCurrentActions(pm);
				if (pm.getTotal()==1) {
					// Must be on "free action" time, which means we need to see if a mt-to-mt peer was
					// ever performed (only need one!)
					Iterator atci = getCharacter().getCurrentActionTypeCodes().iterator();
					for (Iterator i=getCharacter().getCurrentActions().iterator();i.hasNext();) {
						String detailAction = (String)i.next();
						String actionTypeCode = (String)atci.next();
						if (detailAction.startsWith(DayAction.ENH_PEER_ACTION.getCode()) && "MM".equals(actionTypeCode)) {
							// Yes, a mountain to mountain peer was used, so mountainPeer isn't require
							// for the extra phase!
							mtToMtPeer = false;
							break;
						}
					}
				}
				else {
					// If we are NOT on a free action, then the player could be using the Crystal Ball
					mtToMtPeer = false;
				}
			}
			if (mtToMtPeer) {
				getGameHandler().getInspector().getMap().markClearings("mountain",true);
			}
			else {
				getGameHandler().getInspector().getMap().markAllClearings(true);
			}
			
			if (tl.hasClearing()) {
				tl.clearing.setMarked(false); // exclude current clearing
			}
			
			for(ClearingDetail clearing:getGameHandler().getInspector().getMap().getAllMarkedClearings()) {
				if (clearing.getParent().getGameObject().hasThisAttribute(Constants.SP_NO_PEER)) {
					clearing.setMarked(false);
				}
				else if (!enhancedPeer && !mtToMtPeer) {
					clearing.setMarkColor(Color.red);
				}
			}
			
			startMapSelect(DayAction.ENH_PEER_ACTION.getCode(),"PEER in which clearing?");
			getGameHandler().getInspector().getMap().centerOn(tl);
		}
	}
	private void recordRemoteSpellAction() {
		Collection c = getCharacter().getCurrentActions();
		if (!c.contains(DayAction.SPELL_PREP_ACTION.getCode())) {
			boolean canSkipSpellPrep = getCharacter().getGameObject().hasAttribute(Constants.OPTIONAL_BLOCK,Constants.NO_SPX)
										|| getCharacter().affectedByKey(Constants.NO_SPX);
			if (!canSkipSpellPrep) {
				doRecord(DayAction.SPELL_PREP_ACTION.getCode());
				return;
			}
		}
		
		if (character.hasActiveInventoryThisKeyAndValue(Constants.SPECIAL_ACTION,"REMOTE_SPELL")) {
			// Must be the crystal ball, and all clearings are available for selection
			getGameHandler().getInspector().getMap().markAllClearings(true);
		}
		
		TileLocation tl = getCharacter().getPlannedLocation();
		if (tl.hasClearing()) {
			tl.clearing.setMarked(false); // exclude current clearing
		}
		
		startMapSelect(DayAction.REMOTE_SPELL_ACTION.getCode(),"REMOTE SPELL in which clearing?");
		getGameHandler().getInspector().getMap().centerOn(tl);
	}
	private boolean doRecord(String action) {
		return doRecord(action,null,1,1);
	}
	private boolean doRecord(String action,TileLocation actionLocation,int cost) {
		return doRecord(action,actionLocation,cost,1);
	}
	private boolean doRecord(String action,TileLocation actionLocation,int cost,int count) {
		if (cap!=null) {
			cap.scrollActionTableToVisible();
		}
		
		// Record an actionTypeCode to match the action (actionTypeCode is the type of clearing from which the action was performed)
		String actionTypeCode = "*";
		TileLocation tl = getCharacter().getPlannedLocation();
		if (tl.hasClearing()) {
			actionTypeCode = tl.clearing.getTypeCode();
		}
		if (actionLocation!=null && actionLocation.hasClearing()) {
			actionTypeCode = actionTypeCode+actionLocation.clearing.getTypeCode(); // ie., MM for mt-to-mt
		}
		
		Collection c = getCharacter().getCurrentActions();
		if (c==null || c.isEmpty()) {
			// Recording the first action?  Reset the clearingPlot.
			ArrayList plot = new ArrayList();
			plot.add(getCharacter().getCurrentLocation()); // start the plot off with the current location
			getCharacter().setClearingPlot(plot);
			c = new ArrayList();
		}
		if (action.equals(DayAction.SPELL_ACTION.getCode()) && !c.contains(DayAction.SPELL_PREP_ACTION.getCode())) {
			boolean canSkipSpellPrep = getCharacter().getGameObject().hasAttribute(Constants.OPTIONAL_BLOCK,Constants.NO_SPX)
									|| getCharacter().affectedByKey(Constants.NO_SPX);
			if (!canSkipSpellPrep) {
				action = DayAction.SPELL_PREP_ACTION.getCode();
			}
		}
		String detailAction = action;
		if (DayAction.MOVE_ACTION.getCode().equals(detailAction) && character.isPonyLock()) {
			detailAction = detailAction+"!";
		}
		if (actionLocation!=null) {
			if (actionLocation.hasClearing()) {
				detailAction = detailAction+"-"+actionLocation.clearing.getShorthand();
			}
			else {
				detailAction = detailAction+"-"+actionLocation.tile.getTileCode();
			}
		}
		
		String recordAction = detailAction;
		while(cost>1) {
			recordAction = recordAction+","+detailAction;
			cost--;
		}
		
		if (rtp!=null) {
			rtp.processNewAction(recordAction,actionTypeCode,count);
			ActionRow ar = rtp.getLastActionRow();
			if (ar==null || ar.isCancelled() || !ar.isCompleted()) {
				// Don't actually record cancelled actions during daylight
				return false;
			}
		}
		
		for (int i=0;i<count;i++) {
			getCharacter().addCurrentActionTypeCode(actionTypeCode);
			getCharacter().addCurrentAction(recordAction);
			getCharacter().addCurrentActionValid(!recordingARed);
		}
//System.out.println("Recording "+recordAction+" with warning "+(recordingARed?"on":"off"));
		
		PhaseManager pm = getCharacter().getPhaseManager(true);
		getCharacter().updatePhaseManagerWithCurrentActions(pm);
		if (cap!=null) {
			cap.getCharacterFrame().setCurrentPhaseManager(pm);
			
			cap.actionHistoryTable.revalidate();
			cap.actionHistoryTable.repaint();
			cap.getCharacterFrame().updateControls();
		}
//		else if (rtp!=null) {
//			rtp.processNewAction(recordAction);
//		}
		return true;
	}
	public void updateControls(PhaseManager pm,boolean recordingActions,boolean birdsong) {
		TileLocation planned = getCharacter().getPlannedLocation();
		finishAction.setEnabled(getCharacter().isActive() && recordingActions && ((!planned.isBetweenClearings() && !planned.isBetweenTiles()) || getCharacter().canDoDaytimeRecord()));
		boolean canBackspace = getCharacter().isActive() && recordingActions && getCharacter().getCurrentActionCount()>0;
		boolean canUnsend = getCharacter().isActive() && birdsong && !getCharacter().isDoRecord();
		backAction.setEnabled(canBackspace || canUnsend);
		timeToSend = finishAction.isEnabled();
		
		boolean inCave = getCharacter().isActive() && planned!=null && planned.isInClearing() && planned.clearing.isCave();
		boolean pony = getCharacter().isActive() && getCharacter().isPonyActive();
		boolean usingPony = pony&&!inCave;
		boolean followOk = !backAction.isEnabled();
		if (birdsong) {
			if (getCharacter().hasCurrentAction(DayAction.FOLLOW_ACTION.getCode())) {
				recordingActions = false;
			}
			else if (getCharacter().canDoDaytimeRecord()) {
				recordingActions = false;
			}
		}
		else if (character.isBlocked()) {
			recordingActions = false;
		}
		
		handleInvalidAction(hideAction,hideIcon,willBeRed(pm,DayAction.HIDE_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(moveAction,moveIcon,willBeRed(pm,DayAction.MOVE_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(searchAction,searchIcon,willBeRed(pm,DayAction.SEARCH_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(restAction,restIcon,willBeRed(pm,DayAction.REST_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(alertAction,alertIcon,willBeRed(pm,DayAction.ALERT_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(spellAction,spellIcon,willBeRed(pm,DayAction.SPELL_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(tradeAction,tradeIcon,willBeRed(pm,DayAction.TRADE_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(hireAction,hireIcon,willBeRed(pm,DayAction.HIRE_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(peerAction,peerIcon,willBeRed(pm,DayAction.ENH_PEER_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(flyAction,flyIcon,willBeRed(pm,DayAction.FLY_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(remoteSpellAction,remoteSpellIcon,willBeRed(pm,DayAction.REMOTE_SPELL_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(cacheAction,cacheIcon,willBeRed(pm,DayAction.CACHE_ACTION,usingPony,planned),birdsong,recordingActions);
		
		handleInvalidAction(healAction,healIcon,willBeRed(pm,DayAction.HEAL_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(repairAction,repairIcon,willBeRed(pm,DayAction.REPAIR_ACTION,usingPony,planned),birdsong,recordingActions);
		handleInvalidAction(fortifyAction,fortifyIcon,willBeRed(pm,DayAction.FORTIFY_ACTION,usingPony,planned),birdsong,recordingActions);
		
		followAction.setEnabled(finishAction.isEnabled() && getCharacter().isActive() && rtp==null && followOk && getCharacter().actionIsValid(DayAction.FOLLOW_ACTION.getCode(),getCharacter().getCurrentLocation()));
		if (followAction.isEnabled()) {
			timeToSend = false;
		}
		if (getCharacter().hasCurrentAction(DayAction.FOLLOW_ACTION.getCode())) {
			timeToSend = true;
		}
	}
	private void handleInvalidAction(Action action,ActionIcon icon,boolean red,boolean birdsong,boolean recordingActions) {
		if (character.isGone()) {
			action.setEnabled(false);
			return;
		}
		if (birdsong) {
			icon.setWarningOn(red);
			action.setEnabled(recordingActions);
			if (!red) {
				timeToSend = false;
			}
		}
		else {
			action.setEnabled(!red && recordingActions);
			if (action.isEnabled()) {
				timeToSend = false;
			}
		}
		if (currentlyRecordingAction!=null) {
			action.setEnabled(false);
		}
	}
	public boolean canStillRecord() {
		return !timeToSend;
	}
	private boolean willBeRed(PhaseManager pm,DayAction action,boolean usingPony,TileLocation planned) {
		if (getCharacter().actionIsValid(action.getCode(),planned)) {
			boolean validAction = pm.canAddAction(action.getCode(),usingPony);
			return !validAction;
		}
		return true;
	}
	private boolean ignoreHirelingWarning() {
		if (getGameHandler().isOption(RealmSpeakOptions.UNASSIGNED_HIRELINGS_WARNING)) {
			boolean unassignedHirelings = false;
			
			for (Iterator i=getCharacter().getAllHirelings().iterator();i.hasNext();) {
				RealmComponent hireling = (RealmComponent)i.next();
				if (!hireling.isHiredLeader()) {
					RealmComponent heldBy = hireling.getHeldBy();
					if (heldBy.isTile()) {
						unassignedHirelings = true;
						break;
					}
				}
			}
			if (unassignedHirelings) {
				int ret = JOptionPane.showConfirmDialog(
						getGameHandler().getMainFrame(),
						"You have unassigned underlings!  Birdsong (now) is the\n"
						+"only time you may assign hirelings to a guide.\n"
						+"Do you want to proceed without assigning your hirelings?",
						"Unassigned Hirelings!!",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				return ret==JOptionPane.YES_OPTION;
			}
		}
		return true;
	}
	private MouseListener recordMapClickListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent ev) {
			TileLocation tl = getGameHandler().getInspector().getMap().getTileLocationAtPoint(ev.getPoint());
			if (tl!=null) {
				if (tl.tile.isMarked()) {
					tl.clearing = null; // clicking a marked tile nullifies any clearing result
					finishMapSelect(tl);
				}
				if (tl.hasClearing()/* && tl.clearing.isMarked()*/) { // can select ANY clearing
					finishMapSelect(tl);
				}
			}
		}
	};
}