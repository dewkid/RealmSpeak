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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.JSplitPaneImproved;
import com.robin.general.swing.MouseUtility;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.QuestMinorCharacter;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class CharacterInventoryPanel extends CharacterFramePanel {

	protected JSplitPane treasureSplitPane;
	protected RealmObjectPanel activeInventoryObjectPanel;
	protected RealmObjectPanel inactiveInventoryObjectPanel;
	protected JButton dropInventoryButton;
	protected JButton abandonInventoryButton;
	protected JButton activateInventoryButton;
	protected JButton deactivateInventoryButton;
	protected JButton showInventoryDetailButton;
	protected JButton distributeInventoryButton; // development rules only
	
	private RealmObjectPanel queryPanel; // used for inventory queries of awakened spells
	
	public CharacterInventoryPanel(CharacterFrame parent) {
		super(parent);
		init();
	}
	private void init() {
		boolean development = getHostPrefs().hasPref(Constants.EXP_DEVELOPMENT) && getCharacter().isCharacter();
		setLayout(new BorderLayout(5,5));
			activeInventoryObjectPanel = new RealmObjectPanel(true,false);
			activeInventoryObjectPanel.activateFlipView();
			activeInventoryObjectPanel.setOpaque(true);
			activeInventoryObjectPanel.setBackground(new Color(255,255,204));
			activeInventoryObjectPanel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
			activeInventoryObjectPanel.addSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent ev) {
					if (activeInventoryObjectPanel.getSelectedCount()>0) {
						inactiveInventoryObjectPanel.clearSelected();
					}
					getCharacterFrame().updateControls();
				}
			});
			activeInventoryObjectPanel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent ev) {
					if (ev.getClickCount()==2 || MouseUtility.isRightOrControlClick(ev)) {
						GameObject go = activeInventoryObjectPanel.getSelectedGameObject();
						if (go!=null) {
							if (go.hasThisAttribute("gold_special")) {
								if (go.hasThisAttribute("daysLeft")) {
									GoldSpecialChitComponent gsrc = (GoldSpecialChitComponent)RealmComponent.getRealmComponent(go);
									gsrc.display(getMainFrame(),getCharacter());
								}
							}
							else {
								showAwakenedSpells(go);
							}
						}
					}
				}
			});
			inactiveInventoryObjectPanel = new RealmObjectPanel(true,false);
			inactiveInventoryObjectPanel.activateFlipView();
			inactiveInventoryObjectPanel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
			inactiveInventoryObjectPanel.addSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent ev) {
					if (inactiveInventoryObjectPanel.getSelectedCount()>0) {
						activeInventoryObjectPanel.clearSelected();
					}
					getCharacterFrame().updateControls();
				}
			});
			inactiveInventoryObjectPanel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent ev) {
					if (ev.getClickCount()==2 || MouseUtility.isRightOrControlClick(ev)) {
						GameObject go = inactiveInventoryObjectPanel.getSelectedGameObject();
						showAwakenedSpells(go);
					}
				}
			});
		JScrollPane activeScroll = new JScrollPane(activeInventoryObjectPanel);
		activeScroll.setOpaque(true);
		activeScroll.setBackground(new Color(255,255,204));
		JScrollPane inactiveScroll = new JScrollPane(inactiveInventoryObjectPanel);
		treasureSplitPane = new JSplitPaneImproved(JSplitPane.HORIZONTAL_SPLIT,activeScroll,inactiveScroll);
		treasureSplitPane.setDividerLocation(0.5);
		add(treasureSplitPane,"Center");
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			
			if (development) {
				distributeInventoryButton = new JButton("Distribute");
				distributeInventoryButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						doDistribute();
					}
				});
				box.add(distributeInventoryButton);
				box.add(Box.createHorizontalGlue());
			}
			
			if (getCharacterFrame().hostPrefs.hasPref(Constants.ADV_DROPPING)) {
				dropInventoryButton = new JButton("Drop");
				dropInventoryButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						doDrop(true);
					}
				});
				box.add(dropInventoryButton);
				box.add(Box.createHorizontalGlue());
			}
			
			abandonInventoryButton = new JButton("Abandon");
			abandonInventoryButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					doDrop(false);
				}
			});
			box.add(abandonInventoryButton);
			box.add(Box.createHorizontalGlue());
			
			activateInventoryButton = new JButton("Activate");
			activateInventoryButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					doActivate();
				}
			});
			box.add(activateInventoryButton);
			box.add(Box.createHorizontalGlue());
			deactivateInventoryButton = new JButton("Deactivate");
			deactivateInventoryButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					doDeactivate();
				}
			});
			box.add(deactivateInventoryButton);
			box.add(Box.createHorizontalGlue());
			
			showInventoryDetailButton = new JButton("Detail...");
			showInventoryDetailButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					showInventoryDetail();
				}
			});
			box.add(showInventoryDetailButton);
			box.add(Box.createHorizontalGlue());
			
		add(box,"South");
	}
	private static final Font queryFont = new Font("Dialog",Font.BOLD,14);
	private void showAwakenedSpells(GameObject go) {
		if (go!=null && go.hasThisAttribute("treasure") && (go.hasThisAttribute("magic") || go.hasThisAttribute("book"))) {
			Collection c = SpellUtility.getSpells(go,Boolean.TRUE,false,true);
			if (c.size()>0) {
				JPanel panel = new JPanel(new BorderLayout());
				queryPanel = new RealmObjectPanel();
				queryPanel.addObjects(c);
				queryPanel.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent ev) {
						Component c = queryPanel.getComponentAt(ev.getPoint());
						if (c!=null && c instanceof SpellCardComponent) {
							SpellCardComponent sc = (SpellCardComponent)c;
							SpellWrapper spell = new SpellWrapper(sc.getGameObject());
							SpellInfoDialog.showSpellInfo(getGameHandler().getMainFrame(), spell);
						}
						
					}
				});
				panel.add(queryPanel,"Center");
				
				JLabel label = new JLabel("Click spell for more info",JLabel.CENTER);
				label.setOpaque(true);
				label.setBackground(MagicRealmColor.PALEYELLOW);
				label.setFont(queryFont);
				panel.add(label,"North");
				JOptionPane.showMessageDialog(getGameHandler().getMainFrame(),panel,go.getName()+" Awakened Spells",JOptionPane.PLAIN_MESSAGE);
			}
		}
	}
	private boolean actionLocked(GameObject thing) {
		// FIXME This isn't exactly right.  Just because an item is in the PhaseManager, doesn't mean it was
		// necessarily used...
//		RealmTurnPanel turnPanel = getCharacterFrame().getTurnPanel();
//		return (turnPanel!=null && turnPanel.getPhaseManager().getAllGameObjects().contains(thing));
		return false; // for now
	}
	private void doDistribute() {
		// Find all characters in the clearing
		TileLocation tl = getCharacter().getCurrentLocation();
		if (tl!=null && tl.isInClearing()) {
			ArrayList<CharacterWrapper> allCharacters = new ArrayList<CharacterWrapper>();
			double allGold = 0.0;
			ArrayList<GameObject> allInventory = new ArrayList<GameObject>();
			ArrayList<RealmComponent> c = tl.clearing.getClearingComponents(false);
			for (RealmComponent rc:c) {
				if (rc.isPlayerControlledLeader()) {
					CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
					if (!character.hasCurse(Constants.ASHES)) {
						allGold += character.getGold();
					}
					allInventory.addAll(character.getInventory());
					if (rc.isCharacter()) {
						allCharacters.add(character);
					}
				}
			}
			
			if (allCharacters.size()>1 && (allGold>0.0 || !allInventory.isEmpty())) {
				// First, make sure there aren't any local changes (so we can roll back)
				getGameHandler().submitChanges();
				
				// Now, cycle through characters, and give them all the inventory and gold one at a time, and evaluate development levels
				Hashtable<GameObject,DevelopmentProgress> hash = new Hashtable<GameObject,DevelopmentProgress>();
				for (CharacterWrapper character:allCharacters) {
					character.setGold(allGold);
					for (GameObject item:allInventory) {
						item.removeThisAttribute(Constants.ACTIVATED);
						character.getGameObject().add(item);
						// boots are a special case, because they can determine whether or not you score points for heavy items
						if (item.hasThisAttribute("boots")) {
							// Make sure character is not prohibited from wearing boots (only affects custom characters)
							ArrayList list = character.getGameObject().getThisAttributeList(Constants.ITEM_RESTRICTIONS);
							if (list==null || !list.contains("Boots")) {
								// Note:  shouldn't have to worry about boots that are too small for a character, because it wont affect them in any case!
								item.setThisAttribute(Constants.ACTIVATED);
							}
						}
					}
					DevelopmentProgress dp = DevelopmentProgress.createDevelopmentProgress(getHostPrefs(),character);
					hash.put(character.getGameObject(),dp);
				}
				
				// Roll back all changes so that everything goes back to the way it was
				getGameHandler().getClient().getGameData().rollback();
				
				// Finally, reapply the development progress to all characters and submit
				StringBuffer sb = new StringBuffer();
				for (CharacterWrapper character:allCharacters) {
					boolean gain = false;
					DevelopmentProgress dp = hash.get(character.getGameObject());
					sb.append(character.getGameObject().getName());
					sb.append(": ");
					if (dp.getHighestVps()>character.getHighestEarnedVps()) {
						character.setHighestEarnedVps(dp.getHighestVps());
						sb.append("Highest Earned VP Score is now "+dp.getHighestVps());
						gain= true;
					}
					
					int stage = character.getCharacterStage();
					dp.updateStage();
					int diff = character.getCharacterStage()-stage;
					if (diff>0) {
						sb.append(" -- Gained ");
						sb.append(diff);
						sb.append(" Stage");
						sb.append(diff==1?"":"s");
						sb.append("!");
						gain= true;
					}
					if (!gain) {
						sb.append("No Gain");
					}
					sb.append("\n");
				}
				getGameHandler().submitChanges();
				getGameHandler().updateCharacterFrames();
				
				JOptionPane.showMessageDialog(getMainFrame(),sb,"Distribution Results",JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	private void doDrop(boolean plainSight) {
		Inventory selInv = getSelectedInventory();
		if (selInv!=null) {
			if (selInv.getRealmComponent().isGoldSpecial()) {
				plainSight = false;
			}
			else if (!plainSight) {
				int ret = JOptionPane.showConfirmDialog(
						getMainFrame(),
						"Are you sure you want to abandon the "+selInv.getGameObject().getName()+"?",
						"Abandon Inventory",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,selInv.getRealmComponent().getIcon());
				if (ret!=JOptionPane.YES_OPTION) {
					return;
				}
			}
			
			if (selInv.canDeactivate()) {
				if (!TreasureUtility.doDeactivate(getMainFrame(),getCharacter(),selInv.getGameObject())) {
					return;
				}
			}
			GameObject thing = selInv.getGameObject();
			if (thing.hasThisAttribute(Constants.TREASURE_NEW)) {
				// If dropping a "new" item, then only "unnew" this item
				thing.removeThisAttribute(Constants.TREASURE_NEW);
			}
			else {
				// If dropping any other item, then mark ALL inventory not new
				getCharacter().markAllInventoryNotNew();
			}
			getGameHandler().broadcast(getCharacter().getGameObject().getName(),"Dropped the "+thing.getName());
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
							
			if (getCharacter().canDoDaytimeRecord()) {
				RealmTurnPanel turnPanel = getCharacterFrame().getTurnPanel();
				if (turnPanel!=null) {
					getCharacterFrame().getTurnPanel().updatePhaseManagerInactiveThings();
					getCharacterFrame().getTurnPanel().refreshPhaseManagerIcon();
				}
			}
			getCharacter().checkInventoryStatus(getMainFrame(), null, getGameHandler().getUpdateFrameListener());
			getGameHandler().submitChanges();
			getCharacterFrame().updateCharacter();
		}
	}
	private void doActivate() {
		boolean daytimeRecord = getCharacter().canDoDaytimeRecord();
		GameObject thing = inactiveInventoryObjectPanel.getSelectedGameObject();
		if (getCharacterFrame().getTurnPanel()==null || getCharacterFrame().getTurnPanel().getPhaseManager().canActivateThing(thing)) {
			getGameHandler().broadcast(getCharacter().getGameObject().getName(),"Activates "+thing.getName());
			if (TreasureUtility.doActivate(getGameHandler().getMainFrame(),getCharacter(),thing,getGameHandler().getUpdateFrameListener(),false)) {
				if (thing.hasThisAttribute(Constants.TREASURE_NEW)) {
					// If activating a "new" item, then only "unnew" this item
					thing.removeThisAttribute(Constants.TREASURE_NEW);
				}
				else {
					// If activating any other item, then mark ALL inventory not new
					getCharacter().markAllInventoryNotNew();
				}
				if (daytimeRecord) {
					if (getCharacterFrame().showingTurn()) {
						// Activated treasure might have extra actions which SHOULD apply
						getCharacterFrame().getTurnPanel().getPhaseManager().updateNewActivatedTreasure(thing);
						getCharacterFrame().getTurnPanel().getPhaseManager().updateInactiveThings();
						
						// Make sure to mark CAVE status
						TileLocation tl = getCharacter().getCurrentLocation();
						if (tl.isInClearing() && tl.clearing.isCave()) {
							getCharacterFrame().getTurnPanel().getPhaseManager().markInCave();
						}
						
						getCharacterFrame().getTurnPanel().refreshPhaseManagerIcon();
					}
				}
				getCharacter().checkInventoryStatus(getGameHandler().getMainFrame(),null,getGameHandler().getUpdateFrameListener());
			}
			if (!daytimeRecord && getCharacter().canDoDaytimeRecord()) {
				// Just gained the ability to do daytime activities, so kill all pending action rows, and add the toolbar
				if (getCharacterFrame().showingTurn()) {
					getCharacterFrame().getTurnPanel().startDaytimeRecord();
				}
			}
			getCharacterFrame().updateCharacter();
		}
		else {
			JOptionPane.showMessageDialog(
					getGameHandler().getMainFrame(),
					"You cannot activate the "+thing.getName()+
					" because you have already used a horse this turn.",
					"Activate Inventory",JOptionPane.WARNING_MESSAGE);
		}
	}
	private void doDeactivate() {
		boolean daytimeRecord = getCharacter().canDoDaytimeRecord();
		GameObject thing = activeInventoryObjectPanel.getSelectedGameObject();
		if (actionLocked(thing)) { // NOT USED
			JOptionPane.showMessageDialog(
					getGameHandler().getMainFrame(),
					"You cannot deactivate the "+thing.getName()+
					" because you recorded an action during Birdsong that requires it.\n"
					+"Once you have completed this action, you will be able to deactivate the "+thing.getName(),
					"Inactivate Inventory",JOptionPane.WARNING_MESSAGE);
			return;
		}
		getGameHandler().broadcast(getCharacter().getGameObject().getName(),"Deactivates "+thing.getName());
		if (TreasureUtility.doDeactivate(getGameHandler().getMainFrame(),getCharacter(),thing)) {
			// deactivating an item means you are ignoring "new" items (which can't possibly be active)
			// so it is necessary to mark ALL inventory as "notnew"
			getCharacter().markAllInventoryNotNew();
			
			if (daytimeRecord && getCharacterFrame().getTurnPanel()!=null) {
				getCharacterFrame().getTurnPanel().getPhaseManager().updateInactiveThings();
				getCharacterFrame().getTurnPanel().refreshPhaseManagerIcon();
			}
		}
		if (daytimeRecord && !getCharacter().canDoDaytimeRecord()) {
			// Just lost the ability to do daytime activities, so block the character from doing anything further
			if (getCharacterFrame().showingTurn()) {
				int ret = JOptionPane.showConfirmDialog(
					getGameHandler().getMainFrame(),
					"By deactivating that item, you will be blocked, and will no longer be able to record new actions.\n"
					+"Are you sure you want to do this?",
					"Character Block Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if (ret==JOptionPane.YES_OPTION) {
					getCharacterFrame().getTurnPanel().stopDaytimeRecord();
				}
				else {
					// simply reactivate (no need to call RealmUtility for this)
					thing.setThisAttribute(Constants.ACTIVATED);
				}
			}
		}
		getCharacterFrame().updateCharacter();
	}
	private void showInventoryDetail() {
		// Use the trade dialog to show detail
		RealmTradeDialog viewer = new RealmTradeDialog(getGameHandler().getMainFrame(),"Inventory for the "+getCharacter().getCharacterName(),false,false,false);
		viewer.setTradeObjects(getCharacter().getInventory());
		viewer.setVisible(true);
	}

	public void updatePanel() {
		boolean hiredLeader = getCharacter().isHiredLeader();
		ArrayList activeInv = new ArrayList();
		ArrayList inactiveInv = new ArrayList();
		for (Iterator i=getCharacter().getInventory().iterator();i.hasNext();) {
			GameObject item = (GameObject)i.next();
			if (!item.hasThisAttribute(Constants.DEAD)) { // Native horses!
				RealmComponent rc = RealmComponent.getRealmComponent(item);
				if (hiredLeader) {
					if (rc.isNativeHorse()) {
						TileLocation loc = getCharacter().getCurrentLocation();
						if (loc!=null && loc.isInClearing() && loc.clearing.isCave()) {
							inactiveInv.add(item);
							item = null;
						}
					}
				}
				if (rc.isMinorCharacter()) {
					QuestMinorCharacter mc = new QuestMinorCharacter(item);
					if (mc.isVirtual()) {
						item = null;
					}
				}
				
				if (item!=null) {
					if (item.hasThisAttribute(Constants.ACTIVATED) || item.hasThisAttribute("gold_special") || item.hasThisAttribute("boon")) {
						activeInv.add(item);
					}
					else {
						inactiveInv.add(item);
					}
				}
			}
		}
		activeInventoryObjectPanel.removeAll();
		inactiveInventoryObjectPanel.removeAll();
		if (activeInv.size()>0) {
			activeInventoryObjectPanel.addObjects(activeInv);
		}
		if (inactiveInv.size()>0) {
			inactiveInventoryObjectPanel.addObjects(inactiveInv);
		}
	}
	private Inventory getSelectedInventory() {
		GameObject selectedActiveInventory = activeInventoryObjectPanel.getSelectedGameObject();
		GameObject selectedInactiveInventory = inactiveInventoryObjectPanel.getSelectedGameObject();
		if (selectedActiveInventory!=null) {
			return new Inventory(selectedActiveInventory);
		}
		else if (selectedInactiveInventory!=null) {
			return new Inventory(selectedInactiveInventory);
		}
		return null;
	}
	public void updateControls(boolean recordingActions) {
		TileLocation current = getCharacter().getCurrentLocation();
		boolean blocked = getCharacter().isBlocked();
		boolean partway = current!=null && (current.isBetweenClearings() || current.isBetweenTiles());
		boolean inCombat = getCharacter().getCombatCount()>0;
		boolean transmorphed = getCharacter().isTransmorphed();
		boolean playingTurn = getCharacterFrame().getTurnPanel()!=null && getCharacterFrame().getTurnPanel().hasActionsLeft();
		Inventory selInv = getSelectedInventory();
		
		boolean birdsongHouseRule = getGameHandler().getHostPrefs().hasPref(Constants.HOUSE1_ALLOW_BIRDSONG_REARRANGE) && !inCombat;
		boolean rearrangementAllowed = playingTurn || birdsongHouseRule || getCharacter().isDayEndTradingActive() || (selInv!=null && selInv.isNew());
		boolean achar = getCharacter().isCharacter() && getCharacter().isActive();
		
		if (dropInventoryButton!=null) {
			dropInventoryButton.setEnabled(!blocked && selInv!=null && selInv.canDrop() && (playingTurn || birdsongHouseRule || selInv.isNew()) && !partway && !selInv.getRealmComponent().isNativeHorse());
		}
		abandonInventoryButton.setEnabled(!blocked && selInv!=null && selInv.canDrop() && (playingTurn || birdsongHouseRule || selInv.isNew()) && !partway && !selInv.getRealmComponent().isNativeHorse());
		activateInventoryButton.setEnabled(!blocked && achar && selInv!=null && selInv.canActivate() && rearrangementAllowed && !transmorphed && !partway);
		deactivateInventoryButton.setEnabled(!blocked && achar && selInv!=null && selInv.canDeactivate() && rearrangementAllowed && !partway);
		if (distributeInventoryButton!=null) {
			distributeInventoryButton.setEnabled(!blocked && rearrangementAllowed && !partway);
		}
	}
}