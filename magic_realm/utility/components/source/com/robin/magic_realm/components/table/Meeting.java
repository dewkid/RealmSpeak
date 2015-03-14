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

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.ArmorChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.swing.RealmPaymentDialog;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public abstract class Meeting extends Trade {
	
	public static final String BLOCK_BATTLE = "Block/Battle";
	
	protected GameObject merchandise; // might be null if hiring or rolling for meeting
	protected Collection hireGroup; // might be null if trading or rolling for meeting
	
	protected boolean blockBattle;
	
	public Meeting(JFrame frame,TradeInfo trader,GameObject merchandise,Collection hireGroup) {
		super(frame,trader);
		this.merchandise = merchandise;
		this.hireGroup = hireGroup;
		this.blockBattle = false;
	}
	/**
	 * Override method here to guarantee specificAction is applied
	 */
	public void setNewTable(RealmTable newTable) {
		if (specificAction.length()>0) {
			if (newTable instanceof Meeting) { // guarantees specific action is translated across
				((Meeting)newTable).setSpecificAction(specificAction.substring(1)); // trim comma
			}
		}
		super.setNewTable(newTable);
	}
	public boolean isBlockBattle() {
		return blockBattle;
	}
	public String getTableName(boolean longDescription) {
		return "Buy from "+tradeInfo.getName()+" (as "+getMeetingTableName()+")";
	}
	public String getTableKey() {
		return "Meeting"+specificAction;
	}
	public abstract String getMeetingTableName();
	protected void doBlockBattle(CharacterWrapper character) {
		character.setBlocked(true);
		if ((merchandise!=null || hireGroup!=null) && character.isHidden()) { // only cause character to become unhidden if doing a TRADE
			character.setHidden(false);
		}
		blockBattle = true;
	}
	protected boolean useNoNegativeRule() {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(tradeInfo.getGameData());
		return hostPrefs.hasPref(Constants.HOUSE1_NO_NEGATIVE_POINTS);
	}
	protected boolean useDeclineOpportunityRule() {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(tradeInfo.getGameData());
		return hostPrefs.hasPref(Constants.HOUSE2_DECLINE_OPPORTUNITY);
	}
	protected String doOpportunity(CharacterWrapper character,Meeting newTable) {
		if (useDeclineOpportunityRule() && (merchandise!=null || hireGroup!=null)) { // only ask the question, if this is a TRADE or HIRE (not for battling results!!)
			int ret = JOptionPane.showConfirmDialog(
					getParentFrame(),
					"You have rolled OPPORTUNITY.  Do wish to keep this result, or decline it and\n"
					+"take the result from next result down on the "+getMeetingTableName()+" table?\n\n"
					+"   Answer YES to take the opportunity, and get a new roll on the "+newTable.getMeetingTableName()+" table"
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
	protected void doInsult(CharacterWrapper character) {
		String pronoun = character.getGameObject().getThisAttribute("pronoun");
		/*
		 * Character can choose to lose 5 notoriety points and get a No Deal result - otherwise block/battle
		 */
		if (character.getNotoriety()>=5 || !useNoNegativeRule()) {
			int ret = JOptionPane.showConfirmDialog(
					getParentFrame(),
					"The "+character.getGameObject().getName()
					+" must lose 5 notoriety points (current notoriety="
					+character.getRoundedNotoriety()
					+"),\nor be blocked by the "
					+getDenizenName()
					+".\n\nDo you want to lose the points?","INSULT",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE,getRollerImage());
			if (ret==JOptionPane.YES_OPTION) {
				character.addNotoriety(-5);
				return;
			}
		}
		else {
			StringBuffer message = new StringBuffer();
			message.append("The "+character.getGameObject().getName()+" has insulted the "+tradeInfo.getThisAttribute("native"));
			message.append(" but does not have the option to subtract\nfrom notoriety because "+pronoun);
			message.append(" does not have enough points (5 needed).");
			JOptionPane.showMessageDialog(getParentFrame(),message,"Insult - Block/Battle",JOptionPane.WARNING_MESSAGE);
		}
		
		doBlockBattle(character);
	}
	protected void doChallenge(CharacterWrapper character) {
		String pronoun = character.getGameObject().getThisAttribute("pronoun");
		/*
		 * Character can choose to lose 5 fame points and get a No Deal result - otherwise block/battle
		 * DISGUST curse makes it impossible to pay fame points
		 */
		if ((character.getFame()>=5 || !useNoNegativeRule()) && !character.hasCurse(Constants.DISGUST)) {
			int ret = JOptionPane.showConfirmDialog(
					getParentFrame(),
					"The "+character.getGameObject().getName()
					+" must lose 5 fame points (current fame="
					+character.getRoundedFame()
					+"),\nor be blocked by the "
					+getDenizenName()
					+".\n\nDo you want to lose the points?","CHALLENGE",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE,getRollerImage());
			if (ret==JOptionPane.YES_OPTION) {
				character.addFame(-5);
				return;
			}
		}
		else {
			StringBuffer message = new StringBuffer();
			message.append("The "+character.getGameObject().getName()+" has been challenged by the "+tradeInfo.getThisAttribute("native"));
			message.append(" but does not have the option to subtract\nfrom fame because "+pronoun);
			if (character.hasCurse(Constants.DISGUST)) {
				message.append(" is currently cursed with DISGUST.");
			}
			else {
				message.append(" does not have enough points (5 needed).");
			}
			JOptionPane.showMessageDialog(getParentFrame(),message,"Challenge - Block/Battle",JOptionPane.WARNING_MESSAGE,getRollerImage());
		}
		
		doBlockBattle(character);
	}
	protected void processPrice(CharacterWrapper character,int mult) {
		if (merchandise!=null) {
			RealmComponent rc = RealmComponent.getRealmComponent(merchandise);
			if (rc.isArmor() && ((ArmorChitComponent)rc).isDamaged()) {
				repairingMerchandise(character,mult);
			}
			else {
				buyingMerchandise(character,mult);
			}
		}
		else if (hireGroup!=null) {
			hiringNatives(character,mult);
		}
	}
	protected void repairingMerchandise(CharacterWrapper character,int mult) {
		// Everything is handled by the RealmPaymentDialog now.
		RealmPaymentDialog dialog = new RealmPaymentDialog(getParentFrame(),"REPAIR",character,tradeInfo,merchandise,mult,getListener());
		dialog.setVisible(true);
	}
	protected void buyingMerchandise(CharacterWrapper character,int mult) {
		// Everything is handled by the RealmPaymentDialog now.
		RealmPaymentDialog dialog = new RealmPaymentDialog(getParentFrame(),"TRADE",character,tradeInfo,merchandise,mult,getListener());
		dialog.setVisible(true);
	}
	public void hiringNatives(CharacterWrapper character,int mult) {
		int basePrice = 0;
		RealmComponent last = null;
		for (Iterator n=hireGroup.iterator();n.hasNext();) {
			last = (RealmComponent)n.next();
			basePrice += last.getGameObject().getThisInt("base_price");
		}
		//if (basePrice>0) { // Was this necessary?
		int askingPrice = basePrice * mult;
		
		String groupName = tradeInfo.getThisAttribute("native");
		if (hireGroup.size()==1 || groupName==null) {
			groupName = tradeInfo.getName();
			if (hireGroup.size()>1) {
				groupName = groupName+"s";
			}
		}
		groupName = StringUtilities.capitalize(groupName);
		
		StringBuffer sb = new StringBuffer();
		if (getTradeInfo().getNoDrinksReason()!=null) {
			sb.append("(");
			sb.append(getTradeInfo().getNoDrinksReason());
			sb.append(")\n");
		}
		sb.append("You can hire the ");
		sb.append(groupName);
		boolean isBoon = askingPrice==0 && !last.isTraveler();
		if (isBoon) {
			if (last.isMonster()) {
				if (hireGroup.size()>1) {
					sb.append("s");
				}
				askingPrice = basePrice;
			}
			else {
				sb.append(" as a boon, or ");
			}
		}
		sb.append(" for ");
		sb.append(askingPrice==0?basePrice:askingPrice);
		sb.append(" gold.");
		
		String offerTitle = tradeInfo.getName()+" Offer - Price x "+mult;
		
		int charGold = character.getRoundedGold();
		if (character.hasCurse(Constants.ASHES)) {
			sb.append("  Unfortunately, your gold is worthless as long as you have the ASHES curse.");
			JOptionPane.showMessageDialog(getParentFrame(),sb.toString(),offerTitle,JOptionPane.INFORMATION_MESSAGE,last.getIcon());
		}
		else if (charGold>=askingPrice) {
			sb.append("  Will you hire?");
			int ret = JOptionPane.showConfirmDialog(getParentFrame(),sb.toString(),offerTitle,
						JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE,last.getIcon());
			
			if (ret==JOptionPane.YES_OPTION) {
				if (isBoon) {
					// special case for BOON
					ret = JOptionPane.showConfirmDialog(getParentFrame(),"Take the boon?",tradeInfo.getName()+" Offers Boon",
								JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE,last.getIcon());
					if (ret==JOptionPane.YES_OPTION) {
						character.changeRelationship(tradeInfo.getGameObject(),-1);
						character.getGameObject().add(RealmPaymentDialog.createBoon(tradeInfo.getGameObject(),basePrice));
					}
					else {
						askingPrice = basePrice;
					}
				}
				
				if (charGold>=askingPrice) {
					// Subtract price
					character.addGold(-askingPrice);
					
					// Hire the group!
					for (Iterator n=hireGroup.iterator();n.hasNext();) {
						RealmComponent rc = (RealmComponent)n.next();
						character.addHireling(rc.getGameObject());
					}
				}
				else {
					JOptionPane.showMessageDialog(
							getParentFrame(),
							"You only have "+charGold+" gold, so without the BOON, you cannot hire.",
							offerTitle,
							JOptionPane.INFORMATION_MESSAGE,
							last.getIcon());
				}
			}
		}
		else {
			sb.append("  You only have "+charGold+" gold.");
			JOptionPane.showMessageDialog(getParentFrame(),sb.toString(),offerTitle,JOptionPane.INFORMATION_MESSAGE,last.getIcon());
		}
	}
	public static Meeting createMeetingTable(JFrame frame,CharacterWrapper character,TileLocation currentLocation,RealmComponent trader,RealmComponent merchandise,Collection hireGroup,int ignoreBuyDrinksLimit) {
		TradeInfo tradeInfo = getTradeInfo(frame,character,trader,currentLocation,ignoreBuyDrinksLimit,hireGroup==null?0:hireGroup.size());
		
		GameObject merchObj = merchandise==null?null:merchandise.getGameObject();
		
		Meeting meeting = null;
		switch(tradeInfo.getRelationshipType()) {
			case RelationshipType.ENEMY:
				meeting = new MeetingEnemy(frame,tradeInfo,merchObj,hireGroup);
				break;
			case RelationshipType.UNFRIENDLY:
				meeting = new MeetingUnfriendly(frame,tradeInfo,merchObj,hireGroup);
				break;
			case RelationshipType.NEUTRAL:
				meeting = new MeetingNeutral(frame,tradeInfo,merchObj,hireGroup);
				break;
			case RelationshipType.FRIENDLY:
				meeting = new MeetingFriendly(frame,tradeInfo,merchObj,hireGroup);
				break;
			case RelationshipType.ALLY:
				meeting = new MeetingAlly(frame,tradeInfo,merchObj,hireGroup);
				break;
		}
		return meeting;
	}
}