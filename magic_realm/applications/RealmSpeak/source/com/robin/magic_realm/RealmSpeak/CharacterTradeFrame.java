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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ListChooser;
import com.robin.magic_realm.components.MagicRealmColor;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.Inventory;
import com.robin.magic_realm.components.swing.RealmObjectPanel;
import com.robin.magic_realm.components.swing.RealmTradeDialog;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmDirectInfoHolder;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class CharacterTradeFrame extends JFrame {
	
	private RealmGameHandler gameHandler;
	private CharacterWrapper active;
	private CharacterInventoryPanel activeInv;
	private CharacterWrapper include;
	private CharacterInventoryPanel includeInv;
	
	private String otherClientName;
	private TradePanel myPanel;
	private TradePanel otherPanel; // either active or include (THIS IS NOT A NEW PANEL!!)
	
	private TradePanel activePanel;
	private TradePanel includePanel;
	
	private JButton offerButton;
	private JButton okayButton;	// Only the active trader gets this button
	private JButton cancelButton;	// Both traders get this button
	
	public CharacterTradeFrame(RealmGameHandler gameHandler,CharacterWrapper active,CharacterInventoryPanel activeInv,CharacterWrapper include,CharacterInventoryPanel includeInv) {
		this.gameHandler = gameHandler;
		this.active = active;
		this.activeInv = activeInv;
		this.include = include;
		this.includeInv = includeInv;
		initComponents();
	}
	public void updateInv() {
		if (activeInv!=null) {
			activeInv.updatePanel();
		}
		if (includeInv!=null) {
			includeInv.updatePanel();
		}
	}
	public void repaintInv() {
		if (activeInv!=null) {
			activeInv.repaint();
		}
		if (includeInv!=null) {
			includeInv.repaint();
		}
	}
	private void initComponents() {
		String clientName = gameHandler.getClient().getClientName();
		
		// Should only use the approve checkboxes if trading with another player (silly to approve your own decisions!)
		boolean useApprove = activeInv==null || includeInv==null;
		
		if (useApprove) {
			// Just in case the includeInv frame simply hasn't been created yet...
			RealmComponent includeRc = RealmComponent.getRealmComponent(include.getGameObject());
			if (includeRc.ownedBy(RealmComponent.getRealmComponent(active.getGameObject()))) {
				useApprove = false;
			}
		}
		
		setTitle(clientName+" - Trade Window");
		setSize(640,480);
		setLocationRelativeTo(gameHandler.getMainFrame());
		getContentPane().setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel(new GridLayout(2,1));
		activePanel = new TradePanel(clientName,active,useApprove);
		mainPanel.add(activePanel);
		includePanel = new TradePanel(clientName,include,useApprove);
		mainPanel.add(includePanel);
		getContentPane().add(mainPanel,"Center");
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				sendMessage(RealmDirectInfoHolder.TRADE_CANCEL);
				cleanExit();
			}
		});
		box.add(cancelButton);
		box.add(Box.createHorizontalGlue());
		if (clientName.equals(active.getPlayerName())) {
			otherClientName = include.getPlayerName();
			myPanel = activePanel;
			otherPanel = includePanel;
		}
		else {
			otherClientName = active.getPlayerName();
			myPanel = includePanel;
			otherPanel = activePanel;
		}
		if (clientName.equals(active.getPlayerName())) {
			if (!otherClientName.equals(active.getPlayerName())) {
				offerButton = new JButton("Offer");
				offerButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						completeTrade(false);
						sendMessage(RealmDirectInfoHolder.TRADE_DONE);
						cleanExit();
					}
				});
				box.add(offerButton);
			}
			okayButton = new JButton("Finish Trade");
			okayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					completeTrade(true);
					sendMessage(RealmDirectInfoHolder.TRADE_DONE);
					cleanExit();
				}
			});
			box.add(okayButton);
		}
		
		getContentPane().add(box,"South");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		updateControls();
	}
	private void sendMessage(String val) {
		sendMessage(val,null,-1);
	}
	private void sendMessage(String val,int gold) {
		sendMessage(val,null,gold);
	}
	private void sendMessage(String val,Collection gameObjects) {
		sendMessage(val,gameObjects,-1);
	}
	private void sendMessage(String val,Collection gameObjects,int gold) {
		RealmDirectInfoHolder info = new RealmDirectInfoHolder(gameHandler.getClient().getGameData());
		info.setCommand(val);
		if (RealmDirectInfoHolder.TRADE_GOLD.equals(val) && gold>=0) {
			info.setGold(gold);
		}
		else if (gameObjects!=null) {
			info.setGameObjects(gameObjects);
		}
		gameHandler.getClient().sendInfoDirect(otherClientName,info.getInfo());
	}
	private void sendMessageStrings(String val,Collection strings) {
		RealmDirectInfoHolder info = new RealmDirectInfoHolder(gameHandler.getClient().getGameData());
		info.setCommand(val);
		if (strings!=null) {
			info.setStrings(strings);
		}
		gameHandler.getClient().sendInfoDirect(otherClientName,info.getInfo());
	}
	private void updateControls() {
		if (offerButton!=null) {
			offerButton.setEnabled(activePanel.isApproved());
		}
		if (okayButton!=null) {
			okayButton.setEnabled(activePanel.isApproved() && includePanel.isApproved());
		}
	}
	private void cleanExit() {
		gameHandler.killCharacterTradeFrame();
	}
	public void setAccept(boolean val) {
		otherPanel.setApprove(val);
		updateControls();
	}
	public void addInventory(ArrayList<GameObject> in) {
		otherPanel.addInventory(in);
	}
	public void removeInventory(ArrayList<GameObject> in) {
		otherPanel.removeInventory(in);
	}
	public void addDiscoveries(Collection in) {
		otherPanel.addDiscoveries(in);
	}
	public void removeDiscoveries(Collection in) {
		otherPanel.removeDiscoveries(in);
	}
	public void setGold(int gold) {
		otherPanel.setGold(gold);
	}
	private void completeTrade(boolean approvedTrade) {
		// Trade inventory
		ArrayList<GameObject> myStuff = myPanel.getOnTheTable();
		ArrayList<GameObject> hisStuff = otherPanel.getOnTheTable();
		RealmUtility.transferInventory(this,myPanel.getCharacter(),otherPanel.getCharacter(),myStuff,gameHandler.getUpdateFrameListener(),!approvedTrade);
		RealmUtility.transferInventory(this,otherPanel.getCharacter(),myPanel.getCharacter(),hisStuff,gameHandler.getUpdateFrameListener(),false);
		
		// Trade discoveries
		ArrayList<String> myDisc = myPanel.getDiscoveries();
		ArrayList<String> hisDiscoveries = otherPanel.getDiscoveries();
		updateDiscoveries(myPanel.getCharacter(),hisDiscoveries);
		updateDiscoveries(otherPanel.getCharacter(),myDisc);
		
		// Trade gold
		int myGold = myPanel.getGold();
		int hisGold = otherPanel.getGold();
		myPanel.getCharacter().addGold(hisGold);
		myPanel.getCharacter().addGold(-myGold);
		otherPanel.getCharacter().addGold(myGold);
		otherPanel.getCharacter().addGold(-hisGold);
		
		// Submit
		gameHandler.submitChanges();
		gameHandler.updateCharacterFrames();
	}
	public void updateDiscoveries(CharacterWrapper character,ArrayList<String> newDiscoveries) {
		for (Iterator i=newDiscoveries.iterator();i.hasNext();) {
			String discovery = (String)i.next();
			if (discovery.startsWith(TREASURE_LOCATION)) {
				String tl = discovery.substring(TREASURE_LOCATION.length());
				String site = null;
				int paren = tl.indexOf(" ( + ");
				if (paren>=0) {
					site = tl.substring(paren+5,tl.length()-1);
					tl = tl.substring(0,paren);
				}
				if (!character.hasTreasureLocationDiscovery(tl)) {
					character.addTreasureLocationDiscovery(tl);
					if (site!=null && !character.hasTreasureLocationDiscovery(site)) {
						character.addTreasureLocationDiscovery(site);
					}
				}
			}
			else if (discovery.startsWith(HIDDEN_PATH)) {
				String hp = discovery.substring(HIDDEN_PATH.length());
				if (!character.hasHiddenPathDiscovery(hp)) {
					character.addHiddenPathDiscovery(hp);
				}
			}
			else if (discovery.startsWith(SECRET_PASSAGE)) {
				String sp = discovery.substring(SECRET_PASSAGE.length());
				if (!character.hasSecretPassageDiscovery(sp)) {
					character.addSecretPassageDiscovery(sp);
				}
			}
			else if (discovery.startsWith(GATE)) {
				String gate = discovery.substring(GATE.length());
				if (!character.hasOtherChitDiscovery(gate)) {
					character.addOtherChitDiscovery(gate);
				}
			}
			else if (discovery.startsWith(GUILD)) {
				String gate = discovery.substring(GUILD.length());
				if (!character.hasOtherChitDiscovery(gate)) {
					character.addOtherChitDiscovery(gate);
				}
			}
		}
	}
	
	private static final String TREASURE_LOCATION = "Treasure Location: ";
	private static final String HIDDEN_PATH = "Hidden Path: ";
	private static final String SECRET_PASSAGE = "Secret Passage: ";
	private static final String GATE = "Gate: ";
	private static final String GUILD = "Guild: ";
	private static final Font titleFont = new Font("Arial",Font.BOLD,16);
	private class TradePanel extends JPanel {
		private CharacterWrapper character;
		
		private JButton addButton;
		private JButton removeButton;
		private JCheckBox approveBox;
		private JLabel approveLabel;
		
		private JLabel goldLabel;
		
		private RealmObjectPanel view;
		
		private ArrayList<String> discoveries;
		private JList discoveryList;
		private DiscoveryListModel discoveryModel;
		private JButton shareButton;
		private JButton unshareButton;
		
		private ArrayList<GameObject> onTheTable;
		
		public TradePanel(String clientName,CharacterWrapper character,boolean useApprove) {
			super(new BorderLayout());
			onTheTable = new ArrayList<GameObject>();
			setBorder(BorderFactory.createLoweredBevelBorder());
			this.character = character;
			JLabel title = new JLabel(this.character.getGameObject().getName());
			title.setOpaque(true);
			title.setBackground(MagicRealmColor.LIGHTBLUE);
			title.setFont(titleFont);
			add(title,"North");
			
			boolean hasControl = clientName.equals(character.getPlayerName());
			
			view = new RealmObjectPanel(hasControl,false);
			add(new JScrollPane(view),"Center");
			
			discoveries = new ArrayList<String>();
			discoveryModel = new DiscoveryListModel(discoveries);
			discoveryList = new JList(discoveryModel);
			discoveryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			add(new JScrollPane(discoveryList),"East");
			
			Box box = Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			JPanel goldPanel = new JPanel(new BorderLayout());
			goldPanel.setOpaque(true);
			goldPanel.setBackground(MagicRealmColor.GOLD);
			goldPanel.add(new JLabel("Gold",JLabel.CENTER),"North");
			goldLabel = new JLabel("0",JLabel.CENTER);
			ComponentTools.lockComponentSize(goldLabel,50,25);
			goldLabel.setBorder(BorderFactory.createLoweredBevelBorder());
			goldPanel.add(goldLabel,"South");
			goldPanel.setBorder(BorderFactory.createEtchedBorder());
			ComponentTools.lockComponentSize(goldPanel,50,50);
			box.add(goldPanel);
			box.add(Box.createVerticalGlue());
			add(box,"West");
			
			if (hasControl) {
				goldPanel.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent ev) {
						int goldOnTheTable = Integer.valueOf(goldLabel.getText()).intValue();
						int currentGold = (int)TradePanel.this.character.getGold();
						
						if (currentGold>0 && !TradePanel.this.character.hasCurse(Constants.ASHES)) {
							String message = "How much gold (you have "+currentGold+")?";
							String ret = JOptionPane.showInputDialog(goldLabel,message,String.valueOf(goldOnTheTable));
							if (ret!=null) {
								try {
									Integer n = Integer.valueOf(ret);
									if (n.intValue()<0) {
										JOptionPane.showMessageDialog(goldLabel,"Gold can not be negative!","Error!",JOptionPane.ERROR_MESSAGE);
									}
									else if (n.intValue()>currentGold) {
										JOptionPane.showMessageDialog(goldLabel,"You cannot give more than you have!","Error!",JOptionPane.ERROR_MESSAGE);
									}
									else {
										goldLabel.setText(n.toString());
										sendMessage(RealmDirectInfoHolder.TRADE_GOLD,n.intValue());
									}
								}
								catch(NumberFormatException ex) {
									JOptionPane.showMessageDialog(goldLabel,"Invalid number!","Error!",JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					}
				});
				box = Box.createHorizontalBox();
				box.add(Box.createHorizontalGlue());
				box.add(new JLabel("Inventory:"));
				addButton = new JButton("Add");
				addButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						addInventory();
					}
				});
				box.add(addButton);
				removeButton = new JButton("Remove");
				removeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						removeInventory();
					}
				});
				box.add(removeButton);
				box.add(Box.createHorizontalGlue());
				box.add(new JLabel("Discoveries:"));
				shareButton = new JButton("Add");
				shareButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						addDiscoveries();
					}
				});
				box.add(shareButton);
				unshareButton = new JButton("Remove");
				unshareButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						removeDiscoveries();
					}
				});
				box.add(unshareButton);
				box.add(Box.createHorizontalGlue());
				approveBox = new JCheckBox("Approve",!useApprove); // Default checked if not using approve
				approveBox.setFocusable(false);
				approveBox.setOpaque(false);
				approveBox.setBackground(MagicRealmColor.DISCOVERY_HIGHLIGHT_COLOR);
				approveBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						boolean sel = approveBox.isSelected();
						approveBox.setOpaque(sel);
						
						String message = approveBox.isSelected()?
								RealmDirectInfoHolder.TRADE_ACCEPT:
									RealmDirectInfoHolder.TRADE_UNACCEPT;
						sendMessage(message);
						updateControls();
					}
				});
				if (useApprove) {
					box.add(approveBox);
				}
				add(box,"South");
			}
			else {
				approveLabel = new JLabel("APPROVED");
				approveLabel.setOpaque(true);
				approveLabel.setBackground(MagicRealmColor.DISCOVERY_HIGHLIGHT_COLOR);
				approveLabel.setVisible(false);
				if (useApprove) {
					add(approveLabel,"South");
				}
			}
		}
		private void addInventory() {
			ArrayList unpresentedInventory = new ArrayList();
			for (Iterator i=character.getInventory().iterator();i.hasNext();) {
				GameObject item = (GameObject)i.next();
				Inventory inventory = new Inventory(item);
				if (inventory.canDrop()) {
					unpresentedInventory.add(item);
				}
			}
			unpresentedInventory.removeAll(onTheTable);
			if (!unpresentedInventory.isEmpty()) {
				RealmTradeDialog chooser = new RealmTradeDialog(CharacterTradeFrame.this,"Add items to TRADE",true,true,false);
				chooser.setTradeObjects(unpresentedInventory);
				chooser.setVisible(true);
				ArrayList<GameObject> newInventory = chooser.getSelectedObjects();
				if (newInventory!=null && !newInventory.isEmpty()) {
					ArrayList<GameObject> mustDeactivate = new ArrayList<GameObject>();
					for (GameObject go:newInventory) {
						if (go.hasThisAttribute(Constants.MUST_DEACTIVATE) && go.hasThisAttribute(Constants.ACTIVATED)) {
							mustDeactivate.add(go);
							RealmComponent rc = RealmComponent.getRealmComponent(go);
							JOptionPane.showMessageDialog(CharacterTradeFrame.this,"You must deactivate the "+go.getName()+" to trade it.","Deactivate Before Trade!",JOptionPane.PLAIN_MESSAGE,rc.getIcon());
						}
					}
					newInventory.removeAll(mustDeactivate);
					addInventory(newInventory);
					sendMessage(RealmDirectInfoHolder.TRADE_ADD_OBJECTS,newInventory);
				}
			}
		}
		public void addInventory(ArrayList<GameObject> newInventory) {
			onTheTable.addAll(newInventory);
			view.clearSelected();
			view.addObjects(newInventory);
			view.repaint();
			repaintInv();
		}
		private void removeInventory() {
			GameObject[] selGo = view.getSelectedGameObjects();
			if (selGo.length>0) {
				ArrayList<GameObject> toRemove = new ArrayList<GameObject>(Arrays.asList(selGo));
				removeInventory(toRemove);
				sendMessage(RealmDirectInfoHolder.TRADE_REMOVE_OBJECTS,toRemove);
			}
		}
		public void removeInventory(ArrayList<GameObject> toRemove) {
			onTheTable.removeAll(toRemove);
			view.clearSelected();
			view.removeAll();
			view.addObjects(onTheTable);
			repaintInv();
		}
		public void addDiscoveries() {
			boolean activeTransfer = active.getGameObject().equals(character.getGameObject());
			CharacterWrapper from = activeTransfer?active:include;
			CharacterWrapper to = activeTransfer?include:active;
			
			ArrayList<String> dees = new ArrayList<String>();
			
			// Treasure Locations
			ArrayList temp = from.getCurrentClearingKnownTreasureLocations(true);
			if (!temp.isEmpty()) {
				for (Iterator i=temp.iterator();i.hasNext();) {
					String tl = (String)i.next();
					String test = tl;
					int paren = test.indexOf(" ( + ");
					if (paren>=0) {
						test = test.substring(0,paren); // Only test the pre-paren portion
					}
					if (!to.hasTreasureLocationDiscovery(test)) {
						dees.add(TREASURE_LOCATION+tl);
					}
				}
			}
			
			// Hidden Paths
			temp = from.getCurrentClearingKnownHiddenPaths();
			if (!temp.isEmpty()) {
				for (Iterator i=temp.iterator();i.hasNext();) {
					String hp = (String)i.next();
					if (!to.hasHiddenPathDiscovery(hp)) {
						dees.add(HIDDEN_PATH+hp);
					}
				}
			}
			
			// Secret Passages
			temp = from.getCurrentClearingKnownSecretPassages();
			if (!temp.isEmpty()) {
				for (Iterator i=temp.iterator();i.hasNext();) {
					String sp = (String)i.next();
					if (!to.hasSecretPassageDiscovery(sp)) {
						dees.add(SECRET_PASSAGE+sp);
					}
				}
			}
			
			// Gates and Guilds
			temp = from.getCurrentClearingKnownOtherChits();
			if (!temp.isEmpty()) {
				for (Iterator i=temp.iterator();i.hasNext();) {
					String sp = (String)i.next();
					if (!to.hasSecretPassageDiscovery(sp)) {
						dees.add(GATE+sp);
					}
				}
			}
			
			dees.removeAll(discoveries);
			
			if (!dees.isEmpty()) {
				ListChooser chooser = new ListChooser(CharacterTradeFrame.this,"Discoveries to share?",dees);
				chooser.setSize(300,400);
				chooser.setLocationRelativeTo(CharacterTradeFrame.this);
				chooser.setVisible(true);
				ArrayList<String> newDiscoveries = new ArrayList<String>(chooser.getSelectedItems());
				if (newDiscoveries!=null && !newDiscoveries.isEmpty()) {
					addDiscoveries(newDiscoveries);
					sendMessageStrings(RealmDirectInfoHolder.TRADE_ADD_DISC,newDiscoveries);
				}
			}
		}
		public void addDiscoveries(Collection newDiscoveries) {
			discoveries.addAll(newDiscoveries);
			Collections.sort(discoveries);
			discoveryModel.fireChange();
			discoveryList.repaint();
		}
		public void removeDiscoveries() {
			ArrayList toRemove = new ArrayList();
			int[] sel = discoveryList.getSelectedIndices();
			for (int i=0;i<sel.length;i++) {
				toRemove.add(discoveries.get(sel[i]));
			}
			if (!toRemove.isEmpty()) {
				removeDiscoveries(toRemove);
				sendMessageStrings(RealmDirectInfoHolder.TRADE_REMOVE_DISC,toRemove);
			}
		}
		public void removeDiscoveries(Collection toRemove) {
			discoveries.removeAll(toRemove);
			discoveryModel.fireChange();
			discoveryList.repaint();
		}
		public void setGold(int gold) {
			goldLabel.setText(String.valueOf(gold));
		}
		public void setApprove(boolean val) {
			approveLabel.setVisible(val);
		}
		public boolean isApproved() {
			return approveLabel==null?
					approveBox.isSelected():
					approveLabel.isVisible();
		}
		public CharacterWrapper getCharacter() {
			return character;
		}
		public ArrayList<GameObject> getOnTheTable() {
			return onTheTable;
		}
		public ArrayList<String> getDiscoveries() {
			return discoveries;
		}
		public int getGold() {
			return Integer.valueOf(goldLabel.getText()).intValue();
		}
	}
	
	private class DiscoveryListModel extends AbstractListModel {
		private ArrayList data;
		public DiscoveryListModel(ArrayList data) {
			this.data = data;
		}
		public int getSize() {
			return data.size();
		}

		public Object getElementAt(int index) {
			if (index<data.size()) {
				return data.get(index);
			}
			return null;
		}
		public void fireChange() {
			fireContentsChanged(this,0,data.size()-1);
		}
	}
	
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		
		RealmLoader loader = new RealmLoader();
		RealmGameHandler handler = new RealmGameHandler(null,"ip",47474,"name","pass","","",false);
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		CharacterWrapper active = new CharacterWrapper(loader.getData().getGameObjectByName("Wizard"));
		active.setPlayerName("name");
		active.setGold(10);
		active.setCharacterLevel(4);
		active.fetchStartingInventory(new JFrame(),loader.getData(),false);
		CharacterWrapper include = new CharacterWrapper(loader.getData().getGameObjectByName("Elf"));
		include.setPlayerName("name");
		include.setGold(10);
		include.setCharacterLevel(4);
		include.fetchStartingInventory(new JFrame(),loader.getData(),false);
		CharacterTradeFrame frame = handler.createCharacterTradeFrame(active,include);
		frame.setVisible(true);
	}
}