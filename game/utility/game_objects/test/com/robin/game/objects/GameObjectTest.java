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
package com.robin.game.objects;

import org.junit.*;

public class GameObjectTest {
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
	}

	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}

	/**
	 * Sets up the test fixture. (Called before every test case method.)
	 */
	@Before
	public void setUp() {
	}

	/**
	 * Tears down the test fixture. (Called after every test case method.)
	 */
	@After
	public void tearDown() {
	}

	@Test
	public void testAllAttributesMatch() {
		GameObject one = GameObject.createEmptyGameObject();
		one.setThisAttribute("foo",1);
		one.setThisAttribute("asdf",2);
		one.setThisAttribute("qwer",4);
		one.setThisAttribute("zxcv","finder");
		one.setAttribute("spam","hand","green");
		one.setAttribute("rrewm","cool");
		one.setAttribute("spam","tire","blue");
		
		GameObject two = GameObject.createEmptyGameObject();
		two.copyAttributesFrom(one);
		
		GameObject three = GameObject.createEmptyGameObject();
		three.copyAttributesFrom(one);
		three.setThisAttribute("different");
		
		Assert.assertTrue(one.allAttributesMatch(two));
		Assert.assertFalse(one.allAttributesMatch(three));
	}
}