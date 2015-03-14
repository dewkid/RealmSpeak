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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.game.server.*;
import com.robin.general.io.SendMail;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.FlashingButton;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.RealmBattle;
import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.RealmLogWindow;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class RealmHostPanel extends JPanel {

	public static final File AUTOSAVEFILE_BIRDSONG = (new File("autosave_birdsong.rsgame")).getAbsoluteFile();
	public static final File AUTOSAVEFILE = (new File("autosave.rsgame")).getAbsoluteFile();
	public static final File INITIALSAVEFILE = (new File("initsave.rsgame")).getAbsoluteFile();

	private static Logger logger = Logger.getLogger(RealmHostPanel.class.getName());

	protected GameHost host;
	protected GameWrapper game;
	protected HostPrefWrapper hostPrefs;

	protected JTable connectionTable;
	protected ArrayList<GameServer> connections;
	protected Hashtable<String,String> playerEmails;
	
	protected JButton killConnectionButton;

	protected ArrayList changeListeners;

	private boolean listen;
	private boolean doAutoSave = false;

	/* It seems to me, the driving force of the game progression is in the updateCharaters method.  Each time a change is detected
	 * in the host (via the GameHostListener) the characters are polled, and the gamestate is checked.  Here's a summary of the
	 * comments below:
	 * 
	 * GAME_STATE_RECORDING (Set at the start of the game)
	 * 		HOSTCHANGE:		all existing players advance one day, all new players get their first day
	 * 		CHANGES:		characterCount>0 && charactersStillRecording==0
	 * 		FINISH:			player order is determined (2 to number of players+1), state goes to GAME_STATE_PLAYING
	 * 
	 * GAME_STATE_PLAYING:
	 * 		HOSTCHANGE:		player order is renumbered, player 1 is notified to start turn
	 * 		CHANGES:		charactersWithPlayOrder==0
	 * 		FINISH:			state goes to GAME_STATE_RESOLVING
	 * 
	 * GAME_STATE_RESOLVING
	 * 		HOSTCHANGE:		combats are resolved one at a time (in what order?)
	 * 		CHANGES:		combats==0
	 * 		FINISH:			advance game day, state goes to GAME_STATE_RECORDING
	 */

	public RealmHostPanel(GameHost host,boolean listen) {
		logger.fine("New Host Started");
		this.host = host;
		this.listen = listen;
		connections = new ArrayList<GameServer>(host.getServers());
		playerEmails = new Hashtable<String,String>();
		setup();
		initComponents();
	}
	public boolean isLocal() {
		return !listen;
	}

	public void dropAllConnections() {
		host.killAllOutsideConnections();
		rebuildConnectionList();
	}
	private void rebuildConnectionList() {
		connections.clear();
		connections.addAll(host.getServers());
		connectionTable.revalidate();
		connectionTable.repaint();
	}
	
	private void updateControls() {
		killConnectionButton.setEnabled(connectionTable.getSelectedRow()>0); // not zero, because that's the host!!
	}
	
	private void killSelectedConnection() {
		int selected = connectionTable.getSelectedRow();
		connectionTable.clearSelection();
		GameServer server = connections.get(selected);
		host.killConnection(server);
		rebuildConnectionList();
	}

	public void initComponents() {
		setSize(400, 200);
		setLayout(new BorderLayout());
		
		Box topControls = Box.createHorizontalBox();
		killConnectionButton = new JButton("Kill Selected Connection");
		killConnectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				killSelectedConnection();
			}
		});
		topControls.add(Box.createHorizontalGlue());
		topControls.add(killConnectionButton);
		add(topControls,BorderLayout.NORTH);

		connectionTable = new JTable(new ConnectionTableModel());
		connectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		connectionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		int n = 50;
		TableColumn col = connectionTable.getColumnModel().getColumn(3);
		col.setMinWidth(n);
		col.setMaxWidth(n);
		col.setPreferredWidth(n);
		add(new JScrollPane(connectionTable), "Center");

		host.addGameHostListener(new GameHostListener() {
			public void hostModified(GameHostEvent ev) {
				logger.finer("hostModified");
				rebuildConnectionList();
				updateGame(); // This should be the only call to this method.
				if (ev.getNotice()==GameHostEvent.NOTICE_NEW_CONNECTION) {
					RealmDirectInfoHolder holder;
					
					// New connections should provide an email
					holder = new RealmDirectInfoHolder(host.getGameData());
					holder.setCommand(RealmDirectInfoHolder.HOST_NEED_EMAIL);
					ev.getServer().addInfoDirect(new InfoObject(ev.getServer().getClientName(),holder.getInfo()));
					
					// New connections need a copy of the detail log!
					ArrayList<String[]> aList = RealmLogWindow.getSingleton().getStringArrayList();
					ArrayList<String> list = new ArrayList<String>();
					for (String[] line:aList) {
						list.add(line[0]);
						list.add(line[1]);
					}
					holder = new RealmDirectInfoHolder(host.getGameData());
					holder.setCommand(RealmDirectInfoHolder.HOST_DETAIL_LOG);
					holder.setStrings(list);
					ev.getServer().addInfoDirect(new InfoObject(ev.getServer().getClientName(),holder.getInfo()));
					
					// New connections need the random number generator
					holder = new RealmDirectInfoHolder(host.getGameData());
					holder.setCommand(RealmDirectInfoHolder.RANDOM_NUMBER_GENERATOR);
					holder.setString(RandomNumber.getRandomNumberGenerator().toString());
					ev.getServer().addInfoDirect(new InfoObject(ev.getServer().getClientName(),holder.getInfo()));
				}
			}
			
			public void handleHostOnlyInfo(InfoObject io) {
				handleHostOnly(io);
			}

			public void serverLost(GameHostEvent ev) {
				logger.finer("serverLost");
				// Handle lost servers here
				GameServer server = ev.getServer();

				// Find all characters belonging to that server
				GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(host.getGameData()).getPlayerCharacterObjects());
				ArrayList keyVals = new ArrayList();
				keyVals.add(CharacterWrapper.NAME_KEY + "=" + server.getClientName());
				Collection chars = pool.find(keyVals);
				if (chars != null && !chars.isEmpty()) {
					for (Iterator i = chars.iterator(); i.hasNext();) {
						GameObject aChar = (GameObject) i.next();
						CharacterWrapper lostChar = new CharacterWrapper(aChar);
						if (lostChar.isActive()) {
							lostChar.setMissingInAction(true);
						}
						Collection minions = lostChar.getMinions();
						if (minions!=null) {
							for (Iterator m=minions.iterator();m.hasNext();) {
								GameObject minion = (GameObject)m.next();
								CharacterWrapper lostMinion = new CharacterWrapper(minion);
								lostMinion.setMissingInAction(true);
							}
						}
					}
					updateServerData();
				} // else no worries!  They never started a character.
				rebuildConnectionList();
			}
		});
		if (listen) {
			host.startListening(hostPrefs.getGamePort());
			GameConnector connector = host.getConnector();
			add(new JLabel("IP:  " + connector.getIPAddress() + "   Port " + connector.getPort()), "South");
		}
		else {
			add(new JLabel("Not connected"),"South");
		}
		updateControls();
	}
	
	private void handleHostOnly(InfoObject io) {
		RealmDirectInfoHolder holder = new RealmDirectInfoHolder(host.getGameData(),io.getInfo());
		if (RealmDirectInfoHolder.CLIENT_RESPOND_EMAIL.equals(holder.getCommand())) {
			String player = holder.getPlayerName();
			String email = SendMail.normalizeEmail(holder.getString());
			playerEmails.put(player,email);
			
			// Cycle through all active player characters, and reassign the e-mail (in case it has changed)
			for (GameObject go:getLivingCharacters()) {
				CharacterWrapper character = new CharacterWrapper(go);
				if (player.equals(character.getPlayerName())) {
					character.setPlayerEmail(email);
				}
			}
			
			connectionTable.revalidate();
		}
	}

	public void addChangeListener(ChangeListener listener) {
		if (changeListeners == null) {
			changeListeners = new ArrayList();
		}
		changeListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		if (changeListeners != null) {
			changeListeners.remove(listener);
			if (changeListeners.size() == 0) {
				changeListeners = null;
			}
		}
	}

	private void fireStateChanged() {
		if (changeListeners != null) {
			ChangeEvent ev = new ChangeEvent(this);
			for (Iterator i = changeListeners.iterator(); i.hasNext();) {
				ChangeListener listener = (ChangeListener) i.next();
				listener.stateChanged(ev);
			}
		}
	}

	public int getGameState() {
		return game.getState();
	}

	/**
	 * This method is the driving force of the game.  As changes are received by the servers, this method checks the information,
	 * and makes changes if necessary.
	 */
	private synchronized void updateGame() {
		logger.fine("updateGame - start");
		FlashingButton.setFlashEnabled(connections.size() > 1); // enable flashing buttons only if more than one player online
		updateGameState();
		updateServerData();
		fireStateChanged();
		if (doAutoSave) {
			host.getGameData().zipToFile(AUTOSAVEFILE);
			RealmLogWindow.getSingleton().save(AUTOSAVEFILE);
			host.getGameData().zipToFile(AUTOSAVEFILE_BIRDSONG);
			RealmLogWindow.getSingleton().save(AUTOSAVEFILE_BIRDSONG);
			doAutoSave = false;
			logger.fine("Saved file");
//System.out.println("Auto save during update");
		}
		logger.fine("updateGame - done");
	}
	
	private void autoSaveNow() {
		if (hostPrefs.getAutosaveEnabled()) {
			host.getGameData().zipToFile(AUTOSAVEFILE);
			RealmLogWindow.getSingleton().save(AUTOSAVEFILE);
			logger.fine("Saved midday file");
//System.out.println("Auto save midday during update");
		}
	}

	private void updateGameState() {
		logger.fine("Entering updateGameState");
		RealmCalendar cal = RealmCalendar.getCalendar(host.getGameData());
		// Check the condition that ONLY happens at the beginning of a game: game day is 0
		if (game.getDay()==0) {
			// Determine how many days in the week (DIW)
			int days = cal.getDays(game.getMonth());
			
			// Add a number of days equal to (8-DIW)
			game.setDay(8-days);
		}

		int currentGameState = game.getState();
		if (currentGameState == GameWrapper.GAME_STATE_RECORDING) {
			updateGameStateRecording(cal);
		}
		else if (currentGameState == GameWrapper.GAME_STATE_PLAYING) {
			updateGameStatePlaying();
		}
		else if (currentGameState == GameWrapper.GAME_STATE_RESOLVING) {
			updateGameStateResolving();
		}
		else if (currentGameState == GameWrapper.GAME_STATE_DAYEND) {
			updateGameStateMidnight(cal);
		}
		logger.fine("Exiting updateGameState");
	}
	private void updateGameStateRecording(RealmCalendar cal) { // BIRDSONG
		logger.fine("BIRDSONG");
		int recordingCount = 0;
		ArrayList<GameObject> livingCharacters = getLivingCharacters();
		
		//checkForGameEnd(livingCharacters);
		
		// Land any non-turn-recording denizens now
		ArrayList<GameObject> denizenObjects = RealmObjectMaster.getRealmObjectMaster(host.getGameData()).getDenizenObjects();
		for (GameObject go:denizenObjects) {
			String blownSpellId = go.getThisAttribute(Constants.BLOWS_TARGET);
			if (blownSpellId!=null) {
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (!rc.getGameObject().hasThisAttribute(Constants.DEAD) && !rc.isPlayerControlledLeader()) {
					// Land now!
					TileLocation current = rc.getCurrentLocation();
					if (current!=null) {
						current.setFlying(false);
						while(current.clearing==null) {
							int r = RandomNumber.getHighLow(1,6);
							current.clearing = current.tile.getClearing(r);
						}
						ClearingUtility.moveToLocation(go,current);
					}
				}
				
				// Regardless, expire the wind spell
				GameObject spellGo = host.getGameData().getGameObject(Long.valueOf(blownSpellId));
				SpellWrapper spell = new SpellWrapper(spellGo);
				spell.expireSpell();
			}
		}

		for (Iterator i = livingCharacters.iterator(); i.hasNext();) {
			CharacterWrapper character = new CharacterWrapper((GameObject) i.next());

			if (character.getCurrentMonth() != game.getMonth() || character.getCurrentDay() != game.getDay()) {
				// Must be a new character or new day - set 'em up.
				logger.fine("RealmHostPanel setting up new character or new day: " + character.getCharacterName());
				
				TileLocation current = character.getCurrentLocation();

				// First, see if game is over!
				if (game.getState() == GameWrapper.GAME_STATE_GAMEOVER) {
					// Mark game over
					character.applyMidnight();
					character.setGameOver(true);
				}
				else if (current!=null && !character.isGone()) {
					// Setup BIRDSONG
					character.setDoRecord(true);
					character.setCurrentMonth(game.getMonth());
					character.setCurrentDay(game.getDay());
					character.setBasicPhases(cal.getBasicPhases(game.getMonth()));
					character.setSunlightPhases(cal.getSunlightPhases(game.getMonth()));
					character.setShelteredPhases(cal.getShelteredPhases(game.getMonth()));
					character.setMountainMoveCost(cal.getMountainMoveCost(game.getMonth()));
					character.startNewDay(cal,hostPrefs);
					
//						if (game.getTurnCount()>1 && game.getDay()==1) {
//							// New month (other than first).  Update VPs
//							character.updateNewVPRequirement(1);
//						}
				}
			}
			if (character.isDoRecord()) {
				recordingCount++;
			}
		}

		if (livingCharacters.size()==0 || recordingCount == livingCharacters.size()) {
			if (hostPrefs.getAutosaveEnabled()) {
				// Do an auto-save at the start of any day
				doAutoSave = true;
			}
		}
		
		// Extract active characters, so we don't get stuck in an infinite loop
		ArrayList activeCharacters = new ArrayList();
		for (Iterator i = livingCharacters.iterator(); i.hasNext();) {
			CharacterWrapper character = new CharacterWrapper((GameObject) i.next());
			if (character.isActive()) {
				activeCharacters.add(character);
			}
		}

		if (activeCharacters.size() > 0 && recordingCount == 0 && game.getState() != GameWrapper.GAME_STATE_GAMEOVER) { // FINISH BIRDSONG, START PLAY
			// Done recording, order players, and start next phase
			logger.fine("Done recording.  Order players and start next phase.");

			// Roll monster die (or dice)
			DieRoller monsterDieRoller = new DieRoller();
			monsterDieRoller.addRedDie();
			if (hostPrefs.hasPref(Constants.EXP_DOUBLE_MONSTER_DIE)) {
				monsterDieRoller.addRedDie();
			}
			monsterDieRoller.rollDice("Monster Roll");
//System.err.println("RealmHostPanel: DEBUGGO");
//monsterDieRoller.setValue(0,3);
			game.setMonsterDie(monsterDieRoller);
			host.broadcast("host","Monster Die roll is "+monsterDieRoller.getDescription(false));

			game.clearRegeneratedDenizens();
			if (RealmCalendar.isSeventhDay(game.getDay())) {
				// Reset Denizens on 7th day
				SetupCardUtility.resetDenizens(host.getGameData(), monsterDieRoller.getValue(0));
				if (monsterDieRoller.getNumberOfDice()>1) {
					SetupCardUtility.resetDenizens(host.getGameData(), monsterDieRoller.getValue(1));
				}
			}

			// Figure out who is following who, and determine which characters actually get to move here
			ArrayList allChars = new ArrayList(getLivingCharacters());
			HashMap followHash = new HashMap(); // to identify follow cycles
			HashMap charHash = new HashMap(); // to identify all characters quickly
			ArrayList charPool = new ArrayList(); // the ultimate list of characters that perform actions
			for (Iterator i = allChars.iterator(); i.hasNext();) {
				CharacterWrapper character = new CharacterWrapper((GameObject) i.next());
				if (!character.isGone() && !character.isJustUnhired()) {
					character.setTodaysMonsterRoll(monsterDieRoller);
					character.clearActionFollowers();
					String followId = character.getFollowStringId();
					if (followId!=null) {
						followHash.put(character.getGameObject().getStringId(), followId);
					}
					charHash.put(character.getGameObject().getStringId(), character);
				}
			}
			if (followHash.size() > 0) {
				ArrayList keys = new ArrayList(charHash.keySet());
				for (Iterator i = keys.iterator(); i.hasNext();) {
					String id = (String) i.next();
					CharacterWrapper character = (CharacterWrapper) charHash.get(id);
					String nextFollowId = id;
					String followId = null;
					boolean cancelFollow = false;
					while ((nextFollowId = (String) followHash.get(nextFollowId)) != null) {
						if (nextFollowId.equals(id)) {
							cancelFollow = true;
							followId = null; // Cancel follow when cycles back to name
							break;
						}
						followId = nextFollowId;
					}
					if (followId != null) {
						// Following someone, so add as an action follower, and leave out of the charPool
						CharacterWrapper followee = (CharacterWrapper) charHash.get(followId);
						followee.addActionFollower(character);
					}
					else if (!cancelFollow) {
						// Not following?  Get a normal turn (assuming not a familiar)
						charPool.add(character);
					}
				}
			}
			else {
				// No following?  Everyone gets a normal turn.
				charPool.addAll(charHash.values());
			}
			
			// Randomize the character order
			ArrayList randPool = new ArrayList();
			while (!charPool.isEmpty()) {
				int r = RandomNumber.getRandom(charPool.size());
				randPool.add(charPool.remove(r));
			}
			charPool = randPool;
			
			// Strip out characters that have an ability to choose which turn to take
			ArrayList prefCharPool = new ArrayList(); // the characters who will get to go before anyone else
			for (Iterator i=charPool.iterator();i.hasNext();) {
				CharacterWrapper character = (CharacterWrapper)i.next();
				if (character.affectedByKey(Constants.CHOOSE_TURN)) {
					prefCharPool.add(character);
				}
			}
			if (prefCharPool.size()>0) {
				charPool.removeAll(prefCharPool);
				if (prefCharPool.size()>1) {
					CharacterWrapper last = (CharacterWrapper)prefCharPool.get(prefCharPool.size()-1);
					last.setLastPreemptivePlayer(true);
				}
			}

			// Add back characters that get to choose their turn
			if (prefCharPool.size()>0) {
				charPool.addAll(0,prefCharPool);
			}

			int order = 2;
			for (Iterator i = charPool.iterator(); i.hasNext();) {
				CharacterWrapper character = (CharacterWrapper) i.next();
				if (character.isMinion()) {
					// Skip the minion - it will be assigned a turn when the owner is assigned a turn
					continue;
				}
				
				ArrayList minions = character.getMinions();
				if (minions!=null) {
					for (Iterator n=minions.iterator();n.hasNext();) {
						GameObject minion = (GameObject)n.next();
						CharacterWrapper minChar = new CharacterWrapper(minion);
						if (minChar.getFollowStringId()==null) {
							if (minChar.canPlay()) {
								// The character has a familiar that is not following anyone, so it goes right
								// before the character.
								minChar.setPlayOrder(order++);
							}
							else {
								minChar.setPlayOrder(0);
								minChar.setDoRecord(false);
								minChar.clearCurrentActions();
							}
						}
					}
				}
				
				int playOrder = order++;
				character.setPlayOrder(playOrder);
//					character.setCombatPlayOrder(playOrder);
				character.setLastPlayer(!i.hasNext());
				// Followers get their "turns" directly AFTER the guide
				boolean first = true;
				for (Iterator n = character.getActionFollowers().iterator(); n.hasNext();) {
					CharacterWrapper actionFollower = (CharacterWrapper) n.next();
					if (!first) {
						actionFollower.setNoSummon(true); // Every follower should not summon monsters!  They are a group.
					}
					first = false;

					ArrayList actionFollowerMinions = actionFollower.getMinions();
					if (actionFollowerMinions!=null) {
						for (Iterator m=actionFollowerMinions.iterator();m.hasNext();) {
							GameObject minion = (GameObject)m.next();
							CharacterWrapper minChar = new CharacterWrapper(minion);
							if (minChar.getFollowStringId()==null) {
								// The following character has a familiar that is not following anyone, so it goes right
								// before the character.
								minChar.setPlayOrder(order++);
							}
						}
					}
					
					playOrder = order++;
					actionFollower.setPlayOrder(playOrder);
//						actionFollower.setCombatPlayOrder(playOrder);
				}
			}

			game.setState(GameWrapper.GAME_STATE_PLAYING);
			updateGameState(); // recurse
		}
	}
	private void updateGameStatePlaying() { // DAYTIME
		logger.fine("DAYTIME");
		Collection activeCharacters = getLivingCharacters();
		int min = Integer.MAX_VALUE;
//			ArrayList postponedChars = new ArrayList(); // I think there can only be one at a time here...
		CharacterWrapper postponedChar = null;
		ArrayList chars = new ArrayList();
		for (Iterator i = activeCharacters.iterator(); i.hasNext();) {
			CharacterWrapper character = new CharacterWrapper((GameObject) i.next());
			if (!character.isJustUnhired()) { // In case a Native HQ is returned to the setup card after a wish result
				int playOrder = character.getPlayOrder();
				if (playOrder > 0) {
					chars.add(character);
					if (playOrder < min) {
						min = playOrder;
					}
				}
				if (playOrder == -1) { // should be the only one who actually postponed, even if multiple chars have the ability
					if (postponedChar!=null) {
						throw new IllegalStateException("There should NEVER be more than ONE postponed character!");
					}
					postponedChar = character;
				}
			}
		}
		// make sure to remove any actionfollowers of postponedChar
		if (postponedChar!=null) {
			chars.removeAll(postponedChar.getActionFollowers());
		}
		
		if (chars.size() > 0) {
			if (min == 2) {
				// Autosave anytime a new player is taking their turn
				autoSaveNow();
				
				// Sort, so the list is in player order
				Collections.sort(chars,new Comparator() {
					public int compare(Object o1,Object o2) {
						CharacterWrapper c1 = (CharacterWrapper)o1;
						CharacterWrapper c2 = (CharacterWrapper)o2;
						return c1.getPlayOrder()-c2.getPlayOrder();
					}
				});
				
				// Check for the condition where ALL remaining chars can CHOOSE_TURN
				boolean allChooseTurn = true;
				for (Iterator i=chars.iterator();i.hasNext();) {
					CharacterWrapper test = (CharacterWrapper)i.next();
					if (!test.getGameObject().hasThisAttribute(Constants.CHOOSE_TURN)) {
						allChooseTurn = false;
						break;
					}
				}
				
				if (postponedChar!=null) {
					// Find appropriate slot, starting AFTER next character:
					//	- NOT in front of followers of the next character
					//	- NOT in front of another character with the ability to preempt a turn
					//  - NOT after any minions (which is before the owner of said minions)
					boolean added = false;
					boolean afterMinion = false;
					for (int i=1;i<chars.size();i++) {
						CharacterWrapper test = (CharacterWrapper)chars.get(i);
						if (test.getFollowStringId()==null) { // not following
							if (!afterMinion && test.getMinionCount()==0) { // not AFTER a minion or BEFORE a minion keeper
								if (!test.getGameObject().hasThisAttribute(Constants.CHOOSE_TURN)) { // not another preempter
									chars.addAll(i,postponedChar.getActionFollowers());
									chars.add(i,postponedChar);
									added = true;
									if (postponedChar.isLastPreemptivePlayer()) {
										// The last player that can choose a turn has postponed his/her turn
										// This means that there will be a neverending cycle unless the
										// "real" player moves to the front.  Do this now:
										chars.remove(test);
										chars.removeAll(test.getActionFollowers());
										chars.addAll(0,test.getActionFollowers());
										chars.add(0,test);
									}
									break;
								}
							}
						}
						afterMinion = test.isMinion();
					}
					if (!added) {
						// Add 'em to the end if not added
						chars.add(postponedChar);
						chars.addAll(postponedChar.getActionFollowers());
					}
				}
				
				// renumber
				int n=1;
				CharacterWrapper last = null;
				for (Iterator i = chars.iterator(); i.hasNext();) {
					CharacterWrapper character = (CharacterWrapper) i.next();
					character.setPlayOrder(n);
					if (n==1) {
						sendEmail("It is the "+character.getGameObject().getName()+"'s turn to play.",character.getPlayerName());
					}
					character.setLastPlayer(allChooseTurn); // Will be false unless ALL the players are CHOOSE_TURN players
					if (character.getFollowStringId()==null) { // only count the non-followers
						last = character;
					}
					n++;
				}
				if (last!=null) {
					last.setLastPlayer(true);
				}
			}
		}
		else {
			// move on to the next game stage here
			for (Iterator i = activeCharacters.iterator(); i.hasNext();) {
				CharacterWrapper character = new CharacterWrapper((GameObject) i.next());
				character.applySunset();
			}
			game.setState(GameWrapper.GAME_STATE_RESOLVING);
			sendEmailAll("The day is over, and the game moves into evening combat.");
			
			// Expire Day Spells
			SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(host.getGameData());
			spellMaster.expireDaySpells();
			
			// Init combat
			if (hostPrefs.getEnableBattles()) {
				// Init battles, so that RealmBattle.nextCombatAction will work
				ClearingUtility.restoreChitState(host.getGameData());
				RealmBattle.resetCombat(host.getGameData());
				RealmBattle.initCombatOrder(host.getGameData());
			}
			
			// Autosave at the end of all turns (before combat)
			autoSaveNow();
			
			updateGameState(); // recurse
		}
	}
	private void updateGameStateResolving() {
		logger.fine("EVENING");
		autoSaveNow();
		if (RealmBattle.newClearingCombat) {
			// Save at the beginning of every separate combat
			RealmBattle.newClearingCombat = false;
			
			sendEmailCombat("Combat has started in your clearing.");
		}
		if (!RealmBattle.nextCombatAction(host,host.getGameData())) {
			// First, see if ANY character is day end trading
			ArrayList<GameObject> activeCharacters = getLivingCharacters();
			for (GameObject go:activeCharacters) {
				CharacterWrapper character = new CharacterWrapper(go);
				if (character.getWantsDayEndTrades()) {
					character.setBlocked(false);
					character.setDayEndTradingActive(true);
				}
			}
			
			// Move on to the next game stage here
			game.setState(GameWrapper.GAME_STATE_DAYEND);
			updateGameState();
		}
	}
	private void updateGameStateMidnight(RealmCalendar cal) {
		logger.fine("MIDNIGHT");
		boolean doneTrading = true;
		ArrayList<GameObject> activeCharacters = getLivingCharacters();
		for (GameObject go:activeCharacters) {
			CharacterWrapper character = new CharacterWrapper(go);
			if (character.isDayEndTradingActive()) {
				doneTrading = false;
			}
		}
			
		if (!doneTrading) return; // Don't continue if there is still day end trading going on
		
		// Unblock all monsters (this might be overkill, but maybe its not a big deal)
		GamePool pool = new GamePool(host.getGameData().getGameObjects());
		for (GameObject go:pool.find(hostPrefs.getGameKeyVals() + ",monster")) {
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent) rc;
				monster.setBlocked(false);
			}
		}
		
		for (GameObject go:pool.find("guild,color_source")) {
			go.removeThisAttribute("color_source");
		}
		
		// Clean up all tile combat results (PEACE clearings, and WasFatigue results)
		for (GameObject go:RealmObjectMaster.getRealmObjectMaster(host.getGameData()).getTileObjects()) {
			CombatWrapper.clearAllCombatInfo(go);
		}

		// Expire Combat spells, and deenergize permanent spells
		SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(host.getGameData());
		spellMaster.expireCombatSpells();
		spellMaster.deenergizePermanentSpells();
		
		// New Day
		int daysToAdd = 1; // default
		if (RealmCalendar.isSeventhDay(game.getDay())) {
			// Apply new weather here (if using weather)
			if (hostPrefs.isUsingSeasons() && hostPrefs.hasPref(Constants.OPT_WEATHER)) {
				int die = RandomNumber.getDieRoll(6);
				int chit = game.getWeatherChit();
				int result = Math.max(die,chit);
				host.broadcast("host","The weather chit was "+chit+", and the rolled die was "+die+".");
				cal.setWeatherResult(result);
				host.broadcast("host", "The weather for the week is "+cal.getWeatherName(game.getMonth()));
				game.updateWeatherChit();
				host.broadcast("host","A new weather chit was chosen.");
			}
			
			// When calculating days in the NEXT week, we may be referring to a new month
			int bumpMonth = game.getDay()==28?1:0;
			
			// Determine how many days in the week (DIW)
			int days = cal.getDays(game.getMonth()+bumpMonth);
			
			// Add a number of days equal to (8-DIW)
			daysToAdd = 8-days;
		}
		
		// Decrement all terms of hire
		for (Iterator i = pool.find(hostPrefs.getGameKeyVals() + "," + RealmComponent.OWNER_TERM_OF_HIRE).iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			rc.decrementTermOfHire(daysToAdd);
			if (rc.getTermOfHire() == 0) {
				RealmComponent owner = rc.getOwner();
				if (owner.isCharacter()) {
					CharacterWrapper character = new CharacterWrapper(owner.getGameObject());
					character.removeHireling(go);
					host.broadcast("host",character.getGameObject().getName()+" loses "+rc.getGameObject().getName()+" as a hireling (term is up).");
				}
				else {
					throw new IllegalStateException("For some reason, " + go.getName() + " is owned by " + owner.getGameObject().getName() + ", which is not a character!!!");
				}
			}
		}

		// Finally, add day(s)
		int month = game.getMonth();
		game.addDay(daysToAdd);
		if (month!=game.getMonth()) {
			game.bumpMapRepaint();
		}
		
		game.clearClientTakenTurn();
		game.setState(GameWrapper.GAME_STATE_RECORDING);
		updateGameState(); // recurse
		host.broadcast("host","========================================");
		host.broadcast("host","Month "+game.getMonth()+", Day "+game.getDay());
		host.broadcast("host","========================================");
		
		if (hostPrefs.hasPref(Constants.OPT_AUTOMATIC_ENCHANTING) && RealmCalendar.isSeventhDay(game.getDay())) {
			RealmUtility.automaticallyEnchantTiles(host.getGameData(),game);
		}
		
		host.broadcast(Constants.BROADCAST_ATTENTION,"");
		sendEmailAll("New day started:  "+"Month "+game.getMonth()+", Day "+game.getDay());
		
		// Re-energize permanent spells (FIXME this doesn't seem to be sufficient for Flying Carpet??)
		spellMaster.energizePermanentSpells(null,game);
		
		checkForGameEnd(activeCharacters);
	}
	private void checkForGameEnd(ArrayList<GameObject> livingCharacters) {
		if (hostPrefs.hasPref(Constants.QST_BOOK_OF_QUESTS)) {
			for (GameObject go:livingCharacters) {
				CharacterWrapper character = new CharacterWrapper(go);
				if (character.isCharacter() && character.isAllQuestsComplete()) {
					setGameOver();
					break;
				}
			}
		}
		else if (hostPrefs.hasPref(Constants.EXP_SUDDEN_DEATH)) {
			for (GameObject go:livingCharacters) {
				CharacterWrapper character = new CharacterWrapper(go);
				if (character.isCharacter()) {
					if (character.getTotalScore()>=0 && !character.needsToSetVps()) {
						setGameOver();
						break;
					}
				}
			}
		}
		else if (!hostPrefs.getRequiredVPsOff() && game.getMonth()>hostPrefs.getNumberMonthsToPlay()) {
			setGameOver();
		}
	}
	private void setGameOver() {
		game.setState(GameWrapper.GAME_STATE_GAMEOVER);
		ArrayList<GameObject> livingCharacters = getLivingCharacters();
		for(GameObject go:livingCharacters) {
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isCharacter()) {
				CharacterWrapper character = new CharacterWrapper(go);
				character.setGameOver(true);
			}
		}
		SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(host.getGameData());
		spellMaster.expireCombatSpells();
		spellMaster.deenergizePermanentSpells();
	}
	private void sendEmailAll(String message) {
		sendEmail(message,null);
	}
	private void sendEmail(String message,String charName) {
		// Should only send e-mail to those that are offline!
		ArrayList<String> offlineEmails = new ArrayList<String>();
		for (GameObject go:getLivingCharacters()) {
			CharacterWrapper character = new CharacterWrapper(go);
			if (charName==null || charName.equals(character.getPlayerName())) { // specific character or all
				if (character.isMissingInAction()) { // offline
					String email = SendMail.normalizeEmail(character.getPlayerEmail());
					if (email.length()>0 && !offlineEmails.contains(email)) { // unique
						offlineEmails.add(email);
					}
				}
			}
		}
		String subtitle = charName==null?"Game Update":("Attention: "+charName);
		sendEmail(subtitle,message,offlineEmails);
	}
	private void sendEmailCombat(String message) {
		ArrayList<String> offlineEmails = new ArrayList<String>();
		TileLocation tl = RealmBattle.getCurrentCombatLocation(host.getGameData());
		BattleModel model = RealmBattle.buildBattleModel(tl,host.getGameData());
		for (RealmComponent rc:model.getAllOwningCharacters()) {
			CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
			if (character.isMissingInAction()) { // offline
				String email = SendMail.normalizeEmail(character.getPlayerEmail());
				if (email.length()>0 && !offlineEmails.contains(email)) { // unique
					offlineEmails.add(email);
				}
			}
		}
		sendEmail("Battle Update",message,offlineEmails);
	}
	private void sendEmail(String subtitle,String message,ArrayList<String> emails) {
		if (hostPrefs.isEmailNotifications()) {
			if (emails.size()>0) {
				String error = RealmMail.sendMail(hostPrefs,emails,subtitle,message);
				if (error!=null) {
					host.broadcast("EMAIL ERROR",error);
				}
			}
		}
	}
	
	/**
	 * @return 		a new game if none is found
	 */
	private GameWrapper findGame() {
		GamePool pool = new GamePool(host.getGameData().getGameObjects());
		Collection mrGameObjects = pool.extract(GameWrapper.getKeyVals());
		if (mrGameObjects.size() == 1) {
			GameObject go = (GameObject) mrGameObjects.iterator().next();
			return new GameWrapper(go);
		}
		GameObject go = host.getGameData().createNewObject();
		go.setName(host.getGameTitle());

		game = new GameWrapper(go);
		game.setInitialValues();

		return new GameWrapper(go);
	}

	public void setup() {
		game = findGame();

		hostPrefs = HostPrefWrapper.findHostPrefs(host.getGameData());

		updateServerData();

		if (hostPrefs.getAutosaveEnabled()) {
			host.getGameData().zipToFile(AUTOSAVEFILE);
//JOptionPane.showMessageDialog(null,"Auto save during setup");
		}
	}

	private void updateServerData() {
		logger.fine("updateServerData: "+host.getGameData().getDataId()+":"+host.getGameData().getChangeCount());
		if (host.applyChanges(null, host.getGameData().getObjectChanges())) {
//			host.getGameData().commit(); // doing a commit here does a repeat of applyChanges, and that's bad
			host.getGameData().rollback(); // instead, roll these back so they don't get "reapplied"
			logger.fine("Changes after update: "+host.getGameData().getChangeCount()+" <-- better be zero!!");
		}
		else {
			throw new IllegalStateException("Ack!");
		}
	}

	/**
	 * @return		All active (non-dead) characters
	 */
	private ArrayList<GameObject> getLivingCharacters() {
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(host.getGameData()).getPlayerCharacterObjects());
		ArrayList list = pool.find(CharacterWrapper.NAME_KEY);
		ArrayList<GameObject> active = new ArrayList<GameObject>();
		for (Iterator i = list.iterator(); i.hasNext();) {
			CharacterWrapper character = new CharacterWrapper((GameObject) i.next());
			if (!character.isDead()) {
				active.add(character.getGameObject());
				ArrayList<GameObject> minions = character.getMinions();
				if (minions!=null) {
					active.addAll(minions);
				}
			}
		}
		return active;
	}
	public ArrayList<String> getPlayerNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (GameServer server:connections) {
			names.add(server.getClientName());
		}
		return names;
	}

	/**
	 * A table model to show all the active connections
	 */
	private class ConnectionTableModel extends AbstractTableModel {

		protected String[] colNames = {"Player name", "IP Address","email", "Count" };

		public ConnectionTableModel() {
		}

		public int getRowCount() {
			return connections.size();
		}

		public int getColumnCount() {
			return colNames.length;
		}

		public String getColumnName(int col) {
			return colNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (connections != null && row < connections.size()) {
				GameServer server = connections.get(row);
				if (server != null && host != null) {
					switch (col) {
						case 0:
							return server.getClientName();
						case 1:
							return server.getClientIP();
						case 2:
							String clientName = server.getClientName();
							String email = clientName == null ? null : playerEmails.get(null);
							return email == null ? "NA" : email;
						case 3:
							Collection c = RealmObjectMaster.getRealmObjectMaster(host.getGameData()).getPlayerCharacterObjects();
							return new Integer((new GamePool(c)).find(CharacterWrapper.NAME_KEY + "=" + server.getClientName()).size());
					}
				}
			}
			return null;
		}
	}
}