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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TradeInfo;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.TradeType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class RealmPaymentDialog extends AggressiveDialog {
	
	private int multiplier;
	private int basePrice;
	private int askingPrice;
	private int charGold;
	private boolean boonOffer;
	private boolean repair;
	private TradeInfo tradeInfo;
	private GameObject merchandise;
	private CharacterWrapper character;
	private HostPrefWrapper hostPrefs;
	
	private ArrayList onTheTable;
	
	private JTextArea info;
	private JLabel currentInventorySale;
	private JLabel currentRequiredGold;
	
	private RealmObjectPanel invTradeView;
	private JButton addInventory;
	private JButton remInventory;
	
	private JButton payButton;
	private JButton boonButton;
	private JButton cancelButton;
	
	private RealmComponent mc;
	
	private JFrame mainFrame;
	
	private ChangeListener listener;
		
	public RealmPaymentDialog(JFrame parent,String title,CharacterWrapper character,TradeInfo tradeInfo,GameObject mer,int mul,ChangeListener listener) {
		super(parent,title,true);
		this.mainFrame = parent;
		this.character = character;
		this.tradeInfo = tradeInfo;
		this.merchandise = mer;
		this.multiplier = mul;
		this.listener = listener;
		hostPrefs = HostPrefWrapper.findHostPrefs(character.getGameObject().getGameData());
		mc = RealmComponent.getRealmComponent(merchandise);
		repair = mc.isArmor() && ((ArmorChitComponent)mc).isDamaged();
		onTheTable = new ArrayList();
		initComponents();
		setLocationRelativeTo(parent);
	}
	private Border getLabelBorder() {
		Border b = BorderFactory.createEtchedBorder();
		Border a = BorderFactory.createEmptyBorder(5,5,5,5);
		Border r = BorderFactory.createCompoundBorder(b,a);
		return r;
	}
	private JLabel createNumberLabel() {
		return createNumberLabel(0);
	}
	private JLabel createNumberLabel(int num) {
		JLabel label = new JLabel(String.valueOf(num),JLabel.CENTER);
		ComponentTools.lockComponentSize(label,40,25);
		label.setBorder(getLabelBorder());
		return label;
	}
	private void initComponents() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(500,400);
		getContentPane().setLayout(new BorderLayout());
		
		// North Panel - Buying Info
		JPanel north = new JPanel(new BorderLayout(10,10));
		north.setBorder(BorderFactory.createEtchedBorder());
		north.add(new JLabel(mc.getFaceUpIcon()),"West");
		info = new JTextArea(5,40);
		info.setFont(UIManager.getFont("Label.font"));
		info.setEditable(false);
		info.setOpaque(false);
		north.setBackground(new Color(205,255,205));
		north.add(info,"Center");
		if (tradeInfo.getNoDrinksReason()!=null) {
			JLabel noDrinksReason = new JLabel(tradeInfo.getNoDrinksReason());
			noDrinksReason.setOpaque(true);
			noDrinksReason.setBackground(Color.yellow);
			north.add(noDrinksReason,"North");
			setSize(500,420);
		}
		getContentPane().add(north,"North");
		setupInfo();
		
		// East Panel - Tally and buttons
		UniformLabelGroup group = new UniformLabelGroup();
		Box box = Box.createVerticalBox();
		Box line;
		
		// Price
		line = group.createLabelLine("Item Price");
		JLabel currentPrice = createNumberLabel(askingPrice);
		line.add(currentPrice);
		line.add(Box.createHorizontalGlue());
		box.add(line);
		
		// Trade Inventory
		line = group.createLabelLine("Trade Item(s) Value");
		currentInventorySale = createNumberLabel();
		line.add(currentInventorySale);
		box.add(line);
		
		// Gold
		line = group.createLabelLine("Gold Needed");
		currentRequiredGold = createNumberLabel();
//		currentRequiredGold.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),currentRequiredGold.getBorder()));
		line.add(currentRequiredGold);
		box.add(line);
		box.add(Box.createVerticalStrut(5));
		line = Box.createHorizontalBox();
		line.add(Box.createHorizontalGlue());
		line.add(new JLabel("(You currently have "+charGold+" gold.)"));
		box.add(line);
		box.add(Box.createVerticalGlue());
		
		payButton = new JButton("Buy Item Now");
		payButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doPay();
			}
		});
		ComponentTools.lockComponentSize(payButton,100,40);
		box.add(payButton);
		box.add(Box.createVerticalStrut(10));
		boonButton = new JButton("Take Boon");
		boonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doBoon();
			}
		});
		ComponentTools.lockComponentSize(boonButton,100,40);
		boonButton.setEnabled(boonOffer);
		box.add(boonButton);
		box.add(Box.createVerticalStrut(10));
		cancelButton = new JButton("No Deal!");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doCancel();
			}
		});
		ComponentTools.lockComponentSize(cancelButton,100,40);
		box.add(cancelButton);
		box.add(Box.createVerticalGlue());
		getContentPane().add(box,"East");
		
		// Center - Inventory trades
		JPanel center = new JPanel(new BorderLayout(5,5));
		line = Box.createHorizontalBox();
		line.add(new JLabel("Trade Items:"));
		line.add(Box.createHorizontalGlue());
		center.add(line,"North");
		
		invTradeView = new RealmObjectPanel(true,false);
		center.add(new JScrollPane(invTradeView),"Center");
		line = Box.createHorizontalBox();
		line.add(Box.createHorizontalGlue());
		addInventory = new JButton("Add Item");
		addInventory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doAdd();
			}
		});
		line.add(addInventory);
		line.add(Box.createHorizontalGlue());
		remInventory = new JButton("Remove Item");
		remInventory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doRemove();
			}
		});
		line.add(remInventory);
		line.add(Box.createHorizontalGlue());
		center.add(line,"South");
		getContentPane().add(center,"Center");
		
		updateOffers();
	}
	private void doPay() {
		int bonusGold = 0;
		if (askingPrice<0) {
			bonusGold = -askingPrice;
		}
		askingPrice -= getTradeItemValue();
		if (askingPrice<0) {
			askingPrice = 0; // The seller does NOT give change for trade items!!
		}
		character.addGold(-askingPrice);
		character.addGold(bonusGold); // This happens when a seller is paying you to take it off his hands!
		loseTradeItems();
		receiveItem();
		safeExit();
	}
	private void doBoon() {
		character.changeRelationship(tradeInfo.getGameObject(),-1);
		character.getGameObject().add(createBoon(tradeInfo.getGameObject(),basePrice));
		receiveItem();
		safeExit();
	}
	public static GameObject createBoon(GameObject denizen,int basePrice) {
		GameObject boon = denizen.getGameData().createNewObject();
		boon.setName("Boon");
		
		String groupName = denizen.getThisAttribute("native");
		if (groupName==null) {
			groupName = denizen.getThisAttribute("visitor");
		}
		
		boon.setThisAttribute("boon",groupName);
		boon.setThisAttribute("base_price",basePrice);
		return boon;
	}
	private void loseTradeItems() {
		for (Iterator i=onTheTable.iterator();i.hasNext();) {
			GameObject item = (GameObject)i.next();
			TradeUtility.loseItem(character,item,tradeInfo.getGameObject(),hostPrefs.hasPref(Constants.OPT_GRUDGES));
		}
	}
	private void receiveItem() {
		// First check for boons:  handled a bit differently
		if (mc.isBoon()) {
			// Boons are basically IOUs - once repaid, they are removed from your inventory
			character.getGameObject().remove(merchandise);
			
			// Restore one level of friendliness
			character.changeRelationship(tradeInfo.getGameObject(),1);
			
			RealmLogging.logMessage(character.getGameObject().getName(),"Repaid boon, and restored one level of friendliness with "+tradeInfo.getGameObject().getName());
			return;
		}
		
		// Some treasures have special value to native groups...
		int fame = TreasureUtility.getFamePrice(merchandise,tradeInfo.getGameObject());
		if (fame>0 && hostPrefs.hasPref(Constants.OPT_GRUDGES)) {
			// Decrease friendship
			character.changeRelationship(tradeInfo.getGameObject(), -1);
			RealmLogging.logMessage(character.getGameObject().getName(),"Loses a level of friendliness with "+tradeInfo.getGameObject().getName()+".");
		}
		character.addFame(-fame);
		
		if (mc.isSpell()) {
			// spells are learned
			character.recordNewSpell(mainFrame,merchandise);
			RealmLogging.logMessage(character.getGameObject().getName(),"Learns a new spell from the "+tradeInfo.getGameObject().getName()+": "+merchandise.getName());
		}
		else {
			// otherwise, stuff is added
			if (mc.isCard()) {
				CardComponent card = (CardComponent)mc;
				if (!card.isFaceUp()) {
					card.setFaceUp();
				}
			}
			else if (mc.isArmor() && repair) {
				ArmorChitComponent armor = (ArmorChitComponent)mc;
				armor.setIntact(true);
			}
			RealmComponent rc = RealmComponent.getRealmComponent(merchandise);
			rc.setCharacterTimestamp(character);
			merchandise.setThisAttribute(Constants.TREASURE_NEW);
			character.getGameObject().add(merchandise);
			GameObject dwelling = tradeInfo.getGameObject();
			if (RealmComponent.getRealmComponent(tradeInfo.getGameObject()).isNativeLeader()) {
				dwelling = SetupCardUtility.getDenizenHolder(tradeInfo.getGameObject());
			}
			character.addNoteTrade(tradeInfo.getGameObject(),dwelling.getHold());
			character.checkInventoryStatus(mainFrame,merchandise,listener);
			RealmLogging.logMessage(character.getGameObject().getName(),"Buys the "+merchandise.getName()+" from the "+tradeInfo.getGameObject().getName());
		}
		
		QuestRequirementParams params = new QuestRequirementParams();
		params.actionType = CharacterActionType.Trading;
		params.actionName = TradeType.Buy.toString();
		params.objectList = new ArrayList<GameObject>();
		params.objectList.add(merchandise);
		params.targetOfSearch = tradeInfo.getGameObject();
		character.testQuestRequirements(mainFrame,params);
	}
	private void doCancel() {
		if (mc.isBoon()) {
			// Cancelling a boon repayment is permanent, so let the player know!
			int ret = JOptionPane.showConfirmDialog(parent,
					"Cancelling a boon repayment is permanent.  You will not get another opportunity\n"
					+" to repay this boon, and the level of friendliness lost can never be regained.\n\n"
					+"Are you sure you want to cancel?","Warning",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
			if (ret==JOptionPane.NO_OPTION) {
				return;
			}
			
			// Okay, remove the boon without restoring friendliness
			character.getGameObject().remove(merchandise);
		}
		safeExit();
	}
	private void safeExit() {
		setVisible(false);
		dispose();
	}
	private void doAdd() {
		ArrayList unpresentedInventory = new ArrayList();
		for (Iterator i=character.getInventory().iterator();i.hasNext();) {
			GameObject item = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(item);
			if (rc.isItem() && !rc.isNativeHorse()) {
				unpresentedInventory.add(item);
			}
		}
		unpresentedInventory.removeAll(onTheTable);
		if (!unpresentedInventory.isEmpty()) {
			RealmTradeDialog chooser = new RealmTradeDialog((JFrame)parent,"Add items to TRADE",true,true,false);
			chooser.setTradeObjects(unpresentedInventory);
			chooser.setVisible(true);
			Collection newInventory = chooser.getSelectedObjects();
			if (newInventory!=null && !newInventory.isEmpty()) {
				addInventory(newInventory);
			}
		}
	}
	private void addInventory(Collection newInventory) {
		onTheTable.addAll(newInventory);
		invTradeView.clearSelected();
		invTradeView.addObjects(newInventory);
		invTradeView.repaint();
		updateOffers();
	}
	private void doRemove() {
		GameObject[] selGo = invTradeView.getSelectedGameObjects();
		if (selGo.length>0) {
			Collection toRemove = new ArrayList(Arrays.asList(selGo));
			removeInventory(toRemove);
		}
	}
	private void removeInventory(Collection toRemove) {
		onTheTable.removeAll(toRemove);
		invTradeView.clearSelected();
		invTradeView.removeAll();
		invTradeView.addObjects(onTheTable);
		updateOffers();
	}
	private void setupInfo() {
		basePrice = TreasureUtility.getBasePrice(tradeInfo.getTrader(),mc);
		
		if (repair) { // the only reason you'd be buying damaged armor, is because you are repairing it.  (armor never stays damaged in a dwelling)
			basePrice = TreasureUtility.getBaseRepairPrice((ArmorChitComponent)mc);
		}
		
		if (basePrice>0) {
			askingPrice = basePrice * multiplier;
			
			StringBuffer sb = new StringBuffer();
			sb.append("PRICE x ");
			sb.append(multiplier==0?"1 or BOON":String.valueOf(multiplier));
			sb.append("    (Base Price = ");
			sb.append(basePrice);
			sb.append(")\n\n");
			sb.append("The ");
			sb.append(tradeInfo.getGameObject().getName());
			sb.append(" will ");
			sb.append(repair?"repair":"sell");
			sb.append(" the ");
			sb.append(merchandise.getName());
			sb.append(repair?" for":" to");
			sb.append(" you for ");
			sb.append(askingPrice==0?basePrice:askingPrice);
			sb.append(" gold");
			if (askingPrice==0 && !mc.isBoon()) { // boons can't be offered as a boon!!
				sb.append(",\nor give it to you as a BOON (lose 1 level friendliness)");
				askingPrice = basePrice;
				boonOffer = true;
			}
			sb.append(".");
			
			charGold = character.getRoundedGold();
			if (character.hasCurse(Constants.ASHES)) {
				sb.append("\nUnfortunately, your gold is worthless as long as you have the ASHES curse.");
				charGold = 0;
			}
			
			info.setText(sb.toString());
		}
		else {
			askingPrice = basePrice;
			
			StringBuffer sb = new StringBuffer();
			sb.append("The ");
			sb.append(tradeInfo.getGameObject().getName());
			sb.append(" will give the ");
			sb.append(merchandise.getName());
			sb.append(" to you");
			if (askingPrice<0) {
				sb.append("\nplus ");
				sb.append(-askingPrice);
				sb.append(" gold to take it off his hands");
			}
			sb.append(".");
			
			charGold = character.getRoundedGold();
			
			info.setText(sb.toString());
		}
	}
	private int getTradeItemValue() {
		int invSale = 0;
		for (Iterator i=onTheTable.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			invSale += TreasureUtility.getBasePrice(tradeInfo.getTrader(),RealmComponent.getRealmComponent(go));
		}
		return invSale;
	}
	private void updateOffers() {
		int invSale = getTradeItemValue();
		int needed = askingPrice-invSale;
		if (needed<0) needed=0; // no change made for trade items, so don't show negative numbers here
		
		currentInventorySale.setText(String.valueOf(invSale));
		currentRequiredGold.setText(String.valueOf(needed));
		currentRequiredGold.setForeground(needed<=charGold?Color.black:Color.red);
		payButton.setEnabled(needed<=charGold);
	}
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		
		RealmLoader loader = new RealmLoader();
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		CharacterWrapper active = new CharacterWrapper(loader.getData().getGameObjectByName("Wizard"));
		active.setPlayerName("name");
		active.setGold(90);
		active.setCharacterLevel(4);
		active.fetchStartingInventory(new JFrame(),loader.getData(),false);
//		active.applyCurse(Constants.ASHES);
		
		GameObject d = loader.getData().getGameObjectByName("Lancer HQ");
		TradeInfo ti = new TradeInfo(RealmComponent.getRealmComponent(d));
		ti.setNoDrinksReason("The Wizard could not afford to buy drinks.");
		GameObject m = loader.getData().getGameObjectByName("Golden Icon");
		
		JFrame dummy = new JFrame();
		dummy.setLocationRelativeTo(null);
		RealmPaymentDialog dialog = new RealmPaymentDialog(dummy,"Test",active,ti,m,2,null);
		dialog.setVisible(true);
		
		System.out.println("You are left with "+active.getGold()+" gold.");
		System.exit(0);
	}
}