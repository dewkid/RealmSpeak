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

import java.util.ArrayList;

import org.junit.*;

public class GameQueryTest {
	GameData gameData;
	
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
		gameData = new GameData();
	}

	/**
	 * Tears down the test fixture. (Called after every test case method.)
	 */
	@After
	public void tearDown() {
	}
	
	private GameObject createGameObject(String blockName,String key,String value) {
		GameObject go = gameData.createNewObject();
		go.setAttribute(blockName,key,value);
		return go;
	}

	@Test
	public void testQueryFirstObjectByKey() {
		// SETUP
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		list.add(createGameObject("this","foo","1"));
		list.add(createGameObject("this","bar","1"));
		
		// EXECUTE
		GameQuery query = new GameQuery("this");
		GameObject found = query.firstGameObjectWithKey(list,"bar");
		
		// VERIFY
		Assert.assertEquals(list.get(1),found);
	}

	@Test
	public void testQueryFirstObjectByKeyAndValue() {
		// SETUP
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		list.add(createGameObject("this","bar","1"));
		list.add(createGameObject("this","bar","2"));
		list.add(createGameObject("this","bar","3"));
		
		// EXECUTE
		GameQuery query = new GameQuery("this");
		GameObject found = query.firstGameObjectWithKeyAndValue(list,"bar","2");
		
		// VERIFY
		Assert.assertEquals(list.get(1),found);
	}

	@Test
	public void testQueryAllObjects() {
		// SETUP
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		list.add(createGameObject("this","bar","1"));
		list.add(createGameObject("this","bar","2"));
		list.add(createGameObject("this","bar","2"));
		
		// EXECUTE
		GameQuery query = new GameQuery("this");
		
		// VERIFY
		Assert.assertEquals(0,query.allGameObjectsWithKey(list,"foo").size());
		Assert.assertEquals(3,query.allGameObjectsWithKey(list,"bar").size());
		Assert.assertEquals(2,query.allGameObjectsWithKeyAndValue(list,"bar","2").size());
	}
}