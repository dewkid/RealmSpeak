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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.robin.magic_realm.components.attribute.ColorModTest;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParamsTest;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper_WeightTest;
import com.robin.magic_realm.components.wrapper.SpellWrapper_DieModTest;

@RunWith(Suite.class)
@SuiteClasses(
	{
		ColorModTest.class,
		
		DieRuleTest.class,
		//RealmUtilityTest.class,
		TreasureUtilityTest.class,
		
		CharacterWrapper_WeightTest.class,
		SpellWrapper_DieModTest.class,
		
		QuestRequirementParamsTest.class,
	}
)
public class ComponentsTestSuite {
	// I guess there's nothing really needed here...
}