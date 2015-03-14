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
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import com.robin.game.objects.*;
import com.robin.game.server.GameHost;
import com.robin.game.server.GameServer;
import com.robin.general.io.*;
import com.robin.general.swing.*;
import com.robin.general.util.RandomNumber;
import com.robin.general.util.RandomNumberType;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderFrame;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.components.GoldSpecialChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

import edu.stanford.ejalbert.BrowserLauncher;

public class RealmSpeakFrame extends JFrame {
	
	private static final String[][] CHARACTER_CARDS = {
		{"Amazon","amazon"},
		{"Berserker","berserker"},
		{"Black Knight","black_knight"},
		{"Captain","captain"},
		{"Druid","druid"},
		{"Dwarf","dwarf"},
		{"Elf","elf"},
		{"Magician","magician"},
		{"Pilgrim","pilgrim"},
		{"Sorceror","sorceror"},
		{"Swordsman","swordsman"},
		{"White Knight","white_knight"},
		{"Witch King","witch_king"},
		{"Witch","witch"},
		{"Wizard","wizard"},
		{"Woods Girl","woodsgirl"},
	};
	
	private static final String[] MISSIONS = {
		"Escort Party",
		"Food Ale",
	};
	
	private static final String[] SHORT_CAMPAIGNS = {
		"Pillage",
		"Raid",
	};
	
	private static final String[] LONG_CAMPAIGNS = {
		"Conquer",
		"War",
		"Revolt",
		"Quest",
	};
	
	private ActionEvent FRAME_EVENT = new ActionEvent(this,0,"");
	
	protected WindowLayoutManager windowLayoutManager;
	protected JDesktopPane desktop;
	protected JLabel status;
	
	protected GameHost host = null;
	protected RealmHostPanel realmHostFrame = null;
	protected RealmGameHandler gameHandler = null;
	
	protected File lastSaveGame = null;
	
	protected ArrayList<RealmSpeakInternalFrame> gameControlFrames;
	protected ArrayList<CharacterFrame> characterFrames;
	
	protected Integer characterFrameForceLayout = null;
	protected Integer mapForceLayout = null;
	
	protected FileManager exportHTMLFileManager;
	
	protected FileFilter saveGameFileFilter = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory() || (f.isFile() && f.getPath().endsWith("rsgame"));
		}

		public String getDescription() {
			return "RealmSpeak Save Files (*.rsgame)";
		}
	};
	
	protected JMenuBar menu;
		protected JMenu fileMenu;
			protected JMenuItem newGame;
			protected JMenuItem loadGame;
			protected JMenuItem restoreGame;
			protected JMenuItem restoreGameBirdsong;
			protected JMenuItem restartLastGame;
			
			protected JMenuItem saveCurrentGame;
			protected JMenuItem endCurrentGame;
			
			protected JMenuItem exportHTMLSummary;
			protected JMenuItem exportHTMLSummaryHighQuality;
			
			protected JMenuItem gamePlayOptions;
			
			protected JMenuItem exitRealmSpeak;
		protected JMenu networkMenu;
			protected JCheckBoxMenuItem networkingOption;
			protected JMenuItem joinNetworkGame;
			protected JMenuItem dropAllConnections;
		protected JMenu actionMenu;
			protected JMenuItem finishActionItem;
			protected JMenuItem backActionItem;
			protected JMenuItem hideActionItem;
			protected JMenuItem moveActionItem;
			protected JMenuItem searchActionItem;
			protected JMenuItem tradeActionItem;
			protected JMenuItem restActionItem;
			protected JMenuItem alertActionItem;
			protected JMenuItem hireActionItem;
			protected JMenuItem followActionItem;
			protected JMenuItem spellActionItem;
			protected JMenuItem peerActionItem;
			protected JMenuItem flyActionItem;
			protected JMenuItem remoteSpellActionItem;
			protected JMenuItem cacheActionItem;
			protected JMenuItem playNextActionItem;
			protected JMenuItem playAllActionItem;
		protected JMenu windowMenu;
			protected JMenuItem showConnectionsWindow;
			protected JMenuItem showHostPrefsView;
			protected JMenuItem[] organizeWindow;
		protected JMenu viewMenu;
			protected JMenu characterMenuView;
				protected JMenuItem[] characterCardView;
			protected JMenu missionMenuView;
				protected JMenuItem[] missionChitView;
			protected JMenuItem remodeledCounterKeyView;
			protected JMenuItem memoryView;
			protected JMenuItem viewHallOfFameView;
			protected JMenuItem viewGameLog;
			protected JMenu viewDieRollStatistics;
				protected JMenuItem viewDieRollSummary;
				protected JMenuItem viewAllDieRolls;
			protected JMenuItem viewSpellList;
		protected JMenu tablesMenuView;
			protected JMenuItem searchTables;
			protected JMenuItem meetingTable;
			protected JMenuItem commerceTable;
			protected JMenuItem missileTable;
			protected JMenuItem optionalCombatTables;
			protected JMenuItem revMissileTable;
			
			protected JMenuItem curseTable;
			protected JMenuItem wishTable;
			protected JMenuItem popTable;
			protected JMenuItem transformTable;
			protected JMenuItem lostTable;
			protected JMenuItem violentStormTable;
			
			protected JMenuItem enchantedMeadowTable;
			protected JMenuItem toadstoolCircleTable;
			protected JMenuItem cryptKnightTable;
			
			protected JMenuItem priceTreasuresTable;
			protected JMenuItem priceVisitorsTable;
			protected JMenuItem priceOtherTable;
			
			protected JMenuItem itemAndHorsesTable;
		protected JMenu expansionTablesMenuView;
			protected JMenuItem archaeologicalDigTable;
			protected JMenuItem fountainOfHealthTable;
			protected JMenuItem raiseDeadTable;
			protected JMenuItem summonAnimalTable;
			protected JMenuItem summonElementalTable;
			protected JMenuItem captureTable;
			
		protected JMenu expansionRulesView;
			protected JMenuItem generatorRules;
			protected JMenuItem travelerRules;
			protected JMenuItem guildRules;
			
		protected JMenu helpMenu;
//			protected JMenuItem gcHelp;
			protected JMenuItem launchCharacterEditorHelp;
			protected JMenuItem spurGameHelp;
			protected JMenuItem ruleCreditsHelp;
			protected JMenuItem licenseHelp;
			protected JMenuItem creditsHelp;
			protected JMenuItem aboutHelp;
	
	public RealmSpeakFrame() {
		initComponents();
		gameControlFrames = new ArrayList<RealmSpeakInternalFrame>();
		characterFrames = new ArrayList<CharacterFrame>();
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				int lastLayout = windowLayoutManager.getLastLayout();
				if (lastLayout==0) {
					organize();
				}
			}
 		});
		exportHTMLFileManager = new FileManager(this,"Export Destination",null);
		readFramePreferences();
	}
	
	private RealmSpeakOptions realmSpeakOptions;
	
	private void readFramePreferences() {
		realmSpeakOptions = new RealmSpeakOptions();
		realmSpeakOptions.apply(this);
		applyFrameSize(realmSpeakOptions.getOptions());
		
		if (networkingOption.isSelected()!=realmSpeakOptions.getOptions().getBoolean(RealmSpeakOptions.NETWORKING_OPTION)) {
			networkingOption.doClick();
		}
		String val = realmSpeakOptions.getOptions().get(RealmSpeakOptions.LAST_SAVE_LOCATION);
		if (val!=null) {
			lastSaveGame = new File(val);
		}
		val = realmSpeakOptions.getOptions().get(RealmSpeakOptions.LAST_EXPORT_LOCATION);
		if (val!=null) {
			exportHTMLFileManager.setCurrentDirectory(new File(val));
		}
	}
	public RealmSpeakOptions getRealmSpeakOptions() {
		return realmSpeakOptions;
	}
	protected void saveFramePreferences() {
		realmSpeakOptions.getOptions().set(RealmSpeakOptions.NETWORKING_OPTION,networkingOption.isSelected());
		captureFrameSize(realmSpeakOptions.getOptions());
		if (lastSaveGame!=null) {
			realmSpeakOptions.getOptions().set(RealmSpeakOptions.LAST_SAVE_LOCATION,lastSaveGame.getAbsolutePath());
		}
		if (exportHTMLFileManager.getCurrentDirectory()!=null) {
			realmSpeakOptions.getOptions().set(RealmSpeakOptions.LAST_EXPORT_LOCATION,exportHTMLFileManager.getCurrentDirectory().getAbsolutePath());
		}
		captureFrameSize(realmSpeakOptions.getOptions());
		realmSpeakOptions.save();
	}
	private void applyFrameSize(PreferenceManager prefMan) {
		Rectangle pref = ComponentTools.findPreferredRectangle();
		// Set game frame size
		int width = prefMan.getInt(RealmSpeakOptions.FRAME_WIDTH);
		if (width==0) width = pref.width;
		int height = prefMan.getInt(RealmSpeakOptions.FRAME_HEIGHT);
		if (height==0) height = pref.height;
		setSize(width,height);
		
		// Set game frame location
		if (prefMan.get(RealmSpeakOptions.FRAME_X)!=null) {
			int x = prefMan.getInt(RealmSpeakOptions.FRAME_X);
			int y = prefMan.getInt(RealmSpeakOptions.FRAME_Y);
			setLocation(x,y);
		}
		else {
			setLocation(pref.x,pref.y);
		}
	}
	protected void captureFrameSize(PreferenceManager prefMan) {
		// Capture mainFrame state
		prefMan.set(RealmSpeakOptions.FRAME_WIDTH,String.valueOf(getWidth()));
		prefMan.set(RealmSpeakOptions.FRAME_HEIGHT,String.valueOf(getHeight()));
		Point p = getLocation();
		prefMan.set(RealmSpeakOptions.FRAME_X,String.valueOf(p.x));
		prefMan.set(RealmSpeakOptions.FRAME_Y,String.valueOf(p.y));
	}
	private void showSpells() {
		FrameManager.showDefaultManagedFrame(this,new SpellViewer(getGameHandler().getClient().getGameData()),"Game Spells",null,false);
	}
	private void showImage(String title,String path) {
		ImageIcon icon = IconFactory.findIcon(path);
		FrameManager.showDefaultManagedFrame(RealmSpeakFrame.this,new JLabel(icon),title,null,false);
	}
	private void showRtf(String path) {
		StringBuffer sb = new StringBuffer();
		try {
			InputStream stream = ResourceFinder.getInputStream(path);
			if (stream==null) {
				throw new IOException("");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while((line=reader.readLine())!=null) {
				sb.append(line);
			}
		}
		catch(IOException ex) {
			sb.append("Resource not found: "+path);
		}
		JEditorPane detail = new JEditorPane("text/rtf",sb.toString());
		detail.setEditable(false);
		detail.setText(sb.toString());
		JScrollPane sp = new JScrollPane(detail);
		detail.setCaretPosition(0);
		ComponentTools.lockComponentSize(sp,800,600);
		JOptionPane.showMessageDialog(this,sp);
	}
	private void showMemory() {
		StringBuffer sb = new StringBuffer();
		long free = Runtime.getRuntime().freeMemory()/1000;
		long total = Runtime.getRuntime().totalMemory()/1000;
		long max = Runtime.getRuntime().maxMemory()/1000;
		long used = total-free;
		sb.append("Free="+free+" kb\n");
		sb.append("Total="+total+" kb\n");
		sb.append("Used="+used+" kb\n");
		sb.append("Max="+max+" kb\n");
		MessageMaster.showMessage(this,sb.toString(),"Current Memory",JOptionPane.INFORMATION_MESSAGE);
	}
	private void showHallOfFame() {
		HallOfFameView.showHallOfFame(this);
	}
	private void showRuleCredits() {
		ImageIcon icon = IconFactory.findIcon("images/tables/credits3e.gif");
		MessageMaster.showMessage(this, "","3rd Edition Rule Credits",JOptionPane.PLAIN_MESSAGE,icon);
	}
	private void showLicense() {
		StringBuffer text = new StringBuffer();
		text.append("<html><body><font size=\"-1\" face=\"Helvetical, Arial, sans-serif\">");
		text.append("RealmSpeak is the Java implementation of Avalon Hill's Magic Realm boardgame<br>");
		text.append("Copyright (C) 2010  Robin Warren<br>");
		text.append("<br>");
		text.append("Permission to use, copy, and distribute this software and its<br>");
		text.append("documentation for any purpose and without fee is hereby granted, provided<br>");
		text.append("that the above copyright notice appears in all copies and that both the<br>");
		text.append("copyright notice and this permission notice appear in supporting<br>");
		text.append("documentation, and that the same name not be used in advertising or<br>");
		text.append("publicity pertaining to distribution of the software without specific,<br>");
		text.append("written prior permission. We make no representations about the<br>");
		text.append("suitability this software for any purpose. It is provided \"as is\"<br>");
		text.append("without express or implied warranty.<br>");
		text.append("</font></body></html>");
		
		showHtmlWindow("RealmSpeak License",text.toString());
	}
	private void showCredits() {
		String fontName = "Dialog";
		Font header = new Font(fontName,Font.BOLD,22);
		Color headerColor = Color.green;
		Font subheader = new Font(fontName,Font.BOLD,18);
		Color subheaderColor = new Color(0,0,120);
		Font listing = new Font(fontName,Font.BOLD,16);
		Color listingColor = Color.black;
		
		ScrollingText scroller = new ScrollingText(IconFactory.findIcon("pending/logo/parchment.png"),new Insets(32,0,30,0));
		scroller.addLine(new ScrollLine("RealmSpeak",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("Design and Implementation",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Robin Warren",listing,listingColor,null,0,SwingConstants.CENTER,"http://realmspeak.dewkid.com/"));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Code Contributors",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Matt Gardner",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Quest Development and",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Questing the Realm",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Steve Schacher",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Book of Quests",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Jay Richardson",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Magic Realm the Boardgame",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("Publisher",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Avalon Hill 1979",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Original Game Design",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("and Development",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Richard Hamblen",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Original Components",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Monarch Services",listing,listingColor));
		scroller.addLine(new ScrollLine("Richard Hamblen",listing,listingColor));
		scroller.addLine(new ScrollLine("Kim Grommel",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Original Map Graphics",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("George Goebel",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Magic Realm Resources",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("3rd Edition Manual",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Teresa Michelsen",listing,listingColor));
		scroller.addLine(new ScrollLine("Stephen McKnight",listing,listingColor));
		scroller.addLine(new ScrollLine("Jay Richardson",listing,listingColor));
		scroller.addLine(new ScrollLine("Daniel W. Farrow, IV",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Tile Scans & Color Icons",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Dan Evans",listing,listingColor,null,0,SwingConstants.CENTER,"http://www.magicrealm.org/components"));
		scroller.addLine(new ScrollLine("Brian Winter",listing,listingColor,null,0,SwingConstants.CENTER,"http://www.thewinternet.com/magicrealm/"));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Combat Flow Charts",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Brian Winter",listing,listingColor,null,0,SwingConstants.CENTER,"http://www.thewinternet.com/magicrealm/"));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Redesigned Melee Sections",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Jay Richardson",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Remodeled Counter Layout",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Dan Evans",listing,listingColor));
		scroller.addLine(new ScrollLine("John Frenzel",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Websites",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Nand's Magic Realm",listing,listingColor,null,0,SwingConstants.CENTER,"http://www.nand.it/mr/"));
		scroller.addLine(new ScrollLine("Magic Realm Wiki - Peter Morris",listing,listingColor,null,0,SwingConstants.CENTER,"http://homenowned.com/wiki-mr/pmwiki.php"));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("RealmSpeak Playtesters",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("(no particular order)",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("David O",listing,listingColor));
		scroller.addLine(new ScrollLine("Eric Topp",listing,listingColor));
		scroller.addLine(new ScrollLine("Matt",listing,listingColor));
		scroller.addLine(new ScrollLine("Patrick van Beek",listing,listingColor));
		scroller.addLine(new ScrollLine("Peter Kathe",listing,listingColor));
		scroller.addLine(new ScrollLine("Scott DeMers",listing,listingColor));
		scroller.addLine(new ScrollLine("Steve McKnight",listing,listingColor));
		scroller.addLine(new ScrollLine("Teresa Michelsen",listing,listingColor));
		scroller.addLine(new ScrollLine("Vincent Lyon",listing,listingColor));
		scroller.addLine(new ScrollLine("gambulator",listing,listingColor));
		scroller.addLine(new ScrollLine("Daniel W. Farrow, IV",listing,listingColor));
		scroller.addLine(new ScrollLine("R. Robert Hentzel",listing,listingColor));
		scroller.addLine(new ScrollLine("James W Anderson III",listing,listingColor));
		scroller.addLine(new ScrollLine("Kyle Smith",listing,listingColor));
		scroller.addLine(new ScrollLine("Steve Schacher",listing,listingColor));
		scroller.addLine(new ScrollLine("Robert Godfrey",listing,listingColor));
		scroller.addLine(new ScrollLine("Silvestr",listing,listingColor));
		scroller.addLine(new ScrollLine("Yxklyx",listing,listingColor));
		scroller.addLine(new ScrollLine("Stephan Valkyser",listing,listingColor));
		scroller.addLine(new ScrollLine("(let me know if I missed anyone!)",listing,listingColor,null,0,SwingConstants.CENTER,"mailto:robin@dewkid.com"));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Game Expansions",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("Monster Expansion",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Glenn Pruitt",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Expansion One",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("David Stegemeyer",listing,listingColor));
		scroller.addLine(new ScrollLine("Robin Warren",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Expansion Monster Graphics",subheader,subheaderColor));
		scroller.addLine(new ScrollLine("Vrin Thomas",listing,listingColor,null,0,SwingConstants.CENTER,"http://www.facebook.com/vrin.thomas"));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Network Testing",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("David Stegemeyer",listing,listingColor));
		scroller.addLine(new ScrollLine("El Tigris",listing,listingColor));
		scroller.addLine(new ScrollLine("Ross Warren",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("AppleScripts",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("Daniel T",listing,listingColor));
		scroller.addLine(new ScrollLine("Jorge Arroyo",listing,listingColor));
		scroller.addLine(new ScrollLine("Don Limbaugh",listing,listingColor));
		scroller.addLine(new ScrollLine());
		scroller.addLine(new ScrollLine("Free Game Icons",header,headerColor,Color.black,2));
		scroller.addLine(new ScrollLine("molotov.nu",listing,listingColor));
		scroller.addLine(new ScrollLine("(sadly, no longer online)",listing,listingColor));
		scroller.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						URL url = ev.getURL();
						BrowserLauncher.openURL(url.toString());
					}
					catch(IOException ex) {
						// do nothing
					}
				}
			}
		});
		FrameManager.showDefaultManagedFrame(this,scroller,"RealmSpeak Credits",IconFactory.findIcon("pending/logo/rs_logo.png"),true);
		scroller.start();
	}
	private void showAbout() {
		StringBuffer text = new StringBuffer();
		text.append("<html><body><font face=\"Helvetical, Arial, sans-serif\">");
		text.append("<font size=\"+1\"><b>RealmSpeak Java Application</b></font><br>");
		text.append("<font size=\"+1\">Version "+Constants.REALM_SPEAK_VERSION+"</font></font><br><br>");
		text.append("<table>");
		text.append("<tr><td align=\"right\"><b>Compiled Java Version:</b></td><td border=1 align=\"center\" width=150>");
		text.append("1.5.0_19");
		text.append("</td></tr><tr><td align=\"right\"><b>Current Java Version:</b></td><td border=1 align=\"center\" width=150>");
		text.append(System.getProperty("java.version"));
		text.append("</td></tr><tr><td align=\"right\"><b>Processing Power:</b></td><td border=1 align=\"center\" width=150>");
		int cpus = Runtime.getRuntime().availableProcessors();
		text.append(cpus+" CPU"+(cpus==1?"":"s"));
		text.append("</td></tr><tr><td align=\"right\"><b>Random Number Generator:</b></td><td border=1 align=\"center\" width=150>");
		text.append(RandomNumber.getRandomNumberGenerator().toString());
		text.append("</td></tr></table>");
		text.append("</body></html>");
		
		showHtmlWindow("About RealmSpeak",text.toString());
	}
	private void showHtmlWindow(String title,String text) {
		JPanel panel = new JPanel(new BorderLayout());
		JEditorPane pane = new JEditorPane("text/html",text) {
			public boolean isFocusTraversable() {
				return false;
			}
		};
		pane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						URL url = e.getURL();
						BrowserLauncher.openURL(url.toString());
					}
					catch(IOException ex) {
						// do nothing
					}
				}
			}
		});
		pane.setEditable(false);
		pane.setOpaque(false);
		panel.add(pane,BorderLayout.CENTER);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JButton creditsButton = new JButton("RealmSpeak Credits...");
		creditsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				showCredits();
			}
		});
		box.add(creditsButton);
		panel.add(box,BorderLayout.SOUTH);
		
		FrameManager.showDefaultManagedFrame(this,panel,title,IconFactory.findIcon("images/logo/realmbox.jpg"),true);
	}
	private void updateControls() {
		boolean gameInProgress = realmHostFrame!=null;
		boolean joinedGame = gameHandler!=null;
		boolean inBirdsong = joinedGame && (gameHandler.getGame()==null || gameHandler.getGame().getState()==GameWrapper.GAME_STATE_RECORDING);
		boolean isAutosave = RealmHostPanel.AUTOSAVEFILE.exists();
		
		showConnectionsWindow.setEnabled(gameInProgress && !realmHostFrame.isLocal());
		dropAllConnections.setEnabled(gameInProgress && !realmHostFrame.isLocal());
		
		showHostPrefsView.setEnabled(gameInProgress || joinedGame);
		viewSpellList.setEnabled(joinedGame);
		
		networkingOption.setEnabled(!joinedGame && !gameInProgress);
		newGame.setEnabled(!joinedGame && !gameInProgress);
		loadGame.setEnabled(!joinedGame && !gameInProgress);
		restartLastGame.setEnabled(!joinedGame && !gameInProgress && isAutosave);
		restoreGame.setEnabled(!joinedGame && !gameInProgress && isAutosave);
		restoreGameBirdsong.setEnabled(!joinedGame && !gameInProgress && isAutosave);
		
		saveCurrentGame.setEnabled(gameInProgress);
		
		endCurrentGame.setEnabled(gameInProgress && inBirdsong);
		
		exportHTMLSummary.setEnabled(gameInProgress || joinedGame);
		exportHTMLSummaryHighQuality.setEnabled(gameInProgress || joinedGame);
		
		spurGameHelp.setEnabled(gameInProgress);
		launchCharacterEditorHelp.setEnabled(!joinedGame && !gameInProgress);
		
		joinNetworkGame.setEnabled(!joinedGame);
	}
	public void updateMenuActions() {
		CharacterActionControlManager acm = getFrontActionControlManager();
		if (acm!=null) {
			finishActionItem.setEnabled(acm.finishAction.isEnabled());
			backActionItem.setEnabled(acm.backAction.isEnabled());
			hideActionItem.setEnabled(acm.hideAction.isEnabled());
			moveActionItem.setEnabled(acm.moveAction.isEnabled());
			searchActionItem.setEnabled(acm.searchAction.isEnabled());
			tradeActionItem.setEnabled(acm.tradeAction.isEnabled());
			restActionItem.setEnabled(acm.restAction.isEnabled());
			alertActionItem.setEnabled(acm.alertAction.isEnabled());
			hireActionItem.setEnabled(acm.hireAction.isEnabled());
			followActionItem.setEnabled(acm.followAction.isEnabled());
			spellActionItem.setEnabled(acm.spellAction.isEnabled());
			peerActionItem.setEnabled(acm.peerAction.isEnabled());
			flyActionItem.setEnabled(acm.flyAction.isEnabled());
			remoteSpellActionItem.setEnabled(acm.remoteSpellAction.isEnabled());
			cacheActionItem.setEnabled(acm.cacheAction.isEnabled());
		}
		else {
			finishActionItem.setEnabled(false);
			backActionItem.setEnabled(false);
			hideActionItem.setEnabled(false);
			moveActionItem.setEnabled(false);
			searchActionItem.setEnabled(false);
			tradeActionItem.setEnabled(false);
			restActionItem.setEnabled(false);
			alertActionItem.setEnabled(false);
			hireActionItem.setEnabled(false);
			followActionItem.setEnabled(false);
			spellActionItem.setEnabled(false);
			peerActionItem.setEnabled(false);
			flyActionItem.setEnabled(false);
			remoteSpellActionItem.setEnabled(false);
			cacheActionItem.setEnabled(false);
		}
		
		RealmTurnPanel rtp = getFrontRealmTurnPanel();
		if (rtp!=null) {
			boolean actionsLeft = rtp.hasActionsLeft();
			playNextActionItem.setEnabled(actionsLeft);
			playAllActionItem.setEnabled(actionsLeft);
		}
		else {
			playNextActionItem.setEnabled(false);
			playAllActionItem.setEnabled(false);
		}
	}
	public void initComponents() {
		setTitle(Constants.APPLICATION_NAME);
		desktop = new JDesktopPane();
		windowLayoutManager = new WindowLayoutManager(this,desktop);
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(desktop,"Center");
			status = new JLabel(" ");
			status.setOpaque(true);
			status.setDoubleBuffered(true);
		getContentPane().add(status,"South");		
		menu = new JMenuBar();
			fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
				newGame = new JMenuItem("New game");
				newGame.setMnemonic(KeyEvent.VK_N);
				newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
				newGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						hostGame(networkingOption.isSelected());
					}
				});
			fileMenu.add(newGame);
				loadGame = new JMenuItem("Load game");
				loadGame.setMnemonic(KeyEvent.VK_L);
				loadGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK));
				loadGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						loadHostGame(networkingOption.isSelected());
					}
				});
			fileMenu.add(loadGame);
				restoreGame = new JMenuItem("Restore game from autosave");
				restoreGame.setMnemonic(KeyEvent.VK_R);
				restoreGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
				restoreGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						loadHostGame(RealmHostPanel.AUTOSAVEFILE,networkingOption.isSelected());
					}
				});
			fileMenu.add(restoreGame);
				restoreGameBirdsong = new JMenuItem("Restore game from autosave (BIRDSONG)");
				restoreGameBirdsong.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						loadHostGame(RealmHostPanel.AUTOSAVEFILE_BIRDSONG,networkingOption.isSelected());
					}
				});
			fileMenu.add(restoreGameBirdsong);
			fileMenu.add(new JSeparator());
				restartLastGame = new JMenuItem("Restart last game");
				restartLastGame.setMnemonic(KeyEvent.VK_R);
				restartLastGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						int ret = JOptionPane.showConfirmDialog(
								RealmSpeakFrame.this,
								"Restart last game will start the game over from the beginning.\nYou will lose your autosave, so be sure to save before restarting.\n\nContinue with restart?",
								"Restart Last Game",
								JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
						if (ret==JOptionPane.YES_OPTION) {
							loadHostGame(RealmHostPanel.INITIALSAVEFILE,networkingOption.isSelected());
						}
					}
				});
			fileMenu.add(restartLastGame);
			fileMenu.add(new JSeparator());
				saveCurrentGame = new JMenuItem("Save current game");
				saveCurrentGame.setMnemonic(KeyEvent.VK_S);
				saveCurrentGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
				saveCurrentGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						saveHostGame();
					}
				});
			fileMenu.add(saveCurrentGame);
				endCurrentGame = new JMenuItem("End current game");
//				endGameFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,InputEvent.CTRL_MASK));
				endCurrentGame.setMnemonic(KeyEvent.VK_E);
				endCurrentGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						shutDownGame();
					}
				});
			fileMenu.add(endCurrentGame);
			fileMenu.add(new JSeparator());
				exportHTMLSummary = new JMenuItem("Export HTML Game Summary...");
				exportHTMLSummary.setMnemonic(KeyEvent.VK_H);
				exportHTMLSummary.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						File dest = exportHTMLFileManager.getSaveDirectory("Export HTML Game Summary");
						if (dest!=null) {
							DayKey dayKey = new DayKey(gameHandler.getGame().getMonth(),gameHandler.getGame().getDay());
							String folderName = JOptionPane.showInputDialog("Folder Name:","RSGameSummary_"+dayKey.getReadable());
							if (folderName!=null) {
								GameHtmlGenerator generator = new GameHtmlGenerator(gameHandler.getClient().getGameData(),RealmLogWindow.getSingleton().getHtmlString(),false);
								String path = dest.getAbsolutePath()+File.separator+folderName;
								generator.saveHtml(path);
								JOptionPane.showMessageDialog(RealmSpeakFrame.this,"Export Done:\n\n     "+path);
							}
						}
					}
				});
			fileMenu.add(exportHTMLSummary);
				exportHTMLSummaryHighQuality = new JMenuItem("Export HTML Game Summary (High Quality)...");
				exportHTMLSummaryHighQuality.setMnemonic(KeyEvent.VK_H);
				exportHTMLSummaryHighQuality.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						File dest = exportHTMLFileManager.getSaveDirectory("Export HTML Game Summary");
						if (dest!=null) {
							DayKey dayKey = new DayKey(gameHandler.getGame().getMonth(),gameHandler.getGame().getDay());
							String folderName = JOptionPane.showInputDialog("Folder Name:","RSGameSummary_"+dayKey.getReadable());
							if (folderName!=null) {
								GameHtmlGenerator generator = new GameHtmlGenerator(gameHandler.getClient().getGameData(),RealmLogWindow.getSingleton().getHtmlString(),true);
								String path = dest.getAbsolutePath()+File.separator+folderName;
								generator.saveHtml(path);
								JOptionPane.showMessageDialog(RealmSpeakFrame.this,"Export Done:\n\n     "+path);
							}
						}
					}
				});				
			fileMenu.add(exportHTMLSummaryHighQuality);
			fileMenu.add(new JSeparator());
				gamePlayOptions = new JMenuItem("Options...");
				gamePlayOptions.setMnemonic(KeyEvent.VK_O);
				gamePlayOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK));
				gamePlayOptions.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						RealmSpeakOptionPanel panel = new RealmSpeakOptionPanel(RealmSpeakFrame.this,realmSpeakOptions);
						panel.setVisible(true);
					}
				});				
			fileMenu.add(gamePlayOptions);
			fileMenu.add(new JSeparator());
				exitRealmSpeak = new JMenuItem("Exit");
				exitRealmSpeak.setMnemonic(KeyEvent.VK_X);
				exitRealmSpeak.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						close();
					}
				});
			fileMenu.add(exitRealmSpeak);
		menu.add(fileMenu);
			networkMenu = new JMenu("Network");
			networkMenu.setMnemonic(KeyEvent.VK_N);
				networkingOption = new JCheckBoxMenuItem("Network Hosting is OFF",false);
				networkingOption.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						if (networkingOption.isSelected()) {
							networkingOption.setText("Network Hosting is ON");
							setTitle(Constants.APPLICATION_NAME+" - Networking Active");
						}
						else {
							networkingOption.setText("Network Hosting is OFF");
							setTitle(Constants.APPLICATION_NAME);
						}
					}
				});
			networkMenu.add(networkingOption);
			networkMenu.add(new JSeparator());
				joinNetworkGame = new JMenuItem("Join game...");
				joinNetworkGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,InputEvent.CTRL_MASK));
				joinNetworkGame.setMnemonic(KeyEvent.VK_J);
				joinNetworkGame.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						joinGame();
					}
				});
			networkMenu.add(joinNetworkGame);
				dropAllConnections = new JMenuItem("Drop all connections");
				dropAllConnections.setMnemonic(KeyEvent.VK_D);
				dropAllConnections.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						if (JOptionPane.showConfirmDialog(
								RealmSpeakFrame.this,
								"Are you sure you want to drop all connections?  Players will be able to reconnect afterwards.",
								"Drop all connections",
								JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
							realmHostFrame.dropAllConnections();
						}
					}
				});
			networkMenu.add(dropAllConnections);
		menu.add(networkMenu);
			actionMenu = new JMenu("Action");
				finishActionItem = new JMenuItem("Finish");
				finishActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
				finishActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.finishAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(finishActionItem);
				backActionItem = new JMenuItem("Back");
				backActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0));
				backActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.backAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(backActionItem);
			actionMenu.add(new JSeparator());
				hideActionItem = new JMenuItem("Hide");
				hideActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,0));
				hideActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.hideAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(hideActionItem);
				moveActionItem = new JMenuItem("Move");
				moveActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,0));
				moveActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.moveAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(moveActionItem);
				searchActionItem = new JMenuItem("Search");
				searchActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,0));
				searchActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.searchAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(searchActionItem);
				tradeActionItem = new JMenuItem("Trade");
				tradeActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
				tradeActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.tradeAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(tradeActionItem);
				restActionItem = new JMenuItem("Rest");
				restActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,0));
				restActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.restAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(restActionItem);
				alertActionItem = new JMenuItem("Alert");
				alertActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,0));
				alertActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.alertAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(alertActionItem);
				hireActionItem = new JMenuItem("Hire");
				hireActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,0));
				hireActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.hireAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(hireActionItem);
				followActionItem = new JMenuItem("Follow");
				followActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,0));
				followActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.followAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(followActionItem);
				spellActionItem = new JMenuItem("Spell");
				spellActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,0));
				spellActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.spellAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(spellActionItem);
				peerActionItem = new JMenuItem("Peer");
				peerActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,0));
				peerActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.peerAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(peerActionItem);
				flyActionItem = new JMenuItem("Fly");
				flyActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,0));
				flyActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.flyAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(flyActionItem);
				remoteSpellActionItem = new JMenuItem("Remote Spell");
				remoteSpellActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,0));
				remoteSpellActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.remoteSpellAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(remoteSpellActionItem);
				cacheActionItem = new JMenuItem("Cache");
				cacheActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,0));
				cacheActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						CharacterActionControlManager acm = getFrontActionControlManager();
						if (acm!=null) {
							acm.cacheAction.actionPerformed(FRAME_EVENT);
						}
					}
				});
			actionMenu.add(cacheActionItem);
			actionMenu.add(new JSeparator());
				playNextActionItem = new JMenuItem("Play Next");
				playNextActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,0));
				playNextActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						RealmTurnPanel rtp = getFrontRealmTurnPanel();
						if (rtp!=null) {
							rtp.pressPlayNext();
						}
					}
				});
			actionMenu.add(playNextActionItem);
				playAllActionItem = new JMenuItem("Play All");
				playAllActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,0));
				playAllActionItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						RealmTurnPanel rtp = getFrontRealmTurnPanel();
						if (rtp!=null) {
							rtp.pressPlayAll();
						}
					}
				});
			actionMenu.add(playAllActionItem);
		menu.add(actionMenu);
			windowMenu = new JMenu("Window");
			windowMenu.setMnemonic(KeyEvent.VK_W);
				showConnectionsWindow = new JMenuItem("Show Connections");
				showConnectionsWindow.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showConnections();
					}
				});
				organizeWindow = new JMenuItem[3];
				organizeWindow[0] = new JMenuItem("Standard Layout");
				organizeWindow[0].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						mapForceLayout = null;
						characterFrameForceLayout = null;
						organize();
					}
				});
				organizeWindow[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,InputEvent.CTRL_MASK));
				organizeWindow[1] = new JMenuItem("Maximized Layout");
				organizeWindow[1].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						mapForceLayout = new Integer((int)desktop.getSize().getWidth()-500);
						characterFrameForceLayout = new Integer(500);
						organize();
					}
				});
				organizeWindow[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,InputEvent.CTRL_MASK));
				organizeWindow[2] = new JMenuItem("Full Map Layout");
				organizeWindow[2].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						mapForceLayout = new Integer((int)desktop.getSize().getWidth());
						characterFrameForceLayout = null;
						organize();
						if (gameHandler!=null && gameHandler.getInspector()!=null) {
							gameHandler.getInspector().toFront();
						}
					}
				});
				organizeWindow[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,InputEvent.CTRL_MASK));
			updateWindowMenu();
		menu.add(windowMenu);
			viewMenu = new JMenu("View");
			viewMenu.setMnemonic(KeyEvent.VK_V);
				characterMenuView = new JMenu("Characters");
				characterCardView = new JMenuItem[CHARACTER_CARDS.length];
				for (int i=0;i<CHARACTER_CARDS.length;i++) {
					characterCardView[i] = new ShowCharCardViewAction(CHARACTER_CARDS[i]);
					characterMenuView.add(characterCardView[i]);
				}
			viewMenu.add(characterMenuView);
				viewSpellList = new JMenuItem("Game Spells");
				viewSpellList.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showSpells();
						}
					});
			viewMenu.add(viewSpellList);
				missionMenuView = new JMenu("Missions and Campaigns");
				
				missionChitView = new JMenuItem[MISSIONS.length+SHORT_CAMPAIGNS.length+LONG_CAMPAIGNS.length];
				int n=0;
				for (int i=0;i<MISSIONS.length;i++) {
					missionChitView[n] = new ShowGoldSpecialViewAction(this,MISSIONS[i]);
					missionMenuView.add(missionChitView[n]);
					n++;
				}
				missionMenuView.add(new JSeparator());
				for (int i=0;i<SHORT_CAMPAIGNS.length;i++) {
					missionChitView[n] = new ShowGoldSpecialViewAction(this,SHORT_CAMPAIGNS[i]);
					missionMenuView.add(missionChitView[n]);
					n++;
				}
				missionMenuView.add(new JSeparator());
				for (int i=0;i<LONG_CAMPAIGNS.length;i++) {
					missionChitView[n] = new ShowGoldSpecialViewAction(this,LONG_CAMPAIGNS[i]);
					missionMenuView.add(missionChitView[n]);
					n++;
				}
			viewMenu.add(missionMenuView);
				tablesMenuView = new JMenu("Tables");
				tablesMenuView.setMnemonic(KeyEvent.VK_T);
					searchTables = new JMenuItem("Search");
					searchTables.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Search Tables","images/tables/search.gif");
						}
					});
					searchTables.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(searchTables);
					meetingTable = new JMenuItem("Meeting");
					meetingTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Meeting Table","images/tables/meeting.gif");
						}
					});
					meetingTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(meetingTable);
					commerceTable = new JMenuItem("Commerce");
					commerceTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Commerce Table","images/tables/commerce.gif");
						}
					});
					commerceTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(commerceTable);
					missileTable = new JMenuItem("Missile");
					missileTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Missile Table","images/tables/missile.gif");
						}
					});
				tablesMenuView.add(missileTable);
					optionalCombatTables = new JMenuItem("Opt Missile/Fumble/Stumble");
					optionalCombatTables.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Optional Missile/Fumble/Stumble Tables","images/tables/optionalcombat.gif");
						}
					});
				tablesMenuView.add(optionalCombatTables);
					revMissileTable = new JMenuItem("Revised Missile");
					revMissileTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Revised Missile Table","images/tables/revisedmissile.gif");
						}
					});
				tablesMenuView.add(revMissileTable);
				tablesMenuView.add(new JSeparator());
					curseTable = new JMenuItem("Curses");
					curseTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Curse Table","images/tables/curse.gif");
						}
					});
				tablesMenuView.add(curseTable);
					wishTable = new JMenuItem("Wishes");
					wishTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Wishes Table","images/tables/smallblessing.gif");
						}
					});
					wishTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(wishTable);
					popTable = new JMenuItem("Power of the Pit");
					popTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Power of the Pit Table","images/tables/powerofthepit.gif");
						}
					});
					popTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(popTable);
					transformTable = new JMenuItem("Transform");
					transformTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Transform Table","images/tables/transform.gif");
						}
					});
					transformTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(transformTable);
					lostTable = new JMenuItem("Lost");
					lostTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Lost Table","images/tables/lost.gif");
						}
					});
				tablesMenuView.add(lostTable);
					violentStormTable = new JMenuItem("Violent Storm");
					violentStormTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Violent Storm Table","images/tables/violentstorm.gif");
						}
					});
				tablesMenuView.add(violentStormTable);
				tablesMenuView.add(new JSeparator());
					enchantedMeadowTable = new JMenuItem("Enchanted Meadow");
					enchantedMeadowTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Enchanted Meadow Table","images/tables/enchantedmeadow.gif");
						}
					});
				tablesMenuView.add(enchantedMeadowTable);
					toadstoolCircleTable = new JMenuItem("Toadstool Circle");
					toadstoolCircleTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Toadstool Circle Table","images/tables/toadstoolcircle.gif");
						}
					});
				tablesMenuView.add(toadstoolCircleTable);
					cryptKnightTable = new JMenuItem("Crypt of the Knight");
					cryptKnightTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Crypt of the Knight Table","images/tables/cryptoftheknight.gif");
						}
					});
				tablesMenuView.add(cryptKnightTable);
				tablesMenuView.add(new JSeparator());
					priceTreasuresTable = new JMenuItem("Treasure Prices");
					priceTreasuresTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Treasure Prices","images/tables/priceTreasure.gif");
						}
					});
				tablesMenuView.add(priceTreasuresTable);
					priceVisitorsTable = new JMenuItem("Visitor Prices");
					priceVisitorsTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Visitor Prices","images/tables/priceVisitors.gif");
						}
					});
				tablesMenuView.add(priceVisitorsTable);
					priceOtherTable = new JMenuItem("Other Prices");
					priceOtherTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Other Prices","images/tables/priceOther.gif");
						}
					});
				tablesMenuView.add(priceOtherTable);
				tablesMenuView.add(new JSeparator());
					itemAndHorsesTable = new JMenuItem("Items and Horses");
					itemAndHorsesTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Items and Horses Table","images/tables/itemsandhorses.gif");
						}
					});
					itemAndHorsesTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
				tablesMenuView.add(itemAndHorsesTable);
			viewMenu.add(tablesMenuView);
				expansionTablesMenuView = new JMenu("Expansion Tables");
				expansionTablesMenuView.setMnemonic(KeyEvent.VK_E);
					archaeologicalDigTable = new JMenuItem("Archaeological Dig");
					archaeologicalDigTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Archaeological Dig","images/tables/archaeologicaldig.gif");
						}
					});
				expansionTablesMenuView.add(archaeologicalDigTable);
					fountainOfHealthTable = new JMenuItem("Fountain of Health");
					fountainOfHealthTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Fountain of Health","images/tables/fountainofhealth.gif");
						}
					});
				expansionTablesMenuView.add(fountainOfHealthTable);
				expansionTablesMenuView.add(new JSeparator());
					raiseDeadTable = new JMenuItem("Raise Dead");
					raiseDeadTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Raise Dead","images/tables/raisedead.gif");
						}
					});
				expansionTablesMenuView.add(raiseDeadTable);
					summonAnimalTable = new JMenuItem("Summon Animal");
					summonAnimalTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Summon Animal","images/tables/summonanimal.gif");
						}
					});
				expansionTablesMenuView.add(summonAnimalTable);
					summonElementalTable = new JMenuItem("Summon Elemental");
					summonElementalTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Summon Elemental","images/tables/summonelemental.gif");
						}
					});
				expansionTablesMenuView.add(summonElementalTable);
				expansionTablesMenuView.add(new JSeparator());
					captureTable = new JMenuItem("Capture Traveler");
					captureTable.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showImage("Capture Traveler","pending/tables/capture.gif");
						}
					});
				expansionTablesMenuView.add(captureTable);
			viewMenu.add(expansionTablesMenuView);
				expansionRulesView = new JMenu("Expansion Rules");
					generatorRules = new JMenuItem("Monster Generators");
					generatorRules.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showRtf("rules/ExpansionGenerators.rtf");
						}
					});
				expansionRulesView.add(generatorRules);
					travelerRules = new JMenuItem("Travelers");
					travelerRules.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showRtf("rules/Travelers.rtf");
						}
					});
				expansionRulesView.add(travelerRules);
					guildRules = new JMenuItem("Guilds");
					guildRules.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							showRtf("rules/Guilds.rtf");
						}
					});
				expansionRulesView.add(guildRules);
			viewMenu.add(expansionRulesView);
				remodeledCounterKeyView = new JMenuItem("Key to Remodeled Counters");
				remodeledCounterKeyView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showImage("Key to Remodeled Counters","images/help/counters/sample.gif");
					}
				});
			viewMenu.add(remodeledCounterKeyView);
			viewMenu.add(new JSeparator());
				memoryView = new JMenuItem("Memory");
				memoryView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showMemory();
					}
				});
			viewMenu.add(memoryView);
				viewHallOfFameView = new JMenuItem("Hall of Fame");
				viewHallOfFameView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showHallOfFame();
					}
				});
			viewMenu.add(viewHallOfFameView);
				showHostPrefsView = new JMenuItem("Current Game Options");
				showHostPrefsView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						getGameHandler().showHostPrefs();
					}
				});
			viewMenu.add(showHostPrefsView);
				viewGameLog = new JMenuItem("Game Log");
				viewGameLog.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						if (gameHandler!=null) {
							gameHandler.toggleLog();
						}
					}
				});
				viewGameLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,0));
			viewMenu.add(viewGameLog);
				viewDieRollStatistics = new JMenu("Die Roll Statistics");
					viewDieRollSummary = new JMenuItem("Die Roll Summary");
					viewDieRollSummary.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
					viewDieRollSummary.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							if (gameHandler!=null) {
								DieRollerLog log = RealmUtility.getDieRollerLog(gameHandler.getClient().getGameData());
								JTextArea area = new JTextArea(log.getStandardReport(false,true));
								area.setWrapStyleWord(true);
								area.setLineWrap(true);
								area.setEditable(false);
								JScrollPane sp = new JScrollPane(area);
								ComponentTools.lockComponentSize(sp,600,600);
								JOptionPane.showMessageDialog(RealmSpeakFrame.this,sp,"Current Game Die Roll Summary",JOptionPane.INFORMATION_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(RealmSpeakFrame.this,"There is no game running.","Current Game Die Statistics",JOptionPane.INFORMATION_MESSAGE);
							}
						}
					});
				viewDieRollStatistics.add(viewDieRollSummary);
					viewAllDieRolls = new JMenuItem("All Die Rolls");
					viewAllDieRolls.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
					viewAllDieRolls.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							if (gameHandler!=null) {
								DieRollerLog log = RealmUtility.getDieRollerLog(gameHandler.getClient().getGameData());
								JTextArea area = new JTextArea(log.getAllDieRolls());
								area.setWrapStyleWord(true);
								area.setLineWrap(true);
								area.setEditable(false);
								JScrollPane sp = new JScrollPane(area);
								ComponentTools.lockComponentSize(sp,600,600);
								JOptionPane.showMessageDialog(RealmSpeakFrame.this,sp,"All Current Game Die Rolls",JOptionPane.INFORMATION_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(RealmSpeakFrame.this,"There is no game running.","Current Game Die Statistics",JOptionPane.INFORMATION_MESSAGE);
							}
						}
					});
				viewDieRollStatistics.add(viewAllDieRolls);
			viewMenu.add(viewDieRollStatistics);
//				generateGameSummary = new JMenuItem("Generate Game Summary");
//				generateGameSummary.addActionListener(new ActionListener() {
//						public void actionPerformed(ActionEvent ev) {
//							if (gameHandler!=null) {
//								StringBuffer sb = new StringBuffer();
//								for (String note:gameHandler.getNotes()) {
//									sb.append(note);
//									sb.append("\n");
//								}
//								JOptionPane.showMessageDialog(RealmSpeakFrame.this,sb.toString());
//							}
//						}
//				});
//			viewMenu.add(generateGameSummary);
		menu.add(viewMenu);
			helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
				spurGameHelp = new JMenuItem("Spur Game (try this if game seems to freeze)");
				spurGameHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						spurGame();
					}
				});
			helpMenu.add(spurGameHelp);
			helpMenu.add(new JSeparator());
				launchCharacterEditorHelp = new JMenuItem("Launch Character Builder (closes RealmSpeak)");
				launchCharacterEditorHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						setVisible(false);
						dispose();
						RealmCharacterBuilderFrame.main(null);
					}
				});
			helpMenu.add(launchCharacterEditorHelp);
			helpMenu.add(new JSeparator());
				ruleCreditsHelp = new JMenuItem("3rd Edition Rule Credits");
				ruleCreditsHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showRuleCredits();
					}
				});
			helpMenu.add(ruleCreditsHelp);
				licenseHelp = new JMenuItem("RealmSpeak License");
				licenseHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showLicense();
					}
				});
			helpMenu.add(licenseHelp);
				creditsHelp = new JMenuItem("RealmSpeak Credits");
				creditsHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showCredits();
					}
				});
			helpMenu.add(creditsHelp);
				aboutHelp = new JMenuItem("About RealmSpeak");
				aboutHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						showAbout();
					}
				});
			helpMenu.add(aboutHelp);
		menu.add(helpMenu);
		
		setJMenuBar(menu);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				close();
			}
		});
		
		updateControls();
		updateMenuActions();
	}
	/**
	 * An attempt to get the game going again, if it gets stuck.
	 */
	public void spurGame() {
		if (host!=null) {
			GameWrapper game = GameWrapper.findGame(gameHandler.getClient().getGameData());
			game.getGameObject().bumpVersion();
			gameHandler.submitChanges();
			gameHandler.updateCharacterList(); // Guarantees the frames will get updated
		}
	}
	private void loadHostGame(boolean netConnect) {
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
		if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
			lastSaveGame = FileUtilities.fixFileExtension(chooser.getSelectedFile(),".rsgame");
			loadHostGame(lastSaveGame,netConnect);
		}
	}
	private void loadHostGame(File path,boolean netConnect) {
		RealmUtility.resetGame();
		if (path.exists()) {
			showStatus("Loading host game..."+path.getPath());
			GameData data = new GameData();
			data.zipFromFile(path);
			RealmLogWindow.getSingleton().load(path);
			// test version here!!
			GameWrapper game = GameWrapper.findGame(data);
			if (game!=null) {
				String changedFrom = game.getVersionChangedFrom();
				if (!Constants.REALM_SPEAK_VERSION.equals(game.getVersion())) {
					StringBuffer message = new StringBuffer();
					message.append("The save file version is ");
					message.append(game.getVersion());
					message.append(" which is different with the current version ");
					message.append(Constants.REALM_SPEAK_VERSION);
					message.append(" of RealmSpeak.\n\n");
					message.append("Do you wish to continue using this file anyway, despite the chance you may encounter version-related bugs?");
					int ret = QuietOptionPane.showConfirmDialog(
							this,
							message.toString(),
							"Non-matching Versions",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE,
							"I don't want to see this warning again",
							true);
					
					if (ret==JOptionPane.YES_OPTION) {
						if (changedFrom==null) { // only update this, if its the first time the version was changed
							game.setVersionChangedFrom(game.getVersion());
						}
						game.setIgnoreVersionChange(QuietOptionPane.isLastWasSilenced());
						game.setVersion(Constants.REALM_SPEAK_VERSION);
						RealmLogWindow.getSingleton().addMessage("Host","Original version "+game.getVersionChangedFrom()+", changed to "+game.getVersion());
					}
					else {
						showStatus("Non-matching Versions");
						return; 
					}
				}
				else if (changedFrom!=null && !game.isIgnoreVersionChange()) {
					StringBuffer message = new StringBuffer();
					message.append("You are loading a save file (initially version ");
					message.append(changedFrom);
					message.append(") that was used by more than one version of RealmSpeak.");
					message.append("\n\nNote that you may encounter version-related bugs with this file.");
					QuietOptionPane.showMessageDialog(
							this,
							message,
							"Non-matching Versions",
							JOptionPane.WARNING_MESSAGE,
							null,
							"I don't want to see this warning again",
							true);
					if (QuietOptionPane.isLastWasSilenced()) {
						game.setIgnoreVersionChange(true);
					}
				}
			}
			
			RealmUtility.getDieRollerLog(data); // Make sure this exists (important only for legacy games!)
			
			// tag all non-host characters as missing in action
			// If playing a local game, convert all non-host characters to local
			HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
			String hostName = hostPrefs.getHostName();
			GamePool pool = new GamePool(data.getGameObjects());
			Collection characterGameObjects = pool.extract(CharacterWrapper.getKeyVals());
			for (Iterator i=characterGameObjects.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				CharacterWrapper character = new CharacterWrapper(go);
				if (!character.getPlayerName().equals(hostName) && character.isActive()) {
					if (netConnect) {
						// Set missing in action (show as offline)
						character.setMissingInAction(true);
						Collection minions = character.getMinions();
						if (minions!=null) {
							for (Iterator m=minions.iterator();m.hasNext();) {
								GameObject minion = (GameObject)m.next();
								CharacterWrapper lostMinion = new CharacterWrapper(minion);
								lostMinion.setMissingInAction(true);
							}
						}
					}
					else {
						// Simply transfer to host
						character.setPlayerName(hostName);
						//character.setPlayerEmail(""); // leave e-mail alone
					}
				}
			}
			
			showStatus("Loading game data...");
			RealmLoader loader = new RealmLoader();
			makeHost(loader.getMaster(),data,netConnect);
			resetStatus();
		}
		else {
			JOptionPane.showMessageDialog(this,"File not found:  "+lastSaveGame.getPath());
		}
	}
	private void saveHostGame() {
		JFileChooser chooser;
		if (lastSaveGame!=null) {
			String filePath = FileUtilities.getFilePathString(lastSaveGame,false,false);
			chooser = new JFileChooser(new File(filePath));
			chooser.setSelectedFile(lastSaveGame);
		}
		else {
			chooser = new JFileChooser();
			chooser.setSelectedFile(new File("RealmSpeakSave.rsgame"));
		}
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(saveGameFileFilter);
		if (chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			lastSaveGame = FileUtilities.fixFileExtension(chooser.getSelectedFile(),".rsgame");
			showStatus("Saving host game..."+lastSaveGame.getPath());
			host.getGameData().zipToFile(lastSaveGame);
			RealmLogWindow.getSingleton().save(lastSaveGame);
			resetStatus();
		}
	}
	public JDesktopPane getDesktop() {
		return desktop;
	}
	public ICharacterFrame getCharacterFrame(CharacterWrapper character) {
		if (characterFrames!=null && !characterFrames.isEmpty()) {
			for (Iterator i=characterFrames.iterator();i.hasNext();) {
				ICharacterFrame frame = (ICharacterFrame)i.next();
				if (frame.getCharacter().equals(character)) {
					return frame;
				}
			}
		}
		return null;
	}
	public void addFrameToDesktop(RealmSpeakInternalFrame frame) {
		if (frame instanceof CharacterFrame) {
			characterFrames.add((CharacterFrame)frame);
		}
		else {
			gameControlFrames.add(frame);
		}
		desktop.add(frame);
		updateForceWidths();
		frame.setVisible(true);
		frame.organize(desktop);
		
		// For some reason, RealmObjectPanel doesn't resize properly if there is more than one JInternalFrame on
		// the desktop.  This solution is a hack to force it to resize.
		Point p = frame.getLocation();
		Dimension d = frame.getSize();
		frame.reshape(p.x,p.y,d.width+1,d.height+1);
		
		desktop.moveToFront(frame);
		windowLayoutManager.applyLastLayout();
	}
	public void removeFrameFromDesktop(RealmSpeakInternalFrame frame) {
		desktop.remove(frame);
		frame.setVisible(false);
		frame.dispose();
		if (frame instanceof CharacterFrame) {
			((CharacterFrame)frame).cleanup();
			characterFrames.remove((CharacterFrame)frame);
		}
		else {
			gameControlFrames.remove(frame);
		}
		updateWindowMenu();
		repaint();
	}
	public void killHandler() {
		if (gameHandler!=null) {
			// Remove all character frames
			gameHandler.removeAllCharacterFrames();
			
			if (gameHandler.getInspector()!=null) {
				removeFrameFromDesktop(gameHandler.getInspector());
			}
			removeFrameFromDesktop(gameHandler);
			gameHandler = null;
			updateWindowMenu();
			updateControls();
		}
	}
	private void updateForceWidths() {
		if (gameHandler!=null && gameHandler.getInspector()!=null) {
			gameHandler.setForceWidth(characterFrameForceLayout);
			gameHandler.getInspector().setForceWidth(mapForceLayout);
			for (CharacterFrame frame:characterFrames) {
				frame.setForceWidth(characterFrameForceLayout);
			}
		}
	}
	private void organize() {
		windowLayoutManager.clearLastLayout();
		updateForceWidths();
		for (RealmSpeakInternalFrame frame:gameControlFrames) {
			frame.organize(desktop);
		}
		for (CharacterFrame frame:characterFrames) {
			frame.organize(desktop);
		}
	}
	private void showConnections() {
		JOptionPane.showMessageDialog(this,realmHostFrame);
	}
	public void updateWindowMenu() {
		windowMenu.removeAll();
		windowMenu.add(showConnectionsWindow);
		if (showHostPrefsView!=null) windowMenu.add(showHostPrefsView);
		windowMenu.add(new JSeparator());
		for (int i=0;i<organizeWindow.length;i++) {
			windowMenu.add(organizeWindow[i]);
		}
		windowMenu.add(new JSeparator());
		windowMenu.add(windowLayoutManager.getGetterSubMenu());
		windowMenu.add(windowLayoutManager.getSetterSubMenu());
		windowMenu.add(windowLayoutManager.getClearMenuItem());
		
		if (gameControlFrames!=null) {
			windowMenu.add(new JSeparator());
			for (RealmSpeakInternalFrame frame:gameControlFrames) {
				JMenuItem item = new JMenuItem(frame.getTitle());
				item.addActionListener(new ShowFrameAction(frame));
				windowMenu.add(item);
			}
		}
		
		if (characterFrames!=null) {
			if (gameControlFrames.size()>0 && characterFrames.size()>0) {
				windowMenu.add(new JSeparator());
			}
			for (CharacterFrame frame:characterFrames) {
				JMenuItem item = new JMenuItem(frame.getCharacter().getCharacterName(),frame.getCharacter().getIcon());
				item.addActionListener(new ShowFrameAction(frame));
				windowMenu.add(item);
			}
		}
	}
	public void hostGame(boolean netConnect) {
		RealmUtility.resetGame();
		desktop.removeAll();
		
		FlashingButton.setFlashEnabled(false); // at the start, disable all flashing buttons
		
		RealmSpeakInit init = new RealmSpeakInit(this);
		
		// Load XML from resources
		showStatus("Loading game data...");
		init.loadData();
		resetStatus();
		
		// Get the host prefs
		HostGameSetupDialog prefChooser = new HostGameSetupDialog(this,"Host New Game",init.getGameData());
		
		// Setup the local prefs (if any)
		prefChooser.loadPrefsFromLocalConfiguration();
		
		prefChooser.setVisible(true);
		if (prefChooser.getDidStart()) {
			prefChooser.savePrefsToLocalConfiguration();
			
			// Setup the random number generator
			HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(init.getGameData());
			if (hostPrefs.hasPref(Constants.RANDOM_R250_521)) {
				RandomNumber.setRandomNumberGenerator(RandomNumberType.R250_521);
			}
			else if (hostPrefs.hasPref(Constants.RANDOM_MERSENNE_TWISTER)) {
				RandomNumber.setRandomNumberGenerator(RandomNumberType.MersenneTwister);
			}
			else {
				RandomNumber.setRandomNumberGenerator(RandomNumberType.System);
			}
			
			// Make sure there is a DieRoller logger
			DieRoller.setDieRollerLog(RealmUtility.getDieRollerLog(init.getGameData()));
			
			// Do all the pregame work
			init.buildGame();
			
			// Save off the initial layout
			init.getGameData().zipToFile(RealmHostPanel.INITIALSAVEFILE);
			
			// Make a host
			makeHost(init.getMaster(),init.getGameData(),netConnect);
//			// This works well, even though it technically doubles the resources on the host machine.
//			// Probably not worth the effort to change, unless memory becomes an issue.
		}
		resetStatus();
	}
	private void makeHost(GameData master,GameData data,boolean netConnect) {
		// Fetch hostprefs
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
		
		// Make a host
		data.setTracksChanges(true);
		
		host = new GameHost(
				master,
				data,
				hostPrefs.getGameTitle(),
				hostPrefs.getGamePass());
			
		// Launch a game connection frame
		realmHostFrame = new RealmHostPanel(host,netConnect);
//		realmHostFrame.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent ev) {
//				// The only point you can save a game, is during the recording stage...
//				boolean saveable = realmHostFrame.getGameState()==GameWrapper.GAME_STATE_RECORDING;
//				saveCurrentGame.setEnabled(saveable);
//				endCurrentGame.setEnabled(saveable);
//			}
//		});
		
		setTitle("Realm Speak"+(netConnect?" (Hosting)":" (Local)"));
	
		// This works well, even though it technically doubles the resources on the host machine.
		// Probably not worth the effort to change, unless memory becomes an issue.
		String ip = null;
		int port = -1;
		if (host.getConnector()!=null) {
			ip = host.getConnector().getIPAddress();
			port = host.getConnector().getPort();
		}
		// The host's personal pass is the ip address
		startRealmGameHandler(ip,port,hostPrefs.getHostName(),hostPrefs.getGamePass(),ip,hostPrefs.getHostEmail(),true);
		
		updateControls();
	}
	private int readInt(String val) {
		try {
			Integer num = Integer.valueOf(val);
			return num.intValue();
		}
		catch(NumberFormatException ex) {
			// ignore
		}
		return -1;
	}
	public void joinGame() {
		RealmUtility.resetGame();
		PreferenceManager prefMan = new PreferenceManager("RealmSpeak","join.cfg");
		if (prefMan.canLoad()) {
			prefMan.loadPreferences();
		}
		if (prefMan.get("joinIpAddress")==null) {
			prefMan.set("joinIpAddress","localhost");
			prefMan.set("joinPort",String.valueOf(GameHost.DEFAULT_PORT));
			prefMan.set("joinName","");
			prefMan.set("joinPass","");
			prefMan.set("joinPPass","");
			prefMan.set("joinEMail","");
		}
		
		MultiQueryDialog dialog = new MultiQueryDialog(RealmSpeakFrame.this,"Join Game Prefs");
		JComboBox cb = new JComboBox(prefMan.getList("joinIpAddress").toArray());
		cb.setEditable(true);
		dialog.addQueryLine("ip","IP Address",cb);
		dialog.addQueryLine("port","Port",new JTextField(prefMan.get("joinPort")),true);
		dialog.addQueryLine("name","Name",new JTextField(prefMan.get("joinName")),true);
		dialog.addQueryLine("pass","Game Password",new JPasswordField(prefMan.get("joinPass")),true);
		dialog.addQueryLine("ppass","Personal Password (optional)",new JTextField(prefMan.get("joinPPass")),false);
		dialog.addQueryLine("email","e-Mail (optional)",new JTextField(prefMan.get("joinEMail")),false);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		if (readInt(dialog.getText("port"))==-1) {
			JOptionPane.showMessageDialog(RealmSpeakFrame.this,"Port must be a number");
			return;
		}
		String ip = (String)dialog.getComboChoice("ip");
		prefMan.addListItem("joinIpAddress",ip,10); // remember up to 10
		prefMan.set("joinPort",dialog.getText("port"));
		prefMan.set("joinName",dialog.getText("name"));
		prefMan.set("joinPass",dialog.getText("pass"));
		prefMan.set("joinPPass",dialog.getText("ppass"));
		prefMan.set("joinEMail",dialog.getText("email"));
		prefMan.savePreferences();
		if (dialog.saidOkay()) {
			startRealmGameHandler(
					ip.trim(),
					readInt(dialog.getText("port")),
					dialog.getText("name"),
					dialog.getText("pass"),
					dialog.getText("ppass"),
					dialog.getText("email"),
					false);
		}
		FlashingButton.setFlashEnabled(true); // Make sure flashing buttons are active when joining a game
	}
	
	public void startRealmGameHandler(String ip,int port,String name,String pass,String ppass,String email,boolean hostPlayer) {
		if (ip==null) {
			// Non-network game (local)
			gameHandler = new RealmGameHandler(this,host,name,pass,ppass,email);
		}
		else {
			// Network game (hosting)
			gameHandler = new RealmGameHandler(this,ip,port,name,pass,ppass,email,hostPlayer);
		}
		gameHandler.updateToolbarOptions(realmSpeakOptions.getActionIconState());
		addFrameToDesktop(gameHandler);
		updateControls();
	}
	public void showStatus(String val) {
		status.setText(val);
		status.paint(status.getGraphics());
	}
	public void resetStatus() {
		showStatus(" ");
	}
	public boolean shutDownGame() {
		if (host!=null) {
			String message;
			if (realmHostFrame!=null && realmHostFrame.isLocal()) {
				message = "This will end the current game.  Are you sure?";
			}
			else {
				message = "This will end the current game, and kill all the connected clients.  Are you sure?";
			}
			
			int ret = JOptionPane.showConfirmDialog(this,message,"Shut Down Current Game",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);
			if (ret!=JOptionPane.YES_OPTION) {
				return false;
			}
			setTitle("Realm Speak");
			
			// Now to kill the game
			RealmUtility.resetGame();
			CombatFrame.resetSingleton();
			if (gameHandler!=null) {
				gameHandler.cleanup();
			}
			gameHandler = null;
			host.stopListening();
			desktop.removeAll();
			gameControlFrames.clear();
			characterFrames.clear();
			CenteredMapView.clearTileLayer();
			realmHostFrame = null;
			host = null;
			updateControls();
			updateWindowMenu();
			networkingOption.doClick();
			networkingOption.doClick();
//			ImageCache.resetCache();///this shouldn't be necessary
//			MagicRealmImage.resetImages();
			System.gc(); // This TOTALLY shouldn't be necessary, but what can it hurt?
			repaint();
		}
		return true;
	}
	public void close() {
		if (shutDownGame()) {
			saveFramePreferences();
			HallOfFame.save();
			DebugUtility.shutDown();
			System.exit(0);
		}
	}
	public RealmGameHandler getGameHandler() {
		return gameHandler;
	}
	public CharacterActionControlManager getFrontActionControlManager() {
		if (gameHandler!=null) {
			return gameHandler.findTopmostActionControlManager();
		}
		return null;
	}
	public RealmTurnPanel getFrontRealmTurnPanel() {
		if (gameHandler!=null) {
			return gameHandler.findTopmostRealmTurnPanel();
		}
		return null;
	}
	public ArrayList<String> getAllServerNames() {
		ArrayList<String> list = new ArrayList<String>();
		if (host!=null) {
			for (GameServer server:host.getServers()) {
				list.add(server.getClientName());
			}
		}
		return list;
	}
	
	/**
	 * Private inner classes
	 */
	private class ShowFrameAction implements ActionListener {
		private JInternalFrame theFrame;
		public ShowFrameAction(JInternalFrame theFrame) {
			this.theFrame = theFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			theFrame.setVisible(true);
			try {
				theFrame.setIcon(false); // de-iconify
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			desktop.moveToFront(theFrame);
		}
	}
	private class ShowCharCardViewAction extends JMenuItem implements ActionListener {
		private String iconPath;
		public ShowCharCardViewAction(String[] input) {
			super(input[0]);
			String symbolPath = "characters/"+input[1];
			setIcon(ImageCache.getIcon(symbolPath,16,16));
			iconPath = "images/characterdetail/"+input[1]+".jpg";
			addActionListener(this);
		}
		public void actionPerformed(ActionEvent ev) {
			showImage(getText(),iconPath);
		}
	}
	private class ShowGoldSpecialViewAction extends JMenuItem implements ActionListener {
		private RealmSpeakFrame mainFrame;
		private String chitName;
		public ShowGoldSpecialViewAction(RealmSpeakFrame mainFrame,String chitName) {
			super(chitName);
			this.mainFrame = mainFrame;
			this.chitName = chitName;
			addActionListener(this);
		}
		public void actionPerformed(ActionEvent ev) {
			GameData gameData = null;
			RealmGameHandler gameHandler = mainFrame.getGameHandler();
			if (gameHandler==null) {
				RealmLoader loader = new RealmLoader();
				gameData = loader.getData();
			}
			else {
				gameData = gameHandler.getClient().getGameData();
			}
			
			GameObject chitObject = gameData.getGameObjectByName(chitName);
			GoldSpecialChitComponent chit = (GoldSpecialChitComponent)RealmComponent.getRealmComponent(chitObject);
			chit.display(mainFrame,null);
		}
	}
	public static void _main(String[] args) {
		int vps = 5;
		int dim = 28;
		int diw = dim/4;
		int vpPerWeek = vps/4;
		for (int i=1;i<=28;i++) {
			int weeksUsed = (i-1)/diw;
			int v = vps - (vpPerWeek * weeksUsed);
			System.out.println("day "+i+" vps "+v);
		}
	}
	public static void main(String[]args) {
		String ver = System.getProperty("java.vm.version");
		if (ver.startsWith("1.4.") || ver.startsWith("1.3.") || ver.startsWith("1.2.") || ver.startsWith("1.1.")) {
			JOptionPane.showMessageDialog(null,"RealmSpeak is now compiled for Java 1.5.\nYou are currently running version "+ver
					+".\nI would recommend upgrading your Java installation by visiting java.sun.com."
					,"Old Java Installation",JOptionPane.WARNING_MESSAGE);
		}
		
		RealmUtility.findImagesFolderOrExit();
		RealmCharacterBuilderModel.loadAllCustomCharacters();
		LoggingHandler.initLogging();
		RealmUtility.setupTextType();
		DebugUtility.setupArgs(args);
		RealmSpeakFrame frame = new RealmSpeakFrame();
		frame.setVisible(true);
		
		String gamePath = System.getProperty(DebugUtility.LAUNCH_GAME);
		if (gamePath!=null) {
			File file = new File(gamePath);
			file = file.getAbsoluteFile();
			if (file.exists()) {
				frame.loadHostGame(file,false);
			}
			else {
				JOptionPane.showMessageDialog(frame,"Game not found on launch:\n\n   "+gamePath,"Launch Error",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}