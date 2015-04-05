package com.robin.magic_realm.components.effect;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class AnimateEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		GameObject go = context.Target.getGameObject();
		
		go.setName(Constants.UNDEAD_PREFIX+go.getName());
		go.removeThisAttribute(Constants.DEAD);
		go.setThisAttribute(Constants.UNDEAD);
		go.copyAttributeBlock("light","light_an");
		go.copyAttributeBlock("dark","dark_an");
		go.setAttribute("light","chit_color","white");
		if (go.hasAttribute("light","attack_speed")) {
			go.setAttribute("light","attack_speed",go.getAttributeInt("light","attack_speed")+1);
		}
		go.setAttribute("light","move_speed",go.getAttributeInt("light","move_speed")+1);
		go.setAttribute("dark","chit_color","gray");
		if (go.hasAttribute("dark","attack_speed")) {
			go.setAttribute("dark","attack_speed",go.getAttributeInt("dark","attack_speed")+1);
		}
		go.setAttribute("dark","move_speed",go.getAttributeInt("dark","move_speed")+1);
		
		if (go.getHoldCount()==1) {
			GameObject weapon = (GameObject)go.getHold().get(0);
			weapon.setAttribute("light","attack_speed",weapon.getAttributeInt("light","attack_speed")+1);
			weapon.setAttribute("dark","attack_speed",weapon.getAttributeInt("dark","attack_speed")+1);
		}
		
		CharacterWrapper casterChar = new CharacterWrapper(context.Caster);
		casterChar.addHireling(go);
		context.Caster.add(go); // so that you don't have to assign as a follower right away
		CombatWrapper monster = new CombatWrapper(go);
		monster.setSheetOwner(true);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		GameObject caster = context.Spell.getCaster().getGameObject();
		
		GameObject go = context.Target.getGameObject();
		if (go.hasThisAttribute(Constants.UNDEAD)) { // so it doesn't get stuck in an infinite loop!
			go.setName(go.getName().substring(Constants.UNDEAD_PREFIX.length()));
			go.removeThisAttribute(Constants.UNDEAD);
			go.copyAttributeBlock("light_an","light");
			go.copyAttributeBlock("dark_an","dark");
			go.removeAttributeBlock("light_an");
			go.removeAttributeBlock("dark_an");
			if (go.getHoldCount()==1) {
				GameObject weapon = (GameObject)go.getHold().get(0);
				weapon.setAttribute("light","attack_speed",weapon.getAttributeInt("light","attack_speed")-1);
				weapon.setAttribute("dark","attack_speed",weapon.getAttributeInt("dark","attack_speed")-1);
			}
			
			CharacterWrapper casterChar = new CharacterWrapper(caster);
			casterChar.removeHireling(go);
			
			RealmUtility.makeDead(context.Target);
		}
	}

}
