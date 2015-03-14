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

public class GameObjectBlockManagerTest {
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
	
	private GameObject createTestGameObject() {
		GameObject go = GameObject.createEmptyGameObject();
		go.setThisAttribute("foo",1);
		go.setThisAttribute("asdf",2);
		go.setThisAttribute("qwer",4);
		go.setThisAttribute("zxcv","finder");
		go.setAttribute("spam","hand","green");
		go.setAttribute("rrewm","cool");
		go.setAttribute("spam","tire","blue");
		return go;
	}

	@Test
	public void testBlockStore() {
		GameObject main = GameObject.createEmptyGameObject();
		GameObjectBlockManager man = new GameObjectBlockManager(main);
		GameObject two = createTestGameObject();
		man.storeGameObjectInBlocks(two,"test");
		GameObject stored = man.extractGameObjectFromBlocks("test",true,true);
		two._outputDetail();
		stored._outputDetail();
		Assert.assertTrue(stored.allAttributesMatch(two));
		main._outputDetail();
		man.clearBlocks("test");
		main._outputDetail();
	}
}