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

public class QuestRequirementPathTest {
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
	public void testTestPath() { // CV5 PW5 EV4 CV4 CV1 CL4 CL6 P:CL1&CL6 CL1 R1 R2 HP2 HP4 HP1 P:HP4&HP1 HP4 HP1 HP5
		Assert.assertEquals(true,QuestRequirementPath.testPath("CV1 CV2 CV3 CV4 CV5","CV1 CV2 CV3 CV4 CV5"));
		Assert.assertEquals(false,QuestRequirementPath.testPath("CV1 CV2 CV3 CV4","CV1 CV2 CV3 CV4 CV5"));
		Assert.assertEquals(true,QuestRequirementPath.testPath("CV1 CV2 CV3 CV2 CV3 CV4 CV5","CV1 CV2 CV3 CV4 CV5"));
		Assert.assertEquals(false,QuestRequirementPath.testPath("CV1 CV2 CV3 CV2 CV4 CV5","CV1 CV2 CV3 CV4 CV5"));
		Assert.assertEquals(true,QuestRequirementPath.testPath("CV1 CV2 CV3 CV4 CV3 CV2 CV3 CV4 CV5","CV1 CV2 CV3 CV4 CV5"));
		Assert.assertEquals(true,QuestRequirementPath.testPath("CV1 CV2 CV3 P:CV2&CV3 CV2 CV3 CV4 CV5",""));
	}
}