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
package com.robin.magic_realm.RealmBattle.targeting;

import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.magic_realm.RealmBattle.BattleModel;
import com.robin.magic_realm.RealmBattle.CombatFrame;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.*;

public abstract class SpellTargeting {
	
	protected CombatFrame combatFrame;
	protected SpellWrapper spell;
	
	protected ArrayList<GameObject> gameObjects;
	
	public abstract boolean populate(BattleModel battleModel,RealmComponent activeParticipant);
	public abstract boolean assign(HostPrefWrapper hostPrefs,CharacterWrapper activeCharacter);
	public abstract boolean hasTargets();
	
	protected SpellTargeting(CombatFrame combatFrame,SpellWrapper spell) {
		this.combatFrame = combatFrame;
		this.spell = spell;
		gameObjects = new ArrayList<GameObject>();
	}
	protected boolean allowTargetingHirelings() {
		return combatFrame.allowsTreachery() || (spell.isBenevolent() && combatFrame.getHostPrefs().hasPref(Constants.TE_BENEVOLENT_SPELLS));
	}
	/**
	 * This is the primary access to Spell Targeting
	 */
	public static SpellTargeting getTargeting(CombatFrame combatFrame,SpellWrapper spell) {
		SpellTargeting targeting = null;
		String targetType = spell.getGameObject().getThisAttribute("target");
		if ("multiple".equals(targetType)) {
			targeting = new SpellTargetingOtherOpponents(combatFrame,spell);
		}
		else if ("individual".equals(targetType)) {
			targeting = new SpellTargetingIndividual(combatFrame,spell);
		}
		else if ("individual + hex".equals(targetType)) {
			targeting = new SpellTargetingIndividualPlusHex(combatFrame,spell);
		}
		else if ("attacker".equals(targetType)) {
			targeting = new SpellTargetingAttacker(combatFrame,spell);
		}
		else if ("leader".equals(targetType)) {
			targeting = new SpellTargetingLeader(combatFrame,spell);
		}
		else if ("character".equals(targetType)) {
			targeting = new SpellTargetingCharacter(combatFrame,spell,spell.getGameObject().hasThisAttribute("targetLightOnly"));
		}
		else if ("caster".equals(targetType)) {
			targeting = new SpellTargetingCaster(combatFrame,spell);
		}
		else if ("clearing".equals(targetType)) {
			targeting = new SpellTargetingClearing(combatFrame,spell);
		}
		else if ("monster".equals(targetType)) {
			targeting = new SpellTargetingMonster(combatFrame,spell);
		}
		else if ("sound".equals(targetType)) {
			targeting = new SpellTargetingSound(combatFrame,spell);
		}
		else if ("artifact".equals(targetType)) {
			targeting = new SpellTargetingArtifact(combatFrame,spell);
		}
		else if (targetType.indexOf("spell")>=0 || targetType.indexOf("curse")>=0) {
			targeting = new SpellTargetingSpellOrCurse(combatFrame,spell);
		}
		else if (targetType.startsWith("magic")) {
			targeting = new SpellTargetingMagic(combatFrame,spell);
		}
		else if ("spider, octopus".equals(targetType)) {
			targeting = new SpellTargetingSpiderOctopus(combatFrame,spell);
		}
		else if ("bats".equals(targetType)) {
			targeting = new SpellTargetingBats(combatFrame,spell);
		}
		else if ("wolves".equals(targetType)) {
			targeting = new SpellTargetingWolves(combatFrame,spell);
		}
		else if ("goblins".equals(targetType)) {
			targeting = new SpellTargetingGoblins(combatFrame,spell);
		}
		else if ("human group".equals(targetType)) {
			targeting = new SpellTargetingHumanGroup(combatFrame,spell);
		}
		else if ("dragon".equals(targetType)) {
			targeting = new SpellTargetingDragon(combatFrame,spell);
		}
		else if ("demon".equals(targetType)) {
			targeting = new SpellTargetingDemon(combatFrame,spell);
		}
		else if ("weather".equals(targetType)) {
			targeting = new SpellTargetingWeather(combatFrame,spell);
		}
		else if ("weapon".equals(targetType)) {
			targeting = new SpellTargetingWeapon(combatFrame,spell);
		}
		else if ("tile".equals(targetType)) {
			targeting = new SpellTargetingTile(combatFrame,spell);
		}
		else if ("character,tile".equals(targetType)) {
			// Show a dialog to make a choice here
			ButtonOptionDialog choice = new ButtonOptionDialog(combatFrame,null,"Target which?",spell.getGameObject().getName());
			choice.addSelectionObject("Character");
			choice.addSelectionObject("This Tile");
			choice.setVisible(true);
			String result = (String)choice.getSelectedObject();
			if (result!=null) {
				if ("Character".equals(result)) {
					targeting = new SpellTargetingCharacter(combatFrame,spell,false);
				}
				else {
					targeting = new SpellTargetingTile(combatFrame,spell);
				}
			}
		}
		else if ("caster item".equals(targetType)) {
			targeting = new SpellTargetingMyItem(combatFrame,spell,true,true);
		}
		else if ("caster armor".equals(targetType)) {
			targeting = new SpellTargetingMyArmor(combatFrame,spell);
		}
		else if ("inactive weapon".equals(targetType)) {
			targeting = new SpellTargetingMyWeapon(combatFrame,spell);
		}
		else if ("hurt chits".equals(targetType)) {
			targeting = new SpellTargetingHurtChit(combatFrame,spell);
		}
		else if ("denizen".equals(targetType)) {
			targeting = new SpellTargetingDenizen(combatFrame,spell);
		}
		else if ("active sword".equals(targetType)) {
			targeting = new SpellTargetingActiveWeaponType(combatFrame,spell,"sword",false);
		}
		else if ("monsters".equals(targetType)) {
			targeting = new SpellTargetingMonsters(combatFrame,spell);
		}
		else if ("undead".equals(targetType)) {
			targeting = new SpellTargetingUndead(combatFrame,spell);
		}
		else if ("dead monster".equals(targetType)) {
			targeting = new SpellTargetingDeadMonster(combatFrame,spell);
		}
		else if ("staff".equals(targetType)) {
			targeting = new SpellTargetingActiveWeaponType(combatFrame,spell,"staff",true);
		}
		else if ("color".equals(targetType)) {
			targeting = new SpellTargetingColor(combatFrame,spell);
		}
		else if ("MOVE chit".equals(targetType)) {
			targeting = new SpellTargetingChit(combatFrame,spell,"MOVE");
		}
		else if ("native".equals(targetType)) {
			targeting = new SpellTargetingNative(combatFrame,spell);
		}
		
		return targeting;
	}
		/*
		 * This is the complete list of target possibilities:
		 * 
		 * These are done:
		 * ---------------
		 * character
		 * characterL			one Light character
		 * individual			anyone in the clearing
		 * attacker			anyone in the clearing that is an opponent
		 * leader 			any character, hired leader or controlled monster
		 * multiple 			any in clearing - multiple
		 * magic(II,VIII)
		 * magic(III,VII)
		 * magic(IV,VI)
		 * magic(all)
		 * monster
		 * spell				TEST - Any active spell in the clearing
		 * spell,curse			TEST - One active spell or curse
		 * artifact			TEST - Any artifact or book
		 * sound				One face-up sound chit anywhere on the map
		 * clearing(all)		Everything: monsters,natives,characters,spells
		 * clearing			Monsters,natives,characters
		 * bats				All bats in the clearing
		 * spiderOctopus		Spider or Octopus
		 * dragon				Any dragon
		 * goblins			All goblins in the clearing
		 * humanGroup			any one native group, or all giants, or all ogres
		 * weather			the weather chit
		 * 
		 * Need to test:
		 * ------------
		 * weapon				Select any weapon, native, Goblin, Ogre, or Giant's Club
		 * cave				The cave clearing itself.
		 * demon				A demon
		 * 
		 * Still to do:
		 * ------------
		 * tile 				entire tile
		 * character,tile		either one character or spellcaster tile
		 */
}