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

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.*;

public class CharacterActionChitComponent extends StateChitComponent implements BattleChit,MagicChit,Comparable {
	public static final String ALTERNATE_ATTRIBUTES = "chit_alt"; // used when Spell or treasure changes attributes

	public static final String ACTION_CHIT_STATE_KEY = "chit_state";

	public static final String ACTION_CHIT_STATE_ACTIVE = "";
	public static final String ACTION_CHIT_STATE_ALERT = "A";
	public static final String ACTION_CHIT_STATE_FATIGUED = "F";
	public static final String ACTION_CHIT_STATE_COMMITTED = "S"; // committed to a spell, so S is for spell...
	public static final String ACTION_CHIT_STATE_WOUNDED = "W";
	public static final String ACTION_CHIT_STATE_COLOR_WHITE = "CW";
	public static final String ACTION_CHIT_STATE_COLOR_BLACK = "CB";
	public static final String ACTION_CHIT_STATE_COLOR_GRAY = "CY";
	public static final String ACTION_CHIT_STATE_COLOR_GOLD = "CG";
	public static final String ACTION_CHIT_STATE_COLOR_PURPLE = "CP";
	public static final String ACTION_CHIT_STATE_OUT_OF_PLAY = "X";

	// FACE UP
	public static final int ALERT_ID = 0;
	public static final int ACTIVE_ID = 1;
	public static final int FATIGUED_ID = 2;
	public static final int COMMITTED_ID = 3;
	public static final int WOUNDED_ID = 4; // this goes against the rules to be face up, but makes more sense here

	// FACE DOWN
	public static final int COLOR_WHITE_ID = 6;
	public static final int COLOR_BLACK_ID = 7;
	public static final int COLOR_GRAY_ID = 8;
	public static final int COLOR_GOLD_ID = 9;
	public static final int COLOR_PURPLE_ID = 10;
	public static final int OUT_OF_PLAY_ID = 11;

	private boolean usingAlteredAttributes; // a local variable ONLY (no need to persist this value)
	
	public CharacterActionChitComponent(GameObject obj) {
		super(obj);
		lightColor = MagicRealmColor.PEACH;
		darkColor = MagicRealmColor.WHITE;
	}

	public String asKey() {
		return getAction() + getMagicType() + getEffortAsterisks() + getChitAttribute("speed")+getStateId();
	}

	/**
	 * @return		true if the the chit attributes (ie., MAGIC IV2*) exactly match
	 */
	public boolean sameChitAttributes(MagicChit chit) {
		return asKey().equals(chit.asKey());
	}

	public int compareTo(Object o1) {
		int ret = 0;
		if (o1 instanceof CharacterActionChitComponent) {
			CharacterActionChitComponent other = (CharacterActionChitComponent) o1;

			// Then by action:  MOVE, DUCK, FIGHT, BERSERK, MAGIC
			ret = getActionSortOrder()-other.getActionSortOrder();
			if (ret == 0) {
				// Magic type (if any)
				ret = getMagicNumber() - other.getMagicNumber();
				if (ret == 0) {
					// Then by strength:  L,M,H,T
					int s1 = RealmUtility.convertMod(getMod());
					int s2 = RealmUtility.convertMod(other.getMod());
					ret = s1 - s2;
					if (ret == 0) {
						// Then by speed:  1,2,3,4,5,6
						ret = getSpeed().getNum() - other.getSpeed().getNum();
						if (ret == 0) {
							// Then by effort:  no asterisks, *, **
							ret = getEffortAsterisks() - other.getEffortAsterisks();
						}
					}
				}
			}
		}
		else {
			ret = super.compareTo(o1);
		}
		return ret;
	}

	public String getMod() {
		String mod;
		String action = getAction();
		if (action.equals("MAGIC")) {
			mod = getChitAttribute("magic");
		}
		else {
			mod = getChitAttribute("strength");
		}
		return mod;
	}

	public String getName() {
		return CHARACTER_CHIT;
	}

	public int getActionSortOrder() {
		String action = getAction();
		if (isMoveLock()) {
			return 0;
		}
		else if ("FLY".equals(action)) {
			return 1;
		}
		else if ("MOVE".equals(action)) {
			return 2;
		}
		else if (isFightAlert()) {
			return 3;
		}
		else if (isFightLock()) {
			return 4;
		}
		else if ("FIGHT".equals(action)) {
			return 5;
		}
		else if ("MAGIC".equals(action)) {
			return 6;
		}
		return 999;
	}
	public String getAction() {
		String actionChange = getGameObject().getThisAttribute("action_change");
		if (actionChange!=null) {
			usingAlteredAttributes = true;
			return actionChange;
		}
		return getChitAttribute("action").toUpperCase();
	}
	
	/**
	 * Used by ChitBinPanel to determine border color
	 */
	public Color getBorderColor() {
		String action = getAction().toUpperCase();
		if ("M/F".equals(action)) {
			return null;
		}
		else {
			if (isMove()) {
				return Color.blue;
			}
			else if (isFight() || isFightAlert()) {
				return Color.red;
			}
		}
		return null;
	}

	public boolean isMoveFight() {
		return "M/F".equals(getAction().toUpperCase());
	}
	public boolean isMove() {
		String action = getAction().toUpperCase();
		return "MOVE".equals(action)
				|| isMoveLock()
				|| "M/F".equals(action);
	}

	public boolean isFight() {
		String action = getAction().toUpperCase();
		return "FIGHT".equals(action)
				|| isFightLock()
				|| "M/F".equals(action);
	}
	
	public boolean isAnyEffort() {
		return getGameObject().hasThisAttribute(Constants.ANY_EFFORT);
	}
	
	public boolean isTreasureChit() {
		return getGameObject().hasThisAttribute(Constants.TREASURE_CHIT);
	}

	/**
	 * This is essentially the BERSERK chit, but since users may apply it to custom characters, it needs to be more generic.
	 */
	public boolean isFightAlert() {
		return getGameObject().hasThisAttribute("fight_alert");
	}
	public String getFightAlertVulnerability() {
		if (isFightAlert()) {
			return getStrength().toString();
		}
		return null;
	}
	public boolean isMoveLock() {
		return getGameObject().hasThisAttribute("move_lock");
	}
	public boolean isFightLock() {
		return getGameObject().hasThisAttribute("fight_lock");
	}

	public boolean isMagic() {
		return "MAGIC".equals(getAction().toUpperCase());
	}
	public boolean isFly() {
		return "FLY".equals(getAction().toUpperCase());
	}

	public boolean isEnchantable() {
		return isMagic() && getMagicNumber() < 6;
	}

	public Strength getStrength() {
		Strength strength;
		if (gameObject.hasThisAttribute("action_change_str") && !gameObject.hasAttribute(ALTERNATE_ATTRIBUTES, "strength")) {
			usingAlteredAttributes = true;
			strength = new Strength(gameObject.getThisAttribute("action_change_str"));
		}
		else {
			String val = getChitAttribute("strength");
			strength = new Strength(val);
		}
		if (isMove() || isFight()) {
			GameObject owner = getGameObject().getHeldBy();
			if (owner!=null) { // Might be null in the character builder app
				CharacterWrapper character = new CharacterWrapper(getGameObject().getHeldBy());
				if (strength.getChar()!="T" && character.getGameObject().hasThisAttribute(Constants.STRONG_MF)) {
					strength.modify(1);
				}
			}
		}
		return strength;
	}

	public Harm getHarm() {
		return new Harm(getStrength(), getSharpness());
	}

	public int getSharpness() {
		return 0;
	}

	public int getEffortAsterisks() {
		String effort = getChitAttribute("effort");
		try {
			Integer num = Integer.valueOf(effort);
			return num.intValue();
		}
		catch (NumberFormatException ex) {
		}
		return 0;
	}

	public Speed getSpeed() {
		Speed speed = new Speed(); // infinitely slow
		if (isMagic()) {
			speed = getMagicSpeed();
		}
		else if (isMove()) {
			speed = getMoveSpeed();
		}
		else if (isFight()) {
			speed = getAttackSpeed();
		}
		else if (isFly()) {
			speed = getFlySpeed();
		}
		else { // default
			speed = new Speed(getChitAttribute("speed"));
		}
		return speed;
	}

	public void paintComponent(Graphics g1) {
		usingAlteredAttributes = false;
		Graphics2D g = (Graphics2D) g1;
		int stateId = getStateId();
		if (isFaceDown() && getGameObject().getGameData() != null) { // character state chits are always face up in RealmSpeak
			setFacing(FACE_UP);
		}
		super.paintComponent(g);

		TextType tt;

		// Draw Image
		Composite old = g.getComposite();
		if (isFaceUp()) {
			AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
			g.setComposite(composite);
		}
		String iconName = gameObject.getThisAttribute("icon_type");
		String iconFolder = gameObject.getThisAttribute("icon_folder");
		if (iconName!=null && iconFolder!=null) {
			drawIcon(g,iconFolder,iconName,0.4);
		}
		g.setComposite(old);

		if (isFaceDown()) { // attention chits
			return;
		}

		// Embellish chit to indicate current state
		String textTypeName = "BOLD";
		int chitSize = getChitSize();
		Shape shape = getShape(0, 0, chitSize);
		g.setStroke(new BasicStroke(3));
		switch (stateId) {
			case ACTIVE_ID:
				// no embellishment
				break;
			case ALERT_ID:
				g.setColor(MagicRealmColor.CHIT_ALERTED);
				g.draw(shape);
				break;
			case FATIGUED_ID:
				g.setColor(MagicRealmColor.CHIT_FATIGUED);
				g.draw(shape);
				break;
			case WOUNDED_ID:
				g.setColor(MagicRealmColor.CHIT_WOUNDED);
				g.draw(shape);
				break;
			case COLOR_WHITE_ID:
			case COLOR_BLACK_ID:
			case COLOR_GRAY_ID:
			case COLOR_GOLD_ID:
			case COLOR_PURPLE_ID:
				textTypeName = "BOLD";
				if (stateId==COLOR_BLACK_ID) {
					textTypeName = "WHITE_NOTE";
				}
				else if (stateId==COLOR_PURPLE_ID) {
					textTypeName = "WHITE_NOTE";
				}
				ColorMagic cm = getColorMagic();
				cm.setInfinite(true);
				ImageIcon icon = cm.getIcon();
				g.drawImage(cm.getIcon().getImage(), 0, 0, chitSize, chitSize, 0, 0, icon.getIconWidth(), icon.getIconHeight(), null);
				break;
			case COMMITTED_ID:
				g.setColor(MagicRealmColor.CHIT_COMMITTED);
				g.draw(shape);
				break;
			case OUT_OF_PLAY_ID:
				g.setColor(Color.lightGray);
				g.fill(shape);
				break;
		}

		// Draw Information
		int y = 12;
		String action = getAction();
		if ("M/F".equals(action)) {
			y += 6;
			tt = new TextType("MOVE", getChitSize() - 4, "BOLD_BLUE");
			tt.draw(g, 2, y-tt.getHeight(g), Alignment.Center);
			tt = new TextType("FIGHT", getChitSize() - 4, "BOLD_BLUE");
			tt.draw(g, 2, y, Alignment.Center);
		}
		else {
			tt = new TextType(action.toUpperCase(), getChitSize() - 4, textTypeName);
			tt.draw(g, 2, y, Alignment.Center);
		}

		y += tt.getHeight(g);

		String effort = getChitAttribute("effort");
		try {
			Integer num = Integer.valueOf(effort);
			effort = StringUtilities.getRepeatString("*", num.intValue());
		}
		catch (NumberFormatException ex) {
			effort = "";
		}

		Speed chitSpeed = getSpeed();
		String speed = String.valueOf(chitSpeed.getNum());
		if (chitSpeed.getNum() == 0) {
			speed = getChitAttribute("speed") + "(0)"; // to indicate its actual speed
		}

		String mod;
		if (action.equals("MAGIC")) {
			mod = getMagicType();
		}
		else {
			mod = getStrength().toString();
		}
		//  Need to look at usingAlteredAttributes to determine what color to show the attributes
		// I'd like them to be blue, or red, or something that indicates they are "modified"
		if (usingAlteredAttributes) {
			textTypeName = "BOLD_BLUE"; // indicates an altered state
		}
		if (!"neg".equals(mod)) {
			tt = new TextType(mod + speed + effort, getChitSize() - 4, textTypeName);
			tt.draw(g, 2, y, Alignment.Center);
		}
		else {
			tt = new TextType(effort, getChitSize() - 4, textTypeName);
			tt.draw(g, 2, y, Alignment.Center);
		}
		
		if (getGameObject().hasThisAttribute(Constants.UNPLAYABLE)) {
			tt = new TextType("No Play",getChitSize() - 4,"TITLE_RED");
			tt.draw(g,2,1,Alignment.Center);
		}
	}

	/**
	 * This checks the alternate_attributes before checking "this"
	 */
	private String getChitAttribute(String key) {
		String ret = gameObject.getAttribute(ALTERNATE_ATTRIBUTES, key);
		if (ret == null) {
			ret = getAttribute("this", key);
		}
		else {
			usingAlteredAttributes = true;
		}
		return ret;
	}

	public boolean isColor() {
		return getColorMagic() != null;
	}
	
	
	public ColorMagic getEnchantedColorMagic() {
		int num = getMagicNumber();
		switch (num) {
			case 1:
				return new ColorMagic(ColorMagic.WHITE, false);
			case 2:
				return new ColorMagic(ColorMagic.GRAY, false);
			case 3:
				return new ColorMagic(ColorMagic.GOLD, false);
			case 4:
				return new ColorMagic(ColorMagic.PURPLE, false);
			case 5:
				return new ColorMagic(ColorMagic.BLACK, false);
		}
		return null;
	}

	public ColorMagic getColorMagic() {
		if (getGameObject().hasThisAttribute(Constants.MOD_COLOR_SOURCE)) {
			return ColorMagic.makeColorMagic(getGameObject().getThisAttribute(Constants.MOD_COLOR_SOURCE),false);
		}
		int stateId = getStateId();
		switch (stateId) {
			case COLOR_WHITE_ID:
				return new ColorMagic(ColorMagic.WHITE, false);
			case COLOR_BLACK_ID:
				return new ColorMagic(ColorMagic.BLACK, false);
			case COLOR_GRAY_ID:
				return new ColorMagic(ColorMagic.GRAY, false);
			case COLOR_GOLD_ID:
				return new ColorMagic(ColorMagic.GOLD, false);
			case COLOR_PURPLE_ID:
				return new ColorMagic(ColorMagic.PURPLE, false);
		}
		return null;
	}

	// BattleChit Interface
	public boolean targets(BattleChit chit) {
		return false;
	}

	public Integer getLength() {
		return null; // action chits never have inherent length
	}

	public Speed getMoveSpeed() {
		if (isMove()) {
			if (!gameObject.hasAttribute(ALTERNATE_ATTRIBUTES, "speed")) {
				// speedChange is ignored if speed is already altered by a treasure
				int speedChange = getGameObject().getThisInt("move_speed_change");
				if (speedChange>0) {
					usingAlteredAttributes = true;
					return new Speed(speedChange);
				}
			}
			return new Speed(getChitAttribute("speed"));
		}
		else if (isFly()) {
			return getFlySpeed();
		}
		return null;
	}
	
	public Speed getFlySpeed() {
		return new Speed(getChitAttribute("speed"));
	}

	public boolean hasAnAttack() {
		return getAttackSpeed()!=null;
	}
	
	public Speed getAttackSpeed() {
		if (isFight()) {
			return new Speed(getChitAttribute("speed"));
		}
		return null;
	}

	public Speed getMagicSpeed() {
		String action = getChitAttribute("action").toUpperCase();
		if ("MAGIC".equals(action)) {
			// alerted magic is always speed zero
			if (isAlerted()) {
				return new Speed(0); // wheee!
			}
			return new Speed(getChitAttribute("speed"));
		}
		return null;
	}

	public String getMagicType() {
		String changeType = getGameObject().getThisAttribute(Constants.MAGIC_CHANGE);
		if (changeType==null) {
			return getChitAttribute("magic");
		}
		usingAlteredAttributes = true;
		return changeType;
	}
	
	public boolean compatibleWith(ColorMagic cm) {
		return getMagicNumber()==cm.getColorNumber();
	}
	
	public int getMagicNumber() {
		return getMagicNumber(getMagicType());
	}

	public static int getMagicNumber(String type) {
		if ("I".equals(type)) {
			return 1;
		}
		else if ("II".equals(type)) {
			return 2;
		}
		else if ("III".equals(type)) {
			return 3;
		}
		else if ("IV".equals(type)) {
			return 4;
		}
		else if ("V".equals(type)) {
			return 5;
		}
		else if ("VI".equals(type)) {
			return 6;
		}
		else if ("VII".equals(type)) {
			return 7;
		}
		else if ("VIII".equals(type)) {
			return 8;
		}
//		throw new IllegalStateException("Not a MAGIC chit");
		return 0; // NOT A MAGIC CHIT
	}
	
	public ArrayList<Integer> getEnchantableNumbers() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(getMagicNumber());
		return list;
	}
	public void enchant() {
		enchant(getMagicNumber());
	}
	public void enchant(int magicNumber) {
		switch (magicNumber) {
			case 1:
				setState(ACTION_CHIT_STATE_COLOR_WHITE);
				break;
			case 2:
				setState(ACTION_CHIT_STATE_COLOR_GRAY);
				break;
			case 3:
				setState(ACTION_CHIT_STATE_COLOR_GOLD);
				break;
			case 4:
				setState(ACTION_CHIT_STATE_COLOR_PURPLE);
				break;
			case 5:
				setState(ACTION_CHIT_STATE_COLOR_BLACK);
				break;
			default:
				throw new IllegalStateException("Cannot enchant this chit!");
		}
	}

	public void makeActive() {
		setState(ACTION_CHIT_STATE_ACTIVE);
	}

	public void makeFatigued() {
		if (getEffortAsterisks()>0) { // CANNOT fatigue non-effort chits
			setState(ACTION_CHIT_STATE_FATIGUED);
		}
	}

	public void makeWounded() {
		setState(ACTION_CHIT_STATE_WOUNDED);
	}

	public void makeAlerted() {
		setState(ACTION_CHIT_STATE_ALERT);
	}

	public boolean isActive() {
		return getStateId() == ACTIVE_ID;
	}

	public boolean isFatigued() {
		return getStateId() == FATIGUED_ID;
	}

	public boolean isWounded() {
		return getStateId() == WOUNDED_ID;
	}

	public boolean isAlerted() {
		return getStateId() == ALERT_ID;
	}

	private void setState(String stateName) {
		gameObject.setThisAttribute(ACTION_CHIT_STATE_KEY, stateName);
	}

	public int getStateId() {
		String val = gameObject.getThisAttribute(ACTION_CHIT_STATE_KEY);
		if (ACTION_CHIT_STATE_ALERT.equals(val)) {
			return ALERT_ID;
		}
		else if (ACTION_CHIT_STATE_FATIGUED.equals(val)) {
			return FATIGUED_ID;
		}
		else if (ACTION_CHIT_STATE_WOUNDED.equals(val)) {
			return WOUNDED_ID;
		}
		else if (ACTION_CHIT_STATE_COMMITTED.equals(val)) {
			return COMMITTED_ID;
		}
		else if (ACTION_CHIT_STATE_COLOR_WHITE.equals(val)) {
			return COLOR_WHITE_ID;
		}
		else if (ACTION_CHIT_STATE_COLOR_BLACK.equals(val)) {
			return COLOR_BLACK_ID;
		}
		else if (ACTION_CHIT_STATE_COLOR_GRAY.equals(val)) {
			return COLOR_GRAY_ID;
		}
		else if (ACTION_CHIT_STATE_COLOR_GOLD.equals(val)) {
			return COLOR_GOLD_ID;
		}
		else if (ACTION_CHIT_STATE_COLOR_PURPLE.equals(val)) {
			return COLOR_PURPLE_ID;
		}
		else if (ACTION_CHIT_STATE_OUT_OF_PLAY.equals(val)) {
			return OUT_OF_PLAY_ID;
		}
		return ACTIVE_ID;
	}

	public void setAlternateStrength(Strength alt) {
		// Make sure that there isn't something already applied that is stronger than alt
		Strength current = new Strength(gameObject.getAttribute(ALTERNATE_ATTRIBUTES, "strength"));
		if (alt.strongerThan(current)) {
			gameObject.setAttribute(ALTERNATE_ATTRIBUTES, "strength", alt.toString());
		}
	}

	public void removeAlternateStrength() {
		if (gameObject.hasAttribute(ALTERNATE_ATTRIBUTES, "strength")) {
			gameObject.removeAttribute(ALTERNATE_ATTRIBUTES, "strength");
		}
	}

	public void setAlternateSpeed(Speed alt) {
		// Make sure that there isn't something already applied that is faster than alt
		Speed current = new Speed(gameObject.getAttribute(ALTERNATE_ATTRIBUTES, "speed"));
		if (alt.fasterThan(current)) {
			gameObject.setAttribute(ALTERNATE_ATTRIBUTES, "speed", alt.getNum());
		}
	}

	public void removeAlternateSpeed() {
		if (gameObject.hasAttribute(ALTERNATE_ATTRIBUTES, "speed")) {
			gameObject.removeAttribute(ALTERNATE_ATTRIBUTES, "speed");
		}
	}

	public int getManeuverCombatBox() {
		if (isMove()) {
			CombatWrapper combat = new CombatWrapper(getGameObject());
			return combat.getCombatBox();
		}
		return 0;
	}

	public int getAttackCombatBox() {
		if (isFight()) {
			CombatWrapper combat = new CombatWrapper(getGameObject());
			return combat.getCombatBox();
		}
		return 0;
	}

	public boolean applyHit(GameWrapper game,HostPrefWrapper hostPrefs, BattleChit attacker, int box, Harm attackerHarm,int attackOrderPos) {
		// Nothing happens here - its all at the CharacterChitComponent level
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
	
	public boolean expireMoveSpells() {
		boolean expiredOne = false;
		SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
		for (SpellWrapper spell:sm.getAffectingSpells(getGameObject())) {
			if (spell.isMoveSpell()) {
				spell.expireSpell();
				expiredOne = true;
			}
		}
		return expiredOne;
	}
	public String getShortName() {
		StringBuffer sb = new StringBuffer();
		sb.append(getAction());
		sb.append(" ");
		if (isMagic()) {
			sb.append(getMagicType());
		}
		else {
			sb.append(getStrength().getChar());
		}
		sb.append(getSpeed().getNum());
		for (int i=0;i<getEffortAsterisks();i++) {
			sb.append("*");
		}
		return sb.toString();
	}
}