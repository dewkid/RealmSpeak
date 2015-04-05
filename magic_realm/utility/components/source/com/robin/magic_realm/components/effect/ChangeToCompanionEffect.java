package com.robin.magic_realm.components.effect;

import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.MonsterCreator;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class ChangeToCompanionEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		GameObject go = context.Spell.getGameObject();
		
		MonsterCreator monsterCreator = new MonsterCreator(Constants.CHANGE_TO_COMPANION);
		GameObject companion = monsterCreator.createOrReuseMonster(go.getGameData());
		monsterCreator.setupGameObject(
				companion,
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"name"),
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"icon_type"),
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"vulnerability"),
				go.hasAttribute(Constants.CHANGE_TO_COMPANION,"armored"),
				go.hasAttribute(Constants.CHANGE_TO_COMPANION,"flying"));
		companion.setThisAttribute("icon_folder",go.getAttribute(Constants.CHANGE_TO_COMPANION,"icon_folder"));
		monsterCreator.setupSide(
				companion,
				"light",
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"strength"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"sharpness"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"attack_speed"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"length"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"move_speed"),
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"chit_color"));
		monsterCreator.setupSide(
				companion,
				"dark",
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"strength"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"sharpness"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"attack_speed"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"length"),
				go.getAttributeInt(Constants.CHANGE_TO_COMPANION,"move_speed"),
				go.getAttribute(Constants.CHANGE_TO_COMPANION,"chit_color"));
				
		CharacterWrapper casterChar = new CharacterWrapper(context.Caster);
		casterChar.addHireling(companion);
		context.Caster.add(companion); // so that you don't have to assign as a follower right away
		CombatWrapper monster = new CombatWrapper(companion);
		monster.setSheetOwner(true);
		
		ArrayList<String> list = go.getThisAttributeList("created");
		if (list==null) {
			list = new ArrayList<String>();
		}
		for(GameObject obj:monsterCreator.getMonstersCreated()) {
			list.add(obj.getStringId());
		}
		go.setThisAttributeList("created",list);
		
		// Finally
		go.add(context.Target.getGameObject()); // move target into spell (since it is being converted)
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		boolean companionDied = false;
		for (GameObject go:SpellUtility.getCreatedCompanions(context.Spell)) {
			if (go.hasThisAttribute(Constants.DEAD)) {
				companionDied = true;
				break;
			}
		}
		SpellUtility.unsummonCompanions(context.Spell);
		
		GameObject caster = context.Spell.getCaster().getGameObject();
		if (companionDied) {
			// Target is destroyed
			RealmLogging.logMessage(caster.getName(),"Lost the "+ context.Target.getGameObject().getName()+".");
			String targetForItem = context.Spell.getGameObject().getThisAttribute(Constants.CHANGE_TO_COMPANION);
			GameObject dwelling = context.Spell.getGameData().getGameObjectByName(targetForItem);
			dwelling.add(context.Target.getGameObject());
		}
		else {
			// Add target back to character
			caster.add(context.Target.getGameObject());
		}
	}

}
