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
package com.robin.magic_realm.components;

import java.awt.*;
import java.util.*;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.utility.TreasureUtility.ArmorType;
import com.robin.magic_realm.components.wrapper.*;

public class CharacterChitComponent extends RoundChitComponent implements BattleChit,Horsebackable {

	public static final String HIDDEN = LIGHT_SIDE_UP;
	public static final String UNHIDDEN = DARK_SIDE_UP;
	
//	private static boolean pngTest = false;
//	private static boolean usePng = false;

	public CharacterChitComponent(GameObject obj) {
		super(obj);
//		if (!pngTest) {
//			pngTest = true;
//			if (IconFactory.findIcon("images/characters/amazon.png")!=null) {
//				usePng = true;
//			}
//		}
//		if (usePng) {
//			imageExtension = ".png"; // this logic breaks when you absorb a monster... sigh
//		}
		lightColor = MagicRealmColor.FORESTGREEN;
		darkColor = MagicRealmColor.PEACH;
	}

	public void setHidden(boolean val) {
		if (val) {
			setLightSideUp();
		}
		else {
			setDarkSideUp();
		}
	}

	public boolean isHidden() {
		return isLightSideUp();
	}

	public String getName() {
		return CHARACTER;
	}

	public int getChitSize() {
		return T_CHIT_SIZE;
	}

	public ImageIcon getSmallSymbol() {
		String iconFolder = gameObject.getThisAttribute(Constants.ICON_FOLDER);
		String iconType = gameObject.getThisAttribute(Constants.ICON_TYPE);
		return ImageCache.getIcon(iconFolder+"/" + iconType, 25);
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		if (character.isFortified()) {
			g.setColor(Color.blue);
			Stroke old = g.getStroke();
			g.setStroke(Constants.THICK_STROKE);
			int m = T_CHIT_SIZE>>1;
			g.drawLine(4,m-4,T_CHIT_SIZE-4,m-4);
			g.drawLine(4,m+4,T_CHIT_SIZE-4,m+4);
			g.setStroke(old);
		}
		GameObject transmorph = character.getTransmorph();
		if (transmorph != null) {
			// Draw image
			String icon_type = (String) transmorph.getThisAttribute(Constants.ICON_TYPE);
			if (icon_type != null) {
				String iconDir = transmorph.getThisAttribute(Constants.ICON_FOLDER);
				if (useColorIcons()) {
					iconDir = iconDir+"_c";
				}
				drawIcon(g,iconDir, icon_type, 0.75);
			}
			icon_type = (String) gameObject.getThisAttribute(Constants.ICON_TYPE);
			if (icon_type != null) {
				String iconFolder = gameObject.getThisAttribute(Constants.ICON_FOLDER);
				int offset = (getChitSize()>>2);
				drawIcon(g, iconFolder, icon_type, 0.30,0,offset,BACKING);
			}
		}
		else {
			// Draw image
			String iconName = gameObject.getThisAttribute(Constants.ICON_TYPE);
			String iconFolder = gameObject.getThisAttribute(Constants.ICON_FOLDER);
			if (iconName!=null && iconFolder!=null) {
				drawIcon(g,iconFolder,iconName,0.75);
			}
		}

		// Show Wish Strength, if any
		Strength wishStrength = character.getWishStrength();
		if (wishStrength != null) {
			TextType tt = new TextType("HARM=" + wishStrength.toString(), T_CHIT_SIZE, "CLOSED_RED");
			tt.draw(g, 0, (T_CHIT_SIZE - (T_CHIT_SIZE >> 2)), Alignment.Center);
		}

		drawAttentionMarkers(g);
		drawDamageAssessment(g);
	}

	// BattleChit Interface - by itself, a character has no stats - its all in the chits
	public boolean targets(BattleChit chit) {
		RealmComponent rc = getTarget();
		return (rc != null && rc.equals(chit));
	}

	public Integer getLength() {
		MonsterChitComponent transmorph = getTransmorphedComponent();
		if (transmorph==null) {
			int length = 0; // default length (dagger)
			// Derive this from the weapon used.
			CharacterWrapper character = new CharacterWrapper(getGameObject());
			boolean hasWeapon = false;
			WeaponChitComponent weapon = character.getActiveWeapon();
			if (weapon != null) {
				CombatWrapper wCombat = new CombatWrapper(weapon.getGameObject());
				if (wCombat.getCombatBox()>0) {
					hasWeapon = true;
					length = weapon.getLength();
				}
			}
			if (!hasWeapon) {
				GameObject tw = getTreasureWeaponObject();
				if (tw!=null) {
					length = tw.getThisInt("length");
				}
			}
			return new Integer(length);
		}
		return transmorph.getLength();
	}

	public String getLightSideStat() {
		return "this"; // only one stat side
	}

	public String getDarkSideStat() {
		return "this"; // only one stat side
	}

	public MonsterChitComponent getTransmorphedComponent() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		GameObject transmorph = character.getTransmorph();
		if (transmorph != null) {
			return (MonsterChitComponent) RealmComponent.getRealmComponent(transmorph);
		}
		return null;
	}

	public RealmComponent getManeuverChit() {
		return getManeuverChit(true);
	}
	public RealmComponent getManeuverChit(boolean includeHorse) {
		RealmComponent rc = null;
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		GameObject transmorph = character.getTransmorph();
		if (transmorph == null) {
			rc = BattleUtility.findMoveComponentWithCombatBox(character.getMoveSpeedOptions(new Speed(), true,false),includeHorse);
		}
		else {
			// Maneuvers are handled in the Character object
			rc = this;
		}
		return rc;
	}
	
	public Speed getMoveSpeed() {
		return getMoveSpeed(true);
	}

	/**
	 * @return The speed of the character's maneuver, which might be "stopped" if none was played
	 */
	public Speed getMoveSpeed(boolean includeHorse) {
		// Find the character's maneuver for this round
		RealmComponent rc = getManeuverChit(includeHorse);
		if (rc != null) {
			if (rc == this) {
				return BattleUtility.getMoveSpeed(getTransmorphedComponent());
			}
			else {
				return BattleUtility.getMoveSpeed(rc);
			}
		}
		return new Speed();
	}
	
	public Speed getFlySpeed() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		StrengthChit flyChit = character.getFastestFlyStrengthChit(false);
		if (flyChit!=null) {
			return flyChit.getSpeed();
		}
		return null;
	}

	public RealmComponent getAttackChit() {
		RealmComponent rc = null;
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		GameObject transmorph = character.getTransmorph();
		if (transmorph == null) {
			rc = BattleUtility.findFightComponentWithCombatBox(character.getFightSpeedOptions(new Speed(), true));
		}
		else {
			// Fight is handled in the transmorphed monster object
			rc = RealmComponent.getRealmComponent(transmorph);
		}
		return rc;
	}

	public boolean hasAnAttack() {
		return getAttackCombatBox()>0;
	}
	
	/**
	 * @return The speed of the character's attack, which might be "stopped" if none was played
	 */
	public Speed getAttackSpeed() {
		// Find the character's attack for this round
		Speed speed = new Speed();
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		RealmComponent rc = getAttackChit();
		if (rc != null) {
			speed = BattleUtility.getFightSpeed(rc);

			// Weapon speed overrides anything else
			WeaponChitComponent weapon = character.getActiveWeapon();
			if (weapon != null) {
				CombatWrapper combat = new CombatWrapper(weapon.getGameObject());
				if (combat.getCombatBox() > 0) { // only if it was played!
					Speed weaponSpeed = weapon.getSpeed();
					if (weaponSpeed != null) {
						speed = weaponSpeed;
					}
				}
			}
		}
		return speed;
	}
	
	/**
	 * Returns a GameObject that is either a WeaponChitComponent, or a TreasureCardComponent with an attack attribute (Alchemists's Mixture)
	 */
	public GameObject getActiveWeaponObject() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		WeaponChitComponent weapon = character.getActiveWeapon();
		if (weapon==null) {
			return getTreasureWeaponObject();
		}
		return weapon.getGameObject();
	}
	
	public GameObject getTreasureWeaponObject() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		for (Iterator i=character.getActiveInventory().iterator();i.hasNext();) {
			GameObject item = (GameObject)i.next();
			if (item.hasThisAttribute("attack")) {
				return item;
			}
		}
		return null;
	}

	/**
	 * @return The total harm the attack would cause. It is up to the caller to handle reducing sharpness or harm based on factors like armor or using the missile table (or fumble table)
	 */
	public Harm getHarm() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		Strength wishStrength = character.getWishStrength();
		if (wishStrength != null) {
			Harm harm = new Harm(wishStrength, 0);
			harm.setAdjustable(false);
			return harm;
		}
		Strength weaponStrength = new Strength(); // negligible strength (dagger) to start
		int sharpness = 1; // default of a dagger
		sharpness += getGameObject().getThisInt(Constants.ADD_SHARPNESS); // in case poison is applied to a dagger
		boolean ignoreArmor = getGameObject().hasThisAttribute(Constants.IGNORE_ARMOR); // false, unless penetrating grease was applied to dagger

		RealmComponent rc = getAttackChit();
		if (rc != null) {
			if (rc.isMonster()) { // character is transmorphed!
				return ((BattleChit) rc).getHarm();
			}
			boolean hasWeapon = false;
			boolean missileWeapon = false;
			Harm baseHarm = BattleUtility.getHarm(rc); // harm from the attack (ignoring the weapon)
			WeaponChitComponent weapon = character.getActiveWeapon();
			if (weapon != null) {
				if (weapon.getGameObject().hasThisAttribute(Constants.IGNORE_ARMOR)) {
					ignoreArmor = true;
				}
				CombatWrapper wCombat = new CombatWrapper(weapon.getGameObject());
				if (wCombat.getCombatBox()>0) {
					hasWeapon = true;
					missileWeapon = weapon.isMissile();
					weaponStrength = weapon.getStrength();
					sharpness = weapon.getSharpness();
				}
			}
			if (!hasWeapon) {
				// Check for treasure weapons
				GameObject tw = getTreasureWeaponObject();
				if (tw!=null) {
					if (tw.hasThisAttribute(Constants.IGNORE_ARMOR)) {
						ignoreArmor = true;
					}
					hasWeapon = true;
					missileWeapon = tw.hasThisAttribute("missile");
					weaponStrength = new Strength(tw.getThisAttribute("strength"));
					sharpness = tw.getThisInt("sharpness");
					sharpness += tw.getThisInt(Constants.ADD_SHARPNESS);
				}
			}
			if (!hasWeapon && getGameObject().hasThisAttribute(Constants.FIGHT_NO_WEAPON)) {
				weaponStrength = baseHarm.getStrength();
				sharpness = 0;
			}
			if (!missileWeapon && baseHarm.getStrength().strongerThan(weaponStrength)) {
				weaponStrength.bumpUp();
			}
		}

		Harm totalHarm = new Harm(weaponStrength, sharpness, ignoreArmor);
		return totalHarm;
	}

	public boolean hasNaturalArmor() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		return getGameObject().hasThisAttribute(Constants.ARMORED)
				|| character.affectedByKey(Constants.ADDS_ARMOR);
	}
	
	/**
	 * @return The first piece of armor that would be hit by the specified box number. (chits before cards)
	 */
	private RealmComponent getArmor(int box,int attackOrderPos) {
		ArrayList<RealmComponent> armors = getArmors(box,attackOrderPos);
		if (armors!=null && !armors.isEmpty()) {
			return armors.get(0); // Simply return the first (its sorted)
		}
		return null;
	}
	private ArrayList<RealmComponent> getArmors(int box,int attackOrderPos) {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		ArrayList<RealmComponent> armors = new ArrayList<RealmComponent>();
		
		ArrayList<GameObject> search = new ArrayList<GameObject>();
		search.addAll(character.getActiveInventory());
		if (character.isFortified()) {
			search.add(character.getGameObject());
		}
		
		for (GameObject go:search) {
			RealmComponent item = RealmComponent.getRealmComponent(go);
			CombatWrapper combat = new CombatWrapper(go);
			if (combat.getKilledBy() == null || combat.getHitByOrderNumber()==attackOrderPos) { // not destroyed or simultaneous hit
				ArmorType armorType = TreasureUtility.getArmorType(go);
				if (armorType==ArmorType.Special) {
					int boxNum = item.getGameObject().getThisInt("armor_box");
					if (boxNum == 0 || boxNum == box) {
						armors.add(item);
					}
				}
				else if (armorType!=ArmorType.None) {
					if (armorType==ArmorType.Shield) {
						if (combat.getCombatBox() == box) {
							armors.add(item);
						}
					}
					else if (armorType==ArmorType.Helmet && box == 3) {
						armors.add(item);
					}
					else if (armorType==ArmorType.Breastplate && box < 3) {
						armors.add(item);
					}
					else if (armorType==ArmorType.Armor) {
						armors.add(item);
					}
				}
				else if (item.isCharacter()) {
					armors.add(item); // fortification
				}
			}
		}
		if (armors.size() > 0) {
			// Sort chits ahead of treasure cards (exception: Ointment of Steel ahead of full suit of armor), and fortification to the front
			Collections.sort(armors, new Comparator() {
				public int compare(Object o1, Object o2) {
					int ret = 0;

					RealmComponent r1 = (RealmComponent) o1;
					RealmComponent r2 = (RealmComponent) o2;

					// Sort first by armor row (row 1 is the shield row)
					int armorRow1 = r1.getGameObject().getThisInt("armor_row");
					int armorRow2 = r2.getGameObject().getThisInt("armor_row");
					ret = armorRow1 - armorRow2;
					if (ret == 0) {
						// handle Ointment of Steel + suit of armor cases (roundabout way to determine if r2 is ointment of steel) */
						if (r1 instanceof ArmorChitComponent) {
							if (((ArmorChitComponent) r1).isSuitOfArmorType() && (!r2.isChit() && r2.getGameObject().getThisInt("armor_row") == 3)) {
								return 1;
							}
						}
						else if (r2 instanceof ArmorChitComponent) {
							if ((!r1.isChit() && r1.getGameObject().getThisInt("armor_row") == 3) && ((ArmorChitComponent)r2).isSuitOfArmorType()) {
								return -1;
							}
						}
						else {
							// Then by chit vs card
							int score1 = r1.isChit() ? 0 : 1;
							int score2 = r2.isChit() ? 0 : 1;
							ret = score1 - score2;
						}
					}
					return ret;
				}
			});
			return armors;
		}
		return null;
	}
	
	/**
	 * @return true if damage was applied in some way
	 */
	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs, BattleChit attacker, int box, Harm attackerHarm,int attackOrderPos) {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		CombatWrapper combat = new CombatWrapper(getGameObject());
		
		boolean damageTaken = false;

		// Start off with the assumption that the character was NOT killed
		boolean characterWasKilled = false;

		MonsterChitComponent transmorph = getTransmorphedComponent();
		if (transmorph != null) {
			boolean ret = transmorph.applyHit(game,hostPrefs, attacker, box, attackerHarm,attackOrderPos);
			CombatWrapper monCombat = new CombatWrapper(transmorph.getGameObject());
			if (monCombat.getKilledBy() != null) {
				combat.setKilledBy(attacker.getGameObject());
				ret = true;
			}
			return ret;
		}
		
		Harm harm = new Harm(attackerHarm); // clone the harm
		if (harm.getStrength().isRed()) {
			// character is killed automatically if hit by a red side up monster
			characterWasKilled = true;
		}
		else {
			// First thing, check to see if a Horse maneuver was played
			SteedChitComponent horse = (SteedChitComponent) character.getActiveSteed(attackOrderPos);
			if (horse != null && !combat.isTargetingRider(attacker.getGameObject())) {
				CombatWrapper horseCombat = new CombatWrapper(horse.getGameObject());
				if (horseCombat.getCombatBox() > 0) {
					RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits the "
							+getGameObject().getName()+"'s "
							+horse.getGameObject().getName());
					// Horse was active and played - it takes the hit!
					return horse.applyHit(game,hostPrefs, attacker, box, harm,attackOrderPos);// INSTEAD of the character!
				}
			}

			Strength vulnerability = character.getVulnerability();

			// Find armor (if any) at box - chits before cards...
			RealmComponent armor = null;
			boolean isDragonBreath = attacker.isMissile() && attacker.getGameObject().hasThisAttribute("dragon_missile");
			if (harm.getAppliedStrength().strongerThan(new Strength("T")) && attacker.isMissile()) {
				// If harm exceeds T, armor is ignored, and target is killed, regardless of armor (damn!)
				harm.setIgnoresArmor(true);
				RealmLogging.logMessage(attacker.getGameObject().getName(),"Harm is greater than Tremendous ("+harm+")!");
				RealmLogging.logMessage(attacker.getGameObject().getName(),"Missile attack hits a vital unarmored spot!");
			}
			else if (hostPrefs.hasPref(Constants.OPT_PENETRATING_ARMOR) && attacker.isMissile()) {
				// When Penetrating Armor is in play, and the attack is a missile attack, then the armor is never actually "hit".
				
				/*
				 * Harm exceeds T			= target is killed, regardless of armor
				 * Armor exceeds Harm		= harm does nothing
				 * Armor equals Harm		= target takes one wound, but nothing else happens (no damaged/destroyed armor)
				 * Harm exceeds Armor		= harm drops by one level (in ADDITION to sharpness already lost), and inflicts the target
				 * 
				 * Multiple layers can cause multiple reductions
				 */
				
				if (harm.getAppliedStrength().strongerThan(new Strength("T"))) {
				}
				else {
					ArrayList<RealmComponent> armors = getArmors(box,attackOrderPos);
					if (armors!=null && !armors.isEmpty()) {
						harm.dampenSharpness();
						RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits armor, and reduces sharpness: "+harm.toString());
						
						for (RealmComponent test:armors) {
							if (isDragonBreath && test.getGameObject().hasThisAttribute(Constants.IMMUNE_BREATH)) {
								harm = new Harm(new Strength(),0); // negate harm!
								RealmLogging.logMessage(attacker.getGameObject().getName(),"Dragon breath attack is stopped by "+test.getGameObject().getName());
							}
							else {
								Strength armorVulnerability = new Strength(test.getGameObject().getThisAttribute("vulnerability"));
								if (armorVulnerability.strongerThan(harm.getAppliedStrength())) {
									harm = new Harm(new Strength(),0); // negate harm!
									RealmLogging.logMessage(attacker.getGameObject().getName(),"Missile attack is stopped by "+test.getGameObject().getName());
									break;
								}
								else if (armorVulnerability.equals(harm.getAppliedStrength())) {
									harm.setWound(true);
									RealmLogging.logMessage(attacker.getGameObject().getName(),"Missile attack is stopped by "+test.getGameObject().getName()+", but causes 1 wound.");
									break;
								}
								else { // can assume harm is greater than armor now
									harm.dropOneLevel();
									RealmLogging.logMessage(attacker.getGameObject().getName(),"Missile attack penetrates "+test.getGameObject().getName()+"!  Drops one level: "+harm);
								}
							}
						}
					}
				}
			}
			else {
				armor = getArmor(box,attackOrderPos);
				if (armor!=null && isDragonBreath && armor.getGameObject().hasThisAttribute(Constants.IMMUNE_BREATH)) {
					harm = new Harm(new Strength(),0); // negate harm!
					RealmLogging.logMessage(attacker.getGameObject().getName(),"Dragon breath attack is stopped by "+armor.getGameObject().getName());
				}
			}

			boolean tookSeriousWounds = false;
			Strength minForWound = new Strength("L"); // Without armor, L is all that is required to wound
			
			if (armor==null && hasNaturalArmor()) { // custom character possibility
				harm.dampenSharpness();
				RealmLogging.logMessage(attacker.getGameObject().getName(),getGameObject().getName() + " has natural armor, which reduces sharpness: "+harm.toString());
			}
			
			if (!harm.getIgnoresArmor() && armor != null) {
				// If armor, reduce harm by one star, determine if armor is damaged/destroyed, apply wounds
				harm.dampenSharpness();
				if (armor.isCharacter()) {
					RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits characater fortification, and reduces sharpness: "+harm.toString());
				}
				else {
					RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits armor ("+armor.getGameObject().getName()+"), and reduces sharpness: "+harm.toString());
				}
				Strength armorVulnerability = new Strength(armor.getGameObject().getThisAttribute("vulnerability"));
				if (harm.getAppliedStrength().strongerOrEqualTo(armorVulnerability)) {
					boolean destroyed = true;
					damageTaken = true;
					if (armor.isArmor() && !harm.getAppliedStrength().strongerThan(armorVulnerability)) {
						// damaged
						if (armor.isCharacter()) {
							if (!character.isFortDamaged()) {
								RealmLogging.logMessage(attacker.getGameObject().getName(),"Damages the character fortification.");
								character.setFortDamaged(true);
							}
						}
						else {
							ArmorChitComponent armorChit = (ArmorChitComponent) armor;
							if (!armorChit.isDamaged()) {
								RealmLogging.logMessage(attacker.getGameObject().getName(),"Damages the "
										+getGameObject().getName()+"'s "
										+armor.getGameObject().getName());
								destroyed = false;
								armorChit.setIntact(false); // NOW its damaged
							}
						}
					}
					if (destroyed) {
						if (armor.isCharacter()) {
							character.setFortified(false);
							character.setFortDamaged(true);
						}
						else {
							CombatWrapper combatArmor = new CombatWrapper(armor.getGameObject());
	
							// Instead of removing right now, how about "killing" it, so it shows up with a red X
							if (!combatArmor.isDead()) {
								combatArmor.setKilledBy(attacker.getGameObject());
								combatArmor.setHitByOrderNumber(attackOrderPos);
								RealmLogging.logMessage(attacker.getGameObject().getName(),"Destroys the "
										+getGameObject().getName()+"'s "
										+armor.getGameObject().getName());
								
								// Treasure armor, should convert to its gold value.
								if (armor.getGameObject().hasAttribute("destroyed", "base_price")) {
									int basePrice = armor.getGameObject().getAttributeInt("destroyed", "base_price");
									if (basePrice > 0) {
										character.addGold(basePrice);
										RealmLogging.logMessage(getGameObject().getName(),"Gains "
												+basePrice+" gold for the loss of the "
												+armor.getGameObject().getName());
									}
								}
							}
						}

						// Armor cards are simply removed (not relocated anywhere)
					}
				}
				// else armor is unharmed

				// Wound minimum is increased to M if there is armor involved
				minForWound = new Strength("M");
			}
			else if (harm.getAppliedStrength().strongerOrEqualTo(vulnerability)) {
				// Direct hit (no armor)
				if (hostPrefs.hasPref(Constants.ADV_SERIOUS_WOUNDS) && harm.getAppliedStrength().equalTo(vulnerability)) {
					// Serious wounds
					Collection c = character.getNonWoundedChits();
					DieRoller roller = DieRollBuilder.getDieRollBuilder(null,character).createRoller("wounds");
					int seriousWounds = roller.getHighDieResult();
					int currentWounds = combat.getNewWounds();
					
					RealmLogging.logMessage(getGameObject().getName(),"Takes a serious wound!");
					RealmLogging.logMessage(getGameObject().getName(),roller.getDescription());
					RealmLogging.logMessage(getGameObject().getName(),"Serious wound = "+seriousWounds+" wound"+(seriousWounds==1?"":"s")+")");
					if (c != null && c.size() > (currentWounds + seriousWounds)) {
						combat.addNewWounds(seriousWounds);
						combat.addSeriousWoundRoll(roller.getStringResult());
						tookSeriousWounds = true;
						damageTaken = true;
					}
					else {
						// Dead character!
						characterWasKilled = true;
					}
				}
				else {
					// Dead character!
					characterWasKilled = true;
				}
			}
			else if (harm.isWound()) {
				Collection c = character.getNonWoundedChits();
				if (c.size() > 1) {
					combat.addNewWounds(1);
				}
				else {
					// Dead character!
					characterWasKilled = true;
				}
			}

			// Check for wounds
			if (!characterWasKilled && !tookSeriousWounds && harm.getAppliedStrength().strongerOrEqualTo(minForWound)) {
				// Wound character here, unless character is immune...
				if (armor==null || !character.hasActiveInventoryThisKey(Constants.STOP_WOUNDS)) {
					Collection c = character.getActiveChits();
					int currentWounds = combat.getNewWounds();
					if (c != null && c.size() > currentWounds) {
						// Can't do the selection here! (this is called from the host, not the client)
						combat.addNewWounds(1);
						damageTaken = true;
					}
					else {
						// Dead character!
						characterWasKilled = true;
					}
				}
				else {
					RealmLogging.logMessage(getGameObject().getName(),"Avoided taking a wound!");
				}
			}
		}
		if (characterWasKilled) {
			combat.setKilledBy(attacker.getGameObject());
			damageTaken = true;
		}
		return damageTaken;
	}

	public String getMagicType() {
		RealmComponent rc = getAttackChit();
		if (rc.isMonster()) {
			return ((MonsterChitComponent)rc).getMagicType();
		}
		return null;
	}

	public int getManeuverCombatBox() {
		return getManeuverCombatBox(true);
	}
	public int getManeuverCombatBox(boolean includeHorse) {
		RealmComponent rc = getManeuverChit(includeHorse);
		if (rc != null) {
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			return combat.getCombatBox();
		}
		return 0;
	}

	public int getAttackCombatBox() {
		RealmComponent rc = getAttackChit();
		if (rc != null) {
			CombatWrapper combat = new CombatWrapper(rc.getGameObject());
			return combat.getCombatBox();
		}
		return 0;
	}
	public boolean isMissile() {
		// This depends on the weapon
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		GameObject transmorph = character.getTransmorph();
		if (transmorph == null) { // Character must not be transmorphed!
			WeaponChitComponent weapon = character.getActiveWeapon();
			if (weapon != null) {
				return weapon.isMissile();
			}
			else {
				GameObject tw = getTreasureWeaponObject();
				if (tw!=null) {
					return tw.hasThisAttribute("missile");
				}
			}
		}
		return false;
	}
	public String getMissileType() {
		// This depends on the weapon
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		GameObject transmorph = character.getTransmorph();
		if (transmorph == null) { // Character must not be transmorphed!
			WeaponChitComponent weapon = character.getActiveWeapon();
			if (weapon == null) {
				GameObject tw = getTreasureWeaponObject();
				if (tw!=null) {
					return tw.getThisAttribute("missile");
				}
			}
		}
		return "";
	}
	
	private boolean testEffectIsOnActiveWeapon(GameObject effector) {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		WeaponChitComponent weapon = character.getActiveWeapon();
		if (weapon != null) {
			String affectedWeaponId = effector.getThisAttribute(Constants.AFFECTED_WEAPON_ID);
			if (affectedWeaponId != null && affectedWeaponId.equals(weapon.getGameObject().getStringId())) {
				return true;
			}
		}
		return false;
	}
	public boolean activeWeaponStaysAlerted() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		GameObject dust = character.getActiveInventoryThisKey(Constants.ALERTED_WEAPON);
		return dust!=null && testEffectIsOnActiveWeapon(dust);
	}
	public boolean hitsOnTie() {
		boolean hitsOnTie = getGameObject().hasThisAttribute(Constants.HIT_TIE); // In case Ointment of Bite was applied to dagger
		GameObject weapon = getActiveWeaponObject();
		return hitsOnTie || (weapon!=null && weapon.hasThisAttribute(Constants.HIT_TIE));
	}
	public void changeWeaponState(boolean hit) {
		CharacterWrapper character = new CharacterWrapper(getGameObject());

		WeaponChitComponent weapon = character.getActiveWeapon();
		if (weapon != null) {
			// make sure weapon was played in combat this round (otherwise it doesn't change)
			int box = (new CombatWrapper(weapon.getGameObject())).getCombatBox();
			if (box > 0) {
				if (activeWeaponStaysAlerted() || weapon.getGameObject().hasThisAttribute(Constants.ALERTED_WEAPON)) {
					// Treat like the character just missed - keeps weapon alerted
					hit = false;
				}

				// hits should unalert weapons, misses should alert them
				weapon.setAlerted(!hit);
			}
		}
	}
	public void setTarget(RealmComponent comp) {
		super.setTarget(comp);
		MonsterChitComponent monster = getTransmorphedComponent();
		if (monster!=null) {
			monster.setTarget(comp);
		}
	}
	public void clearTarget() {
		super.clearTarget();
		MonsterChitComponent monster = getTransmorphedComponent();
		if (monster!=null) {
			monster.clearTarget();
		}
	}
	public boolean isMistLike() {
		RealmComponent rc = getTransmorphedComponent();
		return rc!=null && rc.isMistLike();
	}
}