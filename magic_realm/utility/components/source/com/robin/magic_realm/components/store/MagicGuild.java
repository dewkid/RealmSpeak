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
package com.robin.magic_realm.components.store;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.GuildChitComponent;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.swing.RealmObjectChooser;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class MagicGuild extends GuildStore {
	
	private static int GT_PRICE = 2;
	
	private static String CURE_SERVICE = "Cancel curse or spell for 5 gold.";
	private static String CURE_FREE_SERVICE = "Cancel curse or spell for free.";
	private static String GENERATE_COLOR_SERVICE = "Generate any color for a day for 5 gold.";
	private static String ADVANCEMENT_SERVICE = "Pay "+GT_PRICE+" Great Treasures to advance to next level.";
	
	private ArrayList<GameObject> greatTreasures;
	private ArrayList<SpellWrapper> bewitchingSpells;
	private ArrayList<String> activeCurses;
	
	public MagicGuild(GuildChitComponent guild, CharacterWrapper character) {
		super(guild, character);
	}
	protected void setupGuildSpecific() {
		boolean journeymanMage = character.isGuildMember(trader) && character.getCurrentGuildLevel()==2; 
		if (!journeymanMage && character.hasCurse(Constants.ASHES)) {
			reasonStoreNotAvailable = "The "+getTraderName()+" does not like your ASHES curse!";
			return;
		}
		
		greatTreasures = new ArrayList<GameObject>();
		for (GameObject go:character.getInventory()) {
			if (go.hasThisAttribute("treasure") && go.hasThisAttribute("great")) {
				greatTreasures.add(go);
			}
		}
		
		activeCurses = character.getAllCurses();
		SpellMasterWrapper smw = SpellMasterWrapper.getSpellMaster(character.getGameData());
		bewitchingSpells = smw.getAffectingSpells(character.getGameObject());
	}
	private boolean madePayment() {
		RealmObjectChooser chooser = new RealmObjectChooser("Choose two Great Treasures to give to the Magic Guild.",character.getGameData(),false);
		chooser.setValidCount(2);
		chooser.addObjectsToChoose(greatTreasures);
		chooser.setVisible(true);
		if (chooser.pressedOkay()) {
			ArrayList<GameObject> toGive = chooser.getChosenObjects();
			for(GameObject go:toGive) {
				TradeUtility.loseItem(character,go,trader.getGameObject(),false);
				go.removeThisAttribute("great");
			}
			trader.getGameObject().addAll(toGive);
			return true;
		}
		return false;
	}
	private String chooseMagicType(JFrame frame) {
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,trader.getIcon(),"Which type of speed 2 MAGIC chit will you take?",getTraderName()+" Reward",false);
		chooser.addSelectionObject("I");
		chooser.addSelectionObject("II");
		chooser.addSelectionObject("III");
		chooser.addSelectionObject("IV");
		chooser.addSelectionObject("V");
		chooser.addSelectionObject("VI");
		chooser.addSelectionObject("VII");
		chooser.addSelectionObject("VIII");
		chooser.setVisible(true);
		return (String)chooser.getSelectedObject();
	}
	private String cureSpellOrCurse(JFrame frame) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(frame,"Cancel which spell/curse?",true);
		for(SpellWrapper spell:bewitchingSpells) {
			String optionKey = chooser.generateOption();
			chooser.addGameObjectToOption(optionKey,spell.getGameObject());
			for (Iterator h=spell.getGameObject().getHold().iterator();h.hasNext();) {
				GameObject hgo = (GameObject)h.next();
				chooser.addGameObjectToOption(optionKey,hgo);
			}
		}
		for (String curse:activeCurses) {
			chooser.generateOption(curse);
		}
		chooser.setVisible(true);
		
		String selText = chooser.getSelectedText();
		if (selText!=null) {
			if (character.hasCurse(selText)) {
				character.removeCurse(selText);
				return "Removed "+selText+" curse.";
			}
			else {
				GameObject go = chooser.getFirstSelectedComponent().getGameObject();
				SpellWrapper spell = new SpellWrapper(go);
				spell.expireSpell();
				return "Canceled "+go.getName()+" spell.";
			}
		}
		return null;
	}
	protected String generateColor(JFrame frame) {
		String colorAlready = trader.getGameObject().getThisAttribute("color_source");
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,trader.getIcon(),"Generate which color?","Generate Color",true);
		for (int i=1;i<=5;i++) {
			ColorMagic cm = new ColorMagic(i,true);
			if (!cm.getColorName().toLowerCase().equals(colorAlready)) {
				chooser.addSelectionObject(cm.getColorName());
			}
		}
		chooser.setVisible(true);
		String chosenColor = (String)chooser.getSelectedObject();
		if (chosenColor!=null) {
			trader.getGameObject().setThisAttribute("color_source",chosenColor.toLowerCase());
			return "Generated "+chosenColor+" magic.";
		}
		return null;
	}
	protected String doGuildService(JFrame frame,int level) {
		int gold = (int)character.getGold();
		int gtCount = greatTreasures.size();
		int activeCursesOrSpells = activeCurses.size() + bewitchingSpells.size();
		
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,trader.getIcon(),"Which service?",getTraderName()+" Services",true);
		if (level<3) chooser.addSelectionObject(ADVANCEMENT_SERVICE,gtCount>=GT_PRICE);
		updateButtonChooser(chooser,level);
		if (level==1) chooser.addSelectionObject(CURE_SERVICE,(gold>=5) && activeCursesOrSpells>0);
		if (level>=2) chooser.addSelectionObject(CURE_FREE_SERVICE,(gold>=10) && activeCursesOrSpells>0);
		if (level>=2) chooser.addSelectionObject(GENERATE_COLOR_SERVICE,gold>=5);
		chooser.setVisible(true);
		
		String selected = (String)chooser.getSelectedObject();
		if (selected!=null) {
			boolean freeAdvancement = isFreeAdvancement(selected);
			if (CURE_SERVICE.equals(selected)) {
				character.addGold(-5);
				return cureSpellOrCurse(frame);
			}
			else if (CURE_FREE_SERVICE.equals(selected)) {
				return cureSpellOrCurse(frame);
			}
			else if (GENERATE_COLOR_SERVICE.equals(selected)) {
				character.addGold(-5);
				return generateColor(frame);
			}
			else if (freeAdvancement || ADVANCEMENT_SERVICE.equals(selected)) {
				if (!freeAdvancement && !madePayment()) {
					return null;
				}
				
				int newLevel = character.getCurrentGuildLevel()+1;
				character.setCurrentGuildLevel(newLevel);
				chooseFriendlinessGain(frame);
				if (newLevel==3) {
					String chosenMagicType = chooseMagicType(frame);
					GameObject go = getNewCharacterChit();
					Strength vul = new Strength(character.getGameObject().getThisAttribute("vulnerability"));
					if (!vul.isTremendous()) {
						vul = vul.addStrength(1);
					}
					go.setThisAttribute("action","magic");
					go.setThisAttribute("speed","2");
					go.setThisAttribute("magic",chosenMagicType);
					go.setThisAttribute("effort","2");
					go.setName(character.getCharacterLevelName(4)+" MAGIC "+chosenMagicType+"2**");
					RealmLogging.logMessage(character.getGameObject().getName(),"Gained a "+go.getName()+" chit.");
				}
				return "Advanced to "+character.getCurrentGuildLevelName()+"!";
			}
		}
		
		return null;
	}
}