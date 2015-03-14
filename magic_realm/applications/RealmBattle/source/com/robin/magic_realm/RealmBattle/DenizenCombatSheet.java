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
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.swing.RealmComponentOptionChooser;
import com.robin.magic_realm.components.utility.BattleUtility;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.*;

public class DenizenCombatSheet extends CombatSheet {

	// By definition, there can only be ONE defender, as the defender is the sheetowner
	// EXCEPTION:  When hirelings deploy to RED-side up T Monsters, and there are other monster attackers, there can be multiple defenders
	private static final int POS_DEFENDER			= 0;
	private static final int POS_DEFENDER_BOX1		= 1; // sheet owner
	private static final int POS_DEFENDER_BOX2		= 2;
	private static final int POS_DEFENDER_BOX3		= 3;
	
	// By definition, there can only be ONE defender target (with or without a horse)
	private static final int POS_DEFENDER_TARGETS	= 4;
	private static final int POS_DEFENDER_TARGET_BOX1	= 5;
	private static final int POS_DEFENDER_TARGET_BOX2	= 6;
	private static final int POS_DEFENDER_TARGET_BOX3	= 7;

	// There can be MULTIPLE attackers
	private static final int POS_ATTACKERS			= 8;
	private static final int POS_ATTACKERS_BOX1		= 9;
	private static final int POS_ATTACKERS_BOX2		=10;
	private static final int POS_ATTACKERS_BOX3		=11;
	
	private static final int POS_ATTACKERS_WEAPON1	=12;
	private static final int POS_ATTACKERS_WEAPON2	=13;
	private static final int POS_ATTACKERS_WEAPON3	=14;
	
	private static final int POS_DEAD_BOX			=15;
	
	// Denizen sheet
	private static final int DEN_ROW1 = 76;
	private static final int DEN_ROW2 = 166;
	private static final int DEN_ROW3 = 258;
	
	private static final int DEN_COL1 = 52;
	private static final int DEN_COL2 = 168;
	private static final int DEN_COL3 = 284;
	
	private static final Point[] DENIZEN_SHEET = {
			new Point(DEN_COL2+80,DEN_ROW1-20),
			new Point(DEN_COL1,DEN_ROW1),
			new Point(DEN_COL2,DEN_ROW2),
			new Point(DEN_COL3,DEN_ROW3),
			
			new Point(DEN_COL1+50,320),
			new Point(DEN_COL1,440),
			new Point(DEN_COL2,440),
			new Point(DEN_COL3,440),
			
			new Point(540,25),
			new Point(428,DEN_ROW1),
			new Point(428,DEN_ROW2),
			new Point(428,DEN_ROW3),
			
			new Point(524,DEN_ROW1),
			new Point(524,DEN_ROW2),
			new Point(524,DEN_ROW3),
			
			new Point(524,440),
	};
	
	private boolean isOwnedByActive;
	private boolean targetNeedsAssignment = false;
	
	/**
	 * Testing constructor ONLY!!!
	 */
	private DenizenCombatSheet() {
		super();
	}
	public DenizenCombatSheet(CombatFrame frame,BattleModel model,RealmComponent participant,boolean interactiveFrame) {
		super(frame,model,participant,interactiveFrame);
		
		RealmComponent owner = sheetOwner.getOwner();
		isOwnedByActive = (owner!=null && owner.equals(combatFrame.getActiveParticipant()));
		
		updateLayout();
	}
	
	public int getDeadBoxIndex() {
		return POS_DEAD_BOX;
	}
	
	protected Point[] getPositions() {
		return DENIZEN_SHEET;
	}

	protected ImageIcon getImageIcon() {
		return ImageCache.getIcon("combat/den_melee3");
	}
	
	protected String[] splitHotSpot(int index) {
		if (combatFrame==null) return horseRiderSplit; // for testing
		if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
			switch(index) {
				case POS_DEFENDER_BOX1:
				case POS_DEFENDER_BOX2:
				case POS_DEFENDER_BOX3:
					if (sheetOwner.hasHorse()) return horseRiderSplit;
					break;
				case POS_DEFENDER_TARGET_BOX1:
				case POS_DEFENDER_TARGET_BOX2:
				case POS_DEFENDER_TARGET_BOX3:
					RealmComponent target = sheetOwner.getTarget();
					if (target!=null && target.hasHorse()) return horseRiderSplit;
					break;
				case POS_ATTACKERS_BOX1:
				case POS_ATTACKERS_BOX2:
				case POS_ATTACKERS_BOX3:
					if (containsHorse(getAllBoxListFromLayout(POS_ATTACKERS_BOX1))) return horseRiderSplit;
					break;
			}
		}
		return null;
	}

	protected void updateHotSpots() {
		CombatWrapper combat = new CombatWrapper(combatFrame.getActiveParticipant().getGameObject());
		GameObject go = combat.getCastSpell();
		SpellWrapper spell = go==null?null:new SpellWrapper(go);
		hotspotHash.clear();
		Collection c;
		
		if (!interactiveFrame) {
			return;
		}
		
		switch(combatFrame.getActionState()) {
			case Constants.COMBAT_LURE:
				if (isOwnedByActive && !sheetOwner.isMistLike()) {
					c = layoutHash.getList(new Integer(POS_ATTACKERS_BOX1));
					if (c==null || c.size()==0) {
						if (combatFrame.areDenizensToLure(sheetOwner)) {
							hotspotHash.put(POS_ATTACKERS_BOX1,"Lure");
						}
					}
					
					// Only add the Flip action if not a "pinning" type Monster
					if (sheetOwner.isMonster()) {
						MonsterChitComponent monster = (MonsterChitComponent)sheetOwner;
						if (monster.canPinOpponent()) {
							break;
						}
					}
					hotspotHash.put(POS_DEFENDER_BOX1,"Flip");
					if (sheetOwner.hasHorse()) {
						hotspotHash.put(POS_DEFENDER_BOX2,"Flip");
					}
				}
				break;
			case Constants.COMBAT_RANDOM_ASSIGN:
				if (!sheetOwner.isMistLike()) {
					if (isOwnedByActive) {
						if (combatFrame.areDenizensToLure(sheetOwner)) {
							// Now, need to make sure this sheetOwner is not currently targeted by a RED-side-up T Monster
							boolean hasRedSideAttacker = false;
							c = layoutHash.getList(new Integer(POS_ATTACKERS_BOX1));
							if (c!=null && !c.isEmpty()) {
								for (Iterator i=c.iterator();i.hasNext();) {
									RealmComponent rc = (RealmComponent)i.next();
									if (rc.isMonster()) {
										MonsterChitComponent monster = (MonsterChitComponent)rc;
										if (monster.isPinningOpponent()) {
											hasRedSideAttacker = true;
											break;
										}
									}
								}
							}
							if (!hasRedSideAttacker) {
								hotspotHash.put(new Integer(POS_ATTACKERS_BOX1),"Random Assignment");
							}
						}
					}
				}
				break;
			case Constants.COMBAT_DEPLOY:
				if (!sheetOwner.isMistLike()) {
					// only unassigned hirelings owned by the playing character can deploy at this point
					c = layoutHash.getList(new Integer(POS_ATTACKERS_BOX1));
					if (c==null || c.size()==0) { // No direct attackers
						c = layoutHash.getList(new Integer(POS_DEFENDER_TARGET_BOX1));
						if (c==null || c.size()==0) { // Don't forget to check defender target for red-side up T Monsters
							if (isOwnedByActive && model.getAllBattleGroups(true).size()>1) {
								hotspotHash.put(new Integer(POS_DEFENDER_BOX1),"Deploy");
							}
						}
					}
				}
				break;
			case Constants.COMBAT_ASSIGN:
				// Character assign
				if (combatFrame.getActiveCharacter()!=null
						&& combatFrame.getActiveCharacterIsHere()
						&& combatFrame.getActiveParticipant().getTarget()==null
						&& spell==null) {
					String title = combatFrame.getActiveCharacter().getGameObject().getName()+" Target";
					if (containsEnemy(
							combatFrame.getActiveParticipant(),
							layoutHash.getList(new Integer(POS_ATTACKERS_BOX1)))) {
						hotspotHash.put(new Integer(POS_ATTACKERS_BOX1),title);
					}
					if (containsEnemy(
							combatFrame.getActiveParticipant(),
							layoutHash.getList(new Integer(POS_DEFENDER_TARGET_BOX1)))) {
						hotspotHash.put(new Integer(POS_DEFENDER_TARGET_BOX1),title);
					}
					RealmComponent sheetOwnerOwner = sheetOwner.getOwner();
					if (combatFrame.allowsTreachery() || !combatFrame.getActiveParticipant().equals(sheetOwnerOwner)) {
						hotspotHash.put(new Integer(POS_DEFENDER_BOX1),title);
					}
				}
				// Hireling assign
				if (isOwnedByActive
						&& layoutHash.get(new Integer(POS_ATTACKERS_BOX1))!=null
						&& sheetOwner.getTarget()==null) {
					hotspotHash.put(new Integer(POS_DEFENDER_TARGET_BOX1),sheetOwner.getGameObject().getName()+" Target");
					targetNeedsAssignment = true;
				}
				break;
			case Constants.COMBAT_POSITIONING:
				boolean canPositionRed = false;
				boolean canPositionCircle = false;
				boolean canPositionSquare = false;
				// See 2nd-edition rule 33.4/1 and 33.4/2
				if (isOwnedByActive) {
					// Defender
					canPositionRed = true;
					
					// Attacking hirelings
					canPositionCircle = true;
					
					// Defender's target
					canPositionSquare = true;
				}
				else if (sheetOwner.getOwnerId()==null) {
					// Uncontrolled denizen sheets are different
					if (containsFriend(
							combatFrame.getActiveParticipant(),
							getAllBoxListFromLayout(POS_ATTACKERS_BOX1))) {
						canPositionCircle = true;
					}
					if (containsFriend(
							combatFrame.getActiveParticipant(),
							getAllBoxListFromLayout(POS_DEFENDER_TARGET_BOX1))) {
						canPositionSquare = true;
					}
//					if (getAllBoxListFromLayout(POS_DEFENDER_BOX1).size()>1) {
//						canPositionRed = true;
//					}
				}
				
				if (canPositionRed) {
					hotspotHash.put(new Integer(POS_DEFENDER_BOX1),"Maneuver");
					hotspotHash.put(new Integer(POS_DEFENDER_BOX2),"Maneuver");
					hotspotHash.put(new Integer(POS_DEFENDER_BOX3),"Maneuver");
				}
				if (canPositionCircle) {
					hotspotHash.put(new Integer(POS_ATTACKERS_BOX1),"Position");
					hotspotHash.put(new Integer(POS_ATTACKERS_BOX2),"Position");
					hotspotHash.put(new Integer(POS_ATTACKERS_BOX3),"Position");
				}
				if (canPositionSquare) {
					hotspotHash.put(new Integer(POS_DEFENDER_TARGET_BOX1),"Position");
					hotspotHash.put(new Integer(POS_DEFENDER_TARGET_BOX2),"Position");
					hotspotHash.put(new Integer(POS_DEFENDER_TARGET_BOX3),"Position");
				}
				
				// Character might have a target to attack
				RealmComponent target = combatFrame.getActiveParticipant().getTarget();
				ArrayList allSheetParticipants = new ArrayList(sheetParticipants);
				allSheetParticipants.add(sheetOwner);
				boolean sheetHasTarget = target!=null && sheetParticipants.contains(target);
				boolean sheetHasSpellTarget = spell!=null && spell.isAttackSpell() && spell.targetsRealmComponents(allSheetParticipants);
				if (sheetHasTarget || sheetHasSpellTarget) {
					int boxReq = spell==null?0:spell.getGameObject().getThisInt("box_req"); // most spells will be zero
					if (spell==null || boxReq==0 || boxReq==1) {
						hotspotHash.put(new Integer(POS_ATTACKERS_WEAPON1),"Attack");
					}
					if (spell==null || boxReq==0 || boxReq==2) {
						hotspotHash.put(new Integer(POS_ATTACKERS_WEAPON2),"Attack");
					}
					if (spell==null || boxReq==0 || boxReq==3) {
						hotspotHash.put(new Integer(POS_ATTACKERS_WEAPON3),"Attack");
					}
				}
				break;
			case Constants.COMBAT_TACTICS:
				CharacterWrapper character = combatFrame.getActiveCharacter();
			
				// Check conditions for REPLACE_FIGHT (Battle Bracelets)
				RealmComponent aTarget = combatFrame.getActiveParticipant().getTarget();
				if (combatFrame.getActiveParticipant().getTarget()!=null && sheetParticipants.contains(aTarget)) {
					if (character.canReplaceFight(aTarget)) {
						// can replace fight
						hotspotHash.put(new Integer(POS_ATTACKERS_WEAPON1),"Replace Attack");
						hotspotHash.put(new Integer(POS_ATTACKERS_WEAPON2),"Replace Attack");
						hotspotHash.put(new Integer(POS_ATTACKERS_WEAPON3),"Replace Attack");
					}
				}
				break;
		}
	}

	protected void updateLayout() {
		battleChitsWithRolls.clear();
		layoutHash.clear();
		
		sheetParticipants = new ArrayList();
		sheetParticipants.add(sheetOwner);
		
		ArrayList excludeList = new ArrayList();
		excludeList.add(sheetOwner);
		
		if (isOwnedByActive || combatFrame.getActionState()>=Constants.COMBAT_RESOLVING || sheetOwner.getOwnerId()==null) {
			// Always place, if owned by active, owned by none, or resolving attacks
			placeParticipant(sheetOwner,POS_DEFENDER_BOX1);
		}
		else {
			// If being attacked by a character, then positioning secrecy is required
			boolean needsSecrecy = isAttackedByCharacter();
			placeParticipant(sheetOwner,POS_DEFENDER_BOX1,needsSecrecy,false);
		}
		
		// Sheet owner's target
		RealmComponent defenderTarget = sheetOwner.getTarget();
		if (defenderTarget!=null) {
			sheetParticipants.add(defenderTarget);
			excludeList.add(defenderTarget);
			if (!addedToDead(defenderTarget)) {
				placeParticipant(defenderTarget,POS_DEFENDER_TARGET_BOX1);
			}
		}
		
		// If the sheet owner is a denizen, then ALL denizens should be in the middle... I think...
		if (sheetOwner.getOwnerId()==null && defenderTarget!=null) {
			int p = 1;
			ArrayList denizens = new ArrayList(model.getDenizenBattleGroup().getBattleParticipants());
			for (Iterator i=denizens.iterator();i.hasNext();) {
				RealmComponent denizen = (RealmComponent)i.next();
				if (!excludeList.contains(denizen) && defenderTarget.equals(denizen.getTarget())) {
					placeParticipant(denizen,POS_DEFENDER_BOX1+p);
					p++;
					p%=3;
					sheetParticipants.add(denizen);
					excludeList.add(denizen);
				}
			}
		}
		
		// Sheet owner's attackers (including characters)
		placeAllAttacks(POS_ATTACKERS_BOX1,POS_ATTACKERS_WEAPON1,excludeList);
		
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
			case POS_ATTACKERS_BOX1:
			case POS_ATTACKERS_BOX2:
			case POS_ATTACKERS_BOX3:
				if (combatFrame.getActionState()==Constants.COMBAT_LURE) {
					int lureCount = combatFrame.selectedDenizenCount();
					if (lureCount<=1) {
						// Luring
						combatFrame.lureDenizens(sheetOwner,1,false);
					}
					else if (lureCount>1) {
						JOptionPane.showMessageDialog(combatFrame,"A hireling can only lure one denizen","Invalid Lure",JOptionPane.WARNING_MESSAGE);
					}
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_RANDOM_ASSIGN) {
					// Do random assignment (guaranteed to have ONE selected here)
					combatFrame.lureDenizens(sheetOwner,1,false);
					combatFrame.updateRandomAssignment();
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_ASSIGN) {
					// Assign Target for character
					combatFrame.assignTarget(filterEnemies(combatFrame.getActiveParticipant(),layoutHash.getList(new Integer(POS_ATTACKERS_BOX1))));
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
					ArrayList list = getAllBoxListFromLayout(POS_ATTACKERS_BOX1);
					if (sheetOwner.getOwnerId()==null) {
						list = filterFriends(combatFrame.getActiveParticipant(),list);
					}
					
					combatFrame.positionAttacker(list,index-POS_ATTACKERS_BOX1+1,false,swingConstant==SwingConstants.LEFT);
					updateLayout();
					repaint();
				}
				break;
			case POS_ATTACKERS_WEAPON1:
			case POS_ATTACKERS_WEAPON2:
			case POS_ATTACKERS_WEAPON3:
				if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
					// Character Play attack
					combatFrame.playAttack(index-POS_ATTACKERS_WEAPON1+1);
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_TACTICS) {
					// Character Move attack
					combatFrame.replaceAttack(index-POS_ATTACKERS_WEAPON1+1);
				}
				break;
			case POS_DEFENDER_BOX1:
			case POS_DEFENDER_BOX2:
			case POS_DEFENDER_BOX3:
				if (combatFrame.getActionState()==Constants.COMBAT_LURE) {
					// Flip the counter
					ArrayList list = layoutHash.getList(index);
					for (Iterator i=list.iterator();i.hasNext();) {
						RealmComponent rc = (RealmComponent)i.next();
						rc.flip();
					}
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_DEPLOY) {
					doDeploy();
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
					// Native/Horse positioning and Monster/Extra positioning
//					ArrayList list = new ArrayList();
//					list.add(sheetOwner);
					// Though MOST of the time, this list will only contain ONE defender, it does happen when there are more
					ArrayList list = getAllBoxListFromLayout(POS_DEFENDER_BOX1);
					boolean includeFlipside = sheetOwner.getOwnerId()!=null && !sheetOwner.isTraveler();
					combatFrame.positionAttacker(list,index-POS_DEFENDER_BOX1+1,includeFlipside,swingConstant==SwingConstants.LEFT);
					updateLayout();
					repaint();
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_ASSIGN) {
					// Character target assignment
					combatFrame.assignTarget(layoutHash.getList(new Integer(POS_DEFENDER_BOX1)));
				}
				break;
			case POS_DEFENDER_TARGET_BOX1:
			case POS_DEFENDER_TARGET_BOX2:
			case POS_DEFENDER_TARGET_BOX3:
				if (combatFrame.getActionState()==Constants.COMBAT_ASSIGN) {
					if (sheetOwner.getTarget()==null) {
						// Assign Target for denizen (sheetOwner)
						combatFrame.assignTarget(sheetOwner,layoutHash.getList(new Integer(POS_ATTACKERS_BOX1)));
						updateLayout(); // so target can move to new box, if needed
						repaint();
					}
					else {
						// Assign Target for character from defenders target box
						combatFrame.assignTarget(layoutHash.getList(new Integer(POS_DEFENDER_TARGET_BOX1)));
					}
				}
				else if (combatFrame.getActionState()==Constants.COMBAT_POSITIONING) {
					combatFrame.positionAttacker(getAllBoxListFromLayout(POS_DEFENDER_TARGET_BOX1),index-POS_DEFENDER_TARGET_BOX1+1,false,swingConstant==SwingConstants.LEFT);
					updateLayout();
					repaint();
				}
				break;
		}
		
		updateHotSpots();
		repaint();
	}

	private void doDeploy() {
		// Deploy
		RealmComponentOptionChooser chooser = new RealmComponentOptionChooser(combatFrame,"Deploy to which target?",true);
		
		// start with unassigned denizens
		for (Iterator i=combatFrame.getUnassignedDenizens().iterator();i.hasNext();) {
			RealmComponent denizen = (RealmComponent)i.next();
			if (denizen.getTarget()==null && !denizen.isMistLike()) {
				chooser.addRealmComponent(denizen);
			}
		}
		
		// cycle through sheetOwners that aren't owned by character
		RealmComponent owner = sheetOwner.getOwner();
		CharacterWrapper sheetOwnerChar = null;
		if (sheetOwner.isPlayerControlledLeader()) {
			sheetOwnerChar = new CharacterWrapper(sheetOwner.getGameObject());
		}
		ArrayList combatSheets = combatFrame.getAllCombatSheets();
		for (Iterator i=combatSheets.iterator();i.hasNext();) {
			CombatSheet sheet = (CombatSheet)i.next();
			RealmComponent aSheetOwner = sheet.getSheetOwner();
			RealmComponent aOwner = sheet.getSheetOwner().getOwner();
			if (!owner.equals(aOwner)) {
				if (!aSheetOwner.isHidden() || (sheetOwnerChar!=null && sheetOwnerChar.foundHiddenEnemy(aSheetOwner.getGameObject()))) {
					chooser.addRealmComponent(aSheetOwner);
				}
			}
		}
		
		// cycle through other sheet participants - identify sheet owner in each case with a small icon
		for (Iterator i=combatSheets.iterator();i.hasNext();) {
			CombatSheet sheet = (CombatSheet)i.next();
			RealmComponent aSheetOwner = sheet.getSheetOwner();
			for (Iterator n=sheet.getAllParticipantsOnSheet().iterator();n.hasNext();) {
				RealmComponent participant = (RealmComponent)n.next();
				if (participant!=null) {
					RealmComponent participantOwner = participant.getOwner();
					if (!owner.equals(participantOwner) && !aSheetOwner.equals(participant)) {
						// Make sure the deploying native can "see" the participant
						if (!participant.isMistLike()) {
							if (!participant.isHidden() || (sheetOwnerChar!=null && sheetOwnerChar.foundHiddenEnemy(participant.getGameObject()))) {
								String option = chooser.generateOption();
								chooser.addRealmComponentToOption(option,aSheetOwner,RealmComponentOptionChooser.DisplayOption.MediumIcon);
								chooser.addRealmComponentToOption(option,participant);
							}
						}
					}
				}
			}
		}
		
		if (!chooser.hasOptions()) {
			JOptionPane.showMessageDialog(combatFrame,"No deployment targets available!","No Targets",JOptionPane.WARNING_MESSAGE);
			return;
		}
		chooser.setVisible(true);
		if (chooser.getSelectedText()!=null) {
			RealmComponent deployTarget = chooser.getLastSelectedComponent();
			
			boolean deployTargetKeepsTarget = false;
			boolean deployTargetMovesToNewSheet = true;
			if (deployTarget.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)deployTarget;
				if (monster.isPinningOpponent()) {
					deployTargetKeepsTarget = true;
					RealmComponent rc = monster.getTarget();
					if (rc.isCharacter()) {
						deployTargetMovesToNewSheet = false;
					}
				}
			}
			
			// Select side
			if (combatFrame.selectDeploymentSide((ChitComponent)sheetOwner)) {
				// Set the target, and leave sheet
				if (deployTarget.getTarget()==null) {
					combatFrame.removeDenizen(deployTarget);
				}
				sheetOwner.setTarget(deployTarget);
				combatFrame.makeWatchfulNatives(deployTarget,false);
				CombatWrapper sCombat = new CombatWrapper(sheetOwner.getGameObject());
				sCombat.setSheetOwner(false);
				
				// If deploying to a character sheet, go into the unpositioned box
				if (deployTarget.isCharacter()) {
					sCombat.setCombatBox(0);
					RealmComponent horse = (RealmComponent)sheetOwner.getHorse();
					if (horse!=null) {
						CombatWrapper hCombat = new CombatWrapper(horse.getGameObject());
						hCombat.setCombatBox(0);
					}
				}
				
				if (deployTargetMovesToNewSheet) {
					BattleUtility.moveToNewSheet(deployTarget,deployTargetKeepsTarget,true);
				}
				// else the attacker is simply moved OFF his own sheet, and onto the same sheet
				// the RED-side-up monster is on
				
				// Sheet owner becomes unhidden when deployed
				if (sheetOwner.isHidden()) {
					sheetOwner.setHidden(false);
				}
				
				// As does the deployTarget (if hidden)
				if (deployTarget.isHidden()) {
					deployTarget.setHidden(false);
				}
				
				// Make sure deployment causes natives to battle appropriately
				if (deployTarget.isNative()) {
					if (!combatFrame.getActiveCharacter().isBattling(deployTarget.getGameObject())) {
						combatFrame.getActiveCharacter().addBattlingNative(deployTarget.getGameObject());
					}
				}
				if (deployTarget.isPacifiedBy(combatFrame.getActiveCharacter())) {
					// Luring a pacified monster or native will break the spell
					SpellWrapper spell = deployTarget.getPacificationSpell(combatFrame.getActiveCharacter());
					spell.expireSpell();
					JOptionPane.showMessageDialog(this,spell.getName()+" was broken!");
				}
				
				// Refresh combat frame
				combatFrame.refreshParticipants();
				combatFrame.madeChange();
			}
		}
	}
	
	public boolean hasUnpositionedDenizens() {
		return false;
	}

	public boolean usesMaxCombatBoxes() {
		return usesMaxCombatBoxes(POS_ATTACKERS_BOX1);
	}
	
	public boolean needsTargetAssignment() {
		return targetNeedsAssignment;
	}

	protected void drawRollers(Graphics g) {
		if (redGroup!=null) drawRollerGroup(g,redGroup,POS_DEFENDER,POS_DEFENDER_BOX1);
		if (circleGroup!=null) drawRollerGroup(g,circleGroup,POS_ATTACKERS,POS_ATTACKERS_BOX1);
		if (squareGroup!=null) drawRollerGroup(g,squareGroup,POS_DEFENDER_TARGETS,POS_DEFENDER_TARGET_BOX1);
	}
	protected void drawOther(Graphics g) {
		// This implementation does nothing
	}
	
	/**
	 * Testing only
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		DenizenCombatSheet sheet = new DenizenCombatSheet();
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
		sheet.squareGroup = new CombatSheet.RollerGroup();
		sheet.squareGroup.repositionRoller = sheet.makeRoller("2:4");
		sheet.squareGroup.changeTacticsRoller1 = sheet.makeRoller("2:4");
		sheet.squareGroup.changeTacticsRoller2 = sheet.makeRoller("2:4");
		sheet.squareGroup.changeTacticsRoller3 = sheet.makeRoller("2:4");
		for (int i=0;i<16;i++) {
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