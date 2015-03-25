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

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.*;
import com.robin.game.server.*;
import com.robin.general.swing.*;
import com.robin.general.util.OrderedHashtable;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.table.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.utility.SpellUtility.TeleportType;

/*
 * SpellWrapper will wrap GameObjects representing Instance Spells owned by a character.  A Spell can be uncast, alive/inert,
 * or alive/energized.  A spell can have any number of targets, but ALWAYS at least one.  The first target in the list (if
 * multiple) is the PRIMARY target.  All spells have an incantationObject, which is often just a MAGIC chit, but can be an
 * artifact or a book.
 */
public class SpellWrapper extends GameObjectWrapper implements BattleChit {
	public static final JFrame dummyFrame = new JFrame(); // this is so affectSpells can work on Battle emulator
	
	private static final String SPELL_ALIVE = "alive";		// Any spell that is alive - could be inert or nullified, but it is alive
	private static final String SPELL_INERT = "inert";		// Inert means that the spell is alive, but not functioning due to lack of color
	private static final String SPELL_AFFECTED = "affected";	// indicates the spell has affected targets
	private static final String SPELL_NULLIFIED = "nullified";	// A spell is nullified when the target is melted into mist, but will be restored when that condition ends
	private static final String SPELL_VIRTUAL = "virtual";		// A virtual spell is an instance of a real spell when cast using Enhanced Magic rules (i.e., the spell isn't tied up)
	public static final String INCANTATION_TIE = "incantation_tie";
	private static final String CASTER_ID = "caster_id";
	
	private static final String TARGET_IDS = "target_ids";
	private static final String TARGET_EXTRA_IDENTIFIER = "target_ex_id";
	private static final String SECONDARY_TARGET = "secondary_target";
	private static final String RED_DIE_LOCK = "red_die_lock";

	private static final String ALWAYS_ACTIVE = "always_active";
	
	public SpellWrapper(GameObject go) {
		super(go);
	}
	public SpellWrapper makeCopy() {
		// Clone the spell
		GameObject copy = getGameObject().getGameData().createNewObject(getGameObject());
		
		// Mark the spell as instance
		copy.setThisAttribute(Constants.SPELL_INSTANCE); // prevents it from appearing in spell chooser for other characters
		
		return new SpellWrapper(copy);
	}
	public String toString() {
		return getGameObject().getName();
	}
	public String getBlockName() {
		return "_s_Block";
	}
	public ColorMagic getRequiredColorMagic() {
		return ColorMagic.makeColorMagic(getGameObject().getThisAttribute("magic_color"),true);
	}
	public boolean canConflict() {
		return getGameObject().hasThisAttribute("spell_strength");
	}
	public int getConflictStrength() {
		return getGameObject().getThisInt("spell_strength");
	}
	public boolean isBenevolent() {
		return getGameObject().hasThisAttribute(Constants.BENEVOLENT);
	}
	/**
	 * Based on the location of its target, this method will return the current locations of the spell.  This will only
	 * matter to permanent spells, as all others expire before energizing is required.
	 */
	public TileLocation getCurrentLocation() {
		// The first target should be sufficient
		RealmComponent rc = getFirstTarget();
		if (rc!=null) {
			if (!rc.isTreasure() || rc.getGameObject().hasThisAttribute(Constants.TREASURE_SEEN)) {
				return ClearingUtility.getTileLocation(rc);
			}
		}
		return null;
	}
	public boolean targetsCharacterOrDenizen() {
		if (!targetsClearing()) {
			for (Iterator i=getTargets().iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isCharacter() || rc.isMonster() || rc.isNative()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * This method is here to help differentiate spells that target individuals versus those that
	 * target a clearing
	 */
	public boolean targetsClearing() {
		String att = getGameObject().getThisAttribute("target");
		return "clearing".equals(att);
	}
	/**
	 * Causes the spell to become alive.  Assumes the proper color was provided.
	 */
	public SpellWrapper castSpell(GameObject incantationObject) {
		return castSpell(incantationObject,false);
	}
	public SpellWrapper castSpellNoEnhancedMagic(GameObject incantationObject) {
		return castSpell(incantationObject,true);
	}
	/**
	 * Finds the spellcaster for a spell
	 */
	private CharacterWrapper findSpellCasterToCastSpell() {
		ArrayList<String> list = new ArrayList<String>();
		GameObject caster = getGameObject().getHeldBy();
		GameObject lastNonNull = caster; 
		while (caster!=null && !caster.hasThisAttribute("character") && !Constants.STORE_SPELLCAST.equals(caster.getThisAttribute(Constants.STORE))) {
			list.add(caster.toString());
			if (list.size()>20) { // That should be big enough to indicate an infinite loop
				System.err.println("Hit an infinite loop condition in findSpellCasterToCastSpell:");
				for (String val:list) {
					System.err.println("    "+val);
				}
				return null;
			}
			lastNonNull = caster;
			caster = caster.getHeldBy();
		}
		GameObject go = caster==null?lastNonNull:caster;
		return go==null?null:new CharacterWrapper(go);
	}
	private SpellWrapper castSpell(GameObject incantationObject,boolean ignoreEnhancedMagic) {
		if (!isVirtual() && isPersistentSpell() && isUsingEnhancedMagic() && !ignoreEnhancedMagic) {
			GameObject virtualSpellGo = getGameObject().copy();
			getGameObject().add(virtualSpellGo);
			SpellWrapper virtualSpell = new SpellWrapper(virtualSpellGo);
			virtualSpell.makeVirtual();
			return virtualSpell.castSpell(incantationObject);
		}
		
		setBoolean(SPELL_AFFECTED,false); // make sure this is cleared out
		setBoolean(SPELL_ALIVE,true);
		setString(INCANTATION_TIE,incantationObject.getStringId());
		incantationObject.addThisAttributeListItem(INCANTATION_TIE,getCastMagicType());
		
		RealmComponent rc = RealmComponent.getRealmComponent(incantationObject);
		if (rc.isActionChit()) {
			// MAGIC chits get "tied-up" with the spell
			getGameObject().add(incantationObject);
		}
		
		// Add the spell to the spell master (only affects day, combat, and permanent spells)
		SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
		sm.addSpell(this);
		
		CharacterWrapper caster = findSpellCasterToCastSpell();
		if (caster!=null) {
			if (caster.getGameObject().hasThisAttribute("character")) {
				// Only log this, if a character is actually casting the spell...
				RealmLogging.logMessage(caster.getGameObject().getName(),"Casts "+getGameObject().getName());
			}
			setString(CASTER_ID, String.valueOf(caster.getGameObject().getId()));
		}
		
		return this;
	}
	public CharacterWrapper getCaster() {
		String id = getString(CASTER_ID);
		if (id!=null) {
			GameObject c = getGameObject().getGameData().getGameObject(Long.valueOf(id));
			return new CharacterWrapper(c);
		}
		return null;
	}
	public GameObject getIncantationObject() {
		if (isAlive()) {
			String id = getString(INCANTATION_TIE);
			if (id!=null) { // Might be null if using Enhanced Magic
				GameObject go = getGameObject().getGameData().getGameObject(Long.valueOf(id));
				return go;
			}
		}
		return null;
	}
	public void makeInert() {
		setBoolean(SPELL_INERT,true);
	}
	public boolean isInert() {
		return getBoolean(SPELL_INERT);
	}
	public void makeVirtual() {
		setBoolean(SPELL_VIRTUAL,true);
	}
	public boolean isVirtual() {
		return getBoolean(SPELL_VIRTUAL);
	}
	public void energize() {
		setBoolean(SPELL_INERT,false);
	}
	public boolean canCast(String clearingCode,int clearingCount) {
		// Only "non-already-cast" spells that are finished (notready indicates the coding isn't in place yet)
		if (!getGameObject().hasThisAttribute("notready") && !isAlive()) {
			String clearingRequirement = getGameObject().getThisAttribute("clearing_req");
			if (clearingRequirement==null || clearingRequirement.equals(clearingCode)) {
				int tileRequirement = getGameObject().getThisInt("tile_req"); // whistle for monsters
				return (tileRequirement==0 || tileRequirement==clearingCount);
			}
		}
		return false;
	}
	public boolean canExpire() {
		return !getGameObject().hasThisAttribute("no_expire");				// The Flying Carpet cannot expire
	}
	
	public void breakIncantation(boolean markIncantationChitsAsUsed) {
		// Break incantation (if any)
		GameObject io = getIncantationObject();
		if (io!=null) {
			ArrayList<String> list = io.getThisAttributeList(INCANTATION_TIE);
			list.remove(getCastMagicType());
			if (list.isEmpty()) {
				io.removeThisAttribute(INCANTATION_TIE);
			}
			else {
				io.removeThisAttribute(INCANTATION_TIE);
				io.setThisAttributeList(INCANTATION_TIE,list);
			}
			setBoolean(INCANTATION_TIE,false);
			
			// Fatigue MAGIC chit (if any)
			RealmComponent rc = RealmComponent.getRealmComponent(io);
			if (rc.isActionChit()) {
				// Return chit to caster
				getCaster().getGameObject().add(io);
				
				// Mark chit as fatigued
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				chit.makeFatigued();
				RealmUtility.reportChitFatigue(getCaster(),chit,"Fatigued chit: ");
				
				if (markIncantationChitsAsUsed) {
					// Mark chit as used
					CombatWrapper combat = new CombatWrapper(getCaster().getGameObject());
					combat.addUsedChit(io);
				}
			}
		}
	}
	/**
	 * Causes this spell to expire, and everything that goes with it
	 */
	public void expireSpell() {
		clearRedDieLock();
		if (isAlive() && canExpire()) {
			// Undo any duration effects
			if (!isInert()) {
				unaffectTargets();
			}
			
			breakIncantation(true);
			
			// Restore any absorbed monsters
			TileLocation loc = getCaster().getCurrentLocation(); // might be null if character is dead!
			boolean casterIsDead = (new CombatWrapper(getCaster().getGameObject())).getKilledBy()!=null;
			
			ArrayList hold = new ArrayList(getGameObject().getHold()); // prevent concurrent mods!
			
			for (Iterator i=hold.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				RealmComponent ab = RealmComponent.getRealmComponent(go);
				if (ab.isMonster() && !ab.getGameObject().hasThisAttribute("animal")) {
					if (affectsCaster() && casterIsDead) {
						RealmUtility.makeDead(ab);
					}
					else {
						ClearingUtility.moveToLocation(go,loc);
					}
				}
				else {
					ClearingUtility.moveToLocation(go,null);
				}
			}
			
			// Remove all targets
			setBoolean(TARGET_IDS,false);
			setBoolean(TARGET_EXTRA_IDENTIFIER,false);
			setBoolean(SECONDARY_TARGET,false);
			
			// Spell dies
			//setBoolean(CASTER_ID,false); // I don't think there is any harm leaving the caster... It's needed for disengagement 5/29/2007
			setBoolean(SPELL_INERT,false);
			setBoolean(SPELL_ALIVE,false);
			setBoolean(SPELL_AFFECTED,false); // Probably redundant
			
			// Remove it from the spell master, just in case
			SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
			sm.removeSpell(this);
		}
	}
	
	public boolean isActive() {
		if(isAlwaysActive()){return true;}
		
		return isAlive() && !isInert() && !isNullified();
	}
	
	public boolean isAlwaysActive() {
		return getGameObject().hasThisAttribute(ALWAYS_ACTIVE);
	}
	public void nullifySpell() {
		unaffectTargets();
		getGameObject().setThisAttribute(SPELL_NULLIFIED);
	}
	public boolean isNullified() {
		return getGameObject().hasThisAttribute(SPELL_NULLIFIED);
	}
	public void restoreSpell() {
		if (isNullified()) {
			GameWrapper game = GameWrapper.findGame(getGameObject().getGameData());
			if (!isInert()) { // Only reenergize, if not inert
				affectTargets(null,game,false);
			}
			getGameObject().removeThisAttribute(SPELL_NULLIFIED);
		}
	}
	public void setRedDieLock(int val) {
		setInt(RED_DIE_LOCK,val);
	}
	public int getRedDieLock() {
		return getInt(RED_DIE_LOCK);
	}
	public void clearRedDieLock() {
		clear(RED_DIE_LOCK);
	}
	public void setExtraIdentifier(String val) {
		setString(TARGET_EXTRA_IDENTIFIER,val);
	}
	public String getExtraIdentifier() {
		return getString(TARGET_EXTRA_IDENTIFIER);
	}
	public void setSecondaryTarget(GameObject val) {
		setString(SECONDARY_TARGET,val.getStringId());
	}
	public GameObject getSecondaryTarget() {
		String id = getString(SECONDARY_TARGET);
		GameObject go = getGameObject().getGameData().getGameObject(Long.valueOf(id));
		return go;
	}
	public boolean removeTarget(GameObject target) {
		if (isAlive() && !isInert()) {
			// If the spell is alive and non-inert, then we'd better disable it's affect on the target (if any)
			unaffect(RealmComponent.getRealmComponent(target));
		}
		
		String removeId = target.getStringId();
		ArrayList targetids = getList(TARGET_IDS);
		if (targetids.contains(removeId)) {
			targetids.remove(removeId);
			return true;
		}
		else if (affectsCaster() && target.equals(getCaster().getGameObject())) {
			// Even though no targets are removed, removing the caster is automatic grounds for expiring the spell!
			expireSpell();
		}
		return false;
	}
	/**
	 * Adds a target of the spell
	 */
	public void addTarget(HostPrefWrapper hostPrefs,GameObject target) {
		addTarget(hostPrefs,target,false);
	}
	/**
	 * @param ignoreBattling		If true, then ignore battling results, because the natives don't know who is targeting them! (Roof Collapses)
	 */
	public void addTarget(HostPrefWrapper hostPrefs,GameObject target,boolean ignoreBattling) {
		addListItem(TARGET_IDS,target.getStringId());
		
		// Be sure to tag the target
		CombatWrapper combat = new CombatWrapper(target);
		GameObject caster = getCaster().getGameObject();
		if (caster!=null && hostPrefs!=null) { // caster might be null if the spell is cast by a treasure (Flying Carpet)
			combat.addAttacker(caster);
			
			CharacterWrapper character = new CharacterWrapper(caster);
			RealmComponent rc = RealmComponent.getRealmComponent(target);
			if (rc.ownedBy(RealmComponent.getRealmComponent(caster))) {
				if (!hostPrefs.hasPref(Constants.TE_BENEVOLENT_SPELLS) || !isBenevolent()) {
					BattleUtility.processTreachery(character,rc);
				}
			}
			
			// if target is an unassigned denizen, move them to their own sheet (sucker punch)
			if (rc.getOwnerId()==null && rc.getTarget()==null) {
				if (!hostPrefs.hasPref(Constants.TE_WATCHFUL_NATIVES)) {
					combat.setSheetOwner(true);
				}
			}
			
			// Make sure we aren't ignoring battling...
			if (!ignoreBattling) {
				// non-battling unhired natives will begin battling the character immediately if attacked
				if (rc.isNative() && !character.isBattling(target)) {
					character.addBattlingNative(target);
				}
				if (rc.isPacifiedBy(character)) {
					// Targeting a pacified monster or native will break the spell
					SpellWrapper spell = rc.getPacificationSpell(character);
					spell.expireSpell();
				}
			}
		}
	}
	public RealmComponent getFirstTarget() {
		ArrayList targetids = getList(TARGET_IDS);
		if(targetids == null)return null;
		
		Object first = targetids.stream().findFirst();
		GameObject target = getGameObject().getGameData().getGameObject(first);
		return RealmComponent.getRealmComponent(target);
	}
	
	public String getTargetsName() {
		RealmComponent rc = getFirstTarget();
		if (rc!=null) {
			return rc.getGameObject().getName();
		}
		return "None";
	}
	
	public ArrayList<RealmComponent> getTargets() {
		ArrayList targetids = getList(TARGET_IDS);
		
		return targetids != null
				? targetids.stream()
						.mapToLong(id -> Long.valueOf((String)id))
						.mapToObj(id -> getGameObject().getGameData().getGameObject(id))
						.map(go -> RealmComponent.getRealmComponent(go))
						.collect(Collectors.toCollection(ArrayList::new))
				: new ArrayList<RealmComponent>();
	}
	/**
	 * This returns the number of actual targets.  If a single target is listed more than once (i.e., Stones Fly), it still is only
	 * counted once.
	 */
	public int getTargetCount() {
		ArrayList targetids = getList(TARGET_IDS);
		
		return targetids != null
				? (int) targetids.stream().distinct().count()
				: 0;
	}
	
	public boolean targetsGameObject(GameObject go) {
		boolean ret = false;
		ArrayList targetids = getList(TARGET_IDS);
		if (targetids!=null) {
			ret = targetids.contains(go.getStringId());
		}
		if (ret==false) {
			RealmComponent at = getAffectedTarget();
			ret = at!=null && at.getGameObject().equals(go);
		}
		return ret;
	}
	/**
	 * @return		true if any one component is targeted
	 */
	public boolean targetsRealmComponents(Collection<?> components) {
		ArrayList targetids = getList(TARGET_IDS);
		if(targetids == null) return false;
		
		return components.stream()
			.map(c -> (RealmComponent)c)
			.anyMatch(rc -> targetids.contains(rc.getGameObject().getStringId()));
	}
	
	public ArrayList<RealmComponent> getTargetedRealmComponents(Collection<?> components) {
		ArrayList targetids = getList(TARGET_IDS);
		
		return targetids != null
				? components.stream()
						.map(c -> (RealmComponent)c)
						.filter(rc -> targetids.contains(rc.getGameObject().getStringId()))
						.collect(Collectors.toCollection(ArrayList::new))
				: new ArrayList<RealmComponent>();
	}
	
	/**
	 * @return		true if the spell is "alive".  Any spell that is cast is alive for the spell's duration, or until
	 * 				it is cancelled.
	 */
	public boolean isAlive() {
		return getBoolean(SPELL_ALIVE);
	}
	public boolean isPersistentSpell() { // Needed for Enhanced Magic
		return !isAttackSpell() && !isInstantSpell();
	}
	/**
	 * @return		true if this spell is the kind that does an attack during a round of combat
	 */
	public boolean isAttackSpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("attack".equals(duration));
	}
	/**
	 * @return		true if this spell is a combat spell
	 */
	public boolean isCombatSpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("combat".equals(duration));
	}
	/**
	 * @return		true if this spell is a day spell
	 */
	public boolean isDaySpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("day".equals(duration));
	}
	/**
	 * @return		true if this spell is an instant spell
	 */
	public boolean isInstantSpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("instant".equals(duration));
	}
	

	/**
	 * @return		true if this spell is a move spell
	 */
	public boolean isMoveSpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("move".equals(duration));
	}
	/**
	 * @return		true if this spell is a permanent spell
	 */
	public boolean isPermanentSpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("permanent".equals(duration));
	}
	/**
	 * @return		true if this spell is a permanent spell
	 */
	public boolean isPhaseSpell() {
		String duration = getGameObject().getThisAttribute("duration");
		return ("phase".equals(duration));
	}
	public boolean hasPhaseChit() {
		return getGameObject().hasThisAttribute("phaseChitID");
	}
	
	/**
	 * @return		true if this spell is a fly chit type of spell
	 */
	public boolean isFlySpell() {
		return getGameObject().hasAttributeBlock(RealmComponent.FLY_CHIT);
	}
	
	/**
	 * @return	true if this is a "no cancel" spell, like the Flying Carpet spell
	 */
	public boolean isNoCancelSpell() {
		return getGameObject().hasThisAttribute("no_cancel");
	}
	
	// Battle Chit Interface
	public RealmComponent getTarget() {
		throw new RuntimeException("getTarget() is not functional in SpellWrapper!!  Use getTargets()");
	}
	public void changeWeaponState(boolean hit) {
		// nothing
	}
	public void flip() {
		// nothing
	}
	public void setFacing(String val) {
		// nothing
	}
	public String getName() {
		return getGameObject().getName();
	}
	public boolean isDenizen() {
		return false;
	}
	public boolean isCharacter() {
		return false;
	}
	public Integer getLength() {
		if (getGameObject().hasThisAttribute("length")) {
			int len = getGameObject().getThisInt("length");
			return new Integer(len);
		}
		return null;
	}
	public Speed getMoveSpeed() {
		return null;
	}
	public Speed getFlySpeed() {
		return null;
	}
	public boolean hasAnAttack() {
		return getAttackCombatBox()>0;
	}
	public Speed getAttackSpeed() {
		RealmComponent rc = RealmComponent.getRealmComponent(getIncantationObject());
		Speed speed = BattleUtility.getMagicSpeed(rc);
		// Check to see if speed is overridden by spell (like Roof Collapses)
		if (getGameObject().hasThisAttribute("attack_speed")) {
			speed = new Speed(getGameObject().getThisInt("attack_speed"));
		}
		return speed;
	}
	public Harm getHarm() {
		Strength strength = new Strength(getGameObject().getThisAttribute("strength"));
		int sharpness = getGameObject().getThisInt("sharpness");
		
		CharacterWrapper caster = getCaster();
		if (caster.hasActiveInventoryThisKey(Constants.ENHANCE_SPELL_SHARPNESS)) {
			sharpness++;
		}
		
		if (sharpness>0 && HostPrefWrapper.findHostPrefs(getGameObject().getGameData()).hasPref(Constants.REV_DAMPEN_FAST_SPELLS)) {
			GameObject go = getIncantationObject();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc.isActionChit()) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				if (chit.getMagicSpeed().getNum()==0) {
					sharpness--;
				}
			}
		}
		return new Harm(strength,sharpness);
	}
	public String getMagicType() {
		return null;
	}
	public String getCastMagicType() {
		return getGameObject().getThisAttribute("spell");
	}
	public int getManeuverCombatBox() {
		CombatWrapper combat = new CombatWrapper(getIncantationObject());
		return combat.getCombatBox();
	}
	public int getAttackCombatBox() {
		CombatWrapper combat = new CombatWrapper(getIncantationObject());
		return combat.getCombatBox();
	}
	public boolean isMissile() {
		return getGameObject().hasThisAttribute("missile");
	}
	public String getMissileType() {
		return getGameObject().getThisAttribute("missile");
	}
	public boolean hitsOnTie() {
		return false;
	}
	public boolean isMonster() {
		return false;
	}
	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs, BattleChit attacker, int box, Harm attackerHarm,int attackOrderPos) {
		// Spells never take hits
		return false;
	}
	private InfoObject buildAnInfoObject(String destClientName,GameData data,String command) {
//		String destClientName = getCaster().getPlayerName();
		RealmDirectInfoHolder info = new RealmDirectInfoHolder(data);
		info.setCommand(command);
		info.addGameObject(getGameObject());
		InfoObject io = new InfoObject(destClientName,info.getInfo());
		return io;
	}
	public void affectTargets(JFrame parent,GameWrapper theGame,boolean expireImmediately) {
		if (getBoolean(SPELL_AFFECTED)) {
			// Don't affect twice in a row!!
			return;
		}
		if (parent==null) {
			parent = dummyFrame;
		}
		if (!getGameObject().hasThisAttribute("host_okay")) {
			String destClientName = getCaster().getPlayerName();
			if (getGameObject().hasThisAttribute("target_client")) {
				
				CharacterWrapper character = new CharacterWrapper(getFirstTarget().getGameObject());
				String name = character.getPlayerName();
				if (name!=null) {
					destClientName = name;
				}
			}
			String command = expireImmediately?RealmDirectInfoHolder.SPELL_AFFECT_TARGETS_EXPIRE_IMMEDIATE:RealmDirectInfoHolder.SPELL_AFFECT_TARGETS;
			GameData data = getGameObject().getGameData();
			if (GameHost.DATA_NAME.equals(data.getDataName())) {
				// Should never "affectTargets" from the host.  Do it on the caster's client.
				if (GameHost.mostRecentHost!=null) {
					GameHost.mostRecentHost.distributeInfo(
							buildAnInfoObject(destClientName,data,command));
					return;
				}
				else {
					throw new IllegalStateException("mostRecentHost is null?");
				}
			}
			else if (GameClient.DATA_NAME.equals(data.getDataName())) {
				// With clients, make sure to affectTargets from the caster's client
				if (GameClient.GetMostRecentClient()!=null) {
					CharacterWrapper caster = getCaster();
					if (caster!=null) {
						// destClientName will be null for the spell that is active on the flying carpet!!
						if (destClientName!=null && !destClientName.equals(GameClient.GetMostRecentClient().getClientName())) {
							GameClient.GetMostRecentClient().sendInfoDirect(
									destClientName,
									buildAnInfoObject(destClientName,data,command).getInfo());
							return;
						}
					}
				}
				else {
					throw new IllegalStateException("mostRecentClient is null?");
				}
			}
		}
		
		if (parent==null) {
			throw new IllegalStateException("Parent should NOT be null here!!");
		}
		
		AffectThread at = new AffectThread(parent,theGame,expireImmediately);
		if (SwingUtilities.isEventDispatchThread()) {
//System.out.println("Already EDT");
			// NON threaded
			at.doAffect();
		}
		else {
//System.out.println("Non EDT - invoke and wait");
			// Run on event dispatch thread!
			//SwingUtilities.invokeLater(at); // THIS is NOT the solution:  this breaks other things.
			try {
				SwingUtilities.invokeAndWait(at); // FIXME This causes a deadlock when WISH/CONTROLBATS are cast at the same time!!!
			}
			catch(InvocationTargetException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private class AffectThread implements Runnable {
		private JFrame parent;
		private GameWrapper theGame;
		private boolean expireImmediately;
		public AffectThread(JFrame parent,GameWrapper theGame,boolean expireImmediately) {
			this.parent = parent;
			this.theGame = theGame;
			this.expireImmediately = expireImmediately;
		}
		public void run() {
			doAffect();
		}
		public void doAffect() {
			// If we get here, then it's okay to proceed
			energize();
			getTargets().forEach(t -> affect(parent, theGame, (RealmComponent)t));	
			
			if (!(isPhaseSpell() && hasPhaseChit())) { // ignore phase spells that still have a phase chit active!!
				setBoolean(SPELL_AFFECTED,true);
			}
			if (expireImmediately) {
				expireSpell();
			}
		}
	}
	private void affect(JFrame parent,GameWrapper theGame,RealmComponent target) {
		if (!isAlive()) {
			// If spell is not alive, it has NO effect
			return;
		}
		GameObject caster = getCaster().getGameObject();
		if (isPhaseSpell() && !hasPhaseChit()) {
			// A Phase spell.  Create a Phase Chit
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			GameObject phaseChit = getGameObject().getGameData().createNewObject();
			phaseChit.setName(getGameObject().getName()+" Phase Chit ("+character.getGameObject().getName()+")");
			phaseChit.copyAttributeBlockFrom(getGameObject(),"phase_chit");
			phaseChit.renameAttributeBlock("phase_chit","this");
			phaseChit.setThisAttribute("spellID",getGameObject().getStringId());
			getGameObject().setThisAttribute("phaseChitID",phaseChit.getStringId());
			character.getGameObject().add(phaseChit);
			// STOP HERE!!!
			return;
		}
		CombatWrapper combat = new CombatWrapper(target.getGameObject());
		if ("weather".equals(getGameObject().getThisAttribute("target"))) {
			String type = getExtraIdentifier();
			if (type.toLowerCase().startsWith("change")) {
				// Change the weather chit
				theGame.updateWeatherChit();
				FrameManager.showDefaultManagedFrame(parent,"The weather chit has been changed.","Change Weather",null,true);
			}
			else {
				// See the weather chit
				int wc = theGame.getWeatherChit();
				WeatherChit chit = new WeatherChit(wc);
				
				FrameManager.showDefaultManagedFrame(parent,"The weather chit is a "+wc,"See Weather",chit.getIcon(),true);
				
				CharacterWrapper character = new CharacterWrapper(caster);
				character.addNote(caster,"See Weather","The weather chit is a "+wc);
			}
		}
		if (target.isCharacter()) {
			// "Character only" effects
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			if (getGameObject().hasThisAttribute("heal")) {
				SpellUtility.heal(character);
			}
			
			if (getGameObject().hasThisAttribute("repair")) {
				character.getInventory().stream()
					.map(obj -> (GameObject)obj)
					.map(go -> RealmComponent.getRealmComponent(go))
					.filter(rc -> rc.isArmor())
					.map(rc -> (ArmorChitComponent)rc)
					.filter(armor -> armor.isDamaged())
					.forEach(armor -> armor.setIntact(true));
			}
			
			if (getGameObject().hasThisAttribute("wish")) {
				Wish wish = new Wish(parent);
				DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,character,getRedDieLock()).createRoller(wish);
				roller.rollDice("Wish");
				String result = wish.apply(character,roller);
				RealmLogging.logMessage(caster.getName(),"Wish roll: "+roller.getDescription());
				RealmLogging.logMessage(caster.getName(),"Wish result: "+result);
				// Force a combat frame redraw somehow...
			}
			if (getGameObject().hasThisAttribute("curse")) {
				Curse curse = new Curse(parent);
				DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,character,getRedDieLock()).createRoller(curse);
				roller.rollDice("Curse");
				String result = curse.apply(character,roller);
				RealmLogging.logMessage(caster.getName(),"Curse roll: "+roller.getDescription());
				RealmLogging.logMessage(caster.getName(),"Curse result: "+result);
			}
			if (getGameObject().hasThisAttribute("spawn")) {
				String spawn = getGameObject().getThisAttribute("spawn");
				if ("phantasm".equals(spawn)) {
					GameObject phantasm = getGameObject().getGameData().createNewObject();
					phantasm.setName(character.getGameObject().getName()+"'s Phantasm");
					phantasm.setThisAttribute("phantasm");
					phantasm.setThisAttribute(Constants.ICON_TYPE,"phantasm");
					phantasm.setThisAttribute(Constants.ICON_FOLDER,"characters");
					phantasm.addThisAttributeListItem(Constants.SPECIAL_ACTION,"ENHANCED_PEER");
					character.getCurrentLocation().clearing.add(phantasm,null);
					CharacterWrapper charPhantasm = new CharacterWrapper(phantasm);
					charPhantasm.setPlayerName(character.getPlayerName());
					charPhantasm.setPlayerPassword(character.getPlayerPassword());
					charPhantasm.setPlayerEmail(character.getPlayerEmail());
					character.addMinion(phantasm);
					RealmComponent rc = RealmComponent.getRealmComponent(phantasm);
					rc.setOwner(RealmComponent.getRealmComponent(character.getGameObject()));
				}
			}

			SpellUtility.ApplyNamedSpellEffectToCharacter(Constants.CHOOSE_TURN, character, this);
			SpellUtility.ApplyNamedSpellEffectToCharacter(Constants.VALE_WALKER, character, this);
			SpellUtility.ApplyNamedSpellEffectToCharacter(Constants.TORCH_BEARER, character, this);
			
			if (getGameObject().hasThisAttribute(Constants.INSTANT_PEER)) {
				character.setDoInstantPeer(true);
			}
			if (getGameObject().hasThisAttribute(Constants.SUMMONING)) {
				SpellUtility.summonCompanions(parent,caster,character,this,getGameObject().getThisAttribute(Constants.SUMMONING));
			}
			if (getGameObject().hasThisAttribute(Constants.TELEPORT)) {
				String teleportType = getGameObject().getThisAttribute(Constants.TELEPORT);
				SpellUtility.doTeleport(parent,getGameObject().getName(),character,TeleportType.valueOf(teleportType));
			}
			
			if (getGameObject().hasThisAttribute(Constants.DISCOVER_ROAD)) {
				character
					.getCurrentClearing()
					.getConnectedPaths()
					.forEach(path -> character.updatePathKnowledge(path));
			}
		}
		if (getGameObject().hasThisAttribute("spell_extra_action")) {
			String extra = getGameObject().getThisAttribute("spell_extra_action");
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			character.addSpellExtraAction(extra,getGameObject());
		}
		
		
		
		if (getGameObject().hasThisAttribute(Constants.SP_PEACE)) {
			boolean attacked = false;
			ArrayList<GameObject> attackers = combat.getAttackers();
			for (GameObject go:attackers) {
				if (!go.equals(caster)) {
					attacked = true;
				}
			}
			if (attacked) {
				RealmLogging.logMessage(
						caster.getName(),
						getGameObject().getName()+" was cancelled because the "
						+target.getGameObject().getName()
						+" is being attacked by someone other than the "+caster.getName()+"!");
			}
			else {
				combat.setPeace(true);
				target.clearTarget();
				if (target.isCharacter()) {
					// Cancel any cast spells
					GameObject go = combat.getCastSpell();
					if (go!=null) {
						SpellWrapper spell = new SpellWrapper(go);
						spell.expireSpell();
						RealmLogging.logMessage(
								spell.getCaster().getGameObject().getName(),
								spell.getGameObject().getName()+" was cancelled because of PEACE spell!");
					}
				}
			}
		}
		if (getGameObject().hasThisAttribute(Constants.BLOWS_TARGET)) {
			target.getGameObject().setThisAttribute(Constants.BLOWS_TARGET,getGameObject().getStringId());
		}
		if (getGameObject().hasThisAttribute(Constants.ASKDEMON)) {
			// The target (demon) is actually irrelevant here: we use only the extra identifier
			String string = getExtraIdentifier();
			int index = string.indexOf(Constants.DEMON_Q_DELIM);
			String playerName = string.substring(0,index);
			String question = string.substring(index+Constants.DEMON_Q_DELIM.length());
			theGame.addQuestion(getCaster().getPlayerName(),playerName,question);
		}
		if (getGameObject().hasThisAttribute("powerofthepit")) {
			int d = getRedDieLock();
			PowerOfThePit.doNow(parent,getCaster().getGameObject(),target.getGameObject(),true,d);
		}
		if (getGameObject().hasThisAttribute("nullify")) {
			if (target.isCharacter()) {
				CharacterWrapper targChar = new CharacterWrapper(target.getGameObject());
				targChar.nullifyCurses();
			}
			SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
			sm.nullifyBewitchingSpells(target.getGameObject(),this);
		}
		if (getGameObject().hasThisAttribute("disengage")) {
			// Remove all attackers and targets
			ArrayList<GameObject> attackers = combat.getAttackers();
			
			attackers.stream()
				.map(a -> RealmComponent.getRealmComponent(a))
				.forEach(rc -> rc.clearTarget());
		
			attackers.stream()
				.map(a -> new CombatWrapper(a))
				.filter(a -> a.getAttackerCount() > 0)
				.forEach(a -> a.setSheetOwner(true));
			
			combat.removeAllAttackers();
		}
		if (getGameObject().hasThisAttribute(Constants.UNASSIGN)) {
			target.clearTarget();
			CombatWrapper aCombat = new CombatWrapper(target.getGameObject());
			aCombat.setSheetOwner(false);
		}
		if (getGameObject().hasThisAttribute("exorcise")) {
			if (target.getGameObject().hasThisAttribute("demon")) {
				combat.setKilledBy(caster);
			}
			else if (target.isCharacter()) {
				CharacterWrapper targChar = new CharacterWrapper(target.getGameObject());
				
				// Cancel Spellcasting (do NOT include this spell!!)
				GameObject castSpell = combat.getCastSpell();
				if (castSpell!=null && !castSpell.equals(getGameObject())) {
					SpellWrapper otherSpell = new SpellWrapper(castSpell);
					otherSpell.expireSpell();
				}
				
				// Cancel curses
				targChar.removeAllCurses();
				
				// Fatigue Color Chits
				for (Iterator i=targChar.getColorChits().iterator();i.hasNext();) {
					CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
					chit.makeFatigued();
				}
			}
			else if (target.isSpell()) {
				SpellWrapper otherSpell = new SpellWrapper(target.getGameObject());
				otherSpell.expireSpell();
			}
			else {
				System.out.println("Invalid target?");
			}
		}
		if (getGameObject().hasThisAttribute("cancel")) {
			if (target.isCharacter()) {
				String curse = getExtraIdentifier();
				CharacterWrapper targChar = new CharacterWrapper(target.getGameObject());
				targChar.removeCurse(curse);
			}
			else {
				// Target is a spell
				SpellWrapper spell = new SpellWrapper(target.getGameObject());
				spell.expireSpell();
			}
		}
		if (getGameObject().hasThisAttribute("move_sound")) {
			// Target is a sound chit, secondary target is the tile to move to
			GameObject soundChit = target.getGameObject();
			GameObject targetTile = getSecondaryTarget();
			targetTile.add(soundChit);
		}
		if (getGameObject().hasThisAttribute(Constants.MAGIC_CHANGE)) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			String change = getGameObject().getThisAttribute(chit.getMagicType());
			if (change==null) {
				throw new IllegalStateException("Undefined chit_change!");
			}
			chit.getGameObject().setThisAttribute(Constants.MAGIC_CHANGE,change);
		}
		if (getGameObject().hasThisAttribute(Constants.FORCED_ENCHANTMENT)) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			String change = getGameObject().getThisAttribute(chit.getMagicType());
			if (change==null) {
				throw new IllegalStateException("Undefined chit_change!");
			}
			chit.enchant(ColorMagic.makeColorMagic(change,true).getColorNumber());
		}
		if (getGameObject().hasThisAttribute("action_change")) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			String speedKey = "s"+chit.getGameObject().getThisAttribute("speed"); // use raw speed - ignores speed changes by treasures
			String strength = getGameObject().getThisAttribute(speedKey);
			if (strength==null) {
				throw new IllegalStateException("Undefined action_change_str!");
			}
			chit.getGameObject().setThisAttribute("action_change","M/F");
			chit.getGameObject().setThisAttribute("action_change_str",strength);
			getCaster().updateChitEffects();
		}
		
		if (getGameObject().hasThisAttribute("move_speed_change")) {
			if (target.isCharacter()) {
				CharacterWrapper character = new CharacterWrapper(target.getGameObject());
				
				character.getAllChits().stream()
					.filter(chit -> "MOVE".equals(chit.getAction()))
					.forEach(chit -> SpellUtility.setAlteredSpeed(chit, "strength", this));
			}
			else if (target.isMonster() || target.isNative()) {
				SpellUtility.setAlteredSpeed(target, "vulnerability", this);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.ANIMATE)) {
			GameObject go = target.getGameObject();
			go.setName(Constants.UNDEAD_PREFIX+go.getName());
			go.removeThisAttribute(Constants.DEAD);
			go.setThisAttribute(Constants.UNDEAD);
			go.copyAttributeBlock("light","light_an");
			go.copyAttributeBlock("dark","dark_an");
			go.setAttribute("light","chit_color","white");
			if (go.hasAttribute("light","attack_speed")) {
				go.setAttribute("light","attack_speed",go.getAttributeInt("light","attack_speed")+1);
			}
			go.setAttribute("light","move_speed",go.getAttributeInt("light","move_speed")+1);
			go.setAttribute("dark","chit_color","gray");
			if (go.hasAttribute("dark","attack_speed")) {
				go.setAttribute("dark","attack_speed",go.getAttributeInt("dark","attack_speed")+1);
			}
			go.setAttribute("dark","move_speed",go.getAttributeInt("dark","move_speed")+1);
			
			if (go.getHoldCount()==1) {
				GameObject weapon = (GameObject)go.getHold().get(0);
				weapon.setAttribute("light","attack_speed",weapon.getAttributeInt("light","attack_speed")+1);
				weapon.setAttribute("dark","attack_speed",weapon.getAttributeInt("dark","attack_speed")+1);
			}
			
			CharacterWrapper casterChar = new CharacterWrapper(caster);
			casterChar.addHireling(go);
			caster.add(go); // so that you don't have to assign as a follower right away
			CombatWrapper monster = new CombatWrapper(go);
			monster.setSheetOwner(true);
		}
		if (getGameObject().hasThisAttribute(Constants.CHANGE_TO_COMPANION)) {
			MonsterCreator monsterCreator = new MonsterCreator(Constants.CHANGE_TO_COMPANION);
			GameObject companion = monsterCreator.createOrReuseMonster(getGameObject().getGameData());
			monsterCreator.setupGameObject(
					companion,
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"name"),
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"icon_type"),
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"vulnerability"),
					getGameObject().hasAttribute(Constants.CHANGE_TO_COMPANION,"armored"),
					getGameObject().hasAttribute(Constants.CHANGE_TO_COMPANION,"flying"));
			companion.setThisAttribute("icon_folder",getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"icon_folder"));
			monsterCreator.setupSide(
					companion,
					"light",
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"strength"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"sharpness"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"attack_speed"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"length"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"move_speed"),
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"chit_color"));
			monsterCreator.setupSide(
					companion,
					"dark",
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"strength"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"sharpness"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"attack_speed"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"length"),
					getGameObject().getAttributeInt(Constants.CHANGE_TO_COMPANION,"move_speed"),
					getGameObject().getAttribute(Constants.CHANGE_TO_COMPANION,"chit_color"));
					
			CharacterWrapper casterChar = new CharacterWrapper(caster);
			casterChar.addHireling(companion);
			caster.add(companion); // so that you don't have to assign as a follower right away
			CombatWrapper monster = new CombatWrapper(companion);
			monster.setSheetOwner(true);
			
			ArrayList<String> list = getGameObject().getThisAttributeList("created");
			if (list==null) {
				list = new ArrayList<String>();
			}
			for(GameObject go:monsterCreator.getMonstersCreated()) {
				list.add(go.getStringId());
			}
			getGameObject().setThisAttributeList("created",list);
			
			// Finally
			getGameObject().add(target.getGameObject()); // move target into spell (since it is being converted)
		}
		if (getGameObject().hasThisAttribute("transmorph")) {
			String transmorph = getGameObject().getThisAttribute("transmorph");
			
			/*
			 * Possibilities:
			 * 	mist - Melt into Mist
			 * 	target - Absorb Essence
			 * 	roll - Transform (need to roll)
			 * 	roll# - Transform already rolled
			 */
			if ("target".equals(transmorph)) {
				if (target.isMonster()) {
					MonsterChitComponent monster = (MonsterChitComponent)target;
					if (monster.isDarkSideUp()) { // Always flip to light side on absorb!
						monster.setLightSideUp();
					}
				}
				target.clearOwner();
				RealmComponent targetsTarget = target.getTarget();
				if (targetsTarget!=null) {
					// Make sure that the target isn't already an attacker somewhere else
					CombatWrapper ttc = new CombatWrapper(targetsTarget.getGameObject());
					ttc.removeAttacker(target.getGameObject());
					
					if (targetsTarget.targeting(target)) {
						// Only clear targetsTarget if actually targeting the target (that's not confusing at all)
						targetsTarget.clearTarget();
					}
					
					// Clear its target
					target.clearTarget();
				}
				if (!getGameObject().getHold().contains(target.getGameObject())) {
					getGameObject().add(target.getGameObject());
					target.getGameObject().removeThisAttribute("clearing");
					combat.removeAllAttackers();
					RealmLogging.logMessage(getCaster().getGameObject().getName(),"Absorbed the "+target.getGameObject().getName());
				}
				else {
					RealmLogging.logMessage(getCaster().getGameObject().getName(),"Turns into the "+target.getGameObject().getName());
				}
				// Record which belongings are active, before inactivating them
				ArrayList<GameObject> inactivated = getCaster().inactivateAllBelongings();
				for (GameObject go:inactivated) {
					addListItem(Constants.ACTIVATED_ITEMS,go.getStringId());
				}
				getCaster().setTransmorph(target.getGameObject());
			}
			else if ("roll".equals(transmorph) || "mist".equals(transmorph)) {
				GameObject transformAnimal = getTransformAnimal();
				
				// In this case, the target is the one that gets transformed
				if (transformAnimal==null) {
					String transformBlock;
					DieRoller roller = null;
					if ("roll".equals(transmorph)) {
						// hasn't rolled yet
						roller = DieRollBuilder.getDieRollBuilder(parent,getCaster(),getRedDieLock()).createRoller("transform");
						int die = roller.getHighDieResult();
						int mod = getGameObject().getThisInt(Constants.SPELL_MOD);
						die += mod;
						if (die<1) die=1;
						if (die>6) die=6;
						transformBlock = "roll"+die;
						
						RealmLogging.logMessage(getCaster().getGameObject().getName(),"Transform roll: "+roller.getDescription());
					}
					else {
						transformBlock = "mist";
					}
					
					// Create a transformAnimal GameObject, so that this can be stored with the spell
					GameData data = getGameObject().getGameData();
					transformAnimal = data.createNewObject();
					
					// Copy attributes from specific block to new GameObject
					copyTransformToObject(getGameObject(),transformBlock,transformAnimal);
					
					// Add the transformAnimal to the spell
					getGameObject().add(transformAnimal);
					
					// Fix the pronoun
					String pronoun = "a ";
					if (transformAnimal.getName().startsWith("E")) {
						pronoun = "an ";
					}
					else if (transformAnimal.getName().equals("Mist")) {
						pronoun = "";
					}
					
					// Report the transform effect
					IconGroup group = new IconGroup(RealmComponent.getRealmComponent(transformAnimal).getIcon(),IconGroup.VERTICAL,1);
					if (roller!=null) {
						group.addIcon(roller.getIcon());
					}
					String message = "The "+target.getGameObject().getName()
							+" was transformed into "+pronoun+transformAnimal.getName()+".";
					
					RealmLogging.logMessage(RealmLogging.BATTLE,message);
					FrameManager.showDefaultManagedFrame(parent,message,"Transform",group,true);
				}
				
				// Do the actual transform
				if (target.isCharacter()) {
					CharacterWrapper character = new CharacterWrapper(target.getGameObject());
					character.setTransmorph(transformAnimal);
				}
				else {
					if (target.getGameObject().hasAttributeBlock("this_h")) { // I think this happens when the target is stuck in an enchanted tile
//						unaffectTargets(); // This does NOT!! work!
						return; // This seems VERY wrong to me, but it works...
					}
					
					// rename this to this_hidden, light to light_hidden, and dark to dark_hidden
					target.getGameObject().renameAttributeBlock("this","this_h");
					target.getGameObject().renameAttributeBlock("light","light_h");
					target.getGameObject().renameAttributeBlock("dark","dark_h");
					
					// Copy this,light,dark from transformAnimal
					target.getGameObject().copyAttributeBlockFrom(transformAnimal,"this");
					target.getGameObject().copyAttributeBlockFrom(transformAnimal,"light");
					target.getGameObject().copyAttributeBlockFrom(transformAnimal,"dark");
					if (target.getGameObject().hasAttribute("this_h","clearing")) {
						target.getGameObject().setThisAttribute("clearing",target.getGameObject().getAttribute("this_h","clearing"));
					}
					if (target.getGameObject().hasAttribute("this_h","monster_die")) {
						target.getGameObject().setThisAttribute("monster_die",target.getGameObject().getAttribute("this_h","monster_die"));
					}
					if (target.getGameObject().hasAttribute("this_h","base_price")) {
						target.getGameObject().setThisAttribute("base_price",target.getGameObject().getAttribute("this_h","base_price"));
					}
					if (target.getGameObject().hasAttribute("this_h","fame")) {
						target.getGameObject().setThisAttribute("fame",target.getGameObject().getAttribute("this_h","fame"));
					}
					if (target.getGameObject().hasAttribute("this_h","notoriety")) {
						target.getGameObject().setThisAttribute("notoriety",target.getGameObject().getAttribute("this_h","notoriety"));
					}
					if (target.getGameObject().hasAttribute("this_h","native")) {
						target.getGameObject().setThisAttribute("native",target.getGameObject().getAttribute("this_h","native"));
					}
				}
				if (target.isPlayerControlledLeader()) {
					CharacterWrapper character = new CharacterWrapper(target.getGameObject());
					
					// Transmorph gold
					double gold = character.getGold();
					if (gold>0) {
						setDouble("transmorphed_gold",gold);
						character.setGold(0.0);
					}
					
					// Move all inventory to the spell, so it doesn't appear in window anymore,
					// but will on double-click of the spell.  This should also disable inventory
					// without changing its active/inactive location
					ArrayList inv = new ArrayList(character.getInventory());
					for (Iterator i=inv.iterator();i.hasNext();) {
						GameObject item = (GameObject)i.next();
						RealmComponent rc = RealmComponent.getRealmComponent(item);
						if (rc.isItem()) {
							getGameObject().add(item);
						}
					} 
				}
				if (target.isMistLike()) {
					// Mists cannot have a target!
					target.clearTarget();
				}
			}
			
			if (getCaster().isTransmorphed()) {
				// Cancel combat spell, if any, and then only if the cast spell is NOT THIS one!
				CombatWrapper casterCombat = new CombatWrapper(caster);
				GameObject cast = casterCombat.getCastSpell();
				if (cast!=null && !cast.equals(getGameObject())) {
					casterCombat.clearCastSpell();
				}
			}
		}
		if (getGameObject().hasThisAttribute("control")) {
			// Make sure none of the caster's hirelings are attacking the monster/native or this spell is cancelled for that monsters
			ArrayList<GameObject> attackers = combat.getAttackers();
			for (GameObject go:attackers) {
				if (!go.equals(caster)) {
					RealmComponent gorc = RealmComponent.getRealmComponent(go);
					if (gorc.getOwnerId().equals(caster.getStringId())) {
						RealmLogging.logMessage(
								caster.getName(),
								getGameObject().getName()+" was cancelled because the "
								+caster.getName()+"'s hirelings are already attacking the "
								+target.getGameObject().getName());
						
						// Remove target manually
						ArrayList targetids = new ArrayList(getList(TARGET_IDS));
						targetids.remove(target.getGameObject().getStringId());
						if (targetids.isEmpty()) {
							expireSpell();
						}
						else {
							setList(TARGET_IDS,targetids);
						}
						return;
					}
				}
			}
			
			// For now, clear the target, though this isn't totally right (see rule 45.5)
			target.clearTarget();
			if (target.isMonster() || target.isNative()) {
				ChitComponent chit = (ChitComponent)target;
				if (chit.isDarkSideUp()) { // Always flip to light side on control!
					chit.setLightSideUp();
				}
			}
			CharacterWrapper controlledMonster = new CharacterWrapper(target.getGameObject());
			controlledMonster.setPlayerName(getCaster().getPlayerName());
			controlledMonster.setWantsCombat(getCaster().getWantsCombat()); // same default
			target.setOwner(RealmComponent.getRealmComponent(getCaster().getGameObject()));
			combat.setSheetOwner(true);
			combat.setWatchful(false);
//			combat.removeAllAttackers();
		}
		if (getGameObject().hasThisAttribute("enchant")) {
			// Add the secondary target spell and chit type to the artifact
			SpellWrapper spellToAdd = new SpellWrapper(getSecondaryTarget());
			if (!spellToAdd.getGameObject().hasThisAttribute(Constants.ARTIFACT_ENHANCED_MAGIC)) {
				// First time this casting has energized, so make a copy of the requisite spell
				spellToAdd = spellToAdd.makeCopy();
				setSecondaryTarget(spellToAdd.getGameObject());
				spellToAdd.getGameObject().setThisAttribute(Constants.ARTIFACT_ENHANCED_MAGIC);
				spellToAdd.getGameObject().setThisAttribute(Constants.SPELL_AWAKENED);
			}
			target.getGameObject().addThisAttributeListItem(Constants.ARTIFACT_ENHANCED_MAGIC,spellToAdd.getCastMagicType());
			target.getGameObject().add(spellToAdd.getGameObject());
		}
		if (getGameObject().hasThisAttribute("fly_strength")) {
			target.getGameObject().setThisAttribute("fly_strength",getGameObject().getThisAttribute("fly_strength"));
			target.getGameObject().setThisAttribute("fly_speed",getGameObject().getThisAttribute("fly_speed"));
		}
		if (getGameObject().hasThisAttribute(Constants.PACIFY)) {
			// If any of the targets of this spell are being attacked by the caster's hirelings, the spell is cancelled.
			for (Iterator i=combat.getAttackers().iterator();i.hasNext();) {
				GameObject attacker = (GameObject)i.next();
				RealmComponent rc = RealmComponent.getRealmComponent(attacker);
				if (!rc.getGameObject().equals(caster)) {
	 				RealmComponent owner = rc.getOwner();
					if (owner!=null && owner.getGameObject().equals(caster)) {
						// oops!
						RealmLogging.logMessage(RealmLogging.BATTLE,"Oops, one of the targets of "+getGameObject().getName()+" is already being attacked by one of the "+caster.getName()+"'s hirelings!");
						RealmLogging.logMessage(RealmLogging.BATTLE,getGameObject().getName()+" is cancelled.");
						expireSpell();
						return;
					}
				}
			}
			
			String pacifyBlock = Constants.PACIFY+getGameObject().getStringId();
			target.getGameObject().addAttributeListItem("this","pacifyBlocks",pacifyBlock);
			int pacifyType = getGameObject().getThisInt(Constants.PACIFY);
			target.getGameObject().setAttribute(pacifyBlock,"pacifyType",pacifyType);
			target.getGameObject().setAttribute(pacifyBlock,"pacifyChar",getCaster().getGameObject().getStringId());
			// Shouldn't clear target unless the target is the caster!
			RealmComponent targetTarget = target.getTarget();
			if (targetTarget!=null && targetTarget.getGameObject().equals(getCaster().getGameObject())) {
				target.clearTarget();
			}
			
			// If you made them watchful, make them unwatchful again
			combat.setWatchful(false);
			
			if (target.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)target;
				if (monster.isDarkSideUp()) { // Always flip to light side on control!
					monster.setLightSideUp();
				}
			}
		}
		if (getGameObject().hasThisAttribute(Constants.ADD_SHARPNESS)) {
			// Increment sharpness by one
			int val = target.getGameObject().getThisInt(Constants.ADD_SHARPNESS) + 1;
			target.getGameObject().setThisAttribute(Constants.ADD_SHARPNESS,val);
		}
		if (getGameObject().hasThisAttribute(Constants.EXTRA_CAVE_PHASE)) {
			ClearingDetail clearing = getTargetAsClearing(target);
			clearing.addFreeAction(Constants.EXTRA_CAVE_PHASE,getGameObject());
		}
		if (getGameObject().hasThisAttribute(Constants.CLEARING_SPELL_EFFECT)) {
			ClearingDetail clearing = getTargetAsClearing(target);
			clearing.addSpellEffect(getGameObject().getThisAttribute(Constants.CLEARING_SPELL_EFFECT));
		}
		if (getGameObject().hasThisAttribute(Constants.ATTRIBUTE_ADD)) {
			OrderedHashtable atts = getGameObject().getAttributeBlock(Constants.ATTRIBUTE_ADD);
			for (Iterator i=atts.keySet().iterator();i.hasNext();) {
				String key = (String)i.next();
				String val = (String)atts.get(key);
				target.getGameObject().setThisAttribute(key,val);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.SP_NO_PEER)) {
			target.getGameObject().setThisAttribute(Constants.SP_NO_PEER);
		}
		if (getGameObject().hasThisAttribute(Constants.SP_MOVE_IS_RANDOM)) {
			target.getGameObject().setThisAttribute(Constants.SP_MOVE_IS_RANDOM);
		}
		if (getGameObject().hasThisAttribute(Constants.REPAIR_ONE)) {
			ArmorChitComponent armor = (ArmorChitComponent)target;
			armor.setIntact(true);
		}
		if (getGameObject().hasThisAttribute(Constants.HEAL_CHIT)) {
			for(RealmComponent rc:getTargets()) {
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				chit.makeActive();
			}
		}
		if (getGameObject().hasAttribute(Constants.ATTRIBUTE_ADD,Constants.ALERTED_WEAPON)) {
			WeaponChitComponent weapon = (WeaponChitComponent)target;
			if (!weapon.isAlerted()) {
				weapon.setAlerted(true);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.FINAL_CHIT_SPEED)) {
			getCaster().updateChitEffects();
		}
		if (getGameObject().hasThisAttribute(Constants.COLOR_MOD)) {
			ColorMod colorMod = ColorMod.createColorMod(getGameObject());
			if (target.isTile()) {
				target.getGameObject().setThisAttribute(Constants.MOD_COLOR_SOURCE,getGameObject().getThisAttribute(Constants.COLOR_MOD));
			}
			else {
				ColorMagic cm;
				if (target.isActionChit()) {
					cm = ((CharacterActionChitComponent)target).getColorMagic();
				}
				else{
					cm = SpellUtility.getColorMagicFor(target);
				}
				cm = colorMod.convertColor(cm);
				if (cm!=null) {
					target.getGameObject().setThisAttribute(Constants.MOD_COLOR_SOURCE,cm.getColorName().toLowerCase());
				}
			}
		}
		if (getGameObject().hasThisAttribute(Constants.SP_STORMY)) {
			// Need to roll on the Violent Storm table...
			DieRoller roller = DieRollBuilder.getDieRollBuilder(parent,getCaster(),getRedDieLock()).createRoller("ViolentStorm");
			roller.rollDice("Violent Storm");
			int phasesLost;
			int t = roller.getHighDieResult();
			if (t<=1) {
				phasesLost = 4;
			}
			else if (t<=3) {
				phasesLost = 3;
			}
			else if (t<=5) {
				phasesLost = 2;
			}
			else {
				phasesLost = 1;
			}
			RealmLogging.logMessage(caster.getName(),"Violent Storm roll: "+roller.getDescription());
			RealmLogging.logMessage(caster.getName(),"Violent Storm result: "+phasesLost+" phase"+(phasesLost==1?"":"s")+" lost on entry");
			target.getGameObject().setThisAttribute(Constants.SP_STORMY,phasesLost);
		}
		if (isFlySpell()) {
			// A Fly spell.  Create a Fly Chit
			GameObject flyChit = getGameObject().getGameData().createNewObject();
			flyChit.setName(getGameObject().getName()+" Fly Chit ("+caster.getName()+")");
			flyChit.copyAttributeBlockFrom(getGameObject(),RealmComponent.FLY_CHIT);
			flyChit.renameAttributeBlock(RealmComponent.FLY_CHIT,"this");
			flyChit.setThisAttribute("spellID",getGameObject().getStringId());
			flyChit.setThisAttribute("sourceSpell",getGameObject().getName());
			getGameObject().setThisAttribute("flyChitID",flyChit.getStringId());
			target.getGameObject().add(flyChit);
			
			if (!target.getGameObject().equals(caster)) {
				RealmComponent flyChitRC = RealmComponent.getRealmComponent(flyChit);
				flyChitRC.setOwner(RealmComponent.getRealmComponent(caster));
			}
		}
		
		// Once the spell affects its target, the marker chit should be removed!
		if (caster!=null) {
			combat.removeAttacker(caster);
		}
	}
	private ClearingDetail getTargetAsClearing(RealmComponent target) {
		TileComponent tile = (TileComponent)target;
		return tile.getClearing(Integer.valueOf(getExtraIdentifier()).intValue());
	}
	public void unaffectTargets() {
		ArrayList targets = getTargets();
		for (Iterator i=targets.iterator();i.hasNext();) {
			RealmComponent target = (RealmComponent)i.next();
			unaffect(target);
		}
		setBoolean(SPELL_AFFECTED,false);
	}
	private void unaffect(RealmComponent target) {
		if (target.isCharacter()) {
			// Character only effects
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			if (getGameObject().hasThisAttribute("spawn")) {
				String spawn = getGameObject().getThisAttribute("spawn");
				if ("phantasm".equals(spawn)) {
					// Simply remove ALL phantasms - they could only have been the result of previous days casting
					Collection c = character.getMinions();
					if (c!=null) {
						ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
						for (Iterator i=c.iterator();i.hasNext();) {
							GameObject minion = (GameObject)i.next();
							if (minion.hasThisAttribute("phantasm")) {
								toRemove.add(minion);
							}
						}
						for (Iterator<GameObject> i=toRemove.iterator();i.hasNext();) {
							GameObject minion = i.next();
							character.removeMinion(minion);
							ClearingUtility.moveToLocation(minion,null);
						}
					}
				}
			}
			if (getGameObject().hasThisAttribute(Constants.CHOOSE_TURN)) {
				character.getGameObject().removeThisAttribute(Constants.CHOOSE_TURN);
			}
			
			if(getGameObject().hasThisAttribute(Constants.VALE_WALKER)){
				character.getGameObject().removeThisAttribute(Constants.VALE_WALKER);
			}
			
			if(getGameObject().hasThisAttribute(Constants.TORCH_BEARER)){
				character.getGameObject().removeThisAttribute(Constants.TORCH_BEARER);
			}
			
			if (isPhaseSpell()) {
				// A Phase spell.  Ditch the phase chit.
				GameObject phaseChit = getGameObject().getGameData().getGameObject(Long.valueOf(getGameObject().getThisAttribute("phaseChitID")));
				character.getGameObject().remove(phaseChit);
				getGameObject().removeThisAttribute("phaseChitID");
			}
			
			if (getGameObject().hasThisAttribute(Constants.SUMMONING)) {
				SpellUtility.unsummonCompanions(this);
			}
		}
		
		if (getGameObject().hasThisAttribute("spell_extra_action")) {
			String extra = getGameObject().getThisAttribute("spell_extra_action");
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			character.removeSpellExtraAction(extra);
		}
		
		if (getGameObject().hasThisAttribute(Constants.BLOWS_TARGET)) {
			// The spell might expire this way, if the target is killed before being blown away
			target.getGameObject().removeThisAttribute(Constants.BLOWS_TARGET);
		}
		if (getGameObject().hasThisAttribute(Constants.MAGIC_CHANGE)) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			if (chit.isColor()) {
				// If the converted chit was enchanted, it fatigues at the end of the spell (Rule 43.5)
				chit.makeFatigued();
			}
			// BUG 1554 - If committed to a spell, spells need to end here
			SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
			sm.expireIncantationSpell(chit.getGameObject());
			target.getGameObject().removeThisAttribute(Constants.MAGIC_CHANGE);
		}
		if (getGameObject().hasThisAttribute(Constants.FORCED_ENCHANTMENT)) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			if (chit.isColor()) {
				// Not sure this is what Deric Page wanted, but it's consistent
				chit.makeFatigued();
			}
		}
		if (getGameObject().hasThisAttribute("action_change")) {
			target.getGameObject().removeThisAttribute("action_change");
			target.getGameObject().removeThisAttribute("action_change_str");
		}
		if (getGameObject().hasThisAttribute("move_speed_change")) {
			if (target.isCharacter()) {
				CharacterWrapper character = new CharacterWrapper(target.getGameObject());
				for (Iterator i=character.getAllChits().iterator();i.hasNext();) {
					CharacterActionChitComponent chit = (CharacterActionChitComponent)i.next();
					if ("MOVE".equals(chit.getAction())) { // only affects "true" MOVE chits (not DUCK or M/F)
						chit.getGameObject().removeThisAttribute("move_speed_change");
					}
				}
			}
			else if (target.isMonster() || target.isNative()) {
				target.getGameObject().removeThisAttribute("move_speed_change");
			}
		}
		if (getGameObject().hasThisAttribute(Constants.ANIMATE)) {
			GameObject caster = getCaster().getGameObject();
			GameObject go = target.getGameObject();
			if (go.hasThisAttribute(Constants.UNDEAD)) { // so it doesn't get stuck in an infinite loop!
				go.setName(go.getName().substring(Constants.UNDEAD_PREFIX.length()));
				go.removeThisAttribute(Constants.UNDEAD);
				go.copyAttributeBlock("light_an","light");
				go.copyAttributeBlock("dark_an","dark");
				go.removeAttributeBlock("light_an");
				go.removeAttributeBlock("dark_an");
				if (go.getHoldCount()==1) {
					GameObject weapon = (GameObject)go.getHold().get(0);
					weapon.setAttribute("light","attack_speed",weapon.getAttributeInt("light","attack_speed")-1);
					weapon.setAttribute("dark","attack_speed",weapon.getAttributeInt("dark","attack_speed")-1);
				}
				
				CharacterWrapper casterChar = new CharacterWrapper(caster);
				casterChar.removeHireling(go);
				
				RealmUtility.makeDead(target);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.CHANGE_TO_COMPANION)) {
			boolean companionDied = false;
			for (GameObject go:SpellUtility.getCreatedCompanions(this)) {
				if (go.hasThisAttribute(Constants.DEAD)) {
					companionDied = true;
					break;
				}
			}
			SpellUtility.unsummonCompanions(this);
			
			GameObject caster = getCaster().getGameObject();
			if (companionDied) {
				// Target is destroyed
				RealmLogging.logMessage(caster.getName(),"Lost the "+target.getGameObject().getName()+".");
				String targetForItem = getGameObject().getThisAttribute(Constants.CHANGE_TO_COMPANION);
				GameObject dwelling = getGameObject().getGameData().getGameObjectByName(targetForItem);
				dwelling.add(target.getGameObject());
			}
			else {
				// Add target back to character
				caster.add(target.getGameObject());
			}
		}
		if (getGameObject().hasThisAttribute("transmorph")) {
			String transmorph = getGameObject().getThisAttribute("transmorph");
			/*
			 * Possibilities:
			 * 	mist - Melt into Mist
			 * 	target - Absorb Essence
			 * 	roll - Transform (need to roll)
			 */
			if ("target".equals(transmorph)) {
				getCaster().setTransmorph(null);
				
				ArrayList<GameObject> inv = getCaster().getInventory();
				
				// Restore active state of items
				GameData data = getGameObject().getGameData();
				ArrayList list = getList(Constants.ACTIVATED_ITEMS);
				if (list!=null) {
					for (Iterator i=list.iterator();i.hasNext();) {
						String id = (String)i.next();
						GameObject go = data.getGameObject(Long.valueOf(id));
						if (go!=null && inv.contains(go)) { // only do this if the item still exists in inventory
							TreasureUtility.doActivate(null,getCaster(),go,new ChangeListener() {
								public void stateChanged(ChangeEvent ev) {
									// does nothing - is that okay here?
								}
							},false);
						}
					}
				
					// Clear the list
					setBoolean(Constants.ACTIVATED_ITEMS,false);
				}
			}
			else if ("roll".equals(transmorph) || "mist".equals(transmorph)) {
				if (target.isCharacter()) {
					CharacterWrapper character = new CharacterWrapper(target.getGameObject());
					character.setTransmorph(null);
				}
				else {
					if (!target.getGameObject().hasAttributeBlock("this_h")) {
						// This is not good!!  Stop here....
						// This happens when casting Pentangle on a Transformed native - putting a return here fixes the problem.
						return;
					}
					// Copy any SPOILS tags (otherwise they get lost!)
					for (Iterator i=target.getGameObject().getThisAttributeBlock().keySet().iterator();i.hasNext();) {
						String key = (String)i.next();
						if (key.startsWith(Constants.SPOILS_)) {
							target.getGameObject().setAttribute("this_h", key);
						}
					}
					
					// preserve the clearing, so that the monster doesn't return to site of transmorph!!!
					int clearing = target.getGameObject().getThisInt("clearing");
					
					// drop this,light,dark
					target.getGameObject().removeAttributeBlock("this");
					target.getGameObject().removeAttributeBlock("light");
					target.getGameObject().removeAttributeBlock("dark");
					
					// rename
					target.getGameObject().renameAttributeBlock("this_h","this");
					target.getGameObject().renameAttributeBlock("light_h","light");
					target.getGameObject().renameAttributeBlock("dark_h","dark");
					
					target.getGameObject().setThisAttribute("clearing",clearing);
				}
				if (target.isPlayerControlledLeader()) {
					CharacterWrapper character = new CharacterWrapper(target.getGameObject());
					
					// Untransmorph gold
					double gold = getDouble("transmorphed_gold");
					if (gold>0) {
						setBoolean("transmorphed_gold",false);
						character.addGold(gold); // add gold, in case transmorphed character picked up some gold!
					}
					
					// Untransmorph inventory
					ArrayList inv = new ArrayList(getGameObject().getHold());
					for (Iterator i=inv.iterator();i.hasNext();) {
						GameObject item = (GameObject)i.next();
						RealmComponent rc = RealmComponent.getRealmComponent(item);
						if (rc.isItem()) {
							character.getGameObject().add(item);
						}
					}
				}
			}
		}
		if (getGameObject().hasThisAttribute("control")) {
			getCaster().removeHireling(target.getGameObject()); // this does all the work we need!
		}
		if (getGameObject().hasThisAttribute("enchant")) {
			// Remove the secondary target spell and chit type from the artifact
			SpellWrapper spellToAdd = new SpellWrapper(getSecondaryTarget());
			target.getGameObject().removeThisAttributeListItem(Constants.ARTIFACT_ENHANCED_MAGIC,spellToAdd.getCastMagicType());
			target.getGameObject().remove(spellToAdd.getGameObject());
			if (target.isEnchanted() && target.isTreasure()) {
				TreasureCardComponent card = (TreasureCardComponent)target;
				card.makeFatigued();
			}
		}
		if (getGameObject().hasThisAttribute("fly_strength")) {
			target.getGameObject().removeThisAttribute("fly_strength");
			target.getGameObject().removeThisAttribute("fly_speed");
		}
		if (getGameObject().hasThisAttribute(Constants.PACIFY)) {
			String pacifyBlock = Constants.PACIFY+getGameObject().getStringId();
			ArrayList inlist = target.getGameObject().getAttributeList("this","pacifyBlocks");
			if (inlist!=null) { // might be null if the spell was cancelled partway through
				ArrayList<String> list = new ArrayList<String>(inlist);
				list.remove(pacifyBlock);
				if (list.isEmpty()) {
					target.getGameObject().removeThisAttribute("pacifyBlocks");
				}
				else {
					target.getGameObject().setThisAttributeList("pacifyBlocks",list);
				}
			}
			target.getGameObject().removeAttributeBlock(pacifyBlock);
		}
		if (getGameObject().hasThisAttribute(Constants.ADD_SHARPNESS)) {
			// Decrement sharpness by one
			int val = target.getGameObject().getThisInt(Constants.ADD_SHARPNESS) - 1;
			if (val==0) {
				target.getGameObject().removeThisAttribute(Constants.ADD_SHARPNESS);
			}
			else {
				target.getGameObject().setThisAttribute(Constants.ADD_SHARPNESS,val);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.EXTRA_CAVE_PHASE)) {
			// This is a special case where the target is a TileComponent, but we want to reference the clearing
			ClearingDetail clearing = getTargetAsClearing(target);
			clearing.removeFreeAction(Constants.EXTRA_CAVE_PHASE);
		}
		if (getGameObject().hasThisAttribute(Constants.CLEARING_SPELL_EFFECT)) {
			ClearingDetail clearing = getTargetAsClearing(target);
			clearing.removeSpellEffect(getGameObject().getThisAttribute(Constants.CLEARING_SPELL_EFFECT));
		}
		if (getGameObject().hasThisAttribute(Constants.ATTRIBUTE_ADD)) {
			OrderedHashtable atts = getGameObject().getAttributeBlock(Constants.ATTRIBUTE_ADD);
			for (Iterator i=atts.keySet().iterator();i.hasNext();) {
				String key = (String)i.next();
				target.getGameObject().removeThisAttribute(key);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.COLOR_MOD)) {
			target.getGameObject().removeThisAttribute(Constants.MOD_COLOR_SOURCE);
		}
		if (getGameObject().hasThisAttribute(Constants.SP_NO_PEER)) {
			target.getGameObject().removeThisAttribute(Constants.SP_NO_PEER);
		}
		if (getGameObject().hasThisAttribute(Constants.SP_MOVE_IS_RANDOM)) {
			target.getGameObject().removeThisAttribute(Constants.SP_MOVE_IS_RANDOM);
		}
		if (getGameObject().hasThisAttribute(Constants.NO_WEIGHT)) {
			// Make sure character inventory can handle the new weight that results from losing the NO_WEIGHT effect
			GameObject heldBy = target.getGameObject().getHeldBy();
			if (heldBy.hasThisAttribute("character")) { // possible to have anything else?... yes a leader or controlled monster...
				CharacterWrapper character = new CharacterWrapper(heldBy);
				character.setNeedsInventoryCheck(true);
			}
		}
		if (getGameObject().hasThisAttribute(Constants.SP_STORMY)) {
			target.getGameObject().removeThisAttribute(Constants.SP_STORMY);
		}
		if (getGameObject().hasThisAttribute("nullify")) {
			if (target.isCharacter()) {
				CharacterWrapper targChar = new CharacterWrapper(target.getGameObject());
				targChar.restoreCurses();
			}
			SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
			sm.restoreBewitchingNullifiedSpells(target.getGameObject(),this);
		}
		if (isFlySpell()) {
			// A Fly spell.  Destroy the FLY Chit, and remove it from the target.
			String chitId = getGameObject().getThisAttribute("flyChitID");
			GameObject flyChit = getGameObject().getGameData().getGameObject(Long.valueOf(chitId));
			target.getGameObject().remove(flyChit);
			getGameObject().removeThisAttribute("flyChitID");
		}
	}
	public boolean affectsCaster() {
		return getGameObject().hasThisAttribute(Constants.AFFECTS_CASTER);
	}
	/**
	 * All spells except for "Absorb Essence" affect the spell target.  With the Aborb Essence spell, the target is absorbed, but
	 * the caster is affected!
	 */
	public RealmComponent getAffectedTarget() {
		if (affectsCaster()) {
			return RealmComponent.getRealmComponent(getCaster().getGameObject());
		}
		return getFirstTarget(); // Not real fond of this, but it will work in all cases where it matters
	}
	public GameObject getTransformAnimal() {
		for (Iterator i=getGameObject().getHold().iterator();i.hasNext();) {
			GameObject test = (GameObject)i.next();
			if (test.hasThisAttribute("animal")) {
				return test;
			}
		}
		return null;
	}
	public boolean isImmuneTo(RealmComponent rc) {
		// This is meaningless in this context, so just return false.
		return false;
	}
	private boolean isUsingEnhancedMagic() {
		HostPrefWrapper hostPrefs = HostPrefWrapper.findHostPrefs(getGameObject().getGameData());
		return hostPrefs.hasPref(Constants.OPT_ENHANCED_MAGIC) || hostPrefs.hasPref(Constants.HOUSE2_REVISED_ENHANCED_MAGIC);
	}
//	public void testVirtual() {
//		setBoolean(SPELL_VIRTUAL,true);
//	}
	public static void copyTransformToObject(GameObject source,String blockName,GameObject dest) {
		String animalName = source.getAttribute(blockName,"name");
		dest.setName(animalName);
		dest.setThisAttribute("animal");
					
		// Ignore these attributes
		String[] ignorVars = {"light_color","dark_color"};
		ArrayList<String> ignoreTest = new ArrayList<String>(Arrays.asList(ignorVars));
		
		// Earmark some attributes for the "this" block
		String[] thisVars = {"vulnerability",Constants.ICON_FOLDER,Constants.ICON_TYPE,"flying","walk_woods","armored","name","mist_like"};
		ArrayList<String> thisTest = new ArrayList<String>(Arrays.asList(thisVars));
		Hashtable hash = source.getAttributeBlock(blockName);
		for (Iterator i=hash.keySet().iterator();i.hasNext();) {
			String key = (String)i.next();
			if (!ignoreTest.contains(key)) {
				String val = (String)hash.get(key);
				if (thisTest.contains(key)) {
					dest.setThisAttribute(key,val);
				}
				else {
					// same attributes on either side
					dest.setAttribute("light",key,val);
					dest.setAttribute("dark",key,val);
				}
			}
		}
		// Set the colors separately
		dest.setAttribute("light","chit_color",source.getAttribute(blockName,"light_color"));
		dest.setAttribute("dark","chit_color",source.getAttribute(blockName,"dark_color"));
	}

}