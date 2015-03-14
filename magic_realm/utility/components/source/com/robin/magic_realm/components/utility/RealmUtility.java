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

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.*;
import com.robin.general.graphics.TextType;
import com.robin.general.io.PreferenceManager;
import com.robin.general.io.ResourceFinder;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.IconFactory;
import com.robin.general.util.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.quest.QuestDeck;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.wrapper.*;

public class RealmUtility {
	
	/**
	 * A utility method for doing all the work that is required to make sure that everything is reset at the 
	 * beginning or end of a game.  Mainly clearing out optimization fields.
	 */
	public static void resetGame() {
		RealmCalendar.reset();
		HallOfFame.save();
		HostPrefWrapper.HOST_PREF_ID = null;
		GameWrapper.GAME_ID = null;
		RealmObjectMaster.resetAll();
		Badge.clearBadgeCache();
		SpellMasterWrapper.MASTER_ID = null;
		SummaryEventWrapper.SEW_ID = null;
		QuestDeck.DECK_ID = null;
		DieRollBuilder.reset();
		RealmComponent.reset();
		SetupCardUtility.reset();
		RealmLogWindow.killSingleton();
		System.gc();
	}
	public static void setupTextType() {
		TextType.addType("TITLE",new Font("Dialog",Font.BOLD,11),Color.blue);
		TextType.addType("TITLE_RED",new Font("Dialog",Font.BOLD,11),Color.red);
		TextType.addType("MINI_RED",new Font("Dialog",Font.PLAIN,9),Color.red);
		TextType.addType("NOTREADY",new Font("Dialog",Font.BOLD,13),MagicRealmColor.FORESTGREEN);
		TextType.addType("CLOSED_RED",new Font("Dialog",Font.BOLD,16),Color.red);
		TextType.addType("YELLOW_BOLD",new Font("Dialog",Font.BOLD,11),Color.yellow);
		TextType.addType("CLEARING_FONT",new Font("Dialog",Font.BOLD,18),MagicRealmColor.YELLOW);
		TextType.addType("NORMAL",new Font("Dialog",Font.PLAIN,10),Color.black);
		TextType.addType("BOLD",new Font("Dialog",Font.BOLD,11),Color.black);
		TextType.addType("WHITE_NOTE",new Font("Dialog",Font.BOLD,11),Color.white);
		TextType.addType("BOLD_BLUE",new Font("Dialog",Font.BOLD,11),Color.blue);
		TextType.addType("BIG_BOLD",new Font("Dialog",Font.BOLD,22),Color.black);
		TextType.addType("BIG_BOLD_BLUE",new Font("Dialog",Font.BOLD,22),Color.blue);
		TextType.addType("ITALIC",new Font("Dialog",Font.ITALIC,11),Color.black);
		TextType.addType("TITLE_GRAY",new Font("Dialog",Font.BOLD,11),Color.gray);
		TextType.addType("STAT_BLACK",new Font("Dialog",Font.BOLD,14),Color.black);
		TextType.addType("STAT_WHITE",new Font("Dialog",Font.BOLD,14),Color.white);
		TextType.addType("STAT_BRIGHT_ORANGE",new Font("Dialog",Font.BOLD,14),Color.orange);
		TextType.addType("STAT_ORANGE",new Font("Dialog",Font.BOLD,14),MagicRealmColor.ORANGE);
		TextType.addType("MED_TITLE_GOLD",new Font("Dialog",Font.BOLD,16),MagicRealmColor.GOLD);
		TextType.addType("INFO_GREEN",new Font("Dialog",Font.BOLD,11),MagicRealmColor.FORESTGREEN);
		TextType.addType("DESTROYED_FONT",new Font("Dialog",Font.BOLD,13),MagicRealmColor.DARKGRAY);
		TextType.addType("AUTHOR",new Font("Dialog",Font.BOLD,9),MagicRealmColor.BLUE);
		TextType.addType("CHAT",new Font("Dialog",Font.PLAIN,14),Color.black);
	}
	/**
	 * Converts mod to a number:
	 * 		L = 1		I = 1
	 * 		M = 2		II = 2
	 * 		H = 3		III = 3
	 * 		T = 4		IV = 4
	 * 					V = 5
	 * 					VI = 6
	 * 					VII = 7
	 * 					VIII = 8
	 */
	public static int convertMod(String mod) {
		if ("L".equals(mod) || "I".equals(mod)) {
			return 1;
		}
		else if ("M".equals(mod) || "II".equals(mod)) {
			return 2;
		}
		else if ("H".equals(mod) || "III".equals(mod)) {
			return 3;
		}
		else if ("T".equals(mod) || "IV".equals(mod)) {
			return 4;
		}
		else if ("V".equals(mod)) {
			return 5;
		}
		else if ("VI".equals(mod)) {
			return 6;
		}
		else if ("VII".equals(mod)) {
			return 7;
		}
		else if ("VIII".equals(mod)) {
			return 8;
		}
		return 0;
	}
	public static void revealAll(GameData data,HostPrefWrapper hostPrefs) {
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList query = new ArrayList();
		query.addAll(GamePool.makeKeyVals(hostPrefs.getGameKeyVals()));
		query.add(RealmComponent.TREASURE);
		ArrayList<GameObject> treasureCards = pool.find(query);
		for (GameObject go:treasureCards) {
			CardComponent card = (CardComponent)RealmComponent.getRealmComponent(go);
			card.setFaceUp();
		}
		
//		query.remove(RealmComponent.TREASURE);
//		query.add(RealmComponent.SOUND);
//		ArrayList<GameObject> chits = pool.find(query);
//		
//		query.remove(RealmComponent.WARNING);
//		query.add(RealmComponent.RED_SPECIAL);
//		chits.addAll(pool.find(query));
//		
//		query.remove(RealmComponent.SOUND);
//		query.add(RealmComponent.WARNING);
//		query.add("!"+RealmComponent.DWELLING);
//		query.add("!"+RealmComponent.TREASURE);
//		chits.addAll(pool.find(query));
//		
//		for (GameObject go:chits) {
//			StateChitComponent chit = (StateChitComponent)RealmComponent.getRealmComponent(go);
//			chit.setFaceUp();
//		}
		
		// Need to refresh map
		query = new ArrayList();
		query.addAll(GamePool.makeKeyVals(hostPrefs.getGameKeyVals()));
		query.add(RealmComponent.TILE);
		ArrayList<GameObject> tiles = pool.find(query);
		for (GameObject tile:tiles) {
			tile.bumpVersion();
			TileComponent tc = (TileComponent)RealmComponent.getRealmComponent(tile);
			tc.setChitsFaceUp();
		}
		CenteredMapView.getSingleton().setReplot(true);
		CenteredMapView.getSingleton().repaint();
	}
	
	public static String getRelationshipNameFor(CharacterWrapper character,RealmComponent trader) {
		return getRelationshipNameFor(getRelationshipBetween(character,trader));
	}
	public static String getRelationshipNameFor(int relationship) {
		if (relationship>RelationshipType.ALLY) relationship = RelationshipType.ALLY;
		if (relationship<RelationshipType.ENEMY) relationship = RelationshipType.ENEMY;
		switch(relationship) {
			case RelationshipType.ALLY:			return "Ally";
			case RelationshipType.FRIENDLY:		return "Friendly";
			case RelationshipType.NEUTRAL:		return "Neutral";
			case RelationshipType.UNFRIENDLY:	return "Unfriendly";
			case RelationshipType.ENEMY:		return "Enemy";
		}
		throw new IllegalStateException("This can't happen!");
	}
	public static int getRelationshipBetween(CharacterWrapper character,RealmComponent trader) {
		int relationship;
		if (trader.isGuild()) {
			relationship = character.isGuildMember(trader) ? character.getCurrentGuildLevel()-1 : -1; // UNFRIENDLY if not a guild member.
		}
		else if (trader.isTraveler()) {
			relationship = 0;
		}
		else {
			// Test for pacification
			Integer pacifyType = trader.getPacifyTypeFor(character);
			if (pacifyType!=null) {
				relationship = pacifyType;
			}
			else {
				relationship = character.getRelationship(trader.getGameObject());
			}
		}
		return relationship;
	}
	public static String getGroupName(RealmComponent rc) {
		String groupName = null;
		if (rc.isNative()) {
			groupName = rc.getGameObject().getThisAttribute("native").toLowerCase();
		}
		else if (rc.isMonster() ||rc.isTraveler()) {
			groupName = rc.getGameObject().getName();
		}
		return groupName;
	}
	/**
	 * @param c			A Collection of GameObjects that have already been identified as natives
	 */
	public static HashLists hashNativesByGroupName(Collection c) {
		HashLists hash = new HashLists();
		for (Iterator i=c.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			hash.put(getGroupName(rc),rc);
		}
		return hash;
	}
	public static String getRelationshipBlockFor(GameObject denizen) {
		String relBlock = Constants.GAME_RELATIONSHIP;
		if (denizen.hasThisAttribute(Constants.BOARD_NUMBER)) {
			relBlock = relBlock + denizen.getThisAttribute(Constants.BOARD_NUMBER);
		}
		return relBlock;
	}
	public static String getRelationshipGroupName(GameObject denizen) {
		RealmComponent rc = RealmComponent.getRealmComponent(denizen);
		if (rc.isNative()) {
			return denizen.getThisAttribute("native");
		}
		else if (rc.isGuild()) {
			return StringUtilities.capitalize(denizen.getThisAttribute("guild"))+" Guild";
		}
		return denizen.getThisAttribute("visitor");
	}
	public static TileComponent findTileForCode(GameData data,String code) {
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(data).getTileObjects());
		
		// code might include a board number!
		String boardNumber = null;
		int bracket = code.indexOf('[');
		if (bracket>0) {
			int other = code.indexOf(']');
			boardNumber = code.substring(bracket+1,other);
			code = code.substring(0,bracket);
		}
		
		ArrayList keyVals = new ArrayList();
		keyVals.add("tile");
		keyVals.add("code="+code);
		if (boardNumber!=null) {
			keyVals.add(Constants.BOARD_NUMBER+"="+boardNumber);
		}
		
		GameObject tileObject = (GameObject)pool.find(keyVals).iterator().next(); // hacky!!
		TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(tileObject);
		return tile;
	}
	public static String getLevelChangeString(int change) {
		StringBuffer sb = new StringBuffer();
		if (change==0) {
			sb.append("No Change");
		}
		else {
			if (change<0) {
				sb.append("Decrease ");
			}
			else if (change>0) {
				sb.append("Increase ");
			}
			change = Math.abs(change);
			sb.append(change);
			sb.append(" level");
			sb.append(change==1?"":"s");
		}
		return sb.toString();
	}
	public static void fetchStartingSpells(JFrame parent,CharacterWrapper character,GameData data,boolean enchantOption) {
		
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(data);
		String hostKeyVals = hostPrefs.getGameKeyVals();
		
		GamePool pool = new GamePool(data.getGameObjects());
        int startingSpellLevel = character.getCharacterLevel();
        if (startingSpellLevel > 4)
        {
            startingSpellLevel = 4;
        }
		String levelKey = "level_"+startingSpellLevel;
		Collection types = character.getGameObject().getAttributeList(levelKey,"spelltypes"); // like [I,VII] (for example)
		if (types!=null && !types.isEmpty()) {
			int spellCount = character.getGameObject().getAttributeInt(levelKey,"spellcount");
			ArrayList<GameObject> choiceSpells = new ArrayList<GameObject>();
			for (Iterator i=types.iterator();i.hasNext();) {
				String type = (String)i.next();
				ArrayList keyVals = new ArrayList();
				keyVals.add(hostKeyVals);
				keyVals.add("spell="+type);
				keyVals.add("!"+Constants.BOARD_NUMBER); // no need to show duplicate spells
				keyVals.add("!"+Constants.SPELL_INSTANCE); // instance spells are clones of the original spells in a character's spellbook
				choiceSpells.addAll(pool.extract(keyVals));
			}
			
			SpellSelector chooser = new SpellSelector(parent,data,choiceSpells,spellCount);
			chooser.setChits(character);
			chooser.setVisible(true);
			
			for (Iterator i=chooser.getSpellSelection().iterator();i.hasNext();) {
				GameObject spell = (GameObject)i.next();
				character.startingSpell(spell);
			}
		}
//		if (enchantOption) {
//			Collection magicChits = character.getEnchantableChits();
//			if (!magicChits.isEmpty()) {
//				ChitChooser chooser = new ChitChooser(new JFrame(),"Enchant chits?",character.getEnchantableChits(),character.getRecordedSpells(data)) {
//					protected boolean canPressOkay() {
//						return true;
//					}
//					protected void clickedChit(ChitComponent chit) {
//						if (chit.isActionChit()) {
//							CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
//							if (achit.isColor()) {
//								achit.makeActive();
//							}
//							else if (achit.isActive()) {
//								achit.enchant();
//							}
//						}
//					}
//				};
//				chooser.setVisible(true);
//			}
//		}
	}
	/**
	 * Returns all unhired natives in the clearing
	 * 
	 * @return		Returns a HashLists object, where RealmComponent lists are keyed by groupName (lowercase)
	 */
	public static HashLists getUnhiredNatives(Collection denizens) {
		HashLists hash = new HashLists();
		for (Iterator i=denizens.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			if (rc.isNative() && rc.getOwner()==null) {
				String groupName = rc.getGameObject().getThisAttribute("native").toLowerCase();
				hash.put(groupName,rc);
			}
		}
		return hash;
	}
	/**
	 * A method for making dead any RealmComoponent that lives
	 */
	public static void makeDead(RealmComponent rc) {
		if (rc==null || rc.getGameObject().hasThisAttribute(Constants.DEAD)) {
			// No point making something DEAD twice!!
			return;
		}
		
		TileLocation rcLocation = ClearingUtility.getTileLocation(rc);
		CombatWrapper combat = new CombatWrapper(rc.getGameObject());
		GameObject killer = combat.getKilledBy();
		RealmComponent killerRc = null;
		if (killer!=null) {
			killerRc = RealmComponent.getRealmComponent(killer);
			if (killerRc!=null && killerRc.isSpell()) { // as in the case of attack spells
				SpellWrapper spell = new SpellWrapper(killer);
				killer = spell.getCaster().getGameObject();
				killerRc = RealmComponent.getRealmComponent(killer);
			}
		}
//		CombatWrapper.clearAllCombatInfo(rc.getGameObject()); // <-- Can't do this, because it removes getKilledBy, and breaks BattleModel.doDisengagement
		
		// Cancel bewitching spells
		SpellMasterWrapper smw = SpellMasterWrapper.getSpellMaster(rc.getGameObject().getGameData());
		smw.expireBewitchingSpells(rc.getGameObject());
		
		// Deal with inventory (only one of these is possible)
		rc.getGameObject().removeThisAttribute(Constants.SPOILS_DONE);
		if (rc.getGameObject().hasThisAttribute(Constants.SPOILS_INVENTORY_TAKEN)) {
			moveInventory(rc.getGameObject(),killer);
			rc.getGameObject().removeThisAttribute(Constants.SPOILS_INVENTORY_TAKEN);
		}
		else if (rc.getGameObject().hasThisAttribute(Constants.SPOILS_INVENTORY_SETUP)) {
			GameObject holder = SetupCardUtility.getDenizenHolder(killer);
			moveInventory(rc.getGameObject(),holder);
			rc.getGameObject().removeThisAttribute(Constants.SPOILS_INVENTORY_SETUP);
		}
		else {
			// This is the default
			if (!rcLocation.isFlying()) { // Had to add this for when wasps die because the queen is killed!
				moveInventory(rc.getGameObject(),rcLocation.tile.getGameObject(),rcLocation.clearing.getNum(),true);
				rc.getGameObject().removeThisAttribute(Constants.SPOILS_INVENTORY_DROP);
			}
		}
		
		// Deal with group belongings (if relevant)
		if (rc.getGameObject().hasThisAttribute(Constants.SPOILS_GROUP_INV_DROP)) {
			GameObject holder = SetupCardUtility.getDenizenHolder(rc.getGameObject());
			moveInventory(holder,rcLocation.tile.getGameObject(),rcLocation.clearing.getNum(),true);
			rc.getGameObject().removeThisAttribute(Constants.SPOILS_GROUP_INV_DROP);
		}
		
		if (rc.isCharacter()) {
			// Character has special dead handling
			CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
			String reason = killer==null?"Killed":("Killed by "+killer.getName());
			character.makeDead(reason);
		}
		else if (rc.isNative() || rc.isHorse() || rc.isMonster() || rc.isTraveler()) {
			// Everything else is generic
			GameObject dead = rc.getGameObject();
			RealmComponent owner = rc.getOwner();
			if (owner!=null) {
				// Make sure hirelings become unhired
				CharacterWrapper character = new CharacterWrapper(owner.getGameObject());
				character.removeHireling(dead);
			}
			ClearingUtility.moveToLocation(dead,null);
			dead.setThisAttribute(Constants.DEAD);
			
			GameObject denizenHolder = SetupCardUtility.getDenizenHolder(dead);
			if (denizenHolder!=null && denizenHolder.hasThisAttribute(Constants.GENERATOR)) {
				// Primary monster was killed, so generator is destroyed
				if (killerRc.getOwnerId()!=null) killerRc = killerRc.getOwner(); // In case killer is a hireling
				CharacterWrapper character = killerRc!=null && killerRc.isCharacter()?new CharacterWrapper(killerRc.getGameObject()):null;
				TreasureUtility.destroyGenerator(character,denizenHolder);
			}
			
			// Check to see if there were any hirelings following this guide
			ArrayList stuff = new ArrayList(rc.getGameObject().getHold());
			for (Iterator i=stuff.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				RealmComponent test = RealmComponent.getRealmComponent(go);
				if (test.isNative() || test.isMonster()) {
					ClearingUtility.moveToLocation(go,rcLocation);
				}
			}
		}
		
		// Last thing to do is clear target and owner
		rc.clearTarget();
		rc.clearOwner();
		
		// Make sure it is light side up for the next regeneration!
		normalizeParticipant(rc);
	}
	public static void normalizeParticipant(RealmComponent rc) {
		if (rc.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)rc;
			monster.setLightSideUp();
			MonsterPartChitComponent weapon = monster.getWeapon();
			if (weapon!=null) {
				weapon.setLightSideUp();
			}
		}
		else if (rc.isNative()) {
			NativeChitComponent nativ = (NativeChitComponent)rc;
			nativ.setLightSideUp();
			NativeSteedChitComponent horse = (NativeSteedChitComponent)nativ.getHorse();
			if (horse!=null) {
				horse.setLightSideUp();
			}
		}
	}
	public static Collection findInventory(RealmComponent victim) {
		ArrayList list = new ArrayList();
		Collection holderHold = new ArrayList(victim.getGameObject().getHold()); // to avoid concurrent mods
		for (Iterator n=holderHold.iterator();n.hasNext();) {
			GameObject go = (GameObject)n.next();
			RealmComponent thing = RealmComponent.getRealmComponent(go);
			if (thing.isItem()) {
				if (!go.hasThisAttribute(Constants.ACTIVATED) || !go.hasThisAttribute("potion")) {
					list.add(go);
				}
			}
		}
		return list;
	}
	public static void transferInventory(JFrame frame,CharacterWrapper from,CharacterWrapper to,ArrayList<GameObject> stuff,ChangeListener listener,boolean requireApproval) {
		for (GameObject item:stuff) {
			if (item.hasThisAttribute(Constants.ACTIVATED)) {
				if (!TreasureUtility.doDeactivate(frame,from,item)) {
					item = null; // can't transfer items that cannot be deactivated!
				}
			}
			if (item!=null) {
				// do the actual transfer
				if (to.getGameObject().hasThisAttribute(RealmComponent.CACHE_CHIT)) {
					// If you are caching stuff, make sure they lose their NEW status
					item.removeThisAttribute(Constants.TREASURE_NEW);
				}
				else {
					// As long as you are not trading with a CACHE, set the treasure as "New"
					item.setThisAttribute(Constants.TREASURE_NEW);
					RealmComponent.getRealmComponent(item).setCharacterTimestamp(to);
				}
				to.getGameObject().add(item);
				
				if (listener!=null) { // listener might be null if you are partway through the cache transfer process.
					to.checkInventoryStatus(frame,item,listener);
					
					// "from" character needs to make sure this doesn't put him/her into an illegal state
					from.checkInventoryStatus(frame,null,listener);
				}
				if (requireApproval) {
					item.setThisAttribute(Constants.REQUIRES_APPROVAL,from.getGameObject().getStringId());
				}
			}
		}
	}
	private static void moveInventory(GameObject inventoryHolder,GameObject destination) {
		moveInventory(inventoryHolder,destination,0,false);
	}
	private static void moveInventory(GameObject inventoryHolder,GameObject destination,int clearing,boolean faceDown) {
		if (inventoryHolder!=null && destination!=null) {
			ArrayList holderHold = new ArrayList();
			holderHold.addAll(inventoryHolder.getHold()); // to avoid concurrent mods
			for (Iterator n=holderHold.iterator();n.hasNext();) {
				GameObject go = (GameObject)n.next();
				RealmComponent thing = RealmComponent.getRealmComponent(go);
				if (thing.isItem() && !thing.isNativeHorse()) { // native horses don't get dropped!
					RealmComponent item = thing;
					if (item.getGameObject().hasThisAttribute(Constants.ACTIVATED)) {
						// If it's activated, then the inventoryHolder is the character!
						if (!TreasureUtility.doDeactivate(null,new CharacterWrapper(inventoryHolder),item.getGameObject())) {
							item = null; // can't transfer items that cannot be deactivated
						}
					}
					if (item!=null) {
						if (item.isWeapon()) {
							((WeaponChitComponent)item).setAlerted(false);
						}
						if (faceDown && item.isTreasure()) {
							CardComponent card = (CardComponent)item;
							card.setFaceDown();
						}
						destination.add(go);
						if (clearing==0) {
							go.removeThisAttribute("clearing");
						}
						else {
							go.setThisAttribute("clearing",clearing);
						}
					}
				}
			}
		}
	}
	public static void findImagesFolderOrExit() {
		File file = new File("images");
		InputStream stream = ResourceFinder.getInputStream("images/images.properties");
		if (stream!=null) {
			try {
				Properties props = new Properties();
				props.load(stream);
				String v = props.getProperty("version");
				if (!Constants.REALM_SPEAK_IMAGES_VERSION.equals(v)) {
					JOptionPane.showMessageDialog(
							null,
							"The images folder version ("+v+") is incorrect (should be "+Constants.REALM_SPEAK_IMAGES_VERSION+").  Please download the newest resource\n"
							+"pack, and replace the images folder before running RealmSpeak.",
							"Invalid Images Folder Version!",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
			catch(IOException ex) {
				JOptionPane.showMessageDialog(
						null,
						"Unable to read the version for the images folder.  You may need to download the newest resource\n"
						+"pack, and replace the images folder before running RealmSpeak.\n\n"
						+ex.toString(),
						"Images Version Read Error!",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		else {
			// Might simply be that they have a version of the images folder that doesn't have the new
			// images.properties file
			if (IconFactory.findIcon("images/tiles/awfulvalley1.gif")==null) {
				JOptionPane.showMessageDialog(
						null,
						"The images folder was not found here:\n\n     "
						+file.getAbsolutePath()
						+"\n\nPlease make sure the images folder\n"
						+"is at the same level as RealmSpeak.jar\nbefore running RealmSpeak.",
						"Images Folder Missing!",
						JOptionPane.ERROR_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(
						null,
						"The images folder version is incorrect.  Please download the newest resource\n"
						+"pack, and replace the images folder before running RealmSpeak.",
						"Invalid Images Folder Version!",
						JOptionPane.ERROR_MESSAGE);
			}
			
			System.exit(0);
		}
	}
	public static MagicChit burnColorChit(JFrame parent,GameWrapper game,CharacterWrapper character) {
		ArrayList<MagicChit> colorChits = character.getColorMagicChits();
		if (!colorChits.isEmpty()) {
			RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(parent,"Choose Color Chit to Fatigue:",true);
			chooser.addRealmComponents(colorChits,false);
			chooser.setVisible(true);
			if (chooser.getSelectedText()!=null) {
				MagicChit colorChit = (MagicChit)chooser.getFirstSelectedComponent();
				burnColorChit(parent,game,character,colorChit);
				return colorChit;
			}
		}
		return null;
	}
	public static void burnColorChit(JFrame parent,GameWrapper game,CharacterWrapper character,MagicChit colorChit) {
		// See if we can energize a permanent spell
		ColorMagic chitColor = colorChit.getColorMagic();
		ArrayList<SpellWrapper> possSpells = new ArrayList<SpellWrapper>();
		TileLocation loc = character.getCurrentLocation();
		SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(character.getGameObject().getGameData());
		for (Iterator i=sm.getAllSpellsInClearing(loc,false).iterator();i.hasNext();) {
			SpellWrapper spell = (SpellWrapper)i.next();
			if (spell.isInert()) { // only inert spells can be reenergized
				ColorMagic spellColor = spell.getRequiredColorMagic();
				if (spellColor==null || spellColor.sameColorAs(chitColor)) {
					possSpells.add(spell);
				}
			}
		}
		if (possSpells.size()>0) {
			Object result = chooseSpell(parent,possSpells,true,true);
			if (result!=null) {
				if (result instanceof SpellWrapper) {
					SpellWrapper spellToEnergize = (SpellWrapper)result;
					
					// Need to make sure this spell doesn't conflict with a stronger spell
					if (spellToEnergize.canConflict()) {
						int str = spellToEnergize.getConflictStrength();
						GameObject at = spellToEnergize.getAffectedTarget().getGameObject();
						SpellMasterWrapper smw = SpellMasterWrapper.getSpellMaster(at.getGameData());
						ArrayList<SpellWrapper> affSpells = smw.getAffectingSpells(at);
						for (SpellWrapper affSpell:affSpells) {
							if (!affSpell.isInert() && affSpell.canConflict() && !affSpell.equals(spellToEnergize)) {
								int aStr = affSpell.getConflictStrength();
								if (aStr>str) {
									JOptionPane.showMessageDialog(
											parent,
											"A stronger spell ("+affSpell.getGameObject().getName()+") prevents the spell from energizing."
											+"\nThe color chit was NOT fatigued.",
											"Cannot Energize",
											JOptionPane.WARNING_MESSAGE);
									return;
								}
							}
						}
					}
					
					RealmLogging.logMessage(character.getGameObject().getName(),"Burns a "+chitColor.getColorName()+" chit to energize "+spellToEnergize.getName());
					spellToEnergize.affectTargets(parent,game,false);
				}
				else {
					RealmLogging.logMessage(character.getGameObject().getName(),"Burns a "+chitColor.getColorName()+" chit.");
				}
			}
			else {
				// Cancelled
				return;
			}
		}
		else {
			int ret = JOptionPane.showConfirmDialog(
					parent,
					"There are no inert spells to energize.  Fatigue the color chit anyway?",
					"No Inert Spells",
					JOptionPane.YES_NO_OPTION);
			if (ret==JOptionPane.NO_OPTION) {
				return;
			}
			RealmLogging.logMessage(character.getGameObject().getName(),"Burns a "+chitColor.getColorName()+" chit.");
		}
		colorChit.makeFatigued();
		RealmUtility.reportChitFatigue(character,colorChit,"Fatigued color chit: ");
	}
	public static Object chooseSpell(JFrame parent,ArrayList<SpellWrapper> possSpells,boolean allowNone,boolean allowCancel) {
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(
				parent,
				"Choose a spell to energize",
				allowCancel);
		int n=0;
		for (SpellWrapper spell:possSpells) {
			String key = "N"+(n++);
			CharacterWrapper caster = spell.getCaster();
			String casterName = (caster!=null && caster.getGameObject().hasThisAttribute("character"))?caster.getGameObject().getName():"";
			chooser.addOption(key,casterName);
			chooser.addRealmComponentToOption(key,RealmComponent.getRealmComponent(spell.getGameObject()));
			ArrayList targets = spell.getTargets();
			if (targets.size()>0) {
				chooser.addRealmComponentToOption(key,(RealmComponent)targets.get(0));
				if (targets.size()>1) {
					// TODO Would be nice to indicate there are other targets...
				}
			}
			GameObject animal = spell.getTransformAnimal();
			if (animal!=null) {
				chooser.addRealmComponentToOption(key,RealmComponent.getRealmComponent(animal));
			}
		}
		if (allowNone) {
			chooser.addOption("None","None");
		}
		chooser.setVisible(true);
		String selText = chooser.getSelectedText();
		if (selText!=null) {
			if (!"None".equals(selText)) {
				return new SpellWrapper(chooser.getFirstSelectedComponent().getGameObject());
			}
			else {
				return "None";
			}
		}
		return null;
	}
	/**
	 * A recursive method for extracting a tree of game objects
	 */
	public static Collection getAllGameObjectsIn(GameObject go,boolean excludeUnseenTreasures) {
		ArrayList ret = new ArrayList();
		for (Iterator i=go.getHold().iterator();i.hasNext();) {
			GameObject ingo = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(ingo);
			if (!excludeUnseenTreasures || !rc.isTreasure() || ingo.hasThisAttribute(Constants.TREASURE_SEEN)) {
				ret.add(ingo);
				ret.addAll(getAllGameObjectsIn(ingo,excludeUnseenTreasures));
			}
		}
		return ret;
	}
	public static PreferenceManager getRealmSpeakPrefs() {
		return new PreferenceManager("RealmSpeak","frame.cfg");
	}
	public static PreferenceManager getWindowLayoutPrefs() {
		return new PreferenceManager("RealmSpeak","windows.cfg");
	}
	public static PreferenceManager getRealmBattlePrefs() {
		return new PreferenceManager("RealmSpeak","battle.cfg");
	}
//	public static void main(String[] args) {
//		System.out.println("Started "+DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
//	}
	public static void popupMessage(JFrame parent,RealmDirectInfoHolder info) {
		ArrayList strings = info.getStrings();
		String title = (String)strings.get(0);
		String message = (String)strings.get(1);
		String rollerStringResult = (String)strings.get(2);
		DieRoller roller = null;
		if (rollerStringResult.length()>0) {
			roller = new DieRoller(rollerStringResult,25,6);
		}
		if (parent==null) {
			JOptionPane.showMessageDialog(new JFrame(), message,title,
					JOptionPane.INFORMATION_MESSAGE,roller==null?null:roller.getIcon());
		}
		else {
			MessageMaster.showMessage(parent, message,title,
					JOptionPane.INFORMATION_MESSAGE,roller==null?null:roller.getIcon());
		}
	}
	public static ArrayList<GameObject> dropNonFlyableStuff(JFrame frame,CharacterWrapper character,Fly fly,TileLocation current) {
		GameObject weightlessItem = character.getWeightlessInactiveItem();
		ArrayList<GameObject> toDrop = new ArrayList<GameObject>();
		for (GameObject item:character.getInventory()) {
			if (item.equals(weightlessItem)) continue; // ignore the weightless item (if any)
			RealmComponent rc = RealmComponent.getRealmComponent(item);
			if (!item.hasThisAttribute("potion")) {// ignore potions (actually, shouldn't really need this line)
				Strength itemWeight = rc.getWeight();
				if (item.hasThisAttribute("horse") // any horse
						|| !fly.getStrength().strongerOrEqualTo(itemWeight)) { // items heavier than fly strength
					toDrop.add(item);
				}
			}
		}
		for (RealmComponent rc:character.getFollowingHirelings()) {
			if (rc.isNative()) {
				NativeChitComponent nativeGuy = (NativeChitComponent)rc;
				if (nativeGuy.getFlySpeed()==null) { // non-flying natives must stay behind too
					toDrop.add(rc.getGameObject());
				}
			}
			else if (rc.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)rc;
				if (monster.getFlySpeed()==null) { // non-flying monsters must stay behind too
					toDrop.add(rc.getGameObject());
				}
			}
			else if (rc.isTraveler()) {
				if (rc.getGameObject().hasThisAttribute(Constants.CAPTURE)) {
					character.removeHireling(rc.getGameObject());
					RealmLogging.logMessage(character.getGameObject().getName(),"The "+rc.getGameObject().getName()+" escaped!");
				}
			}
		}
		
		if (!toDrop.isEmpty()) {
			// Verify that this what the character wants to do!
			JPanel panel = new JPanel(new BorderLayout());
			JPanel things = new JPanel(new FlowLayout());
			for (GameObject item:toDrop) {
				things.add(new JLabel(RealmComponent.getRealmComponent(item).getIcon()));
			}
			panel.add(things,"Center");
			panel.add(new JLabel("If you fly, you will drop these items.  Continue?"),"North");
			int ret = JOptionPane.showConfirmDialog(frame,panel,"Drop Heavy Inventory",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
			if (ret==JOptionPane.NO_OPTION) {
				return null;
			}
			
			for (GameObject item:toDrop) {
				// Make sure activated items are deactivated first
				if (item.hasThisAttribute(Constants.ACTIVATED)) {
					TreasureUtility.doDeactivate(frame,character,item);
				}
				ClearingUtility.moveToLocation(item,current);
			}
		}
		return toDrop;
	}
	public static void doMatchGoldSpecials(GameData data) {
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(data);
		GamePool gsPool = new GamePool(rom.findObjects("gold_special",false));
		GameObject[] chit = new GameObject[2];
		while(!gsPool.isEmpty()) {
			GameObject first = (GameObject)gsPool.get(0);
			String pair = first.getThisAttribute("pair");
			ArrayList list = gsPool.extract("pair="+pair); // should always be 2
			if ((list.size()%2)!=0) throw new IllegalStateException("Gold Special size is not divisible by 2? -> "+list.size());
			while(!list.isEmpty()) {
				chit[0] = (GameObject)list.remove(0);
				chit[1] = (GameObject)list.remove(0);
				chit[0].setThisAttribute("pairid",chit[1].getStringId());
				chit[1].setThisAttribute("pairid",chit[0].getStringId());
			}
		}
	}
	public static void automaticallyEnchantTiles(GameData data,GameWrapper game) {
		// What colors for today?
		RealmCalendar cal = RealmCalendar.getCalendar(data);
		ArrayList<ColorMagic> dayColors = cal.getColorMagic(game.getMonth(),game.getDay());
		
		// Which tiles have this color?
		RealmObjectMaster rom = RealmObjectMaster.getRealmObjectMaster(data);
		ArrayList<TileComponent> enchantableTiles = new ArrayList<TileComponent>();
		for (GameObject tileObject:rom.getTileObjects()) {
			boolean found = false;
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(tileObject);
			for(ClearingDetail clearing:tile.getEnchantedClearings()) {
				for(ColorMagic cm:clearing.getAllSourcesOfColor(true)) {
					if (dayColors.contains(cm)) {
						enchantableTiles.add(tile);
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		
		// Flip em!
		for(TileComponent tile:enchantableTiles) {
			tile.flip();
		}
	}
	public static boolean willBeBlocked(CharacterWrapper character,boolean isFollowing,boolean blockMonsters) {
		// Player's current clearing is checked for monsters, and blocked if needed
		boolean blocked = false;
		if (!character.isMinion() && !character.isHidden() && !isFollowing && !character.isMistLike()) {
			TileLocation tl = character.getCurrentLocation();
			if (tl!=null && tl.hasClearing() && !tl.isBetweenClearings()) {
				ClearingDetail currentClearing = tl.clearing;
				Collection components = currentClearing.getClearingComponents();
				for (Iterator i = components.iterator(); i.hasNext();) {
					RealmComponent rc = (RealmComponent) i.next();
					if (rc instanceof MonsterChitComponent) {
						MonsterChitComponent monster = (MonsterChitComponent)rc;
						// don't block if monster has an owner (until I can get to that piece of code!)
						if (monster.getOwner()==null) {
							// Monsters with Melt-into-Mist affecting them don't block
							if (!monster.isMistLike()) {
								// pacified monsters don't block
								if (!monster.isPacifiedBy(character)) {
									// don't block character if they have immunity to this monster
									RealmComponent charRc = RealmComponent.getRealmComponent(character.getGameObject());
									if (!charRc.isImmuneTo(monster)) {
										blocked = true;
										if (blockMonsters) {
											monster.setBlocked(true); // so monster will stop prowling
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return blocked;
	}
	public static DieRollerLog getDieRollerLog(GameData gameData) {
		GamePool pool = new GamePool(gameData.getGameObjects());
		ArrayList<GameObject> result = pool.find(Constants.DIE_ROLL_LOG);
		GameObject rollLog = null;
		if (result.size()==0) {
			rollLog = gameData.createNewObject();
			rollLog.setName("Die Roll Log");
			rollLog.setThisAttribute(Constants.DIE_ROLL_LOG);
		}
		else {
			rollLog = result.get(0);
		}
		return new DieRollerLog(rollLog);
	}
	
	public static String updateNameToBoard(GameObject source,String name) {
		String boardNumber = source.getThisAttribute(Constants.BOARD_NUMBER);
		if (boardNumber!=null) {
			name = name+" "+boardNumber;
		}
		return name;
	}

	/**
	 * This should be called once the board is built (either by players, or by auto-build) to make sure all clearings that
	 * connect to the borderland are marked appropriately, all chits that don't have clearings (expansion) are placed, and
	 * the setup card is reset and ready to go.
	 */
	public static void finishBoardSetupAfterBuild(HostPrefWrapper hostPrefs,GameData data) {
		ClearingUtility.markBorderlandConnectedClearings(hostPrefs,data);
		ClearingUtility.assignChitClearings(data);
		SetupCardUtility.resetAllTreasureLocationDenizens(data);
		SetupCardUtility.setupDwellingsAndGhosts(hostPrefs,data);
	}
	public static void reportChitFatigue(CharacterWrapper character,MagicChit chit,String title) {
		reportChitFatigue(character,(RealmComponent)chit,title);
	}
	public static void reportChitFatigue(CharacterWrapper character,CharacterActionChitComponent chit,String title) {
		reportChitFatigue(character,(RealmComponent)chit,title);
	}
	public static void reportChitFatigue(CharacterWrapper character,RealmComponent rc,String title) {
		if (rc.isActionChit()) {
			CharacterActionChitComponent actionChit = (CharacterActionChitComponent)rc;
			RealmLogging.logMessage(character.getGameObject().getName(),title+actionChit.getShortName());
		}
	}
	private static final String[] VISITOR = {"Crone","Warlock","Shaman","Scholar"};
	public static String getHTMLPoliticsString(ArrayList<String> list) {
		ArrayList<String> visitors = new ArrayList<String>(Arrays.asList(VISITOR));
		Collections.sort(list);
		StringBufferedList sb = new StringBufferedList(", ","");
		for (String val:list) {
			String name = StringUtilities.capitalize(val);
			if (visitors.contains(name)) {
				sb.append("<i>"+name+"</i>");
			}
			else {
				sb.append(name);
			}
		}
		return sb.toString();
	}
}