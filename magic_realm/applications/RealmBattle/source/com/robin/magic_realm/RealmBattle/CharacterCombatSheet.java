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
package com.robin.magic_realm.RealmBattle;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.game.server.GameClient;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.SpellSet;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.TreasureUtility;
import com.robin.magic_realm.components.utility.TreasureUtility.ArmorType;
import com.robin.magic_realm.components.wrapper.*;

public class CharacterCombatSheet extends CombatSheet {

	private static final int POS_OWNER				= 0;
	
	private static final int POS_TARGET			= 1; // non-positioned targets
	private static final int POS_TARGET_BOX1		= 2;
	private static final int POS_TARGET_BOX2		= 3;
	private static final int POS_TARGET_BOX3		= 4;
	
	private static final int POS_MOVE_BOX1			= 5;
	private static final int POS_MOVE_BOX2			= 6;
	private static final int POS_MOVE_BOX3			= 7;
	
	private static final int POS_ATTACK			= 8; // non-positioned attacks
	private static final int POS_ATTACK_BOX1		= 9;
	private static final int POS_ATTACK_BOX2		=10;
	private static final int POS_ATTACK_BOX3		=11;
	
	private static final int POS_ATTACK_WEAPON1		=12;
	private static final int POS_ATTACK_WEAPON2		=13;
	private static final int POS_ATTACK_WEAPON3		=14;
	
	private static final int POS_SHIELD1			=15; // blocks 1
	private static final int POS_SHIELD2			=16; // blocks 2
	private static final int POS_SHIELD3			=17; // blocks 3
	private static final int POS_BREASTPLATE		=18; // blocks 1 and 2
	private static final int POS_HELMET				=19; // blocks 3
	private static final int POS_SUITOFARMOR		=20; // blocks 1-3
	
	private static final int POS_USEDCHITS			=21;
	private static final int POS_CHARGECHITS		=22;
	
	private static final int POS_DEADBOX			=23;
	
	private static final int CHAR_ROW1 = 74;
	private static final int CHAR_ROW2 = 171;
	private static final int CHAR_ROW3 = 268;
	
	private static final int CHAR_COL1 = 92;
	private static final int CHAR_COL2 = 208;
	private static final int CHAR_COL3 = 322;
	
	private static final Point[] CHARACTER_SHEET = {
			new Point(483,663),
			
			new Point(303,CHAR_ROW1),
			new Point(CHAR_COL1,CHAR_ROW1),
			new Point(CHAR_COL2,CHAR_ROW2),
			new Point(CHAR_COL3,CHAR_ROW3),
			
			new Point(CHAR_COL1,690),
			new Point(CHAR_COL2,690),
			new Point(CHAR_COL3,690),
			
			new Point(530,25),
			new Point(429,CHAR_ROW1),
			new Point(429,CHAR_ROW2),
			new Point(429,CHAR_ROW3),
			
			new Point(525,CHAR_ROW1),
			new Point(525,CHAR_ROW2),
			new Point(525,CHAR_ROW3),
			
			new Point(CHAR_COL1,402),
			new Point(CHAR_COL2,402),
			new Point(CHAR_COL3,402),
			new Point(150,507),
			new Point(321,507),
			new Point(206,603),
			
			new Point(494,458), // Used Chits
			new Point(400,700), // Charge Chits
			
			new Point(CHAR_COL1,CHAR_ROW3), // Dead Box
	};
	
	private RealmComponent sheetOwnerShield;
	
	private ArrayList<Rectangle> spellRegions;
	private Hashtable<Rectangle,SpellCardComponent> spellRegionHash;
	
	/**
	 * Testing constructor ONLY!!!
	 */
	private CharacterCombatSheet() {
		super();
	}
	public CharacterCombatSheet(CombatFrame frame,BattleModel model,RealmComponent participant,boolean interactiveFrame) {
		super(frame,model,participant,interactiveFrame);
		spellRegions = new ArrayList<Rectangle>();
		spellRegionHash = new Hashtable<Rectangle,SpellCardComponent>();
		updateLayout();
	}
	
	protected int getDeadBoxIndex() {
		return POS_DEADBOX;
	}
	
	protected ImageIcon getImageIcon() {
		return ImageCache.getIcon("combat/char_melee2");
	}
	
	protected Point[] getPositions() {
		return CHARACTER_SHEET;
	}
	
	protected String[] splitHotSpot(int index) {
		if (combatFrame==null) return horseRiderSplit; // for testing
		if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
			switch(index) {
				case POS_TARGET:
					if (containsHorse(layoutHash.getList(POS_TARGET))) {
						if (getAllBoxListFromLayout(POS_TARGET_BOX1).isEmpty()) {
							return horseRiderSplit;
						}
					}
					break;
				case POS_TARGET_BOX1:
				case POS_TARGET_BOX2:
				case POS_TARGET_BOX3:
					if (containsHorse(layoutHash.getList(POS_TARGET))) return horseRiderSplit;
					break;
			}
		}
		return null;
	}

	/**
	 * Based on the current action, hotspots are initialized
	 */
	protected void updateHotSpots() {
		hotspotHash.clear();
		CombatWrapper tile = new CombatWrapper(getBattleLocation().tile.getGameObject());
		if (tile.isPeaceClearing(getBattleLocation().clearing.getNum()) || !interactiveFrame) {
			// No activities allowed!
			return;
		}
		CombatWrapper combat = new CombatWrapper(combatFrame.getActiveParticipant().getGameObject());
		GameObject go = combat.getCastSpell();
		SpellWrapper spell = go==null?null:new SpellWrapper(go);
		ArrayList attackers = model.getAttackersFor(combatFrame.getActiveParticipant());
		switch(combatFrame.getActionState()) {
			case Constants.COMBAT_LURE:
				if (!sheetOwner.isMistLike()) {
					if (sheetOwner.equals(combatFrame.getActiveParticipant())) {
						if (combatFrame.areDenizensToLure(sheetOwner)) {
							hotspotHash.put(new Integer(POS_TARGET),"Lure");
						}
					}
				}
				break;
			case Constants.COMBAT_ASSIGN:
				if (combatFrame.getActiveParticipant().getTarget()==null && spell==null) {
					if (containsEnemy(combatFrame.getActiveParticipant(),layoutHash.getList(new Integer(POS_TARGET)))) {
						hotspotHash.put(new Integer(POS_TARGET),combatFrame.getActiveParticipant().getGameObject().getName()+" Target");
					}
					
					// Allow character to be targeted by another character
					if (combatFrame.getActiveCharacterIsHere()
							&& !sheetOwner.equals(combatFrame.getActiveParticipant())
							&& combatFrame.canBeSeen(sheetOwner,false)) {
						hotspotHash.put(new Integer(POS_OWNER),combatFrame.getActiveParticipant().getGameObject().getName()+" Target");
					}
				}
				break;
			case Constants.COMBAT_POSITIONING:
				if (sheetOwner.equals(combatFrame.getActiveParticipant())) {
					hotspotHash.put(new Integer(POS_MOVE_BOX1),"Maneuver");
					hotspotHash.put(new Integer(POS_MOVE_BOX2),"Maneuver");
					hotspotHash.put(new Integer(POS_MOVE_BOX3),"Maneuver");
					
					boolean s1 = hasArmor(POS_SHIELD1);
					boolean s2 = hasArmor(POS_SHIELD2);
					boolean s3 = hasArmor(POS_SHIELD3);
					
					if (s2 || s3) {
						hotspotHash.put(new Integer(POS_SHIELD1),"Position Shield");
					}
					if (s1 || s3) {
						hotspotHash.put(new Integer(POS_SHIELD2),"Position Shield");
					}
					if (s1 || s2) {
						hotspotHash.put(new Integer(POS_SHIELD3),"Position Shield");
					}
					
					if (layoutHash.get(new Integer(POS_TARGET_BOX1))!=null
							|| layoutHash.get(new Integer(POS_TARGET_BOX2))!=null
							|| layoutHash.get(new Integer(POS_TARGET_BOX3))!=null) {
						hotspotHash.put(new Integer(POS_TARGET),"Reset");
					}
					else if (layoutHash.get(new Integer(POS_TARGET))!=null) {
						hotspotHash.put(new Integer(POS_TARGET),"Auto-Position");
					}
					
					if (layoutHash.get(new Integer(POS_TARGET))!=null) {
						hotspotHash.put(new Integer(POS_TARGET_BOX1),"Position Target");
						hotspotHash.put(new Integer(POS_TARGET_BOX2),"Position Target");
						hotspotHash.put(new Integer(POS_TARGET_BOX3),"Position Target");
					}
				}
				// Have to have a target to attack!  (Not really:  see rule 22.4/2a)
				ArrayList allSheetParticipants = new ArrayList(sheetParticipants);
				allSheetParticipants.add(sheetOwner);
				RealmComponent target = combatFrame.getActiveParticipant().getTarget();
				boolean sheetHasTarget = (target==null && spell==null && sheetOwner.equals(combatFrame.getActiveParticipant()))
									|| (target!=null && allSheetParticipants.contains(target));
				boolean sheetHasSpellTarget = spell!=null && spell.isAttackSpell() && spell.targetsRealmComponents(allSheetParticipants);
				if (sheetHasTarget || sheetHasSpellTarget) {
					int boxReq = spell==null?0:spell.getGameObject().getThisInt("box_req"); // most spells will be zero
					if (spell==null || boxReq==0 || boxReq==1) {
						hotspotHash.put(new Integer(POS_ATTACK_WEAPON1),"Attack");
					}
					if (spell==null || boxReq==0 || boxReq==2) {
						hotspotHash.put(new Integer(POS_ATTACK_WEAPON2),"Attack");
					}
					if (spell==null || boxReq==0 || boxReq==3) {
						hotspotHash.put(new Integer(POS_ATTACK_WEAPON3),"Attack");
					}
				}
				// Attacking hirelings
				if (containsFriendOrDenizen(
						combatFrame.getActiveParticipant(),
						getAllBoxListFromLayout(POS_ATTACK_BOX1))) {
					hotspotHash.put(new Integer(POS_ATTACK_BOX1),"Position");
					hotspotHash.put(new Integer(POS_ATTACK_BOX2),"Position");
					hotspotHash.put(new Integer(POS_ATTACK_BOX3),"Position");
				}
				break;
			case Constants.COMBAT_TACTICS:
				CharacterWrapper character = combatFrame.getActiveCharacter();
				
				// Check conditions for REPLACE_MOVE (Elusive Cloak)
				if (sheetOwner.equals(combatFrame.getActiveParticipant())) {
					if (character.canReplaceMove(attackers)) {
						// can replace move
						hotspotHash.put(new Integer(POS_MOVE_BOX1),"Replace Move");
						hotspotHash.put(new Integer(POS_MOVE_BOX2),"Replace Move");
						hotspotHash.put(new Integer(POS_MOVE_BOX3),"Replace Move");
					}
				}
				
				// Check conditions for REPLACE_FIGHT (Battle Bracelets)
				RealmComponent aTarget = combatFrame.getActiveParticipant().getTarget();
				if (combatFrame.getActiveParticipant().getTarget()!=null && (sheetParticipants.contains(aTarget) || sheetOwner.equals(aTarget))) {
					if (character.canReplaceFight(aTarget)) {
						// can replace fight
						hotspotHash.put(new Integer(POS_ATTACK_BOX1),"Replace Fight");
						hotspotHash.put(new Integer(POS_ATTACK_BOX2),"Replace Fight");
						hotspotHash.put(new Integer(POS_ATTACK_BOX3),"Replace Fight");
					}
				}
				break;
		}
	}
	protected boolean hasArmor(int index) {
		ArrayList list = layoutHash.getList(new Integer(index));
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				ArmorType armorType = TreasureUtility.getArmorType(rc.getGameObject());
				if (armorType!=ArmorType.None) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * This method will find all the relevant info from the participant, and layout chits accordingly
	 */
	protected void updateLayout() {
		// First, determine if secrecy is needed (attacked by other character)
		boolean needsSecrecy = alwaysSecret;
		if (combatFrame.getActionState()<Constants.COMBAT_TACTICS
				&& !combatFrame.getActiveParticipant().equals(sheetOwner)
				&& isAttackedByCharacter()) {
			needsSecrecy = true;
		}
		
		battleChitsWithRolls.clear();
		layoutHash.clear();
		layoutHash.put(new Integer(POS_OWNER),sheetOwner);
		
		/*
		 * Cycle through all monsters and natives (not characters) that are in the model, that
		 * are targeting the sheetOwner.  (go into target boxes)
		 */
		sheetParticipants = new ArrayList();
		ArrayList exclude = new ArrayList();
		ArrayList all = model.getAllBattleParticipants(true);
//System.out.println("---------------updateLayout for "+sheetOwner.getGameObject().getName());
		for (Iterator i=all.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper rcCombat = new CombatWrapper(rc.getGameObject());
			RealmComponent target = rc.getTarget();
			
			// If targeting the sheetOwner, put them on the sheet in the target boxes
			if (target!=null && target.equals(sheetOwner)) {
//System.out.println(rc.getGameObject().getName()+" is targeting owner");
				if (!rc.isCharacter()) {
					exclude.add(rc);
					if (!addedToDead(rc)) {
						updateBattleChitsWithRolls(rcCombat);
						CombatWrapper combat = new CombatWrapper(rc.getGameObject());
						int box = combat.getCombatBox();
						layoutHash.put(new Integer(POS_TARGET+box),rc);
						sheetParticipants.add(rc);
						if (rc.isMonster()) {
							MonsterChitComponent monster = (MonsterChitComponent)rc;
							RealmComponent weapon = monster.getWeapon();
							if (weapon!=null) {
								updateBattleChitsWithRolls(new CombatWrapper(weapon.getGameObject()));
								combat = new CombatWrapper(weapon.getGameObject());
								box = combat.getCombatBox();
								if (box>0) {
									// only add monster weapon to layout if in a combat box!
									layoutHash.put(new Integer(POS_TARGET+box),weapon);
								}
							}
						}
						else if (rc.isNative()) {
							RealmComponent horse = (RealmComponent)rc.getHorse();
							if (horse!=null) {
								combat = new CombatWrapper(horse.getGameObject());
								box = combat.getCombatBox();
								if (box>0) {
									// only add horse to layout if in a combat box!
									layoutHash.put(new Integer(POS_TARGET+box),horse);
								}
							}
						}
					}
				}
			}
			
			// If character, place move chits and/or armor (if any)
			if (rc.equals(sheetOwner) && rc.isCharacter()) {
				CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
				CharacterChitComponent characterChit = (CharacterChitComponent)rc;
				
				if (!needsSecrecy) { // don't show moves if being attacked by another character
					RealmComponent maneuverChit = characterChit.getManeuverChit(false);
					if (maneuverChit!=null) {
						CombatWrapper combat = new CombatWrapper(maneuverChit.getGameObject());
						if (combat.getPlacedAsMove()) {
							int box = combat.getCombatBox();
							if (maneuverChit.isCharacter()) { // This implies the character is transmorphed (normally, a character move chit is a chit)
								maneuverChit = characterChit.getTransmorphedComponent().getMoveChit();
							}
							layoutHash.put(new Integer(POS_MOVE_BOX1+box-1),maneuverChit);
						}
					}
				}
				
				for (Iterator n=character.getActiveInventory().iterator();n.hasNext();) {
					GameObject go = (GameObject)n.next();
					ArmorType armorType = TreasureUtility.getArmorType(go);
					RealmComponent item = RealmComponent.getRealmComponent(go);
					if (armorType!=ArmorType.None && armorType!=ArmorType.Special) {
						if (armorType==ArmorType.Shield) {
							CombatWrapper combat = new CombatWrapper(item.getGameObject());
							int box = combat.getCombatBox();
							if (box==0) { // default to box 1
								box = 1;
								combat.setCombatBox(box);
							}
							sheetOwnerShield = item;
							if (needsSecrecy) {
								box = 2; // default position for secrecy
							}
							layoutHash.put(new Integer(POS_SHIELD1+box-1),item);
						}
						else if (armorType==ArmorType.Helmet) {
							layoutHash.put(new Integer(POS_HELMET),item);
						}
						else if (armorType==ArmorType.Breastplate) {
							layoutHash.put(new Integer(POS_BREASTPLATE),item);
						}
						else if (armorType==ArmorType.Armor) {
							layoutHash.put(new Integer(POS_SUITOFARMOR),item);
						}
					}
					else if (item.isTreasure() && item.getGameObject().hasThisAttribute("armor_box")) {
						/*
						 * "armor_box" describes which helmet box
						 * "vulnerability" describes how tough
						 */
						int box = item.getGameObject().getThisInt("armor_box");
						layoutHash.put(new Integer(POS_SHIELD1+box-1),item);
					}
					else if (item.isTreasure() && item.getGameObject().hasThisAttribute("armor_row")) {
						int row = item.getGameObject().getThisInt("armor_row");
						if (row==3) { // This covers the Ointment of Steel
							layoutHash.put(new Integer(POS_SUITOFARMOR),item);
						}
					}
					else if (!needsSecrecy) {
						// Anything with a combat box!
						CombatWrapper combat = new CombatWrapper(item.getGameObject());
						if (combat.getPlacedAsMove()) {
							int box = combat.getCombatBox();
							if (box>0) {
								layoutHash.put(new Integer(POS_MOVE_BOX1+box-1),item);
							}
						}
					}
				}
			}
		}
		
		placeAllAttacks(POS_ATTACK_BOX1,POS_ATTACK_WEAPON1,exclude);
		
		if (sheetOwner.isCharacter()) {
			CombatWrapper combat = new CombatWrapper(sheetOwner.getGameObject());
			
			// Add all charge chits to attackers
			Collection chargeChits = combat.getChargeChits();
			for (Iterator i=chargeChits.iterator();i.hasNext();) {
				RealmComponent rc = RealmComponent.getRealmComponent((GameObject)i.next());
				layoutHash.put(new Integer(POS_CHARGECHITS),rc);
			}
			
			// Add all used chits to used box
			Collection usedChits = combat.getUsedChits();
			for (Iterator i=usedChits.iterator();i.hasNext();) {
				RealmComponent rc = RealmComponent.getRealmComponent((GameObject)i.next());
				if (!rc.isMonster() && !rc.isNative()) {
					layoutHash.put(new Integer(POS_USEDCHITS),rc);
				}
			}
		}
		updateHotSpots();
		
		if (combatFrame.getRollerResults()!=null) {
			updateRollerResults();
		}
	}
	protected void handleClick(int index,int swingConstant) {
		if (hotspotHash.get(new Integer(index))==null) {
			// Don't handle clicks unless there is a hotspot
			return;
		}
		switch(index) {
			case POS_OWNER:
				if (combatFrame.getActionState()==Constants.COMBAT_ASSIGN) {
					ArrayList list = new ArrayList();
					list.add(sheetOwner);
					combatFrame.assignTarget(list);
				}
				break;
			case POS_TARGET:
				if (combatFrame.getActionState()==Constants.COMBAT_LURE) {
					// Luring
					combatFrame.lureDenizens(sheetOwner,0,true);
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_ASSIGN) {
					// Assign Targets
					combatFrame.assignTarget();
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
					// Auto-position targets or reset
					if (layoutHash.get(new Integer(POS_TARGET_BOX1))!=null
							|| layoutHash.get(new Integer(POS_TARGET_BOX2))!=null
							|| layoutHash.get(new Integer(POS_TARGET_BOX3))!=null) {
						// reset ALL when targets have already been placed, and the target hotspot is clicked
						ArrayList toReset = new ArrayList();
						for (int i=0;i<3;i++) {
							ArrayList list = layoutHash.getList(new Integer(POS_TARGET_BOX1+i));
							if (list!=null) {
								toReset.addAll(list);
							}
						}
						for (Iterator i=toReset.iterator();i.hasNext();) {
							RealmComponent rc = (RealmComponent)i.next();
							CombatWrapper combat = new CombatWrapper(rc.getGameObject());
							combat.setCombatBox(0);
						}
						updateLayout();
					}
					else {
						// auto-position
						ArrayList list = new ArrayList(layoutHash.getList(new Integer(POS_TARGET)));
						Collections.sort(list);
						int n=0;
						while(list.size()>0) {
							RealmComponent rc = (RealmComponent)list.remove(0); // pop
							if (rc.isMonster()) {
								MonsterChitComponent monster = (MonsterChitComponent)rc;
								RealmComponent weapon = monster.getWeapon();
								if (weapon!=null) {
									list.add(0,weapon); // push
								}
							}
							else if (rc.isNative()) {
								RealmComponent horse = (RealmComponent)rc.getHorse();
								if (horse!=null) {
									if (swingConstant==SwingConstants.LEFT) {
										CombatWrapper combat = new CombatWrapper(horse.getGameObject());
										combat.setCombatBox(n+1);
									}
									else {
										list.add(0,horse); // push
									}
								}
							}
							CombatWrapper combat = new CombatWrapper(rc.getGameObject());
							combat.setCombatBox(n+1);
							n = (n+1)%3;
						}
						updateLayout();
					}
					combatFrame.updateControls();
				}
				break;
			case POS_TARGET_BOX1:
			case POS_TARGET_BOX2:
			case POS_TARGET_BOX3:
				// Position targets
				ArrayList list = layoutHash.getList(new Integer(POS_TARGET));
				combatFrame.positionTarget(index-POS_TARGET_BOX1+1,list,false,swingConstant==SwingConstants.LEFT);
				break;
			case POS_ATTACK_BOX1:
			case POS_ATTACK_BOX2:
			case POS_ATTACK_BOX3:
				if (combatFrame.getActionState()==Constants.COMBAT_TACTICS) {
					// Move attack
					combatFrame.replaceAttack(index-POS_ATTACK_BOX1+1);
				}
				else {
					combatFrame.positionAttacker(getAllBoxListFromLayout(POS_ATTACK_BOX1),index-POS_ATTACK_BOX1+1,false,swingConstant==SwingConstants.LEFT);
					updateLayout();
					repaint();
				}
				break;
			case POS_ATTACK_WEAPON1:
			case POS_ATTACK_WEAPON2:
			case POS_ATTACK_WEAPON3:
				if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
					// Play attack
					combatFrame.playAttack(index-POS_ATTACK_WEAPON1+1);
				}
				break;
			case POS_MOVE_BOX1:
			case POS_MOVE_BOX2:
			case POS_MOVE_BOX3:
				if (combatFrame.getActionState()==Constants.COMBAT_TACTICS) {
					// Move maneuver
					combatFrame.replaceManeuver(index-POS_MOVE_BOX1+1);
				}
				else {
					// Play maneuver
					combatFrame.playManeuver(index-POS_MOVE_BOX1+1);
				}
				break;
			case POS_SHIELD1:
			case POS_SHIELD2:
			case POS_SHIELD3:
				// Position shield
				if (sheetOwnerShield!=null) {
					CombatWrapper combat = new CombatWrapper(sheetOwnerShield.getGameObject());
					combat.setCombatBox(index-POS_SHIELD1+1);
					combatFrame.updateSelection();
				}
				break;
		}
		updateHotSpots();
		repaint();
	}
	public boolean hasUnpositionedDenizens() {
		return layoutHash.get(new Integer(POS_TARGET))!=null;
	}
	public boolean usesMaxCombatBoxes() {
		return usesMaxCombatBoxes(POS_TARGET_BOX1);
	}
	public boolean needsTargetAssignment() {
		return false;
	}
	protected void drawRollers(Graphics g) {
		if (redGroup!=null) drawRollerGroup(g,redGroup,POS_TARGET,POS_TARGET_BOX1);
		if (circleGroup!=null) drawRollerGroup(g,circleGroup,POS_ATTACK,POS_ATTACK_BOX1);
	}
	private static Rectangle FORT_RECT = new Rectangle(0,330,400,35);  // Fort Rect?  Ewww.
	protected void drawOther(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		if (sheetOwner!=null && sheetOwner.isCharacter()) {
			spellRegions.clear();
			spellRegionHash.clear();
			CharacterWrapper character = new CharacterWrapper(sheetOwner.getGameObject());
			
			if (character.isFortified() || character.isFortDamaged()) {
				g.setColor(Color.blue);
				g.fill(FORT_RECT);
				g.setColor(Color.white);
				g.setFont(Constants.FORTRESS_FONT);
				String fort = character.getVulnerability().getChar() + (character.isFortDamaged()?" (Damaged)":"");
				GraphicsUtil.drawCenteredString(g,FORT_RECT,fort);
				ImageIcon f = ImageCache.getIcon("actions/fortify");
				g.drawImage(f.getImage(),5,333,null);
				g.drawImage(f.getImage(),400-5-f.getIconWidth(),333,null);
				
				if (!character.isFortified()) {
					// Destroyed fortification
					g.setColor(Color.red);
					Stroke old = g.getStroke();
					g.setStroke(Constants.THICK_STROKE);
					g.drawLine(FORT_RECT.x,FORT_RECT.y,FORT_RECT.x+FORT_RECT.width,FORT_RECT.y+FORT_RECT.height);
					g.drawLine(FORT_RECT.x+FORT_RECT.width,FORT_RECT.y,FORT_RECT.x,FORT_RECT.y+FORT_RECT.height);
					g.setStroke(old);
				}
			}
			
			TextType tt = new TextType(sheetOwner.getGameObject().getName(),300,"STAT_BLACK");
			tt.draw(g, 333, 710, Alignment.Center);
			tt = new TextType("Vulnerability = "+character.getVulnerability().toString(),300,"STAT_BLACK");
			tt.draw(g, 333, 730, Alignment.Center);
			
			// If transformed, show monster values
			if (character.isTransmorphed()) {
				GameObject go = character.getTransmorph();
				RealmComponent rc = RealmComponent.getRealmComponent(go);
				if (rc.isMonster()) {
					MonsterChitComponent monster = (MonsterChitComponent)rc;
					Point p = CHARACTER_SHEET[POS_OWNER];
					g.drawImage(monster.getFightChit().getImage(),p.x-60,p.y-110,null);
					g.drawImage(monster.getMoveChit().getImage(),p.x+10,p.y-110,null);
					MonsterPartChitComponent weapon = monster.getWeapon();
					if (weapon!=null) {
						g.drawImage(weapon.getFlipSideImage(),p.x+40,p.y-30,null);
						g.drawImage(weapon.getImage(),p.x+40,p.y-50,null);
					}
				}
			}
			
			boolean isMe = GameClient.GetMostRecentClient()==null || character.getPlayerName().equals(GameClient.GetMostRecentClient().getClientName());
			if (isMe && !alwaysSecret) {
				ArrayList<SpellSet> spellSets = character.getCastableSpellSets();
				if (!spellSets.isEmpty()) {
					ArrayList<SpellCardComponent> spells = new ArrayList<SpellCardComponent>();
					for (SpellSet ss:spellSets) {
						SpellCardComponent spell = (SpellCardComponent)RealmComponent.getRealmComponent(ss.getSpell());
						if (!spells.contains(spell)) {
							spells.add(spell);
						}
					}
					int maxWidth = 200;
					int cardWidth = CardComponent.getMediumCardImageWidth();
					int totalWidth = (cardWidth*spells.size())+(5*(spells.size()-1));
					if (totalWidth>maxWidth) {
						totalWidth = maxWidth;
						cardWidth = ((maxWidth+5)/(spells.size()+1))-5;
					}
					int center = CHARACTER_SHEET[POS_OWNER].x;
					int x = center-(totalWidth>>1);
					int y = 550;
					for (SpellCardComponent spell:spells) {
						g.drawImage(spell.getMediumImage(),x,y,null);
						Rectangle r = new Rectangle(x,y,CardComponent.getMediumCardImageWidth(),CardComponent.getMediumCardImageHeight());
						spellRegions.add(0,r); // push the result, so the list is in reverse order
						spellRegionHash.put(r,spell);
						x += (cardWidth+5);
					}
					tt = new TextType("Castable Spells",300,"BOLD_BLUE");
					tt.draw(g, 333, 530, Alignment.Center);
				}
			}
			
			if (spellPoint!=null && spellCard!=null) {
				g.drawImage(spellCard.getImage(),spellPoint.x,spellPoint.y,null);
			}
			if (tallyPoint!=null && tallyView!=null) {
				tallyView.draw((Graphics2D)g,tallyPoint);
			}
		}
	}
	private Point spellPoint = null;
	private SpellCardComponent spellCard;
	
	private Point tallyPoint = null;
	private CombatTallyView tallyView = null;
	public void updateMouseHover(Point p,boolean isShiftDown) {
		super.updateMouseHover(p,isShiftDown);
		
		Point oldPoint = spellPoint;
		spellPoint = null;
		if (p!=null) {
			for (Rectangle r:spellRegions) {
				if (r.contains(p)) {
					spellPoint = new Point(p.x-CardComponent.CARD_WIDTH,p.y-CardComponent.CARD_HEIGHT);
					spellCard = spellRegionHash.get(r);
					repaint();
					break;
				}
			}
		}
		
		if (spellPoint==null?oldPoint!=null:oldPoint==null) {
			repaint();
		}
		oldPoint = tallyPoint;
		tallyPoint = null;
		if (p!=null) {
			if (mouseHoverIndex!=null && mouseHoverIndex==POS_OWNER) {
				tallyView = new CombatTallyView(getSheetOwner());
				if (tallyView.isValid()) {
					tallyPoint = new Point(p.x - tallyView.getWidth(),p.y - tallyView.getHeight());
					repaint();
				}
				else {
					tallyView = null;
				}
			}
		}
		if (tallyPoint==null?oldPoint!=null:oldPoint==null) {
			repaint();
		}
	}
	/**
	 * Testing only
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		CharacterCombatSheet sheet = new CharacterCombatSheet();
		sheet.redGroup = new CombatSheet.RollerGroup();
		sheet.redGroup.repositionRoller = sheet.makeRoller("2:4");
		sheet.redGroup.changeTacticsRoller1 = sheet.makeRoller("2:4");
		sheet.redGroup.changeTacticsRoller2 = sheet.makeRoller("2:4");
		sheet.redGroup.changeTacticsRoller3 = sheet.makeRoller("2:4");
		sheet.circleGroup = new CombatSheet.RollerGroup();
		sheet.circleGroup.repositionRoller = sheet.makeRoller("2:4");
		sheet.circleGroup.changeTacticsRoller1 = sheet.makeRoller("2:4");
		sheet.circleGroup.changeTacticsRoller2 = sheet.makeRoller("2:4");
		sheet.circleGroup.changeTacticsRoller3 = sheet.makeRoller("2:4");
		for (int i=0;i<24;i++) {
			sheet.hotspotHash.put(new Integer(i),"test");
		}
		frame.getContentPane().add(sheet,"Center");
		frame.setSize(800,600);
		frame.pack();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}
}