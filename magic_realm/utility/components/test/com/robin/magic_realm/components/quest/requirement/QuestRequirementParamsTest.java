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
package com.robin.magic_realm.components.quest.requirement;

import org.junit.*;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.quest.*;

public class QuestRequirementParamsTest {
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
	public void testRoundTrip() {
		GameData gameData = new GameData();
		GameObject go1 = gameData.createNewObject();
		go1.setName("targetOfSearch");
		
		QuestRequirementParams qp = new QuestRequirementParams();
		qp.actionName = "action";
		qp.actionType = CharacterActionType.CompleteMissionCampaign;
		qp.dayKey = "foobar";
		qp.dieResult = 2;
		qp.searchHadAnEffect = true;
		qp.searchType = SearchResultType.Awaken;
		qp.targetOfSearch = go1;
		qp.timeOfCall = GamePhaseType.Birdsong;
		for(int i=0;i<5;i++) {
			GameObject go = gameData.createNewObject();
			go.setName("Object "+i);
			qp.objectList.add(go);
		}
		
		String string = qp.asString();
		System.out.println(string);
		QuestRequirementParams back = QuestRequirementParams.valueOf(string,gameData);
		
		Assert.assertEquals(qp.actionName,back.actionName);
		Assert.assertEquals(qp.actionType,back.actionType);
		Assert.assertEquals(qp.dayKey,back.dayKey);
		Assert.assertEquals(qp.dieResult,back.dieResult);
		Assert.assertEquals(qp.searchHadAnEffect,back.searchHadAnEffect);
		Assert.assertEquals(qp.objectList.size(),back.objectList.size());
		Assert.assertEquals(qp.searchType,back.searchType);
		Assert.assertEquals(qp.targetOfSearch.getName(),back.targetOfSearch.getName());
		Assert.assertEquals(qp.timeOfCall,back.timeOfCall);
		
	}
	
	@Test
	public void testMinimalRoundTrip() {
		GameData gameData = new GameData();
		GameObject go1 = gameData.createNewObject();
		go1.setName("targetOfSearch");
		
		QuestRequirementParams qp = new QuestRequirementParams();
		String string = qp.asString();
		System.out.println(string);
		QuestRequirementParams.valueOf(string,gameData);
		// No exceptions
	}
}