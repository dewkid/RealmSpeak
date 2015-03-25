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
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.quest.Quest;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;

public abstract class RealmComponent extends JComponent implements Comparable {
	
	public static final int DISPLAY_STYLE_CLASSIC = 0;
	public static final int DISPLAY_STYLE_COLOR = 1;
	public static final int DISPLAY_STYLE_FRENZEL = 2;
	
	public static int displayStyle = DISPLAY_STYLE_COLOR;
	
	public static boolean isDisplayStyleColor() {
		return displayStyle==DISPLAY_STYLE_COLOR;
	}
	public static boolean isDisplayStyleFrenzel() {
		return displayStyle==DISPLAY_STYLE_FRENZEL;
	}
	public static boolean useColorIcons() {
		return displayStyle==DISPLAY_STYLE_COLOR || displayStyle==DISPLAY_STYLE_FRENZEL;
	}
	
	public static final String REALMCOMPONENT_BLOCK = "_RCB_";

	private static Hashtable dataComponentHash = null;
	
	public static final String REALMCOMPONENT_MASTER = "RealmComponent Master Object";
	public static final String TARGET_COUNTER = "tc"; // used to increment the target index

	public static final String OWNER_ID = "owner_id"; // game object id of owner
	public static final String OWNER_TERM_OF_HIRE = "owner_term";
	private static final String TARGET_ID = "target_id"; // game object id of target
	private static final String TARGET_INDEX = "targ_idx"; // a counter that indicates who targets who first (needed for War!)

	// Chit identifiers
	public static final String CHARACTER = "character";
	public static final String ANIMAL = "animal";
	public static final String MONSTER = "monster";
	public static final String MONSTER_PART = "part";
	public static final String RED_SPECIAL = "red_special";
	public static final String GOLD_SPECIAL = "gold_special";
	public static final String TILE = "tile";
	public static final String TILE_TYPE = "tile_type";
	public static final String WARNING = "warning";
	public static final String SOUND = "sound";
	public static final String ARMOR = "armor";
	public static final String WEAPON = "weapon";
	public static final String TREASURE_LOCATION = "treasure_location";
	public static final String CACHE_CHIT = "cache_chit";
	public static final String CHARACTER_CHIT = "character_chit";
	public static final String PHASE_CHIT = "phase_chit";
	public static final String FLY_CHIT = "fly_chit";
	public static final String NATIVE = "native";
	public static final String HORSE = "horse";
	public static final String DWELLING = "dwelling";
	public static final String TREASURE = "treasure";
	public static final String BOON = "boon";
	public static final String SPELL = "spell";
	public static final String FAMILIAR = "familiar";
	public static final String PHANTASM = "phantasm";
	
	// Expansion ONE chits
	public static final String GUILD = "guild";
	public static final String TRAVELER = "traveler";
	public static final String GATE = "gate";
	public static final String HAMLET = "hamlet";
	public static final String EVENT = "event";
	public static final String GOLD = "gold";
	public static final String MINOR_TREASURE_LOCATION = "minor_tl";
	
	public static final String QUEST = "quest";

	// Other identifiers
	public static final String NATIVE_HORSE = "native_horse";
	public static final String TREASURE_WITHIN_TREASURE = "treasure_within_treasure";

	public static boolean fullDetail = true; // by default, all objects should be detailed

	protected GameObject gameObject;
	protected boolean shadow = true;

	protected boolean selected = false;

	protected RealmComponent(GameObject obj) {
		this.gameObject = obj;
	}

	/**
	 * If this does what I want, larger objects will sort to the bottom.
	 */
	public int compareTo(Object o1) {
		int ret = 0;
		if (o1 instanceof RealmComponent) {
			RealmComponent rc = (RealmComponent) o1;
			ret = rc.getSortOrder() - getSortOrder();
		}
		return ret;
	}

	protected int getSortOrder() {
		Dimension d = getSize();
		return d.width + d.height;
	}

	public Collection getHold() {
		return gameObject.getHold();
	}
	
	public RealmComponent getHeldBy() {
		GameObject heldBy = gameObject.getHeldBy();
		if (heldBy!=null) {
			return RealmComponent.getRealmComponent(heldBy);
		}
		return null;
	}

	public abstract String getName();

	public String toString() {
		return gameObject.getName();
	}

	public void useShadow(boolean val) {
		shadow = val;
	}

	public String getClassName() {
		String val = getClass().getName();
		int lastDot = val.lastIndexOf(".");
		return val.substring(lastDot + 1);
	}

	/**
	 * Flips the game object.  By default, this implementation does nothing.
	 */
	public void flip() {
	}

	/**
	 * Convenience method for accessing gameObject attributes.  Returns "" if null.
	 */
	protected String getAttribute(String group, String key) {
		String val = gameObject.getAttribute(group, key);
		return val == null ? "" : val;
	}
	protected int getAttributeInt(String group, String key) {
		return gameObject.getAttributeInt(group,key);
	}
	protected boolean hasAttribute(String group, String key) {
		return gameObject.hasAttribute(group,key);
	}

	public GameObject getGameObject() {
		return gameObject;
	}
	
	public Dimension getSize() {
		return super.getSize();
	}

	public boolean equals(Object o1) {
		if (o1 instanceof RealmComponent) {
			RealmComponent rc = (RealmComponent) o1;
			if (gameObject==null && rc.gameObject==null) {
				return true;
			}
			if (gameObject==null || rc.gameObject==null) {
				return false;
			}
			if (gameObject.getId() == rc.gameObject.getId()) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return gameObject.hashCode();
	}

	public boolean isChit() {
		return (this instanceof ChitComponent);
	}

	public boolean isBattleChit() {
		return (this instanceof BattleChit);
	}

	public boolean isCharacter() {
		return (this instanceof CharacterChitComponent);
	}
	
	public boolean isNativeLeader() {
		String rank = getGameObject().getThisAttribute("rank");
		return (this instanceof NativeChitComponent) && "HQ".equals(rank);
	}
	public boolean isTransformedNativeLeader() {
		String rank = getGameObject().getAttribute("this_h","rank");
		return (this instanceof NativeChitComponent) && "HQ".equals(rank);
	}
	
	public boolean isHiredLeader() {
		return isNativeLeader() && CharacterWrapper.hasPlayerBlock(getGameObject());
	}

	public boolean isControlledMonster() {
		return isMonster() && CharacterWrapper.hasPlayerBlock(getGameObject());
	}
	
	public boolean isControlledNative() {
		return isNative() && CharacterWrapper.hasPlayerBlock(getGameObject());
	}
	
	public boolean isAnyLeader() {
		return isCharacter() || isNativeLeader() || isControlledMonster() || isControlledNative();
	}
	
	public boolean isPlayerControlledLeader() {
		return isCharacter() || isHiredLeader() || isControlledMonster() || isControlledNative();
	}
	public boolean canSpy() {
		return isPlayerControlledLeader() || isFamiliar();
	}
	
	public boolean isMagicChit() {
		return (this instanceof MagicChit);
	}
	
	public boolean isEnchanted() {
		return isMagicChit() && ((MagicChit)this).isColor();
	}

	public boolean isActionChit() {
		return (this instanceof CharacterActionChitComponent);
	}

	public boolean isWeapon() {
		return (this instanceof WeaponChitComponent);
	}

	public boolean isTreasure() {
		return (this instanceof TreasureCardComponent);
	}
	
	public boolean isPhaseChit() {
		return (this instanceof PhaseChitComponent);
	}
	
	public boolean isFlyChit() {
		return (this instanceof FlyChitComponent);
	}

	public boolean isSpell() {
		return (this instanceof SpellCardComponent);
	}

	public boolean isHorse() {
		return (this instanceof SteedChitComponent);
	}
	
	public boolean isCard() {
		return (this instanceof CardComponent);
	}
	
	public boolean isNativeHorse() {
		return (this instanceof NativeSteedChitComponent);
	}

	public boolean isArmor() {
		return (this instanceof ArmorChitComponent);
	}
	
	public boolean isArmorCard() {
		return isTreasure() && getGameObject().hasThisAttribute("armor_row");
	}

	public boolean isTile() {
		return (this instanceof TileComponent);
	}
	
	public boolean isCompanion() {
		return getGameObject().hasThisAttribute("companion");
	}

	public boolean isMonster() {
		return (this instanceof MonsterChitComponent) && !(this instanceof MonsterPartChitComponent);
	}
	
	public boolean isTraveler() {
		return (this instanceof TravelerChitComponent);
	}
	
	public boolean isCombativeTraveler() {
		return isTraveler() && ((TravelerChitComponent)this).hasAnAttack() && getOwnerId()!=null;
	}
	
	public boolean isFamiliar() {
		return (this instanceof FamiliarChitComponent);
	}
	
	public boolean isPhantasm() {
		return (this instanceof PhantasmChitComponent);
	}
	
	public boolean isMistLike() {
		return false;
	}
	
	public boolean hasMagicProtection() {
		CharacterWrapper character = new CharacterWrapper(getGameObject());
		return character.hasMagicProtection();
	}
	
	public boolean isTransformAnimal() {
		return (this instanceof TransformChitComponent);
	}

	public boolean isMonsterPart() {
		return (this instanceof MonsterPartChitComponent);
	}

	public boolean isDwelling() {
		return (this instanceof DwellingChitComponent);
	}
	
	public boolean isStateChit() {
		return (this instanceof StateChitComponent);
	}
	
	public boolean isCollectibleThing() {
		return isTreasure() || isArmor() || isWeapon() || isGoldSpecial();
	}

	public boolean isNative() {
		return (this instanceof NativeChitComponent);
	}
	
	public boolean isGoldSpecial() {
		return (this instanceof GoldSpecialChitComponent);
	}
	
	public boolean isRedSpecial() {
		return (this instanceof RedSpecialChitComponent);
	}
	
	public boolean isBoon() {
		return (this instanceof BoonChitComponent);
	}
	
	public boolean isVisitor() {
		return gameObject.hasThisAttribute(Constants.VISITOR);
	}

	public boolean isTreasureLocation() {
		return (this instanceof TreasureLocationChitComponent);
	}
	
	public boolean isCacheChit() {
		return (this instanceof CacheChitComponent);
	}
	
	public boolean isGate() {
		return (this instanceof GateChitComponent);
	}

	public boolean isGuild() {
		return (this instanceof GuildChitComponent);
	}
	
	public boolean isMinorCharacter() {
		return (this instanceof MinorCharacterChitComponent);
	}

	public boolean isActivated() {
		return gameObject.hasThisAttribute(Constants.ACTIVATED);
	}

	public void setActivated(boolean val) {
		if (val) {
			gameObject.setThisAttribute(Constants.ACTIVATED);
		}
		else {
			gameObject.removeThisAttribute(Constants.ACTIVATED);
		}
	}

	public boolean isItem() {
		return isWeapon() || isArmor() || isTreasure() || isHorse() || isNativeHorse();
	}

	public Integer getPacifyTypeFor(CharacterWrapper character) {
		if (!character.isCharacter()) {
			character = character.getHiringCharacter();
			if (character==null) { // Happens in the battle builder
				return null;
			}
		}
			
		Integer pacifyType = null;
		ArrayList list = getGameObject().getThisAttributeList("pacifyBlocks");
		if (list!=null) {
			String testId = character.getGameObject().getStringId();
			for (Iterator i=list.iterator();i.hasNext();) {
				String pacifyBlock = (String)i.next();
				String charId = getGameObject().getAttribute(pacifyBlock,"pacifyChar");
				if (charId.equals(testId)) {
					pacifyType = getGameObject().getInt(pacifyBlock,"pacifyType");
				}
			}
		}
		// Check character for automatic pacification (due to item or character attribute!)
		if (isMonster()) {
			ArrayList<GameObject> invWithPacify = character.getAllActiveInventoryThisKeyAndValue("pacifymonster",null);
			if (character.getGameObject().hasThisAttribute("pacifymonster")) {
				invWithPacify.add(character.getGameObject());
			}
			for (GameObject test:invWithPacify) {
				ArrayList monsters = test.getThisAttributeList("pacifymonster");
				if (monsters.contains(getGameObject().getName())) {
					int testPacify = test.getThisInt("pacifyType");
					if (pacifyType==null || testPacify>pacifyType) { // use the BEST one
						pacifyType = testPacify;
					}
				}
			}
		}
		return pacifyType;
	}
	
	public SpellWrapper getPacificationSpell(CharacterWrapper character) {
		ArrayList list = getGameObject().getThisAttributeList("pacifyBlocks");
		if (list!=null) {
			String testId = character.getGameObject().getStringId();
			for (Iterator i=list.iterator();i.hasNext();) {
				String pacifyBlock = (String)i.next();
				String charId = getGameObject().getAttribute(pacifyBlock,"pacifyChar");
				if (charId.equals(testId)) {
					GameObject theSpell = getGameObject().getGameData().getGameObject(Long.valueOf(pacifyBlock.substring(6)));
					return new SpellWrapper(theSpell);
				}
			}
		}
		return null;
	}
	
	public boolean isPacifiedBy(CharacterWrapper character) {
		return getPacifyTypeFor(character)!=null;
	}
	
	public void setOwner(RealmComponent comp) {
		gameObject.setAttribute(REALMCOMPONENT_BLOCK,OWNER_ID, String.valueOf(comp.gameObject.getId()));
	}
	public void clearOwner() {
		if (!isCharacter() && gameObject.hasAttribute(REALMCOMPONENT_BLOCK,OWNER_ID)) {
			gameObject.removeAttribute(REALMCOMPONENT_BLOCK,OWNER_ID);
			gameObject.removeAttribute(REALMCOMPONENT_BLOCK,OWNER_TERM_OF_HIRE);
		}
	}
	public static void clearOwner(GameObject go) {
		go.removeAttribute(REALMCOMPONENT_BLOCK,OWNER_ID);
		go.removeAttribute(REALMCOMPONENT_BLOCK,OWNER_TERM_OF_HIRE);
	}

	public String getOwnerId() {
		return gameObject.getAttribute(REALMCOMPONENT_BLOCK,OWNER_ID);
	}

	public RealmComponent getOwner() {
		String ownerId = getOwnerId();
		if (ownerId!=null) {
			return RealmComponent.getRealmComponentFromId(gameObject.getGameData(), ownerId);
		}
		return null;
	}

	public boolean ownedBy(RealmComponent comp) {
		RealmComponent owner = getOwner();
		return owner != null && owner.equals(comp);
	}
	
	public void decrementTermOfHire(int val) {
		if (!gameObject.hasThisAttribute("companion")) {
			addTermOfHire(-val);
		}
	}
	
	public void addTermOfHire(int val) {
		int currentTerm = getTermOfHire();
		int newTerm = currentTerm+val;
		if (newTerm>0) {
			gameObject.setAttribute(REALMCOMPONENT_BLOCK,OWNER_TERM_OF_HIRE,newTerm);
		}
		else {
			gameObject.removeAttribute(REALMCOMPONENT_BLOCK,OWNER_TERM_OF_HIRE);
		}
	}
	
	public int getTermOfHire() {
		return gameObject.getInt(REALMCOMPONENT_BLOCK,OWNER_TERM_OF_HIRE);
	}

	/**
	 * Set the primary target
	 */
	public void setTarget(RealmComponent target) {
		gameObject.setAttribute(REALMCOMPONENT_BLOCK,TARGET_ID, target.gameObject.getStringId());
		int ti = nextTargetIndex(gameObject.getGameData());
		gameObject.setAttribute(REALMCOMPONENT_BLOCK,TARGET_INDEX,ti);
		CombatWrapper combat = new CombatWrapper(target.getGameObject());
		combat.addAttacker(getGameObject());
	}
	
	/**
	 * @return		A number that indicates a point in time that the target was assigned.  Lower numbers
	 * 				indicate earlier assignments.
	 */
	public int getTargetIndex() {
		return gameObject.getInt(REALMCOMPONENT_BLOCK,TARGET_INDEX);
	}

	/**
	 * Get the primary target
	 */
	public RealmComponent getTarget() {
		return RealmComponent.getRealmComponentFromId(gameObject.getGameData(), gameObject.getAttribute(REALMCOMPONENT_BLOCK,TARGET_ID));
	}

	/**
	 * Clear primary target
	 */
	public void clearTarget() {
		RealmComponent target = getTarget();
		if (target!=null) {
			gameObject.removeAttribute(REALMCOMPONENT_BLOCK,TARGET_ID);
			gameObject.removeAttribute(REALMCOMPONENT_BLOCK,TARGET_INDEX);
			CombatWrapper combat = new CombatWrapper(target.getGameObject());
			combat.removeAttacker(getGameObject());
			
			// Seems like a RED-side-up T Monster should flip back to light when the target is cleared...
			if (isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)this;
				if (monster.isPinningOpponent()) {
					monster.flip();
				}
			}
		}
	}

	public boolean isDenizen() {
		return (isMonster() || isNative()) && !gameObject.hasAttribute(REALMCOMPONENT_BLOCK,OWNER_ID); // no owner, then must be denizen
	}

	public boolean targeting(RealmComponent comp) {
		String val = gameObject.getAttribute(REALMCOMPONENT_BLOCK,TARGET_ID);
		if (val != null) {
			if (val.equals(comp.gameObject.getStringId())) {
				return true;
			}
		}
		return false;
	}

	public boolean isAssigned() {
		return gameObject.hasAttribute(REALMCOMPONENT_BLOCK,TARGET_ID);
	}

	public boolean hasHorse() {
		return getHorse() != null;
	}
	
	public BattleHorse getHorseIncludeDead() { // gets the horse, even if it is dead
		for (Iterator i = gameObject.getHold().iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc instanceof BattleHorse) {
				return (BattleHorse) rc;
			}
		}
		return null;
	}

	public BattleHorse getHorse() {
		return getHorse(true,-1);
	}
	public BattleHorse getHorse(int attackOrderPos) {
		return getHorse(true,attackOrderPos);
	}
	public BattleHorse getHorse(boolean checkLocation) {
		return getHorse(checkLocation,-1);
	}
	public BattleHorse getHorse(boolean checkLocation,int attackOrderPos) {
		for (Iterator i = gameObject.getHold().iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc instanceof BattleHorse) {
				BattleHorse bh = (BattleHorse)rc;
				CombatWrapper combatHorse = new CombatWrapper(rc.getGameObject());
				if (!bh.isDead() || combatHorse.getHitByOrderNumber()==attackOrderPos) { // horse must not be dead! (or simultaneous)
					if (checkLocation) {
						// Make sure not in a cave!
						TileLocation tl = getCurrentLocation();
						if (tl!=null && tl.hasClearing() && tl.clearing.isCave()) {
							// No horse can be played in a cave, so they are "non-existant"
							return null;
						}
					}
					if (isNative()) {
						// Make sure native is NOT transformed
						if (getGameObject().hasAttributeBlock("this_h")) {
							return null;
						}
						// Make sure horse is not a SteedChitComponent, otherwise keep searching
						if (rc.isHorse()) {
							continue;
						}
					}
					return (BattleHorse) rc;
				}
				break;
			}
		}
		return null;
	}

	/**
	 * Default for RealmComponent is false.  Override this method to change logic.
	 */
	public boolean isHidden() {
		return false;
	}

	/**
	 * Default for RealmComponent does nothing.  Override this method to change logic.
	 */
	public void setHidden(boolean val) {
		// default does nothing
	}

	public ImageIcon getIcon() {
		return new ImageIcon(getImage());
	}
	public ImageIcon getFaceUpIcon() {
		return new ImageIcon(getFaceUpImage());
	}

	public abstract Dimension getComponentSize();

	public Image getImage() {
		Dimension size = getComponentSize();
		BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
		paintComponent(image.getGraphics());
		return image;
	}
	public Image getFaceUpImage() {
		return getImage();
	}
	
	public ImageIcon getNotesIcon() {
		return getMediumIcon();
	}

	public ImageIcon getMediumIcon() {
		return new ImageIcon(getMediumImage());
	}

	public Image getMediumImage() {
		return getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
	}
	
	public Image getPhaseImage() {
		return getImage().getScaledInstance(32,32, Image.SCALE_DEFAULT);
	}

	public ImageIcon getSmallIcon() {
		return new ImageIcon(getSmallImage());
	}

	public Image getSmallImage() {
		return getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT);
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (selected) {
			Color edgeColor = Color.blue;
			Color mainColor = Color.cyan;
			Dimension size = getSize();
			for (int n = 0; n < 4; n++) {
				Color color = GraphicsUtil.convertColor(edgeColor, mainColor, (n * 100) / 4);
				g.setColor(color);
				g.drawRect(n, n, size.width - (n << 1), size.height - (n << 1));
			}
		}
	}

	/////////////////////////////////////////////////////////////
	/// STATIC METHODS

	/**
	 * Takes the GameObject, and applies the appropriate GameObjectComponent wrapper class.
	 */
	private static RealmComponent createRealmComponent(GameObject obj,boolean cache) {
		RealmComponent comp = null;
		if (obj.hasThisAttribute(FAMILIAR)) {
			comp = new FamiliarChitComponent(obj);
		}
		else if (obj.hasThisAttribute(PHANTASM)) {
			comp = new PhantasmChitComponent(obj);
		}
		else if (obj.hasThisAttribute(TREASURE)) {
			comp = new TreasureCardComponent(obj);
		}
		else if (obj.hasThisAttribute(SPELL)) {
			comp = new SpellCardComponent(obj);
		}
		else if (obj.hasThisAttribute(QUEST)) {
			comp = new QuestCardComponent(obj);
		}
		else if (obj.hasThisAttribute(BOON)) {
			comp = new BoonChitComponent(obj);
		}
		else if (obj.hasThisAttribute(HORSE)) {
			if (obj.hasThisAttribute(NATIVE)) {
				comp = new NativeSteedChitComponent(obj);
			}
			else {
				comp = new SteedChitComponent(obj);
			}
		}
		else if (obj.hasThisAttribute(DWELLING) && obj.hasThisAttribute(TILE_TYPE)) {
			comp = new DwellingChitComponent(obj);
		}
		else if (obj.hasThisAttribute(NATIVE)) {
			if (!obj.hasThisAttribute(HORSE) && !obj.hasThisAttribute(DWELLING)) {
				comp = new NativeChitComponent(obj);
			}
		}
		else if (obj.hasThisAttribute(CHARACTER)) {
			comp = new CharacterChitComponent(obj);
			if (comp.getOwnerId()==null) {
				comp.setOwner(comp); // characters own themselves, always.
			}
		}
		else if (obj.hasThisAttribute(ANIMAL)) {
			comp = new TransformChitComponent(obj);
		}
		else if (obj.hasThisAttribute(MONSTER)) {
			if (obj.hasThisAttribute(MONSTER_PART)) {
				comp = new MonsterPartChitComponent(obj);
			}
			else {
				comp = new MonsterChitComponent(obj);
			}
		}
		else if (obj.hasThisAttribute(RED_SPECIAL)) {
			comp = new RedSpecialChitComponent(obj);
		}
		else if (obj.hasThisAttribute(SOUND)) {
			comp = new SoundChitComponent(obj);
		}
		else if (obj.hasThisAttribute(WARNING)) {
			comp = new WarningChitComponent(obj);
		}
		else if (obj.hasThisAttribute(CACHE_CHIT)) {
			comp = new CacheChitComponent(obj);
		}
		else if (obj.hasThisAttribute(TREASURE_LOCATION) && !obj.hasThisAttribute(TREASURE_WITHIN_TREASURE)) {
			comp = new TreasureLocationChitComponent(obj);
		}
		else if (obj.hasThisAttribute(MINOR_TREASURE_LOCATION)) {
			comp = new TreasureLocationChitComponent(obj);
		}
		else if (obj.hasThisAttribute(WEAPON)) {
			comp = new WeaponChitComponent(obj);
		}
		else if (obj.hasThisAttribute(ARMOR)) {
			comp = new ArmorChitComponent(obj);
		}
		else if (obj.hasThisAttribute(CHARACTER_CHIT)) {
			comp = new CharacterActionChitComponent(obj);
		}
		else if (obj.hasThisAttribute(PHASE_CHIT)) {
			comp = new PhaseChitComponent(obj);
		}
		else if (obj.hasThisAttribute(FLY_CHIT)) {
			comp = new FlyChitComponent(obj);
		}
		else if (obj.hasThisAttribute(TILE)) {
			comp = new TileComponent(obj);
		}
		else if (obj.hasThisAttribute(GOLD_SPECIAL)) {
			comp = new GoldSpecialChitComponent(obj);
		}
		else if (obj.hasThisAttribute(GOLD)) {
			comp = new GoldChitComponent(obj);
		}
		else if (obj.hasThisAttribute(TRAVELER)) {
			comp = new TravelerChitComponent(obj);
		}
		else if (obj.hasThisAttribute(EVENT)) {
			comp = new EventChitComponent(obj);
		}
		else if (obj.hasThisAttribute(GATE)) {
			comp = new GateChitComponent(obj);
		}
		else if (obj.hasThisAttribute(HAMLET)) {
			comp = new HamletChitComponent(obj);
		}
		else if (obj.hasThisAttribute(GUILD)) {
			comp = new GuildChitComponent(obj);
		}
		else if (obj.hasThisAttribute(Quest.QUEST_MINOR_CHARS)) {
			comp = new MinorCharacterChitComponent(obj);
		}
		if (comp != null && cache) {
			Hashtable componentHash = getComponentHash(obj);
			componentHash.put(new Long(obj.getId()), comp);
		}
		return comp;
	}
	
	private static RealmComponent getRealmComponentNoHash(GameObject go) {
		return createRealmComponent(go,false);
	}

	private static Hashtable getComponentHash(GameObject go) {
		return getComponentHash(go.getGameData());
	}
	
	public static void resetTargetIndex(GameData data) {
		Hashtable componentHash = getComponentHash(data);
		GameObject master = (GameObject)componentHash.get(REALMCOMPONENT_MASTER);
		master.setThisAttribute(TARGET_COUNTER,0);
	}

	private static int nextTargetIndex(GameData data) {
		Hashtable componentHash = getComponentHash(data);
		GameObject master = (GameObject)componentHash.get(REALMCOMPONENT_MASTER);
		int val = master.getThisInt(TARGET_COUNTER)+1;
		master.setThisAttribute(TARGET_COUNTER,val);
		return val;
	}
	/**
	 * @return			The Hashtable for the corresponding data object.  This is necessary to keep client and
	 * 					host data separated when handling these objects!
	 */
	private static Hashtable getComponentHash(GameData data) {
		if (dataComponentHash == null) {
			dataComponentHash = new Hashtable();
		}
		Long dataid = new Long(data.getDataId());
		Hashtable componentHash = (Hashtable) dataComponentHash.get(dataid);
		if (componentHash == null) {
			componentHash = new Hashtable();
			dataComponentHash.put(dataid, componentHash);
			
			// Make sure there is a master object to handle things like a target index counter
			GameObject master = data.getGameObjectByName(REALMCOMPONENT_MASTER);
			if (master==null) { // this shouldn't happen more than once per game!!
				master = data.createNewObject();
				master.setName(REALMCOMPONENT_MASTER);
				master.setThisAttribute(TARGET_COUNTER,"0");
			}
			componentHash.put(REALMCOMPONENT_MASTER,master);
		}
		return componentHash;
	}

	public static ArrayList getRealmComponents(Collection objects) {
		ArrayList list = new ArrayList();
		for (Iterator i = objects.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			list.add(RealmComponent.getRealmComponent(go));
		}
		return list;
	}

	/**
	 * This method will create a RealmComponent if one isn't found in the cache, unless the provided
	 * GameObject is unrecognizable.
	 * 
	 * @return		RealmComponent or null if not found.
	 */
	public static RealmComponent getRealmComponent(GameObject obj) {
		if (obj==null) {
			throw new IllegalArgumentException("Cannot getRealmComponent for null Object!");
		}
		if (obj.getGameData()==null) { // useful for returning an object for a game template
			return getRealmComponentNoHash(obj);
		}
		RealmComponent comp = null;
		Hashtable componentHash = getComponentHash(obj);
		if (componentHash != null) {
			comp = (RealmComponent) componentHash.get(new Long(obj.getId()));
		}
		if (comp == null) {
			comp = createRealmComponent(obj,true);
		}
		else {
			comp.setGameObject(obj);
		}
		return comp;
	}
	
	public static RealmComponent getRealmComponent(Object obj){
		return getRealmComponent((GameObject)obj);
	}

	public static BattleChit getBattleChit(GameObject go) {
		RealmComponent rc = RealmComponent.getRealmComponent(go);
		if (rc.isSpell()) {
			return new SpellWrapper(go);
		}
		if (rc instanceof BattleChit) {
			return (BattleChit) rc;
		}
		return null;
	}

	/**
	 * This method finds or creates a RealmComponent based on id
	 * 
	 * @return		RealmComponent or null if not found.
	 */
	public static RealmComponent getRealmComponentFromId(GameData dataSource, String stringId) {
		Hashtable componentHash = getComponentHash(dataSource);
		if (stringId != null && componentHash != null) {
			try {
				Long id = Long.valueOf(stringId);
				GameObject go = (GameObject) dataSource.getGameObject(id);
				if (go!=null) {
					return getRealmComponent(go);
				}
			}
			catch (NumberFormatException ex) {
				// empty
			}
		}
		return null;
	}

	/**
	 * @return Returns the selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected The selected to set.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		repaint();
	}

	public void setGameObject(GameObject gameObject) {
		this.gameObject = gameObject;
	}

	/**
	 * @return			current location on map, or null if not applicable
	 */
	public TileLocation getCurrentLocation() {
		return ClearingUtility.getTileLocation(getGameObject());
	}
	public boolean hasImmunities() {
		return !getImmunities().isEmpty();
	}
	public boolean isImmuneTo(RealmComponent rc) {
		ArrayList list = getImmunities();
		if (!list.isEmpty()) {
			// Make sure we resolve to the monster, not the part!
			if (rc.isMonsterPart()) {
				rc = RealmComponent.getRealmComponent(rc.getGameObject().getHeldBy());
			}
			
			// list will contain monster names like:  Flying Demon, Demon, Imp
			String name = rc.getGameObject().getName();
			if (rc.getGameObject().hasThisAttribute(Constants.BOARD_NUMBER)) {
				name = name.substring(0,name.length()-2);
			}
			return list.contains(name);
		}
		return false;
	}
	private ArrayList getImmunities() {
		ArrayList immunities = new ArrayList();
		if (getGameObject().hasThisAttribute(Constants.MONSTER_IMMUNITY)) {
			immunities.addAll(getGameObject().getThisAttributeList(Constants.MONSTER_IMMUNITY));
		}
		if (isCharacter()) {
			CharacterWrapper character = new CharacterWrapper(getGameObject());
			immunities.addAll(character.getActiveInventoryValuesForThisKey(Constants.MONSTER_IMMUNITY,","));
		}
		if (hasMagicProtection()) {
			immunities.add("Imp");
			immunities.add("Winged Demon");
			immunities.add("Demon");
		}
		return immunities;
	}
	public String getFacing() {
		return getGameObject().getThisAttribute(Constants.FACING_KEY);
	}
	public void setFacing(String val) {
		String currentFacing = gameObject.getThisAttribute(Constants.FACING_KEY);
		if (currentFacing==null || !currentFacing.equals(val)) {
			gameObject.setThisAttribute(Constants.FACING_KEY,val);
		}
	}
	public boolean isPlainSight() {
		return gameObject.hasThisAttribute(Constants.PLAIN_SIGHT);
	}
	public boolean isAtYourFeet(CharacterWrapper character) {
		if (isPlainSight()) {
			String id = gameObject.getThisAttribute(Constants.DROPPED_BY);
			return id!=null && character.getGameObject().getStringId().equals(id);
		}
		return false;
	}
	public void clearAtYourFeet() {
		gameObject.removeThisAttribute(Constants.DROPPED_BY);
	}
	
	public static void reset() {
		if (dataComponentHash!=null) {
			dataComponentHash.clear();
			dataComponentHash = null;
		}
	}
	public boolean affectedByKey(String key) {
		return getGameObject().hasThisAttribute(key)
			|| SpellUtility.affectedByBewitchingSpellKey(getGameObject(),key);
	}
	
	public Strength getWeight() {
		return affectedByKey(Constants.NO_WEIGHT) ? new Strength() : new Strength(getGameObject().getThisAttribute(Constants.WEIGHT));
	}
	
	public String getThisBlock() {
		return getGameObject().hasAttributeBlock("this_h")?"this_h":"this";
	}
	
	/**
	 * This stamps the component with a character timestamp, so it can be easily determined when this item was acquired.
	 */
	public void setCharacterTimestamp(CharacterWrapper character) {
		String key = "a"+character.getGameObject().getStringId();
		if (!gameObject.hasAttribute("timestamp",key)) { // Only stamp if not already stamped
			gameObject.setAttribute("timestamp",key,character.getCurrentDayKey());
		}
	}
	public DayKey getCharacterTimestamp(CharacterWrapper character) {
		String key = "a"+character.getGameObject().getStringId();
		String dayKey = gameObject.getAttribute("timestamp",key);
		return dayKey==null?null:new DayKey(dayKey);
	}
}