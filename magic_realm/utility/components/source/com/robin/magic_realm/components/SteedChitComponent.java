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

import java.awt.Graphics;
import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.*;

public class SteedChitComponent extends RoundChitComponent implements BattleHorse {
	public static final String TROT_SIDE_UP = LIGHT_SIDE_UP;
	public static final String GALLOP_SIDE_UP = DARK_SIDE_UP;

	private boolean alteredMoveSpeed = false;
	
	public SteedChitComponent(GameObject obj) {
		super(obj);
		try {
			lightColor = MagicRealmColor.getColor(getAttribute("trot","chit_color"));
			darkColor = MagicRealmColor.getColor(getAttribute("gallop","chit_color"));
		}
		catch(Exception ex) {
			System.out.println("problem with "+obj.getName()+": "+ex);
		}
	}
//public void setFacing(String val) {
//(new Exception()).printStackTrace();
//	System.out.println("Setting facing on horse to: "+val);
//	super.setFacing(val);
//}
//public void flip() {
//	System.out.println("Before flip:  isTrotting="+isTrotting());
//	super.flip();
//	System.out.println("After flip:  isTrotting="+isTrotting());
//}
	public String getLightSideStat() {
		return "trot";
	}
	public String getDarkSideStat() {
		return "gallop";
	}
	public boolean isTrotting() {
		return isLightSideUp();
	}
	public boolean isGalloping() {
		return isDarkSideUp();
	}
	public String getName() {
	    return HORSE;
	}
	public boolean doublesMove() {
		String val = getGameObject().getThisAttribute("move_bonus");
		return "double".equals(val);
	}
	public boolean extraMove() {
		String val = getGameObject().getThisAttribute("move_bonus");
		return "extra".equals(val);
	}
	
	public int getChitSize() {
		return T_CHIT_SIZE;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Draw image
		String horse_type = (String)gameObject.getAttribute("this","horse");
		if (horse_type!=null) {
			drawIcon(g,"steed"+(useColorIcons()?"_c":""),horse_type,0.5);
		}
		
		// Draw Stats
		alteredMoveSpeed = false;
		String statSide = isTrotting()?"trot":"gallop";
		String asterisk = isTrotting()?"":"*";
		
		Speed speed = isTrotting()?getTrotSpeed():getGallopSpeed();
		String strength = (String)gameObject.getAttribute(statSide,"strength");
		
		TextType tt = new TextType(strength,getChitSize(),"BIG_BOLD");
		tt.draw(g,10,(getChitSize()>>1)-tt.getHeight(g),Alignment.Left);
		
		tt = new TextType(speed.getSpeedString()+asterisk,getChitSize(),alteredMoveSpeed?"BIG_BOLD_BLUE":"BIG_BOLD");
		tt.draw(g,getChitSize()>>1,getChitSize()-(getChitSize()>>2)-(getChitSize()>>3),Alignment.Left);
		
		drawDamageAssessment(g);
	}
	// BattleChit Interface
	public boolean targets(BattleChit chit) {
		return false;
	}
	public Integer getLength() {
	    return null; // horses don't have length
	}
	public Speed getMoveSpeed() {
	    return new Speed(getFaceAttributeInteger("move_speed"));
	}
	public Speed getFlySpeed() {
		return null;
	}
	private int getMoveModifier() {
		int mod = 0;
		GameObject go = getGameObject().getHeldBy();
		if (go!=null && go.hasThisAttribute(CHARACTER)) {
			CharacterWrapper character = new CharacterWrapper(go);
			ArrayList<GameObject> list = character.getAllActiveInventoryThisKeyAndValue(Constants.HORSE_MOD,null);
			for (GameObject item:list) {
				mod += item.getThisInt(Constants.HORSE_MOD); // cumulative... though there's really only the Horse Trainer (Traveler) at this time
			}
		}
		alteredMoveSpeed = mod!=0;
		return mod;
	}
	public Speed getTrotSpeed() {
		return new Speed(getAttributeInt("trot","move_speed"),getMoveModifier());
	}
	public Speed getGallopSpeed() {
		return new Speed(getAttributeInt("gallop","move_speed"),getMoveModifier());
	}
	public Strength getTrotStrength() {
		return new Strength(getAttribute("trot","strength"));
	}
	public Strength getGallopStrength() {
		return new Strength(getAttribute("gallop","strength"));
	}
	public boolean hasAnAttack() {
		return false;
	}
	public Speed getAttackSpeed() {
	    return null; // horses don't attack
	}
	public Harm getHarm() {
		return null;
	}
	public Integer getSharpness() {
	    return null; // horses don't have sharpness
	}
	public String getMagicType() {
	    return null; // horses don't have magic
	}
	// BattleHorse Interface
	public void setGallop() {
	    setFacing(GALLOP_SIDE_UP);
	}
	public void setWalk() {
	    setFacing(TROT_SIDE_UP);
	}
	public int getManeuverCombatBox() {
		CombatWrapper combat = new CombatWrapper(getGameObject());
		return combat.getCombatBox();
	}
	public int getAttackCombatBox() {
		CombatWrapper combat = new CombatWrapper(getGameObject());
		return combat.getCombatBox();
	}
	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs,BattleChit attacker,int box,Harm attackerHarm,int attackOrderPos) {
		Harm harm = new Harm(attackerHarm);
		Strength vulnerability = new Strength(getAttribute("this","vulnerability"));
		if (!harm.getIgnoresArmor() && getGameObject().hasThisAttribute(Constants.ARMORED)) {
			harm.dampenSharpness();
			RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits armor, and reduces sharpness: "+harm.toString());
		}
		Strength applied = harm.getAppliedStrength();
		if (applied.strongerOrEqualTo(vulnerability)) {
			// Dead horse!
			CombatWrapper combat = new CombatWrapper(getGameObject());
			combat.setKilledBy(attacker.getGameObject());
			combat.setHitByOrderNumber(attackOrderPos);
			RealmLogging.logMessage(attacker.getGameObject().getName(),"Kills the "+getGameObject().getName());
			return true;
		}
		return false;
	}
	public boolean isMissile() {
		return false;
	}
	public String getMissileType() {
		return null;
	}
	public void changeWeaponState(boolean hit) {
		// Do nothing
	}
	public boolean hitsOnTie() {
		return false; // default
	}
	public SpellWrapper getSpell() {
		return null;
	}
	public Strength getVulnerability() {
		return new Strength(getAttribute("this", "vulnerability"));
	}
	public boolean isArmored() {
		return getGameObject().hasThisAttribute(Constants.ARMORED);
	}
	public boolean isDead() {
		return getGameObject().hasThisAttribute(Constants.DEAD);
	}
}