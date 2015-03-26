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
import com.robin.magic_realm.components.effect.ISpellEffect;
import com.robin.magic_realm.components.effect.SpellEffectContext;
import com.robin.magic_realm.components.effect.SpellEffectFactory;
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
		ArrayList<?> targetids = getList(TARGET_IDS);
		
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
		CombatWrapper combat = new CombatWrapper(target.getGameObject());
		
		SpellEffectContext context = new SpellEffectContext(parent, theGame, target, this, caster);
		ISpellEffect effect = SpellEffectFactory.create(getName().toLowerCase());
		
		if(effect != null){
			effect.apply(context);
			
			//this is here so that the game will still work even if this refactor is half-complete
			if (caster!=null) {combat.removeAttacker(caster);}			
			return;
		} 
		
		if (isPhaseSpell() && !hasPhaseChit()) {
			// A Phase spell.  Create a Phase Chit
			SpellUtility.createPhaseChit(target,getGameObject());
			return;
		}

		if (target.isCharacter()) {
			// "Character only" effects
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			
			
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
		
		if (getGameObject().hasThisAttribute("move_sound")) {
			// Target is a sound chit, secondary target is the tile to move to
			GameObject soundChit = target.getGameObject();
			GameObject targetTile = getSecondaryTarget();
			targetTile.add(soundChit);
		}
		
		if (getGameObject().hasThisAttribute(Constants.FORCED_ENCHANTMENT)) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			String change = getGameObject().getThisAttribute(chit.getMagicType());
			if (change==null) {
				throw new IllegalStateException("Undefined chit_change!");
			}
			chit.enchant(ColorMagic.makeColorMagic(change,true).getColorNumber());
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
	
	public ClearingDetail getTargetAsClearing(RealmComponent target) {
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
		SpellEffectContext context = new SpellEffectContext(null, null, target, this, getCaster().getGameObject());
		ISpellEffect effect = SpellEffectFactory.create(getName().toLowerCase());
		
		if(effect != null){
			effect.unapply(context);
			return;
		}
		
		
		if (target.isCharacter()) {
			// Character only effects
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			
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
		
		if (getGameObject().hasThisAttribute(Constants.FORCED_ENCHANTMENT)) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)target;
			if (chit.isColor()) {
				// Not sure this is what Deric Page wanted, but it's consistent
				chit.makeFatigued();
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

		if (getGameObject().hasThisAttribute(Constants.NO_WEIGHT)) {
			// Make sure character inventory can handle the new weight that results from losing the NO_WEIGHT effect
			GameObject heldBy = target.getGameObject().getHeldBy();
			if (heldBy.hasThisAttribute("character")) { // possible to have anything else?... yes a leader or controlled monster...
				CharacterWrapper character = new CharacterWrapper(heldBy);
				character.setNeedsInventoryCheck(true);
			}
		}


		if (isFlySpell()) {
			// A Fly spell.  Destroy the FLY Chit, and remove it from the target.
			String chitId = getGameObject().getThisAttribute("flyChitID");
			GameObject flyChit = getGameObject().getGameData().getGameObject(Long.valueOf(chitId));
			target.getGameObject().remove(flyChit);
			getGameObject().removeThisAttribute("flyChitID");
		}
	}
	//END OF UNAFFECT
	
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