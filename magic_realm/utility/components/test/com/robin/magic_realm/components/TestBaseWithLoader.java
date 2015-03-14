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

//import static org.junit.Assert.*;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.junit.*;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public abstract class TestBaseWithLoader {

	@BeforeClass
	public static void oneTimeSetUp() {
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	private RealmLoader loader;
	protected HostPrefWrapper hostPrefs;
	protected JFrame parentFrame;
	protected GameWrapper game;
	
	/**
	 * Sets up the test fixture. (Called before every test case method.)
	 */
	@Before
	public void setUp() {
		loader = new RealmLoader();
		ArrayList keyVals = new ArrayList();
		keyVals.add("original_game");
		loader.getData().doSetup("standard_game",keyVals);
		hostPrefs = HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		hostPrefs.setStartingSeason("No Seasons");
		game = GameWrapper.findGame(loader.getData());
		parentFrame = new JFrame();
	}

	/**
	 * Tears down the test fixture. (Called after every test case method.)
	 */
	@After
	public void tearDown() {
		RealmUtility.resetGame();
		loader = null;
	}
	
	protected ArrayList<GameObject> findGameObjects(ArrayList<String> query) {
		GamePool pool = new GamePool(loader.getData().getGameObjects());
		return pool.find(query);
	}

	protected GameObject findGameObject(String name) {
		return findGameObject(name,false);
	}
	protected GameObject findGameObject(String name,boolean activated) {
		GameObject go = loader.getData().getGameObjectByName(name);
		if (activated) {
			go.setThisAttribute(Constants.ACTIVATED);
		}
		return go;
	}
	protected void putGameObjectInClearing(GameObject go,String tileName,int clearingNum) {
		GameObject tile = findGameObject("Crag");
		tile.add(go);
		go.setThisAttribute("clearing",clearingNum);
	}
}