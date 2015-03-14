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
package com.robin.magic_realm.RealmSpeak;

import java.util.*;

import com.robin.game.objects.*;
import com.robin.game.server.GameHost;
import com.robin.general.util.HashLists;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.MRMap.*;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.TravelerChitComponent;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public class RealmSpeakInit {
	
	private RealmSpeakFrame frame;
	
	private RealmLoader loader;
	private GameData data; // convenience
	
	private ArrayList<String> appendNames;
	private HostPrefWrapper hostPrefs;
	
	private int lastRating;
	private int mapAttempt;
	
	public RealmSpeakInit(RealmSpeakFrame frame) {
		this.frame = frame;
	}
	public void loadData() {
		loader = new RealmLoader();
		data = loader.getData();
		data.setDataName(GameHost.DATA_NAME);
	}
	public GameData getGameData() {
		return loader.getData();
	}
	public GameData getMaster() {
		return loader.getMaster();
	}
	public void buildGame() {
		// Create all the necessary starting objects now, so we don't have issues with them later!
		GameWrapper.findGame(data);
		SpellMasterWrapper.getSpellMaster(data);
		SummaryEventWrapper sew = SummaryEventWrapper.getSummaryEventWrapper(data);
		sew.addSummaryEvent("RealmSpeak Version "+Constants.REALM_SPEAK_VERSION);
		
		hostPrefs = HostPrefWrapper.findHostPrefs(data);

		// Add any custom characters now
		RealmCharacterBuilderModel.addCustomCharacters(hostPrefs,data);
		
		// Construct quest "deck" if any
		if (hostPrefs.hasPref(Constants.QST_QUEST_CARDS)) {
			prepQuestDeck();
		}
		else if (hostPrefs.hasPref(Constants.QST_BOOK_OF_QUESTS)) {
			prepBookOfQuests();
		}
		else if (hostPrefs.hasPref(Constants.QST_GUILD_QUESTS)) {
			prepGuildQuests();
		}
		
		// Handle all pre-setup initialization
		appendNames = new ArrayList<String>();
		if (hostPrefs.getMixExpansionTilesEnabled()) {
			enableEtilesInLoader(loader);
		}
		if (hostPrefs.getMultiBoardEnabled()) {
			prepMultiboard();
		}
		if (hostPrefs.getMixExpansionTilesEnabled()) {
			prepExpansionMix();
		}
		if (hostPrefs.getIncludeExpansionSpells()) {
			prepExpansionSpells();
		}
		
		// Cleanup happens AFTER multiplications and mixes
		loader.cleanupData(hostPrefs.getGameKeyVals());
		
		// Set numbers for the monsters
		prepMonsterNumbers();
		
		// Do the setup
		StringBuffer sb = new StringBuffer();
		data.doSetup(sb,hostPrefs.getGameSetupName(),GamePool.makeKeyVals(hostPrefs.getGameKeyVals()));
		//System.out.println(sb.toString()); // UNCOMMENT THIS LINE TO SEE SETUP DETAILS
		
		// Mark item starting locations
		markItemStartingLocations();
		
		// Remove any tiles that don't have chits
		cleanupTiles();
		
		// Get rid of unused generators (if playing expansion)
		removeUnusedGenerators();
		
		// Assign all travelers
		assignTravelerTemplates();
		
		// Build the map
		if (hostPrefs.getBoardAutoSetup()) {
			doBoardAutoSetup();
		}
		frame.resetStatus();
		
		// Match up the gold specials
		RealmUtility.doMatchGoldSpecials(data);
		
		// Some items require a spell be cast (Flying Carpet)
		doItemSpellCasting();
	}
	private void cleanupTiles() {
		ArrayList<GameObject> tiles = RealmObjectMaster.getRealmObjectMaster(data).getTileObjects();
		ArrayList<GameObject> unused = new ArrayList<GameObject>();
		for(GameObject tile:tiles) {
			if (tile.getHoldCount()==0) {
				unused.add(tile);
			}
		}
		GameObject.stripListKeyVals("this",hostPrefs.getGameKeyVals(),unused);
		RealmObjectMaster.getRealmObjectMaster(data).resetTileObjects();
	}
	private void removeUnusedGenerators() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> generators = pool.find(hostPrefs.getGameKeyVals()+",generator");
		for (GameObject generator:generators) {
			if (generator.getHoldCount()==0) {
				generator.stripKeyVals("this",hostPrefs.getGameKeyVals());
			}
		}
	}
	private void assignTravelerTemplates() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> travelers = pool.find(hostPrefs.getGameKeyVals()+",traveler");
		for (GameObject go:travelers) {
			TravelerChitComponent traveler = (TravelerChitComponent)RealmComponent.getRealmComponent(go);
			traveler.assignTravelerTemplate();
		}
	}
	private void enableEtilesInLoader(RealmLoader rl) {
		GamePool etilePool = new GamePool(rl.getData().getGameObjects());
		ArrayList<GameObject> etiles = etilePool.find("etile");
		GameObject.setListKeyVals("this",hostPrefs.getGameKeyVals(),etiles);
	}
	private void prepMultiboard() {
		RealmLoader doubleLoader = new RealmLoader();
		if (hostPrefs.getMixExpansionTilesEnabled()) {
			// Make sure the etile objects are available for multiboard duplication
			enableEtilesInLoader(doubleLoader);
		}
		
		RealmCharacterBuilderModel.addCustomCharacters(hostPrefs,doubleLoader.getData());
		
		doubleLoader.cleanupData(hostPrefs.getGameKeyVals());
		int count = hostPrefs.getMultiBoardCount();
		for (int n=0;n<count-1;n++) {
			String appendName = " "+Constants.MULTI_BOARD_APPENDS.substring(n,n+1);
			appendNames.add(appendName);
		}
		for (String appendName:appendNames) {
			long start = data.getMaxId()+1;
			doubleLoader.getData().renumberObjectsStartingWith(start);
			for (Iterator i=doubleLoader.getData().getGameObjects().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				if (!go.hasThisAttribute("season")) { // The one exception
					GameObject dub = data.createNewObject(go.getId());
					dub.copyFrom(go);
					dub.setThisAttribute(Constants.BOARD_NUMBER,appendName.trim());
					dub.setName(dub.getName()+appendName);
				}
			}
		}
		
		// Resolve objects (holds can't be calculated until all are loaded!)
		for (Iterator i=data.getGameObjects().iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			obj.resolveHold(data.getGameObjectIDHash());
		}
		
		// Expand the setup to accommodate the new tiles
		ArrayList<String> tiedPools = new ArrayList<String>();
		tiedPools.add("SPELL_I");
		tiedPools.add("SPELL_II");
		tiedPools.add("SPELL_III");
		tiedPools.add("SPELL_IV");
		tiedPools.add("SPELL_V");
		tiedPools.add("SPELL_VI");
		tiedPools.add("SPELL_VII");
		tiedPools.add("SPELL_VIII");
		data.findSetup(hostPrefs.getGameSetupName()).expandSetup(appendNames,tiedPools,Constants.BOARD_NUMBER);
	}
	private void prepExpansionMix() {
		// Collect all regular tiles in groups and count them
		GamePool tilePool = new GamePool(RealmObjectMaster.getRealmObjectMaster(data).getTileObjects());

		// Remove the Borderland tile from the mixing: it is REQUIRED
		tilePool.extract("name=Borderland");
		
		appendNames.add(0,"");
		for (String appendName:appendNames) {
			String extraQuery = appendName.length()==0?(",!"+Constants.BOARD_NUMBER):(","+Constants.BOARD_NUMBER+"="+appendName.trim());
			ArrayList<GameObject> mountains = tilePool.find("tile_type=M"+extraQuery);
			int mCount = mountains.size();
			ArrayList<GameObject> caves = tilePool.find("tile_type=C"+extraQuery);
			int cCount = caves.size();
			ArrayList<GameObject> valleys = tilePool.find("tile_type=V"+extraQuery);
			int vCount = valleys.size();
			
			// Mix in expansion tiles per group (XC=C and XM=M and S=V)
			mountains.addAll(tilePool.find("tile_type=XM"+extraQuery));
			caves.addAll(tilePool.find("tile_type=XC"+extraQuery));
			valleys.addAll(tilePool.find("tile_type=S"+extraQuery));
			
			// Strip all game key vals (so initially, NONE of these tiles will make it to the map builder)
			GameObject.stripListKeyVals("this",hostPrefs.getGameKeyVals(),mountains);
			GameObject.stripListKeyVals("this",hostPrefs.getGameKeyVals(),caves);
			GameObject.stripListKeyVals("this",hostPrefs.getGameKeyVals(),valleys);
			
			// Random pick an appropriate # of tiles from each group, and add back the game key vals
			for (int i=0;i<mCount;i++) {
				int r = RandomNumber.getRandom(mountains.size());
				GameObject go = mountains.remove(r);
				go.setThisKeyVals(hostPrefs.getGameKeyVals());
				go.setThisAttribute("tile_type","M");
			}
			for (int i=0;i<cCount;i++) {
				int r = RandomNumber.getRandom(caves.size());
				GameObject go = caves.remove(r);
				go.setThisKeyVals(hostPrefs.getGameKeyVals());
				go.setThisAttribute("tile_type","C");
			}
			for (int i=0;i<vCount;i++) {
				int r = RandomNumber.getRandom(valleys.size());
				GameObject go = valleys.remove(r);
				go.setThisKeyVals(hostPrefs.getGameKeyVals());
				go.setThisAttribute("tile_type","V");
			}
		}
		RealmObjectMaster.getRealmObjectMaster(data).resetTileObjects();
	}
	private void prepExpansionSpells() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> expansionSpells = pool.find("spell,rw_expansion_1");
		for (GameObject go:expansionSpells) {
			go.setThisKeyVals(hostPrefs.getGameKeyVals());
		}
	}
	private void prepMonsterNumbers() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<GameObject> monsters = pool.find("monster");
		
		// First, count each type
		HashLists hl = new HashLists();
		for (GameObject go:monsters) {
			hl.put(go.getName(),go);
		}
		
		// Only number those where there is more than one
		for (Iterator i=hl.keySet().iterator();i.hasNext();) {
			String name = (String)i.next();
			ArrayList list = hl.getList(name);
			if (list.size()>1) {
				for (int n=0;n<list.size();n++) {
					GameObject go = (GameObject)list.get(n);
					go.setThisAttribute(Constants.NUMBER,n+1);
				}
			}
		}
	}
	private void markItemStartingLocations() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList<String> query = new ArrayList<String>();
		query.add("item");
		query.add(hostPrefs.getGameKeyVals());
		for(GameObject item:pool.find(query)) {
			GameObject heldBy = item.getHeldBy();
			if (heldBy==null) continue; // shouldn't happen
			item.setThisAttribute(Constants.SETUP,heldBy.getStringId());
		}
	}
	private void doBoardAutoSetup() {
		Collection keyVals = GamePool.makeKeyVals(hostPrefs.getGameKeyVals());
		lastRating = -1;
		mapAttempt = 0;
		MapProgressReportable reporter = new MapProgressReportable() {
			public void setProgress(int current,int total) {
				String lr = "";
				if (lastRating>=0) {
					lr = " (Last Map Rating = "+lastRating+")";
				}
				frame.showStatus("Attempt #"+mapAttempt+":  Building map ... "+current+" out of "+total+lr);
			}
		};
		int minRating = hostPrefs.getMinimumMapRating();
		int rating = -1;
		while(rating<minRating) {
			mapAttempt++;
			while(!MapBuilder.autoBuildMap(data,keyVals,reporter)) {
				mapAttempt++;
			}
			
			rating = MapRating.getMapRating(data);
			//System.out.println("Map Rating = "+rating);
			lastRating = rating;
		}
		
		// Reset treasure location monsters
		RealmUtility.finishBoardSetupAfterBuild(hostPrefs,data);
			
		GameWrapper.findGame(data).setCurrentMapRating(rating);
	}
	private void prepQuestDeck() {
		QuestDeck deck = QuestDeck.findDeck(data);
		for(Quest template:QuestLoader.loadAllQuestsFromQuestFolder()) {
			if (template.getBoolean(QuestConstants.WORKS_WITH_QTR)) {
				int count = template.getInt(QuestConstants.CARD_COUNT);
				if (count>0) {
					// Add the template to the data object and init deck
					Quest quest = template.copyQuestToGameData(data);
					if (quest.isAllPlay()) {
						deck.addAllPlayCard(quest); // count is ignored for all play cards
					}
					else {
						deck.addCards(quest,count);
					}
				}
			}
		}
		deck.shuffle();
	}
	private void prepBookOfQuests() {
		for(Quest template:QuestLoader.loadAllQuestsFromQuestFolder()) {
			if (template.getBoolean(QuestConstants.WORKS_WITH_BOQ)) {
				template.copyQuestToGameData(data);
			}
		}
	}
	private void prepGuildQuests() {
		for(Quest template:QuestLoader.loadAllQuestsFromQuestFolder()) {
			if (template.getBoolean(QuestConstants.FOR_FIGHTERS_GUILD)
					|| template.getBoolean(QuestConstants.FOR_MAGIC_GUILD)
					|| template.getBoolean(QuestConstants.FOR_THIEVES_GUILD)) {
				template.copyQuestToGameData(data);
			}
		}
	}
	private void doItemSpellCasting() {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList query = new ArrayList();
		query.addAll(GamePool.makeKeyVals(hostPrefs.getGameKeyVals()));
		query.add(Constants.CAST_SPELL_ON_INIT);
		Collection needsSpellInit = pool.find(query);
		for (Iterator i=needsSpellInit.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			for (Iterator n=go.getHold().iterator();n.hasNext();) {
				GameObject sgo = (GameObject)n.next();
				if (sgo.hasThisAttribute("spell")) {
					SpellWrapper spell = new SpellWrapper(sgo);
					spell.castSpellNoEnhancedMagic(go);
					spell.addTarget(hostPrefs,go);
					spell.makeInert(); // starts off as inert
				}
			}
		}
	}
}