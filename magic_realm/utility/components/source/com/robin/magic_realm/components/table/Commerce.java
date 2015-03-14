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
package com.robin.magic_realm.components.table;

import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.TradeType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public abstract class Commerce extends Trade {
	
	protected HostPrefWrapper hostPrefs;
	protected Collection merchandise;
	private boolean blockBattle = false;
	
	public Commerce(JFrame frame,TradeInfo tradeInfo,Collection merchandise,HostPrefWrapper hostPrefs) {
		super(frame,tradeInfo);
		this.merchandise = merchandise;
		this.hostPrefs = hostPrefs;
	}
	/**
	 * Override method here to guarantee specificAction is applied
	 */
	public void setNewTable(RealmTable newTable) {
		if (specificAction.length()>0) {
			if (newTable instanceof Commerce) { // guarantees specific action is translated across
				((Commerce)newTable).setSpecificAction(specificAction.substring(1)); // trim comma
			}
		}
		super.setNewTable(newTable);
	}
	protected String doOpportunity(CharacterWrapper character,Commerce newTable) {
		if (useDeclineOpportunityRule()) {
			int ret = JOptionPane.showConfirmDialog(
					getParentFrame(),
					"You have rolled OPPORTUNITY.  Do wish to keep this result, or decline it and\n"
					+"take the result from next result down on the "+getCommerceTableName()+" table?\n\n"
					+"   Answer YES to take the opportunity, and get a new roll on the "+newTable.getCommerceTableName()+" table"
					+"   Answer NO to decline the opportunity, take the result from rolling a 2 instead",
					"OPPORTUNITY",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (ret==JOptionPane.NO_OPTION) {
				return "Opportunity(declined) - "+applyTwo(character);
			}
		}
		setNewTable(newTable);
		return "Opportunity";
	}
	public String getTableName(boolean longDescription) {
		if (getCommerceTableName().length()==0) {
			return "Sell to "+tradeInfo.getName();
		}
		return "Sell to "+tradeInfo.getName()+" (as "+getCommerceTableName()+")";
	}
	public String getTableKey() {
		return "Meeting"+specificAction; // Commerce is really a meeting table type
	}
	protected boolean useDeclineOpportunityRule() {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(tradeInfo.getGameData());
		return hostPrefs.hasPref(Constants.HOUSE2_DECLINE_OPPORTUNITY);
	}
	public abstract String getCommerceTableName();
	public boolean isBlockBattle() {
		return blockBattle;
	}
	protected void doBlockBattle(CharacterWrapper character) {
		character.setBlocked(true);
		if (character.isHidden()) { // Since Commerce is ALWAYS about TRADE, then you become unhidden here always
			character.setHidden(false);
		}
		blockBattle = true;
	}
	protected int getTotalBasePrice() {
		int totalPrice = 0;
		for (Iterator i=merchandise.iterator();i.hasNext();) {
			RealmComponent merchandise = (RealmComponent)i.next();
			totalPrice += TreasureUtility.getBasePrice(tradeInfo.getTrader(),merchandise);
		}
		return totalPrice;
	}
	protected String offerGold(CharacterWrapper character,int bonus) {
		int total = getTotalBasePrice()+bonus;
		if (total>0) {
			String offer = (bonus>0?"+":"")+(bonus==0?"":String.valueOf(bonus));
			String offerFull = offer+" (total="+total+")";
			int ret = JOptionPane.showConfirmDialog(getParentFrame(),"They offer gold "+offerFull+".\n\nWill you sell?","Offer Gold "+offer,JOptionPane.YES_NO_OPTION);
			if (ret==JOptionPane.YES_OPTION) {
				return process(character,bonus);
			}
			return "Declined Offer";
		}
		return "No Deal";
	}
	protected String demandGold(CharacterWrapper character,int bonus) {
		int total = getTotalBasePrice()+bonus;
		if (total>0) {
			String offer = (bonus>0?"+":"")+(bonus==0?"":String.valueOf(bonus));
			String offerFull = offer+" (total="+total+")";
			int ret = JOptionPane.showConfirmDialog(getParentFrame(),"They demand gold "+offerFull+".\n\nWill you sell?","Demand Gold "+offer,JOptionPane.YES_NO_OPTION);
			if (ret==JOptionPane.YES_OPTION) {
				return process(character,bonus);
			}
		}
		doBlockBattle(character);
		return "Block/Battle";
	}
	protected String process(CharacterWrapper character,int bonus) {
		String result = "";
		// Can be multiple items sold
		int totalGoldReceieved = bonus;
		StringBuffer sb = new StringBuffer();
		ArrayList<GameObject> itemList = new ArrayList<GameObject>();
		for (Iterator i=merchandise.iterator();i.hasNext();) {
			RealmComponent merchandise = (RealmComponent)i.next();
			itemList.add(merchandise.getGameObject());
			int basePrice = TreasureUtility.getBasePrice(tradeInfo.getTrader(),merchandise); // Without commerce rules, the basePrice is the selling price (already figured in by the getBasePrice method)
			sb.append("You sold the "+merchandise.getGameObject().getName()+" for "+basePrice+" gold.\n");
			character.addGold(basePrice);
			totalGoldReceieved += basePrice;
			TradeUtility.loseItem(character,merchandise.getGameObject(),tradeInfo.getGameObject(),hostPrefs.hasPref(Constants.OPT_GRUDGES));
		}
		
		// Finally, make sure the inventory status is still legal 
		character.checkInventoryStatus(getParentFrame(),null,getListener());
		
		if (bonus<0) {
			sb.append("Minus "+bonus+" gold.");
		}
		if (bonus>0) {
			sb.append("Plus "+bonus+" gold.");
		}
		if (bonus!=0) {
			character.addGold(bonus,true); // ignore curses here, because its a NET amount which involves some subtraction
		}
		if (merchandise.size()==1) {
			RealmComponent item = (RealmComponent)merchandise.iterator().next();
			result = "Total received for the "+item.getGameObject().getName()+" was "+totalGoldReceieved+" gold.";
		}
		else {
			result = "Sold "+merchandise.size()+" items for "+totalGoldReceieved+" gold: ";
		}
		
		QuestRequirementParams params = new QuestRequirementParams();
		params.actionType = CharacterActionType.Trading;
		params.actionName = TradeType.Sell.toString();
		params.objectList = itemList;
		params.targetOfSearch = tradeInfo.getGameObject();
		character.testQuestRequirements(getParentFrame(),params);
		
		JTextArea area = new JTextArea();
		area.setFont(UIManager.getFont("Label.font"));
		area.setText(sb.toString());
		area.setEditable(false);
		area.setOpaque(false);
		JOptionPane.showMessageDialog(getParentFrame(),
				area,
				"Selling goods",
				JOptionPane.INFORMATION_MESSAGE);
		return result;
	}
	public static Commerce createCommerceTable(JFrame frame,CharacterWrapper character,TileLocation currentLocation,RealmComponent trader,Collection merchandise,int ignoreBuyDrinksLimit,HostPrefWrapper hostPrefs) {
		if (!hostPrefs.hasPref(Constants.OPT_COMMERCE)) {
			return new CommerceNone(frame,new TradeInfo(trader),merchandise,hostPrefs);
		}
		
		TradeInfo tradeInfo = getTradeInfo(frame,character,trader,currentLocation,ignoreBuyDrinksLimit);
		
		Commerce commerce = null;
		switch(tradeInfo.getRelationshipType()) {
			case RelationshipType.ENEMY:
				commerce = new CommerceEnemy(frame,tradeInfo,merchandise,hostPrefs);
				break;
			case RelationshipType.UNFRIENDLY:
				commerce = new CommerceUnfriendly(frame,tradeInfo,merchandise,hostPrefs);
				break;
			case RelationshipType.NEUTRAL:
				commerce = new CommerceNeutral(frame,tradeInfo,merchandise,hostPrefs);
				break;
			case RelationshipType.FRIENDLY:
				commerce = new CommerceFriendly(frame,tradeInfo,merchandise,hostPrefs);
				break;
			case RelationshipType.ALLY:
				commerce = new CommerceAlly(frame,tradeInfo,merchandise,hostPrefs);
				break;
		}
		return commerce;
	}
}