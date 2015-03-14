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

import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public abstract class Store {
	
	public abstract String doService(JFrame frame);
	
	protected RealmComponent trader;
	protected String reasonStoreNotAvailable = null;
	
	public Store(RealmComponent trader) {
		this.trader = trader;
	}
	
	public boolean canUseStore() {
		return reasonStoreNotAvailable == null;
	}
	
	public String getReasonStoreNotAvailable() {
		return reasonStoreNotAvailable;
	}
	
	public String getTraderName() {
		return trader.getGameObject().getName();
	}
	
	public static Store getStore(TravelerChitComponent traveler,CharacterWrapper character) {
		Store store = null;
		String storeName = traveler.getGameObject().getThisAttribute(Constants.STORE);
		if (storeName!=null) {
			if (Constants.STORE_BLACKSMITH.equals(storeName)) {
				store = new Blacksmith(traveler,character);
			}
			else if (Constants.STORE_MERCHANT.equals(storeName)) {
				store = new Merchant(traveler,character);
			}
			else if (Constants.STORE_CLERIC.equals(storeName)) {
				store = new Cleric(traveler,character);
			}
			else if (Constants.STORE_BARD.equals(storeName)) {
				store = new Bard(traveler,character);
			}
			else if (Constants.STORE_SELLHOLD.equals(storeName)) {
				store = new SellHold(traveler,character);
			}
			else if (Constants.STORE_SPELLCAST.equals(storeName)) {
				store = new SpellCast(traveler,character);
			}
		}
		return store;
	}
	public static GuildStore getGuildStore(GuildChitComponent guild,CharacterWrapper character) {
		GuildStore store = null;
		String storeName = guild.getGameObject().getThisAttribute("guild");
		if (storeName!=null) {
			if ("magic".equals(storeName)) {
				store = new MagicGuild(guild,character);
			}
			else if ("fighters".equals(storeName)) {
				store = new FightersGuild(guild,character);
			}
			else if ("thieves".equals(storeName)) {
				store = new ThievesGuild(guild,character);
			}
		}
		return store;
	}
}