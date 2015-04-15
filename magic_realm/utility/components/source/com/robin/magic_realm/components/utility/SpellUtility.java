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

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.*;

import com.robin.game.objects.*;
import com.robin.general.io.ResourceFinder;
import com.robin.general.swing.*;
import com.robin.general.util.RandomNumber;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.effect.SpellEffectContext;
import com.robin.magic_realm.components.swing.CenteredMapView;
import com.robin.magic_realm.components.swing.TileLocationChooser;
import com.robin.magic_realm.components.table.*;
import com.robin.magic_realm.components.wrapper.*;

public class SpellUtility {
	public enum TeleportType {
		ChooseAny,
		ChooseTileTwo,
		RandomClearing,
		KnownGate,
	}
	
	public static void heal(CharacterWrapper character) {
		// Heal all fatigue and wounds - cancels wither curse
		character.removeCurse(Constants.WITHER);
		for (CharacterActionChitComponent chit:character.getWoundedChits()) {
			chit.makeActive();
		}
		for (CharacterActionChitComponent chit:character.getFatiguedChits()) {
			chit.makeActive();
		}
	}
	
	public static void repair(CharacterWrapper character){
		character.getInventory().stream()
			.map(obj -> (GameObject)obj)
			.map(go -> RealmComponent.getRealmComponent(go))
			.filter(rc -> rc.isArmor())
			.map(rc -> (ArmorChitComponent)rc)
			.filter(armor -> armor.isDamaged())
			.forEach(armor -> armor.setIntact(true));
	}
	
	public static ArrayList<SpellWrapper> getBewitchingSpells(GameObject go) {
		SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(go.getGameData());
		return spellMaster.getAffectingSpells(go);
	}
	
	public static ArrayList<SpellWrapper>getBewitchingSpellsWithKey(GameObject target, String key){
		ArrayList<SpellWrapper>result = new ArrayList<SpellWrapper>();
		
		for(SpellWrapper spell:getBewitchingSpells(target)){
			if(spell.isActive() && spell.getGameObject().hasThisAttribute(key)){
				result.add(spell);
			}
		}
		
		return result;
	}
	
	public static boolean affectedByBewitchingSpellKey(GameObject go,String key) {
		GameData gameData = go.getGameData();
		if (gameData!=null) { // can be null in the character builder tool
			SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(go.getGameData());
			for (SpellWrapper spell:spellMaster.getAffectingSpells(go)) {
				if (spell.isActive() && spell.getGameObject().hasThisAttribute(key)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void doTeleport(JFrame frame,String reason,CharacterWrapper character,TeleportType teleportType) {
		// Get the map to pop to the forefront, centered on the clearing, and the move possibilities marked
		TileLocation chosen;
		TileLocation planned = character.getPlannedLocation();
		if (teleportType==TeleportType.RandomClearing) {
			ArrayList<ClearingDetail> clearings = planned.tile.getClearings();
			clearings.remove(planned.clearing); // Any clearing EXCEPT this one
			int r = RandomNumber.getRandom(clearings.size());
			chosen = clearings.get(r).getTileLocation();
			JOptionPane.showMessageDialog(frame,"The "+character.getGameObject().getName()+" teleports to "+chosen,reason,JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			switch(teleportType) {
				case ChooseAny:
					CenteredMapView.getSingleton().setMarkClearingAlertText("Teleport "+character.getGameObject().getName()+" to which clearing?");
					CenteredMapView.getSingleton().markAllClearings(true);
					if (planned.isInClearing()) {
						planned.clearing.setMarked(false);
					}
					break;
				case ChooseTileTwo:
					CenteredMapView.getSingleton().setMarkClearingAlertText("Teleport "+character.getGameObject().getName()+" to which tile?");
					CenteredMapView.getSingleton().markAdjacentTiles(planned.tile,true,1); // recurse once to pick up the second set!
					break;
				case KnownGate:
					ArrayList<GateChitComponent> knownGates = findKnownGatesForCharacter(character);
					if (!knownGates.isEmpty()) {
						CenteredMapView.getSingleton().setMarkClearingAlertText("Which known gate?");
						for (GateChitComponent gate:knownGates) {
							ClearingDetail clearing = gate.getCurrentLocation().clearing;
							clearing.setMarked(true);
						}
					}
					else {
						JOptionPane.showMessageDialog(frame,"The "+character.getGameObject().getName()+" has not discovered any gates!  Spell fails.",reason,JOptionPane.WARNING_MESSAGE);
						return;
					}
					break;
			}
			TileLocationChooser chooser = new TileLocationChooser(frame,CenteredMapView.getSingleton(),planned);
			chooser.setVisible(true);
			chosen = chooser.getSelectedLocation();
		}
		
		character.jumpMoveHistory(); // because we didn't walk here
		character.moveToLocation(null,chosen);
		RealmLogging.logMessage(character.getGameObject().getName(),"Teleported to "+chosen);
		if (teleportType!=TeleportType.RandomClearing) {
			CenteredMapView.getSingleton().markAllClearings(false);
			CenteredMapView.getSingleton().markAllTiles(false);
		}
		CenteredMapView.getSingleton().centerOn(chosen);
		
		// Followers should stay behind!
		for (Iterator i=character.getFollowingHirelings().iterator();i.hasNext();) {
			RealmComponent hireling = (RealmComponent)i.next();
			ClearingUtility.moveToLocation(hireling.getGameObject(),planned);
			if (hireling.getGameObject().hasThisAttribute(Constants.CAPTURE)) {
				// A captured traveler is immediately freed!
				character.removeHireling(hireling.getGameObject());
				RealmLogging.logMessage(character.getGameObject().getName(),"The "+hireling.getGameObject().getName()+" escaped!");
			}
		}
		
		// Be sure to clear out combat...
		character.clearCombat();
		CombatWrapper.clearAllCombatInfo(character.getGameObject());
	}
	private static ArrayList<GateChitComponent> findKnownGatesForCharacter(CharacterWrapper character) {
		GameData gameData = character.getGameObject().getGameData();
		ArrayList<GateChitComponent> knownGates = new ArrayList<GateChitComponent>();
		ArrayList list = character.getOtherChitDiscoveries();
		if (list!=null) {
			for (Iterator i=character.getOtherChitDiscoveries().iterator();i.hasNext();) {
				String discovery = (String)i.next();
				GameObject go = gameData.getGameObjectByName(discovery);
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc.isGate()) {
					knownGates.add((GateChitComponent)rc);
				}
			}
		}
		return knownGates;
	}
	/*
	 * b.1) When a character Teleports due to a Wish, he and all
of his horses and items (regardless of their weight) instantly
move to whatever clearing he chooses. If an individual teleports
to the clearing where he is already located, he does not move.
When a denizen teleports, it goes to the place where it started the
game: a Ghost or Garrison native goes to the clearing where it
started the game, and any other monster or native goes to its box
on the Appearance Chart. Note: If a hired native is teleported to
the Appearance Chart, he instantly becomes unhired.
	 */
	private static enum SummonType {
		undead,
		animal,
		elemental,
	}
	private static MonsterTable getMonsterTableFor(JFrame parent,String summonType) {
		MonsterTable monsterTable = null;
		switch(SummonType.valueOf(summonType)) {
			case undead:
				monsterTable = new RaiseDead(parent);
				break;
			case elemental:
				monsterTable = new SummonElemental(parent);
				break;
			case animal:
				monsterTable = new SummonAnimal(parent);
				break;
		}
		return monsterTable;
	}
	public static void summonRandomCompanions(JFrame parent,GameObject caster,CharacterWrapper character,SpellWrapper spell,String summonType) {
		MonsterTable monsterTable = getMonsterTableFor(parent,summonType);
		DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,character).createRoller(monsterTable);
		roller.rollDice(summonType);
		String result = monsterTable.apply(character,roller);
		RealmLogging.logMessage(caster.getName(),monsterTable.getTableName(true)+" roll: "+roller.getDescription());
		RealmLogging.logMessage(caster.getName(),monsterTable.getTableName(true)+" result: "+result);
		ArrayList list = spell.getGameObject().getThisAttributeList("created");
		if (list==null) {
			list = new ArrayList();
		}
		for(GameObject go:monsterTable.getMonsterCreator().getMonstersCreated()) {
			list.add(go.getStringId());
		}
		spell.getGameObject().setThisAttributeList("created",list);
	}
	
	public static ArrayList<GameObject> getCreatedCompanions(SpellWrapper spell) {
		GameData gameData = spell.getGameObject().getGameData();
		ArrayList<GameObject> created = new ArrayList<GameObject>();
		ArrayList list = spell.getGameObject().getThisAttributeList("created");
		if (list!=null) {
			for(Iterator i=list.iterator();i.hasNext();) {
				String id = (String)i.next();
				GameObject go = gameData.getGameObject(Long.valueOf(id));
				created.add(go);
			}
		}
		return created;
	}
	public static void unsummonCompanions(SpellWrapper spell) {
		CharacterWrapper caster = spell.getCaster();
		ArrayList<GameObject> created = getCreatedCompanions(spell);
		for (GameObject go:created) {
			go.removeThisAttribute("clearing");
			go.setThisAttribute(Constants.DEAD);
			caster.removeHireling(go);
			GameObject heldBy = go.getHeldBy();
			if (heldBy!=null) {
				heldBy.remove(go);
			}
		}
	}
	public static int getSpellCount(GameObject spellLocation,Boolean awakened,boolean excludeAsteriskType) {
		return getSpells(spellLocation,awakened,excludeAsteriskType,false).size();
	}
	public static ArrayList<GameObject> getSpells(GameObject spellLocation,Boolean awakened,boolean excludeAsteriskType,boolean ignoreEnchanted) {
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		
		RealmComponent sl = RealmComponent.getRealmComponent(spellLocation);
		if (ignoreEnchanted || !sl.isEnchanted()) { // enchanted artifacts/books cannot have active spells!
			for (Iterator i=spellLocation.getHold().iterator();i.hasNext();) {
				GameObject obj = (GameObject)i.next();
				RealmComponent rc = RealmComponent.getRealmComponent(obj);
				if (rc.isSpell()) {
					String spellType = obj.getThisAttribute("spell");
					if (spellType!=null && spellType.trim().length()>0) {
						if (!excludeAsteriskType || !spellType.trim().equals("*")) {
							if (awakened==null || obj.hasThisAttribute(Constants.SPELL_AWAKENED)==awakened.booleanValue()) {
								list.add(obj);
							}
						}
					}
				}
			}
		}
		return list;
	}
	
	public static String getColorSourceName(RealmComponent rc) {
		String colorName;
		if (rc.getGameObject().hasThisAttribute(Constants.MOD_COLOR_SOURCE)) {
			colorName = rc.getGameObject().getThisAttribute(Constants.MOD_COLOR_SOURCE);
		}
		else {
			colorName = rc.getGameObject().getThisAttribute("color_source");
		}
		return colorName;
	}
	
	public static ArrayList<ColorMagic> getSourcesOfColor(RealmComponent test) {
		ArrayList<ColorMagic> colors = new ArrayList<ColorMagic>();
		ArrayList seen = ClearingUtility.dissolveIntoSeenStuff(test);
		for (Iterator i=seen.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			String colorName = getColorSourceName(rc);
			ColorMagic cm = ColorMagic.makeColorMagic(colorName,true);
			if (cm!=null) {
				colors.add(cm);
			}
		}
		return colors;
	}
	public static ColorMagic getColorMagicFor(RealmComponent rc) {
		return ColorMagic.makeColorMagic(getColorSourceName(rc),true);
	}
	public static int chooseRedDie(JFrame parent,String spellKey,CharacterWrapper character) {
		String table = null;
		if (Wish.KEY.equalsIgnoreCase(spellKey)) {
			table = "smallblessing";
		}
		else if (Curse.KEY.equalsIgnoreCase(spellKey)) {
			table = "curse";
		}
		else if (PowerOfThePit.KEY.equalsIgnoreCase(spellKey)) {
			table = "powerofthepit";
		}
		else if ("violentstorm".equalsIgnoreCase(spellKey)) {
			table = "violentstorm";
		}
		else if ("transform".equalsIgnoreCase(spellKey)) {
			table = "transform";
		}
		return DieFaceChooser.getRedDieFace(parent,StringUtilities.capitalize(spellKey),character.getGameObject().getName()+" has the ability to control the RED die.  Choose a result:",table);
	}
	private static String getSpellReferenceName(GameObject spell) {
		String name = spell.getName().toLowerCase();
		name = StringUtilities.findAndReplace(name, " ", "");
		name = StringUtilities.findAndReplace(name, "'", "");
		return name;
	}
	public static String getSpellDetail(GameObject spell) {
		String name = getSpellReferenceName(spell);
		String resource = "text/"+name+".rtf";
		StringBuffer sb = new StringBuffer();
		try {
			InputStream stream = ResourceFinder.getInputStream(resource);
			if (stream==null) {
				throw new IOException("");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while((line=reader.readLine())!=null) {
				sb.append(line);
			}
		}
		catch(IOException ex) {
			sb.append("Resource not found: "+resource);
		}
		return sb.toString();
	}
	public static ImageIcon getSpellDetailTable(GameObject spell) {
		String name = getSpellReferenceName(spell);
		String tableResource = "images/tables/"+name+".gif";
		ImageIcon table = null;
		if (tableResource!=null) {
			table = IconFactory.findIcon(tableResource);
		}
		return table;
	}

	public static void ApplyNamedSpellEffectToTarget(String effect, GameObject target, SpellWrapper spellWrapper) {

			if(!target.hasThisAttribute(effect)){
				target.setThisAttribute(effect);
			}
			else{
				spellWrapper.expireSpell();
				target.setThisAttribute(effect);
				RealmLogging.logMessage(spellWrapper.getCaster().getGameObject().getName(),"Spell expired, because the targeted character already has this ability.");
			}
		
	}

	public static void setAlteredSpeed(RealmComponent chit, String attributeName, SpellWrapper spellWrapper) {	
		String attributeValue = chit.getGameObject().getThisAttribute(attributeName);
		int newspeed = spellWrapper.getGameObject().getThisInt(attributeValue);
		chit.getGameObject().setThisAttribute("move_speed_change", newspeed);
	}
	
	public static void createPhaseChit(RealmComponent target, GameObject spell){
		CharacterWrapper character = new CharacterWrapper(target.getGameObject());
		GameObject phaseChit = spell.getGameData().createNewObject();
		
		phaseChit.setName(spell.getName()+" Phase Chit ("+character.getGameObject().getName()+")");
		phaseChit.copyAttributeBlockFrom(spell,"phase_chit");
		phaseChit.renameAttributeBlock("phase_chit","this");
		
		phaseChit.setThisAttribute("spellID", spell.getStringId());
		spell.setThisAttribute("phaseChitID",phaseChit.getStringId());
		character.getGameObject().add(phaseChit);
	}

	public static boolean TargetsAreBeingAttackedByHirelings(ArrayList<GameObject>attackers, GameObject caster) {
		boolean result = attackers.stream()
			.map(atk -> RealmComponent.getRealmComponent(atk))
			.filter(rc -> !rc.getGameObject().equals(caster)) //all but caster
			.map(rc -> rc.getOwner())
			.anyMatch(owner -> owner != null && owner.getGameObject().equals(caster)); //owned by caster
		
		return result;
	}
	
	public static Optional<GameObject> findNativeFromTheseGroups(ArrayList<String>groups, Predicate<GameObject>predicate, GameWrapper game){
		ArrayList<String>lowerCaseGroups = groups.stream()
				.map(g -> g.toLowerCase())
				.collect(Collectors.toCollection(ArrayList::new));
		
		return game.getGameData().getGameObjects().stream()
		.filter(go -> go.hasThisAttribute("native"))
		.filter(go -> go.hasThisAttribute("denizen"))
		.filter(go -> lowerCaseGroups.contains(go.getThisAttribute("native").toLowerCase()))
		.filter(predicate)
		.sorted(new NativeHireOrder())
		.findFirst();
	}
	
	public static Optional<GameObject> findNativeFromTheseGroups(String group, Predicate<GameObject>predicate, GameWrapper game){
		return game.getGameData().getGameObjects().stream()
		.filter(go -> go.hasThisAttribute("native"))
		.filter(go -> go.hasThisAttribute("denizen"))
		.filter(go -> go.getThisAttribute("native").toLowerCase().equals(group.toLowerCase()))
		.filter(predicate)
		.sorted(new NativeHireOrder())
		.findFirst();	
	}
	
	public static void bringSummonToClearing(CharacterWrapper character, GameObject summon, SpellWrapper spell, ArrayList<GameObject>createdMonsters){
		TileLocation tl = character.getCurrentLocation();
		character.addHireling(summon);
		CombatWrapper combat = new CombatWrapper(summon);
		combat.setSheetOwner(true);
		if (tl!=null && tl.isInClearing()) {
			tl.clearing.add(summon,null);
		}
		character.getGameObject().add(summon); // so that you don't have to assign as a follower right away
		
		ArrayList list = spell.getGameObject().getThisAttributeList("created");
		if (list==null) {
			list = new ArrayList();
		}
		
		if(createdMonsters == null){
			list.add(summon.getStringId());
		} else {
			for(GameObject go:createdMonsters) {
				list.add(go.getStringId());
			}
		}
	}
	
	public static RollResult rollResult(SpellEffectContext context, String rollType){
		DieRoller roller = DieRollBuilder
				.getDieRollBuilder(context.Parent, context.Spell.getCaster(),context.Spell.getRedDieLock())
				.createRoller(rollType.toLowerCase());
		
		int die = roller.getHighDieResult();
		int mod = context.Spell.getGameObject().getThisInt(Constants.SPELL_MOD);
		
		die += mod;
		if (die>=6) die=6;
		if (die<1) die=1;

		
		RealmLogging.logMessage(context.Spell.getCaster().getGameObject().getName(), rollType + " roll: "+ roller.getDescription());
		return new RollResult(roller, roller.getStringResult(), die);
	}

}