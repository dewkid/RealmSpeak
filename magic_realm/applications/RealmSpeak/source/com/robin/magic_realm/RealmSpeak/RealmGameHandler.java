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
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.robin.game.objects.*;
import com.robin.game.server.GameClient;
import com.robin.game.server.GameHost;
import com.robin.general.swing.*;
import com.robin.general.util.*;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.RealmBattle.RealmBattle;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.table.Loot;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

/**
 * This is the master of the client
 */
public class RealmGameHandler extends RealmSpeakInternalFrame {

	private static Logger logger = Logger.getLogger(RealmGameHandler.class.getName());

	protected int characterJoinOrder = 0;
	protected boolean updatingList = false;
	protected JTable characterTable;
	protected CharacterTableModel characterTableModel;
	protected ArrayList characterList;
	protected Hashtable characterFrames;
	protected ArrayList characterFrameOrder;

	protected JLabel connectionStatus;
	protected GameClient client;
	protected boolean local;

	protected JButton addCharacterButton;
	protected JButton suicideCharacterButton;
	protected JButton transferCharacterButton;
	protected JButton unlockCharacterButton;

	protected JButton showLogButton;

	protected JButton startMapBuildingButton;
	protected JButton startGameButton;
	protected JButton extendGameButton;
	protected JButton endGameButton;
	protected JButton revealAllButton;

	protected JButton showSetupCardButton;
	protected JButton showCalendarButton;

	protected JButton editOptionsButton;
	protected JToggleButton showDeadOption;

	protected RealmSpeakFrame parent;
	protected RealmInspectorFrame inspector;
	protected RealmLogWindow log;
	protected RealmTilePickFrame tilePickFrame;

	// host prefs
	protected HostPrefWrapper hostPrefs;
	protected boolean hostPlayer; // if true, then this game handler is the
									// hosts client
	protected boolean localGame;

	// 
	protected GameWrapper game;
	protected TreasureSetupCardView[] treasureSetupCardView;

	protected ArrayList<String[]> relationshipNames = null;

	protected CharacterTradeFrame characterTradeFrame = null;

	protected int iconSize = ActionIcon.ACTION_ICON_NORMAL;

	protected int lastMonth = -99;
	protected String lastWeather = null;
	private String clientPlayerPass;
	private String clientEmail;
	private ArrayList<String> playerWarned = new ArrayList<String>();

	// Update listener
	protected ChangeListener updateFrameListener = new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
			updateCharacterFrames();
			if (tilePickFrame != null) {
				tilePickFrame.updateFrame();
			}
			getInspector().redrawMap();
		}
	};

	public RealmGameHandler(RealmSpeakFrame parent, GameHost host, String name, String pass, String ppass, String email) {
		super("Character List", true, false, true, true);
		this.parent = parent;
		this.hostPlayer = true;
		this.localGame = true;
		this.clientPlayerPass = ppass;
		this.clientEmail = email;
		inspector = null;
		initComponents();
		setup(host, null, -1, name, pass);
	}

	public RealmGameHandler(RealmSpeakFrame parent, String ip, int port, String name, String pass, String ppass, String email, boolean hostPlayer) {
		super("Character List", true, false, true, true);
		this.parent = parent;
		this.hostPlayer = hostPlayer;
		this.localGame = false;
		this.clientPlayerPass = ppass;
		this.clientEmail = email;
		inspector = null;
		initComponents();
		setup(null, ip, port, name, pass);
	}

	public void removeAllCharacterFrames() {
		for (Iterator i = characterFrames.values().iterator(); i.hasNext();) {
			CharacterFrame frame = (CharacterFrame) i.next();
			parent.removeFrameFromDesktop(frame);
		}
		characterFrames.clear();
	}

	public void initComponents() {
		log = RealmLogWindow.getSingleton();

		setSize(500, 300);
		setLocation(200, 100);
		setContentPane(new JPanel(new BorderLayout()));
		characterList = new ArrayList();
		characterFrames = new Hashtable();
		characterFrameOrder = new ArrayList();
		characterTableModel = new CharacterTableModel();
		characterTable = new JTable(characterTableModel);
		characterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		characterTable.getColumnModel().getColumn(0).setMaxWidth(40);
		characterTable.getColumnModel().getColumn(1).setMaxWidth(40);
		characterTable.getColumnModel().getColumn(2).setMaxWidth(100);
		CharacterTableCellRenderer renderer = new CharacterTableCellRenderer();
		characterTable.setDefaultRenderer(ImageIcon.class, renderer);
		characterTable.setRowHeight(25);
		characterTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				CharacterWrapper character = getSelectedCharacter();
				if (character != null && !character.isDead()) {
					String id = character.getGameObject().getStringId();
					CharacterFrame frame = (CharacterFrame) characterFrames.get(id);
					if (frame != null) {
						frame.toFront();
						frame.centerOnToken();
						characterFrameOrder.remove(id);
						characterFrameOrder.add(0, id);
					}
				}
				updateControls();
			}
		});
		characterTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					CharacterWrapper character = getSelectedCharacter();
					if (character != null && character.isCharacter()) {
						CharacterSpyPanel panel = new CharacterSpyPanel(RealmGameHandler.this, character);
						FrameManager.showDefaultManagedFrame(getMainFrame(), panel, character.getGameObject().getName() + " Spy", null, false);
					}
				}
			}
		});
		getContentPane().add(new JScrollPane(characterTable), "Center");
		Box box = Box.createHorizontalBox();
		connectionStatus = new JLabel("Connecting...");
		box.add(connectionStatus);
		box.add(Box.createHorizontalGlue());

		startMapBuildingButton = createButton("images/interface/build.gif", "Build Map!");
		startMapBuildingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				startMapBuilding();
				startMapBuildingButton.setEnabled(false);
				startGameButton.setEnabled(true);
				updateCharacterList();
				updateControls();
			}
		});
		box.add(startMapBuildingButton);

		startGameButton = createButton("images/interface/start.gif", "Start Game!");
		startGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int ret;
				if (!characterList.isEmpty()) {
					ret = JOptionPane.showConfirmDialog(getMainFrame(), "Allow characters to place visitor/mission chits?", "Visitor/Mission", JOptionPane.YES_NO_OPTION);
				}
				else {
					// If there are no characters yet, then there is no point
					// asking
					ret = JOptionPane.NO_OPTION;
				}
				broadcast("host", "Host has started the game.");

				if (ret == JOptionPane.YES_OPTION) {
					game.setPlaceGoldSpecials(true);
					startGoldSpecialPlacement();
				}
				else if (ret == JOptionPane.NO_OPTION) {
					randomGoldSpecialPlacement();
				}
			}
		});
		box.add(startGameButton);

		extendGameButton = createButton("images/interface/extend.gif", "Extend Game 1 Month");
		extendGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doExtendGame();
			}
		});
		box.add(extendGameButton);

		endGameButton = createButton("images/interface/end.gif", "End Game");
		endGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int ret = JOptionPane.showConfirmDialog(getMainFrame(), "You are about to end the game early.  This is irreversible.\n\nAre you sure you want to do this?", "End Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (ret == JOptionPane.YES_OPTION) {
					finalizeGame();
				}
			}
		});
		box.add(endGameButton);

		revealAllButton = createButton("images/interface/reveal.gif", "Reveal All");
		revealAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int ret = JOptionPane.showConfirmDialog(getMainFrame(), "You are about to reveal all the cards/chits.  Once this has\n" + "been done, the game can no longer be extended.  Are you sure\n" + "you want to do this?", "Reveal All", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (ret == JOptionPane.YES_OPTION) {
					doRevealAll();
				}
			}
		});
		box.add(revealAllButton);

		box.add(Box.createHorizontalStrut(10));

		showSetupCardButton = createButton("images/interface/setup.gif", "Setup Card");
		showSetupCardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				TreasureSetupCardView view = getTreasureSetupCardView();
				if (view != null) {
					TreasureSetupCardView.displayView(getMainFrame(), view);
				}
			}
		});
		box.add(showSetupCardButton);

		showCalendarButton = createButton("images/interface/calendar.gif", "Calendar");
		showCalendarButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				RealmCalendarViewer view = new RealmCalendarViewer(client.getGameData());
				view.setLocationRelativeTo(getMainFrame());
				FrameManager.getFrameManager().addFrame(view);
			}
		});
		box.add(showCalendarButton);

		box.add(Box.createHorizontalStrut(10));

		editOptionsButton = createButton("images/interface/options.gif", "Game Options...");
		editOptionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				HostGameSetupDialog setup = new HostGameSetupDialog(new JFrame(), "Game Options for Battle", client.getGameData());
				setup.loadPrefsFromData();
				setup.setVisible(true);
				submitChanges();
			}
		});
		box.add(editOptionsButton);

		box.add(Box.createHorizontalStrut(10));

		startMapBuildingButton.setEnabled(false);
		startGameButton.setEnabled(false);
		extendGameButton.setEnabled(false);
		endGameButton.setEnabled(false);
		revealAllButton.setEnabled(false);

		showDeadOption = new JToggleButton(IconFactory.findIcon("images/interface/dead.gif"), false);
		showDeadOption.setToolTipText("Show DEAD");
		showDeadOption.setFocusable(false);
		ComponentTools.lockComponentSize(showDeadOption, 40, 30);
		showDeadOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				characterTableModel.rebuild();
			}
		});
		box.add(showDeadOption);
		showDeadOption.setEnabled(false); // to start
		getContentPane().add(box, "South");

		box = Box.createVerticalBox();
		addCharacterButton = new JButton(IconFactory.findIcon("icons/plus.gif"));
		addCharacterButton.setToolTipText("New Character...");
		addCharacterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				createNewCharacter();
			}
		});
		addCharacterButton.setEnabled(false);
		box.add(addCharacterButton);
		suicideCharacterButton = new JButton(IconFactory.findIcon("icons/minus.gif"));
		suicideCharacterButton.setToolTipText("Suicide Character");
		suicideCharacterButton.setEnabled(false);
		suicideCharacterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				CharacterWrapper character = getSelectedCharacter();
				if (character != null && character.isActive()) {
					int ret = JOptionPane.showConfirmDialog(getMainFrame(), "This will suicide the " + character.getGameObject().getName() + ".  This is NOT reversible.  Are you sure?", "Kill Selected Character", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (ret == JOptionPane.YES_OPTION) {
						character.makeDead("Suicide");
						CombatWrapper.clearAllCombatInfo(character.getGameObject());
						submitChanges();
						updateCharacterList();
						getInspector().redrawMap();
					}
				}
			}
		});
		box.add(suicideCharacterButton);
		transferCharacterButton = new JButton(IconFactory.findIcon("icons/s_arrow6.gif"));
		transferCharacterButton.setToolTipText("Transfer Character");
		transferCharacterButton.setEnabled(false);
		transferCharacterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doCharacterTransfer();
			}
		});
		box.add(transferCharacterButton);
		unlockCharacterButton = new JButton(IconFactory.findIcon("icons/unlock.gif"));
		unlockCharacterButton.setToolTipText("Reset Player Password");
		unlockCharacterButton.setEnabled(false);
		unlockCharacterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				CharacterWrapper character = getSelectedCharacter();
				character.setPlayerPassword("");
				submitChanges();
				updateCharacterList();
			}
		});
		box.add(unlockCharacterButton);
		box.add(Box.createVerticalGlue());
		showLogButton = new JButton(IconFactory.findIcon("icons/document.gif"));
		showLogButton.setToolTipText("Toggle Game Log");
		showLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				toggleLog();
			}
		});
		box.add(showLogButton);
		getContentPane().add(box, "West");
	}

	private void doCharacterTransfer() {
		CharacterWrapper character = getSelectedCharacter();
		RealmComponent rc = RealmComponent.getRealmComponent(character.getGameObject());
		if (character != null && character.isActive()) {
			ArrayList<String> playerNames = new ArrayList<String>();
			playerNames.add(client.getClientName());
			for (String name : getMainFrame().getAllServerNames()) {
				if (!playerNames.contains(name)) {
					playerNames.add(name);
				}
			}
			GamePool pool = new GamePool(client.getGameData().getGameObjects());
			for (GameObject go : pool.find("character," + CharacterWrapper.NAME_KEY)) {
				CharacterWrapper guy = new CharacterWrapper(go);
				if (!playerNames.contains(guy.getPlayerName())) {
					playerNames.add(guy.getPlayerName());
				}
			}

			ButtonOptionDialog dialog = new ButtonOptionDialog(getMainFrame(), rc.getIcon(), "Transfer to which player?", "Transfer Character");
			dialog.addSelectionObjects(playerNames);
			dialog.addSelectionObject(new StringBuilder("Other Player"));
			dialog.setVisible(true);

			String newPlayerName = null;
			Object obj = dialog.getSelectedObject();
			if (obj != null) {
				if (obj instanceof StringBuilder) {
					newPlayerName = JOptionPane.showInputDialog(getMainFrame(), "Transfer to which player? (case-sensitive)");
				}
				else {
					newPlayerName = obj.toString();
				}
			}

			if (newPlayerName != null) {
				doCharacterTransfer(character, newPlayerName);

				String id = character.getGameObject().getStringId();
				for (GameObject go : pool.find("owner_id=" + id)) {
					if (go.getStringId().equals(id))
						continue;
					if (go.hasAttributeBlock(CharacterWrapper.PLAYER_BLOCK)) {
						CharacterWrapper hireling = new CharacterWrapper(go);
						doCharacterTransfer(hireling, newPlayerName);
					}
				}

				submitChanges();
				updateCharacterList();
			}
		}
	}

	private void doCharacterTransfer(CharacterWrapper character, String newPlayerName) {
		character.setPlayerName(newPlayerName);
		character.setPlayerPassword(""); // so that it get's updated by the
											// player's machine
		character.setPlayerEmail("");

		// Remove frame
		CharacterFrame frame = (CharacterFrame) characterFrames.get(character.getGameObject().getStringId());
		if (frame != null) {
			characterTable.clearSelection();
			String id = character.getGameObject().getStringId();
			characterFrames.remove(id);
			characterFrameOrder.remove(id);
			parent.removeFrameFromDesktop(frame);
		}
	}

	private JButton createButton(String iconPath, String tipText) {
		JButton button = new JButton(IconFactory.findIcon(iconPath));
		button.setToolTipText(tipText);
		button.setFocusable(false);
		ComponentTools.lockComponentSize(button, 40, 30);
		return button;
	}

	public void toggleLog() {
		if (log.isVisible()) {
			log.setVisible(false);
		}
		else {
			log.setVisible(true);
			log.toFront();
			log.scrollToEnd();
		}
	}

	private void finalizeGame() {
		game.setDay(1);
		game.setMonth(hostPrefs.getNumberMonthsToPlay() + 1);
		game.setGameEnded(true);

		// Expire ALL spells
		SpellMasterWrapper smw = SpellMasterWrapper.getSpellMaster(getClient().getGameData());
		smw.expireAllSpells();

		// Penalize any mission/campaign chit carriers
		GamePool pool = new GamePool(getClient().getGameData().getGameObjects());
		ArrayList<GameObject> gs = pool.find(RealmComponent.GOLD_SPECIAL);
		for (GameObject go : gs) {
			GoldSpecialChitComponent chit = (GoldSpecialChitComponent) RealmComponent.getRealmComponent(go);
			if (chit.stillInPlay()) {
				// Set it to one day, so that the next midnight will cause it to
				// expire
				chit.getGameObject().setThisAttribute("daysLeft", 1);
			}
		}

		endGameButton.setEnabled(false);
		revealAllButton.setEnabled(true);
		submitChanges();
		updateControls();
		updateCharacterFrames();
		broadcastMapReplot();
	}

	private void doRevealAll() {
		game.setGameRevealed(true);
		revealAllButton.setEnabled(false);
		// Write code to flip every card and state chit face up
		RealmUtility.revealAll(client.getGameData(), hostPrefs);
		submitChanges();
		updateControls();

		broadcastMapReplot();
	}

	private void doExtendGame() {
		int ret = JOptionPane.showConfirmDialog(getMainFrame(), "This will affect all characters.  Are you sure?", "Extend Game 1 Month", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (ret == JOptionPane.YES_OPTION) {
			GameData data = client.getGameData();

			RealmCalendar cal = RealmCalendar.getCalendar(getClient().getGameData());
			int vps = cal.getVictoryPoints(game.getMonth());

			// Update prefs
			HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
			hostPrefs.setNumberMonthsToPlay(hostPrefs.getNumberMonthsToPlay() + 1);

			// Return game to recording state
			game.setState(GameWrapper.GAME_STATE_RECORDING);

			// Return all characters to playing
			for (Iterator i = data.getGameObjects().iterator(); i.hasNext();) {
				GameObject go = (GameObject) i.next();
				if (go.hasAttributeBlock(CharacterWrapper.PLAYER_BLOCK)) { // was
																			// in
																			// the
																			// game
																			// at
																			// some
																			// point
					CharacterWrapper character = new CharacterWrapper(go);
					if (character.isActive()) {
						character.setGameOver(false);
						if (!hostPrefs.getRequiredVPsOff() && character.isCharacter()) {
							if (hostPrefs.hasPref(Constants.HOUSE2_ANY_VPS)) {
								character.setNewVPRequirement(-1);
							}
							else {
								character.setNewVPRequirement(vps - (Constants.MAX_LEVEL - character.getCharacterLevel()));
							}
						}
					}
				}
			}

			// Reset the state of all the game control buttons
			endGameButton.setEnabled(true);
			revealAllButton.setEnabled(false);
			updateControls();
			broadcastAttention();

			// Send info
			submitChanges();
		}
	}

	private void startGoldSpecialPlacement() {
		ArrayList chars = new ArrayList(characterList);
		if (chars.isEmpty()) {
			randomGoldSpecialPlacement();
		}
		else {
			incrementCharacterToPlace();
			inspector.repaint();
		}
	}

	private void randomGoldSpecialPlacement() {
		broadcast("host", "Visitor/Mission/Campaign chits are placed at random.");
		// Place the 6 gold specials randomly
		GameData data = client.getGameData();
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(data);
		ArrayList query = new ArrayList();
		query.add("!" + Constants.GOLD_SPECIAL_PLACED);
		if (hostPrefs.hasPref(Constants.HOUSE2_IGNORE_CAMPAIGNS)) {
			query.add("!campaign");
		}
		ArrayList<GameObject> gs = new ArrayList<GameObject>(rom.findObjects("gold_special", query, false));
		ArrayList<GameObject> gt = new ArrayList<GameObject>(rom.findObjects("gold_special_target", query, false));
		GameObject[] chit = new GameObject[2];
		while (SetupCardUtility.stillChitsToPlace(hostPrefs)) {
			int r = RandomNumber.getRandom(gs.size());
			int s = hostPrefs.hasPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE) ? 0 : RandomNumber.getRandom(2);
			chit[0] = gs.remove(r);
			chit[1] = chit[0].getGameObjectFromThisAttribute("pairid");
			if (!hostPrefs.hasPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE)) {
				gs.remove(chit[1]);
			}

			int t = RandomNumber.getRandom(gt.size());
			GameObject target = gt.remove(t);
			target.add(chit[s]);
			chit[s].setThisAttribute(Constants.GOLD_SPECIAL_PLACED);
			if (!hostPrefs.hasPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE)) {
				chit[1 - s].setThisAttribute(Constants.GOLD_SPECIAL_PLACED);
			}
			target.setThisAttribute(Constants.GOLD_SPECIAL_PLACED);
			// System.out.println(chit[s].getName()+" is added to "+target.getName());
		}

		// Need to also guarantee that no characters have the place vistor
		// button
		ArrayList chars = new ArrayList(characterList);
		if (!chars.isEmpty()) {
			for (Iterator i = chars.iterator(); i.hasNext();) {
				CharacterWrapper c = (CharacterWrapper) i.next();
				if (c.isCharacter()) {
					c.setNeedsChooseGoldSpecial(false);
				}
			}
		}

		startGame();
	}

	public void incrementCharacterToPlace() {
		ArrayList chars = new ArrayList(characterList);
		if (!chars.isEmpty()) {
			// Sort by join order
			Collections.sort(chars, new Comparator() {
				public int compare(Object o1, Object o2) {
					CharacterWrapper c1 = (CharacterWrapper) o1;
					CharacterWrapper c2 = (CharacterWrapper) o2;
					int ret = c1.getCharacterJoinOrder() - c2.getCharacterJoinOrder();
					return ret;
				}
			});
			boolean incremented = false;
			boolean selectNext = false;
			for (Iterator i = chars.iterator(); i.hasNext();) {
				CharacterWrapper c = (CharacterWrapper) i.next();
				if (c.isCharacter()) {
					if (selectNext) {
						incremented = true;
						c.setNeedsChooseGoldSpecial(true);
						break;
					}
					else if (c.getNeedsChooseGoldSpecial()) {
						selectNext = true;
						c.setNeedsChooseGoldSpecial(false);
					}
				}
				else {
					c.setNeedsChooseGoldSpecial(false);
				}
			}
			if (!incremented) {
				incremented = true;
				CharacterWrapper first = (CharacterWrapper) chars.iterator().next();
				first.setNeedsChooseGoldSpecial(true);
			}
			submitChanges();
			updateCharacterFrames();
		}
	}

	public void startMapBuilding() {
		// Find all players
		ArrayList playerNames = parent.realmHostFrame.getPlayerNames();

		if (playerNames.size() > 0) { // FIXME for now
			// Randomize playerNames here?

			// If more than one player, then assign tiles randomly to each
			// player
			// Find player with borderland, and activate (how?) for placing
			// tiles
			ArrayList tileObjects = new ArrayList(RealmObjectMaster.getRealmObjectMaster(client.getGameData()).getTileObjects());
			int nameIndex = 0;
			String playerName = (String) playerNames.get(nameIndex);
			while (!tileObjects.isEmpty()) {
				int r = RandomNumber.getRandom(tileObjects.size());
				GameObject tile = (GameObject) tileObjects.remove(r);

				// Just in case this is a "remake"
				tile.removeAttribute("mapGrid", "mapPosition");
				tile.removeAttribute("mapGrid", "mapRotation");

				if (tile.getName().equals("Borderland") && !tile.hasThisAttribute(Constants.BOARD_NUMBER)) {
					game.setGameMapBuilder(playerName);
				}
				tile.setThisAttribute(Constants.PLAYER_TO_PLACE, playerName);
				nameIndex = (nameIndex + 1) % playerNames.size();
				playerName = (String) playerNames.get(nameIndex);
				tile.setThisAttribute(Constants.PLAYER_TO_PLACE_NEXT, playerName);
			}
		}
		else {
			// If a single player, assign no tiles, and activate.
			// An active player with no tiles, but tiles left to place, will get
			// a random tile.
			game.setGameMapBuilder((String) playerNames.iterator().next());
		}

		submitChanges();
	}

	public void startGame() {
		broadcastAttention();
		game.setPlaceGoldSpecials(false);
		game.setGameStarted(true);
		submitChanges();
		inspector.getMap().setShowEmbellishments(true);
		inspector.redrawMap();
		startGameButton.setEnabled(false);
		endGameButton.setEnabled(true);
		updateCharacterList();
		updateControls();
	}

	public void showHostPrefs() {
		HostGameSetupDialog dialog = new HostGameSetupDialog(getMainFrame(), "Current Game Options", client.getGameData(), false);
		dialog.loadPrefsFromData();
		dialog.setVisible(true);
	}

	private CharacterWrapper getSelectedCharacter() {
		int selRow = characterTable.getSelectedRow();
		CharacterWrapper character = null;
		if (selRow >= 0 && selRow < characterList.size()) {
			character = characterTableModel.getCharacter(selRow);
		}
		return character;
	}

	public void updateControls() {
		boolean charPoolLocked = game.getCharacterPoolLock();
		boolean canMakeChanges = game.getState() == GameWrapper.GAME_STATE_RECORDING && !charPoolLocked && inspector.getMap().isMapReady();

		setTitle("Character List");

		boolean canModifyCharacter = false;
		CharacterWrapper character = getSelectedCharacter();
		if (character != null && character.isActive() && character.getPlayerName() != null && character.isCharacter()) {
			// only allow suicide/transfer if this player is the host, or the
			// character belongs to this player
			boolean nameMatch = character.getPlayerName().equals(client.getClientName());
			if (isHostPlayer() || nameMatch) {
				canModifyCharacter = true;
			}
		}

		editOptionsButton.setEnabled(isHostPlayer()); // ONLY the host can
														// change options
														// in-game

		suicideCharacterButton.setEnabled(canMakeChanges && canModifyCharacter);
		transferCharacterButton.setEnabled(!charPoolLocked && inspector.getMap().isMapReady() && canModifyCharacter && !localGame);
		unlockCharacterButton.setEnabled(!charPoolLocked && inspector.getMap().isMapReady() && canModifyCharacter && !localGame && character.hasPlayerPassword());
		addCharacterButton.setEnabled(canMakeChanges);

		boolean mapBuilder = hostPrefs.getBoardPlayerSetup() && !inspector.getMap().isMapReady();
		startGameButton.setEnabled(isHostPlayer() && !mapBuilder && !game.getGameStarted() && !charPoolLocked && game.getGameMapBuilder() == null);
		boolean canExtend = isHostPlayer() && !game.getGameEnded() && game.getState() == GameWrapper.GAME_STATE_GAMEOVER && !game.hasBeenRevealed();
		endGameButton.setEnabled(isHostPlayer() && game.getGameStarted() && (canMakeChanges || canExtend));
		extendGameButton.setEnabled(canExtend && !hostPrefs.hasPref(Constants.EXP_SUDDEN_DEATH)); // No
																									// game
																									// extensions
																									// for
																									// sudden
																									// death!!

		showDeadOption.setEnabled(hostPrefs != null && hostPrefs.hasPref(Constants.HOUSE1_DONT_RECYCLE_CHARACTERS));
	}

	public CharacterTradeFrame getCharacterTradeFrame() {
		return characterTradeFrame;
	}

	public CharacterTradeFrame createCharacterTradeFrame(CharacterWrapper active, CharacterWrapper include) {
		if (characterTradeFrame == null) {
			CharacterFrame activeCharFrame = (CharacterFrame) characterFrames.get(active.getGameObject().getStringId());
			CharacterInventoryPanel activeInv = null;
			if (activeCharFrame != null) {
				activeInv = activeCharFrame.inventoryPanel;
			}
			CharacterFrame includeCharFrame = (CharacterFrame) characterFrames.get(include.getGameObject().getStringId());
			CharacterInventoryPanel includeInv = null;
			if (includeCharFrame != null) {
				includeInv = includeCharFrame.inventoryPanel;
			}
			characterTradeFrame = new CharacterTradeFrame(this, active, activeInv, include, includeInv);
			characterTradeFrame.setVisible(true);
			// This invokeLater will guarantee that the trade frame pops to the
			// top
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					characterTradeFrame.toFront();
				}
			});
		}
		return characterTradeFrame;
	}

	public void killCharacterTradeFrame() {
		if (characterTradeFrame != null) {
			characterTradeFrame.updateInv();
			characterTradeFrame.setVisible(false);
			characterTradeFrame.dispose();

			characterTradeFrame = null;
		}
	}

	private void handleDirectInfo(RealmDirectInfoHolder info) {
		String command = info.getCommand();
		if (RealmDirectInfoHolder.HOST_DETAIL_LOG.equals(command)) {
			RealmLogWindow.getSingleton().clearLog();
			ArrayList<String> list = info.getStrings();
			while (list.size() >= 2) {
				String key = list.remove(0);
				String val = list.remove(0);
				RealmLogWindow.getSingleton().addMessage(key, val);
			}
		}
		else if (RealmDirectInfoHolder.TRADE_INIT.equals(command)) {
			if (characterTradeFrame == null) {
				createCharacterTradeFrame(info.getActiveCharacter(), info.getIncludeCharacter());
			}
			else {
				info.setCommand(RealmDirectInfoHolder.TRADE_BUSY);
				getClient().sendInfoDirect(info.getActiveCharacter().getPlayerName(), info.getInfo());
			}
		}
		else if (RealmDirectInfoHolder.TRADE_BUSY.equals(command)) {
			if (characterTradeFrame != null) {
				killCharacterTradeFrame();
				JOptionPane.showMessageDialog(getMainFrame(), info.getActiveCharacter().getGameObject().getName() + " is busy, and cannot trade right now.", "Trade Cancelled", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (RealmDirectInfoHolder.TRADE_ACCEPT.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.setAccept(true);
			}
		}
		else if (RealmDirectInfoHolder.TRADE_UNACCEPT.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.setAccept(false);
			}
		}
		else if (RealmDirectInfoHolder.TRADE_ADD_OBJECTS.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.addInventory(info.getGameObjects());
			}
		}
		else if (RealmDirectInfoHolder.TRADE_REMOVE_OBJECTS.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.removeInventory(info.getGameObjects());
			}
		}
		else if (RealmDirectInfoHolder.TRADE_ADD_DISC.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.addDiscoveries(info.getStrings());
			}
		}
		else if (RealmDirectInfoHolder.TRADE_REMOVE_DISC.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.removeDiscoveries(info.getStrings());
			}
		}
		else if (RealmDirectInfoHolder.TRADE_GOLD.equals(command)) {
			if (characterTradeFrame != null) {
				characterTradeFrame.setGold(info.getGold());
			}
		}
		else if (RealmDirectInfoHolder.TRADE_CANCEL.equals(command) || RealmDirectInfoHolder.TRADE_DONE.equals(command)) {
			killCharacterTradeFrame();
		}
		else if (RealmDirectInfoHolder.SPELL_AFFECT_TARGETS.equals(command)) {
			for (Iterator i = info.getGameObjects().iterator(); i.hasNext();) {
				GameObject spellObject = (GameObject) i.next();
				SpellWrapper spell = new SpellWrapper(spellObject);
				spell.affectTargets(CombatFrame.getSingleton(), game, false);
			}
			// If a spell is being applied through direct info, then make sure
			// the combat frame reflects the change!!
			resetCombatFrame();
		}
		else if (RealmDirectInfoHolder.SPELL_AFFECT_TARGETS_EXPIRE_IMMEDIATE.equals(command)) {
			for (Iterator i = info.getGameObjects().iterator(); i.hasNext();) {
				GameObject spellObject = (GameObject) i.next();
				SpellWrapper spell = new SpellWrapper(spellObject);
				TileLocation before = spell.getCurrentLocation();
				spell.affectTargets(CombatFrame.getSingleton(), game, true); // this
																				// is
																				// STILL
																				// happening
																				// in
																				// a
																				// thread...
				TileLocation after = spell.getCurrentLocation();
				// System.out.println("before="+before);
				// System.out.println("after="+after);
				if (!before.equals(after)) {
					// The spell transported its target, so update combat
					RealmBattle.testCombatInClearing(before, client.getGameData());
				}
			}
			// If a spell is being applied through direct info, then make sure
			// the combat frame reflects the change!!
			resetCombatFrame();
		}
		// else if
		// (RealmDirectInfoHolder.SPELL_WISH_FORCE_TRANSPORT.equals(command)) {
		// // I don't think this is used anymore...
		// ArrayList list = info.getGameObjects();
		// GameObject victim = (GameObject)list.get(0);
		// CharacterWrapper character = new CharacterWrapper(victim);
		// JFrame frame = getMainFrame();
		// if (game.inCombat()) {
		// frame =
		// CombatFrame.getSingleton(frame,getClient().getClientName(),clientSubmitter);
		// }
		// SpellUtility.doTeleport(frame,"Wish",character,TeleportType.ChooseAny);
		// }
		else if (RealmDirectInfoHolder.QUERY_RESPONSE.equals(command)) {
			CombatFrame frame = CombatFrame.getSingleton();
			if (frame != null) {
				frame.handleQueryResponse(info);
			}
		}
		else if (RealmDirectInfoHolder.QUERY_YN.equals(command)) {
			String string = info.getString();
			int colon = string.indexOf(":");
			long id = Long.valueOf(string.substring(0, colon)).longValue();
			string = string.substring(colon + 1);
			int ret = JOptionPane.showConfirmDialog(CombatFrame.getSingleton(), string, "End Combat?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			RealmDirectInfoHolder infoResponse = new RealmDirectInfoHolder(getClient().getGameData(), getClient().getClientName());
			infoResponse.setCommand(RealmDirectInfoHolder.QUERY_RESPONSE);
			infoResponse.setString(id + ":" + (ret == JOptionPane.YES_OPTION ? RealmDirectInfoHolder.QUERY_RESPONSE_YES : RealmDirectInfoHolder.QUERY_RESPONSE_NO));
			getClient().sendInfoDirect(info.getPlayerName(), infoResponse.getInfo());
		}
		else if (RealmDirectInfoHolder.POPUP_MESSAGE.equals(command)) {
			RealmUtility.popupMessage(CombatFrame.getSingleton(), info);
		}
		else if (RealmDirectInfoHolder.HOST_NEED_EMAIL.equals(command)) {
			RealmDirectInfoHolder infoResponse = new RealmDirectInfoHolder(getClient().getGameData(), getClient().getClientName());
			infoResponse.setCommand(RealmDirectInfoHolder.CLIENT_RESPOND_EMAIL);
			infoResponse.setString(clientEmail);
			getClient().sendInfoDirect(null, infoResponse.getInfo()); // null
																		// player
																		// name
																		// indicates
																		// the
																		// message
																		// is
																		// for
																		// the
																		// host
																		// only.
		}
		else if (RealmDirectInfoHolder.RANDOM_NUMBER_GENERATOR.equals(command)) {
			RandomNumber.setRandomNumberGenerator(RandomNumberType.valueOf(info.getString()));
		}
		// All other messages are ignored - I think that's fine
	}

	private void resetCombatFrame() {
		if (CombatFrame.isSingletonShowing()) {
			CombatFrame.getSingleton().handleMissingMonsters();
			CombatFrame.getSingleton().updateFrame(client.getGameData());
		}
	}

	private void handleBroadcast(String key, String message) {
		if (Constants.BROADCAST_ATTENTION.equals(key)) {
			if (!local)
				SoundUtility.playAttention();
		}
		else if (Constants.BROADCAST_SPECIAL_ACTION.equals(key)) {
			if (Constants.MESSAGE_RESTART_MAP_BUILDER.equals(message)) {
				if (hostPlayer) {
					// Start in a separate thread - startMapBuilding does a
					// submitChanges
					Thread thread = new Thread() {
						public void run() {
							startMapBuilding();
							broadcast(Constants.BROADCAST_SPECIAL_ACTION, Constants.MESSAGE_REFRESH_TILE_PICKER);
						}
					};
					thread.start();
				}
			}
			else if (Constants.MESSAGE_REFRESH_TILE_PICKER.equals(message)) {
				if (tilePickFrame != null) {
					// Start in a separate thread - tilePickFrame.refreshTiles
					// does a submitChanges
					Thread thread = new Thread() {
						public void run() {
							tilePickFrame.refreshTiles();
						}
					};
					thread.start();
				}
			}
			else if (Constants.MESSAGE_REPLOT_MAP.equals(message)) {
				getInspector().redrawMap();
			}
		}
		else if (Constants.BROADCAST_SUMMARY_ACTION.equals(key) && isHostPlayer()) {
			// FIXME for now:
			handleSummaryMessage(message);
		}
		else if (key.startsWith(Constants.BROADCAST_PRIVATE_MESSAGE)) {
			String targetPlayerName = key.substring(Constants.BROADCAST_PRIVATE_MESSAGE.length());
			if (targetPlayerName.equals(client.getClientName())) {
				MessageMaster.showMessage(CombatFrame.getShowingSingleton(), message, "Private Message", JOptionPane.INFORMATION_MESSAGE);
				log.addMessage("Private Message", message);
			}
		}
		else if (key.startsWith(Constants.BROADCAST_CHAT)) {
			String id = key.substring(Constants.BROADCAST_CHAT.length());
			GameObject go = client.getGameData().getGameObject(Long.valueOf(id));
			ChatLine line = new ChatLine(new CharacterWrapper(go), message);
			if (line.isValid()) {
				CharacterChatPanel.updateAllChatPanels(line);
				inspector.addChatLine(line);
			}
		}
		else {
			log.addMessage(key, message);
		}
	}

	public void broadcastChat(CharacterWrapper character, String text) {
		broadcast(Constants.BROADCAST_CHAT + character.getGameObject().getStringId(), text);
	}

	public void broadcastAttention() {
		broadcast(Constants.BROADCAST_ATTENTION, "");
	}

	private void handleSummaryMessage(String message) {
		SummaryEventWrapper sew = SummaryEventWrapper.getSummaryEventWrapper(client.getGameData());
		sew.addSummaryEvent(message);
	}

	public ArrayList<String> getNotes() {
		SummaryEventWrapper sew = SummaryEventWrapper.getSummaryEventWrapper(client.getGameData());
		return sew.getSummaryEvents();
	}

	public void broadcast(String key, String message) {
		client.broadcast(key, message);
	}

	public void broadcastMapReplot() {
		game.bumpMapRepaint();
		broadcast(Constants.BROADCAST_SPECIAL_ACTION, Constants.MESSAGE_REPLOT_MAP);
	}

	public void broadcastSummaryMessage(String message) { // Ultimately, this
															// will be a
															// SummaryMessage
															// object
		if (isHostPlayer()) {
			handleSummaryMessage(message);
		}
		else {
			broadcast(Constants.BROADCAST_SUMMARY_ACTION, message);
		}
	}

	public void setup(GameHost host, String ip, int port, String name, String pass) {
		client = new GameClient(RealmLoader.DATA_PATH, ip, name, pass, port) {
			public void receiveInfoDirect(ArrayList inList) {
				RealmDirectInfoHolder info = new RealmDirectInfoHolder(client.getGameData(), inList);
				handleDirectInfo(info);
			}

			public void receiveBroadcast(String key, String message) {
				handleBroadcast(key, message);
			}
		};
		client.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateGameHandler();
					}
				});
			}
		});
		if (host != null) { // this will happen when running a local
							// (non-network) game
			host.connectClient(client);
			local = true;
		}
		else {
			local = false;
		}
		if (parent != null) { // parent will be null when CharacterFrame is
								// called standalone
			client.start();
		}
	}

	private synchronized void updateGameHandler() {
		// Change of strategy (9/12/2007) - Let's see if a synchronized method
		// will fix BUG #915 without breaking anything else.
		// try {
		// while (updatingList) {
		// Thread.sleep(200); // Without this, it hangs unnecessarily
		// }
		// }
		// catch (Exception ex) {
		// // empty
		// }

		if (!client.isConnected()) {
			parent.killHandler();
			if (!client.isLeave()) {
				// This is bad - need to shut down the game handler
				JOptionPane.showMessageDialog(getMainFrame(), "No Connection!  Server might be down.  Check that you are using the correct IP Address, and port.", "Server Down", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		else {
			// Client is connected
			if (!client.isDataLoaded()) {
				setConnectionStatus("Connected.  Receiving data...");
			}
			else {
				// Data is loaded
				if (inspector == null) {
					setConnectionStatus("Data loaded.  Building map view...");

					// Load game
					game = findGame();
					// if (!RealmUtility.ignoreGameVersion()) {
					if (!Constants.REALM_SPEAK_VERSION.equals(game.getVersion())) {
						// Incompatability!
						parent.killHandler();
						client.kill();
						JOptionPane.showMessageDialog(getMainFrame(), "The host is using version " + game.getVersion() + " and you are using " + Constants.REALM_SPEAK_VERSION, "Incompatible Versions!", JOptionPane.ERROR_MESSAGE);
						return;
					}
					else {
						System.out.println("Versions match!");
					}
					// }

					DieRoller.setDieRollerLog(RealmUtility.getDieRollerLog(client.getGameData()));

					// Energize permanent spells as necessary
					SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(client.getGameData());
					spellMaster.energizePermanentSpells(getMainFrame(), game);

					// Create inspector and map singleton
					CenteredMapView.initSingleton(client.getGameData());
					inspector = new RealmInspectorFrame(getMainFrame(), client.getGameData(), game);
					inspector.setZoomSlider(parent.getRealmSpeakOptions().getOptions().getBoolean(RealmSpeakOptions.MAP_SLIDER));
					inspector.setClearingHighlight(parent.getRealmSpeakOptions().getOptions().getBoolean(RealmSpeakOptions.HIGHLIGHT_CLEARING_NUMBERS));
					inspector.setShowSeasonIcon(parent.getRealmSpeakOptions().getOptions().getBoolean(RealmSpeakOptions.SHOW_SEASON_ICON));
					inspector.setShowChatLines(parent.getRealmSpeakOptions().getOptions().getInt(RealmSpeakOptions.NUMBER_OF_CHAT_LINES));
					// parent.updateChatLines();
					inspector.revalidate();
					inspector.repaint();
					inspector.getMap().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent aev) {
							if (aev.getID() == CenteredMapView.CLICK_CLEARING_ACTION) {
								TileLocation tl = TileLocation.parseTileLocation(client.getGameData(), aev.getActionCommand());
								handleClearingClick(tl);
							}
							else if (aev.getID() == CenteredMapView.CLICK_SEASON_ACTION) {
								RealmCalendarViewer view = new RealmCalendarViewer(client.getGameData());
								view.setLocationRelativeTo(getMainFrame());
								view.showSeasonDetail();
							}
						}
					});
					parent.addFrameToDesktop(inspector);

					setConnectionStatus("Data loaded.  Initialize..");

					// Load host prefs
					hostPrefs = HostPrefWrapper.findHostPrefs(client.getGameData());
					ArrayList<String> missing = CustomCharacterLibrary.getSingleton().getMissingCharacterNames(hostPrefs.getAllCharacterKeys());
					if (missing.size() > 0 && !DebugUtility.isIgnoreChars()) {
						StringBuffer sb = new StringBuffer();
						sb.append("You are missing custom characters.  Contact the host, and get files for:");
						for (String name : missing) {
							sb.append("\n   ");
							sb.append(name);
						}
						sb.append("\n\nYou can continue to play, but you may encounter fatal errors when dealing with custom characters during the game.");
						JOptionPane.showMessageDialog(getMainFrame(), sb.toString());
					}

					if (isHostPlayer()) {
						if (hostPrefs.getBoardPlayerSetup() && !inspector.getMap().isMapReady()) {
							startMapBuildingButton.setEnabled(true);
							startGameButton.setEnabled(false);
						}
						else {
							if (game.getGameStarted()) {
								if (game.getGameEnded()) {
									revealAllButton.setEnabled(!game.hasBeenRevealed());
								}
								else {
									endGameButton.setEnabled(true);
								}
							}
							else {
								startGameButton.setEnabled(true);
							}
						}
					}

					// Ready!
					setConnectionStatus("Ready.");
					updateControls();
				}
				else {
					if (hostPrefs.isUsingSeasons()) {
						RealmCalendar cal = RealmCalendar.getCalendar(getClient().getGameData());
						int month = game.getMonth();
						String weather = game.getWeather();
						if (month != lastMonth) {
							lastMonth = month;
							lastWeather = weather;
							if (hostPrefs.hasPref(Constants.OPT_WEATHER)) {
								inspector.getMap().setMapAttentionMessage(cal.getSeasonName(month) + " -- " + cal.getWeatherName(month));
							}
							else {
								inspector.getMap().setMapAttentionMessage(cal.getSeasonName(month));
							}
						}
						else if (hostPrefs.hasPref(Constants.OPT_WEATHER) && !weather.equals(lastWeather)) {
							lastWeather = weather;
							inspector.getMap().setMapAttentionMessage(cal.getWeatherName(month));
						}
					}

					logger.fine("RealmGameHandler notes a change in the client!  Redraw map.");
					inspector.redrawMap();
				}

				// Update character table
				updateCharacterList();
				updateControls();
			}
		}
	}

	private CharacterFrame getTopmostFrame() {
		if (characterFrameOrder.size() > 0) {
			try {
				String topmostId = (String) characterFrameOrder.get(0);
				return (CharacterFrame) characterFrames.get(topmostId);
			}
			catch (IndexOutOfBoundsException ex) {
				// Ignore this exception
			}
		}
		return null;
	}

	public CharacterActionControlManager findTopmostActionControlManager() {
		CharacterActionControlManager acm = null;
		CharacterFrame topmostFrame = getTopmostFrame();
		if (topmostFrame != null) {
			// Okay, the topmost character frame is still recording, so now we
			// can add a move
			if (topmostFrame.getCharacter().isDoRecord()) {
				acm = topmostFrame.getActionPanel().getActionControlManager();
			}
			if (topmostFrame.getCharacter().canDoDaytimeRecord() && topmostFrame.showingTurn()) {
				acm = topmostFrame.getTurnPanel().getActionControlManager();
			}
		}
		return acm;
	}

	public RealmTurnPanel findTopmostRealmTurnPanel() {
		CharacterFrame topmostFrame = getTopmostFrame();
		if (topmostFrame != null && topmostFrame.showingTurn()) {
			return topmostFrame.getTurnPanel();
		}
		return null;
	}

	private void handleClearingClick(TileLocation tl) {
		/*
		 * - Find the topmost character frame - Determine if the character is
		 * still recording a turn (or has prophecy/jewel active) - If yes, add
		 * the move action and update accordingly
		 */
		CharacterFrame topmostFrame = getTopmostFrame();
		if (topmostFrame != null) {
			// Okay, the topmost character frame is still recording, so now we
			// can add a move
			if (topmostFrame.getCharacter().isDoRecord()) {
				topmostFrame.getActionPanel().getActionControlManager().recordExternalMoveAction(tl);
			}
			if (topmostFrame.getCharacter().canDoDaytimeRecord() && topmostFrame.showingTurn()) {
				topmostFrame.getTurnPanel().getActionControlManager().recordExternalMoveAction(tl);
			}
		}
	}

	public ArrayList<String[]> getRelationshipNames() {
		if (relationshipNames == null) {
			GamePool pool = getGamePool();
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
		}
		return relationshipNames;
	}

	public RealmSpeakFrame getMainFrame() {
		return parent;
	}

	public GameClient getClient() {
		return client;
	}

	/**
	 * Uses the current hostPrefs to build a GamePool object holding all the
	 * objects for the current game
	 */
	public GamePool getGamePool() {
		ArrayList keyVals = new ArrayList();
		if (hostPrefs == null) { // this only happens when running the character
									// frame standalone
			hostPrefs = HostPrefWrapper.findHostPrefs(client.getGameData());
		}
		keyVals.add(hostPrefs.getGameKeyVals());
		GamePool pool = new GamePool(client.getGameData().getGameObjects());
		return new GamePool(pool.find(keyVals));
	}

	public TreasureSetupCardView getTreasureSetupCardView() {
		TreasureSetupCardView ret = null;
		if (treasureSetupCardView == null) {
			if (hostPrefs.getMultiBoardEnabled()) {
				int count = hostPrefs.getMultiBoardCount();
				treasureSetupCardView = new TreasureSetupCardView[count];
				treasureSetupCardView[0] = new TreasureSetupCardView(client.getGameData(), client.getClientName(), "!" + Constants.BOARD_NUMBER);
				for (int n = 1; n < count; n++) {
					String boardNumber = Constants.MULTI_BOARD_APPENDS.substring(n - 1, n);
					treasureSetupCardView[n] = new TreasureSetupCardView(client.getGameData(), client.getClientName(), Constants.BOARD_NUMBER + "=" + boardNumber);
				}
			}
			else {
				treasureSetupCardView = new TreasureSetupCardView[1];
				treasureSetupCardView[0] = new TreasureSetupCardView(client.getGameData(), client.getClientName());
			}
		}
		if (treasureSetupCardView.length > 1) {
			ButtonOptionDialog chooser = new ButtonOptionDialog(getMainFrame(), null, "View setup for which board?", "Setup Card", true);
			for (int i = 0; i < treasureSetupCardView.length; i++) {
				chooser.addSelectionObject(treasureSetupCardView[i]);
			}
			chooser.setVisible(true);
			ret = (TreasureSetupCardView) chooser.getSelectedObject();
		}
		else {
			ret = treasureSetupCardView[0];
		}
		return ret;
	}

	public RealmInspectorFrame getInspector() {
		return inspector;
	}

	private void setConnectionStatus(String val) {
		connectionStatus.setText(val);
	}

	/**
	 * Uses gameData to display possible characters to play.
	 */
	protected void createNewCharacter() {

		// Need to inform the host that we are picking a character
		game.setCharacterPoolLock(true);
		logger.fine("Submitting character pool lock...");
		submitChanges();
		logger.fine("Done");

		GamePool pool = getGamePool();
		ArrayList characters = pool.find("character,!" + CharacterWrapper.NAME_KEY);
		Collections.sort(characters, new Comparator() {
			public int compare(Object o1, Object o2) {
				GameObject go1 = (GameObject) o1;
				GameObject go2 = (GameObject) o2;
				return go1.getName().compareTo(go2.getName());
			}
		});

		CharacterChooser chooser = new CharacterChooser(parent, characters, hostPrefs);
		chooser.setVisible(true);

		GameObject chosen = chooser.getChosenCharacter();
		if (chosen != null) {
			CharacterWrapper character = new CharacterWrapper(chosen);

			// Double check to make sure character wasn't taken at the same time
			if (character.getGameObject().hasAttribute(CharacterWrapper.PLAYER_BLOCK, CharacterWrapper.NAME_KEY)) {
				// This should be rare, but it can happen (I think) even with
				// the character pool lock if two players
				// press the "New Character" button at the same time, and select
				// the exact same character.

				JOptionPane.showMessageDialog(getMainFrame(), "Seems that character was chosen already.  Try again.", "Character Taken", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (hostPrefs.hasPref(Constants.OPT_CHAR_ABILITY_ELF) && character.getGameObject().hasThisAttribute(Constants.LIGHT_GREAT)) {
				RealmComponentOptionChooser typeOption = new RealmComponentOptionChooser(getMainFrame(), "Choose a character type:", true);
				typeOption.addOption("light", "Light " + character.getGameObject().getName());
				typeOption.addOption("great", "Great " + character.getGameObject().getName());
				typeOption.setVisible(true);
				String selText = typeOption.getSelectedText();
				if (selText != null) {
					character.setCharacterType(typeOption.getSelectedOptionKey());
				}
				else {
					game.setCharacterPoolLock(false);
					submitChanges();
					return; // canceled
				}
			}

			String name = client.getClientName();
			if (name != null) {
				boolean forceInnStart = hostPrefs.hasPref(Constants.HOUSE1_FORCE_INN_AFTER_GAMESTART) && game.getGameStarted();
				boolean allowDevelopment = hostPrefs.hasPref(Constants.EXP_DEVELOPMENT);
				// Pass DevelopmentPastFour preference to the dialog
				boolean developmentPastFour = hostPrefs.hasPref(Constants.EXP_DEVELOPMENT_PLUS);
				CharacterOptionsDialog dialog = new CharacterOptionsDialog(parent, character, forceInnStart, allowDevelopment, developmentPastFour);
				dialog.setVisible(true);
				if (dialog.saidOkay()) {
					// First, set a chat color based on total characters
					int totalChars = pool.find("character," + CharacterWrapper.NAME_KEY).size();
					character.setChatStyle(ChatStyle.styles[totalChars % ChatStyle.styles.length].getStyleName());

					// Set default values
					character.setPlayerName(name);
					character.setPlayerPassword(clientPlayerPass);
					character.setPlayerEmail(clientEmail);
					String levelName = (String) dialog.getChosenLevelName();
					int level;
					// Handle special case for level 10
					if (levelName.substring(0, 2).equals("10")) {
						level = 10;
					}
					else {
						level = Integer.valueOf(levelName.substring(0, 1)).intValue();
					}
					int bonusChits = dialog.getChosenBonusChits();
					character.setStartingLevel(level);
					character.setCharacterLevel(level); // only supports single
														// digit level numbers
														// (for now)
					// Set starting stage based on level and bonus chits
					character.setStartingStage((level * 3) + bonusChits);
					character.setCharacterStage((level * 3) + bonusChits);
					character.setCharacterExtraChitMarkers((level * 3) + bonusChits);
					character.initChits();
					// Allow player to pick bonus chits
					for (int i = 0; i < bonusChits; i++) {
						ArrayList list = character.getAdvancementChits();
						if (!list.isEmpty()) {
							RealmComponentOptionChooser chitChooser = new RealmComponentOptionChooser(getMainFrame(), "Choose a Bonus Chit", true);
							chitChooser.addRealmComponents(list, false);
							chitChooser.setVisible(true);
							if (chitChooser.getSelectedText() == null)
								continue;
							CharacterActionChitComponent chit = (CharacterActionChitComponent) chitChooser.getFirstSelectedComponent();
							chit.getGameObject().setThisAttribute("chitEarned");
							character.updateChitEffects();
						}
					}
					character.setWantsDayEndTrades(hostPrefs.hasPref(Constants.HOUSE2_DAY_END_TRADING_ON));
					character.setWantsCombat(getMainFrame().getRealmSpeakOptions().dailyCombatOn(character));
					character.clearRelationships(hostPrefs);
					character.initRelationships(hostPrefs);

					if (!hostPrefs.getRequiredVPsOff() && !hostPrefs.hasPref(Constants.QST_BOOK_OF_QUESTS)) {
						RealmCalendar cal = RealmCalendar.getCalendar(getClient().getGameData());
						int vps = 0;
						if (hostPrefs.hasPref(Constants.EXP_SUDDEN_DEATH)) {
							vps = hostPrefs.getVpsToAchieve();
						}
						else {
							int totalMonths = hostPrefs.getNumberMonthsToPlay();
							for (int i = 0; i < totalMonths; i++) {
								int vp = cal.getVictoryPoints(i + 1);
								vps += vp;
							}
						}

						// Lower level characters get to choose fewer VPs
						vps -= (Constants.MAX_LEVEL - level);

						if (vps < Constants.MINIMUM_VPS) {
							vps = Constants.MINIMUM_VPS;
						}

						if (hostPrefs.hasPref(Constants.HOUSE2_ANY_VPS)) {
							vps = -1;
						}

						if (hostPrefs.hasPref(Constants.QST_QUEST_CARDS) && vps!=-1) {
							character.addVictoryRequirements(vps,0,0,0,0,0);
						}
						else {
							character.setNewVPRequirement(vps);
						}
					}

					// Figure out starting location
					// 1) Find the dwelling or ghost gameObject
					if (!hostPrefs.hasPref(Constants.EXP_NO_DWELLING_START)) {
						String starting = dialog.getChosenStartName().toLowerCase();
						ArrayList dwellingKeyVals = new ArrayList();
						if (starting.equals("ghost")) {
							dwellingKeyVals.add("monster");
							dwellingKeyVals.add("icon_type=ghost");
						}
						else {
							dwellingKeyVals.add("dwelling=" + starting);
						}
						if (hostPrefs.getMultiBoardEnabled()) {
							String bn = character.getGameObject().getThisAttribute(Constants.BOARD_NUMBER);
							if (bn != null) {
								dwellingKeyVals.add(Constants.BOARD_NUMBER + "=" + bn);
							}
							else {
								dwellingKeyVals.add("!" + Constants.BOARD_NUMBER);
							}
						}
						Collection startingChits = pool.find(dwellingKeyVals);
						if (startingChits != null && !startingChits.isEmpty()) {
							GameObject startingChit = (GameObject) startingChits.iterator().next();
							// 2) Get the heldBy (tile)
							GameObject tile = startingChit.getHeldBy();

							// 3) Add character to tile
							tile.add(character.getGameObject());

							// 4) Set the character clearing
							String clearing = startingChit.getThisAttribute("clearing");
							character.getGameObject().setThisAttribute("clearing", clearing);
						}
						else {
							throw new IllegalStateException("Starting location is invalid!");
						}
					}
					else {
						// Alternate starting location
						CenteredMapView.getSingleton().setMarkClearingAlertText("Where do you want to start?");
						CenteredMapView.getSingleton().markAllMapEdges(true);
						TileLocationChooser tlChooser = new TileLocationChooser(getMainFrame(), CenteredMapView.getSingleton(), null);
						tlChooser.setVisible(true);
						TileLocation selTl = tlChooser.getSelectedLocation();
						character.moveToLocation(null, selTl);
						CenteredMapView.getSingleton().markAllMapEdges(false);
						CenteredMapView.getSingleton().centerOn(selTl);
					}

					// Fetch Inventory
					character.fetchStartingInventory(getMainFrame(), client.getGameData(), !dialog.wantsRandomInventorySourceSelection());

					// Initialize
					character.tagUnplayableChits(); // this need only happen
													// once, because it examines
													// ALL the chits
					character.updateLevelAttributes(hostPrefs);
					character.setGold(character.getStartingGold());

					character.applyMidnight();
					character.setCharacterJoinOrder(characterJoinOrder++);

					character.setHidden(true); // start the game hidden

					// Choose Spells
					RealmUtility.fetchStartingSpells(parent, character, client.getGameData(), true);

					if (developmentPastFour) {
						fetchExtendedDevelopmentBonuses(character);
					}
					// Call to calculateStartingWorth moved here so that
					// Extended Development equipment is counted as starting
					// equipment
					character.calculateStartingWorth();
					
					if (hostPrefs.hasPref(Constants.QST_QUEST_CARDS)) {
						QuestDeck deck = QuestDeck.findDeck(client.getGameData());
						deck.setupAllPlayCards(getMainFrame(),character);
						deck.drawCards(getMainFrame(),character);
					}

					// No need to send again - it'll get sent quickly enough

					// Done
					broadcast(character.getGameObject().getName(), "Joins the game.");
					broadcastSummaryMessage(character.getGameObject().getName() + " joins the game.");
				}
			}
		}
		game.setCharacterPoolLock(false);
		logger.fine("Releasing character pool lock...");
		submitChanges();
		logger.fine("Done");
	}

	protected void fetchExtendedDevelopmentBonuses(CharacterWrapper character) {
		int level = character.getCharacterLevel();
		if (level >= 6) {
			doPickTreasure(character);
		}
		if (level >= 7) {
			character.addFame(5D);
			character.addNotoriety(10D);
		}
		if (level >= 8) {
			doPickHorse(character);

		}
		if (level >= 9) {
			character.getGameObject().setThisAttribute("extra_phase");
			String adv = "LEVEL 9 BONUS:  Gets bonus phase every day.";
			character.getGameObject().addAttributeListItem("level_4", "advantages", adv);
		}
		if (level >= 10) {
			doPickCounterOrSpell(character);
		}
	}

	protected void doPickTreasure(CharacterWrapper character) {
		// Find all native leaders that have treasure cards
		GamePool pool = getGamePool();
		ArrayList<GameObject> leaders = pool.find("rank=HQ");
		Hashtable<GameObject, ArrayList<GameObject>> hash = new Hashtable<GameObject, ArrayList<GameObject>>();
		for (GameObject leader : leaders) {
			GameObject dwelling = SetupCardUtility.getDenizenHolder(leader);
			ArrayList<GameObject> treasures = new ArrayList<GameObject>();
			for (Iterator i = dwelling.getHold().iterator(); i.hasNext();) {
				GameObject item = (GameObject) i.next();
				if (item.hasThisAttribute("treasure")) {
					treasures.add(item);
				}
			}
			if (!treasures.isEmpty()) {
				hash.put(leader, treasures);
			}
		}

		if (!hash.isEmpty()) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getMainFrame(), "Level Bonus - Select a native group:", false);
			for (GameObject leader : hash.keySet()) {
				ArrayList<GameObject> treasures = hash.get(leader);
				int n = treasures.size();
				String option = chooser.generateOption(n + " treasure" + (n == 1 ? "" : "s"));
				chooser.addGameObjectToOption(option, leader);
			}
			chooser.setVisible(true);
			GameObject chosenLeader = chooser.getFirstSelectedComponent().getGameObject();
			ArrayList<GameObject> treasures = hash.get(chosenLeader);
			int r = RandomNumber.getRandom(treasures.size());
			GameObject randomTreasure = treasures.get(r);
			CardComponent card = (CardComponent) RealmComponent.getRealmComponent(randomTreasure);
			card.setFaceUp();
			JOptionPane.showMessageDialog(getMainFrame(), "You received the " + randomTreasure.getName(), "Level Bonus", JOptionPane.PLAIN_MESSAGE, card.getIcon());

			Loot.addItemToCharacter(getMainFrame(), getUpdateFrameListener(), character, randomTreasure, hostPrefs);
		}
		else {
			JOptionPane.showMessageDialog(getMainFrame(), "Unfortunately, there are no native groups with any treasure cards!", "Level Bonus", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	protected void doPickHorse(CharacterWrapper character) {
		// Find all available horses
		GamePool pool = getGamePool();
		ArrayList<GameObject> leaders = pool.find("rank=HQ");
		ArrayList<GameObject> horses = new ArrayList<GameObject>();
		for (GameObject leader : leaders) {
			GameObject dwelling = SetupCardUtility.getDenizenHolder(leader);
			for (Iterator i = dwelling.getHold().iterator(); i.hasNext();) {
				GameObject item = (GameObject) i.next();
				if (item.hasThisAttribute("horse")) {
					horses.add(item);
				}
			}
		}
		if (!horses.isEmpty()) {
			RealmTradeDialog chooser = new RealmTradeDialog(getMainFrame(), "Level Bonus - Pick a horse:", false, false, true);
			chooser.setTradeObjects(horses);
			chooser.setVisible(true);
			RealmComponent horse = chooser.getFirstSelectedRealmComponent();
			JOptionPane.showMessageDialog(getMainFrame(), "You received the " + horse.getGameObject().getName(), "Level Bonus", JOptionPane.PLAIN_MESSAGE, horse.getIcon());

			Loot.addItemToCharacter(getMainFrame(), getUpdateFrameListener(), character, horse.getGameObject(), hostPrefs);
		}
		else {
			JOptionPane.showMessageDialog(getMainFrame(), "Unfortunately, there are no native groups with any horses!", "Level Bonus", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	protected void doPickCounterOrSpell(CharacterWrapper character) {
		// Take one weapon or armor counter from any native group; or record one
		// extra spell of any type.
		ButtonOptionDialog which = new ButtonOptionDialog(getMainFrame(), null, "Counter or Spell?", "Level Bonus", false);
		which.addSelectionObject("Weapon/Armor Counter");
		which.addSelectionObject("New Spell");
		which.addSelectionObject("Nothing");
		which.setVisible(true);
		String res = (String) which.getSelectedObject();
		if ("New Spell".equals(res)) {
			if (!doPickSpell(character)) {
				doPickCounterOrSpell(character);
			}
		}
		else if ("Weapon/Armor Counter".equals(res)) {
			if (!doPickCounter(character)) {
				doPickCounterOrSpell(character);
			}
		}
		// else nothing
	}

	protected boolean doPickSpell(CharacterWrapper character) {
		// Record one extra spell of any type
		GamePool pool = getGamePool();
		ArrayList<GameObject> spells = pool.find("spell");
		ArrayList<GameObject> learnable = new ArrayList<GameObject>();
		for (GameObject go : spells) {
			String spellType = go.getThisAttribute("spell");
			boolean instance = go.hasThisAttribute("Instance");
			if (!character.hasAlreadyLearned(go) && (spellType != null && spellType.trim().length() > 0 && !spellType.trim().equals("*") && !instance)) {
				learnable.add(go);
			}
		}
		if (!learnable.isEmpty()) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getMainFrame(), "Level Bonus - Select a spell to record:", true);
			chooser.addGameObjects(learnable, false);
			chooser.setVisible(true);
			if (chooser.getSelectedText() != null) {
				RealmComponent rc = chooser.getFirstSelectedComponent();
				character.recordNewSpell(getMainFrame(), rc.getGameObject());
				return true;
			}
		}
		else {
			JOptionPane.showMessageDialog(getMainFrame(), "Unfortunately, you already know all the possible spells!", // pretty
																														// unlikely...
					"Level Bonus", JOptionPane.INFORMATION_MESSAGE);
		}
		return false;
	}

	protected boolean doPickCounter(CharacterWrapper character) {
		// Take one weapon or armor counter from any native group
		GamePool pool = getGamePool();
		ArrayList<GameObject> leaders = pool.find("rank=HQ");
		ArrayList<GameObject> counters = new ArrayList<GameObject>();
		for (GameObject leader : leaders) {
			GameObject dwelling = SetupCardUtility.getDenizenHolder(leader);
			for (Iterator i = dwelling.getHold().iterator(); i.hasNext();) {
				GameObject item = (GameObject) i.next();
				if (!item.hasThisAttribute("treasure")) {
					if (item.hasThisAttribute("weapon") || item.hasThisAttribute("armor")) {
						counters.add(item);
					}
				}
			}
		}
		if (!counters.isEmpty()) {
			RealmTradeDialog chooser = new RealmTradeDialog(getMainFrame(), "Level Bonus - Pick a counter:", false, true, true);
			chooser.setTradeObjects(counters);
			chooser.setVisible(true);
			RealmComponent counter = chooser.getFirstSelectedRealmComponent();
			if (counter != null) {
				JOptionPane.showMessageDialog(getMainFrame(), "You received the " + counter.getGameObject().getName(), "Level Bonus", JOptionPane.PLAIN_MESSAGE, counter.getIcon());

				Loot.addItemToCharacter(getMainFrame(), getUpdateFrameListener(), character, counter.getGameObject(), hostPrefs);
				return true;
			}
		}
		else {
			JOptionPane.showMessageDialog(getMainFrame(), "Unfortunately, there are no native groups with any weapons or armor!", "Level Bonus", JOptionPane.INFORMATION_MESSAGE);
		}
		return false;
	}

	public void submitChanges() {
		logger.finer("submitChanges()");
		GameClient.submitAndWait(client);
		characterTable.repaint();
	}

	private GameWrapper findGame() {
		GamePool pool = new GamePool(client.getGameData().getGameObjects());
		Collection mrGameObjects = pool.extract(GameWrapper.getKeyVals());
		if (mrGameObjects.size() == 1) {
			GameObject go = (GameObject) mrGameObjects.iterator().next();
			return new GameWrapper(go);
		}
		return null;
	}

	/**
	 * Resize according to a set strategy
	 */
	public void organize(JDesktopPane desktop) {
		Dimension size = desktop.getSize();
		int w = size.width >> 1;
		if (forceWidth != null) {
			w = forceWidth.intValue();
		}
		int h = size.height / 4;
		setSize(w, h);
		setLocation(0, 0);
		try {
			setIcon(false);
		}
		catch (PropertyVetoException ex) {
			ex.printStackTrace();
		}
	}

	public boolean onlyOneInstancePerGame() {
		return true;
	}

	public String getFrameTypeName() {
		return "Game";
	}

	public ChangeListener getUpdateFrameListener() {
		return updateFrameListener;
	}

	public void updatePickFrame() {
		// Show tile pick frame, if necessary
		if (game.getGameMapBuilder() != null) {
			if (tilePickFrame == null) {
				tilePickFrame = new RealmTilePickFrame(this, game, inspector.getMap());
				parent.addFrameToDesktop(tilePickFrame);
			}
		}
		else {
			if (tilePickFrame != null) {
				getInspector().getMap().updateGrid(); // Guarantee that the map
														// is built properly
				parent.removeFrameFromDesktop(tilePickFrame);
				tilePickFrame = null;
			}
		}
		if (tilePickFrame != null) {
			// tilePickFrame.refreshPlaceables();
			tilePickFrame.updateFrame();
		}
	}

	/**
	 * Synchronized is necessary here so that the characterList doesn't get
	 * modified simultaneously by two separate threads!
	 * 
	 * This method should not do any resubmits, since this is called in the same
	 * thread as the client machine...
	 * 
	 * NOTE: This method is called very often, and is the current bottleneck for
	 * overall speed of the game. Any optimizations I can do here, will enhance
	 * "everything"!
	 */
	public synchronized void updateCharacterList() {
		updatingList = true;
		game = findGame();
		boolean framesCreated = false;

		updatePickFrame();

		// Energize permanent spells
		SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(client.getGameData());
		spellMaster.energizePermanentSpells(getMainFrame(), game);

		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(client.getGameData()).getPlayerCharacterObjects());
		Collection characterGameObjects = pool.extract(CharacterWrapper.getKeyVals());
		ArrayList charactersAndMinions = new ArrayList();
		for (Iterator i = characterGameObjects.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			CharacterWrapper character = new CharacterWrapper(go);
			charactersAndMinions.add(go);
			if (character.isActive()) {
				Collection minions = character.getMinions();
				if (minions != null) {
					charactersAndMinions.addAll(minions);
				}
			}
		}
		boolean needSubmit = false;
		characterList.clear();
		CharacterWrapper interactiveCharacter = null;
		ArrayList allFound = new ArrayList();
		for (Iterator i = charactersAndMinions.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			CharacterWrapper character = new CharacterWrapper(go);
			characterList.add(character);

			// don't forget to update any active frames! (maybe this is how they
			// could be created...)
			CharacterFrame frame = (CharacterFrame) characterFrames.get(character.getGameObject().getStringId());
			allFound.add(character.getGameObject().getStringId());

			if (frame == null && !character.isDead() && character.getPlayerName().equals(client.getClientName())) {
				if (character.canPlay()) {
					// Make sure player password is correct (only if not local)
					if (isLocal() || character.validPlayerPassword(clientPlayerPass)) {
						// Joining back in after dropping
						character.setPlayerPassword(clientPlayerPass);
						character.setPlayerEmail(clientEmail);
						logger.fine("Restoring " + character.getCharacterName() + " to " + character.getPlayerName());
						character.setMissingInAction(false);
						needSubmit = true;
						// character.setWantsCombat(getMainFrame().dailyCombatOn(character));
						frame = new CharacterFrame(this, character, iconSize);
						frame.centerOnToken();
						parent.addFrameToDesktop(frame);
						String id = character.getGameObject().getStringId();
						characterFrames.put(id, frame);
						characterFrameOrder.add(0, id);
						playerWarned.remove(character.getGameObject().getStringId()); // just
																						// in
																						// case
						framesCreated = true;
						// broadcastChat(character,"<JOINED>"); // This just
						// looks ugly in the chat
					}
					else {
						if (!playerWarned.contains(character.getGameObject().getStringId())) {
							StringBuffer sb = new StringBuffer();
							sb.append("You have the right player name for the ");
							sb.append(character.getCharacterName());
							sb.append(", but the player password is invalid.\n");
							sb.append("Try a new player name, or ask the host to reset the player password for this character.");
							JOptionPane.showMessageDialog(getMainFrame(), sb.toString(), "Invalid Player Password", JOptionPane.ERROR_MESSAGE);
							playerWarned.add(character.getGameObject().getStringId());
						}
					}
				}
			}

			if (frame != null) {
				if (!character.isDead() && !character.getPlayerName().equals(client.getClientName())) {
					// The frame was probably transferred, and needs to be
					// removed!
					characterTable.clearSelection();
					String id = character.getGameObject().getStringId();
					characterFrames.remove(id);
					characterFrameOrder.remove(id);
					parent.removeFrameFromDesktop(frame);
				}
				else if (character.isDead()) {
					// Need to remove the frame from play...
					characterTable.clearSelection();
					String id = character.getGameObject().getStringId();
					characterFrames.remove(id);
					characterFrameOrder.remove(id);
					parent.removeFrameFromDesktop(frame);
					if (character.isCharacter() && !hostPrefs.hasPref(Constants.HOUSE1_DONT_RECYCLE_CHARACTERS)) {
						character.clearPlayerAttributes(); // puts it back in
															// the player pool
															// again
						character.getGameObject().removeThisAttribute(Constants.DEAD);
						characterList.remove(character);
					}
				}
				else if (character.isJustUnhired()) { // A hired leader becomes
														// unhired
					characterTable.clearSelection();
					String id = character.getGameObject().getStringId();
					characterFrames.remove(id);
					characterFrameOrder.remove(id);
					parent.removeFrameFromDesktop(frame);
					boolean monster = RealmComponent.getRealmComponent(character.getGameObject()).isMonster();
					character.clearPlayerAttributes(!monster && hostPrefs.hasPref(Constants.HOUSE2_NATIVES_REMEMBER_DISCOVERIES));
					characterList.remove(character);
				}
				else {
					boolean active = character.isActive();

					frame.updateCharacter();
					frame.updateControls();

					/*
					 * This is where the GUI recognizes a change in game state,
					 * and requests input from the character if needed.
					 */
					int combatStatus = character.getCombatStatus();
					if (active && combatStatus > 0 && combatStatus < Constants.COMBAT_WAIT) {
						interactiveCharacter = character;
					}
					else if (active && character.getPlayOrder() == 1) {
						// Becomes unhidden at the start of the turn, unless he
						// is hidden as a result
						// of following a guide...

						if (!character.hasDoneActionsToday()) {
							character.setHidden(false);
							character.unhideAllCharacterFollowers();
						}
						if (!frame.showingTurn()) {
							broadcast(character.getGameObject().getName(), "Starts turn: " + character.getCurrentLocation());
							RealmTurnPanel turn = new RealmTurnPanel(frame, game, hostPrefs);
							needSubmit = true;
							frame.showYourTurn(turn);
							getInspector().getMap().centerOn(character.getCurrentLocation());
							frame.toFront();
							String id = character.getGameObject().getStringId();
							characterFrameOrder.remove(id);
							characterFrameOrder.add(0, id);
							if (!local)
								SoundUtility.playAttention();
						}
					}
					else if (character.isGameOver()) {
						// Update Hall of Fame if VPs are used
						if (!hostPrefs.getRequiredVPsOff() && character.isCharacter()) {
							HallOfFame.consider(hostPrefs, character);
						}

						// Show game results panel
						frame.showGameOver();

						if (isHostPlayer()) {
							endGameButton.setEnabled(true);
						}
					}
				}
			}
		}
		// Remove any leftover frames (Phantasms, or Familiar)
		ArrayList leftover = new ArrayList(characterFrames.keySet());
		leftover.removeAll(allFound);
		for (Iterator i = leftover.iterator(); i.hasNext();) {
			String id = (String) i.next();
			characterTable.clearSelection();
			CharacterFrame frame = (CharacterFrame) characterFrames.remove(id);
			characterFrameOrder.remove(id);
			parent.removeFrameFromDesktop(frame);
			frame.getCharacter().clearPlayerAttributes();
			characterList.remove(frame.getCharacter());
		}
		if (game.inCombat()) {
			// This seems to work without using the invokeLater, which I think
			// was causing me problems...
			if (interactiveCharacter != null) {
				showCharacterFrame(interactiveCharacter);
				boolean did = CombatFrame.doDisplayInteractive(getMainFrame(), client.getGameData(), client.getClientName(), clientSubmitter, local, isHostPlayer());
				if (!did) {
					needSubmit = true;
				}
			}
			else {
				// Observation only mode!
				CombatFrame.doDisplayObserving(getMainFrame(), client.getGameData(), client.getClientName(), clientSubmitter, local, isHostPlayer());
			}
		}
		else {
			CombatFrame.closeCombatFrameIfNeeded();

			// This guarantees all characters have their combat reset when
			// combat ends
			for (Iterator i = charactersAndMinions.iterator(); i.hasNext();) {
				GameObject go = (GameObject) i.next();
				CharacterWrapper character = new CharacterWrapper(go);
				if (character.getCombatStatus() > 0) {
					character.clearCombat();
				}
			}
		}
		if (needSubmit) {
			submitChanges();
		}

		characterTableModel.rebuild();
		updateControls();
		updatingList = false;
		if (framesCreated) {
			parent.updateWindowMenu();
		}
	}

	private ActionListener clientSubmitter = new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
			GameClient.submitAndWait(client);
		}
	};

	public void showCharacterFrame(CharacterWrapper character) {
		for (Iterator i = characterFrames.values().iterator(); i.hasNext();) {
			CharacterFrame frame = (CharacterFrame) i.next();
			CharacterWrapper test = frame.getCharacter();
			if (test.getGameObject().equals(character.getGameObject())) {
				frame.toFront();
				frame.centerOnToken();
				String id = character.getGameObject().getStringId();
				characterFrameOrder.remove(id);
				characterFrameOrder.add(0, id);
				return;
			}
		}
	}

	/**
	 * Brings the next recording frame to front, if any
	 */
	public void showNextRecordFrame() {
		for (Iterator i = characterFrames.values().iterator(); i.hasNext();) {
			CharacterFrame frame = (CharacterFrame) i.next();
			CharacterWrapper character = frame.getCharacter();
			if (character.isDoRecord()) {
				characterTable.clearSelection();
				int row = characterTableModel.getCharacterRow(character);
				if (row >= 0) {
					characterTable.getSelectionModel().addSelectionInterval(row, row);
				}
				frame.showActionPanel();
				frame.toFront();
				frame.centerOnToken();
				String id = character.getGameObject().getStringId();
				characterFrameOrder.remove(id);
				characterFrameOrder.add(0, id);
				break;
			}
		}
	}

	/**
	 * A convenient way to update all the player's character frames (like when
	 * an item is picked up)
	 */
	public void updateCharacterFrames() {
		for (Iterator i = characterFrames.values().iterator(); i.hasNext();) {
			CharacterFrame frame = (CharacterFrame) i.next();
			if (frame.getCharacter().isActive()) {
				frame.updateCharacter();
			}
		}
		getInspector().redrawMap();
	}

	private static final ImageIcon LOCK_ICON = IconFactory.findIcon("icons/lock.gif");

	private class CharacterTableModel extends AbstractTableModel {
		protected String[] columnName = { " ", " ", "Character", "Player", "Status", };
		protected Class[] columnClass = { ImageIcon.class, ImageIcon.class, String.class, String.class, String.class, };
		private ArrayList list;

		public CharacterTableModel() {
			rebuild();
		}

		public void rebuild() {
			list = new ArrayList();
			if (characterList != null) {
				for (Iterator i = characterList.iterator(); i.hasNext();) {
					CharacterWrapper character = (CharacterWrapper) i.next();
					if (showDeadOption.isSelected() || !character.isDead()) {
						list.add(character);
					}
				}
			}
			fireTableDataChanged();
		}

		public CharacterWrapper getCharacter(int row) {
			if (row >= 0 && row < list.size()) {
				return (CharacterWrapper) list.get(row);
			}
			return null;
		}

		public int getCharacterRow(CharacterWrapper character) {
			int row = 0;
			for (Iterator i = list.iterator(); i.hasNext();) {
				CharacterWrapper test = (CharacterWrapper) i.next();
				if (test.equals(character)) {
					return row;
				}
				row++;
			}
			return -1;
		}

		public int getRowCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		public int getColumnCount() {
			return columnName.length;
		}

		public String getColumnName(int column) {
			return columnName[column];
		}

		public Class getColumnClass(int column) {
			return columnClass[column];
		}

		public Object getValueAt(int row, int column) {
			String hostName = hostPrefs.getHostName();
			if (row < list.size()) {
				CharacterWrapper character = (CharacterWrapper) list.get(row);
				switch (column) {
					case 0:
						return character.hasPlayerPassword() ? LOCK_ICON : null;
					case 1:
						return character.getIcon();
					case 2:
						return character.getCharacterLevelName();
					case 3:
						String name = character.getPlayerName();
						if (name != null && name.equals(hostName) && !localGame) {
							return name + " (host)";
						}
						return name;
					case 4:
						boolean waiting = !game.getPlaceGoldSpecials() && !game.getGameStarted();
						return character.getGameStatus(waiting);
				}
			}
			return null;
		}
	}

	private class CharacterTableCellRenderer extends DefaultTableCellRenderer {
		public CharacterTableCellRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, val, isSel, hasFocus, row, col);
			setHorizontalAlignment(SwingConstants.CENTER);
			if (val instanceof ImageIcon) {
				setText("");
				setIcon((Icon) val);
			}
			else {
				setIcon(null);
			}
			return this;
		}
	}

	public GameWrapper getGame() {
		return game;
	}

	public HostPrefWrapper getHostPrefs() {
		return hostPrefs;
	}

	public boolean isHostPlayer() {
		return hostPlayer;
	}

	public void updateToolbarOptions(int inIconSize) {
		this.iconSize = inIconSize;
		for (Iterator i = characterFrames.values().iterator(); i.hasNext();) {
			CharacterFrame frame = (CharacterFrame) i.next();
			frame.actionPanel.modifyToolbarIconStyle(iconSize);
			frame.updateControls();
		}
	}

	public int getIconSize() {
		return iconSize;
	}

	public void cleanup() {
		removeAllCharacterFrames();
		if (client != null) {
			client.kill();
		}
		if (inspector != null) {
			inspector.cleanup();
		}
		RealmLogWindow.killSingleton();
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isOption(String key) {
		return getMainFrame().getRealmSpeakOptions().getOptions().getBoolean(key);
	}
}