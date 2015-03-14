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

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.*;

public class NativeSteedChitComponent extends SquareChitComponent implements BattleHorse {

	public static final String TROT_SIDE_UP = LIGHT_SIDE_UP;
	public static final String GALLOP_SIDE_UP = DARK_SIDE_UP;
	
	protected NativeSteedChitComponent(GameObject obj) {
		super(obj);
		try {
			lightColor = MagicRealmColor.getColor(getAttribute("trot","chit_color"));
			darkColor = MagicRealmColor.getColor(getAttribute("gallop","chit_color"));
		}
		catch(Exception ex) {
			System.out.println("problem with "+obj.getName()+": "+ex);
		}
	}
	public String getName() {
	    return NATIVE_HORSE;
	}
	
	public int getChitSize() {
		return getRider().isShrunk()?M_CHIT_SIZE:H_CHIT_SIZE;
	}
	private NativeChitComponent getRider() {
		return (NativeChitComponent)RealmComponent.getRealmComponent(getGameObject().getHeldBy());
	}
	protected int sizeModifier() {
		return getRider().sizeModifier();
	}
	protected int speedModifier() {
		return getRider().speedModifier();
	}
	public String getLightSideStat() {
		return "trot";
	}
	public String getDarkSideStat() {
		return "gallop";
	}
	public String[] getFolderAndType() {
		String name = gameObject.getName();
		String letterCode = name.substring(0,1).toUpperCase();
		String horse_type = (String)gameObject.getAttribute("this","horse");
		if (horse_type!=null) {
			String folder = useColorIcons()?"steed_c":"steed";
			horse_type = horse_type + (useColorIcons()?("_"+letterCode.toLowerCase()):"");
			String[] ret = new String[2];
			ret[0] = folder;
			ret[1] = horse_type;
			return ret;
		}
		return null;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		String name = gameObject.getName();
		String letterCode = name.substring(0,1).toUpperCase();
		
		// Draw image
		String horse_type = (String)gameObject.getAttribute("this","horse");
		if (horse_type!=null) {
			String folder = useColorIcons()?"steed_c":"steed";
			horse_type = horse_type + (useColorIcons()?("_"+letterCode.toLowerCase()):"");
			drawIcon(g,folder,horse_type,0.5);
		}
		
		TextType tt;
		
		// Draw Owner
		if (!getGameObject().hasThisAttribute("companion")) {
			String id;
			if (getGameObject().hasThisAttribute(Constants.BOARD_NUMBER)) {
				id = name.substring(name.length() - 4,name.length()-2).trim();
			}
			else {
				id = name.substring(name.length() - 2).trim();
			}
			tt = new TextType(letterCode+id,getChitSize(),"WHITE_NOTE");
			tt.draw(g,getChitSize()-10-tt.getWidth(g),7,Alignment.Left);
		}
		
		// Draw Stats
		String asterisk = isTrotting()?"":"*";
		
		String speed = getMoveSpeed().getSpeedString();
		String strength = getStrength().getChitString();
		
		tt = new TextType(strength,getChitSize(),"BIG_BOLD");
		tt.draw(g,10,(getChitSize()>>1)-tt.getHeight(g),Alignment.Left);
		
		tt = new TextType(speed+asterisk,getChitSize(),"BIG_BOLD");
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
	    return new Speed(getFaceAttributeInteger("move_speed"),speedModifier());
	}
	public Speed getFlySpeed() {
		return null;
	}
	public boolean hasAnAttack() {
		return false;
	}
	public Speed getAttackSpeed() {
	    return null; // horses don't attack
	}
	public Strength getStrength() {
		Strength strength = new Strength(getFaceAttributeString("strength"));
		strength.modify(sizeModifier());
		return strength;
	}
	public Harm getHarm() {
		return null; // horses don't cause harm
	}
	public int getSharpness() {
	    return 0;
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
	public boolean isTrotting() {
		return isLightSideUp();
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
		Strength strength = new Strength(getAttribute("this", "vulnerability"));
		strength.modify(sizeModifier());
		return strength;
	}
	public boolean isArmored() {
		return getGameObject().hasThisAttribute(Constants.ARMORED);
	}
	public String getAttackString() {
		Strength str = getStrength();
		if (!str.isNegligible()) {
			return str.toString();
		}
		return "";
	}
	public boolean doublesMove() {
		String val = getGameObject().getThisAttribute("move_bonus");
		return "double".equals(val);
	}
	public boolean extraMove() {
		String val = getGameObject().getThisAttribute("move_bonus");
		return "extra".equals(val);
	}
	public boolean isDead() {
		return getGameObject().hasThisAttribute(Constants.DEAD);
	}
}