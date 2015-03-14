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
package com.robin.magic_realm.components.table;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.*;
import com.robin.general.swing.DieRoller;
import com.robin.general.util.StringBufferedList;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.utility.SpellUtility.TeleportType;
import com.robin.magic_realm.components.wrapper.*;

public class Wish extends RealmTable {
	
	public static final String KEY = "Wish";
	private WindowAdapter visionListener = new WindowAdapter() {
		public void windowClosed(WindowEvent ev) {
			RealmComponentOptionChooser source = (RealmComponentOptionChooser)ev.getSource();
			if (source.getSelectedText()!=null) {
				showVision(source.getSelectedText());
			}
			source.removeWindowListener(visionListener);
		}
	};
	
	private static final String[] RESULT = {
		"I wish I were elsewhere",
		"I wish you were elsewhere",
		"I wish for a vision",
		"I wish for peace",
		"I wish for health",
		"I wish for strength",
	};
	
	public Wish(JFrame frame) {
		super(frame,null);
	}
	public String getTableName(boolean longDescription) {
		return "Wish";
	}
	public String getTableKey() {
		return KEY;
	}
	public String apply(CharacterWrapper character,DieRoller roller) {
//System.out.println("Wish:  REMOVE THESE LINES!!!!!!!!!");
//roller.setValue(0,1);
//roller.setValue(1,1);
//return applyFive(character);
		if (!character.isMistLike() && !character.hasMagicProtection()) {
			WishRunner wr = new WishRunner(this,character,roller);
			SwingUtilities.invokeLater(wr);
			return RESULT[getResult(roller)-1];
		}
		return "Unaffected";
	}
	public void superApply(CharacterWrapper character,DieRoller roller) {
		super.apply(character,roller);
	}
	private class WishRunner implements Runnable {
		private Wish wish;
		private CharacterWrapper character;
		private DieRoller roller;
		public WishRunner(Wish wish,CharacterWrapper character,DieRoller roller) {
			this.wish = wish;
			this.character = character;
			this.roller = roller;
		}
		public void run() {
			wish.superApply(character,roller);
		}
	}
	private String getWishTitle(CharacterWrapper character) {
		return character.getGameObject().getName()+"'s Wish";
	}
	public String applyOne(CharacterWrapper character) {
		JOptionPane.showMessageDialog(getParentFrame(),"\"I wish I were elsewhere\"",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
		// You teleport to any clearing of your choice.
		SpellUtility.doTeleport(getParentFrame(),"\"I wish I were elsewhere\"",character,TeleportType.ChooseAny);
		return RESULT[0];
	}

	public String applyTwo(CharacterWrapper character) {
		JOptionPane.showMessageDialog(getParentFrame(),"\"I wish you were elsewhere\"",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
		// Any character (not yourself), native, or monster is teleported. (back to setup card or original starting clearing)
		// All spells on transported denizens are cancelled.
		
		// This is going to be tricky!  When another character is selected to be teleported, the location is
		// chosen by the transported player!!  Ack!!
		
		// Okay, first locate all possible targets in the clearing
		TileLocation here = character.getCurrentLocation();
		if (here.isInClearing()) {
			ArrayList livingThings = new ArrayList();
			for (Iterator i=here.clearing.getClearingComponents().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isPlayerControlledLeader()) {
					livingThings.add(rc);
					CharacterWrapper aChar = new CharacterWrapper(rc.getGameObject());
					livingThings.addAll(aChar.getFollowingHirelings());
				}
				else if (rc.isNative() || rc.isMonster()) {
					livingThings.add(rc);
				}
			}
			
			// Remove the target from the choices
			livingThings.remove(RealmComponent.getRealmComponent(character.getGameObject()));
			
			if (!livingThings.isEmpty()) {
				RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getParentFrame(),"Who will be teleported?",false);
				chooser.addRealmComponents(livingThings,false);
				chooser.setVisible(true);
				
				RealmComponent rc = chooser.getFirstSelectedComponent();
				
				// First, make sure any following hirelings are left behind
				CharacterWrapper victim = new CharacterWrapper(rc.getGameObject());
				for (RealmComponent fh:victim.getFollowingHirelings()) {
					ClearingUtility.moveToLocation(fh.getGameObject(),here);
				}
				
				// Make sure it has no targets!
				rc.clearTarget();
				
				if (rc.isCharacter()) {
					// Special handling
					
					// First, check and see if target character is owned by the current player
//					if (character.getPlayerName().equals(victim.getPlayerName())) {
					// For now, ignore letting other player do it.
						SpellUtility.doTeleport(getParentFrame(),"\"I wish you were elsewhere\"",victim,TeleportType.ChooseAny);
//					}
//					else {
//						// This is harder!
//						// TODO Cast WISH with result (1) on the character, so that they can teleport themselves.
//						// TODO Nah, that might be harder than necessary.  How about a direct info transmission?
//						GameData gameData = victim.getGameObject().getGameData();
//						RealmDirectInfoHolder info = new RealmDirectInfoHolder(gameData);
//						info.setCommand(RealmDirectInfoHolder.SPELL_WISH_FORCE_TRANSPORT);
//						info.addGameObject(victim.getGameObject());
//						InfoObject io = new InfoObject(victim.getPlayerName(),info.getInfo());
////						if (GameHost.DATA_NAME.equals(gameData.getDataName()) && GameHost.mostRecentHost!=null) {
////							GameHost.mostRecentHost.distributeInfo(io);
////						}
////						else {
//						try {
//							GameClient.mostRecentClient.sendInfoDirect(victim.getPlayerName(),io.getInfo());
//							GameClient.mostRecentClient.doNextRequestNow();
//							GameClient.mostRecentClient.doIdleRequestNow();
//						}
//						catch(Exception ex) {
//							ex.printStackTrace();
//						}
//							
////						}
//					}
				}
				else {
					// Natives and Monsters
					
					// Teleport to starting position (setup card or native dwelling)
					SetupCardUtility.resetDenizen(rc.getGameObject());
					
					TileLocation newLoc = rc.getCurrentLocation();
					if (newLoc==null) {
						// If the denizen returned to the setup card, then make sure they also become unhired
						RealmComponent owner = rc.getOwner();
						if (owner!=null) {
							// Hired and Controlled monsters
							CharacterWrapper employer = new CharacterWrapper(owner.getGameObject());
							// Remove employment/control
							employer.removeHireling(rc.getGameObject()); // this works for hired and controlled
						}
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(getParentFrame(),"No one (other than yourself) in the clearing to teleport!",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
			}
		}
		
		return RESULT[1];
	}

	private Hashtable placeHash;
	private CharacterWrapper visionCharacter;
	public String applyThree(CharacterWrapper character) {
		JOptionPane.showMessageDialog(getParentFrame(),"\"I wish for a vision\"",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
		// Examine treasures in any one box on the setup card
		
		// Select one of the boxes - include all "dwelling" and "treasure_location" objects
		GameData data = character.getGameObject().getGameData();
		HostPrefWrapper hostPref = HostPrefWrapper.findHostPrefs(data);
		GamePool pool = new GamePool();
		pool.addAll(data.getGameObjects());
		String gameKeyVals = hostPref.getGameKeyVals();
		
		placeHash = new Hashtable();
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(getParentFrame(),"Vision - Select one box to examine treasure:",false);
		ArrayList examine = new ArrayList();
		examine.addAll(pool.find(gameKeyVals+",dwelling"));
		examine.addAll(pool.find(gameKeyVals+",treasure_location"));
		examine.addAll(pool.find(gameKeyVals+",visitor"));
		int keyN = 0;
		for (Iterator i=examine.iterator();i.hasNext();) {
			GameObject place = (GameObject)i.next();
			int count = TreasureUtility.getTreasureCardCount(place);
			if (count>0) {
				String key = "N"+(keyN++);
				String title = place.getName()+" ("+count+")";
				chooser.addOption(key,title);
				placeHash.put(title,place);
			}
		}
		if (chooser.hasOptions()) {
			// Need to do this modeless, so the players can look at the map and their notes before deciding
			chooser.setModal(false);
			visionCharacter = character;
			chooser.addWindowListener(visionListener);
			chooser.setVisible(true);
		}
		
		return RESULT[2];
	}
	private void showVision(String target) {
		GameObject place = (GameObject)placeHash.get(target);
		RealmComponentDisplayDialog viewPanel = new RealmComponentDisplayDialog(getParentFrame(),"I wish for a vision","Vision of the "+place.getName());
		Hashtable old = new Hashtable();
		Collection c = TreasureUtility.getTreasureCards(place);
		StringBufferedList list = new StringBufferedList();
		for (Iterator n=c.iterator();n.hasNext();) {
			GameObject treasure = (GameObject)n.next();
			list.append(treasure.getName());
			TreasureCardComponent rc = (TreasureCardComponent)RealmComponent.getRealmComponent(treasure);
			String facing = rc.getFacing();
			old.put(treasure,facing);
			// This next line allows me to change facing without saving the change
			treasure.getThisAttributeBlock().put(Constants.FACING_KEY,CardComponent.FACE_UP);
			viewPanel.addRealmComponent(rc);
		}
		viewPanel.setVisible(true);
		visionCharacter.addNote(place,"Vision",list.toString());
		for (Iterator n=c.iterator();n.hasNext();) {
			GameObject treasure = (GameObject)n.next();
			String facing = (String)old.get(treasure);
			// This next line allows me to change facing without saving the change
			treasure.getThisAttributeBlock().put(Constants.FACING_KEY,facing);
		}
	}

	public String applyFour(CharacterWrapper character) {
//		sendMessage(character.getGameObject().getGameData(),
//				getDestClientName(character.getGameObject()),
//				getWishTitle(character),
//				"\"I wish for peace\"");
		
		JOptionPane.showMessageDialog(getParentFrame(),"\"I wish for peace\"",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
		// Combat ends in clearing for the day.  All spells not yet in effect are canceled.
		
		TileLocation location = character.getCurrentLocation();
		if (location.hasClearing() && !location.isBetweenClearings()) {
			CombatWrapper tile = new CombatWrapper(location.tile.getGameObject());
			tile.setPeace(true);
			tile.addPeaceClearing(location.clearing.getNum());
		}
		
		return RESULT[3];
	}

	public String applyFive(CharacterWrapper character) {
		JOptionPane.showMessageDialog(getParentFrame(),"\"I wish for health\"",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
		// Heal all fatigued and wounded chits
		ArrayList toHeal = new ArrayList();
		toHeal.addAll(character.getFatiguedChits());
		toHeal.addAll(character.getWoundedChits());
		CombatWrapper combat = new CombatWrapper(character.getGameObject());
		ArrayList<GameObject> used = combat.getUsedChits();
		for (Iterator i=toHeal.iterator();i.hasNext();) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
			if (!used.contains(chit.getGameObject())) {
				chit.makeActive();
			}
		}
		
		// Remove ILL HEALTH and WITHER curses
		character.removeCurse(Constants.ILL_HEALTH);
		character.removeCurse(Constants.WITHER);
		
		return RESULT[4];
	}

	public String applySix(CharacterWrapper character) {
		JOptionPane.showMessageDialog(getParentFrame(),"\"I wish for strength\"",getWishTitle(character),JOptionPane.INFORMATION_MESSAGE,getRollerImage());
		// The next FIGHT or GLOVES card used will apply T strength
		character.setWishStrength(new Strength("T"));
		return RESULT[5];
	}
}