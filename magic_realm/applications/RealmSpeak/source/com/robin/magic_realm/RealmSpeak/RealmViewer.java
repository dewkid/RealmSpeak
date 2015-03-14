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

import com.robin.game.objects.*;
import com.robin.magic_realm.MRMap.MapBuilder;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.Quest;
import com.robin.magic_realm.components.quest.QuestLoader;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.table.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

/**
 * Utility class for viewing game pieces
 */
public class RealmViewer extends JFrame {
	
	private static final boolean SHOW_ONLY_NOTREADY = false;
	
	protected RealmObjectPanel spellViewPanel;
	protected RealmObjectPanel treasureViewPanel;
	protected RealmObjectPanel monsterViewPanel;
	protected RealmObjectPanel nativeViewPanel;
	protected RealmObjectPanel otherViewPanel;
	protected RealmObjectPanel questViewPanel;
	protected JTabbedPane tabbedPanels;
	protected GameData data;
	protected ArrayList<String> keyVals;
	
	public RealmViewer(GameData data,ArrayList<String> keyVals) {
		this.data = data;
		this.keyVals = keyVals;
		initComponents();
		RealmUtility.setupTextType();
		addData();
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ev) {
				spellViewPanel.adjustSize();
				treasureViewPanel.adjustSize();
				monsterViewPanel.adjustSize();
				nativeViewPanel.adjustSize();
				otherViewPanel.adjustSize();
				questViewPanel.adjustSize();
			}
		});
	}
	private void addData() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> list = new ArrayList<GameObject>(pool.find(keyVals));
		
		// Add the summonables
		JFrame dummy = new JFrame();
		CharacterWrapper amazon = new CharacterWrapper(data.getGameObjectByName("Amazon"));
		list.addAll((new RaiseDead(dummy)).getOneOfEach(amazon));
		list.addAll((new SummonAnimal(dummy)).getOneOfEach(amazon));
		list.addAll((new SummonElemental(dummy)).getOneOfEach(amazon));
		
		MonsterCreator mc = new MonsterCreator("test");
		list.add(SetupCardUtility.createBlob(mc,data));
		list.add(SetupCardUtility.createWasp(mc,data));
		
		ArrayList<GameObject> treasureList = new ArrayList<GameObject>();
		ArrayList<GameObject> spellList = new ArrayList<GameObject>();
		for (GameObject obj:list) {
			RealmComponent rc = RealmComponent.getRealmComponent(obj);
			if (rc!=null) {
				boolean notready = rc.getGameObject().hasThisAttribute("notready");
				if (rc.isTreasure()) {
					if (notready || !SHOW_ONLY_NOTREADY) {
						TreasureCardComponent treasure = (TreasureCardComponent)rc;
						treasure.setFaceUp();
						//treasureViewPanel.addObject(obj);
						treasureList.add(obj);
					}
				}
				else if (rc.isSpell()) {
					if (notready || !SHOW_ONLY_NOTREADY) {
						//spellViewPanel.addObject(obj);
						spellList.add(obj);
					}
				}
				else if (rc.isMonster() || rc.isMonsterPart()) {
					monsterViewPanel.addObject(obj);
				}
				else if (rc.isNative() || rc.isNativeHorse()) {
					nativeViewPanel.addObject(obj);
				}
				else {
					if (rc.isStateChit()) {
						StateChitComponent chit = (StateChitComponent)rc;
						chit.setFaceUp();
					}
					otherViewPanel.addObject(obj);
				}
			}
		}
		
		Collections.sort(treasureList,new Comparator<GameObject>() {
			public int compare(GameObject o1, GameObject o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		treasureViewPanel.addObjects(treasureList);
		
		Collections.sort(spellList,new Comparator<GameObject>() {
			public int compare(GameObject o1, GameObject o2) {
				String m1 = o1.getThisAttribute("spell");
				String m2 = o2.getThisAttribute("spell");
				int ret = m1.compareTo(m2);
				if (ret==0) {
					ret = o1.getName().compareTo(o2.getName());
				}
				return ret;
			}
		});
		spellViewPanel.addObjects(spellList);
		
		for (Quest quest:QuestLoader.loadAllQuestsFromQuestFolder()) {
			questViewPanel.addObject(quest.getGameObject());
		}
		
		repaint();
	}
	private void initComponents() {
		setTitle("Realm Viewer");
		setSize(1300,800);
		getContentPane().setLayout(new BorderLayout());
		setLocation(50,50);
		
		tabbedPanels = new JTabbedPane();
		getContentPane().add(tabbedPanels,"Center");
		
		spellViewPanel = new RealmObjectPanel(true,true);
		tabbedPanels.addTab("Spells",new JScrollPane(spellViewPanel));
		treasureViewPanel = new RealmObjectPanel(true,true);
		tabbedPanels.addTab("Treasures",new JScrollPane(treasureViewPanel));
		monsterViewPanel = new RealmObjectPanel(true,true);
		tabbedPanels.addTab("Monsters",new JScrollPane(monsterViewPanel));
		nativeViewPanel = new RealmObjectPanel(true,true);
		tabbedPanels.addTab("Natives",new JScrollPane(nativeViewPanel));
		otherViewPanel = new RealmObjectPanel(true,true);
		tabbedPanels.addTab("Other",new JScrollPane(otherViewPanel));
		questViewPanel = new RealmObjectPanel(true,true);
		tabbedPanels.addTab("Quests",new JScrollPane(questViewPanel));
		
		getContentPane().add(new JLabel("SHIFT-Click to flip counters"),"South");
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
	}
	public static void main(String[]args) {
		RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_FRENZEL;// RealmComponent.DISPLAY_STYLE_COLOR;
		RealmUtility.setupTextType();
		RealmLoader loader = new RealmLoader();
		GameData data = loader.getData();
		ArrayList<String> query = new ArrayList<String>();
		query.add("rw_expansion_1");
		StringBuffer result = new StringBuffer();
		data.doSetup(result,"rw_expansion_1_setup",query);
		//query.add("!original_game");
		
		boolean showPieces = true;
		boolean showMap = false;
		boolean showChooser = false;
		
		if (showPieces) {
			new RealmViewer(data,query).setVisible(true);
		}
		
		if (showChooser || showMap) {
			HostPrefWrapper hostPrefs = new HostPrefWrapper(data.createNewObject());
			hostPrefs.setHostName("Test");
			hostPrefs.setGameKeyVals(Constants.ORIGINAL_GAME);
			ArrayList keyVals = new ArrayList();
			keyVals.add(Constants.ORIGINAL_GAME);
			while(!MapBuilder.autoBuildMap(data,keyVals));
			
			if (showMap) {
				JFrame mapFrame = new JFrame("map");
				mapFrame.getContentPane().setLayout(new BorderLayout());
				final CenteredMapView map = new CenteredMapView(data);
				map.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent ev) {
						if (ev.getButton()==MouseEvent.BUTTON1) {
							Point p = ev.getPoint();
							if (ev.isControlDown()) {
								map.revealChitsAtPoint(p);
							}
							else if (ev.isAltDown()) {
								map.summonMonstersAtPoint(p);
							}
							else if (ev.isShiftDown()) {
								map.markClearingConnectionsAtPoint(p);
							}
							else {
								map.flipTileAtPoint(p);
							}
						}
					}
				});
				mapFrame.getContentPane().add(map,"Center");
				mapFrame.setSize(800,600);
				mapFrame.setLocationRelativeTo(null);
				
				mapFrame.addKeyListener(map.getShiftKeyListener());
				
				map.setScale(0.5);
				map.markAllMapEdges(true);
				
				mapFrame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent ev) {
						System.exit(0);
					}
				});
				mapFrame.setVisible(true);
				map.centerMap();
				
				for (int x=0;x<10;x++) {
					for (int y=0;y<10;y++) {
						Point p = new Point(x-5,y-5);
						if (!map.isTileAtPosition(p)) {
							map.setPositionColor(p,Color.lightGray);
						}
					}
				}
			}
			if (showChooser) {
				GameObject go = data.getGameObjectByName("Borderland");
				TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
				TileLocation tl = new TileLocation(tile.getClearing(1));
				CenteredMapView.initSingleton(data);
				TileLocationChooser chooser = new TileLocationChooser(new JFrame(),CenteredMapView.getSingleton(),tl);
				chooser.setVisible(true);
			}
		}
	}
}