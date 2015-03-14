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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import com.robin.game.objects.*;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.general.util.RandomNumber;
import com.robin.general.util.StringBufferedList;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class GoldSpecialPlacementDialog extends AggressiveDialog {
	
	private static final String[] DEST_HEADER = {"Setup Box","Relationship","Contains"};
	private static final Font HEADER_FONT = new Font("Dialog",Font.BOLD,12);
	private static final Font BIG_FONT = new Font("Dialog",Font.BOLD,20);
	
	private CharacterWrapper character;
	private GameData gameData;
	
	private ArrayList<GameObject[]> chits;
	private ArrayList<GameObject> destinations;
	
	private GoldSpecialChitComponent activeChit;
	private GameObject activeDestination;
	private boolean cancelled = false;
	
	private JLabel chitIcon;
	private JEditorPane editorPane;
	private ArrayList<JToggleButton> chitButtons;
	private ArrayList<PlaceButton> destButtons;
	
	private HostPrefWrapper hostPrefs;
	
	public GoldSpecialPlacementDialog(JFrame owner, CharacterWrapper character) {
		super(owner, character.getGameObject().getName()+" Placing a Gold Chit", true);
		this.character = character;
		this.gameData = character.getGameObject().getGameData();
		hostPrefs = HostPrefWrapper.findHostPrefs(character.getGameData());
	}
	public void rebuildAndShow() {
		activeChit = null;
		activeDestination = null;
		initData();
		initComponents();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	protected void setActiveChit(GoldSpecialChitComponent rc) {
		activeChit = rc;
		chitIcon.setIcon(rc.getIcon());
		editorPane.setText(rc.generateHTML(character));
		editorPane.setCaretPosition(0);
		for (PlaceButton button:destButtons) {
			button.setEnabled(true);
		}
	}
	protected void setActiveDestination(GameObject go) {
		activeDestination = go;
		setVisible(false);
	}
	public GoldSpecialChitComponent getActiveChit() {
		return activeChit;
	}
	public GameObject getActiveDestination() {
		return activeDestination;
	}
	public boolean isCancel() {
		return cancelled;
	}
	private void initData() {
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(gameData);
		ArrayList query = new ArrayList();
		query.add("!"+Constants.GOLD_SPECIAL_PLACED);
		if (hostPrefs.hasPref(Constants.HOUSE2_IGNORE_CAMPAIGNS)) {
			query.add("!campaign");
		}
		ArrayList<GameObject> gs = new ArrayList<GameObject>(rom.findObjects("gold_special",query, false));
		destinations = new ArrayList<GameObject>(rom.findObjects("gold_special_target", false));
		chits = new ArrayList<GameObject[]>();
		while (!gs.isEmpty()) {
			GameObject[] chit = new GameObject[2];
			chit[0] = (GameObject) gs.remove(0);
			chit[1] = (GameObject) gameData.getGameObject(Long.valueOf(chit[0].getThisAttribute("pairid")));
			gs.remove(chit[1]);
			chits.add(chit);
		}
	}
	private void initComponents() {
		setSize(800,600);
		getContentPane().removeAll();
		getContentPane().setLayout(new BorderLayout());
		JLabel titleBar = new JLabel("Choose a chit face on the left, and select a destination at the bottom",JLabel.CENTER);
		titleBar.setOpaque(true);
		titleBar.setBackground(MagicRealmColor.PALEYELLOW);
		titleBar.setFont(HEADER_FONT);
		getContentPane().add(titleBar,"North");
		JTabbedPane chitTabs = new JTabbedPane(JTabbedPane.LEFT);
		chitTabs.setFont(BIG_FONT);
		chitTabs.setBorder(BorderFactory.createEtchedBorder());
		JPanel buttonGrid = null;
		int count = 0;
		int pageCount = 0;
		ButtonGroup group = new ButtonGroup();
		chitButtons = new ArrayList<JToggleButton>();
		boolean ignoreCampaigns = hostPrefs.hasPref(Constants.HOUSE2_IGNORE_CAMPAIGNS);
		for (GameObject[] chit:chits) {
			if (count%6==0) {
				if (buttonGrid!=null) {
					chitTabs.addTab(String.valueOf(++pageCount),buttonGrid);
				}
				buttonGrid = new JPanel(new GridLayout(6,1));
			}
			JPanel subGrid = new JPanel(new GridLayout(1,2));
			subGrid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			for (int i=0;i<2;i++) {
				JToggleButton button;
				boolean isCampaign = chit[i].hasThisAttribute("campaign");
				if (chit[i].hasThisAttribute(Constants.GOLD_SPECIAL_PLACED) || (isCampaign && ignoreCampaigns)) {
					button = new JToggleButton();
					button.setEnabled(false);
				}
				else {
					button = new GoldButton(chit[i]);
				}
				chitButtons.add(button);
				subGrid.add(button);
				group.add(button);
			}
			buttonGrid.add(subGrid);
			count++;
		}
		chitTabs.addTab(String.valueOf(++pageCount),buttonGrid);
		if (pageCount==1) {
			getContentPane().add(buttonGrid,"West");
		}
		else {
			getContentPane().add(chitTabs,"West");
		}
		JPanel mainPanel = new JPanel(new GridLayout(2,1));
		getContentPane().add(mainPanel,"Center");
		
		JPanel topPanel = new JPanel(new GridLayout(1,2));
		
		JPanel infoPanel = new JPanel(new GridLayout(2,1));
		chitIcon = new JLabel();
		chitIcon.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(chitIcon);
		JLabel infoLabel = new JLabel(getCharacterInfo(),JLabel.CENTER);
		infoLabel.setFont(HEADER_FONT);
		infoLabel.setOpaque(true);
		infoLabel.setBorder(BorderFactory.createEtchedBorder());
		infoLabel.setBackground(MagicRealmColor.LIGHTBLUE);
		infoPanel.add(infoLabel);
		topPanel.add(infoPanel);
		
		editorPane = new JEditorPane();
		editorPane.setOpaque(false);
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		topPanel.add(new JScrollPane(editorPane));
		mainPanel.add(topPanel);
		
		// Build destination panel
		JTabbedPane destTabs = new JTabbedPane(JTabbedPane.LEFT);
		destTabs.setFont(BIG_FONT);
		DestinationPanel dPanel = null;
		count = 0;
		pageCount = 0;
		destButtons = new ArrayList<PlaceButton>();
		for (GameObject go:destinations) {
			if (count%7==0) {
				if (dPanel!=null) {
					destTabs.addTab(String.valueOf(++pageCount),dPanel);
				}
				dPanel = new DestinationPanel();
			}
			JPanel destRow = new JPanel(new GridLayout(1,DEST_HEADER.length));
			destRow.setBorder(BorderFactory.createEtchedBorder());
			
			destRow.add(new JLabel(go.getName(),JLabel.CENTER));
			String summon = go.getThisAttribute("summon");
			if (summon.indexOf(',')<0) {
				String relBlock = RealmUtility.getRelationshipBlockFor(go);
				int rel = character.getRelationship(relBlock,summon);
				String relationship = RealmUtility.getRelationshipNameFor(rel);
				destRow.add(new JLabel(relationship,JLabel.CENTER));
			}
			else {
				destRow.add(new JLabel(""));
			}
			
			if (go.getHoldCount()>0) {
				GameObject hold = (GameObject)go.getHold().get(0);
				destRow.add(new JLabel(hold.getName(),JLabel.CENTER));
			}
			else {
				PlaceButton button = new PlaceButton(go);
				destButtons.add(button);
				destRow.add(button);
			}
			
			dPanel.add(destRow);
			count++;
		}
		destTabs.addTab(String.valueOf(++pageCount),dPanel);
		
		if (pageCount==1) {
			mainPanel.add(dPanel);
		}
		else {
			mainPanel.add(destTabs);
		}
		
		JButton randomButton = new JButton("Place Random");
		randomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int r;
				JToggleButton chitButton;
				while(!(chitButton = (chitButtons.get(RandomNumber.getRandom(chitButtons.size())))).isEnabled());
				chitButton.doClick();
				r = RandomNumber.getRandom(destButtons.size());
				destButtons.get(r).doClick();
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				cancelled = true;
				setVisible(false);
			}
		});
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(randomButton);
		box.add(Box.createHorizontalGlue());
		box.add(cancelButton);
		box.add(Box.createHorizontalGlue());
		getContentPane().add(box,"South");
	}
	private String getCharacterInfo() {
		String levelKey = "level_"+character.getCharacterLevel();
		ArrayList types = character.getGameObject().getAttributeList(levelKey,"spelltypes"); // like [I,VII] (for example)
		String info;
		if (types==null) {
			info = character.getGameObject().getName()+" has no spell types.";
		}
		else {
			StringBufferedList list = new StringBufferedList();
			for (Iterator i=types.iterator();i.hasNext();) {
				String val = (String)i.next();
				list.append(val);
			}
			info = character.getGameObject().getName()+" can learn<br>spells of type "+list.toString();
		}
		return "<html><center>"+info+"</center></html>";
	}
	
	private class GoldButton extends JToggleButton implements ActionListener {
		private GoldSpecialChitComponent rc;
		public GoldButton(GameObject go) {
			super();
			rc = (GoldSpecialChitComponent)RealmComponent.getRealmComponent(go);
			setIcon(rc.getIcon());
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent ev) {
			setActiveChit(rc);
		}
	}
	private class PlaceButton extends JButton implements ActionListener {
		private GameObject destination;
		public PlaceButton(GameObject go) {
			super("Place Here");
			setEnabled(false);
			destination = go;
			addActionListener(this);
		}
		public void actionPerformed(ActionEvent ev) {
			setActiveDestination(destination);
		}
	}
	private class DestinationPanel extends JPanel {
		
		private JPanel destHeader;
		private JPanel destBody;
		
		public DestinationPanel() {
			super(new BorderLayout());
			destHeader = new JPanel(new GridLayout(1,DEST_HEADER.length));
			for (int i=0;i<DEST_HEADER.length;i++) {
				JLabel label = new JLabel(DEST_HEADER[i],JLabel.CENTER);
				label.setFont(HEADER_FONT);
				label.setBorder(BorderFactory.createEtchedBorder());
				destHeader.add(label);
			}
			add(destHeader,"North");
			destBody = new JPanel(new GridLayout(7,1));
			add(destBody,"Center");
		}
		public void add(JComponent c) {
			destBody.add(c);
		}
	}
	
	private static void prepMultiboard(HostPrefWrapper hostPrefs,GameData data) {
		RealmLoader doubleLoader = new RealmLoader();
		
		ArrayList<String> appendNames = new ArrayList<String>();
		doubleLoader.cleanupData(hostPrefs.getGameKeyVals());
		int count = hostPrefs.getMultiBoardCount();
		for (int n=0;n<count-1;n++) {
			String appendName = " "+Constants.MULTI_BOARD_APPENDS.substring(n,n+1);
			appendNames.add(appendName);
		}
		for (String appendName:appendNames) {
			long start = data.getMaxId()+1;
			doubleLoader.getData().renumberObjectsStartingWith(start);
			for (Iterator i=doubleLoader.getData().getGameObjects().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				if (!go.hasThisAttribute("season")) { // The one exception
					GameObject dub = data.createNewObject(go.getId());
					dub.copyFrom(go);
					dub.setThisAttribute(Constants.BOARD_NUMBER,appendName.trim());
					dub.setName(dub.getName()+appendName);
				}
			}
		}
		
		// Resolve objects (holds can't be calculated until all are loaded!)
		for (Iterator i=data.getGameObjects().iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			obj.resolveHold(data.getGameObjectIDHash());
		}
		
		// Expand the setup to accommodate the new tiles
		data.findSetup(hostPrefs.getGameSetupName()).expandSetup(appendNames);
	}
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		RealmLoader loader = new RealmLoader();
		HostPrefWrapper hostPrefs = HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		//hostPrefs.setMultiBoardEnabled(true);
		//hostPrefs.setMultiBoardCount(2);
//hostPrefs.setPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE,true);
//hostPrefs.setPref(Constants.HOUSE2_IGNORE_CAMPAIGNS,true);
		prepMultiboard(hostPrefs,loader.getData());
		RealmUtility.doMatchGoldSpecials(loader.getData());
		loader.getData().doSetup(hostPrefs.getGameSetupName(),GamePool.makeKeyVals(hostPrefs.getGameKeyVals()));
		GameObject go = loader.getData().getGameObjectByName("Magician");
		CharacterWrapper character = new CharacterWrapper(go);
		character.initRelationships(hostPrefs);
		character.setCharacterLevel(4);
		
		while(true) {
			GoldSpecialPlacementDialog gsDialog = new GoldSpecialPlacementDialog(new JFrame(),character);
			gsDialog.setLocationRelativeTo(null);
			gsDialog.rebuildAndShow();
			
			if (gsDialog.isCancel()) {
				break;
			}
			
			GoldSpecialChitComponent chit = gsDialog.getActiveChit();
			GameObject destination = gsDialog.getActiveDestination();
			destination.add(chit.getGameObject());
			destination.setThisAttribute(Constants.GOLD_SPECIAL_PLACED);
			chit.getGameObject().setThisAttribute(Constants.GOLD_SPECIAL_PLACED);
			if (!hostPrefs.hasPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE)) {
				chit.getOtherSide().getGameObject().setThisAttribute(Constants.GOLD_SPECIAL_PLACED);
			}
		
			System.out.println("Placing "+chit.getGameObject().getName()+" in the "+destination.getName());
			
			if (!SetupCardUtility.stillChitsToPlace(hostPrefs)) {
				break;
			}
		}
		System.exit(0);
	}
}