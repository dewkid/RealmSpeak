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
package com.robin.magic_realm.components;

import java.awt.Graphics;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.*;
import com.robin.game.server.GameClient;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.util.StringBufferedList;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.RelationshipType;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.CharacterActionType;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class GoldSpecialChitComponent extends SquareChitComponent {
	
	private int clearings;
	private String targetName;

	protected GoldSpecialChitComponent(GameObject obj) {
		super(obj);
		lightColor = MagicRealmColor.GOLD;
		darkColor = MagicRealmColor.GOLD;
	}
	public String getLightSideStat() {
		return "this";
	}
	public String getDarkSideStat() {
		return "this";
	}

	public int getChitSize() {
		return S_CHIT_SIZE;
	}

	public String getName() {
		return GOLD_SPECIAL;
	}
	public GoldSpecialChitComponent getOtherSide() {
		String pairId = getGameObject().getThisAttribute("pairid");
		GameObject other = getGameObject().getGameData().getGameObject(Long.valueOf(pairId));
		return (GoldSpecialChitComponent)RealmComponent.getRealmComponent(other);
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		TextType tt;
		
		String name = gameObject.getName();
		tt = new TextType(name,getChitSize()-4,"BOLD");
		int h = tt.getHeight(g);
		int y = ((getChitSize()-h)>>1)-3;
		tt.draw(g,2,y,Alignment.Center);
	}
	public String generateHTML(CharacterWrapper character) {
		/*
		 * Like to have an HTML document (like the about box) that lists all the chits details:
		 * 	- time_limit
		 * 	- partner
		 * 	- foe
		 * 	- notoriety_cost
		 * 	- fame_cost
		 * 	- primary_target
		 * 	- secondary_target
		 * 	- reward (if any)
		 */
		
		GameData data = getGameObject().getGameData();
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
		GamePool pool = new GamePool(data.getGameObjects());
		
		String rowHeaderStart = "<tr><td align=\"right\" bgcolor=\"#33cc00\"><b>";
		String rowContentStart = "</b></td><td>";
		String rowEnd = "</td></tr>";
		
		StringBuffer text = new StringBuffer();
		text.append("<html><body><font size=\"-1\" face=\"Helvetical, Arial, sans-serif\">");
		text.append("<table cellspacing=\"2\">");
		boolean visitor = getGameObject().hasThisAttribute(Constants.VISITOR);
		if (visitor) {
			// Relationship to Character
			text.append(rowHeaderStart);
			text.append("Relationship:");
			text.append(rowContentStart);
			text.append(RealmUtility.getRelationshipNameFor(character,this));
			text.append(rowEnd);
			
			int small = 0;
			int large = 0;
			ArrayList<String> spellTypes = new ArrayList<String>();
			for (Iterator i=getGameObject().getHold().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				String treasure = go.getThisAttribute("treasure");
				if (treasure!=null) {
					if ("large".equals(treasure)) {
						large++;
					}
					else {
						small++;
					}
				}
				else {
					String spell = go.getThisAttribute("spell");
					if (spell!=null) {
						spellTypes.add(0,spell); // the zero is a hack to force the types to be in order
					}
				}
			}
			
			// Treasures
			if (large>0 || small>0) { // Does large really matter?  Will they ever see anything large?
				text.append(rowHeaderStart);
				text.append("Treasures:");
				text.append(rowContentStart);
				StringBufferedList list = new StringBufferedList();
				if (small>0) list.append(small+" small treasures");
				if (large>0) list.append(large+" large treasures");
				text.append(list.toString());
				text.append(rowEnd);
			}
			
			// Spells
			if (!spellTypes.isEmpty()) {
				text.append(rowHeaderStart);
				text.append("Spells:");
				text.append(rowContentStart);
				StringBufferedList list = new StringBufferedList();
				for (String val:spellTypes) {
					list.append(val);
				}
				text.append(list.toString());
				text.append(rowEnd);
			}
		}
		else {
			// Cost
			text.append(rowHeaderStart);
			text.append("Cost:");
			text.append(rowContentStart);
			int fame = getGameObject().getThisInt("fame_cost");
			int not = getGameObject().getThisInt("notoriety_cost");
			if (fame>0) text.append(fame+" Fame");
			if (fame>0 && not>0) text.append(", ");
			if (not>0) text.append(not+" Notoriety");
			text.append(rowEnd);
			
			// Time limit
			text.append(rowHeaderStart);
			text.append("Time Limit:");
			text.append(rowContentStart);
			text.append(getGameObject().getThisInt("time_limit"));
			text.append(" days");
			text.append(rowEnd);
		}
		// Days left
		if (getGameObject().hasThisAttribute("daysLeft")) {
			text.append(rowHeaderStart);
			text.append("Days Left:");
			text.append(rowContentStart);
			text.append(getGameObject().getThisInt("daysLeft"));
			text.append(" days");
			text.append(rowEnd);
		}
		// Reward
		if (getGameObject().hasThisAttribute("mission")) {
			int reward = getGameObject().getThisInt("reward");
			if (reward==0) {
				// Calculate it here
				if (character!=null) {
					reward = calculateReward(); // should never be 0 unless character is null
				}
			}
			else {
				targetName = getGameObject().getThisAttribute("deliverTarget");
				clearings = getGameObject().getThisInt("clearingCount");
			}
			text.append(rowHeaderStart);
			text.append("Deliver To:");
			text.append(rowContentStart);
			text.append(targetName==null?"?":targetName);
			text.append(rowEnd);
			
			text.append(rowHeaderStart);
			text.append("Reward:");
			text.append(rowContentStart);
			text.append(reward==0?"?":String.valueOf(reward));
			text.append(" gold");
			if (clearings>0) {
				text.append(" (");
				text.append(clearings);
				text.append(" clearing");
				text.append(clearings==1?"":"s");
				text.append(" away X ");
				text.append(reward/clearings); // this is kinda dumb, but it will work
				text.append(" gold)");
			}
			text.append(rowEnd);
		}
		// Partner
		ArrayList partner = getPartners();
		if (!partner.isEmpty()) {
			text.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#33cc00\" rowspan=\"");
			text.append(partner.size());
			text.append("\"><b>");
			text.append("Partner:");
			text.append(rowContentStart);
			for (Iterator i=partner.iterator();i.hasNext();) {
				String group = (String)i.next();
				text.append(StringUtilities.capitalize(group));
				text.append(rowEnd);
			}
		}
		// Foe
		ArrayList foe = getFoes();
		if (!foe.isEmpty()) {
			text.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#33cc00\" rowspan=\"");
			text.append(foe.size());
			text.append("\"><b>");
			text.append("Foe:");
			text.append(rowContentStart);
			int total = 0;
			for (Iterator i=foe.iterator();i.hasNext();) {
				String group = (String)i.next();
				text.append(group);
				
				if (hostPrefs!=null) {
					int onMap = getFoeCount_OnMap(hostPrefs,group,pool);
					int onCard = getFoeCount_OnCard(hostPrefs,group,pool);
					total+=onMap;
					total+=onCard;
					
					if (onMap>0 || onCard>0) {
						if (onMap>0) {
							text.append(", ");
							text.append(onMap);
							text.append(" left on the map");
						}
						if (onCard>0) {
							text.append(", ");
							text.append(onCard);
							text.append(" left on the card");
						}
					}
					else {
						text.append(" - none");
					}
				}
				
				text.append(rowEnd);
			}
			if (hostPrefs!=null) {
				text.append(rowHeaderStart);
				text.append("Total to Kill:");
				text.append(rowContentStart);
				text.append(total);
				text.append(rowEnd);
			}
		}
		
		text.append("</font></body></html>");
		
		return text.toString();
	}
	public ArrayList getPartners() {
		return getList("partner");
	}
	public ArrayList getFoes() {
		return getList("foe");
	}
	private ArrayList getList(String key) {
		ArrayList list = new ArrayList();
		String val = getGameObject().getThisAttribute(key);
		if (val!=null) {
			StringTokenizer tokens = new StringTokenizer(val,",");
			while(tokens.hasMoreTokens()) {
				String group = StringUtilities.capitalize(tokens.nextToken());
				list.add(group);
			}
		}
		return list;
	}
	public int calculateReward() {
		String boardNumber = RealmUtility.updateNameToBoard(getGameObject(),"");
		GameWrapper game = GameWrapper.findGame(getGameObject().getGameData());
		RealmCalendar cal = RealmCalendar.getCalendar(getGameObject().getGameData());
		String primary = cal.getMissionPrimaryTarget(game.getMonth(),getGameObject().getThisAttribute("mission")) + boardNumber;
		String secondary = cal.getMissionSecondaryTarget(game.getMonth(),getGameObject().getThisAttribute("mission")) + boardNumber;
		targetName = primary;
		TileLocation current = ClearingUtility.getTileLocation(getGameObject());
		TileLocation target = getLocationOf(primary);
		if (target==null || target.equals(current)) {
			targetName = secondary;
			target = getLocationOf(secondary);
		}
		clearings = ClearingUtility.calculateClearingCount(current,target); // This leads to a NPE sometimes?
		int reward;
		if (clearings==0) {
			reward = 30; // 3rd edition rules 7.2.4.e1
		}
		else {
			reward = clearings * cal.getMissionRewards(game.getMonth());
		}
		return reward;
	}
	private TileLocation getLocationOf(String target) {
		GameObject go = getGameObject().getGameData().getGameObjectByName(target);
		return ClearingUtility.getTileLocation(go);
	}
	public void makePayment(CharacterWrapper character) {
		getGameObject().setThisAttribute("daysLeft",getGameObject().getThisAttribute("time_limit"));
		int fame = getGameObject().getThisInt("fame_cost");
		int not = getGameObject().getThisInt("notoriety_cost");
		character.addFame(-fame);
		character.addNotoriety(-not);
		if (fame>0) {
			GameClient.broadcastClient(character.getGameObject().getName(),"Loses "+fame+" fame.");
		}
		if (not>0) {
			GameClient.broadcastClient(character.getGameObject().getName(),"Loses "+not+" notoriety.");
		}
	}
	public void gainReward(CharacterWrapper character) {
		GameClient.broadcastClient(character.getGameObject().getName(),"Completed "+getGameObject().getName());
		int fame = getGameObject().getThisInt("fame_cost");
		int not = getGameObject().getThisInt("notoriety_cost");
		character.addFame(fame);
		character.addNotoriety(not);
		if (fame>0) {
			GameClient.broadcastClient(character.getGameObject().getName(),"Regains "+fame+" fame.");
		}
		if (not>0) {
			GameClient.broadcastClient(character.getGameObject().getName(),"Regains "+not+" notoriety.");
		}
		int reward = getGameObject().getThisInt("reward");
		character.addGold(reward);
		if (reward>0) {
			GameClient.broadcastClient(character.getGameObject().getName(),"Received "+reward+" gold as a reward.");
		}
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionType = CharacterActionType.CompleteMissionCampaign;
		qp.actionName = getGameObject().getName();
		qp.targetOfSearch = getGameObject();
		character.addPostQuestParams(qp);
	}
	public boolean isComplete(CharacterWrapper character,TileLocation current) {
		if (getGameObject().hasThisAttribute("mission")) {
			// Mission is complete when in a clearing with the deliverTarget dwelling
			if (current.isInClearing()) {
				RealmComponent dwelling = current.clearing.getDwelling();
				if (dwelling!=null && dwelling.getGameObject().getName().equals(getGameObject().getThisAttribute("deliverTarget"))) {
					return true;
				}
			}
		}
		else if (getGameObject().hasThisAttribute("campaign")) {
			int total = 0;
			// Campaign is complete when all foes are dead (off map AND off setup card)
			GameData data = getGameObject().getGameData();
			HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
			GamePool pool = new GamePool(data.getGameObjects());
			ArrayList foe = getFoes();
			if (!foe.isEmpty()) {
				for (Iterator i=foe.iterator();i.hasNext();) {
					String group = (String)i.next();
					total += getFoeCount_OnMap(hostPrefs,group,pool);
					total += getFoeCount_OnCard(hostPrefs,group,pool);
				}
			}
			return total==0;
		}
		
		return false;
	}
	private ArrayList<String> getBaseFoeQuery(HostPrefWrapper hostPrefs,String group) {
		ArrayList<String> query = new ArrayList<String>();
		query.addAll(GamePool.makeKeyVals(hostPrefs.getGameKeyVals()));
		if ("Quest".equals(getGameObject().getName())) {
			query.add("monster");
			query.add("icon_type="+group.toLowerCase());
		}
		else {
			query.add("!dwelling");
			query.add("!treasure");
			query.add("!horse");
			query.add("native="+group);
		}
		if (getGameObject().hasThisAttribute(Constants.BOARD_NUMBER)) {
			query.add(Constants.BOARD_NUMBER+"="+getGameObject().getThisAttribute(Constants.BOARD_NUMBER));
		}
		return query;
	}
	private int getFoeCount_OnMap(HostPrefWrapper hostPrefs,String group,GamePool pool) {
		ArrayList<String> query = getBaseFoeQuery(hostPrefs,group);
		query.add("clearing");
		return pool.find(query).size();
	}
	private int getFoeCount_OnCard(HostPrefWrapper hostPrefs,String group,GamePool pool) {
		ArrayList<String> query = getBaseFoeQuery(hostPrefs,group);
		query.add("!clearing");
		query.add("!"+Constants.DEAD);
		return getFoeExistCount(pool.find(query));
	}
	private int getFoeExistCount(ArrayList<GameObject> foes) {
		int count = 0;
		for(GameObject foe:foes) {
			if (foe.hasThisAttribute(Constants.SETUP_START_TILE_REQ)) {
				TileLocation tl = ClearingUtility.getTileLocation(foe);
				String tileType = tl.tile.getTileType();
				if (tileType.equals(foe.getThisAttribute(Constants.SETUP_START_TILE_REQ))) {
					count++;
				}
			}
			else {
				count++;
			}
		}
		return count;
	}
	public void setup(CharacterWrapper character) {
		makePayment(character);
		if (getGameObject().hasThisAttribute("mission")) {
			// Missions (Food/Ale, Escort Party)
			int reward = calculateReward();
			getGameObject().setThisAttribute("reward",reward);
			getGameObject().setThisAttribute("clearingCount",clearings);
			getGameObject().setThisAttribute("deliverTarget",targetName);
		}
		else {
			// Campaigns
			character.setCurrentCampaign(getGameObject().getName());
			
			String relBlock = RealmUtility.getRelationshipBlockFor(getGameObject());
			
			// Calculate change in friendliness, and apply (Partners ALL increase by 2)
			for (Iterator i=getPartners().iterator();i.hasNext();) {
				String group = (String)i.next();
				getGameObject().setAttribute("relationship",group.toLowerCase(),2);
				character.changeRelationship(relBlock,group,2);
			}
			// (Foes move to ENEMY)
			if (!"Quest".equals(getGameObject().getName())) {
				for (Iterator i=getFoes().iterator();i.hasNext();) {
					String group = (String)i.next();
					int current = character.getRelationship(relBlock,group);
					int change = RelationshipType.ENEMY - current;
					getGameObject().setAttribute("relationship",group.toLowerCase(),change);
					character.changeRelationship(relBlock,group,change);
				}
			}
		}
	}
	public void expireEffect(CharacterWrapper character) {
		getGameObject().removeThisAttribute("daysLeft");
		if (getGameObject().hasThisAttribute("mission")) {
			getGameObject().removeThisAttribute("reward");
			getGameObject().removeThisAttribute("clearingCount");
			getGameObject().removeThisAttribute("deliverTarget");
		}
		else {
			character.setCurrentCampaign(null);
			
			String relBlock = RealmUtility.getRelationshipBlockFor(getGameObject());
			
			for (Iterator i=getPartners().iterator();i.hasNext();) {
				String group = (String)i.next();
				int val = getGameObject().getInt("relationship",group.toLowerCase());
				character.changeRelationship(relBlock,group,-val);
			}
			// (Foes move to ENEMY)
			if (!"Quest".equals(getGameObject().getName())) {
				for (Iterator i=getFoes().iterator();i.hasNext();) {
					String group = (String)i.next();
					int val = getGameObject().getInt("relationship",group.toLowerCase());
					character.changeRelationship(relBlock,group,-val);
				}
			}
			getGameObject().removeAttributeBlock("relationship");
		}
	}
	public boolean meetsPointRequirement(CharacterWrapper character) {
		int fame = getGameObject().getThisInt("fame_cost");
		int not = getGameObject().getThisInt("notoriety_cost");
		if ((fame==0 || character.getFame()>=fame)
				&& (not==0 || character.getNotoriety()>=not)) {
			return true;
		}
		return false;
	}
	public boolean stillInPlay() {
		return getGameObject().hasThisAttribute("daysLeft");
	}
	public void display(JFrame frame,CharacterWrapper character) {
		JEditorPane pane = new JEditorPane("text/html",generateHTML(character)) {
			public boolean isFocusTraversable() {
				return false;
			}
		};
		pane.setEditable(false);
		pane.setOpaque(false);
		
		JOptionPane.showMessageDialog(
				frame,
				pane,
				getGameObject().getName(),
				JOptionPane.INFORMATION_MESSAGE,
				getIcon()
		);
	}
	/*
	 * See Section 36 of 2nd edition manual
	 * See 7.2.4 of 3rd edition manual
	 * 
	 * - All chits flip on a monster die roll of 6 on the seventh day
	 * - Can only have ONE campaign chit, but any number of mission chits
	 * - Can only take a campaign chit if at least one or more of the foes is on the board
	 * - Can only abandon a campaign if none of the foes are on the board
	 * 		- don't get the cost points back!!
	 * - Campaigns are only complete when all foes are dead (not on the board, not on the setup card)
	 * - War, Conquer, Quest, Revolt last for the rest of the game, 28-days, or until complete (whichever comes first)
	 * - Escort, Food/Ale, Pillage, Raid must be completed in 14 days (by midnight of the 14th day)
	 * - If you fail to complete within the time limit, you must pay the cost again
	 * - When you complete the task, you regain the points you lost
	 * - Mission chits cost 5 Notoriety, and are worth gold equals to the (# clearings to target) times 2
	 * 		- Costs 5 Not
	 * 		- 14 day time limit
	 * 		- Food/Ale must go to the Inn.  If already at Inn, go to House.
	 * 		- Escort Party must go to the Chapel.  If already at Chapel, go to Guard.
	 * - Quest
	 * 		- Costs 20 Not
	 * 		- Partner = Order
	 * 		- Kill ALL Dragons (flying or not)
	 * - Pillage
	 * 		- Costs 10 Not
	 * 		- 14 day time limit
	 * 		- Partner = Bashkars
	 * 		- Foe = Patrol, Soldiers
	 * - Raid
	 * 		- Costs 8 Fame
	 * 		- 14 day time limit
	 * 		- Partner = Lancers, Woodfolk
	 * 		- Foe = Bashkars, Rogues
	 * - Revolt
	 * 		- Costs 35 Not
	 * 		- Partner = Lancers, Woodfolk, Bashkars, Rogues
	 * 		- Foe = Soldiers, Guard, Patrol, Company
	 * - War
	 * 		- Costs 10 Fame 15 Not
	 * 		- Partner = Soldiers, Guard, Patrol
	 * 		- Foe = Company, Bashkars, Rogues
	 * - Conquer
	 * 		- Costs 40 Not
	 * 		- Partner = Soldiers, Guard, Patrol, Company
	 * 		- Foe = Lancers, Woodfolk, Bashkars, Rogues
	 * 
	 * Partner: increases 2 levels friendliness (keep track of levels ABOVE Ally)
	 * Foe: ENEMY
	 */
}