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

import junit.framework.Assert;

import org.junit.Test;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.ArmorChitComponent;
import com.robin.magic_realm.components.TestBaseWithLoader;
import com.robin.magic_realm.components.swing.RealmLogWindow;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class TreasureUtilityTest extends TestBaseWithLoader {
	@Test
	public void testDestroyedArmor() {
		GameObject steve = findGameObject("Amazon");
		CharacterWrapper character = new CharacterWrapper(steve);
		GameObject helmet = findGameObject("Helmet");
		ArmorChitComponent armor = new ArmorChitComponent(helmet);
		armor.setIntact(false);
		TreasureUtility.handleDestroyedItem(character,helmet);
		Assert.assertFalse(armor.isDamaged());
		System.out.println("testDestroyedArmor() logging output:");
		System.out.println(RealmLogWindow.getSingleton().toString());
	}
	
	@Test
	public void testDestroyedArmor_BadReturnDwelling() {
		GameObject steve = findGameObject("Amazon");
		CharacterWrapper character = new CharacterWrapper(steve);
		GameObject helmet = findGameObject("Helmet");
		helmet.setThisAttribute(Constants.ARMOR_RETURN_DWELLING,"Foobar");
		ArmorChitComponent armor = new ArmorChitComponent(helmet);
		armor.setIntact(false);
		TreasureUtility.handleDestroyedItem(character,helmet);
		Assert.assertFalse(armor.isDamaged());
		System.out.println("testDestroyedArmor_BadReturnDwelling() logging output:");
		System.out.println(RealmLogWindow.getSingleton().toString());
	}
}