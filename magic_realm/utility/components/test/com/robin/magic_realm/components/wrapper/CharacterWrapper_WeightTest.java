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
package com.robin.magic_realm.components.wrapper;

import java.util.ArrayList;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.TestBaseWithLoader;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.attribute.StrengthChit;
import com.robin.magic_realm.components.table.Loot;

public class CharacterWrapper_WeightTest extends TestBaseWithLoader {
	
	JFrame dummyFrame = new JFrame();

	@Test
	public void testBasicCarryWeight() {
		CharacterWrapper amazon = new CharacterWrapper(findGameObject("Amazon"));
		Strength weight = amazon.getNeededSupportWeight();
		Assert.assertEquals("M",weight.getChar());
	}
	/*
	 * 0001564: Golden Icon and Order
	 * Description 	I hired O3 and O2 expecting to be able to carry my cached Golden Icon
	 * (T Weight Item) back to the Chapel but I'm unable to move. O2 and O3 are assigned as underlings. 
	 */
	@Test
	public void testCarryTremendousWithOrder() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		woodsgirl.getGameObject().add(findGameObject("Bane Sword"));
		GameObject hireling = findGameObject("Order 1");
		woodsgirl.addHireling(hireling);
		woodsgirl.getGameObject().add(hireling);
		Assert.assertTrue(woodsgirl.canMove());
	}
	/*
	 * 0001632: Hirelings DO count when considering active item weight:
	 * 
	 * 7.3.1.h. A character or hired leader acting as a guide can use the strength of the underlings who are following him,
	 * whether their hiring character approves or not. He can use their move strength to carry items as he moves instead of
	 * an active Move chit or Boots card, or in the case of a hired leader, in place of his own move strength.
	 */
	@Test
	public void testCarryActiveTremendousWithOrder() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		woodsgirl.getGameObject().add(findGameObject("Bane Sword",true));
		GameObject hireling = findGameObject("Order 1");
		woodsgirl.addHireling(hireling);
		woodsgirl.getGameObject().add(hireling);
		Assert.assertTrue(woodsgirl.canMove());
	}
	/*
	 * Pack horses should support inactive weight
	 */
	@Test
	public void testPackHorses() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		GameObject go = findGameObject("Bane Sword");
		woodsgirl.getGameObject().add(go);
		woodsgirl.getGameObject().add(findGameObject("Warhorse"));
		Assert.assertTrue(woodsgirl.canMove());
	}
	/*
	 * 0001429: Pack horses should not support active treasure weight!
	 */
	@Test
	public void testPackHorsesDontSupportActive() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		woodsgirl.getGameObject().add(findGameObject("Bane Sword",true));
		woodsgirl.getGameObject().add(findGameObject("Warhorse"));
		Assert.assertFalse(woodsgirl.canMove());
	}
	@Test
	public void testActiveHorsesDoSupportActive() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		woodsgirl.getGameObject().add(findGameObject("Bane Sword",true));
		woodsgirl.getGameObject().add(findGameObject("Warhorse",true));
		Assert.assertTrue(woodsgirl.canMove());
	}
	@Test
	public void testRecentlyUnhiredChecksInventory() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		GameObject hireling = findGameObject("Order 1");
		woodsgirl.addHireling(hireling);
		woodsgirl.getGameObject().add(hireling);
		woodsgirl.getGameObject().add(findGameObject("Power Boots",true));
		GameObject sword = findGameObject("Bane Sword");
		Loot.addItemToCharacter(dummyFrame,null,woodsgirl,sword);
		woodsgirl.removeHireling(hireling);
		woodsgirl.getGameObject().remove(hireling);
		Assert.assertTrue(woodsgirl.getNeedsInventoryCheck());
		//woodsgirl.checkInventoryStatus(dummyFrame,null,null); // Uncomment this to verify the dialog appears
	}
	@Test
	public void testBagOfCarrying() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		woodsgirl.getGameObject().add(findGameObject("Bane Sword",false)); // T
		woodsgirl.getGameObject().add(findGameObject("Blasted Jewel")); // H
		Assert.assertEquals("T",woodsgirl.getNeededSupportWeight().getChar());
		woodsgirl.getGameObject().add(findGameObject("Bag of Weightless",true));
		Assert.assertEquals("H",woodsgirl.getNeededSupportWeight().getChar());
	}
	@Test
	public void testBagOfCarryingDoesntWorkOnActive() {
		CharacterWrapper woodsgirl = new CharacterWrapper(findGameObject("Woods Girl"));
		woodsgirl.getGameObject().add(findGameObject("Bane Sword",true)); // T (but activated)
		woodsgirl.getGameObject().add(findGameObject("Blasted Jewel")); // H
		Assert.assertEquals("T",woodsgirl.getNeededSupportWeight().getChar());
		woodsgirl.getGameObject().add(findGameObject("Bag of Weightless",true));
		Assert.assertEquals("T",woodsgirl.getNeededSupportWeight().getChar());
	}
	@Test
	public void testTransmorphDragonCarryT() {
		// Setup
		GameObject jewel = findGameObject("Blasted Jewel");
		GameObject dragon = findGameObject("T Dragon");
		CharacterWrapper witchKing = new CharacterWrapper(findGameObject("Witch King"));
		witchKing.getGameObject().add(jewel);
		boolean weak = witchKing.canMove();
		SpellWrapper absorbEssence = new SpellWrapper(findGameObject("Absorb Essence"));
		witchKing.getGameObject().add(absorbEssence.getGameObject());
		Strength before = witchKing.getMoveStrength(true,false);
		absorbEssence.castSpell(witchKing.getGameObject());
		absorbEssence.addTarget(hostPrefs,dragon);
		absorbEssence.affectTargets(dummyFrame,game,false);
		
		// Execute
		Strength after = witchKing.getMoveStrength(true,false);
		boolean strong = witchKing.canMove();
		
		// Verify
		Assert.assertTrue(after.strongerThan(before));
		Assert.assertEquals("T",after.getChar());
		Assert.assertFalse(weak);
		Assert.assertTrue(strong);
	}
	@Test
	public void testAbsorbDemonBroomstickNoFly() {
		// Setup
		GameObject demon = findGameObject("Demon");
		CharacterWrapper witch = new CharacterWrapper(findGameObject("Witch"));
		SpellWrapper absorbEssence = new SpellWrapper(findGameObject("Absorb Essence"));
		SpellWrapper broomstick = new SpellWrapper(findGameObject("Broomstick"));
		witch.getGameObject().add(absorbEssence.getGameObject());
		witch.getGameObject().add(broomstick.getGameObject());
		
		broomstick.castSpell(witch.getGameObject());
		broomstick.addTarget(hostPrefs,witch.getGameObject());
		broomstick.affectTargets(dummyFrame,game,false);
		
		ArrayList<StrengthChit> fly = witch.getFlyStrengthChits(false);
		Assert.assertEquals(1,fly.size());
		Assert.assertTrue(fly.get(0).getStrength().strongerOrEqualTo(witch.getNeededSupportWeight(true)));
		
		absorbEssence.castSpell(witch.getGameObject());
		absorbEssence.addTarget(hostPrefs,demon);
		absorbEssence.affectTargets(dummyFrame,game,false);
		
		fly = witch.getFlyStrengthChits(false);
		Assert.assertEquals(1,fly.size());
		Assert.assertFalse(fly.get(0).getStrength().strongerOrEqualTo(witch.getNeededSupportWeight(true)));
	}
}