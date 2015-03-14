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

import java.util.*;

import com.robin.game.objects.*;
import com.robin.game.server.GameClient;
import com.robin.general.swing.DieRoller;
import com.robin.general.util.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.table.MonsterGrow;
import com.robin.magic_realm.components.table.RaiseDead;
import com.robin.magic_realm.components.wrapper.*;

public class SetupCardUtility {
	
	private static int generatedMonsterCount = 0;
	
	public static void reset() {
		generatedMonsterCount = 0;
	}
	public static void updateGeneratedMonsterInt(GameObject go) {
		go.setThisAttribute(Constants.NUMBER,++generatedMonsterCount);
	}

	/**
	 * This method will summon monsters from the TreasureSetupCard based on warnings/sounds/etc
	 * 
	 * It will also relocate monsters that are prowling to the specified clearing.
	 */
	public static void summonMonsters(ArrayList<GameObject> summoned,TileLocation tl,GameData data,boolean includeWarningSounds,boolean includeSiteChits,int monsterDie) {
		if (DebugUtility.isNoSummon()) {
			// If Debug NO_SUMMON mode is active, then return without doing anything
			return;
		}
		if (!tl.isInClearing()) { // Must be a clearing - can't summon monsters on non-clearings!
			return;
		}
		int clearingNum = tl.clearing.getNum();
		
		// Use a pool to locate all the possible summoning objects for the given monsterDie
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList keyVals = new ArrayList();
		keyVals.add(hostPrefs.getGameKeyVals());
		keyVals.add("monster_die="+monsterDie);
		keyVals.add("!monster"); // no monsters (just their summon boxes)
		keyVals.add("!rank"); // no natives (just their summon boxes)
		ArrayList<GameObject> summons = pool.find(keyVals);
		
		// Break out the objects into three groups
		ArrayList goldSpecials = new ArrayList(); // Visitor/Mission chit boxes
		ArrayList dwellingSpecific = new ArrayList(); // Native groups
		ArrayList treasureLocations = new ArrayList(); // Specific Monsters
		ArrayList otherLocations = new ArrayList(); // summoned in a specific order
		for (GameObject go:summons) {
			if (go.hasKey("gold_special_target")) {
				goldSpecials.add(go);
			}
			else if (go.hasKey("dwelling")) {
				// the native dwellings are dependant on the presence of another regular dwelling (campfires, house, etc.)
				dwellingSpecific.add(go);
			}
			else if (go.hasKey("treasure_location")) {
				// make sure this location is actually in this tile
				if (go.getHeldBy()!=null && go.getHeldBy().equals(tl.tile.getGameObject())) {
					// and that it has anything to summon...
					if (go.getHoldCount()>0) {
						// and that it is face down (rule 12.5/3) - this is probably unnecessary (all tls only have 1 box of monsters anyway)
						TreasureLocationChitComponent tlChit = (TreasureLocationChitComponent)RealmComponent.getRealmComponent(go);
						if (!tlChit.hasSummonedToday(monsterDie)) {
							treasureLocations.add(go);
						}
					}
				}
			}
			else {
				otherLocations.add(go);
			}
		}
		// Sort otherLocations by box_num attribute (those without it, will be sorted to the top)
		Collections.sort(otherLocations,new Comparator() {
			public int compare(Object o1,Object o2) {
				GameObject go1 = (GameObject)o1;
				GameObject go2 = (GameObject)o2;
				int n1 = go1.getInt("this","box_num");
				int n2 = go2.getInt("this","box_num");
				return n1-n2;
			}
		});
		
		// Find and sort the warning/sound chits
		/*
		 * Warning chits summon monsters FIRST
		 * THEN Sound chits (low numbers summon before higher numbers)
		 */
		ArrayList warningChits = SetupCardUtility.getWarnings(tl.tile.getGameObject().getHold(),monsterDie,includeWarningSounds); // this is done separately to capture treasures...
		ArrayList soundChits = new ArrayList();
		ArrayList prowlingMonsters = new ArrayList();
		for (Iterator i=tl.tile.getGameObject().getHold().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc instanceof SoundChitComponent) {
				SoundChitComponent sound = (SoundChitComponent)rc;
				if (!sound.hasSummonedToday(monsterDie)) { // only summon once per day (rule 12.5/3)
					soundChits.add(go);
				}
			}
			else if (rc.isMonster() && !rc.isPlayerControlledLeader()) {
				int die = go.getThisInt("monster_die");
				if (die==monsterDie || die==99) { // ghosts are ALWAYS prowling
					if (!go.hasThisAttribute("blocked")) { // Exclude blocked monsters
						// Finally, make sure there isn't a monster lure in the monster's clearing.
						if (ClearingUtility.getItemInClearingWithKey(rc.getCurrentLocation(),Constants.NO_PROWLING)==null) {
							prowlingMonsters.add(go);
						}
					}
				}
			}
		}
		Collections.sort(soundChits,new Comparator() { // sort sound (no need to sort warnings)
			public int compare(Object o1,Object o2) {
				GameObject go1 = (GameObject)o1;
				GameObject go2 = (GameObject)o2;
				int n1 = go1.getInt("this","clearing");
				int n2 = go2.getInt("this","clearing");
				return n1-n2;
			}
		});
		
		// Expansion:  handle generated monsters
		ArrayList<GameObject> nonCurrentTileProwlers = new ArrayList<GameObject>();
		ArrayList generatedQuery = new ArrayList();
		generatedQuery.add(Constants.GENERATED);
		generatedQuery.add("monster_die="+monsterDie);
		generatedQuery.add("!"+Constants.DEAD);
		for (GameObject go:pool.find(generatedQuery)) {
			if (!prowlingMonsters.contains(go)) {
				nonCurrentTileProwlers.add(go);
			}
		}
		
		// Expansion:  handle visible travelers
		ArrayList travelerQuery = new ArrayList();
		travelerQuery.add(RealmComponent.TRAVELER);
		travelerQuery.add(Constants.SPAWNED);
		travelerQuery.add("!"+RealmComponent.OWNER_ID);
		travelerQuery.add("monster_die="+monsterDie);
		ArrayList<GameObject> travelers = pool.find(travelerQuery);
		
		// Now the process can begin
		
		// Expansion:  move the generated prowlers
		for (GameObject prowler:nonCurrentTileProwlers) {
			MonsterChitComponent monster = (MonsterChitComponent)RealmComponent.getRealmComponent(prowler);
			moveGeneratedMonster(monster);
			if (monster.getCurrentLocation().isInClearing()) {
				updateMonsterBlock(monster);
			}
		}
		
		// Expansion:  move the travelers
		for (GameObject go:travelers) {
			TravelerChitComponent traveler = (TravelerChitComponent)RealmComponent.getRealmComponent(go);
			moveTraveler(traveler);
		}
		
		// Before anything can be summoned, all prowling monsters on the tile need to be moved to the clearing,
		for (Iterator i=prowlingMonsters.iterator();i.hasNext();) {
			GameObject prowler = (GameObject)i.next();
			
			// Verify that the clearing changes, if not, then NO BLOCKING OCCURS!!
			int fromClearing = prowler.getThisInt("clearing");
			if (fromClearing==clearingNum) continue;
			
			// We're good!  Move that bitch! (Sheesh, watch the language!)
			prowler.setThisAttribute("clearing",String.valueOf(clearingNum));
			if (!prowler.hasThisAttribute("blocked")) {
				GameClient.broadcastClient("host",prowler.getName()+" is prowling.");
			}
			
			// If new location contains an unhidden character, then the monster stops prowling
			MonsterChitComponent monster = (MonsterChitComponent)RealmComponent.getRealmComponent(prowler);
			updateMonsterBlock(monster);
		}
		
		// Cycle through native dwellings and summon natives if needed
		GameObject dwellingInClearing = ClearingUtility.findDwellingInClearing(tl.tile.getGameObject(),clearingNum);
		if (dwellingInClearing!=null) {
			String dwellingType = dwellingInClearing.getThisAttribute("dwelling").toLowerCase();
			String bn = dwellingInClearing.getThisAttribute(Constants.BOARD_NUMBER);
			for (Iterator i=dwellingSpecific.iterator();i.hasNext();) {
				GameObject nativeDwelling = (GameObject)i.next();
				
				// need to test the clearing to see if any dwellings are in it that match
				// the attribute "dwelling" in this object
				String dwelling = nativeDwelling.getThisAttribute("dwelling").toLowerCase();
				if (dwelling.indexOf(dwellingType)>=0) {
					// Make sure boardNumber compares (Board B Company only goes to Board B L Fire)
					String bnNative = nativeDwelling.getThisAttribute(Constants.BOARD_NUMBER);
					if (bn==null?bnNative==null:bn.equals(bnNative)) {
						summoned.addAll(ClearingUtility.dumpHoldToTile(tl.tile.getGameObject(),nativeDwelling,clearingNum,"rank"));
					}
				}
			}
		}
		
		// Cycle through gold specials, and summon chits if needed
		Collection clearingComponents = tl.clearing.getClearingComponents();
		for (Iterator i=goldSpecials.iterator();i.hasNext();) {
			GameObject gs = (GameObject)i.next();
			String bn = gs.getThisAttribute(Constants.BOARD_NUMBER);
			
			// This does not need the bn, because it will be compared to the native attribute
			String summon_n = gs.getThisAttribute("summon_n");
			
			// These two are compared to the game object name, and thus require the bn
			String summon_t = gs.getThisAttribute("summon_t");
			String summon_tl = gs.getThisAttribute("summon_tl");
			
			if (bn!=null) {
				summon_t = summon_t+" "+bn;
				summon_tl = summon_tl+" "+bn;
			}
			
			// Iterate through clearing components
			for (Iterator n=clearingComponents.iterator();n.hasNext();) {
				RealmComponent rc = (RealmComponent)n.next();
				String rcBn = rc.getGameObject().getThisAttribute(Constants.BOARD_NUMBER);
				if (bn==null?rcBn==null:bn.equals(rcBn)) { // Make sure the goldSpecialTarget matches the boardNumber of the component
					// Must be an unhired native leader!
					if (summon_n!=null && rc.isNativeLeader() && rc.getOwner()==null && rc.getGameObject().getThisAttribute("native").equalsIgnoreCase(summon_n)) {
						summoned.addAll(ClearingUtility.dumpHoldToTile(tl.tile.getGameObject(),gs,clearingNum));
						break;
					}
					if (summon_t!=null && rc.isTreasure() && rc.getGameObject().getName().equalsIgnoreCase(summon_t)) {
						summoned.addAll(ClearingUtility.dumpHoldToTile(tl.tile.getGameObject(),gs,clearingNum));
						break;
					}
					if (summon_tl!=null && rc.isTreasureLocation() && rc.getGameObject().getName().equalsIgnoreCase(summon_tl)) {
						summoned.addAll(ClearingUtility.dumpHoldToTile(tl.tile.getGameObject(),gs,clearingNum));
						break;
					}
				}
			}
		}
		
		ArrayList<GameObject> newMonsters = new ArrayList<GameObject>();
		
		// Expansion: Generate monsters from SEEN generators 
		for (GameObject go:pool.find("seen,generator,!destroyed,monster_die="+monsterDie)) {
			StateChitComponent rc = (StateChitComponent)RealmComponent.getRealmComponent(go);
			if (!rc.hasSummonedToday(monsterDie)) { // Even generators only summon once per day
				newMonsters.addAll(generateMonsters(go,ClearingUtility.getTileLocation(go).clearing));
				rc.addSummonedToday(monsterDie);
			}
		}
		
		if (includeSiteChits) {
			String tileType = tl.tile.getTileType();
			// Cycle through treasure locations and summon their guardians (if any)
			for (Iterator i=treasureLocations.iterator();i.hasNext();) {
				GameObject trLoc = (GameObject)i.next();
				StateChitComponent chit = (StateChitComponent)RealmComponent.getRealmComponent(trLoc);
				chit.addSummonedToday(monsterDie);
				
				int tlClearing = trLoc.getThisInt("clearing");
				ClearingDetail clearing = tl.tile.getClearing(tlClearing);
				
				ArrayList hold = new ArrayList(trLoc.getHold());
				for (Iterator n=hold.iterator();n.hasNext();) {
					GameObject go = (GameObject)n.next();
					if (go.hasThisAttribute("monster")) {
						// Guardian might have a tilereq, if playing Pruitt's monsters!
						String tileReq = go.getThisAttribute(Constants.SETUP_START_TILE_REQ); // this is optional, and necessary for Pruitt's monsters
						if (tileReq==null || tileReq.equals(tileType)) {
							go.setThisAttribute("clearing",String.valueOf(tlClearing));
							clearing.add(go,null);
							newMonsters.add(go);
						}
					}
				}
			}
		}
		
		// Cycle through warning chits and summon anything possible (warning chits are already filtered when getWarnings is called)
		for (Iterator i=warningChits.iterator();i.hasNext();) {
			GameObject warning = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(warning);
			if (rc.isStateChit()) { // Might be a treasure, like Dragon Essence (bug 453)
				StateChitComponent chit = (StateChitComponent)rc;
				chit.addSummonedToday(monsterDie);
			}
			
			String warningName = warning.getThisAttribute("warning"); // ie., bones
			String tileType = warning.getThisAttribute("tile_type");  // ie., C
			if (tileType==null) {
				tileType = tl.tile.getTileType();
			}
			String summonName = warningName+" "+tileType; // ie., bones C
			
			String boardNum = (String)warning.getThisAttribute(Constants.BOARD_NUMBER);
			GameObject loc = SetupCardUtility.getFirstLocationWithSummonName(otherLocations,summonName,boardNum);
			if (loc!=null) {
				// found one!  do summon by dumping hold to tile
				
				// first, make sure there aren't any reasons why they can't be summoned...  (Holy Relic)
				GameObject go = ClearingUtility.getItemInClearingWithKey(tl,Constants.NO_UNDEAD);
				GameObject firstMonster = loc.getHoldCount()>0?(GameObject)loc.getHold().get(0):null;
				boolean stopSummon = go!=null && firstMonster!=null && firstMonster.hasThisAttribute(Constants.UNDEAD);

				if (!stopSummon) newMonsters.addAll(ClearingUtility.dumpHoldToTile(tl.tile.getGameObject(),loc,clearingNum));
			}
		}
		if (includeWarningSounds) {
			// Cycle through sound chits and summon anything possible
			String tileType = tl.tile.getGameObject().getThisAttribute("tile_type");
			for (Iterator i=soundChits.iterator();i.hasNext();) {
				GameObject sound = (GameObject)i.next();
				StateChitComponent chit = (StateChitComponent)RealmComponent.getRealmComponent(sound);
				chit.addSummonedToday(monsterDie);
				
				String soundName = sound.getThisAttribute("sound"); // ie., roar
				String name = soundName+" "+tileType; // ie., roar M
				int soundClearing = sound.getThisInt("clearing");
				String boardNum = (String)sound.getThisAttribute(Constants.BOARD_NUMBER);
				GameObject loc = SetupCardUtility.getFirstLocationWithSummonName(otherLocations,name,boardNum);
				if (loc!=null) {
					// found one!  do summon by dumping hold to tile
					newMonsters.addAll(ClearingUtility.dumpHoldToTile(tl.tile.getGameObject(),loc,soundClearing));
				}
			}
		}
		summoned.addAll(newMonsters);
		for (Iterator n=newMonsters.iterator();n.hasNext();) {
			GameObject added = (GameObject)n.next();
			RealmComponent rc = RealmComponent.getRealmComponent(added);
			if (rc.isMonster()) {
				SetupCardUtility.updateMonsterBlock((MonsterChitComponent)rc);
			}
		}
	}
	public static GameObject createWasp(MonsterCreator mc,GameData data) {
		GameObject go = mc.createOrReuseMonster(data);
		mc.setupGameObject(go,"Wasp","wasp","M",false,true);
		mc.setupSide(go,"light","L",1,2,0,3,"yellow");
		mc.setupSide(go,"dark","L",1,2,0,3,"yellow");
		return go;
	}
	public static GameObject createBlob(MonsterCreator mc,GameData data) {
		GameObject go = mc.createOrReuseMonster(data);
		mc.setupGameObject(go,"Blob","blob","L",false);
		mc.setupSide(go,"light","L",0,3,0,5,"lightblue");
		mc.setupSide(go,"dark","L",0,3,0,5,"lightblue");
		go.setThisAttribute(Constants.GM_GROW);
		return go;
	}
	private static ArrayList<GameObject> generateMonsters(GameObject generator,ClearingDetail clearing) {
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		GameData data = generator.getGameData();
		String dieString = generator.getThisAttribute(Constants.GENERATOR);
		if (dieString==null) return list;
		String iconType = generator.getThisAttribute("icon_type");
		int monsters = getDieRollForString(dieString);
		MonsterCreator mc = new MonsterCreator("gen"+iconType);
		boolean noUndeadAllowed = ClearingUtility.getItemInClearingWithKey(clearing.getTileLocation(),Constants.NO_UNDEAD)!=null;
		for (int i=0;i<monsters;i++) {
			GameObject go = null;
			if ("wasp".equals(iconType)) {
				go = createWasp(mc,data);
			}
			else if ("blob".equals(iconType)) {
				go = createBlob(mc,data);
			}
			else if ("zombie1".equals(iconType)) {
				if (noUndeadAllowed) break; // yes, break out of the loop
				go = RaiseDead.createUndead(mc,data);
				// color these undead a little differently to distinguish them from others
				go.setAttribute("light","chit_color","gray");
				go.setAttribute("dark","chit_color","darkgray");
				go.setThisAttribute(Constants.UNDEAD);
			}
			if (go!=null) {
				go.setThisAttribute(Constants.GENERATED);
				go.setThisAttribute("clearing",String.valueOf(clearing));
				clearing.add(go,null);
				go.setThisAttribute("monster_die",generator.getThisAttribute("monster_die"));
				go.setThisAttribute(Constants.GENERATOR_ID,generator.getStringId());
			}
		}
		list.addAll(mc.getMonstersCreated());
		return list;
	}
	private static int calculateIncentive(ArrayList<RealmComponent> components,int monsterIncentive,int characterIncentive) {
		int count = 0;
		for (RealmComponent rc:components) {
			if (rc.isCharacter()) count+=characterIncentive;
			if (rc.getGameObject().hasThisAttribute(Constants.GENERATED)) count+=monsterIncentive;
		}
		return count;
	}
	private static void moveGeneratedMonster(MonsterChitComponent monster) {
		GameObject generator = monster.getGameObject().getGameData().getGameObject(monster.getGameObject().getThisInt(Constants.GENERATOR_ID));
		TileLocation home = ClearingUtility.getTileLocation(generator);
		TileLocation current = monster.getCurrentLocation();
		if (monster.isBlocked() || current==null) return;
		int furthest = Integer.MIN_VALUE;
		if (monster.flies()) {
			// Find tiles to move to
			HashLists<Integer,TileComponent> choices = new HashLists<Integer,TileComponent>();
			for (TileComponent adj:current.tile.getAllAdjacentTiles()) {
				int distanceFromHome = ClearingUtility.getDistanceBetweenTiles(adj,home.tile);
				
				// if tile has characters in it, make it MORE interesting
				int interest = calculateIncentive(adj.getAllClearingComponents(),-1,20);
				distanceFromHome += interest*2; // this makes character tiles MUCH more interesting than leaving the generator
				
				furthest = Math.max(furthest,distanceFromHome);
				choices.put(distanceFromHome,adj);
			}
			
			// Randomly choose from furthest locations
			ArrayList<TileComponent> finalChoices = choices.getList(furthest);
			int r = RandomNumber.getRandom(finalChoices.size());
			TileComponent finalTile = finalChoices.get(r);
			TileLocation tl = new TileLocation(finalTile,true);
			
			ClearingUtility.moveToLocation(monster.getGameObject(),tl);
		}
		else {
			// Find clearing to move to
			HashLists<Integer,ClearingDetail> choices = new HashLists<Integer,ClearingDetail>();
			for (PathDetail path:current.clearing.getConnectedPaths()) {
				ClearingDetail other = path.findConnection(current.clearing);
				int distanceFromHome = ClearingUtility.calculateClearingCount(home,other.getTileLocation()); // is this going to kill performance?

				// if clearing has characters in it, make it MORE interesting
				int interest = calculateIncentive(other.getClearingComponents(),-1,20);
				distanceFromHome += interest*2; // this makes character tiles more interesting than leaving the generator
				
				furthest = Math.max(furthest,distanceFromHome);
				choices.put(distanceFromHome,other);
			}
			
			// Randomly choose from furthest locations
			ArrayList<ClearingDetail> finalChoices = choices.getList(furthest);
			int r = RandomNumber.getRandom(finalChoices.size());
			ClearingDetail finalClearing = finalChoices.get(r);
			if (finalClearing.isEdge()) {
				RealmUtility.makeDead(monster); // the monster leaves the board
			}
			else {
				TileLocation tl = finalClearing.getTileLocation();
				ClearingUtility.moveToLocation(monster.getGameObject(),tl);
				if (monster.getGameObject().hasThisAttribute(Constants.GM_GROW)) {
					MonsterGrow table = new MonsterGrow(null,null,monster);
					DieRollBuilder builder = new DieRollBuilder(null,null,0);
					DieRoller roller = builder.createRoller(table.getTableKey(),tl);
					RealmLogging.logMessage("host",table.apply(null,roller));
				}
			}
		}
	}
	private static void moveTraveler(TravelerChitComponent traveler) {
		TileLocation current = traveler.getCurrentLocation();
		if (current == null) return;	// not sure why this can happen, but at least this wont throw an error anymore
		
		// Find clearing to move to
		int mostInterest = Integer.MIN_VALUE;
		HashLists<Integer,ClearingDetail> choices = new HashLists<Integer,ClearingDetail>();
		// Include current clearing when deciding (though with one less incentive)
		choices.put(calculateIncentive(current.clearing.getClearingComponents(),-2,-1)-1,current.clearing);
		for (PathDetail path:current.clearing.getConnectedPaths()) {
			ClearingDetail other = path.findConnection(current.clearing);
			if (other.isEdge()) continue; // travelers don't leave the map

			// if clearing has characters in it, make it MORE interesting
			int interest = calculateIncentive(other.getClearingComponents(),-2,-1);
			
			mostInterest = Math.max(interest,mostInterest);
			choices.put(interest,other);
		}
		
		// Randomly choose from furthest locations
		ArrayList<ClearingDetail> finalChoices = choices.getList(mostInterest);
		int r = RandomNumber.getRandom(finalChoices.size());
		ClearingDetail finalClearing = finalChoices.get(r);
		
		if (!current.clearing.equals(finalClearing)) {
			TileLocation tl = finalClearing.getTileLocation();
			ClearingUtility.moveToLocation(traveler.getGameObject(),tl);
		}
	}
	private static int getDieRollForString(String dieString) {
		String[] s = dieString.toLowerCase().split("d");
		int count = Integer.valueOf(s[0]);
		int sides = Integer.valueOf(s[1]);
		int total = 0;
		for (int i=0;i<count;i++) {
			total += RandomNumber.getDieRoll(sides);
		}
		return total;
	}

	private static ArrayList getWarnings(Collection gameObjects,int monsterDie,boolean includeWarningSounds) {
		ArrayList gos = new ArrayList(gameObjects);
		
		// Find all "seen" treasures
		ArrayList seen = new ArrayList();
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			seen.addAll(ClearingUtility.dissolveIntoSeenStuff(rc));
		}
		for (Iterator i=seen.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (!gos.contains(rc.getGameObject())) {
				gos.add(rc.getGameObject());
			}
		}
		
		// Now process warnings
		ArrayList warnings = new ArrayList();
		for (Iterator i=gos.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (!rc.isDwelling() && go.hasThisAttribute("warning")) {
				if (rc instanceof WarningChitComponent) {
					if (includeWarningSounds) { // only add warning chits if allowed
						WarningChitComponent warning = (WarningChitComponent)rc;
						if (!warning.hasSummonedToday(monsterDie)) { // only summon once per day (rule 12.5/3)
							warnings.add(go);
						}
					}
				}
				else {
					// Non-warning chits (like Dragon Essence) are always added
					warnings.add(go);
				}
			}
		}
		return warnings;
	}

	/**
	 * Cycles through the otherLocations list (sorted previously by box_num) and returns the first
	 * location that matches up with the summon name.  This method is used by the summonMonsters(...) method
	 * to determine which monsters/natives are summoned for a given warning or sound chit name.
	 */
	private static GameObject getFirstLocationWithSummonName(ArrayList otherLocations,String name,String boardNum) {
		name = name.toLowerCase();
		for (Iterator o=otherLocations.iterator();o.hasNext();) {
			GameObject loc = (GameObject)o.next();
			String locBoardNum = loc.getThisAttribute(Constants.BOARD_NUMBER);
			if ((boardNum==null && locBoardNum==null) || (boardNum!=null && boardNum.equals(locBoardNum))) {
				if (loc.getHoldCount()>0) {
					String summon = loc.getThisAttribute("summon"); // TODO Maybe this should be an AttributeList...
					if (summon!=null) {
						StringTokenizer tokens = new StringTokenizer(summon,",");
						while(tokens.hasMoreTokens()) {
							String test = tokens.nextToken().toLowerCase();
							if (name.indexOf(test)>=0) {
								return loc;
							}
						}
					} // else ---- This is probably a non-monster (Native, Treasure, Item, etc.)
				}
			}
		}
		return null;
	}

	public static void summonMonsters(HostPrefWrapper hostPrefs,ArrayList<GameObject> summoned,CharacterWrapper character,int monsterDie) {
		if (!character.isMinion() && !character.isSleep()) { // Minions and sleeping characters do not summon monsters or prowling denizens
			TileLocation current = character.getCurrentLocation();
			boolean atPeaceWithNature = character.affectedByKey(Constants.PEACE_WITH_NATURE);
			boolean warningSounds = !atPeaceWithNature;
			if (!character.getNoSummon()) { // Only the "first" follower in the "group" summons monsters!
				boolean lull = 
					character.getGameObject().hasAttribute(Constants.OPTIONAL_BLOCK,Constants.DRUID_LULL)
						|| character.getGameObject().hasThisAttribute(Constants.DRUID_LULL);
				
				boolean siteChits = !lull;
				
				if (character.isHidden() && hostPrefs.hasPref(Constants.OPT_QUIET_MONSTERS)) {
					warningSounds = false;
					siteChits = false;
				}
				
				summonMonsters(summoned,current,character.getGameObject().getGameData(),warningSounds,siteChits,monsterDie);
			}
		}
	}

	public static void resetDenizen(GameObject denizen) {
		denizen.removeThisAttribute("needs_init");
		
		// Remove tile clearing definition
		denizen.removeThisAttribute("clearing");
		
		// Remove all DEAD designations and leftover killedBy info - make sure light side up too!
		CombatWrapper.clearAllCombatInfo(denizen);
		denizen.removeThisAttribute(Constants.DEAD);
		ChitComponent rc = (ChitComponent)RealmComponent.getRealmComponent(denizen);
		rc.setLightSideUp();
		if (rc.isNative()) {
			NativeSteedChitComponent horse = (NativeSteedChitComponent)rc.getHorseIncludeDead();
			if (horse!=null) {
				horse.setLightSideUp();
				CombatWrapper.clearAllCombatInfo(horse.getGameObject());
				horse.getGameObject().removeThisAttribute(Constants.DEAD);
			}
		}
		
		GameObject denizenHolder = SetupCardUtility.getDenizenHolder(denizen);
		if (denizen.hasThisAttribute("garrison")) {
			// Garrison natives return to the board immediately
			TileLocation tl = ClearingUtility.getTileLocation(denizenHolder);
			tl.clearing.add(denizen,null);
		}
		else if (denizenHolder!=null) {
			// Make sure to cancel any bewitching spells when returning to setup card!
			SpellMasterWrapper smw = SpellMasterWrapper.getSpellMaster(denizen.getGameData());
			smw.expireBewitchingSpells(rc.getGameObject(),null);
			
			// Return denizen to their holding box
			denizenHolder.add(denizen);
		}
		
		if (denizenHolder!=null) {
			RealmComponent dh = RealmComponent.getRealmComponent(denizenHolder);
			if (dh instanceof WarningChitComponent) {
				// Special case for Ghosts trapped in a warning chit - they always pop back out to the tile
				WarningChitComponent warning = (WarningChitComponent)dh;
				warning.setFaceUp();
			}
		}
	}

	public static void updateMonsterBlock(MonsterChitComponent monster) {
		if (!monster.isMistLike()) { // Misty monsters don't block
			TileLocation prowlerLocation = ClearingUtility.getTileLocation(monster);
			for (Iterator n=prowlerLocation.clearing.getClearingComponents().iterator();n.hasNext();) {
				RealmComponent rc = (RealmComponent)n.next();
				if (rc.isPlayerControlledLeader()) {
					if (!rc.isHidden() && !rc.isMistLike()) {
						if (!rc.isImmuneTo(monster)) {
							CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
							if (!character.isSleep()) {
								monster.setBlocked(true);
								character.setBlocked(true);
								GameClient.broadcastClient("host",monster.getGameObject().getName()+" blocks the "+character.getGameObject().getName());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * All monsters and natives for the given monster die are returned to the treasure setup card
	 */
	public static void resetDenizens(GameData data,int monsterDie) {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
		GameWrapper game = GameWrapper.findGame(data);
		GamePool pool = new GamePool(data.getGameObjects());
		
		ArrayList keyVals = new ArrayList();
		keyVals.add(hostPrefs.getGameKeyVals());
		keyVals.add("monster_die="+monsterDie);
		keyVals.add("setup_start"); // this should get all monsters and natives
		keyVals.add("clearing"); // this identifies those that are on tiles
		keyVals.add("!"+RealmComponent.OWNER_ID); // this identifies unhired natives
		Collection returning = pool.extract(keyVals);
		
		keyVals = new ArrayList();
		keyVals.add(hostPrefs.getGameKeyVals());
		keyVals.add("monster_die="+monsterDie);
		keyVals.add("setup_start"); // this should get all monsters and natives
		keyVals.add("needs_init"); // this identifies those that need to initialized (start of game)
		returning.addAll(pool.extract(keyVals));
		
		keyVals = new ArrayList();
		keyVals.add(hostPrefs.getGameKeyVals());
		keyVals.add("monster_die="+monsterDie);
		keyVals.add("setup_start"); // this should get all monsters and natives
		keyVals.add(Constants.DEAD); // this identifies those that are DEAD
		returning.addAll(pool.extract(keyVals));
		
		keyVals = new ArrayList();
		keyVals.add(hostPrefs.getGameKeyVals());
		keyVals.add("monster_die=99"); // the ghosts
		keyVals.add("setup_start");
		keyVals.add("monster");
		keyVals.add(Constants.DEAD); // but only if the ghosts are dead!
		returning.addAll(pool.extract(keyVals));
		
		if (!returning.isEmpty()) {
			GameClient.broadcastClient("host","7th day - denizens return to setup card:");
		}
		
		for (Iterator i=returning.iterator();i.hasNext();) {
			GameObject denizen = (GameObject)i.next();
			GameClient.broadcastClient("host"," - "+denizen.getName());
			game.addRegeneratedDenizen(denizen);
			resetDenizen(denizen);
		}
		
		// Flip all visitor/mission chits
		if (!hostPrefs.hasPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE)) {
			flipGoldSpecialChits(hostPrefs,pool,monsterDie);
		}
	}
	
	private static void flipGoldSpecialChits(HostPrefWrapper hostPrefs,GamePool pool,int monsterDie) {
		ArrayList keyVals = new ArrayList();
		keyVals.add(hostPrefs.getGameKeyVals());
		keyVals.add("monster_die="+monsterDie);
		keyVals.add("gold_special");
		ArrayList allGoldSpecial = pool.extract(keyVals);
		ArrayList toFlip = new ArrayList();
		for (Iterator i=allGoldSpecial.iterator();i.hasNext();) {
			GameObject side1 = (GameObject)i.next();
			GameObject holder = side1.getHeldBy();
			if (holder!=null) {
				RealmComponent rc = RealmComponent.getRealmComponent(holder);
				if (rc==null || !rc.isCharacter()) { // can't flip chit if held by a character!
					toFlip.add(side1);
				}
			}
		}
		for (Iterator i=toFlip.iterator();i.hasNext();) {
			GameObject side1 = (GameObject)i.next();
			GameObject side2 = side1.getGameObjectFromThisAttribute("pairid");
			GameObject holder = side1.getHeldBy();
			if (side1.hasThisAttribute("clearing")) {
				side2.setThisAttribute("clearing",side1.getThisInt("clearing"));
				side1.removeThisAttribute("clearing");
			}
			holder.add(side2);
			holder.remove(side1);
		}
	}

	public static void setupDwellingNatives(GameObject dwelling) {
		if (dwelling.getHoldCount()>0) {
			GameObject tile = dwelling.getHeldBy();
			if (tile!=null) {
				String clearing = dwelling.getThisAttribute("clearing");
				if (clearing!=null) {
					GamePool subpool = new GamePool(dwelling.getHold());
					ArrayList natives = subpool.find(GamePool.makeKeyVals("rank"));
					for (Iterator n=natives.iterator();n.hasNext();) {
						GameObject aNative = (GameObject)n.next();
						aNative.setThisAttribute("clearing",clearing);
						tile.add(aNative);
					}
				}
			}
		}
	}

	public static void setupDwellingsAndGhosts(HostPrefWrapper hostPrefs,GameData data) {
		if (!hostPrefs.hasPref(Constants.EXP_NO_DWELLING_START)) { // Make sure option is enabled before revealing dwellings
			// Dwellings and ghosts should be remapped to the appropriate tiles
			// Simply flip those chits face up, and the rest will work
			ArrayList keyVals = new ArrayList();
			keyVals.add(hostPrefs.getGameKeyVals());
			keyVals.add("warning");
			keyVals.add("tile_type=V");
			keyVals.add("chit");
			GamePool pool = new GamePool(data.getGameObjects());
			Collection warningChits = pool.find(keyVals);
			for (Iterator i=warningChits.iterator();i.hasNext();) {
				GameObject warningChit = (GameObject)i.next();
				WarningChitComponent wc = (WarningChitComponent)RealmComponent.getRealmComponent(warningChit);
				wc.setFaceUp();
			}
			
			// Bring in native groups for each of the dwellings
			keyVals = new ArrayList();
			keyVals.add(hostPrefs.getGameKeyVals());
			keyVals.add("dwelling");
			pool = new GamePool(data.getGameObjects());
			Collection dwellings = pool.find(keyVals);
			for (Iterator i=dwellings.iterator();i.hasNext();) {
				GameObject dwelling = (GameObject)i.next();
				setupDwellingNatives(dwelling);
			}
		}
	}

	/**
	 * Do this at the start of the game to guarantee all treasure locations
	 * have their monsters.  Note: does NOT reset row 6 of the setup card!!
	 */
	public static void resetAllTreasureLocationDenizens(GameData data) {
		for (int i=1;i<=6;i++) {
			resetDenizens(data,i);
		}
	}

	/**
	 * This returns the GameObject that holds all the denizens on the setup card
	 */
	public static GameObject getDenizenHolder(GameObject denizen) {
		GameData data = denizen.getGameData();
		String block = denizen.hasAttributeBlock("this_h")?"this_h":"this";
		String holderName = denizen.getAttribute(block,"setup_start");
//System.out.println(denizen.fineDetail());
//System.out.println("holderName="+holderName);
		if (holderName!=null) {
			ArrayList keys = new ArrayList();
			String boardNum = denizen.getThisAttribute(Constants.BOARD_NUMBER);
			if (boardNum!=null) {
				holderName = holderName + " " + boardNum;
				keys.add(Constants.BOARD_NUMBER+"="+boardNum);
			}
			else {
				keys.add("!"+Constants.BOARD_NUMBER);
			}
			keys.add("name="+holderName);
			keys.add("!character");
			keys.add("ts_section");
			GamePool pool = new GamePool(data.getGameObjects());
			ArrayList holders = pool.find(keys);
			
			GameObject denizenHolder = null;
			if (holders.size()==1) {
				// only 1?  Then its obvious
				denizenHolder = (GameObject)holders.iterator().next();
			}
			else {
				// more than 1?  Better crossreference with box_num
				String boxNum = denizen.getAttribute(block,"box_num");
				for (Iterator n=holders.iterator();n.hasNext();) {
					GameObject holder = (GameObject)n.next();
					if (holder==null || holder.getThisAttribute("box_num")==null) {
						return null;
					}
					if (holder.getThisAttribute("box_num").equals(boxNum)) {
						denizenHolder = holder;
						break;
					}
				}
			}
			return denizenHolder;
		}
		return null;
	}

	public static GameObject getDwellingLeader(GameObject dwelling) {
		String setupStart = StringUtilities.capitalize(dwelling.getThisAttribute("dwelling"));
		String boardNumber = dwelling.getThisAttribute(Constants.BOARD_NUMBER);
		ArrayList<String> query = new ArrayList<String>();
		query.add("rank=HQ");
		query.add("setup_start="+setupStart);
		if (boardNumber!=null) {
			query.add(Constants.BOARD_NUMBER+"="+boardNumber);
		}
		GamePool pool = new GamePool(dwelling.getGameData().getGameObjects());
		return pool.findFirst(query);
	}
	public static boolean stillChitsToPlace(HostPrefWrapper hostPrefs) {
		int boards = hostPrefs.getMultiBoardEnabled() ? hostPrefs.getMultiBoardCount() : 1;
		int totalChitsToPlace = boards * 6;
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(hostPrefs.getGameData());
		ArrayList gs = new ArrayList(rom.findObjects("gold_special,"+Constants.GOLD_SPECIAL_PLACED, false));
		int placedChits = gs.size();
		if (!hostPrefs.hasPref(Constants.HOUSE2_NO_MISSION_VISITOR_FLIPSIDE)) {
			placedChits >>= 1; // divide by 2
		}
		return placedChits<totalChitsToPlace;
	}
}