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
package com.robin.magic_realm.components.utility;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.ArmorChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

/**
 * I'm going to try and move some general trade logic over to this class from Commerce, RealmPaymentDialog, and RealmUtililty.
 */
public class TradeUtility {
	public static void loseItem(CharacterWrapper character,GameObject item,GameObject trader,boolean useGrudges) {
		RealmComponent tradeItem = RealmComponent.getRealmComponent(item);
		if (tradeItem.isActivated()) {
			// providing a null frame ensures no JOptionPanes will pop up
			TreasureUtility.doDeactivate(null,character,tradeItem.getGameObject());
		}
		
		// Some treasures have special value to native groups...
		int fame = TreasureUtility.getFamePrice(item,trader);
		if (fame>0 && useGrudges) {
			// Increase friendship
			character.changeRelationship(trader, 1);
		}
		character.addFame(fame);
		
		if (tradeItem.isArmor()) {
			// Make sure armor is repaired after the sale
			ArmorChitComponent armor = (ArmorChitComponent)tradeItem;
			if (armor.isDamaged()) {
				armor.setIntact(true);
			}
		}
		GameObject holder = SetupCardUtility.getDenizenHolder(trader);
		if (holder!=null) {
			holder.add(item);
		}
		else {
			// Visitors take items directly, and don't have a holder
			trader.add(item);
		}
	}
}