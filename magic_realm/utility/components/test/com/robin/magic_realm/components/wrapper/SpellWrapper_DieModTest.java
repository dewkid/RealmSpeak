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

import static org.junit.Assert.*;

import org.junit.Test;

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.TestBaseWithLoader;
import com.robin.magic_realm.components.table.RealmTable;
import com.robin.magic_realm.components.utility.DieRollBuilder;

public class SpellWrapper_DieModTest extends TestBaseWithLoader {
	
	private CharacterWrapper createBewitchedCharacter(String characterName,String spellName) {
		CharacterWrapper caster = new CharacterWrapper(findGameObject("Magician"));
		CharacterWrapper character = new CharacterWrapper(findGameObject(characterName));
		putGameObjectInClearing(character.getGameObject(),"Crag",6);
		SpellWrapper spell = new SpellWrapper(findGameObject(spellName));
		caster.getGameObject().add(spell.getGameObject());
		spell.castSpell(findGameObject("Magician Magic IV3*")); // doesn't matter
		spell.addTarget(hostPrefs,character.getGameObject());
		spell.affectTargets(parentFrame,game,false);
		return character;
	}
	
	@Test
	public void testBadLuck() {
		// SETUP
		CharacterWrapper amazon = createBewitchedCharacter("Amazon","Bad Luck");
		DieRollBuilder builder = new DieRollBuilder(parentFrame,amazon,0);
		
		// EXECUTE
		DieRoller hide = builder.createHideRoller();
		
		// VERIFY
		assertEquals(1,hide.getModifier());
	}
	@Test
	public void testIllusion() {
		// SETUP
		CharacterWrapper amazon = createBewitchedCharacter("Amazon","Illusion");
		DieRollBuilder builder = new DieRollBuilder(parentFrame,amazon,0);
		RealmTable locate = RealmTable.locate(parentFrame,amazon.getCurrentLocation().clearing);
		
		// EXECUTE
		DieRoller search = builder.createRoller(locate);
		DieRoller hide = builder.createHideRoller();
		
		// VERIFY
		assertEquals(1,search.getModifier());
		assertEquals(0,hide.getModifier());
	}
	@Test
	public void testElvenSight() {
		// SETUP
		CharacterWrapper amazon = createBewitchedCharacter("Amazon","Elven Sight");
		DieRollBuilder builder = new DieRollBuilder(parentFrame,amazon,0);
		RealmTable locate = RealmTable.locate(parentFrame,amazon.getCurrentLocation().clearing);
		
		// EXECUTE
		DieRoller search = builder.createRoller(locate);
		DieRoller hide = builder.createHideRoller();
		
		// VERIFY
		assertEquals(0,search.getModifier());
		assertEquals(1,search.getNumberOfDice());
		assertEquals(0,hide.getModifier());
		assertEquals(2,hide.getNumberOfDice());
	}
}