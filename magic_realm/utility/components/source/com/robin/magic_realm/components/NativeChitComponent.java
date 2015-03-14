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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.StarShape;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CombatWrapper;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class NativeChitComponent extends SquareChitComponent implements BattleChit,Horsebackable {
	protected int chitSize;
	private boolean alteredMoveSpeed = false;
	protected NativeChitComponent(GameObject obj) {
		super(obj);
	}
	public void updateChit() {
		int oldSize = chitSize;
		chitSize = isShrunk()?S_CHIT_SIZE:M_CHIT_SIZE;
		if (isDisplayStyleFrenzel()) {
			chitSize = H_CHIT_SIZE;
		}
		else {
			if (getGameObject().hasThisAttribute("animal")) {
				String vul = getAttribute("this", "vulnerability");
				if (vul.equals("T")) {
					chitSize = T_CHIT_SIZE;
				}
				else if (vul.equals("H")) {
					chitSize = H_CHIT_SIZE;
				}
				else if (vul.equals("M")) {
					chitSize = M_CHIT_SIZE;
				}
				else {
					chitSize = S_CHIT_SIZE;
				}
			}
		}
		if (oldSize!=chitSize) {
			updateSize();
		}
		try {
			if (isDisplayStyleFrenzel()) {
				lightColor = Color.white;
				darkColor = Color.white;
			}
			else {
				lightColor = MagicRealmColor.getColor(getAttribute("light", "chit_color"));
				darkColor = MagicRealmColor.getColor(getAttribute("dark", "chit_color"));
			}
		}
		catch (Exception ex) {
			System.out.println("problem with " + getGameObject().getName() + ": " + ex);
		}
	}
	public Dimension getSize() {
		updateChit();
		return super.getSize();
	}
	public void changeTactics() {
		flip();
	}

	public String getName() {
		return NATIVE;
	}

	public int getChitSize() {
		updateChit();
		return chitSize;
	}

	public String getLightSideStat() {
		return "light";
	}

	public String getDarkSideStat() {
		return "dark";
	}

	protected void paintFrenzelValues(Graphics2D g) {
		int cs = getChitSize();
		
		TextType tt = new TextType(getGameObject().getName(),(cs>>1)+20, "ITALIC");
		tt.draw(g,5,5,Alignment.Left);
		
		int not = getGameObject().getThisInt("notoriety");
		int hire = getGameObject().getThisInt("base_price");
		
		int midy = cs>>1;
		tt = new TextType("N:"+not, cs, "NORMAL");
		tt.draw(g,cs-28,midy-21,Alignment.Left);
		tt = new TextType("G:"+hire, cs, "NORMAL");
		tt.draw(g,cs-29,midy-11,Alignment.Left);
		
		boolean armored = getGameObject().hasThisAttribute(Constants.ARMORED);
		int x = cs - 18;
		int y = 5;
		String vul = getGameObject().getThisAttribute("vulnerability");
		if (vul!=null) {
			tt = new TextType(vul, cs, "STAT_BLACK");
			int rad = Math.max(tt.getWidth(g), tt.getHeight(g)) + 4;
			g.setColor(armored?Color.lightGray:Color.yellow);
			g.fillOval(x - 4, y + 1, rad, rad);
			tt.draw(g, x, y, Alignment.Left);
		}
		
		String commerce = getGameObject().getThisAttribute("commerce");
		if (commerce!=null) {
			if (commerce.indexOf('N')>=0) {
				tt = new TextType("+N",cs,"INFO_GREEN");
				tt.draw(g,5,midy-2,Alignment.Left);
			}
			if (commerce.indexOf('F')>=0) {
				tt = new TextType("+F",cs,"INFO_GREEN");
				tt.draw(g,5,midy+8,Alignment.Left);
			}
		}
		
		if (flies()) {
			tt = new TextType("flies",cs-4, "ITALIC");
			tt.draw(g,0,cs-35,Alignment.Right);
		}
	}
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		
		int cs = getChitSize();

		String name = gameObject.getName();
		String group = gameObject.getThisAttribute("native");
		String letterCode = group.substring(0, 1).toUpperCase();
		
		// Draw image
		String icon_type = gameObject.getThisAttribute(Constants.ICON_TYPE);
		if (icon_type != null) {
			String iconDir = gameObject.getThisAttribute(Constants.ICON_FOLDER);
			if (iconDir==null) {
				iconDir = "natives";
			}
			if (isDisplayStyleColor() || isDisplayStyleFrenzel()) {
				if (iconDir.startsWith("natives")) {
					icon_type = icon_type+"_"+letterCode.toLowerCase();
				}
				iconDir = iconDir+"_c";
			}
			if (chitSize == T_CHIT_SIZE) {
				drawIcon(g, iconDir, icon_type, 0.9);
			}
			else if (chitSize == S_CHIT_SIZE) {
				drawIcon(g, iconDir, icon_type, 0.5);
			}
			else {
				drawIcon(g, iconDir, icon_type, 0.7);
			}
		}

		TextType tt;

		// Draw Name
		if (!getGameObject().hasThisAttribute("companion")) {
			String id;
			if (getGameObject().hasThisAttribute(Constants.BOARD_NUMBER)) {
				id = name.substring(name.length() - 4,name.length()-2).trim();
			}
			else {
				id = name.substring(name.length() - 2).trim();
			}
			tt = new TextType(letterCode + id, getChitSize(), "WHITE_NOTE");
			tt.draw(g, getChitSize() - 10 - tt.getWidth(g), 7, Alignment.Left);
		}
		
		if (isDisplayStyleFrenzel() && isDarkSideUp()) {
			g.setColor(Color.black);
			g.fillRect(4,cs-19,cs-8,17);
		}

		// Draw Stats
		alteredMoveSpeed = false;
		String move_speed = String.valueOf(getMoveSpeed(false).getNum());
		String attack_speed = getAttackSpeed().getSpeedString();
		String strength = getStrength().getChitString();
		int sharpness = getSharpness();
		int x;
		int y;
		
		String statColor = "BOLD";
		if (isDisplayStyleFrenzel()) {
			statColor = isLightSideUp()?"STAT_ORANGE":"STAT_BRIGHT_ORANGE";
			
			String length = getFaceAttributeString("length");
			if (length.trim().length()==0) {
				length="0";
			}
			tt = new TextType("("+length+")", cs,statColor);
			tt.draw(g,cs>>1,cs - tt.getHeight(g) - 5,Alignment.Left);
		}
		

		tt = new TextType(strength + attack_speed, getChitSize(), statColor);
		x = 5;
		y = cs - 5 - tt.getHeight(g);
		tt.draw(g, x, y, Alignment.Left);
		x += tt.getWidth(g) + 4;
		y += tt.getHeight(g) - 6;
		for (int i = 0; i < sharpness; i++) {
			StarShape star = new StarShape(x, y, 5, 7);
			g.fill(star);
			x += 10;
		}

		String moveString = move_speed+(alteredMoveSpeed?"!":"");
		if (isDisplayStyleFrenzel()) {
			tt = new TextType(moveString, cs, "STAT_WHITE");
			x = cs - tt.getWidth(g) - 9;
			y = cs - tt.getHeight(g) - 6;
			int rad = Math.max(tt.getWidth(g), tt.getHeight(g)) + 2;
			g.setColor(Color.blue);
			g.fillOval(x - 5, y + 2, rad, rad);
			tt.draw(g, x, y, Alignment.Left);
		}
		else {
			tt = new TextType(moveString, getChitSize(), statColor);
			x = cs - 5 - tt.getWidth(g);
			y = cs - 5 - tt.getHeight(g);
			tt.draw(g, x, y, Alignment.Left);
		}
		
		if (isDisplayStyleFrenzel()) {
			paintFrenzelValues(g);
		}
		
		drawHorse(g);
		
		drawEmployer(g);
		drawHiddenStatus(g);
		drawAttentionMarkers(g);
		drawDamageAssessment(g);
	}
	private void drawHorse(Graphics g) {
		int size = getChitSize();
		NativeSteedChitComponent horse = (NativeSteedChitComponent)getHorse(false);
		if (horse!=null) {
			String[] ret = horse.getFolderAndType();
			ImageIcon icon = ImageCache.getIcon(ret[0]+"/"+ret[1],20);
			g.drawImage(icon.getImage(),size-icon.getIconWidth()-2,(size>>1),null);
		}
	}

	// BattleChit Interface
	public boolean targets(BattleChit chit) {
		RealmComponent rc = getTarget();
		return (rc != null && rc.equals(chit));
	}

	public Integer getLength() {
		Integer length = getFaceAttributeInteger("length");
		if (length == null)
			length = new Integer(0);
		return length;
	}

	public Speed getMoveSpeed() {
		return getMoveSpeed(true);
	}
	public Speed getMoveSpeed(boolean includeHorse) {
		if (includeHorse) {
			BattleHorse horse = getHorse();
			if (horse!=null) {
				return horse.getMoveSpeed();
			}
		}
		int otherSpeed = getGameObject().getThisInt("move_speed_change");
		if (otherSpeed>0) {
			alteredMoveSpeed = true;
			return new Speed(otherSpeed,speedModifier());
		}
		return new Speed(getFaceAttributeInteger("move_speed"),speedModifier());
	}

	public boolean flies() {
		return getGameObject().hasThisAttribute("flying");
	}
	public Speed getFlySpeed() {
		if (flies()) {
			return getMoveSpeed();
		}
		return null;
	}
	
	public boolean hasAnAttack() {
		return (getFaceAttributeString("strength")!=null && getFaceAttributeString("strength").length()>0)
				|| (getMagicType()!=null && getMagicType().length()>0);
	}
	
	public Speed getAttackSpeed() {
		return new Speed(getFaceAttributeInteger("attack_speed"),speedModifier());
	}

	public Strength getStrength() {
		Strength strength = new Strength(getFaceAttributeString("strength"));
		strength.modify(sizeModifier());
		if (strength.getChar()!="T" && SpellUtility.affectedByBewitchingSpellKey(getGameObject(),Constants.STRONG_MF)) {
			strength.modify(1);
		}
		return strength;
	}

	public Harm getHarm() {
		return new Harm(getStrength(), getSharpness());
	}

	public int getSharpness() {
		int sharpness = getFaceAttributeInt("sharpness");
		sharpness += getGameObject().getThisInt(Constants.ADD_SHARPNESS);
		if (sharpness>0) {
			TileLocation tl = ClearingUtility.getTileLocation(getGameObject());
			if (tl!=null && tl.isInClearing() && tl.clearing.hasSpellEffect(Constants.BLUNTED)) {
				sharpness--;
			}
		}
		return sharpness;
	}

	public String getMagicType() {
		return null; // Native's never have magic
	}

	public int getManeuverCombatBox() {
		return getManeuverCombatBox(true);
	}
	public int getManeuverCombatBox(boolean includeHorse) {
		if (includeHorse) {
			BattleHorse horse = getHorse();
			if (horse!=null) {
				return horse.getManeuverCombatBox();
			}
		}
		CombatWrapper combat = new CombatWrapper(getGameObject());
		return combat.getCombatBox();
	}

	public int getAttackCombatBox() {
		CombatWrapper combat = new CombatWrapper(getGameObject());
		return combat.getCombatBox();
	}

	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs, BattleChit attacker, int box, Harm attackerHarm,int attackOrderPos) {
		CombatWrapper combat = new CombatWrapper(getGameObject());
		
		BattleHorse horse = null;
		if (!combat.isTargetingRider(attacker.getGameObject())) {
			horse = getHorse(attackOrderPos);
		}
		boolean horseHarmed = false;
		if (horse!=null) {
			CombatWrapper horseCombat = new CombatWrapper(horse.getGameObject());
			if (horseCombat.getKilledBy()==null || horseCombat.getHitByOrderNumber()==attackOrderPos) {
				RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits the "
						+getGameObject().getName()+"'s "
						+horse.getGameObject().getName());
						
				horseHarmed = horse.applyHit(game,hostPrefs,attacker,box,attackerHarm,attackOrderPos);
				if (!attackerHarm.getStrength().isRed()) {
					// If harm is NOT RED, then only the horse is hit, otherwise, the RED continues to the native!
					return horseHarmed;
				}
			}
		}
		Harm harm = new Harm(attackerHarm);
		Strength vulnerability = new Strength(getAttribute("this", "vulnerability"));
		if (!harm.getIgnoresArmor() && getGameObject().hasThisAttribute(Constants.ARMORED)) {
			harm.dampenSharpness();
			RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits armor, and reduces sharpness: "+harm.toString());
		}
		Strength applied = harm.getAppliedStrength();
		if (applied.strongerOrEqualTo(vulnerability)) {
			// Dead native!
			combat.setKilledBy(attacker.getGameObject());
			return true;
		}
		return horseHarmed;
	}

	public boolean isMissile() {
		return gameObject.hasThisAttribute("missile");
	}
	public String getMissileType() {
		return gameObject.getThisAttribute("missile");
	}

	public void changeWeaponState(boolean hit) {
		// Do nothing
	}

	public boolean hitsOnTie() {
		return false; // default
	}
	public void setHidden(boolean val) { // I don't like using sides to determine hideness for natives...
		if (isHidden() != val) {
			if (val) {
				getGameObject().setThisAttribute(Constants.HIDDEN);
			}
			else {
				getGameObject().removeThisAttribute(Constants.HIDDEN);
			}
		}
	}
	public boolean isHidden() {
		return getGameObject().hasThisAttribute(Constants.HIDDEN);
	}
	public Strength getVulnerability() {
		Strength vul =  new Strength(getAttribute("this", "vulnerability"));
		vul.modify(sizeModifier());
		return vul;
	}
	public boolean isArmored() {
		return getGameObject().hasThisAttribute(Constants.ARMORED);
	}
	public String getAttackString() {
		Strength str = getStrength();
		if (!str.isNegligible()) {
			StringBuffer sb = new StringBuffer(str.toString());
			sb.append(getAttackSpeed().getNum());
			for (int i=0;i<getSharpness();i++) {
				sb.append("*");
			}
			return sb.toString();
		}
		return "";
	}
	public boolean isMistLike() {
		return getGameObject().hasThisAttribute("mist_like");
	}
	protected int speedModifier() {
		int mod = 0;
		if (getGameObject().hasThisAttribute(Constants.SHRINK)) {
			mod--;
		}
		return mod;
	}
	protected boolean isShrunk() {
		return getGameObject().hasThisAttribute(Constants.SHRINK);
	}
	protected int sizeModifier() {
		int mod = 0;
		if (isShrunk()) {
			mod--;
		}
		return mod;
	}
}