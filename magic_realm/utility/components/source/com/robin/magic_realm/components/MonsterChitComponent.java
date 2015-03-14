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
import java.util.ArrayList;
import java.util.Iterator;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.StarShape;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CombatWrapper;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class MonsterChitComponent extends SquareChitComponent implements BattleChit {
	protected int chitSize;
	
	private boolean alteredMoveSpeed = false;
	
	private MonsterFightChitComponent fightChit = null; // only used when a monster is absorbed!!
	private MonsterMoveChitComponent moveChit = null; // only used when a monster is absorbed!!
	
	public static boolean showMonsterNumbers = false;
	
	public MonsterFightChitComponent getFightChit() {
		if (fightChit==null) {
			fightChit = new MonsterFightChitComponent(getGameObject());
		}
		return fightChit;
	}

	public MonsterMoveChitComponent getMoveChit() {
		if (moveChit==null) {
			moveChit = new MonsterMoveChitComponent(getGameObject());
		}
		return moveChit;
	}

	protected MonsterChitComponent(GameObject obj) {
		super(obj);
	}
	public void updateChit() {
		int oldSize = chitSize;
		if (isDisplayStyleFrenzel()) {
			chitSize = H_CHIT_SIZE;
		}
		else {
			String vul = getVulnerability().getChar();
			if (vul.equals("T") || vul.equals("!")) {
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

	public boolean isBlocked() {
		return getGameObject().hasThisAttribute("blocked");
	}

	public void setBlocked(boolean val) {
		boolean current = getGameObject().hasThisAttribute("blocked");
		if (current != val) {
			if (val) {
				getGameObject().setThisAttribute("blocked");
			}
			else {
				getGameObject().removeThisAttribute("blocked");
			}
		}
	}

	public MonsterPartChitComponent getWeapon() {
		if (!getGameObject().hasThisAttribute("animal")) { // as long as the monster isn't transformed!
			ArrayList list = getGameObject().getHold();
			if (list != null && list.size() > 0) {
				for (Iterator i=list.iterator();i.hasNext();) {
					GameObject weapon = (GameObject)i.next();
					RealmComponent rc = RealmComponent.getRealmComponent(weapon);
					if (rc.isMonsterPart()) { // Might be a Hurricane Winds FLY chit
						return (MonsterPartChitComponent) RealmComponent.getRealmComponent(weapon);
					}
				}
			}
		}
		return null;
	}

	public boolean isTremendous() {
		return getVulnerability().isTremendous();
	}
	
	public boolean canPinOpponent() {
		return getGameObject().hasAttribute("dark","pins");
	}

	public void changeTactics() {
		flip();
	}

	public String getName() {
		return MONSTER;
	}

	public String getLightSideStat() {
		return "light";
	}

	public String getDarkSideStat() {
		return "dark";
	}

	public int getChitSize() {
		updateChit();
		return chitSize;
	}
	
	public boolean isPinningOpponent() {
		return hasFaceAttribute("pins");
	}
	
	public boolean isRedSideUp() {
		return ("RED".equals(getFaceAttributeString("strength")));
	}

	public void paintAttackValues(Graphics2D g,int ox,int oy,Color attackBack) {
		int cs = getChitSize();
		TextType tt;
		String attack_speed = getAttackSpeed().getSpeedString();
		String strength = getStrength().getChitString();
		String magic_type = getFaceAttributeString("magic_type");
		int sharpness = getSharpness();
		
		boolean pins = isPinningOpponent();
		boolean red = strength.equals("!");
		String statColor = "STAT_BLACK";
		g.setColor(Color.black);
		if (isDisplayStyleFrenzel()) {
			if (!pins) {
				if (isLightSideUp()) {
					statColor = "STAT_ORANGE";
				}
				else {
					statColor = "STAT_BRIGHT_ORANGE";
				}
				
				String length = getFaceAttributeString("length");
				if (length.trim().length()==0) {
					length="0";
				}
				tt = new TextType("("+length+")", cs,statColor);
				tt.draw(g,cs>>1,oy - tt.getHeight(g),Alignment.Left);
			}
		}
		
		int x;
		int y;
		if (red) {
			tt = new TextType(attack_speed, cs,statColor);
			x = ox + 8;
			y = oy - 2 - tt.getHeight(g);
			int rad = Math.max(tt.getWidth(g), tt.getHeight(g)) + 4;
			g.fillOval(x - 6, y + 1, rad, rad);
			g.setColor(attackBack);
			g.fillOval(x - 4, y + 3, rad - 4, rad - 4);
		}
		else {
			String string = (strength + magic_type + attack_speed);
			tt = new TextType(string, cs,statColor);
			x = ox + 6;
			y = oy - tt.getHeight(g);
		}
		tt.draw(g, x, y, Alignment.Left);
		x += tt.getWidth(g) + 4;
		y += tt.getHeight(g) - 6;
		for (int i = 0; i < sharpness; i++) {
			StarShape star = new StarShape(x, y, 5, 7);
			g.fill(star);
			x += 10;
		}
	}
	public void paintMoveValues(Graphics2D g,int ox,int oy) {
		int cs = getChitSize();
		alteredMoveSpeed = false;
		Speed move_speed = getMoveSpeed();
		if (move_speed!=null) {
			String string = String.valueOf(move_speed.getNum())+(alteredMoveSpeed?"!":"");
			if (isDisplayStyleFrenzel()) {
				int x,y;
				TextType tt = new TextType(string, cs, "STAT_WHITE");
				x = ox - tt.getWidth(g)-4;
				y = oy - tt.getHeight(g)-2;
				int rad = Math.max(tt.getWidth(g), tt.getHeight(g)) + 2;
				g.setColor(Color.blue);
				g.fillOval(x - 5, y + 2, rad, rad);
				tt.draw(g, x, y, Alignment.Left);
			}
			else {
				int x,y;
				TextType tt = new TextType(string, cs, "STAT_BLACK");
				x = ox - tt.getWidth(g);
				y = oy - tt.getHeight(g);
				tt.draw(g, x, y, Alignment.Left);
			}
		}
	}
	protected void paintFrenzelValues(Graphics2D g) {
		int cs = getChitSize();
		
		TextType tt = new TextType(getGameObject().getName(),(cs>>1)+20, "ITALIC");
		tt.draw(g,5,5,Alignment.Left);
		
		int not = getGameObject().getThisInt("notoriety");
		int fam = getGameObject().getThisInt("fame");
		if (not>0 || fam>0) {
			int midy = cs>>1;
			tt = new TextType("N:"+not, cs, "NORMAL");
			tt.draw(g,cs-25,midy-21,Alignment.Left);
			tt = new TextType("F:"+fam, cs, "NORMAL");
			tt.draw(g,cs-24,midy-11,Alignment.Left);
		}
		
		boolean armored = getGameObject().hasThisAttribute(Constants.ARMORED);
		int x = cs - 18;
		int y = 5;
		String vul = getVulnerability().getChar();//getGameObject().getThisAttribute("vulnerability");
		if (vul!=null) {
			tt = new TextType(vul, cs, "STAT_BLACK");
			int rad = Math.max(tt.getWidth(g), tt.getHeight(g)) + 4;
			g.setColor(armored?Color.lightGray:Color.yellow);
			g.fillOval(x - 4, y + 1, rad, rad);
			tt.draw(g, x, y, Alignment.Left);
		}
		
		String tileReq = getGameObject().getThisAttribute(Constants.SETUP_START_TILE_REQ); // Pruitt's monsters
		if (tileReq!=null) {
			tt = new TextType(tileReq, cs, "STAT_BLACK");
			x = cs - tt.getWidth(g)-6;
			y = cs>>1;
			
			g.setColor(MagicRealmColor.LIGHTGREEN);
			g.fillRect(x-2,y+1,tt.getWidth(g)+4,tt.getHeight(g)+4);
			
			tt.draw(g,x,y,Alignment.Left);
		}
		
		if (flies()) {
			tt = new TextType("flies",cs-4, "ITALIC");
			tt.draw(g,0,cs-35,Alignment.Right);
		}
	}
	protected String getIconFolder() {
		String iconDir = getGameObject().getThisAttribute(Constants.ICON_FOLDER);
		if (useColorIcons()) {
			iconDir = iconDir+"_c";
		}
		return iconDir;
	}
	public Dimension getSize() {
		updateChit();
		return super.getSize();
	}
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		int cs = getChitSize();
		
		Color attackBack = isLightSideUp()?lightColor:darkColor;

		// Draw image
		String icon_type = (String)gameObject.getThisAttribute(Constants.ICON_TYPE);
		if (icon_type != null) {
			if (isDisplayStyleFrenzel()) {
				drawIcon(g, getIconFolder(), icon_type, 0.6,-5,1,null);
			}
			else {
				if (chitSize == T_CHIT_SIZE) {
					drawIcon(g, getIconFolder(), icon_type, 0.9);
				}
				else if (chitSize == S_CHIT_SIZE) {
					drawIcon(g, getIconFolder(), icon_type, 0.5);
				}
				else {
					drawIcon(g, getIconFolder(), icon_type, 0.7);
				}
			}
		}
		
		if (showMonsterNumbers && gameObject.hasThisAttribute(Constants.NUMBER)) {
			g.setColor(Color.black);
			g.setFont(new Font("Dialog",Font.BOLD,11));
			int x = 5;
			int y = cs>>1;
			if (isDisplayStyleFrenzel()) {
				y += 10;
			}
			g.drawString(gameObject.getThisAttribute(Constants.NUMBER),x,y);
		}
		
		
		if (isDisplayStyleFrenzel() && isDarkSideUp()) {
			attackBack = isPinningOpponent()?Color.red:Color.black;
			g.setColor(attackBack);
			g.fillRect(4,cs-20,cs-8,18);
		}

		// Draw Stats
		paintAttackValues(g,0,cs - 5,attackBack);
		paintMoveValues(g,cs - 5,cs - 5);
		
		if (isDisplayStyleFrenzel()) {
			paintFrenzelValues(g);
		}

		drawEmployer(g);
		drawHiddenStatus(g);
		drawAttentionMarkers(g);
		drawDamageAssessment(g);
	}

	// BattleChit Interface
	public boolean targets(BattleChit chit) {
		RealmComponent rc = getTarget();
		return (rc != null && rc.equals(chit));
	}

	public Integer getLength() {
		Integer length = getFaceAttributeInteger("length");
		if (length == null) {
			length = new Integer(0); // tooth and claw
		}
		return length;
	}

	public Speed getMoveSpeed() {
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
	
	public String getAttackString() {
		String magicType = getMagicType();
		if (magicType!=null && magicType.trim().length()>0) {
			return magicType+getAttackSpeed().getNum();
		}
		else {
			Strength str = getStrength();
			if (!str.isNegligible()) {
				StringBuffer sb = new StringBuffer(str.toString());
				sb.append(getAttackSpeed().getNum());
				for (int i=0;i<getSharpness();i++) {
					sb.append("*");
				}
				return sb.toString();
			}
		}
		return "";
	}
	
	protected int speedModifier() {
		int mod = 0;
		if (getGameObject().hasThisAttribute(Constants.SLOWED)) {
			mod++;
		}
		if (getGameObject().hasThisAttribute(Constants.SHRINK)) {
			mod--;
		}
		return mod;
	}
	protected int sizeModifier() {
		int mod = 0;
		if (getGameObject().hasThisAttribute(Constants.SHRINK)) {
			mod--;
		}
		return mod;
	}

	public Speed getAttackSpeed() {
		return new Speed(getFaceAttributeInteger("attack_speed"),speedModifier());
	}

	public Strength getStrength() {
		Strength strength = new Strength(getFaceAttributeString("strength"));
		int mod = sizeModifier();
		if (mod<0) {
			strength.moveRedToMaximum();
		}
		strength.modify(mod);
		if (strength.getChar()!="T" && getGameObject().hasThisAttribute(Constants.STRONG_MF)) {
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
		return getFaceAttributeString("magic_type");
	}

	public int getManeuverCombatBox() {
		CombatWrapper combat = new CombatWrapper(getGameObject());
		return combat.getCombatBox();
	}
	
	public int getAttackCombatBox() {
		CombatWrapper combat = new CombatWrapper(getGameObject());
		return combat.getCombatBox();
	}
	
	public boolean hasAnAttack() {
		return (getFaceAttributeString("strength")!=null && getFaceAttributeString("strength").length()>0)
				|| (getMagicType()!=null && getMagicType().length()>0);
	}

	public Strength getVulnerability() {
		Strength vul =  new Strength(getAttribute("this", "vulnerability"));
		vul.modify(sizeModifier());
		return vul;
	}
	
	public boolean isArmored() {
		return getGameObject().hasThisAttribute(Constants.ARMORED);
	}

	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs, BattleChit attacker, int box, Harm attackerHarm,int attackOrderPos) {
		Harm harm = new Harm(attackerHarm);
		Strength vulnerability = getVulnerability();
		if (!harm.getIgnoresArmor() && isArmored()) {
			harm.dampenSharpness();
			RealmLogging.logMessage(attacker.getGameObject().getName(),"Hits armor, and reduces sharpness: "+harm.toString());
		}
		Strength applied = harm.getAppliedStrength();
		if (applied.strongerOrEqualTo(vulnerability)) {
			// Dead monster!
			CombatWrapper combat = new CombatWrapper(getGameObject());
			combat.setKilledBy(attacker.getGameObject());
			return true;
		}
		return false;
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
	public void setHidden(boolean val) {
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
	
	public boolean isMistLike() {
		return getGameObject().hasThisAttribute("mist_like");
	}
	public boolean isAbsorbed() {
		GameObject heldBy = getGameObject().getHeldBy();
		return heldBy!=null && heldBy.hasThisAttribute("spell");
	}
}