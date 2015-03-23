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

import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.general.util.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.attribute.DayAction.ActionId;
import com.robin.magic_realm.components.store.GuildStore;
import com.robin.magic_realm.components.store.Store;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.table.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;
import com.robin.magic_realm.components.wrapper.CharacterWrapper.ActionState;

public class ActionRow {
	private static final ImageIcon PENDING_ICON = null;
	private static final ImageIcon COMPLETED_ICON = IconFactory.findIcon("images/actions/greencheck.gif");
	private static final ImageIcon CANCELLED_ICON = IconFactory.findIcon("images/actions/redx.gif");
	private static final ImageIcon INVALID_ICON = IconFactory.findIcon("images/actions/ban.gif");
	
	public static final String SLEEPING = "Sleeping";
	
	public static boolean askAboutAbandoningFollowers = false;

	private ImageIcon icon;
	private String action;
	private String actionTypeCode;
	private String result;
	private boolean completed;
	private boolean cancelled;
	
	private boolean autoMarkInventory;
	
	private String blankReason = null;; // identifies a BLANK phase
	private boolean spawned = false; // identifies "spawned" actions that aren't recorded or tracked
	private boolean invalid = false; // identifies an INVALID phase (this doesn't count as a real phase!!)

	private RealmTurnPanel turnPanel;
	private RealmGameHandler gameHandler;
	private CharacterWrapper character;
	private TileLocation location; // for moves only
	private DieRoller roller;
	
	private int count=1;
	private int bonusCount=0; // The number of "bonus" phases that don't apply towards the phase manager
	
	private ActionRow newAction = null;
	
	private RealmTable realmTable = null;
	
	private boolean isFollowing;
	
	private boolean ponyLock = false;
	
	/**
	 * For TESTING ONLY!!!!
	 */
	public ActionRow(String action,String actionTypeCode) {
		this.action = action;
		this.actionTypeCode = actionTypeCode;
		icon = CharacterWrapper.getIconForAction(action);
		result = "";
		completed = false;
		cancelled = false;
		roller = null;
	}

	/**
	 * Primary action constructor
	 */
	public ActionRow(RealmTurnPanel turnPanel,CharacterWrapper character, String action,String actionTypeCode,boolean isFollowing) {
		this.turnPanel = turnPanel;
		this.gameHandler = turnPanel.getGameHandler();
		this.character = character;
		this.action = action;
		this.actionTypeCode = actionTypeCode;
		this.isFollowing = isFollowing;
		icon = CharacterWrapper.getIconForAction(action);
		result = "";
		completed = false;
		cancelled = false;
		roller = null;
	}
	/**
	 * This constructor is used to handle new rolls on tables
	 */
	private ActionRow(RealmTurnPanel turnPanel,CharacterWrapper character, RealmTable table,boolean isFollowing) {
		this.turnPanel = turnPanel;
		this.gameHandler = turnPanel.getGameHandler();
		this.character = character;
		this.action = null;
		this.actionTypeCode = null;
		this.isFollowing = isFollowing;
		icon = null;
		result = "";
		completed = false;
		cancelled = false;
		roller = null;
		realmTable = table;
	}
	public int getPhaseCount() {
		int pc = 1; // default
		if (action.indexOf(",")>=0) {
			StringTokenizer phases = new StringTokenizer(action,",");
			pc = phases.countTokens();
		}
		return pc;
	}
	public String toString() {
		String condition = cancelled?"cancelled":(completed?"completed":"pending");
		return action+" ("+condition+"): "+result;
	}
	public String getAction() {
		return action;
	}
	public ActionId getActionId() {
		return CharacterWrapper.getIdForAction(action);
	}
	private void handleTable() {
		result = realmTable.getTableName(false); // show the short name!
		String message;
		if (realmTable.hideRoller()) {
			message = realmTable.applyOne(character);
		}
		else {
			roller = DieRollBuilder.getDieRollBuilder(gameHandler.getMainFrame(),character).createRoller(realmTable);
			message = realmTable.apply(character,roller);
		}
		if (message!=null) {
			result = result + " - " + message;
			gameHandler.updateCharacterFrames();
		}
		if (realmTable.getNewTable()!=null) {
			newAction = new ActionRow(turnPanel,character,realmTable.getNewTable(),isFollowing);
			newAction.handleTable();
		}
		completed = true;
	}
	public String getResult() {
		return result;
	}
	public ActionRow makeCopy() {
		return new ActionRow(turnPanel,character,action,actionTypeCode,isFollowing);
	}
	public void setCount(int val) {
		count = val;
	}
	public int getCount() {
		return count;
	}
	public int getBonusCount() {
		return bonusCount;
	}
	public void incrementCount() {
		count++;
	}
	
	public ImageIcon getIcon() {
		return icon;
	}

	public String getDescription() {
		String description = "";
		if (blankReason!=null) {
			return "Invalid recorded action!  Blank Phase: "+blankReason;
		}
		if (invalid) {
			return "Invalid phase!";
		}
		ActionId id = CharacterWrapper.getIdForAction(action);
		if (ActionId.Hide==id) {
			description = "Hide";
		}
		else if (ActionId.Move==id) {
			description = "Move to " + location.clearing.getDescription();
			if (ponyLock) {
				description = description + " (Non-Pony Move)";
			}
		}
		else if (ActionId.Search==id) {
			description = "Search";
		}
		else if (ActionId.Trade==id) {
			description = "Trade";
		}
		else if (ActionId.Rest==id) {
			description = "Rest "+count+" time"+(count==1?"":"s");
		}
		else if (ActionId.Alert==id) {
			description = "Alert"+(isFollowing?" (Optional)":"");
		}
		else if (ActionId.Hire==id) {
			description = "Hire";
		}
		else if (ActionId.Follow==id) {
			description = "Follow";
		}
		else if (ActionId.Spell==id) {
			description = "Spell";
		}
		else if (ActionId.SpellPrep==id) {
			description = "Spell Prep";
		}
		else if (ActionId.EnhPeer==id) {
			description = "Enhanced Peer";
		}
		else if (ActionId.Fly==id) {
			description = "Fly to " + location.tile.getTileName();
		}
		else if (ActionId.RemSpell==id) {
			description = "Remote Spell";
		}
		else if (ActionId.Cache==id) {
			description = "Cache";
		}
		else if (ActionId.Heal==id) {
			description = "Heal";
		}
		else if (ActionId.Repair==id) {
			description = "Repair";
		}
		else if (ActionId.Fortify==id) {
			description = "Fortify";
		}
		if (result!=null && result.trim().length()>0) {
			return description + " - " + result;
		}
		return description;
	}
	
	public ImageIcon getStatusIcon() {
		switch(getActionState()) {
			case Pending:	return PENDING_ICON;
			case Invalid:	return INVALID_ICON;
			case Completed:	return COMPLETED_ICON;
			case Cancelled:	return CANCELLED_ICON;
		}
		throw new IllegalStateException("Unknown status");
	}
	private ActionState getActionState() {
		ActionState state = ActionState.Cancelled;
		if (invalid) {
			state = ActionState.Invalid;
		}
		if (!cancelled) {
			if (completed) {
				state = ActionState.Completed;
			}
			else {
				state = ActionState.Pending;
			}
		}
		return state;
	}
	public void setActionState(ActionState state) {
		completed = false;
		cancelled = false;
		invalid = false;
		switch(state) {
			case Pending:
				break;
			case Invalid:
				completed = true;
				invalid = true;
				break;
			case Completed:
				completed = true;
				break;
			case Cancelled:
				completed = true;
				cancelled = true;
				break;
		}
	}

	public boolean isPending() {
		return !cancelled && !completed;
	}

	public TileLocation getLocation() {
		return location;
	}
	public void setLocation(TileLocation location) {
		this.location = location;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public void landCharacterIfNeeded() {
		ActionId id = CharacterWrapper.getIdForAction(action);
		if (id!=ActionId.Fly) {
			// Make sure character is on the ground if not flying
			if (character.land(gameHandler.getMainFrame())) {
				// Check for blocking immediately
				if (!character.isBlocked() && RealmUtility.willBeBlocked(character,isFollowing,true)) {
					character.setBlocked(true);
				}
				gameHandler.getInspector().redrawMap();
			}
		}
	}

	/**
	 * The meat of an action - is this really the best place for this?
	 * 
	 * Mmmmmm.  ACTION MEAT!!!!
	 */
	public void process() {
		if (!isFollowing) {
			if (character.isBlocked()) {
				gameHandler.broadcast(character.getGameObject().getName(),"BLOCKED - Cannot perform action "+action);
				cancelled = true;
				result = "BLOCKED";
				return;
			}
			checkSleep();
			if (character.isSleep()) {
				cancelled = true;
				result = SLEEPING;
				return;
			}
		}
		
		completed = true; // the default - can be modified if there are problems
		
		if (blankReason==null && !invalid) {
			autoMarkInventory = true;
			ActionId id = CharacterWrapper.getIdForAction(action);
			if (ActionId.Hide==id) {
				doHideAction();
			}
			else if (ActionId.Move==id) {
				doMoveAction();
			}
			else if (ActionId.Search==id) {
				autoMarkInventory = false;
				doSearchAction();
			}
			else if (ActionId.Trade==id) {
				autoMarkInventory = false;
				boolean tj = character.canDoDaytimeRecord();
				doTradeAction();
				if (tj!=character.canDoDaytimeRecord()) {
					// Something happened, so BLOCK!!
					character.setBlocked(true);
				}
			}
			else if (ActionId.Rest==id) {
				doRestAction();
			}
			else if (ActionId.Alert==id) {
				doAlertAction();
			}
			else if (ActionId.Hire==id) {
				doHireAction();
			}
			//else if (ActionId.Follow==id) {
				// This is handled differently - not here!
			//}
			else if (ActionId.Spell==id) {
				doSpellAction();
			}
			//else if (ActionId.SpellPrep==id) {
				// does nothing
			//}
			else if (ActionId.EnhPeer==id) {
				doEnhancedPeerAction();
			}
			else if (ActionId.Fly==id) {
				doFlyAction();
			}
			else if (ActionId.RemSpell==id) {
				doRemoteSpellAction();
			}
			else if (ActionId.Cache==id) {
				autoMarkInventory = false;
				doCacheAction();
			}
			else if (ActionId.Heal==id) {
				doHealAction();
			}
			else if (ActionId.Repair==id) {
				autoMarkInventory = false;
				doRepairAction();
			}
			else if (ActionId.Fortify==id) {
				doFortifyAction();
			}
			
			if (autoMarkInventory) {
				character.markAllInventoryNotNew();
			}
			
			logAction();
			
			if (!character.isActive()) {
				return;
			}
		}
		
		checkSleep(); // check again, in case something changed during the action

		if (completed) { // don't check for blocking until completed!
			
			// Check for Violent Storm
			if (willBeAffectedByStorm()) {
				TileLocation current = character.getCurrentLocation();
				int phasesLost = current.tile.getGameObject().getThisInt(Constants.SP_STORMY);
				gameHandler.broadcast(character.getGameObject().getName(),"Caught in Violent Storm!!  Loses "+phasesLost+" phases.");
				turnPanel.doLosePhases(phasesLost);
				character.setStormed(true);
			}
		
			character.addActionPerformedToday(action,getActionState(),result,roller);
		}
	}
	public void updateBlocked() {
		if (!character.isBlocked() && RealmUtility.willBeBlocked(character,isFollowing,true)) {
			character.setBlocked(true);
		}
	}
	public boolean willHavePhaseEndUpdates() {
		boolean sleepable = !character.getFatiguedChits().isEmpty() || !character.getWoundedChits().isEmpty();
		return willBeAffectedByStorm()
			|| (TreasureUtility.getSleepObject(character.getCurrentLocation())!=null && sleepable)
			|| RealmUtility.willBeBlocked(character,isFollowing,false);
	}
	public boolean willBeAffectedByStorm() {
		if (!character.getStormed()) {
			TileLocation current = character.getCurrentLocation();
			if (current.isInClearing()
					&& current.tile.getGameObject().hasThisAttribute(Constants.SP_STORMY)
					&& !current.clearing.isCave()
					&& !current.clearing.holdsDwelling()) {
				// Ended a phase in a stormy clearing, and haven't been affected yet, so...
				return true;
			}
		}
		return false;
	}
	public void logAction() {
		if (completed) {
			DayAction da = CharacterWrapper.getActionForString(action);
			String actionName = da==null?"":da.getName();			
			gameHandler.broadcast(character.getGameObject().getName(),actionName+" - "+getNonsecretKey(result));
			
			// Now that the non-secret portion has been logged, we can convert the result fully to the secret key.
			result = getSecretKey(result);
		}
	}
	/**
	 * The string coming in may have a secret key in it that looks like:
	 * 
	 *   ##Treasure|Deft Gloves##
	 * 
	 * See Loot.characterFindsItem
	 */
	private String[] breakOutKeys(String in) {
		String[] ret = null;
		int start = in.indexOf("##");
		if (start>=0) {
			int end = in.indexOf("##",start+1);
			if (end>=0) {
				int mid = in.indexOf('|');
				if (mid>=0 && mid>start && mid<end) {
					ret = new String[2];
					String front = in.substring(0,start);
					String back = in.substring(end+2);
					ret[0] = front + in.substring(start+2,mid) + back;
					ret[1] = front + in.substring(mid+1,end) + back;
				}
			}
		}
		return ret;
	}
	/**
	 * This is the secret portion of the string.  In the example above, this would return "Deft Gloves".
	 */
	private String getSecretKey(String in) {
		String[] ret = breakOutKeys(in);
		if (ret!=null) {
			return ret[1];
		}
		return in;
	}
	/**
	 * This is the nonsecret portion of the string.  In the example above, this would return "Treasure".
	 */
	private String getNonsecretKey(String in) {
		String[] ret = breakOutKeys(in);
		if (ret!=null) {
			return ret[0];
		}
		return in;
	}
	public void checkSleep() {
		// Find other characters in the clearing, and put them to sleep too
		TileLocation tl = character.getCurrentLocation();
		if (tl.isInClearing()) {
			for (CharacterWrapper testCharacter:ClearingUtility.getCharactersInClearing(tl)) {
				checkSleep(testCharacter);
			}
		}
	}
	private void checkSleep(CharacterWrapper testCharacter) {
		if (!testCharacter.isSleep()) {
			GameObject sleepObject = getSleepObject(testCharacter);
			if (sleepObject!=null) {
				RealmComponent rc = RealmComponent.getRealmComponent(sleepObject);
				if (rc.isTreasure()) {
					TreasureCardComponent treasure = (TreasureCardComponent)rc;
					if (!treasure.isFaceUp()) {
						treasure.setFaceUp();
					}
				}
				testCharacter.setSleep(true);
				if (testCharacter.isFollowingCharacterPlayingTurn()) {
					character.removeActionFollower(testCharacter,null);
					JOptionPane.showMessageDialog(
							gameHandler.getMainFrame(),
							"The "+testCharacter.getGameObject().getName()+" has fallen asleep.",
							sleepObject.getName()+" in clearing",JOptionPane.INFORMATION_MESSAGE,rc.getIcon());
				}
				
				int order = testCharacter.getPlayOrder();
				if (order<2) { // Report sleep effect if testCharacter is current turn, or played turn (NOT future turns!)
					JOptionPane.showMessageDialog(
							gameHandler.getMainFrame(),
							"The "+testCharacter.getGameObject().getName()+" has fallen asleep.",
							sleepObject.getName()+" in clearing",JOptionPane.INFORMATION_MESSAGE,rc.getIcon());
				}
				RealmLogging.logMessage(
						testCharacter.getGameObject().getName(),
						"Has fallen asleep due to the presence of the "+sleepObject.getName()+".");
			}
		}
	}
	private GameObject getSleepObject(CharacterWrapper testCharacter) {
		TileLocation current = testCharacter.getCurrentLocation();
		if (current.isInClearing()) {
			// First check to see if character has fatigued chits, and can rest
			if (testCharacter.getFatiguedChits().size()>0 && !testCharacter.hasCurse(Constants.WITHER)) {
				// Now see if there are any sleep treasures
				return TreasureUtility.getSleepObject(current);
			}
		}
		return null;
	}
	public DieRoller getRoller() {
		return roller;
	}
	public void setRoller(DieRoller roller) {
		this.roller = roller;
	}
	
	private void doHideAction() {
		if (!character.isHidden()) {
			TileLocation location = character.getCurrentLocation();
			GameObject noHideItem = ClearingUtility.getItemInClearingWithKey(location,Constants.NO_HIDE);
			if (character.hasCurse(Constants.SQUEAK)) {
				result = "Failed due to SQUEAK curse";
			}
			else if (noHideItem!=null) {
				result = "Failed due to the "+noHideItem.getName();
			}
			else {
				RealmCalendar cal = RealmCalendar.getCalendar(gameHandler.getClient().getGameData());
				HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(gameHandler.getClient().getGameData());
				boolean canHide = !cal.isHideDisabled(character.getCurrentMonth());
				if (!canHide && hostPrefs.hasPref(Constants.HOUSE3_SNOW_HIDE_EXCLUDE_CAVES) && location.isInClearing() && location.clearing.isCave()) {
					canHide = true;
				}
				
				if (canHide) {
					roller = DieRollBuilder.getDieRollBuilder(gameHandler.getMainFrame(),character).createHideRoller();
					if (roller.getHighDieResult() < 6) {
						result = "Succeeded";
						character.setHidden(true);
						for (Iterator i=character.getActionFollowers().iterator();i.hasNext();) {
							CharacterWrapper follower = (CharacterWrapper)i.next();
							if (!follower.hasCurse(Constants.SQUEAK)) {
								follower.setHidden(true);
							}
						}
					}
					else {
						result = "Failed";
					}
				}
				else {
					result = "HIDE table is disabled, due to inclement weather.";
				}
			}
		}
		else {
			result = "N/A";
		}
	}
	private void doFortifyAction() {
		if (!character.isFortified()) {
			roller = DieRollBuilder.getDieRollBuilder(gameHandler.getMainFrame(),character).createFortifyRoller();
			if (roller.getHighDieResult() < 6) {
				result = "Succeeded";
				character.setFortified(true);
			}
			else {
				result = "Failed";
			}
		}
		else {
			result = "N/A";
		}
	}
	private void doMoveAction() {
		TileLocation current = character.getCurrentLocation();
		
		// Before starting, make sure that you aren't "lost in the maze"
		RealmComponent discoverToLeave = ClearingUtility.findDiscoverToLeaveComponent(current,character);
		if (discoverToLeave!=null) {
			JOptionPane.showMessageDialog(gameHandler.getMainFrame(),"You are trapped in the "+discoverToLeave.getGameObject().getName()+"! MOVE is cancelled.",
					"Trapped!",JOptionPane.PLAIN_MESSAGE,discoverToLeave.getFaceUpIcon());
			cancelled = true;
			return;
		}
		
		// First and foremost, make sure character can carry everything
		if (!character.canMove() && current.isInClearing()) {
			JOptionPane.showMessageDialog(gameHandler.getMainFrame(),"You cannot move with your current inventory.  Drop something first.");
			completed = false;
			return;
		}
		
		
		result = "";
		if (character.moveRandomly() && !current.isBetweenClearings()) {
			// Pick a random location
			DieRoller roller = new DieRoller();
			roller.adjustDieSize(25, 6);
			roller.addRedDie();
			roller.rollDice("Random Clearing");
			int c = roller.getTotal();
			
			// Find all clearings that match the number
			ArrayList<ClearingDetail> clearings = new ArrayList<ClearingDetail>();
			for (Iterator i=current.clearing.getConnectedPaths().iterator();i.hasNext();) {
				PathDetail path = (PathDetail)i.next();
				ClearingDetail clearing = path.findConnection(current.clearing);
				if (clearing!=null) {
					if (clearing.getNum()==c) {
						clearings.add(clearing);
					}
				}
			}
			
			// If none, cancel move action
			if (clearings.isEmpty()) {
				cancelled = false;
				completed = true;
				result = "Random move to clearing "+c+" is invalid!";
				return;
			}
			else {
				result = "Random move to clearing "+c+": ";
				
				if (clearings.size()==1) {
					// If one, do move action
					location = new TileLocation(clearings.get(0));
				}
				else {
					// If more than one, let player choose
					CenteredMapView.getSingleton().setMarkClearingAlertText("Random move to clearing "+c+": pick one!");
					CenteredMapView.getSingleton().markClearings(clearings,true);
					TileLocationChooser chooser = new TileLocationChooser(gameHandler.getMainFrame(),CenteredMapView.getSingleton(),current);
					chooser.setVisible(true);
					
					// Update the location
					CenteredMapView.getSingleton().markClearings(clearings,false);
					location = chooser.getSelectedLocation();
				}
			}
		}
		
		// Player is moved to clearing
		if (location != null) {
			// clearing might NOT be on the same side, if a tile flipped somewhere, so update it here
			location.clearing = location.clearing.correctSide();
			
			// Validate that the player CAN move along the path (if discovery was needed)
			PathDetail path = current.hasClearing()?current.clearing.getConnectingPath(location.clearing):null;
			boolean overridePath = false;
			
			if (character.canWalkWoods(current.tile) || (current.isTileOnly() && !current.isFlying())) {
				ArrayList validClearings = new ArrayList();
				if (current.clearing!=null) {
					validClearings.addAll(current.clearing.getParent().getClearings());
				}
				else if (current.tile.equals(location.tile)) {
					validClearings.addAll(location.clearing.getParent().getClearings());
				}
				if (current.isBetweenClearings()) {
					validClearings.addAll(current.getOther().tile.getClearings());
				}
				if (validClearings.contains(location.clearing)) {
					overridePath = true;
				}
			}
			
			boolean validMove = true;
			if (current.isBetweenClearings()) {
				validMove = current.clearing.equals(location.clearing) || current.getOther().clearing.equals(location.clearing);
			}
			
			if (!overridePath && path==null) {
				overridePath = ClearingUtility.canUseGates(character,location.clearing);
			}
			
			if (validMove && (overridePath || current.isBetweenClearings() || path!=null)) {
				if (overridePath || current.isBetweenClearings() || character.validPath(path)) {
					// Make sure that if the character is moving into a mountain clearing, check current clearing
					// to make sure monsters don't block the first half of that move
					if (location.clearing.moveCost(character)>1 && RealmUtility.willBeBlocked(character,isFollowing,true)) {
						character.setBlocked(true);
						cancelled = true;
						result = "BLOCKED";
						return;
					}
					
					// Move followers - FIXME Not totally right... but close!
					ArrayList actionFollowers = character.getActionFollowers();
					
					if (actionFollowers.size()>0) {
						ArrayList canLeaveBehind = new ArrayList();
						ArrayList encumberedFollowers = new ArrayList();
						for (Iterator i=actionFollowers.iterator();i.hasNext();) {
							CharacterWrapper follower = (CharacterWrapper)i.next();
							if (!follower.foundHiddenEnemy(character.getGameObject())) {
								canLeaveBehind.add(follower);
							}
							if (!follower.canFollow()) {
								encumberedFollowers.add(follower);
							}
						}
						if (!encumberedFollowers.isEmpty()) {
							StringBuffer message = new StringBuffer();
							for (Iterator i=encumberedFollowers.iterator();i.hasNext();) {
								CharacterWrapper follower = (CharacterWrapper)i.next();
								if (message.length()>0) {
									if (i.hasNext()) {
										message.append(", ");
									}
									else {
										message.append(" and ");
									}
									message.append("the ");
								}
								else {
									message.append("The ");
								}
								message.append(follower.getGameObject().getName());
							}
							message.append(encumberedFollowers.size()==1?" is":" are");
							message.append(" encumbered\nand will be left behind.  Move anyway?");
							int ret = JOptionPane.showConfirmDialog(
									gameHandler.getMainFrame(),
									message.toString(),
									"Encumbered Followers",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.INFORMATION_MESSAGE);
							if (ret==JOptionPane.NO_OPTION) {
								completed = false;
								return;
							}
						}
						if (character.isHidden()) {
							int totalCanLeaveBehind = canLeaveBehind.size();
							if (askAboutAbandoningFollowers && totalCanLeaveBehind>0) {
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
								
								int ret = QuietOptionPane.showConfirmDialog(
											gameHandler.getMainFrame(),
											message.toString(),
											"Unwanted Followers?",
											JOptionPane.YES_NO_OPTION,
											"Don't ask again this turn",true);
								askAboutAbandoningFollowers = !QuietOptionPane.isLastWasSilenced();
								if (ret==JOptionPane.YES_OPTION) {
									turnPanel.doAbandonActionFollowers();
									actionFollowers = character.getActionFollowers();
								}
							}
						}
					}
					
					if (character.isMistLike()) {
						ArrayList<RealmComponent> followingHirelings = character.getFollowingHirelings();
						if (!followingHirelings.isEmpty()) {
							// Drop following hirelings in the clearing
							for (RealmComponent fh:followingHirelings) {
								ClearingUtility.moveToLocation(fh.getGameObject(),current);
							}
						}
					}
					
					// Here is the ACTUAL MOVE
					character.moveToLocation(gameHandler.getMainFrame(),location);
					if (location.hasClearing() && location.clearing.isEdge()) {
						// Character has left the map
						character.makeGone();
					}
					PathDetail reverse = null;
					if (path!=null) {
						reverse = path.getEdgePathFromOtherTile();
						character.updatePathKnowledge(path);
					}
					if (reverse!=null) {
						character.updatePathKnowledge(reverse);
					}
					
					// Move the action followers too (FIXME What happens to followers following a character leaving a map?)
					for (Iterator i=actionFollowers.iterator();i.hasNext();) {
						CharacterWrapper follower = (CharacterWrapper)i.next();
						if (!overridePath || path!=null) {
							if (follower.canFollow()) {
								follower.moveToLocation(gameHandler.getMainFrame(),location);
								// Followers ALWAYS learn secrets (unless walking woods...?)
								if (!overridePath) {
									follower.updatePathKnowledge(path);
									if (reverse!=null) {
										follower.updatePathKnowledge(reverse);
									}
								}
							}
							else {
								// Oops, follower was likely encumbered!  Take 'em off the list!
								character.removeActionFollower(follower,gameHandler.getGame().getMonsterDie());
							}
						}
						else {
							// Oops, follower can't follow character because he is walking woods
							character.removeActionFollower(follower,gameHandler.getGame().getMonsterDie());
						}
					}
					
					gameHandler.getInspector().getMap().centerOn(character.getCurrentLocation());
					gameHandler.updateCharacterFrames();
					result = result+"moved";
					
					if (!overridePath && !current.isBetweenClearings() && (path.isNarrow() || (reverse!=null && reverse.isNarrow()))) {
						// Other characters in the same clearing who have found hidden enemies
						// for the day should gain a discovery when this move occurs (on either end of the path!)
						if (current.hasClearing()) {
							for (RealmComponent rc:current.clearing.getClearingComponents()) {
								if (rc.canSpy() && !rc.getGameObject().equals(character.getGameObject())) {
									CharacterWrapper spy = new CharacterWrapper(rc.getGameObject());
									if (!character.isHidden() || spy.foundHiddenEnemy(character.getGameObject())) {
										spy.updatePathKnowledge(path); // spy's that see character leave only get the path they are leaving on!
									}
								}
							}
						}
						for (RealmComponent rc:location.clearing.getClearingComponents()) {
							if (rc.canSpy() && !rc.getGameObject().equals(character.getGameObject())) {
								CharacterWrapper spy = new CharacterWrapper(rc.getGameObject());
								if (!character.isHidden() || spy.foundHiddenEnemy(character.getGameObject())) {
									// spy's that see a character enter only get the reverse, unless there isn't one.
									if (reverse==null) {
										spy.updatePathKnowledge(path);
									}
									else {
										spy.updatePathKnowledge(reverse);
									}
								}
							}
						}
					}
				}
				else {
					cancelled = true;
					result = "Cannot Move: undiscovered path";
				}
			}
			else {
				cancelled = true;
				result = "Cannot Move: no path";
			}
		}
		else {
			// this should never happen
			throw new IllegalStateException("null clearing during ActionRow.process!");
		}
		if (cancelled && turnPanel.getActionControlManager()!=null) {
			JOptionPane.showMessageDialog(gameHandler.getMainFrame(),result,"Move Cancelled",JOptionPane.WARNING_MESSAGE);
		}
	}
	private void doSearchAction() {
		if (character.hasCurse(Constants.EYEMIST)) {
			result = "Cannot SEARCH with EYEMIST curse.";
			return;
		}
		// Player chooses from one type of search table
		RealmTable searchTable = null;
		TileLocation current = character.getCurrentLocation(); // shouldn't be able to do a search if not in a clearing!
		
		boolean magicSight = character.usesMagicSight(); // magic sight limits what character can do
		
		// choose from Peer, Locate, Loot, ReadingRunes
		// Should be able to cancel to stop a playAll
		ButtonOptionDialog chooseSearch = new ButtonOptionDialog(gameHandler.getMainFrame(), null, "Search:", "", true);
		if (magicSight) {
			addTableToChooser(chooseSearch,RealmTable.magicSight(gameHandler.getMainFrame()));
		}
		else {
			if (character.getPeerAny()) {
				addTableToChooser(chooseSearch,RealmTable.peerAny(gameHandler.getMainFrame()));
			}
			else {
				RealmCalendar cal = RealmCalendar.getCalendar(gameHandler.getClient().getGameData());
				boolean canPeer = character.canPeer() && !cal.isPeerDisabled(character.getCurrentMonth());
				addTableToChooser(chooseSearch,RealmTable.peer(gameHandler.getMainFrame(),null),canPeer);
				if (current.clearing.isMountain()) {
					// If in a mountain clearing, allow peer into mountain/woods clearing in same or adjacent tiles
					addTableToChooser(chooseSearch,RealmTable.mountainPeer(gameHandler.getMainFrame()),canPeer);
				}
			}
			addTableToChooser(chooseSearch,RealmTable.locate(gameHandler.getMainFrame(),null));
		}
		
		for (RealmComponent rc:current.clearing.getClearingComponents()) {
			// Loot is a special case, as it requires a TL
			if (rc.getGameObject().hasThisAttribute("treasure_location")) {
				if (/*!rc.getGameObject().hasThisAttribute("discovery") ||*/ // Why did I have this?
						character.hasTreasureLocationDiscovery(rc.getGameObject().getName())) {
					
					// no point in looting if nothing is left! (exception: Sites with TableLoot)
					if (TreasureUtility.getTreasureCount(rc.getGameObject(),character)>0 || rc.getGameObject().hasAttributeBlock("table")) {
						// can't loot sites that still need to be opened (crypt, vault)
						if (!rc.getGameObject().hasThisAttribute(Constants.NEEDS_OPEN)) {
							Loot loot = (Loot)RealmTable.loot(gameHandler.getMainFrame(),character,rc.getGameObject(),gameHandler.getUpdateFrameListener());
							if (!magicSight || (loot instanceof TableLoot)) {
								addTableToChooser(chooseSearch,loot);
							}
						}
					}
					
					// any spells for Read Runes?
					if (!magicSight && character.isCharacter() && SpellUtility.getSpellCount(rc.getGameObject(),null,true)>0) {
						addTableToChooser(chooseSearch,RealmTable.readRunes(gameHandler.getMainFrame(),rc.getGameObject()));
					}
				}
			}
			else if (rc.isTraveler() && rc.getOwnerId()==null && rc.getGameObject().hasThisAttribute(Constants.CAPTURE)) {
				addTableToChooser(chooseSearch,RealmTable.capture(gameHandler.getMainFrame(),(TravelerChitComponent)rc));
			}
		}
		
		ArrayList<GameObject> openableSites = character.getAllOpenableSites();
		if (!openableSites.isEmpty()) {
			String message = "Open";
			chooseSearch.addSelectionObject(message);
			IconGroup group = new IconGroup(IconGroup.HORIZONTAL,2);
			for (GameObject go:openableSites) {
				group.addIcon(RealmComponent.getRealmComponent(go).getIcon());
			}
			chooseSearch.setSelectionObjectIcon(message,group);
		}
		
		if (!magicSight && ClearingUtility.getAbandonedItemCount(current)>0) {
			// don't need hint icons for clearing loots...
			chooseSearch.addSelectionObject(RealmTable.loot(gameHandler.getMainFrame(),character,current,gameHandler.getUpdateFrameListener()));
		}
		
		// check player inventory
		if (!magicSight) {
			for (GameObject item:character.getEnhancingItems()) {
				if (SpellUtility.getSpellCount(item,null,true)>0) {
					addTableToChooser(chooseSearch,RealmTable.readRunes(gameHandler.getMainFrame(),item));
				}
			}
		}
		
		chooseSearch.setLocationRelativeTo(gameHandler.getMainFrame());
		chooseSearch.pack();
		chooseSearch.setVisible(true);
		
		Object selected = chooseSearch.getSelectedObject();
		if (selected instanceof String) {
			// Currently, the only String possibility, is the option to open the VAULT
			// This is a bit hacky, but will work for now.
			character.markAllInventoryNotNew();
			TreasureUtility.openOneObject(turnPanel.getGameHandler().getMainFrame(),character,openableSites,turnPanel.getGameHandler().getUpdateFrameListener(),false);
			doSearchAction(); // recurses because we don't want to use up a search
			return;
		}
		else {
			searchTable = (RealmTable)selected;
		}

		if (searchTable==null) {
			// player cancelled the dialog, so get out of here
			completed = false;
			return;
		}

		if (searchTable.fulfilledPrerequisite(gameHandler.getMainFrame(),character)) {
			character.markAllInventoryNotNew();
			realmTable = searchTable;
			handleTable();
		}
		else {
			// Didn't fulfill prerequisite (ie., fatigue a chit), so cancel the action.
			completed = false;
		}
	}
	private void addTableToChooser(ButtonOptionDialog chooser,RealmTable table) {
		addTableToChooser(chooser,table,true);
	}
	private void addTableToChooser(ButtonOptionDialog chooser,RealmTable table,boolean enabled) {
		chooser.addSelectionObject(table,enabled);
		chooser.setSelectionObjectIcon(table,table.getHintIcon(character));
	}
	private static final String TRADE_BUY = "BUY";
	private static final String TRADE_SELL = "SELL";
	private static final String TRADE_REPAIR = "Repair Armor";
	private static final String TRADE_JOIN = "Join Guild";
	private static final String TRADE_SERVICES = "Guild Services";
	private void doTradeAction() {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(gameHandler.getClient().getGameData());
		// Player chooses from all native leaders in the clearing
		// Player then chooses from items for sale
		TileLocation tl = character.getCurrentLocation();
		ArrayList<RealmComponent> traders = ClearingUtility.getAllTraders(tl.clearing);
		if (!traders.isEmpty()) { // need traders to trade!
			// Select a trader
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Select trade action:",true);
			int keyN = 0;
			for (RealmComponent rc:traders) {
				if (rc.isTraveler()) {
					chooser.addRealmComponent(rc,rc.getGameObject().getName());
				}
				else if (rc.isGuild()) {
					if (character.hasOtherChitDiscovery(rc.getGameObject().getName())) {
						String key = null;
						if (character.isGuildMember(rc)) {
							key = chooser.generateOption(TRADE_SERVICES);
						}
						else if (character.getCurrentGuild()==null) {
							key = chooser.generateOption(TRADE_JOIN);
						}
						if (key!=null) {
							chooser.addRealmComponentToOption(key,rc,RealmComponentOptionChooser.DisplayOption.Darkside);
						}
						for (int n=0;n<2;n++) {
							int relationship = RealmUtility.getRelationshipBetween(character,rc);
							String relName = RealmUtility.getRelationshipNameFor(relationship);
							key = "N"+(keyN++);
							String text = (n==0?TRADE_BUY:TRADE_SELL)+" ("+relName+")";
							chooser.addOption(key,text);
							chooser.addRealmComponentToOption(key,rc,RealmComponentOptionChooser.DisplayOption.Darkside);
						}
					}
				}
				else {
					for (int n=0;n<2;n++) {
						String key = "N"+(keyN++);
						String text = n==0?TRADE_BUY:TRADE_SELL;
						chooser.addOption(key,text);
						chooser.addRealmComponentToOption(key,rc);
					}
					if (hostPrefs.hasPref(Constants.HOUSE3_DWELLING_ARMOR_REPAIR)) {
						if (TreasureUtility.getDamagedArmor(character.getSellableInventory()).size()>0) {
							String key = chooser.generateOption(TRADE_REPAIR);
							chooser.addRealmComponentToOption(key,rc);
						}
					}
				}
			}
			chooser.addOption("none","No Trade");
			chooser.setVisible(true);
			String selText = chooser.getSelectedText();
			if (selText!=null) {
				character.markAllInventoryNotNew();
				if ("No Trade".equals(selText)) {
					result = "Cancelled Trade";
					return;
				}
				RealmComponent trader = chooser.getFirstSelectedComponent();
				if (trader.isTraveler()) {
					Store store = Store.getStore((TravelerChitComponent)trader,character);
					if (store!=null && store.canUseStore()) {
						result = store.doService(gameHandler.getMainFrame());
						if (result==null) completed = false;
					}
					else {
						JOptionPane.showMessageDialog(gameHandler.getMainFrame(),store.getReasonStoreNotAvailable(),"Store Not Available!",JOptionPane.PLAIN_MESSAGE,trader.getIcon());
						completed = false;
					}
				}
				else if (trader.isGuild() && selText.equals(TRADE_JOIN)) {
					character.setCurrentGuild(trader.getGameObject().getThisAttribute("guild"));
					character.setCurrentGuildLevel(1);
					result = "Joined the "+trader.getGameObject().getName();
				}
				else if (trader.isGuild() && selText.equals(TRADE_SERVICES)) {
					GuildStore store = Store.getGuildStore((GuildChitComponent)trader,character);
					if (store!=null && store.canUseStore()) {
						result = store.doService(gameHandler.getMainFrame());
						if (result==null) completed = false;
					}
					else {
						String reason = store==null?"No Store Found?!?":store.getReasonStoreNotAvailable();
						JOptionPane.showMessageDialog(gameHandler.getMainFrame(),reason,"Store Not Available!",JOptionPane.PLAIN_MESSAGE,trader.getIcon());
						completed = false;
					}
				}
				else {
					if (selText.startsWith(TRADE_BUY)) selText = TRADE_BUY;
					if (selText.startsWith(TRADE_SELL)) selText = TRADE_SELL;
					processTrade(trader,selText,hostPrefs);
				}
			}
			else {
				completed = false;
			}
		}
	}
	private void processTrade(RealmComponent trader,String tradeAction,HostPrefWrapper hostPrefs) {
		ArrayList<GameObject> hold = null;
		String traderName = trader.isNative()?trader.getGameObject().getThisAttribute("native"):trader.getGameObject().getName();
		String relName = RealmUtility.getRelationshipNameFor(character,trader);
		String traderRel = traderName+" ("+relName+")";
		if (TRADE_BUY.equals(tradeAction)) {
			if (trader.isNative()) {
				// Native Leader - trade with their dwelling's hold
				GameObject holder = SetupCardUtility.getDenizenHolder(trader.getGameObject());
				hold = new ArrayList<GameObject>(holder.getHold());
			}
			else {
				// Visitor or Guild - trade directly with their hold
				hold = new ArrayList();
				for(Object o:trader.getGameObject().getHold()) {
					GameObject go = (GameObject)o;
					RealmComponent rc = RealmComponent.getRealmComponent(go);
					if (!rc.isSpell() || character.canLearn(go)) {
						hold.add(go);
					}
				}
			}
			
			// Find and add any boons for this trader
			hold.addAll(character.getBoons(trader.getGameObject()));
			
			// Update the character notebook accordingly
			character.addNoteTrade(trader.getGameObject(),hold);
		}
		else if (TRADE_REPAIR.equals(tradeAction)) {
			hold = TreasureUtility.getDamagedArmor(character.getSellableInventory());
		}
		else { // TRADE_SELL
			hold = character.getSellableInventory();
		}
		
		if (!hold.isEmpty()) {
			// Cool - now do trading
			
			// First, make sure all treasures are marked as "seen"
			for (Iterator i=hold.iterator();i.hasNext();) {
				GameObject item = (GameObject)i.next();
				if (!item.hasThisAttribute(Constants.TREASURE_SEEN)) {
					item.setThisAttribute(Constants.TREASURE_SEEN);
				}
			}
			
			// Just in case the Flowers of Rest are here, we'd better check for sleep...
			checkSleep();
			if (character.isSleep()) {
				// oops!
				return;
			}
			
			RealmTradeDialog tradeDialog;
			if (TRADE_BUY.equals(tradeAction)) {
				// Buying
				tradeDialog = new RealmTradeDialog(gameHandler.getMainFrame(),"Select an item or spell to BUY from "+traderRel+":",false,false,true);
				
				// Log what is being offered up
				StringBufferedList sb = new StringBufferedList();
				for(GameObject go:hold) {
					RealmComponent rc = RealmComponent.getRealmComponent(go);
					if (go.hasThisAttribute(RealmComponent.TREASURE) && !hostPrefs.hasPref(Constants.HOUSE1_NO_SECRETS)) {
						sb.append("Treasure");
					}
					else if (rc.isSpell() && !hostPrefs.hasPref(Constants.HOUSE1_NO_SECRETS)) {
						sb.append("Spell");
					}
					else {
						sb.append(go.getName());
					}
				}
				sb.countIdenticalItems();
				gameHandler.broadcast(character.getGameObject().getName(),"Buying from "+trader.getGameObject().getName()+".");
				gameHandler.broadcast(character.getGameObject().getName(),"Available for sale: "+sb.toString());
			}
			else if (TRADE_REPAIR.equals(tradeAction)) {
				// Repair
				tradeDialog = new RealmTradeDialog(gameHandler.getMainFrame(),"Select armor to have "+traderRel+" REPAIR:",false,true,false);
				tradeDialog.setRepairMode(true);
			}
			else {
				// Selling
				tradeDialog = new RealmTradeDialog(gameHandler.getMainFrame(),"Select item(s) to SELL to "+traderRel+":",true,true,false);
			}
			tradeDialog.setDealingCharacter(character);
			tradeDialog.setTrader(trader);
			tradeDialog.setTradeObjects(hold);
			tradeDialog.setVisible(true);
			
			Collection selComponents = tradeDialog.getSelectedRealmComponents();
			if (selComponents!=null && selComponents.size()>0) {
				boolean repair = TRADE_REPAIR.equals(tradeAction);
				if (TRADE_BUY.equals(tradeAction) || repair) { // TRADE_BUY or TRADE_REPAIR
					
					// Can only be one item purchased
					RealmComponent merchandise = (RealmComponent)selComponents.iterator().next();
					
					// Let's make sure this item CAN be bought
					if (!repair && hostPrefs.hasPref(Constants.HOUSE1_NO_NEGATIVE_POINTS)) {
						int famePrice = TreasureUtility.getFamePrice(merchandise.getGameObject(),trader.getGameObject());
						if (famePrice>character.getFame()) {
							JOptionPane.showMessageDialog(
									gameHandler.getMainFrame(),
									"That item would cause your fame to be negative, which violates the host's rules.",
									"Invalid Purchase",
									JOptionPane.INFORMATION_MESSAGE,
									merchandise.getFaceUpIcon());
							completed = false;
							return;
						}
					}
					
					GameObject go = merchandise.getGameObject();
					String merchandiseName = "the "+go.getName();
					if (go.hasThisAttribute(RealmComponent.TREASURE) && !hostPrefs.hasPref(Constants.HOUSE1_NO_SECRETS)) {
						merchandiseName = "a treasure";
					}
					
					if (repair) {
						gameHandler.broadcast(character.getGameObject().getName(),"Bidding to repair "+merchandiseName);
					}
					else {
						gameHandler.broadcast(character.getGameObject().getName(),"Bidding for "+merchandiseName);
					}
					
					// Determine price, and then verify with player that they want to buy
					realmTable = Meeting.createMeetingTable(
							gameHandler.getMainFrame(),
							character,
							character.getCurrentLocation(),
							trader,
							merchandise,
							null,
							RelationshipType.ALLY);
					((Meeting)realmTable).setSpecificAction("Trade");
					handleTable();
				}
				else {
					// Log what is being sold
					StringBufferedList sb = new StringBufferedList();
					for(Iterator i=selComponents.iterator();i.hasNext();) {
						RealmComponent rc = (RealmComponent)i.next();
						sb.append(rc.getGameObject().getName());
					}
					sb.countIdenticalItems();
					gameHandler.broadcast(character.getGameObject().getName(),"Selling to "+trader.getGameObject().getName()+".");
					gameHandler.broadcast(character.getGameObject().getName(),"Attempting to sell: "+sb.toString());
					
					realmTable = Commerce.createCommerceTable(
							gameHandler.getMainFrame(),
							character,
							character.getCurrentLocation(),
							trader,
							selComponents,
							RelationshipType.ALLY,
							hostPrefs);
					((Commerce)realmTable).setSpecificAction("Trade");
					handleTable();
				}
			}
			else {
				completed = false;
			}
		}
		else {
			JOptionPane.showMessageDialog(gameHandler.getMainFrame(),"Nothing to trade!");
			completed = false;
		}
	}
	private void doRestAction() {
		if (character.hasCurse(Constants.ILL_HEALTH)) {
			result = "Cannot REST with ILL HEALTH curse.";
		}
		else if (character.isTransmorphed()) {
			result = "Cannot REST while transmorphed.";
		}
		else {
			ArrayList restChoices = character.getRestableChits();
			if (!restChoices.isEmpty()) { // has to be chits to rest!
				if (RealmUtility.willBeBlocked(character,isFollowing,false)) {
					// Block after the first phase!
					
					// Make this one 1 phase, and then split any remaining count into a new action row
					int newCount = count-1;
					count = 1;
					if (newCount>0) {
						newAction = makeCopy();
						newAction.setCount(newCount);
					}
				}
				bonusCount = character.getRestBonus(count);
				ChitRestManager rester = new ChitRestManager(gameHandler.getMainFrame(),character,count+bonusCount);
				rester.setVisible(true);
				if (rester.isFinished()) {
					result = "Rested "+(count+bonusCount)+" asterisk"+((count+bonusCount)==1?"":"s");
				}
				else {
					// Cancelled!
					completed = false;
				}
			}
			else {
				if (character.hasCurse(Constants.WITHER) && character.getFatiguedChits().size()>0) {
					result = "Unable to rest fully, due to WITHER curse.";
				}
				else {
					result = "You are fully rested.";
				}
			}
		}
		// Make sure followers get a rest too!
		for (Iterator i=character.getActionFollowers().iterator();i.hasNext();) {
			CharacterWrapper follower = (CharacterWrapper)i.next();
			if (!follower.hasCurse(Constants.ILL_HEALTH)
					&& !follower.isTransmorphed()
					&& !follower.getRestableChits().isEmpty()) {
				follower.setFollowRests(count);
			}
		}
	}
	private void doHealAction() {
		// Select a character in the same clearing that has wounds/fatigue, and does not have ILL_HEALTH, or is transmorphed
		TileLocation current = character.getCurrentLocation();
		ArrayList<RealmComponent> canBeHealed = new ArrayList<RealmComponent>();
		for (RealmComponent rc:current.clearing.getClearingComponents()) {
			if (rc.isCharacter()) {
				if (!rc.getGameObject().equals(character.getGameObject())) { // can't be you!
					CharacterWrapper aCharacter = new CharacterWrapper(rc.getGameObject());
					if (!aCharacter.hasCurse(Constants.ILL_HEALTH) && !aCharacter.isTransmorphed()) {
						if (aCharacter.getRestableChits().size()>0) {
							canBeHealed.add(rc);
						}
					}
				}
			}
		}
		
		if (canBeHealed.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Who will you heal?",true);
			chooser.addRealmComponents(canBeHealed,false);
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				RealmComponent rc = chooser.getFirstSelectedComponent();
				CharacterWrapper aCharacter = new CharacterWrapper(rc.getGameObject());
				ChitRestManager rester = new ChitRestManager(gameHandler.getMainFrame(),aCharacter,1);
				rester.setVisible(true);
				if (rester.isFinished()) {
					result = "Healed the "+aCharacter.getGameObject().getName()+" 1 asterisk.";
				}
				else {
					// Cancelled!
					completed = false;
				}
			}
		}
		else {
			result = "no one to heal"; 
		}
	}
	private void doAlertAction() {
		// Make sure followers get an alert too!
		for (Iterator i=character.getActionFollowers().iterator();i.hasNext();) {
			CharacterWrapper follower = (CharacterWrapper)i.next();
			follower.addCurrentAction(DayAction.ALERT_ACTION.getCode());
			follower.addCurrentActionTypeCode(actionTypeCode);
			follower.addCurrentActionValid(true);
		}
		// Player chooses from all inactive weapons and spell chits
		ArrayList alertChoices = new ArrayList();
		Collection c = character.getActiveChits();
		for (Iterator i=c.iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			if (chit.isMagic() || chit.isFightAlert()) {
				alertChoices.add(chit);
			}
		}
		WeaponChitComponent weapon = character.getActiveWeapon();
		if (weapon!=null) {
			alertChoices.add(weapon);
		}
		if (alertChoices.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Alert which?",true);
			int keyN = 0;
			for (Iterator i=alertChoices.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isWeapon()) {
					// Add both sides of weapon, if any
					weapon = (WeaponChitComponent)rc;
					String key = "a"+(keyN++);
					chooser.addOption(key,"Alert");
					chooser.addRealmComponentToOption(key,weapon,weapon.isAlerted()?RealmComponentOptionChooser.DisplayOption.Normal:RealmComponentOptionChooser.DisplayOption.Flipside);
					key = "u"+(keyN++);
					chooser.addOption(key,"Unalert");
					chooser.addRealmComponentToOption(key,weapon,weapon.isAlerted()?RealmComponentOptionChooser.DisplayOption.Flipside:RealmComponentOptionChooser.DisplayOption.Normal);
				}
				else {
					String key = "k"+(keyN++);
					chooser.addOption(key,"Alert");
					chooser.addRealmComponentToOption(key,rc);
				}
			}
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				RealmComponent rc = chooser.getFirstSelectedComponent();
				if (rc.isWeapon()) {
					boolean alert = chooser.getSelectedOptionKey().startsWith("a");
					((WeaponChitComponent)rc).setAlerted(alert);
				}
				else {
					CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
					if (chit.isFightAlert()) {
						chit.makeFatigued(); // fatigues instantly
						character.getGameObject().setThisAttribute(Constants.ENHANCED_VULNERABILITY,chit.getFightAlertVulnerability());
					}
					else {
						chit.makeAlerted();
					}
				}
				result = "alerted "+rc.getGameObject().getName();
				gameHandler.updateCharacterFrames();
			}
			else {
				if (isFollowing) {
					int ret = JOptionPane.showConfirmDialog(
							gameHandler.getMainFrame(),
							"Do you want to skip the ALERT action?",
							"ALERT is optional for followers",
							JOptionPane.YES_NO_OPTION);
					if (ret==JOptionPane.YES_OPTION) {
						cancelled = true;
						return;
					}
				}
				completed = false;
				return;
			}
		}
		else {
			result = "nothing to alert";
		}
	}
	private void doRepairAction() {
		ArrayList<ArmorChitComponent> damagedArmor = new ArrayList<ArmorChitComponent>();
		for(GameObject go:character.getInventory()) {
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isArmor()) {
				ArmorChitComponent armor = (ArmorChitComponent)rc;
				if (armor.isDamaged()) {
					damagedArmor.add(armor);
				}
			}
		}
		
		if (damagedArmor.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Repair which?",true);
			chooser.addRealmComponents(damagedArmor,false);
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				ArmorChitComponent armor = (ArmorChitComponent)chooser.getFirstSelectedComponent();
				armor.setIntact(true);
				result = "repaired "+armor.getGameObject().getName();
				gameHandler.updateCharacterFrames();
			}
		}
		else {
			result = "nothing to repair";
		}
	}
	private void doHireAction() {
		// Player chooses from native groups, and then gets to bid on lowest ranked native
		
		// hire_type=single or group
		// If "group", hire highest number first, down to HQ last (HQ is essentially 0)
		// HIRE same as TRADE, only the merchandise is the natives themselves
		// Term of hire is fourteen days, or until the character is killed
		TileLocation tl = character.getCurrentLocation();
		ArrayList hireables = ClearingUtility.getAllHireables(character,tl.clearing);
		HashLists hash = RealmUtility.hashNativesByGroupName(hireables);
		if (hash.size()>0) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Hire which?",true);
			chooser.setButtonTextPosition(SwingConstants.CENTER,SwingConstants.BOTTOM);
			//chooser.setForceColumns(1);
			chooser.setFillByRow(false);
			chooser.setSortBiggestFirst(true);
			for (Iterator i=hash.keySet().iterator();i.hasNext();) {
				String groupName = (String)i.next();
				ArrayList list = hash.getList(groupName);
				Collections.sort(list,new Comparator() {
					public int compare(Object o1,Object o2) {
						RealmComponent n1 = (RealmComponent)o1;
						String rs1 = n1.getGameObject().getThisAttribute("rank");
						if (rs1==null) rs1 = "0";
						Integer rank1 = "HQ".equals(rs1)?new Integer(0):Integer.valueOf(rs1);
						
						RealmComponent n2 = (RealmComponent)o2;
						String rs2 = n2.getGameObject().getThisAttribute("rank");
						if (rs2==null) rs2 = "0";
						Integer rank2 = "HQ".equals(rs2)?new Integer(0):Integer.valueOf(rs2);
						
						return rank1.compareTo(rank2);
					}
				});
				int basePrice;
				ChitComponent last = (ChitComponent)list.get(list.size()-1);
				if ("group".equals(last.getGameObject().getThisAttribute("hire_type")) || last.isMonster()) {
					// Add the whole group if hire_type is group, or we are referring to monsters
					String option = chooser.generateOption(StringUtilities.capitalize(groupName));
					basePrice = 0;
					int rehire = 0;
					int newhire = 0;
					for (Iterator n=list.iterator();n.hasNext();) {
						RealmComponent rc = (RealmComponent)n.next();
						if (rc.getOwnerId()==null) newhire++; else rehire++;
						chooser.addRealmComponentToOption(option,rc);
						basePrice += rc.getGameObject().getThisInt("base_price");
					}
					String prefix = "Hire ";
					if (rehire>0) {
						if (newhire==0) {
							prefix = "Rehire ";
						}
						else {
							prefix = "Hire (some rehire) ";
						}
					}
					String postfix = "";
					if (last.isMonster() && list.size()>1) {
						postfix = "s";
					}
					if (character.hasActiveInventoryThisKeyAndValue(Constants.HALF_PRICE,groupName)) {
						basePrice >>= 1;
					}
					chooser.addOption(option,prefix+StringUtilities.capitalize(groupName)+postfix+" (base: "+basePrice+" gold)");
				}
				else { // hire_type=="single" or a traveler
					// Add only the last unhired member of the group
					// Add all the hired natives (one at a time)
					last = null;
					for (Iterator n=list.iterator();n.hasNext();) {
						RealmComponent rc = (RealmComponent)n.next();
						if (rc.getOwnerId()==null) {
							last = (ChitComponent)rc;
						}
						else {
							// Re-hire
							String option = chooser.generateOption(StringUtilities.capitalize(groupName));
							chooser.addRealmComponentToOption(option,rc);
							basePrice = rc.getGameObject().getThisInt("base_price");
							chooser.addOption(option,"Rehire "+StringUtilities.capitalize(groupName)+" (base: "+basePrice+" gold)");
						}
					}
					if (last!=null) {
						// New hire single
						String option = chooser.generateOption(StringUtilities.capitalize(groupName));
						chooser.addRealmComponentToOption(option,last);
						basePrice = last.getGameObject().getThisInt("base_price");
						if (character.hasActiveInventoryThisKeyAndValue(Constants.HALF_PRICE,groupName)) {
							basePrice >>= 1;
						}
						chooser.addOption(option,"Hire "+StringUtilities.capitalize(groupName)+" (base: "+basePrice+" gold)");
					}
				}
			}
			
			chooser.addOption("none","Cancel Hire");
			chooser.setVisible(true);
			String selText = chooser.getSelectedText();
			if (selText!=null) {
				if ("Cancel Hire".equals(selText)) {
					result = "Cancelled Hire";
					return;
				}
				ArrayList list = new ArrayList(chooser.getSelectedComponents());
				ChitComponent last = (ChitComponent)list.get(list.size()-1);
				
				// Now we have the group to hire.  Need to do the Meeting table...
				realmTable = Meeting.createMeetingTable(
						gameHandler.getMainFrame(),
						character,
						character.getCurrentLocation(),
						last,
						null,
						list,
						last.isTraveler()?RelationshipType.NEUTRAL:RelationshipType.ALLY);
				((Meeting)realmTable).setSpecificAction("Hire");
				
				if (last.isTraveler()) {
					// No need to roll for travelers
					((Meeting)realmTable).hiringNatives(character,1);
				}
				else {
					handleTable();
				}
			}
			else {
				completed = false;
			}
		}
		else {
			result = "Nobody to hire";
		}
	}
	private void doSpellAction() {
		TileLocation targetClearing = character.getCurrentLocation();
		if (character.getPeerAny()) {
			CenteredMapView.getSingleton().setMarkClearingAlertText("Enchant in which clearing?");
			CenteredMapView.getSingleton().markAllClearings(true);
			TileLocationChooser chooser = new TileLocationChooser(gameHandler.getMainFrame(),CenteredMapView.getSingleton(),targetClearing);
			chooser.setVisible(true);
			CenteredMapView.getSingleton().markAllClearings(false);
			targetClearing = chooser.getSelectedLocation();
		}
		doSpellAction(character.getInfiniteColorSources(),targetClearing);
	}
	private void doSpellAction(Collection colorMagicSources,TileLocation targetClearing) {
		// SPX actions are ignored.  Need to ask player if they want
		// to enchant a chit, or a tile.  The tile option would only be available if the conditions are right
		// (right color/invocation combination available)
		
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(gameHandler.getClient().getGameData());
		
		ArrayList<MagicChit> enchantable = new ArrayList<MagicChit>();
		
		// Chits
		ArrayList enchantableChits = character.getEnchantableChits();
		Collections.sort(enchantableChits);
		enchantable.addAll(enchantableChits);
		
		if (hostPrefs.hasPref(Constants.OPT_ENHANCED_ARTIFACTS)) {
			// Enchantable Artifacts and Books
			for(GameObject item:character.getActiveInventory()) {
				RealmComponent rc = RealmComponent.getRealmComponent(item);
				if (rc.isMagicChit()) {
					MagicChit mc = (MagicChit)rc;
					if (mc.isEnchantable()) {
						enchantable.add(mc);
					}
				}
			}
		}
	
		if (!enchantable.isEmpty()) {
			// Determine if any of the color magic (infinite sources first) are available to enchant the tile
			ArrayList tileEnchantableSets = new ArrayList(); // CharacterChitActionComponent[] set
			for (MagicChit chit:enchantable) {
				for (Iterator i=colorMagicSources.iterator();i.hasNext();) {
					ColorMagic infiniteSource = (ColorMagic)i.next();
					if (chit.compatibleWith(infiniteSource)) {
						// Create a set of one (no need to use up your own color magic when there is an infinite source!)
						RealmComponent[] set = new RealmComponent[2];
						set[0] = (RealmComponent)chit;
						set[1] = targetClearing.tile;
						tileEnchantableSets.add(set);
						break; // no need to keep searching infinite sources!  Any one is good enough.
					}
				}
			}
			// check own color chits (player may not want to use infinite source if it uses the wrong chit!)
			ArrayList<MagicChit> colorMagicChits = new ArrayList<MagicChit>();
			colorMagicChits.addAll(character.getColorChits());
			if (hostPrefs.hasPref(Constants.OPT_ENHANCED_ARTIFACTS)) {
				// Artifacts and Books enchanted into color
				for(GameObject item:character.getActiveInventory()) {
					RealmComponent rc = RealmComponent.getRealmComponent(item);
					if (rc.isMagicChit()) {
						MagicChit mc = (MagicChit)rc;
						if (mc.isColor()) {
							colorMagicChits.add(mc);
						}
					}
				}
			}
			for (MagicChit chit:enchantable) {
				for (MagicChit colorChit:colorMagicChits) {
					ColorMagic consumableSource = colorChit.getColorMagic();
					if (chit.compatibleWith(consumableSource)) {
						// Create a set of one (no need to use up your own color magic when there is an infinite source!)
						RealmComponent[] set = new RealmComponent[3];
						set[0] = (RealmComponent)chit;
						set[1] = (RealmComponent)colorChit;
						set[2] = targetClearing.tile;
						tileEnchantableSets.add(set);
						// find all possible combinations!
					}
				}
			}
			
			RealmComponentOptionChooser compChooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Enchant which?",true);
			int keyN = 0;
			for (Iterator i=enchantable.iterator();i.hasNext();) {
				RealmComponent chit = (RealmComponent)i.next();
				String key = "k"+(keyN++);
				if (chit.isActionChit()) {
					compChooser.addOption(key,"MAGIC Chit");
				}
				else {
					compChooser.addOption(key,"Artifact/Book");
				}
				compChooser.addRealmComponentToOption(key,chit);
			}
			for (Iterator i=tileEnchantableSets.iterator();i.hasNext();) {
				RealmComponent[] chit = (RealmComponent[])i.next();
				String key = "k"+(keyN++);
				compChooser.addOption(key,"Tile");
				for (int n=0;n<chit.length;n++) {
					compChooser.addRealmComponentToOption(key,chit[n]);
				}
			}
			if (compChooser.hasOptions()) {
				compChooser.setVisible(true);
				String text = compChooser.getSelectedText();
				if (text!=null) {
					if ("Tile".equals(text)) {
						// enchant a tile
						TileComponent tile = targetClearing.tile;
						tile.flip();
						result = "enchanted "+tile.getTileName();
						// fatigue the chit(s) used to do it
						Collection chits = compChooser.getSelectedComponents();
						for (Iterator i=chits.iterator();i.hasNext();) {
							RealmComponent rc = (RealmComponent)i.next();
							if (rc.isMagicChit()) {
								MagicChit chit = (MagicChit)rc;
								if (chit.isColor()) { // Only fatigue the color chit - not the incantation
									chit.makeFatigued();
									RealmUtility.reportChitFatigue(character,chit,"Fatigued color chit: ");
								}
							}
						}
						gameHandler.updateCharacterFrames();
						gameHandler.broadcastMapReplot();
					}
					else {
						// enchant a chit
						MagicChit chit = (MagicChit)compChooser.getFirstSelectedComponent();
						if (chit!=null) {
							int enchantNumber;
							ArrayList<Integer> list = chit.getEnchantableNumbers();
							if (list.size()>1) {
								ButtonOptionDialog colorChooser = new ButtonOptionDialog(gameHandler.getMainFrame(),chit.getIcon(),"What color?","Enchant "+chit.getGameObject().getName(),false);
								for(int mn:list) {
									ColorMagic cm = new ColorMagic(mn,false);
									colorChooser.addSelectionObject(cm.getColorName());
								}
								colorChooser.setVisible(true);
								String colorName = (String)colorChooser.getSelectedObject();
								enchantNumber = ColorMagic.makeColorMagic(colorName,false).getColorNumber();
							}
							else {
								enchantNumber = list.get(0);
							}
							
							chit.enchant(enchantNumber);
							result = "enchanted "+chit.getGameObject().getName();
							gameHandler.updateCharacterFrames();
						}// this shouldn't happen
					}
				}
				else {
					completed = false;
					return;
				}
			}
		}
		else {
			result = "nothing to enchant";
		}
	}
	private void doEnhancedPeerAction() {
		// Player peers into clearing
		if (location.clearing != null) {
			// clearing might NOT be on the same side, if a tile flipped somewhere, so update it here
			location.clearing = location.clearing.correctSide();
			realmTable = new EnhancedPeer(gameHandler.getMainFrame(),location.clearing);
			handleTable();
		}
	}
	private void doFlyAction() {
		// First, make sure Flying is a possibility - otherwise BLOCK character..? See Rule 47.2
		ArrayList<StrengthChit> flyStrengthChits = character.getFlyStrengthChits(true);
		TileLocation current = character.getCurrentLocation();
		boolean startedBetweenTiles = current.isBetweenTiles();
		if (current.isFlying() && (startedBetweenTiles || current.isTileOnly())) {
			// Must be able to fly
			flyStrengthChits.add(0,new StrengthChit(null,new Strength("T"),new Speed(0))); // this will ALWAYS be the strongest chit, so add it first
		}
		if (flyStrengthChits.isEmpty()) {
			result = "Unable to fly.";
			cancelled = true;
			return;
		}
		
		// Strip out any chits that aren't strong enough to support character
		Strength vul = character.getVulnerability();
		ArrayList<StrengthChit> strongEnough = new ArrayList<StrengthChit>();
		for (StrengthChit sc:flyStrengthChits) {
			if (sc.getStrength().strongerOrEqualTo(vul)) {
				strongEnough.add(sc);
			}
		}
		
		// Make sure Character is not too heavy
		if (strongEnough.isEmpty()) {
			result = "Too heavy to fly.";
			cancelled = true;
			return;
		}
		
		flyStrengthChits = strongEnough;
		
		// Make sure intended target tile for flying is possible (might not be if a previously recorded move is invalid!)
		ArrayList allAvailableTiles = new ArrayList(current.tile.getAllAdjacentTiles());
		allAvailableTiles.add(current.tile);
		if (!allAvailableTiles.contains(location.tile)) {
			result = "Target tile too far away.";
			cancelled = true;
			return;
		}
		
		// Choose a fly chit (if necessary)
		StrengthChit flyStrengthChit = null;
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Choose a FLY chit:",true);
		for (StrengthChit sc:flyStrengthChits) {
			if (sc.getGameObject()==null) {
				flyStrengthChit = sc;
				break;
			}
			chooser.addRealmComponent(sc.getRealmComponent());
		}
		Fly fly = null;
		if (flyStrengthChit==null) {
			chooser.setVisible(true);
			RealmComponent rc = chooser.getFirstSelectedComponent();
			if (rc==null) {
				// cancelled allows you to back out
				completed = false;
				return;
			}
			fly = new Fly(rc);
		}
		else {
			fly = new Fly(flyStrengthChit);
		}
		
		// Next, drop all items heavier than the fly chit, AND any horses, regardless of weight
		if (!current.isBetweenTiles()) {
			ArrayList<GameObject> toDrop = RealmUtility.dropNonFlyableStuff(gameHandler.getMainFrame(),character,fly,current);
			if (toDrop==null) {
				completed = false;
				return;
			}
			for (GameObject item:toDrop) {
				gameHandler.broadcast(character.getGameObject().getName(),item.getName()+" was left behind!");
			}
		}
		
		// Good.  Flying is possible, and will happen.  Check to see if we are using up a FLY chit.
		if (fly!=null) {
			fly.useFly();
		}
		
		// Player is moved to new location
		if (location != null) {
			character.moveToLocation(gameHandler.getMainFrame(),location);
			result = "Flew to tile.";
			if (startedBetweenTiles) {
				character.land(gameHandler.getMainFrame());
				result = "Flew to tile and landed.";
			}
			gameHandler.getInspector().getMap().centerOn(character.getCurrentLocation());
			gameHandler.updateCharacterFrames();
			
			// Character's do not stay hidden when they fly
			if (character.isHidden()) {
				character.setHidden(false);
			}
			
			// Followers shouldn't follow here, unless they can fly, or they are a familiar
			for (Iterator i=character.getActionFollowers().iterator();i.hasNext();) {
				CharacterWrapper follower = (CharacterWrapper)i.next();
				if (follower.mustFly() || follower.isFamiliar()) {
					follower.moveToLocation(gameHandler.getMainFrame(),location);
					if (follower.isHidden()) {
						follower.setHidden(false);
					}
				}
				else {
					character.removeActionFollower(follower,gameHandler.getGame().getMonsterDie());
				}
			}
		}
		else {
			// this should never happen
			throw new IllegalStateException("null location during ActionRow.process!");
		}
	}
	private void doRemoteSpellAction() {
		Collection colorSources = character.getInfiniteColorSources();
		colorSources.addAll(location.clearing.getAllSourcesOfColor(true));
		doSpellAction(colorSources,location);
	}
	private void doCacheAction() {
		RealmComponent charRc = RealmComponent.getRealmComponent(character.getGameObject());
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(gameHandler.getMainFrame(),"Select cache:",true);
		TileLocation tl = character.getCurrentLocation();
		if (tl.isInClearing()) {
			// Add an option to open a new cache
			chooser.generateOption("New CACHE");
			
			// Add all existing caches in clearing
			for (Iterator i=tl.clearing.getClearingComponents().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isCacheChit()) {
					if (rc.getOwner()==charRc) { // Only the individual that created the cache can open it!
						String key = chooser.generateOption("Open");
						chooser.addRealmComponentToOption(key, rc);
					}
				}
			}
			
			chooser.setVisible(true);
			String sel = chooser.getSelectedText();
			if (sel!=null) {
				CacheChitComponent cache;
				if (sel.startsWith("New")) {
					// New CACHE
					GameObject go = character.getGameObject().getGameData().createNewObject();
					int num = character.getNextCacheNumber();
					go.setName(character.getGameObject().getName()+"'s Cache #"+num);
					character.addTreasureLocationDiscovery(go.getName());
					go.setThisAttribute(RealmComponent.CACHE_CHIT);
					go.setThisAttribute("clearing",tl.clearing.getNumString());
					go.setThisAttribute(RealmComponent.TREASURE_LOCATION,character.getGameObject().getName());
					go.setThisAttribute("cache_number",num);
					go.setThisAttribute("discovery");
					go.setThisAttribute("chit");
					
					// These attributes will enable the cache to appear on the setup card
					HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(go.getGameData());
					go.setThisAttribute("ts_section","zcache"); // z so that it sorts to the end
					go.setThisAttribute("ts_color","gold");
					go.setThisKeyVals(hostPrefs.getGameKeyVals());
					go.setThisAttribute("ts_cansee",character.getPlayerName());
					if (character.getGameObject().hasThisAttribute(Constants.BOARD_NUMBER)) {
						// Might as well put the B character caches on the B setup card, and so on.
						go.setThisAttribute(Constants.BOARD_NUMBER,character.getGameObject().getThisAttribute(Constants.BOARD_NUMBER));
					}
					
					cache = (CacheChitComponent)RealmComponent.getRealmComponent(go);
					cache.setOwner(RealmComponent.getRealmComponent(character.getGameObject()));
					cache.setFaceUp();
					tl.clearing.add(go,null);
				}
				else {
					// Existing CACHE
					cache = (CacheChitComponent)chooser.getFirstSelectedComponent();
				}
				
				// Trade with CACHE
				CharacterWrapper cacheCharacter = new CharacterWrapper(cache.getGameObject());
				CacheTransferDialog transferDialog = new CacheTransferDialog(
													gameHandler.getMainFrame(),
													character,
													cacheCharacter,
													gameHandler.getUpdateFrameListener());
				transferDialog.setVisible(true);
				
				// If cache is empty, delete it
				cache.testEmpty();
			}
			else {
				completed = false;
			}
		}
		else {
			result = "Can only CACHE in a clearing!";
		}
	}
	/**
	 * @return Returns the newAction.
	 */
	public ActionRow getNewAction() {
		return newAction;
	}
	/**
	 * @param result The result to set.
	 */
	public void setResult(String result) {
		this.result = result;
	}
	/**
	 * @return Returns the spawned.
	 */
	public boolean isSpawned() {
		return spawned;
	}
	/**
	 * @param spawned The spawned to set.
	 */
	public void setSpawned(boolean spawned) {
		this.spawned = spawned;
	}
	public void makeBlankPhase(String reason) {
		blankReason = reason;
	}
	public boolean isBlankPhase() {
		return blankReason!=null;
	}
	public void makeInvalidPhase() {
		invalid = true;
	}
	public boolean isInvalidPhase() {
		return invalid;
	}
	public boolean willMoveToCave() {
		return (action.startsWith("M") && location.isInClearing() && location.clearing.isCave());
	}
	/**
	 * @return Returns the isFollowing.
	 */
	public boolean getIsFollowing() {
		return isFollowing;
	}

	public void setPonyLock(boolean ponyLock) {
		this.ponyLock = ponyLock;
	}
	public boolean isPonyLock() {
		return ponyLock;
	}
	
	public boolean isFollow() {
		ActionId id = CharacterWrapper.getIdForAction(action);
		return (id==ActionId.Follow);
	}
	
	public static void main(String[] args) {
		RealmLoader loader = new RealmLoader();
		CardComponent em = (CardComponent)RealmComponent.getRealmComponent(loader.getData().getGameObjectByName("Enchanted Meadow"));
		ChitComponent tr = (ChitComponent)RealmComponent.getRealmComponent(loader.getData().getGameObjectByName("Guard 1"));
		em.setFaceUp();
		ButtonOptionDialog chooser = new ButtonOptionDialog(new JFrame(),null,"Foobar!","Hey there!");
		chooser.addSelectionObject("Test");
		IconGroup group = new IconGroup(IconGroup.HORIZONTAL,2);
		group.addIcon(em.getMediumIcon());
		group.addIcon(tr.getMediumIcon());
		chooser.setSelectionObjectIcon("Test",group);
		chooser.setVisible(true);
		System.exit(0);
	}
}