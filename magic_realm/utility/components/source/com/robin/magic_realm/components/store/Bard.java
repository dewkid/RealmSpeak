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

import javax.swing.JFrame;

import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class Bard extends Store {
	
	private static Story[] storiesForSale = {
		new Story("a travel",5,5),
		new Story("a battle",25,30),
		new Story("an adventure",100,120),
		new Story("a heroes",200,300),
	};

	private CharacterWrapper character;
	private double fame;
	
	public Bard(RealmComponent trader, CharacterWrapper character) {
		super(trader);
		this.character = character;
		setupStore();
	}
	private void setupStore() {
		fame = character.getFame();
		if (character.hasCurse(Constants.DISGUST)) {
			reasonStoreNotAvailable = "The "+getTraderName()+" does not like your DISGUST curse.";
			return;
		}
		if (fame<5) {
			reasonStoreNotAvailable = "The "+character.getName()+" does not have enough fame (need at least 5) to make it worth the "+getTraderName()+"'s trouble.";
			return;
		}
	}

	public String doService(JFrame frame) {
		ButtonOptionDialog chooser = new ButtonOptionDialog(frame,trader.getIcon(),"Which service?",getTraderName(),true);
		for (Story story:storiesForSale) {
			chooser.addSelectionObject(story,fame>=story.getFameCost());
		}
		chooser.setVisible(true);
		Story selected = (Story)chooser.getSelectedObject();
		if (selected!=null) {
			character.addFame(-selected.getFameCost());
			character.addGold(selected.getGoldGain());
			return selected.getResult();
		}
		return null;
	}
	private static class Story {
		private String storyName;
		private int fameCost;
		private int goldGain;
		public Story(String storyName,int fameCost,int goldGain) {
			this.storyName = storyName;
			this.fameCost = fameCost;
			this.goldGain = goldGain;
		}
		public String toString() {
			return generateString("Sell","Lose","Gain");
		}
		public String getResult() {
			return generateString("Sold","Lost","Gained");
		}
		private String generateString(String verb,String lose,String gain) {
			StringBuilder sb = new StringBuilder();
			sb.append(verb);
			sb.append(" ");
			sb.append(storyName);
			sb.append(" story (");
			sb.append(lose);
			sb.append(" ");
			sb.append(fameCost);
			sb.append(" Fame, ");
			sb.append(gain);
			sb.append(" ");
			sb.append(goldGain);
			sb.append("Gold)");
			return sb.toString();
		}
		public int getFameCost() {
			return fameCost;
		}
		public int getGoldGain() {
			return goldGain;
		}
	}
}