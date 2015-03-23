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
package com.robin.magic_realm.components.wrapper;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import com.robin.game.objects.*;
import com.robin.general.util.HashLists;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.RealmCalendar;

/**
 * A Class to encapsulate permanent/day spell handling
 */
public class SpellMasterWrapper extends GameObjectWrapper {
	
	/*
	 * Durations:
	 * 
	 * Continuous
	 * 	Combat - 42.7/1: remains in effect for the rest of the day; it expires at Midnight.
	 * 	Day - 42.7/2: remains in effect until the end of the next Daylight period; it expires
	 * 				at Sunset of the day after it is cast.
	 * 	Permanent - 42.8: never expire, but they do not continuously affect their targets. A Permanent
	 * 				spell affects its target only when it is "energized" . When it does not affect
	 * 				its target, it is "inert".
	 * 
	 * Delayed
	 * 	Move
	 * 	Phase
	 *  Cave -- CJM -- trying to get this to work
	 * 
	 * Instant
	 * 	Attack
	 * 	Instant
	 */
	
	private static final String PERMANENT_SPELLS = "permanent";
	private static final String DAY_SPELLS = "day";
	private static final String COMBAT_SPELLS = "combat";
	private static final String PHASE_SPELLS = "phase";
	private static final String MOVE_SPELLS = "move";
	
	public SpellMasterWrapper(GameObject gm) {
		super(gm);
	}
	public String getBlockName() {
		return "this";
	}
	/**
	 * @param target	The target to test
	 * 
	 * @return		List of SpellWrapper objects that are currently bewitching the target
	 */
	public ArrayList<SpellWrapper> getAffectingSpells(GameObject target) {
		ArrayList<SpellWrapper> ret = new ArrayList<SpellWrapper>();
		for (SpellWrapper spell:getSpells(null)) {
			if (spell.targetsGameObject(target)) {
				ret.add(spell);
			}
		}
		return ret;
	}
	/**
	 * @return		The single SpellWrapper object that was cast by this incantation object (if any)
	 */
	public SpellWrapper getIncantedSpell(GameObject incantation) {
		for (SpellWrapper spell:getSpells(null)) {
			GameObject si = spell.getIncantationObject();
			if (si!=null && si.equals(incantation)) {
				return spell;
			}
		}
		return null;
	}
	public ArrayList getList(String key) {
		ArrayList list = super.getList(key);
		if (list==null) {
			return new ArrayList();
		}
		return list;
	}
	/**
	 * @param location			The location to search
	 * @param needForCancel		If true, excludes noCancel spells
	 * 
	 * @return				A list of ALL breakable spells (currently Combat,Day,Permanent) that are in the clearing.
	 */
	public ArrayList getAllSpellsInClearing(TileLocation location,boolean needForCancel) {
		ArrayList ret = new ArrayList();
		for (Iterator i=getSpells(null).iterator();i.hasNext();) {
			SpellWrapper spell = (SpellWrapper)i.next();
			if (!needForCancel || !spell.isNoCancelSpell()) {
				TileLocation test = spell.getCurrentLocation();
				if (test!=null) { // test might be null if the spell is targeting a treasure that hasn't yet been seen
					if (test.equals(location)) {
						ret.add(spell);
					}
					else if (test.isTileOnly()) {
						ret.add(spell);
					}
				}
			}
		}
		return ret;
	}
	private ArrayList<SpellWrapper> getSpells(String duration) {
		GameData data = getGameObject().getGameData();
		ArrayList ids = new ArrayList();
		if (duration==null) {
			ids.addAll(getList(PERMANENT_SPELLS));
			ids.addAll(getList(DAY_SPELLS));
			ids.addAll(getList(COMBAT_SPELLS));
			ids.addAll(getList(PHASE_SPELLS));
			ids.addAll(getList(MOVE_SPELLS));
		}
		else {
			ids.addAll(getList(duration));
		}
		ArrayList<SpellWrapper> ret = new ArrayList<SpellWrapper>();
		for (Iterator i=ids.iterator();i.hasNext();) {
			String id = (String)i.next();
			GameObject go = data.getGameObject(Long.valueOf(id));
			ret.add(new SpellWrapper(go));
		}
		return ret;
	}
	public void breakAllIncantations(boolean markIncantationChitsAsUsed) {
		for (SpellWrapper spell:getSpells(null)) {
			spell.breakIncantation(markIncantationChitsAsUsed);
		}
	}
	public void expireAllSpells() {
		// Expire the spells, one at a time
		for (SpellWrapper spell:getSpells(null)) {
			spell.expireSpell();
		}
		
		// Clear all spell lists
		setBoolean(DAY_SPELLS,false);
		setBoolean(COMBAT_SPELLS,false);
		setBoolean(PERMANENT_SPELLS,false);
		setBoolean(PHASE_SPELLS,false);
		setBoolean(MOVE_SPELLS,false);
	}
	/**
	 * Causes all day spells to expire
	 */
	public void expireDaySpells() {
		// Expire the spells, one at a time
		for (SpellWrapper spell:getSpells(DAY_SPELLS)) {
			spell.expireSpell();
		}
		
		// Clear the day spell list
		setBoolean(DAY_SPELLS,false);
	}
	
	/**
	 * Causes all combat spells to expire
	 */
	public void expireCombatSpells() {
		// Expire the spells, one at a time
		for (SpellWrapper spell:getSpells(COMBAT_SPELLS)) {
			spell.expireSpell();
		}

		// Clear the combat spell list
		setBoolean(COMBAT_SPELLS,false);
	}
	/**
	 * Causes all phase spells to expire
	 * 
	 * @return true when spells were expired
	 */
	public boolean expirePhaseSpells() {
		boolean ret = false;
		// Expire the spells, one at a time
		for (SpellWrapper spell:getSpells(PHASE_SPELLS)) {
			spell.expireSpell();
			ret = true;
		}

		// Clear the phase spell list
		setBoolean(PHASE_SPELLS,false);
		return ret;
	}
	/**
	 * Calculates spell locations, and figures out the colors (infinite sources only here)
	 */
	public void energizePermanentSpells(JFrame frame,GameWrapper game) {
		HashLists conflicts = new HashLists();
		for (SpellWrapper spell:getSpells(PERMANENT_SPELLS)) {
			if (spell.isInert()) { // no point energizing non-inert spells!
				TileLocation loc = spell.getCurrentLocation();
				if (loc==null) {
					// This means the spell was lost somehow
					spell.expireSpell();
				}
				else {
					if (spellCanEnergize(game,loc,spell,true)) {
						if (spell.canConflict()) {
							// Before we can add this, need to make sure that the affected target isn't already
							// afflicted with a STRONGER spell
							boolean addSpell = true;
							
							int str = spell.getConflictStrength();
							GameObject at = spell.getAffectedTarget().getGameObject();
							ArrayList<SpellWrapper> affSpells = getAffectingSpells(at);
							for (SpellWrapper affSpell:affSpells) {
								if (!affSpell.isInert() && affSpell.canConflict() && !affSpell.equals(spell)) {
									int aStr = affSpell.getConflictStrength();
									if (aStr>=str) {
										// The active spell gets priority because it is equal to or greater in strength than this spell
										addSpell = false;
										break;
									}
								}
							}
							
							if (addSpell) {
								conflicts.put(at,spell);
							}
						}
						else {
							spell.affectTargets(frame,game,false);
						}
					}
				}
			}
		}
		
		// Resolve conflicts per target (if any)
		for (Iterator i=conflicts.keySet().iterator();i.hasNext();) {
			GameObject target = (GameObject)i.next();
			SpellWrapper strongest = null;
			ArrayList list = conflicts.getList(target);
			if (list.size()==1) {
				// No conflict!
				strongest = (SpellWrapper)list.get(0);
			}
			else {
				// Multiple spells affecting target - find the strongest one
				ArrayList<SpellWrapper> strongGroup = new ArrayList<SpellWrapper>();
				int bestStrength = 0;
				for (Iterator n=list.iterator();n.hasNext();) {
					SpellWrapper spell = (SpellWrapper)n.next();
					int strength = spell.getConflictStrength();
					if (strength > bestStrength) {
						strongGroup.clear();
						bestStrength = strength;
					}
					if (strength == bestStrength) {
						strongGroup.add(spell);
					}
				}
				if (strongGroup.size()==1) {
					// Found the strongest spell
					strongest = strongGroup.get(0);
				}
				else {
					// uh-oh, this means there are two spells with equal strength affecting the same target
					// In this case, it is up to the spellcaster to decide which spell goes into effect
					
					// Make sure its all the same caster
					CharacterWrapper commonCaster = null;
					for (SpellWrapper spell:strongGroup) {
						CharacterWrapper caster = spell.getCaster();
						if (commonCaster==null) {
							commonCaster = caster;
						}
						if (!commonCaster.equals(caster)) {
							commonCaster = null;
							break;
						}
					}
					if (commonCaster!=null) {
						// Found a common caster
						commonCaster.setSpellConflicts(strongGroup);
					}
					else {
						// No common caster?  No choice here, but to pick one at random!
						int r = RandomNumber.getRandom(strongGroup.size());
						strongest = strongGroup.get(r);
					}
				}
			}
			if (strongest!=null) {
				strongest.affectTargets(frame,game,false);
			}
		}
	}
	private boolean spellCanEnergize(GameWrapper game,TileLocation loc,SpellWrapper spell,boolean includeCalendar) {
		RealmCalendar cal = RealmCalendar.getCalendar(game.getGameObject().getGameData());
		ArrayList infiniteSources = new ArrayList();
		if (loc.isInClearing()) {
			infiniteSources.addAll(loc.clearing.getAllSourcesOfColor(true));
		}
		else if (loc.isBetweenClearings()) {
			infiniteSources.addAll(loc.clearing.getAllSourcesOfColor(true));
			infiniteSources.addAll(loc.getOther().clearing.getAllSourcesOfColor(true));
		}
		else if (loc.isTileOnly()) {
			infiniteSources.addAll(loc.tile.getAllSourcesOfColor());
		}
		else if (loc.isBetweenTiles()) {
			infiniteSources.addAll(loc.tile.getAllSourcesOfColor());
			infiniteSources.addAll(loc.getOther().tile.getAllSourcesOfColor());
		}
		
		if (includeCalendar) {
			// 7th day color magic!
			infiniteSources.addAll(cal.getColorMagic(game.getMonth(),game.getDay()));
		}

		if (infiniteSources.size()>0) {
			ColorMagic spellColor = spell.getRequiredColorMagic();
			if (spellColor==null || infiniteSources.contains(spellColor)) {
				// We got it!
				return true;
//				spell.affectTargets(frame,game,false);
			}
		}
		return false;
	}
	public void deenergizePermanentSpells() {
		// Make each permanent spell "inert", one at a time
		boolean didDeenergize = false;
		for (SpellWrapper spell:getSpells(PERMANENT_SPELLS)) {
			if (!spell.isInert()) { // If spell is already inert, then don't deenergize it!
				// Don't deenergize spells that have an automatic supply of color magic
				if (spellCanEnergize(GameWrapper.findGame(spell.getGameData()),spell.getCurrentLocation(),spell,false)) {
					continue;
				}
				
				if(!spell.isAlwaysActive()){
					spell.unaffectTargets();
					spell.makeInert();
					didDeenergize = true;
				}
			}
		}
		if (didDeenergize) {
			// Yes, this is a dangerous recursion, but necessary I think.  If you Absorb Essence, and then activate a Transform
			// spell, you end up two layers deep in spell wizardry, and this is the only way to guarantee that both are deenergized.
			deenergizePermanentSpells();
		}
	}
	/**
	 * Adds a spell to the master list.  Organizes into three bins:  permanent, day, combat.  All other spell
	 * duration types (instant,attack,phase,move) are ignored here.
	 */
	public void addSpell(SpellWrapper spell) {
		String duration = spell.getGameObject().getThisAttribute("duration");
		if (PERMANENT_SPELLS.equals(duration)) {
			addPermanentSpell(spell);
		}
		else if (DAY_SPELLS.equals(duration)) {
			addDaySpell(spell);
		}
		else if (COMBAT_SPELLS.equals(duration)) {
			addCombatSpell(spell);
		}
		else if (PHASE_SPELLS.equals(duration)) {
			addPhaseSpell(spell);
		}
		else if (MOVE_SPELLS.equals(duration)) {
			addMoveSpell(spell);
		}
	}
	private void addPermanentSpell(SpellWrapper spell) {
		addListItem(PERMANENT_SPELLS,spell.getGameObject().getStringId());
	}
	private void addDaySpell(SpellWrapper spell) {
		addListItem(DAY_SPELLS,spell.getGameObject().getStringId());
	}
	private void addCombatSpell(SpellWrapper spell) {
		addListItem(COMBAT_SPELLS,spell.getGameObject().getStringId());
	}
	private void addMoveSpell(SpellWrapper spell) {
		addListItem(MOVE_SPELLS,spell.getGameObject().getStringId());
	}
	private void addPhaseSpell(SpellWrapper spell) {
		// only add a phase spell that has been activated (has a chit)
		if (spell.hasPhaseChit()) {
			addListItem(PHASE_SPELLS,spell.getGameObject().getStringId());
		}
	}
	

	public void removeSpell(SpellWrapper spell) {
		String duration = spell.getGameObject().getThisAttribute("duration");
		ArrayList list = getList(duration);
		if (list!=null && list.contains(spell.getGameObject().getStringId())) {
			list = new ArrayList(list);
			list.remove(spell.getGameObject().getStringId());
			setList(duration,list);
		}
	}
	public void expireIncantationSpell(GameObject incantation) {
		SpellWrapper spell = getIncantedSpell(incantation);
		if (spell!=null) {
			spell.expireSpell();
		}
	}
	public void expireBewitchingSpells(GameObject target) {
		expireBewitchingSpells(target,null,false);
	}
	public void expireBewitchingSpells(GameObject target,SpellWrapper exclude) {
		expireBewitchingSpells(target,exclude,false);
	}
	public void nullifyBewitchingSpells(GameObject target,SpellWrapper exclude) {
		expireBewitchingSpells(target,exclude,true);
	}
	private void expireBewitchingSpells(GameObject target,SpellWrapper exclude,boolean nullify) {
		for (SpellWrapper spell:getAffectingSpells(target)) {
			if (exclude!=null && exclude.getGameObject().equals(spell.getGameObject())) continue;
			if (!nullify) {
				spell.removeTarget(target);
				if (spell.getTargetCount()==0) {
					spell.expireSpell();
//System.err.println(spell.getGameObject().getName()+" is expired");
				}
			}
			else {
				spell.nullifySpell();
//System.err.println(spell.getGameObject().getName()+" is nullified");
			}
		}
	}
	public void restoreBewitchingNullifiedSpells(GameObject target,SpellWrapper exclude) {
		for (SpellWrapper spell:getAffectingSpells(target)) {
			if (exclude==null || !exclude.getGameObject().equals(spell.getGameObject())) {
				if (spell.isNullified()) {
					spell.restoreSpell();
//System.err.println(spell.getGameObject().getName()+" is restored after nullification");
				}
			}
		}
	}
	
	private static final String SPELL_MASTER_KEY = "__RealmSpellMaster_";
	public static Long MASTER_ID = null;
	public static SpellMasterWrapper getSpellMaster(GameData data) {
//System.out.println("MASTER_ID = "+MASTER_ID+", dataid = "+data.dataid);
		if (MASTER_ID==null) {
//System.out.println(data.toIdentifier()+": MASTER_ID is null");
			GamePool pool = new GamePool(data.getGameObjects());
			ArrayList list = pool.find(SPELL_MASTER_KEY);
			GameObject gm = null;
			if (list!=null && list.size()==1) {
				gm = (GameObject)list.iterator().next();
//System.out.println(data.toIdentifier()+": Found a SpellMaster!");
			}
			if (gm==null) {
				gm = data.createNewObject();
				gm.setName(SPELL_MASTER_KEY);
				gm.setThisAttribute(SPELL_MASTER_KEY);
//System.out.println(data.toIdentifier()+": Creating a new SpellMaster!");
			}
			MASTER_ID = new Long(gm.getId());
			return new SpellMasterWrapper(gm);
		}
		else {
			return new SpellMasterWrapper(data.getGameObject(MASTER_ID));
		}
	}
}